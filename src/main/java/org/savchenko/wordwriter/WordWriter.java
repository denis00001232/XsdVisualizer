package org.savchenko.wordwriter;

import lombok.SneakyThrows;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.savchenko.dto.CellDto;
import org.w3c.dom.Node;

import java.io.FileOutputStream;
import java.math.BigInteger;
import java.util.*;

public class WordWriter {
    private int bookmarkId = 0;
    private XWPFDocument doc;
    @SneakyThrows
    public void createDocument(List<CellDto> cellDtoList) {
        doc = new XWPFDocument();
        for (CellDto cellDto : cellDtoList) {
            createTable(cellDto);
        }
        FileOutputStream fileOutputStream = new FileOutputStream("test.docx");
        doc.write(fileOutputStream);
    }

    private void createBookmark(XWPFParagraph p, String bookmarkName) {
        CTP paragraph = p.getCTP(); // Получаем XML-параграф

        // Создаём тег начала закладки (bookmarkStart)
        CTBookmark start = paragraph.addNewBookmarkStart();
        start.setName(bookmarkName);
        start.setId(BigInteger.valueOf(bookmarkId)); // Важно: id должны совпадать у start и end

        // Создаём тег конца закладки (bookmarkEnd)
        CTMarkupRange end = paragraph.addNewBookmarkEnd();
        end.setId(BigInteger.valueOf(bookmarkId));

        bookmarkId++;
    }

    private void wrapRunWithHyperlinkToBookmark(XWPFRun run, String bookmarkName) {
        run.setUnderline(UnderlinePatterns.SINGLE);
        run.setColor("009af3");
        if (run == null) throw new IllegalArgumentException("run is null");
        if (bookmarkName == null || bookmarkName.isEmpty()) {
            throw new IllegalArgumentException("bookmarkName is null or empty");
        }

        // Получаем низкоуровневые объекты
        XWPFParagraph paragraph = run.getParagraph();
        if (paragraph == null) {
            throw new IllegalStateException("Run has no parent paragraph");
        }

        CTP ctp = paragraph.getCTP();
        CTR ctr = run.getCTR();

        // DOM-узлы параграфа и самого run
        Node ctpNode = ctp.getDomNode();
        Node ctrNode = ctr.getDomNode();
        Node parent = ctrNode.getParentNode();
        if (parent == null || parent != ctpNode) {
            // Для надёжности: ожидаем, что CTR непосредственно лежит под CTP (без вложенного hyperlink)
            // Если он уже внутри другого контейнера, потребуется дополнительная логика.
            // Но попытаемся продолжить — Word обычно допускает разные структуры.
        }

        // Создаём <w:hyperlink w:anchor="bookmarkName"> на уровне CTP
        CTHyperlink ctHyperlink = ctp.addNewHyperlink();
        ctHyperlink.setAnchor(bookmarkName);

        // Получаем DOM-узел гиперссылки
        Node hyperlinkNode = ctHyperlink.getDomNode();

        // Вставляем гиперссылку перед текущим CTR
        // (чтобы сохранить исходную позицию run в потоке)
        ctpNode.insertBefore(hyperlinkNode, ctrNode);

        // Удаляем CTR из параграфа и перемещаем внутрь <w:hyperlink>
        ctpNode.removeChild(ctrNode);
        hyperlinkNode.appendChild(ctrNode);
    }

