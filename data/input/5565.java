public class RelationTypeSupport implements RelationType {
    private static final long oldSerialVersionUID = -8179019472410837190L;
    private static final long newSerialVersionUID = 4611072955724144607L;
    private static final ObjectStreamField[] oldSerialPersistentFields =
    {
      new ObjectStreamField("myTypeName", String.class),
      new ObjectStreamField("myRoleName2InfoMap", HashMap.class),
      new ObjectStreamField("myIsInRelServFlg", boolean.class)
    };
    private static final ObjectStreamField[] newSerialPersistentFields =
    {
      new ObjectStreamField("typeName", String.class),
      new ObjectStreamField("roleName2InfoMap", Map.class),
      new ObjectStreamField("isInRelationService", boolean.class)
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
    private String typeName = null;
    private Map<String,RoleInfo> roleName2InfoMap =
        new HashMap<String,RoleInfo>();
    private boolean isInRelationService = false;
    public RelationTypeSupport(String relationTypeName,
                            RoleInfo[] roleInfoArray)
        throws IllegalArgumentException,
               InvalidRelationTypeException {
        if (relationTypeName == null || roleInfoArray == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationTypeSupport.class.getName(),
                "RelationTypeSupport", relationTypeName);
        initMembers(relationTypeName, roleInfoArray);
        RELATION_LOGGER.exiting(RelationTypeSupport.class.getName(),
                "RelationTypeSupport");
        return;
    }
    protected RelationTypeSupport(String relationTypeName)
    {
        if (relationTypeName == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationTypeSupport.class.getName(),
                "RelationTypeSupport", relationTypeName);
        typeName = relationTypeName;
        RELATION_LOGGER.exiting(RelationTypeSupport.class.getName(),
                "RelationTypeSupport");
        return;
    }
    public String getRelationTypeName() {
        return typeName;
    }
    public List<RoleInfo> getRoleInfos() {
        return new ArrayList<RoleInfo>(roleName2InfoMap.values());
    }
    public RoleInfo getRoleInfo(String roleInfoName)
        throws IllegalArgumentException,
               RoleInfoNotFoundException {
        if (roleInfoName == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationTypeSupport.class.getName(),
                "getRoleInfo", roleInfoName);
        RoleInfo result = roleName2InfoMap.get(roleInfoName);
        if (result == null) {
            StringBuilder excMsgStrB = new StringBuilder();
            String excMsg = "No role info for role ";
            excMsgStrB.append(excMsg);
            excMsgStrB.append(roleInfoName);
            throw new RoleInfoNotFoundException(excMsgStrB.toString());
        }
        RELATION_LOGGER.exiting(RelationTypeSupport.class.getName(),
                "getRoleInfo");
        return result;
    }
    protected void addRoleInfo(RoleInfo roleInfo)
        throws IllegalArgumentException,
               InvalidRelationTypeException {
        if (roleInfo == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationTypeSupport.class.getName(),
                "addRoleInfo", roleInfo);
        if (isInRelationService) {
            String excMsg = "Relation type cannot be updated as it is declared in the Relation Service.";
            throw new RuntimeException(excMsg);
        }
        String roleName = roleInfo.getName();
        if (roleName2InfoMap.containsKey(roleName)) {
            StringBuilder excMsgStrB = new StringBuilder();
            String excMsg = "Two role infos provided for role ";
            excMsgStrB.append(excMsg);
            excMsgStrB.append(roleName);
            throw new InvalidRelationTypeException(excMsgStrB.toString());
        }
        roleName2InfoMap.put(roleName, new RoleInfo(roleInfo));
        RELATION_LOGGER.exiting(RelationTypeSupport.class.getName(),
                "addRoleInfo");
        return;
    }
    void setRelationServiceFlag(boolean flag) {
        isInRelationService = flag;
        return;
    }
    private void initMembers(String relationTypeName,
                             RoleInfo[] roleInfoArray)
        throws IllegalArgumentException,
               InvalidRelationTypeException {
        if (relationTypeName == null || roleInfoArray == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationTypeSupport.class.getName(),
                "initMembers", relationTypeName);
        typeName = relationTypeName;
        checkRoleInfos(roleInfoArray);
        for (int i = 0; i < roleInfoArray.length; i++) {
            RoleInfo currRoleInfo = roleInfoArray[i];
            roleName2InfoMap.put(currRoleInfo.getName(),
                                 new RoleInfo(currRoleInfo));
        }
        RELATION_LOGGER.exiting(RelationTypeSupport.class.getName(),
                "initMembers");
        return;
    }
    static void checkRoleInfos(RoleInfo[] roleInfoArray)
        throws IllegalArgumentException,
               InvalidRelationTypeException {
        if (roleInfoArray == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        if (roleInfoArray.length == 0) {
            String excMsg = "No role info provided.";
            throw new InvalidRelationTypeException(excMsg);
        }
        Set<String> roleNames = new HashSet<String>();
        for (int i = 0; i < roleInfoArray.length; i++) {
            RoleInfo currRoleInfo = roleInfoArray[i];
            if (currRoleInfo == null) {
                String excMsg = "Null role info provided.";
                throw new InvalidRelationTypeException(excMsg);
            }
            String roleName = currRoleInfo.getName();
            if (roleNames.contains(roleName)) {
                StringBuilder excMsgStrB = new StringBuilder();
                String excMsg = "Two role infos provided for role ";
                excMsgStrB.append(excMsg);
                excMsgStrB.append(roleName);
                throw new InvalidRelationTypeException(excMsgStrB.toString());
            }
            roleNames.add(roleName);
        }
        return;
    }
    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
      if (compat)
      {
        ObjectInputStream.GetField fields = in.readFields();
        typeName = (String) fields.get("myTypeName", null);
        if (fields.defaulted("myTypeName"))
        {
          throw new NullPointerException("myTypeName");
        }
        roleName2InfoMap = cast(fields.get("myRoleName2InfoMap", null));
        if (fields.defaulted("myRoleName2InfoMap"))
        {
          throw new NullPointerException("myRoleName2InfoMap");
        }
        isInRelationService = fields.get("myIsInRelServFlg", false);
        if (fields.defaulted("myIsInRelServFlg"))
        {
          throw new NullPointerException("myIsInRelServFlg");
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
        fields.put("myTypeName", typeName);
        fields.put("myRoleName2InfoMap", roleName2InfoMap);
        fields.put("myIsInRelServFlg", isInRelationService);
        out.writeFields();
      }
      else
      {
        out.defaultWriteObject();
      }
    }
}
