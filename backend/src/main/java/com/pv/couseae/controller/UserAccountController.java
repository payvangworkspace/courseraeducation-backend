package com.pv.couseae.controller;

import com.pv.couseae.entities.Documents;
import com.pv.couseae.entities.User;
import com.pv.couseae.enums.UserProcessingMode;
import com.pv.couseae.model.AccountDetailsModel;
import com.pv.couseae.model.PersonalDetailsModel;
import com.pv.couseae.services.EmailServices;
import com.pv.couseae.services.UserService;
import com.pv.couseae.utill.RegExAndValidations;
import com.pv.couseae.utill.ResponseModel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
//@CrossOrigin
@RequestMapping("/user")
@AllArgsConstructor
public class UserAccountController {

    private UserService userService;
    private ModelMapper mapper;
    private EmailServices emailServices;

    /**
     * Account Details
     *
     * @param userId - userId string
     * @return users' account info
     */
    @GetMapping("accountDetails/{userId}")
    ResponseEntity<?> getAccountDetails(@PathVariable String userId) {
        User user = this.userService.userById(userId);
        if (user == null)
            return ResponseModel.error("User not found");
        AccountDetailsModel accountDetailsModel = this.mapper.map(user, new TypeToken<AccountDetailsModel>() {
        }.getType());
        return ResponseModel.success("Account Details", accountDetailsModel);
    }


    /**
     * Personal details
     *
     * @param userId - userId for user details
     * @return personal details
     */
    @GetMapping("personalDetails/{userId}")
    ResponseEntity<?> getPersonalDetails(@PathVariable String userId) {
        User user = this.userService.userById(userId);
        if (user == null)
            return ResponseModel.error("User not found");
        PersonalDetailsModel personalDetailsModel = this.mapper.map(user, new TypeToken<PersonalDetailsModel>() {
        }.getType());
        return ResponseModel.success("All users department list", personalDetailsModel);
    }


    // ------------- Update Details -----------------

    @PutMapping("updateDetails")
    public ResponseEntity<?> newAddress(@RequestBody Map<String, String> updatedDetails, Principal principal) {
        String userName=updatedDetails.getOrDefault("userName", "");


        if (userService.isAdmin(principal.getName())){
           if(userName.isEmpty())
               return ResponseModel.error("Username should not be empty");

        }else userName = principal.getName();

        User currentUser = this.userService.userById(userName);
        if (currentUser==null)
            return ResponseModel.error("User not found");

        RegExAndValidations.validatePhoneNumber(currentUser.getContactNumber());

        String contactNumber = updatedDetails.getOrDefault("contactNumber", currentUser.getContactNumber());
        String dateOfBirth =updatedDetails.getOrDefault("dateOfBirth", currentUser.getDateOfBirth());
        String gender =updatedDetails.getOrDefault("gender", currentUser.getGender());
        String address=updatedDetails.getOrDefault("address", currentUser.getAddressDetails());

        currentUser.setContactNumber(contactNumber);
        currentUser.setDateOfBirth(dateOfBirth);
        currentUser.setGender(gender);
        currentUser.setAddressDetails(address);
        this.userService.updateUser(currentUser);
        return ResponseModel.created("Address added successfully");
    }

// ------------- Operations -----------------

