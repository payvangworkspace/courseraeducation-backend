package com.pv.couseae.Dtos;

import lombok.*;

@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateKeyReq {
    public String merchantId;
    public String allowedIp;
//    public Long ttlSeconds;
}
