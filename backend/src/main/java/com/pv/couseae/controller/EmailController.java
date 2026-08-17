package com.pv.couseae.controller;

import com.pv.couseae.entities.EmailMaster;
import com.pv.couseae.mappers.EmailRequestModel;
import com.pv.couseae.repos.EmailMasterRepo;
import com.pv.couseae.services.EmailServices;
import com.pv.couseae.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@AllArgsConstructor
public class EmailController {

    private EmailServices emailService;
    private UserService userService;
    private final EmailMasterRepo emailMasterRepo;

@PostMapping("/SaveEmailMaster")
public ResponseEntity<?> saveEmailMaster(@RequestBody EmailMaster emailMaster) {
    try {
        emailMaster.setCreatedDate(LocalDateTime.now());
        emailMasterRepo.save(emailMaster);
        return ResponseEntity.ok("Record Save Successfully");
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(500).build();
    }
}
    @PostMapping("/UpdateEmailMaster")
    public ResponseEntity<?> UpdateEmailMaster(@RequestBody EmailMaster emailMaster) {
        try {
            EmailMaster emlmstr=emailMasterRepo.findById(emailMaster.getId()).get();
            emlmstr.setCcEmail(emailMaster.getCcEmail());
            emlmstr.setSubject(emailMaster.getSubject());
            emlmstr.setFromEmail(emailMaster.getFromEmail());
            emlmstr.setBodyTemplate(emailMaster.getBodyTemplate());
            emlmstr.setSmtpHost(emailMaster.getSmtpHost());
            emlmstr.setSmtpPort(emailMaster.getSmtpPort());
            emlmstr.setSmtpUser(emailMaster.getSmtpUser());
            emlmstr.setSmtpPassword(emailMaster.getSmtpPassword());
            emlmstr.setUseSsl(emailMaster.isUseSsl());
            emlmstr.setStatus(emailMaster.getStatus());

            emailMaster.setUpdatedDate(LocalDateTime.now());
            emailMasterRepo.save(emailMaster);
            return ResponseEntity.ok("Record Save Successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    @PostMapping("/GetEmailMasterList")
    public ResponseEntity<?> GetEmailMasterList() {
        try {
            return ResponseEntity.ok(emailMasterRepo.findAll());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/send-email")
    public String sendEmail(@RequestBody EmailRequestModel emailRequest) {
        try {
            emailService.sendEmail(emailRequest.getTo(), emailRequest.getSubject(), emailRequest.getBody());
            return "Email sent successfully!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error to send email";
        }
    }
}
