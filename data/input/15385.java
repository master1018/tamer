class COMMarkerSegment extends MarkerSegment {
    private static final String ENCODING = "ISO-8859-1";
    COMMarkerSegment(JPEGBuffer buffer) throws IOException {
        super(buffer);
        loadData(buffer);
    }
    COMMarkerSegment(String comment) {
        super(JPEG.COM);
        data = comment.getBytes(); 
    }
    COMMarkerSegment(Node node) throws IIOInvalidTreeException{
        super(JPEG.COM);
        if (node instanceof IIOMetadataNode) {
            IIOMetadataNode ourNode = (IIOMetadataNode) node;
            data = (byte []) ourNode.getUserObject();
        }
        if (data == null) {
            String comment =
                node.getAttributes().getNamedItem("comment").getNodeValue();
            if (comment != null) {
                data = comment.getBytes(); 
            } else {
                throw new IIOInvalidTreeException("Empty comment node!", node);
            }
        }
    }
    String getComment() {
        try {
            return new String (data, ENCODING);
        } catch (UnsupportedEncodingException e) {}  
        return null;
    }
    IIOMetadataNode getNativeNode() {
        IIOMetadataNode node = new IIOMetadataNode("com");
        node.setAttribute("comment", getComment());
        if (data != null) {
            node.setUserObject(data.clone());
        }
        return node;
    }
    void write(ImageOutputStream ios) throws IOException {
        length = 2 + data.length;
        writeTag(ios);
        ios.write(data);
    }
    void print() {
        printTag("COM");
        System.out.println("<" + getComment() + ">");
    }
}
