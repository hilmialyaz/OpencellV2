package org.meveo.api.ws.impl;

import org.meveo.api.dto.billing.CalculateTerminationPenaltyRequestDto;
import org.meveo.api.dto.billing.CalculateTerminationPenaltyResponseDto;
import org.meveo.api.logging.WsRestApiInterceptor;
import org.meveo.api.ws.BillingCustomWs;

import javax.interceptor.Interceptors;
import javax.jws.WebService;
import java.math.BigDecimal;

@WebService(serviceName = "BillingCustomWs", endpointInterface = "org.meveo.api.ws.BillingCustomWs")
@Interceptors({WsRestApiInterceptor.class})
public class BillingCustomWsImpl extends BaseWs implements BillingCustomWs {


    @Override
    public CalculateTerminationPenaltyResponseDto calculateTerminationPenalty(CalculateTerminationPenaltyRequestDto postData) {
        CalculateTerminationPenaltyResponseDto resp = new CalculateTerminationPenaltyResponseDto();
        resp.setFee(new BigDecimal(postData.getSubscriptionCode().length() ) );

        return resp;
    }
}