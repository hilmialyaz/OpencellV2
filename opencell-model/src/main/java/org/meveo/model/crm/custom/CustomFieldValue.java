package org.meveo.model.crm.custom;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.meveo.commons.utils.CustomDateSerializer;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.BusinessEntity;
import org.meveo.model.DatePeriod;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.EntityReferenceWrapper;
import org.meveo.model.shared.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * Encapsulates a custom field value. Supports the following data types:
 * <ul>
 * <li>string, date, double and long that are stored as separate fields</li>
 * <li>reference to an entity, that is serialized as Json to serializedValue field</li>
 * <li>a child entity containing the actual data, that is serialized as Json to serializedValue field</li>
 * <li>a list or a map of above mentioned data types, serialized as Json to to serializedValue field</li>
 * </ul>
 * 
 * A reference to an entity, child entity, list and map values should not be modified behind the scenes - an appropriate SET method has to be called to serialize the value. - This
 * limitations comes from MERGE loosing transient values and thus JPA callback @postUpdate can not be used (see CustomFieldInstance class).
 * 
 * Serialized value format is described in serializeValue() method for each data type.
 * 
 * entityReferenceValueForGUI, mapValuesForGUI, matrixValuesForGUI fields are used in data entry from GUI ONLY.
 * 
 * @author Andrius Karpavicius
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomFieldValue implements Serializable {

    private static final long serialVersionUID = -9038541899269528670L;

    public static String MAP_KEY = "key";
    public static String MAP_VALUE = "value";

    public static String MATRIX_COLUMN_NAME_SEPARATOR = "/";
    public static String MATRIX_KEY_SEPARATOR = "|";
    public static String RON_VALUE_SEPARATOR = "<";

    private static String SERIALIZATION_SEPARATOR = "|";

    /**
     * Period to which value applies to
     */
    @JsonUnwrapped()
    private DatePeriod period;

    /**
     * Value priority if periods overlapp
     */
    private Integer priority;

    /**
     * String type value
     */
    @JsonProperty("string")
    private String stringValue;

    /**
     * Date type value
     */
    @JsonProperty("date")
    @JsonSerialize(using = CustomDateSerializer.class)
    private Date dateValue;

    /**
     * Long type value
     */
    @JsonProperty("long")
    private Long longValue;

    /**
     * Double type value
     */
    @JsonProperty("double")
    private Double doubleValue;

    /**
     * Entity reference type value
     */
    @JsonProperty("entity")
    private EntityReferenceWrapper entityReferenceValue;

    /**
     * List of Strings type value
     */
    @JsonProperty("listString")
    private List<String> listStringValue = null;

    /**
     * List of Date type value
     */
    @JsonProperty("listDate")
    @JsonSerialize(contentUsing = CustomDateSerializer.class)
    private List<Date> listDateValue = null;

    /**
     * List of Long type value
     */
    @JsonProperty("listLong")
    private List<Long> listLongValue = null;

    /**
     * List of Double type value
     */
    @JsonProperty("listDouble")
    private List<Double> listDoubleValue = null;

    /**
     * List of Entity references type value
     */
    @JsonProperty("listEntity")
    private List<EntityReferenceWrapper> listEntityValue = null;

    /**
     * Map of String type value
     */
    @JsonProperty("mapString")
    private Map<String, String> mapStringValue = null;

    /**
     * Map of Date type value
     */
    @JsonProperty("mapDate")
    @JsonSerialize(contentUsing = CustomDateSerializer.class)
    private Map<String, Date> mapDateValue = null;

    /**
     * Map of Long type value
     */
    @JsonProperty("mapLong")
    private Map<String, Long> mapLongValue = null;

    /**
     * Map of Double type value
     */
    @JsonProperty("mapDouble")
    private Map<String, Double> mapDoubleValue = null;

    /**
     * Map of Entity reference type value
     */
    @JsonProperty("mapEntity")
    private Map<String, EntityReferenceWrapper> mapEntityValue = null;

    /**
     * Contains mapValue adapted for GUI data entry in the following way:
     * 
     * List item corresponds to an entry in a mapValue with the following list's map values: MAP_KEY=mapValue.entry.key and MAP_VALUE=mapValue.entry.value
     */
    @JsonIgnore
    private List<Map<String, Object>> mapValuesForGUI = new ArrayList<Map<String, Object>>();

    /**
     * Contains mapValue adapted for GUI data entry in the following way:
     * 
     * List item corresponds to an entry in a mapValue with the following list's map values: MAP_VALUE=mapValue.entry.value, mapValue.entry.key is parsed into separate key/value
     * pairs and inserted into map
     */
    @JsonIgnore
    private List<Map<String, Object>> matrixValuesForGUI = new ArrayList<Map<String, Object>>();

    /**
     * Contains entity reference converted into a CustomFieldValueHolder object in the following way:
     * 
     * CustomFieldValueHolder.entity = entity reference CEI object, CustomFieldValueHolder.values = childEntityValue.fieldValues
     */
    @JsonIgnore
    private List<CustomFieldValueHolder> childEntityValuesForGUI = new ArrayList<>();

    /**
     * Contains entityReferenceValue converted into a BusinessEntity object in the following way:
     * 
     * A class of entityReferenceValue.className type is instantiated with code field set to entityReferenceValue.code value and in case it is Custom Entity Instance class, a
     * cetCode field is set to entityReferenceValue.classnameCode value
     */
    @JsonIgnore
    private BusinessEntity entityReferenceValueForGUI;

    @JsonIgnore
    protected boolean isNewPeriod = false;

    public CustomFieldValue() {
    }

    public CustomFieldValue(Object value) {
        setValue(value);
    }

    public CustomFieldValue(DatePeriod period, Integer priority, Object value) {
        this.period = period;
        this.priority = priority;
        setValue(value);
        this.isNewPeriod = true;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public Date getDateValue() {
        return dateValue;
    }

    public void setDateValue(Date dateValue) {
        this.dateValue = dateValue;
    }

    public Long getLongValue() {
        return longValue;
    }

    public void setLongValue(Long longValue) {
        this.longValue = longValue;
    }

    public Double getDoubleValue() {
        return doubleValue;
    }

    public void setDoubleValue(Double doubleValue) {
        this.doubleValue = doubleValue;
    }

    public void setMapValuesForGUI(List<Map<String, Object>> mapValuesForGUI) {
        this.mapValuesForGUI = mapValuesForGUI;
    }

    public List<Map<String, Object>> getMapValuesForGUI() {
        return mapValuesForGUI;
    }

    public List<Map<String, Object>> getMatrixValuesForGUI() {
        return matrixValuesForGUI;
    }

    public void setMatrixValuesForGUI(List<Map<String, Object>> matrixValuesForGUI) {
        this.matrixValuesForGUI = matrixValuesForGUI;
    }

    public EntityReferenceWrapper getEntityReferenceValue() {
        return entityReferenceValue;
    }

    /**
     * Set a reference to an entity value. Value is serialized immediately.
     * 
     * NOTE: Always set a new value. DO NOT edit the value, as it will not be persisted, or explicitly call serializeValue() afterwards.
     * 
     * @param entityReferenceValue Reference to an entity value
     */
    public void setEntityReferenceValue(EntityReferenceWrapper entityReferenceValue) {
        this.entityReferenceValue = entityReferenceValue;
    }

    @SuppressWarnings("rawtypes")
    public List getListValue() {
        if (listStringValue != null) {
            return listStringValue;
        } else if (listDateValue != null) {
            return listDateValue;
        } else if (listLongValue != null) {
            return listLongValue;
        } else if (listDoubleValue != null) {
            return listDoubleValue;
        } else if (listEntityValue != null) {
            return listEntityValue;
        }

        return null;
    }

    /**
     * Set a list of values.
     * 
     * @param listValue list of values to set.
     */
    @SuppressWarnings({ "rawtypes" })
    public void setListValue(List listValue) {

        Iterator iterator = ((List) listValue).iterator();
        Class itemClass = findItemClass(iterator);

        if (itemClass == String.class) {
            listStringValue = new ArrayList<>();
            for (Object listItem : listValue) {
                listStringValue.add(listItem.toString());
            }

        } else if (itemClass == Date.class) {
            listDateValue = new ArrayList<>();
            for (Object listItem : listValue) {
                listDateValue.add((Date) listItem);
            }

        } else if (itemClass == Integer.class || itemClass == Long.class) {
            listLongValue = new ArrayList<>();
            for (Object listItem : listValue) {
                if (listItem instanceof Integer) {
                    listLongValue.add(((Integer) listItem).longValue());
                } else if (listItem instanceof Long) {
                    listLongValue.add(((Long) listItem));
                }
            }

        } else if (itemClass == Double.class || itemClass == BigDecimal.class) {
            listDoubleValue = new ArrayList<>();
            for (Object listItem : listValue) {
                if (listItem instanceof BigDecimal) {
                    listDoubleValue.add(((BigDecimal) listItem).doubleValue());
                } else if (listItem instanceof Double) {
                    listDoubleValue.add((Double) listItem);
                }
            }

        } else if (itemClass == EntityReferenceWrapper.class) {
            listEntityValue = new ArrayList<>();
            for (Object listItem : listValue) {
                listEntityValue.add((EntityReferenceWrapper) listItem);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    public Map getMapValue() {
        if (mapStringValue != null && !mapStringValue.isEmpty()) {
            return mapStringValue;
        } else if (mapDateValue != null && !mapDateValue.isEmpty()) {
            return mapDateValue;
        } else if (mapLongValue != null && !mapLongValue.isEmpty()) {
            return mapLongValue;
        } else if (mapDoubleValue != null && !mapDoubleValue.isEmpty()) {
            return mapDoubleValue;
        } else if (mapEntityValue != null && !mapEntityValue.isEmpty()) {
            return mapEntityValue;
        }
        return null;
    }

    /**
     * Set a map of values. Value is serialized immediately.
     * 
     * NOTE: Always set a new value. DO NOT edit the value, as it will not be persisted, or explicitly call serializeValue() afterwards.
     * 
     * @param mapValue A map of values
     */
    @SuppressWarnings({ "rawtypes" })
    public void setMapValue(Map<String, Object> mapValue) {

        Map<String, Object> mapCopy = null;
        // Handle map that stores matrix type values - not interested in storing matrix column names
        if (mapValue.containsKey(MAP_KEY)) {

            mapCopy = new LinkedHashMap<String, Object>();
            mapCopy.putAll(mapValue);
            mapCopy.remove(MAP_KEY);

            // Object columnNames = mapValue.get(MAP_KEY);
            // String columnNamesString = null;
            // if (columnNames instanceof String) {
            // columnNamesString = (String) columnNames;
            //
            // } else if (columnNames instanceof Collection) {
            // columnNamesString = StringUtils.concatenate(MATRIX_COLUMN_NAME_SEPARATOR, (Collection) columnNames);
            // }

            // A regular map
        } else {
            mapCopy = mapValue;
        }

        // Find the first not null value to determine item class.
        Iterator iterator = mapCopy.values().iterator();
        Class itemClass = findItemClass(iterator);

        if (itemClass == String.class) {
            mapStringValue = new LinkedHashMap<>();
            for (Entry<String, Object> mapItem : mapCopy.entrySet()) {
                mapStringValue.put(mapItem.getKey(), mapItem.getValue().toString());
            }

        } else if (itemClass == Date.class) {
            mapDateValue = new LinkedHashMap<>();
            for (Entry<String, Object> mapItem : mapCopy.entrySet()) {
                mapDateValue.put(mapItem.getKey(), (Date) mapItem.getValue());
            }

        } else if (itemClass == Integer.class || itemClass == Long.class) {
            mapLongValue = new LinkedHashMap<>();
            for (Entry<String, Object> mapItem : mapCopy.entrySet()) {
                if (mapItem.getValue() instanceof Integer) {
                    mapLongValue.put(mapItem.getKey(), ((Integer) mapItem.getValue()).longValue());
                } else if (mapItem.getValue() instanceof Long) {
                    mapLongValue.put(mapItem.getKey(), (Long) mapItem.getValue());
                }
            }

        } else if (itemClass == Double.class || itemClass == BigDecimal.class) {
            mapDoubleValue = new LinkedHashMap<>();
            for (Entry<String, Object> mapItem : mapCopy.entrySet()) {
                if (mapItem.getValue() instanceof BigDecimal) {
                    mapDoubleValue.put(mapItem.getKey(), ((BigDecimal) mapItem.getValue()).doubleValue());
                } else if (mapItem.getValue() instanceof Double) {
                    mapDoubleValue.put(mapItem.getKey(), (Double) mapItem.getValue());
                }
            }

        } else if (itemClass == EntityReferenceWrapper.class) {
            mapEntityValue = new LinkedHashMap<>();
            for (Entry<String, Object> mapItem : mapCopy.entrySet()) {
                mapEntityValue.put(mapItem.getKey(), (EntityReferenceWrapper) mapItem.getValue());
            }
        }
    }

    public void setEntityReferenceValueForGUI(BusinessEntity businessEntity) {
        this.entityReferenceValueForGUI = businessEntity;
    }

    public BusinessEntity getEntityReferenceValueForGUI() {
        return entityReferenceValueForGUI;
    }

    public List<CustomFieldValueHolder> getChildEntityValuesForGUI() {
        return childEntityValuesForGUI;
    }

    public void setChildEntityValuesForGUI(List<CustomFieldValueHolder> childEntityValuesForGUI) {
        this.childEntityValuesForGUI = childEntityValuesForGUI;
    }

    public DatePeriod getPeriod() {
        return period;
    }

    public void setPeriod(DatePeriod period) {
        this.period = period;
    }

    public int getPriority() {
        if (priority == null) {
            return 0;
        } else {
            return priority;
        }
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * Set value of a given type
     * 
     * @param value value object
     * @param fieldType field type.
     */
    public void setSingleValue(Object value, CustomFieldTypeEnum fieldType) {

        switch (fieldType) {
        case DATE:
            dateValue = (Date) value;
            break;

        case DOUBLE:

            if (value instanceof BigDecimal) {
                doubleValue = ((BigDecimal) value).setScale(2, RoundingMode.HALF_UP).doubleValue();
            } else if (value instanceof Long) {
                doubleValue = ((Long) value).doubleValue();
            } else if (value instanceof Integer) {
                doubleValue = ((Integer) value).doubleValue();
            } else {
                doubleValue = (Double) value;
            }
            break;

        case LONG:
            if (value instanceof BigDecimal) {
                longValue = ((BigDecimal) value).longValue();
            } else if (value instanceof Double) {
                longValue = ((Double) value).longValue();
            } else if (value instanceof Integer) {
                longValue = ((Integer) value).longValue();
            } else {
                longValue = (Long) value;
            }
            break;

        case STRING:
        case LIST:
        case TEXT_AREA:
            stringValue = (String) value;

            break;

        case ENTITY:
            setEntityReferenceValue(new EntityReferenceWrapper((BusinessEntity) value));
            break;

        case CHILD_ENTITY:
            throw new RuntimeException("Child entity type of field supports only list of entities");

        case MULTI_VALUE:
            throw new RuntimeException("Multi-value type of field supports only matrix");
        }
    }

    @SuppressWarnings("rawtypes")
    public String toXmlText(SimpleDateFormat sdf) {

        if (stringValue != null) {
            return stringValue;
        } else if (dateValue != null) {
            return sdf.format(dateValue);
        } else if (longValue != null) {
            return longValue.toString();
        } else if (doubleValue != null) {
            return doubleValue.toString();
        } else {
            List listValue = getListValue();
            if (listValue != null) {
                return listValue.toString();
            }
            Map mapValue = getMapValue();
            if (mapValue != null) {
                return mapValue.toString();
            }
        }
        return "";
    }

    public String getValueAsString(SimpleDateFormat sdf) {

        if (stringValue != null) {
            return stringValue;
        } else if (dateValue != null) {
            return sdf.format(dateValue);
        } else if (longValue != null) {
            return longValue.toString();
        } else if (doubleValue != null) {
            return doubleValue.toString();
        }

        return "";
    }

    /**
     * Get a short representation of a value to be used as display in GUI in inherited fields.
     * 
     * @param value object value.
     * @param dateFormat Date format
     * @return Return formated value when storage type is Single and concatenated values when storage type is multiple
     */
    @SuppressWarnings("unchecked")
    public static String getShortRepresentationOfValueObj(Object value, String dateFormat) {
        if (value == null) {
            return null;

        } else if (value instanceof Map) {
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

            StringBuilder builder = new StringBuilder();
            int i = 0;
            for (Map.Entry<String, Object> valueInfo : ((Map<String, Object>) value).entrySet()) {
                builder.append(builder.length() == 0 ? "" : ", ");
                Object val = valueInfo.getValue();
                if (val instanceof Date) {
                    val = sdf.format(val);
                } else if (val instanceof EntityReferenceWrapper) {
                    val = ((EntityReferenceWrapper) val).getCode();
                }

                builder.append(String.format("%s: [%s]", valueInfo.getKey(), val));
                i++;
                if (i >= 10) {
                    break;
                }
            }

            return builder.toString();

        } else if (value instanceof List) {
            StringBuilder builder = new StringBuilder();
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            int i = 0;
            for (Object val : ((List<Object>) value)) {
                builder.append(builder.length() == 0 ? "" : ", ");
                if (val instanceof Date) {
                    val = sdf.format(val);
                } else if (val instanceof EntityReferenceWrapper) {
                    val = ((EntityReferenceWrapper) val).getCode();
                }

                builder.append(val);
                i++;
                if (i >= 10) {
                    break;
                }
            }

            return builder.toString();

        } else if (value instanceof EntityReferenceWrapper) {
            return ((EntityReferenceWrapper) value).getCode();

        } else if (value instanceof Date) {
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            return sdf.format((Date) value);

        } else {
            return value.toString();
        }

    }

    /**
     * Get a short representation of a value to be used as display in GUI in versioned field summary (periods table)
     * 
     * @param cft Custom field template
     * @param dateFormat Date format
     * @return Return formated value when storage type is Single and concatenated values when storage type is multiple
     */
    public String getShortRepresentationOfValue(CustomFieldTemplate cft, String dateFormat) {

        if (cft.getStorageType() == CustomFieldStorageTypeEnum.LIST || cft.getStorageType() == CustomFieldStorageTypeEnum.MAP) {
            StringBuilder builder = new StringBuilder();

            Logger log = LoggerFactory.getLogger(getClass());
            log.error("AKK in short rep : map is {}, {}", mapDateValue!=null , mapValuesForGUI!=null);
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            int i = 0;
            for (Map<String, Object> valueInfo : mapValuesForGUI) {
                builder.append(builder.length() == 0 ? "" : ", ");

                Object value = valueInfo.get(MAP_VALUE);
                if (cft.getFieldType() == CustomFieldTypeEnum.DATE) {

                    value = sdf.format(value);
                } else if (cft.getFieldType() == CustomFieldTypeEnum.ENTITY && value != null) {
                    value = ((BusinessEntity) value).getCode();

                } else if (cft.getFieldType() == CustomFieldTypeEnum.CHILD_ENTITY && value != null) {
                    value = ((CustomFieldValueHolder) value).getShortRepresentationOfValues();
                }

                if (cft.getStorageType() == CustomFieldStorageTypeEnum.LIST) {
                    builder.append(value);
                } else {
                    builder.append(String.format("%s: [%s]", valueInfo.get(MAP_KEY), value));
                }
                i++;
                if (i >= 10) {
                    break;
                }
            }

            if (mapValuesForGUI.size() > 10) {
                builder.append(", ...");
            }

            return builder.toString();

        } else if (cft.getStorageType() == CustomFieldStorageTypeEnum.MATRIX) {
            StringBuilder builder = new StringBuilder();

            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            int i = 0;
            for (Map<String, Object> mapInfo : matrixValuesForGUI) {

                StringBuilder keyBuilder = new StringBuilder();

                for (CustomFieldMatrixColumn column : cft.getMatrixKeyColumns()) {
                    String columnValue = (String) mapInfo.get(column.getCode());
                    if (columnValue != null) {
                        keyBuilder.append(keyBuilder.length() == 0 ? "" : "|");
                        keyBuilder.append(column.getCode()).append("/").append(columnValue);
                    }
                }

                Object value = null;

                if (cft.getFieldType() == CustomFieldTypeEnum.MULTI_VALUE) {

                    StringBuilder valueBuilder = new StringBuilder();

                    for (CustomFieldMatrixColumn column : cft.getMatrixValueColumns()) {
                        Object columnValue = mapInfo.get(column.getCode());
                        if (columnValue != null) {
                            valueBuilder.append(valueBuilder.length() == 0 ? "" : "|");
                            // No need to check for column data type as they are all work fine with toString(). If in future date is added, would need to format it
                            valueBuilder.append(column.getCode()).append("/").append(columnValue);
                        }
                    }

                    value = valueBuilder.toString();

                } else {

                    value = mapInfo.get(MAP_VALUE);
                    if (value == null) {
                        continue;
                    }

                    if (cft.getFieldType() == CustomFieldTypeEnum.DATE) {
                        value = sdf.format(value);

                    } else if (cft.getFieldType() == CustomFieldTypeEnum.ENTITY && value != null) {
                        value = ((BusinessEntity) value).getCode();

                    } else if (cft.getFieldType() == CustomFieldTypeEnum.CHILD_ENTITY && value != null) {
                        value = ((CustomFieldValueHolder) value).getShortRepresentationOfValues();
                    }
                }

                builder.append(builder.length() == 0 ? "" : ", ");
                builder.append(String.format("%s: [%s]", keyBuilder.toString(), value));
                i++;
                if (i >= 10) {
                    break;
                }
            }

            if (matrixValuesForGUI.size() > 10) {
                builder.append(", ...");
            }

            return builder.toString();

        } else if (cft.getStorageType() == CustomFieldStorageTypeEnum.SINGLE) {
            switch (cft.getFieldType()) {
            case DATE:
                if (dateValue != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
                    return sdf.format(dateValue);
                }
                break;
            case DOUBLE:
                if (doubleValue != null) {
                    return doubleValue.toString();
                }
                break;
            case ENTITY:
                if (entityReferenceValue != null) {
                    return entityReferenceValue.getCode();
                }
                break;
            case LONG:
                if (longValue != null) {
                    return longValue.toString();
                }
                break;
            case STRING:
            case LIST:
            case TEXT_AREA:
                return stringValue;
            case CHILD_ENTITY:
                throw new RuntimeException("Child entity type of field supports only list of entities");
            case MULTI_VALUE:
                throw new RuntimeException("Multi-value type of field supports only matrix");
            }
        }
        return null;
    }

    /**
     * Check if values is empty when used in data entry/display for GUI (use XXXForGUI fields instead of serializedValue field )
     * 
     * @return True is value is empty
     */
    public boolean isValueEmptyForGui() {
        boolean isEmpty = ((stringValue == null || stringValue.isEmpty()) && dateValue == null && longValue == null && doubleValue == null && entityReferenceValueForGUI == null
                && (mapValuesForGUI == null || mapValuesForGUI.isEmpty()) && (matrixValuesForGUI == null || matrixValuesForGUI.isEmpty())
                && (childEntityValuesForGUI == null || childEntityValuesForGUI.isEmpty()));

        if (isEmpty) {
            return true;

        } else if (matrixValuesForGUI != null && !matrixValuesForGUI.isEmpty()) {
            for (Map<String, Object> mapValue : matrixValuesForGUI) {
                for (Object value : mapValue.values()) {
                    boolean empty = StringUtils.isBlank(value);
                    if (!empty) {
                        // Logger log = LoggerFactory.getLogger(getClass());
                        // log.error("AKK cfv matrix is NOT empty {}", matrixValuesForGUI);
                        return false;
                    }
                }
            }
            return true;

        } else if (childEntityValuesForGUI != null && !childEntityValuesForGUI.isEmpty()) {
            for (CustomFieldValueHolder childEntity : childEntityValuesForGUI) {
                for (List<CustomFieldValue> cfValueList : childEntity.getValuesByCode().values()) {
                    for (CustomFieldValue cfValue : cfValueList) {
                        if (!cfValue.isValueEmptyForGui()) {

                            // Logger log = LoggerFactory.getLogger(getClass());
                            // log.error("AKK cfv che is NOT empty {}", cfi);
                            return false;
                        }
                    }

                }
            }
            return true;
        }

        // Logger log = LoggerFactory.getLogger(getClass());
        // log.error("AKK cfv is NOT empty {}", toString());
        return false;
    }

    /**
     * Check if values is empty when used in non-GUI data manipulation (do not use XXXForGUI fields)
     * 
     * @return True is value is empty
     */
    public boolean isValueEmpty() {
        return ((stringValue == null || stringValue.isEmpty()) && dateValue == null && longValue == null && doubleValue == null
                && (listStringValue == null || listStringValue.isEmpty()) && (listDateValue == null || listDateValue.isEmpty())
                && (listLongValue == null || listLongValue.isEmpty()) && (listDoubleValue == null || listDoubleValue.isEmpty())
                && (listEntityValue == null || listEntityValue.isEmpty()) && (mapStringValue == null || mapStringValue.isEmpty())
                && (mapDateValue == null || mapDateValue.isEmpty()) && (mapLongValue == null || mapLongValue.isEmpty()) && (mapDoubleValue == null || mapDoubleValue.isEmpty())
                && (mapEntityValue == null || mapEntityValue.isEmpty()) && (entityReferenceValue == null || entityReferenceValue.isEmpty()));
    }

    /**
     * Serialize a reference to an entity, list or map of values to a Json string in the following format:
     * <ul>
     * <li>"entity"|<json representation of EntityReferenceWrapper></li>
     * <li>"list_"<value classname eg. String>|<json representation of List></li>
     * <li>"map_"<value classname eg. String>|<json representation of Map></li>
     * <li>"matrix_"<value classname eg. String>|<key names>|<json representation of Map></li>
     * </ul>
     * 
     * @param cft Custom field template
     * @param valueToSerialize Value to serialize
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static String serializeValueToString(CustomFieldTemplate cft, Object valueToSerialize) {

        if (valueToSerialize == null) {
            return null;
        }

        GsonBuilder builder = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        Gson gson = builder.create();

        String sValue = null;
        if (valueToSerialize instanceof EntityReferenceWrapper && !((EntityReferenceWrapper) valueToSerialize).isEmpty()) {
            sValue = "entity" + SERIALIZATION_SEPARATOR + gson.toJson(valueToSerialize);

        } else if (valueToSerialize instanceof List && !((List) valueToSerialize).isEmpty()) {

            Class itemClass = null;
            if (cft.getFieldType() == CustomFieldTypeEnum.DATE) {
                itemClass = Date.class;
            } else if (cft.getFieldType() == CustomFieldTypeEnum.DOUBLE) {
                itemClass = Double.class;
            } else if (cft.getFieldType() == CustomFieldTypeEnum.ENTITY || cft.getFieldType() == CustomFieldTypeEnum.CHILD_ENTITY) {
                itemClass = EntityReferenceWrapper.class;
            } else if (cft.getFieldType() == CustomFieldTypeEnum.STRING || cft.getFieldType() == CustomFieldTypeEnum.LIST || cft.getFieldType() == CustomFieldTypeEnum.TEXT_AREA) {
                itemClass = String.class;
            } else if (cft.getFieldType() == CustomFieldTypeEnum.LONG) {
                itemClass = Long.class;
            }

            if (itemClass != null) {
                sValue = "list_" + itemClass.getSimpleName() + SERIALIZATION_SEPARATOR + gson.toJson(((List) valueToSerialize));
            } else {
                sValue = null;
            }

        } else if (valueToSerialize instanceof Map && !((Map) valueToSerialize).isEmpty()) {

            Class itemClass = null;
            if (cft.getFieldType() == CustomFieldTypeEnum.DATE) {
                itemClass = Date.class;
            } else if (cft.getFieldType() == CustomFieldTypeEnum.DOUBLE) {
                itemClass = Double.class;
            } else if (cft.getFieldType() == CustomFieldTypeEnum.ENTITY || cft.getFieldType() == CustomFieldTypeEnum.CHILD_ENTITY) {
                itemClass = EntityReferenceWrapper.class;
            } else if (cft.getFieldType() == CustomFieldTypeEnum.STRING || cft.getFieldType() == CustomFieldTypeEnum.LIST || cft.getFieldType() == CustomFieldTypeEnum.TEXT_AREA) {
                itemClass = String.class;
            } else if (cft.getFieldType() == CustomFieldTypeEnum.LONG) {
                itemClass = Long.class;
            }

            // Handle map that stores matrix type values
            if (cft.getStorageType() == CustomFieldStorageTypeEnum.MATRIX) {

                Map<String, Object> mapCopy = new LinkedHashMap<String, Object>();
                mapCopy.putAll(((Map) valueToSerialize));
                mapCopy.remove(MAP_KEY);

                String columnNamesString = StringUtils.concatenate(MATRIX_COLUMN_NAME_SEPARATOR, cft.getMatrixColumnCodes());

                if (itemClass != null) {
                    sValue = "matrix_" + itemClass.getSimpleName() + SERIALIZATION_SEPARATOR + columnNamesString + SERIALIZATION_SEPARATOR + gson.toJson(mapCopy);
                } else {
                    sValue = null;
                }

                // A regular map
            } else {

                if (itemClass != null) {
                    sValue = "map_" + itemClass.getSimpleName() + SERIALIZATION_SEPARATOR + gson.toJson(((Map) valueToSerialize));
                } else {
                    sValue = null;
                }
            }
        }
        return sValue;
    }

    /**
     * Get the data type of the first item. If the type is Integer check for further item in the list to see if a Double item exists.
     */
    @SuppressWarnings("rawtypes")
    private static Class findItemClass(Iterator iterator) {
        if (!iterator.hasNext()) {
            return null;
        }
        Object item = iterator.next();
        while (item == null && iterator.hasNext()) {
            item = iterator.next();
        }

        Class itemClass = null;
        if (item != null) {
            itemClass = item.getClass();
        } else {
            return null;
        }

        if (itemClass != null && (itemClass.equals(Long.class) || itemClass.equals(Integer.class))) {
            // check for further type
            while (iterator.hasNext()) {
                item = iterator.next();
                if (item != null) {
                    if (Double.class.equals(item.getClass())) {
                        itemClass = Double.class;
                        break;
                    }
                }
            }
        }

        return itemClass;
    }

    /**
     * Deserialize a serialized reference to an entity, list or map of values. See method serialize() for serialized value format
     * 
     * @param serializedValue Serialized value
     * @return EntityReferenceWrapper, ChildEntityValueWrapper, List or Map object
     */
    private static Object deserializeValueFromString(String serializedValue) {
        if (serializedValue == null) {
            return null;
        }

        GsonBuilder builder = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        Gson gson = builder.create();

        int firstSeparatorIndex = serializedValue.indexOf(SERIALIZATION_SEPARATOR);

        String type = serializedValue.substring(0, firstSeparatorIndex);
        String subType = null;

        Object deserializedValue = null;
        if (type.indexOf('_') > 0) {
            subType = type.substring(serializedValue.indexOf("_") + 1);
            type = type.substring(0, serializedValue.indexOf("_"));
        }

        if ("entity".equals(type)) {
            String sValue = serializedValue.substring(firstSeparatorIndex + 1);
            EntityReferenceWrapper entityReferenceValue = gson.fromJson(sValue, EntityReferenceWrapper.class);
            deserializedValue = entityReferenceValue;

        } else if ("list".equals(type)) {

            // Type defaults to String
            Type itemType = new TypeToken<List<String>>() {
            }.getType();

            // Determine an appropriate type
            if (Date.class.getSimpleName().equals(subType)) {
                itemType = new TypeToken<List<Date>>() {
                }.getType();
            } else if (Double.class.getSimpleName().equals(subType) || BigDecimal.class.getSimpleName().equals(subType)) {
                itemType = new TypeToken<List<Double>>() {
                }.getType();
            } else if (Long.class.getSimpleName().equals(subType) || Integer.class.getSimpleName().equals(subType)) {
                itemType = new TypeToken<List<Long>>() {
                }.getType();
            } else if (EntityReferenceWrapper.class.getSimpleName().equals(subType)) {
                itemType = new TypeToken<List<EntityReferenceWrapper>>() {
                }.getType();
            }

            String sValue = serializedValue.substring(firstSeparatorIndex + 1);
            List<Object> listValue = gson.fromJson(sValue, itemType);
            deserializedValue = listValue;

        } else if ("map".equals(type)) {

            // Type defaults to String
            Type itemType = new TypeToken<LinkedHashMap<String, String>>() {
            }.getType();

            // Determine an appropriate type
            if (Date.class.getSimpleName().equals(subType)) {
                itemType = new TypeToken<LinkedHashMap<String, Date>>() {
                }.getType();
            } else if (Double.class.getSimpleName().equals(subType) || BigDecimal.class.getSimpleName().equals(subType))

            {
                itemType = new TypeToken<LinkedHashMap<String, Double>>() {
                }.getType();
            } else if (Long.class.getSimpleName().equals(subType) || Integer.class.getSimpleName().equals(subType)) {
                itemType = new TypeToken<LinkedHashMap<String, Long>>() {
                }.getType();
            } else if (EntityReferenceWrapper.class.getSimpleName().equals(subType)) {
                itemType = new TypeToken<LinkedHashMap<String, EntityReferenceWrapper>>() {
                }.getType();
            }

            String sValue = serializedValue.substring(firstSeparatorIndex + 1);
            Map<String, Object> mapValue = gson.fromJson(sValue, itemType);
            deserializedValue = mapValue;

        } else if ("matrix".equals(type)) {

            // Type defaults to String
            Type itemType = new TypeToken<LinkedHashMap<String, String>>() {
            }.getType();

            // Determine an appropriate type
            if (Date.class.getSimpleName().equals(subType)) {
                itemType = new TypeToken<LinkedHashMap<String, Date>>() {
                }.getType();
            } else if (Double.class.getSimpleName().equals(subType) || BigDecimal.class.getSimpleName().equals(subType)) {
                itemType = new TypeToken<LinkedHashMap<String, Double>>() {
                }.getType();
            } else if (Long.class.getSimpleName().equals(subType) || Integer.class.getSimpleName().equals(subType)) {
                itemType = new TypeToken<LinkedHashMap<String, Long>>() {
                }.getType();
            } else if (EntityReferenceWrapper.class.getSimpleName().equals(subType)) {
                itemType = new TypeToken<LinkedHashMap<String, EntityReferenceWrapper>>() {
                }.getType();
            }

            int secondSeparatorIndex = serializedValue.indexOf(SERIALIZATION_SEPARATOR, firstSeparatorIndex + 1);
            String keys = serializedValue.substring(firstSeparatorIndex + 1, secondSeparatorIndex);
            String sValue = serializedValue.substring(secondSeparatorIndex + 1);

            Map<String, Object> mapValue = gson.fromJson(sValue, itemType);
            mapValue.put(MAP_KEY, keys);
            deserializedValue = mapValue;
        }

        // Logger log = LoggerFactory.getLogger(CustomFieldValue.class);
        // log.trace("Value {} deserialized to {}", serializedValue, deserializedValue);
        return deserializedValue;
    }

    public Object getValue() {
        if (mapStringValue != null && !mapStringValue.isEmpty()) {
            return mapStringValue;
        } else if (mapDateValue != null && !mapDateValue.isEmpty()) {
            return mapDateValue;
        } else if (mapLongValue != null && !mapLongValue.isEmpty()) {
            return mapLongValue;
        } else if (mapDoubleValue != null && !mapDoubleValue.isEmpty()) {
            return mapDoubleValue;
        } else if (mapEntityValue != null && !mapEntityValue.isEmpty()) {
            return mapEntityValue;
        } else if (listStringValue != null && !listStringValue.isEmpty()) {
            return listStringValue;
        } else if (listDateValue != null && !listDateValue.isEmpty()) {
            return listDateValue;
        } else if (listLongValue != null && !listLongValue.isEmpty()) {
            return listLongValue;
        } else if (listDoubleValue != null && !listDoubleValue.isEmpty()) {
            return listDoubleValue;
        } else if (listEntityValue != null && !listEntityValue.isEmpty()) {
            return listEntityValue;
        } else if (stringValue != null) {
            return stringValue;
        } else if (dateValue != null) {
            return dateValue;
        } else if (doubleValue != null) {
            return doubleValue;
        } else if (longValue != null) {
            return longValue;
        } else if (entityReferenceValue != null) {
            return entityReferenceValue;
        }
        return null;
    }

    /**
     * Set value of a given type
     * 
     * @param value Value to set
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void setValue(Object value) {

        dateValue = null;
        doubleValue = null;
        longValue = null;
        stringValue = null;
        entityReferenceValue = null;
        mapStringValue = null;
        mapDateValue = null;
        mapLongValue = null;
        mapDoubleValue = null;
        mapEntityValue = null;
        listStringValue = null;
        listDateValue = null;
        listLongValue = null;
        listDoubleValue = null;
        listEntityValue = null;

        if (value instanceof Date) {
            dateValue = (Date) value;

        } else if (value instanceof BigDecimal) {
            doubleValue = ((BigDecimal) value).setScale(2, RoundingMode.HALF_UP).doubleValue();

        } else if (value instanceof Double) {
            doubleValue = (Double) value;

        } else if (value instanceof Long) {
            longValue = (Long) value;

        } else if (value instanceof Integer) {
            longValue = ((Integer) value).longValue();

        } else if (value instanceof String) {
            stringValue = (String) value;

        } else if (value instanceof BusinessEntity) {
            setEntityReferenceValue(new EntityReferenceWrapper((BusinessEntity) value));

        } else if (value instanceof EntityReferenceWrapper) {
            setEntityReferenceValue((EntityReferenceWrapper) value);

        } else if (value instanceof Map) {
            setMapValue((Map) value);

        } else if (value instanceof List) {
            setListValue((List) value);
        }
    }

    @Override
    public String toString() {
        final int maxLen = 10;
        return String.format(
            "CustomFieldValue [stringValue=%s, dateValue=%s, longValue=%s, doubleValue=%s, entityReferenceValue=%s, listValue=%s, mapValue=%s, mapValuesForGUI=%s, matrixValuesForGUI=%s, childEntityValuesForGUI=%s, entityReferenceValueForGUI=%s]",
            stringValue, dateValue, longValue, doubleValue, entityReferenceValue, getListValue() != null ? toString(getListValue(), maxLen) : null,
            getMapValue() != null ? toString(getMapValue().entrySet(), maxLen) : null, mapValuesForGUI != null ? toString(mapValuesForGUI, maxLen) : null,
            matrixValuesForGUI != null ? toString(matrixValuesForGUI, maxLen) : null, childEntityValuesForGUI != null ? toString(childEntityValuesForGUI, maxLen) : null,
            entityReferenceValueForGUI);
    }

    /**
     * @param collection collection
     * @param maxLen max length
     * @return collection as string.
     */
    private String toString(Collection<?> collection, int maxLen) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        int i = 0;
        for (Iterator<?> iterator = collection.iterator(); iterator.hasNext() && i < maxLen; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(iterator.next());
        }
        builder.append("]");
        return builder.toString();
    }

    /**
     * Convert (deserialize) a string value (serialized value in case of list, map, entity, childEntity) into an object according to custom field data type definition.
     * 
     * @param cft custom field template.s
     * @param valueToConvert Value to convert
     * @return A value corresponding to custom field data type definition
     */
    public static Object parseValueFromString(CustomFieldTemplate cft, String valueToConvert) {

        if (valueToConvert == null) {
            return null;
        }

        try {

            if (cft.getStorageType() == CustomFieldStorageTypeEnum.SINGLE && !cft.getFieldType().isStoredSerialized()) {
                if (cft.getFieldType() == CustomFieldTypeEnum.DOUBLE) {
                    return Double.parseDouble(valueToConvert);
                } else if (cft.getFieldType() == CustomFieldTypeEnum.LONG) {
                    return Long.parseLong(valueToConvert);
                } else if (cft.getFieldType() == CustomFieldTypeEnum.STRING || cft.getFieldType() == CustomFieldTypeEnum.LIST
                        || cft.getFieldType() == CustomFieldTypeEnum.TEXT_AREA) {
                    return valueToConvert;
                } else if (cft.getFieldType() == CustomFieldTypeEnum.DATE) {
                    return DateUtils.parseDateWithPattern(valueToConvert, DateUtils.DATE_TIME_PATTERN);
                }
            } else {

                List<String> matrixColumnNames = cft.getMatrixColumnCodes();

                String serializedValueWithMetaInfo = null;

                // Add seralization metadata if it is not available in json string
                if (!valueToConvert.contains(SERIALIZATION_SEPARATOR) || valueToConvert.indexOf("{") < valueToConvert.indexOf(SERIALIZATION_SEPARATOR)) {
                    if (cft.getStorageType() == CustomFieldStorageTypeEnum.SINGLE && cft.getFieldType() == CustomFieldTypeEnum.ENTITY) {
                        serializedValueWithMetaInfo = "entity" + SERIALIZATION_SEPARATOR + valueToConvert;

                    } else if (cft.getStorageType() == CustomFieldStorageTypeEnum.SINGLE && cft.getFieldType() == CustomFieldTypeEnum.CHILD_ENTITY) {
                        serializedValueWithMetaInfo = "childEntity" + SERIALIZATION_SEPARATOR + valueToConvert;

                    } else if (cft.getStorageType() == CustomFieldStorageTypeEnum.LIST) {
                        serializedValueWithMetaInfo = "list_" + cft.getFieldType().getDataClass().getSimpleName() + SERIALIZATION_SEPARATOR + valueToConvert;

                    } else if (cft.getStorageType() == CustomFieldStorageTypeEnum.MAP) {
                        serializedValueWithMetaInfo = "map_" + cft.getFieldType().getDataClass().getSimpleName() + SERIALIZATION_SEPARATOR + valueToConvert;

                    } else if (cft.getStorageType() == CustomFieldStorageTypeEnum.MATRIX) {
                        serializedValueWithMetaInfo = "matrix_" + cft.getFieldType().getDataClass().getSimpleName() + SERIALIZATION_SEPARATOR
                                + StringUtils.concatenate(MATRIX_COLUMN_NAME_SEPARATOR, matrixColumnNames) + SERIALIZATION_SEPARATOR + valueToConvert;
                    }
                } else {
                    serializedValueWithMetaInfo = valueToConvert;
                }

                Object deserializedValue = CustomFieldValue.deserializeValueFromString(serializedValueWithMetaInfo);
                return deserializedValue;

            }

        } catch (Exception e) {
            Logger log = LoggerFactory.getLogger(CustomFieldValue.class);
            log.error("Failed to parse {} for CFT {}", valueToConvert, cft, e);
            return null;
        }
        return null;
    }

    /**
     * Convert (serialize) object according to custom field data type definition to a string value (serialized value in case of list, map, entity, childEntity).
     * @param cft customer field template.
     * @param valueToConvert Value to convert
     * @return A value corresponding to custom field data type definition
     */
    public static String convertValueToString(CustomFieldTemplate cft, Object valueToConvert) {

        if (valueToConvert == null) {
            return null;
        }

        try {

            if (cft.getStorageType() == CustomFieldStorageTypeEnum.SINGLE && !cft.getFieldType().isStoredSerialized()) {
                if (cft.getFieldType() == CustomFieldTypeEnum.DATE) {
                    return DateUtils.formatDateWithPattern((Date) valueToConvert, DateUtils.DATE_TIME_PATTERN);
                } else {
                    return valueToConvert.toString();
                }
            } else {
                return serializeValueToString(cft, valueToConvert);
            }

        } catch (Exception e) {
            Logger log = LoggerFactory.getLogger(CustomFieldValue.class);
            log.error("Failed to convert {} to String for CFT {}", valueToConvert, cft, e);
            return null;
        }
    }
}