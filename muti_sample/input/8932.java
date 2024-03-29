class JFIFMarkerSegment extends MarkerSegment {
    int majorVersion;
    int minorVersion;
    int resUnits;
    int Xdensity;
    int Ydensity;
    int thumbWidth;
    int thumbHeight;
    JFIFThumbRGB thumb = null;  
    ArrayList extSegments = new ArrayList();
    ICCMarkerSegment iccSegment = null; 
    private static final int THUMB_JPEG = 0x10;
    private static final int THUMB_PALETTE = 0x11;
    private static final int THUMB_UNASSIGNED = 0x12;
    private static final int THUMB_RGB = 0x13;
    private static final int DATA_SIZE = 14;
    private static final int ID_SIZE = 5;
    private final int MAX_THUMB_WIDTH = 255;
    private final int MAX_THUMB_HEIGHT = 255;
    private final boolean debug = false;
    private boolean inICC = false;
    private ICCMarkerSegment tempICCSegment = null;
    JFIFMarkerSegment() {
        super(JPEG.APP0);
        majorVersion = 1;
        minorVersion = 2;
        resUnits = JPEG.DENSITY_UNIT_ASPECT_RATIO;
        Xdensity = 1;
        Ydensity = 1;
        thumbWidth = 0;
        thumbHeight = 0;
    }
    JFIFMarkerSegment(JPEGBuffer buffer) throws IOException {
        super(buffer);
        buffer.bufPtr += ID_SIZE;  
        majorVersion = buffer.buf[buffer.bufPtr++];
        minorVersion = buffer.buf[buffer.bufPtr++];
        resUnits = buffer.buf[buffer.bufPtr++];
        Xdensity = (buffer.buf[buffer.bufPtr++] & 0xff) << 8;
        Xdensity |= buffer.buf[buffer.bufPtr++] & 0xff;
        Ydensity = (buffer.buf[buffer.bufPtr++] & 0xff) << 8;
        Ydensity |= buffer.buf[buffer.bufPtr++] & 0xff;
        thumbWidth = buffer.buf[buffer.bufPtr++] & 0xff;
        thumbHeight = buffer.buf[buffer.bufPtr++] & 0xff;
        buffer.bufAvail -= DATA_SIZE;
        if (thumbWidth > 0) {
            thumb = new JFIFThumbRGB(buffer, thumbWidth, thumbHeight);
        }
    }
    JFIFMarkerSegment(Node node) throws IIOInvalidTreeException {
        this();
        updateFromNativeNode(node, true);
    }
    protected Object clone() {
        JFIFMarkerSegment newGuy = (JFIFMarkerSegment) super.clone();
        if (!extSegments.isEmpty()) { 
            newGuy.extSegments = new ArrayList();
            for (Iterator iter = extSegments.iterator(); iter.hasNext();) {
                JFIFExtensionMarkerSegment jfxx =
                    (JFIFExtensionMarkerSegment) iter.next();
                newGuy.extSegments.add(jfxx.clone());
            }
        }
        if (iccSegment != null) {
            newGuy.iccSegment = (ICCMarkerSegment) iccSegment.clone();
        }
        return newGuy;
    }
    void addJFXX(JPEGBuffer buffer, JPEGImageReader reader)
        throws IOException {
        extSegments.add(new JFIFExtensionMarkerSegment(buffer, reader));
    }
    void addICC(JPEGBuffer buffer) throws IOException {
        if (inICC == false) {
            if (iccSegment != null) {
                throw new IIOException
                    ("> 1 ICC APP2 Marker Segment not supported");
            }
            tempICCSegment = new ICCMarkerSegment(buffer);
            if (inICC == false) { 
                iccSegment = tempICCSegment;
                tempICCSegment = null;
            }
        } else {
            if (tempICCSegment.addData(buffer) == true) {
                iccSegment = tempICCSegment;
                tempICCSegment = null;
            }
        }
    }
    void addICC(ICC_ColorSpace cs) throws IOException {
        if (iccSegment != null) {
            throw new IIOException
                ("> 1 ICC APP2 Marker Segment not supported");
        }
        iccSegment = new ICCMarkerSegment(cs);
    }
    IIOMetadataNode getNativeNode() {
        IIOMetadataNode node = new IIOMetadataNode("app0JFIF");
        node.setAttribute("majorVersion", Integer.toString(majorVersion));
        node.setAttribute("minorVersion", Integer.toString(minorVersion));
        node.setAttribute("resUnits", Integer.toString(resUnits));
        node.setAttribute("Xdensity", Integer.toString(Xdensity));
        node.setAttribute("Ydensity", Integer.toString(Ydensity));
        node.setAttribute("thumbWidth", Integer.toString(thumbWidth));
        node.setAttribute("thumbHeight", Integer.toString(thumbHeight));
        if (!extSegments.isEmpty()) {
            IIOMetadataNode JFXXnode = new IIOMetadataNode("JFXX");
            node.appendChild(JFXXnode);
            for (Iterator iter = extSegments.iterator(); iter.hasNext();) {
                JFIFExtensionMarkerSegment seg =
                    (JFIFExtensionMarkerSegment) iter.next();
                JFXXnode.appendChild(seg.getNativeNode());
            }
        }
        if (iccSegment != null) {
            node.appendChild(iccSegment.getNativeNode());
        }
        return node;
    }
    void updateFromNativeNode(Node node, boolean fromScratch)
        throws IIOInvalidTreeException {
        NamedNodeMap attrs = node.getAttributes();
        if (attrs.getLength() > 0) {
            int value = getAttributeValue(node, attrs, "majorVersion",
                                          0, 255, false);
            majorVersion = (value != -1) ? value : majorVersion;
            value = getAttributeValue(node, attrs, "minorVersion",
                                      0, 255, false);
            minorVersion = (value != -1) ? value : minorVersion;
            value = getAttributeValue(node, attrs, "resUnits", 0, 2, false);
            resUnits = (value != -1) ? value : resUnits;
            value = getAttributeValue(node, attrs, "Xdensity", 1, 65535, false);
            Xdensity = (value != -1) ? value : Xdensity;
            value = getAttributeValue(node, attrs, "Ydensity", 1, 65535, false);
            Ydensity = (value != -1) ? value : Ydensity;
            value = getAttributeValue(node, attrs, "thumbWidth", 0, 255, false);
            thumbWidth = (value != -1) ? value : thumbWidth;
            value = getAttributeValue(node, attrs, "thumbHeight", 0, 255, false);
            thumbHeight = (value != -1) ? value : thumbHeight;
        }
        if (node.hasChildNodes()) {
            NodeList children = node.getChildNodes();
            int count = children.getLength();
            if (count > 2) {
                throw new IIOInvalidTreeException
                    ("app0JFIF node cannot have > 2 children", node);
            }
            for (int i = 0; i < count; i++) {
                Node child = children.item(i);
                String name = child.getNodeName();
                if (name.equals("JFXX")) {
                    if ((!extSegments.isEmpty()) && fromScratch) {
                        throw new IIOInvalidTreeException
                            ("app0JFIF node cannot have > 1 JFXX node", node);
                    }
                    NodeList exts = child.getChildNodes();
                    int extCount = exts.getLength();
                    for (int j = 0; j < extCount; j++) {
                        Node ext = exts.item(j);
                        extSegments.add(new JFIFExtensionMarkerSegment(ext));
                    }
                }
                if (name.equals("app2ICC")) {
                    if ((iccSegment != null) && fromScratch) {
                        throw new IIOInvalidTreeException
                            ("> 1 ICC APP2 Marker Segment not supported", node);
                    }
                    iccSegment = new ICCMarkerSegment(child);
                }
            }
        }
    }
    int getThumbnailWidth(int index) {
        if (thumb != null) {
            if (index == 0) {
                return thumb.getWidth();
            }
            index--;
        }
        JFIFExtensionMarkerSegment jfxx =
            (JFIFExtensionMarkerSegment) extSegments.get(index);
        return jfxx.thumb.getWidth();
    }
    int getThumbnailHeight(int index) {
        if (thumb != null) {
            if (index == 0) {
                return thumb.getHeight();
            }
            index--;
        }
        JFIFExtensionMarkerSegment jfxx =
            (JFIFExtensionMarkerSegment) extSegments.get(index);
        return jfxx.thumb.getHeight();
    }
    BufferedImage getThumbnail(ImageInputStream iis,
                               int index,
                               JPEGImageReader reader) throws IOException {
        reader.thumbnailStarted(index);
        BufferedImage ret = null;
        if ((thumb != null) && (index == 0)) {
                ret = thumb.getThumbnail(iis, reader);
        } else {
            if (thumb != null) {
                index--;
            }
            JFIFExtensionMarkerSegment jfxx =
                (JFIFExtensionMarkerSegment) extSegments.get(index);
            ret = jfxx.thumb.getThumbnail(iis, reader);
        }
        reader.thumbnailComplete();
        return ret;
    }
    void write(ImageOutputStream ios,
               JPEGImageWriter writer) throws IOException {
        write(ios, null, writer);
    }
    void write(ImageOutputStream ios,
               BufferedImage thumb,
               JPEGImageWriter writer) throws IOException {
        int thumbWidth = 0;
        int thumbHeight = 0;
        int thumbLength = 0;
        int [] thumbData = null;
        if (thumb != null) {
            thumbWidth = thumb.getWidth();
            thumbHeight = thumb.getHeight();
            if ((thumbWidth > MAX_THUMB_WIDTH)
                || (thumbHeight > MAX_THUMB_HEIGHT)) {
                writer.warningOccurred(JPEGImageWriter.WARNING_THUMB_CLIPPED);
            }
            thumbWidth = Math.min(thumbWidth, MAX_THUMB_WIDTH);
            thumbHeight = Math.min(thumbHeight, MAX_THUMB_HEIGHT);
            thumbData = thumb.getRaster().getPixels(0, 0,
                                                    thumbWidth, thumbHeight,
                                                    (int []) null);
            thumbLength = thumbData.length;
        }
        length = DATA_SIZE + LENGTH_SIZE + thumbLength;
        writeTag(ios);
        byte [] id = {0x4A, 0x46, 0x49, 0x46, 0x00};
        ios.write(id);
        ios.write(majorVersion);
        ios.write(minorVersion);
        ios.write(resUnits);
        write2bytes(ios, Xdensity);
        write2bytes(ios, Ydensity);
        ios.write(thumbWidth);
        ios.write(thumbHeight);
        if (thumbData != null) {
            writer.thumbnailStarted(0);
            writeThumbnailData(ios, thumbData, writer);
            writer.thumbnailComplete();
        }
    }
    void writeThumbnailData(ImageOutputStream ios,
                            int [] thumbData,
                            JPEGImageWriter writer) throws IOException {
        int progInterval = thumbData.length / 20;  
        if (progInterval == 0) {
            progInterval = 1;
        }
        for (int i = 0; i < thumbData.length; i++) {
            ios.write(thumbData[i]);
            if ((i > progInterval) && (i % progInterval == 0)) {
                writer.thumbnailProgress
                    (((float) i * 100) / ((float) thumbData.length));
            }
        }
    }
    void writeWithThumbs(ImageOutputStream ios,
                         List thumbnails,
                         JPEGImageWriter writer) throws IOException {
        if (thumbnails != null) {
            JFIFExtensionMarkerSegment jfxx = null;
            if (thumbnails.size() == 1) {
                if (!extSegments.isEmpty()) {
                    jfxx = (JFIFExtensionMarkerSegment) extSegments.get(0);
                }
                writeThumb(ios,
                           (BufferedImage) thumbnails.get(0),
                           jfxx,
                           0,
                           true,
                           writer);
            } else {
                write(ios, writer);  
                for (int i = 0; i < thumbnails.size(); i++) {
                    jfxx = null;
                    if (i < extSegments.size()) {
                        jfxx = (JFIFExtensionMarkerSegment) extSegments.get(i);
                    }
                    writeThumb(ios,
                               (BufferedImage) thumbnails.get(i),
                               jfxx,
                               i,
                               false,
                               writer);
                }
            }
        } else {  
            write(ios, writer);
        }
    }
    private void writeThumb(ImageOutputStream ios,
                            BufferedImage thumb,
                            JFIFExtensionMarkerSegment jfxx,
                            int index,
                            boolean onlyOne,
                            JPEGImageWriter writer) throws IOException {
        ColorModel cm = thumb.getColorModel();
        ColorSpace cs = cm.getColorSpace();
        if (cm instanceof IndexColorModel) {
            if (onlyOne) {
                write(ios, writer);
            }
            if ((jfxx == null)
                || (jfxx.code == THUMB_PALETTE)) {
                writeJFXXSegment(index, thumb, ios, writer); 
            } else {
                BufferedImage thumbRGB =
                    ((IndexColorModel) cm).convertToIntDiscrete
                    (thumb.getRaster(), false);
                jfxx.setThumbnail(thumbRGB);
                writer.thumbnailStarted(index);
                jfxx.write(ios, writer);  
                writer.thumbnailComplete();
            }
        } else if (cs.getType() == ColorSpace.TYPE_RGB) {
            if (jfxx == null) {
                if (onlyOne) {
                    write(ios, thumb, writer); 
                } else {
                    writeJFXXSegment(index, thumb, ios, writer); 
                }
            } else {
                if (onlyOne) {
                    write(ios, writer);
                }
                if (jfxx.code == THUMB_PALETTE) {
                    writeJFXXSegment(index, thumb, ios, writer); 
                    writer.warningOccurred
                        (JPEGImageWriter.WARNING_NO_RGB_THUMB_AS_INDEXED);
                } else {
                    jfxx.setThumbnail(thumb);
                    writer.thumbnailStarted(index);
                    jfxx.write(ios, writer);  
                    writer.thumbnailComplete();
                }
            }
        } else if (cs.getType() == ColorSpace.TYPE_GRAY) {
            if (jfxx == null) {
                if (onlyOne) {
                    BufferedImage thumbRGB = expandGrayThumb(thumb);
                    write(ios, thumbRGB, writer); 
                } else {
                    writeJFXXSegment(index, thumb, ios, writer); 
                }
            } else {
                if (onlyOne) {
                    write(ios, writer);
                }
                if (jfxx.code == THUMB_RGB) {
                    BufferedImage thumbRGB = expandGrayThumb(thumb);
                    writeJFXXSegment(index, thumbRGB, ios, writer);
                } else if (jfxx.code == THUMB_JPEG) {
                    jfxx.setThumbnail(thumb);
                    writer.thumbnailStarted(index);
                    jfxx.write(ios, writer);  
                    writer.thumbnailComplete();
                } else if (jfxx.code == THUMB_PALETTE) {
                    writeJFXXSegment(index, thumb, ios, writer); 
                    writer.warningOccurred
                        (JPEGImageWriter.WARNING_NO_GRAY_THUMB_AS_INDEXED);
                }
            }
        } else {
            writer.warningOccurred
                (JPEGImageWriter.WARNING_ILLEGAL_THUMBNAIL);
        }
    }
    private class IllegalThumbException extends Exception {}
    private void writeJFXXSegment(int index,
                                  BufferedImage thumbnail,
                                  ImageOutputStream ios,
                                  JPEGImageWriter writer) throws IOException {
        JFIFExtensionMarkerSegment jfxx = null;
        try {
             jfxx = new JFIFExtensionMarkerSegment(thumbnail);
        } catch (IllegalThumbException e) {
            writer.warningOccurred
                (JPEGImageWriter.WARNING_ILLEGAL_THUMBNAIL);
            return;
        }
        writer.thumbnailStarted(index);
        jfxx.write(ios, writer);
        writer.thumbnailComplete();
    }
    private static BufferedImage expandGrayThumb(BufferedImage thumb) {
        BufferedImage ret = new BufferedImage(thumb.getWidth(),
                                              thumb.getHeight(),
                                              BufferedImage.TYPE_INT_RGB);
        Graphics g = ret.getGraphics();
        g.drawImage(thumb, 0, 0, null);
        return ret;
    }
    static void writeDefaultJFIF(ImageOutputStream ios,
                                 List thumbnails,
                                 ICC_Profile iccProfile,
                                 JPEGImageWriter writer)
        throws IOException {
        JFIFMarkerSegment jfif = new JFIFMarkerSegment();
        jfif.writeWithThumbs(ios, thumbnails, writer);
        if (iccProfile != null) {
            writeICC(iccProfile, ios);
        }
    }
    void print() {
        printTag("JFIF");
        System.out.print("Version ");
        System.out.print(majorVersion);
        System.out.println(".0"
                           + Integer.toString(minorVersion));
        System.out.print("Resolution units: ");
        System.out.println(resUnits);
        System.out.print("X density: ");
        System.out.println(Xdensity);
        System.out.print("Y density: ");
        System.out.println(Ydensity);
        System.out.print("Thumbnail Width: ");
        System.out.println(thumbWidth);
        System.out.print("Thumbnail Height: ");
        System.out.println(thumbHeight);
        if (!extSegments.isEmpty()) {
            for (Iterator iter = extSegments.iterator(); iter.hasNext();) {
                JFIFExtensionMarkerSegment extSegment =
                    (JFIFExtensionMarkerSegment) iter.next();
                extSegment.print();
            }
        }
        if (iccSegment != null) {
            iccSegment.print();
        }
    }
    class JFIFExtensionMarkerSegment extends MarkerSegment {
        int code;
        JFIFThumb thumb;
        private static final int DATA_SIZE = 6;
        private static final int ID_SIZE = 5;
        JFIFExtensionMarkerSegment(JPEGBuffer buffer, JPEGImageReader reader)
            throws IOException {
            super(buffer);
            buffer.bufPtr += ID_SIZE;  
            code = buffer.buf[buffer.bufPtr++] & 0xff;
            buffer.bufAvail -= DATA_SIZE;
            if (code == THUMB_JPEG) {
                thumb = new JFIFThumbJPEG(buffer, length, reader);
            } else {
                buffer.loadBuf(2);
                int thumbX = buffer.buf[buffer.bufPtr++] & 0xff;
                int thumbY = buffer.buf[buffer.bufPtr++] & 0xff;
                buffer.bufAvail -= 2;
                if (code == THUMB_PALETTE) {
                    thumb = new JFIFThumbPalette(buffer, thumbX, thumbY);
                } else {
                    thumb = new JFIFThumbRGB(buffer, thumbX, thumbY);
                }
            }
        }
        JFIFExtensionMarkerSegment(Node node) throws IIOInvalidTreeException {
            super(JPEG.APP0);
            NamedNodeMap attrs = node.getAttributes();
            if (attrs.getLength() > 0) {
                code = getAttributeValue(node,
                                         attrs,
                                         "extensionCode",
                                         THUMB_JPEG,
                                         THUMB_RGB,
                                         false);
                if (code == THUMB_UNASSIGNED) {
                throw new IIOInvalidTreeException
                    ("invalid extensionCode attribute value", node);
                }
            } else {
                code = THUMB_UNASSIGNED;
            }
            if (node.getChildNodes().getLength() != 1) {
                throw new IIOInvalidTreeException
                    ("app0JFXX node must have exactly 1 child", node);
            }
            Node child = node.getFirstChild();
            String name = child.getNodeName();
            if (name.equals("JFIFthumbJPEG")) {
                if (code == THUMB_UNASSIGNED) {
                    code = THUMB_JPEG;
                }
                thumb = new JFIFThumbJPEG(child);
            } else if (name.equals("JFIFthumbPalette")) {
                if (code == THUMB_UNASSIGNED) {
                    code = THUMB_PALETTE;
                }
                thumb = new JFIFThumbPalette(child);
            } else if (name.equals("JFIFthumbRGB")) {
                if (code == THUMB_UNASSIGNED) {
                    code = THUMB_RGB;
                }
                thumb = new JFIFThumbRGB(child);
            } else {
                throw new IIOInvalidTreeException
                    ("unrecognized app0JFXX child node", node);
            }
        }
        JFIFExtensionMarkerSegment(BufferedImage thumbnail)
            throws IllegalThumbException {
            super(JPEG.APP0);
            ColorModel cm = thumbnail.getColorModel();
            int csType = cm.getColorSpace().getType();
            if (cm.hasAlpha()) {
                throw new IllegalThumbException();
            }
            if (cm instanceof IndexColorModel) {
                code = THUMB_PALETTE;
                thumb = new JFIFThumbPalette(thumbnail);
            } else if (csType == ColorSpace.TYPE_RGB) {
                code = THUMB_RGB;
                thumb = new JFIFThumbRGB(thumbnail);
            } else if (csType == ColorSpace.TYPE_GRAY) {
                code = THUMB_JPEG;
                thumb = new JFIFThumbJPEG(thumbnail);
            } else {
                throw new IllegalThumbException();
            }
        }
        void setThumbnail(BufferedImage thumbnail) {
            try {
                switch (code) {
                case THUMB_PALETTE:
                    thumb = new JFIFThumbPalette(thumbnail);
                    break;
                case THUMB_RGB:
                    thumb = new JFIFThumbRGB(thumbnail);
                    break;
                case THUMB_JPEG:
                    thumb = new JFIFThumbJPEG(thumbnail);
                    break;
                }
            } catch (IllegalThumbException e) {
                throw new InternalError("Illegal thumb in setThumbnail!");
            }
        }
        protected Object clone() {
            JFIFExtensionMarkerSegment newGuy =
                (JFIFExtensionMarkerSegment) super.clone();
            if (thumb != null) {
                newGuy.thumb = (JFIFThumb) thumb.clone();
            }
            return newGuy;
        }
        IIOMetadataNode getNativeNode() {
            IIOMetadataNode node = new IIOMetadataNode("app0JFXX");
            node.setAttribute("extensionCode", Integer.toString(code));
            node.appendChild(thumb.getNativeNode());
            return node;
        }
        void write(ImageOutputStream ios,
                   JPEGImageWriter writer) throws IOException {
            length = LENGTH_SIZE + DATA_SIZE + thumb.getLength();
            writeTag(ios);
            byte [] id = {0x4A, 0x46, 0x58, 0x58, 0x00};
            ios.write(id);
            ios.write(code);
            thumb.write(ios, writer);
        }
        void print() {
            printTag("JFXX");
            thumb.print();
        }
    }
    abstract class JFIFThumb implements Cloneable {
        long streamPos = -1L;  
        abstract int getLength(); 
        abstract int getWidth();
        abstract int getHeight();
        abstract BufferedImage getThumbnail(ImageInputStream iis,
                                            JPEGImageReader reader)
            throws IOException;
        protected JFIFThumb() {}
        protected JFIFThumb(JPEGBuffer buffer) throws IOException{
            streamPos = buffer.getStreamPosition();
        }
        abstract void print();
        abstract IIOMetadataNode getNativeNode();
        abstract void write(ImageOutputStream ios,
                            JPEGImageWriter writer) throws IOException;
        protected Object clone() {
            try {
                return super.clone();
            } catch (CloneNotSupportedException e) {} 
            return null;
        }
    }
    abstract class JFIFThumbUncompressed extends JFIFThumb {
        BufferedImage thumbnail = null;
        int thumbWidth;
        int thumbHeight;
        String name;
        JFIFThumbUncompressed(JPEGBuffer buffer,
                              int width,
                              int height,
                              int skip,
                              String name)
            throws IOException {
            super(buffer);
            thumbWidth = width;
            thumbHeight = height;
            buffer.skipData(skip);
            this.name = name;
        }
        JFIFThumbUncompressed(Node node, String name)
            throws IIOInvalidTreeException {
            thumbWidth = 0;
            thumbHeight = 0;
            this.name = name;
            NamedNodeMap attrs = node.getAttributes();
            int count = attrs.getLength();
            if (count > 2) {
                throw new IIOInvalidTreeException
                    (name +" node cannot have > 2 attributes", node);
            }
            if (count != 0) {
                int value = getAttributeValue(node, attrs, "thumbWidth",
                                              0, 255, false);
                thumbWidth = (value != -1) ? value : thumbWidth;
                value = getAttributeValue(node, attrs, "thumbHeight",
                                          0, 255, false);
                thumbHeight = (value != -1) ? value : thumbHeight;
            }
        }
        JFIFThumbUncompressed(BufferedImage thumb) {
            thumbnail = thumb;
            thumbWidth = thumb.getWidth();
            thumbHeight = thumb.getHeight();
            name = null;  
        }
        void readByteBuffer(ImageInputStream iis,
                            byte [] data,
                            JPEGImageReader reader,
                            float workPortion,
                            float workOffset) throws IOException {
            int progInterval = Math.max((int)(data.length/20/workPortion),
                                        1);
            for (int offset = 0;
                 offset < data.length;) {
                int len = Math.min(progInterval, data.length-offset);
                iis.read(data, offset, len);
                offset += progInterval;
                float percentDone = ((float) offset* 100)
                    / data.length
                    * workPortion + workOffset;
                if (percentDone > 100.0F) {
                    percentDone = 100.0F;
                }
                reader.thumbnailProgress (percentDone);
            }
        }
        int getWidth() {
            return thumbWidth;
        }
        int getHeight() {
            return thumbHeight;
        }
        IIOMetadataNode getNativeNode() {
            IIOMetadataNode node = new IIOMetadataNode(name);
            node.setAttribute("thumbWidth", Integer.toString(thumbWidth));
            node.setAttribute("thumbHeight", Integer.toString(thumbHeight));
            return node;
        }
        void write(ImageOutputStream ios,
                   JPEGImageWriter writer) throws IOException {
            if ((thumbWidth > MAX_THUMB_WIDTH)
                || (thumbHeight > MAX_THUMB_HEIGHT)) {
                writer.warningOccurred(JPEGImageWriter.WARNING_THUMB_CLIPPED);
            }
            thumbWidth = Math.min(thumbWidth, MAX_THUMB_WIDTH);
            thumbHeight = Math.min(thumbHeight, MAX_THUMB_HEIGHT);
            ios.write(thumbWidth);
            ios.write(thumbHeight);
        }
        void writePixels(ImageOutputStream ios,
                         JPEGImageWriter writer) throws IOException {
            if ((thumbWidth > MAX_THUMB_WIDTH)
                || (thumbHeight > MAX_THUMB_HEIGHT)) {
                writer.warningOccurred(JPEGImageWriter.WARNING_THUMB_CLIPPED);
            }
            thumbWidth = Math.min(thumbWidth, MAX_THUMB_WIDTH);
            thumbHeight = Math.min(thumbHeight, MAX_THUMB_HEIGHT);
            int [] data = thumbnail.getRaster().getPixels(0, 0,
                                                          thumbWidth,
                                                          thumbHeight,
                                                          (int []) null);
            writeThumbnailData(ios, data, writer);
        }
        void print() {
            System.out.print(name + " width: ");
            System.out.println(thumbWidth);
            System.out.print(name + " height: ");
            System.out.println(thumbHeight);
        }
    }
    class JFIFThumbRGB extends JFIFThumbUncompressed {
        JFIFThumbRGB(JPEGBuffer buffer, int width, int height)
            throws IOException {
            super(buffer, width, height, width*height*3, "JFIFthumbRGB");
        }
        JFIFThumbRGB(Node node) throws IIOInvalidTreeException {
            super(node, "JFIFthumbRGB");
        }
        JFIFThumbRGB(BufferedImage thumb) throws IllegalThumbException {
            super(thumb);
        }
        int getLength() {
            return (thumbWidth*thumbHeight*3);
        }
        BufferedImage getThumbnail(ImageInputStream iis,
                                   JPEGImageReader reader)
            throws IOException {
            iis.mark();
            iis.seek(streamPos);
            DataBufferByte buffer = new DataBufferByte(getLength());
            readByteBuffer(iis,
                           buffer.getData(),
                           reader,
                           1.0F,
                           0.0F);
            iis.reset();
            WritableRaster raster =
                Raster.createInterleavedRaster(buffer,
                                               thumbWidth,
                                               thumbHeight,
                                               thumbWidth*3,
                                               3,
                                               new int [] {0, 1, 2},
                                               null);
            ColorModel cm = new ComponentColorModel(JPEG.JCS.sRGB,
                                                    false,
                                                    false,
                                                    ColorModel.OPAQUE,
                                                    DataBuffer.TYPE_BYTE);
            return new BufferedImage(cm,
                                     raster,
                                     false,
                                     null);
        }
        void write(ImageOutputStream ios,
                   JPEGImageWriter writer) throws IOException {
            super.write(ios, writer); 
            writePixels(ios, writer);
        }
    }
    class JFIFThumbPalette extends JFIFThumbUncompressed {
        private static final int PALETTE_SIZE = 768;
        JFIFThumbPalette(JPEGBuffer buffer, int width, int height)
            throws IOException {
            super(buffer,
                  width,
                  height,
                  PALETTE_SIZE + width * height,
                  "JFIFThumbPalette");
        }
        JFIFThumbPalette(Node node) throws IIOInvalidTreeException {
            super(node, "JFIFThumbPalette");
        }
        JFIFThumbPalette(BufferedImage thumb) throws IllegalThumbException {
            super(thumb);
            IndexColorModel icm = (IndexColorModel) thumbnail.getColorModel();
            if (icm.getMapSize() > 256) {
                throw new IllegalThumbException();
            }
        }
        int getLength() {
            return (thumbWidth*thumbHeight + PALETTE_SIZE);
        }
        BufferedImage getThumbnail(ImageInputStream iis,
                                   JPEGImageReader reader)
            throws IOException {
            iis.mark();
            iis.seek(streamPos);
            byte [] palette = new byte [PALETTE_SIZE];
            float palettePart = ((float) PALETTE_SIZE) / getLength();
            readByteBuffer(iis,
                           palette,
                           reader,
                           palettePart,
                           0.0F);
            DataBufferByte buffer = new DataBufferByte(thumbWidth*thumbHeight);
            readByteBuffer(iis,
                           buffer.getData(),
                           reader,
                           1.0F-palettePart,
                           palettePart);
            iis.read();
            iis.reset();
            IndexColorModel cm = new IndexColorModel(8,
                                                     256,
                                                     palette,
                                                     0,
                                                     false);
            SampleModel sm = cm.createCompatibleSampleModel(thumbWidth,
                                                            thumbHeight);
            WritableRaster raster =
                Raster.createWritableRaster(sm, buffer, null);
            return new BufferedImage(cm,
                                     raster,
                                     false,
                                     null);
        }
        void write(ImageOutputStream ios,
                   JPEGImageWriter writer) throws IOException {
            super.write(ios, writer); 
            byte [] palette = new byte[768];
            IndexColorModel icm = (IndexColorModel) thumbnail.getColorModel();
            byte [] reds = new byte [256];
            byte [] greens = new byte [256];
            byte [] blues = new byte [256];
            icm.getReds(reds);
            icm.getGreens(greens);
            icm.getBlues(blues);
            for (int i = 0; i < 256; i++) {
                palette[i*3] = reds[i];
                palette[i*3+1] = greens[i];
                palette[i*3+2] = blues[i];
            }
            ios.write(palette);
            writePixels(ios, writer);
        }
    }
    class JFIFThumbJPEG extends JFIFThumb {
        JPEGMetadata thumbMetadata = null;
        byte [] data = null;  
        private static final int PREAMBLE_SIZE = 6;
        JFIFThumbJPEG(JPEGBuffer buffer,
                      int length,
                      JPEGImageReader reader) throws IOException {
            super(buffer);
            long finalPos = streamPos + (length - PREAMBLE_SIZE);
            buffer.iis.seek(streamPos);
            thumbMetadata = new JPEGMetadata(false, true, buffer.iis, reader);
            buffer.iis.seek(finalPos);
            buffer.bufAvail = 0;
            buffer.bufPtr = 0;
        }
        JFIFThumbJPEG(Node node) throws IIOInvalidTreeException {
            if (node.getChildNodes().getLength() > 1) {
                throw new IIOInvalidTreeException
                    ("JFIFThumbJPEG node must have 0 or 1 child", node);
            }
            Node child = node.getFirstChild();
            if (child != null) {
                String name = child.getNodeName();
                if (!name.equals("markerSequence")) {
                    throw new IIOInvalidTreeException
                        ("JFIFThumbJPEG child must be a markerSequence node",
                         node);
                }
                thumbMetadata = new JPEGMetadata(false, true);
                thumbMetadata.setFromMarkerSequenceNode(child);
            }
        }
        JFIFThumbJPEG(BufferedImage thumb) throws IllegalThumbException {
            int INITIAL_BUFSIZE = 4096;
            int MAZ_BUFSIZE = 65535 - 2 - PREAMBLE_SIZE;
            try {
                ByteArrayOutputStream baos =
                    new ByteArrayOutputStream(INITIAL_BUFSIZE);
                MemoryCacheImageOutputStream mos =
                    new MemoryCacheImageOutputStream(baos);
                JPEGImageWriter thumbWriter = new JPEGImageWriter(null);
                thumbWriter.setOutput(mos);
                JPEGMetadata metadata =
                    (JPEGMetadata) thumbWriter.getDefaultImageMetadata
                    (new ImageTypeSpecifier(thumb), null);
                MarkerSegment jfif = metadata.findMarkerSegment
                    (JFIFMarkerSegment.class, true);
                if (jfif == null) {
                    throw new IllegalThumbException();
                }
                metadata.markerSequence.remove(jfif);
                thumbWriter.write(new IIOImage(thumb, null, metadata));
                thumbWriter.dispose();
                if (baos.size() > MAZ_BUFSIZE) {
                    throw new IllegalThumbException();
                }
                data = baos.toByteArray();
            } catch (IOException e) {
                throw new IllegalThumbException();
            }
        }
        int getWidth() {
            int retval = 0;
            SOFMarkerSegment sof =
                (SOFMarkerSegment) thumbMetadata.findMarkerSegment
                (SOFMarkerSegment.class, true);
            if (sof != null) {
                retval = sof.samplesPerLine;
            }
            return retval;
        }
        int getHeight() {
            int retval = 0;
            SOFMarkerSegment sof =
                (SOFMarkerSegment) thumbMetadata.findMarkerSegment
                (SOFMarkerSegment.class, true);
            if (sof != null) {
                retval = sof.numLines;
            }
            return retval;
        }
        private class ThumbnailReadListener
            implements IIOReadProgressListener {
            JPEGImageReader reader = null;
            ThumbnailReadListener (JPEGImageReader reader) {
                this.reader = reader;
            }
            public void sequenceStarted(ImageReader source, int minIndex) {}
            public void sequenceComplete(ImageReader source) {}
            public void imageStarted(ImageReader source, int imageIndex) {}
            public void imageProgress(ImageReader source,
                                      float percentageDone) {
                reader.thumbnailProgress(percentageDone);
            }
            public void imageComplete(ImageReader source) {}
            public void thumbnailStarted(ImageReader source,
                int imageIndex, int thumbnailIndex) {}
            public void thumbnailProgress(ImageReader source, float percentageDone) {}
            public void thumbnailComplete(ImageReader source) {}
            public void readAborted(ImageReader source) {}
        }
        BufferedImage getThumbnail(ImageInputStream iis,
                                   JPEGImageReader reader)
            throws IOException {
            iis.mark();
            iis.seek(streamPos);
            JPEGImageReader thumbReader = new JPEGImageReader(null);
            thumbReader.setInput(iis);
            thumbReader.addIIOReadProgressListener
                (new ThumbnailReadListener(reader));
            BufferedImage ret = thumbReader.read(0, null);
            thumbReader.dispose();
            iis.reset();
            return ret;
        }
        protected Object clone() {
            JFIFThumbJPEG newGuy = (JFIFThumbJPEG) super.clone();
            if (thumbMetadata != null) {
                newGuy.thumbMetadata = (JPEGMetadata) thumbMetadata.clone();
            }
            return newGuy;
        }
        IIOMetadataNode getNativeNode() {
            IIOMetadataNode node = new IIOMetadataNode("JFIFthumbJPEG");
            if (thumbMetadata != null) {
                node.appendChild(thumbMetadata.getNativeTree());
            }
            return node;
        }
        int getLength() {
            if (data == null) {
                return 0;
            } else {
                return data.length;
            }
        }
        void write(ImageOutputStream ios,
                   JPEGImageWriter writer) throws IOException {
            int progInterval = data.length / 20;  
            if (progInterval == 0) {
                progInterval = 1;
            }
            for (int offset = 0;
                 offset < data.length;) {
                int len = Math.min(progInterval, data.length-offset);
                ios.write(data, offset, len);
                offset += progInterval;
                float percentDone = ((float) offset * 100) / data.length;
                if (percentDone > 100.0F) {
                    percentDone = 100.0F;
                }
                writer.thumbnailProgress (percentDone);
            }
        }
        void print () {
            System.out.println("JFIF thumbnail stored as JPEG");
        }
    }
    static void writeICC(ICC_Profile profile, ImageOutputStream ios)
        throws IOException {
        int LENGTH_LENGTH = 2;
        final String ID = "ICC_PROFILE";
        int ID_LENGTH = ID.length()+1; 
        int COUNTS_LENGTH = 2;
        int MAX_ICC_CHUNK_SIZE =
            65535 - LENGTH_LENGTH - ID_LENGTH - COUNTS_LENGTH;
        byte [] data = profile.getData();
        int numChunks = data.length / MAX_ICC_CHUNK_SIZE;
        if ((data.length % MAX_ICC_CHUNK_SIZE) != 0) {
            numChunks++;
        }
        int chunkNum = 1;
        int offset = 0;
        for (int i = 0; i < numChunks; i++) {
            int dataLength = Math.min(data.length-offset, MAX_ICC_CHUNK_SIZE);
            int segLength = dataLength+COUNTS_LENGTH+ID_LENGTH+LENGTH_LENGTH;
            ios.write(0xff);
            ios.write(JPEG.APP2);
            MarkerSegment.write2bytes(ios, segLength);
            byte [] id = ID.getBytes("US-ASCII");
            ios.write(id);
            ios.write(0); 
            ios.write(chunkNum++);
            ios.write(numChunks);
            ios.write(data, offset, dataLength);
            offset += dataLength;
        }
    }
    class ICCMarkerSegment extends MarkerSegment {
        ArrayList chunks = null;
        byte [] profile = null; 
        private static final int ID_SIZE = 12;
        int chunksRead;
        int numChunks;
        ICCMarkerSegment(ICC_ColorSpace cs) {
            super(JPEG.APP2);
            chunks = null;
            chunksRead = 0;
            numChunks = 0;
            profile = cs.getProfile().getData();
        }
        ICCMarkerSegment(JPEGBuffer buffer) throws IOException {
            super(buffer);  
            if (debug) {
                System.out.println("Creating new ICC segment");
            }
            buffer.bufPtr += ID_SIZE; 
            buffer.bufAvail -= ID_SIZE;
            length -= ID_SIZE;
            int chunkNum = buffer.buf[buffer.bufPtr] & 0xff;
            numChunks = buffer.buf[buffer.bufPtr+1] & 0xff;
            if (chunkNum > numChunks) {
                throw new IIOException
                    ("Image format Error; chunk num > num chunks");
            }
            if (numChunks == 1) {
                length -= 2;
                profile = new byte[length];
                buffer.bufPtr += 2;
                buffer.bufAvail-=2;
                buffer.readData(profile);
                inICC = false;
            } else {
                byte [] profileData = new byte[length];
                length -= 2;
                buffer.readData(profileData);
                chunks = new ArrayList();
                chunks.add(profileData);
                chunksRead = 1;
                inICC = true;
            }
        }
        ICCMarkerSegment(Node node) throws IIOInvalidTreeException {
            super(JPEG.APP2);
            if (node instanceof IIOMetadataNode) {
                IIOMetadataNode ourNode = (IIOMetadataNode) node;
                ICC_Profile prof = (ICC_Profile) ourNode.getUserObject();
                if (prof != null) {  
                    profile = prof.getData();
                }
            }
        }
        protected Object clone () {
            ICCMarkerSegment newGuy = (ICCMarkerSegment) super.clone();
            if (profile != null) {
                newGuy.profile = (byte[]) profile.clone();
            }
            return newGuy;
        }
        boolean addData(JPEGBuffer buffer) throws IOException {
            if (debug) {
                System.out.println("Adding to ICC segment");
            }
            buffer.bufPtr++;
            buffer.bufAvail--;
            int dataLen = (buffer.buf[buffer.bufPtr++] & 0xff) << 8;
            dataLen |= buffer.buf[buffer.bufPtr++] & 0xff;
            buffer.bufAvail -= 2;
            dataLen -= 2;
            buffer.bufPtr += ID_SIZE; 
            buffer.bufAvail -= ID_SIZE;
            dataLen -= ID_SIZE;
            int chunkNum = buffer.buf[buffer.bufPtr] & 0xff;
            if (chunkNum > numChunks) {
                throw new IIOException
                    ("Image format Error; chunk num > num chunks");
            }
            int newNumChunks = buffer.buf[buffer.bufPtr+1] & 0xff;
            if (numChunks != newNumChunks) {
                throw new IIOException
                    ("Image format Error; icc num chunks mismatch");
            }
            dataLen -= 2;
            if (debug) {
                System.out.println("chunkNum: " + chunkNum
                                   + ", numChunks: " + numChunks
                                   + ", dataLen: " + dataLen);
            }
            boolean retval = false;
            byte [] profileData = new byte[dataLen];
            buffer.readData(profileData);
            chunks.add(profileData);
            length += dataLen;
            chunksRead++;
            if (chunksRead < numChunks) {
                inICC = true;
            } else {
                if (debug) {
                    System.out.println("Completing profile; total length is "
                                       + length);
                }
                profile = new byte[length];
                int index = 0;
                for (int i = 1; i <= numChunks; i++) {
                    boolean foundIt = false;
                    for (int chunk = 0; chunk < chunks.size(); chunk++) {
                        byte [] chunkData = (byte []) chunks.get(chunk);
                        if (chunkData[0] == i) { 
                            System.arraycopy(chunkData, 2,
                                             profile, index,
                                             chunkData.length-2);
                            index += chunkData.length-2;
                            foundIt = true;
                        }
                    }
                    if (foundIt == false) {
                        throw new IIOException
                            ("Image Format Error: Missing ICC chunk num " + i);
                    }
                }
                chunks = null;
                chunksRead = 0;
                numChunks = 0;
                inICC = false;
                retval = true;
            }
            return retval;
        }
        IIOMetadataNode getNativeNode() {
            IIOMetadataNode node = new IIOMetadataNode("app2ICC");
            if (profile != null) {
                node.setUserObject(ICC_Profile.getInstance(profile));
            }
            return node;
        }
        void write(ImageOutputStream ios) throws IOException {
        }
        void print () {
            printTag("ICC Profile APP2");
        }
    }
}
