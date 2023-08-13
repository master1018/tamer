class AmbiguousMember extends Exception {
    public MemberDefinition field1;
    public MemberDefinition field2;
    public AmbiguousMember(MemberDefinition field1, MemberDefinition field2) {
        super(field1.getName() + " + " + field2.getName());
        this.field1 = field1;
        this.field2 = field2;
    }
}
