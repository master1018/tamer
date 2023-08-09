public class StandardMidiFileReader extends MidiFileReader {
    private static final int MThd_MAGIC = 0x4d546864;  
    private static final int MIDI_TYPE_0 = 0;
    private static final int MIDI_TYPE_1 = 1;
    private static final int bisBufferSize = 1024; 
    private static final int types[] = {
        MIDI_TYPE_0,
        MIDI_TYPE_1
    };
    public MidiFileFormat getMidiFileFormat(InputStream stream) throws InvalidMidiDataException, IOException {
        return getMidiFileFormatFromStream(stream, MidiFileFormat.UNKNOWN_LENGTH, null);
    }
    private MidiFileFormat getMidiFileFormatFromStream(InputStream stream, int fileLength, SMFParser smfParser) throws InvalidMidiDataException, IOException {
        int maxReadLength = 16;
        int duration = MidiFileFormat.UNKNOWN_LENGTH;
        DataInputStream dis;
        if (stream instanceof DataInputStream) {
            dis = (DataInputStream) stream;
        } else {
            dis = new DataInputStream(stream);
        }
        if (smfParser == null) {
            dis.mark(maxReadLength);
        } else {
            smfParser.stream = dis;
        }
        int type;
        int numtracks;
        float divisionType;
        int resolution;
        try {
            int magic = dis.readInt();
            if( !(magic == MThd_MAGIC) ) {
                throw new InvalidMidiDataException("not a valid MIDI file");
            }
            int bytesRemaining = dis.readInt() - 6;
            type = dis.readShort();
            numtracks = dis.readShort();
            int timing = dis.readShort();
            if (timing > 0) {
                divisionType = Sequence.PPQ;
                resolution = timing;
            } else {
                int frameCode = -1 * (timing >> 8);
                switch(frameCode) {
                case 24:
                    divisionType = Sequence.SMPTE_24;
                    break;
                case 25:
                    divisionType = Sequence.SMPTE_25;
                    break;
                case 29:
                    divisionType = Sequence.SMPTE_30DROP;
                    break;
                case 30:
                    divisionType = Sequence.SMPTE_30;
                    break;
                default:
                    throw new InvalidMidiDataException("Unknown frame code: " + frameCode);
                }
                resolution = timing & 0xFF;
            }
            if (smfParser != null) {
                dis.skip(bytesRemaining);
                smfParser.tracks = numtracks;
            }
        } finally {
            if (smfParser == null) {
                dis.reset();
            }
        }
        MidiFileFormat format = new MidiFileFormat(type, divisionType, resolution, fileLength, duration);
        return format;
    }
    public MidiFileFormat getMidiFileFormat(URL url) throws InvalidMidiDataException, IOException {
        InputStream urlStream = url.openStream(); 
        BufferedInputStream bis = new BufferedInputStream( urlStream, bisBufferSize );
        MidiFileFormat fileFormat = null;
        try {
            fileFormat = getMidiFileFormat( bis ); 
        } finally {
            bis.close();
        }
        return fileFormat;
    }
    public MidiFileFormat getMidiFileFormat(File file) throws InvalidMidiDataException, IOException {
        FileInputStream fis = new FileInputStream(file); 
        BufferedInputStream bis = new BufferedInputStream(fis, bisBufferSize);
        long length = file.length();
        if (length > Integer.MAX_VALUE) {
            length = MidiFileFormat.UNKNOWN_LENGTH;
        }
        MidiFileFormat fileFormat = null;
        try {
            fileFormat = getMidiFileFormatFromStream(bis, (int) length, null);
        } finally {
            bis.close();
        }
        return fileFormat;
    }
    public Sequence getSequence(InputStream stream) throws InvalidMidiDataException, IOException {
        SMFParser smfParser = new SMFParser();
        MidiFileFormat format = getMidiFileFormatFromStream(stream,
                                                            MidiFileFormat.UNKNOWN_LENGTH,
                                                            smfParser);
        if ((format.getType() != 0) && (format.getType() != 1)) {
            throw new InvalidMidiDataException("Invalid or unsupported file type: "  + format.getType());
        }
        Sequence sequence = new Sequence(format.getDivisionType(), format.getResolution());
        for (int i = 0; i < smfParser.tracks; i++) {
            if (smfParser.nextTrack()) {
                smfParser.readTrack(sequence.createTrack());
            } else {
                break;
            }
        }
        return sequence;
    }
    public Sequence getSequence(URL url) throws InvalidMidiDataException, IOException {
        InputStream is = url.openStream();  
        is = new BufferedInputStream(is, bisBufferSize);
        Sequence seq = null;
        try {
            seq = getSequence(is);
        } finally {
            is.close();
        }
        return seq;
    }
    public Sequence getSequence(File file) throws InvalidMidiDataException, IOException {
        InputStream is = new FileInputStream(file); 
        is = new BufferedInputStream(is, bisBufferSize);
        Sequence seq = null;
        try {
            seq = getSequence(is);
        } finally {
            is.close();
        }
        return seq;
    }
}
class SMFParser {
    private static final int MTrk_MAGIC = 0x4d54726b;  
    private static final boolean STRICT_PARSER = false;
    private static final boolean DEBUG = false;
    int tracks;                       
    DataInputStream stream;   
    private int trackLength = 0;  
    private byte[] trackData = null;
    private int pos = 0;
    public SMFParser() {
    }
    private int readUnsigned() throws IOException {
        return trackData[pos++] & 0xFF;
    }
    private void read(byte[] data) throws IOException {
        System.arraycopy(trackData, pos, data, 0, data.length);
        pos += data.length;
    }
    private long readVarInt() throws IOException {
        long value = 0; 
        int currentByte = 0;
        do {
            currentByte = trackData[pos++] & 0xFF;
            value = (value << 7) + (currentByte & 0x7F);
        } while ((currentByte & 0x80) != 0);
        return value;
    }
    private int readIntFromStream() throws IOException {
        try {
            return stream.readInt();
        } catch (EOFException eof) {
            throw new EOFException("invalid MIDI file");
        }
    }
    boolean nextTrack() throws IOException, InvalidMidiDataException {
        int magic;
        trackLength = 0;
        do {
            if (stream.skipBytes(trackLength) != trackLength) {
                if (!STRICT_PARSER) {
                    return false;
                }
                throw new EOFException("invalid MIDI file");
            }
            magic = readIntFromStream();
            trackLength = readIntFromStream();
        } while (magic != MTrk_MAGIC);
        if (!STRICT_PARSER) {
            if (trackLength < 0) {
                return false;
            }
        }
        trackData = new byte[trackLength];
        try {
            stream.readFully(trackData);
        } catch (EOFException eof) {
            if (!STRICT_PARSER) {
                return false;
            }
            throw new EOFException("invalid MIDI file");
        }
        pos = 0;
        return true;
    }
    private boolean trackFinished() {
        return pos >= trackLength;
    }
    void readTrack(Track track) throws IOException, InvalidMidiDataException {
        try {
            long tick = 0;
            int status = 0;
            boolean endOfTrackFound = false;
            while (!trackFinished() && !endOfTrackFound) {
                MidiMessage message;
                int data1 = -1;         
                int data2 = 0;
                tick += readVarInt();
                int byteValue = readUnsigned();
                if (byteValue >= 0x80) {
                    status = byteValue;
                } else {
                    data1 = byteValue;
                }
                switch (status & 0xF0) {
                case 0x80:
                case 0x90:
                case 0xA0:
                case 0xB0:
                case 0xE0:
                    if (data1 == -1) {
                        data1 = readUnsigned();
                    }
                    data2 = readUnsigned();
                    message = new FastShortMessage(status | (data1 << 8) | (data2 << 16));
                    break;
                case 0xC0:
                case 0xD0:
                    if (data1 == -1) {
                        data1 = readUnsigned();
                    }
                    message = new FastShortMessage(status | (data1 << 8));
                    break;
                case 0xF0:
                    switch(status) {
                    case 0xF0:
                    case 0xF7:
                        int sysexLength = (int) readVarInt();
                        byte[] sysexData = new byte[sysexLength];
                        read(sysexData);
                        SysexMessage sysexMessage = new SysexMessage();
                        sysexMessage.setMessage(status, sysexData, sysexLength);
                        message = sysexMessage;
                        break;
                    case 0xFF:
                        int metaType = readUnsigned();
                        int metaLength = (int) readVarInt();
                        byte[] metaData = new byte[metaLength];
                        read(metaData);
                        MetaMessage metaMessage = new MetaMessage();
                        metaMessage.setMessage(metaType, metaData, metaLength);
                        message = metaMessage;
                        if (metaType == 0x2F) {
                            endOfTrackFound = true;
                        }
                        break;
                    default:
                        throw new InvalidMidiDataException("Invalid status byte: " + status);
                    } 
                    break;
                default:
                    throw new InvalidMidiDataException("Invalid status byte: " + status);
                } 
                track.add(new MidiEvent(message, tick));
            } 
        } catch (ArrayIndexOutOfBoundsException e) {
            if (DEBUG) e.printStackTrace();
            throw new EOFException("invalid MIDI file");
        }
    }
}
