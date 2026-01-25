package org.savchenko.xsdparser.parser;

import org.eclipse.xsd.*;
import org.savchenko.node.XsdNode;
import org.savchenko.xsdparser.exception.XsdParsingException;
import org.savchenko.xsdparser.builder.XsdNodeBuilder;
import org.savchenko.xsdparser.context.ParsingContext;
import org.savchenko.xsdparser.factory.XsdNodeBuilderFactory;
import org.savchenko.xsdparser.utils.QualifiedName;

public class XsdElementParser {
    private final XsdNodeBuilderFactory nodeBuilderFactory;
    
    public XsdElementParser(XsdNodeBuilderFactory nodeBuilderFactory) {
        this.nodeBuilderFactory = nodeBuilderFactory;
    }
    
    public void parseElement(XSDElementDeclaration element, XsdNode parent, ParsingContext ctx, XSDParticle xsdParticle) {
        XsdNode node = nodeBuilderFactory.createBuilder()
                .parentNode(parent)
                .elementNode(element, xsdParticle)
                .build();
        parent.getChildren().add(node);
        
        XSDTypeDefinition type = element.getTypeDefinition();
        
        if (type instanceof XSDComplexTypeDefinition complexType) {
            parseComplexType(complexType, node, ctx, false);
        } else if (type instanceof XSDSimpleTypeDefinition simpleType) {
            parseSimpleType(simpleType, node, ctx);
        }
    }
    
    public void parseComplexType(XSDComplexTypeDefinition type, XsdNode parent, ParsingContext ctx, boolean isRoot) {
        XsdNode node = nodeBuilderFactory.createBuilder()
                .parentNode(parent)
                .complexTypeNode(type)
                .build();
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
        XsdNode node = nodeBuilderFactory.createBuilder()
                .parentNode(parent)
                .simpleTypeNode(type)
                .build();
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
        XsdNode node = nodeBuilderFactory.createBuilder()
                .parentNode(parent)
                .attributeNode(attrUse)
                .build();
        parent.getChildren().add(node);
        
        // Parse attribute type
        XSDSimpleTypeDefinition attrType = attrUse.getAttributeDeclaration().getTypeDefinition();
        parseSimpleType(attrType, node, ctx);
    }
    
    private void parseAttributeGroup(XSDAttributeGroupDefinition groupDef, XsdNode parent, ParsingContext ctx) {
        XsdNode node = nodeBuilderFactory.createBuilder()
                .parentNode(parent)
                .attributeGroupNode(groupDef)
                .build();
        parent.getChildren().add(node);
        
        XSDAttributeGroupDefinition resolved = groupDef.getResolvedAttributeGroupDefinition();
        resolved.getAttributeUses().forEach(attrUse -> parseAttributeUse(attrUse, node, ctx));
    }
    
    private void parseBaseType(XSDComplexTypeDefinition type, XsdNode parent, ParsingContext ctx) {
        XSDTypeDefinition baseType = type.getBaseTypeDefinition();
        
        if (baseType != null && !"anyType".equals(baseType.getName())) {
            XsdNode baseNode = nodeBuilderFactory.createBuilder()
                    .parentNode(parent)
                    .baseNode()
                    .build();
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
        
        XSDTerm term = particle.getTerm();
        
        if (term instanceof XSDElementDeclaration element) {
            parseElement(element, parent, ctx, particle);
        }
        else if (term instanceof XSDModelGroup group) {
            if (group.eContainer() instanceof XSDModelGroupDefinition groupDef) {
                parseModelGroupDefinition(groupDef, parent, ctx, particle);
            } else {
                parseModelGroup(group, parent, ctx, particle);
            }
        }
        else if (term instanceof XSDWildcard) {
            parseWildcard(parent, ctx);
        }
    }
    
    private void parseModelGroup(XSDModelGroup group, XsdNode parent, ParsingContext ctx, XSDParticle xsdParticle) {
        XsdNode node = nodeBuilderFactory.createBuilder()
                .parentNode(parent)
                .modelGroupNode(group, xsdParticle)
                .build();
        parent.getChildren().add(node);

        
        for (XSDParticle particle : group.getParticles()) {
            parseParticle(particle, node, ctx);
        }
    }
    
    private void parseModelGroupDefinition(XSDModelGroupDefinition groupDef, XsdNode parent, ParsingContext ctx,
                                           XSDParticle xsdParticle) {
        XsdNode node = nodeBuilderFactory.createBuilder()
                .parentNode(parent)
                .groupDefinitionNode(groupDef, xsdParticle)
                .build();
        parent.getChildren().add(node);
        
        XSDModelGroupDefinition resolved = groupDef.getResolvedModelGroupDefinition();
        parseModelGroup(resolved.getModelGroup(), node, ctx, xsdParticle);
    }
    
    private void parseWildcard(XsdNode parent, ParsingContext ctx) {
        XsdNode node = nodeBuilderFactory.createBuilder()
                .parentNode(parent)
                .wildcardNode()
                .build();
        parent.getChildren().add(node);
    }
}