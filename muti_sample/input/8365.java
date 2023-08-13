public class DirectoryLoader extends ClassLoader {
    private Hashtable cache;
    private File root;
    public DirectoryLoader (File rootDir) {
        cache = new Hashtable();
        if (rootDir == null || !rootDir.isDirectory()) {
            throw new IllegalArgumentException();
        }
        root = rootDir;
    }
    private DirectoryLoader () {}
    public Class loadClass(String className) throws ClassNotFoundException {
        return loadClass(className, true);
    }
    public synchronized Class loadClass(String className, boolean resolve)
        throws ClassNotFoundException {
        Class result;
        byte  classData[];
        result = (Class) cache.get(className);
        if (result == null) {
            try {
                result = super.findSystemClass(className);
            } catch (ClassNotFoundException e) {
                classData = getClassFileData(className);
                if (classData == null) {
                    throw new ClassNotFoundException();
                }
                result = defineClass(classData, 0, classData.length);
                if (result == null) {
                    throw new ClassFormatError();
                }
                if (resolve) resolveClass(result);
                cache.put(className, result);
            }
        }
        return result;
    }
    private byte[] getClassFileData (String className) {
        byte result[] = null;
        FileInputStream stream = null;
        File classFile = new File(root,className.replace('.',File.separatorChar) + ".class");
        try {
            stream = new FileInputStream(classFile);
            result = new byte[stream.available()];
            stream.read(result);
        } catch(ThreadDeath death) {
            throw death;
        } catch (Throwable e) {
        }
        finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch(ThreadDeath death) {
                    throw death;
                } catch (Throwable e) {
                }
            }
        }
        return result;
    }
}
