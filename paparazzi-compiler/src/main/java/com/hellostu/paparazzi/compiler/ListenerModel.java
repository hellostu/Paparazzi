package com.hellostu.paparazzi.compiler;

import com.squareup.javapoet.*;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by stuartlynch on 20/05/2016.
 */
public class ListenerModel {

    private String                          packageName;
    private String                          className;
    private TypeMirror                      typeMirror;
    private ArrayList<ExecutableElement>    methods;
    private Reference                       reference;

    ///////////////////////////////////////////////////////////////
    // LIFECYCLE
    ///////////////////////////////////////////////////////////////

    public ListenerModel(String packageName, String className, TypeMirror typeMirror, Reference reference) {
        this.className = className;
        this.packageName = packageName;
        this.typeMirror = typeMirror;
        this.methods = new ArrayList<>();
        this.reference = reference;
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

        String[] classNameElements = className.split("\\.");
        String newClassName = "";
        switch (reference) {
            case STRONG:
                newClassName = classNameElements[classNameElements.length-1] + "s";
                break;
            case WEAK:
                newClassName = "Weak" + classNameElements[classNameElements.length-1] + "s";
                break;
        }
        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(newClassName)
                .addSuperinterface(TypeName.get(typeMirror))
                .addField(listenersField)
                .addMethod(constructor)
                .addMethod(addListenerMethod)
                .addMethod(removeListenerMethod);

        for(ExecutableElement executableElement : methods) {
            MethodListenerImplementation listenerMethod = new MethodListenerImplementation(executableElement, className, "listeners", reference);
            typeSpecBuilder.addMethod(listenerMethod.generateMethodSpec());
        }

        TypeSpec typeSpec = typeSpecBuilder.build();

        return JavaFile.builder(packageName, typeSpec).build();
    }

}
