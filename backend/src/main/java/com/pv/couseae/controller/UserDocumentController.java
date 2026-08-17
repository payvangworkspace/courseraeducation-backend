package com.pv.couseae.controller;

import com.pv.couseae.entities.Documents;
import com.pv.couseae.entities.User;
import com.pv.couseae.notification.Notifications;
import com.pv.couseae.services.UserService;
import com.pv.couseae.utill.FileUtils;
import com.pv.couseae.utill.ResponseModel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
//@CrossOrigin
@RequestMapping("/user")
@AllArgsConstructor
public class UserDocumentController {
    private UserService userService;
    private Notifications notifications;


    @PostMapping("document")
    @SneakyThrows
    public ResponseEntity<?> newDocuments(@RequestParam MultipartFile file, @RequestParam String documentType, @RequestParam String userId) {
        Documents isExisting = this.userService.getDocumentByTypeAndUser(documentType, new User(userId));
        Documents document = new Documents();
        if (isExisting != null && isExisting.isVerified())
            return ResponseModel.error("Document already verified");
        if (isExisting != null)
            document.setDocumentId(isExisting.getDocumentId());

        document.setDocumentType(documentType);
        document.setDocumentFileName(file.getOriginalFilename());
        document.setDocumentFileType(file.getContentType());
        document.setDocumentFile(FileUtils.compressFile(file.getBytes()));
        document.setUser(new User(userId));
        this.userService.addDocuments(document);
        return ResponseModel.created("Document added successfully");
    }

    @PutMapping("document/verify/{documentId}")
    ResponseEntity<?> verifyDocument(@PathVariable String documentId){
        Documents isExisting = this.userService.getDocumentById(documentId);
        if (isExisting != null && isExisting.isVerified())
            return ResponseModel.error("Document already verified");
        if (isExisting!=null){
            isExisting.setVerified(true);
            this.userService.addDocuments(isExisting);
            this.notifications.sendDocumentVerify(isExisting);
            return ResponseModel.success("Document - "+isExisting.getDocumentType() +" is verified successfully");
        }
        return ResponseModel.customValidations("Document", "Document record is not found");
    }
    @PutMapping("document/reject")
    ResponseEntity<?> rejectDocument(@RequestBody Map<String,String> document){
        if (!document.containsKey("reason") || document.get("reason").isEmpty())
            return ResponseModel.error("Reason should not be empty to reject the document");

        Documents isExisting = this.userService.getDocumentById(document.get("documentId"));
        if (isExisting != null && isExisting.isVerified())
            return ResponseModel.error("Document already verified");
        if (isExisting!=null){
            isExisting.setRejectedReason(document.get("reason"));
            isExisting.setVerified(false);
            this.userService.addDocuments(isExisting);
            return ResponseModel.success("Document - "+isExisting.getDocumentType() +" is rejected successfully");
        }
        return ResponseModel.error("Document record is not found");
    }

    @GetMapping("document/{userId}")
    ResponseEntity<?> getUsersDocuments(@PathVariable String userId) {
        List<Documents> allDocuments = this.userService.getDocumentByUser(new User(userId));
        return ResponseModel.success("All documents", allDocuments);
    }

    @GetMapping("document/file/{documentId}")
    ResponseEntity<?> getDocumentsFile(@PathVariable String documentId) {
        Documents documents = this.userService.getDocumentById(documentId);
        if (documents != null)
            return ResponseModel.sendMedia(documents.getDocumentFile(), documents.getDocumentFileType());
        return ResponseModel.customValidations("File", "file not found");
    }

}
