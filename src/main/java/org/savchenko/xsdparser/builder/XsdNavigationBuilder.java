package org.savchenko.xsdparser.builder;

import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDTypeDefinition;
import org.savchenko.node.XsdNode;
import org.savchenko.xsdparser.exception.XsdParsingException;
import org.savchenko.xsdparser.factory.XsdNodeFactory;
import org.savchenko.xsdparser.context.ParsingContext;
import org.savchenko.xsdparser.parser.XsdElementParser;

import java.util.List;

public class XsdNavigationBuilder {
    private final XsdNodeFactory nodeFactory;
    private final XsdElementParser elementParser;
    
    public XsdNavigationBuilder(XsdNodeFactory nodeFactory, XsdElementParser elementParser) {
        this.nodeFactory = nodeFactory;
        this.elementParser = elementParser;
    }
    
    public List<XsdNode> buildNavigation(XSDElementDeclaration rootElement) {
        XSDComplexTypeDefinition rootType = extractComplexType(rootElement);
        
        ParsingContext context = ParsingContext.forNavigationMode();
        context.addPendingType(rootType);
        
        while (context.hasPendingTypes()) {
            XSDComplexTypeDefinition type = context.pollPendingType();
            XsdNode root = nodeFactory.createRootNode(type);
            context.addNode(root);
            
            elementParser.parseComplexType(type, root, context, true);
        }
        
        return context.getAllNodes();
    }
    
    private XSDComplexTypeDefinition extractComplexType(XSDElementDeclaration element) {
        XSDTypeDefinition type = element.getTypeDefinition();
        
        if (type instanceof XSDComplexTypeDefinition complexType) {
            return complexType;
        }
        
        throw new XsdParsingException("Root element is not a complex type");
    }
}