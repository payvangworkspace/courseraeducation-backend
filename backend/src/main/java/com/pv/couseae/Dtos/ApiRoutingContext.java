package com.pv.couseae.Dtos;

import com.pv.couseae.entities.ApiMaster;
import com.pv.couseae.entities.MerchantAggregatorMapping;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiRoutingContext {
    private MerchantAggregatorMapping aggregatorMapping;
    private ApiMaster apiMaster;
}
