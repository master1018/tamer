public class NamedObject  {
    private final ObjectName name;
    private final DynamicMBean object;
    public NamedObject(ObjectName objectName, DynamicMBean object)  {
        if (objectName.isPattern()) {
            throw new RuntimeOperationsException(new IllegalArgumentException("Invalid name->"+ objectName.toString()));
        }
        this.name= objectName;
        this.object= object;
    }
    public NamedObject(String objectName, DynamicMBean object) throws MalformedObjectNameException{
        ObjectName objName= new ObjectName(objectName);
        if (objName.isPattern()) {
            throw new RuntimeOperationsException(new IllegalArgumentException("Invalid name->"+ objName.toString()));
        }
        this.name= objName;
        this.object= object;
    }
    public boolean equals(Object object)  {
        if (this == object) return true;
        if (object == null) return false;
        if (!(object instanceof NamedObject)) return false;
        NamedObject no = (NamedObject) object;
        return name.equals(no.getName());
    }
    public int hashCode() {
        return name.hashCode();
    }
    public ObjectName getName()  {
        return name;
    }
    public DynamicMBean getObject()  {
        return object;
   }
 }
