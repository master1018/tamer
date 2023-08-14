class TestWebServer implements HttpConstants {
    private final static String LOGTAG = "httpsv";
    Vector threads = new Vector();
    Vector activeThreads = new Vector();
    int timeout = 0;
    int workers = 5;
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
    String redirectHost = null;
    int redirectCode = -1;
    int acceptLimit = 100;
    int acceptedConnections = 0;
    public TestWebServer() {
    }
    public void initServer(boolean log) throws Exception {
        initServer(DEFAULT_PORT, DEFAULT_TIMEOUT, log);
    }
    public void initServer(int port, boolean log) throws Exception {
        initServer(port, DEFAULT_TIMEOUT, log);
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
            Log.d(LOGTAG, s);
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
    public void setAcceptLimit(int limit) {
        acceptLimit = limit;
    }
    public void setRedirect(String redirect, int code) {
        redirectHost = redirect;
        redirectCode = code;
        log("Server will redirect output to "+redirect+" code "+code);
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
                    e.printStackTrace();
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
            try {
                while (running) {
                    Socket s = ss.accept();
                    acceptedConnections++;
                    if (acceptedConnections >= acceptLimit) {
                        running = false;
                    }
                    Worker w = null;
                    synchronized (threads) {
                        if (threads.isEmpty()) {
                            Worker ws = new Worker();
                            ws.setSocket(s);
                            activeThreads.addElement(ws);
                            (new Thread(ws, "additional worker")).start();
                        } else {
                            w = (Worker) threads.elementAt(0);
                            threads.removeElementAt(0);
                            w.setSocket(s);
                        }
                    }
                }
            } catch (SocketException e) {
                log("SocketException in AcceptThread: probably closed during accept");
                running = false;
            } catch (IOException e) {
                log("IOException in AcceptThread");
                e.printStackTrace();
                running = false;
            }
            log("AcceptThread terminated" + this);
        }
        public void close() {
            try {
                running = false;
                ss.close();
                for (Enumeration e = activeThreads.elements(); e.hasMoreElements();) {
                    Worker w = (Worker)e.nextElement();
                    w.close();
                }
                activeThreads.clear();
            } catch (IOException e) {
                log("IOException caught by server socket close");
            }
        }
    }
    final static int BUF_SIZE = 2048;
    static final byte[] EOL = {(byte)'\r', (byte)'\n' };
    class Worker implements HttpConstants, Runnable {
        byte[] buf;
        private Socket s;
        private int requestMethod;
        private String testID;
        private int testNum;
        private boolean readStarted;
        private boolean hasContent = false;
        boolean running = false;
        private Hashtable<String, String> headers = new Hashtable<String, String>();
        Worker() {
            buf = new byte[BUF_SIZE];
            s = null;
        }
        synchronized void setSocket(Socket s) {
            this.s = s;
            notify();
        }
        synchronized void close() {
            running = false;
            notify();
        }
        public synchronized void run() {
            running = true;
            while(running) {
                if (s == null) {
                    try {
                        log(this+" Moving to wait state");
                        wait();
                    } catch (InterruptedException e) {
                        continue;
                    }
                    if (!running) break;
                }
                try {
                    handleClient();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                s = null;
                Vector pool = threads;
                synchronized (pool) {
                    if (pool.size() >= workers) {
                        activeThreads.remove(this);
                        return;
                    } else {
                        pool.addElement(this);
                    }
                }
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
                while ((!readStarted) || (is.available() > 0)) {
                    int data = is.read();
                    if (data == -1) {
                        log("EOF returned");
                        return -1;
                    }
                    buf[read] = (byte)data;
                    System.out.print((char)data);
                    readStarted = true;
                    if (buf[read++]==(byte)'\n') {
                        System.out.println();
                        return read;
                    }
                }
            } catch (IOException e) {
                log("IOException from readOneLine");
                e.printStackTrace();
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
                e.printStackTrace();
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
                testID = new String(buf, 0, index, i-index);
                if (testID.startsWith("/")) {
                    testID = testID.substring(1);
                }
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
            String headerValue = new String(buf, i, nread-1);
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
                            if ((testNum < 0) || (testNum > TestWebData.tests.length - 1)) {
                                send404(ps);
                            } else {
                                sendFile(ps);
                            }
                            break;
                        case HEAD_METHOD:
                            break;
                        case POST_METHOD:
                            if ((testNum > 0) || (testNum < TestWebData.tests.length - 1)) {
                                sendFile(ps);
                            }
                            break;
                        default:
                            break;
                    }
                } else { 
                    switch (redirectCode) {
                        case 301:
                            psPrint(ps, TestWebData.testServerResponse[TestWebData.REDIRECT_301]);
                            break;
                        case 302:
                            psPrint(ps, TestWebData.testServerResponse[TestWebData.REDIRECT_302]);
                            break;
                        case 303:
                            psPrint(ps, TestWebData.testServerResponse[TestWebData.REDIRECT_303]);
                            break;
                        case 307:
                            psPrint(ps, TestWebData.testServerResponse[TestWebData.REDIRECT_307]);
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
        void psWrite(PrintStream ps, byte[] bytes, int len) throws IOException {
            log(new String(bytes));
            ps.write(bytes, 0, len);
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
            if ((testNum < 0) || (testNum > TestWebData.tests.length - 1)) {
                psPrint(ps, HTTP_VERSION_STRING + " " + HTTP_NOT_FOUND + " not found");
                psWriteEOL(ps);
            }  else {
                psPrint(ps, HTTP_VERSION_STRING + " " + HTTP_OK+" OK");
                psWriteEOL(ps);
            }
            log("Status sent");
        }
        void printHeaders(PrintStream ps) throws IOException {
            psPrint(ps,"Server: TestWebServer"+mPort);
            psWriteEOL(ps);
            psPrint(ps, "Date: " + (new Date()));
            psWriteEOL(ps);
            psPrint(ps, "Connection: " + ((keepAlive) ? "Keep-Alive" : "Close"));
            psWriteEOL(ps);
            if (redirectCode == -1) {
                if (!TestWebData.testParams[testNum].testDir) {
                    if (chunked) {
                        psPrint(ps, "Transfer-Encoding: chunked");
                    } else {
                        psPrint(ps, "Content-length: "+TestWebData.testParams[testNum].testLength);
                    }
                    psWriteEOL(ps);
                    psPrint(ps,"Last Modified: " + (new
                                                    Date(TestWebData.testParams[testNum].testLastModified)));
                    psWriteEOL(ps);
                    psPrint(ps, "Content-type: " + TestWebData.testParams[testNum].testType);
                    psWriteEOL(ps);
                } else {
                    psPrint(ps, "Content-type: text/html");
                    psWriteEOL(ps);
                }
            } else {
                psPrint(ps, "Content-length: "+(TestWebData.testServerResponse[TestWebData.REDIRECT_301]).length());
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
            int dataSize = TestWebData.tests[testNum].length;
            if (chunked) {
                psPrint(ps, Integer.toHexString(dataSize));
                psWriteEOL(ps);
                psWrite(ps, TestWebData.tests[testNum], dataSize);
                psWriteEOL(ps);
                psPrint(ps, "0");
                psWriteEOL(ps);
                psWriteEOL(ps);
            } else {
                psWrite(ps, TestWebData.tests[testNum], dataSize);
            }
        }
    }
}
