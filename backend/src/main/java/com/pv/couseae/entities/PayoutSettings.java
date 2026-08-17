package com.pv.couseae.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pv.couseae.utill.Tracker;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Document()
public class PayoutSettings extends Tracker {

    @Id
    private String payoutSettingsId;

    @DBRef
    @JsonIgnoreProperties({"contactNumber","dateOfBirth","gender","accountNonLocked","accountNonExpired","credentialsNonExpired"})
    private User user;

    @DBRef
    @JsonIgnoreProperties({"contactNumber","dateOfBirth","gender","accountNonLocked","accountNonExpired","credentialsNonExpired"})
    private User acquirer;
    private String AcquirerProfile;

    private int acquirerPriority;
    private int	acquirerProfilePriority;

    private double minimumAmount;
    private double maximumAmount;
}
