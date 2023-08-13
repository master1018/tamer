class NetMaskImpl extends PrincipalImpl implements Group, Serializable {
    private static final long serialVersionUID = -7332541893877932896L;
    protected byte[] subnet = null;
    protected int prefix = -1;
    public NetMaskImpl () throws UnknownHostException {
    }
    private byte[] extractSubNet(byte[] b) {
        int addrLength = b.length;
        byte[] subnet = null;
        if (SNMP_LOGGER.isLoggable(Level.FINEST)) {
            SNMP_LOGGER.logp(Level.FINEST, NetMaskImpl.class.getName(),
                "extractSubNet", "BINARY ARRAY :");
            StringBuffer buff = new StringBuffer();
            for(int i =0; i < addrLength; i++) {
                buff.append((b[i] &0xFF) +":");
            }
            SNMP_LOGGER.logp(Level.FINEST, NetMaskImpl.class.getName(),
                "extractSubNet", buff.toString());
        }
        int fullyCoveredByte = prefix / 8;
        if(fullyCoveredByte == addrLength) {
            if (SNMP_LOGGER.isLoggable(Level.FINEST)) {
                SNMP_LOGGER.logp(Level.FINEST, NetMaskImpl.class.getName(), "extractSubNet",
                   "The mask is the complete address, strange..." + addrLength);
            }
            subnet = b;
            return subnet;
        }
        if(fullyCoveredByte > addrLength) {
            if (SNMP_LOGGER.isLoggable(Level.FINEST)) {
                SNMP_LOGGER.logp(Level.FINEST, NetMaskImpl.class.getName(), "extractSubNet",
                   "The number of covered byte is longer than the address. BUG");
            }
            throw new IllegalArgumentException("The number of covered byte is longer than the address.");
        }
        int partialyCoveredIndex = fullyCoveredByte;
        if (SNMP_LOGGER.isLoggable(Level.FINEST)) {
            SNMP_LOGGER.logp(Level.FINEST, NetMaskImpl.class.getName(), "extractSubNet",
               "Partially covered index : " + partialyCoveredIndex);
        }
        byte toDeal = b[partialyCoveredIndex];
        if (SNMP_LOGGER.isLoggable(Level.FINEST)) {
            SNMP_LOGGER.logp(Level.FINEST, NetMaskImpl.class.getName(), "extractSubNet",
               "Partially covered byte : " + toDeal);
        }
        int nbbits = prefix % 8;
        int subnetSize = 0;
        if(nbbits == 0)
        subnetSize = partialyCoveredIndex;
        else
        subnetSize = partialyCoveredIndex + 1;
        if (SNMP_LOGGER.isLoggable(Level.FINEST)) {
            SNMP_LOGGER.logp(Level.FINEST, NetMaskImpl.class.getName(), "extractSubNet",
               "Remains : " + nbbits);
        }
        byte mask = 0;
        for(int i = 0; i < nbbits; i++) {
            mask |= (1 << (7 - i));
        }
        if (SNMP_LOGGER.isLoggable(Level.FINEST)) {
            SNMP_LOGGER.logp(Level.FINEST, NetMaskImpl.class.getName(), "extractSubNet",
               "Mask value : " + (mask & 0xFF));
        }
        byte maskedValue = (byte) ((int)toDeal & (int)mask);
        if (SNMP_LOGGER.isLoggable(Level.FINEST)) {
            SNMP_LOGGER.logp(Level.FINEST, NetMaskImpl.class.getName(), "extractSubNet",
               "Masked byte : "  + (maskedValue &0xFF));
        }
        subnet = new byte[subnetSize];
        if (SNMP_LOGGER.isLoggable(Level.FINEST)) {
            SNMP_LOGGER.logp(Level.FINEST, NetMaskImpl.class.getName(), "extractSubNet",
               "Resulting subnet : ");
        }
        for(int i = 0; i < partialyCoveredIndex; i++) {
            subnet[i] = b[i];
            if (SNMP_LOGGER.isLoggable(Level.FINEST)) {
                SNMP_LOGGER.logp(Level.FINEST, NetMaskImpl.class.getName(), "extractSubNet",
                   (subnet[i] & 0xFF) +":");
            }
        }
        if(nbbits != 0) {
            subnet[partialyCoveredIndex] = maskedValue;
            if (SNMP_LOGGER.isLoggable(Level.FINEST)) {
                SNMP_LOGGER.logp(Level.FINEST, NetMaskImpl.class.getName(), "extractSubNet",
                    "Last subnet byte : " + (subnet[partialyCoveredIndex] &0xFF));
            }
        }
        return subnet;
    }
  public NetMaskImpl (String a, int prefix) throws UnknownHostException {
        super(a);
        this.prefix = prefix;
        subnet = extractSubNet(getAddress().getAddress());
  }
  public boolean addMember(Principal p) {
        return true;
  }
  public int hashCode() {
        return super.hashCode();
  }
    public boolean equals (Object p) {
        if (p instanceof PrincipalImpl || p instanceof NetMaskImpl){
            PrincipalImpl received = (PrincipalImpl) p;
            InetAddress addr = received.getAddress();
            if (SNMP_LOGGER.isLoggable(Level.FINEST)) {
                SNMP_LOGGER.logp(Level.FINEST, NetMaskImpl.class.getName(), "equals",
                    "Received Address : " + addr);
            }
            byte[] recAddr = addr.getAddress();
            for(int i = 0; i < subnet.length; i++) {
                if (SNMP_LOGGER.isLoggable(Level.FINEST)) {
                    SNMP_LOGGER.logp(Level.FINEST, NetMaskImpl.class.getName(), "equals",
                        "(recAddr[i]) : " + (recAddr[i] & 0xFF));
                    SNMP_LOGGER.logp(Level.FINEST, NetMaskImpl.class.getName(), "equals",
                        "(recAddr[i] & subnet[i]) : " +
                         ((recAddr[i] & (int)subnet[i]) &0xFF) +
                         " subnet[i] : " + (subnet[i] &0xFF));
                }
                if((recAddr[i] & subnet[i]) != subnet[i]) {
                    if (SNMP_LOGGER.isLoggable(Level.FINEST)) {
                        SNMP_LOGGER.logp(Level.FINEST, NetMaskImpl.class.getName(), "equals",
                            "FALSE");
                    }
                    return false;
                }
            }
            if (SNMP_LOGGER.isLoggable(Level.FINEST)) {
                SNMP_LOGGER.logp(Level.FINEST, NetMaskImpl.class.getName(), "equals",
                    "TRUE");
            }
            return true;
        } else
            return false;
    }
  public boolean isMember(Principal p) {
        if ((p.hashCode() & super.hashCode()) == p.hashCode()) return true;
        else return false;
  }
  public Enumeration<? extends Principal> members(){
        Vector<Principal> v = new Vector<Principal>(1);
        v.addElement(this);
        return v.elements();
  }
  public boolean removeMember(Principal p) {
        return true;
  }
  public String toString() {
        return ("NetMaskImpl :"+ super.getAddress().toString() + "/" + prefix);
  }
}
