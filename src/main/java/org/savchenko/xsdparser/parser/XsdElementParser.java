package org.savchenko.xsdparser.parser;

import org.eclipse.xsd.*;
import org.savchenko.node.XsdNode;
import org.savchenko.xsdparser.exception.XsdParsingException;
import org.savchenko.xsdparser.factory.XsdNodeFactory;
import org.savchenko.xsdparser.context.ParsingContext;
import org.savchenko.xsdparser.utils.QualifiedName;

public class XsdElementParser {
    private final XsdNodeFactory nodeFactory;
    
    public XsdElementParser(XsdNodeFactory nodeFactory) {
        this.nodeFactory = nodeFactory;
    }
    
    public void parseElement(XSDElementDeclaration element, XsdNode parent, ParsingContext ctx) {
        XsdNode node = nodeFactory.createElementNode(element, parent);
        parent.getChildren().add(node);
        
        XSDTypeDefinition type = element.getTypeDefinition();
        
        if (type instanceof XSDComplexTypeDefinition complexType) {
            parseComplexType(complexType, node, ctx, false);
        } else if (type instanceof XSDSimpleTypeDefinition simpleType) {
            parseSimpleType(simpleType, node, ctx);
        }
    }
    
    public void parseComplexType(XSDComplexTypeDefinition type, XsdNode parent, ParsingContext ctx, boolean isRoot) {
        XsdNode node = nodeFactory.createComplexTypeNode(type, parent);
        parent.getChildren().add(node);
        
        // Handle named complex types
        if (type.getName() != null) {
            QualifiedName qname = QualifiedName.from(type);
            String qnameStr = qname.toSafeFileName();
            
            // Navigation mode: create link to separate tree
            if (ctx.isNavigationMode() && !isRoot) {
                node.setLinkToChild(qnameStr);
                if (ctx.markAsProcessed(qname)) {
                    ctx.addPendingType(type);
                }
                return;
            }
            
            // Tree mode: check for recursion
            if (ctx.isTreeMode()) {
                if (node.isChainAlreadyHasQName(qnameStr)) {
                    node.setRecursive(true);
                    return;
                }
                node.setQName(qnameStr);
            }
        }
        
        // Parse attributes
        parseAttributes(type, node, ctx);
        
        // Parse base type
        parseBaseType(type, node, ctx);
        
        // Parse content
        parseContent(type, node, ctx);
    }
    
    private void parseSimpleType(XSDSimpleTypeDefinition type, XsdNode parent, ParsingContext ctx) {
        XsdNode node = nodeFactory.createSimpleTypeNode(type, parent);
        parent.getChildren().add(node);
    }
    
    private void parseAttributes(XSDComplexTypeDefinition type, XsdNode parent, ParsingContext ctx) {
        for (XSDAttributeGroupContent content : type.getAttributeContents()) {
            if (content instanceof XSDAttributeUse attrUse) {
                parseAttributeUse(attrUse, parent, ctx);
            } else if (content instanceof XSDAttributeGroupDefinition groupDef) {
                parseAttributeGroup(groupDef, parent, ctx);
            }
        }
    }
    
    private void parseAttributeUse(XSDAttributeUse attrUse, XsdNode parent, ParsingContext ctx) {
        XsdNode node = nodeFactory.createAttributeNode(attrUse, parent);
        parent.getChildren().add(node);
        
        // Parse attribute type
        XSDSimpleTypeDefinition attrType = attrUse.getAttributeDeclaration().getTypeDefinition();
        parseSimpleType(attrType, node, ctx);
    }
    
    private void parseAttributeGroup(XSDAttributeGroupDefinition groupDef, XsdNode parent, ParsingContext ctx) {
        XsdNode node = nodeFactory.createAttributeGroupNode(groupDef, parent);
        parent.getChildren().add(node);
        
        XSDAttributeGroupDefinition resolved = groupDef.getResolvedAttributeGroupDefinition();
        resolved.getAttributeUses().forEach(attrUse -> parseAttributeUse(attrUse, node, ctx));
    }
    
    private void parseBaseType(XSDComplexTypeDefinition type, XsdNode parent, ParsingContext ctx) {
        XSDTypeDefinition baseType = type.getBaseTypeDefinition();
        
        if (baseType != null && !"anyType".equals(baseType.getName())) {
            XsdNode baseNode = nodeFactory.createBaseNode(parent);
            parent.getChildren().add(baseNode);
            
            if (baseType instanceof XSDSimpleTypeDefinition simpleType) {
                parseSimpleType(simpleType, baseNode, ctx);
            } else if (baseType instanceof XSDComplexTypeDefinition complexType) {
                parseComplexType(complexType, baseNode, ctx, false);
            } else {
                throw new XsdParsingException("Unknown base type: " + baseType.getClass());
            }
        }
    }
    
    private void parseContent(XSDComplexTypeDefinition type, XsdNode parent, ParsingContext ctx) {
        XSDComplexTypeContent content = type.getContent();
        
        if (content instanceof XSDParticle particle) {
            parseParticle(particle, parent, ctx);
        }
    }
    
    public void parseParticle(XSDParticle particle, XsdNode parent, ParsingContext ctx) {
        XsdNode node = nodeFactory.createParticleNode(particle, parent);
        parent.getChildren().add(node);
        
        XSDTerm term = particle.getTerm();
        
        if (term instanceof XSDElementDeclaration element) {
            parseElement(element, node, ctx);
        }
        else if (term instanceof XSDModelGroup group) {
            if (group.eContainer() instanceof XSDModelGroupDefinition groupDef) {
                parseModelGroupDefinition(groupDef, node, ctx);
            } else {
                parseModelGroup(group, node, ctx);
            }
        }
        else if (term instanceof XSDWildcard) {
            parseWildcard(node, ctx);
        }
    }
    
    private void parseModelGroup(XSDModelGroup group, XsdNode parent, ParsingContext ctx) {
        XsdNode node = nodeFactory.createModelGroupNode(group, parent);
        parent.getChildren().add(node);
        
        for (XSDParticle particle : group.getParticles()) {
            parseParticle(particle, node, ctx);
        }
    }
    
    private void parseModelGroupDefinition(XSDModelGroupDefinition groupDef, XsdNode parent, ParsingContext ctx) {
        XsdNode node = nodeFactory.createGroupDefinitionNode(groupDef, parent);
        parent.getChildren().add(node);
        
        // Create child node for the model group content
        XsdNode childNode = new XsdNode(node);
        node.getChildren().add(childNode);
        
        XSDModelGroupDefinition resolved = groupDef.getResolvedModelGroupDefinition();
        parseModelGroup(resolved.getModelGroup(), childNode, ctx);
    }
    
    private void parseWildcard(XsdNode parent, ParsingContext ctx) {
        XsdNode node = nodeFactory.createWildcardNode(parent);
        parent.getChildren().add(node);
    }
}