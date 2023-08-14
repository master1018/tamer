public class TestGetSoundbankUrl {
    private static void assertTrue(boolean value) throws Exception
    {
        if(!value)
            throw new RuntimeException("assertTrue fails!");
    }
    public static void main(String[] args) throws Exception {
        File file = new File(System.getProperty("test.src", "."), "ding.dls");
        URL url = file.toURI().toURL();
        Soundbank dls = new DLSSoundbankReader().getSoundbank(url);
        assertTrue(dls.getInstruments().length == 1);
        Patch patch = dls.getInstruments()[0].getPatch();
        assertTrue(patch.getProgram() == 0);
        assertTrue(patch.getBank() == 0);
    }
}
