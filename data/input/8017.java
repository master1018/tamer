public class SortMethodsTest {
  static String createClass(String name, int nrOfMethods) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    pw.println("public class " + name + "{");
    for (int i = 0; i < nrOfMethods; i++) {
      pw.println("  public void m" + i + "() {}");
    }
    pw.println("  public static String sayHello() {");
    pw.println("    return \"Hello from class \" + " + name +
               ".class.getName() + \" with \" + " + name +
               ".class.getDeclaredMethods().length + \" methods\";");
    pw.println("  }");
    pw.println("}");
    pw.close();
    return sw.toString();
  }
  public static void main(String args[]) {
    JavaCompiler comp = ToolProvider.getSystemJavaCompiler();
    DiagnosticCollector<JavaFileObject> diags = new DiagnosticCollector<JavaFileObject>();
    final String cName = new String("ManyMethodsClass");
    Vector<Long> results = new Vector<Long>();
    for (int i = 6; i < 600000; i*=10) {
      String klass =  createClass(cName, i);
      JavaMemoryFileObject file = new JavaMemoryFileObject(cName, klass);
      MemoryFileManager mfm = new MemoryFileManager(comp.getStandardFileManager(diags, null, null), file);
      CompilationTask task = comp.getTask(null, mfm, diags, null, null, Arrays.asList(file));
      if (task.call()) {
        try {
          MemoryClassLoader mcl = new MemoryClassLoader(file);
          long start = System.nanoTime();
          Class<? extends Object> c = Class.forName(cName, true, mcl);
          long end = System.nanoTime();
          results.add(end - start);
          Method m = c.getDeclaredMethod("sayHello", new Class[0]);
          String ret = (String)m.invoke(null, new Object[0]);
          System.out.println(ret + " (loaded and resloved in " + (end - start) + "ns)");
        } catch (Exception e) {
          System.err.println(e);
        }
      }
      else {
        System.out.println(klass);
        System.out.println();
        for (Diagnostic diag : diags.getDiagnostics()) {
          System.out.println(diag.getCode() + "\n" + diag.getKind() + "\n" + diag.getPosition());
          System.out.println(diag.getSource() + "\n" + diag.getMessage(null));
        }
      }
    }
    long lastRatio = 0;
    for (int i = 2; i < results.size(); i++) {
      long normalized1 = Math.max(results.get(i-1) - results.get(0), 1);
      long normalized2 = Math.max(results.get(i) - results.get(0), 1);
      long ratio = normalized2/normalized1;
      lastRatio = ratio;
      System.out.println("10 x more methods requires " + ratio + " x more time");
    }
    if (lastRatio > 80) {
      throw new RuntimeException("ATTENTION: it seems that class loading needs quadratic time with regard to the number of class methods!!!");
    }
  }
}
class JavaMemoryFileObject extends SimpleJavaFileObject {
  private final String code;
  private ByteArrayOutputStream byteCode;
  JavaMemoryFileObject(String name, String code) {
    super(URI.create("string:
    this.code = code;
  }
  @Override
  public CharSequence getCharContent(boolean ignoreEncodingErrors) {
    return code;
  }
  @Override
  public OutputStream openOutputStream() {
    byteCode = new ByteArrayOutputStream();
    return byteCode;
  }
  byte[] getByteCode() {
    return byteCode.toByteArray();
   }
}
class MemoryClassLoader extends ClassLoader {
  private final JavaMemoryFileObject jfo;
  public MemoryClassLoader(JavaMemoryFileObject jfo) {
    this.jfo = jfo;
  }
  public Class findClass(String name) {
    byte[] b = jfo.getByteCode();
    return defineClass(name, b, 0, b.length);
  }
}
class MemoryFileManager extends ForwardingJavaFileManager<JavaFileManager> {
  private final JavaFileObject jfo;
  public MemoryFileManager(StandardJavaFileManager jfm, JavaFileObject jfo) {
    super(jfm);
    this.jfo = jfo;
  }
  @Override
  public FileObject getFileForInput(Location location, String packageName,
                                    String relativeName) throws IOException {
    return jfo;
  }
  @Override
  public JavaFileObject getJavaFileForOutput(Location location, String qualifiedName,
                                             Kind kind, FileObject outputFile) throws IOException {
    return jfo;
  }
}
