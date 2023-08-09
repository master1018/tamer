public class IsSoundbankSupported {
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
        SimpleSoundbank sbk = new SimpleSoundbank();
        SimpleInstrument ins = new SimpleInstrument();
        sbk.addInstrument(ins);
        assertTrue(synth.isSoundbankSupported(sbk));
        Soundbank dummysbk = new Soundbank()
        {
            public String getName() {
                return null;
            }
            public String getVersion() {
                return null;
            }
            public String getVendor() {
                return null;
            }
            public String getDescription() {
                return null;
            }
            public SoundbankResource[] getResources() {
                return null;
            }
            public Instrument[] getInstruments() {
                Instrument ins = new Instrument(null, null, null, null)
                {
                    public Object getData() {
                        return null;
                    }
                };
                return new Instrument[] {ins};
            }
            public Instrument getInstrument(Patch patch) {
                return null;
            }
        };
        assertTrue(!synth.isSoundbankSupported(dummysbk));
        synth.close();
    }
}
