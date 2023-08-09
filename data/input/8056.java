public class SnmpNull extends SnmpValue {
    private static final long serialVersionUID = 1783782515994279177L;
    public SnmpNull() {
        tag = NullTag ;
    }
    public SnmpNull(String dummy) {
        this();
    }
    public SnmpNull(int t) {
        tag = t ;
    }
    public int getTag() {
        return tag ;
    }
    public String toString() {
        String result = "" ;
        if (tag != 5) {
            result += "[" + tag + "] " ;
        }
        result += "NULL" ;
        switch(tag) {
        case errNoSuchObjectTag :
            result += " (noSuchObject)" ;
            break ;
        case errNoSuchInstanceTag :
            result += " (noSuchInstance)" ;
            break ;
        case errEndOfMibViewTag :
            result += " (endOfMibView)" ;
            break ;
        }
        return result ;
    }
    public SnmpOid toOid() {
        throw new IllegalArgumentException() ;
    }
    final synchronized public SnmpValue duplicate() {
        return (SnmpValue) clone() ;
    }
    final synchronized public Object clone() {
        SnmpNull  newclone = null ;
        try {
            newclone = (SnmpNull) super.clone() ;
            newclone.tag = tag ;
        } catch (CloneNotSupportedException e) {
            throw new InternalError() ; 
        }
        return newclone ;
    }
    final public String getTypeName() {
        return name ;
    }
    public boolean isNoSuchObjectValue() {
        return (tag == SnmpDataTypeEnums.errNoSuchObjectTag);
    }
    public boolean isNoSuchInstanceValue() {
        return (tag == SnmpDataTypeEnums.errNoSuchInstanceTag);
    }
    public boolean isEndOfMibViewValue() {
        return (tag == SnmpDataTypeEnums.errEndOfMibViewTag);
    }
    final static String name = "Null" ;
    private int tag = 5 ;
}
