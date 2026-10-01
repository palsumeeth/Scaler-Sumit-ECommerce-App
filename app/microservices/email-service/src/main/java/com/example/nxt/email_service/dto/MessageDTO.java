package com.example.nxt.email_service.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MessageDTO {
    private String to;
    private String from;
    private String subject;
    private String body;
}
