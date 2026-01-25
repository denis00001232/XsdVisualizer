package org.savchenko.xsdparser.builder;

import org.eclipse.xsd.*;
import org.savchenko.node.XsdNode;
import org.savchenko.node.XsdNodeType;
import org.savchenko.xsdparser.utils.DocumentationExtractor;
import org.savchenko.xsdparser.utils.QualifiedName;


public class XsdNodeBuilder {
    private final DocumentationExtractor docExtractor;

    private final XsdNode node = new XsdNode(null);
    
    public XsdNodeBuilder(DocumentationExtractor docExtractor) {
        this.docExtractor = docExtractor;
    }
    
    public XsdNodeBuilder rootNode(XSDComplexTypeDefinition type) {
        node.setType(XsdNodeType.SCHEMA);
        node.setName("root");
        node.setFileName(QualifiedName.from(type).toSafeFileName());
        return this;
    }
    
    public XsdNodeBuilder elementNode(XSDElementDeclaration element, XSDParticle particle) {
        applyParticle(particle);

        node.setType(XsdNodeType.ELEMENT);
        node.setName(element.getName());
        setDocumentation(node, element.getAnnotation());

        return this;
    }
    
    public XsdNodeBuilder complexTypeNode(XSDComplexTypeDefinition type) {
        node.setType(XsdNodeType.COMPLEX_TYPE);
        node.setName(type.getName());
        node.setTargetNameSpace(type.getTargetNamespace());
        setDocumentation(node, type.getAnnotation());
        return this;
    }
    
    public XsdNodeBuilder simpleTypeNode(XSDSimpleTypeDefinition type) {
        node.setType(XsdNodeType.SIMPLE_TYPE);
        node.setName(type.getName());
        node.setTargetNameSpace(type.getTargetNamespace());
        setDocumentation(node, type.getAnnotation());
        return this;
    }
    
    public XsdNodeBuilder attributeNode(XSDAttributeUse attributeUse) {
        XSDAttributeDeclaration attrDecl = attributeUse.getAttributeDeclaration();
        
        node.setType(XsdNodeType.ATTRIBUTE);
        node.setName(attrDecl.getName());
        setDocumentation(node, attrDecl.getAnnotation());
        
        node.setMinOccurs("0");
        if ("required".equals(attributeUse.getUse().getName())) {
            node.setMinOccurs("1");
        }
        
        return this;
    }
    
    public XsdNodeBuilder attributeGroupNode(XSDAttributeGroupDefinition groupDef) {
        XSDAttributeGroupDefinition resolved = groupDef.getResolvedAttributeGroupDefinition();

        node.setType(XsdNodeType.ATTRIBUTE_GROUP);
        node.setName(resolved.getName());
        setDocumentation(node, resolved.getAnnotation());
        
        return this;
    }

    
    public XsdNodeBuilder modelGroupNode(XSDModelGroup group, XSDParticle particle) {
        applyParticle(particle);
        
        switch (group.getCompositor().getValue()) {
            case XSDCompositor.SEQUENCE -> node.setType(XsdNodeType.SEQUENCE);
            case XSDCompositor.CHOICE -> node.setType(XsdNodeType.CHOICE);
            case XSDCompositor.ALL -> node.setType(XsdNodeType.ALL);
        }
        
        return this;
    }
    
    public XsdNodeBuilder groupDefinitionNode(XSDModelGroupDefinition groupDef, XSDParticle particle) {
        applyParticle(particle);
        XSDModelGroupDefinition resolved = groupDef.getResolvedModelGroupDefinition();

        node.setType(XsdNodeType.GROUP);
        node.setName(groupDef.getName());
        node.setTargetNameSpace(groupDef.getTargetNamespace());
        setDocumentation(node, resolved.getAnnotation());
        
        return this;
    }
    
    public XsdNodeBuilder wildcardNode() {
        node.setType(XsdNodeType.ANY);
        return this;
    }
    
    public XsdNodeBuilder baseNode() {
        node.setType(XsdNodeType.BASE);
        return this;
    }
    
    private void setDocumentation(XsdNode node, XSDAnnotation annotation) {
        docExtractor.extract(annotation).ifPresent(node::setDocumentation);
    }

    private void applyParticle(XSDParticle particle) {
        node.setMinOccurs(String.valueOf(particle.getMinOccurs()));
        node.setMaxOccurs(particle.getMaxOccurs() == -1 ? "unbounded" : String.valueOf(particle.getMaxOccurs()));
    }

    public XsdNodeBuilder parentNode(XsdNode parent) {
        node.setParentNode(parent);
        return this;
    }

    public XsdNode build() {
        return node;
    }
}