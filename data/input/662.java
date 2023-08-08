public class SupportedWindowStatesContentAssistProcessor extends AbstractGenericXmlContentAssistProcessor<ICustomWindowState> {
    static class BuildInWindowState implements ICustomWindowState {
        private String name;
        private String description;
        public BuildInWindowState(String name, String description) {
            super();
            this.name = name;
            this.description = description;
        }
        public void addDescription(Locale locale, String description) {
        }
        public String getDescription(Locale locale) {
            return this.description;
        }
        public String getId() {
            return null;
        }
        public String getName() {
            return this.name;
        }
        public void removeDescription(Locale locale) {
        }
        public void setId(String id) {
        }
        public void setName(String name) throws IllegalArgumentException {
        }
    }
    public SupportedWindowStatesContentAssistProcessor() {
        super(Constants.XML_DESCRIPTOR_ELEMENT_PORTLET_SUPPORTS_WINDOWSTATE);
    }
    @Override
    protected List<ICustomWindowState> computeElementsProposal(IDocument document) {
        IPortletArtifact artifact = SSEUtil.getPortletArtifact(document);
        List<ICustomWindowState> returnValue = new Vector<ICustomWindowState>();
        returnValue.add(new BuildInWindowState("NORMAL", "Build-in window state"));
        returnValue.add(new BuildInWindowState("MAXIMIZED", "Build-in window state"));
        returnValue.add(new BuildInWindowState("MINIMIZED", "Build-in window state"));
        if (artifact != null) {
            returnValue.addAll(artifact.getPortletApplication().getCustomWindowStates());
        }
        return returnValue;
    }
    @Override
    protected String getAdditionalInformation(ICustomWindowState element) {
        String description = element.getDescription(null);
        if (description == null) description = "";
        String message;
        if (element instanceof BuildInWindowState) {
            message = Messages.sse_contentassist_supportedwindowstate_buildin_additionalInformation;
        } else {
            message = Messages.sse_contentassist_supportedwindowstate_custom_additionalInformation;
        }
        return NLS.bind(message, element.getName(), description);
    }
    @Override
    protected String getDisplayString(ICustomWindowState element) {
        String message;
        if (element instanceof BuildInWindowState) {
            message = Messages.sse_contentassist_supportedwindowstate_buildin_displayString;
        } else {
            message = Messages.sse_contentassist_supportedwindowstate_custom_displayString;
        }
        return NLS.bind(message, element.getName());
    }
    @Override
    protected Image getImage(ICustomWindowState element) {
        return PortletImageRegistry.getImage(ImageType.ICON, ImageConstants.PORTLET_CUSTOMWINDOWSTATE);
    }
    @Override
    protected String getReplacement(ICustomWindowState element) {
        return element.getName();
    }
}
