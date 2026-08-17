package com.pv.couseae.entities;

import com.pv.couseae.utill.Tracker;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document("locationCountry")
public class LocationCountry extends Tracker {

    @Id
    private String countryId;

    @NotNull(message = "Country Name should not be empty")
    private String countryName;

    private String countryCapital;
    private String countryCode;
    private String countryPhoneCode;
    private String countryNumericCode;
    private String emoji;
    private String emojiU;
    public LocationCountry(String countryId) {
        this.countryId = countryId;
    }
}
