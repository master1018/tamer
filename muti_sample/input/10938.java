public class DnsClient {
    private static final int IDENT_OFFSET = 0;
    private static final int FLAGS_OFFSET = 2;
    private static final int NUMQ_OFFSET  = 4;
    private static final int NUMANS_OFFSET = 6;
    private static final int NUMAUTH_OFFSET = 8;
    private static final int NUMADD_OFFSET = 10;
    private static final int DNS_HDR_SIZE = 12;
    private static final int NO_ERROR       = 0;
    private static final int FORMAT_ERROR   = 1;
    private static final int SERVER_FAILURE = 2;
    private static final int NAME_ERROR     = 3;
    private static final int NOT_IMPL       = 4;
    private static final int REFUSED        = 5;
    private static final String[] rcodeDescription = {
        "No error",
        "DNS format error",
        "DNS server failure",
        "DNS name not found",
        "DNS operation not supported",
        "DNS service refused"
    };
    private static final int DEFAULT_PORT = 53;
    private InetAddress[] servers;
    private int[] serverPorts;
    private int timeout;                
    private int retries;                
    private DatagramSocket udpSocket;
    private Set<Integer> reqs;
    private Map<Integer, byte[]> resps;
    public DnsClient(String[] servers, int timeout, int retries)
            throws NamingException {
        this.timeout = timeout;
        this.retries = retries;
        try {
            udpSocket = new DatagramSocket();
        } catch (java.net.SocketException e) {
            NamingException ne = new ConfigurationException();
            ne.setRootCause(e);
            throw ne;
        }
        this.servers = new InetAddress[servers.length];
        serverPorts = new int[servers.length];
        for (int i = 0; i < servers.length; i++) {
            int colon = servers[i].indexOf(':',
                                           servers[i].indexOf(']') + 1);
            serverPorts[i] = (colon < 0)
                ? DEFAULT_PORT
                : Integer.parseInt(servers[i].substring(colon + 1));
            String server = (colon < 0)
                ? servers[i]
                : servers[i].substring(0, colon);
            try {
                this.servers[i] = InetAddress.getByName(server);
            } catch (java.net.UnknownHostException e) {
                NamingException ne = new ConfigurationException(
                        "Unknown DNS server: " + server);
                ne.setRootCause(e);
                throw ne;
            }
        }
        reqs = Collections.synchronizedSet(new HashSet<Integer>());
        resps = Collections.synchronizedMap(new HashMap<Integer, byte[]>());
    }
    protected void finalize() {
        close();
    }
    private Object queuesLock = new Object();
    public void close() {
        udpSocket.close();
        synchronized (queuesLock) {
            reqs.clear();
            resps.clear();
        }
    }
    private int ident = 0;              
    private Object identLock = new Object();
    ResourceRecords query(DnsName fqdn, int qclass, int qtype,
                          boolean recursion, boolean auth)
            throws NamingException {
        int xid;
        synchronized (identLock) {
            ident = 0xFFFF & (ident + 1);
            xid = ident;
        }
        reqs.add(xid);
        Packet pkt = makeQueryPacket(fqdn, xid, qclass, qtype, recursion);
        Exception caughtException = null;
        boolean[] doNotRetry = new boolean[servers.length];
        for (int retry = 0; retry < retries; retry++) {
            for (int i = 0; i < servers.length; i++) {
                if (doNotRetry[i]) {
                    continue;
                }
                try {
                    if (debug) {
                        dprint("SEND ID (" + (retry + 1) + "): " + xid);
                    }
                    byte[] msg = null;
                    msg = doUdpQuery(pkt, servers[i], serverPorts[i],
                                        retry, xid);
                    if (msg == null) {
                        if (resps.size() > 0) {
                            msg = lookupResponse(xid);
                        }
                        if (msg == null) { 
                            continue;
                        }
                    }
                    Header hdr = new Header(msg, msg.length);
                    if (auth && !hdr.authoritative) {
                        caughtException = new NameNotFoundException(
                                "DNS response not authoritative");
                        doNotRetry[i] = true;
                        continue;
                    }
                    if (hdr.truncated) {    
                        for (int j = 0; j < servers.length; j++) {
                            int ij = (i + j) % servers.length;
                            if (doNotRetry[ij]) {
                                continue;
                            }
                            try {
                                Tcp tcp =
                                    new Tcp(servers[ij], serverPorts[ij]);
                                byte[] msg2;
                                try {
                                    msg2 = doTcpQuery(tcp, pkt);
                                } finally {
                                    tcp.close();
                                }
                                Header hdr2 = new Header(msg2, msg2.length);
                                if (hdr2.query) {
                                    throw new CommunicationException(
                                        "DNS error: expecting response");
                                }
                                checkResponseCode(hdr2);
                                if (!auth || hdr2.authoritative) {
                                    hdr = hdr2;
                                    msg = msg2;
                                    break;
                                } else {
                                    doNotRetry[ij] = true;
                                }
                            } catch (Exception e) {
                            }
                        } 
                    }
                    return new ResourceRecords(msg, msg.length, hdr, false);
                } catch (IOException e) {
                    if (debug) {
                        dprint("Caught IOException:" + e);
                    }
                    if (caughtException == null) {
                        caughtException = e;
                    }
                    if (e.getClass().getName().equals(
                            "java.net.PortUnreachableException")) {
                        doNotRetry[i] = true;
                    }
                } catch (NameNotFoundException e) {
                    throw e;
                } catch (CommunicationException e) {
                    if (caughtException == null) {
                        caughtException = e;
                    }
                } catch (NamingException e) {
                    if (caughtException == null) {
                        caughtException = e;
                    }
                    doNotRetry[i] = true;
                }
            } 
        } 
        reqs.remove(xid);
        if (caughtException instanceof NamingException) {
            throw (NamingException) caughtException;
        }
        NamingException ne = new CommunicationException("DNS error");
        ne.setRootCause(caughtException);
        throw ne;
    }
    ResourceRecords queryZone(DnsName zone, int qclass, boolean recursion)
            throws NamingException {
        int xid;
        synchronized (identLock) {
            ident = 0xFFFF & (ident + 1);
            xid = ident;
        }
        Packet pkt = makeQueryPacket(zone, xid, qclass,
                                     ResourceRecord.QTYPE_AXFR, recursion);
        Exception caughtException = null;
        for (int i = 0; i < servers.length; i++) {
            try {
                Tcp tcp = new Tcp(servers[i], serverPorts[i]);
                byte[] msg;
                try {
                    msg = doTcpQuery(tcp, pkt);
                    Header hdr = new Header(msg, msg.length);
                    checkResponseCode(hdr);
                    ResourceRecords rrs =
                        new ResourceRecords(msg, msg.length, hdr, true);
                    if (rrs.getFirstAnsType() != ResourceRecord.TYPE_SOA) {
                        throw new CommunicationException(
                                "DNS error: zone xfer doesn't begin with SOA");
                    }
                    if (rrs.answer.size() == 1 ||
                            rrs.getLastAnsType() != ResourceRecord.TYPE_SOA) {
                        do {
                            msg = continueTcpQuery(tcp);
                            if (msg == null) {
                                throw new CommunicationException(
                                        "DNS error: incomplete zone transfer");
                            }
                            hdr = new Header(msg, msg.length);
                            checkResponseCode(hdr);
                            rrs.add(msg, msg.length, hdr);
                        } while (rrs.getLastAnsType() !=
                                 ResourceRecord.TYPE_SOA);
                    }
                    rrs.answer.removeElementAt(rrs.answer.size() - 1);
                    return rrs;
                } finally {
                    tcp.close();
                }
            } catch (IOException e) {
                caughtException = e;
            } catch (NameNotFoundException e) {
                throw e;
            } catch (NamingException e) {
                caughtException = e;
            }
        }
        if (caughtException instanceof NamingException) {
            throw (NamingException) caughtException;
        }
        NamingException ne = new CommunicationException(
                "DNS error during zone transfer");
        ne.setRootCause(caughtException);
        throw ne;
    }
    private byte[] doUdpQuery(Packet pkt, InetAddress server,
                                     int port, int retry, int xid)
            throws IOException, NamingException {
        int minTimeout = 50; 
        synchronized (udpSocket) {
            DatagramPacket opkt = new DatagramPacket(
                    pkt.getData(), pkt.length(), server, port);
            DatagramPacket ipkt = new DatagramPacket(new byte[8000], 8000);
            udpSocket.connect(server, port);
            int pktTimeout = (timeout * (1 << retry));
            try {
                udpSocket.send(opkt);
                int timeoutLeft = pktTimeout;
                int cnt = 0;
                do {
                    if (debug) {
                       cnt++;
                        dprint("Trying RECEIVE(" +
                                cnt + ") retry(" + (retry + 1) +
                                ") for:" + xid  + "    sock-timeout:" +
                                timeoutLeft + " ms.");
                    }
                    udpSocket.setSoTimeout(timeoutLeft);
                    long start = System.currentTimeMillis();
                    udpSocket.receive(ipkt);
                    long end = System.currentTimeMillis();
                    byte[] data = new byte[ipkt.getLength()];
                    data = ipkt.getData();
                    if (isMatchResponse(data, xid)) {
                        return data;
                    }
                    timeoutLeft = pktTimeout - ((int) (end - start));
                } while (timeoutLeft > minTimeout);
            } finally {
                udpSocket.disconnect();
            }
            return null; 
        }
    }
    private byte[] doTcpQuery(Tcp tcp, Packet pkt) throws IOException {
        int len = pkt.length();
        tcp.out.write(len >> 8);
        tcp.out.write(len);
        tcp.out.write(pkt.getData(), 0, len);
        tcp.out.flush();
        byte[] msg = continueTcpQuery(tcp);
        if (msg == null) {
            throw new IOException("DNS error: no response");
        }
        return msg;
    }
    private byte[] continueTcpQuery(Tcp tcp) throws IOException {
        int lenHi = tcp.in.read();      
        if (lenHi == -1) {
            return null;        
        }
        int lenLo = tcp.in.read();      
        if (lenLo == -1) {
            throw new IOException("Corrupted DNS response: bad length");
        }
        int len = (lenHi << 8) | lenLo;
        byte[] msg = new byte[len];
        int pos = 0;                    
        while (len > 0) {
            int n = tcp.in.read(msg, pos, len);
            if (n == -1) {
                throw new IOException(
                        "Corrupted DNS response: too little data");
            }
            len -= n;
            pos += n;
        }
        return msg;
    }
    private Packet makeQueryPacket(DnsName fqdn, int xid,
                                   int qclass, int qtype, boolean recursion) {
        int qnameLen = fqdn.getOctets();
        int pktLen = DNS_HDR_SIZE + qnameLen + 4;
        Packet pkt = new Packet(pktLen);
        short flags = recursion ? Header.RD_BIT : 0;
        pkt.putShort(xid, IDENT_OFFSET);
        pkt.putShort(flags, FLAGS_OFFSET);
        pkt.putShort(1, NUMQ_OFFSET);
        pkt.putShort(0, NUMANS_OFFSET);
        pkt.putInt(0, NUMAUTH_OFFSET);
        makeQueryName(fqdn, pkt, DNS_HDR_SIZE);
        pkt.putShort(qtype, DNS_HDR_SIZE + qnameLen);
        pkt.putShort(qclass, DNS_HDR_SIZE + qnameLen + 2);
        return pkt;
    }
    private void makeQueryName(DnsName fqdn, Packet pkt, int off) {
        for (int i = fqdn.size() - 1; i >= 0; i--) {
            String label = fqdn.get(i);
            int len = label.length();
            pkt.putByte(len, off++);
            for (int j = 0; j < len; j++) {
                pkt.putByte(label.charAt(j), off++);
            }
        }
        if (!fqdn.hasRootLabel()) {
            pkt.putByte(0, off);
        }
    }
    private byte[] lookupResponse(Integer xid) throws NamingException {
        if (debug) {
            dprint("LOOKUP for: " + xid +
                "\tResponse Q:" + resps);
        }
        byte[] pkt;
        if ((pkt = (byte[]) resps.get(xid)) != null) {
            checkResponseCode(new Header(pkt, pkt.length));
            synchronized (queuesLock) {
                resps.remove(xid);
                reqs.remove(xid);
            }
            if (debug) {
                dprint("FOUND (" + Thread.currentThread() +
                    ") for:" + xid);
            }
        }
        return pkt;
    }
    private boolean isMatchResponse(byte[] pkt, int xid)
                throws NamingException {
        Header hdr = new Header(pkt, pkt.length);
        if (hdr.query) {
            throw new CommunicationException("DNS error: expecting response");
        }
        if (!reqs.contains(xid)) { 
            return false;
        }
        if (hdr.xid == xid) {
            if (debug) {
                dprint("XID MATCH:" + xid);
            }
            checkResponseCode(hdr);
            synchronized (queuesLock) {
                resps.remove(xid);
                reqs.remove(xid);
            }
            return true;
        }
        synchronized (queuesLock) {
            if (reqs.contains(xid)) { 
                resps.put(xid, pkt);
            }
        }
        if (debug) {
            dprint("NO-MATCH SEND ID:" +
                                xid + " RECVD ID:" + hdr.xid +
                                "    Response Q:" + resps +
                                "    Reqs size:" + reqs.size());
        }
        return false;
    }
    private void checkResponseCode(Header hdr) throws NamingException {
        int rcode = hdr.rcode;
        if (rcode == NO_ERROR) {
            return;
        }
        String msg = (rcode < rcodeDescription.length)
            ? rcodeDescription[rcode]
            : "DNS error";
        msg += " [response code " + rcode + "]";
        switch (rcode) {
        case SERVER_FAILURE:
            throw new ServiceUnavailableException(msg);
        case NAME_ERROR:
            throw new NameNotFoundException(msg);
        case NOT_IMPL:
        case REFUSED:
            throw new OperationNotSupportedException(msg);
        case FORMAT_ERROR:
        default:
            throw new NamingException(msg);
        }
    }
    private static boolean debug = false;
    public static void setDebug(boolean flag) {
        debug = flag;
    }
    private static void dprint(String mess) {
        if (debug) {
            System.err.println("DNS: " + mess);
        }
    }
}
class Tcp {
    private Socket sock;
    java.io.InputStream in;
    java.io.OutputStream out;
    Tcp(InetAddress server, int port) throws IOException {
        sock = new Socket(server, port);
        sock.setTcpNoDelay(true);
        out = new java.io.BufferedOutputStream(sock.getOutputStream());
        in = new java.io.BufferedInputStream(sock.getInputStream());
    }
    void close() throws IOException {
        sock.close();
    }
}
class Packet {
        byte buf[];
        Packet(int len) {
                buf = new byte[len];
        }
        Packet(byte data[], int len) {
                buf = new byte[len];
                System.arraycopy(data, 0, buf, 0, len);
        }
        void putInt(int x, int off) {
                buf[off + 0] = (byte)(x >> 24);
                buf[off + 1] = (byte)(x >> 16);
                buf[off + 2] = (byte)(x >> 8);
                buf[off + 3] = (byte)x;
        }
        void putShort(int x, int off) {
                buf[off + 0] = (byte)(x >> 8);
                buf[off + 1] = (byte)x;
        }
        void putByte(int x, int off) {
                buf[off] = (byte)x;
        }
        void putBytes(byte src[], int src_offset, int dst_offset, int len) {
                System.arraycopy(src, src_offset, buf, dst_offset, len);
        }
        int length() {
                return buf.length;
        }
        byte[] getData() {
                return buf;
        }
}