    /**
     * update User status - if already true, then make it false vice versa
     *
     * @param userId - userId which have to change status
     * @return - status
     */
    @PutMapping("status/{userId}")
    ResponseEntity<?> updateLockedStatus(@PathVariable String userId) {
        User user = this.userService.userById(userId);
        user.setAccountNonLocked(!user.isAccountNonLocked());
        user.setStatus(!user.isStatus());
        this.userService.updateUser(user);
        return ResponseModel.success(user.isAccountNonLocked() ? "User Enabled" : "User disabled");
    }
    @PutMapping("/payoutstatus/{userId}")
    ResponseEntity<?> updatePayoutStatus(@PathVariable String userId) {
        User user = this.userService.userById(userId);

        user.setPayoutEnabled(user.isPayoutEnabled()? false : true);
        this.userService.updateUser(user);
        return ResponseModel.success(user.isPayoutEnabled() ? "User Payout Enabled" : "User Payout disabled");
    }
    @PutMapping("/payoutStatusViaApplication/{userId}")
    ResponseEntity<?> updatePayoutStatusViaApplication(@PathVariable String userId) {
        User user = this.userService.userById(userId);

        user.setPayoutEnabledViaApp(user.isPayoutEnabledViaApp()? false : true);
        this.userService.updateUser(user);
        return ResponseModel.success(user.isPayoutEnabledViaApp() ? "User Payout Via App Enabled" : "User Payout Via App disabled");
    }
    @PutMapping("payinstatus/{userId}")
    ResponseEntity<?> updatePayinStatus(@PathVariable String userId) {
        User user = this.userService.userById(userId);

        user.setPayinEnabled(user.isPayinEnabled()? false : true);
        this.userService.updateUser(user);
        return ResponseModel.success(user.isPayinEnabled() ? "User Payin Enabled" : "User Payin disabled");
    }
    @PutMapping("/payinGststatus/{userId}")
    ResponseEntity<?> updatePayinGstStatus(@PathVariable String userId) {
        User user = this.userService.userById(userId);

        user.setPayinGstEnabled(user.isPayinGstEnabled()? false : true);
        this.userService.updateUser(user);
        return ResponseModel.success(user.isPayinGstEnabled() ? "User Payin GST Enabled" : "User Payin GST disabled");
    }
    @PutMapping("/payoutGststatus/{userId}")
    ResponseEntity<?> updatePayoutGstStatus(@PathVariable String userId) {
        User user = this.userService.userById(userId);

        user.setPayoutGstEnabled(user.isPayoutGstEnabled()? false : true);
        this.userService.updateUser(user);
        return ResponseModel.success(user.isPayoutGstEnabled() ? "User Payout GST Enabled" : "User Payout GST disabled");
    }
    @PutMapping("/payoutFeeReturnStatus/{userId}")
    ResponseEntity<?> updatePayoutFeeReturnStatus(@PathVariable String userId) {
        User user = this.userService.userById(userId);

        user.setFeeReturnOnRefund(user.isFeeReturnOnRefund()? false : true);
        this.userService.updateUser(user);
        return ResponseModel.success(user.isFeeReturnOnRefund() ? "User Payout Fee Return Enabled" : "User Payout Fee Return disabled");
    }
    @PutMapping("/UpdateMerchantShortCode/{userId}/ShortCode/{shortCode}")
    ResponseEntity<?> updatePayoutShortcode(@PathVariable String userId,@PathVariable String shortCode) {
        User user = this.userService.userById(userId);

        user.setShortCode(shortCode);
        this.userService.updateUser(user);
        return ResponseModel.success("Update Short Code....");
    }
    /**
     * update User verification status - if already true, then make it false vice versa
     *
     * @param userId - userId which have to change status
     * @return - status
     */
    @PutMapping("verifyUser/{userId}")
    ResponseEntity<?> verifyUser(@PathVariable String userId) {
        User user = this.userService.userById(userId);
        if (!user.isVerified()) {
            List<Documents> allDocuments = this.userService.getDocumentByUser(new User(userId));
            log.info("All documents size: "+allDocuments.size());
            if (allDocuments.size() < 8)
                return ResponseModel.error("All document are not uploaded");
            int count = 0;
            for (Documents items : allDocuments) {
                log.info("Document name: "+items.getDocumentFileName()+", Verified: "+items.isVerified());
                if (!items.isVerified())
                    return ResponseModel.error("Document verification is not completed");
            }
        }
        user.setVerified(!user.isVerified());
        user.setVerificationDate(LocalDateTime.now());
        this.userService.updateUser(user);
        this.emailServices.sendMerchantVerification(user);
        return ResponseModel.success(user.isVerified() ? "User verified" : "User not verified");
    }

    /**
     * update User verification status - if already true, then make it false vice versa
     *
     * @param userId - userId which have to change status
     * @return - status
     */
    @PutMapping("authStatus/{userId}")
    ResponseEntity<?> authStatus(@PathVariable String userId) {
        User user = this.userService.userById(userId);
        user.setProcessingMode(user.getProcessingMode().equals(UserProcessingMode.SALE.toString()) ? UserProcessingMode.AUTH.toString() : UserProcessingMode.SALE.toString());
        this.userService.updateUser(user);
        return ResponseModel.success("User set to " + user.getProcessingMode());
    }

    // ------------- End Operations -----------------
}
