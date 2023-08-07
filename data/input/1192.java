class BlobEnabler {
    public BlobEnable defaults;
    public Map<String, BlobEnable> specific;
    public BlobEnabler() {
        this.specific = new HashMap<String, BlobEnable>();
        this.defaults = BlobEnable.Never;
    }
}
public class ClientHandler extends OutputQueue {
    private final Log log = LogFactory.getLog(ClientHandler.class);
    private Queue<java.lang.Object> threadToDriverQueue;
    private final SAXParser parser;
    private Map<String, BlobEnabler> blobEnableMap = null;
    private String device = null;
    private boolean allDevices = false;
    BlobEnable defaults = BlobEnable.Never;
    NonBlockungXMLByteBuffer nonBlockungXMLByteBuffer;
    SaxHandler saxHandler;
    IndiServer server;
    public ClientHandler(Reactor r, SelectableChannel ch) throws IOException, ParserConfigurationException, SAXException {
        super(r, ch);
        nonBlockungXMLByteBuffer = new NonBlockungXMLByteBuffer();
        this.parser = SAXParserFactory.newInstance().newSAXParser();
        this.blobEnableMap = new HashMap<String, BlobEnabler>();
    }
    public void shutDown() {
        this.reactor.unregister(this);
    }
    public void setServer(IndiServer server) {
        this.server = server;
        this.saxHandler = new SaxHandler(server.getDispatcher(), this);
    }
    @Override
    public void onRead() throws IOException {
        try {
            long oldpos = nonBlockungXMLByteBuffer.getBuffer().position();
            ((ReadableByteChannel) this.channel).read(nonBlockungXMLByteBuffer.getBuffer());
            if (nonBlockungXMLByteBuffer.getBuffer().position() - oldpos == 0) {
                throw new RuntimeException("Client Disconnected");
            }
            for (byte[] doc = nonBlockungXMLByteBuffer.next(); doc != null; doc = nonBlockungXMLByteBuffer.next()) {
                this.parser.parse(new ByteArrayInputStream(doc), this.saxHandler);
            }
        } catch (Exception e) {
            server.bogusClient(this, e);
        }
    }
    private void onClientDisconnected() {
        this.reactor.unregister(this);
    }
    public void onGetProperties(GetProperties o) {
        if (o.device == null) {
            this.allDevices = true;
        } else {
            this.device = o.device;
        }
    }
    public synchronized void onEnableBlob(EnableBlob eb) {
        if (this.device == null) {
            this.defaults = eb.blobenable;
        }
        BlobEnabler ber = this.blobEnableMap.get(eb.device);
        if (ber == null) {
            BlobEnabler be = new BlobEnabler();
            if (eb.name == null) {
                be.defaults = eb.blobenable;
                this.blobEnableMap.put(eb.device, be);
            } else {
                be.specific.put(eb.name, eb.blobenable);
                this.blobEnableMap.put(eb.device, be);
            }
        } else {
            if (eb.name == null) {
                ber.defaults = eb.blobenable;
            } else {
                ber.specific.put(eb.name, eb.blobenable);
            }
        }
    }
    private static boolean getEnabled(boolean blob, BlobEnable be) {
        if (blob) {
            switch(be) {
                case Only:
                    return true;
                case Also:
                    return true;
                case Never:
                    return false;
            }
        } else {
            switch(be) {
                case Only:
                    return false;
                case Also:
                    return true;
                case Never:
                    return true;
            }
        }
        throw new RuntimeException();
    }
    public synchronized boolean getVectorEnabled(Vector vec) {
        BlobEnabler ber = this.blobEnableMap.get(vec.getDevice());
        if (ber == null) {
            return getEnabled(vec instanceof BlobVector, this.defaults);
        } else {
            BlobEnable be = ber.specific.get(vec.getName());
            if (be == null) {
                return getEnabled(vec instanceof BlobVector, ber.defaults);
            } else {
                return getEnabled(vec instanceof BlobVector, be);
            }
        }
    }
    public synchronized void send(org.indi.objects.Object object, TransferType type, String message) {
        String dev = null;
        if (object instanceof Vector) {
            Vector vec = (Vector) object;
            dev = vec.getDevice();
            if (!getVectorEnabled(vec)) {
                return;
            }
        }
        if (object instanceof Message) {
            dev = ((Message) object).getDevice();
            BlobEnabler ber = this.blobEnableMap.get(dev);
            if (ber == null) {
                if (!getEnabled(false, this.defaults)) {
                    return;
                }
            } else {
                if (!getEnabled(false, ber.defaults)) {
                    return;
                }
            }
        }
        if ((dev == this.device) | (this.allDevices)) {
            String data = object.getXML(type, message);
            ByteBuffer output = ByteBuffer.allocate(data.length());
            output.put(data.getBytes());
            output.flip();
            write(output);
        }
    }
}
