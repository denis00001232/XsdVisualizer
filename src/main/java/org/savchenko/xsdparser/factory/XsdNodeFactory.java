package org.savchenko.xsdparser.factory;

import org.eclipse.xsd.*;
import org.savchenko.node.XsdNode;
import org.savchenko.node.XsdNodeType;
import org.savchenko.xsdparser.utils.DocumentationExtractor;
import org.savchenko.xsdparser.utils.QualifiedName;


public class XsdNodeFactory {
    private final DocumentationExtractor docExtractor;
    
    public XsdNodeFactory(DocumentationExtractor docExtractor) {
        this.docExtractor = docExtractor;
    }
    
    public XsdNode createRootNode(XSDComplexTypeDefinition type) {
        XsdNode node = new XsdNode(null);
        node.setType(XsdNodeType.SCHEMA);
        node.setName("root");
        node.setFileName(QualifiedName.from(type).toSafeFileName());
        return node;
    }
    
    public XsdNode createElementNode(XSDElementDeclaration element, XsdNode parent) {
        XsdNode node = new XsdNode(parent);
        node.setType(XsdNodeType.ELEMENT);
        node.setName(element.getName());
        setDocumentation(node, element.getAnnotation());
        return node;
    }
    
    public XsdNode createComplexTypeNode(XSDComplexTypeDefinition type, XsdNode parent) {
        XsdNode node = new XsdNode(parent);
        node.setType(XsdNodeType.COMPLEX_TYPE);
        node.setName(type.getName());
        node.setTargetNameSpace(type.getTargetNamespace());
        setDocumentation(node, type.getAnnotation());
        return node;
    }
    
    public XsdNode createSimpleTypeNode(XSDSimpleTypeDefinition type, XsdNode parent) {
        XsdNode node = new XsdNode(parent);
        node.setType(XsdNodeType.SIMPLE_TYPE);
        node.setName(type.getName());
        node.setTargetNameSpace(type.getTargetNamespace());
        setDocumentation(node, type.getAnnotation());
        return node;
    }
    
    public XsdNode createAttributeNode(XSDAttributeUse attributeUse, XsdNode parent) {
        XsdNode node = new XsdNode(parent);
        XSDAttributeDeclaration attrDecl = attributeUse.getAttributeDeclaration();
        
        node.setType(XsdNodeType.ATTRIBUTE);
        node.setName(attrDecl.getName());
        setDocumentation(node, attrDecl.getAnnotation());
        
        node.setMinOccurs("0");
        if ("required".equals(attributeUse.getUse().getName())) {
            node.setMinOccurs("1");
        }
        
        return node;
    }
    
    public XsdNode createAttributeGroupNode(XSDAttributeGroupDefinition groupDef, XsdNode parent) {
        XSDAttributeGroupDefinition resolved = groupDef.getResolvedAttributeGroupDefinition();
        
        XsdNode node = new XsdNode(parent);
        node.setType(XsdNodeType.ATTRIBUTE_GROUP);
        node.setName(resolved.getName());
        setDocumentation(node, resolved.getAnnotation());
        
        return node;
    }
    
    public XsdNode createParticleNode(XSDParticle particle, XsdNode parent) {
        XsdNode node = new XsdNode(parent);
        node.setMinOccurs(String.valueOf(particle.getMinOccurs()));
        node.setMaxOccurs(particle.getMaxOccurs() == -1 ? "unbounded" : String.valueOf(particle.getMaxOccurs()));
        return node;
    }
    
    public XsdNode createModelGroupNode(XSDModelGroup group, XsdNode parent) {
        XsdNode node = new XsdNode(parent);
        
        switch (group.getCompositor().getValue()) {
            case XSDCompositor.SEQUENCE -> node.setType(XsdNodeType.SEQUENCE);
            case XSDCompositor.CHOICE -> node.setType(XsdNodeType.CHOICE);
            case XSDCompositor.ALL -> node.setType(XsdNodeType.ALL);
        }
        
        return node;
    }
    
    public XsdNode createGroupDefinitionNode(XSDModelGroupDefinition groupDef, XsdNode parent) {
        XSDModelGroupDefinition resolved = groupDef.getResolvedModelGroupDefinition();
        
        XsdNode node = new XsdNode(parent);
        node.setType(XsdNodeType.GROUP);
        node.setName(groupDef.getName());
        node.setTargetNameSpace(groupDef.getTargetNamespace());
        setDocumentation(node, resolved.getAnnotation());
        
        return node;
    }
    
    public XsdNode createWildcardNode(XsdNode parent) {
        XsdNode node = new XsdNode(parent);
        node.setType(XsdNodeType.ANY);
        return node;
    }
    
    public XsdNode createBaseNode(XsdNode parent) {
        XsdNode node = new XsdNode(parent);
        node.setType(XsdNodeType.BASE);
        return node;
    }
    
    private void setDocumentation(XsdNode node, XSDAnnotation annotation) {
        docExtractor.extract(annotation).ifPresent(node::setDocumentation);
    }
}