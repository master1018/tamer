public class Seek
{
    public static void main(String argv[]) throws Exception
    {
        String dir = System.getProperty("test.src", ".");
        RandomAccessFile raf = new RandomAccessFile
            (new File(dir, "Seek.java"), "r");
        try {
            raf.seek(-10);
            throw new Exception
                ("Should have thrown an IOException when seek offset is < 0");
        } catch (IOException e) {
        } finally {
            raf.close();
        }
    }
}
