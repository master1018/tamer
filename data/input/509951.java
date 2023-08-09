public class MidiSystem {
    private final static String midiDeviceProviderPath = 
        "META-INF/services/javax.sound.midi.spi.MidiDeviceProvider";
    private final static String midiFileReaderPath =
        "META-INF/services/javax.sound.midi.spi.MidiFileReader";
    private final static String midiFileWriterPath =
        "META-INF/services/javax.sound.midi.spi.MidiFileWriter";
    private final static String soundbankReaderPath =
        "META-INF/services/javax.sound.midi.spi.SoundbankReader";
    private final static String receiverName = "javax.sound.midi.Receiver";
    private final static String sequencerName = "javax.sound.midi.Sequencer";
    private final static String synthesizerName = "javax.sound.midi.Synthesizer";
    private final static String transmitterName = "javax.sound.midi.Transmitter";
    public static MidiDevice getMidiDevice(MidiDevice.Info info)
            throws MidiUnavailableException {
        List<?> deviceProviders = ProviderService.getProviders(midiDeviceProviderPath);
        for (int i = 0; i < deviceProviders.size(); i++) {
            MidiDevice.Info[] deviceInfo = ((MidiDeviceProvider) deviceProviders.get(i)).getDeviceInfo();
            for (Info element : deviceInfo) {
                if (element.equals(info)) {
                    return ((MidiDeviceProvider) deviceProviders.get(i)).getDevice(info);
                }
            }
        }
        throw new IllegalArgumentException("Requested device not installed: " + info.getName());
    }
    public static MidiDevice.Info[] getMidiDeviceInfo() {
        List<?> deviceProviders = ProviderService.getProviders(midiDeviceProviderPath);
        List<MidiDevice.Info> infos = new ArrayList<MidiDevice.Info>();
        for (int i = 0; i < deviceProviders.size(); i++) {
            MidiDevice.Info[] deviceInfo = ((MidiDeviceProvider) deviceProviders.get(i)).getDeviceInfo();
            for (Info element : deviceInfo) {
                infos.add(element);
            }
        }
        MidiDevice.Info[] temp = new MidiDevice.Info[infos.size()];
        return infos.toArray(temp);
    }
    public static MidiFileFormat getMidiFileFormat(File file) throws InvalidMidiDataException,
            IOException {
        List<?> fileReaderProviders = ProviderService.getProviders(midiFileReaderPath);
        if (fileReaderProviders.size() == 0) {
            throw new Error("There is no MidiFileReaderProviders on your system!!!");
        }
        return ((MidiFileReader) fileReaderProviders.get(0)).getMidiFileFormat(file);
    }
    public static MidiFileFormat getMidiFileFormat(InputStream stream) throws InvalidMidiDataException, 
            IOException {
        List<?> fileReaderProviders = ProviderService.getProviders(midiFileReaderPath);
        if (fileReaderProviders.size() == 0) {
            throw new Error("There is no MidiFileReaderProviders on your system!!!");
        }
        return ((MidiFileReader) fileReaderProviders.get(0)).getMidiFileFormat(stream);
    }
    public static MidiFileFormat getMidiFileFormat(URL url) throws InvalidMidiDataException,
            IOException {
        List<?> fileReaderProviders = ProviderService.getProviders(midiFileReaderPath);
        if (fileReaderProviders.size() == 0) {
            throw new Error("There is no MidiFileReaderProviders on your system!!!");
        }
        return ((MidiFileReader) fileReaderProviders.get(0)).getMidiFileFormat(url);
    }
    public static int[] getMidiFileTypes() {
        List<?> fileWriterProviders = ProviderService.getProviders(midiFileWriterPath);
        if (fileWriterProviders.size() == 0) {
            throw new Error("There is no MidiFileWriterProviders on your system!!!");
        }
        return ((MidiFileWriter) fileWriterProviders.get(0)).getMidiFileTypes();
    }
    public static int[] getMidiFileTypes(Sequence sequence) {
        List<?> fileWriterProviders = ProviderService.getProviders(midiFileWriterPath);
        if (fileWriterProviders.size() == 0) {
            throw new Error("There is no MidiFileWriterProviders on your system!!!");
        }
        return ((MidiFileWriter) fileWriterProviders.get(0)).getMidiFileTypes(sequence);
    }
    public static Receiver getReceiver() throws MidiUnavailableException {
        List<String> defaultDevice = ProviderService.getDefaultDeviceDescription(receiverName);
        List<?> deviceProviders = ProviderService.getProviders(midiDeviceProviderPath);
        String provName;
        int deviceNum = -1;
        if (defaultDevice.size() != 0) {
            for (int i = 0; i < deviceProviders.size(); i++) {
                provName = deviceProviders.get(i).toString();
                if (provName.substring(0, provName.indexOf("@")).equals(defaultDevice.get(0))) {
                    deviceNum = i;
                    break;
                }
            }
            if (deviceNum != -1) {
                MidiDevice.Info[] deviceInfo = ((MidiDeviceProvider) deviceProviders.get(deviceNum)).getDeviceInfo();
                for (Info element : deviceInfo) {
                    if (element.getName().equals(defaultDevice.get(1))) {
                        try {
                            return ((MidiDeviceProvider) deviceProviders.get(deviceNum)).getDevice(element).getReceiver();
                        } catch (MidiUnavailableException e) {}
                    }
                }
            for (Info element : deviceInfo) {
                    try {
                        return ((MidiDeviceProvider) deviceProviders.get(deviceNum)).getDevice(element).getReceiver();
                    } catch (MidiUnavailableException e) {}
                }
            }
            for (int i = 0; i < deviceProviders.size(); i++) {
                MidiDevice.Info[] deviceInfo = ((MidiDeviceProvider) deviceProviders.get(i)).getDeviceInfo();
                for (Info element : deviceInfo) {
                    if (element.getName().equals(defaultDevice.get(1))) {
                        try {
                            return ((MidiDeviceProvider) deviceProviders.get(i)).getDevice(element).getReceiver();
                        } catch (MidiUnavailableException e) {}
                    }
                }
            }
        }
        for (int i = 0; i < deviceProviders.size(); i++) {
            MidiDevice.Info[] deviceInfo = ((MidiDeviceProvider) deviceProviders.get(i)).getDeviceInfo();
            for (Info element : deviceInfo) {
                try {
                    return ((MidiDeviceProvider) deviceProviders.get(i)).getDevice(element).getReceiver();
                } catch (MidiUnavailableException e) {}
            }
        }
        throw new MidiUnavailableException("There are no Recivers installed on your system!");
    }
    public static Sequence getSequence(File file) throws InvalidMidiDataException, IOException {
        List<?> fileReaderProviders = ProviderService.getProviders(midiFileReaderPath);
        try {
            ((List)fileReaderProviders).add((Object)Class.forName("com.android.internal.sound.midi.AndroidMidiFileReader").newInstance());
        } catch (Exception ex) {
        }
        if (fileReaderProviders.size() == 0) {
            throw new Error("There is no MidiFileReaderProviders on your system!!!");
        }
        return ((MidiFileReader) fileReaderProviders.get(0)).getSequence(file);
    }
    public static Sequence getSequence(InputStream stream) throws InvalidMidiDataException,
            IOException {
        List<?> fileReaderProviders = ProviderService.getProviders(midiFileReaderPath);
        try {
            ((List)fileReaderProviders).add(Class.forName("com.android.internal.sound.midi.AndroidMidiFileReader").newInstance());
        } catch (Exception ex) {
        }
        if (fileReaderProviders.size() == 0) {
            throw new Error("There is no MidiFileReaderProviders on your system!!!");
        }
        return ((MidiFileReader) fileReaderProviders.get(0)).getSequence(stream);
    }
    public static Sequence getSequence(URL url) throws InvalidMidiDataException, IOException {
        List<?> fileReaderProviders = ProviderService.getProviders(midiFileReaderPath);
        try {
            ((List)fileReaderProviders).add(Class.forName("com.android.internal.sound.midi.AndroidMidiFileReader").newInstance());
        } catch (Exception ex) {
        }
        if (fileReaderProviders.size() == 0) {
            throw new Error("There is no MidiFileReaderProviders on your system!!!");
        }
        return ((MidiFileReader) fileReaderProviders.get(0)).getSequence(url);
    }
    public static Sequencer getSequencer() throws MidiUnavailableException {
        return getSequencer(true);
    }
    public static Sequencer getSequencer(boolean connected) throws MidiUnavailableException {
        List<String> defaultDevice = ProviderService.getDefaultDeviceDescription(sequencerName);
        List<?>  deviceProviders = ProviderService.getProviders(midiDeviceProviderPath);
        Sequencer sequencer;
        Transmitter seqTrans;
        Synthesizer synth;
        Receiver recv;
        String provName;
        int deviceNum = -1;
        if (defaultDevice.size() != 0) {
            for (int i = 0; i < deviceProviders.size(); i++) {
                provName = deviceProviders.get(i).toString();
                if (provName.substring(0, provName.indexOf("@")).equals(defaultDevice.get(0))) {
                    deviceNum = i;
                    break;
                }
            }
            if (deviceNum != -1) {
                MidiDevice.Info[] deviceInfo = ((MidiDeviceProvider) deviceProviders.get(deviceNum)).getDeviceInfo();
                for (Info element : deviceInfo) {
                    if (element.getName().equals(defaultDevice.get(1))) {
                        if (((MidiDeviceProvider) deviceProviders.get(deviceNum)).getDevice(element) instanceof Sequencer) {
                            if (connected) {
                                sequencer = (Sequencer) ((MidiDeviceProvider) deviceProviders.get(deviceNum)).getDevice(element);
                                seqTrans = sequencer.getTransmitter();
                                try {
                                    synth = MidiSystem.getSynthesizer();
                                    recv = synth.getReceiver();                                    
                                } catch (MidiUnavailableException e) {
                                    recv = MidiSystem.getReceiver();
                                }
                                seqTrans.setReceiver(recv);
                                return sequencer;
                            }
                            return (Sequencer) ((MidiDeviceProvider) deviceProviders.get(deviceNum)).getDevice(element);
                        }
                    }
                }
                for (Info element : deviceInfo) {
                    if (((MidiDeviceProvider) deviceProviders.get(deviceNum)).getDevice(element) instanceof Sequencer) {
                        if (connected) {
                            sequencer = (Sequencer) ((MidiDeviceProvider) deviceProviders.get(deviceNum)).getDevice(element);
                            seqTrans = sequencer.getTransmitter();
                            try {
                                synth = MidiSystem.getSynthesizer();
                                recv = synth.getReceiver();                                    
                            } catch (MidiUnavailableException e) {
                                recv = MidiSystem.getReceiver();
                            }
                            seqTrans.setReceiver(recv);
                            return sequencer;
                        }
                        return (Sequencer) ((MidiDeviceProvider) deviceProviders.get(deviceNum)).getDevice(element);
                    }
                }
            }
            for (int i = 0; i < deviceProviders.size(); i++) {
                MidiDevice.Info[] deviceInfo = ((MidiDeviceProvider) deviceProviders.get(i)).getDeviceInfo();
                for (Info element : deviceInfo) {
                    if (element.getName().equals(defaultDevice.get(1))) {
                        if (((MidiDeviceProvider) deviceProviders.get(i)).getDevice(element) instanceof Sequencer) {
                            if (connected) {
                                sequencer = (Sequencer) ((MidiDeviceProvider) deviceProviders.get(i)).getDevice(element);
                                seqTrans = sequencer.getTransmitter();
                                try {
                                    synth = MidiSystem.getSynthesizer();
                                    recv = synth.getReceiver();                                    
                                } catch (MidiUnavailableException e) {
                                    recv = MidiSystem.getReceiver();
                                }
                                seqTrans.setReceiver(recv);
                                return sequencer;
                            }
                            return (Sequencer) ((MidiDeviceProvider) deviceProviders.get(i)).getDevice(element);
                        }
                    }
                }
            }
        }
        for (int i = 0; i < deviceProviders.size(); i++) {
            MidiDevice.Info[] deviceInfo = ((MidiDeviceProvider) deviceProviders.get(i)).getDeviceInfo();
            for (Info element : deviceInfo) {
                if (((MidiDeviceProvider) deviceProviders.get(i)).getDevice(element) instanceof Sequencer) {
                    if (connected) {
                        sequencer = (Sequencer) ((MidiDeviceProvider) deviceProviders.get(i)).getDevice(element);
                        seqTrans = sequencer.getTransmitter();
                        try {
                            synth = MidiSystem.getSynthesizer();
                            recv = synth.getReceiver();                                    
                        } catch (MidiUnavailableException e) {
                            recv = MidiSystem.getReceiver();
                        }
                        seqTrans.setReceiver(recv);
                        return sequencer;
                    }
                    return (Sequencer) ((MidiDeviceProvider) deviceProviders.get(i)).getDevice(element);
                }
            }
        }
        try {
            return (Sequencer)(Class.forName("com.android.internal.sound.midi.AndroidSequencer").newInstance());
        } catch (Exception ex) {
        }
        throw new MidiUnavailableException("There are no Synthesizers installed on your system!");
    }
    public static Soundbank getSoundbank(File file) throws InvalidMidiDataException,
            IOException {
        List<?> soundbankReaderProviders = ProviderService.getProviders(soundbankReaderPath);
        if (soundbankReaderProviders.size() == 0) {
            throw new Error("There is no SoundbankReaderProviders on your system!!!");
        }
        return ((SoundbankReader) soundbankReaderProviders.get(0)).getSoundbank(file);
    }
    public static Soundbank getSoundbank(InputStream stream) throws InvalidMidiDataException, IOException {
        List<?> soundbankReaderProviders = ProviderService.getProviders(soundbankReaderPath);
        if (soundbankReaderProviders.size() == 0) {
            throw new Error("There is no SoundbankReaderProviders on your system!!!");
        }
        return ((SoundbankReader) soundbankReaderProviders.get(0)).getSoundbank(stream);
    }
    public static Soundbank getSoundbank(URL url) throws InvalidMidiDataException, IOException {
        List<?> soundbankReaderProviders = ProviderService.getProviders(soundbankReaderPath);
        if (soundbankReaderProviders.size() == 0) {
            throw new Error("There is no SoundbankReaderProviders on your system!!!");
        }
        return ((SoundbankReader) soundbankReaderProviders.get(0)).getSoundbank(url);
    }
    public static Synthesizer getSynthesizer() throws MidiUnavailableException {
        List<String> defaultDevice = ProviderService.getDefaultDeviceDescription(synthesizerName);
        List<?> deviceProviders = ProviderService.getProviders(midiDeviceProviderPath);
        String provName;
        int deviceNum = -1;
        if (defaultDevice.size() != 0) {
            for (int i = 0; i < deviceProviders.size(); i++) {
                provName = deviceProviders.get(i).toString();
                if (provName.substring(0, provName.indexOf("@")).equals(defaultDevice.get(0))) {
                    deviceNum = i;
                    break;
                }
            }
            if (deviceNum != -1) {
                MidiDevice.Info[] deviceInfo = ((MidiDeviceProvider) deviceProviders.get(deviceNum)).getDeviceInfo();
                for (Info element : deviceInfo) {
                    if (element.getName().equals(defaultDevice.get(1))) {
                        if (((MidiDeviceProvider) deviceProviders.get(deviceNum)).getDevice(element) instanceof Synthesizer) {
                            return (Synthesizer) ((MidiDeviceProvider) deviceProviders.get(deviceNum)).getDevice(element);
                        }
                    }
                }
                for (Info element : deviceInfo) {
                    if (((MidiDeviceProvider) deviceProviders.get(deviceNum)).getDevice(element) instanceof Synthesizer) {
                        return (Synthesizer) ((MidiDeviceProvider) deviceProviders.get(deviceNum)).getDevice(element);
                    }
                }
            }
            for (int i = 0; i < deviceProviders.size(); i++) {
                MidiDevice.Info[] deviceInfo = ((MidiDeviceProvider) deviceProviders.get(i)).getDeviceInfo();
                for (Info element : deviceInfo) {
                    if (element.getName().equals(defaultDevice.get(1))) {
                        if (((MidiDeviceProvider) deviceProviders.get(i)).getDevice(element) instanceof Synthesizer) {
                            return (Synthesizer) ((MidiDeviceProvider) deviceProviders.get(i)).getDevice(element);
                        }
                    }
                }
            }
        }
        for (int i = 0; i < deviceProviders.size(); i++) {
            MidiDevice.Info[] deviceInfo = ((MidiDeviceProvider) deviceProviders.get(i)).getDeviceInfo();
            for (Info element : deviceInfo) {
                if (((MidiDeviceProvider) deviceProviders.get(i)).getDevice(element) instanceof Synthesizer) {
                    return (Synthesizer) ((MidiDeviceProvider) deviceProviders.get(i)).getDevice(element);
                }
            }
        }
        throw new MidiUnavailableException("There are no Synthesizers installed on your system!");
    }
    public static Transmitter getTransmitter() throws MidiUnavailableException {
        List<String> defaultDevice = ProviderService.getDefaultDeviceDescription(transmitterName);
        List<?> deviceProviders = ProviderService.getProviders(midiDeviceProviderPath);
        String provName;
        int deviceNum = -1;
        if (defaultDevice.size() != 0) {
            for (int i = 0; i < deviceProviders.size(); i++) {
                provName = deviceProviders.get(i).toString();
                if (provName.substring(0, provName.indexOf("@")).equals(defaultDevice.get(0))) {
                    deviceNum = i;
                    break;
                }
            }
            if (deviceNum != -1) {
                MidiDevice.Info[] deviceInfo = ((MidiDeviceProvider) deviceProviders.get(deviceNum)).getDeviceInfo();
                for (Info element : deviceInfo) {
                    if (element.getName().equals(defaultDevice.get(1))) {
                        try {
                            return ((MidiDeviceProvider) deviceProviders.get(deviceNum)).getDevice(element).getTransmitter();
                        } catch (MidiUnavailableException e) {}
                    }
                }
                for (Info element : deviceInfo) {
                    try {
                        return ((MidiDeviceProvider) deviceProviders.get(deviceNum)).getDevice(element).getTransmitter();
                    } catch (MidiUnavailableException e) {}
                }
            }
            for (int i = 0; i < deviceProviders.size(); i++) {
                MidiDevice.Info[] deviceInfo = ((MidiDeviceProvider) deviceProviders.get(i)).getDeviceInfo();
                for (Info element : deviceInfo) {
                    if (element.getName().equals(defaultDevice.get(1))) {
                        try {
                            return ((MidiDeviceProvider) deviceProviders.get(i)).getDevice(element).getTransmitter();
                        } catch (MidiUnavailableException e) {}
                    }
                }
            }
        }
        for (int i = 0; i < deviceProviders.size(); i++) {
            MidiDevice.Info[] deviceInfo = ((MidiDeviceProvider) deviceProviders.get(i)).getDeviceInfo();
            for (Info element : deviceInfo) {
                try {
                    return ((MidiDeviceProvider) deviceProviders.get(i)).getDevice(element).getTransmitter();
                } catch (MidiUnavailableException e) {}
            }
        }
        throw new MidiUnavailableException("There are no Transmitters installed on your system!");
    }
    public static boolean isFileTypeSupported(int fileType) {
        List<?> fileWriterProviders = ProviderService.getProviders(midiFileWriterPath);
        if (fileWriterProviders.size() == 0) {
            throw new Error("There is no MidiFileWriterProviders on your system!!!");
        }
        return ((MidiFileWriter) fileWriterProviders.get(0)).isFileTypeSupported(fileType);
    }
    public static boolean isFileTypeSupported(int fileType, Sequence sequence) {
        List<?> fileWriterProviders = ProviderService.getProviders(midiFileWriterPath);
        if (fileWriterProviders.size() == 0) {
            throw new Error("There is no MidiFileWriterProviders on your system!!!");
        }
        return ((MidiFileWriter) fileWriterProviders.get(0)).isFileTypeSupported(fileType, sequence);
    }
    public static int write(Sequence in, int type, File out) throws IOException {
        List<?>  fileWriterProviders = ProviderService.getProviders(midiFileWriterPath);
        if (fileWriterProviders.size() == 0) {
            throw new Error("There is no MidiFileWriterProviders on your system!!!");
        }
        return ((MidiFileWriter) fileWriterProviders.get(0)).write(in, type, out);
    }
    public static int write(Sequence in, int fileType, OutputStream out) throws IOException {
        List<?>  fileWriterProviders = ProviderService.getProviders(midiFileWriterPath);
        if (fileWriterProviders.size() == 0) {
            throw new Error("There is no MidiFileWriterProviders on your system!!!");
        }
        return ((MidiFileWriter) fileWriterProviders.get(0)).write(in, fileType, out);
    }
}
