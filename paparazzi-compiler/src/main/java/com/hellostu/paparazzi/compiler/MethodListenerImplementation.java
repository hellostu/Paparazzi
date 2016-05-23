package com.hellostu.paparazzi.compiler;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import java.util.List;

/**
 * Created by stuartlynch on 22/05/2016.
 */
public class MethodListenerImplementation {

    private ExecutableElement   executableElement;
    private String              listenerClassName;
    private String              listenerListVariableName;
    private Reference           reference;

    ///////////////////////////////////////////////////////////////
    // LIFECYCLE
    ///////////////////////////////////////////////////////////////

    public MethodListenerImplementation(ExecutableElement executableElement, String listenerClassName, String listenerListVariableName, Reference reference) {
        this.executableElement = executableElement;
        this.listenerClassName = listenerClassName;
        this.listenerListVariableName = listenerListVariableName;
        this.reference = reference;
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
        throw new RuntimeException("Invalid Reference");
    }

    ///////////////////////////////////////////////////////////////
    // Private Methods
    ///////////////////////////////////////////////////////////////

    private MethodSpec generateStrongMethodSpec() {
        MethodSpec.Builder methodSpecBuilder = methodSpecBuilder();
        methodSpecBuilder.beginControlFlow("for($L listener : $L)", listenerClassName, listenerListVariableName)
                .addStatement(getCallListenerString())
                .endControlFlow();
        return methodSpecBuilder.build();
    }

    private MethodSpec generateWeakMethodSpec() {
        return methodSpecBuilder()
                .addStatement("Iterator<WeakReference<$L>> iterator = $L.iterator()", listenerClassName, listenerListVariableName)
                .beginControlFlow("while(iterator.hasNext())")
                .addStatement("$L listener = iterator.next().get()", listenerClassName)
                .beginControlFlow("if(listener != null)")
                .addStatement(getCallListenerString())
                .nextControlFlow("else")
                .addStatement("iterator.remove()")
                .endControlFlow()
                .endControlFlow()
                .build();
    }

    private MethodSpec.Builder methodSpecBuilder() {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(executableElement.getSimpleName().toString())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class);

        List<? extends VariableElement> params = executableElement.getParameters();
        for(VariableElement variableElement : params) {
            methodBuilder.addParameter(TypeName.get(variableElement.asType()), variableElement.getSimpleName().toString());
        }

        return methodBuilder;
    }

    private String getCallListenerString() {
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
