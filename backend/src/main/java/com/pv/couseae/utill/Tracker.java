package com.pv.couseae.utill;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public abstract class Tracker {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMM yyyy  hh:mm:ss a")
    @Field(value = "createdDate")
    @CreatedDate
    private LocalDateTime createdDate;

    @Field("createdBy")
    @CreatedBy
    private String createdBy;

    @LastModifiedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMM yyyy  hh:mm:ss a")
    private LocalDateTime lastModifiedDate;
    @LastModifiedBy
    private String lastModifiedBy;

    private boolean status = true;

    public LocalDateTime getCreatedDate() {
        if (this.createdDate==null)
            return AuthUtils.createdDate();
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        if (createdDate==null)
            this.createdDate = AuthUtils.createdDate();
        else this.createdDate = createdDate;
    }

    public String getCreatedBy() {
        if (this.createdBy==null|| this.createdBy.isEmpty())
            return AuthUtils.authUser();
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        if (createdBy==null || createdBy.isEmpty())
            this.createdBy =  AuthUtils.authUser();
        else this.createdBy = createdBy;
    }
}
