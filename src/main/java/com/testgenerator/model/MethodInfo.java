// MethodInfo.java

package com.testgenerator.model;

/**
 * A model class representing method information.
 */
public class MethodInfo {
    private String methodName;
    private String returnType;
    private String[] parameters;

    public MethodInfo(String methodName, String returnType, String[] parameters) {
        this.methodName = methodName;
        this.returnType = returnType;
        this.parameters = parameters;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getReturnType() {
        return returnType;
    }

    public String[] getParameters() {
        return parameters;
    }
}