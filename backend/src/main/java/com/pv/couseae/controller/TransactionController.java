package com.pv.couseae.controller;

import com.pv.couseae.entities.PayinRequest;
import com.pv.couseae.entities.PayinRequestCrypto;
import com.pv.couseae.services.PayinService;
import com.pv.couseae.services.UserService;
import com.pv.couseae.utill.ResponseModel;
import com.pv.couseae.utill.SearchRequest;
import com.pv.couseae.utill.SearchRequestCrypto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("transaction")
@AllArgsConstructor
public class TransactionController {

    private PayinService orderService;
    private UserService userService;

    /**
     * Main transaction listing — now sourced from payin_requests.
     */
    @PostMapping
    ResponseEntity<?> getAllTransactions(@RequestBody SearchRequest searchRequest, Principal principal) {
        String loginUser = this.userService.loginUserForData(principal.getName());
        if (!loginUser.isEmpty() && !loginUser.equalsIgnoreCase("R")) {
            searchRequest.setUserName(loginUser);
        }

        Page<PayinRequest> transactionsList = this.orderService.PayinTxnSearch(searchRequest);
        log.info("Payin transactions fetched: {}", transactionsList.getTotalElements());

        return ResponseModel.success("All transactions", transactionsList);
    }

    /**
     * Payin transactions (kept as-is — same source).
     */
    @PostMapping("/getPayinTxndetails")
    public ResponseEntity<?> getPayinTxn(@RequestBody SearchRequest searchRequest, Principal principal) {
        String loginUser = this.userService.loginUserForData(principal.getName());
        if (!loginUser.isEmpty() && !loginUser.equalsIgnoreCase("R")) {
            searchRequest.setUserName(loginUser);
        }
        Page<PayinRequest> transactionsList = this.orderService.PayinTxnSearch(searchRequest);
        return ResponseModel.success("Payin transactions", transactionsList);
    }

    /**
     * Excel report — also from payin_requests.
     */
    @PostMapping("/generateReport")
    public ResponseEntity<?> generateReport(@RequestBody SearchRequest searchRequest, Principal principal) throws IOException {
        String loginUser = this.userService.loginUserForData(principal.getName());
        if (!loginUser.isEmpty() && !loginUser.equalsIgnoreCase("R")) {
            searchRequest.setUserName(loginUser);
        }
        log.info("Generate Report -> UserName: {}, FromDate: {}, ToDate: {}, Status: {}, Type: {}",
                searchRequest.getUserName(),
                searchRequest.getDateFrom(),
                searchRequest.getDateTo(),
                searchRequest.getStatus(),
                searchRequest.getType());

        List<PayinRequest> getTxn = this.orderService.PayinTxnSearchForExcel(searchRequest);
        log.info("Payin txn list size for excel: {}", getTxn.size());
        byte[] excelBytes = generateExcel(getTxn);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Orders_Report.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelBytes);
    }

    /**
     * Crypto payin transactions.
     */
    @PostMapping("/getPayinCryptoTxndetails")
    public ResponseEntity<?> getCryptoPayinTxn(@RequestBody SearchRequestCrypto searchRequest, Principal principal) {
        String loginUser = this.userService.loginUserForData(principal.getName());
        if (!loginUser.isEmpty() && !loginUser.equalsIgnoreCase("R")) {
            searchRequest.setUserName(loginUser);
        }
        Page<PayinRequestCrypto> transactionsList = this.orderService.PayinCryptoTxnSearch(searchRequest);
        return ResponseModel.success("Payin transactions", transactionsList);
    }

    // ─────────────────────────────────────────────────────
    // Excel generation from PayinRequest
    // ─────────────────────────────────────────────────────
    private byte[] generateExcel(List<PayinRequest> ordersList) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Orders");

            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "Order ID", "Transaction Id", "Payable Amount",
                    "Merchant", "Acquirer", "MerchantCharges", "Transaction Status",
                    "Transaction Type", "Transaction Date", "txnType", "utrNumber",
                    "customerName", "customerContactNumber", "customerEmail",
                    "ordRequestId", "returnUrl", "createdDate", "lastModifiedDate"
            };

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            for (int i = 0; i < ordersList.size(); i++) {
                PayinRequest order = ordersList.get(i);
                Row row = sheet.createRow(i + 1);

                row.createCell(0).setCellValue(order.getOrderId() != null ? order.getOrderId() : "");
                row.createCell(1).setCellValue(order.getPayment_id() != null ? order.getPayment_id() : "");
                row.createCell(2).setCellValue(order.getAmount() != null ? order.getAmount().toString() : "");
                row.createCell(3).setCellValue(order.getFirstName() != null ? order.getFirstName() : "");
                row.createCell(4).setCellValue(order.getAggregatorCode() != null ? order.getAggregatorCode() : "");
                row.createCell(5).setCellValue("0");
                row.createCell(6).setCellValue(order.getTransactionStatus() != null ? order.getTransactionStatus() : "");
                row.createCell(7).setCellValue(order.getTransactionType() != null ? order.getTransactionType() : "");
                row.createCell(8).setCellValue(order.getPayment_date() != null ? order.getPayment_date() : "");
                row.createCell(9).setCellValue(order.getTransactionType() != null ? order.getTransactionType() : "");
                row.createCell(10).setCellValue(order.getPayment_id() != null ? order.getPayment_id() : "");
                row.createCell(11).setCellValue(order.getFirstName() != null ? order.getFirstName() : "");
                row.createCell(12).setCellValue(order.getCustomerMobile() != null ? order.getCustomerMobile() : "");
                row.createCell(13).setCellValue(order.getCustomerEmail() != null ? order.getCustomerEmail() : "");
                row.createCell(14).setCellValue("");
                row.createCell(15).setCellValue(order.getPaymentLink() != null ? order.getPaymentLink() : "");
                row.createCell(16).setCellValue(order.getCreatedOn() != null ? order.getCreatedOn().toString() : "");
                row.createCell(17).setCellValue(order.getUpdatedAt() != null ? order.getUpdatedAt().toString() : "");
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}