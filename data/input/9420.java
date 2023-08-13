public class AddResource {
    private static void assertEquals(Object a, Object b) throws Exception
    {
        if(!a.equals(b))
            throw new RuntimeException("assertEquals fails!");
    }
    public static void main(String[] args) throws Exception {
        SimpleSoundbank soundbank = new SimpleSoundbank();
        SoundbankResource res = new SoundbankResource(soundbank, "test", null) {
            public Object getData() {
                return null;
            }};
        soundbank.addResource(res);
        assertEquals(soundbank.getResources().length, 1);
        assertEquals(soundbank.getResources()[0], res);
    }
}
