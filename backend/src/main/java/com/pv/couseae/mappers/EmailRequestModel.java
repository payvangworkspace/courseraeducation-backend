package com.pv.couseae.mappers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmailRequestModel {
    private String to;
    private String subject;
    private String body;
    private String payload;
}
