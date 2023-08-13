public class DisconnectAfterEOF {
    static class Worker extends Thread {
        Socket s;
        Worker(Socket s) {
            this.s = s;
        }
        public void run() {
            try {
                InputStream in = s.getInputStream();
                PrintStream out = new PrintStream(
                                        new BufferedOutputStream(
                                                s.getOutputStream() ));
                byte b[] = new byte[1024];
                int n = -1;
                int cl = -1;
                int remaining = -1;
                StringBuffer sb = new StringBuffer();
                boolean close = false;
                boolean inBody = false;
                for (;;) {
                    boolean sendResponse = false;
                    try {
                        n = in.read(b);
                    } catch (IOException ioe) {
                        n = -1;
                    }
                    if (n <= 0) {
                        if (inBody) {
                            System.err.println("ERROR: Client closed before before " +
                                "entire request received.");
                        }
                        return;
                    }
                    if (inBody) {
                        if (n > remaining) {
                            System.err.println("Receiving more than expected!!!");
                            return;
                        }
                        remaining -= n;
                        if (remaining == 0) {
                            sendResponse = true;
                            n = 0;
                        } else {
                            continue;
                        }
                    }
                    for (int i=0; i<n; i++) {
                        char c = (char)b[i];
                        if (c != '\n') {
                            sb.append(c);
                            continue;
                        }
                        int len = sb.length();
                        if (len > 0) {
                            if (sb.charAt(len-1) != '\r') {
                                System.err.println("Unexpected CR in header!!");
                                return;
                            }
                        }
                        sb.setLength(len-1);
                        if (sb.length() == 0) {
                            if (cl < 0) {
                                System.err.println("Content-Length not found!!!");
                                return;
                            }
                            int dataRead = n - (i+1);
                            remaining = cl - dataRead;
                            if (remaining > 0) {
                                inBody = true;
                                break;
                            } else {
                                sendResponse = true;
                            }
                        } else {
                            String line = sb.toString().toLowerCase();
                            if (line.startsWith("content-length")) {
                                StringTokenizer st = new StringTokenizer(line, ":");
                                st.nextToken();
                                cl = Integer.parseInt(st.nextToken().trim());
                            }
                            if (line.startsWith("connection")) {
                                StringTokenizer st = new StringTokenizer(line, ":");
                                st.nextToken();
                                if (st.nextToken().trim().equals("close")) {
                                    close =true;
                                }
                            }
                        }
                        sb = new StringBuffer();
                    }
                   if (sendResponse) {
                        int rspLen = 32000;
                        out.print("HTTP/1.1 200 OK\r\n");
                        out.print("Content-Length: " + rspLen + "\r\n");
                        out.print("\r\n");
                        if (rspLen > 0)
                            out.write(new byte[rspLen]);
                        out.flush();
                        if (close)
                            return;
                        sendResponse = false;
                        inBody = false;
                        cl = -1;
                   }
                }
            } catch (IOException ioe) {
            } finally {
                try {
                    s.close();
                } catch (Exception e) { }
                System.out.println("+ Worker thread shutdown.");
            }
        }
    }
    static class Server extends Thread {
        ServerSocket ss;
        Server(ServerSocket ss) {
            this.ss = ss;
        }
        public void run() {
            try {
                for (;;) {
                    Socket s = ss.accept();
                    Worker w = new Worker(s);
                    w.start();
                }
            } catch (IOException ioe) {
            }
            System.out.println("+ Server shutdown.");
        }
        public void shutdown() {
            try {
                ss.close();
            } catch (IOException ioe) { }
        }
    }
    static URLConnection doRequest(String uri) throws IOException {
        URLConnection uc = (new URL(uri)).openConnection();
        uc.setDoOutput(true);
        OutputStream out = uc.getOutputStream();
        out.write(new byte[16000]);
        uc.getInputStream();
        return uc;
    }
    static URLConnection doResponse(URLConnection uc) throws IOException {
        int cl = ((HttpURLConnection)uc).getContentLength();
        byte b[] = new byte[4096];
        int n;
        do {
            n = uc.getInputStream().read(b);
            if (n > 0) cl -= n;
        } while (n > 0);
        if (cl != 0) {
            throw new RuntimeException("ERROR: content-length mismatch");
        }
        return uc;
    }
    public static void main(String args[]) throws Exception {
        ServerSocket ss = new ServerSocket(0);
        Server svr = new Server(ss);
        svr.start();
        String uri = "http:
                     Integer.toString(ss.getLocalPort()) +
                     "/foo.html";
        URLConnection uc1 = doRequest(uri);
        doResponse(uc1);
        Thread.sleep(2000);
        URLConnection uc2 = doRequest(uri);
        ((HttpURLConnection)uc1).disconnect();
        IOException ioe = null;
        try {
            doResponse(uc2);
        } catch (IOException x) {
            ioe = x;
        }
        ((HttpURLConnection)uc2).disconnect();
        svr.shutdown();
        if (ioe != null) {
            throw ioe;
        }
    }
}
