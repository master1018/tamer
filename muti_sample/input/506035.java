public class HttpURLConnectionImpl extends HttpURLConnection {
    private static final String POST = "POST";
    private static final String GET = "GET";
    private static final String PUT = "PUT";
    private static final String HEAD = "HEAD";
    private static final byte[] CRLF = new byte[] { '\r', '\n' };
    private static final byte[] HEX_DIGITS = new byte[] {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };
    private final int defaultPort;
    private int httpVersion = 1; 
    protected HttpConnection connection;
    private InputStream is;
    private InputStream uis;
    private OutputStream socketOut;
    private OutputStream cacheOut;
    private ResponseCache responseCache;
    private CacheResponse cacheResponse;
    private CacheRequest cacheRequest;
    private boolean hasTriedCache;
    private HttpOutputStream os;
    private boolean sentRequest;
    boolean sendChunked;
    private String proxyName;
    private int hostPort = -1;
    private String hostName;
    private InetAddress hostAddress;
    private Proxy proxy;
    private URI uri;
    private static Header defaultReqHeader = new Header();
    private Header reqHeader;
    private Header resHeader;
    private class LocalCloseInputStream extends InputStream {
        private boolean closed;
        public LocalCloseInputStream() {
            closed = false;
        }
        public int read() throws IOException {
            if (closed) {
                throwClosed();
            }
            int result = is.read();
            if (useCaches && cacheOut != null) {
                cacheOut.write(result);
            }
            return result;
        }
        public int read(byte[] b, int off, int len) throws IOException {
            if (closed) {
                throwClosed();
            }
            int result = is.read(b, off, len);
            if (result > 0) {
                if (useCaches && cacheOut != null) {
                    cacheOut.write(b, off, result);
                }
            }
            return result;
        }
        public int read(byte[] b) throws IOException {
            if (closed) {
                throwClosed();
            }
            int result = is.read(b);
            if (result > 0) {
                if (useCaches && cacheOut != null) {
                    cacheOut.write(b, 0, result);
                }
            }
            return result;
        }
        public long skip(long n) throws IOException {
            if (closed) {
                throwClosed();
            }
            return is.skip(n);
        }
        public int available() throws IOException {
            if (closed) {
                throwClosed();
            }
            return is.available();
        }
        public void close() {
            closed = true;
            if (useCaches && cacheRequest != null) {
                cacheRequest.abort();
            }
        }
        public void mark(int readLimit) {
            if (! closed) {
                is.mark(readLimit);
            }
        }
        public void reset() throws IOException {
            if (closed) {
                throwClosed();
            }
            is.reset();
        }
        public boolean markSupported() {
            return is.markSupported();
        }
        private void throwClosed() throws IOException {
            throw new IOException("stream closed");
        }
    }
    private class LimitedInputStream extends InputStream {
        int bytesRemaining;
        public LimitedInputStream(int length) {
            bytesRemaining = length;
        }
        @Override
        public void close() throws IOException {
            if(bytesRemaining > 0) {
                bytesRemaining = 0;
                disconnect(true); 
            } else {
                disconnect(false);
            }
            if (useCaches && null != cacheRequest) {
                cacheRequest.abort();
            }
        }
        @Override
        public int available() throws IOException {
            if (bytesRemaining <= 0) {
                return 0;
            }
            int result = is.available();
            if (result > bytesRemaining) {
                return bytesRemaining;
            }
            return result;
        }
        @Override
        public int read() throws IOException {
            if (bytesRemaining <= 0) {
                disconnect(false);
                return -1;
            }
            int result = is.read();
            if (useCaches && null != cacheOut) {
                cacheOut.write(result);
            }
            bytesRemaining--;
            if (bytesRemaining <= 0) {
                disconnect(false);
            }
            return result;
        }
        @Override
        public int read(byte[] buf, int offset, int length) throws IOException {
            if (offset < 0 || offset > buf.length) {
                throw new ArrayIndexOutOfBoundsException(Msg.getString("K002e", offset));
            }
            if (length < 0 || buf.length - offset < length) {
                throw new ArrayIndexOutOfBoundsException(Msg.getString("K0031", length));
            }
            if (bytesRemaining <= 0) {
                disconnect(false);
                return -1;
            }
            if (length > bytesRemaining) {
                length = bytesRemaining;
            }
            int result = is.read(buf, offset, length);
            if (result > 0) {
                bytesRemaining -= result;
                if (useCaches && null != cacheOut) {
                    cacheOut.write(buf, offset, result);
                }
            }
            if (bytesRemaining <= 0) {
                disconnect(false);
            }
            return result;
        }
        public long skip(int amount) throws IOException {
            if (bytesRemaining <= 0) {
                disconnect(false);
                return -1;
            }
            if (amount > bytesRemaining) {
                amount = bytesRemaining;
            }
            long result = is.skip(amount);
            if (result > 0) {
                bytesRemaining -= result;
            }
            if (bytesRemaining <= 0) {
                disconnect(false);
            }
            return result;
        }
    }
    private class ChunkedInputStream extends InputStream {
        private int bytesRemaining = -1;
        private boolean atEnd;
        public ChunkedInputStream() throws IOException {
            readChunkSize();
        }
        @Override
        public void close() throws IOException {
            if (atEnd) {
                return;
            }
            skipOutstandingChunks();
            if (available() > 0) {
                disconnect(true);
            } else {
                disconnect(false);
            }
            atEnd = true;
            if (useCaches && null != cacheRequest) {
                cacheRequest.abort();
            }
        }
        private void skipOutstandingChunks() throws IOException {
            while (!atEnd) {
                while (bytesRemaining > 0) {
                    long skipped = is.skip(bytesRemaining);
                    bytesRemaining -= skipped;
                }
                readChunkSize();
            }
        }
        @Override
        public int available() throws IOException {
            if (atEnd) {
                return 0;
            }
            int result = is.available();
            if (result > bytesRemaining) {
                return bytesRemaining;
            }
            return result;
        }
        private void readChunkSize() throws IOException {
            if (atEnd) {
                return;
            }
            if (bytesRemaining == 0) {
                readln(); 
            }
            String size = readln();
            int index = size.indexOf(";");
            if (index >= 0) {
                size = size.substring(0, index);
            }
            bytesRemaining = Integer.parseInt(size.trim(), 16);
            if (bytesRemaining == 0) {
                atEnd = true;
                readHeaders();
            }
        }
        @Override
        public int read() throws IOException {
            if (bytesRemaining <= 0) {
                readChunkSize();
            }
            if (atEnd) {
                disconnect(false);
                return -1;
            }
            bytesRemaining--;
            int result = is.read();
            if (useCaches && null != cacheOut) {
                cacheOut.write(result);
            }
            return result;
        }
        @Override
        public int read(byte[] buf, int offset, int length) throws IOException {
            if (offset > buf.length || offset < 0) {
                throw new ArrayIndexOutOfBoundsException(Msg.getString("K002e", offset));
            }
            if (length < 0 || buf.length - offset < length) {
                throw new ArrayIndexOutOfBoundsException(Msg.getString("K0031", length));
            }
            if (bytesRemaining <= 0) {
                readChunkSize();
            }
            if (atEnd) {
                disconnect(false);
                return -1;
            }
            if (length > bytesRemaining) {
                length = bytesRemaining;
            }
            int result = is.read(buf, offset, length);
            if (result > 0) {
                bytesRemaining -= result;
                if (useCaches && null != cacheOut) {
                    cacheOut.write(buf, offset, result);
                }
            }
            return result;
        }
        public long skip(int amount) throws IOException {
            if (atEnd) {
                return -1;
            }
            if (bytesRemaining <= 0) {
                readChunkSize();
            }
            if (atEnd) {
                disconnect(false);
                return -1;
            }
            if (amount > bytesRemaining) {
                amount = bytesRemaining;
            }
            long result = is.skip(amount);
            if (result > 0) {
                bytesRemaining -= result;
            }
            return result;
        }
    }
    private class FixedLengthHttpOutputStream extends HttpOutputStream {
        private final int fixedLength;
        private int actualLength;
        public FixedLengthHttpOutputStream(int fixedLength) {
            this.fixedLength = fixedLength;
        }
        @Override public void close() throws IOException {
            if (closed) {
                return;
            }
            closed = true;
            socketOut.flush();
            if (actualLength != fixedLength) {
                throw new IOException("actual length of " + actualLength +
                        " did not match declared fixed length of " + fixedLength);
            }
        }
        @Override public void flush() throws IOException {
            checkClosed();
            socketOut.flush();
        }
        @Override public void write(byte[] buffer, int offset, int count) throws IOException {
            checkClosed();
            if (buffer == null) {
                throw new NullPointerException();
            }
            if (offset < 0 || count < 0 || offset > buffer.length || buffer.length - offset < count) {
                throw new ArrayIndexOutOfBoundsException(Msg.getString("K002f"));
            }
            checkSpace(count);
            socketOut.write(buffer, offset, count);
            actualLength += count;
        }
        @Override public void write(int oneByte) throws IOException {
            checkClosed();
            checkSpace(1);
            socketOut.write(oneByte);
            ++actualLength;
        }
        @Override public int size() {
            return fixedLength;
        }
        private void checkSpace(int byteCount) throws IOException {
            if (actualLength + byteCount > fixedLength) {
                throw new IOException("declared fixed content length of " + fixedLength +
                        " bytes exceeded");
            }
        }
    }
    private abstract class HttpOutputStream extends OutputStream {
        public boolean closed;
        protected void checkClosed() throws IOException {
            if (closed) {
                throw new IOException(Msg.getString("K0059"));
            }
        }
        public boolean isCached() {
            return false;
        }
        public boolean isChunked() {
            return false;
        }
        public void flushToSocket() throws IOException {
        }
        public abstract int size();
    }
    private static final byte[] FINAL_CHUNK = new byte[] { '0', '\r', '\n', '\r', '\n' };
    private class DefaultHttpOutputStream extends HttpOutputStream {
        private int cacheLength;
        private int defaultCacheSize = 1024;
        private ByteArrayOutputStream cache;
        private boolean writeToSocket;
        private int limit;
        public DefaultHttpOutputStream() {
            cacheLength = defaultCacheSize;
            cache = new ByteArrayOutputStream(cacheLength);
            limit = -1;
        }
        public DefaultHttpOutputStream(int limit, int chunkLength) {
            writeToSocket = true;
            this.limit = limit;
            if (limit > 0) {
                cacheLength = limit;
            } else {
                if (chunkLength > 3) {
                    defaultCacheSize = chunkLength;
                }
                cacheLength = calculateChunkDataLength();
            }
            cache = new ByteArrayOutputStream(cacheLength);
        }
        private int calculateChunkDataLength() {
            int bitSize = Integer.toHexString(defaultCacheSize).length();
            int headSize = (Integer.toHexString(defaultCacheSize - bitSize - 2).length()) + 2;
            return defaultCacheSize - headSize;
        }
        private void writeHex(int i) throws IOException {
            int cursor = 8;
            do {
                hex[--cursor] = HEX_DIGITS[i & 0xf];
            } while ((i >>>= 4) != 0);
            socketOut.write(hex, cursor, 8 - cursor);
        }
        private byte[] hex = new byte[8];
        private void sendCache(boolean close) throws IOException {
            int size = cache.size();
            if (size > 0 || close) {
                if (limit < 0) {
                    if (size > 0) {
                        writeHex(size);
                        socketOut.write(CRLF);
                        cache.writeTo(socketOut);
                        cache.reset();
                        socketOut.write(CRLF);
                    }
                    if (close) {
                        socketOut.write(FINAL_CHUNK);
                    }
                }
            }
        }
        @Override
        public synchronized void flush() throws IOException {
            checkClosed();
            if (writeToSocket) {
                sendCache(false);
                socketOut.flush();
            }
        }
        @Override
        public void flushToSocket() throws IOException  {
            if (isCached()) {
                cache.writeTo(socketOut);
            }
        }
        @Override
        public synchronized void close() throws IOException {
            if (closed) {
                return;
            }
            closed = true;
            if (writeToSocket) {
                if (limit > 0) {
                    throw new IOException(Msg.getString("K00a4"));
                }
                sendCache(closed);
            }
        }
        @Override
        public synchronized void write(int data) throws IOException {
            checkClosed();
            if (limit >= 0) {
                if (limit == 0) {
                    throw new IOException(Msg.getString("K00b2"));
                }
                limit--;
            }
            cache.write(data);
            if (writeToSocket && cache.size() >= cacheLength) {
                sendCache(false);
            }
        }
        @Override
        public synchronized void write(byte[] buffer, int offset, int count) throws IOException {
            checkClosed();
            if (buffer == null) {
                throw new NullPointerException();
            }
            if (offset < 0 || count < 0 || offset > buffer.length
                    || buffer.length - offset < count) {
                throw new ArrayIndexOutOfBoundsException(Msg.getString("K002f"));
            }
            if (limit >= 0) {
                if (count > limit) {
                    throw new IOException(Msg.getString("K00b2"));
                }
                limit -= count;
                cache.write(buffer, offset, count);
                if (limit == 0) {
                    cache.writeTo(socketOut);
                }
            } else {
                if (!writeToSocket || cache.size() + count < cacheLength) {
                    cache.write(buffer, offset, count);
                } else {
                    writeHex(cacheLength);
                    socketOut.write(CRLF);
                    int writeNum = cacheLength - cache.size();
                    cache.write(buffer, offset, writeNum);
                    cache.writeTo(socketOut);
                    cache.reset();
                    socketOut.write(CRLF);
                    int left = count - writeNum;
                    int position = offset + writeNum;
                    while (left > cacheLength) {
                        writeHex(cacheLength);
                        socketOut.write(CRLF);
                        socketOut.write(buffer, position, cacheLength);
                        socketOut.write(CRLF);
                        left = left - cacheLength;
                        position = position + cacheLength;
                    }
                    cache.write(buffer, position, left);
                }
            }
        }
        @Override
        public synchronized int size() {
            return cache.size();
        }
        @Override public boolean isCached() {
            return !writeToSocket;
        }
        @Override public boolean isChunked() {
            return writeToSocket && limit == -1;
        }
    }
    protected HttpURLConnectionImpl(URL url) {
        this(url, 80);
    }
    protected HttpURLConnectionImpl(URL url, int port) {
        super(url);
        defaultPort = port;
        reqHeader = (Header) defaultReqHeader.clone();
        try {
            uri = url.toURI();
        } catch (URISyntaxException e) {
        }
        responseCache = AccessController
                .doPrivileged(new PrivilegedAction<ResponseCache>() {
                    public ResponseCache run() {
                        return ResponseCache.getDefault();
                    }
                });
    }
    protected HttpURLConnectionImpl(URL url, int port, Proxy proxy) {
        this(url, port);
        this.proxy = proxy;
    }
    @Override
    public void connect() throws IOException {
        if (connected) {
            return;
        }
        if (getFromCache()) {
            return;
        }
        try {
            uri = new URI(url.getProtocol(),
                          null,
                          url.getHost(),
                          url.getPort(),
                          null,
                          null,
                          null);
        } catch (URISyntaxException e1) {
            throw new IOException(e1.getMessage());
        }
        connection = null;
        if (proxy != null) {
            connection = getHTTPConnection(proxy);
        } else {
            ProxySelector selector = ProxySelector.getDefault();
            List<Proxy> proxyList = selector.select(uri);
            if (proxyList != null) {
                for (Proxy selectedProxy : proxyList) {
                    if (selectedProxy.type() == Proxy.Type.DIRECT) {
                        continue;
                    }
                    try {
                        connection = getHTTPConnection(selectedProxy);
                        proxy = selectedProxy;
                        break; 
                    } catch (IOException e) {
                        selector.connectFailed(uri, selectedProxy.address(), e);
                    }
                }
            }
        }
        if (connection == null) {
            connection = getHTTPConnection(null);
        }
        connection.setSoTimeout(getReadTimeout());
        setUpTransportIO(connection);
        connected = true;
    }
    protected HttpConnection getHTTPConnection(Proxy proxy) throws IOException {
        HttpConfiguration configuration;
        if (proxy == null || proxy.type() == Proxy.Type.DIRECT) {
            this.proxy = null; 
            configuration = new HttpConfiguration(uri);
        } else {
            configuration = new HttpConfiguration(uri, proxy);
        }
        return HttpConnectionPool.INSTANCE.get(configuration, getConnectTimeout());
    }
    protected void setUpTransportIO(HttpConnection connection) throws IOException {
        socketOut = connection.getOutputStream();
        is = connection.getInputStream();
    }
    private boolean getFromCache() throws IOException {
        if (useCaches && null != responseCache && !hasTriedCache) {
            hasTriedCache = true;
            if (null == resHeader) {
                resHeader = new Header();
            }
            cacheResponse = responseCache.get(uri, method, resHeader
                    .getFieldMap());
            if (null != cacheResponse) {
                Map<String, List<String>> headMap = cacheResponse.getHeaders();
                if (null != headMap) {
                    resHeader = new Header(headMap);
                }
                is = cacheResponse.getBody();
                if (null != is) {
                    return true;
                }
            }
        }
        if (hasTriedCache && null != is) {
            return true;
        }
        return false;
    }
    private void putToCache() throws IOException {
        if (useCaches && null != responseCache) {
            cacheRequest = responseCache.put(uri, this);
            if (null != cacheRequest) {
                cacheOut = cacheRequest.getBody();
            }
        }
    }
    @Override
    public void disconnect() {
        disconnect(true);
    }
    private synchronized void disconnect(boolean closeSocket) {
        if (connection != null) {
            if (closeSocket || ((os != null) && !os.closed)) {
                connection.closeSocketAndStreams();
            } else {
                HttpConnectionPool.INSTANCE.recycle(connection);
            }
            connection = null;
        }
        is = null;
        os = null;
    }
    protected void endRequest() throws IOException {
        if (os != null) {
            os.close();
        }
        sentRequest = false;
    }
    public static String getDefaultRequestProperty(String field) {
        return defaultReqHeader.get(field);
    }
    @Override
    public InputStream getErrorStream() {
        if (connected && method != HEAD && responseCode >= HTTP_BAD_REQUEST) {
            return uis;
        }
        return null;
    }
    @Override
    public String getHeaderField(int pos) {
        try {
            getInputStream();
        } catch (IOException e) {
        }
        if (null == resHeader) {
            return null;
        }
        return resHeader.get(pos);
    }
    @Override
    public String getHeaderField(String key) {
        try {
            getInputStream();
        } catch (IOException e) {
        }
        if (null == resHeader) {
            return null;
        }
        return resHeader.get(key);
    }
    @Override
    public String getHeaderFieldKey(int pos) {
        try {
            getInputStream();
        } catch (IOException e) {
        }
        if (null == resHeader) {
            return null;
        }
        return resHeader.getKey(pos);
    }
    @Override
    public Map<String, List<String>> getHeaderFields() {
        try {
            getInputStream();
        } catch (IOException e) {
        }
        if (null == resHeader) {
            return null;
        }
        return resHeader.getFieldMap();
    }
    @Override
    public Map<String, List<String>> getRequestProperties() {
        if (connected) {
            throw new IllegalStateException(Msg.getString("K0091"));
        }
        return reqHeader.getFieldMap();
    }
    @Override
    public InputStream getInputStream() throws IOException {
        if (!doInput) {
            throw new ProtocolException(Msg.getString("K008d"));
        }
        connect();
        doRequest();
        if (responseCode >= HTTP_BAD_REQUEST) {
            throw new FileNotFoundException(url.toString());
        }
        return uis;
    }
    private InputStream getContentStream() throws IOException {
        if (uis != null) {
            return uis;
        }
        String encoding = resHeader.get("Transfer-Encoding");
        if (encoding != null && encoding.toLowerCase().equals("chunked")) {
            return uis = new ChunkedInputStream();
        }
        String sLength = resHeader.get("Content-Length");
        if (sLength != null) {
            try {
                int length = Integer.parseInt(sLength);
                return uis = new LimitedInputStream(length);
            } catch (NumberFormatException e) {
            }
        }
        return uis = new LocalCloseInputStream();
    }
    @Override
    public OutputStream getOutputStream() throws IOException {
        if (!doOutput) {
            throw new ProtocolException(Msg.getString("K008e"));
        }
        if (sentRequest) {
            throw new ProtocolException(Msg.getString("K0090"));
        }
        if (os != null) {
            return os;
        }
        if (method == GET) {
            method = POST;
        }
        if (method != PUT && method != POST) {
            throw new ProtocolException(Msg.getString("K008f", method));
        }
        int limit = -1;
        String contentLength = reqHeader.get("Content-Length");
        if (contentLength != null) {
            limit = Integer.parseInt(contentLength);
        }
        String encoding = reqHeader.get("Transfer-Encoding");
        if (httpVersion > 0 && encoding != null) {
            encoding = encoding.toLowerCase();
            if ("chunked".equals(encoding)) {
                sendChunked = true;
                limit = -1;
            }
        }
        if (chunkLength > 0) {
            sendChunked = true;
            limit = -1;
        }
        if (fixedContentLength >= 0) {
            os = new FixedLengthHttpOutputStream(fixedContentLength);
            doRequest();
            return os;
        }
        if ((httpVersion > 0 && sendChunked) || limit >= 0) {
            os = new DefaultHttpOutputStream(limit, chunkLength);
            doRequest();
            return os;
        }
        if (!connected) {
            connect();
        }
        return os = new DefaultHttpOutputStream();
    }
    @Override
    public Permission getPermission() throws IOException {
        return new SocketPermission(getHostName() + ":" + getHostPort(), "connect, resolve");
    }
    @Override
    public String getRequestProperty(String field) {
        if (null == field) {
            return null;
        }
        return reqHeader.get(field);
    }
    String readln() throws IOException {
        boolean lastCr = false;
        StringBuilder result = new StringBuilder(80);
        int c = is.read();
        if (c < 0) {
            return null;
        }
        while (c != '\n') {
            if (lastCr) {
                result.append('\r');
                lastCr = false;
            }
            if (c == '\r') {
                lastCr = true;
            } else {
                result.append((char) c);
            }
            c = is.read();
            if (c < 0) {
                break;
            }
        }
        return result.toString();
    }
    protected String requestString() {
        if (usingProxy() || proxyName != null) {
            return url.toString();
        }
        String file = url.getFile();
        if (file == null || file.length() == 0) {
            file = "/";
        }
        return file;
    }
    private boolean sendRequest() throws IOException {
        byte[] request = createRequest();
        if (!connected) {
            connect();
        }
        if (null != cacheResponse) {
            return true;
        }
        socketOut.write(request);
        sentRequest = true;
        if (os != null) {
            os.flushToSocket();
        }
        if (os == null || os.isCached()) {
            readServerResponse();
            return true;
        }
        return false;
    }
    void readServerResponse() throws IOException {
        socketOut.flush();
        do {
            responseCode = -1;
            responseMessage = null;
            resHeader = new Header();
            String line = readln();
            if (line != null) {
                resHeader.setStatusLine(line.trim());
                readHeaders();
            }
        } while (getResponseCode() == 100);
        if (method == HEAD || (responseCode >= 100 && responseCode < 200)
                || responseCode == HTTP_NO_CONTENT
                || responseCode == HTTP_NOT_MODIFIED) {
            disconnect();
            uis = new LimitedInputStream(0);
        }
        putToCache();
    }
    @Override
    public int getResponseCode() throws IOException {
        connect();
        doRequest();
        if (responseCode != -1) {
            return responseCode;
        }
        String response = resHeader.getStatusLine();
        if (response == null || !response.startsWith("HTTP/")) {
            return -1;
        }
        response = response.trim();
        int mark = response.indexOf(" ") + 1;
        if (mark == 0) {
            return -1;
        }
        if (response.charAt(mark - 2) != '1') {
            httpVersion = 0;
        }
        int last = mark + 3;
        if (last > response.length()) {
            last = response.length();
        }
        responseCode = Integer.parseInt(response.substring(mark, last));
        if (last + 1 <= response.length()) {
            responseMessage = response.substring(last + 1);
        }
        return responseCode;
    }
    void readHeaders() throws IOException {
        String line;
        while (((line = readln()) != null) && (line.length() > 1)) {
            int idx;
            if ((idx = line.indexOf(":")) < 0) {
                resHeader.add("", line.trim());
            } else {
                resHeader.add(line.substring(0, idx), line.substring(idx + 1).trim());
            }
        }
    }
    private byte[] createRequest() throws IOException {
        StringBuilder output = new StringBuilder(256);
        output.append(method);
        output.append(' ');
        output.append(requestString());
        output.append(' ');
        output.append("HTTP/1.");
        if (httpVersion == 0) {
            output.append("0\r\n");
        } else {
            output.append("1\r\n");
        }
        boolean hasContentLength = false;
        for (int i = 0; i < reqHeader.length(); i++) {
            String key = reqHeader.getKey(i);
            if (key != null) {
                String lKey = key.toLowerCase();
                if ((os != null && !os.isChunked())
                        || (!lKey.equals("transfer-encoding") && !lKey.equals("content-length"))) {
                    output.append(key);
                    String value = reqHeader.get(i);
                    if (lKey.equals("content-length")) {
                        hasContentLength = true;
                        if(fixedContentLength >= 0){
                            value = String.valueOf(fixedContentLength);
                        }
                    }
                    if (value != null) {
                        output.append(": ");
                        output.append(value);
                    }
                    output.append("\r\n");
                }
            }
        }
        if (fixedContentLength >= 0 && !hasContentLength) {
            output.append("content-length: ");
            output.append(String.valueOf(fixedContentLength));
            output.append("\r\n");
        }
        if (reqHeader.get("User-Agent") == null) {
            output.append("User-Agent: ");
            String agent = getSystemProperty("http.agent");
            if (agent == null) {
                output.append("Java");
                output.append(getSystemProperty("java.version"));
            } else {
                output.append(agent);
            }
            output.append("\r\n");
        }
        if (reqHeader.get("Host") == null) {
            output.append("Host: ");
            output.append(url.getHost());
            int port = url.getPort();
            if (port > 0 && port != defaultPort) {
                output.append(':');
                output.append(Integer.toString(port));
            }
            output.append("\r\n");
        }
        if (httpVersion > 0 && reqHeader.get("Connection") == null) {
            output.append("Connection: Keep-Alive\r\n");
        }
        if (os != null) {
            if (reqHeader.get("Content-Type") == null) {
                output.append("Content-Type: application/x-www-form-urlencoded\r\n");
            }
            if (os.isCached()) {
                if (reqHeader.get("Content-Length") == null) {
                    output.append("Content-Length: ");
                    output.append(Integer.toString(os.size()));
                    output.append("\r\n");
                }
            } else if (os.isChunked()) {
                output.append("Transfer-Encoding: chunked\r\n");
            }
        }
        output.append("\r\n");
        return output.toString().getBytes("ISO8859_1");
    }
    public static void setDefaultRequestProperty(String field, String value) {
        defaultReqHeader.add(field, value);
    }
    @Override
    public void setIfModifiedSince(long newValue) {
        super.setIfModifiedSince(newValue);
        SimpleDateFormat sdf = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = sdf.format(new Date(newValue));
        reqHeader.add("If-Modified-Since", date);
    }
    @Override
    public void setRequestProperty(String field, String newValue) {
        if (connected) {
            throw new IllegalStateException(Msg.getString("K0092"));
        }
        if (field == null) {
            throw new NullPointerException();
        }
        reqHeader.set(field, newValue);
    }
    @Override
    public void addRequestProperty(String field, String value) {
        if (connected) {
            throw new IllegalAccessError(Msg.getString("K0092"));
        }
        if (field == null) {
            throw new NullPointerException();
        }
        reqHeader.add(field, value);
    }
    private int getHostPort() {
        if (hostPort < 0) {
            if (proxy != null) {
                hostPort = ((InetSocketAddress) proxy.address()).getPort();
            } else {
                hostPort = url.getPort();
            }
            if (hostPort < 0) {
                hostPort = defaultPort;
            }
        }
        return hostPort;
    }
    private InetAddress getHostAddress() throws IOException {
        if (hostAddress == null) {
            if (proxy != null && proxy.type() != Proxy.Type.DIRECT) {
                hostAddress = ((InetSocketAddress) proxy.address())
                        .getAddress();
            } else {
                hostAddress = InetAddress.getByName(url.getHost());
            }
        }
        return hostAddress;
    }
    private String getHostName() {
        if (hostName == null) {
            if (proxy != null) {
                hostName = ((InetSocketAddress) proxy.address()).getHostName();
            } else {
                hostName = url.getHost();
            }
        }
        return hostName;
    }
    private String getSystemProperty(final String property) {
        return AccessController.doPrivileged(new PriviAction<String>(property));
    }
    @Override
    public boolean usingProxy() {
        return (proxy != null && proxy.type() != Proxy.Type.DIRECT);
    }
    protected void doRequest() throws IOException {
        if (sentRequest) {
            if (resHeader == null && os != null) {
                os.close();
                readServerResponse();
                getContentStream();
            }
            return;
        }
        doRequestInternal();
    }
    void doRequestInternal() throws IOException {
        int redirect = 0;
        while (true) {
            if (!sendRequest()) {
                return;
            }
            if (responseCode == HTTP_PROXY_AUTH) {
                if (!usingProxy()) {
                    throw new IOException(Msg.getString("KA017"));
                }
                String challenge = resHeader.get("Proxy-Authenticate");
                if (challenge == null) {
                    throw new IOException(Msg.getString("KA016"));
                }
                endRequest();
                disconnect();
                connected = false;
                String credentials = getAuthorizationCredentials(challenge);
                if (credentials == null) {
                    break;
                }
                setRequestProperty("Proxy-Authorization", credentials);
                continue;
            }
            if (responseCode == HTTP_UNAUTHORIZED) {
                String challenge = resHeader.get("WWW-Authenticate");
                if (challenge == null) {
                    throw new IOException(Msg.getString("KA018"));
                }
                endRequest();
                disconnect();
                connected = false;
                String credentials = getAuthorizationCredentials(challenge);
                if (credentials == null) {
                    break;
                }
                setRequestProperty("Authorization", credentials);
                continue;
            }
            if (getInstanceFollowRedirects()) {
                if ((responseCode == HTTP_MULT_CHOICE
                        || responseCode == HTTP_MOVED_PERM
                        || responseCode == HTTP_MOVED_TEMP
                        || responseCode == HTTP_SEE_OTHER || responseCode == HTTP_USE_PROXY)
                        && os == null) {
                    if (++redirect > 4) {
                        throw new ProtocolException(Msg.getString("K0093"));
                    }
                    String location = getHeaderField("Location");
                    if (location != null) {
                        if (responseCode == HTTP_USE_PROXY) {
                            int start = 0;
                            if (location.startsWith(url.getProtocol() + ':')) {
                                start = url.getProtocol().length() + 1;
                            }
                            if (location.startsWith("
                                start += 2;
                            }
                            setProxy(location.substring(start));
                        } else {
                            url = new URL(url, location);
                            hostName = url.getHost();
                            hostPort = -1;
                        }
                        endRequest();
                        disconnect();
                        connected = false;
                        continue;
                    }
                }
            }
            break;
        }
        getContentStream();
    }
    private String getAuthorizationCredentials(String challenge)
            throws IOException {
        int idx = challenge.indexOf(" ");
        String scheme = challenge.substring(0, idx);
        int realm = challenge.indexOf("realm=\"") + 7;
        String prompt = null;
        if (realm != -1) {
            int end = challenge.indexOf('"', realm);
            if (end != -1) {
                prompt = challenge.substring(realm, end);
            }
        }
        PasswordAuthentication pa = Authenticator
                .requestPasswordAuthentication(getHostAddress(), getHostPort(),
                        url.getProtocol(), prompt, scheme);
        if (pa == null) {
            return null;
        }
        byte[] bytes = (pa.getUserName() + ":" + new String(pa.getPassword()))
                .getBytes("ISO8859_1");
        String encoded = Base64.encode(bytes, "ISO8859_1");
        return scheme + " " + encoded;
    }
    private void setProxy(String proxy) {
        int index = proxy.indexOf(':');
        if (index == -1) {
            proxyName = proxy;
            hostPort = defaultPort;
        } else {
            proxyName = proxy.substring(0, index);
            String port = proxy.substring(index + 1);
            try {
                hostPort = Integer.parseInt(port);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(Msg.getString("K00af", port));
            }
            if (hostPort < 0 || hostPort > 65535) {
                throw new IllegalArgumentException(Msg.getString("K00b0"));
            }
        }
    }
}
