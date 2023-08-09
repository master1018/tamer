public class WbxmlPrimitiveSerializer implements PrimitiveSerializer {
    private String mVersionNs;
    private String mTransacNs;
    public WbxmlPrimitiveSerializer(ImpsVersion impsVersion, String versionNs,
            String transacNs) {
        mVersionNs = versionNs;
        mTransacNs = transacNs;
        mWbxmlSerializer = new WbxmlSerializer(impsVersion);
    }
    private WbxmlSerializer mWbxmlSerializer;
    public void serialize(Primitive primitive, OutputStream out) throws IOException,
            SerializerException {
        mWbxmlSerializer.reset();
        mWbxmlSerializer.setOutput(out);
        PrimitiveElement elem = primitive.createMessage(mVersionNs, mTransacNs);
        writeElement(elem);
    }
    private void writeElement(PrimitiveElement element) throws IOException,
            SerializerException {
        String name = element.getTagName();
        String[] atts = null;
        Map<String, String> attrMap = element.getAttributes();
        if(attrMap != null && attrMap.size() > 0) {
            atts = new String[attrMap.size() * 2];
            int index = 0;
            for (Map.Entry<String, String> entry : attrMap.entrySet()) {
                atts[index++] = entry.getKey();
                atts[index++] = entry.getValue();
            }
        }
        mWbxmlSerializer.startElement(name, atts);
        String contents = element.getContents();
        if(contents != null) {
            mWbxmlSerializer.characters(contents);
        }
        if(element.getChildCount() > 0) {
            for(PrimitiveElement child : element.getChildren()) {
                writeElement(child);
            }
        }
        mWbxmlSerializer.endElement();
    }
}
