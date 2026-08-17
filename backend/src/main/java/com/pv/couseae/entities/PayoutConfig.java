package com.pv.couseae.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pv.couseae.utill.Tracker;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PayoutConfig extends Tracker {

    @Id
    private String payoutConfigId;
    @DBRef
    @JsonIgnoreProperties({"contactNumber","dateOfBirth","gender","accountNonLocked","accountNonExpired","credentialsNonExpired"})
    private User user;
}
