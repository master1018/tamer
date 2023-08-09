public class Close {
    private static void assertEquals(Object a, Object b) throws Exception
    {
        if(!a.equals(b))
            throw new RuntimeException("assertEquals fails!");
    }
    public static void main(String[] args) throws Exception {
        RIFFWriter writer = null;
        RIFFReader reader = null;
        File tempfile = File.createTempFile("test",".riff");
        try
        {
            writer = new RIFFWriter(tempfile, "TEST");
            writer.close();
            writer = null;
            FileInputStream fis = new FileInputStream(tempfile);
            reader = new RIFFReader(fis);
            reader.close();
            reader = null;
        }
        finally
        {
            if(writer != null)
                writer.close();
            if(reader != null)
                reader.close();
            if(tempfile.exists())
                if(!tempfile.delete())
                    tempfile.deleteOnExit();
        }
    }
}
