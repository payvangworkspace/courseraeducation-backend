package com.pv.couseae.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pv.couseae.Dtos.UserCacheDTO;
import com.pv.couseae.entities.*;
import com.pv.couseae.enums.UserImageTypes;
import com.pv.couseae.enums.UserProcessingMode;
import com.pv.couseae.enums.UserRoles;
import com.pv.couseae.handlers.UserNotFoundException;
import com.pv.couseae.mappers.UserCacheMapper;
import com.pv.couseae.model.UserListModel;
import com.pv.couseae.repos.*;
import com.pv.couseae.utill.*;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private final UserRepo userRepository;
    private final AddressDetailsRepo addressDetailsRepo;
    private final BusinessDetailsRepo businessDetailsRepo;
    private final DocumentsRepo documentsRepo;
    private final BankDetailsRepo bankDetailsRepo;
    private final ModelMapper mapper;
    private final ObjectMapper objectMapper;
    private final UserImageRepo userImageRepo;
    private final UserActivityRepo activityRepo;
    private final MongoTemplate mongoTemplate;
    private final RedisTemplate<String, UserCacheDTO> userRedis;
    private final AuthUtils authUtils;
    private final MerchantRedisService merchantRedisService;

    public UserServiceImpl(UserRepo userRepository,
                           AddressDetailsRepo addressDetailsRepo,
                           BusinessDetailsRepo businessDetailsRepo,
                           DocumentsRepo documentsRepo,
                           BankDetailsRepo bankDetailsRepo,
                           ModelMapper mapper,
                           @Qualifier("plainObjectMapper") ObjectMapper objectMapper,
                           UserImageRepo userImageRepo,
                           UserActivityRepo activityRepo,
                           MongoTemplate mongoTemplate,
                           @Qualifier("userCacheRedisTemplate") RedisTemplate<String, UserCacheDTO> userRedis,
                           AuthUtils authUtils,
                           MerchantRedisService merchantRedisService) {
        this.userRepository = userRepository;
        this.addressDetailsRepo = addressDetailsRepo;
        this.businessDetailsRepo = businessDetailsRepo;
        this.documentsRepo = documentsRepo;
        this.bankDetailsRepo = bankDetailsRepo;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
        this.userImageRepo = userImageRepo;
        this.activityRepo = activityRepo;
        this.mongoTemplate = mongoTemplate;
        this.userRedis = userRedis;
        this.authUtils = authUtils;
        this.merchantRedisService = merchantRedisService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Fallback to DB
        User user = this.userRepository.findByUserId(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        // Save in Redis
        UserCacheDTO dto = UserCacheMapper.toDto(user);
        userRedis.opsForValue().set(authUtils.getRedisUserKey(username), dto);
        UserCacheDTO cachedUser1 = userRedis.opsForValue().get(authUtils.getRedisUserKey(username));
        log.info("the cached user ---->" + cachedUser1);

        return user;
    }

    @Override
    public UserDetails loadUserByUsernameAndAppId(String username, String appId) {
        return this.userRepository.findByUserIdAndAppKeyCaseSensitive(username, appId);
    }

    @Override
    public UserDetails loadUserByAppIdAndSecretKey(String merchantAppId, String merchantSecretId) {
        return this.userRepository.findByAppKeyAndSecretKey(merchantAppId, merchantSecretId);
    }

    @Override
    public List<Document> getRoleCountByCreatedBy(String createdBy) {
        log.info("Inside getRoleCountByCreatedBy method -->" + createdBy);
        MatchOperation match = Aggregation.match(Criteria.where("createdBy").is(createdBy));
        GroupOperation group = Aggregation.group("role").count().as("count");
        Aggregation aggregation = Aggregation.newAggregation(match, group);
        AggregationResults<Document> results = mongoTemplate.aggregate(
                aggregation, "users", Document.class);
        return results.getMappedResults();
    }

    @Override
    public UserDetailsService userDetailsService() {
        return username -> {
            // 1️⃣ Fetch password ONLY from DB
            User user = userRepository.findByUserId(username);
            if (user == null) {
                throw new UsernameNotFoundException("User not found");
            }

            // 2️⃣ Cache SAFE data
            UserCacheDTO dto = UserCacheMapper.toDto(user);
            userRedis.opsForValue().set(authUtils.getRedisUserKey(username), dto);
            log.info("The user details before setting Spring Security:" + user);
            UserCacheDTO cachedUser = userRedis.opsForValue().get(authUtils.getRedisUserKey(username));
            log.info("the cached user ---->" + cachedUser);

            // 3️⃣ Return UserDetails (password NEVER from Redis)
            return user;
        };
    }

    @Override
    public UserCacheDTO CacheuserById(String email) {
        String key = authUtils.getRedisUserKey(email);
        UserCacheDTO cached = userRedis.opsForValue().get(key);
        if (cached != null) {
            log.info("User loaded from Redis");
            return cached;
        }
        User user = userRepository.findByUserId(email);
        if (user == null) {
            return null;
        }
        UserCacheDTO dto = UserCacheMapper.toDto(user);
        userRedis.opsForValue().set(key, dto);
        return dto;
    }

    @Override
    public User userById(String email) {
        log.info("Return Response from DB not from Redis Cache...");
        User user = this.userRepository.findByUserId(email);
        if (user == null) {
            return null;
        }
        UserCacheDTO dto = UserCacheMapper.toDto(user);
        userRedis.opsForValue().set(authUtils.getRedisUserKey(email), dto);
        UserCacheDTO cachedUser1 = userRedis.opsForValue().get(authUtils.getRedisUserKey(email));
        log.info("the cached user ---->" + cachedUser1);
        return user;
    }

    @Override
    public User createUser(User userRequest) {
        userRequest.setProcessingMode(UserProcessingMode.AUTH.toString());
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        userRequest.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        String key = "CoUrSe" + DateTimeUtils.dateToken();
        userRequest.setAppKey(key);
        userRequest.setSecretKey(RandomStringGenerator.AESKeyGeneratorBase64());
        if (userRequest.getCreatedBy() == null || userRequest.getCreatedBy().isEmpty())
            userRequest.setCreatedBy(AuthUtils.authUser());
        if (userRequest.getCreatedDate() == null)
            userRequest.setCreatedDate(AuthUtils.createdDate());

        // ✅ Single save
        User savedUser = this.userRepository.save(userRequest);

        // ✅ If new user is a merchant, update Redis cache
        if ("MERCHANT".equalsIgnoreCase(savedUser.getRole())) {
            updateMerchantCache(savedUser);
        }

        return savedUser;
    }

    @Override
    public void updateUser(User currentUser) {
        if (currentUser.getCreatedBy() == null || currentUser.getCreatedBy().isEmpty())
            currentUser.setCreatedBy(AuthUtils.authUser());
        if (currentUser.getCreatedDate() == null)
            currentUser.setCreatedDate(AuthUtils.createdDate());

        User savedUser = this.userRepository.save(currentUser);

        // ✅ Update Redis merchant cache if merchant
        if ("MERCHANT".equalsIgnoreCase(savedUser.getRole())) {
            updateMerchantCache(savedUser);
        }
        // Update per-user Redis cache (fire-and-forget)
        UserCacheDTO dto = UserCacheMapper.toDto(savedUser);
        userRedis.opsForValue().set(authUtils.getRedisUserKey(savedUser.getUsername()), dto);
    }

    @Override
    public AddressDetails addAddressDetails(AddressDetails addressDetails) {
        return this.addressDetailsRepo.save(addressDetails);
    }

    @Override
    public void addUpdateBusinessDetails(BusinessDetails businessDetails) {
        this.businessDetailsRepo.save(businessDetails);
    }

    @Override
    public void addDocuments(Documents documents) {
        this.documentsRepo.save(documents);
    }

    @Override
    public BankDetails getBankDetailsByAccountOrCardNumberAndUser(String bankAccountNumber, String cardNumber, User user) {
        return this.bankDetailsRepo.findByUserAndBankAccountNumberOrUserAndCardNumber(user, bankAccountNumber, user, cardNumber);
    }

    @Override
    public BankDetails addBankDetails(BankDetails bankDetails) {
        return this.bankDetailsRepo.save(bankDetails);
    }

    @Override
    public BusinessDetails getBusinessDetailsByUser(User user) {
        return this.businessDetailsRepo.findByUser(user);
    }

    @Override
    public BusinessDetails getBusinessDetailsById(String id) {
        return this.businessDetailsRepo.findById(id).orElse(null);
    }

    @Override
    public Documents getDocumentByTypeAndUser(String documentType, User user) {
        return this.documentsRepo.findByDocumentTypeAndUser(documentType, user);
    }

    @Override
    public List<Documents> getDocumentByUser(User user) {
        return this.documentsRepo.findAllByUser(user);
    }

    @Override
    public Documents getDocumentById(String documentId) {
        return this.documentsRepo.findById(documentId).orElse(null);
    }

    @Override
    public void deleteBankDetailsById(String bankDetailId, String name) {
        this.bankDetailsRepo.deleteByBankDetailIdAndUser(bankDetailId, new User(name));
    }

    @Override
    public Page<User> getAllUsersByRole(String roleName, SearchRequest searchRequest) {
        Pageable pageable = PageRequest.of(searchRequest.getStart(), searchRequest.getSize());
        if (!searchRequest.getKeyword().isEmpty()) {
            return this.userRepository.findAllByRoleAndFullNameLikeIgnoreCaseOrderByFullName(roleName, searchRequest.getKeyword(), pageable);
        } else return this.userRepository.findAllByRoleOrderByFullName(roleName, pageable);
    }

    @Override
    public Page<User> getAllUsersByRole(String roleName, SearchRequest searchRequest, String user) {
        Pageable pageable = PageRequest.of(searchRequest.getStart(), searchRequest.getSize());
        if (!searchRequest.getKeyword().isEmpty()) {
            return this.userRepository.findAllByRoleAndFullNameLikeIgnoreCaseAndCreatedByOrderByFullName(roleName, searchRequest.getKeyword(), user, pageable);
        } else return this.userRepository.findAllByRoleAndCreatedByOrderByFullName(roleName, user, pageable);
    }

    @Override
    public Page<User> getAllUsers(SearchRequest searchRequest) {
        Pageable pageable = PageRequest.of(searchRequest.getStart(), searchRequest.getSize());
        if (!searchRequest.getKeyword().isEmpty()) {
            return this.userRepository.findAllByFullNameLikeIgnoreCaseOrderByFullName(searchRequest.getKeyword(), pageable);
        } else return this.userRepository.findAll(pageable);
    }

    @Override
    public boolean isAdmin(String userId) {
        User user = this.userRepository.findByUserId(userId);
        if (user == null) throw new UserNotFoundException("User not found");
        return user.getRole().equalsIgnoreCase("ADMIN") || user.getRole().equalsIgnoreCase("SUBADMIN");
    }

    @Override
    public boolean isMerchant(String userId) {
        User user = this.userRepository.findByUserId(userId);
        if (user == null) throw new UserNotFoundException("User not found");
        return user.getRole().equalsIgnoreCase("MERCHANT");
    }

    @Override
    public String loginUserForData(String userId) {
        User user = this.userRepository.findByUserId(userId);
        log.info("User Role is : " + user.getRole());
        if (user.getRole().equalsIgnoreCase(UserRoles.RESELLER.toString()))
            return "R";
        else if (user.getRole().equalsIgnoreCase(UserRoles.MERCHANT.toString()))
            return user.getUserId();
        else if (user.getRole().equalsIgnoreCase(UserRoles.SUBMERCHANT.toString())) {
            return user.getCreatedBy();
        } else return "";
    }

    @Override
    public String loginUserRole(String userId) {
        User user = this.userRepository.findByUserId(userId);
        log.info("User Role is : " + user.getRole());
        return user.getRole();
    }

    @Override
    public User getUserByAppId(String appKey) {
        return this.userRepository.findByAppKey(appKey);
    }

    @Override
    public User getUserByAppIdAndSecretKey(String merchantAppId, String merchantSecretId) {
        return this.userRepository.findByAppKeyAndSecretKey(merchantAppId, merchantSecretId);
    }

    @Override
    public User getUserDetails(String appId, String secretKey) {
        log.info("🔍 Fetching user for appKey={} secretKey=****", appId);
        return userRepository.findByAppKeyAndSecretKey(appId, secretKey);
    }

    @Override
    public void resetPassword(User user, String password) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(password));
        this.userRepository.save(user);
    }

    @Override
    public UsersImages getImageByUserNameAndType(String userName, UserImageTypes userImageTypes) {
        return this.userImageRepo.findByUserNameAndUserImageType(userName, userImageTypes);
    }

    @Override
    public void saveUserImages(UsersImages brandLogo) {
        this.userImageRepo.save(brandLogo);
    }

    @Override
    public void createUserActivity(String userName) {
        UserActivity userActivity = new UserActivity();
        userActivity.setCreatedBy(userName);
        userActivity.setCreatedDate(LocalDateTime.now());
        this.activityRepo.save(userActivity);
    }

    @Override
    public Page<UserActivity> getActivity(SearchRequest searchRequest) {
        Pageable pageable = PageRequest.of(searchRequest.getStart(), searchRequest.getSize());
        return this.activityRepo.findAllByCreatedBy(searchRequest.getUserName(), pageable);
    }

    @Override
    public List<User> getAllUsersListByRole(String merchant) {
        return this.userRepository.findByRoleOrderByFullName(merchant);
    }

    @Override
    public long userCountByRole(String merchantRole) {
        return this.userRepository.countByRole(merchantRole);
    }

    @Override
    public BankDetails getBankDetailsById(String bankDetailId) {
        return this.bankDetailsRepo.findById(bankDetailId).orElse(null);
    }

    // ─────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────

    private void updateMerchantCache(User merchant) {
        String cacheKey = CacheKeys.MERCHANTS_USERLIST;

        List<UserListModel> cachedList = merchantRedisService.getMerchantsUserListModel(cacheKey);
        if (cachedList == null) {
            cachedList = new ArrayList<>();
        }

        // ModelMapper maps matching fields only — ignores User's accountBalance/password/etc.
        UserListModel model = this.mapper.map(merchant, UserListModel.class);

        cachedList.removeIf(u -> u.getUserId() != null && u.getUserId().equals(model.getUserId()));
        cachedList.add(model);

        // Null-safe sort: a legacy entry with a null createdDate won't NPE — nulls go last.
        cachedList.sort(
                Comparator.comparing(UserListModel::getCreatedDate,
                        Comparator.nullsLast(Comparator.reverseOrder())));

        merchantRedisService.saveMerchantsUserListModel(cacheKey, cachedList);

        log.info("Merchant {} added/updated in Redis cache, total size={}", model.getUserId(), cachedList.size());
    }
}