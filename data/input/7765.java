public class FinalizeInflater {
    public static void main(String[] args) throws Throwable {
        try (ZipFile zf = new ZipFile(new File(System.getProperty("test.src", "."), "input.zip")))
        {
            ZipEntry ze = zf.getEntry("ReadZip.java");
            read(zf.getInputStream(ze));
            System.gc();
            System.runFinalization();
            System.gc();
            read(zf.getInputStream(ze));
        }
    }
    private static void read(InputStream is)
        throws IOException
    {
        Wrapper wrapper = new Wrapper(is);
        byte[] buffer = new byte[32];
        try {
            while(is.read(buffer)>0){}
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    static class Wrapper{
        InputStream is;
        public Wrapper(InputStream is) {
            this.is = is;
        }
        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            is.close();
        }
    }
}
