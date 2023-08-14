public class SetVersion {
    private static void assertEquals(Object a, Object b) throws Exception
    {
        if(!a.equals(b))
            throw new RuntimeException("assertEquals fails!");
    }
    public static void main(String[] args) throws Exception {
        SimpleSoundbank soundbank = new SimpleSoundbank();
        soundbank.setVersion("hello");
        assertEquals(soundbank.getVersion(), "hello");
    }
}
