class AmbiguousClass extends ClassNotFound {
    public Identifier name1;
    public Identifier name2;
    public AmbiguousClass(Identifier name1, Identifier name2) {
        super(name1.getName());
        this.name1 = name1;
        this.name2 = name2;
    }
}
