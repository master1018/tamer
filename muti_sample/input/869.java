public class HttpTransaction {
    String command;
    URI requesturi;
    HttpServer.ServerWorker server;
    MessageHeader reqheaders, reqtrailers;
    String reqbody;
    byte[] rspbody;
    MessageHeader rspheaders, rsptrailers;
    SocketChannel ch;
    int rspbodylen;
    boolean rspchunked;
    HttpTransaction (HttpServer.ServerWorker server, String command,
                        URI requesturi, MessageHeader headers,
                        String body, MessageHeader trailers, SocketChannel ch) {
        this.command = command;
        this.requesturi = requesturi;
        this.reqheaders = headers;
        this.reqbody = body;
        this.reqtrailers = trailers;
        this.ch = ch;
        this.server = server;
    }
    public String getRequestHeader (String key) {
        return reqheaders.findValue (key);
    }
    public String getResponseHeader (String key) {
        return rspheaders.findValue (key);
    }
    public URI getRequestURI () {
        return requesturi;
    }
    public String toString () {
        StringBuffer buf = new StringBuffer();
        buf.append ("Request from: ").append (ch.toString()).append("\r\n");
        buf.append ("Command: ").append (command).append("\r\n");
        buf.append ("Request URI: ").append (requesturi).append("\r\n");
        buf.append ("Headers: ").append("\r\n");
        buf.append (reqheaders.toString()).append("\r\n");
        buf.append ("Body: ").append (reqbody).append("\r\n");
        buf.append ("---------Response-------\r\n");
        buf.append ("Headers: ").append("\r\n");
        if (rspheaders != null) {
            buf.append (rspheaders.toString()).append("\r\n");
        }
        String rbody = rspbody == null? "": new String (rspbody);
        buf.append ("Body: ").append (rbody).append("\r\n");
        return new String (buf);
    }
    public String getRequestTrailer (String key) {
        return reqtrailers.findValue (key);
    }
    public void addResponseHeader (String key, String val) {
        if (rspheaders == null)
            rspheaders = new MessageHeader ();
        rspheaders.add (key, val);
    }
    public void setResponseHeader (String key, String val) {
        if (rspheaders == null)
            rspheaders = new MessageHeader ();
        rspheaders.set (key, val);
    }
    public void addResponseTrailer (String key, String val) {
        if (rsptrailers == null)
            rsptrailers = new MessageHeader ();
        rsptrailers.add (key, val);
    }
    public String getRequestMethod (){
        return command;
    }
    public void orderlyClose () {
        try {
            server.orderlyCloseChannel (ch);
        } catch (IOException e) {
            System.out.println (e);
        }
    }
    public void abortiveClose () {
        try {
            server.abortiveCloseChannel(ch);
        } catch (IOException e) {
            System.out.println (e);
        }
    }
    public SocketChannel channel() {
        return ch;
    }
    public String getRequestEntityBody (){
        return reqbody;
    }
    public void setResponseEntityBody (String body){
        rspbody = body.getBytes();
        rspbodylen = body.length();
        rspchunked = false;
        addResponseHeader ("Content-length", Integer.toString (rspbodylen));
    }
    public void setResponseEntityBody (byte[] body, int len){
        rspbody = body;
        rspbodylen = len;
        rspchunked = false;
        addResponseHeader ("Content-length", Integer.toString (rspbodylen));
    }
    public void setResponseEntityBody (InputStream is) throws IOException {
        byte[] buf = new byte [2048];
        byte[] total = new byte [2048];
        int total_len = 2048;
        int c, len=0;
        while ((c=is.read (buf)) != -1) {
            if (len+c > total_len) {
                byte[] total1 = new byte [total_len * 2];
                System.arraycopy (total, 0, total1, 0, len);
                total = total1;
                total_len = total_len * 2;
            }
            System.arraycopy (buf, 0, total, len, c);
            len += c;
        }
        setResponseEntityBody (total, len);
    }
    public void setResponseEntityBody (String[] body) {
        StringBuffer buf = new StringBuffer ();
        int len = 0;
        for (int i=0; i<body.length; i++) {
            String chunklen = Integer.toHexString (body[i].length());
            len += body[i].length();
            buf.append (chunklen).append ("\r\n");
            buf.append (body[i]).append ("\r\n");
        }
        buf.append ("0\r\n");
        rspbody = new String (buf).getBytes();
        rspbodylen = rspbody.length;
        rspchunked = true;
        addResponseHeader ("Transfer-encoding", "chunked");
    }
    public void sendResponse (int rCode, String rTag) throws IOException {
        OutputStream os = new HttpServer.NioOutputStream(channel(), server.getSSLEngine(), server.outNetBB(), server.outAppBB());
        PrintStream ps = new PrintStream (os);
        ps.print ("HTTP/1.1 " + rCode + " " + rTag + "\r\n");
        if (rspheaders != null) {
            rspheaders.print (ps);
        } else {
            ps.print ("\r\n");
        }
        ps.flush ();
        if (rspbody != null) {
            os.write (rspbody, 0, rspbodylen);
            os.flush();
        }
        if (rsptrailers != null) {
            rsptrailers.print (ps);
        } else if (rspchunked) {
            ps.print ("\r\n");
        }
        ps.flush();
    }
    public void sendPartialResponse (int rCode, String rTag)throws IOException {
        OutputStream os = new HttpServer.NioOutputStream(channel(), server.getSSLEngine(), server.outNetBB(), server.outAppBB());
        PrintStream ps = new PrintStream (os);
        ps.print ("HTTP/1.1 " + rCode + " " + rTag + "\r\n");
        ps.flush();
        if (rspbody != null) {
            os.write (rspbody, 0, rspbodylen-1);
            os.flush();
        }
        if (rsptrailers != null) {
            rsptrailers.print (ps);
        }
        ps.flush();
    }
}
