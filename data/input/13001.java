public class ReleaseInflater {
    public static void main(String[] args) throws Exception {
        ZipFile zf = new ZipFile(new File(System.getProperty("test.src"),
                                          "input.jar"));
        ZipEntry e = zf.getEntry("ReleaseInflater.java");
        InputStream in1 = zf.getInputStream(e);
        in1.close();
        in1.close();
        InputStream in2 = zf.getInputStream(e);
        InputStream in3 = zf.getInputStream(e);
        if (in2.read() != in3.read()) {
            throw new Exception("Stream is corrupted!");
        }
    }
}
