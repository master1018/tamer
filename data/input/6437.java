public class RoleUnresolved implements Serializable {
    private static final long oldSerialVersionUID = -9026457686611660144L;
    private static final long newSerialVersionUID = -48350262537070138L;
    private static final ObjectStreamField[] oldSerialPersistentFields =
    {
      new ObjectStreamField("myRoleName", String.class),
      new ObjectStreamField("myRoleValue", ArrayList.class),
      new ObjectStreamField("myPbType", int.class)
    };
    private static final ObjectStreamField[] newSerialPersistentFields =
    {
      new ObjectStreamField("roleName", String.class),
      new ObjectStreamField("roleValue", List.class),
      new ObjectStreamField("problemType", int.class)
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
    private String roleName = null;
    private List<ObjectName> roleValue = null;
    private int problemType;
    public RoleUnresolved(String name,
                          List<ObjectName> value,
                          int pbType)
        throws IllegalArgumentException {
        if (name == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        setRoleName(name);
        setRoleValue(value);
        setProblemType(pbType);
        return;
    }
    public String getRoleName() {
        return roleName;
    }
    public List<ObjectName> getRoleValue() {
        return roleValue;
    }
    public int getProblemType() {
        return problemType;
    }
    public void setRoleName(String name)
        throws IllegalArgumentException {
        if (name == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        roleName = name;
        return;
    }
    public void setRoleValue(List<ObjectName> value) {
        if (value != null) {
            roleValue = new ArrayList<ObjectName>(value);
        } else {
            roleValue = null;
        }
        return;
    }
    public void setProblemType(int pbType)
        throws IllegalArgumentException {
        if (!(RoleStatus.isRoleStatus(pbType))) {
            String excMsg = "Incorrect problem type.";
            throw new IllegalArgumentException(excMsg);
        }
        problemType = pbType;
        return;
    }
    public Object clone() {
        try {
            return new RoleUnresolved(roleName, roleValue, problemType);
        } catch (IllegalArgumentException exc) {
            return null; 
        }
    }
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("role name: " + roleName);
        if (roleValue != null) {
            result.append("; value: ");
            for (Iterator<ObjectName> objNameIter = roleValue.iterator();
                 objNameIter.hasNext();) {
                ObjectName currObjName = objNameIter.next();
                result.append(currObjName.toString());
                if (objNameIter.hasNext()) {
                    result.append(", ");
                }
            }
        }
        result.append("; problem type: " + problemType);
        return result.toString();
    }
    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
      if (compat)
      {
        ObjectInputStream.GetField fields = in.readFields();
        roleName = (String) fields.get("myRoleName", null);
        if (fields.defaulted("myRoleName"))
        {
          throw new NullPointerException("myRoleName");
        }
        roleValue = cast(fields.get("myRoleValue", null));
        if (fields.defaulted("myRoleValue"))
        {
          throw new NullPointerException("myRoleValue");
        }
        problemType = fields.get("myPbType", 0);
        if (fields.defaulted("myPbType"))
        {
          throw new NullPointerException("myPbType");
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
        fields.put("myRoleName", roleName);
        fields.put("myRoleValue", roleValue);
        fields.put("myPbType", problemType);
        out.writeFields();
      }
      else
      {
        out.defaultWriteObject();
      }
    }
}
