class JDMHostInform extends SimpleNode {
    protected String name= "";
    JDMHostInform(int id) {
        super(id);
    }
    JDMHostInform(Parser p, int id) {
        super(p, id);
    }
    public static Node jjtCreate(int id) {
        return new JDMHostInform(id);
    }
    public static Node jjtCreate(Parser p, int id) {
        return new JDMHostInform(p, id);
    }
}
