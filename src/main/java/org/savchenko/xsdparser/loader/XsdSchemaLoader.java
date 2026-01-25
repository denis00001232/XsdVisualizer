package org.savchenko.xsdparser.loader;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.xsd.*;
import org.eclipse.xsd.util.XSDResourceFactoryImpl;
import org.savchenko.xsdparser.exception.XsdParsingException;

import java.util.List;

public class XsdSchemaLoader {
    
    public XSDElementDeclaration loadRootElement(String xsdFilePath) {
        XSDSchema schema = loadSchema(xsdFilePath);
        return findRootElement(schema);
    }
    
    private XSDSchema loadSchema(String xsdFilePath) {
        ResourceSet resourceSet = createResourceSet();
        Resource resource = resourceSet.getResource(URI.createFileURI(xsdFilePath), true);
        return (XSDSchema) resource.getContents().get(0);
    }
    
    private ResourceSet createResourceSet() {
        ResourceSet resourceSet = new ResourceSetImpl();
        resourceSet.getResourceFactoryRegistry()
            .getExtensionToFactoryMap()
            .put("xsd", new XSDResourceFactoryImpl());
        return resourceSet;
    }
    
    private XSDElementDeclaration findRootElement(XSDSchema schema) {
        List<XSDElementDeclaration> rootElements = schema.getContents().stream()
            .filter(XSDElementDeclaration.class::isInstance)
            .map(XSDElementDeclaration.class::cast)
            .toList();
            
        if (rootElements.isEmpty()) {
            throw new XsdParsingException("No root elements found in schema");
        }
        
        return rootElements.get(0);
    }
}