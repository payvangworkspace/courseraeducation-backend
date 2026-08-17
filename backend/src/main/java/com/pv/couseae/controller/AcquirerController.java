package com.pv.couseae.controller;

import com.pv.couseae.entities.Acquirer;
import com.pv.couseae.entities.AcquirerModel;
import com.pv.couseae.services.AcquirerService;
import com.pv.couseae.utill.ResponseModel;
import com.pv.couseae.utill.SearchRequest;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
//@CrossOrigin
@RequestMapping("acquirer")
@AllArgsConstructor
public class AcquirerController {
    private AcquirerService acquirerService;
    private ModelMapper mapper;

    @PostMapping
    public ResponseEntity<?> createAcquirer(@RequestBody Acquirer acquirer) {
        Acquirer existingAcquirer = this.acquirerService.getByCodeOrName(acquirer.getAcquirerCode(), acquirer.getFullName()); // find the existing user with
        if (existingAcquirer != null) {
            return ResponseModel.error("Acquirer name or code already exist");
        } else if (!acquirer.isPayin() && !acquirer.isPayout()) {
            return ResponseModel.error("Select Acquirer type");
        } else if (acquirer.isPayin() && acquirer.getAcquirerPgId().isEmpty()) {
            return ResponseModel.error("Acquirer Payin PgId, SecretId or Password should not be null");
        } else if (acquirer.isPayout() && acquirer.getAcquirerPayoutPgId().isEmpty()) {
            return ResponseModel.error("Acquirer Payout PgId, SecretId or Password should not be null");
        } else {
            this.acquirerService.addNewAcquirer(acquirer);
        }
        return ResponseModel.created("ACQUIRER created successfully");
    }

    @PostMapping("all")
    ResponseEntity<?> getAllAcquirer(@RequestBody SearchRequest searchRequest) {
        Page<Acquirer> acquirerList = this.acquirerService.geAllAcquirer(searchRequest);
        List<AcquirerModel> acquirerModels = this.mapper.map(acquirerList.getContent(), new TypeToken<List<AcquirerModel>>() {
        }.getType());
        return ResponseModel.success("All Acquirers list", acquirerModels, acquirerList);
    }

    @PostMapping("status/{type}")
    ResponseEntity<?> getAllAcquirer(@PathVariable String type, @RequestBody Acquirer acquirerId) {
        String acquirerStatus = "Acquirer status type not define";
        if (type.equalsIgnoreCase("payin")) {
            acquirerStatus = this.acquirerService.updateStatus(acquirerId.getAcquirerId(), "payin");
        } else if (type.equalsIgnoreCase("payout")) {
            acquirerStatus = this.acquirerService.updateStatus(acquirerId.getAcquirerId(), "payout");
        } else if (type.equalsIgnoreCase("acquirer")) {
            acquirerStatus = this.acquirerService.updateStatus(acquirerId.getAcquirerId(), "acquirer");
        } else
            return ResponseModel.error("status not define");
        return ResponseModel.success(acquirerStatus);
    }

    @PostMapping("updatePayin")
    ResponseEntity<?> updateAcquirerPayinDetails(@RequestBody Acquirer payinDetails) {
        if (payinDetails.getAcquirerId().isEmpty())
            return ResponseModel.error("Acquirer not found");
        if (payinDetails.getAcquirerPgId() == null || payinDetails.getAcquirerPgId().isEmpty())
            return ResponseModel.error("AcquirerPgId should not be empty");
        if (payinDetails.getAcquirerPgKey().isEmpty() && payinDetails.getAcquirerPgPassword().isEmpty())
            return ResponseModel.error("PayIn PGKey or Password should not be empty");

        Acquirer acquirer = this.acquirerService.acquirerById(payinDetails.getAcquirerId());
        acquirer.setPayin(true);
        acquirer.setAcquirerPgId(payinDetails.getAcquirerPgId());
        acquirer.setAcquirerPgKey(payinDetails.getAcquirerPgKey());
        acquirer.setAcquirerPgPassword(payinDetails.getAcquirerPgPassword());
        this.acquirerService.updateAcquirer(acquirer);
        return ResponseModel.success("Acquirer payin updated successfully");
    }

    @PostMapping("updatePayout")
    ResponseEntity<?> updateAcquirerPayoutDetails(@RequestBody Acquirer payoutDetails) {
        if (payoutDetails.getAcquirerId().isEmpty())
            return ResponseModel.error("Acquirer not found");
        if (payoutDetails.getAcquirerPayoutPgId() == null || payoutDetails.getAcquirerPayoutPgId().isEmpty())
            return ResponseModel.error("Acquirer PayoutPgId should not be empty");
        if (payoutDetails.getAcquirerPayoutPgKey().isEmpty() && payoutDetails.getAcquirerPayoutPgPassword().isEmpty())
            return ResponseModel.error("Payout PgKey or Password should not be empty");
        Acquirer acquirer = this.acquirerService.acquirerById(payoutDetails.getAcquirerId());
        acquirer.setPayout(true);
        acquirer.setAcquirerPayoutPgId(payoutDetails.getAcquirerPayoutPgId());
        acquirer.setAcquirerPayoutPgKey(payoutDetails.getAcquirerPayoutPgKey());
        acquirer.setAcquirerPayoutPgPassword(payoutDetails.getAcquirerPayoutPgPassword());
        this.acquirerService.updateAcquirer(acquirer);
        return ResponseModel.success("Acquirer payout updated successfully");
    }

    @GetMapping("{acquirerId}")
    ResponseEntity<?> getAcquirerById(@PathVariable String acquirerId) {
        Acquirer acquirer = this.acquirerService.acquirerById(acquirerId);
        return ResponseModel.success(acquirer != null ? "Acquirer" : "Acquirer not found", acquirer);
    }

    @DeleteMapping("{acquirerId}")
    ResponseEntity<?> deleteAcquirer(@PathVariable String acquirerId) {
        this.acquirerService.deleteAcquirer(acquirerId);
        return ResponseModel.deleted();
    }
}