package org.meveo.api.dto.catalog;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Edward P. Legaspi
 * @since 2 Oct 2017
 */
@XmlRootElement(name = "BSMConfiguration")
@XmlAccessorType(XmlAccessType.FIELD)
public class BSMConfigurationDto implements Serializable {

    private static final long serialVersionUID = 1140305701541170851L;

    @XmlAttribute
	private String code;

	/**
	 * We used this to configure the custom fields for BSM services.
	 */
	@XmlElement(name = "service")
	private ServiceConfigurationDto serviceConfiguration;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public ServiceConfigurationDto getServiceConfiguration() {
		return serviceConfiguration;
	}

	public void setServiceConfiguration(ServiceConfigurationDto serviceConfiguration) {
		this.serviceConfiguration = serviceConfiguration;
	}

}
