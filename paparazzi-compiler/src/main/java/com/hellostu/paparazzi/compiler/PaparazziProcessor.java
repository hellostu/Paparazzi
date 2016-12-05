package com.hellostu.paparazzi.compiler;

import com.google.auto.service.AutoService;
import com.hellostu.paparazzi.Listener;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;

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
        return annotationTypes;
    }

    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Listener.class);
        for(Element element : elements) {
            try {
                ListenersJavaGenerator listenersJavaGenerator = new ListenersJavaGenerator((TypeElement)element, messager);
                listenersJavaGenerator.buildJavaFile().writeTo(filer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }

}
