public class ScriptJConsolePlugin extends JConsolePlugin
                     implements ScriptShellPanel.CommandProcessor {
    private volatile ScriptShellPanel window;
    private Map<String, JPanel> tabs;
    private volatile ScriptEngine engine;
    private CountDownLatch engineReady = new CountDownLatch(1);
    private String extension;
    private volatile String prompt;
    public ScriptJConsolePlugin() {
    }
    @Override public Map<String, JPanel> getTabs() {
        createScriptEngine();
        window = new ScriptShellPanel(this);
        tabs = new HashMap<String, JPanel>();
        tabs.put("Script Shell", window);
        new Thread(new Runnable() {
            @Override
            public void run() {
                initScriptEngine();
                engineReady.countDown();
            }
        }).start();
        return tabs;
    }
    @Override public SwingWorker<?,?> newSwingWorker() {
        return null;
    }
    @Override public void dispose() {
        window.dispose();
    }
    @Override
    public String getPrompt() {
        return prompt;
    }
    @Override
    public String executeCommand(String cmd) {
        String res;
        try {
           engineReady.await();
           Object tmp = engine.eval(cmd);
           res = (tmp == null)? null : tmp.toString();
        } catch (InterruptedException ie) {
           res = ie.getMessage();
        } catch (ScriptException se) {
           res = se.getMessage();
        }
        return res;
    }
    private void createScriptEngine() {
        ScriptEngineManager manager = new ScriptEngineManager();
        String language = getScriptLanguage();
        engine = manager.getEngineByName(language);
        if (engine == null) {
            throw new RuntimeException("cannot load " + language + " engine");
        }
        extension = engine.getFactory().getExtensions().get(0);
        prompt = extension + ">";
        engine.setBindings(createBindings(), ScriptContext.ENGINE_SCOPE);
    }
    private static final String LANGUAGE_KEY = "com.sun.demo.jconsole.console.language";
    private String getScriptLanguage() {
        String lang = System.getProperty(LANGUAGE_KEY);
        if (lang == null) {
            lang = "JavaScript";
        }
        return lang;
    }
    private Bindings createBindings() {
        Map<String, Object> map =
                Collections.synchronizedMap(new HashMap<String, Object>());
        return new SimpleBindings(map);
    }
    private void initScriptEngine() {
        setGlobals();
        loadInitFile();
        loadUserInitFile();
    }
    private void setGlobals() {
        engine.put("engine", engine);
        engine.put("window", window);
        engine.put("plugin", this);
    }
    private void loadInitFile() {
        String oldFilename = (String) engine.get(ScriptEngine.FILENAME);
        engine.put(ScriptEngine.FILENAME, "<built-in jconsole." + extension + ">");
        try {
            Class<? extends ScriptJConsolePlugin> myClass = this.getClass();
            InputStream stream = myClass.getResourceAsStream("/resources/jconsole." +
                                       extension);
            if (stream != null) {
                engine.eval(new InputStreamReader(new BufferedInputStream(stream)));
            }
        } catch (Exception exp) {
            exp.printStackTrace();
        } finally {
            engine.put(ScriptEngine.FILENAME, oldFilename);
        }
    }
    private void loadUserInitFile() {
        String oldFilename = (String) engine.get(ScriptEngine.FILENAME);
        String home = System.getProperty("user.home");
        if (home == null) {
            return;
        }
        String fileName = home + File.separator + "jconsole." + extension;
        if (! (new File(fileName).exists())) {
            return;
        }
        engine.put(ScriptEngine.FILENAME, fileName);
        try {
            engine.eval(new FileReader(fileName));
        } catch (Exception exp) {
            exp.printStackTrace();
        } finally {
            engine.put(ScriptEngine.FILENAME, oldFilename);
        }
    }
}
