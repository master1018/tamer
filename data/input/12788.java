public class ModelMBeanInfoSupport extends MBeanInfo implements ModelMBeanInfo {
    private static final long oldSerialVersionUID = -3944083498453227709L;
    private static final long newSerialVersionUID = -1935722590756516193L;
    private static final ObjectStreamField[] oldSerialPersistentFields =
    {
        new ObjectStreamField("modelMBeanDescriptor", Descriptor.class),
                new ObjectStreamField("mmbAttributes", MBeanAttributeInfo[].class),
                new ObjectStreamField("mmbConstructors", MBeanConstructorInfo[].class),
                new ObjectStreamField("mmbNotifications", MBeanNotificationInfo[].class),
                new ObjectStreamField("mmbOperations", MBeanOperationInfo[].class),
                new ObjectStreamField("currClass", String.class)
    };
    private static final ObjectStreamField[] newSerialPersistentFields =
    {
        new ObjectStreamField("modelMBeanDescriptor", Descriptor.class),
                new ObjectStreamField("modelMBeanAttributes", MBeanAttributeInfo[].class),
                new ObjectStreamField("modelMBeanConstructors", MBeanConstructorInfo[].class),
                new ObjectStreamField("modelMBeanNotifications", MBeanNotificationInfo[].class),
                new ObjectStreamField("modelMBeanOperations", MBeanOperationInfo[].class)
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
    private Descriptor modelMBeanDescriptor = null;
    private MBeanAttributeInfo[] modelMBeanAttributes;
    private MBeanConstructorInfo[] modelMBeanConstructors;
    private MBeanNotificationInfo[] modelMBeanNotifications;
    private MBeanOperationInfo[] modelMBeanOperations;
    private static final String ATTR = "attribute";
    private static final String OPER = "operation";
    private static final String NOTF = "notification";
    private static final String CONS = "constructor";
    private static final String MMB = "mbean";
    private static final String ALL = "all";
    private static final String currClass = "ModelMBeanInfoSupport";
    public ModelMBeanInfoSupport(ModelMBeanInfo  mbi) {
        super(mbi.getClassName(),
                mbi.getDescription(),
                mbi.getAttributes(),
                mbi.getConstructors(),
                mbi.getOperations(),
                mbi.getNotifications());
        modelMBeanAttributes = mbi.getAttributes();
        modelMBeanConstructors = mbi.getConstructors();
        modelMBeanOperations = mbi.getOperations();
        modelMBeanNotifications = mbi.getNotifications();
        try {
            Descriptor mbeandescriptor = mbi.getMBeanDescriptor();
            modelMBeanDescriptor = validDescriptor(mbeandescriptor);
        } catch (MBeanException mbe) {
            modelMBeanDescriptor = validDescriptor(null);
            if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
                MODELMBEAN_LOGGER.logp(Level.FINER,
                        ModelMBeanInfoSupport.class.getName(),
                        "ModelMBeanInfo(ModelMBeanInfo)",
                        "Could not get a valid modelMBeanDescriptor, " +
                        "setting a default Descriptor");
            }
        }
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanInfoSupport.class.getName(),
                    "ModelMBeanInfo(ModelMBeanInfo)", "Exit");
        }
    }
    public ModelMBeanInfoSupport(String className,
            String description,
            ModelMBeanAttributeInfo[] attributes,
            ModelMBeanConstructorInfo[] constructors,
            ModelMBeanOperationInfo[] operations,
            ModelMBeanNotificationInfo[] notifications) {
        this(className, description, attributes, constructors,
                operations, notifications, null);
    }
    public ModelMBeanInfoSupport(String    className,
            String description,
            ModelMBeanAttributeInfo[] attributes,
            ModelMBeanConstructorInfo[] constructors,
            ModelMBeanOperationInfo[] operations,
            ModelMBeanNotificationInfo[] notifications,
            Descriptor mbeandescriptor) {
        super(className,
                description,
                (attributes != null) ? attributes : NO_ATTRIBUTES,
                (constructors != null) ? constructors : NO_CONSTRUCTORS,
                (operations != null) ? operations : NO_OPERATIONS,
                (notifications != null) ? notifications : NO_NOTIFICATIONS);
        modelMBeanAttributes = attributes;
        modelMBeanConstructors = constructors;
        modelMBeanOperations = operations;
        modelMBeanNotifications = notifications;
        modelMBeanDescriptor = validDescriptor(mbeandescriptor);
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanInfoSupport.class.getName(),
                    "ModelMBeanInfoSupport(String,String,ModelMBeanAttributeInfo[]," +
                    "ModelMBeanConstructorInfo[],ModelMBeanOperationInfo[]," +
                    "ModelMBeanNotificationInfo[],Descriptor)",
                    "Exit");
        }
    }
    private static final ModelMBeanAttributeInfo[] NO_ATTRIBUTES =
            new ModelMBeanAttributeInfo[0];
    private static final ModelMBeanConstructorInfo[] NO_CONSTRUCTORS =
            new ModelMBeanConstructorInfo[0];
    private static final ModelMBeanNotificationInfo[] NO_NOTIFICATIONS =
            new ModelMBeanNotificationInfo[0];
    private static final ModelMBeanOperationInfo[] NO_OPERATIONS =
            new ModelMBeanOperationInfo[0];
    public Object clone() {
        return(new ModelMBeanInfoSupport(this));
    }
    public Descriptor[] getDescriptors(String inDescriptorType)
    throws MBeanException, RuntimeOperationsException {
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanInfoSupport.class.getName(),
                    "getDescriptors(String)", "Entry");
        }
        if ((inDescriptorType == null) || (inDescriptorType.equals(""))) {
            inDescriptorType = "all";
        }
        final Descriptor[] retList;
        if (inDescriptorType.equalsIgnoreCase(MMB)) {
            retList = new Descriptor[] {modelMBeanDescriptor};
        } else if (inDescriptorType.equalsIgnoreCase(ATTR)) {
            final MBeanAttributeInfo[] attrList = modelMBeanAttributes;
            int numAttrs = 0;
            if (attrList != null) numAttrs = attrList.length;
            retList = new Descriptor[numAttrs];
            for (int i=0; i < numAttrs; i++) {
                retList[i] = (((ModelMBeanAttributeInfo)
                    attrList[i]).getDescriptor());
            }
        } else if (inDescriptorType.equalsIgnoreCase(OPER)) {
            final MBeanOperationInfo[] operList = modelMBeanOperations;
            int numOpers = 0;
            if (operList != null) numOpers = operList.length;
            retList = new Descriptor[numOpers];
            for (int i=0; i < numOpers; i++) {
                retList[i] = (((ModelMBeanOperationInfo)
                    operList[i]).getDescriptor());
            }
        } else if (inDescriptorType.equalsIgnoreCase(CONS)) {
            final MBeanConstructorInfo[] consList =  modelMBeanConstructors;
            int numCons = 0;
            if (consList != null) numCons = consList.length;
            retList = new Descriptor[numCons];
            for (int i=0; i < numCons; i++) {
                retList[i] = (((ModelMBeanConstructorInfo)
                    consList[i]).getDescriptor());
            }
        } else if (inDescriptorType.equalsIgnoreCase(NOTF)) {
            final MBeanNotificationInfo[] notifList = modelMBeanNotifications;
            int numNotifs = 0;
            if (notifList != null) numNotifs = notifList.length;
            retList = new Descriptor[numNotifs];
            for (int i=0; i < numNotifs; i++) {
                retList[i] = (((ModelMBeanNotificationInfo)
                    notifList[i]).getDescriptor());
            }
        } else if (inDescriptorType.equalsIgnoreCase(ALL)) {
            final MBeanAttributeInfo[] attrList = modelMBeanAttributes;
            int numAttrs = 0;
            if (attrList != null) numAttrs = attrList.length;
            final MBeanOperationInfo[] operList = modelMBeanOperations;
            int numOpers = 0;
            if (operList != null) numOpers = operList.length;
            final MBeanConstructorInfo[] consList = modelMBeanConstructors;
            int numCons = 0;
            if (consList != null) numCons = consList.length;
            final MBeanNotificationInfo[] notifList = modelMBeanNotifications;
            int numNotifs = 0;
            if (notifList != null) numNotifs = notifList.length;
            int count = numAttrs + numCons + numOpers + numNotifs + 1;
            retList = new Descriptor[count];
            retList[count-1] = modelMBeanDescriptor;
            int j=0;
            for (int i=0; i < numAttrs; i++) {
                retList[j] = (((ModelMBeanAttributeInfo)
                    attrList[i]).getDescriptor());
                j++;
            }
            for (int i=0; i < numCons; i++) {
                retList[j] = (((ModelMBeanConstructorInfo)
                    consList[i]).getDescriptor());
                j++;
            }
            for (int i=0; i < numOpers; i++) {
                retList[j] = (((ModelMBeanOperationInfo)operList[i]).
                        getDescriptor());
                j++;
            }
            for (int i=0; i < numNotifs; i++) {
                retList[j] = (((ModelMBeanNotificationInfo)notifList[i]).
                        getDescriptor());
                j++;
            }
        } else {
            final IllegalArgumentException iae =
                    new IllegalArgumentException("Descriptor Type is invalid");
            final String msg = "Exception occurred trying to find"+
                    " the descriptors of the MBean";
            throw new RuntimeOperationsException(iae,msg);
        }
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanInfoSupport.class.getName(),
                    "getDescriptors(String)", "Exit");
        }
        return retList;
    }
    public void setDescriptors(Descriptor[] inDescriptors)
    throws MBeanException, RuntimeOperationsException {
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanInfoSupport.class.getName(),
                    "setDescriptors(Descriptor[])", "Entry");
        }
        if (inDescriptors==null) {
            throw new RuntimeOperationsException(
                    new IllegalArgumentException("Descriptor list is invalid"),
                    "Exception occurred trying to set the descriptors " +
                    "of the MBeanInfo");
        }
        if (inDescriptors.length == 0) { 
            return;
        }
        for (int j=0; j < inDescriptors.length; j++) {
            setDescriptor(inDescriptors[j],null);
        }
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanInfoSupport.class.getName(),
                    "setDescriptors(Descriptor[])", "Exit");
        }
    }
    public Descriptor getDescriptor(String inDescriptorName)
    throws MBeanException, RuntimeOperationsException {
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanInfoSupport.class.getName(),
                    "getDescriptor(String)", "Entry");
        }
        return(getDescriptor(inDescriptorName, null));
    }
    public Descriptor getDescriptor(String inDescriptorName,
            String inDescriptorType)
            throws MBeanException, RuntimeOperationsException {
        if (inDescriptorName==null) {
            throw new RuntimeOperationsException(
                    new IllegalArgumentException("Descriptor is invalid"),
                    "Exception occurred trying to set the descriptors of " +
                    "the MBeanInfo");
        }
        if (MMB.equalsIgnoreCase(inDescriptorType)) {
            return (Descriptor) modelMBeanDescriptor.clone();
        }
        if (ATTR.equalsIgnoreCase(inDescriptorType) || inDescriptorType == null) {
            ModelMBeanAttributeInfo attr = getAttribute(inDescriptorName);
            if (attr != null)
                return attr.getDescriptor();
            if (inDescriptorType != null)
                return null;
        }
        if (OPER.equalsIgnoreCase(inDescriptorType) || inDescriptorType == null) {
            ModelMBeanOperationInfo oper = getOperation(inDescriptorName);
            if (oper != null)
                return oper.getDescriptor();
            if (inDescriptorType != null)
                return null;
        }
        if (CONS.equalsIgnoreCase(inDescriptorType) || inDescriptorType == null) {
            ModelMBeanConstructorInfo oper =
                    getConstructor(inDescriptorName);
            if (oper != null)
                return oper.getDescriptor();
            if (inDescriptorType != null)
                return null;
        }
        if (NOTF.equalsIgnoreCase(inDescriptorType) || inDescriptorType == null) {
            ModelMBeanNotificationInfo notif =
                    getNotification(inDescriptorName);
            if (notif != null)
                return notif.getDescriptor();
            if (inDescriptorType != null)
                return null;
        }
        if (inDescriptorType == null)
            return null;
        throw new RuntimeOperationsException(
                new IllegalArgumentException("Descriptor Type is invalid"),
                "Exception occurred trying to find the descriptors of the MBean");
    }
    public void setDescriptor(Descriptor inDescriptor,
            String inDescriptorType)
            throws MBeanException, RuntimeOperationsException {
        final String excMsg =
                "Exception occurred trying to set the descriptors of the MBean";
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanInfoSupport.class.getName(),
                    "setDescriptor(Descriptor,String)", "Entry");
        }
        if (inDescriptor==null) {
            inDescriptor = new DescriptorSupport();
        }
        if ((inDescriptorType == null) || (inDescriptorType.equals(""))) {
            inDescriptorType =
                    (String) inDescriptor.getFieldValue("descriptorType");
            if (inDescriptorType == null) {
                   MODELMBEAN_LOGGER.logp(Level.FINER,
                                ModelMBeanInfoSupport.class.getName(),
                                "setDescriptor(Descriptor,String)",
                                "descriptorType null in both String parameter and Descriptor, defaulting to "+ MMB);
                inDescriptorType = MMB;
            }
        }
        String inDescriptorName =
                (String) inDescriptor.getFieldValue("name");
        if (inDescriptorName == null) {
            MODELMBEAN_LOGGER.logp(Level.FINER,
                                ModelMBeanInfoSupport.class.getName(),
                                "setDescriptor(Descriptor,String)",
                                "descriptor name null, defaulting to "+ this.getClassName());
            inDescriptorName = this.getClassName();
        }
        boolean found = false;
        if (inDescriptorType.equalsIgnoreCase(MMB)) {
            setMBeanDescriptor(inDescriptor);
            found = true;
        } else if (inDescriptorType.equalsIgnoreCase(ATTR)) {
            MBeanAttributeInfo[] attrList =  modelMBeanAttributes;
            int numAttrs = 0;
            if (attrList != null) numAttrs = attrList.length;
            for (int i=0; i < numAttrs; i++) {
                if (inDescriptorName.equals(attrList[i].getName())) {
                    found = true;
                    ModelMBeanAttributeInfo mmbai =
                            (ModelMBeanAttributeInfo) attrList[i];
                    mmbai.setDescriptor(inDescriptor);
                    if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
                        StringBuilder strb = new StringBuilder()
                        .append("Setting descriptor to ").append(inDescriptor)
                        .append("\t\n local: AttributeInfo descriptor is ")
                        .append(mmbai.getDescriptor())
                        .append("\t\n modelMBeanInfo: AttributeInfo descriptor is ")
                        .append(this.getDescriptor(inDescriptorName,"attribute"));
                        MODELMBEAN_LOGGER.logp(Level.FINER,
                                ModelMBeanInfoSupport.class.getName(),
                                "setDescriptor(Descriptor,String)",
                                strb.toString());
                    }
                }
            }
        } else if (inDescriptorType.equalsIgnoreCase(OPER)) {
            MBeanOperationInfo[] operList =  modelMBeanOperations;
            int numOpers = 0;
            if (operList != null) numOpers = operList.length;
            for (int i=0; i < numOpers; i++) {
                if (inDescriptorName.equals(operList[i].getName())) {
                    found = true;
                    ModelMBeanOperationInfo mmboi =
                            (ModelMBeanOperationInfo) operList[i];
                    mmboi.setDescriptor(inDescriptor);
                }
            }
        } else if (inDescriptorType.equalsIgnoreCase(CONS)) {
            MBeanConstructorInfo[] consList =  modelMBeanConstructors;
            int numCons = 0;
            if (consList != null) numCons = consList.length;
            for (int i=0; i < numCons; i++) {
                if (inDescriptorName.equals(consList[i].getName())) {
                    found = true;
                    ModelMBeanConstructorInfo mmbci =
                            (ModelMBeanConstructorInfo) consList[i];
                    mmbci.setDescriptor(inDescriptor);
                }
            }
        } else if (inDescriptorType.equalsIgnoreCase(NOTF)) {
            MBeanNotificationInfo[] notifList =  modelMBeanNotifications;
            int numNotifs = 0;
            if (notifList != null) numNotifs = notifList.length;
            for (int i=0; i < numNotifs; i++) {
                if (inDescriptorName.equals(notifList[i].getName())) {
                    found = true;
                    ModelMBeanNotificationInfo mmbni =
                            (ModelMBeanNotificationInfo) notifList[i];
                    mmbni.setDescriptor(inDescriptor);
                }
            }
        } else {
            RuntimeException iae =
                    new IllegalArgumentException("Invalid descriptor type: " +
                    inDescriptorType);
            throw new RuntimeOperationsException(iae, excMsg);
        }
        if (!found) {
            RuntimeException iae =
                    new IllegalArgumentException("Descriptor name is invalid: " +
                    "type=" + inDescriptorType +
                    "; name=" + inDescriptorName);
            throw new RuntimeOperationsException(iae, excMsg);
        }
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanInfoSupport.class.getName(),
                    "setDescriptor(Descriptor,String)", "Exit");
        }
    }
    public ModelMBeanAttributeInfo getAttribute(String inName)
    throws MBeanException, RuntimeOperationsException {
        ModelMBeanAttributeInfo retInfo = null;
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanInfoSupport.class.getName(),
                    "getAttribute(String)", "Entry");
        }
        if (inName == null) {
            throw new RuntimeOperationsException(
                    new IllegalArgumentException("Attribute Name is null"),
                    "Exception occurred trying to get the " +
                    "ModelMBeanAttributeInfo of the MBean");
        }
        MBeanAttributeInfo[] attrList = modelMBeanAttributes;
        int numAttrs = 0;
        if (attrList != null) numAttrs = attrList.length;
        for (int i=0; (i < numAttrs) && (retInfo == null); i++) {
            if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
                final StringBuilder strb = new StringBuilder()
                .append("\t\n this.getAttributes() MBeanAttributeInfo Array ")
                .append(i).append(":")
                .append(((ModelMBeanAttributeInfo)attrList[i]).getDescriptor())
                .append("\t\n this.modelMBeanAttributes MBeanAttributeInfo Array ")
                .append(i).append(":")
                .append(((ModelMBeanAttributeInfo)modelMBeanAttributes[i]).getDescriptor());
                MODELMBEAN_LOGGER.logp(Level.FINER,
                        ModelMBeanInfoSupport.class.getName(),
                        "getAttribute(String)", strb.toString());
            }
            if (inName.equals(attrList[i].getName())) {
                retInfo = ((ModelMBeanAttributeInfo)attrList[i].clone());
            }
        }
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanInfoSupport.class.getName(),
                    "getAttribute(String)", "Exit");
        }
        return retInfo;
    }
    public ModelMBeanOperationInfo getOperation(String inName)
    throws MBeanException, RuntimeOperationsException {
        ModelMBeanOperationInfo retInfo = null;
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanInfoSupport.class.getName(),
                    "getOperation(String)", "Entry");
        }
        if (inName == null) {
            throw new RuntimeOperationsException(
                    new IllegalArgumentException("inName is null"),
                    "Exception occurred trying to get the " +
                    "ModelMBeanOperationInfo of the MBean");
        }
        MBeanOperationInfo[] operList = modelMBeanOperations; 
        int numOpers = 0;
        if (operList != null) numOpers = operList.length;
        for (int i=0; (i < numOpers) && (retInfo == null); i++) {
            if (inName.equals(operList[i].getName())) {
                retInfo = ((ModelMBeanOperationInfo) operList[i].clone());
            }
        }
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanInfoSupport.class.getName(),
                    "getOperation(String)", "Exit");
        }
        return retInfo;
    }
    public ModelMBeanConstructorInfo getConstructor(String inName)
    throws MBeanException, RuntimeOperationsException {
        ModelMBeanConstructorInfo retInfo = null;
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanInfoSupport.class.getName(),
                    "getConstructor(String)", "Entry");
        }
        if (inName == null) {
            throw new RuntimeOperationsException(
                    new IllegalArgumentException("Constructor name is null"),
                    "Exception occurred trying to get the " +
                    "ModelMBeanConstructorInfo of the MBean");
        }
        MBeanConstructorInfo[] consList = modelMBeanConstructors; 
        int numCons = 0;
        if (consList != null) numCons = consList.length;
        for (int i=0; (i < numCons) && (retInfo == null); i++) {
            if (inName.equals(consList[i].getName())) {
                retInfo = ((ModelMBeanConstructorInfo) consList[i].clone());
            }
        }
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanInfoSupport.class.getName(),
                    "getConstructor(String)", "Exit");
        }
        return retInfo;
    }
    public ModelMBeanNotificationInfo getNotification(String inName)
    throws MBeanException, RuntimeOperationsException {
        ModelMBeanNotificationInfo retInfo = null;
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanInfoSupport.class.getName(),
                    "getNotification(String)", "Entry");
        }
        if (inName == null) {
            throw new RuntimeOperationsException(
                    new IllegalArgumentException("Notification name is null"),
                    "Exception occurred trying to get the " +
                    "ModelMBeanNotificationInfo of the MBean");
        }
        MBeanNotificationInfo[] notifList = modelMBeanNotifications; 
        int numNotifs = 0;
        if (notifList != null) numNotifs = notifList.length;
        for (int i=0; (i < numNotifs) && (retInfo == null); i++) {
            if (inName.equals(notifList[i].getName())) {
                retInfo = ((ModelMBeanNotificationInfo) notifList[i].clone());
            }
        }
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanInfoSupport.class.getName(),
                    "getNotification(String)", "Exit");
        }
        return retInfo;
    }
    @Override
    public Descriptor getDescriptor() {
        return getMBeanDescriptorNoException();
    }
    public Descriptor getMBeanDescriptor() throws MBeanException {
        return getMBeanDescriptorNoException();
    }
    private Descriptor getMBeanDescriptorNoException() {
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanInfoSupport.class.getName(),
                    "getMBeanDescriptorNoException()", "Entry");
        }
        if (modelMBeanDescriptor == null)
            modelMBeanDescriptor = validDescriptor(null);
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanInfoSupport.class.getName(),
                    "getMBeanDescriptorNoException()",
                    "Exit, returning: " + modelMBeanDescriptor);
        }
        return (Descriptor) modelMBeanDescriptor.clone();
    }
    public void setMBeanDescriptor(Descriptor inMBeanDescriptor)
    throws MBeanException, RuntimeOperationsException {
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanInfoSupport.class.getName(),
                    "setMBeanDescriptor(Descriptor)", "Entry");
        }
        modelMBeanDescriptor = validDescriptor(inMBeanDescriptor);
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
            clone.setField("name", this.getClassName());
            MODELMBEAN_LOGGER.finer("Defaulting Descriptor name to " + this.getClassName());
        }
        if (defaulted && clone.getFieldValue("descriptorType")==null) {
            clone.setField("descriptorType", MMB);
            MODELMBEAN_LOGGER.finer("Defaulting descriptorType to \"" + MMB + "\"");
        }
        if (clone.getFieldValue("displayName") == null) {
            clone.setField("displayName",this.getClassName());
            MODELMBEAN_LOGGER.finer("Defaulting Descriptor displayName to " + this.getClassName());
        }
        if (clone.getFieldValue("persistPolicy") == null) {
            clone.setField("persistPolicy","never");
            MODELMBEAN_LOGGER.finer("Defaulting Descriptor persistPolicy to \"never\"");
        }
        if (clone.getFieldValue("log") == null) {
            clone.setField("log","F");
            MODELMBEAN_LOGGER.finer("Defaulting Descriptor \"log\" field to \"F\"");
        }
        if (clone.getFieldValue("visibility") == null) {
            clone.setField("visibility","1");
            MODELMBEAN_LOGGER.finer("Defaulting Descriptor visibility to 1");
        }
        if (!clone.isValid()) {
             throw new RuntimeOperationsException(new IllegalArgumentException("Invalid Descriptor argument"),
                "The isValid() method of the Descriptor object itself returned false,"+
                "one or more required fields are invalid. Descriptor:" + clone.toString());
        }
        if (! ((String)clone.getFieldValue("descriptorType")).equalsIgnoreCase(MMB)) {
                 throw new RuntimeOperationsException(new IllegalArgumentException("Invalid Descriptor argument"),
                "The Descriptor \"descriptorType\" field does not match the object described. " +
                 " Expected: "+ MMB + " , was: " + clone.getFieldValue("descriptorType"));
        }
        return clone;
    }
    private void readObject(ObjectInputStream in)
    throws IOException, ClassNotFoundException {
        if (compat) {
            ObjectInputStream.GetField fields = in.readFields();
            modelMBeanDescriptor =
                    (Descriptor) fields.get("modelMBeanDescriptor", null);
            if (fields.defaulted("modelMBeanDescriptor")) {
                throw new NullPointerException("modelMBeanDescriptor");
            }
            modelMBeanAttributes =
                    (MBeanAttributeInfo[]) fields.get("mmbAttributes", null);
            if (fields.defaulted("mmbAttributes")) {
                throw new NullPointerException("mmbAttributes");
            }
            modelMBeanConstructors =
                    (MBeanConstructorInfo[]) fields.get("mmbConstructors", null);
            if (fields.defaulted("mmbConstructors")) {
                throw new NullPointerException("mmbConstructors");
            }
            modelMBeanNotifications =
                    (MBeanNotificationInfo[]) fields.get("mmbNotifications", null);
            if (fields.defaulted("mmbNotifications")) {
                throw new NullPointerException("mmbNotifications");
            }
            modelMBeanOperations =
                    (MBeanOperationInfo[]) fields.get("mmbOperations", null);
            if (fields.defaulted("mmbOperations")) {
                throw new NullPointerException("mmbOperations");
            }
        } else {
            in.defaultReadObject();
        }
    }
    private void writeObject(ObjectOutputStream out)
    throws IOException {
        if (compat) {
            ObjectOutputStream.PutField fields = out.putFields();
            fields.put("modelMBeanDescriptor", modelMBeanDescriptor);
            fields.put("mmbAttributes", modelMBeanAttributes);
            fields.put("mmbConstructors", modelMBeanConstructors);
            fields.put("mmbNotifications", modelMBeanNotifications);
            fields.put("mmbOperations", modelMBeanOperations);
            fields.put("currClass", currClass);
            out.writeFields();
        } else {
            out.defaultWriteObject();
        }
    }
}
