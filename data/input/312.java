public class AccessDecisionShow extends AbsAccessDecision {
    private static Logger cat = Logger.getLogger(AccessDecisionShow.class.getName());
    public AccessDecisionShow() {
    }
    public boolean access_allowed(String operation) {
        cat.debug("Access check for operation : " + operation);
        Properties p = ServiceFactory.getSSLService().getClientData();
        NamedValues nval = new NamedValues(p);
        cat.debug("Client is :" + nval.toString());
        return true;
    }
}
