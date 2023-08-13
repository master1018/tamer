public class LoadInstruments {
    private static void assertEquals(Object a, Object b) throws Exception
    {
        if(!a.equals(b))
            throw new RuntimeException("assertEquals fails!");
    }
    private static void assertTrue(boolean value) throws Exception
    {
        if(!value)
            throw new RuntimeException("assertTrue fails!");
    }
    public static void main(String[] args) throws Exception {
        AudioSynthesizer synth = new SoftSynthesizer();
        synth.openStream(null, null);
        Soundbank defsbk = synth.getDefaultSoundbank();
        if(defsbk != null)
        {
            synth.unloadAllInstruments(defsbk);
            SimpleSoundbank sbk = new SimpleSoundbank();
            SimpleInstrument ins = new SimpleInstrument();
            ins.setPatch(new Patch(0,1));
            sbk.addInstrument(ins);
            SimpleInstrument ins2 = new SimpleInstrument();
            ins2.setPatch(new Patch(0,2));
            sbk.addInstrument(ins2);
            synth.loadInstruments(sbk, new Patch[] {ins2.getPatch()});
            assertTrue(synth.getLoadedInstruments().length == 1);
        }
        synth.close();
    }
}
