public class NoteOverFlowTest2 {
    public static void main(String[] args) throws Exception
    {
        AudioSynthesizer synth = new SoftSynthesizer();
        AudioFormat format = new AudioFormat(44100, 16, 2, true, false);
        Map<String, Object> p = new HashMap<String, Object>();
        p.put("max polyphony", new Integer(5));
        AudioInputStream stream = synth.openStream(format, p);
        SF2Soundbank sf2 = new SF2Soundbank();
        SF2Sample sample = new SF2Sample(sf2);
        sample.setName("test sample");
        sample.setData(new byte[100]);
        sample.setSampleRate(44100);
        sample.setOriginalPitch(20);
        sf2.addResource(sample);
        SF2Layer layer = new SF2Layer(sf2);
        layer.setName("test layer");
        sf2.addResource(layer);
        for (int i = 0; i < 100; i++) {
            SF2LayerRegion region = new SF2LayerRegion();
            region.setSample(sample);
            layer.getRegions().add(region);
        }
        SF2Instrument ins = new SF2Instrument(sf2);
        ins.setPatch(new Patch(0,0));
        ins.setName("test instrument");
        sf2.addInstrument(ins);
        SF2InstrumentRegion insregion = new SF2InstrumentRegion();
        insregion.setLayer(layer);
        ins.getRegions().add(insregion);
        synth.unloadAllInstruments(synth.getDefaultSoundbank());
        synth.loadAllInstruments(sf2);
        MidiChannel ch1 = synth.getChannels()[0];
        ch1.programChange(0);
        ch1.noteOn(64, 64);
        stream.skip(format.getFrameSize() * ((int)(format.getFrameRate() * 2)));
        synth.close();
    }
}
