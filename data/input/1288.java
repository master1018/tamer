public class RP0130 extends WSRPMessageAssertionProcess {
    public RP0130(BaseMessageValidator impl) {
        super(impl);
    }
    public AssertionResult validate(TestAssertion testAssertion, EntryContext entryContext) throws WSIException {
        if (validator.isOneWayResponse(entryContext)) {
            na();
        } else {
            try {
                Document respDoc = entryContext.getResponseDocument();
                NodeList markupTypesNodes = NodeUtils.getNodes(respDoc, "markupTypes");
                int nodeCount = markupTypesNodes.getLength();
                int wildCardCount = 0;
                if (nodeCount > 0) {
                    String mimeType;
                    for (int ii = 0; ii < nodeCount; ii++) {
                        mimeType = NodeUtils.getTextNode(markupTypesNodes.item(ii), "mimeType").getNodeValue();
                        if (mimeType.indexOf("*") != -1) {
                            wildCardCount++;
                        }
                    }
                    if (wildCardCount > 0) {
                        warn("The Producer used wildcard in " + wildCardCount + " MimeTypes");
                    } else {
                        pass();
                    }
                } else {
                    na();
                }
            } catch (TransformerException te) {
                te.printStackTrace();
                fail(te.getMessage());
            }
        }
        return createAssertionResult(testAssertion, result, failureDetailMessage);
    }
}
