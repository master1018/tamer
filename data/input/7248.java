final public class LdapReferralException extends
    javax.naming.ldap.LdapReferralException {
    private int handleReferrals;
    private Hashtable envprops;
    private String nextName;
    private Control[] reqCtls;
    private Vector referrals = null;    
    private int referralIndex = 0;      
    private int referralCount = 0;      
    private boolean foundEntry = false; 
    private boolean skipThisReferral = false;
    private int hopCount = 1;
    private NamingException errorEx = null;
    private String newRdn = null;
    private boolean debug = false;
            LdapReferralException nextReferralEx = null; 
    LdapReferralException(Name resolvedName,
        Object resolvedObj,
        Name remainingName,
        String explanation,
        Hashtable envprops,
        String nextName,
        int handleReferrals,
        Control[] reqCtls) {
        super(explanation);
        if (debug)
            System.out.println("LdapReferralException constructor");
        setResolvedName(resolvedName);
        setResolvedObj(resolvedObj);
        setRemainingName(remainingName);
        this.envprops = envprops;
        this.nextName = nextName;
        this.handleReferrals = handleReferrals;
        this.reqCtls =
            (handleReferrals == LdapClient.LDAP_REF_FOLLOW ? reqCtls : null);
    }
    public Context getReferralContext() throws NamingException {
        return getReferralContext(envprops, null);
    }
    public Context getReferralContext(Hashtable<?,?> newProps) throws
        NamingException {
        return getReferralContext(newProps, null);
    }
    public Context getReferralContext(Hashtable<?,?> newProps, Control[] connCtls)
        throws NamingException {
        if (debug)
            System.out.println("LdapReferralException.getReferralContext");
        LdapReferralContext refCtx = new LdapReferralContext(
            this, newProps, connCtls, reqCtls,
            nextName, skipThisReferral, handleReferrals);
        refCtx.setHopCount(hopCount + 1);
        if (skipThisReferral) {
            skipThisReferral = false; 
        }
        return (Context)refCtx;
    }
    public Object getReferralInfo() {
        if (debug) {
            System.out.println("LdapReferralException.getReferralInfo");
            System.out.println("  referralIndex=" + referralIndex);
        }
        if (hasMoreReferrals()) {
            return referrals.elementAt(referralIndex);
        } else {
            return null;
        }
    }
    public void retryReferral() {
        if (debug)
            System.out.println("LdapReferralException.retryReferral");
        if (referralIndex > 0)
            referralIndex--; 
    }
    public boolean skipReferral() {
        if (debug)
            System.out.println("LdapReferralException.skipReferral");
        skipThisReferral = true;
        try {
            getNextReferral();
        } catch (ReferralException e) {
        }
        return (hasMoreReferrals() || hasMoreReferralExceptions());
    }
    void setReferralInfo(Vector referrals, boolean continuationRef) {
        if (debug)
            System.out.println("LdapReferralException.setReferralInfo");
        this.referrals = referrals;
        if (referrals != null) {
            referralCount = referrals.size();
        }
        if (debug) {
            for (int i = 0; i < referralCount; i++) {
                System.out.println("  [" + i + "] " + referrals.elementAt(i));
            }
        }
    }
    String getNextReferral() throws ReferralException {
        if (debug)
            System.out.println("LdapReferralException.getNextReferral");
        if (hasMoreReferrals()) {
            return (String)referrals.elementAt(referralIndex++);
        } else if (hasMoreReferralExceptions()) {
            throw nextReferralEx;
        } else {
            return null;
        }
    }
    LdapReferralException
        appendUnprocessedReferrals(LdapReferralException back) {
        if (debug) {
            System.out.println(
                "LdapReferralException.appendUnprocessedReferrals");
            dump();
            if (back != null) {
                back.dump();
            }
        }
        LdapReferralException front = this;
        if (! front.hasMoreReferrals()) {
            front = nextReferralEx; 
            if ((errorEx != null) && (front != null)) {
                front.setNamingException(errorEx); 
            }
        }
        if (this == back) {
            return front;
        }
        if ((back != null) && (! back.hasMoreReferrals())) {
            back = back.nextReferralEx; 
        }
        if (back == null) {
            return front;
        }
        LdapReferralException ptr = front;
        while (ptr.nextReferralEx != null) {
            ptr = ptr.nextReferralEx;
        }
        ptr.nextReferralEx = back; 
        return front;
    }
    boolean hasMoreReferrals() {
        if (debug)
            System.out.println("LdapReferralException.hasMoreReferrals");
        return (! foundEntry) && (referralIndex < referralCount);
    }
    boolean hasMoreReferralExceptions() {
        if (debug)
            System.out.println(
                "LdapReferralException.hasMoreReferralExceptions");
        return (nextReferralEx != null);
    }
    void setHopCount(int hopCount) {
        if (debug)
            System.out.println("LdapReferralException.setHopCount");
        this.hopCount = hopCount;
    }
    void setNameResolved(boolean resolved) {
        if (debug)
            System.out.println("LdapReferralException.setNameResolved");
        foundEntry = resolved;
    }
    void setNamingException(NamingException e) {
        if (debug)
            System.out.println("LdapReferralException.setNamingException");
        if (errorEx == null) {
            e.setRootCause(this); 
            errorEx = e;
        }
    }
    String getNewRdn() {
        if (debug)
            System.out.println("LdapReferralException.getNewRdn");
        return newRdn;
    }
    void setNewRdn(String newRdn) {
        if (debug)
            System.out.println("LdapReferralException.setNewRdn");
        this.newRdn = newRdn;
    }
    NamingException getNamingException() {
        if (debug)
            System.out.println("LdapReferralException.getNamingException");
        return errorEx;
    }
    void dump() {
        System.out.println();
        System.out.println("LdapReferralException.dump");
        LdapReferralException ptr = this;
        while (ptr != null) {
            ptr.dumpState();
            ptr = ptr.nextReferralEx;
        }
    }
    private void dumpState() {
        System.out.println("LdapReferralException.dumpState");
        System.out.println("  hashCode=" + hashCode());
        System.out.println("  foundEntry=" + foundEntry);
        System.out.println("  skipThisReferral=" + skipThisReferral);
        System.out.println("  referralIndex=" + referralIndex);
        if (referrals != null) {
            System.out.println("  referrals:");
            for (int i = 0; i < referralCount; i++) {
                System.out.println("    [" + i + "] " + referrals.elementAt(i));
            }
        } else {
            System.out.println("  referrals=null");
        }
        System.out.println("  errorEx=" + errorEx);
        if (nextReferralEx == null) {
            System.out.println("  nextRefEx=null");
        } else {
            System.out.println("  nextRefEx=" + nextReferralEx.hashCode());
        }
        System.out.println();
    }
}
