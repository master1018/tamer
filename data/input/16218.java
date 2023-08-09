class NameAndTypeData {
    MemberDefinition field;
    NameAndTypeData(MemberDefinition field) {
        this.field = field;
    }
    public int hashCode() {
        return field.getName().hashCode() * field.getType().hashCode();
    }
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof NameAndTypeData)) {
            NameAndTypeData nt = (NameAndTypeData)obj;
            return field.getName().equals(nt.field.getName()) &&
                field.getType().equals(nt.field.getType());
        }
        return false;
    }
    public String toString() {
        return "%%" + field.toString() + "%%";
    }
}
