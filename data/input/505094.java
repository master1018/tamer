public class BooleanAttributeDescriptor extends ListAttributeDescriptor {
    public BooleanAttributeDescriptor(String xmlLocalName, String uiName, String nsUri,
            String tooltip) {
        super(xmlLocalName, uiName, nsUri, tooltip,
                new String[] { "true", "false" } );
    }
}
