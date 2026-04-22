package com.testgenerator.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MethodInfo {
    private String name;
    private String signature;
    private String returnType;
    private boolean isPublic;
    private boolean isStatic;
}
