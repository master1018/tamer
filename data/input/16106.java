public class ModelMBeanAttributeInfo
    extends MBeanAttributeInfo
    implements DescriptorAccess {
    private static final long oldSerialVersionUID = 7098036920755973145L;
    private static final long newSerialVersionUID = 6181543027787327345L;
    private static final ObjectStreamField[] oldSerialPersistentFields =
    {
      new ObjectStreamField("attrDescriptor", Descriptor.class),
      new ObjectStreamField("currClass", String.class)
    };
    private static final ObjectStreamField[] newSerialPersistentFields =
    {
      new ObjectStreamField("attrDescriptor", Descriptor.class)
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
        private Descriptor attrDescriptor = validDescriptor(null);
        private final static String currClass = "ModelMBeanAttributeInfo";
        public ModelMBeanAttributeInfo(String name,
                                       String description,
                                       Method getter,
                                       Method setter)
        throws javax.management.IntrospectionException {
                super(name, description, getter, setter);
                if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
                    MODELMBEAN_LOGGER.logp(Level.FINER,
                            ModelMBeanAttributeInfo.class.getName(),
                            "ModelMBeanAttributeInfo(" +
                            "String,String,Method,Method)",
                            "Entry", name);
                }
                attrDescriptor = validDescriptor(null);
        }
        public ModelMBeanAttributeInfo(String name,
                                       String description,
                                       Method getter,
                                       Method setter,
                                       Descriptor descriptor)
        throws javax.management.IntrospectionException {
                super(name, description, getter, setter);
                if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
                    MODELMBEAN_LOGGER.logp(Level.FINER,
                            ModelMBeanAttributeInfo.class.getName(),
                            "ModelMBeanAttributeInfo(" +
                            "String,String,Method,Method,Descriptor)",
                            "Entry", name);
                }
                attrDescriptor = validDescriptor(descriptor);
        }
        public ModelMBeanAttributeInfo(String name,
                                       String type,
                                       String description,
                                       boolean isReadable,
                                       boolean isWritable,
                                       boolean isIs)
    {
                super(name, type, description, isReadable, isWritable, isIs);
                if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
                    MODELMBEAN_LOGGER.logp(Level.FINER,
                            ModelMBeanAttributeInfo.class.getName(),
                            "ModelMBeanAttributeInfo(" +
                            "String,String,String,boolean,boolean,boolean)",
                            "Entry", name);
                }
                attrDescriptor = validDescriptor(null);
        }
        public ModelMBeanAttributeInfo(String name,
                                       String type,
                                       String description,
                                       boolean isReadable,
                                       boolean isWritable,
                                       boolean isIs,
                                       Descriptor descriptor)
        {
                super(name, type, description, isReadable, isWritable, isIs);
                if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
                    MODELMBEAN_LOGGER.logp(Level.FINER,
                            ModelMBeanAttributeInfo.class.getName(),
                            "ModelMBeanAttributeInfo(String,String,String," +
                            "boolean,boolean,boolean,Descriptor)",
                            "Entry", name);
                }
                attrDescriptor = validDescriptor(descriptor);
        }
        public ModelMBeanAttributeInfo(ModelMBeanAttributeInfo inInfo)
        {
                super(inInfo.getName(),
                          inInfo.getType(),
                          inInfo.getDescription(),
                          inInfo.isReadable(),
                          inInfo.isWritable(),
                          inInfo.isIs());
                if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
                    MODELMBEAN_LOGGER.logp(Level.FINER,
                            ModelMBeanAttributeInfo.class.getName(),
                            "ModelMBeanAttributeInfo(ModelMBeanAttributeInfo)",
                            "Entry");
                }
                Descriptor newDesc = inInfo.getDescriptor();
                attrDescriptor = validDescriptor(newDesc);
        }
        public Descriptor getDescriptor() {
            if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
                MODELMBEAN_LOGGER.logp(Level.FINER,
                        ModelMBeanAttributeInfo.class.getName(),
                        "getDescriptor()", "Entry");
            }
                if (attrDescriptor == null) {
                    attrDescriptor = validDescriptor(null);
                }
                return((Descriptor)attrDescriptor.clone());
        }
        public void setDescriptor(Descriptor inDescriptor) {
            attrDescriptor =  validDescriptor(inDescriptor);
        }
        @Override
        public Object clone()
        {
            if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
                MODELMBEAN_LOGGER.logp(Level.FINER,
                        ModelMBeanAttributeInfo.class.getName(),
                        "clone()", "Entry");
            }
                return(new ModelMBeanAttributeInfo(this));
        }
        @Override
        public String toString()
        {
            return
                "ModelMBeanAttributeInfo: " + this.getName() +
                " ; Description: " + this.getDescription() +
                " ; Types: " + this.getType() +
                " ; isReadable: " + this.isReadable() +
                " ; isWritable: " + this.isWritable() +
                " ; Descriptor: " + this.getDescriptor();
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
                clone.setField("descriptorType", "attribute");
                MODELMBEAN_LOGGER.finer("Defaulting descriptorType to \"attribute\"");
            }
            if (clone.getFieldValue("displayName") == null) {
                clone.setField("displayName",this.getName());
                MODELMBEAN_LOGGER.finer("Defaulting Descriptor displayName to " + this.getName());
            }
            if (!clone.isValid()) {
                 throw new RuntimeOperationsException(new IllegalArgumentException("Invalid Descriptor argument"),
                    "The isValid() method of the Descriptor object itself returned false,"+
                    "one or more required fields are invalid. Descriptor:" + clone.toString());
            }
            if (!getName().equalsIgnoreCase((String)clone.getFieldValue("name"))) {
                    throw new RuntimeOperationsException(new IllegalArgumentException("Invalid Descriptor argument"),
                    "The Descriptor \"name\" field does not match the object described. " +
                     " Expected: "+ this.getName() + " , was: " + clone.getFieldValue("name"));
            }
            if (!"attribute".equalsIgnoreCase((String)clone.getFieldValue("descriptorType"))) {
                     throw new RuntimeOperationsException(new IllegalArgumentException("Invalid Descriptor argument"),
                    "The Descriptor \"descriptorType\" field does not match the object described. " +
                     " Expected: \"attribute\" ," + " was: " + clone.getFieldValue("descriptorType"));
            }
            return clone;
        }
    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
      in.defaultReadObject();
    }
    private void writeObject(ObjectOutputStream out)
            throws IOException {
      if (compat)
      {
        ObjectOutputStream.PutField fields = out.putFields();
        fields.put("attrDescriptor", attrDescriptor);
        fields.put("currClass", currClass);
        out.writeFields();
      }
      else
      {
        out.defaultWriteObject();
      }
    }
}
