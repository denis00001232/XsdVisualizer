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
    public static void main(String[] args) throws JsonProcessingException {
        XsdReader xsdReader = new XsdReader();
        List<CellDto> cellDtoList = xsdReader.readSchemaElementAsNav("C:\\Users\\d.savchenko\\Desktop\\buildschemas\\xsd\\exon\\finScoringTax.xsd");
        HtmlWriter htmlWriter = new HtmlWriter();
        for (CellDto cellDto : cellDtoList) {
            htmlWriter.writeHtml(cellDto);
        }
        htmlWriter.writeNavTable(cellDtoList);
    }
}