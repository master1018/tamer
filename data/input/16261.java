public final class LdapResult {
    int msgId;
    public int status;                  
    String matchedDN;
    String errorMessage;
    Vector referrals = null;
    LdapReferralException refEx = null;
    Vector entries = null;
    Vector resControls = null;
    public byte[] serverCreds = null;   
    String extensionId = null;          
    byte[] extensionValue = null;       
    boolean compareToSearchResult(String name) {
        boolean successful = false;
        switch (status) {
            case LdapClient.LDAP_COMPARE_TRUE:
                status = LdapClient.LDAP_SUCCESS;
                entries = new Vector(1,1);
                Attributes attrs = new BasicAttributes(LdapClient.caseIgnore);
                LdapEntry entry = new LdapEntry( name, attrs );
                entries.addElement(entry);
                successful = true;
                break;
            case LdapClient.LDAP_COMPARE_FALSE:
                status = LdapClient.LDAP_SUCCESS;
                entries = new Vector(0);
                successful = true;
                break;
            default:
                successful = false;
                break;
        }
        return successful;
    }
}
