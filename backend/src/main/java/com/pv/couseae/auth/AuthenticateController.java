package com.pv.couseae.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pv.couseae.entities.User;
import com.pv.couseae.security.JwtService;
import com.pv.couseae.services.RSAKeyService;
import com.pv.couseae.services.RedisBlacklistService;
import com.pv.couseae.services.UserService;
import com.pv.couseae.utill.AESUtil;
import com.pv.couseae.utill.RSAUtil;
import com.pv.couseae.utill.ResponseModel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
//@CrossOrigin
@AllArgsConstructor
public class AuthenticateController {
    @Autowired
    private Environment environment;
    private AuthenticationManager authenticationManager;
    private JwtService jwtService;
    private UserService userService;
    private ModelMapper mapper;
    @Autowired
    private RSAKeyService rsaKeyService;
    @Autowired
    private RSAUtil rsaUtil;
    private AESUtil AESUtil;
    // final TokenBlacklist blacklist;
    private final RedisBlacklistService blacklistService;
    /**
     * Generate token and user details
     *
     * @param -loginRequest - userName and password
     * @return userDetails with token
     */

    @PostMapping("/GetTestToken")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        log.info("Received login request username: "+loginRequest.getUserName()+", password: "+loginRequest.getPassword());
        ResponseEntity<?> auth = authenticateUser(loginRequest.getUserName(), loginRequest.getPassword());
        if (auth != null) return auth;
        String profile = environment.getProperty("spring.profiles.active");
        log.info("Active profile: "+profile+" and env is ->"+ environment.getActiveProfiles()[0]);
        if (profile.equals("demo")) {
            UserDetails userDetails = this.userService.loadUserByUsername(loginRequest.getUserName());

            String token = this.jwtService.generateToken(userDetails);
            // this.userService.createUserActivity(loginRequest.getUserName());
            final User user = this.userService.userById(loginRequest.getUserName());
            Map<String, String> respData = new HashMap<>();
            respData.put("token", token);
            respData.put("userId", userDetails.getUsername());
            respData.put("userStatus", String.valueOf(user.isStatus()));
            return ResponseEntity.ok(respData);
        } else {
            Map<String, String> respData = new HashMap<>();
            respData.put("token", "You are not authorized to access this API at Production environment.");
            //respData.put("userId", userDetails.getUsername());
            return ResponseEntity.ok(respData);
        }

    }
    @PostMapping("/generate-token")
    public ResponseEntity<?> generateTokenValue(@RequestBody Map<String, String> payload) throws Exception {
        String encData = payload.get("data");
        String encryptedAESKey = payload.get("aesKey");

        log.info("Received encrypted data: "+encData);
        // 1️⃣ Decrypt AES key
        String aesKey = rsaUtil.decryptAESKey(encryptedAESKey);
        log.info("aesKey: "+aesKey);
        // 2️⃣ Decrypt request data
        String decryptedJson = rsaUtil.decryptAES(encData, aesKey);


        System.out.println("Decrypted request: " + decryptedJson);
        ObjectMapper objectMapper = new ObjectMapper();
        LoginRequest loginRequest = objectMapper.readValue(decryptedJson, LoginRequest.class);
        //log.info("Received login request username: "+loginRequest.getUserName()+", password: "+loginRequest.getPassword());
        ResponseEntity<?> auth = authenticateUser(loginRequest.getUserName(), loginRequest.getPassword());
        if (auth != null) return auth;
        UserDetails userDetails = this.userService.loadUserByUsername(loginRequest.getUserName());
        String token = this.jwtService.generateToken(userDetails);
        this.userService.createUserActivity(loginRequest.getUserName());
        final User user = this.userService.userById(loginRequest.getUserName());
        boolean ispayoutEnableViaApp= user.isPayoutEnabledViaApp()? user.isPayoutEnabledViaApp() : false;
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(LoginResponse.builder().email(user.getUserId()).fullName(user.getFullName()).contactNumber(user.getContactNumber())
                .userRole(user.getRole()).isVerified(user.isVerified()).isPayoutEnabledViaApp(ispayoutEnableViaApp).token(token).build());
        //log.info("jsonString token: "+jsonString);
        String encservdata=rsaUtil.encryptAES(jsonString, aesKey);
        log.info("encservdata: "+encservdata);
        String decData=rsaUtil.decryptAES(encservdata, aesKey);
        log.info("decData: "+decData);
        return ResponseEntity.ok(encservdata);

    }

    private ResponseEntity<?> authenticateUser(String userName, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userName, password));
        } catch (UsernameNotFoundException e) {
            return ResponseModel.error("User not found");
        } catch (LockedException e) {
            return ResponseModel.error("Your Account has been locked, Please connect with administrator");
        } catch (AccountExpiredException e) {
            return ResponseModel.error("Your Account has been suspended, Please connect with administrator");
        } catch (CredentialsExpiredException e) {
            return ResponseModel.error("Your Credentials has been expired, Please connect with administrator");
        } catch (DisabledException e) {
            return ResponseModel.error("Your Account has been disabled, Please connect with administrator");
        } catch (BadCredentialsException e) {
            return ResponseModel.error("Invalid Credentials");
        }
        return null;
    }
    @PostMapping("/logoutuser")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token) {
        //blacklist.BlacklistTokenCleanup();
        String jwt = token.replace("Bearer ", "");
        log.info("Received Req for logout to user {}: "+this.jwtService.extractUserName(jwt));
        long expiryTime = this.jwtService.getExpiryTime(jwt); // extract `exp` from JWT
        //blacklist.blacklistToken(jwt, expiryTime * 1000); // store in blacklist
        blacklistService.blacklist(jwt, expiryTime);
        return ResponseEntity.ok("Logged out successfully");
    }
    @GetMapping("/TestUrl")
    public String testUrl() {
        return "Zenithpay API TestUrl is Working....!";
    }

}
