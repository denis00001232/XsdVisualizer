package org.savchenko.htmlwriter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.savchenko.node.XsdNode;

import java.util.List;

public class NavigationTableGenerator {
    private final HtmlWriterConfig config;
    
    public NavigationTableGenerator(HtmlWriterConfig config) {
        this.config = config;
    }
    
    public Document generateNavigationTable(List<XsdNode> nodes) {
        Document doc = createBaseDocument();
        Element table = doc.body().appendElement("table")
            .attr("class", "styled-table");
        
        writeTableHeader(table);
        writeTableRows(table, nodes);
        
        return doc;
    }
    
    private Document createBaseDocument() {
        Document doc = Jsoup.parse("<html></html>");
        doc.outputSettings().charset("UTF-8");
        doc.head().appendElement("meta").attr("charset", "UTF-8");
        doc.title("Navigation page");
        doc.head().appendElement("style").text(config.getStyleTableCss());
        return doc;
    }
    
    private void writeTableHeader(Element table) {
        Element row = table.appendElement("tr");
        row.appendElement("th").appendText("Structure name");
        row.appendElement("th").appendText("Description");
    }
    
    private void writeTableRows(Element table, List<XsdNode> nodes) {
        for (XsdNode node : nodes) {
            if (node.hasChildren()) {
                XsdNode firstChild = node.getChildren().get(0);
                Element row = table.appendElement("tr");
                
                row.appendElement("td")
                    .appendElement("a")
                    .attr("href", config.getSchemasSubDirectory() + "/" + node.getFileName())
                    .attr("target", "_blank")
                    .appendText(firstChild.getName());
                
                String documentation = firstChild.getDocumentation() != null 
                    ? firstChild.getDocumentation() 
                    : "";
                row.appendElement("td").appendText(documentation);
            }
        }
    }
}