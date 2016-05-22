package com.hellostu.paparazzi.compiler;

import com.squareup.javapoet.*;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
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
    private ArrayList<ExecutableElement>    methods;

    ///////////////////////////////////////////////////////////////
    // LIFECYCLE
    ///////////////////////////////////////////////////////////////

    public ListenerModel(String packageName, String className, TypeMirror typeMirror) {
        this.className = className;
        this.packageName = packageName;
        this.typeMirror = typeMirror;
        this.methods = new ArrayList<ExecutableElement>();
    }

    ///////////////////////////////////////////////////////////////
    // LIFECYCLE
    ///////////////////////////////////////////////////////////////

    public void addListenerMethod(ExecutableElement executableElement) {
        methods.add(executableElement);
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

        for(ExecutableElement executableElement : methods) {
            MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder(executableElement.getSimpleName().toString())
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class);

            List<? extends VariableElement> params = executableElement.getParameters();
            for(VariableElement variableElement : params) {
                methodSpecBuilder.addParameter(TypeName.get(variableElement.asType()), variableElement.getSimpleName().toString());
            }

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("listener.");
            stringBuilder.append(executableElement.getSimpleName().toString());
            stringBuilder.append("(");

            for(int i = 0; i < params.size(); i++) {
                VariableElement variableElement = params.get(i);
                if(i != 0) {
                    stringBuilder.append(", ");
                }
                stringBuilder.append(variableElement.getSimpleName());
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
