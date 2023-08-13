@TestTargetClass(Pack200.Unpacker.class)
public class Pack200UnpackerTest extends TestCase {
    Unpacker unpacker;
    Map properties;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "properties",
        args = {}
    )
    @KnownFailure("No Implementation in Android!")
    public void testProperties() {
        assertTrue(properties.size()>0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "unpack",
        args = {java.io.File.class, java.util.jar.JarOutputStream.class}
    )
    @KnownFailure("No Implementation in Android!")
    public void testUnpackInputStreamJarOutputStream() throws IOException {
        File resources = Support_Resources.createTempFolder();
        Support_Resources.copyFile(resources, null, "junit4-4.3.1.jar");
        File jarFile = new File(resources, "junit4-4.3.1.jar");
        JarFile jf = new JarFile(jarFile);
        int jarEntries = jf.size();
        File packFile1 = Support_Resources.createTempFile("pack200_1");
        File packFile2 = Support_Resources.createTempFile("pack200_2");
        File packFile3 = Support_Resources.createTempFile("pack200_3");
        FileOutputStream fos1 = new FileOutputStream(packFile1);
        FileOutputStream fos2 = new FileOutputStream(packFile2);
        FileOutputStream fos3 = new FileOutputStream(packFile3);
        properties.put(Packer.EFFORT, "0");
        Packer packer = Pack200.newPacker();
        packer.pack(jf, fos1);
        jf.close();
        fos1.close();
        jf = new JarFile(jarFile);
        properties.put(Packer.EFFORT, "1");
        packer.pack(jf, fos2);
        jf.close();
        fos2.close();
        jf = new JarFile(jarFile);
        properties.put(Packer.EFFORT, "9");
        packer.pack(jf, fos3);
        jf.close();
        fos3.close();
        File jarFile1 = Support_Resources.createTempFile("jar_1");
        File jarFile2 = Support_Resources.createTempFile("jar_2");
        File jarFile3 = Support_Resources.createTempFile("jar_3");
        JarOutputStream jos1 = new JarOutputStream(new FileOutputStream(jarFile1));
        JarOutputStream jos2 = new JarOutputStream(new FileOutputStream(jarFile2));
        JarOutputStream jos3 = new JarOutputStream(new FileOutputStream(jarFile3));
        unpacker.unpack(packFile1, jos1);
        unpacker.unpack(packFile2, jos2);
        unpacker.unpack(packFile3, jos3);
        jos1.close();
        jos2.close();
        jos3.close();
        assertEquals(jarFile1.length(), jarFile2.length());
        assertEquals(jarFile2.length(), jarFile3.length());
        assertEquals(jarEntries, new JarFile(jarFile1).size());
        assertEquals(jarEntries, new JarFile(jarFile2).size());
        assertEquals(jarEntries, new JarFile(jarFile3).size());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "unpack",
        args = {java.io.InputStream.class, java.util.jar.JarOutputStream.class}
    )
    @KnownFailure("No Implementation in Android!")
    public void testUnpackFileJarOutputStream() throws IOException {
        File resources = Support_Resources.createTempFolder();
        Support_Resources.copyFile(resources, null, "junit4-4.3.1.jar");
        File jarFile = new File(resources, "junit4-4.3.1.jar");
        JarFile jf = new JarFile(jarFile);
        int jarEntries = jf.size();
        File packFile1 = Support_Resources.createTempFile("pack200_1");
        File packFile2 = Support_Resources.createTempFile("pack200_2");
        File packFile3 = Support_Resources.createTempFile("pack200_3");
        FileOutputStream fos1 = new FileOutputStream(packFile1);
        FileOutputStream fos2 = new FileOutputStream(packFile2);
        FileOutputStream fos3 = new FileOutputStream(packFile3);
        properties.put(Packer.EFFORT, "0");
        Packer packer = Pack200.newPacker();
        packer.pack(jf, fos1);
        jf.close();
        fos1.close();
        jf = new JarFile(jarFile);
        properties.put(Packer.EFFORT, "1");
        packer.pack(jf, fos2);
        jf.close();
        fos2.close();
        jf = new JarFile(jarFile);
        properties.put(Packer.EFFORT, "9");
        packer.pack(jf, fos3);
        jf.close();
        fos3.close();
        File jarFile1 = Support_Resources.createTempFile("jar_1");
        File jarFile2 = Support_Resources.createTempFile("jar_2");
        File jarFile3 = Support_Resources.createTempFile("jar_3");
        JarOutputStream jos1 = new JarOutputStream(new FileOutputStream(jarFile1));
        JarOutputStream jos2 = new JarOutputStream(new FileOutputStream(jarFile2));
        JarOutputStream jos3 = new JarOutputStream(new FileOutputStream(jarFile3));
        FileInputStream fis1 = new FileInputStream(packFile1);
        FileInputStream fis2 = new FileInputStream(packFile2);
        FileInputStream fis3 = new FileInputStream(packFile3);
        unpacker.unpack(fis1, jos1);
        unpacker.unpack(fis2, jos2);
        unpacker.unpack(fis3, jos3);
        jos1.close();
        jos2.close();
        jos3.close();
        assertEquals(jarFile1.length(), jarFile2.length());
        assertEquals(jarFile2.length(), jarFile3.length());
        assertEquals(jarEntries, new JarFile(jarFile1).size());
        assertEquals(jarEntries, new JarFile(jarFile2).size());
        assertEquals(jarEntries, new JarFile(jarFile3).size());
    }
    class MyPCL implements PropertyChangeListener {
        boolean flag = false;
        public boolean isCalled() {
            return flag;
        }
        public void propertyChange(PropertyChangeEvent arg0) {
            flag = true;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "addPropertyChangeListener",
        args = {java.beans.PropertyChangeListener.class}
    )
    @KnownFailure("No Implementation in Android!")
    public void testAddPropertyChangeListener() {
        MyPCL pcl = new MyPCL();
        unpacker.addPropertyChangeListener(pcl);
        assertFalse(pcl.isCalled());
        properties.put(Unpacker.PROGRESS, "0");
        assertTrue(pcl.isCalled());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "removePropertyChangeListener",
        args = {java.beans.PropertyChangeListener.class}
    )
    @KnownFailure("No Implementation in Android!")
    public void testRemovePropertyChangeListener() {
        MyPCL pcl = new MyPCL();
        unpacker.addPropertyChangeListener(pcl);
        assertFalse(pcl.isCalled());
        unpacker.removePropertyChangeListener(pcl);
        properties.put(Unpacker.PROGRESS, "7");
        assertFalse(pcl.isCalled());
    }
    @Override
    protected void setUp() {
        unpacker = Pack200.newUnpacker();
        properties = unpacker.properties();
    }
    @Override
    protected void tearDown() {
        unpacker = null;
        properties = null;
    }
}
