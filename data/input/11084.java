public class TestPreciseTimestampRendering {
    public static AudioFormat format = new AudioFormat(44100, 16, 1, true,
            false);
    public static SF2Soundbank createTestSoundbank() {
        SF2Soundbank soundbank = new SF2Soundbank();
        float[] data = new float[100];
        Arrays.fill(data, 0);
        data[0] = 1.0f;
        byte[] bdata = new byte[data.length * format.getFrameSize()];
        AudioFloatConverter.getConverter(format).toByteArray(data, bdata);
        SF2Sample sample = new SF2Sample(soundbank);
        sample.setName("Test Sample");
        sample.setData(bdata);
        sample.setSampleRate((long) format.getSampleRate());
        sample.setOriginalPitch(69);
        soundbank.addResource(sample);
        SF2Layer layer = new SF2Layer(soundbank);
        layer.setName("Test Layer");
        soundbank.addResource(layer);
        SF2LayerRegion region = new SF2LayerRegion();
        region.setSample(sample);
        layer.getRegions().add(region);
        SF2Instrument ins = new SF2Instrument(soundbank);
        ins.setName("Test Instrument");
        soundbank.addInstrument(ins);
        SF2InstrumentRegion insregion = new SF2InstrumentRegion();
        insregion.setLayer(layer);
        ins.getRegions().add(insregion);
        return soundbank;
    }
    public static Soundbank createTestSoundbankWithChannelMixer() {
        SF2Soundbank soundbank = createTestSoundbank();
        SimpleSoundbank simplesoundbank = new SimpleSoundbank();
        SimpleInstrument simpleinstrument = new SimpleInstrument() {
            public ModelChannelMixer getChannelMixer(MidiChannel channel,
                    AudioFormat format) {
                return new ModelAbstractChannelMixer() {
                    boolean active = true;
                    public boolean process(float[][] buffer, int offset, int len) {
                        for (int i = 0; i < buffer.length; i++) {
                            float[] cbuffer = buffer[i];
                            for (int j = 0; j < cbuffer.length; j++) {
                                cbuffer[j] = -cbuffer[j];
                            }
                        }
                        return active;
                    }
                    public void stop() {
                        active = false;
                    }
                };
            }
        };
        simpleinstrument.add(soundbank.getInstruments()[0]);
        simplesoundbank.addInstrument(simpleinstrument);
        return simplesoundbank;
    }
    public static void main(String[] args) throws Exception {
        test(createTestSoundbank());
        test(createTestSoundbankWithChannelMixer());
    }
    public static void test(Soundbank soundbank) throws Exception {
        AudioSynthesizer synth = new SoftSynthesizer();
        AudioInputStream stream = synth.openStream(format, null);
        synth.unloadAllInstruments(synth.getDefaultSoundbank());
        synth.loadAllInstruments(soundbank);
        Receiver recv = synth.getReceiver();
        ShortMessage reverb_off = new ShortMessage();
        reverb_off.setMessage(ShortMessage.CONTROL_CHANGE, 91, 0);
        recv.send(reverb_off, -1);
        ShortMessage full_volume = new ShortMessage();
        full_volume.setMessage(ShortMessage.CONTROL_CHANGE, 7, 127);
        recv.send(full_volume, -1);
        Random random = new Random(3485934583945l);
        long[] test_timestamps = new long[30];
        for (int i = 1; i < test_timestamps.length; i++) {
            test_timestamps[i] = i * 44100
                    + (int) (random.nextDouble() * 22050.0);
        }
        for (int i = 0; i < test_timestamps.length; i++) {
            ShortMessage midi_on = new ShortMessage();
            midi_on.setMessage(ShortMessage.NOTE_ON, 69, 127);
            recv.send(midi_on,
                    (long) ((test_timestamps[i] / 44100.0) * 1000000.0));
        }
        float[] fbuffer = new float[100];
        byte[] buffer = new byte[fbuffer.length * format.getFrameSize()];
        long firsts = -1;
        int counter = 0;
        long s = 0;
        long max_jitter = 0;
        outerloop: for (int k = 0; k < 10000000; k++) {
            stream.read(buffer);
            AudioFloatConverter.getConverter(format).toFloatArray(buffer,
                    fbuffer);
            for (int i = 0; i < fbuffer.length; i++) {
                if (fbuffer[i] != 0) {
                    if (firsts == -1)
                        firsts = s;
                    long measure_time = (s - firsts);
                    long predicted_time = test_timestamps[counter];
                    long jitter = Math.abs(measure_time - predicted_time);
                    if (jitter > 10)
                        max_jitter = jitter;
                    counter++;
                    if (counter == test_timestamps.length)
                        break outerloop;
                }
                s++;
            }
        }
        synth.close();
        if (counter == 0)
            throw new Exception("Nothing was measured!");
        if (max_jitter != 0) {
            throw new Exception("Jitter has occurred! "
                    + "(max jitter = " + max_jitter + ")");
        }
    }
}
