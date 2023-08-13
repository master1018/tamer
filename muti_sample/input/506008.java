public class ScriptRunner {
  private final Object scope;
  private final String variable;
  private ScriptRunner(Object scope, String variable) {
    this.scope = scope;
    this.variable = variable;
  }
  public static ScriptRunner newInstance(Object scope, String variable) {
    return new ScriptRunner(scope, variable);
  }
  public static void run(String scriptfilename) {
    try {
      initPython();
      PythonInterpreter python = new PythonInterpreter();
      python.execfile(scriptfilename);
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
  private static void initPython() {
    Properties props = new Properties();
    props.setProperty("python.verbose", "error");
    props.setProperty("python.path", System.getProperty("java.class.path"));
    PythonInterpreter.initialize(System.getProperties(), props, new String[] {""});
  }
  public void console() throws IOException {
    initPython();
    InteractiveConsole python = new InteractiveConsole();
    initInterpreter(python, scope, variable);
    python.interact();
  }
  public static void console(PyObject locals) {
    initPython();
    InteractiveConsole python = new InteractiveConsole(locals);
    python.interact();
  }
  public static void initInterpreter(PythonInterpreter python, Object scope, String variable) 
      throws IOException {
    python.set(variable, scope);
  }
}
