public class SnmpAcl implements InetAddressAcl, Serializable {
    private static final long serialVersionUID = -6702287103824397063L;
    static final PermissionImpl READ  = new PermissionImpl("READ");
    static final PermissionImpl WRITE = new PermissionImpl("WRITE");
    public SnmpAcl(String Owner)
        throws UnknownHostException, IllegalArgumentException {
        this(Owner,null);
    }
    public SnmpAcl(String Owner, String aclFileName)
        throws UnknownHostException, IllegalArgumentException {
        trapDestList= new Hashtable<InetAddress, Vector<String>>();
        informDestList= new Hashtable<InetAddress, Vector<String>>();
        owner = new PrincipalImpl();
        try {
            acl = new AclImpl(owner,Owner);
            AclEntry ownEntry = new AclEntryImpl(owner);
            ownEntry.addPermission(READ);
            ownEntry.addPermission(WRITE);
            acl.addEntry(owner,ownEntry);
        } catch (NotOwnerException ex) {
            if (SNMP_LOGGER.isLoggable(Level.FINEST)) {
                SNMP_LOGGER.logp(Level.FINEST, SnmpAcl.class.getName(),
                    "SnmpAcl(String,String)",
                    "Should never get NotOwnerException as the owner " +
                    "is built in this constructor");
            }
        }
        if (aclFileName == null) setDefaultFileName();
        else setAuthorizedListFile(aclFileName);
        readAuthorizedListFile();
    }
    public Enumeration entries() {
        return acl.entries();
    }
    public Enumeration<String> communities() {
        HashSet<String> set = new HashSet<String>();
        Vector<String> res = new Vector<String>();
        for (Enumeration e = acl.entries() ; e.hasMoreElements() ;) {
            AclEntryImpl entry = (AclEntryImpl) e.nextElement();
            for (Enumeration cs = entry.communities();
                 cs.hasMoreElements() ;) {
                set.add((String) cs.nextElement());
            }
        }
        String[] objs = set.toArray(new String[0]);
        for(int i = 0; i < objs.length; i++)
            res.addElement(objs[i]);
        return res.elements();
    }
    public String getName() {
        return acl.getName();
    }
    static public PermissionImpl getREAD() {
        return READ;
    }
    static public PermissionImpl getWRITE() {
        return WRITE;
    }
    public static String getDefaultAclFileName() {
        final String fileSeparator =
            System.getProperty("file.separator");
        final StringBuffer defaultAclName =
            new StringBuffer(System.getProperty("java.home")).
            append(fileSeparator).append("lib").append(fileSeparator).
            append("snmp.acl");
        return defaultAclName.toString();
    }
    public void setAuthorizedListFile(String filename)
        throws IllegalArgumentException {
        File file = new File(filename);
        if (!file.isFile() ) {
            if (SNMP_LOGGER.isLoggable(Level.FINEST)) {
                SNMP_LOGGER.logp(Level.FINEST, SnmpAcl.class.getName(),
                    "setAuthorizedListFile", "ACL file not found: " + filename);
            }
            throw new
                IllegalArgumentException("The specified file ["+file+"] "+
                                         "doesn't exist or is not a file, "+
                                         "no configuration loaded");
        }
        if (SNMP_LOGGER.isLoggable(Level.FINER)) {
            SNMP_LOGGER.logp(Level.FINER, SnmpAcl.class.getName(),
                "setAuthorizedListFile", "Default file set to " + filename);
        }
        authorizedListFile = filename;
    }
    public void rereadTheFile() throws NotOwnerException, UnknownHostException {
        alwaysAuthorized = false;
        acl.removeAll(owner);
        trapDestList.clear();
        informDestList.clear();
        AclEntry ownEntry = new AclEntryImpl(owner);
        ownEntry.addPermission(READ);
        ownEntry.addPermission(WRITE);
        acl.addEntry(owner,ownEntry);
        readAuthorizedListFile();
    }
    public String getAuthorizedListFile() {
        return authorizedListFile;
    }
    public boolean checkReadPermission(InetAddress address) {
        if (alwaysAuthorized) return ( true );
        PrincipalImpl p = new PrincipalImpl(address);
        return acl.checkPermission(p, READ);
    }
    public boolean checkReadPermission(InetAddress address, String community) {
        if (alwaysAuthorized) return ( true );
        PrincipalImpl p = new PrincipalImpl(address);
        return acl.checkPermission(p, community, READ);
    }
    public boolean checkCommunity(String community) {
        return acl.checkCommunity(community);
    }
    public boolean checkWritePermission(InetAddress address) {
        if (alwaysAuthorized) return ( true );
        PrincipalImpl p = new PrincipalImpl(address);
        return acl.checkPermission(p, WRITE);
    }
    public boolean checkWritePermission(InetAddress address, String community) {
        if (alwaysAuthorized) return ( true );
        PrincipalImpl p = new PrincipalImpl(address);
        return acl.checkPermission(p, community, WRITE);
    }
    public Enumeration getTrapDestinations() {
        return trapDestList.keys();
    }
    public Enumeration getTrapCommunities(InetAddress i) {
        Vector list = null;
        if ((list = (Vector)trapDestList.get(i)) != null ) {
            if (SNMP_LOGGER.isLoggable(Level.FINER)) {
                SNMP_LOGGER.logp(Level.FINER, SnmpAcl.class.getName(),
                    "getTrapCommunities", "["+i.toString()+"] is in list");
            }
            return list.elements();
        } else {
            list = new Vector();
            if (SNMP_LOGGER.isLoggable(Level.FINER)) {
                SNMP_LOGGER.logp(Level.FINER, SnmpAcl.class.getName(),
                    "getTrapCommunities", "["+i.toString()+"] is not in list");
            }
            return list.elements();
        }
    }
    public Enumeration getInformDestinations() {
        return informDestList.keys();
    }
    public Enumeration getInformCommunities(InetAddress i) {
        Vector list = null;
        if ((list = (Vector)informDestList.get(i)) != null ) {
            if (SNMP_LOGGER.isLoggable(Level.FINER)) {
                SNMP_LOGGER.logp(Level.FINER, SnmpAcl.class.getName(),
                    "getInformCommunities", "["+i.toString()+"] is in list");
            }
            return list.elements();
        } else {
            list = new Vector();
            if (SNMP_LOGGER.isLoggable(Level.FINER)) {
                SNMP_LOGGER.logp(Level.FINER, SnmpAcl.class.getName(),
                    "getInformCommunities", "["+i.toString()+"] is not in list");
            }
            return list.elements();
        }
    }
    private void readAuthorizedListFile() {
        alwaysAuthorized = false;
        if (authorizedListFile == null) {
            if (SNMP_LOGGER.isLoggable(Level.FINER)) {
                SNMP_LOGGER.logp(Level.FINER, SnmpAcl.class.getName(),
                    "readAuthorizedListFile", "alwaysAuthorized set to true");
            }
            alwaysAuthorized = true ;
        } else {
            Parser parser = null;
            try {
                parser= new Parser(new FileInputStream(getAuthorizedListFile()));
            } catch (FileNotFoundException e) {
                if (SNMP_LOGGER.isLoggable(Level.FINEST)) {
                    SNMP_LOGGER.logp(Level.FINEST, SnmpAcl.class.getName(),
                            "readAuthorizedListFile",
                            "The specified file was not found, authorize everybody");
                }
                alwaysAuthorized = true ;
                return;
            }
            try {
                JDMSecurityDefs n = parser.SecurityDefs();
                n.buildAclEntries(owner, acl);
                n.buildTrapEntries(trapDestList);
                n.buildInformEntries(informDestList);
            } catch (ParseException e) {
                if (SNMP_LOGGER.isLoggable(Level.FINEST)) {
                    SNMP_LOGGER.logp(Level.FINEST, SnmpAcl.class.getName(),
                        "readAuthorizedListFile", "Got parsing exception", e);
                }
                throw new IllegalArgumentException(e.getMessage());
            } catch (Error err) {
                if (SNMP_LOGGER.isLoggable(Level.FINEST)) {
                    SNMP_LOGGER.logp(Level.FINEST, SnmpAcl.class.getName(),
                        "readAuthorizedListFile", "Got unexpected error", err);
                }
                throw new IllegalArgumentException(err.getMessage());
            }
            for(Enumeration e = acl.entries(); e.hasMoreElements();) {
                AclEntryImpl aa = (AclEntryImpl) e.nextElement();
                if (SNMP_LOGGER.isLoggable(Level.FINER)) {
                    SNMP_LOGGER.logp(Level.FINER, SnmpAcl.class.getName(),
                            "readAuthorizedListFile",
                            "===> " + aa.getPrincipal().toString());
                }
                for (Enumeration eee = aa.permissions();eee.hasMoreElements();) {
                    java.security.acl.Permission perm = (java.security.acl.Permission)eee.nextElement();
                    if (SNMP_LOGGER.isLoggable(Level.FINER)) {
                        SNMP_LOGGER.logp(Level.FINER, SnmpAcl.class.getName(),
                                "readAuthorizedListFile", "perm = " + perm);
                    }
                }
            }
        }
    }
    private void setDefaultFileName() {
        try {
            setAuthorizedListFile(getDefaultAclFileName());
        } catch (IllegalArgumentException x) {
        }
    }
    private AclImpl acl = null;
    private boolean alwaysAuthorized = false;
    private String authorizedListFile = null;
    private Hashtable<InetAddress, Vector<String>> trapDestList = null;
    private Hashtable<InetAddress, Vector<String>> informDestList = null;
    private PrincipalImpl owner = null;
}
