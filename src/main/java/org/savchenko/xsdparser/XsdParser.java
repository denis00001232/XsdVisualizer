package org.savchenko.xsdparser;

import org.eclipse.xsd.XSDElementDeclaration;
import org.savchenko.node.XsdNode;
import org.savchenko.xsdparser.builder.XsdNavigationBuilder;
import org.savchenko.xsdparser.builder.XsdTreeBuilder;
import org.savchenko.xsdparser.builder.XsdNodeBuilder;
import org.savchenko.xsdparser.factory.XsdNodeBuilderFactory;
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
        XsdNodeBuilderFactory xsdNodeBuilderFactory = new XsdNodeBuilderFactory();
        XsdElementParser elementParser = new XsdElementParser(new XsdNodeBuilderFactory());
        
        // Create builders
        this.treeBuilder = new XsdTreeBuilder(xsdNodeBuilderFactory, elementParser);
        this.navigationBuilder = new XsdNavigationBuilder(xsdNodeBuilderFactory, elementParser);
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