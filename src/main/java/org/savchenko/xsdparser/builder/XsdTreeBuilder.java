package org.savchenko.xsdparser.builder;

import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDTypeDefinition;
import org.savchenko.node.XsdNode;
import org.savchenko.xsdparser.context.ParsingContext;
import org.savchenko.xsdparser.exception.XsdParsingException;
import org.savchenko.xsdparser.factory.XsdNodeFactory;
import org.savchenko.xsdparser.parser.XsdElementParser;


public class XsdTreeBuilder {
    private final XsdNodeFactory nodeFactory;
    private final XsdElementParser elementParser;
    
    public XsdTreeBuilder(XsdNodeFactory nodeFactory, XsdElementParser elementParser) {
        this.nodeFactory = nodeFactory;
        this.elementParser = elementParser;
    }
    
    public XsdNode buildTree(XSDElementDeclaration rootElement) {
        XSDComplexTypeDefinition complexType = extractComplexType(rootElement);
        
        XsdNode root = nodeFactory.createRootNode(complexType);
        ParsingContext context = ParsingContext.forTreeMode();
        
        elementParser.parseComplexType(complexType, root, context, true);
        
        return root;
    }
    
    private XSDComplexTypeDefinition extractComplexType(XSDElementDeclaration element) {
        XSDTypeDefinition type = element.getTypeDefinition();
        
        if (type instanceof XSDComplexTypeDefinition complexType) {
            return complexType;
        }
        
        throw new XsdParsingException("Root element is not a complex type");
    }
}