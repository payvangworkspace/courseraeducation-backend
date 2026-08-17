package com.pv.couseae.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pv.couseae.Dtos.HdfcOrderStatusRespDTO;
import com.pv.couseae.Dtos.HdfcSessionResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PayinMappers {
    @Autowired
    private ObjectMapper objectMapper;

    public HdfcSessionResponseDTO parseResponse(String json) {
        try {
            return objectMapper.readValue(json, HdfcSessionResponseDTO.class);
        } catch (Exception e) {
            log.error("❌ Failed to parse HDFC JSON", e);
            throw new RuntimeException("JSON parse error", e);
        }
    }

    public HdfcOrderStatusRespDTO parseHdfcOrderStatusResponse(String json) {
        try {
            return objectMapper.readValue(json, HdfcOrderStatusRespDTO.class);
            } catch (Exception e) {
                log.error("❌ Failed to parse HDFC Order Status JSON", e);
                throw new RuntimeException("JSON parse error", e);
            }
            }

}
