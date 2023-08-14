public class SdpProvider extends NetHooks.Provider {
    private static final int MAX_PORT = 65535;
    private final boolean enabled;
    private final List<Rule> rules;
    private PrintStream log;
    public SdpProvider() {
        String file = AccessController.doPrivileged(
            new GetPropertyAction("com.sun.sdp.conf"));
        if (file == null) {
            this.enabled = false;
            this.rules = null;
            return;
        }
        List<Rule> list = null;
        if (file != null) {
            try {
                list = loadRulesFromFile(file);
            } catch (IOException e) {
                fail("Error reading %s: %s", file, e.getMessage());
            }
        }
        PrintStream out = null;
        String logfile = AccessController.doPrivileged(
            new GetPropertyAction("com.sun.sdp.debug"));
        if (logfile != null) {
            out = System.out;
            if (logfile.length() > 0) {
                try {
                    out = new PrintStream(logfile);
                } catch (IOException ignore) { }
            }
        }
        this.enabled = !list.isEmpty();
        this.rules = list;
        this.log = out;
    }
    private static enum Action {
        BIND,
        CONNECT;
    }
    private static interface Rule {
        boolean match(Action action, InetAddress address, int port);
    }
    private static class PortRangeRule implements Rule {
        private final Action action;
        private final int portStart;
        private final int portEnd;
        PortRangeRule(Action action, int portStart, int portEnd) {
            this.action = action;
            this.portStart = portStart;
            this.portEnd = portEnd;
        }
        Action action() {
            return action;
        }
        @Override
        public boolean match(Action action, InetAddress address, int port) {
            return (action == this.action &&
                    port >= this.portStart &&
                    port <= this.portEnd);
        }
    }
    private static class AddressPortRangeRule extends PortRangeRule {
        private final byte[] addressAsBytes;
        private final int prefixByteCount;
        private final byte mask;
        AddressPortRangeRule(Action action, InetAddress address,
                             int prefix, int port, int end)
        {
            super(action, port, end);
            this.addressAsBytes = address.getAddress();
            this.prefixByteCount = prefix >> 3;
            this.mask = (byte)(0xff << (8 - (prefix % 8)));
        }
        @Override
        public boolean match(Action action, InetAddress address, int port) {
            if (action != action())
                return false;
            byte[] candidate = address.getAddress();
            if (candidate.length != addressAsBytes.length)
                return false;
            for (int i=0; i<prefixByteCount; i++) {
                if (candidate[i] != addressAsBytes[i])
                    return false;
            }
            if ((prefixByteCount < addressAsBytes.length) &&
                ((candidate[prefixByteCount] & mask) !=
                 (addressAsBytes[prefixByteCount] & mask)))
                    return false;
            return super.match(action, address, port);
        }
    }
    private static int[] parsePortRange(String s) {
        int pos = s.indexOf('-');
        try {
            int[] result = new int[2];
            if (pos < 0) {
                boolean all = s.equals("*");
                result[0] = all ? 0 : Integer.parseInt(s);
                result[1] = all ? MAX_PORT : result[0];
            } else {
                String low = s.substring(0, pos);
                if (low.length() == 0) low = "*";
                String high = s.substring(pos+1);
                if (high.length() == 0) high = "*";
                result[0] = low.equals("*") ? 0 : Integer.parseInt(low);
                result[1] = high.equals("*") ? MAX_PORT : Integer.parseInt(high);
            }
            return result;
        } catch (NumberFormatException e) {
            return new int[0];
        }
    }
    private static void fail(String msg, Object... args) {
        Formatter f = new Formatter();
        f.format(msg, args);
        throw new RuntimeException(f.out().toString());
    }
    private static List<Rule> loadRulesFromFile(String file)
        throws IOException
    {
        Scanner scanner = new Scanner(new File(file));
        try {
            List<Rule> result = new ArrayList<Rule>();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.length() == 0 || line.charAt(0) == '#')
                    continue;
                String[] s = line.split("\\s+");
                if (s.length != 3) {
                    fail("Malformed line '%s'", line);
                    continue;
                }
                Action action = null;
                for (Action a: Action.values()) {
                    if (s[0].equalsIgnoreCase(a.name())) {
                        action = a;
                        break;
                    }
                }
                if (action == null) {
                    fail("Action '%s' not recognized", s[0]);
                    continue;
                }
                int[] ports = parsePortRange(s[2]);
                if (ports.length == 0) {
                    fail("Malformed port range '%s'", s[2]);
                    continue;
                }
                if (s[1].equals("*")) {
                    result.add(new PortRangeRule(action, ports[0], ports[1]));
                    continue;
                }
                int pos = s[1].indexOf('/');
                try {
                    if (pos < 0) {
                        InetAddress[] addresses = InetAddress.getAllByName(s[1]);
                        for (InetAddress address: addresses) {
                            int prefix =
                                (address instanceof Inet4Address) ? 32 : 128;
                            result.add(new AddressPortRangeRule(action, address,
                                prefix, ports[0], ports[1]));
                        }
                    } else {
                        InetAddress address = InetAddress
                            .getByName(s[1].substring(0, pos));
                        int prefix = -1;
                        try {
                            prefix = Integer.parseInt(s[1].substring(pos+1));
                            if (address instanceof Inet4Address) {
                                if (prefix < 0 || prefix > 32) prefix = -1;
                            } else {
                                if (prefix < 0 || prefix > 128) prefix = -1;
                            }
                        } catch (NumberFormatException e) {
                        }
                        if (prefix > 0) {
                            result.add(new AddressPortRangeRule(action,
                                        address, prefix, ports[0], ports[1]));
                        } else {
                            fail("Malformed prefix '%s'", s[1]);
                            continue;
                        }
                    }
                } catch (UnknownHostException uhe) {
                    fail("Unknown host or malformed IP address '%s'", s[1]);
                    continue;
                }
            }
            return result;
        } finally {
            scanner.close();
        }
    }
    private void convertTcpToSdpIfMatch(FileDescriptor fdObj,
                                               Action action,
                                               InetAddress address,
                                               int port)
        throws IOException
    {
        boolean matched = false;
        for (Rule rule: rules) {
            if (rule.match(action, address, port)) {
                SdpSupport.convertSocket(fdObj);
                matched = true;
                break;
            }
        }
        if (log != null) {
            String addr = (address instanceof Inet4Address) ?
                address.getHostAddress() : "[" + address.getHostAddress() + "]";
            if (matched) {
                log.format("%s to %s:%d (socket converted to SDP protocol)\n", action, addr, port);
            } else {
                log.format("%s to %s:%d (no match)\n", action, addr, port);
            }
        }
    }
    @Override
    public void implBeforeTcpBind(FileDescriptor fdObj,
                              InetAddress address,
                              int port)
        throws IOException
    {
        if (enabled)
            convertTcpToSdpIfMatch(fdObj, Action.BIND, address, port);
    }
    @Override
    public void implBeforeTcpConnect(FileDescriptor fdObj,
                                InetAddress address,
                                int port)
        throws IOException
    {
        if (enabled)
            convertTcpToSdpIfMatch(fdObj, Action.CONNECT, address, port);
    }
}
