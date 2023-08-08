public class ATCreateActivityResultContent implements IOperationContent, Serializable {
    private static final long serialVersionUID = 1L;
    private String coordEPR;
    private String coordContext;
    public ATCreateActivityResultContent(String coordEPR, OMElement coordContext) {
        this.coordContext = coordContext.toString();
        this.coordEPR = coordEPR;
    }
    public String getCoordEPR() {
        return coordEPR;
    }
    public OMElement getCoordContext() {
        OMElement returnValue = null;
        try {
            returnValue = AXIOMUtil.stringToOM(coordContext);
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        return returnValue;
    }
}
