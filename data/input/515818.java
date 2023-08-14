@TestTargetClass(Pack200.Packer.class)
public class Pack200PackerTest extends TestCase {
    Packer packer;
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
        method = "pack",
        args = {java.util.jar.JarFile.class, java.io.OutputStream.class}
    )
    @KnownFailure("No Implementation in Android!")
    public void testPackJarFileOutputStream() throws IOException {
        File resources = Support_Resources.createTempFolder();
        Support_Resources.copyFile(resources, null, "junit4-4.3.1.jar");
        File jarFile = new File(resources, "junit4-4.3.1.jar");
        JarFile jf = new JarFile(jarFile);
        File packFile1 = Support_Resources.createTempFile("pack200_1");
        File packFile2 = Support_Resources.createTempFile("pack200_2");
        File packFile3 = Support_Resources.createTempFile("pack200_3");
        FileOutputStream fos1 = new FileOutputStream(packFile1);
        FileOutputStream fos2 = new FileOutputStream(packFile2);
        FileOutputStream fos3 = new FileOutputStream(packFile3);
        properties.put(Packer.EFFORT, "0");
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
        assertTrue(jarFile.length()!=packFile1.length());
        assertTrue(packFile1.length()>packFile2.length());
        assertTrue(packFile2.length()>packFile3.length());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "pack",
        args = {java.util.jar.JarInputStream.class, java.io.OutputStream.class}
    )
    @KnownFailure("No Implementation in Android!")
    public void testPackJarInputStreamOutputStream() throws IOException {
        File resources = Support_Resources.createTempFolder();
        Support_Resources.copyFile(resources, null, "junit4-4.3.1.jar");
        File jarFile = new File(resources, "junit4-4.3.1.jar");
        JarInputStream jis = new JarInputStream(new FileInputStream(jarFile));
        File packFile1 = Support_Resources.createTempFile("pack200_1");
        File packFile2 = Support_Resources.createTempFile("pack200_2");
        File packFile3 = Support_Resources.createTempFile("pack200_3");
        FileOutputStream fos1 = new FileOutputStream(packFile1);
        FileOutputStream fos2 = new FileOutputStream(packFile2);
        FileOutputStream fos3 = new FileOutputStream(packFile3);
        properties.put(Packer.EFFORT, "0");
        packer.pack(jis, fos1);
        fos1.close();
        jis = new JarInputStream(new FileInputStream(jarFile));
        properties.put(Packer.EFFORT, "1");
        packer.pack(jis, fos2);
        fos2.close();
        jis = new JarInputStream(new FileInputStream(jarFile));
        properties.put(Packer.EFFORT, "9");
        packer.pack(jis, fos3);
        fos3.close();
        assertTrue(jarFile.length()!=packFile1.length());
        assertTrue(packFile1.length()>packFile2.length());
        assertTrue(packFile2.length()>packFile3.length());
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
        packer.addPropertyChangeListener(pcl);
        assertFalse(pcl.isCalled());
        properties.put(Packer.EFFORT, "7");
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
        packer.addPropertyChangeListener(pcl);
        assertFalse(pcl.isCalled());
        packer.removePropertyChangeListener(pcl);
        properties.put(Packer.EFFORT, "7");
        assertFalse(pcl.isCalled());
    }
    @Override
    protected void setUp() {
        packer = Pack200.newPacker();
        properties = packer.properties();
    }
    @Override
    protected void tearDown() {
        packer = null;
        properties = null;
    }
}
