class JDMInformInterestedHost extends SimpleNode {
    JDMInformInterestedHost(int id) {
        super(id);
    }
    JDMInformInterestedHost(Parser p, int id) {
        super(p, id);
    }
    public static Node jjtCreate(int id) {
        return new JDMInformInterestedHost(id);
    }
    public static Node jjtCreate(Parser p, int id) {
        return new JDMInformInterestedHost(p, id);
    }
}
