class DHTMarkerSegment extends MarkerSegment {
    List tables = new ArrayList();
    DHTMarkerSegment(boolean needFour) {
        super(JPEG.DHT);
        tables.add(new Htable(JPEGHuffmanTable.StdDCLuminance, true, 0));
        if (needFour) {
            tables.add(new Htable(JPEGHuffmanTable.StdDCChrominance, true, 1));
        }
        tables.add(new Htable(JPEGHuffmanTable.StdACLuminance, false, 0));
        if (needFour) {
            tables.add(new Htable(JPEGHuffmanTable.StdACChrominance, false, 1));
        }
    }
    DHTMarkerSegment(JPEGBuffer buffer) throws IOException {
        super(buffer);
        int count = length;
        while (count > 0) {
            Htable newGuy = new Htable(buffer);
            tables.add(newGuy);
            count -= 1 + 16 + newGuy.values.length;
        }
        buffer.bufAvail -= length;
    }
    DHTMarkerSegment(JPEGHuffmanTable[] dcTables,
                     JPEGHuffmanTable[] acTables) {
        super(JPEG.DHT);
        for (int i = 0; i < dcTables.length; i++) {
            tables.add(new Htable(dcTables[i], true, i));
        }
        for (int i = 0; i < acTables.length; i++) {
            tables.add(new Htable(acTables[i], false, i));
        }
    }
    DHTMarkerSegment(Node node) throws IIOInvalidTreeException {
        super(JPEG.DHT);
        NodeList children = node.getChildNodes();
        int size = children.getLength();
        if ((size < 1) || (size > 4)) {
            throw new IIOInvalidTreeException("Invalid DHT node", node);
        }
        for (int i = 0; i < size; i++) {
            tables.add(new Htable(children.item(i)));
        }
    }
    protected Object clone() {
        DHTMarkerSegment newGuy = (DHTMarkerSegment) super.clone();
        newGuy.tables = new ArrayList(tables.size());
        Iterator iter = tables.iterator();
        while (iter.hasNext()) {
            Htable table = (Htable) iter.next();
            newGuy.tables.add(table.clone());
        }
        return newGuy;
    }
    IIOMetadataNode getNativeNode() {
        IIOMetadataNode node = new IIOMetadataNode("dht");
        for (int i= 0; i<tables.size(); i++) {
            Htable table = (Htable) tables.get(i);
            node.appendChild(table.getNativeNode());
        }
        return node;
    }
    void write(ImageOutputStream ios) throws IOException {
    }
    void print() {
        printTag("DHT");
        System.out.println("Num tables: "
                           + Integer.toString(tables.size()));
        for (int i= 0; i<tables.size(); i++) {
            Htable table = (Htable) tables.get(i);
            table.print();
        }
        System.out.println();
    }
    Htable getHtableFromNode(Node node) throws IIOInvalidTreeException {
        return new Htable(node);
    }
    void addHtable(JPEGHuffmanTable table, boolean isDC, int id) {
        tables.add(new Htable(table, isDC, id));
    }
    class Htable implements Cloneable {
        int tableClass;  
        int tableID; 
        private static final int NUM_LENGTHS = 16;
        short [] numCodes = new short[NUM_LENGTHS];
        short [] values;
        Htable(JPEGBuffer buffer) {
            tableClass = buffer.buf[buffer.bufPtr] >>> 4;
            tableID = buffer.buf[buffer.bufPtr++] & 0xf;
            for (int i = 0; i < NUM_LENGTHS; i++) {
                numCodes[i] = (short) (buffer.buf[buffer.bufPtr++] & 0xff);
            }
            int numValues = 0;
            for (int i = 0; i < NUM_LENGTHS; i++) {
                numValues += numCodes[i];
            }
            values = new short[numValues];
            for (int i = 0; i < numValues; i++) {
                values[i] = (short) (buffer.buf[buffer.bufPtr++] & 0xff);
            }
        }
        Htable(JPEGHuffmanTable table, boolean isDC, int id) {
            tableClass = isDC ? 0 : 1;
            tableID = id;
            numCodes = table.getLengths();
            values = table.getValues();
        }
        Htable(Node node) throws IIOInvalidTreeException {
            if (node.getNodeName().equals("dhtable")) {
                NamedNodeMap attrs = node.getAttributes();
                int count = attrs.getLength();
                if (count != 2) {
                    throw new IIOInvalidTreeException
                        ("dhtable node must have 2 attributes", node);
                }
                tableClass = getAttributeValue(node, attrs, "class", 0, 1, true);
                tableID = getAttributeValue(node, attrs, "htableId", 0, 3, true);
                if (node instanceof IIOMetadataNode) {
                    IIOMetadataNode ourNode = (IIOMetadataNode) node;
                    JPEGHuffmanTable table =
                        (JPEGHuffmanTable) ourNode.getUserObject();
                    if (table == null) {
                        throw new IIOInvalidTreeException
                            ("dhtable node must have user object", node);
                    }
                    numCodes = table.getLengths();
                    values = table.getValues();
                } else {
                    throw new IIOInvalidTreeException
                        ("dhtable node must have user object", node);
                }
            } else {
                throw new IIOInvalidTreeException
                    ("Invalid node, expected dqtable", node);
            }
        }
        protected Object clone() {
            Htable newGuy = null;
            try {
                newGuy = (Htable) super.clone();
            } catch (CloneNotSupportedException e) {} 
            if (numCodes != null) {
                newGuy.numCodes = (short []) numCodes.clone();
            }
            if (values != null) {
                newGuy.values = (short []) values.clone();
            }
            return newGuy;
        }
        IIOMetadataNode getNativeNode() {
            IIOMetadataNode node = new IIOMetadataNode("dhtable");
            node.setAttribute("class", Integer.toString(tableClass));
            node.setAttribute("htableId", Integer.toString(tableID));
            node.setUserObject(new JPEGHuffmanTable(numCodes, values));
            return node;
        }
        void print() {
            System.out.println("Huffman Table");
            System.out.println("table class: "
                               + ((tableClass == 0) ? "DC":"AC"));
            System.out.println("table id: " + Integer.toString(tableID));
            (new JPEGHuffmanTable(numCodes, values)).toString();
        }
    }
}
