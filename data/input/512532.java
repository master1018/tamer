public class NoSdCardWritePermissionTest extends AndroidTestCase {
    public void testWriteExternalStorage() throws FileNotFoundException, IOException {
        try {
            String fl = Environment.getExternalStorageDirectory().toString() +
                         "/this-should-not-exist.txt";
            FileOutputStream strm = new FileOutputStream(fl);
            strm.write("Oops!".getBytes());
            strm.flush();
            strm.close();
            fail("Was able to create and write to " + fl);
        } catch (SecurityException e) {
        } catch (FileNotFoundException e) {
        }
    }
}
