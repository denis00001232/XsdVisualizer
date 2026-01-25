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
        AppRunner appRunner = new AppRunner();
        appRunner.run(args);
    }
}