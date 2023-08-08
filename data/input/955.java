public class BpaSession extends Session {
    private BpaNetworkLink networkLink = null;
    public BpaSession(BpaNetworkLink theNetwork, String traceFileName, int traceLevel) throws BpsStartException {
        super(theNetwork.getUserId(), theNetwork.getPassword(), theNetwork.getHostUrl());
        boolean networkProblem = false;
        setNetworkLink(theNetwork);
        try {
            bnsInitialize();
            if (traceLevel > 0) traceOn(traceFileName + ".Bpa.log", traceLevel);
        } catch (BsiException e) {
            networkProblem = (e.getClass().getName()).equals("epo.bsi.BsiCommunicationException");
            if (!networkProblem) {
                throw new BpsStartException(BpsException.ERR_INST_BSI_SESSION, "BSI Rc = " + e.getReturnCode() + " Mess. : " + e.getMessage());
            } else {
            }
        }
    }
    public BpaNetworkLink getNetworkLink() {
        return networkLink;
    }
    private void setNetworkLink(BpaNetworkLink newValue) {
        this.networkLink = newValue;
    }
}
