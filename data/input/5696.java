class SOFMarkerSegment extends MarkerSegment {
    int samplePrecision;
    int numLines;
    int samplesPerLine;
    ComponentSpec [] componentSpecs;  
    SOFMarkerSegment(boolean wantProg,
                     boolean wantExtended,
                     boolean willSubsample,
                     byte[] componentIDs,
                     int numComponents) {
        super(wantProg ? JPEG.SOF2
              : wantExtended ? JPEG.SOF1
              : JPEG.SOF0);
        samplePrecision = 8;
        numLines = 0;
        samplesPerLine = 0;
        componentSpecs = new ComponentSpec[numComponents];
        for(int i = 0; i < numComponents; i++) {
            int factor = 1;
            int qsel = 0;
            if (willSubsample) {
                factor = 2;
                if ((i == 1) || (i == 2)) {
                    factor = 1;
                    qsel = 1;
                }
            }
            componentSpecs[i] = new ComponentSpec(componentIDs[i], factor, qsel);
        }
    }
    SOFMarkerSegment(JPEGBuffer buffer) throws IOException{
        super(buffer);
        samplePrecision = buffer.buf[buffer.bufPtr++];
        numLines = (buffer.buf[buffer.bufPtr++] & 0xff) << 8;
        numLines |= buffer.buf[buffer.bufPtr++] & 0xff;
        samplesPerLine = (buffer.buf[buffer.bufPtr++] & 0xff) << 8;
        samplesPerLine |= buffer.buf[buffer.bufPtr++] & 0xff;
        int numComponents = buffer.buf[buffer.bufPtr++];
        componentSpecs = new ComponentSpec [numComponents];
        for (int i = 0; i < numComponents; i++) {
            componentSpecs[i] = new ComponentSpec(buffer);
        }
        buffer.bufAvail -= length;
    }
    SOFMarkerSegment(Node node) throws IIOInvalidTreeException {
        super(JPEG.SOF0);
        samplePrecision = 8;
        numLines = 0;
        samplesPerLine = 0;
        updateFromNativeNode(node, true);
    }
    protected Object clone() {
        SOFMarkerSegment newGuy = (SOFMarkerSegment) super.clone();
        if (componentSpecs != null) {
            newGuy.componentSpecs = (ComponentSpec []) componentSpecs.clone();
            for (int i = 0; i < componentSpecs.length; i++) {
                newGuy.componentSpecs[i] =
                    (ComponentSpec) componentSpecs[i].clone();
            }
        }
        return newGuy;
    }
    IIOMetadataNode getNativeNode() {
        IIOMetadataNode node = new IIOMetadataNode("sof");
        node.setAttribute("process", Integer.toString(tag-JPEG.SOF0));
        node.setAttribute("samplePrecision",
                          Integer.toString(samplePrecision));
        node.setAttribute("numLines",
                          Integer.toString(numLines));
        node.setAttribute("samplesPerLine",
                          Integer.toString(samplesPerLine));
        node.setAttribute("numFrameComponents",
                          Integer.toString(componentSpecs.length));
        for (int i = 0; i < componentSpecs.length; i++) {
            node.appendChild(componentSpecs[i].getNativeNode());
        }
        return node;
    }
    void updateFromNativeNode(Node node, boolean fromScratch)
        throws IIOInvalidTreeException {
        NamedNodeMap attrs = node.getAttributes();
        int value = getAttributeValue(node, attrs, "process", 0, 2, false);
        tag = (value != -1) ? value+JPEG.SOF0 : tag;
        value = getAttributeValue(node, attrs, "samplePrecision", 8, 8, false);
        value = getAttributeValue(node, attrs, "numLines", 0, 65535, false);
        numLines = (value != -1) ? value : numLines;
        value = getAttributeValue(node, attrs, "samplesPerLine", 0, 65535, false);
        samplesPerLine = (value != -1) ? value : samplesPerLine;
        int numComponents = getAttributeValue(node, attrs, "numFrameComponents",
                                              1, 4, false);
        NodeList children = node.getChildNodes();
        if (children.getLength() != numComponents) {
            throw new IIOInvalidTreeException
                ("numFrameComponents must match number of children", node);
        }
        componentSpecs = new ComponentSpec [numComponents];
        for (int i = 0; i < numComponents; i++) {
            componentSpecs[i] = new ComponentSpec(children.item(i));
        }
    }
    void write(ImageOutputStream ios) throws IOException {
    }
    void print () {
        printTag("SOF");
        System.out.print("Sample precision: ");
        System.out.println(samplePrecision);
        System.out.print("Number of lines: ");
        System.out.println(numLines);
        System.out.print("Samples per line: ");
        System.out.println(samplesPerLine);
        System.out.print("Number of components: ");
        System.out.println(componentSpecs.length);
        for(int i = 0; i<componentSpecs.length; i++) {
            componentSpecs[i].print();
        }
    }
    int getIDencodedCSType () {
        for (int i = 0; i < componentSpecs.length; i++) {
            if (componentSpecs[i].componentId < 'A') {
                return JPEG.JCS_UNKNOWN;
            }
        }
        switch(componentSpecs.length) {
        case 3:
            if ((componentSpecs[0].componentId == 'R')
                &&(componentSpecs[0].componentId == 'G')
                &&(componentSpecs[0].componentId == 'B')) {
                return JPEG.JCS_RGB;
            }
            if ((componentSpecs[0].componentId == 'Y')
                &&(componentSpecs[0].componentId == 'C')
                &&(componentSpecs[0].componentId == 'c')) {
                return JPEG.JCS_YCC;
            }
            break;
        case 4:
            if ((componentSpecs[0].componentId == 'R')
                &&(componentSpecs[0].componentId == 'G')
                &&(componentSpecs[0].componentId == 'B')
                &&(componentSpecs[0].componentId == 'A')) {
                return JPEG.JCS_RGBA;
            }
            if ((componentSpecs[0].componentId == 'Y')
                &&(componentSpecs[0].componentId == 'C')
                &&(componentSpecs[0].componentId == 'c')
                &&(componentSpecs[0].componentId == 'A')) {
                return JPEG.JCS_YCCA;
            }
        }
        return JPEG.JCS_UNKNOWN;
    }
    ComponentSpec getComponentSpec(byte id, int factor, int qSelector) {
        return new ComponentSpec(id, factor, qSelector);
    }
    class ComponentSpec implements Cloneable {
        int componentId;
        int HsamplingFactor;
        int VsamplingFactor;
        int QtableSelector;
        ComponentSpec(byte id, int factor, int qSelector) {
            componentId = id;
            HsamplingFactor = factor;
            VsamplingFactor = factor;
            QtableSelector = qSelector;
        }
        ComponentSpec(JPEGBuffer buffer) {
            componentId = buffer.buf[buffer.bufPtr++];
            HsamplingFactor = buffer.buf[buffer.bufPtr] >>> 4;
            VsamplingFactor = buffer.buf[buffer.bufPtr++] & 0xf;
            QtableSelector = buffer.buf[buffer.bufPtr++];
        }
        ComponentSpec(Node node) throws IIOInvalidTreeException {
            NamedNodeMap attrs = node.getAttributes();
            componentId = getAttributeValue(node, attrs, "componentId", 0, 255, true);
            HsamplingFactor = getAttributeValue(node, attrs, "HsamplingFactor",
                                                1, 255, true);
            VsamplingFactor = getAttributeValue(node, attrs, "VsamplingFactor",
                                                1, 255, true);
            QtableSelector = getAttributeValue(node, attrs, "QtableSelector",
                                               0, 3, true);
        }
        protected Object clone() {
            try {
                return super.clone();
            } catch (CloneNotSupportedException e) {} 
            return null;
        }
        IIOMetadataNode getNativeNode() {
            IIOMetadataNode node = new IIOMetadataNode("componentSpec");
            node.setAttribute("componentId",
                              Integer.toString(componentId));
            node.setAttribute("HsamplingFactor",
                              Integer.toString(HsamplingFactor));
            node.setAttribute("VsamplingFactor",
                              Integer.toString(VsamplingFactor));
            node.setAttribute("QtableSelector",
                              Integer.toString(QtableSelector));
            return node;
        }
        void print () {
            System.out.print("Component ID: ");
            System.out.println(componentId);
            System.out.print("H sampling factor: ");
            System.out.println(HsamplingFactor);
            System.out.print("V sampling factor: ");
            System.out.println(VsamplingFactor);
            System.out.print("Q table selector: ");
            System.out.println(QtableSelector);
        }
    }
}
