public class Role implements Serializable {
    private static final long oldSerialVersionUID = -1959486389343113026L;
    private static final long newSerialVersionUID = -279985518429862552L;
    private static final ObjectStreamField[] oldSerialPersistentFields =
    {
      new ObjectStreamField("myName", String.class),
      new ObjectStreamField("myObjNameList", ArrayList.class)
    };
    private static final ObjectStreamField[] newSerialPersistentFields =
    {
      new ObjectStreamField("name", String.class),
      new ObjectStreamField("objectNameList", List.class)
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
    private String name = null;
    private List<ObjectName> objectNameList = new ArrayList<ObjectName>();
    public Role(String roleName,
                List<ObjectName> roleValue)
        throws IllegalArgumentException {
        if (roleName == null || roleValue == null) {
            String excMsg = "Invalid parameter";
            throw new IllegalArgumentException(excMsg);
        }
        setRoleName(roleName);
        setRoleValue(roleValue);
        return;
    }
    public String getRoleName() {
        return name;
    }
    public List<ObjectName> getRoleValue() {
        return objectNameList;
    }
    public void setRoleName(String roleName)
        throws IllegalArgumentException {
        if (roleName == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        name = roleName;
        return;
    }
    public void setRoleValue(List<ObjectName> roleValue)
        throws IllegalArgumentException {
        if (roleValue == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        objectNameList = new ArrayList<ObjectName>(roleValue);
        return;
    }
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("role name: " + name + "; role value: ");
        for (Iterator<ObjectName> objNameIter = objectNameList.iterator();
             objNameIter.hasNext();) {
            ObjectName currObjName = objNameIter.next();
            result.append(currObjName.toString());
            if (objNameIter.hasNext()) {
                result.append(", ");
            }
        }
        return result.toString();
    }
    public Object clone() {
        try {
            return new Role(name, objectNameList);
        } catch (IllegalArgumentException exc) {
            return null; 
        }
    }
    public static String roleValueToString(List<ObjectName> roleValue)
        throws IllegalArgumentException {
        if (roleValue == null) {
            String excMsg = "Invalid parameter";
            throw new IllegalArgumentException(excMsg);
        }
        StringBuilder result = new StringBuilder();
        for (ObjectName currObjName : roleValue) {
            if (result.length() > 0)
                result.append("\n");
            result.append(currObjName.toString());
        }
        return result.toString();
    }
    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
      if (compat)
      {
        ObjectInputStream.GetField fields = in.readFields();
        name = (String) fields.get("myName", null);
        if (fields.defaulted("myName"))
        {
          throw new NullPointerException("myName");
        }
        objectNameList = cast(fields.get("myObjNameList", null));
        if (fields.defaulted("myObjNameList"))
        {
          throw new NullPointerException("myObjNameList");
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
        fields.put("myName", name);
        fields.put("myObjNameList", objectNameList);
        out.writeFields();
      }
      else
      {
        out.defaultWriteObject();
      }
    }
}
