final public class ManageReferralControl extends BasicControl {
    public static final String OID = "2.16.840.1.113730.3.4.2";
    private static final long serialVersionUID = 909382692585717224L;
    public ManageReferralControl() {
        super(OID, true, null);
    }
    public ManageReferralControl(boolean criticality) {
        super(OID, criticality, null);
    }
}
