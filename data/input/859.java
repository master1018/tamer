public class VerificationPanel extends AbstractSCUPanel {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(VerificationPanel.class);
    private AssociationEventsPanel statusPanel = new AssociationEventsPanel();
    private EchoQueryRunnerListener listener = new EchoQueryRunnerListener();
    public VerificationPanel(UIController controller) {
        super(controller);
        setRemoteDevicePanelVisible(true);
        getRemoteDevicePanel().connectButton.setVisible(false);
        getRemoteDevicePanel().disconnectButton.setVisible(false);
        setEchoActionListener(new EchoActionListener());
        add(getTopPanel(), BorderLayout.NORTH);
        getConnector().getConfiguration().setMonitor(statusPanel);
        add(statusPanel, BorderLayout.CENTER);
    }
    private class EchoActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            try {
                if (checkConnectSettings()) {
                    getController().startLongPerform(true, "");
                    statusPanel.addInformation(I18n.getTrans(I18n.getTrans("performingCEcho")));
                    openAssocation(createAssociateRequest(), getAssociationListener());
                }
            } catch (Exception ex) {
                getController().stopLongPerform();
                statusPanel.addError(ex.getMessage());
            }
        }
    }
    private class EchoQueryRunnerListener extends DefaultAssociationListener {
        @Override
        public void associationOpened(Association association, AssociateSession associateSession) throws Exception {
            super.associationOpened(association, associateSession);
            CEchoRequestMessage message = association.getMessageFactory().newCEchoRequest();
            logger.info("CEchoRequestMessage: \n " + message.toString());
            byte presID = associateSession.getSuitablePresentationContextID(SOPClass.Verification);
            association.sendMessage(presID, message);
        }
        @Override
        public void messageReceived(Association association, byte presentationContextID, DimseMessage message) throws Exception {
            super.messageReceived(association, presentationContextID, message);
            if (message instanceof CEchoResponseMessage) {
                if (((CEchoResponseMessage) message).isSuccess()) {
                    statusPanel.addInformation(I18n.getTrans("echoSucceeded"));
                }
            } else {
                statusPanel.addError(I18n.getTrans("echoFailed"));
            }
            getController().stopLongPerform();
        }
        public void exceptionCaught(Association association, Throwable cause) {
            statusPanel.addError(UserMessages.getNetworkError(I18n.getTrans("echo")));
            getController().stopLongPerform();
        }
    }
    @Override
    protected AssociateRequest createAssociateRequest() throws Exception {
        return getConnector().getAssociatePrimitiveFactory().newVerificationQuery();
    }
    @Override
    protected AssociationListener getAssociationListener() {
        return listener;
    }
}
