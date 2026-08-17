package com.pv.couseae.services;

import com.pv.couseae.Dtos.UserCacheDTO;
import com.pv.couseae.entities.*;
import com.pv.couseae.enums.UserImageTypes;
import com.pv.couseae.utill.SearchRequest;
import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService {
    UserDetails loadUserByUsername(String username);
    UserDetails loadUserByUsernameAndAppId(String username, String appId);
    UserDetails loadUserByAppIdAndSecretKey(String merchantAppId, String merchantSecretId);

    List<Document> getRoleCountByCreatedBy(String createdBy);

    UserDetailsService userDetailsService();

    UserCacheDTO CacheuserById(String email);

    User userById(String email);

    User createUser(User userRequest);

    void updateUser(User currentUser);

    AddressDetails addAddressDetails(AddressDetails addressDetails);

    void addUpdateBusinessDetails(BusinessDetails businessDetails);

    void addDocuments(Documents documents);

    BankDetails getBankDetailsByAccountOrCardNumberAndUser(String bankAccountNumber, String cardNumber, User user);

    BankDetails addBankDetails(BankDetails bankDetails);

    BusinessDetails getBusinessDetailsByUser(User user);

    BusinessDetails getBusinessDetailsById(String id);

    Documents getDocumentByTypeAndUser(String documentType, User user);

    List<Documents> getDocumentByUser(User user);

    Documents getDocumentById(String documentId);

    void deleteBankDetailsById(String bankDetailId, String name);

    Page<User> getAllUsersByRole(String roleName, SearchRequest searchRequest,String user);
    Page<User> getAllUsersByRole(String roleName, SearchRequest searchRequest);

    Page<User> getAllUsers(SearchRequest searchRequest);

    boolean isAdmin(String userId);

    boolean isMerchant(String userId);

    String loginUserForData(String userId);

    String loginUserRole(String userId);

    User getUserByAppId(String appKey);

    User getUserByAppIdAndSecretKey(String merchantAppId, String merchantSecretId);


    User getUserDetails(String appId, String secretKey);

    void resetPassword(User user, String password);

    UsersImages getImageByUserNameAndType(String userName, UserImageTypes userImageTypes);

    void saveUserImages(UsersImages brandLogo);

    void createUserActivity(String userName);

    Page<UserActivity> getActivity(SearchRequest searchRequest);

    List<User> getAllUsersListByRole(String merchant);

    long userCountByRole(String merchantRole);

    BankDetails getBankDetailsById(String bankDetailId);
}
