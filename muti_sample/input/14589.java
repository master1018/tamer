public abstract class JSJavaScriptEngine extends MapScriptObject {
    public void startConsole() {
      start(true);
    }
    public void start() {
      start(false);
    }
    public void defineFunction(Object target, Method method) {
      putFunction(target, method, false);
    }
    public Object call(String name, Object[] args) {
      Invocable invocable = (Invocable)engine;
      try {
        return invocable.invokeFunction(name, args);
      } catch (RuntimeException re) {
        throw re;
      } catch (Exception exp) {
        throw new RuntimeException(exp);
      }
    }
    public Object address(Object[] args) {
        if (args.length != 1) return UNDEFINED;
        Object o = args[0];
        if (o != null && o instanceof JSJavaObject) {
            return ((JSJavaObject)o).getOop().getHandle().toString();
        } else {
            return UNDEFINED;
        }
    }
    public Object classof(Object[] args) {
        if (args.length != 1) {
            return UNDEFINED;
        }
        Object o = args[0];
        if (o != null) {
            if (o instanceof JSJavaObject) {
                if (o instanceof JSJavaInstance) {
                    return ((JSJavaInstance)o).getJSJavaClass();
                } else if (o instanceof JSJavaArray) {
                    return ((JSJavaArray)o).getJSJavaClass();
                } else {
                    return UNDEFINED;
                }
            } else if (o instanceof String) {
                InstanceKlass ik = SystemDictionaryHelper.findInstanceKlass((String) o);
                return getJSJavaFactory().newJSJavaKlass(ik).getJSJavaClass();
            } else {
                return UNDEFINED;
            }
        } else {
            return UNDEFINED;
        }
    }
    public Object dumpClass(Object[] args) {
        if (args.length == 0) {
            return Boolean.FALSE;
        }
        Object clazz = args[0];
      if (clazz == null) {
          return Boolean.FALSE;
      }
        InstanceKlass ik = null;
        if (clazz instanceof String) {
            String name = (String) clazz;
            if (name.startsWith("0x")) {
                VM vm = VM.getVM();
                Address addr = vm.getDebugger().parseAddress(name);
                Oop oop = vm.getObjectHeap().newOop(addr.addOffsetToAsOopHandle(0));
                if (oop instanceof InstanceKlass) {
                    ik = (InstanceKlass) oop;
                } else {
                    return Boolean.FALSE;
                }
            } else {
                ik = SystemDictionaryHelper.findInstanceKlass((String) clazz);
            }
        } else if (clazz instanceof JSJavaClass) {
            JSJavaKlass jk = ((JSJavaClass)clazz).getJSJavaKlass();
            if (jk != null && jk instanceof JSJavaInstanceKlass) {
                ik = ((JSJavaInstanceKlass)jk).getInstanceKlass();
            }
        } else {
            return Boolean.FALSE;
        }
        if (ik == null) return Boolean.FALSE;
        StringBuffer buf = new StringBuffer();
        if (args.length > 1) {
            buf.append(args[1].toString());
        } else {
            buf.append('.');
        }
        buf.append(File.separatorChar);
        buf.append(ik.getName().asString().replace('/', File.separatorChar));
        buf.append(".class");
        String fileName = buf.toString();
        File file = new File(fileName);
        try {
            int index = fileName.lastIndexOf(File.separatorChar);
            File dir = new File(fileName.substring(0, index));
            dir.mkdirs();
            FileOutputStream fos = new FileOutputStream(file);
            ClassWriter cw = new ClassWriter(ik, fos);
            cw.write();
            fos.close();
        } catch (IOException exp) {
            printError(exp.toString(), exp);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
    public Object dumpHeap(Object[] args) {
        String fileName = "heap.bin";
        if (args.length > 0) {
            fileName = args[0].toString();
        }
        return new JMap().writeHeapHprofBin(fileName)? Boolean.TRUE: Boolean.FALSE;
    }
    public void help(Object[] args) {
        println("Function/Variable        Description");
        println("=================        ===========");
        println("address(jobject)         returns the address of the Java object");
        println("classof(jobject)         returns the class object of the Java object");
        println("dumpClass(jclass,[dir])  writes .class for the given Java Class");
        println("dumpHeap([file])         writes heap in hprof binary format");
        println("help()                   prints this help message");
        println("identityHash(jobject)    returns the hashCode of the Java object");
        println("mirror(jobject)          returns a local mirror of the Java object");
        println("load([file1, file2,...]) loads JavaScript file(s). With no files, reads <stdin>");
        println("object(string)           converts a string address into Java object");
        println("owner(jobject)           returns the owner thread of this monitor or null");
        println("sizeof(jobject)          returns the size of Java object in bytes");
        println("staticof(jclass, field)  returns a static field of the given Java class");
        println("read([prompt])           reads a single line from standard input");
        println("quit()                   quits the interactive load call");
        println("jvm                      the target jvm that is being debugged");
    }
    public Object identityHash(Object[] args) {
        if (args.length != 1) return UNDEFINED;
        Object o = args[0];
        if (o != null && o instanceof JSJavaObject) {
            return new Long(((JSJavaObject)o).getOop().identityHash());
        } else {
            return UNDEFINED;
        }
    }
    public void load(Object[] args) {
       for (int i = 0; i < args.length; i++) {
         processSource(args[i].toString());
       }
    }
    public Object mirror(Object[] args) {
        Object o = args[0];
        if (o != null && o instanceof JSJavaObject) {
            Oop oop = ((JSJavaObject)o).getOop();
            Object res = null;
            try {
                if (oop instanceof InstanceKlass) {
                    res = getObjectReader().readClass((InstanceKlass) oop);
                } else {
                    res = getObjectReader().readObject(oop);
                }
            } catch (Exception e) {
                if (debug) e.printStackTrace(getErrorStream());
            }
            return (res != null)? res : UNDEFINED;
        } else {
            return UNDEFINED;
        }
    }
    public Object owner(Object[] args) {
        Object o = args[0];
        if (o != null && o instanceof JSJavaObject) {
            return getOwningThread((JSJavaObject)o);
        } else {
            return UNDEFINED;
        }
    }
    public Object object(Object[] args) {
        Object o = args[0];
        if (o != null && o instanceof String) {
            VM vm = VM.getVM();
            Address addr = vm.getDebugger().parseAddress((String)o);
            Oop oop = vm.getObjectHeap().newOop(addr.addOffsetToAsOopHandle(0));
            return getJSJavaFactory().newJSJavaObject(oop);
        } else {
            return UNDEFINED;
        }
    }
    public Object sizeof(Object[] args) {
        if (args.length != 1) return UNDEFINED;
        Object o = args[0];
        if (o != null && o instanceof JSJavaObject) {
            return new Long(((JSJavaObject)o).getOop().getObjectSize());
        } else {
            return UNDEFINED;
        }
    }
    public Object staticof(Object[] args) {
        Object classname = args[0];
        Object fieldname = args[1];
        if (fieldname == null || classname == null ||
            !(fieldname instanceof String)) {
            return UNDEFINED;
        }
        InstanceKlass ik = null;
        if (classname instanceof JSJavaClass) {
            JSJavaClass jclass = (JSJavaClass) classname;
            JSJavaKlass jk = jclass.getJSJavaKlass();
            if (jk != null && jk instanceof JSJavaInstanceKlass) {
                ik = ((JSJavaInstanceKlass)jk).getInstanceKlass();
            }
        } else if (classname instanceof String) {
            ik = SystemDictionaryHelper.findInstanceKlass((String)classname);
        } else {
            return UNDEFINED;
        }
        if (ik == null) {
            return UNDEFINED;
        }
        JSJavaFactory factory = getJSJavaFactory();
        try {
            return ((JSJavaInstanceKlass) factory.newJSJavaKlass(ik)).getStaticFieldValue((String)fieldname);
        } catch (NoSuchFieldException e) {
            return UNDEFINED;
        }
    }
    public Object read(Object[] args) {
        BufferedReader in = getInputReader();
      if (in == null) {
        return null;
      }
        if (args.length > 0) {
          print(args[0].toString());
          print(":");
        }
        try {
          return in.readLine();
        } catch (IOException exp) {
        exp.printStackTrace();
          throw new RuntimeException(exp);
        }
    }
    public void quit(Object[] args) {
        quit();
    }
    public void writeln(Object[] args) {
      for (int i = 0; i < args.length; i++) {
        print(args[i].toString());
        print(" ");
      }
      println("");
    }
    public void write(Object[] args) {
      for (int i = 0; i < args.length; i++) {
        print(args[i].toString());
        print(" ");
      }
    }
    protected void start(boolean console) {
      ScriptContext context = engine.getContext();
      OutputStream out = getOutputStream();
      if (out != null) {
        context.setWriter(new PrintWriter(out));
      }
      OutputStream err = getErrorStream();
      if (err != null) {
        context.setErrorWriter(new PrintWriter(err));
      }
      loadInitFile();
      loadUserInitFile();
      JSJavaFactory fac = getJSJavaFactory();
      JSJavaVM jvm = (fac != null)? fac.newJSJavaVM() : null;
      call("main", new Object[] { this, jvm });
      if (console) {
        processSource(null);
      }
    }
    protected JSJavaScriptEngine(boolean debug) {
        this.debug = debug;
      ScriptEngineManager manager = new ScriptEngineManager();
      engine = manager.getEngineByName("javascript");
      if (engine == null) {
        throw new RuntimeException("can't load JavaScript engine");
      }
      Method[] methods = getClass().getMethods();
      for (int i = 0; i < methods.length; i++) {
        Method m = methods[i];
        if (! Modifier.isPublic(m.getModifiers())) {
          continue;
        }
        Class[] argTypes = m.getParameterTypes();
        if (argTypes.length == 1 &&
            argTypes[0] == Object[].class) {
          putFunction(this, m);
        }
      }
    }
    protected JSJavaScriptEngine() {
        this(false);
    }
    protected abstract ObjectReader getObjectReader();
    protected abstract JSJavaFactory getJSJavaFactory();
    protected void printPrompt(String str) {
        System.err.print(str);
        System.err.flush();
    }
    protected void loadInitFile() {
      InputStream is = JSJavaScriptEngine.class.getResourceAsStream("sa.js");
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      evalReader(reader, "sa.js");
    }
    protected void loadUserInitFile() {
        File initFile = new File(getUserInitFileDir(), getUserInitFileName());
        if (initFile.exists() && initFile.isFile()) {
          processSource(initFile.getAbsolutePath());
        }
    }
    protected String getUserInitFileDir() {
        return System.getProperty("user.home");
    }
    protected String getUserInitFileName() {
        return "jsdb.js";
    }
    protected BufferedReader getInputReader() {
      if (inReader == null) {
        inReader = new BufferedReader(new InputStreamReader(System.in));
      }
      return inReader;
    }
    protected PrintStream getOutputStream() {
      return System.out;
    }
    protected PrintStream getErrorStream() {
      return System.err;
    }
    protected void print(String name) {
      getOutputStream().print(name);
    }
    protected void println(String name) {
      getOutputStream().println(name);
    }
    protected void printError(String message) {
        printError(message, null);
    }
    protected void printError(String message, Exception exp) {
        getErrorStream().println(message);
        if (exp != null && debug) {
          exp.printStackTrace(getErrorStream());
        }
    }
    protected boolean isQuitting() {
        return quitting;
    }
    protected void quit() {
        quitting = true;
    }
    protected ScriptEngine getScriptEngine() {
      return engine;
    }
    private JSJavaThread getOwningThread(JSJavaObject jo) {
        Oop oop = jo.getOop();
        Mark mark = oop.getMark();
        ObjectMonitor mon = null;
      Address owner = null;
        JSJavaThread owningThread = null;
        if (! mark.hasMonitor()) {
            if (mark.hasLocker()) {
                owner = mark.locker().getAddress(); 
            }
        } else {
            mon = mark.monitor();
            owner = mon.owner();
        }
        if (owner != null) {
            JSJavaFactory factory = getJSJavaFactory();
            owningThread = (JSJavaThread) factory.newJSJavaThread(VM.getVM().getThreads().owningThreadFromMonitor(owner));
        }
        return owningThread;
    }
    private void processSource(String filename) {
        if (filename == null) {
            BufferedReader in = getInputReader();
            String sourceName = "<stdin>";
            int lineno = 0;
            boolean hitEOF = false;
            do {
                int startline = lineno;
                printPrompt("jsdb> ");
                Object source = read(EMPTY_ARRAY);
                if (source == null) {
                   hitEOF = true;
                   break;
                }
                lineno++;
                Object result = evalString(source.toString(), sourceName, startline);
                if (result != null) {
                    printError(result.toString());
                }
                if (isQuitting()) {
                    break;
                }
            } while (!hitEOF);
        } else {
            Reader in = null;
            try {
                in = new BufferedReader(new FileReader(filename));
                evalReader(in, filename);
            } catch (FileNotFoundException ex) {
                println("File '" + filename + "' not found");
                throw new RuntimeException(ex);
            }
        }
    }
    protected Object evalString(String source, String filename, int lineNum) {
       try {
         engine.put(ScriptEngine.FILENAME, filename);
         return engine.eval(source);
       } catch (ScriptException sexp) {
         printError(sexp.toString(), sexp);
         } catch (Exception exp) {
         printError(exp.toString(), exp);
       }
       return null;
    }
    private Object evalReader(Reader in, String filename) {
       try {
         engine.put(ScriptEngine.FILENAME, filename);
         return engine.eval(in);
       } catch (ScriptException sexp) {
         System.err.println(sexp);
         printError(sexp.toString(), sexp);
         } finally {
         try {
           in.close();
         } catch (IOException ioe) {
           printError(ioe.toString(), ioe);
         }
       }
       return null;
    }
    private BufferedReader inReader;
    protected final boolean debug;
    private boolean quitting;
    private ScriptEngine engine;
}
