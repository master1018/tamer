package jmri.jmrix.loconet.locoio;

import java.beans.*;
import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetListener;
import jmri.jmrix.loconet.LocoNetMessage;

/**
 *
 * @author plocher
 */
public class LocoIOData implements LocoNetListener, java.beans.PropertyChangeListener {

    private int sv0;

    private int unitAddress;

    private int unitSubAddress;

    private PropertyChangeSupport dataListeners = new PropertyChangeSupport(this);

    /**
     * Define the number of rows in the table, which is also
     * the number of "channels" in a signel LocoIO unit
     */
    private int _numRows = 16;

    /**
     * LocoBuffer is always address 0x01 0x50
     */
    private static final int LocoBufferAddress = 0x0150;

    private String locoBufferVersion = "<unknown>";

    private String locoIOVersion = "<unknown>";

    private String status = "<unknown>";

    /**
     * Per-port SV data
     */
    private LocoIOMode[] lim = new LocoIOMode[_numRows];

    private int[] addr = new int[_numRows];

    private int[] sv = new int[_numRows];

    private int[] v1 = new int[_numRows];

    private int[] v2 = new int[_numRows];

    private int[] readState = new int[_numRows];

    private int[] writeState = new int[_numRows];

    /**
     * Record whether this pin is looking to capture a value
     * from the LocoNet
     */
    private boolean[] capture = new boolean[_numRows];

    private String[] mode = new String[_numRows];

    private LocoIOModeList validmodes;

