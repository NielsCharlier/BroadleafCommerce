/*
 * #%L
 * BroadleafCommerce Framework
 * %%
 * Copyright (C) 2009 - 2013 Broadleaf Commerce
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.broadleafcommerce.core.catalog.domain;

import org.broadleafcommerce.common.copy.CreateResponse;
import org.broadleafcommerce.common.copy.MultiTenantCopyContext;
import org.broadleafcommerce.common.extensibility.jpa.copy.DirectCopyTransform;
import org.broadleafcommerce.common.extensibility.jpa.copy.DirectCopyTransformMember;
import org.broadleafcommerce.common.extensibility.jpa.copy.DirectCopyTransformTypes;
import org.broadleafcommerce.common.i18n.service.DynamicTranslationProvider;
import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.common.presentation.AdminPresentation;
import org.broadleafcommerce.common.presentation.AdminPresentationClass;
import org.broadleafcommerce.common.presentation.client.SupportedFieldType;
import org.broadleafcommerce.core.catalog.service.dynamic.DynamicSkuPrices;
import org.broadleafcommerce.core.catalog.service.dynamic.SkuPricingConsiderationContext;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "BLC_PRODUCT_OPTION_VALUE")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "blProducts")
@AdminPresentationClass(friendlyName = "Product Option Value")
@DirectCopyTransform({
        @DirectCopyTransformMember(templateTokens = DirectCopyTransformTypes.SANDBOX, skipOverlaps=true),
        @DirectCopyTransformMember(templateTokens = DirectCopyTransformTypes.MULTITENANT_CATALOG)
})
public class ProductOptionValueImpl implements ProductOptionValue {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "ProductOptionValueId")
    @GenericGenerator(
        name = "ProductOptionValueId",
        strategy = "org.broadleafcommerce.common.persistence.IdOverrideTableGenerator",
        parameters = {
                @Parameter(name = "segment_value", value = "ProductOptionValueImpl"),
                @Parameter(name = "entity_name", value = "org.broadleafcommerce.core.catalog.domain.ProductOptionValueImpl")
        })
    @Column(name = "PRODUCT_OPTION_VALUE_ID")
    protected Long id;

    @Column(name = "ATTRIBUTE_VALUE")
    @AdminPresentation(friendlyName = "productOptionValue_attributeValue", 
            prominent = true, order = Presentation.FieldOrder.ATTRIBUTE_VALUE,
            translatable = true, gridOrder = Presentation.FieldOrder.ATTRIBUTE_VALUE)
    protected String attributeValue;

    @Column(name = "DISPLAY_ORDER")
    @AdminPresentation(friendlyName = "productOptionValue_displayOrder", prominent = true,
            gridOrder = Presentation.FieldOrder.DISPLAY_ORDER, order = Presentation.FieldOrder.DISPLAY_ORDER)
    protected Long displayOrder;

    @Column(name = "PRICE_ADJUSTMENT", precision = 19, scale = 5)
    @AdminPresentation(friendlyName = "productOptionValue_adjustment", fieldType = SupportedFieldType.MONEY,
            prominent = true, gridOrder = Presentation.FieldOrder.PRICE_ADJUSTMENT, order = Presentation.FieldOrder.PRICE_ADJUSTMENT)
    protected BigDecimal priceAdjustment;

    @ManyToOne(targetEntity = ProductOptionImpl.class, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "PRODUCT_OPTION_ID")
    protected ProductOption productOption;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getAttributeValue() {
    	String attributeValue = this.attributeValue;
    	int i = attributeValue.indexOf('#');
    	if (i >= 0) {
    		attributeValue = attributeValue.substring(0, i);
    	}        
    	return DynamicTranslationProvider.getValue(this, "attributeValue", attributeValue);
    }
    
    @Override
    public String getRawAttributeValue() {
    	return attributeValue;
    }
    
    @Override
    public String getAttributeInfo() {
    	String attributeInfo = null;
    	int i = attributeValue.indexOf('#');
    	if (i >= 0) {
    		attributeInfo = attributeValue.substring(i);
    	}
        return attributeInfo;
    }

    @Override
    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }

    @Override
    public Long getDisplayOrder() {
        return displayOrder;
    }

    @Override
    public void setDisplayOrder(Long displayOrder) {
        this.displayOrder = displayOrder;
    }

    @Override
    public Money getPriceAdjustment() {

        Money returnPrice = null;

        if (SkuPricingConsiderationContext.hasDynamicPricing()) {

            DynamicSkuPrices dynamicPrices = SkuPricingConsiderationContext.getSkuPricingService().getPriceAdjustment(this, priceAdjustment == null ? null : new Money(priceAdjustment), SkuPricingConsiderationContext.getSkuPricingConsiderationContext());
            returnPrice = dynamicPrices.getPriceAdjustment();

        } else {
            if (priceAdjustment != null) {
                returnPrice = new Money(priceAdjustment, Money.defaultCurrency());
            }
        }

        return returnPrice;
    }

    @Override
    public void setPriceAdjustment(Money priceAdjustment) {
        this.priceAdjustment = Money.toAmount(priceAdjustment);
    }

    @Override
    public ProductOption getProductOption() {
        return productOption;
    }

    @Override
    public void setProductOption(ProductOption productOption) {
        this.productOption = productOption;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!getClass().isAssignableFrom(obj.getClass())) {
            return false;
        }
        ProductOptionValueImpl other = (ProductOptionValueImpl) obj;

        if (id != null && other.id != null) {
            return id.equals(other.id);
        }

        if (getAttributeValue() == null) {
            if (other.getAttributeValue() != null) {
                return false;
            }
        } else if (!getAttributeValue().equals(other.getAttributeValue())) {
            return false;
        }
        return true;
    }

    @Override
    public <G extends ProductOptionValue> CreateResponse<G> createOrRetrieveCopyInstance(MultiTenantCopyContext context) throws CloneNotSupportedException {
        CreateResponse<G> createResponse = context.createOrRetrieveCopyInstance(this);
        if (createResponse.isAlreadyPopulated()) {
            return createResponse;
        }
        ProductOptionValue cloned = createResponse.getClone();
        cloned.setAttributeValue(attributeValue);
        cloned.setDisplayOrder(displayOrder);
        cloned.setPriceAdjustment(getPriceAdjustment());
        if (productOption != null) {
            cloned.setProductOption(productOption.createOrRetrieveCopyInstance(context).getClone());
        }
        
        return  createResponse;
    }

    public static class Presentation {

        public static class FieldOrder {

            public static final int ATTRIBUTE_VALUE = 1000;
            public static final int DISPLAY_ORDER = 3000;
            public static final int PRICE_ADJUSTMENT = 2000;
        }
    }

}
