public class ChunkedEncoding implements Runnable {
    ServerSocket ss;
    public void run() {
        try {
            Socket s = ss.accept();
            PrintStream out = new PrintStream(
                                 new BufferedOutputStream(
                                    s.getOutputStream() ));
            out.print("HTTP/1.1 200\r\n");
            out.print("Transfer-Encoding: chunked\r\n");
            out.print("Content-Type: text/html\r\n");
            out.print("\r\n");
            out.flush();
            Thread.sleep(5000);
            Random rand = new Random();
            int len;
            do {
                len = rand.nextInt(128*1024);
            } while (len < 32*1024);
            int chunkSize;
            do {
                chunkSize = rand.nextInt(len / 3);
            } while (chunkSize < 2*1024);
            byte buf[] = new byte[len];
            int cs = 0;
            for (int i=0; i<len; i++) {
                buf[i] = (byte)('a' + rand.nextInt(26));
                cs = (cs + buf[i]) % 65536;
            }
            int remaining = len;
            int pos = 0;
            while (remaining > 0) {
                int size = Math.min(remaining, chunkSize);
                out.print( Integer.toHexString(size) );
                out.print("\r\n");
                out.write( buf, pos, size );
                pos += size;
                remaining -= size;
                out.print("\r\n");
                out.flush();
            }
            out.print("0\r\n");
            out.flush();
            String trailer = "Checksum:" + cs + "\r\n";
            out.print(trailer);
            out.print("\r\n");
            out.flush();
            s.close();
            ss.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    ChunkedEncoding() throws Exception {
        ss = new ServerSocket(0);
        (new Thread(this)).start();
        String uri = "http:
                     Integer.toString(ss.getLocalPort()) +
                     "/foo";
        URL url = new URL(uri);
        HttpURLConnection http = (HttpURLConnection)url.openConnection();
        http.setRequestProperty("TE", "trailers");
        long ts = System.currentTimeMillis();
        InputStream in = http.getInputStream();
        long te = System.currentTimeMillis();
        if ( (te-ts) > 2000) {
            throw new Exception("getInputStream didn't return immediately");
        }
        int nread;
        int cs = 0;
        byte b[] = new byte[1024];
        do {
            nread = in.read(b);
            if (nread > 0) {
                for (int i=0; i<nread; i++) {
                    cs = (cs + b[i]) % 65536;
                }
            }
        } while (nread > 0);
        String trailer = http.getHeaderField("Checksum");
        if (trailer == null) {
            throw new Exception("Checksum trailer missing from response");
        }
        int rcvd_cs = Integer.parseInt(trailer);
        if (rcvd_cs != cs) {
            throw new Exception("Trailer checksum doesn't equal calculated checksum");
        }
        http.disconnect();
    }
    public static void main(String args[]) throws Exception {
        new ChunkedEncoding();
    }
}
