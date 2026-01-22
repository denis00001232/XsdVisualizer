package org.savchenko;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.savchenko.dto.XsdNode;
import org.savchenko.htmlwriter.HtmlWriter;
import org.savchenko.xsdparser.XsdReaderEclipse;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main323(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                XsdReaderEclipse xsdReader = new XsdReaderEclipse();
                List<XsdNode> xsdNodeList = xsdReader.readSchemaElementAsNav(scanner.nextLine());
                HtmlWriter htmlWriter = new HtmlWriter();
                for (XsdNode xsdNode : xsdNodeList) {
                    htmlWriter.writeHtml(xsdNode);
                }
                htmlWriter.writeNavTable(xsdNodeList);
            } catch (Exception e) {
                System.out.println("Введен нехороший формат");
                e.printStackTrace();
            }
        }
    }

    public static void main23(String[] arg) {
        XsdReaderEclipse xsdReader = new XsdReaderEclipse();
        XsdNode xsdNode = xsdReader.readSchemaElement("C:\\Users\\d.savchenko\\Desktop\\buildschemas\\xsd\\pio\\ON_AKTREZRABP_KS2.xsd");
        HtmlWriter htmlWriter = new HtmlWriter();
        htmlWriter.writeSingleHtml(xsdNode);
    }

    public static void main(String[] arg) throws JsonProcessingException {
        XsdReaderEclipse xsdReaderXerces = new XsdReaderEclipse();
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(xsdReaderXerces.readSchemaElement("C:\\Users\\denis\\Desktop\\builchemas\\buildschemas\\xsd\\v_17_0\\info\\infoParticipant.xsd")));
    }
}