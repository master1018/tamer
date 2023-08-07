public class IDREFImpl extends org.apache.xmlbeans.impl.values.JavaStringHolderEx implements org.xmlsoap.schemas.soap.encoding.IDREF {
    private static final javax.xml.namespace.QName ID$0 = new javax.xml.namespace.QName("", "id");
    private static final javax.xml.namespace.QName HREF$2 = new javax.xml.namespace.QName("", "href");
    public IDREFImpl(org.apache.xmlbeans.SchemaType sType) {
        super(sType, true);
    }
    protected IDREFImpl(org.apache.xmlbeans.SchemaType sType, boolean b) {
        super(sType, b);
    }
    public java.lang.String getId() {
        synchronized (monitor()) {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue) get_store().find_attribute_user(ID$0);
            if (target == null) {
                return null;
            }
            return target.getStringValue();
        }
    }
    public org.apache.xmlbeans.XmlID xgetId() {
        synchronized (monitor()) {
            check_orphaned();
            org.apache.xmlbeans.XmlID target = null;
            target = (org.apache.xmlbeans.XmlID) get_store().find_attribute_user(ID$0);
            return target;
        }
    }
    public boolean isSetId() {
        synchronized (monitor()) {
            check_orphaned();
            return get_store().find_attribute_user(ID$0) != null;
        }
    }
    public void setId(java.lang.String id) {
        synchronized (monitor()) {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue) get_store().find_attribute_user(ID$0);
            if (target == null) {
                target = (org.apache.xmlbeans.SimpleValue) get_store().add_attribute_user(ID$0);
            }
            target.setStringValue(id);
        }
    }
    public void xsetId(org.apache.xmlbeans.XmlID id) {
        synchronized (monitor()) {
            check_orphaned();
            org.apache.xmlbeans.XmlID target = null;
            target = (org.apache.xmlbeans.XmlID) get_store().find_attribute_user(ID$0);
            if (target == null) {
                target = (org.apache.xmlbeans.XmlID) get_store().add_attribute_user(ID$0);
            }
            target.set(id);
        }
    }
    public void unsetId() {
        synchronized (monitor()) {
            check_orphaned();
            get_store().remove_attribute(ID$0);
        }
    }
    public java.lang.String getHref() {
        synchronized (monitor()) {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue) get_store().find_attribute_user(HREF$2);
            if (target == null) {
                return null;
            }
            return target.getStringValue();
        }
    }
    public org.apache.xmlbeans.XmlAnyURI xgetHref() {
        synchronized (monitor()) {
            check_orphaned();
            org.apache.xmlbeans.XmlAnyURI target = null;
            target = (org.apache.xmlbeans.XmlAnyURI) get_store().find_attribute_user(HREF$2);
            return target;
        }
    }
    public boolean isSetHref() {
        synchronized (monitor()) {
            check_orphaned();
            return get_store().find_attribute_user(HREF$2) != null;
        }
    }
    public void setHref(java.lang.String href) {
        synchronized (monitor()) {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue) get_store().find_attribute_user(HREF$2);
            if (target == null) {
                target = (org.apache.xmlbeans.SimpleValue) get_store().add_attribute_user(HREF$2);
            }
            target.setStringValue(href);
        }
    }
    public void xsetHref(org.apache.xmlbeans.XmlAnyURI href) {
        synchronized (monitor()) {
            check_orphaned();
            org.apache.xmlbeans.XmlAnyURI target = null;
            target = (org.apache.xmlbeans.XmlAnyURI) get_store().find_attribute_user(HREF$2);
            if (target == null) {
                target = (org.apache.xmlbeans.XmlAnyURI) get_store().add_attribute_user(HREF$2);
            }
            target.set(href);
        }
    }
    public void unsetHref() {
        synchronized (monitor()) {
            check_orphaned();
            get_store().remove_attribute(HREF$2);
        }
    }
}
