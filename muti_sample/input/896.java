class JDMAccess extends SimpleNode {
  protected int access= -1;
  JDMAccess(int id) {
    super(id);
  }
  JDMAccess(Parser p, int id) {
    super(p, id);
  }
  public static Node jjtCreate(int id) {
      return new JDMAccess(id);
  }
  public static Node jjtCreate(Parser p, int id) {
      return new JDMAccess(p, id);
  }
  protected void putPermission(AclEntryImpl entry) {
    if (access == ParserConstants.RO) {
       entry.addPermission(com.sun.jmx.snmp.IPAcl.SnmpAcl.getREAD());
    }
    if (access == ParserConstants.RW) {
       entry.addPermission(com.sun.jmx.snmp.IPAcl.SnmpAcl.getREAD());
       entry.addPermission(com.sun.jmx.snmp.IPAcl.SnmpAcl.getWRITE());
    }
  }
}
