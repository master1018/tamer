class ClassLoaderTarg extends ClassLoader {
    private Hashtable loaded = new Hashtable();
    String id;
    public ClassLoaderTarg(String id) {
        this.id = id;
    }
    private byte[] loadClassBytes(String cname)
                                         throws ClassNotFoundException {
        StringTokenizer stk = new StringTokenizer(System.getProperty("java.class.path"),
                                                  File.pathSeparator);
        while (stk.hasMoreTokens()) {
           File cfile = new File(stk.nextToken() + File.separator +
                                 cname + ".class");
           if (cfile.exists()) {
               System.out.println("loading from: " + cfile);
               return loadBytes(cfile, cname);
           } else {
               System.out.println("no file: " + cfile + " - trying next");
           }
        }
        throw new ClassNotFoundException(cname);
    }
    private byte[] loadBytes(File cfile, String cname)
                                         throws ClassNotFoundException {
        try {
            long fsize = cfile.length();
            if (fsize > 0) {
                FileInputStream in = new FileInputStream(cfile);
                byte[] cbytes = new byte[(int)fsize];
                in.read(cbytes);
                in.close();
                return cbytes;
            }
        } catch (IOException exc) {
        }
        throw new ClassNotFoundException(cname);
    }
    public synchronized Class findClass(String cname)
                                     throws ClassNotFoundException {
        Class klass = (Class)loaded.get(cname);
        if (klass == null) {
            byte[] cbytes = loadClassBytes(cname);
            klass = defineClass(cname, cbytes, 0, cbytes.length);
            loaded.put(cname, klass);
            System.err.println("ClassLoaderTarg (" + id +") loaded: " + cname);
        }
        return klass;
    }
    protected void finalize() {
        UnloadEventTarg.classLoaderFinalized(id);
        try {
            super.finalize();
        } catch (Throwable thrown) {
        }
    }
}
