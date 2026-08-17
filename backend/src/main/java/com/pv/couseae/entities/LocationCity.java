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
@NoArgsConstructor
@AllArgsConstructor
@Document( "LocationCity")
public class LocationCity extends Tracker {
	
	@Id
	private String cityId;

	private String cityName;

	private String cityCode;

	@DBRef
	@JsonIgnoreProperties({"createdDate","createdBy","lastModifiedDate","lastModifiedBy","status",})
	private LocationState state;

	public LocationCity(String cityId) {
		this.cityId = cityId;
	}
}
