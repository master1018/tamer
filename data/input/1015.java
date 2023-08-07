public class ColumnDescriptorEntry implements Comparable {
    public ColumnDescriptorEntry(String name, int type) {
        this.name = name;
        this.type = type;
    }
    public ColumnDescriptorEntry(String name, int type, boolean nullable, boolean primaryKey) {
        this.name = name;
        this.nullable = nullable;
        this.primaryKey = primaryKey;
        this.type = type;
    }
    public boolean isNullable() {
        return nullable;
    }
    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }
    boolean nullable = false;
    public boolean isPrimaryKey() {
        return primaryKey;
    }
    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }
    boolean primaryKey = false;
    String name;
    int type;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
    public int compareTo(Object o) {
        if (!(o instanceof ColumnDescriptorEntry)) {
            throw new ClassCastException("Error: The given object must be of type ColumnDescriptor to be compared here. It is of type " + o.getClass().getName());
        }
        ColumnDescriptorEntry cd = (ColumnDescriptorEntry) o;
        if (getName() == null) {
            if (cd.getName() == null) return 0;
            return cd.getName().compareTo(null);
        }
        return getName().compareTo(cd.getName());
    }
}
