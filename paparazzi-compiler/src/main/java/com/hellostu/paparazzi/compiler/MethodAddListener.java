package com.hellostu.paparazzi.compiler;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Modifier;
import java.lang.ref.WeakReference;
import java.util.Iterator;

/**
 * Created by stuartlynch on 22/05/2016.
 */
public class MethodAddListener {

    private String      listenerClassName;
    private String      shortListenerClassName;
    private TypeName    listenerTypeName;
    private String      listenerListVariableName;
    private Reference   reference;

    ///////////////////////////////////////////////////////////////
    // LIFECYCLE
    ///////////////////////////////////////////////////////////////

    public MethodAddListener(String listenerClassName, TypeName listenerTypeName, String listenerListVariableName, Reference reference) {
        this.listenerClassName = listenerClassName;
        this.listenerTypeName = listenerTypeName;
        this.listenerListVariableName = listenerListVariableName;
        this.reference = reference;

        String[] components = listenerClassName.split("\\.");
        this.shortListenerClassName = components[components.length-1];
    }

    ///////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////

    public MethodSpec generateMethodSpec() {
        switch (reference) {
            case STRONG:
                return generateStrongMethodSpec();
            case WEAK:
                return generateWeakMethodSpec();
        }
        throw new RuntimeException("Invalid Reference Type");
    }

    ///////////////////////////////////////////////////////////////
    // Private Methods
    ///////////////////////////////////////////////////////////////

    private MethodSpec generateStrongMethodSpec() {
        return MethodSpec.methodBuilder("add" + shortListenerClassName)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(listenerTypeName, "listener")
                .beginControlFlow("for($L storedListener : $L)", listenerClassName, listenerListVariableName)
                .beginControlFlow("if(listener == storedListener)")
                .addStatement("return")
                .endControlFlow()
                .endControlFlow()
                .addStatement("$L.add(listener)", listenerListVariableName)
                .build();
    }

    private MethodSpec generateWeakMethodSpec() {
        return MethodSpec.methodBuilder("addWeak" + shortListenerClassName)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(listenerTypeName, "listener")
                .addStatement("boolean shouldAddNew = true")
                .addStatement("$T<$T<$L>> iterator = $L.iterator()", Iterator.class, WeakReference.class, listenerClassName, listenerListVariableName)
                .beginControlFlow("while(iterator.hasNext())")
                .addStatement("$L storedListener = iterator.next().get()", listenerClassName)
                .beginControlFlow("if(storedListener == null)")
                .addStatement("iterator.remove()")
                .nextControlFlow("else if(storedListener == listener)")
                .addStatement("shouldAddNew = false")
                .endControlFlow()
                .endControlFlow()
                .beginControlFlow("if(shouldAddNew)")
                .addStatement("$L.add(new WeakReference(listener))", listenerListVariableName)
                .endControlFlow()
                .build();
    }

}
