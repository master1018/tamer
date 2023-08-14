class SimpleClassLoader extends ClassLoader {
    public static int numFinalizers;
    SimpleClassLoader() {
        super(null);
    }
    protected Class findClass(String name) throws ClassNotFoundException {
        File f = new File(System.getProperty("test.classes"), name + ".class");
        InputStream fi = null;
        try {
            fi = new FileInputStream(f);
            int length = fi.available();
            byte[] bytes = new byte[length];
            fi.read(bytes, 0, length);
            return defineClass(name, bytes, 0, length);
        }
        catch (IOException exception) {
            throw new ClassNotFoundException(name, exception);
        }
        finally {
            if (null != fi) {
                try {
                    fi.close();
                } catch (IOException exception) {
                }
            }
        }
    }
    protected void finalize() throws Throwable {
        super.finalize();
        numFinalizers++;
    }
}
