public class RoleInfo implements Serializable {
    private static final long oldSerialVersionUID = 7227256952085334351L;
    private static final long newSerialVersionUID = 2504952983494636987L;
    private static final ObjectStreamField[] oldSerialPersistentFields =
    {
      new ObjectStreamField("myName", String.class),
      new ObjectStreamField("myIsReadableFlg", boolean.class),
      new ObjectStreamField("myIsWritableFlg", boolean.class),
      new ObjectStreamField("myDescription", String.class),
      new ObjectStreamField("myMinDegree", int.class),
      new ObjectStreamField("myMaxDegree", int.class),
      new ObjectStreamField("myRefMBeanClassName", String.class)
    };
    private static final ObjectStreamField[] newSerialPersistentFields =
    {
      new ObjectStreamField("name", String.class),
      new ObjectStreamField("isReadable", boolean.class),
      new ObjectStreamField("isWritable", boolean.class),
      new ObjectStreamField("description", String.class),
      new ObjectStreamField("minDegree", int.class),
      new ObjectStreamField("maxDegree", int.class),
      new ObjectStreamField("referencedMBeanClassName", String.class)
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
    public static final int ROLE_CARDINALITY_INFINITY = -1;
    private String name = null;
    private boolean isReadable;
    private boolean isWritable;
    private String description = null;
    private int minDegree;
    private int maxDegree;
    private String referencedMBeanClassName = null;
    public RoleInfo(String roleName,
                    String mbeanClassName,
                    boolean read,
                    boolean write,
                    int min,
                    int max,
                    String descr)
    throws IllegalArgumentException,
           InvalidRoleInfoException,
           ClassNotFoundException,
           NotCompliantMBeanException {
        init(roleName,
             mbeanClassName,
             read,
             write,
             min,
             max,
             descr);
        return;
    }
    public RoleInfo(String roleName,
                    String mbeanClassName,
                    boolean read,
                    boolean write)
    throws IllegalArgumentException,
           ClassNotFoundException,
           NotCompliantMBeanException {
        try {
            init(roleName,
                 mbeanClassName,
                 read,
                 write,
                 1,
                 1,
                 null);
        } catch (InvalidRoleInfoException exc) {
        }
        return;
    }
    public RoleInfo(String roleName,
                    String mbeanClassName)
    throws IllegalArgumentException,
           ClassNotFoundException,
           NotCompliantMBeanException {
        try {
            init(roleName,
                 mbeanClassName,
                 true,
                 true,
                 1,
                 1,
                 null);
        } catch (InvalidRoleInfoException exc) {
        }
        return;
    }
    public RoleInfo(RoleInfo roleInfo)
        throws IllegalArgumentException {
        if (roleInfo == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        try {
            init(roleInfo.getName(),
                 roleInfo.getRefMBeanClassName(),
                 roleInfo.isReadable(),
                 roleInfo.isWritable(),
                 roleInfo.getMinDegree(),
                 roleInfo.getMaxDegree(),
                 roleInfo.getDescription());
        } catch (InvalidRoleInfoException exc3) {
        }
    }
    public String getName() {
        return name;
    }
    public boolean isReadable() {
        return isReadable;
    }
    public boolean isWritable() {
        return isWritable;
    }
    public String getDescription() {
        return description;
    }
    public int getMinDegree() {
        return minDegree;
    }
    public int getMaxDegree() {
        return maxDegree;
    }
    public String getRefMBeanClassName() {
        return referencedMBeanClassName;
    }
    public boolean checkMinDegree(int value) {
        if (value >= ROLE_CARDINALITY_INFINITY &&
            (minDegree == ROLE_CARDINALITY_INFINITY
             || value >= minDegree)) {
            return true;
        } else {
            return false;
        }
    }
    public boolean checkMaxDegree(int value) {
        if (value >= ROLE_CARDINALITY_INFINITY &&
            (maxDegree == ROLE_CARDINALITY_INFINITY ||
             (value != ROLE_CARDINALITY_INFINITY &&
              value <= maxDegree))) {
            return true;
        } else {
            return false;
        }
    }
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("role info name: " + name);
        result.append("; isReadable: " + isReadable);
        result.append("; isWritable: " + isWritable);
        result.append("; description: " + description);
        result.append("; minimum degree: " + minDegree);
        result.append("; maximum degree: " + maxDegree);
        result.append("; MBean class: " + referencedMBeanClassName);
        return result.toString();
    }
    private void init(String roleName,
                      String mbeanClassName,
                      boolean read,
                      boolean write,
                      int min,
                      int max,
                      String descr)
            throws IllegalArgumentException,
                   InvalidRoleInfoException {
        if (roleName == null ||
            mbeanClassName == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        name = roleName;
        isReadable = read;
        isWritable = write;
        if (descr != null) {
            description = descr;
        }
        boolean invalidRoleInfoFlg = false;
        StringBuilder excMsgStrB = new StringBuilder();
        if (max != ROLE_CARDINALITY_INFINITY &&
            (min == ROLE_CARDINALITY_INFINITY ||
             min > max)) {
            excMsgStrB.append("Minimum degree ");
            excMsgStrB.append(min);
            excMsgStrB.append(" is greater than maximum degree ");
            excMsgStrB.append(max);
            invalidRoleInfoFlg = true;
        } else if (min < ROLE_CARDINALITY_INFINITY ||
                   max < ROLE_CARDINALITY_INFINITY) {
            excMsgStrB.append("Minimum or maximum degree has an illegal value, must be [0, ROLE_CARDINALITY_INFINITY].");
            invalidRoleInfoFlg = true;
        }
        if (invalidRoleInfoFlg) {
            throw new InvalidRoleInfoException(excMsgStrB.toString());
        }
        minDegree = min;
        maxDegree = max;
        referencedMBeanClassName = mbeanClassName;
        return;
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
        isReadable = fields.get("myIsReadableFlg", false);
        if (fields.defaulted("myIsReadableFlg"))
        {
          throw new NullPointerException("myIsReadableFlg");
        }
        isWritable = fields.get("myIsWritableFlg", false);
        if (fields.defaulted("myIsWritableFlg"))
        {
          throw new NullPointerException("myIsWritableFlg");
        }
        description = (String) fields.get("myDescription", null);
        if (fields.defaulted("myDescription"))
        {
          throw new NullPointerException("myDescription");
        }
        minDegree = fields.get("myMinDegree", 0);
        if (fields.defaulted("myMinDegree"))
        {
          throw new NullPointerException("myMinDegree");
        }
        maxDegree = fields.get("myMaxDegree", 0);
        if (fields.defaulted("myMaxDegree"))
        {
          throw new NullPointerException("myMaxDegree");
        }
        referencedMBeanClassName = (String) fields.get("myRefMBeanClassName", null);
        if (fields.defaulted("myRefMBeanClassName"))
        {
          throw new NullPointerException("myRefMBeanClassName");
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
        fields.put("myIsReadableFlg", isReadable);
        fields.put("myIsWritableFlg", isWritable);
        fields.put("myDescription", description);
        fields.put("myMinDegree", minDegree);
        fields.put("myMaxDegree", maxDegree);
        fields.put("myRefMBeanClassName", referencedMBeanClassName);
        out.writeFields();
      }
      else
      {
        out.defaultWriteObject();
      }
    }
}
