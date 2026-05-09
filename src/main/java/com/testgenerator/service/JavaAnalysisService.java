// JavaAnalysisService.java

package com.testgenerator.service;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.testgenerator.model.ClassInfo;
import com.testgenerator.model.MethodInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class JavaAnalysisService {
    
    /**
     * Analiza un archivo Java y extrae información de clases y métodos
     */
    public List<ClassInfo> analyzeJavaFile(String filePath, String javaCode) {
        List<ClassInfo> classes = new ArrayList<>();

        try {
            CompilationUnit cu = StaticJavaParser.parse(javaCode);

            cu.findAll(ClassOrInterfaceDeclaration.class).forEach(classDecl -> {
                ClassInfo classInfo = new ClassInfo();
                classInfo.setName(classDecl.getNameAsString());
                classInfo.setFilePath(filePath);
//                classInfo.setIsInterface(classDecl.isInterface());

                List<MethodInfo> methods = new ArrayList<>();

                classDecl.getMethods().forEach(method -> {
                    MethodInfo methodInfo = new MethodInfo();
                    methodInfo.setName(method.getNameAsString());
                    methodInfo.setSignature(method.getSignature().toString());
                    methodInfo.setReturnType(method.getType().asString());
//                    methodInfo.setIsPublic(method.isPublic());
//                    methodInfo.setIsStatic(method.isStatic());

                    methods.add(methodInfo);
                });

                classInfo.setMethods(methods);
                classes.add(classInfo);

                log.debug("Clase analizada: {} con {} métodos", 
                    classInfo.getName(), methods.size());
            });

        } catch (Exception e) {
            log.error("Error analizando archivo: {}", filePath, e);
        }

        return classes;
    }

    /**
     * Calcula estimación de cobertura de pruebas
     */
    public int estimateCoverage(List<MethodInfo> methods) {
        if (methods.isEmpty()) return 0;

        // Estimación simple: métodos públicos que no son getters/setters
        long complexMethods = methods.stream()
            .filter(m -> m.isPublic() && !isSimpleGetter(m))
            .count();

        return (int) ((complexMethods / (double) methods.size()) * 100);
    }

    private boolean isSimpleGetter(MethodInfo method) {
        return (method.getName().startsWith("get") || 
                method.getName().startsWith("is")) &&
               method.getSignature().contains("()");
    }
}