    /** Creates a new instance of LocoIOData */
    public LocoIOData(int unitAddr, int unitSubAddr) {
        timeoutcounter = 0;
        unitAddress = unitAddr;
        unitSubAddress = unitSubAddr;
        validmodes = new LocoIOModeList();
        for (int i = 0; i < _numRows; i++) {
            setMode(i, "<none>");
            lim[i] = null;
            setAddr(i, 0);
            setSV(i, 0);
            setV1(i, 0);
            setV2(i, 0);
            readState[i] = NONE;
            writeState[i] = NONE;
            capture[i] = false;
        }
        if (LnTrafficController.instance() != null) {
            LnTrafficController.instance().addLocoNetListener(~0, this);
        } else {
            log.error("No LocoNet interface available");
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        dataListeners.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        dataListeners.removePropertyChangeListener(pcl);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String s = "LocoIOData: " + evt.getPropertyName() + " := " + evt.getNewValue() + " from " + evt.getSource();
        System.out.println(s);
    }

    /**
     * Address and SubAddress of this device
     *<p>
     * High byte of the Address is fixed to 0x01
     *<p>
     * Low byte Address must be in the range of
     * 0x01 .. 0x4F, 0x51 .. 0x7F
     * <P>
     * 0x50 is reserved for the LocoBuffer
     *<p>
     *The subAddress is in the range of 0x01 .. 0x7E
     *<p>
     * 0x7f is reserved
     */
    public synchronized void setUnitAddress(int unit, int unitSub) {
        setUnitAddress(unit);
        setUnitSubAddress(unitSub);
    }

    public synchronized void setUnitAddress(int unit) {
        dataListeners.firePropertyChange("UnitAddress", Integer.valueOf(unitAddress), Integer.valueOf(0x0100 | (unit & 0x07F)));
        unitAddress = 0x0100 | (unit & 0x07F);
    }

    public synchronized void setUnitSubAddress(int unitSub) {
        dataListeners.firePropertyChange("UnitSubAddress", Integer.valueOf(unitSubAddress), Integer.valueOf(unitSub & 0x07F));
        unitSubAddress = unitSub & 0x07F;
    }

    public synchronized int getUnitAddress() {
        return unitAddress & 0x07F;
    }

    public synchronized int getUnitSubAddress() {
        return unitSubAddress & 0x07F;
    }

    /**
     * TODO: LocoIO Board level configuration
     *<p>
     * Bit 0: 0 =default, 1=Port Refresh<br>
     * Bit 1: 0 = Fixed code PBs, 1= Alternated code PBs<br>
     * Bit 2: 0 = default - Not used<br>
     * Bit 3: 0 = default, 1 = Ports 5-12 are Servo Ports<br>
     * Bit 4-7: Blink Rate<br>
     */
    public void setUnitConfig(int portRefresh, int altCodePBs, int isServo, int blinkRate) {
        int newsv0 = ((portRefresh & 0x01)) | ((altCodePBs & 0x01) << 0x01) | ((isServo & 0x01) << 0x03) | ((blinkRate & 0x0F) << 0x04);
        dataListeners.firePropertyChange("UnitConfig", Integer.valueOf(sv0), Integer.valueOf(newsv0));
        sv0 = newsv0;
    }

    public int getUnitConfig() {
        return sv0 & 0xFF;
    }

    public void setLBVersion(String version) {
        locoBufferVersion = version;
        dataListeners.firePropertyChange("LBVersionChange", "", locoBufferVersion);
    }

    public String getLBVersion() {
        return locoBufferVersion;
    }

    public void setLIOVersion(String version) {
        locoIOVersion = version;
        dataListeners.firePropertyChange("LIOVersionChange", "", locoIOVersion);
    }

    public String getLIOVersion() {
        return locoBufferVersion;
    }

    public void setStatus(String msg) {
        status = msg;
        dataListeners.firePropertyChange("StatusChange", "", status);
    }

    public String getStatus() {
        return status;
    }

    public void setSV(int channel, int value) {
        sv[channel] = value & 0xFF;
        dataListeners.firePropertyChange("PortChange", Integer.valueOf(-1), Integer.valueOf(channel));
    }

    public int getSV(int channel) {
        return sv[channel] & 0xFF;
    }

    public void setV1(int channel, LocoIOMode l, int address) {
        setV1(channel, validmodes.addressToValue1(l, getAddr(channel)));
    }

    public void setV1(int channel, int value) {
        v1[channel] = value & 0xFF;
        dataListeners.firePropertyChange("PortChange", Integer.valueOf(-1), Integer.valueOf(channel));
    }

    public int getV1(int channel) {
        return v1[channel] & 0xFF;
    }

    public void setV2(int channel, LocoIOMode l, int address) {
        setV2(channel, validmodes.addressToValue2(l, getAddr(channel)));
    }

    public void setV2(int channel, int value) {
        v2[channel] = value & 0xFF;
        dataListeners.firePropertyChange("PortChange", Integer.valueOf(-1), Integer.valueOf(channel));
    }

    public int getV2(int channel) {
        return v2[channel] & 0xFF;
    }

    /**
     * The addr field (for the address info used in each LocoIO channel)
     *
     * @param channel integer value of the addresses in use for this row (0=invalid)
     */
    public void setAddr(int channel, int value) {
        addr[channel] = value & 0x7FF;
        dataListeners.firePropertyChange("PortChange", Integer.valueOf(-1), Integer.valueOf(channel));
    }

    public int getAddr(int channel) {
        return addr[channel] & 0x7FF;
    }

    public void setMode(int channel, String m) {
        mode[channel] = m;
        dataListeners.firePropertyChange("PortChange", Integer.valueOf(-1), Integer.valueOf(channel));
    }

    public String getMode(int channel) {
        return mode[channel];
    }

    public void setLIM(int channel, String s) {
        if (validmodes != null) {
            setLIM(channel, validmodes.getLocoIOModeFor(s));
        }
    }

    public void setLIM(int channel) {
        if (validmodes != null) {
            setLIM(channel, validmodes.getLocoIOModeFor(getSV(channel), getV1(channel), getV2(channel)));
        }
    }

    public void setLIM(int channel, LocoIOMode m) {
        lim[channel] = m;
        dataListeners.firePropertyChange("PortChange", Integer.valueOf(-1), Integer.valueOf(channel));
    }

    public LocoIOMode getLIM(int channel) {
        return lim[channel];
    }

    public void readValues(int channel) {
        readState[channel] = READ;
        issueNextOperation();
    }

    public void captureValues(int channel) {
        capture[channel] = true;
    }

    public void writeValues(int channel) {
        writeState[channel] = WRITE;
        issueNextOperation();
    }

    /**
     * Start reading all rows back
     */
    public void readAll() {
        for (int row = 0; row < _numRows; row++) {
            readState[row] = READ;
        }
        issueNextOperation();
    }

    /**
     * Start writing all rows out
     */
    public void writeAll() {
        for (int row = 0; row < _numRows; row++) {
            writeState[row] = WRITE;
        }
        issueNextOperation();
    }

    public LocoIOModeList getLocoIOModeList() {
        return validmodes;
    }

    protected final int NONE = 0;

    protected final int READVALUE1 = 1;

    protected final int READINGVALUE1 = 2;

    protected final int READVALUE2 = 3;

    protected final int READINGVALUE2 = 4;

    protected final int READMODE = 5;

    protected final int READINGMODE = 6;

    protected final int READ = READVALUE1;

    protected final int WRITEVALUE1 = 11;

    protected final int WRITINGVALUE1 = 12;

    protected final int WRITEVALUE2 = 13;

    protected final int WRITINGVALUE2 = 14;

    protected final int WRITEMODE = 15;

    protected final int WRITINGMODE = 16;

    protected final int WRITE = WRITEVALUE1;

    private int lastOpCv = -1;

    private boolean reading = false;

    protected int highPart(int value) {
        return value / 256;
    }

    protected int lowPart(int value) {
        return value - 256 * highPart(value);
    }

    private String dotme(int val) {
        int dit;
        int x = val;
        StringBuffer ret = new StringBuffer();
        if (val == 0) return "0";
        while (x != 0) {
            dit = x % 10;
            ret.insert(0, Integer.toString(dit));
            x = x / 10;
            if (x != 0) ret.insert(0, ".");
        }
        return ret.toString();
    }

    /**
     * Listen to the LocoNet.
     * We're listening for incoming OPC_PEER_XFR messages, which might
     * be part of a read or write sequence.  We're also
     * _sometimes_ listening for commands as part of a "capture"
     * operation.
     *<P>
     * The incoming LocoNet OPC_PEER_XFR messages don't retain any information
     * about the CV number or whether it was a read or write operation.
     * We store the data regardless of whether it was read or write, but
     * we need to remember the cv number in the lastOpCv member.
     *<P>
     * @param m Incoming message
     */
    public synchronized void message(LocoNetMessage m) {
        int opCode = m.getOpCode();
        switch(opCode) {
            case LnConstants.OPC_PEER_XFER:
                int src = m.getElement(2);
                int dst = m.getElement(3) + m.getElement(4) * 256;
                int[] packet = m.getPeerXfrData();
                if (src == lowPart(LocoBufferAddress)) {
                    String lbv = ((packet[2] != 0) ? dotme(packet[2]) : "1.0");
                    setLBVersion(lbv);
                }
                if (dst == LocoBufferAddress && src == lowPart(unitAddress) && (packet[4] == unitSubAddress)) {
                    stopTimer();
                    replyReceived();
                    String fw = ((packet[2] != 0) ? dotme(packet[2]) : "1.3.2");
                    setLIOVersion(fw);
                    if (packet[0] == LocoIO.LOCOIO_SV_READ || reading) {
                        if (lastOpCv >= 0 && lastOpCv <= 50) {
                            int data = (packet[2] != 0) ? packet[5] : packet[7];
                            int channel = (lastOpCv / 3) - 1;
                            if (channel < 0) {
                                log.warn("... channel is less than zero!!!");
                                channel = 0;
                            }
                            int type = lastOpCv - (channel * 3 + 3);
                            log.debug("... updating port " + channel + " SV" + type + "(" + (type == 1 ? "value1" : type == 2 ? "value2" : type == 0 ? "mode" : "unknown") + ") = 0x" + Integer.toHexString(data));
                            if (type == 2) {
                                setV2(channel, data);
                                setMode(channel, "<none>");
                            } else if (type == 1) {
                                setV1(channel, data);
                                setMode(channel, "<none>");
                            } else if (type == 0) {
                                setSV(channel, data);
                                LocoIOMode lim = validmodes.getLocoIOModeFor(getSV(channel), getV1(channel), getV2(channel));
                                if (lim == null) {
                                    setMode(channel, "<none>");
                                    setAddr(channel, 0);
                                    log.debug("Could not find mode!");
                                } else {
                                    setMode(channel, lim.getFullMode());
                                    setAddr(channel, validmodes.valuesToAddress(lim.getOpcode(), getSV(channel), getV1(channel), getV2(channel)));
                                }
                                log.debug("... decoded address (" + "cv=" + Integer.toHexString(getSV(channel)) + " " + "v1=" + Integer.toHexString(getV1(channel)) + " " + "v2=" + Integer.toHexString(getV2(channel)) + ") " + "is " + getAddr(channel) + "(0x" + Integer.toHexString(getAddr(channel)) + ")");
                            } else {
                                log.warn("OPC_PEER_XFR: Type (" + type + ") is not {0,1,2} for channel " + channel);
                            }
                        } else {
                        }
                    }
                    issueNextOperation();
                    return;
                } else return;
            case LnConstants.OPC_INPUT_REP:
                if (log.isDebugEnabled()) {
                    log.debug(LnConstants.OPC_NAME(opCode) + " received");
                }
                for (int i = 0; i < _numRows; i++) {
                    if (capture[i]) {
                        if (log.isDebugEnabled()) log.debug("row set for capture: " + i);
                        int val1 = m.getElement(1);
                        int val2 = m.getElement(2);
                        setAddr(i, ((val2 & 0x0F) << 5) * 256 + ((val1 & 0x7f) << 1) | (((val2 & LnConstants.OPC_SW_REQ_DIR) == LnConstants.OPC_SW_REQ_DIR) ? 0x01 : 0x00));
                        capture[i] = false;
                    }
                }
                return;
            case LnConstants.OPC_SW_REQ:
                if (log.isDebugEnabled()) {
                    log.debug(LnConstants.OPC_NAME(opCode) + " received");
                }
                for (int i = 0; i < _numRows; i++) {
                    if (capture[i]) {
                        if (log.isDebugEnabled()) log.debug("row set for capture: " + i);
                        int val1 = m.getElement(1);
                        int val2 = m.getElement(2);
                        int addr = LocoIO.SENSOR_ADR(val1, val2);
                        setAddr(i, addr);
                        capture[i] = false;
                    }
                }
                return;
            default:
        }
    }

    /**
     * A valid reply has been received, so the read/write
     * worked, and the state should be advanced.
     */
    protected synchronized void replyReceived() {
        timeoutcounter = 0;
        switch(readState[currentPin]) {
            case NONE:
                break;
            case READVALUE1:
            case READINGVALUE1:
                readState[currentPin] = READVALUE2;
                return;
            case READVALUE2:
            case READINGVALUE2:
                readState[currentPin] = READMODE;
                return;
            case READMODE:
            case READINGMODE:
                readState[currentPin] = NONE;
                return;
            default:
                log.error("Pin " + currentPin + " unexpected read state, can't advance " + readState[currentPin]);
                readState[currentPin] = NONE;
                return;
        }
        switch(writeState[currentPin]) {
            case NONE:
                return;
            case WRITEVALUE1:
            case WRITINGVALUE1:
                writeState[currentPin] = WRITEVALUE2;
                return;
            case WRITEVALUE2:
            case WRITINGVALUE2:
                writeState[currentPin] = WRITEMODE;
                return;
            case WRITEMODE:
            case WRITINGMODE:
                writeState[currentPin] = NONE;
                return;
            default:
                log.error("Pin " + currentPin + " unexpected write state, can't advance " + writeState[currentPin]);
                writeState[currentPin] = NONE;
                return;
        }
    }

    private int currentPin = 0;

    /**
     * Look through the table to find the next thing that
     * needs to be read.
     */
    protected synchronized void issueNextOperation() {
        stopTimer();
        for (int i = 0; i < _numRows; i++) {
            currentPin = i;
            if (readState[i] != NONE) {
                switch(readState[i]) {
                    case READVALUE1:
                    case READINGVALUE1:
                        readState[i] = READINGVALUE1;
                        lastOpCv = i * 3 + 4;
                        setStatus("read SV" + lastOpCv + " (port" + i + " v1)");
                        sendReadCommand(unitAddress, unitSubAddress, lastOpCv);
                        return;
                    case READVALUE2:
                    case READINGVALUE2:
                        readState[i] = READINGVALUE2;
                        lastOpCv = i * 3 + 5;
                        setStatus("read SV" + lastOpCv + " (port " + i + " v2)");
                        sendReadCommand(unitAddress, unitSubAddress, lastOpCv);
                        return;
                    case READMODE:
                    case READINGMODE:
                        readState[i] = READINGMODE;
                        lastOpCv = i * 3 + 3;
                        setStatus("read SV" + lastOpCv + " (port " + i + " mode)");
                        sendReadCommand(unitAddress, unitSubAddress, lastOpCv);
                        return;
                    default:
                        log.error("found an unexpected state: " + readState[1] + " on port " + i);
                        return;
                }
            }
        }
        for (int i = 0; i < _numRows; i++) {
            currentPin = i;
            if (writeState[i] != NONE) {
                switch(writeState[i]) {
                    case WRITEVALUE1:
                    case WRITINGVALUE1:
                        writeState[i] = WRITINGVALUE1;
                        lastOpCv = i * 3 + 4;
                        setStatus("write SV" + lastOpCv + " ( port " + i + " v1)");
                        sendWriteCommand(unitAddress, unitSubAddress, lastOpCv, getV1(i));
                        return;
                    case WRITEVALUE2:
                    case WRITINGVALUE2:
                        writeState[i] = WRITINGVALUE2;
                        lastOpCv = i * 3 + 5;
                        setStatus("write SV" + lastOpCv + " (port" + i + " v2)");
                        sendWriteCommand(unitAddress, unitSubAddress, lastOpCv, getV2(i));
                        return;
                    case WRITEMODE:
                    case WRITINGMODE:
                        writeState[i] = WRITINGMODE;
                        lastOpCv = i * 3 + 3;
                        setStatus("write SV" + lastOpCv + " (port" + i + " mode)");
                        sendWriteCommand(unitAddress, unitSubAddress, lastOpCv, getSV(i));
                        return;
                    default:
                        log.error("found an unexpected state: " + writeState[1] + " on port " + i);
                        return;
                }
            }
        }
        setStatus("OK");
        lastOpCv = -1;
        currentPin = 0;
    }

    /**
     * Timer Management
     * Protect against communication failures,
     * addressing mixups and the like.
     */
    private static int TIMEOUT = 2000;

    protected javax.swing.Timer timer = null;

    private int timeoutcounter;

    /**
     * Internal routine to handle a timeout during read/write
     * by retrying the same operation.
     */
    protected synchronized void timeout() {
        if (log.isDebugEnabled()) log.debug("timeout!");
        setStatus("Timeout");
        if (timeoutcounter++ == 5) {
            for (int i = 0; i < _numRows; i++) {
                readState[i] = NONE;
                writeState[i] = NONE;
            }
            setStatus("Aborted");
            setLIOVersion("<unknown>");
            timeoutcounter = 0;
            stopTimer();
        } else {
            issueNextOperation();
        }
    }

    /**
     * Internal routine to start timer to protect the mode-change.
     */
    protected void startTimer() {
        restartTimer(TIMEOUT);
    }

    /**
     * Internal routine to stop timer, as all is well
     */
    protected void stopTimer() {
        if (timer != null) timer.stop();
    }

    /**
     * Internal routine to handle timer starts & restarts
     */
    protected void restartTimer(int delay) {
        if (timer == null) {
            timer = new javax.swing.Timer(delay, new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    timeout();
                }
            });
        }
        timer.stop();
        timer.setInitialDelay(delay);
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * Read a SV from a given LocoIO device
     *
     * @param locoIOAddress
     * @param locoIOSubAddress
     * @param cv
     */
    void sendReadCommand(int locoIOAddress, int locoIOSubAddress, int cv) {
        reading = true;
        LnTrafficController.instance().sendLocoNetMessage(LocoIO.readCV(locoIOAddress, locoIOSubAddress, cv));
        startTimer();
    }

    /**
     * Write an SV to a given LocoIO device
     *
     * @param locoIOAddress
     * @param locoIOSubAddress
     * @param cv
      *@param data
     */
    void sendWriteCommand(int locoIOAddress, int locoIOSubAddress, int cv, int data) {
        reading = false;
        LnTrafficController.instance().sendLocoNetMessage(LocoIO.writeCV(locoIOAddress, locoIOSubAddress, cv, data));
        startTimer();
    }

    public void dispose() {
        if (log.isDebugEnabled()) log.debug("dispose");
        stopTimer();
        LnTrafficController.instance().removeLocoNetListener(~0, this);
        addr = null;
        mode = null;
        sv = null;
        v1 = null;
        v2 = null;
        lim = null;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LocoIOData.class.getName());
}
