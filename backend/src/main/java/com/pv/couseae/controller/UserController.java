package com.pv.couseae.controller;

import com.pv.couseae.entities.BusinessDetails;
import com.pv.couseae.entities.User;
import com.pv.couseae.enums.UserImageTypes;
import com.pv.couseae.model.MerchantModel;
import com.pv.couseae.model.UserListModel;
import com.pv.couseae.model.UserRegistrationModel;
import com.pv.couseae.notification.Notifications;
import com.pv.couseae.services.AcquirerService;
import com.pv.couseae.services.MerchantRedisService;
import com.pv.couseae.services.ResellerMappingService;
import com.pv.couseae.services.UserService;
import com.pv.couseae.utill.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AcquirerService acquirerService;
    private final ModelMapper mapper;
    private final Notifications notifications;
    private final ResellerMappingService resellerMappingService;
    private final AuthUtils authUtils;
    private final MerchantRedisService merchantRedisService;

    private static final String HARDCODED_HEADER_VALUE = "X7k#mP2vQ9zR";

    @Value("${admin.creation.secret-key}")
    private String propertySecretKey;

    // ─────────────────────────────────────────────────────
    // Admin
    // ─────────────────────────────────────────────────────

    @PostMapping("all")
    ResponseEntity<?> getAllUsers(@RequestBody SearchRequest searchRequest) {
        Page<User> usersList = this.userService.getAllUsers(searchRequest);
        return ResponseModel.success("All users list", usersList);
    }

    @PostMapping("/UserCreationViaAdmin")
    public ResponseEntity<?> createUserViaAdmin(@RequestBody UserRegistrationModel req) {
        if (!authUtils.isValidPassowrd(req.getPassword())) {
            log.info("Invalid Password..");
            return ResponseModel.error("Invalid Password. Password should be at least 8 characters long and should contain at least one uppercase letter, one lowercase letter, one digit and one special character.");
        }
        User user = this.userService.userById(req.getUserId());
        if (user != null) {
            return ResponseModel.customValidations("User Name", "User name/email already exist");
        }
        RegExAndValidations.validateEmail(req.getUserId());
        RegExAndValidations.validatePhoneNumber(req.getContactNumber());

        String role = req.getRoleId() == null || req.getRoleId().isBlank()
                ? "MERCHANT"
                : req.getRoleId().trim().toUpperCase();
        boolean createAsMerchant = "MERCHANT".equals(role)
                || (req.getBusinessName() != null && !req.getBusinessName().isBlank());

        if (createAsMerchant) {
            req.setRoleId("MERCHANT");
            return persistMerchant(req);
        }

        User newUser = new User();
        newUser.setUserId(req.getUserId());
        newUser.setContactNumber(req.getContactNumber());
        newUser.setFullName(req.getFullName());
        newUser.setPassword(req.getPassword());
        newUser.setVerified(true);
        newUser.setEmailVerified(true);
        newUser.setContactVerified(true);
        newUser.setRole(role);
        this.userService.createUser(newUser);
        sendOnboardingForRole(newUser);
        return ResponseModel.created("User created successfully...");
    }

    @PostMapping("admin")
    public ResponseEntity<?> createAdmin(@RequestBody UserRegistrationModel admin,
                                         @RequestHeader(value = "X-Admin-Verify-1", required = false) String verifyHeader1,
                                         @RequestHeader(value = "X-Admin-Verify-2", required = false) String verifyHeader2) {
        if (!authUtils.isValidPassowrd(admin.getPassword())) {
            log.info("Invalid Password..");
            return ResponseModel.error("Invalid Password. Password should be at least 8 characters long and should contain at least one uppercase letter, one lowercase letter, one digit and one special character.");
        }

        // 1. Verify both headers are present and correct
        if (verifyHeader1 == null || verifyHeader2 == null) {
            log.warn("Admin creation attempt missing verification headers");
            return ResponseModel.error("Unauthorized");
        }

        boolean header1Valid = MessageDigest.isEqual(HARDCODED_HEADER_VALUE.getBytes(StandardCharsets.UTF_8),
                verifyHeader1.getBytes(StandardCharsets.UTF_8));

        boolean header2Valid = MessageDigest.isEqual(
                propertySecretKey.getBytes(StandardCharsets.UTF_8),
                verifyHeader2.getBytes(StandardCharsets.UTF_8));

        if (!header1Valid || !header2Valid) {
            log.warn("Admin creation attempt with invalid verification headers");
            return ResponseModel.error("Unauthorized");
        }
        User user = this.userService.userById(admin.getUserId());
        if (user != null) {
            return ResponseModel.customValidations("Admin Name", "Admin name/email already exist");
        }
        RegExAndValidations.validateEmail(admin.getUserId());
        RegExAndValidations.validatePhoneNumber(admin.getContactNumber());
        User newUser = new User();
        newUser.setUserId(admin.getUserId());
        newUser.setContactNumber(admin.getContactNumber());
        newUser.setFullName(admin.getFullName());
        newUser.setPassword(admin.getPassword());
        newUser.setVerified(true);
        newUser.setEmailVerified(true);
        newUser.setContactVerified(true);
        newUser.setRole("ADMIN");
        this.userService.createUser(newUser);
        sendOnboardingForRole(newUser);
        return ResponseModel.created("Admin created successfully");
    }

    // ─────────────────────────────────────────────────────
    // Merchant
    // ─────────────────────────────────────────────────────

    @PostMapping("merchant")
    public ResponseEntity<?> createMerchant(@RequestBody UserRegistrationModel merchant) {
        User user = this.userService.userById(merchant.getUserId());
        if (user != null) {
            return ResponseModel.customValidations("MerchantModel Name", "MerchantModel name/email already exist");
        }
        RegExAndValidations.validateEmail(merchant.getUserId());
        RegExAndValidations.validatePhoneNumber(merchant.getContactNumber());

        User newUser = new User();
        newUser.setUserId(merchant.getUserId());
        newUser.setContactNumber(merchant.getContactNumber());
        newUser.setFullName(merchant.getFullName());
        newUser.setPassword(merchant.getPassword());
        newUser.setRole("MERCHANT");
        newUser.setBusinessName(merchant.getBusinessName());

        User createdUser = this.userService.createUser(newUser);

        persistBusinessDetails(merchant, createdUser);

        // ✅ Invalidate Redis cache so new merchant appears on next fetch
        merchantRedisService.deleteMerchants(CacheKeys.MERCHANTS_MERCHANTMODEL);
        merchantRedisService.deleteMerchants(CacheKeys.MERCHANTS_USERLIST);
        log.info("Redis cache invalidated after new merchant creation: {}", merchant.getUserId());

        sendOnboardingForRole(createdUser);
        return ResponseModel.created("MerchantModel created successfully");
    }

    @PostMapping("merchant/all")
    ResponseEntity<?> getAllMerchant(@RequestBody SearchRequest searchRequest, Principal principal) {

        List<MerchantModel> merchantModelList = merchantRedisService
                .getMerchantsMerchantModel(CacheKeys.MERCHANTS_MERCHANTMODEL);

        // ✅ Validate cache shape once, before ANY downstream method touches it
        if (!isValidMerchantModelList(merchantModelList)) {
            log.warn("Detected stale/corrupted merchant cache — evicting and rebuilding from DB");
            merchantRedisService.deleteMerchants(CacheKeys.MERCHANTS_MERCHANTMODEL);
            merchantModelList = null;
        }

        log.info("Redis merchant cache size: {}",
                merchantModelList != null ? merchantModelList.size() : 0);

        long totalMerch = this.userService.userCountByRole("MERCHANT");
        log.info("Total merchant count in DB: {}", totalMerch);

        if (merchantModelList == null || merchantModelList.isEmpty()) {
            merchantModelList = loadAllMerchantsFromDB();
            merchantRedisService.saveMerchantsMerchantModel(CacheKeys.MERCHANTS_MERCHANTMODEL, merchantModelList);
            log.info("Loaded {} merchants from DB and saved to Redis", merchantModelList.size());

        } else if (merchantModelList.size() < totalMerch) {
            log.info("Redis size ({}) < DB count ({}) — syncing missing merchants",
                    merchantModelList.size(), totalMerch);
            merchantModelList = syncMissingMerchants(merchantModelList);
        } else {
            log.info("Redis cache is up to date");
        }

        List<MerchantModel> filteredList = filterMerchants(merchantModelList, searchRequest);
        log.info("Filtered merchant list size: {}", filteredList.size());

        return ResponseModel.success("All MerchantModel list", filteredList);
    }

    @PostMapping("merchant/list")
    ResponseEntity<?> getAllMerchantList(@RequestBody SearchRequest searchRequest, Principal principal) {

        List<UserListModel> userListModels = merchantRedisService
                .getMerchantsUserListModel(CacheKeys.MERCHANTS_USERLIST);

        log.info("Redis UserListModel cache size: {}",
                userListModels != null ? userListModels.size() : 0);

        long totalMerch = this.userService.userCountByRole("MERCHANT");
        log.info("Total merchant count in DB: {}", totalMerch);

        String loginUserRole = this.userService.loginUserForData(principal.getName());
        boolean isReseller = loginUserRole.equalsIgnoreCase("R");

        if (userListModels == null || userListModels.isEmpty()) {
            // ✅ Redis empty — load from DB
            log.info("Redis empty — loading from DB");
            userListModels = loadAllUserListModelsFromDB(principal, isReseller);
            merchantRedisService.saveMerchantsUserListModel(CacheKeys.MERCHANTS_USERLIST, userListModels);
            log.info("Saved {} merchants to Redis", userListModels.size());

        } else if (userListModels.size() < totalMerch) {
            // ✅ Redis has fewer than DB — sync missing
            log.info("Redis size ({}) < DB count ({}) — syncing missing merchants",
                    userListModels.size(), totalMerch);
            userListModels = syncMissingUserListModels(userListModels, principal, isReseller);

        }else if (userListModels.size() > totalMerch) {
        // ✅ Redis has MORE than DB — stale entries for deleted merchants; prune them
        log.info("Redis size ({}) > DB count ({}) — pruning stale merchants",
                userListModels.size(), totalMerch);
        userListModels = pruneStaleUserListModels(userListModels, principal, isReseller);

        } else {
            log.info("Redis UserListModel cache is up to date");
        }

        return ResponseModel.success("All MerchantModel list", userListModels);
    }

    @GetMapping("merchant/{userId}")
    ResponseEntity<?> getMerchant(@PathVariable String userId) {
        User users = this.userService.userById(userId);

        MerchantModel merchantModel = new MerchantModel();
        merchantModel.setUserId(users.getUserId());
        merchantModel.setShortCode(users.getShortCode());
        merchantModel.setAppKey(users.getAppKey());
        merchantModel.setSecretKey(users.getSecretKey());
        merchantModel.setContactNumber(users.getContactNumber());
        merchantModel.setFullName(users.getFullName());
        merchantModel.setDateOfBirth(users.getDateOfBirth());
        merchantModel.setVerified(users.isVerified());
        merchantModel.setGender(users.getGender());
        merchantModel.setAddressDetails(users.getAddressDetails());
        merchantModel.setCreatedDate(users.getCreatedDate());
        merchantModel.setCreatedBy(users.getCreatedBy());
        merchantModel.setVerificationDate(users.getVerificationDate());

        BusinessDetails businessDetails = this.userService.getBusinessDetailsByUser(users);
        merchantModel.setBusinessName(businessDetails.getBusinessName());
        merchantModel.setGstVat(businessDetails.getGstVat());
        merchantModel.setPanSsn(businessDetails.getPanSsn());
        merchantModel.setWebsiteUrl(businessDetails.getWebsiteUrl());
        merchantModel.setBusinessType(businessDetails.getBusinessType());
        merchantModel.setBusinessSubType(businessDetails.getBusinessSubType());

        // ✅ Fixed duplicate calls
        merchantModel.setLoginLogo(this.userService.getImageByUserNameAndType(userId, UserImageTypes.LOGIN_LOGO) != null);
        merchantModel.setPageLogo(this.userService.getImageByUserNameAndType(userId, UserImageTypes.PAGE_LOGO) != null);
        merchantModel.setBrandLogo(this.userService.getImageByUserNameAndType(userId, UserImageTypes.BRAND_LOGO) != null);

        return ResponseModel.success("MerchantModel", merchantModel);
    }

    @PostMapping("resetPassword")
    ResponseEntity<?> resetPassword(@RequestBody Map<String, String> resetData, Principal principal) {
        if (!resetData.containsKey("password")) {
            return ResponseModel.error("New password should not be empty");
        }
        if (!authUtils.isValidPassowrd(resetData.get("password"))) {
            log.error("Invalid Password..");
            return ResponseModel.error("Invalid Password. Password should be at least 8 characters long and should contain at least one uppercase letter, one lowercase letter, one digit and one special character.");
        }
        User user = this.userService.userById(principal.getName());
        if (user != null) {
            try {
                this.userService.resetPassword(user, resetData.get("password"));
                return ResponseModel.success("Password reset successfully");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return ResponseModel.error("User Not found");
    }

    @GetMapping("test")
    ResponseEntity<?> whatsAppTest() {
        return ResponseModel.success("Successfully");
    }

    @GetMapping("GetRandomAESKey")
    ResponseEntity<?> getRandomAESKey() {
        log.info("Generating AES Key");
        return ResponseModel.success("Successfully", RandomStringGenerator.AESKeyGeneratorBase64());
    }

    // ─────────────────────────────────────────────────────
    // Private Helper Methods
    // ─────────────────────────────────────────────────────

    // ✅ Cheap shape check — does the list actually contain MerchantModel instances?
    private boolean isValidMerchantModelList(List<MerchantModel> list) {
        if (list == null || list.isEmpty()) {
            return true; // empty/null is fine — handled by the load-from-DB branch
        }
        try {
            Object first = list.get(0);
            return first instanceof MerchantModel;
        } catch (Exception e) {
            return false;
        }
    }

    // ✅ Load ALL merchants from DB (no pagination — full list)
    private List<MerchantModel> loadAllMerchantsFromDB() {
        List<User> allUsers = this.userService.getAllUsersListByRole("MERCHANT");
        List<MerchantModel> merchantModelList = new ArrayList<>();
        for (User usr : allUsers) {
            MerchantModel model = buildMerchantModel(usr);
            if (model != null) merchantModelList.add(model);
        }
        return merchantModelList;
    }

    // ✅ Sync missing MerchantModel entries
    private List<MerchantModel> syncMissingMerchants(List<MerchantModel> cachedList) {

        Set<String> cachedIds;
        try {
            cachedIds = cachedList.stream()
                    .map(MerchantModel::getUserId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        } catch (ClassCastException e) {
            log.error("Corrupted merchant cache detected in Redis — evicting and reloading from DB", e);
            merchantRedisService.deleteMerchants(CacheKeys.MERCHANTS_MERCHANTMODEL);
            List<MerchantModel> freshList = loadAllMerchantsFromDB();
            merchantRedisService.saveMerchantsMerchantModel(CacheKeys.MERCHANTS_MERCHANTMODEL, freshList);
            return freshList;
        }

        List<User> allUsersFromDB = this.userService.getAllUsersListByRole("MERCHANT");

        List<MerchantModel> missingMerchants = new ArrayList<>();
        for (User usr : allUsersFromDB) {
            if (!cachedIds.contains(usr.getUserId())) {
                MerchantModel model = buildMerchantModel(usr);
                if (model != null) {
                    missingMerchants.add(model);
                    log.info("Missing MerchantModel added: {}", usr.getUserId());
                }
            }
        }

        if (!missingMerchants.isEmpty()) {
            cachedList.addAll(missingMerchants);
            merchantRedisService.saveMerchantsMerchantModel(CacheKeys.MERCHANTS_MERCHANTMODEL, cachedList);
            log.info("Redis updated with {} total MerchantModels", cachedList.size());
        }

        return cachedList;
    }

    // ✅ Build MerchantModel from User entity
    private MerchantModel buildMerchantModel(User usr) {
        try {
            MerchantModel merchantModel = new MerchantModel();
            merchantModel.setUserId(usr.getUserId());
            merchantModel.setAppKey(usr.getAppKey());
            merchantModel.setSecretKey(usr.getSecretKey());
            merchantModel.setContactNumber(usr.getContactNumber());
            merchantModel.setFullName(usr.getFullName());
            merchantModel.setDateOfBirth(usr.getDateOfBirth());
            merchantModel.setVerified(usr.isVerified());
            merchantModel.setCreatedDate(usr.getCreatedDate());
            merchantModel.setCreatedBy(usr.getCreatedBy());
            merchantModel.setVerificationDate(usr.getVerificationDate());

            BusinessDetails businessDetails = this.userService.getBusinessDetailsByUser(usr);
            if (businessDetails != null) {
                merchantModel.setBusinessName(businessDetails.getBusinessName());
                merchantModel.setGstVat(businessDetails.getGstVat());
                merchantModel.setPanSsn(businessDetails.getPanSsn());
                merchantModel.setWebsiteUrl(businessDetails.getWebsiteUrl());
                merchantModel.setBusinessType(businessDetails.getBusinessType());
                merchantModel.setBusinessSubType(businessDetails.getBusinessSubType());
            }
            return merchantModel;
        } catch (Exception e) {
            log.error("Failed to build MerchantModel for userId={}: {}", usr.getUserId(), e.getMessage());
            return null;
        }
    }

    private ResponseEntity<?> persistMerchant(UserRegistrationModel merchant) {
        User newUser = new User();
        newUser.setUserId(merchant.getUserId());
        newUser.setContactNumber(merchant.getContactNumber());
        newUser.setFullName(merchant.getFullName());
        newUser.setPassword(merchant.getPassword());
        newUser.setRole("MERCHANT");
        newUser.setBusinessName(merchant.getBusinessName());
        newUser.setVerified(true);
        newUser.setEmailVerified(true);
        newUser.setContactVerified(true);
        newUser.setPayinEnabled(true);

        User createdUser = this.userService.createUser(newUser);
        persistBusinessDetails(merchant, createdUser);

        merchantRedisService.deleteMerchants(CacheKeys.MERCHANTS_MERCHANTMODEL);
        merchantRedisService.deleteMerchants(CacheKeys.MERCHANTS_USERLIST);
        log.info("Redis cache invalidated after merchant creation: {}", merchant.getUserId());

        sendOnboardingForRole(createdUser);
        return ResponseModel.created("Merchant created successfully");
    }

    private void persistBusinessDetails(UserRegistrationModel merchant, User createdUser) {
        BusinessDetails businessDetails = new BusinessDetails();
        businessDetails.setBusinessName(merchant.getBusinessName());
        businessDetails.setPanSsn(merchant.getPanSsn());
        businessDetails.setGstVat(merchant.getGstVat());
        businessDetails.setWebsiteUrl(merchant.getWebsite());
        businessDetails.setUser(createdUser);
        businessDetails.setBusinessType(merchant.getBusinessType());
        businessDetails.setBusinessSubType(
                merchant.getBusinessSubType() != null && !merchant.getBusinessSubType().isBlank()
                        ? merchant.getBusinessSubType()
                        : merchant.getBusinessType());
        businessDetails.setBusinessEmail(merchant.getUserId());
        businessDetails.setPhone(merchant.getContactNumber());
        this.userService.addUpdateBusinessDetails(businessDetails);
    }

    private void sendOnboardingForRole(User created) {
        try {
            String role = created.getRole() == null ? "" : created.getRole();
            if (role.equalsIgnoreCase("MERCHANT")) {
                this.notifications.sendOnboardingMerchant(created);
            } else if (role.equalsIgnoreCase("SUBADMIN")) {
                this.notifications.sendOnboardingSubAdmin(created);
            } else if (role.equalsIgnoreCase("RESELLER")) {
                this.notifications.sendOnboardingReseller(created);
            } else if (role.equalsIgnoreCase("SUBMERCHANT")) {
                this.notifications.sendOnboardingSubMerchant(created);
            } else {
                this.notifications.sendAdminOnboard(created);
            }
        } catch (Exception e) {
            log.warn("Onboarding email skipped for {}: {}", created.getUserId(), e.getMessage());
        }
    }

    // ✅ Filter MerchantModel list by SearchRequest
    private List<MerchantModel> filterMerchants(List<MerchantModel> list, SearchRequest searchRequest) {
        if (searchRequest == null) return list;

        return list.stream()
                .filter(m -> {
                    // Filter by userId
                    if (searchRequest.getUserName() != null && !searchRequest.getUserName().isEmpty()) {
                        return m.getUserId() != null &&
                                m.getUserId().equalsIgnoreCase(searchRequest.getUserName());
                    }
                    // Filter by keyword (name or business name)
                    if (searchRequest.getKeyword() != null && !searchRequest.getKeyword().isEmpty()) {
                        String kw = searchRequest.getKeyword().toLowerCase();
                        boolean nameMatch = m.getFullName() != null &&
                                m.getFullName().toLowerCase().contains(kw);
                        boolean bizMatch  = m.getBusinessName() != null &&
                                m.getBusinessName().toLowerCase().contains(kw);
                        return nameMatch || bizMatch;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    // ✅ Load all UserListModels from DB
    private List<UserListModel> loadAllUserListModelsFromDB(Principal principal, boolean isReseller) {
        List<User> allMerchants;
        if (isReseller) {
            allMerchants = this.resellerMappingService
                    .getAllMappedMerchantByReseller(new User(principal.getName()));
        } else {
            allMerchants = this.userService.getAllUsersListByRole("MERCHANT");
        }

        List<UserListModel> userListModels = this.mapper.map(
                allMerchants,
                new TypeToken<List<UserListModel>>() {}.getType()
        );

        return userListModels.stream()
                .sorted(Comparator.comparing(UserListModel::getCreatedDate).reversed())
                .collect(Collectors.toList());
    }
    // ✅ Remove UserListModel entries that no longer exist in the DB
    private List<UserListModel> pruneStaleUserListModels(
            List<UserListModel> cachedList,
            Principal principal,
            boolean isReseller) {

        // Build the authoritative set of userIds currently in the DB
        List<User> allMerchantsFromDB;
        if (isReseller) {
            allMerchantsFromDB = this.resellerMappingService
                    .getAllMappedMerchantByReseller(new User(principal.getName()));
        } else {
            allMerchantsFromDB = this.userService.getAllUsersListByRole("MERCHANT");
        }

        Set<String> dbUserIds = allMerchantsFromDB.stream()
                .map(User::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        int before = cachedList.size();

        // Keep only cached entries that still exist in the DB
        List<UserListModel> pruned = cachedList.stream()
                .filter(m -> m.getUserId() != null && dbUserIds.contains(m.getUserId()))
                .sorted(Comparator.comparing(UserListModel::getCreatedDate).reversed())
                .collect(Collectors.toList());

        int removed = before - pruned.size();
        if (removed > 0) {
            merchantRedisService.saveMerchantsUserListModel(CacheKeys.MERCHANTS_USERLIST, pruned);
            log.info("Pruned {} stale UserListModel(s) — Redis now has {} entries", removed, pruned.size());
        } else {
            log.info("No stale UserListModels found despite size mismatch — nothing pruned");
        }

        return pruned;
    }
    // ✅ Sync missing UserListModel entries
    private List<UserListModel> syncMissingUserListModels(
            List<UserListModel> cachedList,
            Principal principal,
            boolean isReseller) {

        Set<String> cachedUserIds = cachedList.stream()
                .map(UserListModel::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<User> allMerchantsFromDB;
        if (isReseller) {
            allMerchantsFromDB = this.resellerMappingService
                    .getAllMappedMerchantByReseller(new User(principal.getName()));
        } else {
            allMerchantsFromDB = this.userService.getAllUsersListByRole("MERCHANT");
        }

        List<User> missingUsers = allMerchantsFromDB.stream()
                .filter(u -> !cachedUserIds.contains(u.getUserId()))
                .collect(Collectors.toList());

        if (!missingUsers.isEmpty()) {
            log.info("Found {} missing UserListModels — adding to Redis", missingUsers.size());

            List<UserListModel> missingModels = this.mapper.map(
                    missingUsers,
                    new TypeToken<List<UserListModel>>() {}.getType()
            );

            cachedList.addAll(missingModels);
            cachedList = cachedList.stream()
                    .sorted(Comparator.comparing(UserListModel::getCreatedDate).reversed())
                    .collect(Collectors.toList());

            merchantRedisService.saveMerchantsUserListModel(CacheKeys.MERCHANTS_USERLIST, cachedList);
            log.info("Redis updated with {} total UserListModels", cachedList.size());
        } else {
            log.info("No missing UserListModels — Redis is up to date");
        }

        return cachedList;
    }
}