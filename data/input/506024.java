public class MiscRegressionTest extends TestCase {
    @SmallTest
    public void testDefaultKeystore() {
        String type = KeyStore.getDefaultType();
        Assert.assertEquals("Default keystore type must be Bouncy Castle", "BKS", type);
        try {
            KeyStore store = KeyStore.getInstance(KeyStore.getDefaultType());
            Assert.assertNotNull("Keystore must not be null", store);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        try {
            KeyStore store = KeyStore.getInstance("BKS");
            Assert.assertNotNull("Keystore must not be null", store);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    @SmallTest
    public void testShortSerialization() throws Exception {
        String x = new String("serialize_foobar");
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        (new java.io.ObjectOutputStream(baos)).writeObject(x);
        ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(baos.toByteArray()));
        Class<ObjectInputStream> oClass = ObjectInputStream.class;
        Method m = oClass.getDeclaredMethod("setField", new Class[] { Object.class, Class.class, String.class, short.class});
        short start = 123;
        short origval = -1; 
        Short obj = new Short(start);
        Class<Short> declaringClass = Short.class;
        String fieldDescName = "value";
        assertEquals(obj.shortValue(), start);
        m.setAccessible(true); 
        m.invoke(ois, new Object[]{ obj, declaringClass, fieldDescName, new Short(origval)} );
        short res = obj.shortValue();
        assertEquals("Read and written values must be equal", origval, res);
    }
    @MediumTest
    public void testAndroidLogHandler() throws Exception {
        Logger.global.severe("This has logging Level.SEVERE, should become ERROR");
        Logger.global.warning("This has logging Level.WARNING, should become WARN");
        Logger.global.info("This has logging Level.INFO, should become INFO");
        Logger.global.config("This has logging Level.CONFIG, should become DEBUG");
        Logger.global.fine("This has logging Level.FINE, should become VERBOSE");
        Logger.global.finer("This has logging Level.FINER, should become VERBOSE");
        Logger.global.finest("This has logging Level.FINEST, should become VERBOSE");
    }
    @MediumTest
    public void testJavaContextClassLoader() throws Exception {
        Assert.assertNotNull("Must hava a Java context ClassLoader",
                             Thread.currentThread().getContextClassLoader());
    }
    @SmallTest
    public void testMethodToString() {
        try {
            Method m1 = Object.class.getMethod("notify", new Class[] { });
            Method m2 = Object.class.getMethod("toString", new Class[] { });
            Method m3 = Object.class.getMethod("wait", new Class[] { long.class, int.class });
            Method m4 = Object.class.getMethod("equals", new Class[] { Object.class });
            Method m5 = String.class.getMethod("valueOf", new Class[] { char[].class });
            Method m6 = Runtime.class.getMethod("exec", new Class[] { String[].class });
            assertEquals("Method.toString() must match expectations",
                    "public final native void java.lang.Object.notify()",
                    m1.toString());
            assertEquals("Method.toString() must match expectations",
                    "public java.lang.String java.lang.Object.toString()",
                    m2.toString());
            assertEquals("Method.toString() must match expectations",
                    "public final native void java.lang.Object.wait(long,int) throws java.lang.InterruptedException",
                    m3.toString());
            assertEquals("Method.toString() must match expectations",
                    "public boolean java.lang.Object.equals(java.lang.Object)",
                    m4.toString());
            assertEquals("Method.toString() must match expectations",
                    "public static java.lang.String java.lang.String.valueOf(char[])",
                    m5.toString());
            assertEquals("Method.toString() must match expectations",
                    "public java.lang.Process java.lang.Runtime.exec(java.lang.String[]) throws java.io.IOException",
                    m6.toString());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    enum TrafficLights {
        RED,
        YELLOW {},
        GREEN {
            @SuppressWarnings("unused")
            int i;
            @SuppressWarnings("unused")
            void foobar() {}
        };
    }
    @SmallTest
    public void testClassIsEnum() {
        Class<?> trafficClass = TrafficLights.class;
        Class<?> redClass = TrafficLights.RED.getClass();
        Class<?> yellowClass = TrafficLights.YELLOW.getClass();
        Class<?> greenClass = TrafficLights.GREEN.getClass();
        Assert.assertSame("Classes must be equal", trafficClass, redClass);
        Assert.assertNotSame("Classes must be different", trafficClass, yellowClass);
        Assert.assertNotSame("Classes must be different", trafficClass, greenClass);
        Assert.assertNotSame("Classes must be different", yellowClass, greenClass);
        Assert.assertTrue("Must be an enum", trafficClass.isEnum());
        Assert.assertTrue("Must be an enum", redClass.isEnum());
        Assert.assertFalse("Must not be an enum", yellowClass.isEnum());
        Assert.assertFalse("Must not be an enum", greenClass.isEnum());
        Assert.assertNotNull("Must have enum constants", trafficClass.getEnumConstants());
        Assert.assertNull("Must not have enum constants", yellowClass.getEnumConstants());
        Assert.assertNull("Must not have enum constants", greenClass.getEnumConstants());
    }
    public void checkJarCertificates(File file) {
        try {
            JarFile jarFile = new JarFile(file);
            JarEntry je = jarFile.getJarEntry("AndroidManifest.xml");
            byte[] readBuffer = new byte[1024];
            long t0 = System.currentTimeMillis();
            InputStream is = jarFile.getInputStream(je);
            while (is.read(readBuffer, 0, readBuffer.length) != -1) {
            }
            is.close();
            Certificate[] certs = je != null ? je.getCertificates() : null;
            long t1 = System.currentTimeMillis();
            android.util.Log.d("TestHarness", "loadCertificates() took " + (t1 - t0) + " ms");
            if (certs == null) {
                android.util.Log.d("TestHarness", "We have no certificates");
            } else {
                android.util.Log.d("TestHarness", "We have " + certs.length + " certificates");
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    @LargeTest
    public void testJarCertificates() {
        File[] files = new File("/system/app").listFiles();
        for (int i = 0; i < files.length; i++) {
            checkJarCertificates(files[i]);
        }
    }
    private static final long MY_LONG = 5073258162644648461L;
    @SmallTest
    public void testLongFieldReflection() {
        try {
            Field field = getClass().getDeclaredField("MY_LONG");
            assertEquals(5073258162644648461L, field.getLong(null));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    @SmallTest
    public void testLinkedHashMap() {
        LinkedHashMap map = new LinkedHashMap<String, String>(10, 0.75f, true);
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("key3", "value3");
        Iterator iterator = map.keySet().iterator();
        String id = (String) iterator.next();
        map.get(id);
        try {
            iterator.next();
            fail("expected ConcurrentModificationException was not thrown.");
        } catch(ConcurrentModificationException e) {
        }
        LinkedHashMap mapClone = (LinkedHashMap) map.clone();
        iterator = map.keySet().iterator();
        id = (String) iterator.next();
        mapClone.get(id);
        try {
            iterator.next();
        } catch(ConcurrentModificationException e) {
            fail("expected ConcurrentModificationException was not thrown.");
        }
    }
    @LargeTest
    public void testZipStressManifest() {
        android.util.Log.d("MiscRegressionTest", "ZIP stress test started");
        long time0 = System.currentTimeMillis();
        try {
            File[] files = new File("/system/app").listFiles();
            byte[] buffer = new byte[512];
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    android.util.Log.d("MiscRegressionTest",
                            "ZIP stress test processing " + files[i] + "...");
                    ZipFile zip = new ZipFile(files[i]);
                    ZipEntry entry = zip.getEntry("AndroidManifest.xml");
                    InputStream stream = zip.getInputStream(entry);
                    int j = stream.read(buffer);
                    while (j != -1) {
                        j = stream.read(buffer);
                    }
                    stream.close();
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        long time1 = System.currentTimeMillis();
        android.util.Log.d("MiscRegressionTest", "ZIP stress test finished, " +
                "time was " + (time1- time0) + "ms");
    }
    @LargeTest
    public void testZipStressAllFiles() {
        android.util.Log.d("MiscRegressionTest", "ZIP stress test started");
        long time0 = System.currentTimeMillis();
        try {
            File[] files = new File("/system/app").listFiles();
            byte[] buffer = new byte[512];
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    android.util.Log.d("MiscRegressionTest",
                            "ZIP stress test processing " + files[i] + "...");
                    ZipFile zip = new ZipFile(files[i]);
                    Enumeration<? extends ZipEntry> entries = zip.entries();
                    while (entries.hasMoreElements()) {
                        InputStream stream = zip.getInputStream(entries.nextElement());
                        int j = stream.read(buffer);
                        while (j != -1) {
                            j = stream.read(buffer);
                        }
                        stream.close();
                    }
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        long time1 = System.currentTimeMillis();
        android.util.Log.d("MiscRegressionTest", "ZIP stress test finished, " +
                "time was " + (time1- time0) + "ms");
    }
    @SmallTest
    public void testOsEncodingProperty() {
        long time0 = System.currentTimeMillis();
        String[] files = new File("/system/app").list();
        long time1 = System.currentTimeMillis();
        android.util.Log.d("MiscRegressionTest", "File.list() test finished, " +
                "time was " + (time1- time0) + "ms");
    }
    private void assertEquals(byte[] a, byte[] b) {
        assertEquals("Arrays must have same length", a.length, b.length);
        for (int i = 0; i < a.length; i++) {
            assertEquals("Array elements #" + i + " must be equal", a[i], b[i]);
        }
    }
    @LargeTest
    public void testZipDeflateInflateStress() {
        final int DATA_SIZE = 16384;
        Random random = new Random(42); 
        try {
            for (int j = 1; j <=2 ; j++) {
                byte[] input = new byte[DATA_SIZE];
                if (j == 1) {
                    random.nextBytes(input);
                } else {
                    int pos = 0;
                    while (pos < input.length) {
                        byte what = (byte)random.nextInt(256);
                        int howMany = random.nextInt(32);
                        if (pos + howMany >= input.length) {
                            howMany = input.length - pos;
                        }
                        Arrays.fill(input, pos, pos + howMany, what);
                        pos += howMany;
                    }
                }
                for (int i = 1; i <= 9; i++) {
                    android.util.Log.d("MiscRegressionTest", "ZipDeflateInflateStress test (" + j + "," + i + ")...");
                    byte[] zipped = new byte[2 * DATA_SIZE]; 
                    Deflater deflater = new Deflater(i);
                    deflater.setInput(input);
                    deflater.finish();
                    deflater.deflate(zipped);
                    byte[] output = new byte[DATA_SIZE];
                    Inflater inflater = new Inflater();
                    inflater.setInput(zipped);
                    inflater.finished();
                    inflater.inflate(output);
                    assertEquals(input, output);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    class MyThread extends Thread {
        public MyThread(String name) {
            super(name);
        }
        @Override
        public void run() {
            doSomething();
        }
        public void doSomething() {
            for (int i = 0; i < 20;) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }
            }
        }
    }
    class MyOtherThread extends Thread {
        public int visibleTraces;
        public MyOtherThread(ThreadGroup group, String name) {
            super(group, name);
        }
        @Override
        public void run() {
            visibleTraces = Thread.getAllStackTraces().size();
        }
    }
    @LargeTest
    public void testThreadGetStackTrace() {
        MyThread t1 = new MyThread("t1");
        t1.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
        }
        StackTraceElement[] traces = t1.getStackTrace();
        StackTraceElement trace = traces[traces.length - 2];
        assertTrue("Must find MyThread.doSomething in trace",
                trace.getClassName().endsWith("$MyThread") && 
                trace.getMethodName().equals("doSomething"));
        ThreadGroup g1 = new ThreadGroup("1");
        MyOtherThread t2 = new MyOtherThread(g1, "t2");
        t2.start();
        try {
            t2.join();
        } catch (InterruptedException ex) {
        }
        assertTrue("Must have traces for all threads", t2.visibleTraces > 1);
    }
}
