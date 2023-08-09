public class XMLParseException
extends Exception
{
    private static final long oldSerialVersionUID = -7780049316655891976L;
    private static final long newSerialVersionUID = 3176664577895105181L;
    private static final ObjectStreamField[] oldSerialPersistentFields =
    {
      new ObjectStreamField("msgStr", String.class)
    };
  private static final ObjectStreamField[] newSerialPersistentFields = { };
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
    public  XMLParseException ()
    {
      super("XML Parse Exception.");
    }
    public  XMLParseException (String s)
    {
      super("XML Parse Exception: " + s);
    }
    public  XMLParseException (Exception e, String s)
    {
      super("XML Parse Exception: " + s + ":" + e.toString());
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
        fields.put("msgStr", getMessage());
        out.writeFields();
      }
      else
      {
        out.defaultWriteObject();
      }
    }
}
