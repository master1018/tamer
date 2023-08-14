public class FileAccessPermissionTest extends AndroidTestCase {
    public void testSystemDirAccess() {
        File file = new File("/system");
        assertTrue(file.canRead());
        assertFalse(file.canWrite());
        File fakeSystemFile = new File(file, "test");
        try {
            fakeSystemFile.createNewFile();
            fail("should throw out IO exception");
        } catch (IOException e) {
        }
        assertFalse(fakeSystemFile.mkdirs());
        file = new File("/system/app");
        assertTrue(file.canRead());
        assertFalse(file.canWrite());
        File[] apkFiles = file.listFiles();
        for (File f : apkFiles) {
            assertTrue(f.canRead());
            assertFalse(f.canWrite());
            assertFalse(f.delete());
        }
    }
    public void testAccessAppDataDir() {
        File file = new File("/data/app");
        assertTrue(file.isDirectory());
        assertFalse(file.canRead());
        assertFalse(file.canWrite());
        File[] files = file.listFiles();
        assertTrue(files == null || files.length == 0);
        File dir = getContext().getFilesDir();
        assertTrue(dir.canRead());
        assertTrue(dir.canWrite());
        File newFile = new File(dir, System.currentTimeMillis() + "test.txt");
        try {
            assertTrue(newFile.createNewFile());
            writeFileCheck(newFile);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        File userAppDataDir = new File("/data/data");
        File otherAppDataDir = new File(userAppDataDir, "com.test.test.dir");
        assertFalse(otherAppDataDir.mkdirs());
        files = userAppDataDir.listFiles();
        assertTrue(files == null || files.length == 0);
        File newOtherAppFile = new File(userAppDataDir, "test.txt");
        try {
            assertFalse(newOtherAppFile.createNewFile());
            writeFileCheck(newOtherAppFile);
            fail("Created file in other app's directory");
        } catch (IOException e) {
        }
        File sdcardDir = Environment.getExternalStorageDirectory();
        assertTrue(sdcardDir.exists());
        File sdcardFile = new File(sdcardDir, System.currentTimeMillis() + "test.txt");
        try {
            assertTrue(sdcardFile.createNewFile());
            writeFileCheck(sdcardFile);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        File sdcardSubDir = new File(sdcardDir, System.currentTimeMillis() + "test");
        assertTrue(sdcardSubDir.mkdirs());
        File sdcardSubDirFile = new File(sdcardSubDir, System.currentTimeMillis() + "test.txt");
        try {
            assertTrue(sdcardSubDirFile.createNewFile());
            writeFileCheck(sdcardSubDirFile);
        } catch (IOException e) {
            fail(e.getMessage());
        } finally {
            assertTrue(sdcardSubDir.delete());
        }
    }
    private void writeFileCheck(File file) {
        FileOutputStream fout = null;
        FileInputStream fin = null;
        try {
            byte[]data = new byte[]{0x1, 0x2, 0x3,0x4};
            fout = new FileOutputStream(file);
            fout.write(data);
            fout.flush();
            fout.close();
            fout = null;
            fin = new FileInputStream(file);
            for (int i = 0; i < 4; i++) {
                assertEquals(i + 1, fin.read());
            }
            fin.close();
            fin = null;
        } catch (FileNotFoundException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        } finally {
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException e) {
                    fail(e.getMessage());
                }
            }
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                    fail(e.getMessage());
                }
            }
            assertTrue(file.delete());
        }
    }
}
