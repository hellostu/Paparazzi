package com.hellostu.paparazzi.compiler;

import com.hellostu.paparazzi.Executor;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

import static javax.tools.Diagnostic.Kind.ERROR;

/**
 * Created by stuartlynch on 02/12/2016.
 */

public class ListenersJavaGenerator {

    private TypeElement     typeElement;
    private TypeName        typeName;


    ///////////////////////////////////////////////////////////////
    // LIFECYCLE
    ///////////////////////////////////////////////////////////////

    public ListenersJavaGenerator(TypeElement typeElement, Messager messager) throws InvalidClassException {
        this.typeElement = typeElement;
        this.typeName = TypeName.get(typeElement.asType());

        if(typeElement.getKind().isInterface() == false) {
            messager.printMessage(ERROR, "Listener tag only be placed on an Interface type");
            throw new InvalidClassException("Listener tag only be placed on an Interface type");
        }

        if(typeElement.getNestingKind() != NestingKind.TOP_LEVEL) {
            messager.printMessage(ERROR, "Listener tag must be placed on a top level Interface");
            throw new InvalidClassException("Listener tag must be placed on a top level Interface");
        }
    }

    public JavaFile buildJavaFile() {
        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(typeElement.getSimpleName() + "s")
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(typeName)
                .addField(generateListenersFieldItem())
                .addField(generateExecutorFieldItem())
                .addMethod(generateConstructor())
                .addMethod(generateAddListenerMethod())
                .addMethod(generateRemoveListenerMethod())
                .addMethods(generateListenerImplementation());

        //Add Generics
        for(TypeParameterElement t : typeElement.getTypeParameters()) {
            typeSpecBuilder.addTypeVariable(TypeVariableName.get(t));
        }

        String packageName = typeElement.getEnclosingElement().toString();
        if(packageName.startsWith("package ")) {
            packageName = packageName.replace("package ", "");
        }
        return JavaFile.builder(packageName, typeSpecBuilder.build()).build();
    }

    ///////////////////////////////////////////////////////////////
    // Private Methods
    ///////////////////////////////////////////////////////////////

    private FieldSpec generateListenersFieldItem() {
        ClassName arrayListClassName = ClassName.get(ArrayList.class);
        ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(arrayListClassName, typeName);
        return FieldSpec.builder(parameterizedTypeName, "listeners")
                .addModifiers(Modifier.PRIVATE)
                .build();
    }

    private FieldSpec generateExecutorFieldItem() {
        return FieldSpec.builder(Executor.class, "executor", Modifier.PRIVATE).build();
    }

    private MethodSpec generateConstructor() {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Executor.class, "executor")
                .addStatement("this.listeners = new ArrayList<>()")
                .addStatement("this.executor = executor")
                .build();
    }

    private MethodSpec generateAddListenerMethod() {
        return MethodSpec.methodBuilder("add" + typeElement.getSimpleName())
                .addModifiers(Modifier.PUBLIC)
                .addParameter(typeName, "listener")
                .beginControlFlow("for($L storedListener : $L)", typeElement.getSimpleName(), "listeners")
                .beginControlFlow("if(listener == storedListener)")
                .addStatement("return")
                .endControlFlow()
                .endControlFlow()
                .addStatement("$L.add(listener)", "listeners")
                .build();
    }

    private MethodSpec generateRemoveListenerMethod() {
        return MethodSpec.methodBuilder("remove" +  typeElement.getSimpleName())
                .addModifiers(Modifier.PUBLIC)
                .addParameter(typeName, "listener")
                .addStatement("$L.remove(listener)", "listeners")
                .build();
    }

    private Iterable<MethodSpec> generateListenerImplementation() {
        ArrayList<MethodSpec> methodSpecs = new ArrayList<>();
        for(Element element : typeElement.getEnclosedElements()) {
            if(element.getKind() == ElementKind.METHOD) {
                methodSpecs.add(generateListenerMethodImplementation((ExecutableElement)element));
            }
        }
        return methodSpecs;
    }

    private MethodSpec generateListenerMethodImplementation(ExecutableElement executableElement) {
        MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder(executableElement.getSimpleName().toString());
        for (VariableElement element : executableElement.getParameters()) {
            TypeName typeName = TypeName.get(element.asType());
            String varName = element.getSimpleName().toString();
            methodSpecBuilder.addParameter(typeName, varName, Modifier.FINAL);
        }
        methodSpecBuilder.addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .beginControlFlow("for(final $L listener : $L)", typeElement.getSimpleName(), "listeners")
                .beginControlFlow("this.executor.execute(new $T()", Runnable.class)
                .beginControlFlow("public void run()")
                .addStatement(getCallListenerString(executableElement))
                .endControlFlow()
                .endControlFlow()
                .addStatement(")")
                .endControlFlow();
        return methodSpecBuilder.build();
    }

    private String getCallListenerString(ExecutableElement executableElement) {
        List<? extends VariableElement> params = executableElement.getParameters();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("listener" + ".");
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
        return stringBuilder.toString();
    }

}
