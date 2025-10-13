package org.savchenko.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Виды типов String type:
 * 1. "element"
 * 2. "choice"
 * 3. "any"
 * 4. "sequence"
 * 5. "group"
 */

@Data
public class CellDto {
    private String uniqueId;

    private String minOccurs;
    private String maxOccurs;

    private String name;
    private String type;

    private String documentation;
    private boolean isVisible = true;

    private Map<String, String> conditions = new HashMap<>();

    private List<CellDto> children = new ArrayList<>();
}
