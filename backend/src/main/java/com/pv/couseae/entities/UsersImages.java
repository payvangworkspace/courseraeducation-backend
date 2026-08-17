package com.pv.couseae.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pv.couseae.enums.UserImageTypes;
import com.pv.couseae.utill.Tracker;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Document
public class UsersImages extends Tracker {
    @Id
    private String imageId;

    private UserImageTypes userImageType;

    private String imageName;

    private String imageType;

    @JsonIgnore
    private byte[] image;

    private String userName;
}
