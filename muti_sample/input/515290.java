public final class SocketPermission extends Permission implements Serializable {
    private static final long serialVersionUID = -7204263841984476862L;
    static final int SP_CONNECT = 1;
    static final int SP_LISTEN = 2;
    static final int SP_ACCEPT = 4;
    static final int SP_RESOLVE = 8;
    @SuppressWarnings("nls")
    private static final String[] actionNames = { "", "connect", "listen", "",
            "accept", "", "", "", "resolve" };
    private transient boolean isPartialWild;
    private transient boolean isWild;
    private static final int HIGHEST_PORT = 65535;
    private static final int LOWEST_PORT = 0;
    transient String hostName; 
    transient String ipString; 
    transient boolean resolved; 
    transient int portMin = LOWEST_PORT;
    transient int portMax = HIGHEST_PORT;
    private String actions; 
    transient int actionsMask = SP_RESOLVE;
    public SocketPermission(String host, String action) {
        super(host.equals("") ? "localhost" : host); 
        hostName = getHostString(host);
        if (action == null) {
            throw new NullPointerException();
        }
        if (action.equals("")) { 
            throw new IllegalArgumentException();
        }
        setActions(action);
        actions = toCanonicalActionString(action);
        parsePort(host, hostName);
    }
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || this.getClass() != other.getClass()) {
            return false;
        }
        SocketPermission sp = (SocketPermission) other;
        if (!hostName.equalsIgnoreCase(sp.hostName)) {
            if (getIPString(true) == null || !ipString.equalsIgnoreCase(sp.getIPString(true))) {
                return false;
            }
        }
        if (this.actionsMask != SP_RESOLVE) {
            if (this.portMin != sp.portMin) {
                return false;
            }
            if (this.portMax != sp.portMax) {
                return false;
            }
        }
        return this.actionsMask == sp.actionsMask;
    }
    @Override
    public int hashCode() {
        return hostName.hashCode() ^ actionsMask ^ portMin ^ portMax;
    }
    @Override
    public String getActions() {
        return actions;
    }
    private void setActions(String actions) throws IllegalArgumentException {
        if (actions.equals("")) { 
            return;
        }
        boolean parsing = true;
        String action;
        StringBuilder sb = new StringBuilder();
        int pos = 0, length = actions.length();
        while (parsing) {
            char c;
            sb.setLength(0);
            while (pos < length && (c = actions.charAt(pos++)) != ',') {
                sb.append(c);
            }
            if (pos == length) {
                parsing = false;
            }
            action = sb.toString().trim().toLowerCase();
            if (action.equals(actionNames[SP_CONNECT])) {
                actionsMask |= SP_CONNECT;
            } else if (action.equals(actionNames[SP_LISTEN])) {
                actionsMask |= SP_LISTEN;
            } else if (action.equals(actionNames[SP_ACCEPT])) {
                actionsMask |= SP_ACCEPT;
            } else if (action.equals(actionNames[SP_RESOLVE])) {
            } else {
                throw new IllegalArgumentException(Msg.getString("K0048", 
                        action));
            }
        }
    }
    @Override
    public boolean implies(Permission p) {
        SocketPermission sp;
        try {
            sp = (SocketPermission) p;
        } catch (ClassCastException e) {
            return false;
        }
        if (sp == null || (actionsMask & sp.actionsMask) != sp.actionsMask) {
            return false;
        }
        if (!p.getActions().equals("resolve")) { 
            if ((sp.portMin < this.portMin) || (sp.portMax > this.portMax)) {
                return false;
            }
        }
        return checkHost(sp);
    }
    @Override
    public PermissionCollection newPermissionCollection() {
        return new SocketPermissionCollection();
    }
    private void parsePort(String hostPort, String host) throws IllegalArgumentException {
       String port = hostPort.substring(host.length());
       String emptyString = ""; 
       if (emptyString.equals(port)) {
           portMin = 80;
           portMax = 80;
           return;
       }
       if (":*".equals(port)) {
           portMin = 0;
           portMax = 65535;
           return;
       }
       port = port.substring(1);
       int negIdx = port.indexOf('-');
       String strPortMin = emptyString;
       String strPortMax = emptyString;
       if (-1 == negIdx) {
           strPortMin = port;
           strPortMax = port;
       } else {
           strPortMin = port.substring(0, negIdx);
           strPortMax = port.substring(negIdx + 1);
           if (emptyString.equals(strPortMin)) {
               strPortMin = "0";
           }
           if (emptyString.equals(strPortMax)) {
               strPortMax = "65535";
           }
       }
       try {
           portMin = Integer.valueOf(strPortMin).intValue();
           portMax = Integer.valueOf(strPortMax).intValue();
           if (portMin > portMax) {
               throw new IllegalArgumentException(Msg.getString("K0049", port)); 
           }
       } catch (NumberFormatException e) {
           throw new IllegalArgumentException(Msg.getString("K004a", port)); 
       }
    }
    private String toCanonicalActionString(String action) {
        if (action == null || action.equals("") || actionsMask == SP_RESOLVE) { 
            return actionNames[SP_RESOLVE]; 
        }
        StringBuilder sb = new StringBuilder();
        if ((actionsMask & SP_CONNECT) == SP_CONNECT) {
            sb.append(',');
            sb.append(actionNames[SP_CONNECT]);
        }
        if ((actionsMask & SP_LISTEN) == SP_LISTEN) {
            sb.append(',');
            sb.append(actionNames[SP_LISTEN]);
        }
        if ((actionsMask & SP_ACCEPT) == SP_ACCEPT) {
            sb.append(',');
            sb.append(actionNames[SP_ACCEPT]);
        }
        sb.append(',');
        sb.append(actionNames[SP_RESOLVE]);
        return actions = sb.substring(1, sb.length());
    }
    private String getIPString(boolean isCheck) {
        if (!resolved) {
            try {
                ipString = InetAddress.getHostNameInternal(hostName, isCheck);
            } catch (UnknownHostException e) {
            }
            resolved = true;
        }
        return ipString;
    }
    private String getHostString(String host) throws IllegalArgumentException {
        host = host.trim();
        int idx = -1;
        idx = host.indexOf(':');
        isPartialWild = (host.length() > 0 && host.charAt(0) == '*');
        if (isPartialWild) {
            resolved = true;
            isWild = (host.length() == 1);
            if (isWild) {
                return host;
            }
            if (idx > -1) {
                host = host.substring(0, idx);
            }
            return host.toLowerCase();
        }
        int lastIdx = host.lastIndexOf(':');
        if (idx == lastIdx) {
            if (-1 != idx) {
                host = host.substring(0, idx);
            }
            return host.toLowerCase();
        }
        boolean isFirstBracket = (host.charAt(0) == '[');
        if (!isFirstBracket) {
            int colonNum = 0;
            for (int i = 0; i < host.length(); ++i) {
                if (host.charAt(i) == ':') {
                    colonNum++;
                }
            }
            if (8 == colonNum) {
                host = host.substring(0, lastIdx);
            }
            if (Inet6Util.isIP6AddressInFullForm(host)) {
                return host.toLowerCase();
            }
            throw new IllegalArgumentException(Msg.getString("K004a", host));
        }
        int bbracketIdx = host.indexOf(']');
        if (-1 == bbracketIdx) {
            throw new IllegalArgumentException(Msg.getString("K004a", host));
        }
        host = host.substring(0, bbracketIdx + 1);
        if (Inet6Util.isValidIP6Address(host)) {
            return host.toLowerCase();
        }
        throw new IllegalArgumentException(Msg.getString("K004a", host));
    }
    boolean checkHost(SocketPermission sp) {
        if (isPartialWild) {
            if (isWild) {
                return true; 
            }
            int length = hostName.length() - 1;
            return sp.hostName.regionMatches(sp.hostName.length() - length,
                    hostName, 1, length);
        }
        return (getIPString(false) != null && ipString.equals(sp.getIPString(false)))
                || hostName.equals(sp.hostName);
    }
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
    }
    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        stream.defaultReadObject();
        isPartialWild = false;
        isWild = false;
        portMin = LOWEST_PORT;
        portMax = HIGHEST_PORT;
        actionsMask = SP_RESOLVE;
        hostName = getHostString(getName());
        parsePort(getName(), hostName);
        setActions(actions);
    }
}
