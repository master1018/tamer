public class SnmpAdaptorServer extends CommunicatorServer
    implements SnmpAdaptorServerMBean, MBeanRegistration, SnmpDefinitions,
               SnmpMibHandler {
    private int                 trapPort = 162;
    private int                 informPort = 162;
    InetAddress address = null;
    private Object ipacl = null;
    private SnmpPduFactory pduFactory = null;
    private SnmpUserDataFactory userDataFactory = null;
    private boolean authRespEnabled = true;
    private boolean authTrapEnabled = true;
    private SnmpOid enterpriseOid = new SnmpOid("1.3.6.1.4.1.42");
    int bufferSize = 1024;
    private transient long            startUpTime     = 0;
    private transient DatagramSocket  socket          = null;
    transient DatagramSocket          trapSocket      = null;
    private transient SnmpSession     informSession   = null;
    private transient DatagramPacket  packet          = null;
    transient Vector<SnmpMibAgent>    mibs            = new Vector<SnmpMibAgent>();
    private transient SnmpMibTree     root;
    private transient boolean         useAcl = true;
    private int maxTries = 3 ;
    private int timeout = 3 * 1000 ;
    int snmpOutTraps=0;
    private int snmpOutGetResponses=0;
    private int snmpOutGenErrs=0;
    private int snmpOutBadValues=0;
    private int snmpOutNoSuchNames=0;
    private int snmpOutTooBigs=0;
    int snmpOutPkts=0;
    private int snmpInASNParseErrs=0;
    private int snmpInBadCommunityUses=0;
    private int snmpInBadCommunityNames=0;
    private int snmpInBadVersions=0;
    private int snmpInGetRequests=0;
    private int snmpInGetNexts=0;
    private int snmpInSetRequests=0;
    private int snmpInPkts=0;
    private int snmpInTotalReqVars=0;
    private int snmpInTotalSetVars=0;
    private int snmpSilentDrops=0;
    private static final String InterruptSysCallMsg =
        "Interrupted system call";
    static final SnmpOid sysUpTimeOid = new SnmpOid("1.3.6.1.2.1.1.3.0") ;
    static final SnmpOid snmpTrapOidOid = new SnmpOid("1.3.6.1.6.3.1.1.4.1.0");
    private ThreadService threadService;
    private static int threadNumber = 6;
    static {
        String s = System.getProperty("com.sun.jmx.snmp.threadnumber");
        if (s != null) {
            try {
                threadNumber = Integer.parseInt(System.getProperty(s));
            } catch (Exception e) {
                SNMP_ADAPTOR_LOGGER.logp(Level.FINER,
                        SnmpAdaptorServer.class.getName(),
                        "<static init>",
                        "Got wrong value for com.sun.jmx.snmp.threadnumber: " +
                        s + ". Use the default value: " + threadNumber);
            }
        }
    }
    public SnmpAdaptorServer() {
        this(true, null, com.sun.jmx.snmp.ServiceName.SNMP_ADAPTOR_PORT,
             null) ;
    }
    public SnmpAdaptorServer(int port) {
        this(true, null, port, null) ;
    }
    public SnmpAdaptorServer(InetAddressAcl acl) {
        this(false, acl, com.sun.jmx.snmp.ServiceName.SNMP_ADAPTOR_PORT,
             null) ;
    }
    public SnmpAdaptorServer(InetAddress addr) {
        this(true, null, com.sun.jmx.snmp.ServiceName.SNMP_ADAPTOR_PORT,
             addr) ;
    }
    public SnmpAdaptorServer(InetAddressAcl acl, int port) {
        this(false, acl, port, null) ;
    }
    public SnmpAdaptorServer(int port, InetAddress addr) {
        this(true, null, port, addr) ;
    }
    public SnmpAdaptorServer(InetAddressAcl acl, InetAddress addr) {
        this(false, acl, com.sun.jmx.snmp.ServiceName.SNMP_ADAPTOR_PORT,
             addr) ;
    }
    public SnmpAdaptorServer(InetAddressAcl acl, int port, InetAddress addr) {
        this(false, acl, port, addr);
    }
    public SnmpAdaptorServer(boolean useAcl, int port, InetAddress addr) {
        this(useAcl,null,port,addr);
    }
    private SnmpAdaptorServer(boolean forceAcl, InetAddressAcl acl,
                              int port, InetAddress addr) {
        super(CommunicatorServer.SNMP_TYPE) ;
        if (acl == null && forceAcl) {
            try {
                acl = (InetAddressAcl)
                    new SnmpAcl("SNMP protocol adaptor IP ACL");
            } catch (UnknownHostException e) {
                if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, dbgTag,
                        "constructor", "UnknowHostException when creating ACL",e);
                }
            }
        } else {
            this.useAcl = (acl!=null) || forceAcl;
        }
        init(acl, port, addr) ;
    }
    public int getServedClientCount() {
        return super.getServedClientCount();
    }
    public int getActiveClientCount() {
        return super.getActiveClientCount();
    }
    public int getMaxActiveClientCount() {
        return super.getMaxActiveClientCount();
    }
    public void setMaxActiveClientCount(int c)
        throws java.lang.IllegalStateException {
        super.setMaxActiveClientCount(c);
    }
    public InetAddressAcl getInetAddressAcl() {
        return (InetAddressAcl)ipacl;
    }
    public Integer getTrapPort() {
        return new Integer(trapPort) ;
    }
    public void setTrapPort(Integer port) {
        setTrapPort(port.intValue());
    }
    public void setTrapPort(int port) {
        int val= port ;
        if (val < 0) throw new
            IllegalArgumentException("Trap port cannot be a negative value");
        trapPort= val ;
    }
    public int getInformPort() {
        return informPort;
    }
    public void setInformPort(int port) {
        if (port < 0)
            throw new IllegalArgumentException("Inform request port "+
                                               "cannot be a negative value");
        informPort= port ;
    }
    public String getProtocol() {
        return "snmp";
    }
    public Integer getBufferSize() {
        return new Integer(bufferSize) ;
    }
    public void setBufferSize(Integer s)
        throws java.lang.IllegalStateException {
        if ((state == ONLINE) || (state == STARTING)) {
            throw new IllegalStateException("Stop server before carrying out"+
                                            " this operation");
        }
        bufferSize = s.intValue() ;
    }
    final public int getMaxTries() {
        return maxTries;
    }
    final public synchronized void setMaxTries(int newMaxTries) {
        if (newMaxTries < 0)
            throw new IllegalArgumentException();
        maxTries = newMaxTries;
    }
    final public int getTimeout() {
        return timeout;
    }
    final public synchronized void setTimeout(int newTimeout) {
        if (newTimeout < 0)
            throw new IllegalArgumentException();
        timeout= newTimeout;
    }
    public SnmpPduFactory getPduFactory() {
        return pduFactory ;
    }
    public void setPduFactory(SnmpPduFactory factory) {
        if (factory == null)
            pduFactory = new SnmpPduFactoryBER() ;
        else
            pduFactory = factory ;
    }
    public void setUserDataFactory(SnmpUserDataFactory factory) {
        userDataFactory = factory ;
    }
    public SnmpUserDataFactory getUserDataFactory() {
        return userDataFactory;
    }
    public boolean getAuthTrapEnabled() {
        return authTrapEnabled ;
    }
    public void setAuthTrapEnabled(boolean enabled) {
        authTrapEnabled = enabled ;
    }
    public boolean getAuthRespEnabled() {
        return authRespEnabled ;
    }
    public void setAuthRespEnabled(boolean enabled) {
        authRespEnabled = enabled ;
    }
    public String getEnterpriseOid() {
        return enterpriseOid.toString() ;
    }
    public void setEnterpriseOid(String oid) throws IllegalArgumentException {
        enterpriseOid = new SnmpOid(oid) ;
    }
    public String[] getMibs() {
        String[] result = new String[mibs.size()] ;
        int i = 0 ;
        for (Enumeration e = mibs.elements() ; e.hasMoreElements() ;) {
            SnmpMibAgent mib = (SnmpMibAgent)e.nextElement() ;
            result[i++] = mib.getMibName();
        }
        return result ;
    }
    public Long getSnmpOutTraps() {
        return new Long(snmpOutTraps);
    }
    public Long getSnmpOutGetResponses() {
        return new Long(snmpOutGetResponses);
    }
    public Long getSnmpOutGenErrs() {
        return new Long(snmpOutGenErrs);
    }
    public Long getSnmpOutBadValues() {
        return new Long(snmpOutBadValues);
    }
    public Long getSnmpOutNoSuchNames() {
        return new Long(snmpOutNoSuchNames);
    }
    public Long getSnmpOutTooBigs() {
        return new Long(snmpOutTooBigs);
    }
    public Long getSnmpInASNParseErrs() {
        return new Long(snmpInASNParseErrs);
    }
    public Long getSnmpInBadCommunityUses() {
        return new Long(snmpInBadCommunityUses);
    }
    public Long getSnmpInBadCommunityNames() {
        return new Long(snmpInBadCommunityNames);
    }
    public Long getSnmpInBadVersions() {
        return new Long(snmpInBadVersions);
    }
    public Long getSnmpOutPkts() {
        return new Long(snmpOutPkts);
    }
    public Long getSnmpInPkts() {
        return new Long(snmpInPkts);
    }
    public Long getSnmpInGetRequests() {
        return new Long(snmpInGetRequests);
    }
    public Long getSnmpInGetNexts() {
        return new Long(snmpInGetNexts);
    }
    public Long getSnmpInSetRequests() {
        return new Long(snmpInSetRequests);
    }
    public Long getSnmpInTotalSetVars() {
        return new Long(snmpInTotalSetVars);
    }
    public Long getSnmpInTotalReqVars() {
        return new Long(snmpInTotalReqVars);
    }
    public Long getSnmpSilentDrops() {
        return new Long(snmpSilentDrops);
    }
    public Long getSnmpProxyDrops() {
        return new Long(0);
    }
    public ObjectName preRegister(MBeanServer server, ObjectName name)
        throws java.lang.Exception {
        if (name == null) {
            name = new ObjectName(server.getDefaultDomain() + ":" +
                             com.sun.jmx.snmp.ServiceName.SNMP_ADAPTOR_SERVER);
        }
        return (super.preRegister(server, name));
    }
    public void postRegister (Boolean registrationDone) {
        super.postRegister(registrationDone);
    }
    public void preDeregister() throws java.lang.Exception {
        super.preDeregister();
    }
    public void postDeregister() {
        super.postDeregister();
    }
    public SnmpMibHandler addMib(SnmpMibAgent mib)
        throws IllegalArgumentException {
        if (mib == null) {
            throw new IllegalArgumentException() ;
        }
        if(!mibs.contains(mib))
            mibs.addElement(mib);
        root.register(mib);
        return this;
    }
    public SnmpMibHandler addMib(SnmpMibAgent mib, SnmpOid[] oids)
        throws IllegalArgumentException {
        if (mib == null) {
            throw new IllegalArgumentException() ;
        }
        if(oids == null)
            return addMib(mib);
        if(!mibs.contains(mib))
            mibs.addElement(mib);
        for (int i = 0; i < oids.length; i++) {
            root.register(mib, oids[i].longValue());
        }
        return this;
    }
    public SnmpMibHandler addMib(SnmpMibAgent mib, String contextName)
        throws IllegalArgumentException {
        return addMib(mib);
    }
    public SnmpMibHandler addMib(SnmpMibAgent mib,
                                 String contextName,
                                 SnmpOid[] oids)
        throws IllegalArgumentException {
        return addMib(mib, oids);
    }
    public boolean removeMib(SnmpMibAgent mib, String contextName) {
        return removeMib(mib);
    }
    public boolean removeMib(SnmpMibAgent mib) {
        root.unregister(mib);
        return (mibs.removeElement(mib)) ;
    }
    public boolean removeMib(SnmpMibAgent mib, SnmpOid[] oids) {
        root.unregister(mib, oids);
        return (mibs.removeElement(mib)) ;
    }
    public boolean removeMib(SnmpMibAgent mib,
                             String contextName,
                             SnmpOid[] oids) {
        return removeMib(mib, oids);
    }
    protected void doBind()
        throws CommunicationException, InterruptedException {
        try {
            synchronized (this) {
                socket = new DatagramSocket(port, address) ;
            }
            dbgTag = makeDebugTag();
        } catch (SocketException e) {
            if (e.getMessage().equals(InterruptSysCallMsg))
                throw new InterruptedException(e.toString()) ;
            else {
                if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, dbgTag,
                        "doBind", "cannot bind on port " + port);
                }
                throw new CommunicationException(e) ;
            }
        }
    }
    public int getPort() {
        synchronized (this) {
            if (socket != null) return socket.getLocalPort();
        }
        return super.getPort();
    }
    protected void doUnbind()
        throws CommunicationException, InterruptedException {
        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
            SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                "doUnbind","Finally close the socket");
        }
        synchronized (this) {
            if (socket != null) {
                socket.close() ;
                socket = null ;
            }
        }
        closeTrapSocketIfNeeded() ;
        closeInformSocketIfNeeded() ;
    }
    void createSnmpRequestHandler(SnmpAdaptorServer server, int id,
                                  DatagramSocket s, DatagramPacket p,
                                  SnmpMibTree tree, Vector m, Object a,
                                  SnmpPduFactory factory,
                                  SnmpUserDataFactory dataFactory,
                                  MBeanServer f, ObjectName n) {
        final SnmpRequestHandler handler =
            new SnmpRequestHandler(this, id, s, p, tree, m, a, factory,
                                   dataFactory, f, n);
        threadService.submitTask(handler);
    }
    protected void doReceive()
        throws CommunicationException, InterruptedException {
        try {
            packet = new DatagramPacket(new byte[bufferSize], bufferSize) ;
            socket.receive(packet);
            int state = getState();
            if(state != ONLINE) {
                if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                        "doReceive","received a message but state not online, returning.");
                }
                return;
            }
            createSnmpRequestHandler(this, servedClientCount, socket,
                                     packet, root, mibs, ipacl, pduFactory,
                                     userDataFactory, topMBS, objectName);
        } catch (SocketException e) {
            if (e.getMessage().equals(InterruptSysCallMsg))
                throw new InterruptedException(e.toString()) ;
            else
                throw new CommunicationException(e) ;
        } catch (InterruptedIOException e) {
            throw new InterruptedException(e.toString()) ;
        } catch (CommunicationException e) {
            throw e ;
        } catch (Exception e) {
            throw new CommunicationException(e) ;
        }
        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
            SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                "doReceive", "received a message");
        }
    }
    protected void doError(Exception e) throws CommunicationException {
        return;
    }
    protected void doProcess()
        throws CommunicationException, InterruptedException {
    }
    protected int getBindTries() {
        return 1;
    }
    public void stop(){
        final int port = getPort();
        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
            SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                "stop", "Stopping: using port " + port);
        }
        if ((state == ONLINE) || (state == STARTING)){
            super.stop();
            try {
                DatagramSocket sn = new DatagramSocket(0);
                try {
                    byte[] ob = new byte[1];
                    DatagramPacket pk;
                    if (address != null)
                        pk = new DatagramPacket(ob , 1, address, port);
                    else
                        pk = new DatagramPacket(ob , 1,
                                 java.net.InetAddress.getLocalHost(), port);
                    if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                            "stop", "Sending: using port " + port);
                    }
                    sn.send(pk);
                } finally {
                    sn.close();
                }
            } catch (Throwable e){
                if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, dbgTag,
                        "stop", "Got unexpected Throwable", e);
                }
            }
        }
    }
    public void snmpV1Trap(int generic, int specific,
                           SnmpVarBindList varBindList)
        throws IOException, SnmpStatusException {
        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
            SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                "snmpV1Trap", "generic=" + generic +
                  ", specific=" + specific);
        }
        SnmpPduTrap pdu = new SnmpPduTrap() ;
        pdu.address = null ;
        pdu.port = trapPort ;
        pdu.type = pduV1TrapPdu ;
        pdu.version = snmpVersionOne ;
        pdu.community = null ;
        pdu.enterprise = enterpriseOid ;
        pdu.genericTrap = generic ;
        pdu.specificTrap = specific ;
        pdu.timeStamp = getSysUpTime();
        if (varBindList != null) {
            pdu.varBindList = new SnmpVarBind[varBindList.size()] ;
            varBindList.copyInto(pdu.varBindList);
        }
        else
            pdu.varBindList = null ;
        try {
            if (address != null)
                pdu.agentAddr = handleMultipleIpVersion(address.getAddress());
            else pdu.agentAddr =
              handleMultipleIpVersion(InetAddress.getLocalHost().getAddress());
        } catch (UnknownHostException e) {
            byte[] zeroedAddr = new byte[4];
            pdu.agentAddr = handleMultipleIpVersion(zeroedAddr) ;
        }
        sendTrapPdu(pdu) ;
    }
    private SnmpIpAddress handleMultipleIpVersion(byte[] address) {
        if(address.length == 4)
          return new SnmpIpAddress(address);
        else {
            if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, dbgTag,
                    "handleMultipleIPVersion",
                      "Not an IPv4 address, return null");
            }
            return null;
        }
    }
    public void snmpV1Trap(InetAddress addr, String cs, int generic,
                           int specific, SnmpVarBindList varBindList)
        throws IOException, SnmpStatusException {
        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
            SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                "snmpV1Trap", "generic=" + generic + ", specific=" +
                  specific);
        }
        SnmpPduTrap pdu = new SnmpPduTrap() ;
        pdu.address = null ;
        pdu.port = trapPort ;
        pdu.type = pduV1TrapPdu ;
        pdu.version = snmpVersionOne ;
        if(cs != null)
            pdu.community = cs.getBytes();
        else
            pdu.community = null ;
        pdu.enterprise = enterpriseOid ;
        pdu.genericTrap = generic ;
        pdu.specificTrap = specific ;
        pdu.timeStamp = getSysUpTime();
        if (varBindList != null) {
            pdu.varBindList = new SnmpVarBind[varBindList.size()] ;
            varBindList.copyInto(pdu.varBindList);
        }
        else
            pdu.varBindList = null ;
        try {
            if (address != null)
                pdu.agentAddr = handleMultipleIpVersion(address.getAddress());
            else pdu.agentAddr =
              handleMultipleIpVersion(InetAddress.getLocalHost().getAddress());
        } catch (UnknownHostException e) {
            byte[] zeroedAddr = new byte[4];
            pdu.agentAddr = handleMultipleIpVersion(zeroedAddr) ;
        }
        if(addr != null)
            sendTrapPdu(addr, pdu) ;
        else
            sendTrapPdu(pdu);
    }
    public void snmpV1Trap(InetAddress addr,
                           SnmpIpAddress agentAddr,
                           String cs,
                           SnmpOid enterpOid,
                           int generic,
                           int specific,
                           SnmpVarBindList varBindList,
                           SnmpTimeticks time)
        throws IOException, SnmpStatusException {
        snmpV1Trap(addr,
                   trapPort,
                   agentAddr,
                   cs,
                   enterpOid,
                   generic,
                   specific,
                   varBindList,
                   time);
    }
    public void snmpV1Trap(SnmpPeer peer,
                           SnmpIpAddress agentAddr,
                           SnmpOid enterpOid,
                           int generic,
                           int specific,
                           SnmpVarBindList varBindList,
                           SnmpTimeticks time)
        throws IOException, SnmpStatusException {
        SnmpParameters p = (SnmpParameters) peer.getParams();
        snmpV1Trap(peer.getDestAddr(),
                   peer.getDestPort(),
                   agentAddr,
                   p.getRdCommunity(),
                   enterpOid,
                   generic,
                   specific,
                   varBindList,
                   time);
    }
    private void snmpV1Trap(InetAddress addr,
                            int port,
                            SnmpIpAddress agentAddr,
                            String cs,
                            SnmpOid enterpOid,
                            int generic,
                            int specific,
                            SnmpVarBindList varBindList,
                            SnmpTimeticks time)
        throws IOException, SnmpStatusException {
        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
            SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                "snmpV1Trap", "generic=" + generic + ", specific=" +
                  specific);
        }
        SnmpPduTrap pdu = new SnmpPduTrap() ;
        pdu.address = null ;
        pdu.port = port ;
        pdu.type = pduV1TrapPdu ;
        pdu.version = snmpVersionOne ;
        if(cs != null)
            pdu.community = cs.getBytes();
        else
            pdu.community = null ;
        if(enterpOid != null)
            pdu.enterprise = enterpOid;
        else
            pdu.enterprise = enterpriseOid ;
        pdu.genericTrap = generic ;
        pdu.specificTrap = specific ;
        if(time != null)
            pdu.timeStamp = time.longValue();
        else
            pdu.timeStamp = getSysUpTime();
        if (varBindList != null) {
            pdu.varBindList = new SnmpVarBind[varBindList.size()] ;
            varBindList.copyInto(pdu.varBindList);
        }
        else
            pdu.varBindList = null ;
        if (agentAddr == null) {
            try {
                final InetAddress inetAddr =
                    (address!=null)?address:InetAddress.getLocalHost();
                agentAddr = handleMultipleIpVersion(inetAddr.getAddress());
            }  catch (UnknownHostException e) {
                byte[] zeroedAddr = new byte[4];
                agentAddr = handleMultipleIpVersion(zeroedAddr);
            }
        }
        pdu.agentAddr = agentAddr;
        if(addr != null)
            sendTrapPdu(addr, pdu) ;
        else
            sendTrapPdu(pdu);
    }
    public void snmpV2Trap(SnmpPeer peer,
                           SnmpOid trapOid,
                           SnmpVarBindList varBindList,
                           SnmpTimeticks time)
        throws IOException, SnmpStatusException {
        SnmpParameters p = (SnmpParameters) peer.getParams();
        snmpV2Trap(peer.getDestAddr(),
                   peer.getDestPort(),
                   p.getRdCommunity(),
                   trapOid,
                   varBindList,
                   time);
    }
    public void snmpV2Trap(SnmpOid trapOid, SnmpVarBindList varBindList)
        throws IOException, SnmpStatusException {
        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
            SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                "snmpV2Trap", "trapOid=" + trapOid);
        }
        SnmpPduRequest pdu = new SnmpPduRequest() ;
        pdu.address = null ;
        pdu.port = trapPort ;
        pdu.type = pduV2TrapPdu ;
        pdu.version = snmpVersionTwo ;
        pdu.community = null ;
        SnmpVarBindList fullVbl ;
        if (varBindList != null)
            fullVbl = (SnmpVarBindList)varBindList.clone() ;
        else
            fullVbl = new SnmpVarBindList(2) ;
        SnmpTimeticks sysUpTimeValue = new SnmpTimeticks(getSysUpTime()) ;
        fullVbl.insertElementAt(new SnmpVarBind(snmpTrapOidOid, trapOid), 0) ;
        fullVbl.insertElementAt(new SnmpVarBind(sysUpTimeOid, sysUpTimeValue),
                                0);
        pdu.varBindList = new SnmpVarBind[fullVbl.size()] ;
        fullVbl.copyInto(pdu.varBindList) ;
        sendTrapPdu(pdu) ;
    }
    public void snmpV2Trap(InetAddress addr, String cs, SnmpOid trapOid,
                           SnmpVarBindList varBindList)
        throws IOException, SnmpStatusException {
        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
            SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                "snmpV2Trap", "trapOid=" + trapOid);
        }
        SnmpPduRequest pdu = new SnmpPduRequest() ;
        pdu.address = null ;
        pdu.port = trapPort ;
        pdu.type = pduV2TrapPdu ;
        pdu.version = snmpVersionTwo ;
        if(cs != null)
            pdu.community = cs.getBytes();
        else
            pdu.community = null;
        SnmpVarBindList fullVbl ;
        if (varBindList != null)
            fullVbl = (SnmpVarBindList)varBindList.clone() ;
        else
            fullVbl = new SnmpVarBindList(2) ;
        SnmpTimeticks sysUpTimeValue = new SnmpTimeticks(getSysUpTime()) ;
        fullVbl.insertElementAt(new SnmpVarBind(snmpTrapOidOid, trapOid), 0) ;
        fullVbl.insertElementAt(new SnmpVarBind(sysUpTimeOid, sysUpTimeValue),
                                0);
        pdu.varBindList = new SnmpVarBind[fullVbl.size()] ;
        fullVbl.copyInto(pdu.varBindList) ;
        if(addr != null)
            sendTrapPdu(addr, pdu);
        else
            sendTrapPdu(pdu);
    }
    public void snmpV2Trap(InetAddress addr,
                           String cs,
                           SnmpOid trapOid,
                           SnmpVarBindList varBindList,
                           SnmpTimeticks time)
        throws IOException, SnmpStatusException {
        snmpV2Trap(addr,
                   trapPort,
                   cs,
                   trapOid,
                   varBindList,
                   time);
    }
    private void snmpV2Trap(InetAddress addr,
                            int port,
                            String cs,
                            SnmpOid trapOid,
                            SnmpVarBindList varBindList,
                            SnmpTimeticks time)
        throws IOException, SnmpStatusException {
        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
            final StringBuilder strb = new StringBuilder()
                .append("trapOid=").append(trapOid)
                .append("\ncommunity=").append(cs)
                .append("\naddr=").append(addr)
                .append("\nvarBindList=").append(varBindList)
                .append("\ntime=").append(time)
                .append("\ntrapPort=").append(port);
            SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                "snmpV2Trap", strb.toString());
        }
        SnmpPduRequest pdu = new SnmpPduRequest() ;
        pdu.address = null ;
        pdu.port = port ;
        pdu.type = pduV2TrapPdu ;
        pdu.version = snmpVersionTwo ;
        if(cs != null)
            pdu.community = cs.getBytes();
        else
            pdu.community = null;
        SnmpVarBindList fullVbl ;
        if (varBindList != null)
            fullVbl = (SnmpVarBindList)varBindList.clone() ;
        else
            fullVbl = new SnmpVarBindList(2) ;
        SnmpTimeticks sysUpTimeValue = null;
        if(time != null)
            sysUpTimeValue = time;
        else
            sysUpTimeValue = new SnmpTimeticks(getSysUpTime()) ;
        fullVbl.insertElementAt(new SnmpVarBind(snmpTrapOidOid, trapOid), 0) ;
        fullVbl.insertElementAt(new SnmpVarBind(sysUpTimeOid, sysUpTimeValue),
                                0);
        pdu.varBindList = new SnmpVarBind[fullVbl.size()] ;
        fullVbl.copyInto(pdu.varBindList) ;
        if(addr != null)
            sendTrapPdu(addr, pdu) ;
        else
            sendTrapPdu(pdu);
    }
    public void snmpPduTrap(InetAddress address, SnmpPduPacket pdu)
            throws IOException, SnmpStatusException {
        if(address != null)
            sendTrapPdu(address, pdu);
        else
            sendTrapPdu(pdu);
    }
    public void snmpPduTrap(SnmpPeer peer,
                            SnmpPduPacket pdu)
        throws IOException, SnmpStatusException {
        if(peer != null) {
            pdu.port = peer.getDestPort();
            sendTrapPdu(peer.getDestAddr(), pdu);
        }
        else {
            pdu.port = getTrapPort().intValue();
            sendTrapPdu(pdu);
        }
    }
    private void sendTrapPdu(SnmpPduPacket pdu)
     throws SnmpStatusException, IOException {
        SnmpMessage msg = null ;
        try {
            msg = (SnmpMessage)pduFactory.encodeSnmpPdu(pdu, bufferSize) ;
            if (msg == null) {
                throw new SnmpStatusException(
                          SnmpDefinitions.snmpRspAuthorizationError) ;
            }
        }
        catch (SnmpTooBigException x) {
            if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, dbgTag,
                    "sendTrapPdu", "Trap pdu is too big. " +
                     "Trap hasn't been sent to anyone" );
            }
            throw new SnmpStatusException(SnmpDefinitions.snmpRspTooBig) ;
        }
        int sendingCount = 0 ;
        openTrapSocketIfNeeded() ;
        if (ipacl != null) {
            Enumeration ed = ((InetAddressAcl)ipacl).getTrapDestinations() ;
            while (ed.hasMoreElements()) {
                msg.address = (InetAddress)ed.nextElement() ;
                Enumeration ec = ((InetAddressAcl)ipacl).
                    getTrapCommunities(msg.address) ;
                while (ec.hasMoreElements()) {
                    msg.community = ((String)ec.nextElement()).getBytes() ;
                    try {
                        sendTrapMessage(msg) ;
                        sendingCount++ ;
                    }
                    catch (SnmpTooBigException x) {
                        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                            SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, dbgTag,
                                "sendTrapPdu", "Trap pdu is too big. " +
                                 "Trap hasn't been sent to "+msg.address);
                        }
                    }
                }
            }
        }
        if (sendingCount == 0) {
            try {
                msg.address = InetAddress.getLocalHost() ;
                sendTrapMessage(msg) ;
            } catch (SnmpTooBigException x) {
                if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, dbgTag,
                        "sendTrapPdu", "Trap pdu is too big. " +
                         "Trap hasn't been sent.");
                }
            } catch (UnknownHostException e) {
                if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, dbgTag,
                        "sendTrapPdu", "Trap pdu is too big. " +
                         "Trap hasn't been sent.");
                }
            }
        }
        closeTrapSocketIfNeeded() ;
    }
    private void sendTrapPdu(InetAddress addr, SnmpPduPacket pdu)
        throws SnmpStatusException, IOException {
        SnmpMessage msg = null ;
        try {
            msg = (SnmpMessage)pduFactory.encodeSnmpPdu(pdu, bufferSize) ;
            if (msg == null) {
                throw new SnmpStatusException(
                          SnmpDefinitions.snmpRspAuthorizationError) ;
            }
        } catch (SnmpTooBigException x) {
            if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, dbgTag,
                    "sendTrapPdu", "Trap pdu is too big. " +
                     "Trap hasn't been sent to the specified host.");
            }
            throw new SnmpStatusException(SnmpDefinitions.snmpRspTooBig) ;
        }
        openTrapSocketIfNeeded() ;
        if (addr != null) {
            msg.address = addr;
            try {
                sendTrapMessage(msg) ;
            } catch (SnmpTooBigException x) {
                if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, dbgTag,
                        "sendTrapPdu", "Trap pdu is too big. " +
                         "Trap hasn't been sent to " +  msg.address);
                }
            }
        }
        closeTrapSocketIfNeeded() ;
    }
    private void sendTrapMessage(SnmpMessage msg)
        throws IOException, SnmpTooBigException {
        byte[] buffer = new byte[bufferSize] ;
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length) ;
        int encodingLength = msg.encodeMessage(buffer) ;
        packet.setLength(encodingLength) ;
        packet.setAddress(msg.address) ;
        packet.setPort(msg.port) ;
        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
            SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                "sendTrapMessage", "sending trap to " + msg.address + ":" +
                  msg.port);
        }
        trapSocket.send(packet) ;
        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
            SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                "sendTrapMessage", "sent to " + msg.address + ":" +
                  msg.port);
        }
        snmpOutTraps++;
        snmpOutPkts++;
    }
    synchronized void openTrapSocketIfNeeded() throws SocketException {
        if (trapSocket == null) {
            trapSocket = new DatagramSocket(0, address) ;
            if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
                SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                    "openTrapSocketIfNeeded", "using port " +
                      trapSocket.getLocalPort() + " to send traps");
            }
        }
    }
    synchronized void closeTrapSocketIfNeeded() {
        if ((trapSocket != null) && (state != ONLINE)) {
            trapSocket.close() ;
            trapSocket = null ;
        }
    }
    public Vector snmpInformRequest(SnmpInformHandler cb, SnmpOid trapOid,
                                    SnmpVarBindList varBindList)
        throws IllegalStateException, IOException, SnmpStatusException {
        if (!isActive()) {
            throw new IllegalStateException(
               "Start SNMP adaptor server before carrying out this operation");
        }
        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
            SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                "snmpInformRequest", "trapOid=" + trapOid);
        }
        SnmpVarBindList fullVbl ;
        if (varBindList != null)
            fullVbl = (SnmpVarBindList)varBindList.clone() ;
        else
            fullVbl = new SnmpVarBindList(2) ;
        SnmpTimeticks sysUpTimeValue = new SnmpTimeticks(getSysUpTime()) ;
        fullVbl.insertElementAt(new SnmpVarBind(snmpTrapOidOid, trapOid), 0) ;
        fullVbl.insertElementAt(new SnmpVarBind(sysUpTimeOid, sysUpTimeValue),
                                0);
        openInformSocketIfNeeded() ;
        Vector<SnmpInformRequest> informReqList = new Vector<SnmpInformRequest>();
        InetAddress addr = null;
        String cs = null;
        if (ipacl != null) {
            Enumeration ed = ((InetAddressAcl)ipacl).getInformDestinations() ;
            while (ed.hasMoreElements()) {
                addr = (InetAddress)ed.nextElement() ;
                Enumeration ec = ((InetAddressAcl)ipacl).
                    getInformCommunities(addr) ;
                while (ec.hasMoreElements()) {
                    cs = (String)ec.nextElement() ;
                    informReqList.addElement(
                       informSession.makeAsyncRequest(addr, cs, cb,
                                              fullVbl,getInformPort())) ;
                }
            }
        }
        return informReqList ;
    }
    public SnmpInformRequest snmpInformRequest(InetAddress addr,
                                               String cs,
                                               SnmpInformHandler cb,
                                               SnmpOid trapOid,
                                               SnmpVarBindList varBindList)
        throws IllegalStateException, IOException, SnmpStatusException {
        return snmpInformRequest(addr,
                                 getInformPort(),
                                 cs,
                                 cb,
                                 trapOid,
                                 varBindList);
    }
    public SnmpInformRequest snmpInformRequest(SnmpPeer peer,
                                               SnmpInformHandler cb,
                                               SnmpOid trapOid,
                                               SnmpVarBindList varBindList)
        throws IllegalStateException, IOException, SnmpStatusException {
        SnmpParameters p = (SnmpParameters) peer.getParams();
        return snmpInformRequest(peer.getDestAddr(),
                                 peer.getDestPort(),
                                 p.getInformCommunity(),
                                 cb,
                                 trapOid,
                                 varBindList);
    }
    public static final int mapErrorStatus(int errorStatus,
                                           int protocolVersion,
                                           int reqPduType) {
        return SnmpSubRequestHandler.mapErrorStatus(errorStatus,
                                                    protocolVersion,
                                                    reqPduType);
    }
    private SnmpInformRequest snmpInformRequest(InetAddress addr,
                                                int port,
                                                String cs,
                                                SnmpInformHandler cb,
                                                SnmpOid trapOid,
                                                SnmpVarBindList varBindList)
        throws IllegalStateException, IOException, SnmpStatusException {
        if (!isActive()) {
            throw new IllegalStateException(
              "Start SNMP adaptor server before carrying out this operation");
        }
        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
            SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                "snmpInformRequest", "trapOid=" + trapOid);
        }
        SnmpVarBindList fullVbl ;
        if (varBindList != null)
            fullVbl = (SnmpVarBindList)varBindList.clone() ;
        else
            fullVbl = new SnmpVarBindList(2) ;
        SnmpTimeticks sysUpTimeValue = new SnmpTimeticks(getSysUpTime()) ;
        fullVbl.insertElementAt(new SnmpVarBind(snmpTrapOidOid, trapOid), 0) ;
        fullVbl.insertElementAt(new SnmpVarBind(sysUpTimeOid, sysUpTimeValue),
                                0);
        openInformSocketIfNeeded() ;
        return informSession.makeAsyncRequest(addr, cs, cb, fullVbl, port) ;
    }
    synchronized void openInformSocketIfNeeded() throws SocketException {
        if (informSession == null) {
            informSession = new SnmpSession(this) ;
            if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
                SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                   "openInformSocketIfNeeded",
                      "to send inform requests and receive inform responses");
            }
        }
    }
    synchronized void closeInformSocketIfNeeded() {
        if ((informSession != null) && (state != ONLINE)) {
            informSession.destroySession() ;
            informSession = null ;
        }
    }
    InetAddress getAddress() {
        return address;
    }
    protected void finalize() {
        try {
            if (socket != null) {
                socket.close() ;
                socket = null ;
            }
            threadService.terminate();
        } catch (Exception e) {
            if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
                SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                   "finalize", "Exception in finalizer", e);
            }
        }
    }
    String makeDebugTag() {
        return "SnmpAdaptorServer["+ getProtocol() + ":" + getPort() + "]";
    }
    void updateRequestCounters(int pduType) {
        switch(pduType)  {
        case pduGetRequestPdu:
            snmpInGetRequests++;
            break;
        case pduGetNextRequestPdu:
            snmpInGetNexts++;
            break;
        case pduSetRequestPdu:
            snmpInSetRequests++;
            break;
        default:
            break;
        }
        snmpInPkts++ ;
    }
    void updateErrorCounters(int errorStatus) {
        switch(errorStatus) {
        case snmpRspNoError:
            snmpOutGetResponses++;
            break;
        case snmpRspGenErr:
            snmpOutGenErrs++;
            break;
        case snmpRspBadValue:
            snmpOutBadValues++;
            break;
        case snmpRspNoSuchName:
            snmpOutNoSuchNames++;
            break;
        case snmpRspTooBig:
            snmpOutTooBigs++;
            break;
        default:
            break;
        }
        snmpOutPkts++ ;
    }
    void updateVarCounters(int pduType, int n) {
        switch(pduType) {
        case pduGetRequestPdu:
        case pduGetNextRequestPdu:
        case pduGetBulkRequestPdu:
            snmpInTotalReqVars += n ;
            break ;
        case pduSetRequestPdu:
            snmpInTotalSetVars += n ;
            break ;
        }
    }
    void incSnmpInASNParseErrs(int n) {
        snmpInASNParseErrs += n ;
    }
    void incSnmpInBadVersions(int n) {
        snmpInBadVersions += n ;
    }
    void incSnmpInBadCommunityUses(int n) {
        snmpInBadCommunityUses += n ;
    }
    void incSnmpInBadCommunityNames(int n) {
        snmpInBadCommunityNames += n ;
    }
    void incSnmpSilentDrops(int n) {
        snmpSilentDrops += n ;
    }
    long getSysUpTime() {
        return (System.currentTimeMillis() - startUpTime) / 10 ;
    }
    private void readObject(ObjectInputStream stream)
        throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        mibs      = new Vector<SnmpMibAgent>() ;
    }
    private void init(Object acl, int p, InetAddress a) {
        root= new SnmpMibTree();
        root.setDefaultAgent(new SnmpErrorHandlerAgent());
        startUpTime= java.lang.System.currentTimeMillis();
        maxActiveClientCount = 10;
        pduFactory = new SnmpPduFactoryBER() ;
        port = p ;
        ipacl = acl ;
        address = a ;
        if ((ipacl == null) && (useAcl == true))
            throw new IllegalArgumentException("ACL object cannot be null") ;
        threadService = new ThreadService(threadNumber);
    }
    SnmpMibAgent getAgentMib(SnmpOid oid) {
        return root.getAgentMib(oid);
    }
    protected Thread createMainThread() {
        final Thread t = super.createMainThread();
        t.setDaemon(true);
        return t;
    }
}
