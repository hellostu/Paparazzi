package com.hellostu.paparazzi.compiler;

import com.google.auto.service.AutoService;
import com.hellostu.paparazzi.Listener;
import com.sun.tools.javac.code.Symbol;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import static javax.tools.Diagnostic.Kind.ERROR;

/**
 * Created by stuartlynch on 20/05/2016.
 */
@AutoService(Processor.class)
public final class PaparazziProcessor extends AbstractProcessor {

    private Filer       filer;
    private Messager    messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(Listener.class.getName());
    }

    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Listener.class);

        for(Element element : elements) {
            if(element instanceof TypeElement) {
                try {
                    processTypeElement((TypeElement)element).makeJavaFile().writeTo(filer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    private ListenerModel processTypeElement(TypeElement typeElement) {
        if(typeElement.getKind().isInterface() == false) {
            messager.printMessage(ERROR, "Listener tag only be placed on an Interface type");
            return null;
        }

        String[] classNameElements = typeElement.getQualifiedName().toString().split("\\.");
        StringBuilder stringBuilder = new StringBuilder();
        for(int i=0; i < classNameElements.length-1; i++) {
            if(stringBuilder.length() != 0) {
                stringBuilder.append(".");
            }
            stringBuilder.append(classNameElements[i]);
        }

        String className = typeElement.getQualifiedName().toString();
        TypeElement parentElement = typeElement;
        while(parentElement.getNestingKind().isNested()) {
            parentElement = (TypeElement)parentElement.getEnclosingElement();
        }
        String packageName = parentElement.getEnclosingElement().toString();
        className = className.substring(packageName.length() + 1);
        System.out.println(className);
        System.out.println(packageName);

        ListenerModel listenerModel = new ListenerModel(packageName, className, typeElement.asType());
        for(Element element : typeElement.getEnclosedElements()) {
            if(element.getKind() == ElementKind.METHOD) {
                Symbol.MethodSymbol methodSymbol = (Symbol.MethodSymbol) element;
                listenerModel.addListenerMethod(methodSymbol);
            }
        }
        return listenerModel;
    }

}
