package org.savchenko.xsdparser.factory;

import org.savchenko.xsdparser.builder.XsdNodeBuilder;
import org.savchenko.xsdparser.utils.DocumentationExtractor;

public class XsdNodeBuilderFactory {

    private DocumentationExtractor documentationExtractor = new DocumentationExtractor();

    public XsdNodeBuilderFactory(DocumentationExtractor documentationExtractor) {
        this.documentationExtractor = documentationExtractor;
    }

    public XsdNodeBuilderFactory() {}

    public XsdNodeBuilder createBuilder() {
        return new XsdNodeBuilder(documentationExtractor);
    }

}
