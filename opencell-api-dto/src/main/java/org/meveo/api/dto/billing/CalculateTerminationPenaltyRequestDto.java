package org.meveo.api.dto.billing;

import org.meveo.api.dto.BaseDto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Edward P. Legaspi
 **/
@XmlRootElement(name = "CalculateTerminationPenaltyRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class CalculateTerminationPenaltyRequestDto extends BaseDto {

    private static final long serialVersionUID = 8352154466061113933L;

    @XmlElement(required = true)
    private String subscriptionCode;


    public String getSubscriptionCode() {
        return subscriptionCode;
    }


}