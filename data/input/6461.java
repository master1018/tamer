public final class EnumEditor implements PropertyEditor {
    private final List<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();
    private final Class type;
    private final String[] tags;
    private Object value;
    public EnumEditor( Class type ) {
        Object[] values = type.getEnumConstants();
        if ( values == null ) {
            throw new IllegalArgumentException( "Unsupported " + type );
        }
        this.type = type;
        this.tags = new String[values.length];
        for ( int i = 0; i < values.length; i++ ) {
            this.tags[i] = ( ( Enum )values[i] ).name();
        }
    }
    public Object getValue() {
        return this.value;
    }
    public void setValue( Object value ) {
        if ( ( value != null ) && !this.type.isInstance( value ) ) {
            throw new IllegalArgumentException( "Unsupported value: " + value );
        }
        Object oldValue;
        PropertyChangeListener[] listeners;
        synchronized ( this.listeners ) {
            oldValue = this.value;
            this.value = value;
            if ( ( value == null ) ? oldValue == null : value.equals( oldValue ) ) {
                return; 
            }
            int size = this.listeners.size();
            if ( size == 0 ) {
                return; 
            }
            listeners = this.listeners.toArray( new PropertyChangeListener[size] );
        }
        PropertyChangeEvent event = new PropertyChangeEvent( this, null, oldValue, value );
        for ( PropertyChangeListener listener : listeners ) {
            listener.propertyChange( event );
        }
    }
    public String getAsText() {
        return ( this.value != null )
                ? ( ( Enum )this.value ).name()
                : null;
    }
    public void setAsText( String text ) {
        setValue( ( text != null )
                ? Enum.valueOf( this.type, text )
                : null );
    }
    public String[] getTags() {
        return this.tags.clone();
    }
    public String getJavaInitializationString() {
        String name = getAsText();
        return ( name != null )
                ? this.type.getName() + '.' + name
                : "null";
    }
    public boolean isPaintable() {
        return false;
    }
    public void paintValue( Graphics gfx, Rectangle box ) {
    }
    public boolean supportsCustomEditor() {
        return false;
    }
    public Component getCustomEditor() {
        return null;
    }
    public void addPropertyChangeListener( PropertyChangeListener listener ) {
        synchronized ( this.listeners ) {
            this.listeners.add( listener );
        }
    }
    public void removePropertyChangeListener( PropertyChangeListener listener ) {
        synchronized ( this.listeners ) {
            this.listeners.remove( listener );
        }
    }
}
