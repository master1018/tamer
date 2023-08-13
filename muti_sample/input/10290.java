public abstract class SnmpValue implements Cloneable, Serializable, SnmpDataTypeEnums {
    public String toAsn1String() {
        return "[" + getTypeName() + "] " + toString();
    }
    public abstract SnmpOid toOid() ;
    public abstract String getTypeName() ;
    public abstract SnmpValue duplicate() ;
    public boolean isNoSuchObjectValue() {
        return false;
    }
    public boolean isNoSuchInstanceValue() {
        return false;
    }
    public boolean isEndOfMibViewValue() {
        return false;
    }
}
