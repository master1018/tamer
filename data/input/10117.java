public class ModelMBeanNotificationInfo
    extends MBeanNotificationInfo
    implements DescriptorAccess {
    private static final long oldSerialVersionUID = -5211564525059047097L;
    private static final long newSerialVersionUID = -7445681389570207141L;
    private static final ObjectStreamField[] oldSerialPersistentFields =
    {
      new ObjectStreamField("notificationDescriptor", Descriptor.class),
      new ObjectStreamField("currClass", String.class)
    };
    private static final ObjectStreamField[] newSerialPersistentFields =
    {
      new ObjectStreamField("notificationDescriptor", Descriptor.class)
    };
    private static final long serialVersionUID;
    private static final ObjectStreamField[] serialPersistentFields;
    private static boolean compat = false;
    static {
        try {
            GetPropertyAction act = new GetPropertyAction("jmx.serial.form");
            String form = AccessController.doPrivileged(act);
            compat = (form != null && form.equals("1.0"));
        } catch (Exception e) {
        }
        if (compat) {
            serialPersistentFields = oldSerialPersistentFields;
            serialVersionUID = oldSerialVersionUID;
        } else {
            serialPersistentFields = newSerialPersistentFields;
            serialVersionUID = newSerialVersionUID;
        }
    }
    private Descriptor notificationDescriptor;
    private static final String currClass = "ModelMBeanNotificationInfo";
    public ModelMBeanNotificationInfo(String[] notifTypes,
                                      String name,
                                      String description) {
        this(notifTypes,name,description,null);
    }
    public ModelMBeanNotificationInfo(String[] notifTypes,
                                      String name,
                                      String description,
                                      Descriptor descriptor) {
        super(notifTypes, name, description);
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanNotificationInfo.class.getName(),
                    "ModelMBeanNotificationInfo", "Entry");
        }
        notificationDescriptor = validDescriptor(descriptor);
    }
    public ModelMBeanNotificationInfo(ModelMBeanNotificationInfo inInfo) {
        this(inInfo.getNotifTypes(),
             inInfo.getName(),
             inInfo.getDescription(),inInfo.getDescriptor());
    }
    public Object clone () {
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanNotificationInfo.class.getName(),
                    "clone()", "Entry");
        }
        return(new ModelMBeanNotificationInfo(this));
    }
    public Descriptor getDescriptor() {
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanNotificationInfo.class.getName(),
                    "getDescriptor()", "Entry");
        }
        if (notificationDescriptor == null) {
            if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
                MODELMBEAN_LOGGER.logp(Level.FINER,
                        ModelMBeanNotificationInfo.class.getName(),
                        "getDescriptor()", "Descriptor value is null, " +
                        "setting descriptor to default values");
            }
            notificationDescriptor = validDescriptor(null);
        }
        return((Descriptor)notificationDescriptor.clone());
    }
    public void setDescriptor(Descriptor inDescriptor) {
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanNotificationInfo.class.getName(),
                    "setDescriptor(Descriptor)", "Entry");
        }
        notificationDescriptor = validDescriptor(inDescriptor);
    }
    public String toString() {
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanNotificationInfo.class.getName(),
                    "toString()", "Entry");
        }
        final StringBuilder retStr = new StringBuilder();
        retStr.append("ModelMBeanNotificationInfo: ")
            .append(this.getName());
        retStr.append(" ; Description: ")
            .append(this.getDescription());
        retStr.append(" ; Descriptor: ")
            .append(this.getDescriptor());
        retStr.append(" ; Types: ");
        String[] nTypes = this.getNotifTypes();
        for (int i=0; i < nTypes.length; i++) {
            if (i > 0) retStr.append(", ");
            retStr.append(nTypes[i]);
        }
        return retStr.toString();
    }
    private Descriptor validDescriptor(final Descriptor in) throws RuntimeOperationsException {
        Descriptor clone;
        boolean defaulted = (in == null);
        if (defaulted) {
            clone = new DescriptorSupport();
            MODELMBEAN_LOGGER.finer("Null Descriptor, creating new.");
        } else {
            clone = (Descriptor) in.clone();
        }
        if (defaulted && clone.getFieldValue("name")==null) {
            clone.setField("name", this.getName());
            MODELMBEAN_LOGGER.finer("Defaulting Descriptor name to " + this.getName());
        }
        if (defaulted && clone.getFieldValue("descriptorType")==null) {
            clone.setField("descriptorType", "notification");
            MODELMBEAN_LOGGER.finer("Defaulting descriptorType to \"notification\"");
        }
        if (clone.getFieldValue("displayName") == null) {
            clone.setField("displayName",this.getName());
            MODELMBEAN_LOGGER.finer("Defaulting Descriptor displayName to " + this.getName());
        }
        if (clone.getFieldValue("severity") == null) {
            clone.setField("severity", "6");
            MODELMBEAN_LOGGER.finer("Defaulting Descriptor severity field to 6");
        }
        if (!clone.isValid()) {
             throw new RuntimeOperationsException(new IllegalArgumentException("Invalid Descriptor argument"),
                "The isValid() method of the Descriptor object itself returned false,"+
                "one or more required fields are invalid. Descriptor:" + clone.toString());
        }
        if (!getName().equalsIgnoreCase((String) clone.getFieldValue("name"))) {
                throw new RuntimeOperationsException(new IllegalArgumentException("Invalid Descriptor argument"),
                "The Descriptor \"name\" field does not match the object described. " +
                 " Expected: "+ this.getName() + " , was: " + clone.getFieldValue("name"));
        }
        if (!"notification".equalsIgnoreCase((String) clone.getFieldValue("descriptorType"))) {
                 throw new RuntimeOperationsException(new IllegalArgumentException("Invalid Descriptor argument"),
                "The Descriptor \"descriptorType\" field does not match the object described. " +
                 " Expected: \"notification\" ," + " was: " + clone.getFieldValue("descriptorType"));
        }
        return clone;
    }
    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }
    private void writeObject(ObjectOutputStream out)
        throws IOException {
        if (compat) {
            ObjectOutputStream.PutField fields = out.putFields();
            fields.put("notificationDescriptor", notificationDescriptor);
            fields.put("currClass", currClass);
            out.writeFields();
        } else {
            out.defaultWriteObject();
        }
    }
}
