abstract class Host extends SimpleNode implements Serializable {
    public Host(int id) {
        super(id);
    }
    public Host(Parser p, int id) {
        super(p, id);
    }
    protected abstract PrincipalImpl createAssociatedPrincipal()
        throws UnknownHostException;
    protected abstract String getHname();
    public void buildAclEntries(PrincipalImpl owner, AclImpl acl) {
        PrincipalImpl p=null;
        try {
            p = createAssociatedPrincipal();
        } catch(UnknownHostException e) {
            if (SNMP_LOGGER.isLoggable(Level.FINEST)) {
                SNMP_LOGGER.logp(Level.FINEST, Host.class.getName(),
                        "buildAclEntries",
                        "Cannot create ACL entry; got exception", e);
            }
            throw new IllegalArgumentException("Cannot create ACL entry for " + e.getMessage());
        }
        AclEntryImpl entry= null;
        try {
            entry = new AclEntryImpl(p);
            registerPermission(entry);
            acl.addEntry(owner, entry);
        } catch(UnknownHostException e) {
            if (SNMP_LOGGER.isLoggable(Level.FINEST)) {
                SNMP_LOGGER.logp(Level.FINEST, Host.class.getName(),
                        "buildAclEntries",
                        "Cannot create ACL entry; got exception", e);
            }
            return;
        } catch(NotOwnerException a) {
            if (SNMP_LOGGER.isLoggable(Level.FINEST)) {
                SNMP_LOGGER.logp(Level.FINEST, Host.class.getName(),
                        "buildAclEntries",
                        "Cannot create ACL entry; got exception", a);
            }
            return;
        }
    }
    private void registerPermission(AclEntryImpl entry) {
        JDMHost host= (JDMHost) jjtGetParent();
        JDMManagers manager= (JDMManagers) host.jjtGetParent();
        JDMAclItem acl= (JDMAclItem) manager.jjtGetParent();
        JDMAccess access= acl.getAccess();
        access.putPermission(entry);
        JDMCommunities comm= acl.getCommunities();
        comm.buildCommunities(entry);
    }
    public void buildTrapEntries(Hashtable<InetAddress, Vector<String>> dest) {
        JDMHostTrap host= (JDMHostTrap) jjtGetParent();
        JDMTrapInterestedHost hosts= (JDMTrapInterestedHost) host.jjtGetParent();
        JDMTrapItem trap = (JDMTrapItem) hosts.jjtGetParent();
        JDMTrapCommunity community = trap.getCommunity();
        String comm = community.getCommunity();
        InetAddress add = null;
        try {
            add = java.net.InetAddress.getByName(getHname());
        } catch(UnknownHostException e) {
            if (SNMP_LOGGER.isLoggable(Level.FINEST)) {
                SNMP_LOGGER.logp(Level.FINEST, Host.class.getName(),
                        "buildTrapEntries",
                        "Cannot create TRAP entry; got exception", e);
            }
            return;
        }
        Vector<String> list = null;
        if (dest.containsKey(add)){
            list = dest.get(add);
            if (!list.contains(comm)){
                list.addElement(comm);
            }
        } else {
            list = new Vector<String>();
            list.addElement(comm);
            dest.put(add,list);
        }
    }
    public void buildInformEntries(Hashtable<InetAddress, Vector<String>> dest) {
        JDMHostInform host= (JDMHostInform) jjtGetParent();
        JDMInformInterestedHost hosts= (JDMInformInterestedHost) host.jjtGetParent();
        JDMInformItem inform = (JDMInformItem) hosts.jjtGetParent();
        JDMInformCommunity community = inform.getCommunity();
        String comm = community.getCommunity();
        InetAddress add = null;
        try {
            add = java.net.InetAddress.getByName(getHname());
        } catch(UnknownHostException e) {
            if (SNMP_LOGGER.isLoggable(Level.FINEST)) {
                SNMP_LOGGER.logp(Level.FINEST, Host.class.getName(),
                        "buildTrapEntries",
                        "Cannot create INFORM entry; got exception", e);
            }
            return;
        }
        Vector<String> list = null;
        if (dest.containsKey(add)){
            list = dest.get(add);
            if (!list.contains(comm)){
                list.addElement(comm);
            }
        } else {
            list = new Vector<String>();
            list.addElement(comm);
            dest.put(add,list);
        }
    }
}
