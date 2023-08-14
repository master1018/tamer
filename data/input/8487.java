class DRIMarkerSegment extends MarkerSegment {
    int restartInterval = 0;
    DRIMarkerSegment(JPEGBuffer buffer)
        throws IOException {
        super(buffer);
        restartInterval = (buffer.buf[buffer.bufPtr++] & 0xff) << 8;
        restartInterval |= buffer.buf[buffer.bufPtr++] & 0xff;
        buffer.bufAvail -= length;
    }
    DRIMarkerSegment(Node node) throws IIOInvalidTreeException {
        super(JPEG.DRI);
        updateFromNativeNode(node, true);
    }
    IIOMetadataNode getNativeNode() {
        IIOMetadataNode node = new IIOMetadataNode("dri");
        node.setAttribute("interval", Integer.toString(restartInterval));
        return node;
    }
    void updateFromNativeNode(Node node, boolean fromScratch)
        throws IIOInvalidTreeException {
        restartInterval = getAttributeValue(node, null, "interval",
                                            0, 65535, true);
    }
    void write(ImageOutputStream ios) throws IOException {
    }
    void print() {
        printTag("DRI");
        System.out.println("Interval: "
                           + Integer.toString(restartInterval));
    }
}
