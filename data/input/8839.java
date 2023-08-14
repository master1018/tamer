public class SerialVer extends Applet {
    GridBagLayout gb;
    TextField classname_t;
    Button show_b;
    TextField serialversion_t;
    Label footer_l;
    private static final long serialVersionUID = 7666909783837760853L;
    public synchronized void init() {
        gb = new GridBagLayout();
        setLayout(gb);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        Label l1 = new Label(Res.getText("FullClassName"));
        l1.setAlignment(Label.RIGHT);
        gb.setConstraints(l1, c);
        add(l1);
        classname_t = new TextField(20);
        c.gridwidth = GridBagConstraints.RELATIVE;
        c.weightx = 1.0;
        gb.setConstraints(classname_t, c);
        add(classname_t);
        show_b = new Button(Res.getText("Show"));
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 0.0;        
        gb.setConstraints(show_b, c);
        add(show_b);
        Label l2 = new Label(Res.getText("SerialVersion"));
        l2.setAlignment(Label.RIGHT);
        c.gridwidth = 1;
        gb.setConstraints(l2, c);
        add(l2);
        serialversion_t = new TextField(50);
        serialversion_t.setEditable(false);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gb.setConstraints(serialversion_t, c);
        add(serialversion_t);
        footer_l = new Label();
        c.gridwidth = GridBagConstraints.REMAINDER;
        gb.setConstraints(footer_l, c);
        add(footer_l);
        classname_t.requestFocus();
    }
    public void start() {
        classname_t.requestFocus();
    }
    public boolean action(Event ev, Object obj) {
        if (ev.target == classname_t) {
            show((String)ev.arg);
            return true;
        } else if (ev.target == show_b) {
            show(classname_t.getText());
            return true;
        }
        return false;
    }
    public boolean handleEvent(Event ev) {
        boolean rc = super.handleEvent(ev);
        return rc;
    }
    void show(String classname) {
        try {
            footer_l.setText(""); 
            serialversion_t.setText(""); 
            if (classname.equals("")) {
                return;
            }
            String s = serialSyntax(classname);
            if (s != null) {
                serialversion_t.setText(s);
            } else {
                footer_l.setText(Res.getText("NotSerializable", classname));
            }
        } catch (ClassNotFoundException cnf) {
            footer_l.setText(Res.getText("ClassNotFound", classname));
        }
    }
    static URLClassLoader loader = null;
    static void initializeLoader(String cp)
                                throws MalformedURLException, IOException {
        URL[] urls;
        StringTokenizer st = new StringTokenizer(cp, File.pathSeparator);
        int count = st.countTokens();
        urls = new URL[count];
        for (int i = 0; i < count; i++) {
            urls[i] = ParseUtil.fileToEncodedURL(
                new File(new File(st.nextToken()).getCanonicalPath()));
        }
        loader = new URLClassLoader(urls);
    }
    static String serialSyntax(String classname) throws ClassNotFoundException {
        String ret = null;
        boolean classFound = false;
        if (classname.indexOf('$') != -1) {
            ret = resolveClass(classname);
        } else {
            try {
                ret = resolveClass(classname);
                classFound = true;
            } catch (ClassNotFoundException e) {
            }
            if (!classFound) {
                StringBuffer workBuffer = new StringBuffer(classname);
                String workName = workBuffer.toString();
                int i;
                while ((i = workName.lastIndexOf('.')) != -1 && !classFound) {
                    workBuffer.setCharAt(i, '$');
                    try {
                        workName = workBuffer.toString();
                        ret = resolveClass(workName);
                        classFound = true;
                    } catch (ClassNotFoundException e) {
                    }
                }
            }
            if (!classFound) {
                throw new ClassNotFoundException();
            }
        }
        return ret;
    }
    static String resolveClass(String classname) throws ClassNotFoundException {
        Class cl = Class.forName(classname, false, loader);
        ObjectStreamClass desc = ObjectStreamClass.lookup(cl);
        if (desc != null) {
            return "    static final long serialVersionUID = " +
                desc.getSerialVersionUID() + "L;";
        } else {
            return null;
        }
    }
    public static void main(String[] args) {
        boolean show = false;
        String envcp = null;
        int i = 0;
        if (args.length == 0) {
            usage();
            System.exit(1);
        }
        for (i = 0; i < args.length; i++) {
            if (args[i].equals("-show")) {
                show = true;
            } else if (args[i].equals("-classpath")) {
                if ((i+1 == args.length) || args[i+1].startsWith("-")) {
                    System.err.println(Res.getText("error.missing.classpath"));
                    usage();
                    System.exit(1);
                }
                envcp = new String(args[i+1]);
                i++;
            }  else if (args[i].startsWith("-")) {
                System.err.println(Res.getText("invalid.flag", args[i]));
                usage();
                System.exit(1);
            } else {
                break;          
            }
        }
        if (envcp == null) {
            envcp = System.getProperty("env.class.path");
            if (envcp == null) {
                envcp = ".";
            }
        }
        try {
            initializeLoader(envcp);
        } catch (MalformedURLException mue) {
            System.err.println(Res.getText("error.parsing.classpath", envcp));
            System.exit(2);
        } catch (IOException ioe) {
            System.err.println(Res.getText("error.parsing.classpath", envcp));
            System.exit(3);
        }
        if (!show) {
            if (i == args.length) {
                usage();
                System.exit(1);
            }
            boolean exitFlag = false;
            for (i = i; i < args.length; i++ ) {
                try {
                    String syntax = serialSyntax(args[i]);
                    if (syntax != null)
                        System.out.println(args[i] + ":" + syntax);
                    else {
                        System.err.println(Res.getText("NotSerializable",
                            args[i]));
                        exitFlag = true;
                    }
                } catch (ClassNotFoundException cnf) {
                    System.err.println(Res.getText("ClassNotFound", args[i]));
                    exitFlag = true;
                }
            }
            if (exitFlag) {
                System.exit(1);
            }
        } else {
            if (i < args.length) {
                System.err.println(Res.getText("ignoring.classes"));
                System.exit(1);
            }
            Frame f =  new SerialVerFrame();
            SerialVer sv = new SerialVer();
            sv.init();
            f.add("Center", sv);
            f.pack();
            f.show();
        }
    }
    public static void usage() {
        System.err.println(Res.getText("usage"));
    }
}
class SerialVerFrame extends Frame {
    MenuBar menu_mb;
    Menu file_m;
    MenuItem exit_i;
    private static final long serialVersionUID = -7248105987187532533L;
    SerialVerFrame() {
        super(Res.getText("SerialVersionInspector"));
        file_m = new Menu(Res.getText("File"));
        file_m.add(exit_i = new MenuItem(Res.getText("Exit")));
        menu_mb = new MenuBar();
        menu_mb.add(file_m);
    }
    public boolean handleEvent(Event e) {
        if (e.id == Event.WINDOW_DESTROY) {
            exit(0);
        }
        return super.handleEvent(e);
    }
    public boolean action(Event ev, Object obj) {
        if (ev.target == exit_i) {
            exit(0);
        }
        return false;
    }
    void exit(int ret) {
        System.exit(ret);
    }
}
class Res {
    private static ResourceBundle messageRB;
    static void initResource() {
        try {
            messageRB =
                ResourceBundle.getBundle("sun.tools.serialver.resources.serialver");
        } catch (MissingResourceException e) {
            throw new Error("Fatal: Resource for serialver is missing");
        }
    }
    static String getText(String key) {
        return getText(key, (String)null);
    }
    static String getText(String key, String a1) {
        return getText(key, a1, null);
    }
    static String getText(String key, String a1, String a2) {
        return getText(key, a1, a2, null);
    }
    static String getText(String key, String a1, String a2, String a3) {
        if (messageRB == null) {
            initResource();
        }
        try {
            String message = messageRB.getString(key);
            String[] args = new String[3];
            args[0] = a1;
            args[1] = a2;
            args[2] = a3;
            return MessageFormat.format(message, args);
        } catch (MissingResourceException e) {
            throw new Error("Fatal: Resource for serialver is broken. There is no " + key + " key in resource.");
        }
    }
}
