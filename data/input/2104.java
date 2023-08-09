public class ReadByteArrayIntInt {
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
            RIFFWriter chunk = writer.writeChunk("TSCH");
            chunk.write(new byte[] {1,2,3});
            writer.close();
            writer = null;
            FileInputStream fis = new FileInputStream(tempfile);
            reader = new RIFFReader(fis);
            assertEquals(reader.getFormat(), "RIFF");
            assertEquals(reader.getType(), "TEST");
            RIFFReader readchunk = reader.nextChunk();
            assertEquals(readchunk.getFormat(), "TSCH");
            assertEquals(readchunk.read(), 1);
            assertEquals(readchunk.read(), 2);
            assertEquals(readchunk.read(), 3);
            fis.close();
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
