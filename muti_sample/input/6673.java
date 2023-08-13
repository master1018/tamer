public class TestGetSoundbankInputStream {
    private static void assertTrue(boolean value) throws Exception
    {
        if(!value)
            throw new RuntimeException("assertTrue fails!");
    }
    public static void main(String[] args) throws Exception {
        File file = new File(System.getProperty("test.src", "."), "ding.dls");
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        try
        {
            Soundbank dls = new DLSSoundbankReader().getSoundbank(bis);
            assertTrue(dls.getInstruments().length == 1);
            Patch patch = dls.getInstruments()[0].getPatch();
            assertTrue(patch.getProgram() == 0);
            assertTrue(patch.getBank() == 0);
        }
        finally
        {
            bis.close();
        }
    }
}
