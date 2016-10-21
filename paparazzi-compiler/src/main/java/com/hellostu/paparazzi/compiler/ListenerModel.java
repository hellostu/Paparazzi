package com.hellostu.paparazzi.compiler;

import com.squareup.javapoet.*;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by stuartlynch on 20/05/2016.
 */
public class ListenerModel {

    private String                                  packageName;
    private String                                  className;
    private TypeMirror                              typeMirror;
    private ArrayList<ExecutableElement>            methods;
    private Reference                               reference;
    private List<? extends TypeParameterElement>    generics;

    ///////////////////////////////////////////////////////////////
    // LIFECYCLE
    ///////////////////////////////////////////////////////////////

    public ListenerModel(String packageName, String className, List<? extends TypeParameterElement> generics, TypeMirror typeMirror, Reference reference) {
        this.className = className;
        this.packageName = packageName;
        this.typeMirror = typeMirror;
        this.methods = new ArrayList<>();
        this.reference = reference;
        this.generics = generics;
    }

    ///////////////////////////////////////////////////////////////
    // LIFECYCLE
    ///////////////////////////////////////////////////////////////

    public void addListenerMethod(ExecutableElement executableElement) {
        methods.add(executableElement);
    }

    public JavaFile makeJavaFile() {
        String listenersListVariableName = "listeners";
        FieldSpec listenersField = new FieldListeners(TypeName.get(typeMirror), listenersListVariableName, reference)
                .generateFieldSpec();

        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("listeners = new ArrayList<>()")
                .build();

        MethodSpec addListenerMethod = new MethodAddListener(className, TypeName.get(typeMirror), listenersListVariableName, reference)
                .generateMethodSpec();

        MethodSpec removeListenerMethod = new MethodRemoveListener(className, TypeName.get(typeMirror), listenersListVariableName, reference)
                .generateMethodSpec();

        String newClassName = "";
        switch (reference) {
            case STRONG:
                newClassName = className.replace('.', '_') + "s";
                break;
            case WEAK:
                String[] components = className.split("\\.");
                newClassName = "Weak" + components[components.length-1] + "s";
                components[components.length - 1] = newClassName;
                newClassName = "";
                for(int i=0; i< components.length; i++) {
                    if(newClassName.equals("") == false) {
                        newClassName += "_";
                    }
                    newClassName += components[i];
                }
                break;
        }

        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(newClassName)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(TypeName.get(typeMirror))
                .addField(listenersField)
                .addMethod(constructor)
                .addMethod(addListenerMethod)
                .addMethod(removeListenerMethod);

        for(TypeParameterElement t : generics) {
            typeSpecBuilder.addTypeVariable(TypeVariableName.get(t));
        }

        for(ExecutableElement executableElement : methods) {
            MethodListenerImplementation listenerMethod = new MethodListenerImplementation(executableElement, className, "listeners", reference);
            typeSpecBuilder.addMethod(listenerMethod.generateMethodSpec());
        }

        TypeSpec typeSpec = typeSpecBuilder.build();

        return JavaFile.builder(packageName, typeSpec).build();
    }

}
