package org.savchenko.xsdparser;

import org.eclipse.xsd.XSDElementDeclaration;
import org.savchenko.node.XsdNode;
import org.savchenko.xsdparser.builder.XsdNavigationBuilder;
import org.savchenko.xsdparser.builder.XsdTreeBuilder;
import org.savchenko.xsdparser.factory.XsdNodeFactory;
import org.savchenko.xsdparser.loader.XsdSchemaLoader;
import org.savchenko.xsdparser.parser.XsdElementParser;
import org.savchenko.xsdparser.utils.DocumentationExtractor;

import java.util.List;

public class XsdParser {
    private final XsdSchemaLoader schemaLoader;
    private final XsdTreeBuilder treeBuilder;
    private final XsdNavigationBuilder navigationBuilder;
    
    public XsdParser() {
        // Create dependencies
        this.schemaLoader = new XsdSchemaLoader();
        DocumentationExtractor docExtractor = new DocumentationExtractor();
        XsdNodeFactory nodeFactory = new XsdNodeFactory(docExtractor);
        XsdElementParser elementParser = new XsdElementParser(nodeFactory);
        
        // Create builders
        this.treeBuilder = new XsdTreeBuilder(nodeFactory, elementParser);
        this.navigationBuilder = new XsdNavigationBuilder(nodeFactory, elementParser);
    }
    
    public XsdNode parseAsTree(String xsdFilePath) {
        XSDElementDeclaration root = schemaLoader.loadRootElement(xsdFilePath);
        return treeBuilder.buildTree(root);
    }
    
    public List<XsdNode> parseAsNavigation(String xsdFilePath) {
        XSDElementDeclaration root = schemaLoader.loadRootElement(xsdFilePath);
        return navigationBuilder.buildNavigation(root);
    }
}