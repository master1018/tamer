public class Support_TestWebServer implements Support_HttpConstants {
    private final static String LOGTAG = "httpsv";
    private final Map<String, Request> pathToRequest
            = new ConcurrentHashMap<String, Request>();
    int timeout = 0;
    final static int DEFAULT_PORT = 8080;
    final static int DEFAULT_TIMEOUT = 5000;
    protected String HTTP_VERSION_STRING = "HTTP/1.1";
    private boolean http11 = true;
    private AcceptThread acceptT;
    int mTimeout;
    int mPort;
    boolean mLog = false;
    boolean keepAlive = true;
    boolean chunked = false;
    int maxChunkSize = 1024;
    String redirectHost = null;
    int redirectCode = -1;
    int acceptLimit = 100;
    int acceptedConnections = 0;
    public Support_TestWebServer() {
    }
    public void initServer(boolean log) throws Exception {
        initServer(DEFAULT_PORT, DEFAULT_TIMEOUT, log);
    }
    public void initServer(int port, boolean log) throws Exception {
        initServer(port, DEFAULT_TIMEOUT, log);
    }
    public void initServer(int port, String servePath, String contentType)
            throws Exception {
        Support_TestWebData.initDynamicTestWebData(servePath, contentType);
        initServer(port, DEFAULT_TIMEOUT, false);
    }
    public void initServer(int port, int timeout, boolean log) throws Exception {
        mPort = port;
        mTimeout = timeout;
        mLog = log;
        keepAlive = true;
        if (acceptT == null) {
            acceptT = new AcceptThread();
            acceptT.init();
            acceptT.start();
        }
    }
    protected void log(String s) {
        if (mLog) {
            Logger.global.fine(s);
        }
    }
    public void setHttpVersion11(boolean set) {
        http11 = set;
        if (set) {
            HTTP_VERSION_STRING = "HTTP/1.1";
        } else {
            HTTP_VERSION_STRING = "HTTP/1.0";
        }
    }
    public void setKeepAlive(boolean value) {
        keepAlive = value;
    }
    public void setChunked(boolean value) {
        chunked = value;
    }
    public void setMaxChunkSize(int maxChunkSize) {
        this.maxChunkSize = maxChunkSize;
    }
    public void setAcceptLimit(int limit) {
        acceptLimit = limit;
    }
    public void setRedirect(String redirect, int code) {
        redirectHost = redirect;
        redirectCode = code;
        log("Server will redirect output to "+redirect+" code "+code);
    }
    public Map<String, Request> pathToRequest() {
        return pathToRequest;
    }
    public int getNumAcceptedConnections() {
        return acceptedConnections;
    }
    public void close() {
        if (acceptT != null) {
            log("Closing AcceptThread"+acceptT);
            acceptT.close();
            acceptT = null;
        }
    }
    class AcceptThread extends Thread {
        ServerSocket ss = null;
        boolean running = false;
        public void init() {
            InetSocketAddress ia = new InetSocketAddress(mPort);
            while (true) {
                try {
                    ss = new ServerSocket();
                    ss.setReuseAddress(true);
                    ss.bind(ia);
                    break;
                } catch (IOException e) {
                    log("IOException in AcceptThread.init()");                    
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
        public synchronized void run() {
            running = true;
            while (running) {
                try {
                    Socket s = ss.accept();
                    acceptedConnections++;
                    if (acceptedConnections >= acceptLimit) {
                        running = false;
                    }
                    new Thread(new Worker(s), "additional worker").start();
                } catch (SocketException e) {
                    log(e.getMessage());
                } catch (IOException e) {
                    log(e.getMessage());
                }
            }
            log("AcceptThread terminated" + this);
        }
        public void close() {
            try {
                running = false;
                ss.close();
            } catch (IOException e) {
                log("IOException caught by server socket close");
            }
        }
    }
    final static int BUF_SIZE = 2048;
    static final byte[] EOL = {(byte)'\r', (byte)'\n' };
    public static class Request {
        private final String path;
        private final Map<String, String> headers;
        public Request(String path, Map<String, String> headers) {
            this.path = path;
            this.headers = new LinkedHashMap<String, String>(headers);
        }
        public String getPath() {
            return path;
        }
        public Map<String, String> getHeaders() {
            return headers;
        }
    }
    class Worker implements Support_HttpConstants, Runnable {
        byte[] buf;
        private Socket s;
        private int requestMethod;
        private String testID;
        private String path;
        private int testNum;
        private boolean readStarted;
        private boolean hasContent = false;
        private Map<String, String> headers = new LinkedHashMap<String, String>();
        Worker(Socket s) {
            this.buf = new byte[BUF_SIZE];
            this.s = s;
        }
        public synchronized void run() {
            try {
                handleClient();
            } catch (Exception e) {
                log("Exception during handleClient in the TestWebServer: " + e.getMessage());
            }
            log(this+" terminated");
        }
        private void clearBuffer() {
            for (int i = 0; i < BUF_SIZE; i++) {
                buf[i] = 0;
            }
        }
        private int readOneLine(InputStream is) {
            int read = 0;
            clearBuffer();
            try {
                log("Reading one line: started ="+readStarted+" avail="+is.available());
                StringBuilder log = new StringBuilder();
                while ((!readStarted) || (is.available() > 0)) {
                    int data = is.read();
                    if (data == -1) {
                        log("EOF returned");
                        return -1;
                    }
                    buf[read] = (byte)data;
                    log.append((char)data);
                    readStarted = true;
                    if (buf[read++]==(byte)'\n') {
                        log(log.toString());
                        return read;
                    }
                }
            } catch (IOException e) {
                log("IOException from readOneLine");
            }
            return read;
        }
        private int readData(InputStream is, int length) {
            int read = 0;
            int count;
            byte[] buf = new byte[length];
            try {
                while (is.available() > 0) {
                    count = is.read(buf, read, length-read);
                    read += count;
                }
            } catch (IOException e) {
                log("IOException from readData");
            }
            return read;
        }
        private int parseStatusLine(InputStream is) {
            int index;
            int nread = 0;
            log("Parse status line");
            nread = readOneLine(is);
            if (nread == -1) {
                requestMethod = UNKNOWN_METHOD;
                return -1;
            }
            if (buf[0] == (byte)'G' &&
                buf[1] == (byte)'E' &&
                buf[2] == (byte)'T' &&
                buf[3] == (byte)' ') {
                requestMethod = GET_METHOD;
                log("GET request");
                index = 4;
            } else if (buf[0] == (byte)'H' &&
                       buf[1] == (byte)'E' &&
                       buf[2] == (byte)'A' &&
                       buf[3] == (byte)'D' &&
                       buf[4] == (byte)' ') {
                requestMethod = HEAD_METHOD;
                log("HEAD request");
                index = 5;
            } else if (buf[0] == (byte)'P' &&
                       buf[1] == (byte)'O' &&
                       buf[2] == (byte)'S' &&
                       buf[3] == (byte)'T' &&
                       buf[4] == (byte)' ') {
                requestMethod = POST_METHOD;
                log("POST request");
                index = 5;
            } else {
                requestMethod = UNKNOWN_METHOD;
                return -1;
            }
            if (requestMethod > UNKNOWN_METHOD) {
                int i = index;
                while (buf[i] != (byte)' ') {
                    if ((buf[i] == (byte)'\n') || (buf[i] == (byte)'\r')) {
                        requestMethod = UNKNOWN_METHOD;
                        return -1;
                    }
                    i++;
                }
                path = new String(buf, 0, index, i-index);
                testID = path.substring(1);
                return nread;
            }
            return -1;
        }
        private int parseHeader(InputStream is) {
            int index = 0;
            int nread = 0;
            log("Parse a header");
            nread = readOneLine(is);
            if (nread == -1) {
                requestMethod = UNKNOWN_METHOD;
                return -1;
            }
            int i = index;
            while (buf[i] != (byte)':') {
                if ((buf[i] == (byte)'\n') || (buf[i] == (byte)'\r')) {
                    return UNKNOWN_METHOD;
                }
                i++;
            }
            String headerName = new String(buf, 0, i);
            i++; 
            while (buf[i] == ' ') {
                i++;
            }
            String headerValue = new String(buf, i, nread - i - 2); 
            headers.put(headerName, headerValue);
            return nread;
        }
        private int readHeaders(InputStream is) {
            int nread = 0;
            log("Read headers");
            while (true) {
                int headerLen = 0;
                headerLen = parseHeader(is);
                if (headerLen == -1)
                    return -1;
                nread += headerLen;
                if (headerLen <= 2) {
                    return nread;
                }
            }
        }
        private int readContent(InputStream is) {
            int nread = 0;
            log("Read content");
            String lengthString = headers.get(requestHeaders[REQ_CONTENT_LENGTH]);
            int length = new Integer(lengthString).intValue();
            length = readData(is, length);
            return length;
        }
        void handleClient() throws IOException {
            InputStream is = new BufferedInputStream(s.getInputStream());
            PrintStream ps = new PrintStream(s.getOutputStream());
            int nread = 0;
            s.setSoTimeout(mTimeout);
            s.setTcpNoDelay(true);
            do {
                nread = parseStatusLine(is);
                if (requestMethod != UNKNOWN_METHOD) {
                    nread = readHeaders(is);
                    pathToRequest().put(path, new Request(path, headers));
                    if (headers.get(requestHeaders[REQ_CONTENT_LENGTH]) != null) {
                        nread = readContent(is);
                    }
                } else {
                    if (nread > 0) {
                        ps.print(HTTP_VERSION_STRING + " " + HTTP_BAD_METHOD +
                                 " unsupported method type: ");
                        ps.write(buf, 0, 5);
                        ps.write(EOL);
                        ps.flush();
                    } else {
                    }
                    if (!keepAlive || nread <= 0) {
                        headers.clear();
                        readStarted = false;
                        log("SOCKET CLOSED");
                        s.close();
                        return;
                    }
                }
                testNum = -1;
                printStatus(ps);
                printHeaders(ps);
                psWriteEOL(ps);
                if (redirectCode == -1) {
                    switch (requestMethod) {
                        case GET_METHOD:
                            if ((testNum < -1) || (testNum > Support_TestWebData.tests.length - 1)) {
                                send404(ps);
                            } else {
                                sendFile(ps);
                            }
                            break;
                        case HEAD_METHOD:
                            break;
                        case POST_METHOD:
                            if ((testNum > 0) || (testNum < Support_TestWebData.tests.length - 1)) {
                                sendFile(ps);
                            }
                            break;
                        default:
                            break;
                    }
                } else { 
                    switch (redirectCode) {
                        case 301:
                            psPrint(ps, Support_TestWebData.testServerResponse[Support_TestWebData.REDIRECT_301]);
                            break;
                        case 302:
                            psPrint(ps, Support_TestWebData.testServerResponse[Support_TestWebData.REDIRECT_302]);
                            break;
                        case 303:
                            psPrint(ps, Support_TestWebData.testServerResponse[Support_TestWebData.REDIRECT_303]);
                            break;
                        case 307:
                            psPrint(ps, Support_TestWebData.testServerResponse[Support_TestWebData.REDIRECT_307]);
                            break;
                        default:
                            break;
                    }
                }
                ps.flush();
                readStarted = false;
                headers.clear();
            } while (keepAlive);
            log("SOCKET CLOSED");
            s.close();
        }
        void psPrint(PrintStream ps, String s) throws IOException {
            log(s);
            ps.print(s);
        }
        void psWrite(PrintStream ps, byte[] bytes, int offset, int count) throws IOException {
            log(new String(bytes));
            ps.write(bytes, offset, count);
        }
        void psWriteEOL(PrintStream ps) throws IOException {
            log("CRLF");
            ps.write(EOL);
        }
        void printStatus(PrintStream ps) throws IOException {
            if (redirectCode != -1) {
                log("REDIRECTING TO "+redirectHost+" status "+redirectCode);
                psPrint(ps, HTTP_VERSION_STRING + " " + redirectCode +" Moved permanently");
                psWriteEOL(ps);
                psPrint(ps, "Location: " + redirectHost);
                psWriteEOL(ps);
                return;
            }
            if (testID.startsWith("test")) {
                testNum = Integer.valueOf(testID.substring(4))-1;
            }
            if ((testNum < -1) || (testNum > Support_TestWebData.tests.length - 1)) {
                psPrint(ps, HTTP_VERSION_STRING + " " + HTTP_NOT_FOUND + " not found");
                psWriteEOL(ps);
            }  else {
                psPrint(ps, HTTP_VERSION_STRING + " " + HTTP_OK+" OK");
                psWriteEOL(ps);
            }
            log("Status sent");
        }
        void printHeaders(PrintStream ps) throws IOException {
            if ((testNum < -1) || (testNum > Support_TestWebData.tests.length - 1)) {
                return;
            }
            SimpleDateFormat df = new SimpleDateFormat("EE, dd MMM yyyy HH:mm:ss");
            psPrint(ps,"Server: TestWebServer"+mPort);
            psWriteEOL(ps);
            psPrint(ps, "Date: " + df.format(new Date()));
            psWriteEOL(ps);
            psPrint(ps, "Connection: " + ((keepAlive) ? "Keep-Alive" : "Close"));
            psWriteEOL(ps);
            if (redirectCode == -1) {
                if (testNum == -1) {
                    if (!Support_TestWebData.test0DataAvailable) {
                        log("testdata was not initilaized");
                        return;
                    }
                    if (chunked) {
                        psPrint(ps, "Transfer-Encoding: chunked");
                    } else {
                        psPrint(ps, "Content-length: "
                                + Support_TestWebData.test0Data.length);
                    }
                    psWriteEOL(ps);
                    psPrint(ps, "Last Modified: " + (new Date(
                            Support_TestWebData.test0Params.testLastModified)));
                    psWriteEOL(ps);
                    psPrint(ps, "Content-type: "
                            + Support_TestWebData.test0Params.testType);
                    psWriteEOL(ps);
                    if (Support_TestWebData.testParams[testNum].testExp > 0) {
                        long exp;
                        exp = Support_TestWebData.testParams[testNum].testExp;
                        psPrint(ps, "expires: "
                                + df.format(exp) + " GMT");
                        psWriteEOL(ps);
                    }
                } else if (!Support_TestWebData.testParams[testNum].testDir) {
                    if (chunked) {
                        psPrint(ps, "Transfer-Encoding: chunked");
                    } else {
                        psPrint(ps, "Content-length: "+Support_TestWebData.testParams[testNum].testLength);
                    }
                    psWriteEOL(ps);
                    psPrint(ps,"Last Modified: " + (new
                                                    Date(Support_TestWebData.testParams[testNum].testLastModified)));
                    psWriteEOL(ps);
                    psPrint(ps, "Content-type: " + Support_TestWebData.testParams[testNum].testType);
                    psWriteEOL(ps);
                    if (Support_TestWebData.testParams[testNum].testExp > 0) {
                        long exp;
                        exp = Support_TestWebData.testParams[testNum].testExp;
                        psPrint(ps, "expires: "
                                + df.format(exp) + " GMT");
                        psWriteEOL(ps);
                    }
                } else {
                    psPrint(ps, "Content-type: text/html");
                    psWriteEOL(ps);
                }
            } else {
                psPrint(ps, "Content-length: "+(Support_TestWebData.testServerResponse[Support_TestWebData.REDIRECT_301]).length());
                psWriteEOL(ps);
                psWriteEOL(ps);
            }
            log("Headers sent");
        }
        void send404(PrintStream ps) throws IOException {
            ps.println("Not Found\n\n"+
                       "The requested resource was not found.\n");
        }
        void sendFile(PrintStream ps) throws IOException {
            if (testNum == -1) {
                if (!Support_TestWebData.test0DataAvailable) {
                    log("test data was not initialized");
                    return;
                }
                sendFile(ps, Support_TestWebData.test0Data);
            } else {
                sendFile(ps, Support_TestWebData.tests[testNum]);
            }
        }
        void sendFile(PrintStream ps, byte[] bytes) throws IOException {
            if (chunked) {
                int offset = 0;
                while (offset < bytes.length) {
                    int chunkSize = Math.min(bytes.length - offset, maxChunkSize);
                    psPrint(ps, Integer.toHexString(chunkSize));
                    psWriteEOL(ps);
                    psWrite(ps, bytes, offset, chunkSize);
                    psWriteEOL(ps);
                    offset += chunkSize;
                }
                psPrint(ps, "0");
                psWriteEOL(ps);
                psWriteEOL(ps);
            } else {
                psWrite(ps, bytes, 0, bytes.length);
            }
        }
    }
}
