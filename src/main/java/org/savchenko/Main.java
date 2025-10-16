package org.savchenko;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.savchenko.ExcelReader.ExcelReader;
import org.savchenko.htmlwriter.HtmlWriter;
import org.savchenko.xsdparser.XsdReader;
import org.xmlet.xsdparser.core.XsdParser;

import java.io.FileOutputStream;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String path = scanner.nextLine();
            XsdReader xsdReader = new XsdReader(path);
            HtmlWriter htmlWriter = new HtmlWriter();
            htmlWriter.writeHtml(xsdReader.getReadResult());
        }
    }
}