    private void createTable(CellDto cellDto) {
        if (cellDto.getChildren().size() == 0) return;
        XWPFParagraph p = doc.createParagraph();
        p.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun r = p.createRun();
        r.setFontSize(16);
        r.setText(cellDto.getName());
        createBookmark(p, cellDto.getLinkToChild());
        XWPFTable table = doc.createTable(countRows(cellDto) + 1, 4);
        XWPFParagraph xwpfParagraph = doc.createParagraph();
        xwpfParagraph.setSpacingAfter(40);
        System.out.println(countRows(cellDto));
        table.setWidth("99%");
        table.getRow(0).getCell(0).setText("Название структуры");
        table.getRow(0).getCell(1).setText("Название составного элемента");
        table.getRow(0).getCell(2).setText("Описание");
        table.getRow(0).getCell(3).setText("Тип элемента");
        table.getRow(1).getCell(0).setText(cellDto.getName());
        int itr = 0;
        for (CellDto cellDtoChild : cellDto.getChildren()) {
            itr++;
            if ("choice".equals(cellDtoChild.getType())) {
                for (CellDto choiceCellDto : cellDtoChild.getChildren()) {
                    itr++;
                    createRow(table, choiceCellDto, itr);
                    createRecurrentlyTable(choiceCellDto);
                }
            } else {
                createRow(table, cellDtoChild, itr);
                createRecurrentlyTable(cellDtoChild);
            }

        }
    }

    public void createRecurrentlyTable(CellDto cellDto) {
        if (cellDto.getChildren().isEmpty()) return;
        createTable(cellDto.getChildren().get(0));
    }

    private void createRow(XWPFTable table, CellDto cellDto, int index) {
        try {
            table.getRow(index).getCell(1).setText(cellDto.getName());
            table.getRow(index).getCell(2).setText(cellDto.getDocumentation());
            XWPFParagraph p = table.getRow(index).getCell(3).addParagraph();
            XWPFRun r = p.createRun();
            List<CellDto> cellDtoChildren = cellDto.getChildren();
            if (!cellDtoChildren.isEmpty()) {
                r.setText(cellDtoChildren.get(0).getName());
                if (cellDto.getLinkToChild() != null) {
                    wrapRunWithHyperlinkToBookmark(r, cellDto.getLinkToChild());
                }
            } else {
                r.setText("Простой");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private int countRows(CellDto cellDto) {
        int amount = 0;
        for (CellDto cellDtoChild : cellDto.getChildren()) {
            if ("choice".equals(cellDtoChild.getType())) {
                amount += cellDtoChild.getChildren().size();
            } else {
                amount += 1;
            }
        }
        return amount;
    }

    public void modifyCellDtoList(List<CellDto> cellDtoList) {
        cellDtoList.forEach(this::modifyCellDto);
    }

    public void modifyCellDto(CellDto cellDto) {
        List<CellDto> newChildren = new ArrayList<>();
        Queue<CellDto> cellDtoChecked = new LinkedList<>();
        if (cellDto.getFileName() != null) {
            cellDto.setLinkToChild(cellDto.getFileName());
        }
        cellDtoChecked.addAll(cellDto.getChildren());

        while (!cellDtoChecked.isEmpty()) {
            CellDto childCellDto = cellDtoChecked.poll();
            String type = childCellDto.getType();
            if ("choice".equals(type)) { //делаем choice как есть
                newChildren.add(childCellDto);
            }
            else if ("element".equals(type) && !childCellDto.getChildren().isEmpty()) { //для элемента подтягиваем название его сложного типа
                CellDto childOfElement = childCellDto.getChildren().get(0);
                if (childOfElement.getName() == null) {
                    childOfElement.setName(childCellDto.getName());
                    childCellDto.setLinkToChild(childCellDto.getName());
                    childOfElement.setLinkToChild(childCellDto.getName());
                }
                newChildren.add(childCellDto);
            }
            else if ("seq".equals(type) || "complexCon".equals(type)) {
                cellDtoChecked.addAll(childCellDto.getChildren()); // просто закидываем всех дочек sequence и common type наверх
            }
            else if ("base".equals(type)) {
                CellDto childOfBase = childCellDto.getChildren().get(0);
                cellDtoChecked.add(childOfBase);
            }
            else if ("attribute".equals(type)) {
                newChildren.add(childCellDto);
            } else if ("ct".equals(type) || "ct externalImport".equals(type)) {

                newChildren.add(childCellDto);
            }
        }
        cellDto.setChildren(newChildren);
        newChildren.forEach(this::modifyCellDto); // рекурсивно выполняем для детей
    }

}
