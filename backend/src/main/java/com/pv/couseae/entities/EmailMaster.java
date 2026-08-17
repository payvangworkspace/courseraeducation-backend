package com.pv.couseae.entities;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "email_master")
public class EmailMaster {
    @Id
    private String id;   // Mongo uses String/ObjectId

    private String emailCode;
    private String fromEmail;
    private String ccEmail;
    private String subject;

    private String bodyTemplate; // Mongo handles large text, no need for columnDefinition

    private String smtpHost;
    private int smtpPort;
    private String smtpUser;
    private String smtpPassword;
    private boolean useSsl;
    private String status;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

}
