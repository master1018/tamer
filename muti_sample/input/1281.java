public abstract class MonitoredAttributeBase implements MonitoredAttribute {
    String name;
    MonitoredAttributeInfo attributeInfo;
    public MonitoredAttributeBase( String name, MonitoredAttributeInfo info ) {
        this.name = name;
        this.attributeInfo = info;
    }
    MonitoredAttributeBase( String name ) {
        this.name = name;
    }
    void setMonitoredAttributeInfo( MonitoredAttributeInfo info ) {
        this.attributeInfo = info;
    }
    public void clearState( ) {
    }
    public abstract Object getValue( );
    public void setValue( Object value ) {
        if( !attributeInfo.isWritable() ) {
            throw new IllegalStateException(
                "The Attribute " + name + " is not Writable..." );
        }
        throw new IllegalStateException(
            "The method implementation is not provided for the attribute " +
            name );
    }
    public MonitoredAttributeInfo getAttributeInfo( ) {
        return attributeInfo;
    }
    public String getName( ) {
        return name;
    }
} 
