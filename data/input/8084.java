public class UserContentHandler implements Runnable {
    ServerSocket ss;
    public void run() {
        try {
            Socket s = ss.accept();
            s.setTcpNoDelay(true);
            PrintStream out = new PrintStream(
                                 new BufferedOutputStream(
                                    s.getOutputStream() ));
            out.print("HTTP/1.1 200 OK\r\n");
            out.print("Content-Length: 11\r\n");
            out.print("Content-Type: text/plain\r\n");
            out.print("\r\n");
            out.print("l;ajfdjafd\n");
            out.flush();
            Thread.sleep(2000);
            s.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    UserContentHandler() throws Exception {
        ss = new ServerSocket(0);
        Thread thr = new Thread(this);
        thr.start();
        try {
            Object o = new COM.foo.content.text.plain();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Properties props = System.getProperties();
        props.put("java.content.handler.pkgs", "COM.foo.content");
        System.setProperties(props);
        URL u = new URL("http:
                        "/anything.txt");
        if (!(u.openConnection().getContent() instanceof String)) {
            throw new RuntimeException("Load user defined content handler failed.");
        } else {
            System.err.println("Load user defined content handler succeed!");
        }
    }
    public static void main(String args[]) throws Exception {
        new UserContentHandler();
    }
}
