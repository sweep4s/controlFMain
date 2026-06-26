package com.controlf.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssemblyMemberDTO {
    private Long id;
    private String firstName;
    private String lastname;
    private String territorial;
}
