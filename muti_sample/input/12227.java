class JDMInformCommunity extends SimpleNode {
    protected String community= "";
    JDMInformCommunity(int id) {
        super(id);
    }
    JDMInformCommunity(Parser p, int id) {
        super(p, id);
    }
    public static Node jjtCreate(int id) {
        return new JDMInformCommunity(id);
    }
    public static Node jjtCreate(Parser p, int id) {
        return new JDMInformCommunity(p, id);
    }
    public String getCommunity() {
        return community;
    }
}
