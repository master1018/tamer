public class B6427768 {
    static class MyAuthHandler implements FtpAuthHandler {
        public int authType() {
                return 2;
        }
        public boolean authenticate(String user, String password) {
                return false;
        }
        public boolean authenticate(String user, String password, String account) {
                return false;
        }
    }
    static class MyFileSystemHandler implements FtpFileSystemHandler {
        private String currentDir = "/";
        public MyFileSystemHandler(String path) {
                currentDir = path;
        }
        public boolean cd(String path) {
            currentDir = path;
            return true;
        }
        public boolean cdUp() {
            return true;
        }
        public String pwd() {
            return currentDir;
        }
        public InputStream getFile(String name) {
            return null;
        }
        public long getFileSize(String name) {
            return -1;
        }
        public boolean fileExists(String name) {
            return false;
        }
        public InputStream listCurrentDir() {
            return null;
        }
        public OutputStream putFile(String name) {
            return null;
        }
        public boolean removeFile(String name) {
            return false;
        }
        public boolean mkdir(String name) {
            return false;
        }
        public boolean rename(String from, String to) {
            return false;
        }
    }
    public static void main(String[] args) throws IOException {
        FtpServer server = new FtpServer(0);
        int port = server.getLocalPort();
        server.setFileSystemHandler(new MyFileSystemHandler("/"));
        server.setAuthHandler(new MyAuthHandler());
        server.start();
        URL url = new URL("ftp:
        URLConnection con = url.openConnection();
        try {
            con.getInputStream();
        } catch(sun.net.ftp.FtpLoginException e) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ie) {
            }
            if (server.activeClientsCount() > 0) {
                server.killClients();
                throw new RuntimeException("URLConnection didn't close the ftp connection on failure to login");
            }
        } finally {
            server.terminate();
        }
    }
}
