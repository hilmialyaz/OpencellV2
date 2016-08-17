package org.meveo.api.dto.usage;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.meveo.api.dto.BaseDto;

@XmlRootElement(name = "CatUsage")
@XmlAccessorType(XmlAccessType.FIELD)
public class CatUsageDto extends BaseDto {

	private static final long serialVersionUID = 1L;
	private String code;
	private String description;
	
	@XmlElementWrapper
    @XmlElement(name="subCatUsage")
	List<SubCatUsageDto> listSubCatUsage = new ArrayList<SubCatUsageDto>();

	public CatUsageDto() {

	}

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @param code
	 *            the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the listSubCatUsage
	 */
	public List<SubCatUsageDto> getListSubCatUsage() {
		return listSubCatUsage;
	}

	/**
	 * @param listSubCatUsage
	 *            the listSubCatUsage to set
	 */
	public void setListSubCatUsage(List<SubCatUsageDto> listSubCatUsage) {
		this.listSubCatUsage = listSubCatUsage;
	}

	/**
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}


	@Override
	public String toString() {
		return "CatUsageDto [code=" + code + ", description=" + description + ", listSubCatUsage=" + listSubCatUsage + "]";
	}
	
	

}
