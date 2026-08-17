package com.pv.couseae.controller;

import com.pv.couseae.entities.UserActivity;
import com.pv.couseae.services.UserService;
import com.pv.couseae.utill.ResponseModel;
import com.pv.couseae.utill.SearchRequest;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
//@CrossOrigin
@RequestMapping("/userActivity")
@AllArgsConstructor
public class UserActivityController {
    private UserService userService;

    @PostMapping
    public ResponseEntity<?> getUserActivity(@RequestBody SearchRequest searchRequest, Principal principal){
        if (!this.userService.isAdmin(principal.getName())|| searchRequest.getUserName()==null ||searchRequest.getUserName().isEmpty())
            searchRequest.setUserName(principal.getName());
        Page<UserActivity> getActivityList = this.userService.getActivity(searchRequest);
        return ResponseModel.success("User Activity",getActivityList);
    }
}
