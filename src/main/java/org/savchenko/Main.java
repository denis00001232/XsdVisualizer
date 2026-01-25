package org.savchenko;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.savchenko.node.XsdNode;
import org.savchenko.htmlwriter.HtmlWriter;
import org.savchenko.xsdparser.XsdParser;

import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                XsdParser xsdParser = new XsdParser();
                List<XsdNode> xsdNodeList = xsdParser.parseAsNavigation(scanner.nextLine());
                HtmlWriter htmlWriter = new HtmlWriter();
                htmlWriter.writeNavigationTable(xsdNodeList);
            } catch (Exception e) {
                System.out.println("Введен нехороший формат");
                e.printStackTrace();
            }
        }
    }
}