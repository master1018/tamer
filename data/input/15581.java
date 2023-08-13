public class ProgramAndBankChange {
    private static SimpleInstrument generateTestInstrument(Patch patch) {
        ModelOscillator osc = new ModelOscillator() {
            public float getAttenuation() {
                return 0;
            }
            public int getChannels() {
                return 1;
            }
            public ModelOscillatorStream open(float samplerate) {
                return new ModelOscillatorStream() {
                    public void close() throws IOException {
                    }
                    public void noteOff(int velocity) {
                    }
                    public void noteOn(MidiChannel channel, VoiceStatus voice,
                            int noteNumber, int velocity) {
                    }
                    public int read(float[][] buffer, int offset, int len)
                            throws IOException {
                        return len;
                    }
                    public void setPitch(float ipitch) {
                    }
                };
            }
        };
        ModelPerformer performer = new ModelPerformer();
        performer.getOscillators().add(osc);
        SimpleInstrument testinstrument = new SimpleInstrument();
        testinstrument.setPatch(patch);
        testinstrument.add(performer);
        return testinstrument;
    }
    private static void assertTrue(boolean value) throws Exception {
        if (!value)
            throw new RuntimeException("assertTrue fails!");
    }
    private static void testProgramAndBank(SoftSynthesizer soft,
            AudioInputStream stream, Patch patch) throws Exception {
        int program = patch.getProgram();
        int bank = patch.getBank();
        MidiChannel channel = soft.getChannels()[0];
        byte[] buff = new byte[2048];
        channel.programChange(bank, program);
        channel.noteOn(64, 64);
        stream.read(buff, 0, buff.length);
        int foundprogram = -1;
        int foundbank = -1;
        VoiceStatus[] vstatus = soft.getVoiceStatus();
        for (int i = 0; i < vstatus.length; i++) {
            if (vstatus[i].active) {
                foundprogram = vstatus[i].program;
                foundbank = vstatus[i].bank;
                break;
            }
        }
        assertTrue(foundprogram == program);
        assertTrue(foundbank == bank);
        channel.noteOn(64, 0);
        stream.read(buff, 0, buff.length);
        channel = soft.getChannels()[1];
        channel.controlChange(0x00, bank / 128);
        channel.controlChange(0x20, bank % 128);
        channel.programChange(program);
        channel.noteOn(64, 64);
        stream.read(buff, 0, buff.length);
        foundprogram = -1;
        foundbank = -1;
        vstatus = soft.getVoiceStatus();
        for (int i = 0; i < vstatus.length; i++) {
            if (vstatus[i].active) {
                foundprogram = vstatus[i].program;
                foundbank = vstatus[i].bank;
                break;
            }
        }
        assertTrue(foundprogram == program);
        assertTrue(foundbank == bank);
        channel.noteOn(64, 0);
        stream.read(buff, 0, buff.length);
    }
    public static void main(String[] args) throws Exception {
        SoftSynthesizer soft = new SoftSynthesizer();
        AudioInputStream stream = soft.openStream(null, null);
        soft.unloadAllInstruments(soft.getDefaultSoundbank());
        soft.loadInstrument(generateTestInstrument(new Patch(0, 0)));
        soft.loadInstrument(generateTestInstrument(new Patch(7, 0)));
        soft.loadInstrument(generateTestInstrument(new Patch(20, 10)));
        soft.loadInstrument(generateTestInstrument(new Patch(3678, 15)));
        soft.loadInstrument(generateTestInstrument(new Patch(4678, 15)));
        testProgramAndBank(soft, stream, new Patch(0, 0));
        testProgramAndBank(soft, stream, new Patch(7, 0));
        testProgramAndBank(soft, stream, new Patch(20, 10));
        testProgramAndBank(soft, stream, new Patch(3678, 15));
        testProgramAndBank(soft, stream, new Patch(4678, 15));
        soft.close();
    }
}
