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
import org.savchenko.xsdparser.XsdReaderEclipse;
import org.savchenko.xsdparser.XsdReaderXerces;
import org.xmlet.xsdparser.core.XsdParser;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main4(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                XsdReaderEclipse xsdReader = new XsdReaderEclipse();
                List<CellDto> cellDtoList = xsdReader.readSchemaElementAsNav(scanner.nextLine());
                ObjectMapper objectMapper = new ObjectMapper();
                FileOutputStream fileOutputStream = new FileOutputStream("test.json");
                fileOutputStream.write(objectMapper.writeValueAsBytes(cellDtoList.get(0)));
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

    public static void main3(String[] arg) {
        XsdReaderEclipse xsdReader = new XsdReaderEclipse();
        CellDto cellDto = xsdReader.readSchemaElement("C:\\Users\\d.savchenko\\Desktop\\buildschemas\\xsd\\gsn\\gsnPetitionPrescriptExecutionDateChange.xsd");
        HtmlWriter htmlWriter = new HtmlWriter();
        htmlWriter.writeSingleHtml(cellDto);
    }

    public static void main2(String[] arg) throws JsonProcessingException {
        XsdReaderEclipse xsdReaderXerces = new XsdReaderEclipse();
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(xsdReaderXerces.readSchemaElement("C:\\Users\\d.savchenko\\Desktop\\buildschemas\\xsd\\pio\\ON_AKTREZRABP_CON.xsd")));
    }
}