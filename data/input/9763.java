public class Sanity {
    public static void main(String[] args) throws Exception {
        Scanner s = new Scanner(new File(args[0]));
        try {
            while (s.hasNextLine()) {
                String link = s.nextLine();
                NetworkInterface ni = NetworkInterface.getByName(link);
                if (ni != null) {
                    Enumeration<InetAddress> addrs = ni.getInetAddresses();
                    while (addrs.hasMoreElements()) {
                        InetAddress addr = addrs.nextElement();
                        System.out.format("Testing %s: %s\n", link, addr.getHostAddress());
                        test(addr);
                    }
                }
            }
        } finally {
            s.close();
        }
    }
    static void test(InetAddress addr) throws Exception {
        ServerSocketChannel ssc = Sdp.openServerSocketChannel();
        try {
            ssc.socket().bind(new InetSocketAddress(addr, 0));
            int port = ssc.socket().getLocalPort();
            SocketChannel client = Sdp.openSocketChannel();
            try {
                client.connect(new InetSocketAddress(addr, port));
                SocketChannel peer = ssc.accept();
                try {
                    testConnection(Channels.newOutputStream(client),
                                   Channels.newInputStream(peer));
                } finally {
                    peer.close();
                }
            } finally {
                client.close();
            }
            client = Sdp.openSocketChannel();
            try {
                client.socket().bind(new InetSocketAddress(addr, 0));
                client.connect(new InetSocketAddress(addr, port));
                ssc.accept().close();
            } finally {
                client.close();
            }
        } finally {
            ssc.close();
        }
        ServerSocket ss = Sdp.openServerSocket();
        try {
            ss.bind(new InetSocketAddress(addr, 0));
            int port = ss.getLocalPort();
            Socket s = Sdp.openSocket();
            try {
                s.connect(new InetSocketAddress(addr, port));
                Socket peer = ss.accept();
                try {
                    testConnection(s.getOutputStream(), peer.getInputStream());
                } finally {
                    peer.close();
                }
            } finally {
                s.close();
            }
            s = Sdp.openSocket();
            try {
                s.bind(new InetSocketAddress(addr, 0));
                s.connect(new InetSocketAddress(addr, port));
                ss.accept().close();
            } finally {
                s.close();
            }
        } finally {
            ss.close();
        }
    }
    static void testConnection(OutputStream out, InputStream in)
        throws IOException
    {
        byte[] msg = "hello".getBytes();
        out.write(msg);
        byte[] ba = new byte[100];
        int nread = 0;
        while (nread < msg.length) {
            int n = in.read(ba);
            if (n < 0)
                throw new IOException("EOF not expected!");
            nread += n;
        }
    }
}
