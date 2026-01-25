package org.savchenko.htmlwriter.generator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.savchenko.htmlwriter.HtmlElementWriter;
import org.savchenko.htmlwriter.config.HtmlWriterConfig;
import org.savchenko.node.XsdNode;


public class HtmlDocumentGenerator {
    private final HtmlWriterConfig config;
    private final HtmlElementWriter elementWriter;
    
    public HtmlDocumentGenerator(HtmlWriterConfig config, HtmlElementWriter elementWriter) {
        this.config = config;
        this.elementWriter = elementWriter;
    }
    
    public Document generateSchemaDocument(XsdNode rootNode) {
        Document doc = createBaseDocument(rootNode.getFileName());
        
        doc.head().appendElement("style").text(config.getStyleCss());
        
        // SVG layer for connectors
        doc.body().appendElement("svg")
            .attr("class", "connectors-container")
            .attr("id", "svgLayer");
        
        // Navigation panel
        Element navContainer = doc.body().appendElement("div");
        elementWriter.writeNavigationPanel(navContainer);
        
        // Root container
        Element rootContainer = doc.body().appendElement("div")
            .attr("class", "container-block-root");
        elementWriter.writeRootContainer(rootContainer, rootNode);
        elementWriter.writeContainerSequence(rootContainer, rootNode);
        
        // Script
        doc.body().appendElement("script").text(config.getScriptJs());
        
        return doc;
    }
    
    private Document createBaseDocument(String title) {
        Document doc = Jsoup.parse("<html></html>");
        doc.outputSettings().prettyPrint(false);
        doc.outputSettings().charset("UTF-8");
        doc.head().appendElement("meta").attr("charset", "UTF-8");
        doc.title(title);
        return doc;
    }
}