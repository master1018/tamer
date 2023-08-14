public class ClassPathLoader extends ClassLoader
{
    private ClassPath classPath;
    public ClassPathLoader(ClassPath classPath) {
        this.classPath = classPath;
    }
    protected Class findClass(String name) throws ClassNotFoundException
    {
        byte[] b = loadClassData(name);
        return defineClass(name, b, 0, b.length);
    }
    private byte[] loadClassData(String className)
        throws ClassNotFoundException
    {
        String filename = className.replace('.', File.separatorChar)
                          + ".class";
        ClassFile classFile = classPath.getFile(filename);
        if (classFile != null) {
            Exception reportedError = null;
            byte data[] = null;
            try {
                DataInputStream input
                    = new DataInputStream(classFile.getInputStream());
                data = new byte[(int)classFile.length()];
                try {
                    input.readFully(data);
                } catch (IOException ex) {
                    data = null;
                    reportedError = ex;
                } finally {
                    try { input.close(); } catch (IOException ex) {}
                }
            } catch (IOException ex) {
                reportedError = ex;
            }
            if (data == null)
                throw new ClassNotFoundException(className, reportedError);
            return data;
        }
        throw new ClassNotFoundException(className);
    }
}
