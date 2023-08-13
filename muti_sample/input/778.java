public class ModelMBeanConstructorInfo
    extends MBeanConstructorInfo
    implements DescriptorAccess {
    private static final long oldSerialVersionUID = -4440125391095574518L;
    private static final long newSerialVersionUID = 3862947819818064362L;
    private static final ObjectStreamField[] oldSerialPersistentFields =
    {
      new ObjectStreamField("consDescriptor", Descriptor.class),
      new ObjectStreamField("currClass", String.class)
    };
    private static final ObjectStreamField[] newSerialPersistentFields =
    {
      new ObjectStreamField("consDescriptor", Descriptor.class)
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
        private Descriptor consDescriptor = validDescriptor(null);
        private final static String currClass = "ModelMBeanConstructorInfo";
        public ModelMBeanConstructorInfo(String description,
                                         Constructor<?> constructorMethod)
    {
                super(description, constructorMethod);
                if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
                    MODELMBEAN_LOGGER.logp(Level.FINER,
                            ModelMBeanConstructorInfo.class.getName(),
                            "ModelMBeanConstructorInfo(String,Constructor)",
                            "Entry");
                }
                consDescriptor = validDescriptor(null);
        }
        public ModelMBeanConstructorInfo(String description,
                                         Constructor<?> constructorMethod,
                                         Descriptor descriptor)
        {
                super(description, constructorMethod);
                if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
                    MODELMBEAN_LOGGER.logp(Level.FINER,
                            ModelMBeanConstructorInfo.class.getName(),
                            "ModelMBeanConstructorInfo(" +
                            "String,Constructor,Descriptor)", "Entry");
                }
                consDescriptor = validDescriptor(descriptor);
        }
        public ModelMBeanConstructorInfo(String name,
                                         String description,
                                         MBeanParameterInfo[] signature)
        {
                super(name, description, signature);
                if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
                    MODELMBEAN_LOGGER.logp(Level.FINER,
                            ModelMBeanConstructorInfo.class.getName(),
                            "ModelMBeanConstructorInfo(" +
                            "String,String,MBeanParameterInfo[])", "Entry");
                }
                consDescriptor = validDescriptor(null);
        }
        public ModelMBeanConstructorInfo(String name,
                                         String description,
                                         MBeanParameterInfo[] signature,
                                         Descriptor descriptor)
        {
                super(name, description, signature);
                if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
                    MODELMBEAN_LOGGER.logp(Level.FINER,
                            ModelMBeanConstructorInfo.class.getName(),
                            "ModelMBeanConstructorInfo(" +
                            "String,String,MBeanParameterInfo[],Descriptor)",
                            "Entry");
                }
                consDescriptor = validDescriptor(descriptor);
        }
        ModelMBeanConstructorInfo(ModelMBeanConstructorInfo old)
        {
                super(old.getName(), old.getDescription(), old.getSignature());
                if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
                    MODELMBEAN_LOGGER.logp(Level.FINER,
                            ModelMBeanConstructorInfo.class.getName(),
                            "ModelMBeanConstructorInfo(" +
                            "ModelMBeanConstructorInfo)", "Entry");
                }
                consDescriptor = validDescriptor(consDescriptor);
        }
        @Override
        public Object clone ()
        {
            if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
                MODELMBEAN_LOGGER.logp(Level.FINER,
                        ModelMBeanConstructorInfo.class.getName(),
                        "clone()", "Entry");
            }
                return(new ModelMBeanConstructorInfo(this)) ;
        }
        @Override
        public Descriptor getDescriptor()
        {
            if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
                MODELMBEAN_LOGGER.logp(Level.FINER,
                        ModelMBeanConstructorInfo.class.getName(),
                        "getDescriptor()", "Entry");
            }
            if (consDescriptor == null){
                consDescriptor = validDescriptor(null);
            }
            return((Descriptor)consDescriptor.clone());
        }
        public void setDescriptor(Descriptor inDescriptor)
        {
            if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
                MODELMBEAN_LOGGER.logp(Level.FINER,
                        ModelMBeanConstructorInfo.class.getName(),
                        "setDescriptor()", "Entry");
            }
            consDescriptor = validDescriptor(inDescriptor);
        }
        @Override
        public String toString()
        {
            if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
                MODELMBEAN_LOGGER.logp(Level.FINER,
                        ModelMBeanConstructorInfo.class.getName(),
                        "toString()", "Entry");
            }
                String retStr =
                    "ModelMBeanConstructorInfo: " + this.getName() +
                    " ; Description: " + this.getDescription() +
                    " ; Descriptor: " + this.getDescriptor() +
                    " ; Signature: ";
                MBeanParameterInfo[] pTypes = this.getSignature();
                for (int i=0; i < pTypes.length; i++)
                {
                        retStr = retStr.concat((pTypes[i]).getType() + ", ");
                }
                return retStr;
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
                clone.setField("descriptorType", "operation");
                MODELMBEAN_LOGGER.finer("Defaulting descriptorType to \"operation\"");
            }
            if (clone.getFieldValue("displayName") == null) {
                clone.setField("displayName",this.getName());
                MODELMBEAN_LOGGER.finer("Defaulting Descriptor displayName to " + this.getName());
            }
            if (clone.getFieldValue("role") == null) {
                clone.setField("role","constructor");
                MODELMBEAN_LOGGER.finer("Defaulting Descriptor role field to \"constructor\"");
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
            if (!"operation".equalsIgnoreCase((String) clone.getFieldValue("descriptorType"))) {
                     throw new RuntimeOperationsException(new IllegalArgumentException("Invalid Descriptor argument"),
                    "The Descriptor \"descriptorType\" field does not match the object described. " +
                     " Expected: \"operation\" ," + " was: " + clone.getFieldValue("descriptorType"));
            }
            if (! ((String)clone.getFieldValue("role")).equalsIgnoreCase("constructor")) {
                     throw new RuntimeOperationsException(new IllegalArgumentException("Invalid Descriptor argument"),
                    "The Descriptor \"role\" field does not match the object described. " +
                     " Expected: \"constructor\" ," + " was: " + clone.getFieldValue("role"));
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
        fields.put("consDescriptor", consDescriptor);
        fields.put("currClass", currClass);
        out.writeFields();
      }
      else
      {
        out.defaultWriteObject();
      }
    }
}
