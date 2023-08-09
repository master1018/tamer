public class SoftMidiAudioFileReader extends AudioFileReader {
    public static final Type MIDI = new Type("MIDI", "mid");
    private static AudioFormat format = new AudioFormat(44100, 16, 2, true, false);
    public AudioFileFormat getAudioFileFormat(Sequence seq)
            throws UnsupportedAudioFileException, IOException {
        long totallen = seq.getMicrosecondLength() / 1000000;
        long len = (long) (format.getFrameRate() * (totallen + 4));
        return new AudioFileFormat(MIDI, format, (int) len);
    }
    public AudioInputStream getAudioInputStream(Sequence seq)
            throws UnsupportedAudioFileException, IOException {
        AudioSynthesizer synth = (AudioSynthesizer) new SoftSynthesizer();
        AudioInputStream stream;
        Receiver recv;
        try {
            stream = synth.openStream(format, null);
            recv = synth.getReceiver();
        } catch (MidiUnavailableException e) {
            throw new IOException(e.toString());
        }
        float divtype = seq.getDivisionType();
        Track[] tracks = seq.getTracks();
        int[] trackspos = new int[tracks.length];
        int mpq = 500000;
        int seqres = seq.getResolution();
        long lasttick = 0;
        long curtime = 0;
        while (true) {
            MidiEvent selevent = null;
            int seltrack = -1;
            for (int i = 0; i < tracks.length; i++) {
                int trackpos = trackspos[i];
                Track track = tracks[i];
                if (trackpos < track.size()) {
                    MidiEvent event = track.get(trackpos);
                    if (selevent == null || event.getTick() < selevent.getTick()) {
                        selevent = event;
                        seltrack = i;
                    }
                }
            }
            if (seltrack == -1)
                break;
            trackspos[seltrack]++;
            long tick = selevent.getTick();
            if (divtype == Sequence.PPQ)
                curtime += ((tick - lasttick) * mpq) / seqres;
            else
                curtime = (long) ((tick * 1000000.0 * divtype) / seqres);
            lasttick = tick;
            MidiMessage msg = selevent.getMessage();
            if (msg instanceof MetaMessage) {
                if (divtype == Sequence.PPQ) {
                    if (((MetaMessage) msg).getType() == 0x51) {
                        byte[] data = ((MetaMessage) msg).getData();
                        mpq = ((data[0] & 0xff) << 16)
                                | ((data[1] & 0xff) << 8) | (data[2] & 0xff);
                    }
                }
            } else {
                recv.send(msg, curtime);
            }
        }
        long totallen = curtime / 1000000;
        long len = (long) (stream.getFormat().getFrameRate() * (totallen + 4));
        stream = new AudioInputStream(stream, stream.getFormat(), len);
        return stream;
    }
    public AudioInputStream getAudioInputStream(InputStream inputstream)
            throws UnsupportedAudioFileException, IOException {
        inputstream.mark(200);
        Sequence seq;
        try {
            seq = MidiSystem.getSequence(inputstream);
        } catch (InvalidMidiDataException e) {
            inputstream.reset();
            throw new UnsupportedAudioFileException();
        } catch (IOException e) {
            inputstream.reset();
            throw new UnsupportedAudioFileException();
        }
        return getAudioInputStream(seq);
    }
    public AudioFileFormat getAudioFileFormat(URL url)
            throws UnsupportedAudioFileException, IOException {
        Sequence seq;
        try {
            seq = MidiSystem.getSequence(url);
        } catch (InvalidMidiDataException e) {
            throw new UnsupportedAudioFileException();
        } catch (IOException e) {
            throw new UnsupportedAudioFileException();
        }
        return getAudioFileFormat(seq);
    }
    public AudioFileFormat getAudioFileFormat(File file)
            throws UnsupportedAudioFileException, IOException {
        Sequence seq;
        try {
            seq = MidiSystem.getSequence(file);
        } catch (InvalidMidiDataException e) {
            throw new UnsupportedAudioFileException();
        } catch (IOException e) {
            throw new UnsupportedAudioFileException();
        }
        return getAudioFileFormat(seq);
    }
    public AudioInputStream getAudioInputStream(URL url)
            throws UnsupportedAudioFileException, IOException {
        Sequence seq;
        try {
            seq = MidiSystem.getSequence(url);
        } catch (InvalidMidiDataException e) {
            throw new UnsupportedAudioFileException();
        } catch (IOException e) {
            throw new UnsupportedAudioFileException();
        }
        return getAudioInputStream(seq);
    }
    public AudioInputStream getAudioInputStream(File file)
            throws UnsupportedAudioFileException, IOException {
        if (!file.getName().toLowerCase().endsWith(".mid"))
            throw new UnsupportedAudioFileException();
        Sequence seq;
        try {
            seq = MidiSystem.getSequence(file);
        } catch (InvalidMidiDataException e) {
            throw new UnsupportedAudioFileException();
        } catch (IOException e) {
            throw new UnsupportedAudioFileException();
        }
        return getAudioInputStream(seq);
    }
    public AudioFileFormat getAudioFileFormat(InputStream inputstream)
            throws UnsupportedAudioFileException, IOException {
        inputstream.mark(200);
        Sequence seq;
        try {
            seq = MidiSystem.getSequence(inputstream);
        } catch (InvalidMidiDataException e) {
            inputstream.reset();
            throw new UnsupportedAudioFileException();
        } catch (IOException e) {
            inputstream.reset();
            throw new UnsupportedAudioFileException();
        }
        return getAudioFileFormat(seq);
    }
}
