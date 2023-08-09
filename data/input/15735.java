public class AddInstrument {
    private static void assertEquals(Object a, Object b) throws Exception
    {
        if(!a.equals(b))
            throw new RuntimeException("assertEquals fails!");
    }
    public static void main(String[] args) throws Exception {
        SimpleSoundbank soundbank = new SimpleSoundbank();
        SimpleInstrument ins = new SimpleInstrument();
        ins.setPatch(new Patch(3,7));
        soundbank.addInstrument(ins);
        assertEquals(soundbank.getInstruments().length, 1);
        assertEquals(soundbank.getInstruments()[0], ins);
    }
}
