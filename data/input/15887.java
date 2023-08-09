public class StandardMidiFileWriter extends MidiFileWriter {
    private static final int MThd_MAGIC = 0x4d546864;  
    private static final int MTrk_MAGIC = 0x4d54726b;  
    private static final int ONE_BYTE   = 1;
    private static final int TWO_BYTE   = 2;
    private static final int SYSEX      = 3;
    private static final int META       = 4;
    private static final int ERROR      = 5;
    private static final int IGNORE     = 6;
    private static final int MIDI_TYPE_0 = 0;
    private static final int MIDI_TYPE_1 = 1;
    private static final int bufferSize = 16384;  
    private DataOutputStream tddos;               
    private static final int types[] = {
        MIDI_TYPE_0,
        MIDI_TYPE_1
    };
    public int[] getMidiFileTypes() {
        int[] localArray = new int[types.length];
        System.arraycopy(types, 0, localArray, 0, types.length);
        return localArray;
    }
    public int[] getMidiFileTypes(Sequence sequence){
        int typesArray[];
        Track tracks[] = sequence.getTracks();
        if( tracks.length==1 ) {
            typesArray = new int[2];
            typesArray[0] = MIDI_TYPE_0;
            typesArray[1] = MIDI_TYPE_1;
        } else {
            typesArray = new int[1];
            typesArray[0] = MIDI_TYPE_1;
        }
        return typesArray;
    }
    public boolean isFileTypeSupported(int type) {
        for(int i=0; i<types.length; i++) {
            if( type == types[i] ) {
                return true;
            }
        }
        return false;
    }
    public int write(Sequence in, int type, OutputStream out) throws IOException {
        byte [] buffer = null;
        int bytesRead = 0;
        long bytesWritten = 0;
        if( !isFileTypeSupported(type,in) ) {
            throw new IllegalArgumentException("Could not write MIDI file");
        }
        InputStream fileStream = getFileStream(type,in);
        if (fileStream == null) {
            throw new IllegalArgumentException("Could not write MIDI file");
        }
        buffer = new byte[bufferSize];
        while( (bytesRead = fileStream.read( buffer )) >= 0 ) {
            out.write( buffer, 0, (int)bytesRead );
            bytesWritten += bytesRead;
        }
        return (int) bytesWritten;
    }
    public int write(Sequence in, int type, File out) throws IOException {
        FileOutputStream fos = new FileOutputStream(out); 
        int bytesWritten = write( in, type, fos );
        fos.close();
        return bytesWritten;
    }
    private InputStream getFileStream(int type, Sequence sequence) throws IOException {
        Track tracks[] = sequence.getTracks();
        int bytesBuilt = 0;
        int headerLength = 14;
        int length = 0;
        int timeFormat;
        float divtype;
        PipedOutputStream   hpos = null;
        DataOutputStream    hdos = null;
        PipedInputStream    headerStream = null;
        InputStream         trackStreams [] = null;
        InputStream         trackStream = null;
        InputStream fStream = null;
        if( type==MIDI_TYPE_0 ) {
            if (tracks.length != 1) {
                return null;
            }
        } else if( type==MIDI_TYPE_1 ) {
            if (tracks.length < 1) { 
                return null;
            }
        } else {
            if(tracks.length==1) {
                type = MIDI_TYPE_0;
            } else if(tracks.length>1) {
                type = MIDI_TYPE_1;
            } else {
                return null;
            }
        }
        trackStreams = new InputStream[tracks.length];
        int trackCount = 0;
        for(int i=0; i<tracks.length; i++) {
            try {
                trackStreams[trackCount] = writeTrack( tracks[i], type );
                trackCount++;
            } catch (InvalidMidiDataException e) {
                if(Printer.err) Printer.err("Exception in write: " + e.getMessage());
            }
        }
        if( trackCount == 1 ) {
            trackStream = trackStreams[0];
        } else if( trackCount > 1 ){
            trackStream = trackStreams[0];
            for(int i=1; i<tracks.length; i++) {
                if (trackStreams[i] != null) {
                    trackStream = new SequenceInputStream( trackStream, trackStreams[i]);
                }
            }
        } else {
            throw new IllegalArgumentException("invalid MIDI data in sequence");
        }
        hpos = new PipedOutputStream();
        hdos = new DataOutputStream(hpos);
        headerStream = new PipedInputStream(hpos);
        hdos.writeInt( MThd_MAGIC );
        hdos.writeInt( headerLength - 8 );
        if(type==MIDI_TYPE_0) {
            hdos.writeShort( 0 );
        } else {
            hdos.writeShort( 1 );
        }
        hdos.writeShort( (short) trackCount );
        divtype = sequence.getDivisionType();
        if( divtype == Sequence.PPQ ) {
            timeFormat = sequence.getResolution();
        } else if( divtype == Sequence.SMPTE_24) {
            timeFormat = (24<<8) * -1;
            timeFormat += (sequence.getResolution() & 0xFF);
        } else if( divtype == Sequence.SMPTE_25) {
            timeFormat = (25<<8) * -1;
            timeFormat += (sequence.getResolution() & 0xFF);
        } else if( divtype == Sequence.SMPTE_30DROP) {
            timeFormat = (29<<8) * -1;
            timeFormat += (sequence.getResolution() & 0xFF);
        } else if( divtype == Sequence.SMPTE_30) {
            timeFormat = (30<<8) * -1;
            timeFormat += (sequence.getResolution() & 0xFF);
        } else {
            return null;
        }
        hdos.writeShort( timeFormat );
        fStream = new SequenceInputStream(headerStream, trackStream);
        hdos.close();
        length = bytesBuilt + headerLength;
        return fStream;
    }
    private int getType(int byteValue) {
        if ((byteValue & 0xF0) == 0xF0) {
            switch(byteValue) {
            case 0xF0:
            case 0xF7:
                return SYSEX;
            case 0xFF:
                return META;
            }
            return IGNORE;
        }
        switch(byteValue & 0xF0) {
        case 0x80:
        case 0x90:
        case 0xA0:
        case 0xB0:
        case 0xE0:
            return TWO_BYTE;
        case 0xC0:
        case 0xD0:
            return ONE_BYTE;
        }
        return ERROR;
    }
    private final static long mask = 0x7F;
    private int writeVarInt(long value) throws IOException {
        int len = 1;
        int shift=63; 
        while ((shift > 0) && ((value & (mask << shift)) == 0)) shift-=7;
        while (shift > 0) {
            tddos.writeByte((int) (((value & (mask << shift)) >> shift) | 0x80));
            shift-=7;
            len++;
        }
        tddos.writeByte((int) (value & mask));
        return len;
    }
    private InputStream writeTrack( Track track, int type ) throws IOException, InvalidMidiDataException {
        int bytesWritten = 0;
        int lastBytesWritten = 0;
        int size = track.size();
        PipedOutputStream thpos = new PipedOutputStream();
        DataOutputStream  thdos = new DataOutputStream(thpos);
        PipedInputStream  thpis = new PipedInputStream(thpos);
        ByteArrayOutputStream tdbos = new ByteArrayOutputStream();
        tddos = new DataOutputStream(tdbos);
        ByteArrayInputStream tdbis = null;
        SequenceInputStream  fStream = null;
        long currentTick = 0;
        long deltaTick = 0;
        long eventTick = 0;
        int runningStatus = -1;
        for(int i=0; i<size; i++) {
            MidiEvent event = track.get(i);
            int status;
            int eventtype;
            int metatype;
            int data1, data2;
            int length;
            byte data[] = null;
            ShortMessage shortMessage = null;
            MetaMessage  metaMessage  = null;
            SysexMessage sysexMessage = null;
            eventTick = event.getTick();
            deltaTick = event.getTick() - currentTick;
            currentTick = event.getTick();
            status = event.getMessage().getStatus();
            eventtype = getType( status );
            switch( eventtype ) {
            case ONE_BYTE:
                shortMessage = (ShortMessage) event.getMessage();
                data1 = shortMessage.getData1();
                bytesWritten += writeVarInt( deltaTick );
                if(status!=runningStatus) {
                    runningStatus=status;
                    tddos.writeByte(status);  bytesWritten += 1;
                }
                tddos.writeByte(data1);   bytesWritten += 1;
                break;
            case TWO_BYTE:
                shortMessage = (ShortMessage) event.getMessage();
                data1 = shortMessage.getData1();
                data2 = shortMessage.getData2();
                bytesWritten += writeVarInt( deltaTick );
                if(status!=runningStatus) {
                    runningStatus=status;
                    tddos.writeByte(status);  bytesWritten += 1;
                }
                tddos.writeByte(data1);   bytesWritten += 1;
                tddos.writeByte(data2);   bytesWritten += 1;
                break;
            case SYSEX:
                sysexMessage = (SysexMessage) event.getMessage();
                length     = sysexMessage.getLength();
                data       = sysexMessage.getMessage();
                bytesWritten += writeVarInt( deltaTick );
                runningStatus=status;
                tddos.writeByte( data[0] ); bytesWritten += 1;
                bytesWritten += writeVarInt( (data.length-1) );
                tddos.write(data, 1, (data.length-1));
                bytesWritten += (data.length-1);
                break;
            case META:
                metaMessage = (MetaMessage) event.getMessage();
                length    = metaMessage.getLength();
                data      = metaMessage.getMessage();
                bytesWritten += writeVarInt( deltaTick );
                runningStatus=status;
                tddos.write( data, 0, data.length );
                bytesWritten += data.length;
                break;
            case IGNORE:
                break;
            case ERROR:
                break;
            default:
                throw new InvalidMidiDataException("internal file writer error");
            }
        }
        thdos.writeInt(MTrk_MAGIC);
        thdos.writeInt(bytesWritten);
        bytesWritten += 8;
        tdbis = new ByteArrayInputStream( tdbos.toByteArray() );
        fStream = new SequenceInputStream(thpis,tdbis);
        thdos.close();
        tddos.close();
        return fStream;
    }
}
