package com.hellostu.paparazzi.compiler;

import com.google.auto.service.AutoService;
import com.hellostu.paparazzi.Listener;
import com.hellostu.paparazzi.WeakListener;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
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
        Set<String> annotationTypes = new LinkedHashSet<>();
        annotationTypes.add(Listener.class.getCanonicalName());
        annotationTypes.add(WeakListener.class.getCanonicalName());
        return annotationTypes;
    }

    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Listener.class);
        for(Element element : elements) {
            if(element instanceof TypeElement) {
                try {
                    processTypeElement((TypeElement)element, Reference.STRONG).makeJavaFile().writeTo(filer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        elements = roundEnv.getElementsAnnotatedWith(WeakListener.class);
        for(Element element : elements) {
            if(element instanceof TypeElement) {
                try {
                    processTypeElement((TypeElement)element, Reference.WEAK).makeJavaFile().writeTo(filer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    private ListenerModel processTypeElement(TypeElement typeElement, Reference reference) {
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
        List<? extends TypeParameterElement> generics = typeElement.getTypeParameters();

        TypeElement parentElement = typeElement;
        while(parentElement.getNestingKind().isNested()) {
            parentElement = (TypeElement)parentElement.getEnclosingElement();
        }
        String packageName = parentElement.getEnclosingElement().toString();
        className = className.substring(packageName.length() + 1);

        ListenerModel listenerModel = new ListenerModel(packageName, className, generics, typeElement.asType(), reference);
        for(Element element : typeElement.getEnclosedElements()) {
            if(element.getKind() == ElementKind.METHOD) {
                ExecutableElement executableElement = (ExecutableElement) element;
                listenerModel.addListenerMethod(executableElement);
            }
        }
        return listenerModel;
    }

}
