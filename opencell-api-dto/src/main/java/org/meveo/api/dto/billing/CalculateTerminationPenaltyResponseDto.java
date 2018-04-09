package org.meveo.api.dto.billing;

import org.meveo.api.dto.BaseDto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;

/**
 * @author Edward P. Legaspi
 **/
@XmlRootElement(name = "CalculateTerminationPenaltyResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class CalculateTerminationPenaltyResponseDto extends BaseDto {

    private static final long serialVersionUID = 8352154466061113933L;

    @XmlElement(required = true)
    private BigDecimal fee;


    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }
}