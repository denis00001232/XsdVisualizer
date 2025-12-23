package org.savchenko.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CellDto {
    private String minOccurs;
    private String maxOccurs;
    private String fileName;
    private String linkToChild;
    private String targetNameSpace;
    private String name;
    private String type;
    private String documentation;
    private List<CellDto> children = new ArrayList<>();
    private String pathFromRoot = "";
}
