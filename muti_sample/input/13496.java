public class ConfigFile extends javax.security.auth.login.Configuration {
    private StreamTokenizer st;
    private int lookahead;
    private int linenum;
    private HashMap<String, LinkedList<AppConfigurationEntry>> configuration;
    private boolean expandProp = true;
    private URL url;
    private static Debug debugConfig = Debug.getInstance("configfile");
    private static Debug debugParser = Debug.getInstance("configparser");
    public ConfigFile() {
        try {
            init(url);
        } catch (IOException ioe) {
            throw (SecurityException)
                new SecurityException(ioe.getMessage()).initCause(ioe);
        }
    }
    public ConfigFile(URI uri) {
        try {
            url = uri.toURL();
            init(url);
        } catch (MalformedURLException mue) {
            throw (SecurityException)
                new SecurityException(mue.getMessage()).initCause(mue);
        } catch (IOException ioe) {
            throw (SecurityException)
                new SecurityException(ioe.getMessage()).initCause(ioe);
        }
    }
    private void init(URL url) throws IOException {
        boolean initialized = false;
        FileReader fr = null;
        String sep = File.separator;
        if ("false".equals(System.getProperty("policy.expandProperties"))) {
            expandProp = false;
        }
        HashMap<String, LinkedList<AppConfigurationEntry>> newConfig =
                new HashMap<>();
        if (url != null) {
            if (debugConfig != null) {
                debugConfig.println("reading " + url);
            }
            init(url, newConfig);
            configuration = newConfig;
            return;
        }
        String allowSys = java.security.Security.getProperty
                                                ("policy.allowSystemProperty");
        if ("true".equalsIgnoreCase(allowSys)) {
            String extra_config = System.getProperty
                                        ("java.security.auth.login.config");
            if (extra_config != null) {
                boolean overrideAll = false;
                if (extra_config.startsWith("=")) {
                    overrideAll = true;
                    extra_config = extra_config.substring(1);
                }
                try {
                    extra_config = PropertyExpander.expand(extra_config);
                } catch (PropertyExpander.ExpandException peee) {
                    MessageFormat form = new MessageFormat
                        (ResourcesMgr.getString
                                ("Unable.to.properly.expand.config",
                                "sun.security.util.AuthResources"));
                    Object[] source = {extra_config};
                    throw new IOException(form.format(source));
                }
                URL configURL = null;
                try {
                    configURL = new URL(extra_config);
                } catch (java.net.MalformedURLException mue) {
                    File configFile = new File(extra_config);
                    if (configFile.exists()) {
                        configURL = configFile.toURI().toURL();
                    } else {
                        MessageFormat form = new MessageFormat
                            (ResourcesMgr.getString
                                ("extra.config.No.such.file.or.directory.",
                                "sun.security.util.AuthResources"));
                        Object[] source = {extra_config};
                        throw new IOException(form.format(source));
                    }
                }
                if (debugConfig != null) {
                    debugConfig.println("reading "+configURL);
                }
                init(configURL, newConfig);
                initialized = true;
                if (overrideAll) {
                    if (debugConfig != null) {
                        debugConfig.println("overriding other policies!");
                    }
                    configuration = newConfig;
                    return;
                }
            }
        }
        int n = 1;
        String config_url;
        while ((config_url = java.security.Security.getProperty
                                        ("login.config.url."+n)) != null) {
            try {
                config_url = PropertyExpander.expand
                        (config_url).replace(File.separatorChar, '/');
                if (debugConfig != null) {
                    debugConfig.println("\tReading config: " + config_url);
                }
                init(new URL(config_url), newConfig);
                initialized = true;
            } catch (PropertyExpander.ExpandException peee) {
                MessageFormat form = new MessageFormat
                        (ResourcesMgr.getString
                                ("Unable.to.properly.expand.config",
                                "sun.security.util.AuthResources"));
                Object[] source = {config_url};
                throw new IOException(form.format(source));
            }
            n++;
        }
        if (initialized == false && n == 1 && config_url == null) {
            if (debugConfig != null) {
                debugConfig.println("\tReading Policy " +
                                "from ~/.java.login.config");
            }
            config_url = System.getProperty("user.home");
            String userConfigFile = config_url +
                      File.separatorChar + ".java.login.config";
            if (new File(userConfigFile).exists()) {
                init(new File(userConfigFile).toURI().toURL(),
                    newConfig);
            }
        }
        configuration = newConfig;
    }
    private void init(URL config,
        HashMap<String, LinkedList<AppConfigurationEntry>> newConfig)
        throws IOException {
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(getInputStream(config), "UTF-8");
            readConfig(isr, newConfig);
        } catch (FileNotFoundException fnfe) {
            if (debugConfig != null) {
                debugConfig.println(fnfe.toString());
            }
            throw new IOException(ResourcesMgr.getString
                    ("Configuration.Error.No.such.file.or.directory",
                    "sun.security.util.AuthResources"));
        } finally {
            if (isr != null) {
                isr.close();
            }
        }
    }
    public AppConfigurationEntry[] getAppConfigurationEntry
    (String applicationName) {
        LinkedList<AppConfigurationEntry> list = null;
        synchronized (configuration) {
            list = configuration.get(applicationName);
        }
        if (list == null || list.size() == 0)
            return null;
        AppConfigurationEntry[] entries =
                                new AppConfigurationEntry[list.size()];
        Iterator<AppConfigurationEntry> iterator = list.iterator();
        for (int i = 0; iterator.hasNext(); i++) {
            AppConfigurationEntry e = iterator.next();
            entries[i] = new AppConfigurationEntry(e.getLoginModuleName(),
                                                e.getControlFlag(),
                                                e.getOptions());
        }
        return entries;
    }
    public synchronized void refresh() {
        java.lang.SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(new AuthPermission("refreshLoginConfiguration"));
        java.security.AccessController.doPrivileged
            (new java.security.PrivilegedAction<Void>() {
            public Void run() {
                try {
                    init(url);
                } catch (java.io.IOException ioe) {
                    throw (SecurityException) new SecurityException
                                (ioe.getLocalizedMessage()).initCause(ioe);
                }
                return null;
            }
        });
    }
    private void readConfig(Reader reader,
        HashMap<String, LinkedList<AppConfigurationEntry>> newConfig)
        throws IOException {
        int linenum = 1;
        if (!(reader instanceof BufferedReader))
            reader = new BufferedReader(reader);
        st = new StreamTokenizer(reader);
        st.quoteChar('"');
        st.wordChars('$', '$');
        st.wordChars('_', '_');
        st.wordChars('-', '-');
        st.lowerCaseMode(false);
        st.slashSlashComments(true);
        st.slashStarComments(true);
        st.eolIsSignificant(true);
        lookahead = nextToken();
        while (lookahead != StreamTokenizer.TT_EOF) {
            parseLoginEntry(newConfig);
        }
    }
    private void parseLoginEntry(
        HashMap<String, LinkedList<AppConfigurationEntry>> newConfig)
        throws IOException {
        String appName;
        String moduleClass;
        String sflag;
        AppConfigurationEntry.LoginModuleControlFlag controlFlag;
        LinkedList<AppConfigurationEntry> configEntries = new LinkedList<>();
        appName = st.sval;
        lookahead = nextToken();
        if (debugParser != null) {
            debugParser.println("\tReading next config entry: " + appName);
        }
        match("{");
        while (peek("}") == false) {
            moduleClass = match("module class name");
            sflag = match("controlFlag");
            if (sflag.equalsIgnoreCase("REQUIRED"))
                controlFlag =
                        AppConfigurationEntry.LoginModuleControlFlag.REQUIRED;
            else if (sflag.equalsIgnoreCase("REQUISITE"))
                controlFlag =
                        AppConfigurationEntry.LoginModuleControlFlag.REQUISITE;
            else if (sflag.equalsIgnoreCase("SUFFICIENT"))
                controlFlag =
                        AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT;
            else if (sflag.equalsIgnoreCase("OPTIONAL"))
                controlFlag =
                        AppConfigurationEntry.LoginModuleControlFlag.OPTIONAL;
            else {
                MessageFormat form = new MessageFormat(ResourcesMgr.getString
                        ("Configuration.Error.Invalid.control.flag.flag",
                        "sun.security.util.AuthResources"));
                Object[] source = {sflag};
                throw new IOException(form.format(source));
            }
            HashMap<String, String> options = new HashMap<>();
            String key;
            String value;
            while (peek(";") == false) {
                key = match("option key");
                match("=");
                try {
                    value = expand(match("option value"));
                } catch (PropertyExpander.ExpandException peee) {
                    throw new IOException(peee.getLocalizedMessage());
                }
                options.put(key, value);
            }
            lookahead = nextToken();
            if (debugParser != null) {
                debugParser.println("\t\t" + moduleClass + ", " + sflag);
                java.util.Iterator<String> i = options.keySet().iterator();
                while (i.hasNext()) {
                    key = i.next();
                    debugParser.println("\t\t\t" +
                                        key +
                                        "=" +
                                        options.get(key));
                }
            }
            AppConfigurationEntry entry = new AppConfigurationEntry
                                                        (moduleClass,
                                                        controlFlag,
                                                        options);
            configEntries.add(entry);
        }
        match("}");
        match(";");
        if (newConfig.containsKey(appName)) {
            MessageFormat form = new MessageFormat(ResourcesMgr.getString
                ("Configuration.Error.Can.not.specify.multiple.entries.for.appName",
                "sun.security.util.AuthResources"));
            Object[] source = {appName};
            throw new IOException(form.format(source));
        }
        newConfig.put(appName, configEntries);
    }
    private String match(String expect) throws IOException {
        String value = null;
        switch(lookahead) {
        case StreamTokenizer.TT_EOF:
            MessageFormat form1 = new MessageFormat(ResourcesMgr.getString
                ("Configuration.Error.expected.expect.read.end.of.file.",
                "sun.security.util.AuthResources"));
            Object[] source1 = {expect};
            throw new IOException(form1.format(source1));
        case '"':
        case StreamTokenizer.TT_WORD:
            if (expect.equalsIgnoreCase("module class name") ||
                expect.equalsIgnoreCase("controlFlag") ||
                expect.equalsIgnoreCase("option key") ||
                expect.equalsIgnoreCase("option value")) {
                value = st.sval;
                lookahead = nextToken();
            } else {
                MessageFormat form = new MessageFormat(ResourcesMgr.getString
                        ("Configuration.Error.Line.line.expected.expect.found.value.",
                        "sun.security.util.AuthResources"));
                Object[] source = {new Integer(linenum), expect, st.sval};
                throw new IOException(form.format(source));
            }
            break;
        case '{':
            if (expect.equalsIgnoreCase("{")) {
                lookahead = nextToken();
            } else {
                MessageFormat form = new MessageFormat(ResourcesMgr.getString
                        ("Configuration.Error.Line.line.expected.expect.",
                        "sun.security.util.AuthResources"));
                Object[] source = {new Integer(linenum), expect, st.sval};
                throw new IOException(form.format(source));
            }
            break;
        case ';':
            if (expect.equalsIgnoreCase(";")) {
                lookahead = nextToken();
            } else {
                MessageFormat form = new MessageFormat(ResourcesMgr.getString
                        ("Configuration.Error.Line.line.expected.expect.",
                        "sun.security.util.AuthResources"));
                Object[] source = {new Integer(linenum), expect, st.sval};
                throw new IOException(form.format(source));
            }
            break;
        case '}':
            if (expect.equalsIgnoreCase("}")) {
                lookahead = nextToken();
            } else {
                MessageFormat form = new MessageFormat(ResourcesMgr.getString
                        ("Configuration.Error.Line.line.expected.expect.",
                        "sun.security.util.AuthResources"));
                Object[] source = {new Integer(linenum), expect, st.sval};
                throw new IOException(form.format(source));
            }
            break;
        case '=':
            if (expect.equalsIgnoreCase("=")) {
                lookahead = nextToken();
            } else {
                MessageFormat form = new MessageFormat(ResourcesMgr.getString
                        ("Configuration.Error.Line.line.expected.expect.",
                        "sun.security.util.AuthResources"));
                Object[] source = {new Integer(linenum), expect, st.sval};
                throw new IOException(form.format(source));
            }
            break;
        default:
            MessageFormat form = new MessageFormat(ResourcesMgr.getString
                        ("Configuration.Error.Line.line.expected.expect.found.value.",
                        "sun.security.util.AuthResources"));
            Object[] source = {new Integer(linenum), expect, st.sval};
            throw new IOException(form.format(source));
        }
        return value;
    }
    private boolean peek(String expect) {
        boolean found = false;
        switch (lookahead) {
        case ',':
            if (expect.equalsIgnoreCase(","))
                found = true;
            break;
        case ';':
            if (expect.equalsIgnoreCase(";"))
                found = true;
            break;
        case '{':
            if (expect.equalsIgnoreCase("{"))
                found = true;
            break;
        case '}':
            if (expect.equalsIgnoreCase("}"))
                found = true;
            break;
        default:
        }
        return found;
    }
    private int nextToken() throws IOException {
        int tok;
        while ((tok = st.nextToken()) == StreamTokenizer.TT_EOL) {
            linenum++;
        }
        return tok;
    }
    private InputStream getInputStream(URL url) throws IOException {
        if ("file".equalsIgnoreCase(url.getProtocol())) {
            try {
                return url.openStream();
            } catch (Exception e) {
                String file = url.getPath();
                if (url.getHost().length() > 0) {  
                    file = "
                }
                if (debugConfig != null) {
                    debugConfig.println("cannot read " + url +
                            ", try " + file);
                }
                return new FileInputStream(file);
            }
        } else {
            return url.openStream();
        }
    }
    private String expand(String value)
        throws PropertyExpander.ExpandException, IOException {
        if ("".equals(value)) {
            return value;
        }
        if (expandProp) {
            String s = PropertyExpander.expand(value);
            if (s == null || s.length() == 0) {
                MessageFormat form = new MessageFormat(ResourcesMgr.getString
                        ("Configuration.Error.Line.line.system.property.value.expanded.to.empty.value",
                        "sun.security.util.AuthResources"));
                Object[] source = {new Integer(linenum), value};
                throw new IOException(form.format(source));
            }
            return s;
        } else {
            return value;
        }
    }
}
