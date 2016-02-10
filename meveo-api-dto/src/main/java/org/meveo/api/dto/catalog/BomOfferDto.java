package org.meveo.api.dto.catalog;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.meveo.api.dto.BaseDto;
import org.meveo.api.dto.CustomFieldsDto;

@XmlRootElement(name = "BomOffer")
@XmlAccessorType(XmlAccessType.FIELD)
public class BomOfferDto extends BaseDto {

	private static final long serialVersionUID = 4557706201829891403L;

	@NotNull
	@XmlAttribute(required = true)
	private String bomCode;

	private CustomFieldsDto offerCustomFields;

	private String prefix;

	public String getBomCode() {
		return bomCode;
	}

	public void setBomCode(String bomCode) {
		this.bomCode = bomCode;
	}

	public CustomFieldsDto getOfferCustomFields() {
		return offerCustomFields;
	}

	public void setOfferCustomFields(CustomFieldsDto offerCustomFields) {
		this.offerCustomFields = offerCustomFields;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public String toString() {
		return "BomOfferDto [bomCode=" + bomCode + ", offerCustomFields=" + offerCustomFields + ", prefix=" + prefix
				+ "]";
	}

	// private String bomVersion;
	// private String serviceCodePrefix;
	// private List<String> servicesToActivate;
	// private Map<String, CustomFieldsDto> serviceCFVs;
}
