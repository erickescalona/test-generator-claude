package com.testgenerator.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassInfo {
    private String name;
    private String filePath;
    private boolean isInterface;
    private List<MethodInfo> methods;
}
