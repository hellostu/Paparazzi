package com.hellostu.paparazzi.compiler;

import com.squareup.javapoet.*;
import com.sun.tools.javac.code.Symbol;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by stuartlynch on 20/05/2016.
 */
public class ListenerModel {

    private String                          packageName;
    private String                          className;
    private TypeMirror                      typeMirror;
    private ArrayList<Symbol.MethodSymbol>  methods;

    ///////////////////////////////////////////////////////////////
    // LIFECYCLE
    ///////////////////////////////////////////////////////////////

    public ListenerModel(String packageName, String className, TypeMirror typeMirror) {
        this.className = className;
        this.packageName = packageName;
        this.typeMirror = typeMirror;
        this.methods = new ArrayList<Symbol.MethodSymbol>();
    }

    ///////////////////////////////////////////////////////////////
    // LIFECYCLE
    ///////////////////////////////////////////////////////////////

    public void addListenerMethod(Symbol.MethodSymbol method) {
        methods.add(method);
    }

    public JavaFile makeJavaFile() {
        ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(ClassName.get(ArrayList.class), TypeName.get(typeMirror));

        FieldSpec listenersField = FieldSpec.builder(parameterizedTypeName, "listeners")
                .addModifiers(Modifier.PRIVATE)
                .build();

        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("listeners = new ArrayList<>()")
                .build();

        MethodSpec addListenerMethod = MethodSpec.methodBuilder("addListener")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.get(typeMirror), "listener")
                .addStatement("listeners.add(listener)")
                .build();

        MethodSpec removeListenerMethod = MethodSpec.methodBuilder("removeListener")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.get(typeMirror), "listener")
                .addStatement("listeners.remove(listener)")
                .build();

        String[] classNameElements = className.split("\\.");
        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(classNameElements[classNameElements.length-1] + "s")
                .addSuperinterface(TypeName.get(typeMirror))
                .addField(listenersField)
                .addMethod(constructor)
                .addMethod(addListenerMethod)
                .addMethod(removeListenerMethod);

        for(Symbol.MethodSymbol methodSymbol : methods) {
            MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder(methodSymbol.name.toString())
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class);

            List<Symbol.VarSymbol> params = methodSymbol.params();
            for(Symbol.VarSymbol varSymbol : params) {
                methodSpecBuilder.addParameter(TypeName.get(varSymbol.type), varSymbol.name.toString());
            }

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("listener.");
            stringBuilder.append(methodSymbol.name.toString());
            stringBuilder.append("(");

            for(int i = 0; i < params.size(); i++) {
                Symbol.VarSymbol varSymbol = params.get(i);
                if(i != 0) {
                    stringBuilder.append(", ");
                }
                stringBuilder.append(varSymbol.name);
            }
            stringBuilder.append(")");

            methodSpecBuilder.beginControlFlow("for($L listener : listeners)", className)
                    .addStatement(stringBuilder.toString())
                    .endControlFlow();

            typeSpecBuilder.addMethod(methodSpecBuilder.build());
        }

        TypeSpec typeSpec = typeSpecBuilder.build();

        return JavaFile.builder(packageName, typeSpec).build();
    }

}
