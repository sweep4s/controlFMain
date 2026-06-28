package com.controlf.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PoliticoImportResultDTO {
    private int found;
    private int imported;
    private int duplicates;
}