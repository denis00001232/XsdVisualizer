package org.savchenko.xsdparser.utils;

import org.eclipse.xsd.XSDComplexTypeDefinition;

public record QualifiedName(String namespace, String localName) {
    
    public static QualifiedName from(XSDComplexTypeDefinition type) {
        return new QualifiedName(
            type.getTargetNamespace(),
            type.getName()
        );
    }
    
    public String toSafeFileName() {
        String sanitized = sanitizeNamespace(namespace) + localName + ".html";
        return sanitized.replaceAll("#", "sharp");
    }
    
    private static String sanitizeNamespace(String ns) {
        if (ns == null || ns.isBlank()) {
            return "";
        }
        
        return ns.replaceFirst("^https?://", "")
                 .replace(".xsd", "")
                 .replaceAll("[\\\\/:*?\"<>|]", "_")      // Windows forbidden chars
                 .replaceAll("[\\x00-\\x1F]", "_")         // Control chars
                 .replaceAll("_+", "_")                    // Collapse underscores
                 .replaceAll("[._ ]+$", "")                // Trim trailing
                 + "_";
    }
    
    @Override
    public String toString() {
        return toSafeFileName();
    }
}