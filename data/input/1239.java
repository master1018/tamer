public class JndiAuthorization extends BaseAclsAuthorization implements Authorization {
    static final String JNDI_PROPFILE = "jndi.properties";
    private final Jndi jndi;
    public JndiAuthorization(final Framework framework, final File aclBaseDir) throws IOException {
        this(framework, new File(framework.getConfigDir(), JNDI_PROPFILE), aclBaseDir);
    }
    public JndiAuthorization(final Framework framework, final File configFile, final File aclBaseDir) throws IOException {
        super(framework, aclBaseDir);
        final PropertyLookup lookup = PropertyLookup.create(configFile);
        final JndiConfigParser cp = new JndiConfigParser(lookup);
        final JndiConfig cfg = cp.parse();
        try {
            logger.debug("Connecting to JNDI Server: " + cfg.getConnectionUrl());
            jndi = new Jndi(cfg);
        } catch (NamingException e) {
            throw new AuthorizationException("Caught NameNotFoundException, error: " + e.getMessage() + ", Unable to connect to JNDI Server: " + cfg.getConnectionUrl() + " with connectionName: " + cfg.getConnectionName());
        }
        logger.debug(toString());
    }
    static Logger logger = Logger.getLogger(JndiAuthorization.class.getName());
    public String[] determineUserRoles(String user) {
        String[] roles;
        try {
            logger.debug("obtaining list of roles for user: " + user);
            roles = jndi.getRoles(user);
        } catch (NamingException e) {
            logger.error("Unable to obtain role memberships for user: " + user);
            throw new AuthorizationException("Caught NamingException, error: " + e.getMessage() + ", Unable to obtain role memberships for user: " + user);
        }
        return roles;
    }
    public String toString() {
        return "JndiAuthorization{" + "aclBasedir=" + getAclBasedir() + "}";
    }
}
