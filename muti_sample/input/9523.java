public class GetInstrument {
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
        assertEquals(soundbank.getInstrument(new Patch(3,7)), ins);
    }
}
