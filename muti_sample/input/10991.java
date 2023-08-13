public class NTLoginModule implements LoginModule {
    private NTSystem ntSystem;
    private Subject subject;
    private CallbackHandler callbackHandler;
    private Map<String, ?> sharedState;
    private Map<String, ?> options;
    private boolean debug = false;
    private boolean debugNative = false;
    private boolean succeeded = false;
    private boolean commitSucceeded = false;
    private NTUserPrincipal userPrincipal;              
    private NTSidUserPrincipal userSID;                 
    private NTDomainPrincipal userDomain;               
    private NTSidDomainPrincipal domainSID;             
    private NTSidPrimaryGroupPrincipal primaryGroup;    
    private NTSidGroupPrincipal groups[];               
    private NTNumericCredential iToken;                 
    public void initialize(Subject subject, CallbackHandler callbackHandler,
                           Map<String,?> sharedState,
                           Map<String,?> options)
    {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = sharedState;
        this.options = options;
        debug = "true".equalsIgnoreCase((String)options.get("debug"));
        debugNative="true".equalsIgnoreCase((String)options.get("debugNative"));
        if (debugNative == true) {
            debug = true;
        }
    }
    public boolean login() throws LoginException {
        succeeded = false; 
        ntSystem = new NTSystem(debugNative);
        if (ntSystem == null) {
            if (debug) {
                System.out.println("\t\t[NTLoginModule] " +
                                   "Failed in NT login");
            }
            throw new FailedLoginException
                ("Failed in attempt to import the " +
                 "underlying NT system identity information");
        }
        if (ntSystem.getName() == null) {
            throw new FailedLoginException
                ("Failed in attempt to import the " +
                 "underlying NT system identity information");
        }
        userPrincipal = new NTUserPrincipal(ntSystem.getName());
        if (debug) {
            System.out.println("\t\t[NTLoginModule] " +
                               "succeeded importing info: ");
            System.out.println("\t\t\tuser name = " +
                userPrincipal.getName());
        }
        if (ntSystem.getUserSID() != null) {
            userSID = new NTSidUserPrincipal(ntSystem.getUserSID());
            if (debug) {
                System.out.println("\t\t\tuser SID = " +
                        userSID.getName());
            }
        }
        if (ntSystem.getDomain() != null) {
            userDomain = new NTDomainPrincipal(ntSystem.getDomain());
            if (debug) {
                System.out.println("\t\t\tuser domain = " +
                        userDomain.getName());
            }
        }
        if (ntSystem.getDomainSID() != null) {
            domainSID =
                new NTSidDomainPrincipal(ntSystem.getDomainSID());
            if (debug) {
                System.out.println("\t\t\tuser domain SID = " +
                        domainSID.getName());
            }
        }
        if (ntSystem.getPrimaryGroupID() != null) {
            primaryGroup =
                new NTSidPrimaryGroupPrincipal(ntSystem.getPrimaryGroupID());
            if (debug) {
                System.out.println("\t\t\tuser primary group = " +
                        primaryGroup.getName());
            }
        }
        if (ntSystem.getGroupIDs() != null &&
            ntSystem.getGroupIDs().length > 0) {
            String groupSIDs[] = ntSystem.getGroupIDs();
            groups = new NTSidGroupPrincipal[groupSIDs.length];
            for (int i = 0; i < groupSIDs.length; i++) {
                groups[i] = new NTSidGroupPrincipal(groupSIDs[i]);
                if (debug) {
                    System.out.println("\t\t\tuser group = " +
                        groups[i].getName());
                }
            }
        }
        if (ntSystem.getImpersonationToken() != 0) {
            iToken = new NTNumericCredential(ntSystem.getImpersonationToken());
            if (debug) {
                System.out.println("\t\t\timpersonation token = " +
                        ntSystem.getImpersonationToken());
            }
        }
        succeeded = true;
        return succeeded;
    }
    public boolean commit() throws LoginException {
        if (succeeded == false) {
            if (debug) {
                System.out.println("\t\t[NTLoginModule]: " +
                    "did not add any Principals to Subject " +
                    "because own authentication failed.");
            }
            return false;
        }
        if (subject.isReadOnly()) {
            throw new LoginException ("Subject is ReadOnly");
        }
        Set<Principal> principals = subject.getPrincipals();
        if (!principals.contains(userPrincipal)) {
            principals.add(userPrincipal);
        }
        if (userSID != null && !principals.contains(userSID)) {
            principals.add(userSID);
        }
        if (userDomain != null && !principals.contains(userDomain)) {
            principals.add(userDomain);
        }
        if (domainSID != null && !principals.contains(domainSID)) {
            principals.add(domainSID);
        }
        if (primaryGroup != null && !principals.contains(primaryGroup)) {
            principals.add(primaryGroup);
        }
        for (int i = 0; groups != null && i < groups.length; i++) {
            if (!principals.contains(groups[i])) {
                principals.add(groups[i]);
            }
        }
        Set<Object> pubCreds = subject.getPublicCredentials();
        if (iToken != null && !pubCreds.contains(iToken)) {
            pubCreds.add(iToken);
        }
        commitSucceeded = true;
        return true;
    }
    public boolean abort() throws LoginException {
        if (debug) {
            System.out.println("\t\t[NTLoginModule]: " +
                "aborted authentication attempt");
        }
        if (succeeded == false) {
            return false;
        } else if (succeeded == true && commitSucceeded == false) {
            ntSystem = null;
            userPrincipal = null;
            userSID = null;
            userDomain = null;
            domainSID = null;
            primaryGroup = null;
            groups = null;
            iToken = null;
            succeeded = false;
        } else {
            logout();
        }
        return succeeded;
    }
    public boolean logout() throws LoginException {
        if (subject.isReadOnly()) {
            throw new LoginException ("Subject is ReadOnly");
        }
        Set<Principal> principals = subject.getPrincipals();
        if (principals.contains(userPrincipal)) {
            principals.remove(userPrincipal);
        }
        if (principals.contains(userSID)) {
            principals.remove(userSID);
        }
        if (principals.contains(userDomain)) {
            principals.remove(userDomain);
        }
        if (principals.contains(domainSID)) {
            principals.remove(domainSID);
        }
        if (principals.contains(primaryGroup)) {
            principals.remove(primaryGroup);
        }
        for (int i = 0; groups != null && i < groups.length; i++) {
            if (principals.contains(groups[i])) {
                principals.remove(groups[i]);
            }
        }
        Set<Object> pubCreds = subject.getPublicCredentials();
        if (pubCreds.contains(iToken)) {
            pubCreds.remove(iToken);
        }
        succeeded = false;
        commitSucceeded = false;
        userPrincipal = null;
        userDomain = null;
        userSID = null;
        domainSID = null;
        groups = null;
        primaryGroup = null;
        iToken = null;
        ntSystem = null;
        if (debug) {
                System.out.println("\t\t[NTLoginModule] " +
                                "completed logout processing");
        }
        return true;
    }
}
