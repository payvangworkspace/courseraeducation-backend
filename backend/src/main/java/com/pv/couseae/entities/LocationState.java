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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document( "LocationState")
public class LocationState extends Tracker {
	@Id
	private String stateId;
	private String stateName;
	private String stateCode;

	@DBRef
	@JsonIgnoreProperties({"createdDate","createdBy","lastModifiedDate","lastModifiedBy","status",})
	private LocationCountry country;

	public LocationState(String stateId) {
		this.stateId = stateId;
	}
}
