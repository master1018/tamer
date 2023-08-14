public class MBeanServerNotificationFilter extends NotificationFilterSupport {
    private static final long oldSerialVersionUID = 6001782699077323605L;
    private static final long newSerialVersionUID = 2605900539589789736L;
    private static final ObjectStreamField[] oldSerialPersistentFields =
    {
      new ObjectStreamField("mySelectObjNameList", Vector.class),
      new ObjectStreamField("myDeselectObjNameList", Vector.class)
    };
    private static final ObjectStreamField[] newSerialPersistentFields =
    {
      new ObjectStreamField("selectedNames", List.class),
      new ObjectStreamField("deselectedNames", List.class)
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
    private List<ObjectName> selectedNames = new Vector<ObjectName>();
    private List<ObjectName> deselectedNames = null;
    public MBeanServerNotificationFilter() {
        super();
        RELATION_LOGGER.entering(MBeanServerNotificationFilter.class.getName(),
                "MBeanServerNotificationFilter");
        enableType(MBeanServerNotification.REGISTRATION_NOTIFICATION);
        enableType(MBeanServerNotification.UNREGISTRATION_NOTIFICATION);
        RELATION_LOGGER.exiting(MBeanServerNotificationFilter.class.getName(),
                "MBeanServerNotificationFilter");
        return;
    }
    public synchronized void disableAllObjectNames() {
        RELATION_LOGGER.entering(MBeanServerNotificationFilter.class.getName(),
                "disableAllObjectNames");
        selectedNames = new Vector<ObjectName>();
        deselectedNames = null;
        RELATION_LOGGER.exiting(MBeanServerNotificationFilter.class.getName(),
                "disableAllObjectNames");
        return;
    }
    public synchronized void disableObjectName(ObjectName objectName)
        throws IllegalArgumentException {
        if (objectName == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(MBeanServerNotificationFilter.class.getName(),
                "disableObjectName", objectName);
        if (selectedNames != null) {
            if (selectedNames.size() != 0) {
                selectedNames.remove(objectName);
            }
        }
        if (deselectedNames != null) {
            if (!(deselectedNames.contains(objectName))) {
                deselectedNames.add(objectName);
            }
        }
        RELATION_LOGGER.exiting(MBeanServerNotificationFilter.class.getName(),
                "disableObjectName");
        return;
    }
    public synchronized void enableAllObjectNames() {
        RELATION_LOGGER.entering(MBeanServerNotificationFilter.class.getName(),
                "enableAllObjectNames");
        selectedNames = null;
        deselectedNames = new Vector<ObjectName>();
        RELATION_LOGGER.exiting(MBeanServerNotificationFilter.class.getName(),
                "enableAllObjectNames");
        return;
    }
    public synchronized void enableObjectName(ObjectName objectName)
        throws IllegalArgumentException {
        if (objectName == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(MBeanServerNotificationFilter.class.getName(),
                "enableObjectName", objectName);
        if (deselectedNames != null) {
            if (deselectedNames.size() != 0) {
                deselectedNames.remove(objectName);
            }
        }
        if (selectedNames != null) {
            if (!(selectedNames.contains(objectName))) {
                selectedNames.add(objectName);
            }
        }
        RELATION_LOGGER.exiting(MBeanServerNotificationFilter.class.getName(),
                "enableObjectName");
        return;
    }
    public synchronized Vector<ObjectName> getEnabledObjectNames() {
        if (selectedNames != null) {
            return new Vector<ObjectName>(selectedNames);
        } else {
            return null;
        }
    }
    public synchronized Vector<ObjectName> getDisabledObjectNames() {
        if (deselectedNames != null) {
            return new Vector<ObjectName>(deselectedNames);
        } else {
            return null;
        }
    }
    public synchronized boolean isNotificationEnabled(Notification notif)
        throws IllegalArgumentException {
        if (notif == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(MBeanServerNotificationFilter.class.getName(),
                "isNotificationEnabled", notif);
        String ntfType = notif.getType();
        Vector<String> enabledTypes = getEnabledTypes();
        if (!(enabledTypes.contains(ntfType))) {
            RELATION_LOGGER.logp(Level.FINER,
                    MBeanServerNotificationFilter.class.getName(),
                    "isNotificationEnabled",
                    "Type not selected, exiting");
            return false;
        }
        MBeanServerNotification mbsNtf = (MBeanServerNotification)notif;
        ObjectName objName = mbsNtf.getMBeanName();
        boolean isSelectedFlg = false;
        if (selectedNames != null) {
            if (selectedNames.size() == 0) {
                RELATION_LOGGER.logp(Level.FINER,
                        MBeanServerNotificationFilter.class.getName(),
                        "isNotificationEnabled",
                        "No ObjectNames selected, exiting");
                return false;
            }
            isSelectedFlg = selectedNames.contains(objName);
            if (!isSelectedFlg) {
                RELATION_LOGGER.logp(Level.FINER,
                        MBeanServerNotificationFilter.class.getName(),
                        "isNotificationEnabled",
                        "ObjectName not in selected list, exiting");
                return false;
            }
        }
        if (!isSelectedFlg) {
            if (deselectedNames == null) {
                RELATION_LOGGER.logp(Level.FINER,
                        MBeanServerNotificationFilter.class.getName(),
                        "isNotificationEnabled",
                        "ObjectName not selected, and all " +
                        "names deselected, exiting");
                return false;
            } else if (deselectedNames.contains(objName)) {
                RELATION_LOGGER.logp(Level.FINER,
                        MBeanServerNotificationFilter.class.getName(),
                        "isNotificationEnabled",
                        "ObjectName explicitly not selected, exiting");
                return false;
            }
        }
        RELATION_LOGGER.logp(Level.FINER,
                MBeanServerNotificationFilter.class.getName(),
                "isNotificationEnabled",
                "ObjectName selected, exiting");
        return true;
    }
    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
      if (compat)
      {
        ObjectInputStream.GetField fields = in.readFields();
        selectedNames = cast(fields.get("mySelectObjNameList", null));
        if (fields.defaulted("mySelectObjNameList"))
        {
          throw new NullPointerException("mySelectObjNameList");
        }
        deselectedNames = cast(fields.get("myDeselectObjNameList", null));
        if (fields.defaulted("myDeselectObjNameList"))
        {
          throw new NullPointerException("myDeselectObjNameList");
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
        fields.put("mySelectObjNameList", selectedNames);
        fields.put("myDeselectObjNameList", deselectedNames);
        out.writeFields();
      }
      else
      {
        out.defaultWriteObject();
      }
    }
}
