package org.savchenko.xsdparser.utils;

import org.eclipse.xsd.XSDAnnotation;

import java.util.Optional;

public class DocumentationExtractor {
    
    public Optional<String> extract(XSDAnnotation annotation) {
        if (annotation == null) {
            return Optional.empty();
        }
        
        if (annotation.getUserInformation().isEmpty()) {
            return Optional.empty();
        }
        
        return Optional.ofNullable(annotation.getUserInformation().get(0).getTextContent())
            .map(String::trim)
            .filter(s -> !s.isEmpty());
    }
}