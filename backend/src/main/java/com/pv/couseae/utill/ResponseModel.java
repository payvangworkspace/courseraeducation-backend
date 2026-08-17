package com.pv.couseae.utill;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResponseModel {

    /**
     * {@code Success Response}.
     *
     * @param message must not be null
     * @return <b>status</b>: 200- OK, <b>data</b>, <b>data count</b>
     * @author Mayank Jyoti Verma
     */
    private static final String SOURCE = "Payout-service";
    public static ResponseEntity<Object> success(String message) {
        Map<String, Object> data = new HashMap<>();
        data.put("source", SOURCE);
        data.put("status", "success");
        data.put("message", message);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    public static ResponseEntity<Object> success(String message, List<?> payload) {
        Map<String, Object> data = new HashMap<>();
        data.put("source", SOURCE);
        data.put("status", "success");
        data.put("message", message);
        data.put("data", payload);
        data.put("count", payload.size());
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    public static ResponseEntity<Object> success(String message, Object payload) {
        Map<String, Object> data = new HashMap<>();
        data.put("source", SOURCE);
        data.put("status", "success");
        data.put("message", message);
        data.put("data", payload);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    public static ResponseEntity<Object> success(String message, Page<?> payload) {
        Map<String, Object> data = new HashMap<>();
        data.put("source", SOURCE);
        data.put("status", "success");
        data.put("message", message);
        data.put("data", payload.getContent());
        data.put("pageNumber", payload.getNumber());
        data.put("pageSize", payload.getSize());
        data.put("totalPage", payload.getTotalPages());
        data.put("totalElement", payload.getTotalElements());
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    public static ResponseEntity<Object> success(String message, Page<?> payload, Object summary) {
        Map<String, Object> data = new HashMap<>();
        data.put("source", SOURCE);
        data.put("status", "success");
        data.put("message", message);
        data.put("summary", summary);
        data.put("data", payload.getContent());
        data.put("pageNumber", payload.getNumber());
        data.put("pageSize", payload.getSize());
        data.put("totalPage", payload.getTotalPages());
        data.put("totalElement", payload.getTotalElements());
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    public static ResponseEntity<Object> success(String message, List<?> payloadData, Page<?> payload) {
        Map<String, Object> data = new HashMap<>();
        data.put("source", SOURCE);
        data.put("status", "success");
        data.put("message", message);
        data.put("data", payloadData);
        data.put("pageNumber", payload.getNumber());
        data.put("pageSize", payload.getSize());
        data.put("totalPage", payload.getTotalPages());
        data.put("totalElement", payload.getTotalElements());
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    public static ResponseEntity<Object> success(String message, List<?> payload, int pageNumber, int pageSize, long totalElements, int totalPages) {
        Map<String, Object> data = new HashMap<>();
        data.put("source", SOURCE);
        data.put("status", "success");
        data.put("message", message);
        data.put("data", payload);
        data.put("pageNumber", pageNumber);
        data.put("pageSize", pageSize);
        data.put("totalPage", totalPages);
        data.put("totalElement", totalElements);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    /**
     * {@code Created Response}.
     *
     * @param message must not be null
     * @return <b>status</b>: 201- Created, <b>data</b>, <b>data count</b>
     * @author Mayank Jyoti Verma
     */
    public static ResponseEntity<Object> created(String message) {
        Map<String, Object> data = new HashMap<>();
        data.put("source", SOURCE);
        data.put("status", "success");
        data.put("message", message);
        return new ResponseEntity<>(data, HttpStatus.CREATED);
    }

    public static ResponseEntity<Object> createdWithPayload(String message, List<?> payload) {
        Map<String, Object> data = new HashMap<>();
        data.put("source", SOURCE);
        data.put("status", "success");
        data.put("message", message);
        data.put("data", payload);
        return new ResponseEntity<>(data, HttpStatus.CREATED);
    }

    /**
     * Response error
     *
     * @return ResponseEntity<Object>(error message, status);
     * @author Mayank Jyoti Verma
     */
    public static ResponseEntity<Object> error(String message) {
        Map<String, Object> data = new HashMap<>();
        data.put("source", SOURCE);
        data.put("status", "fail");
        data.put("message", message);
        return new ResponseEntity<>(data, HttpStatus.BAD_REQUEST);
    }


    public static ResponseEntity<Object> error(String message, Object object) {
        Map<String, Object> data = new HashMap<>();
        data.put("source", SOURCE);
        data.put("status", "fail");
        data.put("statusCode", "400");
        data.put("message", message);
        data.put("data", object);
        return new ResponseEntity<>(data, HttpStatus.BAD_REQUEST);
    }

    /**
     * Custom validation error
     *
     * @param fieldName    : field name which is not valid
     * @param errorMessage : error message for validation
     * @return Return custom error Response Data
     */
    public static ResponseEntity<Object> customValidations(String fieldName, String errorMessage) {
        Map<String, Object> data = new HashMap<>();
        data.put("source", SOURCE);
        data.put("status", "fail");
        data.put("fieldName", fieldName);
        data.put("message", errorMessage);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    /**
     * File uploaded successfully
     *
     * @param fileName
     * @param fileType
     * @return
     */
    public static ResponseEntity<Object> fileUploaded(String fileName, String fileType) {
        Map<String, Object> data = new HashMap<>();
        data.put("source", SOURCE);
        data.put("status", "success");
        data.put("message", "File uploaded successfully");
        data.put("fileName", fileName);
        data.put("fileType", fileType);

        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    /**
     * Send Media in Original format
     *
     * @param fileData  - original file data in byte
     * @param fileType- type of the file like jpeg, jpg, pdf etc
     * @return file as original format
     * @author mayankjyotiverma
     */
    public static ResponseEntity<Object> sendMedia(byte[] fileData, String fileType) {
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.valueOf(fileType)).body(FileUtils.decompressFile(fileData));
    }

    public static ResponseEntity<Object> deleted() {
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    //------------------------Integration Response------------------------------
    public static ResponseEntity<Object> successAtIntegration(String message, Object payload) {
        Map<String, Object> data = new HashMap<>();
        data.put("source", SOURCE);
        data.put("status", "success");
        data.put("statusCode", "200");
        data.put("message", message);
        data.put("data", payload);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }
    public static ResponseEntity<Object> errorAtIntegration(String message) {
        Map<String, Object> data = new HashMap<>();
        data.put("source", SOURCE);
        data.put("status", "fail");
        data.put("statusCode", "400");
        data.put("message", message);
        return new ResponseEntity<>(data, HttpStatus.BAD_REQUEST);
    }
    public static ResponseEntity<Object> errorAtIntegration(String message, Object objectData) {
        Map<String, Object> data = new HashMap<>();
        data.put("source", SOURCE);
        data.put("status", "fail");
        data.put("statusCode", "400");
        data.put("message", message);
        data.put("result", objectData);
        return new ResponseEntity<>(data, HttpStatus.BAD_REQUEST);
    }


//------------------------End Integration Response------------------------------
}
