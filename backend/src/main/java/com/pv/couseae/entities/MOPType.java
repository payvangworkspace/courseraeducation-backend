package com.pv.couseae.entities;

import com.pv.couseae.utill.Tracker;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document("mopType")
public class MOPType extends Tracker {
    @Id
    private String mopTypeId;
    private String mopTypeName;
    private String mopTypeCode;

    public MOPType(String mopTypeId) {
        this.mopTypeId = mopTypeId;
    }
}
