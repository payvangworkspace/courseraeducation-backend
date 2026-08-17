package com.pv.couseae.mappers;

import com.pv.couseae.Dtos.merchant.MerchantAcquirerDTO;
import com.pv.couseae.Dtos.merchant.MerchantDto;
import com.pv.couseae.entities.MerchantAcquirerTdrMdrMapping;
import com.pv.couseae.entities.MerchantCharges;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This class maps MerchantAcquirerTdrMdrMapping to MerchantAcquirerDTO
 * @author <Devendra>
 *
 */
public class MerchantAcquirerMapper {
    public List<MerchantAcquirerDTO> mapToDTO(List<MerchantAcquirerTdrMdrMapping> mappings) {
        return mappings.stream().map(mapping -> {
            MerchantAcquirerDTO dto = new MerchantAcquirerDTO();
            // Basic info
            dto.setMerchantTdrMdrId(mapping.getMerchantTdrMdrId());
            if(mapping.getMerchant() != null) {
                dto.setMerchantUserId(mapping.getMerchant().getUserId());
                dto.setMerchantFullName(mapping.getMerchant().getFullName());
            }

            if(mapping.getAcquirer() != null) {
                dto.setAcquirerUserId(mapping.getAcquirer().getAcquirerId());
                dto.setAcquirerFullName(mapping.getAcquirer().getFullName());
                dto.setAcquirerCode(mapping.getAcquirer().getAcquirerCode()); // if available
                dto.setAquirerstatus(mapping.getAcquirer().isStatus());

                dto.setAcquirerPgId(mapping.getAcquirer().getAcquirerPgId());
                dto.setAcquirerPgKey(mapping.getAcquirer().getAcquirerPgKey());
                dto.setAcquirerPgPassword(mapping.getAcquirer().getAcquirerPgPassword());
                dto.setPayin(mapping.getAcquirer().isPayin());
                dto.setPayinWebhookUrl(mapping.getAcquirer().getPayinWebhookUrl());

                dto.setAcquirerPayoutPgId(mapping.getAcquirer().getAcquirerPayoutPgId());
                dto.setAcquirerPayoutPgKey(mapping.getAcquirer().getAcquirerPayoutPgKey());
                dto.setAcquirerPayoutPgPassword(mapping.getAcquirer().getAcquirerPayoutPgPassword());
                dto.setPayout(mapping.getAcquirer().isPayout());
                dto.setPayoutWebhookUrl(mapping.getAcquirer().getPayoutWebhookUrl());

            }

            if(mapping.getPaymentType() != null) {
                dto.setPaymentTypeId(mapping.getPaymentType().getPaymentTypeId());
                dto.setPaymentTypeName(mapping.getPaymentType().getPaymentTypeName());
                dto.setPaymentTypeCode(mapping.getPaymentType().getPaymentTypeCode());
            }

            if(mapping.getMopType() != null) {
                dto.setMopTypeId(mapping.getMopType().getMopTypeId());
                dto.setMopTypeName(mapping.getMopType().getMopTypeName());
                dto.setMopTypeCode(mapping.getMopType().getMopTypeCode());
            }

            // TDR / MDR / GST / Charges

            dto.setPriority(mapping.getPriority());
            dto.setAmountLimit(mapping.getAmountLimit());
            dto.setMapping_gstVat(mapping.getGstVat());
            // Add merchantCharge, pgCharge, bankCharge, min/max limits etc. if available in mapping or related entities
            if(mapping.getMerchant() != null){
           dto.setMerchantList(mapMerchantListToDto(mapping.getMerchantCharges()));
            }
            // Payment gateway info
            // dto.setIsPayin(...); dto.setAcquirerPgId(...); etc.
            return  dto;
        }).collect(Collectors.toList());
    }
    public List<MerchantDto> mapMerchantListToDto(List<MerchantCharges> merchants) {
        return merchants.stream()
                .map(merchant -> new MerchantDto(
                        merchant.getGstVat(),
                        merchant.getMerchantCharge(),
                        merchant.getPgCharge(),
                        merchant.getBankCharge(),
                        merchant.getMinimumAmountLimit(),
                        merchant.getMaximumAmountLimit(),
                        merchant.isFixCharge()
                ))
                .collect(Collectors.toList());
    }

}
