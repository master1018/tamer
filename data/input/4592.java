public class MonitoredObjectImpl implements MonitoredObject {
    private final String name;
    private final String description;
    private Map children = new HashMap();
    private Map monitoredAttributes = new HashMap();
    private MonitoredObject parent = null;
    MonitoredObjectImpl( String name, String description ) {
        this.name = name;
        this.description = description;
    }
    public MonitoredObject getChild( String name ) {
        synchronized( this ) {
            return (MonitoredObject) children.get( name );
        }
    }
    public Collection getChildren( ) {
        synchronized( this ) {
            return children.values();
        }
    }
    public void addChild( MonitoredObject m ) {
        if (m != null){
            synchronized( this ) {
                children.put( m.getName(), m);
                m.setParent( this );
            }
        }
    }
    public void removeChild( String name ) {
        if (name != null){
            synchronized( this ) {
                children.remove( name );
            }
        }
    }
    public synchronized MonitoredObject getParent( ) {
       return parent;
    }
    public synchronized void setParent( MonitoredObject p ) {
        parent = p;
    }
    public MonitoredAttribute getAttribute( String name ) {
        synchronized( this ) {
            return (MonitoredAttribute) monitoredAttributes.get( name );
        }
    }
    public Collection getAttributes( ) {
        synchronized( this ) {
            return monitoredAttributes.values();
        }
    }
    public void addAttribute( MonitoredAttribute value ) {
        if (value != null) {
            synchronized( this ) {
                monitoredAttributes.put( value.getName(), value );
            }
        }
    }
    public void removeAttribute( String name ) {
        if (name != null) {
            synchronized( this ) {
                monitoredAttributes.remove( name );
            }
        }
    }
    public void clearState( ) {
        synchronized( this ) {
            Iterator i = monitoredAttributes.values().iterator();
            while( i.hasNext( ) ) {
                ((MonitoredAttribute)i.next()).clearState();
            }
            i = children.values().iterator();
            while( i.hasNext() ) {
                ((MonitoredObject)i.next()).clearState();
           }
        }
    }
    public String getName( ) {
        return name;
    }
    public String getDescription( ) {
        return description;
    }
}
