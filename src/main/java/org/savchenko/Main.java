package org.savchenko;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.savchenko.ExcelReader.ExcelReader;
import org.savchenko.dto.CellDto;
import org.savchenko.htmlwriter.HtmlWriter;
import org.savchenko.wordwriter.WordWriter;
import org.savchenko.xsdparser.XsdReader;
import org.xmlet.xsdparser.core.XsdParser;

import java.io.FileOutputStream;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main2(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                XsdReader xsdReader = new XsdReader();
                List<CellDto> cellDtoList = xsdReader.readSchemaElementAsNav(scanner.nextLine());
                HtmlWriter htmlWriter = new HtmlWriter();
                for (CellDto cellDto : cellDtoList) {
                    htmlWriter.writeHtml(cellDto);
                }
                htmlWriter.writeNavTable(cellDtoList);
            } catch (Exception e) {
                System.out.println("Введен нехороший формат");
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] arg) {
        XsdReader xsdReader = new XsdReader();
        CellDto cellDto = xsdReader.readSchemaElement("C:\\Users\\d.savchenko\\Desktop\\buildschemas\\xsd\\v_13_0\\idActs\\AIVPV.xsd");
        HtmlWriter htmlWriter = new HtmlWriter();
        htmlWriter.writeSingleHtml(cellDto);
    }
}