class ClassNotFound extends Exception {
    public Identifier name;
    public ClassNotFound(Identifier nm) {
        super(nm.toString());
        name = nm;
    }
}
