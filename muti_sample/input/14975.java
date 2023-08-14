public class ProcImageClassLoader extends ClassLoader {
   public ProcImageClassLoader(ClassLoader parent) {
      super(parent);
   }
   public ProcImageClassLoader() {
      this(Thread.currentThread().getContextClassLoader());
   }
   protected Class findClass(String className) throws ClassNotFoundException {
      try {
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         InstanceKlass klass = SystemDictionaryHelper.findInstanceKlass(className);
         ClassWriter cw = new ClassWriter(klass, bos);
         cw.write();
         byte[] buf = bos.toByteArray();
         return defineClass(className, buf, 0, buf.length);
      } catch (Exception e) {
         throw (ClassNotFoundException) new ClassNotFoundException().initCause(e);
      }
   }
}
