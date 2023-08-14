public class RoleResult implements Serializable {
    private static final long oldSerialVersionUID = 3786616013762091099L;
    private static final long newSerialVersionUID = -6304063118040985512L;
    private static final ObjectStreamField[] oldSerialPersistentFields =
    {
      new ObjectStreamField("myRoleList", RoleList.class),
      new ObjectStreamField("myRoleUnresList", RoleUnresolvedList.class)
    };
    private static final ObjectStreamField[] newSerialPersistentFields =
    {
      new ObjectStreamField("roleList", RoleList.class),
      new ObjectStreamField("unresolvedRoleList", RoleUnresolvedList.class)
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
    private RoleList roleList = null;
    private RoleUnresolvedList unresolvedRoleList = null;
    public RoleResult(RoleList list,
                      RoleUnresolvedList unresolvedList) {
        setRoles(list);
        setRolesUnresolved(unresolvedList);
        return;
    }
    public RoleList getRoles() {
        return roleList;
    }
    public RoleUnresolvedList getRolesUnresolved() {
        return unresolvedRoleList;
    }
    public void setRoles(RoleList list) {
        if (list != null) {
            roleList = new RoleList();
            for (Iterator<?> roleIter = list.iterator();
                 roleIter.hasNext();) {
                Role currRole = (Role)(roleIter.next());
                roleList.add((Role)(currRole.clone()));
            }
        } else {
            roleList = null;
        }
        return;
    }
    public void setRolesUnresolved(RoleUnresolvedList unresolvedList) {
        if (unresolvedList != null) {
            unresolvedRoleList = new RoleUnresolvedList();
            for (Iterator<?> roleUnresIter = unresolvedList.iterator();
                 roleUnresIter.hasNext();) {
                RoleUnresolved currRoleUnres =
                    (RoleUnresolved)(roleUnresIter.next());
                unresolvedRoleList.add((RoleUnresolved)(currRoleUnres.clone()));
            }
        } else {
            unresolvedRoleList = null;
        }
        return;
    }
    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
      if (compat)
      {
        ObjectInputStream.GetField fields = in.readFields();
        roleList = (RoleList) fields.get("myRoleList", null);
        if (fields.defaulted("myRoleList"))
        {
          throw new NullPointerException("myRoleList");
        }
        unresolvedRoleList = (RoleUnresolvedList) fields.get("myRoleUnresList", null);
        if (fields.defaulted("myRoleUnresList"))
        {
          throw new NullPointerException("myRoleUnresList");
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
        fields.put("myRoleList", roleList);
        fields.put("myRoleUnresList", unresolvedRoleList);
        out.writeFields();
      }
      else
      {
        out.defaultWriteObject();
      }
    }
}
