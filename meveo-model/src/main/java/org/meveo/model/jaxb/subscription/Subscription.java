/*
* (C) Copyright 2009-2013 Manaty SARL (http://manaty.net/) and contributors.
*
* Licensed under the GNU Public Licence, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.gnu.org/licenses/gpl-2.0.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.2-147 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.02.03 at 11:45:33 PM WET 
//


package org.meveo.model.jaxb.subscription;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}subscriptionDate" minOccurs="0"/>
 *         &lt;element ref="{}endAgreementDate" minOccurs="0"/>
 *         &lt;element ref="{}status"/>
 *         &lt;element ref="{}description" minOccurs="0"/>
 *         &lt;element ref="{}services" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="code" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="userAccountId" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="offerCode" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "subscriptionDate",
    "endAgreementDate",
    "status",
    "description",
    "services",
    "accessPoints"
})
@XmlRootElement(name = "subscription")
public class Subscription {

    protected String subscriptionDate;
    protected String endAgreementDate;
    @XmlElement(required = true)
    protected Status status;
    protected String description;
    protected Services services;
    protected AccessPoints accessPoints;
    @XmlAttribute(name = "code")
    protected String code;
    @XmlAttribute(name = "userAccountId")
    protected String userAccountId;
    @XmlAttribute(name = "offerCode")
    protected String offerCode;

    /**
     * Gets the value of the subscriptionDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSubscriptionDate() {
        return subscriptionDate;
    }

    /**
     * Sets the value of the subscriptionDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSubscriptionDate(String value) {
        this.subscriptionDate = value;
    }

    /**
     * Gets the value of the endAgreementDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEndAgreementDate() {
        return endAgreementDate;
    }

    /**
     * Sets the value of the endAgreementDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEndAgreementDate(String value) {
        this.endAgreementDate = value;
    }

    /**
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link Status }
     *     
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link Status }
     *     
     */
    public void setStatus(Status value) {
        this.status = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the services property.
     * 
     * @return
     *     possible object is
     *     {@link Services }
     *     
     */
    public Services getServices() {
        return services;
    }

    /**
     * Sets the value of the services property.
     * 
     * @param value
     *     allowed object is
     *     {@link Services }
     *     
     */
    public void setServices(Services value) {
        this.services = value;
    }

    /**
     * Gets the value of the code property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCode() {
        return code;
    }

    /**
     * Sets the value of the code property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCode(String value) {
        this.code = value;
    }

    /**
     * Gets the value of the userAccountId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserAccountId() {
        return userAccountId;
    }

    /**
     * Sets the value of the userAccountId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserAccountId(String value) {
        this.userAccountId = value;
    }

    /**
     * Gets the value of the offerCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOfferCode() {
        return offerCode;
    }

    /**
     * Sets the value of the offerCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOfferCode(String value) {
        this.offerCode = value;
    }

	public AccessPoints getAccessPoints() {
		return accessPoints;
	}

	public void setAccessPoints(AccessPoints accessPoints) {
		this.accessPoints = accessPoints;
	}
    
    

}
