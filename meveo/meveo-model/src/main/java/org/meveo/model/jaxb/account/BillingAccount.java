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
// Generated on: 2011.02.01 at 06:47:58 PM WET 
//


package org.meveo.model.jaxb.account;

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
 *         &lt;element ref="{}subscriptionDate"/>
 *         &lt;element ref="{}description"/>
 *         &lt;element ref="{}externalRef1"/>
 *         &lt;element ref="{}externalRef2"/>
 *         &lt;element ref="{}company"/>
 *         &lt;element ref="{}name"/>
 *         &lt;element ref="{}address"/>
 *         &lt;element ref="{}electronicBilling"/>
 *         &lt;element ref="{}email"/>
 *         &lt;element ref="{}bankCoordinates"/>
 *         &lt;element ref="{}tradingCountryCode"/>
 *         &lt;element ref="{}tradingLanguageCode"/>
 *         &lt;element ref="{}userAccounts"/>
 *       &lt;/sequence>
 *       &lt;attribute name="code" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="customerAccountId" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="paymentMethod" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="billingCycle" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
	"customerAccountId",
    "subscriptionDate",
    "description",
    "externalRef1",
    "externalRef2",
    "company",
    "name",
    "address",
    "electronicBilling",
    "email",
    "bankCoordinates",
    "tradingCountryCode",
    "tradingLanguageCode",
    "userAccounts"
})
@XmlRootElement(name = "billingAccount")
public class BillingAccount {

    @XmlElement(required = true)
    protected String subscriptionDate;
    @XmlElement(required = true)
    protected String description;
    @XmlElement(required = true)
    protected String externalRef1;
    @XmlElement(required = true)
    protected String externalRef2;
    @XmlElement(required = true)
    protected String company;
    @XmlElement(required = true)
    protected Name name;
    @XmlElement(required = true)
    protected Address address;
    @XmlElement(required = true)
    protected String electronicBilling;
    @XmlElement(required = true)
    protected String email;
    @XmlElement(required = true)
    protected BankCoordinates bankCoordinates;
    @XmlElement(required = true)
    protected BankCoordinates tradingCountryCode;
    @XmlElement(required = true)
    protected BankCoordinates tradingLanguageCode;
    @XmlElement(required = true)
    protected UserAccounts userAccounts;
    @XmlAttribute(name = "code")
    protected String code;
    @XmlAttribute(name = "customerAccountId")
    protected String customerAccountId;
    @XmlAttribute(name = "paymentMethod")
    protected String paymentMethod;
    @XmlAttribute(name = "billingCycle")
    protected String billingCycle;

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
     * Gets the value of the externalRef1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExternalRef1() {
        return externalRef1;
    }

    /**
     * Sets the value of the externalRef1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExternalRef1(String value) {
        this.externalRef1 = value;
    }

    /**
     * Gets the value of the externalRef2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExternalRef2() {
        return externalRef2;
    }

    /**
     * Sets the value of the externalRef2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExternalRef2(String value) {
        this.externalRef2 = value;
    }

    /**
     * Gets the value of the company property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCompany() {
        return company;
    }

    /**
     * Sets the value of the company property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCompany(String value) {
        this.company = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link Name }
     *     
     */
    public Name getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link Name }
     *     
     */
    public void setName(Name value) {
        this.name = value;
    }

    /**
     * Gets the value of the address property.
     * 
     * @return
     *     possible object is
     *     {@link Address }
     *     
     */
    public Address getAddress() {
        return address;
    }

    /**
     * Sets the value of the address property.
     * 
     * @param value
     *     allowed object is
     *     {@link Address }
     *     
     */
    public void setAddress(Address value) {
        this.address = value;
    }

    /**
     * Gets the value of the electronicBilling property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getElectronicBilling() {
        return electronicBilling;
    }

    /**
     * Sets the value of the electronicBilling property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setElectronicBilling(String value) {
        this.electronicBilling = value;
    }

    /**
     * Gets the value of the email property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the value of the email property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEmail(String value) {
        this.email = value;
    }

    /**
     * Gets the value of the bankCoordinates property.
     * 
     * @return
     *     possible object is
     *     {@link BankCoordinates }
     *     
     */
    public BankCoordinates getBankCoordinates() {
        return bankCoordinates;
    }

    /**
     * Sets the value of the bankCoordinates property.
     * 
     * @param value
     *     allowed object is
     *     {@link BankCoordinates }
     *     
     */
    public void setBankCoordinates(BankCoordinates value) {
        this.bankCoordinates = value;
    }

    public BankCoordinates getTradingCountryCode() {
		return tradingCountryCode;
	}

	public void setTradingCountryCode(BankCoordinates tradingCountryCode) {
		this.tradingCountryCode = tradingCountryCode;
	}

	public BankCoordinates getTradingLanguageCode() {
		return tradingLanguageCode;
	}

	public void setTradingLanguageCode(BankCoordinates tradingLanguageCode) {
		this.tradingLanguageCode = tradingLanguageCode;
	}

	/**
     * Gets the value of the userAccounts property.
     * 
     * @return
     *     possible object is
     *     {@link UserAccounts }
     *     
     */
    public UserAccounts getUserAccounts() {
        return userAccounts;
    }

    /**
     * Sets the value of the userAccounts property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserAccounts }
     *     
     */
    public void setUserAccounts(UserAccounts value) {
        this.userAccounts = value;
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
     * Gets the value of the customerAccountId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCustomerAccountId() {
        return customerAccountId;
    }

    /**
     * Sets the value of the customerAccountId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCustomerAccountId(String value) {
        this.customerAccountId = value;
    }

    /**
     * Gets the value of the paymentMethod property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPaymentMethod() {
        return paymentMethod;
    }

    /**
     * Sets the value of the paymentMethod property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPaymentMethod(String value) {
        this.paymentMethod = value;
    }

    /**
     * Gets the value of the billingCycle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBillingCycle() {
        return billingCycle;
    }

    /**
     * Sets the value of the billingCycle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBillingCycle(String value) {
        this.billingCycle = value;
    }

}
