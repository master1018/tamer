final class AdbHelper {
    static final int WAIT_TIME = 5; 
    static final String DEFAULT_ENCODING = "ISO-8859-1"; 
    private AdbHelper() {
    }
    static class AdbResponse {
        public AdbResponse() {
            message = "";
        }
        public boolean ioSuccess; 
        public boolean okay; 
        public boolean timeout; 
        public String message; 
    }
    public static SocketChannel open(InetSocketAddress adbSockAddr,
            Device device, int devicePort) throws IOException {
        SocketChannel adbChan = SocketChannel.open(adbSockAddr);
        try {
            adbChan.socket().setTcpNoDelay(true);
            adbChan.configureBlocking(false);
            setDevice(adbChan, device);
            byte[] req = createAdbForwardRequest(null, devicePort);
            if (write(adbChan, req) == false)
                throw new IOException("failed submitting request to ADB"); 
            AdbResponse resp = readAdbResponse(adbChan, false);
            if (!resp.okay)
                throw new IOException("connection request rejected"); 
            adbChan.configureBlocking(true);
        } catch (IOException ioe) {
            adbChan.close();
            throw ioe;
        }
        return adbChan;
    }
    public static SocketChannel createPassThroughConnection(InetSocketAddress adbSockAddr,
            Device device, int pid) throws IOException {
        SocketChannel adbChan = SocketChannel.open(adbSockAddr);
        try {
            adbChan.socket().setTcpNoDelay(true);
            adbChan.configureBlocking(false);
            setDevice(adbChan, device);
            byte[] req = createJdwpForwardRequest(pid);
            if (write(adbChan, req) == false)
                throw new IOException("failed submitting request to ADB"); 
            AdbResponse resp = readAdbResponse(adbChan, false );
            if (!resp.okay)
                throw new IOException("connection request rejected: " + resp.message); 
            adbChan.configureBlocking(true);
        } catch (IOException ioe) {
            adbChan.close();
            throw ioe;
        }
        return adbChan;
    }
    private static byte[] createAdbForwardRequest(String addrStr, int port) {
        String reqStr;
        if (addrStr == null)
            reqStr = "tcp:" + port;
        else
            reqStr = "tcp:" + port + ":" + addrStr;
        return formAdbRequest(reqStr);
    }
    private static byte[] createJdwpForwardRequest(int pid) {
        String reqStr = String.format("jdwp:%1$d", pid); 
        return formAdbRequest(reqStr);
    }
    static byte[] formAdbRequest(String req) {
        String resultStr = String.format("%04X%s", req.length(), req); 
        byte[] result;
        try {
            result = resultStr.getBytes(DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace(); 
            return null;
        }
        assert result.length == req.length() + 4;
        return result;
    }
    static AdbResponse readAdbResponse(SocketChannel chan, boolean readDiagString)
            throws IOException {
        AdbResponse resp = new AdbResponse();
        byte[] reply = new byte[4];
        if (read(chan, reply) == false) {
            return resp;
        }
        resp.ioSuccess = true;
        if (isOkay(reply)) {
            resp.okay = true;
        } else {
            readDiagString = true; 
            resp.okay = false;
        }
        while (readDiagString) {
            byte[] lenBuf = new byte[4];
            if (read(chan, lenBuf) == false) {
                Log.w("ddms", "Expected diagnostic string not found");
                break;
            }
            String lenStr = replyToString(lenBuf);
            int len;
            try {
                len = Integer.parseInt(lenStr, 16);
            } catch (NumberFormatException nfe) {
                Log.w("ddms", "Expected digits, got '" + lenStr + "': "
                        + lenBuf[0] + " " + lenBuf[1] + " " + lenBuf[2] + " "
                        + lenBuf[3]);
                Log.w("ddms", "reply was " + replyToString(reply));
                break;
            }
            byte[] msg = new byte[len];
            if (read(chan, msg) == false) {
                Log.w("ddms", "Failed reading diagnostic string, len=" + len);
                break;
            }
            resp.message = replyToString(msg);
            Log.v("ddms", "Got reply '" + replyToString(reply) + "', diag='"
                    + resp.message + "'");
            break;
        }
        return resp;
    }
    public static RawImage getFrameBuffer(InetSocketAddress adbSockAddr, Device device)
            throws IOException {
        RawImage imageParams = new RawImage();
        byte[] request = formAdbRequest("framebuffer:"); 
        byte[] nudge = {
            0
        };
        byte[] reply;
        SocketChannel adbChan = null;
        try {
            adbChan = SocketChannel.open(adbSockAddr);
            adbChan.configureBlocking(false);
            setDevice(adbChan, device);
            if (write(adbChan, request) == false)
                throw new IOException("failed asking for frame buffer");
            AdbResponse resp = readAdbResponse(adbChan, false );
            if (!resp.ioSuccess || !resp.okay) {
                Log.w("ddms", "Got timeout or unhappy response from ADB fb req: "
                        + resp.message);
                adbChan.close();
                return null;
            }
            reply = new byte[4];
            if (read(adbChan, reply) == false) {
                Log.w("ddms", "got partial reply from ADB fb:");
                Log.hexDump("ddms", LogLevel.WARN, reply, 0, reply.length);
                adbChan.close();
                return null;
            }
            ByteBuffer buf = ByteBuffer.wrap(reply);
            buf.order(ByteOrder.LITTLE_ENDIAN);
            int version = buf.getInt();
            int headerSize = RawImage.getHeaderSize(version);
            reply = new byte[headerSize * 4];
            if (read(adbChan, reply) == false) {
                Log.w("ddms", "got partial reply from ADB fb:");
                Log.hexDump("ddms", LogLevel.WARN, reply, 0, reply.length);
                adbChan.close();
                return null;
            }
            buf = ByteBuffer.wrap(reply);
            buf.order(ByteOrder.LITTLE_ENDIAN);
            if (imageParams.readHeader(version, buf) == false) {
                Log.e("Screenshot", "Unsupported protocol: " + version);
                return null;
            }
            Log.d("ddms", "image params: bpp=" + imageParams.bpp + ", size="
                    + imageParams.size + ", width=" + imageParams.width
                    + ", height=" + imageParams.height);
            if (write(adbChan, nudge) == false)
                throw new IOException("failed nudging");
            reply = new byte[imageParams.size];
            if (read(adbChan, reply) == false) {
                Log.w("ddms", "got truncated reply from ADB fb data");
                adbChan.close();
                return null;
            }
            imageParams.data = reply;
        } finally {
            if (adbChan != null) {
                adbChan.close();
            }
        }
        return imageParams;
    }
    public static void executeRemoteCommand(InetSocketAddress adbSockAddr,
            String command, Device device, IShellOutputReceiver rcvr)
            throws IOException {
        Log.v("ddms", "execute: running " + command);
        SocketChannel adbChan = null;
        try {
            adbChan = SocketChannel.open(adbSockAddr);
            adbChan.configureBlocking(false);
            setDevice(adbChan, device);
            byte[] request = formAdbRequest("shell:" + command); 
            if (write(adbChan, request) == false)
                throw new IOException("failed submitting shell command");
            AdbResponse resp = readAdbResponse(adbChan, false );
            if (!resp.ioSuccess || !resp.okay) {
                Log.e("ddms", "ADB rejected shell command (" + command + "): " + resp.message);
                throw new IOException("sad result from adb: " + resp.message);
            }
            byte[] data = new byte[16384];
            ByteBuffer buf = ByteBuffer.wrap(data);
            while (true) {
                int count;
                if (rcvr != null && rcvr.isCancelled()) {
                    Log.v("ddms", "execute: cancelled");
                    break;
                }
                count = adbChan.read(buf);
                if (count < 0) {
                    rcvr.flush();
                    Log.v("ddms", "execute '" + command + "' on '" + device + "' : EOF hit. Read: "
                            + count);
                    break;
                } else if (count == 0) {
                    try {
                        Thread.sleep(WAIT_TIME * 5);
                    } catch (InterruptedException ie) {
                    }
                } else {
                    if (rcvr != null) {
                        rcvr.addOutput(buf.array(), buf.arrayOffset(), buf.position());
                    }
                    buf.rewind();
                }
            }
        } finally {
            if (adbChan != null) {
                adbChan.close();
            }
            Log.v("ddms", "execute: returning");
        }
    }
    public static void runEventLogService(InetSocketAddress adbSockAddr, Device device,
            LogReceiver rcvr) throws IOException {
        runLogService(adbSockAddr, device, "events", rcvr); 
    }
    public static void runLogService(InetSocketAddress adbSockAddr, Device device, String logName,
            LogReceiver rcvr) throws IOException {
        SocketChannel adbChan = null;
        try {
            adbChan = SocketChannel.open(adbSockAddr);
            adbChan.configureBlocking(false);
            setDevice(adbChan, device);
            byte[] request = formAdbRequest("log:" + logName);
            if (write(adbChan, request) == false) {
                throw new IOException("failed to submit the log command");
            }
            AdbResponse resp = readAdbResponse(adbChan, false );
            if (!resp.ioSuccess || !resp.okay) {
                throw new IOException("Device rejected log command: " + resp.message);
            }
            byte[] data = new byte[16384];
            ByteBuffer buf = ByteBuffer.wrap(data);
            while (true) {
                int count;
                if (rcvr != null && rcvr.isCancelled()) {
                    break;
                }
                count = adbChan.read(buf);
                if (count < 0) {
                    break;
                } else if (count == 0) {
                    try {
                        Thread.sleep(WAIT_TIME * 5);
                    } catch (InterruptedException ie) {
                    }
                } else {
                    if (rcvr != null) {
                        rcvr.parseNewData(buf.array(), buf.arrayOffset(), buf.position());
                    }
                    buf.rewind();
                }
            }
        } finally {
            if (adbChan != null) {
                adbChan.close();
            }
        }
    }
    public static boolean createForward(InetSocketAddress adbSockAddr, Device device, int localPort,
            int remotePort) throws IOException {
        SocketChannel adbChan = null;
        try {
            adbChan = SocketChannel.open(adbSockAddr);
            adbChan.configureBlocking(false);
            byte[] request = formAdbRequest(String.format(
                    "host-serial:%1$s:forward:tcp:%2$d;tcp:%3$d", 
                    device.getSerialNumber(), localPort, remotePort));
            if (write(adbChan, request) == false) {
                throw new IOException("failed to submit the forward command.");
            }
            AdbResponse resp = readAdbResponse(adbChan, false );
            if (!resp.ioSuccess || !resp.okay) {
                throw new IOException("Device rejected command: " + resp.message);
            }
        } finally {
            if (adbChan != null) {
                adbChan.close();
            }
        }
        return true;
    }
    public static boolean removeForward(InetSocketAddress adbSockAddr, Device device, int localPort,
            int remotePort) throws IOException {
        SocketChannel adbChan = null;
        try {
            adbChan = SocketChannel.open(adbSockAddr);
            adbChan.configureBlocking(false);
            byte[] request = formAdbRequest(String.format(
                    "host-serial:%1$s:killforward:tcp:%2$d;tcp:%3$d", 
                    device.getSerialNumber(), localPort, remotePort));
            if (!write(adbChan, request)) {
                throw new IOException("failed to submit the remove forward command.");
            }
            AdbResponse resp = readAdbResponse(adbChan, false );
            if (!resp.ioSuccess || !resp.okay) {
                throw new IOException("Device rejected command: " + resp.message);
            }
        } finally {
            if (adbChan != null) {
                adbChan.close();
            }
        }
        return true;
    }
    static boolean isOkay(byte[] reply) {
        return reply[0] == (byte)'O' && reply[1] == (byte)'K'
                && reply[2] == (byte)'A' && reply[3] == (byte)'Y';
    }
    static String replyToString(byte[] reply) {
        String result;
        try {
            result = new String(reply, DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace(); 
            result = "";
        }
        return result;
    }
    static boolean read(SocketChannel chan, byte[] data) {
       try {
           read(chan, data, -1, DdmPreferences.getTimeOut());
       } catch (IOException e) {
           Log.d("ddms", "readAll: IOException: " + e.getMessage());
           return false;
       }
       return true;
    }
    static void read(SocketChannel chan, byte[] data, int length, int timeout) throws IOException {
        ByteBuffer buf = ByteBuffer.wrap(data, 0, length != -1 ? length : data.length);
        int numWaits = 0;
        while (buf.position() != buf.limit()) {
            int count;
            count = chan.read(buf);
            if (count < 0) {
                Log.d("ddms", "read: channel EOF");
                throw new IOException("EOF");
            } else if (count == 0) {
                if (timeout != 0 && numWaits * WAIT_TIME > timeout) {
                    Log.d("ddms", "read: timeout");
                    throw new IOException("timeout");
                }
                try {
                    Thread.sleep(WAIT_TIME);
                } catch (InterruptedException ie) {
                }
                numWaits++;
            } else {
                numWaits = 0;
            }
        }
    }
    static boolean write(SocketChannel chan, byte[] data) {
        try {
            write(chan, data, -1, DdmPreferences.getTimeOut());
        } catch (IOException e) {
            Log.e("ddms", e);
            return false;
        }
        return true;
    }
    static void write(SocketChannel chan, byte[] data, int length, int timeout)
            throws IOException {
        ByteBuffer buf = ByteBuffer.wrap(data, 0, length != -1 ? length : data.length);
        int numWaits = 0;
        while (buf.position() != buf.limit()) {
            int count;
            count = chan.write(buf);
            if (count < 0) {
                Log.d("ddms", "write: channel EOF");
                throw new IOException("channel EOF");
            } else if (count == 0) {
                if (timeout != 0 && numWaits * WAIT_TIME > timeout) {
                    Log.d("ddms", "write: timeout");
                    throw new IOException("timeout");
                }
                try {
                    Thread.sleep(WAIT_TIME);
                } catch (InterruptedException ie) {
                }
                numWaits++;
            } else {
                numWaits = 0;
            }
        }
    }
    static void setDevice(SocketChannel adbChan, Device device)
            throws IOException {
        if (device != null) {
            String msg = "host:transport:" + device.getSerialNumber(); 
            byte[] device_query = formAdbRequest(msg);
            if (write(adbChan, device_query) == false)
                throw new IOException("failed submitting device (" + device +
                        ") request to ADB");
            AdbResponse resp = readAdbResponse(adbChan, false );
            if (!resp.okay)
                throw new IOException("device (" + device +
                        ") request rejected: " + resp.message);
        }
    }
}
