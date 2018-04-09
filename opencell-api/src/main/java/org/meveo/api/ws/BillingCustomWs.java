package org.meveo.api.ws;

import org.meveo.api.dto.billing.CalculateTerminationPenaltyRequestDto;
import org.meveo.api.dto.billing.CalculateTerminationPenaltyResponseDto;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 * Created by kemala on 9.04.2018.
 */
@WebService
public interface BillingCustomWs {

    @WebMethod
    CalculateTerminationPenaltyResponseDto calculateTerminationPenalty(@WebParam(name = "customer") CalculateTerminationPenaltyRequestDto postData);

}
