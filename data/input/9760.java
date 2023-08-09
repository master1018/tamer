public class ModelMBeanOperationInfo extends MBeanOperationInfo
         implements DescriptorAccess
{
    private static final long oldSerialVersionUID = 9087646304346171239L;
    private static final long newSerialVersionUID = 6532732096650090465L;
    private static final ObjectStreamField[] oldSerialPersistentFields =
    {
      new ObjectStreamField("operationDescriptor", Descriptor.class),
      new ObjectStreamField("currClass", String.class)
    };
    private static final ObjectStreamField[] newSerialPersistentFields =
    {
      new ObjectStreamField("operationDescriptor", Descriptor.class)
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
        private Descriptor operationDescriptor = validDescriptor(null);
        private static final String currClass = "ModelMBeanOperationInfo";
        public ModelMBeanOperationInfo(String description,
                                       Method operationMethod)
        {
                super(description, operationMethod);
                if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
                    MODELMBEAN_LOGGER.logp(Level.FINER,
                            ModelMBeanOperationInfo.class.getName(),
                            "ModelMBeanOperationInfo(String,Method)",
                            "Entry");
                }
                operationDescriptor = validDescriptor(null);
        }
        public ModelMBeanOperationInfo(String description,
                                       Method operationMethod,
                                       Descriptor descriptor)
        {
                super(description, operationMethod);
                if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
                    MODELMBEAN_LOGGER.logp(Level.FINER,
                            ModelMBeanOperationInfo.class.getName(),
                            "ModelMBeanOperationInfo(String,Method,Descriptor)",
                            "Entry");
                }
                operationDescriptor = validDescriptor(descriptor);
        }
        public ModelMBeanOperationInfo(String name,
                                       String description,
                                       MBeanParameterInfo[] signature,
                                       String type,
                                       int impact)
        {
                super(name, description, signature, type, impact);
                if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
                    MODELMBEAN_LOGGER.logp(Level.FINER,
                            ModelMBeanOperationInfo.class.getName(),
                            "ModelMBeanOperationInfo(" +
                            "String,String,MBeanParameterInfo[],String,int)",
                            "Entry");
                }
                operationDescriptor = validDescriptor(null);
        }
        public ModelMBeanOperationInfo(String name,
                                       String description,
                                       MBeanParameterInfo[] signature,
                                       String type,
                                       int impact,
                                       Descriptor descriptor)
        {
                super(name, description, signature, type, impact);
                if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
                    MODELMBEAN_LOGGER.logp(Level.FINER,
                            ModelMBeanOperationInfo.class.getName(),
                            "ModelMBeanOperationInfo(String,String," +
                            "MBeanParameterInfo[],String,int,Descriptor)",
                            "Entry");
                }
                operationDescriptor = validDescriptor(descriptor);
        }
        public ModelMBeanOperationInfo(ModelMBeanOperationInfo inInfo)
        {
                super(inInfo.getName(),
                          inInfo.getDescription(),
                          inInfo.getSignature(),
                          inInfo.getReturnType(),
                          inInfo.getImpact());
                if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
                    MODELMBEAN_LOGGER.logp(Level.FINER,
                            ModelMBeanOperationInfo.class.getName(),
                            "ModelMBeanOperationInfo(ModelMBeanOperationInfo)",
                            "Entry");
                }
                Descriptor newDesc = inInfo.getDescriptor();
                operationDescriptor = validDescriptor(newDesc);
        }
        public Object clone ()
        {
            if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
                MODELMBEAN_LOGGER.logp(Level.FINER,
                        ModelMBeanOperationInfo.class.getName(),
                        "clone()", "Entry");
            }
                return(new ModelMBeanOperationInfo(this)) ;
        }
        public Descriptor getDescriptor()
        {
            if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
                MODELMBEAN_LOGGER.logp(Level.FINER,
                        ModelMBeanOperationInfo.class.getName(),
                        "getDescriptor()", "Entry");
            }
            if (operationDescriptor == null) {
                operationDescriptor = validDescriptor(null);
            }
            return((Descriptor) operationDescriptor.clone());
        }
        public void setDescriptor(Descriptor inDescriptor)
        {
            if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
                MODELMBEAN_LOGGER.logp(Level.FINER,
                        ModelMBeanOperationInfo.class.getName(),
                        "setDescriptor(Descriptor)", "Entry");
            }
            operationDescriptor = validDescriptor(inDescriptor);
        }
        public String toString()
        {
            if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
                MODELMBEAN_LOGGER.logp(Level.FINER,
                        ModelMBeanOperationInfo.class.getName(),
                        "toString()", "Entry");
            }
                String retStr =
                    "ModelMBeanOperationInfo: " + this.getName() +
                    " ; Description: " + this.getDescription() +
                    " ; Descriptor: " + this.getDescriptor() +
                    " ; ReturnType: " + this.getReturnType() +
                    " ; Signature: ";
                MBeanParameterInfo[] pTypes = this.getSignature();
                for (int i=0; i < pTypes.length; i++)
                {
                        retStr = retStr.concat((pTypes[i]).getType() + ", ");
                }
                return retStr;
        }
        private Descriptor validDescriptor(final Descriptor in)
        throws RuntimeOperationsException {
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
                clone.setField("role","operation");
                MODELMBEAN_LOGGER.finer("Defaulting Descriptor role field to \"operation\"");
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
            final String role = (String)clone.getFieldValue("role");
            if (!(role.equalsIgnoreCase("operation") ||
                  role.equalsIgnoreCase("setter") ||
                  role.equalsIgnoreCase("getter"))) {
                     throw new RuntimeOperationsException(new IllegalArgumentException("Invalid Descriptor argument"),
                    "The Descriptor \"role\" field does not match the object described. " +
                     " Expected: \"operation\", \"setter\", or \"getter\" ," + " was: " + clone.getFieldValue("role"));
            }
            final Object targetValue = clone.getFieldValue("targetType");
            if (targetValue != null) {
                if (!(targetValue instanceof java.lang.String)) {
                    throw new RuntimeOperationsException(new IllegalArgumentException("Invalid Descriptor argument"),
                    "The Descriptor field \"targetValue\" is invalid class. " +
                     " Expected: java.lang.String, " + " was: " + targetValue.getClass().getName());
                }
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
        fields.put("operationDescriptor", operationDescriptor);
        fields.put("currClass", currClass);
        out.writeFields();
      }
      else
      {
        out.defaultWriteObject();
      }
    }
}
