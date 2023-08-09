public class B7050028 {
    public static void main(String[] args) throws Exception {
        URLConnection conn = B7050028.class.getResource("B7050028.class").openConnection();
        int len = conn.getContentLength();
        byte[] data = new byte[len];
        InputStream is = conn.getInputStream();
        is.read(data);
        is.close();
        conn.setDefaultUseCaches(false);
        File jar = File.createTempFile("B7050028", ".jar");
        jar.deleteOnExit();
        OutputStream os = new FileOutputStream(jar);
        ZipOutputStream zos = new ZipOutputStream(os);
        ZipEntry ze = new ZipEntry("B7050028.class");
        ze.setMethod(ZipEntry.STORED);
        ze.setSize(len);
        CRC32 crc = new CRC32();
        crc.update(data);
        ze.setCrc(crc.getValue());
        zos.putNextEntry(ze);
        zos.write(data, 0, len);
        zos.closeEntry();
        zos.finish();
        zos.close();
        os.close();
        System.out.println(new URLClassLoader(new URL[] {new URL("jar:" + jar.toURI() + "!/")}, ClassLoader.getSystemClassLoader().getParent()).loadClass(B7050028.class.getName()));
    }
    private B7050028() {}
}
