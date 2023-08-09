public class NewSoftTuningPatch {
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
        SoftTuning tuning = new SoftTuning(new Patch(8,32));
        assertEquals(tuning.getPatch().getProgram(), 32);
        assertEquals(tuning.getPatch().getBank(), 8);
    }
}
