public class CEchoSCP extends MockServer {
    public class MyAssociateRequestHandler extends DefaultAssociateRequestHandler {
        @Override
        public AssociateResponse requestReceived(Association aAssociation, AssociateRequest aAssociateRequest) {
            AssociateResponse lReponse = createDefaultResponse(aAssociateRequest);
            Iterator<PresentationContextItem> lPres = aAssociateRequest.getPresentationIterator();
            while (lPres.hasNext()) {
                PresentationContextItemRQ lPresRQ = (PresentationContextItemRQ) lPres.next();
                if (lPresRQ.getAbstractSyntax().equals(SOPClass.Verification.getUID())) {
                    lReponse.addPresentationContext(lPresRQ.getID(), NetworkStaticProperties.PresentationContextReasons.ACCEPTANCE, lPresRQ.getTransferSyntax(0));
                } else {
                    lReponse.addPresentationContext(lPresRQ.getID(), NetworkStaticProperties.PresentationContextReasons.USER_REJECTION, lPresRQ.getTransferSyntax(0));
                }
            }
            return lReponse;
        }
    }
    public class MyDimseServicesManager implements DimseServicesManager {
        public DimseService borrowService(Association aAssociation, AbstractDimseMessage aMessage) throws Exception {
            if (aMessage.isCEchoRequest()) {
                return new VerificationSCPService();
            } else {
                return null;
            }
        }
        public void returnService(DimseService aService) {
            aService = null;
        }
    }
    public CEchoSCP(WorkerHandler aWorkerHandler) {
        super(aWorkerHandler);
    }
    @Override
    public void start() throws Exception {
        DimseServiceBroker lBroker = new DimseServiceBroker();
        lBroker.setDimseServicesManager(new MyDimseServicesManager());
        AssociateRequestHandler = new MyAssociateRequestHandler();
        AssociationListener = lBroker;
        super.start();
    }
}
