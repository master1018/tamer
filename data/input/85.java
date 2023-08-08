public class RelationshipType extends EnsemblType {
    private static HashMap<String, RelationshipType> typeHash;
    public static final RelationshipType UNKNOWN = RelationshipType.makeRelationshipType("unknown");
    private static RelationshipType makeRelationshipType(String typeName) {
        if (RelationshipType.typeHash == null) {
            RelationshipType.typeHash = new HashMap<String, RelationshipType>();
        }
        RelationshipType rt = getRelationshipType(typeName);
        if (rt == null) {
            rt = new RelationshipType(typeName);
            RelationshipType.typeHash.put(typeName, rt);
        }
        return rt;
    }
    protected RelationshipType(String value) {
        this.label = value;
    }
    public static Collection<? extends RelationshipType> getAllTypes() {
        return typeHash.values();
    }
    public static RelationshipType getRelationshipType(String value) {
        return typeHash.get(value);
    }
    private Object readResolve() throws ObjectStreamException {
        return this;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        String otherString = obj.toString();
        return this.toString().equals(otherString);
    }
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.label != null ? this.label.hashCode() : 0);
        return hash;
    }
}
