public class RemapInstrument {
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
            Instrument ins3 = defsbk.getInstrument(new Patch(0,3));
            Instrument ins10 = defsbk.getInstrument(new Patch(0,10));
            assertTrue(synth.remapInstrument(ins3, ins10));
            Instrument[] loaded = synth.getLoadedInstruments();
            for (int i = 0; i < loaded.length; i++) {
                if(loaded[i].getPatch().getBank() == ins3.getPatch().getBank())
                if(loaded[i].getPatch().getProgram() == ins3.getPatch().getProgram())
                {
                    assertEquals(loaded[i].getName(), ins10.getName());
                    break;
                }
            }
        }
        synth.close();
    }
}
