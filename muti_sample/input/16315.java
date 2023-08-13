class MarkerSegment implements Cloneable {
    protected static final int LENGTH_SIZE = 2; 
    int tag;      
    int length;    
    byte [] data = null;  
    boolean unknown = false; 
    MarkerSegment(JPEGBuffer buffer) throws IOException {
        buffer.loadBuf(3);  
        tag = buffer.buf[buffer.bufPtr++] & 0xff;
        length = (buffer.buf[buffer.bufPtr++] & 0xff) << 8;
        length |= buffer.buf[buffer.bufPtr++] & 0xff;
        length -= 2;  
        buffer.bufAvail -= 3;
        buffer.loadBuf(length);
    }
    MarkerSegment(int tag) {
        this.tag = tag;
        length = 0;
    }
    MarkerSegment(Node node) throws IIOInvalidTreeException {
        tag = getAttributeValue(node,
                                null,
                                "MarkerTag",
                                0, 255,
                                true);
        length = 0;
        if (node instanceof IIOMetadataNode) {
            IIOMetadataNode iioNode = (IIOMetadataNode) node;
            try {
                data = (byte []) iioNode.getUserObject();
            } catch (Exception e) {
                IIOInvalidTreeException newGuy =
                    new IIOInvalidTreeException
                    ("Can't get User Object", node);
                newGuy.initCause(e);
                throw newGuy;
            }
        } else {
            throw new IIOInvalidTreeException
                ("Node must have User Object", node);
        }
    }
    protected Object clone() {
        MarkerSegment newGuy = null;
        try {
            newGuy = (MarkerSegment) super.clone();
        } catch (CloneNotSupportedException e) {} 
        if (this.data != null) {
            newGuy.data = (byte[]) data.clone();
        }
        return newGuy;
    }
    void loadData(JPEGBuffer buffer) throws IOException {
        data = new byte[length];
        buffer.readData(data);
    }
    IIOMetadataNode getNativeNode() {
        IIOMetadataNode node = new IIOMetadataNode("unknown");
        node.setAttribute("MarkerTag", Integer.toString(tag));
        node.setUserObject(data);
        return node;
    }
    static int getAttributeValue(Node node,
                                 NamedNodeMap attrs,
                                 String name,
                                 int min,
                                 int max,
                                 boolean required)
        throws IIOInvalidTreeException {
        if (attrs == null) {
            attrs = node.getAttributes();
        }
        String valueString = attrs.getNamedItem(name).getNodeValue();
        int value = -1;
        if (valueString == null) {
            if (required) {
                throw new IIOInvalidTreeException
                    (name + " attribute not found", node);
            }
        } else {
              value = Integer.parseInt(valueString);
              if ((value < min) || (value > max)) {
                  throw new IIOInvalidTreeException
                      (name + " attribute out of range", node);
              }
        }
        return value;
    }
    void writeTag(ImageOutputStream ios) throws IOException {
        ios.write(0xff);
        ios.write(tag);
        write2bytes(ios, length);
    }
    void write(ImageOutputStream ios) throws IOException {
        length = 2 + ((data != null) ? data.length : 0);
        writeTag(ios);
        if (data != null) {
            ios.write(data);
        }
    }
    static void write2bytes(ImageOutputStream ios,
                            int value) throws IOException {
        ios.write((value >> 8) & 0xff);
        ios.write(value & 0xff);
    }
    void printTag(String prefix) {
        System.out.println(prefix + " marker segment - marker = 0x"
                           + Integer.toHexString(tag));
        System.out.println("length: " + length);
    }
    void print() {
        printTag("Unknown");
        if (length > 10) {
            System.out.print("First 5 bytes:");
            for (int i=0;i<5;i++) {
                System.out.print(" Ox"
                                 + Integer.toHexString((int)data[i]));
            }
            System.out.print("\nLast 5 bytes:");
            for (int i=data.length-5;i<data.length;i++) {
                System.out.print(" Ox"
                                 + Integer.toHexString((int)data[i]));
            }
        } else {
            System.out.print("Data:");
            for (int i=0;i<data.length;i++) {
                System.out.print(" Ox"
                                 + Integer.toHexString((int)data[i]));
            }
        }
        System.out.println();
    }
}
