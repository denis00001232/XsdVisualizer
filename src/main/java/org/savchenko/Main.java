package org.savchenko;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.savchenko.htmlwriter.HtmlWriter;
import org.savchenko.xsdparser.XsdReader;
import org.xmlet.xsdparser.core.XsdParser;

public class Main {
    public static void main(String[] args) {
        XsdReader xsdReader = new XsdReader("C:\\Users\\d.savchenko\\Desktop\\buildschemas\\xsd\\idActs\\AIGE.xsd");
        HtmlWriter htmlWriter = new HtmlWriter();
        htmlWriter.writeHtml(xsdReader.getReadResult());
    }
}