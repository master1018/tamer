public class NewSoftAudioBuffer {
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
        AudioFormat frm = new AudioFormat(8000, 8, 1, true, false);
        SoftAudioBuffer buff = new SoftAudioBuffer(377, frm);
        assertEquals(buff.getSize(), 377);
        assertEquals(buff.getFormat(), frm);
        assertTrue(buff.isSilent());
    }
}
