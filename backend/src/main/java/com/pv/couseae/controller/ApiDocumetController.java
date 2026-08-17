/*
package com.pv.couseae.controller;

import com.zenithpay.zenithpay.user.models.ApiDocuments;
import com.zenithpay.zenithpay.user.models.User;
import com.zenithpay.zenithpay.user.services.ApiDocumentService;
import com.zenithpay.zenithpay.utils.FileUtils;
import com.zenithpay.zenithpay.utils.ResponseModel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/apidoc")
@AllArgsConstructor
public class ApiDocumetController {

    private final ApiDocumentService apiDocService;

    // Get all documents
    @GetMapping("/GetAllDocuments")
    public ResponseEntity<?>  getAll() {
        return ResponseModel.success("Get All Api Doc",apiDocService.getAll());
    }
    @GetMapping("/GetActiveApiDocs")
    public ResponseEntity<?> getActiveApiDoc() {
        return ResponseModel.success("Get Active Api Doc",apiDocService.getActive());
    }
    // Get a single document by ID
    @GetMapping("/findApiDocById/{documentId}")
    public ResponseEntity<?>  getById(@PathVariable String documentId) {


        log.info("In findApiDocById and Doc id is ---: " +documentId);
        ApiDocuments doc=apiDocService.getById(documentId);
        if (doc == null || doc.getDocumentFile() == null) {
            return ResponseModel.customValidations("File", "file not found");
        }

        byte[] decompressed = FileUtils.decompressFile(doc.getDocumentFile());

        Map<String, String> fileData = new HashMap<>();
        fileData.put("fileName", doc.getDocumentFileName());
        fileData.put("fileType", doc.getDocumentFileType());
        fileData.put("data", Base64.getEncoder().encodeToString(decompressed));

        return ResponseModel.success("Document fetched successfully", fileData);
    }

    @PostMapping("/uploadApiDoc")
    @SneakyThrows
    public ResponseEntity<?> newDocuments(@RequestParam MultipartFile file, @RequestParam String documentType, @RequestParam String userId) {
        ApiDocuments document = new ApiDocuments();
        User usr=new User(userId);
        document.setUser(usr);
        document.setDocumentType(documentType);
        document.setDocumentFileName(file.getOriginalFilename());
        document.setDocumentFileType(file.getContentType());
        document.setDocumentFile(FileUtils.compressFile(file.getBytes()));
        document.setActive(true);
        apiDocService.saveApiDoc(document);
        return ResponseModel.created("Document added successfully");
    }

    @PatchMapping("/isActiveDoc/{documentId}/status")
    public ResponseEntity<?>  updateStatus(@PathVariable String documentId,@RequestParam boolean active) {
//# Activate a document
//        curl -X PATCH "http://localhost:8080/isActiveDoc/12345/status?active=true"
//# Deactivate a document
//        curl -X PATCH "http://localhost:8080/isActiveDoc/12345/status?active=false"
        ApiDocuments updated = apiDocService.toggleActive(documentId, active);
        return ResponseModel.success("update Api Doc",updated);
    }

    @DeleteMapping("/deleteDoc/{documentId}")
    public ResponseEntity<?> deleteApiDoc(@PathVariable String documentId) {
//  curl -X DELETE "http://localhost:8080/deleteDoc/12345"
        apiDocService.deleteById(documentId);
        return ResponseModel.success("Api Doc deleted successfully", documentId);
    }



}
*/
