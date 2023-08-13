class PortMixer extends AbstractMixer {
    private static final int SRC_UNKNOWN      = 0x01;
    private static final int SRC_MICROPHONE   = 0x02;
    private static final int SRC_LINE_IN      = 0x03;
    private static final int SRC_COMPACT_DISC = 0x04;
    private static final int SRC_MASK         = 0xFF;
    private static final int DST_UNKNOWN      = 0x0100;
    private static final int DST_SPEAKER      = 0x0200;
    private static final int DST_HEADPHONE    = 0x0300;
    private static final int DST_LINE_OUT     = 0x0400;
    private static final int DST_MASK         = 0xFF00;
    private Port.Info[] portInfos;
    private PortMixerPort[] ports;
    private long id = 0;
    PortMixer(PortMixerProvider.PortMixerInfo portMixerInfo) {
        super(portMixerInfo,              
              null,                       
              null,                       
              null);                      
        if (Printer.trace) Printer.trace(">> PortMixer: constructor");
        int count = 0;
        int srcLineCount = 0;
        int dstLineCount = 0;
        try {
            try {
                id = nOpen(getMixerIndex());
                if (id != 0) {
                    count = nGetPortCount(id);
                    if (count < 0) {
                        if (Printer.trace) Printer.trace("nGetPortCount() returned error code: " + count);
                        count = 0;
                    }
                }
            } catch (Exception e) {}
            portInfos = new Port.Info[count];
            for (int i = 0; i < count; i++) {
                int type = nGetPortType(id, i);
                srcLineCount += ((type & SRC_MASK) != 0)?1:0;
                dstLineCount += ((type & DST_MASK) != 0)?1:0;
                portInfos[i] = getPortInfo(i, type);
            }
        } finally {
            if (id != 0) {
                nClose(id);
            }
            id = 0;
        }
        sourceLineInfo = new Port.Info[srcLineCount];
        targetLineInfo = new Port.Info[dstLineCount];
        srcLineCount = 0; dstLineCount = 0;
        for (int i = 0; i < count; i++) {
            if (portInfos[i].isSource()) {
                sourceLineInfo[srcLineCount++] = portInfos[i];
            } else {
                targetLineInfo[dstLineCount++] = portInfos[i];
            }
        }
        if (Printer.trace) Printer.trace("<< PortMixer: constructor completed");
    }
    public Line getLine(Line.Info info) throws LineUnavailableException {
        Line.Info fullInfo = getLineInfo(info);
        if ((fullInfo != null) && (fullInfo instanceof Port.Info)) {
            for (int i = 0; i < portInfos.length; i++) {
                if (fullInfo.equals(portInfos[i])) {
                    return getPort(i);
                }
            }
        }
        throw new IllegalArgumentException("Line unsupported: " + info);
    }
    public int getMaxLines(Line.Info info) {
        Line.Info fullInfo = getLineInfo(info);
        if (fullInfo == null) {
            return 0;
        }
        if (fullInfo instanceof Port.Info) {
            return 1;
        }
        return 0;
    }
    protected void implOpen() throws LineUnavailableException {
        if (Printer.trace) Printer.trace(">> PortMixer: implOpen (id="+id+")");
        id = nOpen(getMixerIndex());
        if (Printer.trace) Printer.trace("<< PortMixer: implOpen succeeded.");
    }
    protected void implClose() {
        if (Printer.trace) Printer.trace(">> PortMixer: implClose");
        long thisID = id;
        id = 0;
        nClose(thisID);
        if (ports != null) {
            for (int i = 0; i < ports.length; i++) {
                if (ports[i] != null) {
                    ports[i].disposeControls();
                }
            }
        }
        if (Printer.trace) Printer.trace("<< PortMixer: implClose succeeded");
    }
    protected void implStart() {}
    protected void implStop() {}
    private Port.Info getPortInfo(int portIndex, int type) {
        switch (type) {
        case SRC_UNKNOWN:      return new PortInfo(nGetPortName(getID(), portIndex), true);
        case SRC_MICROPHONE:   return Port.Info.MICROPHONE;
        case SRC_LINE_IN:      return Port.Info.LINE_IN;
        case SRC_COMPACT_DISC: return Port.Info.COMPACT_DISC;
        case DST_UNKNOWN:      return new PortInfo(nGetPortName(getID(), portIndex), false);
        case DST_SPEAKER:      return Port.Info.SPEAKER;
        case DST_HEADPHONE:    return Port.Info.HEADPHONE;
        case DST_LINE_OUT:     return Port.Info.LINE_OUT;
        }
        if (Printer.debug) Printer.debug("unknown port type: "+type);
        return null;
    }
    int getMixerIndex() {
        return ((PortMixerProvider.PortMixerInfo) getMixerInfo()).getIndex();
    }
    Port getPort(int index) {
        if (ports == null) {
            ports = new PortMixerPort[portInfos.length];
        }
        if (ports[index] == null) {
            ports[index] = new PortMixerPort((Port.Info)portInfos[index], this, index);
            return ports[index];
        }
        return ports[index];
    }
    long getID() {
        return id;
    }
    private static class PortMixerPort extends AbstractLine implements Port {
        private int portIndex;
        private long id;
        private PortMixerPort(Port.Info info,
                              PortMixer mixer,
                              int portIndex) {
            super(info, mixer, null);
            if (Printer.trace) Printer.trace("PortMixerPort CONSTRUCTOR: info: " + info);
            this.portIndex = portIndex;
        }
        void implOpen() throws LineUnavailableException {
            if (Printer.trace) Printer.trace(">> PortMixerPort: implOpen().");
            long newID = ((PortMixer) mixer).getID();
            if ((id == 0) || (newID != id) || (controls.length == 0)) {
                id = newID;
                Vector vector = new Vector();
                synchronized (vector) {
                    nGetControls(id, portIndex, vector);
                    controls = new Control[vector.size()];
                    for (int i = 0; i < controls.length; i++) {
                        controls[i] = (Control) vector.elementAt(i);
                    }
                }
            } else {
                enableControls(controls, true);
            }
            if (Printer.trace) Printer.trace("<< PortMixerPort: implOpen() succeeded");
        }
        private void enableControls(Control[] controls, boolean enable) {
            for (int i = 0; i < controls.length; i++) {
                if (controls[i] instanceof BoolCtrl) {
                    ((BoolCtrl) controls[i]).closed = !enable;
                }
                else if (controls[i] instanceof FloatCtrl) {
                    ((FloatCtrl) controls[i]).closed = !enable;
                }
                else if (controls[i] instanceof CompoundControl) {
                    enableControls(((CompoundControl) controls[i]).getMemberControls(), enable);
                }
            }
        }
        private void disposeControls() {
            enableControls(controls, false);
            controls = new Control[0];
        }
        void implClose() {
            if (Printer.trace) Printer.trace(">> PortMixerPort: implClose()");
            enableControls(controls, false);
            if (Printer.trace) Printer.trace("<< PortMixerPort: implClose() succeeded");
        }
        public void open() throws LineUnavailableException {
            synchronized (mixer) {
                if (!isOpen()) {
                    if (Printer.trace) Printer.trace("> PortMixerPort: open");
                    mixer.open(this);
                    try {
                        implOpen();
                        setOpen(true);
                    } catch (LineUnavailableException e) {
                        mixer.close(this);
                        throw e;
                    }
                    if (Printer.trace) Printer.trace("< PortMixerPort: open succeeded");
                }
            }
        }
        public void close() {
            synchronized (mixer) {
                if (isOpen()) {
                    if (Printer.trace) Printer.trace("> PortMixerPort.close()");
                    setOpen(false);
                    implClose();
                    mixer.close(this);
                    if (Printer.trace) Printer.trace("< PortMixerPort.close() succeeded");
                }
            }
        }
    } 
    private static class BoolCtrl extends BooleanControl {
        private long controlID;
        private boolean closed = false;
        private static BooleanControl.Type createType(String name) {
            if (name.equals("Mute")) {
                return BooleanControl.Type.MUTE;
            }
            else if (name.equals("Select")) {
            }
            return new BCT(name);
        }
        private BoolCtrl(long controlID, String name) {
            this(controlID, createType(name));
        }
        private BoolCtrl(long controlID, BooleanControl.Type typ) {
            super(typ, false);
            this.controlID = controlID;
        }
        public void setValue(boolean value) {
            if (!closed) {
                nControlSetIntValue(controlID, value?1:0);
            }
        }
        public boolean getValue() {
            if (!closed) {
                return (nControlGetIntValue(controlID)!=0)?true:false;
            }
            return false;
        }
        private static class BCT extends BooleanControl.Type {
            private BCT(String name) {
                super(name);
            }
        }
    }
    private static class CompCtrl extends CompoundControl {
        private CompCtrl(String name, Control[] controls) {
            super(new CCT(name), controls);
        }
        private static class CCT extends CompoundControl.Type {
            private CCT(String name) {
                super(name);
            }
        }
    }
    private static class FloatCtrl extends FloatControl {
        private long controlID;
        private boolean closed = false;
        private final static FloatControl.Type[] FLOAT_CONTROL_TYPES = {
            null,
            FloatControl.Type.BALANCE,
            FloatControl.Type.MASTER_GAIN,
            FloatControl.Type.PAN,
            FloatControl.Type.VOLUME
        };
        private FloatCtrl(long controlID, String name,
                          float min, float max, float precision, String units) {
            this(controlID, new FCT(name), min, max, precision, units);
        }
        private FloatCtrl(long controlID, int type,
                          float min, float max, float precision, String units) {
            this(controlID, FLOAT_CONTROL_TYPES[type], min, max, precision, units);
        }
        private FloatCtrl(long controlID, FloatControl.Type typ,
                         float min, float max, float precision, String units) {
            super(typ, min, max, precision, 1000, min, units);
            this.controlID = controlID;
        }
        public void setValue(float value) {
            if (!closed) {
                nControlSetFloatValue(controlID, value);
            }
        }
        public float getValue() {
            if (!closed) {
                return nControlGetFloatValue(controlID);
            }
            return getMinimum();
        }
        private static class FCT extends FloatControl.Type {
            private FCT(String name) {
                super(name);
            }
        }
    }
    private static class PortInfo extends Port.Info {
        private PortInfo(String name, boolean isSource) {
            super(Port.class, name, isSource);
        }
    }
    private static native long nOpen(int mixerIndex) throws LineUnavailableException;
    private static native void nClose(long id);
    private static native int nGetPortCount(long id);
    private static native int nGetPortType(long id, int portIndex);
    private static native String nGetPortName(long id, int portIndex);
    private static native void nGetControls(long id, int portIndex, Vector vector);
    private static native void nControlSetIntValue(long controlID, int value);
    private static native int nControlGetIntValue(long controlID);
    private static native void nControlSetFloatValue(long controlID, float value);
    private static native float nControlGetFloatValue(long controlID);
}
