public class ReadZip {
    private static void unreached (Object o)
        throws Exception
    {
        throw new Exception ("Expected exception was not thrown");
    }
    public static void main(String args[]) throws Exception {
        try (ZipFile zf = new ZipFile(new File(System.getProperty("test.src", "."),
                                               "input.zip"))) {
            try { unreached (zf.getEntry(null)); }
            catch (NullPointerException e) {}
            try { unreached (zf.getInputStream(null)); }
            catch (NullPointerException e) {}
            ZipEntry ze = zf.getEntry("ReadZip.java");
            if (ze == null) {
                throw new Exception("cannot read from zip file");
            }
        }
        File newZip = new File(System.getProperty("test.dir", "."), "input2.zip");
        Files.copy(Paths.get(System.getProperty("test.src", ""), "input.zip"),
                   newZip.toPath(), StandardCopyOption.REPLACE_EXISTING);
        try (OutputStream os = Files.newOutputStream(newZip.toPath(),
                                                     StandardOpenOption.APPEND)) {
            os.write(1); os.write(3); os.write(5); os.write(7);
        }
        try (ZipFile zf = new ZipFile(newZip)) {
            ZipEntry ze = zf.getEntry("ReadZip.java");
            if (ze == null) {
                throw new Exception("cannot read from zip file");
            }
        } finally {
            newZip.delete();
        }
        try {
            try (FileOutputStream fos = new FileOutputStream(newZip);
                 ZipOutputStream zos = new ZipOutputStream(fos))
            {
                ZipEntry ze = new ZipEntry("ZipEntry");
                zos.putNextEntry(ze);
                zos.write(1); zos.write(2); zos.write(3); zos.write(4);
                zos.closeEntry();
                zos.setComment("This is the comment for testing");
            }
            try (ZipFile zf = new ZipFile(newZip)) {
                ZipEntry ze = zf.getEntry("ZipEntry");
                if (ze == null)
                    throw new Exception("cannot read entry from zip file");
                if (!"This is the comment for testing".equals(zf.getComment()))
                    throw new Exception("cannot read comment from zip file");
            }
        } finally {
            newZip.delete();
        }
        try { unreached (new ZipFile(
                             new File(System.getProperty("test.src", "."),
                                     "input"
                                      + String.valueOf(new java.util.Random().nextInt())
                                      + ".zip")));
        } catch (FileNotFoundException fnfe) {}
    }
}
