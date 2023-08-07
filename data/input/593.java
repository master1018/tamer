public class Network implements java.io.Serializable {
    private java.lang.Long id;
    private java.lang.String displayName;
    private java.lang.String networkCode;
    private java.lang.String propertyCode;
    private java.lang.String timeZone;
    private java.lang.String currencyCode;
    private java.lang.String effectiveRootAdUnitId;
    private java.lang.Long contentBrowseCustomTargetingKeyId;
    public Network() {
    }
    public Network(java.lang.Long id, java.lang.String displayName, java.lang.String networkCode, java.lang.String propertyCode, java.lang.String timeZone, java.lang.String currencyCode, java.lang.String effectiveRootAdUnitId, java.lang.Long contentBrowseCustomTargetingKeyId) {
        this.id = id;
        this.displayName = displayName;
        this.networkCode = networkCode;
        this.propertyCode = propertyCode;
        this.timeZone = timeZone;
        this.currencyCode = currencyCode;
        this.effectiveRootAdUnitId = effectiveRootAdUnitId;
        this.contentBrowseCustomTargetingKeyId = contentBrowseCustomTargetingKeyId;
    }
    public java.lang.Long getId() {
        return id;
    }
    public void setId(java.lang.Long id) {
        this.id = id;
    }
    public java.lang.String getDisplayName() {
        return displayName;
    }
    public void setDisplayName(java.lang.String displayName) {
        this.displayName = displayName;
    }
    public java.lang.String getNetworkCode() {
        return networkCode;
    }
    public void setNetworkCode(java.lang.String networkCode) {
        this.networkCode = networkCode;
    }
    public java.lang.String getPropertyCode() {
        return propertyCode;
    }
    public void setPropertyCode(java.lang.String propertyCode) {
        this.propertyCode = propertyCode;
    }
    public java.lang.String getTimeZone() {
        return timeZone;
    }
    public void setTimeZone(java.lang.String timeZone) {
        this.timeZone = timeZone;
    }
    public java.lang.String getCurrencyCode() {
        return currencyCode;
    }
    public void setCurrencyCode(java.lang.String currencyCode) {
        this.currencyCode = currencyCode;
    }
    public java.lang.String getEffectiveRootAdUnitId() {
        return effectiveRootAdUnitId;
    }
    public void setEffectiveRootAdUnitId(java.lang.String effectiveRootAdUnitId) {
        this.effectiveRootAdUnitId = effectiveRootAdUnitId;
    }
    public java.lang.Long getContentBrowseCustomTargetingKeyId() {
        return contentBrowseCustomTargetingKeyId;
    }
    public void setContentBrowseCustomTargetingKeyId(java.lang.Long contentBrowseCustomTargetingKeyId) {
        this.contentBrowseCustomTargetingKeyId = contentBrowseCustomTargetingKeyId;
    }
    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Network)) return false;
        Network other = (Network) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && ((this.id == null && other.getId() == null) || (this.id != null && this.id.equals(other.getId()))) && ((this.displayName == null && other.getDisplayName() == null) || (this.displayName != null && this.displayName.equals(other.getDisplayName()))) && ((this.networkCode == null && other.getNetworkCode() == null) || (this.networkCode != null && this.networkCode.equals(other.getNetworkCode()))) && ((this.propertyCode == null && other.getPropertyCode() == null) || (this.propertyCode != null && this.propertyCode.equals(other.getPropertyCode()))) && ((this.timeZone == null && other.getTimeZone() == null) || (this.timeZone != null && this.timeZone.equals(other.getTimeZone()))) && ((this.currencyCode == null && other.getCurrencyCode() == null) || (this.currencyCode != null && this.currencyCode.equals(other.getCurrencyCode()))) && ((this.effectiveRootAdUnitId == null && other.getEffectiveRootAdUnitId() == null) || (this.effectiveRootAdUnitId != null && this.effectiveRootAdUnitId.equals(other.getEffectiveRootAdUnitId()))) && ((this.contentBrowseCustomTargetingKeyId == null && other.getContentBrowseCustomTargetingKeyId() == null) || (this.contentBrowseCustomTargetingKeyId != null && this.contentBrowseCustomTargetingKeyId.equals(other.getContentBrowseCustomTargetingKeyId())));
        __equalsCalc = null;
        return _equals;
    }
    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getId() != null) {
            _hashCode += getId().hashCode();
        }
        if (getDisplayName() != null) {
            _hashCode += getDisplayName().hashCode();
        }
        if (getNetworkCode() != null) {
            _hashCode += getNetworkCode().hashCode();
        }
        if (getPropertyCode() != null) {
            _hashCode += getPropertyCode().hashCode();
        }
        if (getTimeZone() != null) {
            _hashCode += getTimeZone().hashCode();
        }
        if (getCurrencyCode() != null) {
            _hashCode += getCurrencyCode().hashCode();
        }
        if (getEffectiveRootAdUnitId() != null) {
            _hashCode += getEffectiveRootAdUnitId().hashCode();
        }
        if (getContentBrowseCustomTargetingKeyId() != null) {
            _hashCode += getContentBrowseCustomTargetingKeyId().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
    private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(Network.class, true);
    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("https:
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("id");
        elemField.setXmlName(new javax.xml.namespace.QName("https:
        elemField.setXmlType(new javax.xml.namespace.QName("http:
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("displayName");
        elemField.setXmlName(new javax.xml.namespace.QName("https:
        elemField.setXmlType(new javax.xml.namespace.QName("http:
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("networkCode");
        elemField.setXmlName(new javax.xml.namespace.QName("https:
        elemField.setXmlType(new javax.xml.namespace.QName("http:
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("propertyCode");
        elemField.setXmlName(new javax.xml.namespace.QName("https:
        elemField.setXmlType(new javax.xml.namespace.QName("http:
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("timeZone");
        elemField.setXmlName(new javax.xml.namespace.QName("https:
        elemField.setXmlType(new javax.xml.namespace.QName("http:
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("currencyCode");
        elemField.setXmlName(new javax.xml.namespace.QName("https:
        elemField.setXmlType(new javax.xml.namespace.QName("http:
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("effectiveRootAdUnitId");
        elemField.setXmlName(new javax.xml.namespace.QName("https:
        elemField.setXmlType(new javax.xml.namespace.QName("http:
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("contentBrowseCustomTargetingKeyId");
        elemField.setXmlName(new javax.xml.namespace.QName("https:
        elemField.setXmlType(new javax.xml.namespace.QName("http:
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }
    public static org.apache.axis.encoding.Serializer getSerializer(java.lang.String mechType, java.lang.Class _javaType, javax.xml.namespace.QName _xmlType) {
        return new org.apache.axis.encoding.ser.BeanSerializer(_javaType, _xmlType, typeDesc);
    }
    public static org.apache.axis.encoding.Deserializer getDeserializer(java.lang.String mechType, java.lang.Class _javaType, javax.xml.namespace.QName _xmlType) {
        return new org.apache.axis.encoding.ser.BeanDeserializer(_javaType, _xmlType, typeDesc);
    }
}
