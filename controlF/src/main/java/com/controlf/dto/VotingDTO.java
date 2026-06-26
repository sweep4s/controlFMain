package com.controlf.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VotingDTO {
    private Long id;
    private String votingDate;
    private String proposalDescription;
    private String themeDescription;
    private String description;
}
