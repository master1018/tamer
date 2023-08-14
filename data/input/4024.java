public class RelationNotification extends Notification {
    private static final long oldSerialVersionUID = -2126464566505527147L;
    private static final long newSerialVersionUID = -6871117877523310399L;
    private static final ObjectStreamField[] oldSerialPersistentFields =
    {
        new ObjectStreamField("myNewRoleValue", ArrayList.class),
        new ObjectStreamField("myOldRoleValue", ArrayList.class),
        new ObjectStreamField("myRelId", String.class),
        new ObjectStreamField("myRelObjName", ObjectName.class),
        new ObjectStreamField("myRelTypeName", String.class),
        new ObjectStreamField("myRoleName", String.class),
        new ObjectStreamField("myUnregMBeanList", ArrayList.class)
    };
    private static final ObjectStreamField[] newSerialPersistentFields =
    {
        new ObjectStreamField("newRoleValue", List.class),
        new ObjectStreamField("oldRoleValue", List.class),
        new ObjectStreamField("relationId", String.class),
        new ObjectStreamField("relationObjName", ObjectName.class),
        new ObjectStreamField("relationTypeName", String.class),
        new ObjectStreamField("roleName", String.class),
        new ObjectStreamField("unregisterMBeanList", List.class)
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
    public static final String RELATION_BASIC_CREATION = "jmx.relation.creation.basic";
    public static final String RELATION_MBEAN_CREATION = "jmx.relation.creation.mbean";
    public static final String RELATION_BASIC_UPDATE = "jmx.relation.update.basic";
    public static final String RELATION_MBEAN_UPDATE = "jmx.relation.update.mbean";
    public static final String RELATION_BASIC_REMOVAL = "jmx.relation.removal.basic";
    public static final String RELATION_MBEAN_REMOVAL = "jmx.relation.removal.mbean";
    private String relationId = null;
    private String relationTypeName = null;
    private ObjectName relationObjName = null;
    private List<ObjectName> unregisterMBeanList = null;
    private String roleName = null;
    private List<ObjectName> oldRoleValue = null;
    private List<ObjectName> newRoleValue = null;
    public RelationNotification(String notifType,
                                Object sourceObj,
                                long sequence,
                                long timeStamp,
                                String message,
                                String id,
                                String typeName,
                                ObjectName objectName,
                                List<ObjectName> unregMBeanList)
        throws IllegalArgumentException {
        super(notifType, sourceObj, sequence, timeStamp, message);
        initMembers(1,
                    notifType,
                    sourceObj,
                    sequence,
                    timeStamp,
                    message,
                    id,
                    typeName,
                    objectName,
                    unregMBeanList,
                    null,
                    null,
                    null);
        return;
    }
    public RelationNotification(String notifType,
                                Object sourceObj,
                                long sequence,
                                long timeStamp,
                                String message,
                                String id,
                                String typeName,
                                ObjectName objectName,
                                String name,
                                List<ObjectName> newValue,
                                List<ObjectName> oldValue
                                )
            throws IllegalArgumentException {
        super(notifType, sourceObj, sequence, timeStamp, message);
        initMembers(2,
                    notifType,
                    sourceObj,
                    sequence,
                    timeStamp,
                    message,
                    id,
                    typeName,
                    objectName,
                    null,
                    name,
                    newValue,
                    oldValue);
        return;
    }
    public String getRelationId() {
        return relationId;
    }
    public String getRelationTypeName() {
        return relationTypeName;
    }
    public ObjectName getObjectName() {
        return relationObjName;
    }
    public List<ObjectName> getMBeansToUnregister() {
        List<ObjectName> result;
        if (unregisterMBeanList != null) {
            result = new ArrayList<ObjectName>(unregisterMBeanList);
        } else {
            result = Collections.emptyList();
        }
        return result;
    }
    public String getRoleName() {
        String result = null;
        if (roleName != null) {
            result = roleName;
        }
        return result;
    }
    public List<ObjectName> getOldRoleValue() {
        List<ObjectName> result;
        if (oldRoleValue != null) {
            result = new ArrayList<ObjectName>(oldRoleValue);
        } else {
            result = Collections.emptyList();
        }
        return result;
    }
    public List<ObjectName> getNewRoleValue() {
        List<ObjectName> result;
        if (newRoleValue != null) {
            result = new ArrayList<ObjectName>(newRoleValue);
        } else {
            result = Collections.emptyList();
        }
        return result;
    }
    private void initMembers(int notifKind,
                             String notifType,
                             Object sourceObj,
                             long sequence,
                             long timeStamp,
                             String message,
                             String id,
                             String typeName,
                             ObjectName objectName,
                             List<ObjectName> unregMBeanList,
                             String name,
                             List<ObjectName> newValue,
                             List<ObjectName> oldValue)
            throws IllegalArgumentException {
        boolean badInitFlg = false;
        if (notifType == null ||
            sourceObj == null ||
            (!(sourceObj instanceof RelationService) &&
             !(sourceObj instanceof ObjectName)) ||
            id == null ||
            typeName == null) {
            badInitFlg = true;
        }
        if (notifKind == 1) {
            if ((!(notifType.equals(RelationNotification.RELATION_BASIC_CREATION)))
                &&
                (!(notifType.equals(RelationNotification.RELATION_MBEAN_CREATION)))
                &&
                (!(notifType.equals(RelationNotification.RELATION_BASIC_REMOVAL)))
                &&
                (!(notifType.equals(RelationNotification.RELATION_MBEAN_REMOVAL)))
                ) {
                badInitFlg = true;
            }
        } else if (notifKind == 2) {
            if (((!(notifType.equals(RelationNotification.RELATION_BASIC_UPDATE)))
                 &&
                 (!(notifType.equals(RelationNotification.RELATION_MBEAN_UPDATE))))
                || name == null ||
                oldValue == null ||
                newValue == null) {
                badInitFlg = true;
            }
        }
        if (badInitFlg) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        relationId = id;
        relationTypeName = typeName;
        relationObjName = objectName;
        if (unregMBeanList != null) {
            unregisterMBeanList = new ArrayList<ObjectName>(unregMBeanList);
        }
        if (name != null) {
            roleName = name;
        }
        if (oldValue != null) {
            oldRoleValue = new ArrayList<ObjectName>(oldValue);
        }
        if (newValue != null) {
            newRoleValue = new ArrayList<ObjectName>(newValue);
        }
        return;
    }
    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
      if (compat)
      {
        ObjectInputStream.GetField fields = in.readFields();
        newRoleValue = cast(fields.get("myNewRoleValue", null));
        if (fields.defaulted("myNewRoleValue"))
        {
          throw new NullPointerException("newRoleValue");
        }
        oldRoleValue = cast(fields.get("myOldRoleValue", null));
        if (fields.defaulted("myOldRoleValue"))
        {
          throw new NullPointerException("oldRoleValue");
        }
        relationId = (String) fields.get("myRelId", null);
        if (fields.defaulted("myRelId"))
        {
          throw new NullPointerException("relationId");
        }
        relationObjName = (ObjectName) fields.get("myRelObjName", null);
        if (fields.defaulted("myRelObjName"))
        {
          throw new NullPointerException("relationObjName");
        }
        relationTypeName = (String) fields.get("myRelTypeName", null);
        if (fields.defaulted("myRelTypeName"))
        {
          throw new NullPointerException("relationTypeName");
        }
        roleName = (String) fields.get("myRoleName", null);
        if (fields.defaulted("myRoleName"))
        {
          throw new NullPointerException("roleName");
        }
        unregisterMBeanList = cast(fields.get("myUnregMBeanList", null));
        if (fields.defaulted("myUnregMBeanList"))
        {
          throw new NullPointerException("unregisterMBeanList");
        }
      }
      else
      {
        in.defaultReadObject();
      }
    }
    private void writeObject(ObjectOutputStream out)
            throws IOException {
      if (compat)
      {
        ObjectOutputStream.PutField fields = out.putFields();
        fields.put("myNewRoleValue", newRoleValue);
        fields.put("myOldRoleValue", oldRoleValue);
        fields.put("myRelId", relationId);
        fields.put("myRelObjName", relationObjName);
        fields.put("myRelTypeName", relationTypeName);
        fields.put("myRoleName",roleName);
        fields.put("myUnregMBeanList", unregisterMBeanList);
        out.writeFields();
      }
      else
      {
        out.defaultWriteObject();
      }
    }
}
