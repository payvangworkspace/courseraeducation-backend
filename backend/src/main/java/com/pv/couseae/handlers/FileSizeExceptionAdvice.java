package com.pv.couseae.handlers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class FileSizeExceptionAdvice {

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<Object> handleMaxUploadException(MaxUploadSizeExceededException e, HttpServletRequest request,
			HttpServletResponse response) {
		log.error("MaxUploadSizeExceededException ");
		log.error(String.valueOf(e));
		Map<String, Object> body = new LinkedHashMap<>();
        body.put("source", "main-service");
		body.put("status", "fail");
		body.put("message", "File Size too large");
		body.put("errors", e.getLocalizedMessage());
		return new ResponseEntity<>(body, HttpStatus.OK);
	}
}