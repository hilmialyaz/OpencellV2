package org.meveo.model.billing;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.meveo.model.BaseEntity;
import org.meveo.model.ExportIdentifier;
import org.meveo.model.IEntity;
import org.meveo.model.crm.Provider;

/**
 * @author Edward P. Legaspi
 **/
@Entity
@ExportIdentifier({ "provider" })
@Table(name = "billing_invoice_configuration", uniqueConstraints = @UniqueConstraint(columnNames = { "provider_id" }))
@GenericGenerator(name = "ID_GENERATOR", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
        @Parameter(name = "sequence_name", value = "billing_invoice_configuration_seq"), })
public class InvoiceConfiguration extends BaseEntity implements Serializable, IEntity {

    private static final long serialVersionUID = -735961368678724497L;

    @Type(type = "numeric_boolean")
    @Column(name = "display_subscriptions")
    private Boolean displaySubscriptions = false;

    @Type(type = "numeric_boolean")
    @Column(name = "display_services")
    private Boolean displayServices = false;

    @Type(type = "numeric_boolean")
    @Column(name = "display_offers")
    private Boolean displayOffers = false;

    @Type(type = "numeric_boolean")
    @Column(name = "display_priceplans")
    private Boolean displayPricePlans = false;

    @Type(type = "numeric_boolean")
    @Column(name = "display_edrs")
    private Boolean displayEdrs = false;

    @Type(type = "numeric_boolean")
    @Column(name = "display_provider")
    private Boolean displayProvider = false;

    @Type(type = "numeric_boolean")
    @Column(name = "display_detail")
    private Boolean displayDetail = true;

    @Type(type = "numeric_boolean")
    @Column(name = "display_cf_as_xml")
    private Boolean displayCfAsXML = false;

    @Type(type = "numeric_boolean")
    @Column(name = "display_charges_periods")
    private Boolean displayChargesPeriods = false;

    @Type(type = "numeric_boolean")
    @Column(name = "display_billing_cycle")
    private Boolean displayBillingCycle = false;

    @Type(type = "numeric_boolean")
    @Column(name = "display_orders")
    private Boolean displayOrders = false;

    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    private Provider provider;

    public Boolean getDisplaySubscriptions() {
        return displaySubscriptions;
    }

    public void setDisplaySubscriptions(Boolean displaySubscriptions) {
        this.displaySubscriptions = displaySubscriptions;
    }

    public Boolean getDisplayServices() {
        return displayServices;
    }

    public void setDisplayServices(Boolean displayServices) {
        this.displayServices = displayServices;
    }

    public Boolean getDisplayOffers() {
        return displayOffers;
    }

    public void setDisplayOffers(Boolean displayOffers) {
        this.displayOffers = displayOffers;
    }

    public Boolean getDisplayPricePlans() {
        return displayPricePlans;
    }

    public void setDisplayPricePlans(Boolean displayPricePlans) {
        this.displayPricePlans = displayPricePlans;
    }

    public Boolean getDisplayEdrs() {
        return displayEdrs;
    }

    public void setDisplayEdrs(Boolean displayEdrs) {
        this.displayEdrs = displayEdrs;
    }

    public Boolean getDisplayProvider() {
        return displayProvider;
    }

    public void setDisplayProvider(Boolean displayProvider) {
        this.displayProvider = displayProvider;
    }

    public Boolean getDisplayDetail() {
        return displayDetail;
    }

    public void setDisplayDetail(Boolean displayDetail) {
        this.displayDetail = displayDetail;
    }

    public Boolean getDisplayCfAsXML() {
        return displayCfAsXML;
    }

    public void setDisplayCfAsXML(Boolean displayCfAsXML) {
        this.displayCfAsXML = displayCfAsXML;
    }

    public Boolean getDisplayChargesPeriods() {
        return displayChargesPeriods;
    }

    public void setDisplayChargesPeriods(Boolean displayChargesPeriods) {
        this.displayChargesPeriods = displayChargesPeriods;
    }

    public Boolean getDisplayBillingCycle() {
        return displayBillingCycle;
    }

    public void setDisplayBillingCycle(Boolean displayBillingCycle) {
        this.displayBillingCycle = displayBillingCycle;
    }

    @Override
    public String toString() {
        return "InvoiceConfiguration [displaySubscriptions=" + displaySubscriptions + ", displayServices=" + displayServices + ", displayOffers=" + displayOffers + ", "
                + "displayPricePlans=" + displayPricePlans + ", displayEdrs=" + displayEdrs + ", displayProvider=" + displayProvider + ", " + "displayDetail=" + displayDetail
                + ", displayCfAsXML=" + displayCfAsXML + ", displayChargesPeriods=" + displayChargesPeriods + ", displayBillingCycle=" + displayBillingCycle + ",displayOrders="
                + displayOrders + "]";
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    /**
     * @return the displayOrders
     */
    public Boolean getDisplayOrders() {
        return displayOrders;
    }

    /**
     * @param displayOrders the displayOrders to set
     */
    public void setDisplayOrders(Boolean displayOrders) {
        this.displayOrders = displayOrders;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof InvoiceConfiguration)) {
            return false;
        }

        InvoiceConfiguration other = (InvoiceConfiguration) obj;

        if (getId() != null && other.getId() != null && getId().equals(other.getId())) {
            return true;
        }

        // if (code == null) {
        // if (other.getCode() != null) {
        // return false;
        // }
        // } else if (!code.equals(other.getCode())) {
        // return false;
        // }
        // Always return true as there can be only one record of invoice configuration
        return true;
    }

}