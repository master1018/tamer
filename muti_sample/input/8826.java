class DirectAudioDevice extends AbstractMixer {
    private static final int CLIP_BUFFER_TIME = 1000; 
    private static final int DEFAULT_LINE_BUFFER_TIME = 500; 
    private int deviceCountOpened = 0;
    private int deviceCountStarted = 0;
    DirectAudioDevice(DirectAudioDeviceProvider.DirectAudioDeviceInfo portMixerInfo) {
        super(portMixerInfo,              
              null,                       
              null,                       
              null);                      
        if (Printer.trace) Printer.trace(">> DirectAudioDevice: constructor");
        DirectDLI srcLineInfo = createDataLineInfo(true);
        if (srcLineInfo != null) {
            sourceLineInfo = new Line.Info[2];
            sourceLineInfo[0] = srcLineInfo;
            sourceLineInfo[1] = new DirectDLI(Clip.class, srcLineInfo.getFormats(),
                                              srcLineInfo.getHardwareFormats(),
                                              32, 
                                              AudioSystem.NOT_SPECIFIED);
        } else {
            sourceLineInfo = new Line.Info[0];
        }
        DataLine.Info dstLineInfo = createDataLineInfo(false);
        if (dstLineInfo != null) {
            targetLineInfo = new Line.Info[1];
            targetLineInfo[0] = dstLineInfo;
        } else {
            targetLineInfo = new Line.Info[0];
        }
        if (Printer.trace) Printer.trace("<< DirectAudioDevice: constructor completed");
    }
    private DirectDLI createDataLineInfo(boolean isSource) {
        Vector formats = new Vector();
        AudioFormat[] hardwareFormatArray = null;
        AudioFormat[] formatArray = null;
        synchronized(formats) {
            nGetFormats(getMixerIndex(), getDeviceID(),
                        isSource ,
                        formats);
            if (formats.size() > 0) {
                int size = formats.size();
                int formatArraySize = size;
                hardwareFormatArray = new AudioFormat[size];
                for (int i = 0; i < size; i++) {
                    AudioFormat format = (AudioFormat)formats.elementAt(i);
                    hardwareFormatArray[i] = format;
                    int bits = format.getSampleSizeInBits();
                    boolean isSigned = format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED);
                    boolean isUnsigned = format.getEncoding().equals(AudioFormat.Encoding.PCM_UNSIGNED);
                    if ((isSigned || isUnsigned)) {
                        formatArraySize++;
                    }
                }
                formatArray = new AudioFormat[formatArraySize];
                int formatArrayIndex = 0;
                for (int i = 0; i < size; i++) {
                    AudioFormat format = hardwareFormatArray[i];
                    formatArray[formatArrayIndex++] = format;
                    int bits = format.getSampleSizeInBits();
                    boolean isSigned = format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED);
                    boolean isUnsigned = format.getEncoding().equals(AudioFormat.Encoding.PCM_UNSIGNED);
                    if (bits == 8) {
                        if (isSigned) {
                            formatArray[formatArrayIndex++] =
                                new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,
                                    format.getSampleRate(), bits, format.getChannels(),
                                    format.getFrameSize(), format.getSampleRate(),
                                    format.isBigEndian());
                        }
                        else if (isUnsigned) {
                            formatArray[formatArrayIndex++] =
                                new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                                    format.getSampleRate(), bits, format.getChannels(),
                                    format.getFrameSize(), format.getSampleRate(),
                                    format.isBigEndian());
                        }
                    } else if (bits > 8 && (isSigned || isUnsigned)) {
                        formatArray[formatArrayIndex++] =
                            new AudioFormat(format.getEncoding(),
                                              format.getSampleRate(), bits,
                                              format.getChannels(),
                                              format.getFrameSize(),
                                              format.getSampleRate(),
                                              !format.isBigEndian());
                    }
                }
            }
        }
        if (formatArray != null) {
            return new DirectDLI(isSource?SourceDataLine.class:TargetDataLine.class,
                                 formatArray, hardwareFormatArray,
                                 32, 
                                 AudioSystem.NOT_SPECIFIED);
        }
        return null;
    }
    public Line getLine(Line.Info info) throws LineUnavailableException {
        Line.Info fullInfo = getLineInfo(info);
        if (fullInfo == null) {
            throw new IllegalArgumentException("Line unsupported: " + info);
        }
        if (fullInfo instanceof DataLine.Info) {
            DataLine.Info dataLineInfo = (DataLine.Info)fullInfo;
            AudioFormat lineFormat;
            int lineBufferSize = AudioSystem.NOT_SPECIFIED;
            AudioFormat[] supportedFormats = null;
            if (info instanceof DataLine.Info) {
                supportedFormats = ((DataLine.Info)info).getFormats();
                lineBufferSize = ((DataLine.Info)info).getMaxBufferSize();
            }
            if ((supportedFormats == null) || (supportedFormats.length == 0)) {
                lineFormat = null;
            } else {
                lineFormat = supportedFormats[supportedFormats.length-1];
                if (!Toolkit.isFullySpecifiedPCMFormat(lineFormat)) {
                    lineFormat = null;
                }
            }
            if (dataLineInfo.getLineClass().isAssignableFrom(DirectSDL.class)) {
                return new DirectSDL(dataLineInfo, lineFormat, lineBufferSize, this);
            }
            if (dataLineInfo.getLineClass().isAssignableFrom(DirectClip.class)) {
                return new DirectClip(dataLineInfo, lineFormat, lineBufferSize, this);
            }
            if (dataLineInfo.getLineClass().isAssignableFrom(DirectTDL.class)) {
                return new DirectTDL(dataLineInfo, lineFormat, lineBufferSize, this);
            }
        }
        throw new IllegalArgumentException("Line unsupported: " + info);
    }
    public int getMaxLines(Line.Info info) {
        Line.Info fullInfo = getLineInfo(info);
        if (fullInfo == null) {
            return 0;
        }
        if (fullInfo instanceof DataLine.Info) {
            return getMaxSimulLines();
        }
        return 0;
    }
    protected void implOpen() throws LineUnavailableException {
        if (Printer.trace) Printer.trace("DirectAudioDevice: implOpen - void method");
    }
    protected void implClose() {
        if (Printer.trace) Printer.trace("DirectAudioDevice: implClose - void method");
    }
    protected void implStart() {
        if (Printer.trace) Printer.trace("DirectAudioDevice: implStart - void method");
    }
    protected void implStop() {
        if (Printer.trace) Printer.trace("DirectAudioDevice: implStop - void method");
    }
    int getMixerIndex() {
        return ((DirectAudioDeviceProvider.DirectAudioDeviceInfo) getMixerInfo()).getIndex();
    }
    int getDeviceID() {
        return ((DirectAudioDeviceProvider.DirectAudioDeviceInfo) getMixerInfo()).getDeviceID();
    }
    int getMaxSimulLines() {
        return ((DirectAudioDeviceProvider.DirectAudioDeviceInfo) getMixerInfo()).getMaxSimulLines();
    }
    private static void addFormat(Vector v, int bits, int frameSizeInBytes, int channels, float sampleRate,
                                  int encoding, boolean signed, boolean bigEndian) {
        AudioFormat.Encoding enc = null;
        switch (encoding) {
        case PCM:
            enc = signed?AudioFormat.Encoding.PCM_SIGNED:AudioFormat.Encoding.PCM_UNSIGNED;
            break;
        case ULAW:
            enc = AudioFormat.Encoding.ULAW;
            if (bits != 8) {
                if (Printer.err) Printer.err("DirectAudioDevice.addFormat called with ULAW, but bitsPerSample="+bits);
                bits = 8; frameSizeInBytes = channels;
            }
            break;
        case ALAW:
            enc = AudioFormat.Encoding.ALAW;
            if (bits != 8) {
                if (Printer.err) Printer.err("DirectAudioDevice.addFormat called with ALAW, but bitsPerSample="+bits);
                bits = 8; frameSizeInBytes = channels;
            }
            break;
        }
        if (enc==null) {
            if (Printer.err) Printer.err("DirectAudioDevice.addFormat called with unknown encoding: "+encoding);
            return;
        }
        if (frameSizeInBytes <= 0) {
            if (channels > 0) {
                frameSizeInBytes = ((bits + 7) / 8) * channels;
            } else {
                frameSizeInBytes = AudioSystem.NOT_SPECIFIED;
            }
        }
        v.add(new AudioFormat(enc, sampleRate, bits, channels, frameSizeInBytes, sampleRate, bigEndian));
    }
    protected static AudioFormat getSignOrEndianChangedFormat(AudioFormat format) {
        boolean isSigned = format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED);
        boolean isUnsigned = format.getEncoding().equals(AudioFormat.Encoding.PCM_UNSIGNED);
        if (format.getSampleSizeInBits() > 8 && isSigned) {
            return new AudioFormat(format.getEncoding(),
                                   format.getSampleRate(), format.getSampleSizeInBits(), format.getChannels(),
                                   format.getFrameSize(), format.getFrameRate(), !format.isBigEndian());
        }
        else if (format.getSampleSizeInBits() == 8 && (isSigned || isUnsigned)) {
            return new AudioFormat(isSigned?AudioFormat.Encoding.PCM_UNSIGNED:AudioFormat.Encoding.PCM_SIGNED,
                                   format.getSampleRate(), format.getSampleSizeInBits(), format.getChannels(),
                                   format.getFrameSize(), format.getFrameRate(), format.isBigEndian());
        }
        return null;
    }
    private static class DirectDLI extends DataLine.Info {
        AudioFormat[] hardwareFormats;
        private DirectDLI(Class clazz, AudioFormat[] formatArray,
                          AudioFormat[] hardwareFormatArray,
                          int minBuffer, int maxBuffer) {
            super(clazz, formatArray, minBuffer, maxBuffer);
            this.hardwareFormats = hardwareFormatArray;
        }
        public boolean isFormatSupportedInHardware(AudioFormat format) {
            if (format == null) return false;
            for (int i = 0; i < hardwareFormats.length; i++) {
                if (format.matches(hardwareFormats[i])) {
                    return true;
                }
            }
            return false;
        }
         private AudioFormat[] getHardwareFormats() {
             return hardwareFormats;
         }
    }
    private static class DirectDL extends AbstractDataLine implements EventDispatcher.LineMonitor {
        protected int mixerIndex;
        protected int deviceID;
        protected long id;
        protected int waitTime;
        protected volatile boolean flushing = false;
        protected boolean isSource;         
        protected volatile long bytePosition;
        protected volatile boolean doIO = false;     
        protected volatile boolean stoppedWritten = false; 
        protected volatile boolean drained = false; 
        protected boolean monitoring = false;
        protected int softwareConversionSize = 0;
        protected AudioFormat hardwareFormat;
        private Gain gainControl = new Gain();
        private Mute muteControl = new Mute();
        private Balance balanceControl = new Balance();
        private Pan panControl = new Pan();
        private float leftGain, rightGain;
        protected volatile boolean noService = false; 
        protected final Object lockNative = new Object();
        protected DirectDL(DataLine.Info info,
                           DirectAudioDevice mixer,
                           AudioFormat format,
                           int bufferSize,
                           int mixerIndex,
                           int deviceID,
                           boolean isSource) {
            super(info, mixer, null, format, bufferSize);
            if (Printer.trace) Printer.trace("DirectDL CONSTRUCTOR: info: " + info);
            this.mixerIndex = mixerIndex;
            this.deviceID = deviceID;
            this.waitTime = 10; 
            this.isSource = isSource;
        }
        void implOpen(AudioFormat format, int bufferSize) throws LineUnavailableException {
            if (Printer.trace) Printer.trace(">> DirectDL: implOpen("+format+", "+bufferSize+" bytes)");
            Toolkit.isFullySpecifiedAudioFormat(format);
            if (!isSource) {
                JSSecurityManager.checkRecordPermission();
            }
            int encoding = PCM;
            if (format.getEncoding().equals(AudioFormat.Encoding.ULAW)) {
                encoding = ULAW;
            }
            else if (format.getEncoding().equals(AudioFormat.Encoding.ALAW)) {
                encoding = ALAW;
            }
            if (bufferSize <= AudioSystem.NOT_SPECIFIED) {
                bufferSize = (int) Toolkit.millis2bytes(format, DEFAULT_LINE_BUFFER_TIME);
            }
            DirectDLI ddli = null;
            if (info instanceof DirectDLI) {
                ddli = (DirectDLI) info;
            }
            if (isSource) {
                if (!format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)
                    && !format.getEncoding().equals(AudioFormat.Encoding.PCM_UNSIGNED)) {
                    controls = new Control[0];
                }
                else if (format.getChannels() > 2
                         || format.getSampleSizeInBits() > 16) {
                    controls = new Control[0];
                } else {
                    if (format.getChannels() == 1) {
                        controls = new Control[2];
                    } else {
                        controls = new Control[4];
                        controls[2] = balanceControl;
                        controls[3] = panControl;
                    }
                    controls[0] = gainControl;
                    controls[1] = muteControl;
                }
            }
            if (Printer.debug) Printer.debug("DirectAudioDevice: got "+controls.length+" controls.");
            hardwareFormat = format;
            softwareConversionSize = 0;
            if (ddli != null && !ddli.isFormatSupportedInHardware(format)) {
                AudioFormat newFormat = getSignOrEndianChangedFormat(format);
                if (ddli.isFormatSupportedInHardware(newFormat)) {
                    hardwareFormat = newFormat;
                    softwareConversionSize = format.getFrameSize() / format.getChannels();
                    if (Printer.debug) {
                        Printer.debug("DirectAudioDevice: softwareConversionSize "
                                      +softwareConversionSize+":");
                        Printer.debug("  from "+format);
                        Printer.debug("  to   "+newFormat);
                    }
                }
            }
            bufferSize = ((int) bufferSize / format.getFrameSize()) * format.getFrameSize();
            id = nOpen(mixerIndex, deviceID, isSource,
                    encoding,
                    hardwareFormat.getSampleRate(),
                    hardwareFormat.getSampleSizeInBits(),
                    hardwareFormat.getFrameSize(),
                    hardwareFormat.getChannels(),
                    hardwareFormat.getEncoding().equals(
                        AudioFormat.Encoding.PCM_SIGNED),
                    hardwareFormat.isBigEndian(),
                    bufferSize);
            if (id == 0) {
                throw new LineUnavailableException(
                        "line with format "+format+" not supported.");
            }
            this.bufferSize = nGetBufferSize(id, isSource);
            if (this.bufferSize < 1) {
                this.bufferSize = bufferSize;
            }
            this.format = format;
            waitTime = (int) Toolkit.bytes2millis(format, this.bufferSize) / 4;
            if (waitTime < 10) {
                waitTime = 1;
            }
            else if (waitTime > 1000) {
                waitTime = 1000;
            }
            bytePosition = 0;
            stoppedWritten = false;
            doIO = false;
            calcVolume();
            if (Printer.trace) Printer.trace("<< DirectDL: implOpen() succeeded");
        }
        void implStart() {
            if (Printer.trace) Printer.trace(" >> DirectDL: implStart()");
            if (!isSource) {
                JSSecurityManager.checkRecordPermission();
            }
            synchronized (lockNative)
            {
                nStart(id, isSource);
            }
            monitoring = requiresServicing();
            if (monitoring) {
                getEventDispatcher().addLineMonitor(this);
            }
            doIO = true;
            if (isSource && stoppedWritten) {
                setStarted(true);
                setActive(true);
            }
            if (Printer.trace) Printer.trace("<< DirectDL: implStart() succeeded");
        }
        void implStop() {
            if (Printer.trace) Printer.trace(">> DirectDL: implStop()");
            if (!isSource) {
                JSSecurityManager.checkRecordPermission();
            }
            if (monitoring) {
                getEventDispatcher().removeLineMonitor(this);
                monitoring = false;
            }
            synchronized (lockNative) {
                nStop(id, isSource);
            }
            synchronized(lock) {
                doIO = false;
                lock.notifyAll();
            }
            setActive(false);
            setStarted(false);
            stoppedWritten = false;
            if (Printer.trace) Printer.trace(" << DirectDL: implStop() succeeded");
        }
        void implClose() {
            if (Printer.trace) Printer.trace(">> DirectDL: implClose()");
            if (!isSource) {
                JSSecurityManager.checkRecordPermission();
            }
            if (monitoring) {
                getEventDispatcher().removeLineMonitor(this);
                monitoring = false;
            }
            doIO = false;
            long oldID = id;
            id = 0;
            synchronized (lockNative) {
                nClose(oldID, isSource);
            }
            bytePosition = 0;
            softwareConversionSize = 0;
            if (Printer.trace) Printer.trace("<< DirectDL: implClose() succeeded");
        }
        public int available() {
            if (id == 0) {
                return 0;
            }
            int a;
            synchronized (lockNative) {
                a = nAvailable(id, isSource);
            }
            return a;
        }
        public void drain() {
            noService = true;
            int counter = 0;
            long startPos = getLongFramePosition();
            boolean posChanged = false;
            while (!drained) {
                synchronized (lockNative) {
                    if ((id == 0) || (!doIO) || !nIsStillDraining(id, isSource))
                        break;
                }
                if ((counter % 5) == 4) {
                    long thisFramePos = getLongFramePosition();
                    posChanged = posChanged | (thisFramePos != startPos);
                    if ((counter % 50) > 45) {
                        if (!posChanged) {
                            if (Printer.err) Printer.err("Native reports isDraining, but frame position does not increase!");
                            break;
                        }
                        posChanged = false;
                        startPos = thisFramePos;
                    }
                }
                counter++;
                synchronized(lock) {
                    try {
                        lock.wait(10);
                    } catch (InterruptedException ie) {}
                }
            }
            if (doIO && id != 0) {
                drained = true;
            }
            noService = false;
        }
        public void flush() {
            if (id != 0) {
                flushing = true;
                synchronized(lock) {
                    lock.notifyAll();
                }
                synchronized (lockNative) {
                    if (id != 0) {
                        nFlush(id, isSource);
                    }
                }
                drained = true;
            }
        }
        public long getLongFramePosition() {
            long pos;
            synchronized (lockNative) {
                pos = nGetBytePosition(id, isSource, bytePosition);
            }
            if (pos < 0) {
                if (Printer.debug) Printer.debug("DirectLine.getLongFramePosition: Native reported pos="
                                                 +pos+"! is changed to 0. byteposition="+bytePosition);
                pos = 0;
            }
            return (pos / getFormat().getFrameSize());
        }
        public int write(byte[] b, int off, int len) {
            flushing = false;
            if (len == 0) {
                return 0;
            }
            if (len < 0) {
                throw new IllegalArgumentException("illegal len: "+len);
            }
            if (len % getFormat().getFrameSize() != 0) {
                throw new IllegalArgumentException("illegal request to write "
                                                   +"non-integral number of frames ("
                                                   +len+" bytes, "
                                                   +"frameSize = "+getFormat().getFrameSize()+" bytes)");
            }
            if (off < 0) {
                throw new ArrayIndexOutOfBoundsException(off);
            }
            if (off + len > b.length) {
                throw new ArrayIndexOutOfBoundsException(b.length);
            }
            if (!isActive() && doIO) {
                setActive(true);
                setStarted(true);
            }
            int written = 0;
            while (!flushing) {
                int thisWritten;
                synchronized (lockNative) {
                    thisWritten = nWrite(id, b, off, len,
                            softwareConversionSize,
                            leftGain, rightGain);
                    if (thisWritten < 0) {
                        break;
                    }
                    bytePosition += thisWritten;
                    if (thisWritten > 0) {
                        drained = false;
                    }
                }
                len -= thisWritten;
                written += thisWritten;
                if (doIO && len > 0) {
                    off += thisWritten;
                    synchronized (lock) {
                        try {
                            lock.wait(waitTime);
                        } catch (InterruptedException ie) {}
                    }
                } else {
                    break;
                }
            }
            if (written > 0 && !doIO) {
                stoppedWritten = true;
            }
            return written;
        }
        protected boolean requiresServicing() {
            return nRequiresServicing(id, isSource);
        }
        public void checkLine() {
            synchronized (lockNative) {
                if (monitoring
                        && doIO
                        && id != 0
                        && !flushing
                        && !noService) {
                    nService(id, isSource);
                }
            }
        }
        private void calcVolume() {
            if (getFormat() == null) {
                return;
            }
            if (muteControl.getValue()) {
                leftGain = 0.0f;
                rightGain = 0.0f;
                return;
            }
            float gain = gainControl.getLinearGain();
            if (getFormat().getChannels() == 1) {
                leftGain = gain;
                rightGain = gain;
            } else {
                float bal = balanceControl.getValue();
                if (bal < 0.0f) {
                    leftGain = gain;
                    rightGain = gain * (bal + 1.0f);
                } else {
                    leftGain = gain * (1.0f - bal);
                    rightGain = gain;
                }
            }
        }
        protected class Gain extends FloatControl {
            private float linearGain = 1.0f;
            private Gain() {
                super(FloatControl.Type.MASTER_GAIN,
                      Toolkit.linearToDB(0.0f),
                      Toolkit.linearToDB(2.0f),
                      Math.abs(Toolkit.linearToDB(1.0f)-Toolkit.linearToDB(0.0f))/128.0f,
                      -1,
                      0.0f,
                      "dB", "Minimum", "", "Maximum");
            }
            public void setValue(float newValue) {
                float newLinearGain = Toolkit.dBToLinear(newValue);
                super.setValue(Toolkit.linearToDB(newLinearGain));
                linearGain = newLinearGain;
                calcVolume();
            }
            float getLinearGain() {
                return linearGain;
            }
        } 
        private class Mute extends BooleanControl {
            private Mute() {
                super(BooleanControl.Type.MUTE, false, "True", "False");
            }
            public void setValue(boolean newValue) {
                super.setValue(newValue);
                calcVolume();
            }
        }  
        private class Balance extends FloatControl {
            private Balance() {
                super(FloatControl.Type.BALANCE, -1.0f, 1.0f, (1.0f / 128.0f), -1, 0.0f,
                      "", "Left", "Center", "Right");
            }
            public void setValue(float newValue) {
                setValueImpl(newValue);
                panControl.setValueImpl(newValue);
                calcVolume();
            }
            void setValueImpl(float newValue) {
                super.setValue(newValue);
            }
        } 
        private class Pan extends FloatControl {
            private Pan() {
                super(FloatControl.Type.PAN, -1.0f, 1.0f, (1.0f / 128.0f), -1, 0.0f,
                      "", "Left", "Center", "Right");
            }
            public void setValue(float newValue) {
                setValueImpl(newValue);
                balanceControl.setValueImpl(newValue);
                calcVolume();
            }
            void setValueImpl(float newValue) {
                super.setValue(newValue);
            }
        } 
    } 
    private static class DirectSDL extends DirectDL implements SourceDataLine {
        private DirectSDL(DataLine.Info info,
                          AudioFormat format,
                          int bufferSize,
                          DirectAudioDevice mixer) {
            super(info, mixer, format, bufferSize, mixer.getMixerIndex(), mixer.getDeviceID(), true);
            if (Printer.trace) Printer.trace("DirectSDL CONSTRUCTOR: completed");
        }
    }
    private static class DirectTDL extends DirectDL implements TargetDataLine {
        private DirectTDL(DataLine.Info info,
                          AudioFormat format,
                          int bufferSize,
                          DirectAudioDevice mixer) {
            super(info, mixer, format, bufferSize, mixer.getMixerIndex(), mixer.getDeviceID(), false);
            if (Printer.trace) Printer.trace("DirectTDL CONSTRUCTOR: completed");
        }
        public int read(byte[] b, int off, int len) {
            flushing = false;
            if (len == 0) {
                return 0;
            }
            if (len < 0) {
                throw new IllegalArgumentException("illegal len: "+len);
            }
            if (len % getFormat().getFrameSize() != 0) {
                throw new IllegalArgumentException("illegal request to read "
                                                   +"non-integral number of frames ("
                                                   +len+" bytes, "
                                                   +"frameSize = "+getFormat().getFrameSize()+" bytes)");
            }
            if (off < 0) {
                throw new ArrayIndexOutOfBoundsException(off);
            }
            if (off + len > b.length) {
                throw new ArrayIndexOutOfBoundsException(b.length);
            }
            if (!isActive() && doIO) {
                setActive(true);
                setStarted(true);
            }
            int read = 0;
            while (doIO && !flushing) {
                int thisRead;
                synchronized (lockNative) {
                    thisRead = nRead(id, b, off, len, softwareConversionSize);
                    if (thisRead < 0) {
                        break;
                    }
                    bytePosition += thisRead;
                    if (thisRead > 0) {
                        drained = false;
                    }
                }
                len -= thisRead;
                read += thisRead;
                if (len > 0) {
                    off += thisRead;
                    synchronized(lock) {
                        try {
                            lock.wait(waitTime);
                        } catch (InterruptedException ie) {}
                    }
                } else {
                    break;
                }
            }
            if (flushing) {
                read = 0;
            }
            return read;
        }
    }
    private static class DirectClip extends DirectDL implements Clip,  Runnable, AutoClosingClip {
        private Thread thread;
        private byte[] audioData = null;
        private int frameSize;         
        private int m_lengthInFrames;
        private int loopCount;
        private int clipBytePosition;   
        private int newFramePosition;   
        private int loopStartFrame;
        private int loopEndFrame;      
        private boolean autoclosing = false;
        private DirectClip(DataLine.Info info,
                           AudioFormat format,
                           int bufferSize,
                           DirectAudioDevice mixer) {
            super(info, mixer, format, bufferSize, mixer.getMixerIndex(), mixer.getDeviceID(), true);
            if (Printer.trace) Printer.trace("DirectClip CONSTRUCTOR: completed");
        }
        public void open(AudioFormat format, byte[] data, int offset, int bufferSize)
            throws LineUnavailableException {
            Toolkit.isFullySpecifiedAudioFormat(format);
            byte[] newData = new byte[bufferSize];
            System.arraycopy(data, offset, newData, 0, bufferSize);
            open(format, data, bufferSize / format.getFrameSize());
        }
        private void open(AudioFormat format, byte[] data, int frameLength)
            throws LineUnavailableException {
            Toolkit.isFullySpecifiedAudioFormat(format);
            synchronized (mixer) {
                if (Printer.trace) Printer.trace("> DirectClip.open(format, data, frameLength)");
                if (Printer.debug) Printer.debug("   data="+((data==null)?"null":""+data.length+" bytes"));
                if (Printer.debug) Printer.debug("   frameLength="+frameLength);
                if (isOpen()) {
                    throw new IllegalStateException("Clip is already open with format " + getFormat() +
                                                    " and frame lengh of " + getFrameLength());
                } else {
                    this.audioData = data;
                    this.frameSize = format.getFrameSize();
                    this.m_lengthInFrames = frameLength;
                    bytePosition = 0;
                    clipBytePosition = 0;
                    newFramePosition = -1; 
                    loopStartFrame = 0;
                    loopEndFrame = frameLength - 1;
                    loopCount = 0; 
                    try {
                        open(format, (int) Toolkit.millis2bytes(format, CLIP_BUFFER_TIME)); 
                    } catch (LineUnavailableException lue) {
                        audioData = null;
                        throw lue;
                    } catch (IllegalArgumentException iae) {
                        audioData = null;
                        throw iae;
                    }
                    int priority = Thread.NORM_PRIORITY
                        + (Thread.MAX_PRIORITY - Thread.NORM_PRIORITY) / 3;
                    thread = JSSecurityManager.createThread(this,
                                                            "Direct Clip", 
                                                            true,     
                                                            priority, 
                                                            false);  
                    thread.start();
                }
            }
            if (isAutoClosing()) {
                getEventDispatcher().autoClosingClipOpened(this);
            }
            if (Printer.trace) Printer.trace("< DirectClip.open completed");
        }
        public void open(AudioInputStream stream) throws LineUnavailableException, IOException {
            Toolkit.isFullySpecifiedAudioFormat(format);
            synchronized (mixer) {
                if (Printer.trace) Printer.trace("> DirectClip.open(stream)");
                byte[] streamData = null;
                if (isOpen()) {
                    throw new IllegalStateException("Clip is already open with format " + getFormat() +
                                                    " and frame lengh of " + getFrameLength());
                }
                int lengthInFrames = (int)stream.getFrameLength();
                if (Printer.debug) Printer.debug("DirectClip: open(AIS): lengthInFrames: " + lengthInFrames);
                int bytesRead = 0;
                if (lengthInFrames != AudioSystem.NOT_SPECIFIED) {
                    int arraysize = lengthInFrames * stream.getFormat().getFrameSize();
                    streamData = new byte[arraysize];
                    int bytesRemaining = arraysize;
                    int thisRead = 0;
                    while (bytesRemaining > 0 && thisRead >= 0) {
                        thisRead = stream.read(streamData, bytesRead, bytesRemaining);
                        if (thisRead > 0) {
                            bytesRead += thisRead;
                            bytesRemaining -= thisRead;
                        }
                        else if (thisRead == 0) {
                            Thread.yield();
                        }
                    }
                } else {
                    int MAX_READ_LIMIT = 16384;
                    DirectBAOS dbaos  = new DirectBAOS();
                    byte tmp[] = new byte[MAX_READ_LIMIT];
                    int thisRead = 0;
                    while (thisRead >= 0) {
                        thisRead = stream.read(tmp, 0, tmp.length);
                        if (thisRead > 0) {
                            dbaos.write(tmp, 0, thisRead);
                            bytesRead += thisRead;
                        }
                        else if (thisRead == 0) {
                            Thread.yield();
                        }
                    } 
                    streamData = dbaos.getInternalBuffer();
                }
                lengthInFrames = bytesRead / stream.getFormat().getFrameSize();
                if (Printer.debug) Printer.debug("Read to end of stream. lengthInFrames: " + lengthInFrames);
                open(stream.getFormat(), streamData, lengthInFrames);
                if (Printer.trace) Printer.trace("< DirectClip.open(stream) succeeded");
            } 
        }
        public int getFrameLength() {
            return m_lengthInFrames;
        }
        public long getMicrosecondLength() {
            return Toolkit.frames2micros(getFormat(), getFrameLength());
        }
        public void setFramePosition(int frames) {
            if (Printer.trace) Printer.trace("> DirectClip: setFramePosition: " + frames);
            if (frames < 0) {
                frames = 0;
            }
            else if (frames >= getFrameLength()) {
                frames = getFrameLength();
            }
            if (doIO) {
                newFramePosition = frames;
            } else {
                clipBytePosition = frames * frameSize;
                newFramePosition = -1;
            }
            bytePosition = frames * frameSize;
            flush();
            synchronized (lockNative) {
                nSetBytePosition(id, isSource, frames * frameSize);
            }
            if (Printer.debug) Printer.debug("  DirectClip.setFramePosition: "
                                             +" doIO="+doIO
                                             +" newFramePosition="+newFramePosition
                                             +" clipBytePosition="+clipBytePosition
                                             +" bytePosition="+bytePosition
                                             +" getLongFramePosition()="+getLongFramePosition());
            if (Printer.trace) Printer.trace("< DirectClip: setFramePosition");
        }
        public long getLongFramePosition() {
            return super.getLongFramePosition();
        }
        public synchronized void setMicrosecondPosition(long microseconds) {
            if (Printer.trace) Printer.trace("> DirectClip: setMicrosecondPosition: " + microseconds);
            long frames = Toolkit.micros2frames(getFormat(), microseconds);
            setFramePosition((int) frames);
            if (Printer.trace) Printer.trace("< DirectClip: setMicrosecondPosition succeeded");
        }
        public void setLoopPoints(int start, int end) {
            if (Printer.trace) Printer.trace("> DirectClip: setLoopPoints: start: " + start + " end: " + end);
            if (start < 0 || start >= getFrameLength()) {
                throw new IllegalArgumentException("illegal value for start: "+start);
            }
            if (end >= getFrameLength()) {
                throw new IllegalArgumentException("illegal value for end: "+end);
            }
            if (end == -1) {
                end = getFrameLength() - 1;
                if (end < 0) {
                    end = 0;
                }
            }
            if (end < start) {
                throw new IllegalArgumentException("End position " + end + "  preceeds start position " + start);
            }
            loopStartFrame = start;
            loopEndFrame = end;
            if (Printer.trace) Printer.trace("  loopStart: " + loopStartFrame + " loopEnd: " + loopEndFrame);
            if (Printer.trace) Printer.trace("< DirectClip: setLoopPoints completed");
        }
        public void loop(int count) {
            loopCount = count;
            start();
        }
        void implOpen(AudioFormat format, int bufferSize) throws LineUnavailableException {
            if (audioData == null) {
                throw new IllegalArgumentException("illegal call to open() in interface Clip");
            }
            super.implOpen(format, bufferSize);
        }
        void implClose() {
            if (Printer.trace) Printer.trace(">> DirectClip: implClose()");
            Thread oldThread = thread;
            thread = null;
            doIO = false;
            if (oldThread != null) {
                synchronized(lock) {
                    lock.notifyAll();
                }
                try {
                    oldThread.join(2000);
                } catch (InterruptedException ie) {}
            }
            super.implClose();
            audioData = null;
            newFramePosition = -1;
            getEventDispatcher().autoClosingClipClosed(this);
            if (Printer.trace) Printer.trace("<< DirectClip: implClose() succeeded");
        }
        void implStart() {
            if (Printer.trace) Printer.trace("> DirectClip: implStart()");
            super.implStart();
            if (Printer.trace) Printer.trace("< DirectClip: implStart() succeeded");
        }
        void implStop() {
            if (Printer.trace) Printer.trace(">> DirectClip: implStop()");
            super.implStop();
            loopCount = 0;
            if (Printer.trace) Printer.trace("<< DirectClip: implStop() succeeded");
        }
        public void run() {
            if (Printer.trace) Printer.trace(">>> DirectClip: run() threadID="+Thread.currentThread().getId());
            while (thread != null) {
                synchronized(lock) {
                    if (!doIO) {
                        try {
                            lock.wait();
                        } catch(InterruptedException ie) {}
                    }
                }
                while (doIO) {
                    if (newFramePosition >= 0) {
                        clipBytePosition = newFramePosition * frameSize;
                        newFramePosition = -1;
                    }
                    int endFrame = getFrameLength() - 1;
                    if (loopCount > 0 || loopCount == LOOP_CONTINUOUSLY) {
                        endFrame = loopEndFrame;
                    }
                    long framePos = (clipBytePosition / frameSize);
                    int toWriteFrames = (int) (endFrame - framePos + 1);
                    int toWriteBytes = toWriteFrames * frameSize;
                    if (toWriteBytes > getBufferSize()) {
                        toWriteBytes = Toolkit.align(getBufferSize(), frameSize);
                    }
                    int written = write(audioData, (int) clipBytePosition, toWriteBytes); 
                    clipBytePosition += written;
                    if (doIO && newFramePosition < 0 && written >= 0) {
                        framePos = clipBytePosition / frameSize;
                        if (framePos > endFrame) {
                            if (loopCount > 0 || loopCount == LOOP_CONTINUOUSLY) {
                                if (loopCount != LOOP_CONTINUOUSLY) {
                                    loopCount--;
                                }
                                newFramePosition = loopStartFrame;
                            } else {
                                if (Printer.debug) Printer.debug("stop clip in run() loop:");
                                if (Printer.debug) Printer.debug("  doIO="+doIO+" written="+written+" clipBytePosition="+clipBytePosition);
                                if (Printer.debug) Printer.debug("  framePos="+framePos+" endFrame="+endFrame);
                                drain();
                                stop();
                            }
                        }
                    }
                }
            }
            if (Printer.trace) Printer.trace("<<< DirectClip: run() threadID="+Thread.currentThread().getId());
        }
        public boolean isAutoClosing() {
            return autoclosing;
        }
        public void setAutoClosing(boolean value) {
            if (value != autoclosing) {
                if (isOpen()) {
                    if (value) {
                        getEventDispatcher().autoClosingClipOpened(this);
                    } else {
                        getEventDispatcher().autoClosingClipClosed(this);
                    }
                }
                autoclosing = value;
            }
        }
        protected boolean requiresServicing() {
            return false;
        }
    } 
    private static class DirectBAOS extends ByteArrayOutputStream {
        public DirectBAOS() {
            super();
        }
        public byte[] getInternalBuffer() {
            return buf;
        }
    } 
    private static native void nGetFormats(int mixerIndex, int deviceID,
                                           boolean isSource, Vector formats);
    private static native long nOpen(int mixerIndex, int deviceID, boolean isSource,
                                     int encoding,
                                     float sampleRate,
                                     int sampleSizeInBits,
                                     int frameSize,
                                     int channels,
                                     boolean signed,
                                     boolean bigEndian,
                                     int bufferSize) throws LineUnavailableException;
    private static native void nStart(long id, boolean isSource);
    private static native void nStop(long id, boolean isSource);
    private static native void nClose(long id, boolean isSource);
    private static native int nWrite(long id, byte[] b, int off, int len, int conversionSize,
                                     float volLeft, float volRight);
    private static native int nRead(long id, byte[] b, int off, int len, int conversionSize);
    private static native int nGetBufferSize(long id, boolean isSource);
    private static native boolean nIsStillDraining(long id, boolean isSource);
    private static native void nFlush(long id, boolean isSource);
    private static native int nAvailable(long id, boolean isSource);
    private static native long nGetBytePosition(long id, boolean isSource, long javaPos);
    private static native void nSetBytePosition(long id, boolean isSource, long pos);
    private static native boolean nRequiresServicing(long id, boolean isSource);
    private static native void nService(long id, boolean isSource);
}
