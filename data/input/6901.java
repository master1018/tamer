class JDMInformItem extends SimpleNode {
    protected JDMInformCommunity comm = null;
    JDMInformItem(int id) {
        super(id);
    }
    JDMInformItem(Parser p, int id) {
        super(p, id);
    }
    public static Node jjtCreate(int id) {
        return new JDMInformItem(id);
    }
    public static Node jjtCreate(Parser p, int id) {
        return new JDMInformItem(p, id);
    }
    public JDMInformCommunity getCommunity(){
        return comm;
    }
}
