package org.savchenko.xsdparser.builder;

import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDTypeDefinition;
import org.savchenko.node.XsdNode;
import org.savchenko.xsdparser.context.ParsingContext;
import org.savchenko.xsdparser.exception.XsdParsingException;
import org.savchenko.xsdparser.factory.XsdNodeBuilderFactory;
import org.savchenko.xsdparser.parser.XsdElementParser;


public class XsdTreeBuilder {
    private final XsdNodeBuilderFactory xsdNodeBuilderFactory;
    private final XsdElementParser elementParser;
    
    public XsdTreeBuilder(XsdNodeBuilderFactory xsdNodeBuilderFactory, XsdElementParser elementParser) {
        this.xsdNodeBuilderFactory = xsdNodeBuilderFactory;
        this.elementParser = elementParser;
    }
    
    public XsdNode buildTree(XSDElementDeclaration rootElement) {
        XSDComplexTypeDefinition complexType = extractComplexType(rootElement);
        
        XsdNode root = xsdNodeBuilderFactory.createBuilder().rootNode(complexType).build();
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