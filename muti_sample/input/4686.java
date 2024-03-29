public class MarkResetTest {
    private class FtpServer extends Thread {
        private ServerSocket    server;
        private int port;
        private boolean done = false;
        private boolean pasvEnabled = true;
        private boolean portEnabled = true;
        private boolean extendedEnabled = true;
        private class FtpServerHandler extends Thread {
            BufferedReader in;
            PrintWriter out;
            Socket client;
            private final int ERROR = 0;
            private final int USER = 1;
            private final int PASS = 2;
            private final int CWD =  3;
            private final int TYPE = 4;
            private final int RETR = 5;
            private final int PASV = 6;
            private final int PORT = 7;
            private final int QUIT = 8;
            private final int EPSV = 9;
            String[] cmds = { "USER", "PASS", "CWD",
                                "TYPE", "RETR", "PASV",
                                "PORT", "QUIT", "EPSV"};
            private String arg = null;
            private ServerSocket pasv = null;
            private int data_port = 0;
            private InetAddress data_addr = null;
            private int parseCmd(String cmd) {
                if (cmd == null || cmd.length() < 3)
                    return ERROR;
                int blank = cmd.indexOf(' ');
                if (blank < 0)
                    blank = cmd.length();
                if (blank < 3)
                    return ERROR;
                String s = cmd.substring(0, blank);
                if (cmd.length() > blank+1)
                    arg = cmd.substring(blank+1, cmd.length());
                else
                    arg = null;
                for (int i = 0; i < cmds.length; i++) {
                    if (s.equalsIgnoreCase(cmds[i]))
                        return i+1;
                }
                return ERROR;
            }
            public FtpServerHandler(Socket cl) {
                client = cl;
            }
            protected boolean isPasvSet() {
                if (pasv != null && !pasvEnabled) {
                    try {
                        pasv.close();
                    } catch (IOException ex) {
                    }
                    pasv = null;
                }
                if (pasvEnabled && pasv != null)
                    return true;
                return false;
            }
            protected OutputStream getOutDataStream() {
                try {
                    if (isPasvSet()) {
                        Socket s = pasv.accept();
                        return s.getOutputStream();
                    }
                    if (data_addr != null) {
                        Socket s = new Socket(data_addr, data_port);
                        data_addr = null;
                        data_port = 0;
                        return s.getOutputStream();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
            protected InputStream getInDataStream() {
                try {
                    if (isPasvSet()) {
                        Socket s = pasv.accept();
                        return s.getInputStream();
                    }
                    if (data_addr != null) {
                        Socket s = new Socket(data_addr, data_port);
                        data_addr = null;
                        data_port = 0;
                        return s.getInputStream();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
            public void run() {
                boolean done = false;
                String str;
                int res;
                boolean logged = false;
                boolean waitpass = false;
                try {
                    in = new BufferedReader(new InputStreamReader(
                                                client.getInputStream()));
                    out = new PrintWriter(client.getOutputStream(), true);
                    out.println("220 tatooine FTP server (SunOS 5.8) ready.");
                } catch (Exception ex) {
                    return;
                }
                while (!done) {
                    try {
                        str = in.readLine();
                        res = parseCmd(str);
                        if ((res > PASS && res != QUIT) && !logged) {
                            out.println("530 Not logged in.");
                            continue;
                        }
                        switch (res) {
                        case ERROR:
                            out.println("500 '" + str +
                                        "': command not understood.");
                            break;
                        case USER:
                            if (!logged && !waitpass) {
                                out.println("331 Password required for " + arg);
                                waitpass = true;
                            } else {
                                out.println("503 Bad sequence of commands.");
                            }
                            break;
                        case PASS:
                            if (!logged && waitpass) {
                                out.println("230-Welcome to the FTP server!");
                                out.println("ab");
                                out.println("230 Guest login ok, " +
                                            "access restrictions apply.");
                                logged = true;
                                waitpass = false;
                            } else
                                out.println("503 Bad sequence of commands.");
                            break;
                        case QUIT:
                            out.println("221 Goodbye.");
                            out.flush();
                            out.close();
                            if (pasv != null)
                                pasv.close();
                            done = true;
                            break;
                        case TYPE:
                            out.println("200 Type set to " + arg + ".");
                            break;
                        case CWD:
                            out.println("250 CWD command successful.");
                            break;
                        case EPSV:
                            if (!extendedEnabled || !pasvEnabled) {
                                out.println("500 EPSV is disabled, " +
                                                "use PORT instead.");
                                continue;
                            }
                            if ("all".equalsIgnoreCase(arg)) {
                                out.println("200 EPSV ALL command successful.");
                                continue;
                            }
                            try {
                                if (pasv == null)
                                    pasv = new ServerSocket(0);
                                int port = pasv.getLocalPort();
                                out.println("229 Entering Extended" +
                                        " Passive Mode (|||" + port + "|)");
                            } catch (IOException ssex) {
                                out.println("425 Can't build data connection:" +
                                                " Connection refused.");
                            }
                            break;
                        case PASV:
                            if (!pasvEnabled) {
                                out.println("500 PASV is disabled, " +
                                                "use PORT instead.");
                                continue;
                            }
                            try {
                                if (pasv == null)
                                    pasv = new ServerSocket(0);
                                int port = pasv.getLocalPort();
                                out.println("227 Entering Passive Mode" +
                                                " 127,0,0,1," +
                                            (port >> 8) + "," + (port & 0xff));
                            } catch (IOException ssex) {
                                out.println("425 Can't build data connection:" +
                                                 "Connection refused.");
                            }
                            break;
                        case PORT:
                            if (!portEnabled) {
                                out.println("500 PORT is disabled, " +
                                                "use PASV instead");
                                continue;
                            }
                            StringBuffer host;
                            int i = 0, j = 4;
                            while (j > 0) {
                                i = arg.indexOf(',', i + 1);
                                if (i < 0)
                                    break;
                                j--;
                            }
                            if (j != 0) {
                                out.println("500 '" + arg + "':" +
                                            " command not understood.");
                                continue;
                            }
                            try {
                                host = new StringBuffer(arg.substring(0, i));
                                for (j = 0; j < host.length(); j++)
                                    if (host.charAt(j) == ',')
                                        host.setCharAt(j, '.');
                                String ports = arg.substring(i+1);
                                i = ports.indexOf(',');
                                data_port = Integer.parseInt(
                                                ports.substring(0, i)) << 8;
                                data_port += (Integer.parseInt(
                                                ports.substring(i+1)));
                                data_addr = InetAddress.getByName(
                                                        host.toString());
                                out.println("200 Command okay.");
                            } catch (Exception ex3) {
                                data_port = 0;
                                data_addr = null;
                                out.println("500 '" + arg + "':" +
                                             " command not understood.");
                            }
                            break;
                        case RETR:
                            {
                                File file = new File(arg);
                                if (!file.exists()) {
                                   System.out.println("File not found");
                                   out.println("200 Command okay.");
                                   out.println("550 '" + arg +
                                            "' No such file or directory.");
                                   break;
                                }
                                FileInputStream fin = new FileInputStream(file);
                                OutputStream dout = getOutDataStream();
                                if (dout != null) {
                                   out.println("150 Binary data connection" +
                                                " for " + arg +
                                                " (" + client.getInetAddress().
                                                getHostAddress() + ") (" +
                                                file.length() + " bytes).");
                                    int c;
                                    int len = 0;
                                    while ((c = fin.read()) != -1) {
                                        dout.write(c);
                                        len++;
                                    }
                                    dout.flush();
                                    dout.close();
                                    fin.close();
                                   out.println("226 Binary Transfer complete.");
                                } else {
                                    out.println("425 Can't build data" +
                                        " connection: Connection refused.");
                                }
                            }
                            break;
                        }
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                        try {
                            out.close();
                        } catch (Exception ex2) {
                        }
                        done = true;
                    }
                }
            }
        }
        public FtpServer(int port) {
            this.port = port;
        }
        public FtpServer() {
            this(21);
        }
        public int getPort() {
            if (server != null)
                return server.getLocalPort();
            return 0;
        }
        synchronized public void terminate() {
            done = true;
        }
        public void run() {
            try {
                server = new ServerSocket(port);
                Socket client;
                client = server.accept();
                (new FtpServerHandler(client)).start();
                server.close();
            } catch (Exception e) {
            }
        }
    }
    public static void main(String[] args) throws Exception {
        MarkResetTest test = new MarkResetTest();
    }
    public MarkResetTest() {
        FtpServer server = null;
        try {
            server = new FtpServer(0);
            server.start();
            int port = 0;
            while (port == 0) {
                Thread.sleep(500);
                port = server.getPort();
            }
            String filename = "EncDec.doc";
            URL url = new URL("ftp:
                                filename);
            URLConnection con = url.openConnection();
            System.out.println("getContent: " + con.getContent());
            System.out.println("getContent-length: " + con.getContentLength());
            InputStream is = con.getInputStream();
            System.out.println("Call GuessContentTypeFromStream()" +
                                " several times..");
            for (int i = 0; i < 5; i++) {
                System.out.println((i + 1) + " mime-type: " +
                        con.guessContentTypeFromStream(is));
            }
            int len = 0;
            int c;
            while ((c = is.read()) != -1) {
                len++;
            }
            is.close();
            System.out.println("read: " + len + " bytes of the file");
            server.terminate();
            server.interrupt();
            if (len != (new File(filename)).length()) {
                throw new Exception("Failed to read the file correctly");
            }
            System.out.println("PASSED: File read correctly");
        } catch (Exception e) {
            e.printStackTrace();
            try {
                server.terminate();
                server.interrupt();
            } catch (Exception ex) {
            }
            throw new RuntimeException("FTP support error: " + e.getMessage());
        }
    }
}
