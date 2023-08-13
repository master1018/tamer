public class MidiSystem {
    private MidiSystem() {
    }
    public static MidiDevice.Info[] getMidiDeviceInfo() {
        List allInfos = new ArrayList();
        List providers = getMidiDeviceProviders();
        for(int i = 0; i < providers.size(); i++) {
            MidiDeviceProvider provider = (MidiDeviceProvider) providers.get(i);
            MidiDevice.Info[] tmpinfo = provider.getDeviceInfo();
            for (int j = 0; j < tmpinfo.length; j++) {
                allInfos.add( tmpinfo[j] );
            }
        }
        MidiDevice.Info[] infosArray = (MidiDevice.Info[]) allInfos.toArray(new MidiDevice.Info[0]);
        return infosArray;
    }
    public static MidiDevice getMidiDevice(MidiDevice.Info info) throws MidiUnavailableException {
        List providers = getMidiDeviceProviders();
        for(int i = 0; i < providers.size(); i++) {
            MidiDeviceProvider provider = (MidiDeviceProvider) providers.get(i);
            if (provider.isDeviceSupported(info)) {
                MidiDevice device = provider.getDevice(info);
                return device;
            }
        }
        throw new IllegalArgumentException("Requested device not installed: " + info);
    }
    public static Receiver getReceiver() throws MidiUnavailableException {
        MidiDevice device = getDefaultDeviceWrapper(Receiver.class);
        Receiver receiver;
        if (device instanceof ReferenceCountingDevice) {
            receiver = ((ReferenceCountingDevice) device).getReceiverReferenceCounting();
        } else {
            receiver = device.getReceiver();
        }
        if (!(receiver instanceof MidiDeviceReceiver)) {
            receiver = new MidiDeviceReceiverEnvelope(device, receiver);
        }
        return receiver;
    }
    public static Transmitter getTransmitter() throws MidiUnavailableException {
        MidiDevice device = getDefaultDeviceWrapper(Transmitter.class);
        Transmitter transmitter;
        if (device instanceof ReferenceCountingDevice) {
            transmitter = ((ReferenceCountingDevice) device).getTransmitterReferenceCounting();
        } else {
            transmitter = device.getTransmitter();
        }
        if (!(transmitter instanceof MidiDeviceTransmitter)) {
            transmitter = new MidiDeviceTransmitterEnvelope(device, transmitter);
        }
        return transmitter;
    }
    public static Synthesizer getSynthesizer() throws MidiUnavailableException {
        return (Synthesizer) getDefaultDeviceWrapper(Synthesizer.class);
    }
    public static Sequencer getSequencer() throws MidiUnavailableException {
        return getSequencer(true);
    }
    public static Sequencer getSequencer(boolean connected)
        throws MidiUnavailableException {
        Sequencer seq = (Sequencer) getDefaultDeviceWrapper(Sequencer.class);
        if (connected) {
            Receiver rec = null;
            MidiUnavailableException mue = null;
            try {
                Synthesizer synth = getSynthesizer();
                if (synth instanceof ReferenceCountingDevice) {
                    rec = ((ReferenceCountingDevice) synth).getReceiverReferenceCounting();
                } else {
                    synth.open();
                    try {
                        rec = synth.getReceiver();
                    } finally {
                        if (rec == null) {
                            synth.close();
                        }
                    }
                }
            } catch (MidiUnavailableException e) {
                if (e instanceof MidiUnavailableException) {
                    mue = (MidiUnavailableException) e;
                }
            }
            if (rec == null) {
                try {
                    rec = MidiSystem.getReceiver();
                } catch (Exception e) {
                    if (e instanceof MidiUnavailableException) {
                        mue = (MidiUnavailableException) e;
                    }
                }
            }
            if (rec != null) {
                seq.getTransmitter().setReceiver(rec);
                if (seq instanceof AutoConnectSequencer) {
                    ((AutoConnectSequencer) seq).setAutoConnect(rec);
                }
            } else {
                if (mue != null) {
                    throw mue;
                }
                throw new MidiUnavailableException("no receiver available");
            }
        }
        return seq;
    }
    public static Soundbank getSoundbank(InputStream stream)
        throws InvalidMidiDataException, IOException {
        SoundbankReader sp = null;
        Soundbank s = null;
        List providers = getSoundbankReaders();
        for(int i = 0; i < providers.size(); i++) {
            sp = (SoundbankReader)providers.get(i);
            s = sp.getSoundbank(stream);
            if( s!= null) {
                return s;
            }
        }
        throw new InvalidMidiDataException("cannot get soundbank from stream");
    }
    public static Soundbank getSoundbank(URL url)
        throws InvalidMidiDataException, IOException {
        SoundbankReader sp = null;
        Soundbank s = null;
        List providers = getSoundbankReaders();
        for(int i = 0; i < providers.size(); i++) {
            sp = (SoundbankReader)providers.get(i);
            s = sp.getSoundbank(url);
            if( s!= null) {
                return s;
            }
        }
        throw new InvalidMidiDataException("cannot get soundbank from stream");
    }
    public static Soundbank getSoundbank(File file)
        throws InvalidMidiDataException, IOException {
        SoundbankReader sp = null;
        Soundbank s = null;
        List providers = getSoundbankReaders();
        for(int i = 0; i < providers.size(); i++) {
            sp = (SoundbankReader)providers.get(i);
            s = sp.getSoundbank(file);
            if( s!= null) {
                return s;
            }
        }
        throw new InvalidMidiDataException("cannot get soundbank from stream");
    }
    public static MidiFileFormat getMidiFileFormat(InputStream stream)
        throws InvalidMidiDataException, IOException {
        List providers = getMidiFileReaders();
        MidiFileFormat format = null;
        for(int i = 0; i < providers.size(); i++) {
            MidiFileReader reader = (MidiFileReader) providers.get(i);
            try {
                format = reader.getMidiFileFormat( stream ); 
                break;
            } catch (InvalidMidiDataException e) {
                continue;
            }
        }
        if( format==null ) {
            throw new InvalidMidiDataException("input stream is not a supported file type");
        } else {
            return format;
        }
    }
    public static MidiFileFormat getMidiFileFormat(URL url)
        throws InvalidMidiDataException, IOException {
        List providers = getMidiFileReaders();
        MidiFileFormat format = null;
        for(int i = 0; i < providers.size(); i++) {
            MidiFileReader reader = (MidiFileReader) providers.get(i);
            try {
                format = reader.getMidiFileFormat( url ); 
                break;
            } catch (InvalidMidiDataException e) {
                continue;
            }
        }
        if( format==null ) {
            throw new InvalidMidiDataException("url is not a supported file type");
        } else {
            return format;
        }
    }
    public static MidiFileFormat getMidiFileFormat(File file)
        throws InvalidMidiDataException, IOException {
        List providers = getMidiFileReaders();
        MidiFileFormat format = null;
        for(int i = 0; i < providers.size(); i++) {
            MidiFileReader reader = (MidiFileReader) providers.get(i);
            try {
                format = reader.getMidiFileFormat( file ); 
                break;
            } catch (InvalidMidiDataException e) {
                continue;
            }
        }
        if( format==null ) {
            throw new InvalidMidiDataException("file is not a supported file type");
        } else {
            return format;
        }
    }
    public static Sequence getSequence(InputStream stream)
        throws InvalidMidiDataException, IOException {
        List providers = getMidiFileReaders();
        Sequence sequence = null;
        for(int i = 0; i < providers.size(); i++) {
            MidiFileReader reader = (MidiFileReader) providers.get(i);
            try {
                sequence = reader.getSequence( stream ); 
                break;
            } catch (InvalidMidiDataException e) {
                continue;
            }
        }
        if( sequence==null ) {
            throw new InvalidMidiDataException("could not get sequence from input stream");
        } else {
            return sequence;
        }
    }
    public static Sequence getSequence(URL url)
        throws InvalidMidiDataException, IOException {
        List providers = getMidiFileReaders();
        Sequence sequence = null;
        for(int i = 0; i < providers.size(); i++) {
            MidiFileReader reader = (MidiFileReader) providers.get(i);
            try {
                sequence = reader.getSequence( url ); 
                break;
            } catch (InvalidMidiDataException e) {
                continue;
            }
        }
        if( sequence==null ) {
            throw new InvalidMidiDataException("could not get sequence from URL");
        } else {
            return sequence;
        }
    }
    public static Sequence getSequence(File file)
        throws InvalidMidiDataException, IOException {
        List providers = getMidiFileReaders();
        Sequence sequence = null;
        for(int i = 0; i < providers.size(); i++) {
            MidiFileReader reader = (MidiFileReader) providers.get(i);
            try {
                sequence = reader.getSequence( file ); 
                break;
            } catch (InvalidMidiDataException e) {
                continue;
            }
        }
        if( sequence==null ) {
            throw new InvalidMidiDataException("could not get sequence from file");
        } else {
            return sequence;
        }
    }
    public static int[] getMidiFileTypes() {
        List providers = getMidiFileWriters();
        Set allTypes = new HashSet();
        for (int i = 0; i < providers.size(); i++ ) {
            MidiFileWriter writer = (MidiFileWriter) providers.get(i);
            int[] types = writer.getMidiFileTypes();
            for (int j = 0; j < types.length; j++ ) {
                allTypes.add(new Integer(types[j]));
            }
        }
        int resultTypes[] = new int[allTypes.size()];
        int index = 0;
        Iterator iterator = allTypes.iterator();
        while (iterator.hasNext()) {
            Integer integer = (Integer) iterator.next();
            resultTypes[index++] = integer.intValue();
        }
        return resultTypes;
    }
    public static boolean isFileTypeSupported(int fileType) {
        List providers = getMidiFileWriters();
        for (int i = 0; i < providers.size(); i++ ) {
            MidiFileWriter writer = (MidiFileWriter) providers.get(i);
            if( writer.isFileTypeSupported(fileType)) {
                return true;
            }
        }
        return false;
    }
    public static int[] getMidiFileTypes(Sequence sequence) {
        List providers = getMidiFileWriters();
        Set allTypes = new HashSet();
        for (int i = 0; i < providers.size(); i++ ) {
            MidiFileWriter writer = (MidiFileWriter) providers.get(i);
            int[] types = writer.getMidiFileTypes(sequence);
            for (int j = 0; j < types.length; j++ ) {
                allTypes.add(new Integer(types[j]));
            }
        }
        int resultTypes[] = new int[allTypes.size()];
        int index = 0;
        Iterator iterator = allTypes.iterator();
        while (iterator.hasNext()) {
            Integer integer = (Integer) iterator.next();
            resultTypes[index++] = integer.intValue();
        }
        return resultTypes;
    }
    public static boolean isFileTypeSupported(int fileType, Sequence sequence) {
        List providers = getMidiFileWriters();
        for (int i = 0; i < providers.size(); i++ ) {
            MidiFileWriter writer = (MidiFileWriter) providers.get(i);
            if( writer.isFileTypeSupported(fileType,sequence)) {
                return true;
            }
        }
        return false;
    }
    public static int write(Sequence in, int fileType, OutputStream out) throws IOException {
        List providers = getMidiFileWriters();
        int bytesWritten = -2;
        for (int i = 0; i < providers.size(); i++ ) {
            MidiFileWriter writer = (MidiFileWriter) providers.get(i);
            if( writer.isFileTypeSupported( fileType, in ) ) {
                bytesWritten = writer.write(in, fileType, out);
                break;
            }
        }
        if (bytesWritten == -2) {
            throw new IllegalArgumentException("MIDI file type is not supported");
        }
        return bytesWritten;
    }
    public static int write(Sequence in, int type, File out) throws IOException {
        List providers = getMidiFileWriters();
        int bytesWritten = -2;
        for (int i = 0; i < providers.size(); i++ ) {
            MidiFileWriter writer = (MidiFileWriter) providers.get(i);
            if( writer.isFileTypeSupported( type, in ) ) {
                bytesWritten = writer.write(in, type, out);
                break;
            }
        }
        if (bytesWritten == -2) {
            throw new IllegalArgumentException("MIDI file type is not supported");
        }
        return bytesWritten;
    }
    private static List getMidiDeviceProviders() {
        return getProviders(MidiDeviceProvider.class);
    }
    private static List getSoundbankReaders() {
        return getProviders(SoundbankReader.class);
    }
    private static List getMidiFileWriters() {
        return getProviders(MidiFileWriter.class);
    }
    private static List getMidiFileReaders() {
        return getProviders(MidiFileReader.class);
    }
    private static MidiDevice getDefaultDeviceWrapper(Class deviceClass)
        throws MidiUnavailableException{
        try {
            return getDefaultDevice(deviceClass);
        } catch (IllegalArgumentException iae) {
            MidiUnavailableException mae = new MidiUnavailableException();
            mae.initCause(iae);
            throw mae;
        }
    }
    private static MidiDevice getDefaultDevice(Class deviceClass) {
        List providers = getMidiDeviceProviders();
        String providerClassName = JDK13Services.getDefaultProviderClassName(deviceClass);
        String instanceName = JDK13Services.getDefaultInstanceName(deviceClass);
        MidiDevice device;
        if (providerClassName != null) {
            MidiDeviceProvider defaultProvider = getNamedProvider(providerClassName, providers);
            if (defaultProvider != null) {
                if (instanceName != null) {
                    device = getNamedDevice(instanceName, defaultProvider, deviceClass);
                    if (device != null) {
                        return device;
                    }
                }
                device = getFirstDevice(defaultProvider, deviceClass);
                if (device != null) {
                    return device;
                }
            }
        }
        if (instanceName != null) {
            device = getNamedDevice(instanceName, providers, deviceClass);
            if (device != null) {
                return device;
            }
        }
        device = getFirstDevice(providers, deviceClass);
        if (device != null) {
            return device;
        }
        throw new IllegalArgumentException("Requested device not installed");
    }
    private static MidiDeviceProvider getNamedProvider(String providerClassName, List providers) {
        for(int i = 0; i < providers.size(); i++) {
            MidiDeviceProvider provider = (MidiDeviceProvider) providers.get(i);
            if (provider.getClass().getName().equals(providerClassName)) {
                return provider;
            }
        }
        return null;
    }
    private static MidiDevice getNamedDevice(String deviceName,
                                             MidiDeviceProvider provider,
                                             Class deviceClass) {
        MidiDevice device;
        device = getNamedDevice(deviceName, provider, deviceClass,
                                 false, false);
        if (device != null) {
            return device;
        }
        if (deviceClass == Receiver.class) {
            device = getNamedDevice(deviceName, provider, deviceClass,
                                     true, false);
            if (device != null) {
                return device;
            }
        }
        return null;
    }
    private static MidiDevice getNamedDevice(String deviceName,
                                             MidiDeviceProvider provider,
                                             Class deviceClass,
                                             boolean allowSynthesizer,
                                             boolean allowSequencer) {
        MidiDevice.Info[] infos = provider.getDeviceInfo();
        for (int i = 0; i < infos.length; i++) {
            if (infos[i].getName().equals(deviceName)) {
                MidiDevice device = provider.getDevice(infos[i]);
                if (isAppropriateDevice(device, deviceClass,
                                        allowSynthesizer, allowSequencer)) {
                    return device;
                }
            }
        }
        return null;
    }
    private static MidiDevice getNamedDevice(String deviceName,
                                             List providers,
                                             Class deviceClass) {
        MidiDevice device;
        device = getNamedDevice(deviceName, providers, deviceClass,
                                 false, false);
        if (device != null) {
            return device;
        }
        if (deviceClass == Receiver.class) {
            device = getNamedDevice(deviceName, providers, deviceClass,
                                     true, false);
            if (device != null) {
                return device;
            }
        }
        return null;
    }
    private static MidiDevice getNamedDevice(String deviceName,
                                             List providers,
                                             Class deviceClass,
                                             boolean allowSynthesizer,
                                             boolean allowSequencer) {
        for(int i = 0; i < providers.size(); i++) {
            MidiDeviceProvider provider = (MidiDeviceProvider) providers.get(i);
            MidiDevice device = getNamedDevice(deviceName, provider,
                                               deviceClass,
                                               allowSynthesizer,
                                               allowSequencer);
            if (device != null) {
                return device;
            }
        }
        return null;
    }
    private static MidiDevice getFirstDevice(MidiDeviceProvider provider,
                                             Class deviceClass) {
        MidiDevice device;
        device = getFirstDevice(provider, deviceClass,
                                false, false);
        if (device != null) {
            return device;
        }
        if (deviceClass == Receiver.class) {
            device = getFirstDevice(provider, deviceClass,
                                    true, false);
            if (device != null) {
                return device;
            }
        }
        return null;
    }
    private static MidiDevice getFirstDevice(MidiDeviceProvider provider,
                                             Class deviceClass,
                                             boolean allowSynthesizer,
                                             boolean allowSequencer) {
        MidiDevice.Info[] infos = provider.getDeviceInfo();
        for (int j = 0; j < infos.length; j++) {
            MidiDevice device = provider.getDevice(infos[j]);
            if (isAppropriateDevice(device, deviceClass,
                                    allowSynthesizer, allowSequencer)) {
                return device;
            }
        }
        return null;
    }
    private static MidiDevice getFirstDevice(List providers,
                                             Class deviceClass) {
        MidiDevice device;
        device = getFirstDevice(providers, deviceClass,
                                false, false);
        if (device != null) {
            return device;
        }
        if (deviceClass == Receiver.class) {
            device = getFirstDevice(providers, deviceClass,
                                    true, false);
            if (device != null) {
                return device;
            }
        }
        return null;
    }
    private static MidiDevice getFirstDevice(List providers,
                                             Class deviceClass,
                                             boolean allowSynthesizer,
                                             boolean allowSequencer) {
        for(int i = 0; i < providers.size(); i++) {
            MidiDeviceProvider provider = (MidiDeviceProvider) providers.get(i);
            MidiDevice device = getFirstDevice(provider, deviceClass,
                                               allowSynthesizer,
                                               allowSequencer);
            if (device != null) {
                return device;
            }
        }
        return null;
    }
    private static boolean isAppropriateDevice(MidiDevice device,
                                               Class deviceClass,
                                               boolean allowSynthesizer,
                                               boolean allowSequencer) {
        if (deviceClass.isInstance(device)) {
            return true;
        } else {
            if ( (! (device instanceof Sequencer) &&
                  ! (device instanceof Synthesizer) ) ||
                 ((device instanceof Sequencer) && allowSequencer) ||
                 ((device instanceof Synthesizer) && allowSynthesizer)) {
                if ((deviceClass == Receiver.class &&
                     device.getMaxReceivers() != 0) ||
                    (deviceClass == Transmitter.class &&
                     device.getMaxTransmitters() != 0)) {
                    return true;
                }
            }
        }
        return false;
    }
    private static List getProviders(Class providerClass) {
        return JDK13Services.getProviders(providerClass);
    }
}
