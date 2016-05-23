package com.hellostu.paparazzi.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Modifier;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by stuartlynch on 22/05/2016.
 */
public class FieldListeners {

    private TypeName    listenerClassTypeName;
    private String      fieldName;
    private Reference   reference;

    ///////////////////////////////////////////////////////////////
    // Lifecycle
    ///////////////////////////////////////////////////////////////

    public FieldListeners(TypeName listenerClassTypeName, String fieldName, Reference reference) {
        this.fieldName = fieldName;
        this.listenerClassTypeName = listenerClassTypeName;
        this.reference = reference;
    }

    ///////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////

    public FieldSpec generateFieldSpec() {
        switch (reference) {
            case STRONG:
                return generateStrongFieldSpec();
            case WEAK:
                return generateWeakFieldSpec();
        }
        throw new RuntimeException("Invalid Reference Type");
    }

    ///////////////////////////////////////////////////////////////
    // Private Methods
    ///////////////////////////////////////////////////////////////

    private FieldSpec generateStrongFieldSpec() {
        ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(ClassName.get(ArrayList.class), listenerClassTypeName);
        return FieldSpec.builder(parameterizedTypeName, fieldName)
                .addModifiers(Modifier.PRIVATE)
                .build();
    }

    private FieldSpec generateWeakFieldSpec() {
        ParameterizedTypeName weakTypeName = ParameterizedTypeName.get(ClassName.get(WeakReference.class), listenerClassTypeName);
        ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(ClassName.get(ArrayList.class), weakTypeName);
        return FieldSpec.builder(parameterizedTypeName, fieldName)
                .addModifiers(Modifier.PRIVATE)
                .build();
    }

}
