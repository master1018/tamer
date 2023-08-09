public final class KeyTool {
    private boolean debug = false;
    private Command command = null;
    private String sigAlgName = null;
    private String keyAlgName = null;
    private boolean verbose = false;
    private int keysize = -1;
    private boolean rfc = false;
    private long validity = (long)90;
    private String alias = null;
    private String dname = null;
    private String dest = null;
    private String filename = null;
    private String infilename = null;
    private String outfilename = null;
    private String srcksfname = null;
    private Set<Pair <String, String>> providers = null;
    private String storetype = null;
    private String srcProviderName = null;
    private String providerName = null;
    private String pathlist = null;
    private char[] storePass = null;
    private char[] storePassNew = null;
    private char[] keyPass = null;
    private char[] keyPassNew = null;
    private char[] newPass = null;
    private char[] destKeyPass = null;
    private char[] srckeyPass = null;
    private String ksfname = null;
    private File ksfile = null;
    private InputStream ksStream = null; 
    private String sslserver = null;
    private String jarfile = null;
    private KeyStore keyStore = null;
    private boolean token = false;
    private boolean nullStream = false;
    private boolean kssave = false;
    private boolean noprompt = false;
    private boolean trustcacerts = false;
    private boolean protectedPath = false;
    private boolean srcprotectedPath = false;
    private CertificateFactory cf = null;
    private KeyStore caks = null; 
    private char[] srcstorePass = null;
    private String srcstoretype = null;
    private Set<char[]> passwords = new HashSet<>();
    private String startDate = null;
    private List<String> ids = new ArrayList<>();   
    private List<String> v3ext = new ArrayList<>();
    enum Command {
        CERTREQ("Generates.a.certificate.request",
            ALIAS, SIGALG, FILEOUT, KEYPASS, KEYSTORE, DNAME,
            STOREPASS, STORETYPE, PROVIDERNAME, PROVIDERCLASS,
            PROVIDERARG, PROVIDERPATH, V, PROTECTED),
        CHANGEALIAS("Changes.an.entry.s.alias",
            ALIAS, DESTALIAS, KEYPASS, KEYSTORE, STOREPASS,
            STORETYPE, PROVIDERNAME, PROVIDERCLASS, PROVIDERARG,
            PROVIDERPATH, V, PROTECTED),
        DELETE("Deletes.an.entry",
            ALIAS, KEYSTORE, STOREPASS, STORETYPE,
            PROVIDERNAME, PROVIDERCLASS, PROVIDERARG,
            PROVIDERPATH, V, PROTECTED),
        EXPORTCERT("Exports.certificate",
            RFC, ALIAS, FILEOUT, KEYSTORE, STOREPASS,
            STORETYPE, PROVIDERNAME, PROVIDERCLASS, PROVIDERARG,
            PROVIDERPATH, V, PROTECTED),
        GENKEYPAIR("Generates.a.key.pair",
            ALIAS, KEYALG, KEYSIZE, SIGALG, DESTALIAS, DNAME,
            STARTDATE, EXT, VALIDITY, KEYPASS, KEYSTORE,
            STOREPASS, STORETYPE, PROVIDERNAME, PROVIDERCLASS,
            PROVIDERARG, PROVIDERPATH, V, PROTECTED),
        GENSECKEY("Generates.a.secret.key",
            ALIAS, KEYPASS, KEYALG, KEYSIZE, KEYSTORE,
            STOREPASS, STORETYPE, PROVIDERNAME, PROVIDERCLASS,
            PROVIDERARG, PROVIDERPATH, V, PROTECTED),
        GENCERT("Generates.certificate.from.a.certificate.request",
            RFC, INFILE, OUTFILE, ALIAS, SIGALG, DNAME,
            STARTDATE, EXT, VALIDITY, KEYPASS, KEYSTORE,
            STOREPASS, STORETYPE, PROVIDERNAME, PROVIDERCLASS,
            PROVIDERARG, PROVIDERPATH, V, PROTECTED),
        IMPORTCERT("Imports.a.certificate.or.a.certificate.chain",
            NOPROMPT, TRUSTCACERTS, PROTECTED, ALIAS, FILEIN,
            KEYPASS, KEYSTORE, STOREPASS, STORETYPE,
            PROVIDERNAME, PROVIDERCLASS, PROVIDERARG,
            PROVIDERPATH, V),
        IMPORTKEYSTORE("Imports.one.or.all.entries.from.another.keystore",
            SRCKEYSTORE, DESTKEYSTORE, SRCSTORETYPE,
            DESTSTORETYPE, SRCSTOREPASS, DESTSTOREPASS,
            SRCPROTECTED, SRCPROVIDERNAME, DESTPROVIDERNAME,
            SRCALIAS, DESTALIAS, SRCKEYPASS, DESTKEYPASS,
            NOPROMPT, PROVIDERCLASS, PROVIDERARG, PROVIDERPATH,
            V),
        KEYPASSWD("Changes.the.key.password.of.an.entry",
            ALIAS, KEYPASS, NEW, KEYSTORE, STOREPASS,
            STORETYPE, PROVIDERNAME, PROVIDERCLASS, PROVIDERARG,
            PROVIDERPATH, V),
        LIST("Lists.entries.in.a.keystore",
            RFC, ALIAS, KEYSTORE, STOREPASS, STORETYPE,
            PROVIDERNAME, PROVIDERCLASS, PROVIDERARG,
            PROVIDERPATH, V, PROTECTED),
        PRINTCERT("Prints.the.content.of.a.certificate",
            RFC, FILEIN, SSLSERVER, JARFILE, V),
        PRINTCERTREQ("Prints.the.content.of.a.certificate.request",
            FILEIN, V),
        PRINTCRL("Prints.the.content.of.a.CRL.file",
            FILEIN, V),
        STOREPASSWD("Changes.the.store.password.of.a.keystore",
            NEW, KEYSTORE, STOREPASS, STORETYPE, PROVIDERNAME,
            PROVIDERCLASS, PROVIDERARG, PROVIDERPATH, V),
        KEYCLONE("Clones.a.key.entry",
            ALIAS, DESTALIAS, KEYPASS, NEW, STORETYPE,
            KEYSTORE, STOREPASS, PROVIDERNAME, PROVIDERCLASS,
            PROVIDERARG, PROVIDERPATH, V),
        SELFCERT("Generates.a.self.signed.certificate",
            ALIAS, SIGALG, DNAME, STARTDATE, VALIDITY, KEYPASS,
            STORETYPE, KEYSTORE, STOREPASS, PROVIDERNAME,
            PROVIDERCLASS, PROVIDERARG, PROVIDERPATH, V),
        GENCRL("Generates.CRL",
            RFC, FILEOUT, ID,
            ALIAS, SIGALG, EXT, KEYPASS, KEYSTORE,
            STOREPASS, STORETYPE, PROVIDERNAME, PROVIDERCLASS,
            PROVIDERARG, PROVIDERPATH, V, PROTECTED),
        IDENTITYDB("Imports.entries.from.a.JDK.1.1.x.style.identity.database",
            FILEIN, STORETYPE, KEYSTORE, STOREPASS, PROVIDERNAME,
            PROVIDERCLASS, PROVIDERARG, PROVIDERPATH, V);
        final String description;
        final Option[] options;
        Command(String d, Option... o) {
            description = d;
            options = o;
        }
        @Override
        public String toString() {
            return "-" + name().toLowerCase(Locale.ENGLISH);
        }
    };
    enum Option {
        ALIAS("alias", "<alias>", "alias.name.of.the.entry.to.process"),
        DESTALIAS("destalias", "<destalias>", "destination.alias"),
        DESTKEYPASS("destkeypass", "<arg>", "destination.key.password"),
        DESTKEYSTORE("destkeystore", "<destkeystore>", "destination.keystore.name"),
        DESTPROTECTED("destprotected", null, "destination.keystore.password.protected"),
        DESTPROVIDERNAME("destprovidername", "<destprovidername>", "destination.keystore.provider.name"),
        DESTSTOREPASS("deststorepass", "<arg>", "destination.keystore.password"),
        DESTSTORETYPE("deststoretype", "<deststoretype>", "destination.keystore.type"),
        DNAME("dname", "<dname>", "distinguished.name"),
        EXT("ext", "<value>", "X.509.extension"),
        FILEOUT("file", "<filename>", "output.file.name"),
        FILEIN("file", "<filename>", "input.file.name"),
        ID("id", "<id:reason>", "Serial.ID.of.cert.to.revoke"),
        INFILE("infile", "<filename>", "input.file.name"),
        KEYALG("keyalg", "<keyalg>", "key.algorithm.name"),
        KEYPASS("keypass", "<arg>", "key.password"),
        KEYSIZE("keysize", "<keysize>", "key.bit.size"),
        KEYSTORE("keystore", "<keystore>", "keystore.name"),
        NEW("new", "<arg>", "new.password"),
        NOPROMPT("noprompt", null, "do.not.prompt"),
        OUTFILE("outfile", "<filename>", "output.file.name"),
        PROTECTED("protected", null, "password.through.protected.mechanism"),
        PROVIDERARG("providerarg", "<arg>", "provider.argument"),
        PROVIDERCLASS("providerclass", "<providerclass>", "provider.class.name"),
        PROVIDERNAME("providername", "<providername>", "provider.name"),
        PROVIDERPATH("providerpath", "<pathlist>", "provider.classpath"),
        RFC("rfc", null, "output.in.RFC.style"),
        SIGALG("sigalg", "<sigalg>", "signature.algorithm.name"),
        SRCALIAS("srcalias", "<srcalias>", "source.alias"),
        SRCKEYPASS("srckeypass", "<arg>", "source.key.password"),
        SRCKEYSTORE("srckeystore", "<srckeystore>", "source.keystore.name"),
        SRCPROTECTED("srcprotected", null, "source.keystore.password.protected"),
        SRCPROVIDERNAME("srcprovidername", "<srcprovidername>", "source.keystore.provider.name"),
        SRCSTOREPASS("srcstorepass", "<arg>", "source.keystore.password"),
        SRCSTORETYPE("srcstoretype", "<srcstoretype>", "source.keystore.type"),
        SSLSERVER("sslserver", "<server[:port]>", "SSL.server.host.and.port"),
        JARFILE("jarfile", "<filename>", "signed.jar.file"),
        STARTDATE("startdate", "<startdate>", "certificate.validity.start.date.time"),
        STOREPASS("storepass", "<arg>", "keystore.password"),
        STORETYPE("storetype", "<storetype>", "keystore.type"),
        TRUSTCACERTS("trustcacerts", null, "trust.certificates.from.cacerts"),
        V("v", null, "verbose.output"),
        VALIDITY("validity", "<valDays>", "validity.number.of.days");
        final String name, arg, description;
        Option(String name, String arg, String description) {
            this.name = name;
            this.arg = arg;
            this.description = description;
        }
        @Override
        public String toString() {
            return "-" + name;
        }
    };
    private static final Class[] PARAM_STRING = { String.class };
    private static final String JKS = "jks";
    private static final String NONE = "NONE";
    private static final String P11KEYSTORE = "PKCS11";
    private static final String P12KEYSTORE = "PKCS12";
    private final String keyAlias = "mykey";
    private static final java.util.ResourceBundle rb =
        java.util.ResourceBundle.getBundle("sun.security.util.Resources");
    private static final Collator collator = Collator.getInstance();
    static {
        collator.setStrength(Collator.PRIMARY);
    };
    private KeyTool() { }
    public static void main(String[] args) throws Exception {
        KeyTool kt = new KeyTool();
        kt.run(args, System.out);
    }
    private void run(String[] args, PrintStream out) throws Exception {
        try {
            parseArgs(args);
            if (command != null) {
                doCommands(out);
            }
        } catch (Exception e) {
            System.out.println(rb.getString("keytool.error.") + e);
            if (verbose) {
                e.printStackTrace(System.out);
            }
            if (!debug) {
                System.exit(1);
            } else {
                throw e;
            }
        } finally {
            for (char[] pass : passwords) {
                if (pass != null) {
                    Arrays.fill(pass, ' ');
                    pass = null;
                }
            }
            if (ksStream != null) {
                ksStream.close();
            }
        }
    }
    void parseArgs(String[] args) {
        int i=0;
        boolean help = args.length == 0;
        for (i=0; (i < args.length) && args[i].startsWith("-"); i++) {
            String flags = args[i];
            if (i == args.length - 1) {
                for (Option option: Option.values()) {
                    if (collator.compare(flags, option.toString()) == 0) {
                        if (option.arg != null) errorNeedArgument(flags);
                        break;
                    }
                }
            }
            String modifier = null;
            int pos = flags.indexOf(':');
            if (pos > 0) {
                modifier = flags.substring(pos+1);
                flags = flags.substring(0, pos);
            }
            boolean isCommand = false;
            for (Command c: Command.values()) {
                if (collator.compare(flags, c.toString()) == 0) {
                    command = c;
                    isCommand = true;
                    break;
                }
            }
            if (isCommand) {
            } else if (collator.compare(flags, "-export") == 0) {
                command = EXPORTCERT;
            } else if (collator.compare(flags, "-genkey") == 0) {
                command = GENKEYPAIR;
            } else if (collator.compare(flags, "-import") == 0) {
                command = IMPORTCERT;
            }
            else if (collator.compare(flags, "-help") == 0) {
                help = true;
            }
            else if (collator.compare(flags, "-keystore") == 0 ||
                    collator.compare(flags, "-destkeystore") == 0) {
                ksfname = args[++i];
            } else if (collator.compare(flags, "-storepass") == 0 ||
                    collator.compare(flags, "-deststorepass") == 0) {
                storePass = getPass(modifier, args[++i]);
                passwords.add(storePass);
            } else if (collator.compare(flags, "-storetype") == 0 ||
                    collator.compare(flags, "-deststoretype") == 0) {
                storetype = args[++i];
            } else if (collator.compare(flags, "-srcstorepass") == 0) {
                srcstorePass = getPass(modifier, args[++i]);
                passwords.add(srcstorePass);
            } else if (collator.compare(flags, "-srcstoretype") == 0) {
                srcstoretype = args[++i];
            } else if (collator.compare(flags, "-srckeypass") == 0) {
                srckeyPass = getPass(modifier, args[++i]);
                passwords.add(srckeyPass);
            } else if (collator.compare(flags, "-srcprovidername") == 0) {
                srcProviderName = args[++i];
            } else if (collator.compare(flags, "-providername") == 0 ||
                    collator.compare(flags, "-destprovidername") == 0) {
                providerName = args[++i];
            } else if (collator.compare(flags, "-providerpath") == 0) {
                pathlist = args[++i];
            } else if (collator.compare(flags, "-keypass") == 0) {
                keyPass = getPass(modifier, args[++i]);
                passwords.add(keyPass);
            } else if (collator.compare(flags, "-new") == 0) {
                newPass = getPass(modifier, args[++i]);
                passwords.add(newPass);
            } else if (collator.compare(flags, "-destkeypass") == 0) {
                destKeyPass = getPass(modifier, args[++i]);
                passwords.add(destKeyPass);
            } else if (collator.compare(flags, "-alias") == 0 ||
                    collator.compare(flags, "-srcalias") == 0) {
                alias = args[++i];
            } else if (collator.compare(flags, "-dest") == 0 ||
                    collator.compare(flags, "-destalias") == 0) {
                dest = args[++i];
            } else if (collator.compare(flags, "-dname") == 0) {
                dname = args[++i];
            } else if (collator.compare(flags, "-keysize") == 0) {
                keysize = Integer.parseInt(args[++i]);
            } else if (collator.compare(flags, "-keyalg") == 0) {
                keyAlgName = args[++i];
            } else if (collator.compare(flags, "-sigalg") == 0) {
                sigAlgName = args[++i];
            } else if (collator.compare(flags, "-startdate") == 0) {
                startDate = args[++i];
            } else if (collator.compare(flags, "-validity") == 0) {
                validity = Long.parseLong(args[++i]);
            } else if (collator.compare(flags, "-ext") == 0) {
                v3ext.add(args[++i]);
            } else if (collator.compare(flags, "-id") == 0) {
                ids.add(args[++i]);
            } else if (collator.compare(flags, "-file") == 0) {
                filename = args[++i];
            } else if (collator.compare(flags, "-infile") == 0) {
                infilename = args[++i];
            } else if (collator.compare(flags, "-outfile") == 0) {
                outfilename = args[++i];
            } else if (collator.compare(flags, "-sslserver") == 0) {
                sslserver = args[++i];
            } else if (collator.compare(flags, "-jarfile") == 0) {
                jarfile = args[++i];
            } else if (collator.compare(flags, "-srckeystore") == 0) {
                srcksfname = args[++i];
            } else if ((collator.compare(flags, "-provider") == 0) ||
                        (collator.compare(flags, "-providerclass") == 0)) {
                if (providers == null) {
                    providers = new HashSet<Pair <String, String>> (3);
                }
                String providerClass = args[++i];
                String providerArg = null;
                if (args.length > (i+1)) {
                    flags = args[i+1];
                    if (collator.compare(flags, "-providerarg") == 0) {
                        if (args.length == (i+2)) errorNeedArgument(flags);
                        providerArg = args[i+2];
                        i += 2;
                    }
                }
                providers.add(
                        Pair.of(providerClass, providerArg));
            }
            else if (collator.compare(flags, "-v") == 0) {
                verbose = true;
            } else if (collator.compare(flags, "-debug") == 0) {
                debug = true;
            } else if (collator.compare(flags, "-rfc") == 0) {
                rfc = true;
            } else if (collator.compare(flags, "-noprompt") == 0) {
                noprompt = true;
            } else if (collator.compare(flags, "-trustcacerts") == 0) {
                trustcacerts = true;
            } else if (collator.compare(flags, "-protected") == 0 ||
                    collator.compare(flags, "-destprotected") == 0) {
                protectedPath = true;
            } else if (collator.compare(flags, "-srcprotected") == 0) {
                srcprotectedPath = true;
            } else  {
                System.err.println(rb.getString("Illegal.option.") + flags);
                tinyHelp();
            }
        }
        if (i<args.length) {
            System.err.println(rb.getString("Illegal.option.") + args[i]);
            tinyHelp();
        }
        if (command == null) {
            if (help) {
                usage();
            } else {
                System.err.println(rb.getString("Usage.error.no.command.provided"));
                tinyHelp();
            }
        } else if (help) {
            usage();
            command = null;
        }
    }
    boolean isKeyStoreRelated(Command cmd) {
        return cmd != PRINTCERT && cmd != PRINTCERTREQ;
    }
    void doCommands(PrintStream out) throws Exception {
        if (storetype == null) {
            storetype = KeyStore.getDefaultType();
        }
        storetype = KeyStoreUtil.niceStoreTypeName(storetype);
        if (srcstoretype == null) {
            srcstoretype = KeyStore.getDefaultType();
        }
        srcstoretype = KeyStoreUtil.niceStoreTypeName(srcstoretype);
        if (P11KEYSTORE.equalsIgnoreCase(storetype) ||
                KeyStoreUtil.isWindowsKeyStore(storetype)) {
            token = true;
            if (ksfname == null) {
                ksfname = NONE;
            }
        }
        if (NONE.equals(ksfname)) {
            nullStream = true;
        }
        if (token && !nullStream) {
            System.err.println(MessageFormat.format(rb.getString
                (".keystore.must.be.NONE.if.storetype.is.{0}"), storetype));
            System.err.println();
            tinyHelp();
        }
        if (token &&
            (command == KEYPASSWD || command == STOREPASSWD)) {
            throw new UnsupportedOperationException(MessageFormat.format(rb.getString
                        (".storepasswd.and.keypasswd.commands.not.supported.if.storetype.is.{0}"), storetype));
        }
        if (P12KEYSTORE.equalsIgnoreCase(storetype) && command == KEYPASSWD) {
            throw new UnsupportedOperationException(rb.getString
                        (".keypasswd.commands.not.supported.if.storetype.is.PKCS12"));
        }
        if (token && (keyPass != null || newPass != null || destKeyPass != null)) {
            throw new IllegalArgumentException(MessageFormat.format(rb.getString
                (".keypass.and.new.can.not.be.specified.if.storetype.is.{0}"), storetype));
        }
        if (protectedPath) {
            if (storePass != null || keyPass != null ||
                    newPass != null || destKeyPass != null) {
                throw new IllegalArgumentException(rb.getString
                        ("if.protected.is.specified.then.storepass.keypass.and.new.must.not.be.specified"));
            }
        }
        if (srcprotectedPath) {
            if (srcstorePass != null || srckeyPass != null) {
                throw new IllegalArgumentException(rb.getString
                        ("if.srcprotected.is.specified.then.srcstorepass.and.srckeypass.must.not.be.specified"));
            }
        }
        if (KeyStoreUtil.isWindowsKeyStore(storetype)) {
            if (storePass != null || keyPass != null ||
                    newPass != null || destKeyPass != null) {
                throw new IllegalArgumentException(rb.getString
                        ("if.keystore.is.not.password.protected.then.storepass.keypass.and.new.must.not.be.specified"));
            }
        }
        if (KeyStoreUtil.isWindowsKeyStore(srcstoretype)) {
            if (srcstorePass != null || srckeyPass != null) {
                throw new IllegalArgumentException(rb.getString
                        ("if.source.keystore.is.not.password.protected.then.srcstorepass.and.srckeypass.must.not.be.specified"));
            }
        }
        if (validity <= (long)0) {
            throw new Exception
                (rb.getString("Validity.must.be.greater.than.zero"));
        }
        if (providers != null) {
            ClassLoader cl = null;
            if (pathlist != null) {
                String path = null;
                path = PathList.appendPath(
                        path, System.getProperty("java.class.path"));
                path = PathList.appendPath(
                        path, System.getProperty("env.class.path"));
                path = PathList.appendPath(path, pathlist);
                URL[] urls = PathList.pathToURLs(path);
                cl = new URLClassLoader(urls);
            } else {
                cl = ClassLoader.getSystemClassLoader();
            }
            for (Pair <String, String> provider: providers) {
                String provName = provider.fst;
                Class<?> provClass;
                if (cl != null) {
                    provClass = cl.loadClass(provName);
                } else {
                    provClass = Class.forName(provName);
                }
                String provArg = provider.snd;
                Object obj;
                if (provArg == null) {
                    obj = provClass.newInstance();
                } else {
                    Constructor<?> c = provClass.getConstructor(PARAM_STRING);
                    obj = c.newInstance(provArg);
                }
                if (!(obj instanceof Provider)) {
                    MessageFormat form = new MessageFormat
                        (rb.getString("provName.not.a.provider"));
                    Object[] source = {provName};
                    throw new Exception(form.format(source));
                }
                Security.addProvider((Provider)obj);
            }
        }
        if (command == LIST && verbose && rfc) {
            System.err.println(rb.getString
                ("Must.not.specify.both.v.and.rfc.with.list.command"));
            tinyHelp();
        }
        if (command == GENKEYPAIR && keyPass!=null && keyPass.length < 6) {
            throw new Exception(rb.getString
                ("Key.password.must.be.at.least.6.characters"));
        }
        if (newPass != null && newPass.length < 6) {
            throw new Exception(rb.getString
                ("New.password.must.be.at.least.6.characters"));
        }
        if (destKeyPass != null && destKeyPass.length < 6) {
            throw new Exception(rb.getString
                ("New.password.must.be.at.least.6.characters"));
        }
        if (isKeyStoreRelated(command)) {
            if (ksfname == null) {
                ksfname = System.getProperty("user.home") + File.separator
                    + ".keystore";
            }
            if (!nullStream) {
                try {
                    ksfile = new File(ksfname);
                    if (ksfile.exists() && ksfile.length() == 0) {
                        throw new Exception(rb.getString
                        ("Keystore.file.exists.but.is.empty.") + ksfname);
                    }
                    ksStream = new FileInputStream(ksfile);
                } catch (FileNotFoundException e) {
                    if (command != GENKEYPAIR &&
                        command != GENSECKEY &&
                        command != IDENTITYDB &&
                        command != IMPORTCERT &&
                        command != IMPORTKEYSTORE &&
                        command != PRINTCRL) {
                        throw new Exception(rb.getString
                                ("Keystore.file.does.not.exist.") + ksfname);
                    }
                }
            }
        }
        if ((command == KEYCLONE || command == CHANGEALIAS)
                && dest == null) {
            dest = getAlias("destination");
            if ("".equals(dest)) {
                throw new Exception(rb.getString
                        ("Must.specify.destination.alias"));
            }
        }
        if (command == DELETE && alias == null) {
            alias = getAlias(null);
            if ("".equals(alias)) {
                throw new Exception(rb.getString("Must.specify.alias"));
            }
        }
        if (providerName == null) {
            keyStore = KeyStore.getInstance(storetype);
        } else {
            keyStore = KeyStore.getInstance(storetype, providerName);
        }
        if (!nullStream) {
            keyStore.load(ksStream, storePass);
            if (ksStream != null) {
                ksStream.close();
            }
        }
        if (nullStream && storePass != null) {
            keyStore.load(null, storePass);
        } else if (!nullStream && storePass != null) {
            if (ksStream == null && storePass.length < 6) {
                throw new Exception(rb.getString
                        ("Keystore.password.must.be.at.least.6.characters"));
            }
        } else if (storePass == null) {
            if (!protectedPath && !KeyStoreUtil.isWindowsKeyStore(storetype) &&
                (command == CERTREQ ||
                        command == DELETE ||
                        command == GENKEYPAIR ||
                        command == GENSECKEY ||
                        command == IMPORTCERT ||
                        command == IMPORTKEYSTORE ||
                        command == KEYCLONE ||
                        command == CHANGEALIAS ||
                        command == SELFCERT ||
                        command == STOREPASSWD ||
                        command == KEYPASSWD ||
                        command == IDENTITYDB)) {
                int count = 0;
                do {
                    if (command == IMPORTKEYSTORE) {
                        System.err.print
                                (rb.getString("Enter.destination.keystore.password."));
                    } else {
                        System.err.print
                                (rb.getString("Enter.keystore.password."));
                    }
                    System.err.flush();
                    storePass = Password.readPassword(System.in);
                    passwords.add(storePass);
                    if (!nullStream && (storePass == null || storePass.length < 6)) {
                        System.err.println(rb.getString
                                ("Keystore.password.is.too.short.must.be.at.least.6.characters"));
                        storePass = null;
                    }
                    if (storePass != null && !nullStream && ksStream == null) {
                        System.err.print(rb.getString("Re.enter.new.password."));
                        char[] storePassAgain = Password.readPassword(System.in);
                        passwords.add(storePassAgain);
                        if (!Arrays.equals(storePass, storePassAgain)) {
                            System.err.println
                                (rb.getString("They.don.t.match.Try.again"));
                            storePass = null;
                        }
                    }
                    count++;
                } while ((storePass == null) && count < 3);
                if (storePass == null) {
                    System.err.println
                        (rb.getString("Too.many.failures.try.later"));
                    return;
                }
            } else if (!protectedPath
                    && !KeyStoreUtil.isWindowsKeyStore(storetype)
                    && isKeyStoreRelated(command)) {
                if (command != PRINTCRL) {
                    System.err.print(rb.getString("Enter.keystore.password."));
                    System.err.flush();
                    storePass = Password.readPassword(System.in);
                    passwords.add(storePass);
                }
            }
            if (nullStream) {
                keyStore.load(null, storePass);
            } else if (ksStream != null) {
                ksStream = new FileInputStream(ksfile);
                keyStore.load(ksStream, storePass);
                ksStream.close();
            }
        }
        if (storePass != null && P12KEYSTORE.equalsIgnoreCase(storetype)) {
            MessageFormat form = new MessageFormat(rb.getString(
                "Warning.Different.store.and.key.passwords.not.supported.for.PKCS12.KeyStores.Ignoring.user.specified.command.value."));
            if (keyPass != null && !Arrays.equals(storePass, keyPass)) {
                Object[] source = {"-keypass"};
                System.err.println(form.format(source));
                keyPass = storePass;
            }
            if (newPass != null && !Arrays.equals(storePass, newPass)) {
                Object[] source = {"-new"};
                System.err.println(form.format(source));
                newPass = storePass;
            }
            if (destKeyPass != null && !Arrays.equals(storePass, destKeyPass)) {
                Object[] source = {"-destkeypass"};
                System.err.println(form.format(source));
                destKeyPass = storePass;
            }
        }
        if (command == PRINTCERT || command == IMPORTCERT
                || command == IDENTITYDB || command == PRINTCRL) {
            cf = CertificateFactory.getInstance("X509");
        }
        if (trustcacerts) {
            caks = getCacertsKeyStore();
        }
        if (command == CERTREQ) {
            PrintStream ps = null;
            if (filename != null) {
                ps = new PrintStream(new FileOutputStream
                                                 (filename));
                out = ps;
            }
            try {
                doCertReq(alias, sigAlgName, out);
            } finally {
                if (ps != null) {
                    ps.close();
                }
            }
            if (verbose && filename != null) {
                MessageFormat form = new MessageFormat(rb.getString
                        ("Certification.request.stored.in.file.filename."));
                Object[] source = {filename};
                System.err.println(form.format(source));
                System.err.println(rb.getString("Submit.this.to.your.CA"));
            }
        } else if (command == DELETE) {
            doDeleteEntry(alias);
            kssave = true;
        } else if (command == EXPORTCERT) {
            PrintStream ps = null;
            if (filename != null) {
                ps = new PrintStream(new FileOutputStream
                                                 (filename));
                out = ps;
            }
            try {
                doExportCert(alias, out);
            } finally {
                if (ps != null) {
                    ps.close();
                }
            }
            if (filename != null) {
                MessageFormat form = new MessageFormat(rb.getString
                        ("Certificate.stored.in.file.filename."));
                Object[] source = {filename};
                System.err.println(form.format(source));
            }
        } else if (command == GENKEYPAIR) {
            if (keyAlgName == null) {
                keyAlgName = "DSA";
            }
            doGenKeyPair(alias, dname, keyAlgName, keysize, sigAlgName);
            kssave = true;
        } else if (command == GENSECKEY) {
            if (keyAlgName == null) {
                keyAlgName = "DES";
            }
            doGenSecretKey(alias, keyAlgName, keysize);
            kssave = true;
        } else if (command == IDENTITYDB) {
            InputStream inStream = System.in;
            if (filename != null) {
                inStream = new FileInputStream(filename);
            }
            try {
                doImportIdentityDatabase(inStream);
            } finally {
                if (inStream != System.in) {
                    inStream.close();
                }
            }
        } else if (command == IMPORTCERT) {
            InputStream inStream = System.in;
            if (filename != null) {
                inStream = new FileInputStream(filename);
            }
            String importAlias = (alias!=null)?alias:keyAlias;
            try {
                if (keyStore.entryInstanceOf(
                        importAlias, KeyStore.PrivateKeyEntry.class)) {
                    kssave = installReply(importAlias, inStream);
                    if (kssave) {
                        System.err.println(rb.getString
                            ("Certificate.reply.was.installed.in.keystore"));
                    } else {
                        System.err.println(rb.getString
                            ("Certificate.reply.was.not.installed.in.keystore"));
                    }
                } else if (!keyStore.containsAlias(importAlias) ||
                        keyStore.entryInstanceOf(importAlias,
                            KeyStore.TrustedCertificateEntry.class)) {
                    kssave = addTrustedCert(importAlias, inStream);
                    if (kssave) {
                        System.err.println(rb.getString
                            ("Certificate.was.added.to.keystore"));
                    } else {
                        System.err.println(rb.getString
                            ("Certificate.was.not.added.to.keystore"));
                    }
                }
            } finally {
                if (inStream != System.in) {
                    inStream.close();
                }
            }
        } else if (command == IMPORTKEYSTORE) {
            doImportKeyStore();
            kssave = true;
        } else if (command == KEYCLONE) {
            keyPassNew = newPass;
            if (alias == null) {
                alias = keyAlias;
            }
            if (keyStore.containsAlias(alias) == false) {
                MessageFormat form = new MessageFormat
                    (rb.getString("Alias.alias.does.not.exist"));
                Object[] source = {alias};
                throw new Exception(form.format(source));
            }
            if (!keyStore.entryInstanceOf(alias, KeyStore.PrivateKeyEntry.class)) {
                MessageFormat form = new MessageFormat(rb.getString(
                        "Alias.alias.references.an.entry.type.that.is.not.a.private.key.entry.The.keyclone.command.only.supports.cloning.of.private.key"));
                Object[] source = {alias};
                throw new Exception(form.format(source));
            }
            doCloneEntry(alias, dest, true);  
            kssave = true;
        } else if (command == CHANGEALIAS) {
            if (alias == null) {
                alias = keyAlias;
            }
            doCloneEntry(alias, dest, false);
            if (keyStore.containsAlias(alias)) {
                doDeleteEntry(alias);
            }
            kssave = true;
        } else if (command == KEYPASSWD) {
            keyPassNew = newPass;
            doChangeKeyPasswd(alias);
            kssave = true;
        } else if (command == LIST) {
            if (alias != null) {
                doPrintEntry(alias, out, true);
            } else {
                doPrintEntries(out);
            }
        } else if (command == PRINTCERT) {
            doPrintCert(out);
        } else if (command == SELFCERT) {
            doSelfCert(alias, dname, sigAlgName);
            kssave = true;
        } else if (command == STOREPASSWD) {
            storePassNew = newPass;
            if (storePassNew == null) {
                storePassNew = getNewPasswd("keystore password", storePass);
            }
            kssave = true;
        } else if (command == GENCERT) {
            if (alias == null) {
                alias = keyAlias;
            }
            InputStream inStream = System.in;
            if (infilename != null) {
                inStream = new FileInputStream(infilename);
            }
            PrintStream ps = null;
            if (outfilename != null) {
                ps = new PrintStream(new FileOutputStream(outfilename));
                out = ps;
            }
            try {
                doGenCert(alias, sigAlgName, inStream, out);
            } finally {
                if (inStream != System.in) {
                    inStream.close();
                }
                if (ps != null) {
                    ps.close();
                }
            }
        } else if (command == GENCRL) {
            if (alias == null) {
                alias = keyAlias;
            }
            PrintStream ps = null;
            if (filename != null) {
                ps = new PrintStream(new FileOutputStream(filename));
                out = ps;
            }
            try {
                doGenCRL(out);
            } finally {
                if (ps != null) {
                    ps.close();
                }
            }
        } else if (command == PRINTCERTREQ) {
            InputStream inStream = System.in;
            if (filename != null) {
                inStream = new FileInputStream(filename);
            }
            try {
                doPrintCertReq(inStream, out);
            } finally {
                if (inStream != System.in) {
                    inStream.close();
                }
            }
        } else if (command == PRINTCRL) {
            doPrintCRL(filename, out);
        }
        if (kssave) {
            if (verbose) {
                MessageFormat form = new MessageFormat
                        (rb.getString(".Storing.ksfname."));
                Object[] source = {nullStream ? "keystore" : ksfname};
                System.err.println(form.format(source));
            }
            if (token) {
                keyStore.store(null, null);
            } else {
                FileOutputStream fout = null;
                try {
                    fout = (nullStream ?
                                        (FileOutputStream)null :
                                        new FileOutputStream(ksfname));
                    keyStore.store
                        (fout,
                        (storePassNew!=null) ? storePassNew : storePass);
                } finally {
                    if (fout != null) {
                        fout.close();
                    }
                }
            }
        }
    }
    private void doGenCert(String alias, String sigAlgName, InputStream in, PrintStream out)
            throws Exception {
        Certificate signerCert = keyStore.getCertificate(alias);
        byte[] encoded = signerCert.getEncoded();
        X509CertImpl signerCertImpl = new X509CertImpl(encoded);
        X509CertInfo signerCertInfo = (X509CertInfo)signerCertImpl.get(
                X509CertImpl.NAME + "." + X509CertImpl.INFO);
        X500Name issuer = (X500Name)signerCertInfo.get(X509CertInfo.SUBJECT + "." +
                                           CertificateSubjectName.DN_NAME);
        Date firstDate = getStartDate(startDate);
        Date lastDate = new Date();
        lastDate.setTime(firstDate.getTime() + validity*1000L*24L*60L*60L);
        CertificateValidity interval = new CertificateValidity(firstDate,
                                                               lastDate);
        PrivateKey privateKey =
                (PrivateKey)recoverKey(alias, storePass, keyPass).fst;
        if (sigAlgName == null) {
            sigAlgName = getCompatibleSigAlgName(privateKey.getAlgorithm());
        }
        Signature signature = Signature.getInstance(sigAlgName);
        signature.initSign(privateKey);
        X509CertInfo info = new X509CertInfo();
        info.set(X509CertInfo.VALIDITY, interval);
        info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(
                    new java.util.Random().nextInt() & 0x7fffffff));
        info.set(X509CertInfo.VERSION,
                    new CertificateVersion(CertificateVersion.V3));
        info.set(X509CertInfo.ALGORITHM_ID,
                    new CertificateAlgorithmId(
                        AlgorithmId.getAlgorithmId(sigAlgName)));
        info.set(X509CertInfo.ISSUER, new CertificateIssuerName(issuer));
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        boolean canRead = false;
        StringBuffer sb = new StringBuffer();
        while (true) {
            String s = reader.readLine();
            if (s == null) break;
            if (s.startsWith("-----BEGIN") && s.indexOf("REQUEST") >= 0) {
                canRead = true;
            } else if (s.startsWith("-----END") && s.indexOf("REQUEST") >= 0) {
                break;
            } else if (canRead) {
                sb.append(s);
            }
        }
        byte[] rawReq = new BASE64Decoder().decodeBuffer(new String(sb));
        PKCS10 req = new PKCS10(rawReq);
        info.set(X509CertInfo.KEY, new CertificateX509Key(req.getSubjectPublicKeyInfo()));
        info.set(X509CertInfo.SUBJECT, new CertificateSubjectName(
                dname==null?req.getSubjectName():new X500Name(dname)));
        CertificateExtensions reqex = null;
        Iterator<PKCS10Attribute> attrs = req.getAttributes().getAttributes().iterator();
        while (attrs.hasNext()) {
            PKCS10Attribute attr = attrs.next();
            if (attr.getAttributeId().equals(PKCS9Attribute.EXTENSION_REQUEST_OID)) {
                reqex = (CertificateExtensions)attr.getAttributeValue();
            }
        }
        CertificateExtensions ext = createV3Extensions(
                reqex,
                null,
                v3ext,
                req.getSubjectPublicKeyInfo(),
                signerCert.getPublicKey());
        info.set(X509CertInfo.EXTENSIONS, ext);
        X509CertImpl cert = new X509CertImpl(info);
        cert.sign(privateKey, sigAlgName);
        dumpCert(cert, out);
        for (Certificate ca: keyStore.getCertificateChain(alias)) {
            if (ca instanceof X509Certificate) {
                X509Certificate xca = (X509Certificate)ca;
                if (!isSelfSigned(xca)) {
                    dumpCert(xca, out);
                }
            }
        }
    }
    private void doGenCRL(PrintStream out)
            throws Exception {
        if (ids == null) {
            throw new Exception("Must provide -id when -gencrl");
        }
        Certificate signerCert = keyStore.getCertificate(alias);
        byte[] encoded = signerCert.getEncoded();
        X509CertImpl signerCertImpl = new X509CertImpl(encoded);
        X509CertInfo signerCertInfo = (X509CertInfo)signerCertImpl.get(
                X509CertImpl.NAME + "." + X509CertImpl.INFO);
        X500Name owner = (X500Name)signerCertInfo.get(X509CertInfo.SUBJECT + "." +
                                           CertificateSubjectName.DN_NAME);
        Date firstDate = getStartDate(startDate);
        Date lastDate = (Date) firstDate.clone();
        lastDate.setTime(lastDate.getTime() + (long)validity*1000*24*60*60);
        CertificateValidity interval = new CertificateValidity(firstDate,
                                                               lastDate);
        PrivateKey privateKey =
                (PrivateKey)recoverKey(alias, storePass, keyPass).fst;
        if (sigAlgName == null) {
            sigAlgName = getCompatibleSigAlgName(privateKey.getAlgorithm());
        }
        X509CRLEntry[] badCerts = new X509CRLEntry[ids.size()];
        for (int i=0; i<ids.size(); i++) {
            String id = ids.get(i);
            int d = id.indexOf(':');
            if (d >= 0) {
                CRLExtensions ext = new CRLExtensions();
                ext.set("Reason", new CRLReasonCodeExtension(Integer.parseInt(id.substring(d+1))));
                badCerts[i] = new X509CRLEntryImpl(new BigInteger(id.substring(0, d)),
                        firstDate, ext);
            } else {
                badCerts[i] = new X509CRLEntryImpl(new BigInteger(ids.get(i)), firstDate);
            }
        }
        X509CRLImpl crl = new X509CRLImpl(owner, firstDate, lastDate, badCerts);
        crl.sign(privateKey, sigAlgName);
        if (rfc) {
            out.println("-----BEGIN X509 CRL-----");
            new BASE64Encoder().encodeBuffer(crl.getEncodedInternal(), out);
            out.println("-----END X509 CRL-----");
        } else {
            out.write(crl.getEncodedInternal());
        }
    }
    private void doCertReq(String alias, String sigAlgName, PrintStream out)
        throws Exception
    {
        if (alias == null) {
            alias = keyAlias;
        }
        Pair<Key,char[]> objs = recoverKey(alias, storePass, keyPass);
        PrivateKey privKey = (PrivateKey)objs.fst;
        if (keyPass == null) {
            keyPass = objs.snd;
        }
        Certificate cert = keyStore.getCertificate(alias);
        if (cert == null) {
            MessageFormat form = new MessageFormat
                (rb.getString("alias.has.no.public.key.certificate."));
            Object[] source = {alias};
            throw new Exception(form.format(source));
        }
        PKCS10 request = new PKCS10(cert.getPublicKey());
        CertificateExtensions ext = createV3Extensions(null, null, v3ext, cert.getPublicKey(), null);
        request.getAttributes().setAttribute(X509CertInfo.EXTENSIONS,
                new PKCS10Attribute(PKCS9Attribute.EXTENSION_REQUEST_OID, ext));
        if (sigAlgName == null) {
            sigAlgName = getCompatibleSigAlgName(privKey.getAlgorithm());
        }
        Signature signature = Signature.getInstance(sigAlgName);
        signature.initSign(privKey);
        X500Name subject = dname == null?
                new X500Name(((X509Certificate)cert).getSubjectDN().toString()):
                new X500Name(dname);
        request.encodeAndSign(subject, signature);
        request.print(out);
    }
    private void doDeleteEntry(String alias) throws Exception {
        if (keyStore.containsAlias(alias) == false) {
            MessageFormat form = new MessageFormat
                (rb.getString("Alias.alias.does.not.exist"));
            Object[] source = {alias};
            throw new Exception(form.format(source));
        }
        keyStore.deleteEntry(alias);
    }
    private void doExportCert(String alias, PrintStream out)
        throws Exception
    {
        if (storePass == null
                && !KeyStoreUtil.isWindowsKeyStore(storetype)) {
            printWarning();
        }
        if (alias == null) {
            alias = keyAlias;
        }
        if (keyStore.containsAlias(alias) == false) {
            MessageFormat form = new MessageFormat
                (rb.getString("Alias.alias.does.not.exist"));
            Object[] source = {alias};
            throw new Exception(form.format(source));
        }
        X509Certificate cert = (X509Certificate)keyStore.getCertificate(alias);
        if (cert == null) {
            MessageFormat form = new MessageFormat
                (rb.getString("Alias.alias.has.no.certificate"));
            Object[] source = {alias};
            throw new Exception(form.format(source));
        }
        dumpCert(cert, out);
    }
    private char[] promptForKeyPass(String alias, String orig, char[] origPass) throws Exception{
        if (P12KEYSTORE.equalsIgnoreCase(storetype)) {
            return origPass;
        } else if (!token) {
            int count;
            for (count = 0; count < 3; count++) {
                MessageFormat form = new MessageFormat(rb.getString
                        ("Enter.key.password.for.alias."));
                Object[] source = {alias};
                System.err.println(form.format(source));
                if (orig == null) {
                    System.err.print(rb.getString
                            (".RETURN.if.same.as.keystore.password."));
                } else {
                    form = new MessageFormat(rb.getString
                            (".RETURN.if.same.as.for.otherAlias."));
                    Object[] src = {orig};
                    System.err.print(form.format(src));
                }
                System.err.flush();
                char[] entered = Password.readPassword(System.in);
                passwords.add(entered);
                if (entered == null) {
                    return origPass;
                } else if (entered.length >= 6) {
                    System.err.print(rb.getString("Re.enter.new.password."));
                    char[] passAgain = Password.readPassword(System.in);
                    passwords.add(passAgain);
                    if (!Arrays.equals(entered, passAgain)) {
                        System.err.println
                            (rb.getString("They.don.t.match.Try.again"));
                        continue;
                    }
                    return entered;
                } else {
                    System.err.println(rb.getString
                        ("Key.password.is.too.short.must.be.at.least.6.characters"));
                }
            }
            if (count == 3) {
                if (command == KEYCLONE) {
                    throw new Exception(rb.getString
                        ("Too.many.failures.Key.entry.not.cloned"));
                } else {
                    throw new Exception(rb.getString
                            ("Too.many.failures.key.not.added.to.keystore"));
                }
            }
        }
        return null;    
    }
    private void doGenSecretKey(String alias, String keyAlgName,
                              int keysize)
        throws Exception
    {
        if (alias == null) {
            alias = keyAlias;
        }
        if (keyStore.containsAlias(alias)) {
            MessageFormat form = new MessageFormat(rb.getString
                ("Secret.key.not.generated.alias.alias.already.exists"));
            Object[] source = {alias};
            throw new Exception(form.format(source));
        }
        SecretKey secKey = null;
        KeyGenerator keygen = KeyGenerator.getInstance(keyAlgName);
        if (keysize != -1) {
            keygen.init(keysize);
        } else if ("DES".equalsIgnoreCase(keyAlgName)) {
            keygen.init(56);
        } else if ("DESede".equalsIgnoreCase(keyAlgName)) {
            keygen.init(168);
        } else {
            throw new Exception(rb.getString
                ("Please.provide.keysize.for.secret.key.generation"));
        }
        secKey = keygen.generateKey();
        if (keyPass == null) {
            keyPass = promptForKeyPass(alias, null, storePass);
        }
        keyStore.setKeyEntry(alias, secKey, keyPass, null);
    }
    private static String getCompatibleSigAlgName(String keyAlgName)
            throws Exception {
        if ("DSA".equalsIgnoreCase(keyAlgName)) {
            return "SHA1WithDSA";
        } else if ("RSA".equalsIgnoreCase(keyAlgName)) {
            return "SHA256WithRSA";
        } else if ("EC".equalsIgnoreCase(keyAlgName)) {
            return "SHA256withECDSA";
        } else {
            throw new Exception(rb.getString
                    ("Cannot.derive.signature.algorithm"));
        }
    }
    private void doGenKeyPair(String alias, String dname, String keyAlgName,
                              int keysize, String sigAlgName)
        throws Exception
    {
        if (keysize == -1) {
            if ("EC".equalsIgnoreCase(keyAlgName)) {
                keysize = 256;
            } else if ("RSA".equalsIgnoreCase(keyAlgName)) {
                keysize = 2048;
            } else {
                keysize = 1024;
            }
        }
        if (alias == null) {
            alias = keyAlias;
        }
        if (keyStore.containsAlias(alias)) {
            MessageFormat form = new MessageFormat(rb.getString
                ("Key.pair.not.generated.alias.alias.already.exists"));
            Object[] source = {alias};
            throw new Exception(form.format(source));
        }
        if (sigAlgName == null) {
            sigAlgName = getCompatibleSigAlgName(keyAlgName);
        }
        CertAndKeyGen keypair =
                new CertAndKeyGen(keyAlgName, sigAlgName, providerName);
        X500Name x500Name;
        if (dname == null) {
            x500Name = getX500Name();
        } else {
            x500Name = new X500Name(dname);
        }
        keypair.generate(keysize);
        PrivateKey privKey = keypair.getPrivateKey();
        X509Certificate[] chain = new X509Certificate[1];
        chain[0] = keypair.getSelfCertificate(
                x500Name, getStartDate(startDate), validity*24L*60L*60L);
        if (verbose) {
            MessageFormat form = new MessageFormat(rb.getString
                ("Generating.keysize.bit.keyAlgName.key.pair.and.self.signed.certificate.sigAlgName.with.a.validity.of.validality.days.for"));
            Object[] source = {new Integer(keysize),
                                privKey.getAlgorithm(),
                                chain[0].getSigAlgName(),
                                new Long(validity),
                                x500Name};
            System.err.println(form.format(source));
        }
        if (keyPass == null) {
            keyPass = promptForKeyPass(alias, null, storePass);
        }
        keyStore.setKeyEntry(alias, privKey, keyPass, chain);
        doSelfCert(alias, null, sigAlgName);
    }
    private void doCloneEntry(String orig, String dest, boolean changePassword)
        throws Exception
    {
        if (orig == null) {
            orig = keyAlias;
        }
        if (keyStore.containsAlias(dest)) {
            MessageFormat form = new MessageFormat
                (rb.getString("Destination.alias.dest.already.exists"));
            Object[] source = {dest};
            throw new Exception(form.format(source));
        }
        Pair<Entry,char[]> objs = recoverEntry(keyStore, orig, storePass, keyPass);
        Entry entry = objs.fst;
        keyPass = objs.snd;
        PasswordProtection pp = null;
        if (keyPass != null) {  
            if (!changePassword || P12KEYSTORE.equalsIgnoreCase(storetype)) {
                keyPassNew = keyPass;
            } else {
                if (keyPassNew == null) {
                    keyPassNew = promptForKeyPass(dest, orig, keyPass);
                }
            }
            pp = new PasswordProtection(keyPassNew);
        }
        keyStore.setEntry(dest, entry, pp);
    }
    private void doChangeKeyPasswd(String alias) throws Exception
    {
        if (alias == null) {
            alias = keyAlias;
        }
        Pair<Key,char[]> objs = recoverKey(alias, storePass, keyPass);
        Key privKey = objs.fst;
        if (keyPass == null) {
            keyPass = objs.snd;
        }
        if (keyPassNew == null) {
            MessageFormat form = new MessageFormat
                (rb.getString("key.password.for.alias."));
            Object[] source = {alias};
            keyPassNew = getNewPasswd(form.format(source), keyPass);
        }
        keyStore.setKeyEntry(alias, privKey, keyPassNew,
                             keyStore.getCertificateChain(alias));
    }
    private void doImportIdentityDatabase(InputStream in)
        throws Exception
    {
        System.err.println(rb.getString
            ("No.entries.from.identity.database.added"));
    }
    private void doPrintEntry(String alias, PrintStream out,
                              boolean printWarning)
        throws Exception
    {
        if (storePass == null && printWarning
                && !KeyStoreUtil.isWindowsKeyStore(storetype)) {
            printWarning();
        }
        if (keyStore.containsAlias(alias) == false) {
            MessageFormat form = new MessageFormat
                (rb.getString("Alias.alias.does.not.exist"));
            Object[] source = {alias};
            throw new Exception(form.format(source));
        }
        if (verbose || rfc || debug) {
            MessageFormat form = new MessageFormat
                (rb.getString("Alias.name.alias"));
            Object[] source = {alias};
            out.println(form.format(source));
            if (!token) {
                form = new MessageFormat(rb.getString
                    ("Creation.date.keyStore.getCreationDate.alias."));
                Object[] src = {keyStore.getCreationDate(alias)};
                out.println(form.format(src));
            }
        } else {
            if (!token) {
                MessageFormat form = new MessageFormat
                    (rb.getString("alias.keyStore.getCreationDate.alias."));
                Object[] source = {alias, keyStore.getCreationDate(alias)};
                out.print(form.format(source));
            } else {
                MessageFormat form = new MessageFormat
                    (rb.getString("alias."));
                Object[] source = {alias};
                out.print(form.format(source));
            }
        }
        if (keyStore.entryInstanceOf(alias, KeyStore.SecretKeyEntry.class)) {
            if (verbose || rfc || debug) {
                Object[] source = {"SecretKeyEntry"};
                out.println(new MessageFormat(
                        rb.getString("Entry.type.type.")).format(source));
            } else {
                out.println("SecretKeyEntry, ");
            }
        } else if (keyStore.entryInstanceOf(alias, KeyStore.PrivateKeyEntry.class)) {
            if (verbose || rfc || debug) {
                Object[] source = {"PrivateKeyEntry"};
                out.println(new MessageFormat(
                        rb.getString("Entry.type.type.")).format(source));
            } else {
                out.println("PrivateKeyEntry, ");
            }
            Certificate[] chain = keyStore.getCertificateChain(alias);
            if (chain != null) {
                if (verbose || rfc || debug) {
                    out.println(rb.getString
                        ("Certificate.chain.length.") + chain.length);
                    for (int i = 0; i < chain.length; i ++) {
                        MessageFormat form = new MessageFormat
                                (rb.getString("Certificate.i.1."));
                        Object[] source = {new Integer((i + 1))};
                        out.println(form.format(source));
                        if (verbose && (chain[i] instanceof X509Certificate)) {
                            printX509Cert((X509Certificate)(chain[i]), out);
                        } else if (debug) {
                            out.println(chain[i].toString());
                        } else {
                            dumpCert(chain[i], out);
                        }
                    }
                } else {
                    out.println
                        (rb.getString("Certificate.fingerprint.SHA1.") +
                        getCertFingerPrint("SHA1", chain[0]));
                }
            }
        } else if (keyStore.entryInstanceOf(alias,
                KeyStore.TrustedCertificateEntry.class)) {
            Certificate cert = keyStore.getCertificate(alias);
            Object[] source = {"trustedCertEntry"};
            String mf = new MessageFormat(
                    rb.getString("Entry.type.type.")).format(source) + "\n";
            if (verbose && (cert instanceof X509Certificate)) {
                out.println(mf);
                printX509Cert((X509Certificate)cert, out);
            } else if (rfc) {
                out.println(mf);
                dumpCert(cert, out);
            } else if (debug) {
                out.println(cert.toString());
            } else {
                out.println("trustedCertEntry, ");
                out.println(rb.getString("Certificate.fingerprint.SHA1.")
                            + getCertFingerPrint("SHA1", cert));
            }
        } else {
            out.println(rb.getString("Unknown.Entry.Type"));
        }
    }
    KeyStore loadSourceKeyStore() throws Exception {
        boolean isPkcs11 = false;
        InputStream is = null;
        if (P11KEYSTORE.equalsIgnoreCase(srcstoretype) ||
                KeyStoreUtil.isWindowsKeyStore(srcstoretype)) {
            if (!NONE.equals(srcksfname)) {
                System.err.println(MessageFormat.format(rb.getString
                    (".keystore.must.be.NONE.if.storetype.is.{0}"), srcstoretype));
                System.err.println();
                tinyHelp();
            }
            isPkcs11 = true;
        } else {
            if (srcksfname != null) {
                File srcksfile = new File(srcksfname);
                    if (srcksfile.exists() && srcksfile.length() == 0) {
                        throw new Exception(rb.getString
                                ("Source.keystore.file.exists.but.is.empty.") +
                                srcksfname);
                }
                is = new FileInputStream(srcksfile);
            } else {
                throw new Exception(rb.getString
                        ("Please.specify.srckeystore"));
            }
        }
        KeyStore store;
        try {
            if (srcProviderName == null) {
                store = KeyStore.getInstance(srcstoretype);
            } else {
                store = KeyStore.getInstance(srcstoretype, srcProviderName);
            }
            if (srcstorePass == null
                    && !srcprotectedPath
                    && !KeyStoreUtil.isWindowsKeyStore(srcstoretype)) {
                System.err.print(rb.getString("Enter.source.keystore.password."));
                System.err.flush();
                srcstorePass = Password.readPassword(System.in);
                passwords.add(srcstorePass);
            }
            if (P12KEYSTORE.equalsIgnoreCase(srcstoretype)) {
                if (srckeyPass != null && srcstorePass != null &&
                        !Arrays.equals(srcstorePass, srckeyPass)) {
                    MessageFormat form = new MessageFormat(rb.getString(
                        "Warning.Different.store.and.key.passwords.not.supported.for.PKCS12.KeyStores.Ignoring.user.specified.command.value."));
                    Object[] source = {"-srckeypass"};
                    System.err.println(form.format(source));
                    srckeyPass = srcstorePass;
                }
            }
            store.load(is, srcstorePass);   
        } finally {
            if (is != null) {
                is.close();
            }
        }
        if (srcstorePass == null
                && !KeyStoreUtil.isWindowsKeyStore(srcstoretype)) {
            System.err.println();
            System.err.println(rb.getString
                (".WARNING.WARNING.WARNING."));
            System.err.println(rb.getString
                (".The.integrity.of.the.information.stored.in.the.srckeystore."));
            System.err.println(rb.getString
                (".WARNING.WARNING.WARNING."));
            System.err.println();
        }
        return store;
    }
    private void doImportKeyStore() throws Exception {
        if (alias != null) {
            doImportKeyStoreSingle(loadSourceKeyStore(), alias);
        } else {
            if (dest != null || srckeyPass != null || destKeyPass != null) {
                throw new Exception(rb.getString(
                        "if.alias.not.specified.destalias.srckeypass.and.destkeypass.must.not.be.specified"));
            }
            doImportKeyStoreAll(loadSourceKeyStore());
        }
    }
    private int doImportKeyStoreSingle(KeyStore srckeystore, String alias)
            throws Exception {
        String newAlias = (dest==null) ? alias : dest;
        if (keyStore.containsAlias(newAlias)) {
            Object[] source = {alias};
            if (noprompt) {
                System.err.println(new MessageFormat(rb.getString(
                        "Warning.Overwriting.existing.alias.alias.in.destination.keystore")).format(source));
            } else {
                String reply = getYesNoReply(new MessageFormat(rb.getString(
                        "Existing.entry.alias.alias.exists.overwrite.no.")).format(source));
                if ("NO".equals(reply)) {
                    newAlias = inputStringFromStdin(rb.getString
                            ("Enter.new.alias.name.RETURN.to.cancel.import.for.this.entry."));
                    if ("".equals(newAlias)) {
                        System.err.println(new MessageFormat(rb.getString(
                                "Entry.for.alias.alias.not.imported.")).format(
                                source));
                        return 0;
                    }
                }
            }
        }
        Pair<Entry,char[]> objs = recoverEntry(srckeystore, alias, srcstorePass, srckeyPass);
        Entry entry = objs.fst;
        PasswordProtection pp = null;
        if (destKeyPass != null) {
            pp = new PasswordProtection(destKeyPass);
        } else if (objs.snd != null) {
            pp = new PasswordProtection(objs.snd);
        }
        try {
            keyStore.setEntry(newAlias, entry, pp);
            return 1;
        } catch (KeyStoreException kse) {
            Object[] source2 = {alias, kse.toString()};
            MessageFormat form = new MessageFormat(rb.getString(
                    "Problem.importing.entry.for.alias.alias.exception.Entry.for.alias.alias.not.imported."));
            System.err.println(form.format(source2));
            return 2;
        }
    }
    private void doImportKeyStoreAll(KeyStore srckeystore) throws Exception {
        int ok = 0;
        int count = srckeystore.size();
        for (Enumeration<String> e = srckeystore.aliases();
                                        e.hasMoreElements(); ) {
            String alias = e.nextElement();
            int result = doImportKeyStoreSingle(srckeystore, alias);
            if (result == 1) {
                ok++;
                Object[] source = {alias};
                MessageFormat form = new MessageFormat(rb.getString("Entry.for.alias.alias.successfully.imported."));
                System.err.println(form.format(source));
            } else if (result == 2) {
                if (!noprompt) {
                    String reply = getYesNoReply("Do you want to quit the import process? [no]:  ");
                    if ("YES".equals(reply)) {
                        break;
                    }
                }
            }
        }
        Object[] source = {ok, count-ok};
        MessageFormat form = new MessageFormat(rb.getString(
                "Import.command.completed.ok.entries.successfully.imported.fail.entries.failed.or.cancelled"));
        System.err.println(form.format(source));
    }
    private void doPrintEntries(PrintStream out)
        throws Exception
    {
        if (storePass == null
                && !KeyStoreUtil.isWindowsKeyStore(storetype)) {
            printWarning();
        } else {
            out.println();
        }
        out.println(rb.getString("Keystore.type.") + keyStore.getType());
        out.println(rb.getString("Keystore.provider.") +
                keyStore.getProvider().getName());
        out.println();
        MessageFormat form;
        form = (keyStore.size() == 1) ?
                new MessageFormat(rb.getString
                        ("Your.keystore.contains.keyStore.size.entry")) :
                new MessageFormat(rb.getString
                        ("Your.keystore.contains.keyStore.size.entries"));
        Object[] source = {new Integer(keyStore.size())};
        out.println(form.format(source));
        out.println();
        for (Enumeration<String> e = keyStore.aliases();
                                        e.hasMoreElements(); ) {
            String alias = e.nextElement();
            doPrintEntry(alias, out, false);
            if (verbose || rfc) {
                out.println(rb.getString("NEWLINE"));
                out.println(rb.getString
                        ("STAR"));
                out.println(rb.getString
                        ("STARNN"));
            }
        }
    }
    private static <T> Iterable<T> e2i(final Enumeration<T> e) {
        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return new Iterator<T>() {
                    @Override
                    public boolean hasNext() {
                        return e.hasMoreElements();
                    }
                    @Override
                    public T next() {
                        return e.nextElement();
                    }
                    public void remove() {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                };
            }
        };
    }
    public static Collection<? extends CRL> loadCRLs(String src) throws Exception {
        InputStream in = null;
        URI uri = null;
        if (src == null) {
            in = System.in;
        } else {
            try {
                uri = new URI(src);
                if (uri.getScheme().equals("ldap")) {
                } else {
                    in = uri.toURL().openStream();
                }
            } catch (Exception e) {
                try {
                    in = new FileInputStream(src);
                } catch (Exception e2) {
                    if (uri == null || uri.getScheme() == null) {
                        throw e2;   
                    } else {
                        throw e;    
                    }
                }
            }
        }
        if (in != null) {
            try {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                byte[] b = new byte[4096];
                while (true) {
                    int len = in.read(b);
                    if (len < 0) break;
                    bout.write(b, 0, len);
                }
                return CertificateFactory.getInstance("X509").generateCRLs(
                        new ByteArrayInputStream(bout.toByteArray()));
            } finally {
                if (in != System.in) {
                    in.close();
                }
            }
        } else {    
            String path = uri.getPath();
            if (path.charAt(0) == '/') path = path.substring(1);
            LDAPCertStoreHelper h = new LDAPCertStoreHelper();
            CertStore s = h.getCertStore(uri);
            X509CRLSelector sel =
                    h.wrap(new X509CRLSelector(), null, path);
            return s.getCRLs(sel);
        }
    }
    public static List<CRL> readCRLsFromCert(X509Certificate cert)
            throws Exception {
        List<CRL> crls = new ArrayList<>();
        CRLDistributionPointsExtension ext =
                X509CertImpl.toImpl(cert).getCRLDistributionPointsExtension();
        if (ext == null) return crls;
        for (DistributionPoint o: (List<DistributionPoint>)
                ext.get(CRLDistributionPointsExtension.POINTS)) {
            GeneralNames names = o.getFullName();
            if (names != null) {
                for (GeneralName name: names.names()) {
                    if (name.getType() == GeneralNameInterface.NAME_URI) {
                        URIName uriName = (URIName)name.getName();
                        for (CRL crl: KeyTool.loadCRLs(uriName.getName())) {
                            if (crl instanceof X509CRL) {
                                crls.add((X509CRL)crl);
                            }
                        }
                        break;  
                    }
                }
            }
        }
        return crls;
    }
    private static String verifyCRL(KeyStore ks, CRL crl)
            throws Exception {
        X509CRLImpl xcrl = (X509CRLImpl)crl;
        X500Principal issuer = xcrl.getIssuerX500Principal();
        for (String s: e2i(ks.aliases())) {
            Certificate cert = ks.getCertificate(s);
            if (cert instanceof X509Certificate) {
                X509Certificate xcert = (X509Certificate)cert;
                if (xcert.getSubjectX500Principal().equals(issuer)) {
                    try {
                        ((X509CRLImpl)crl).verify(cert.getPublicKey());
                        return s;
                    } catch (Exception e) {
                    }
                }
            }
        }
        return null;
    }
    private void doPrintCRL(String src, PrintStream out)
            throws Exception {
        for (CRL crl: loadCRLs(src)) {
            printCRL(crl, out);
            String issuer = null;
            if (caks != null) {
                issuer = verifyCRL(caks, crl);
                if (issuer != null) {
                    System.out.println("Verified by " + issuer + " in cacerts");
                }
            }
            if (issuer == null && keyStore != null) {
                issuer = verifyCRL(keyStore, crl);
                if (issuer != null) {
                    System.out.println("Verified by " + issuer + " in keystore");
                }
            }
            if (issuer == null) {
                out.println(rb.getString
                        ("STAR"));
                out.println("WARNING: not verified. Make sure -keystore and -alias are correct.");
                out.println(rb.getString
                        ("STARNN"));
            }
        }
    }
    private void printCRL(CRL crl, PrintStream out)
            throws Exception {
        if (rfc) {
            X509CRL xcrl = (X509CRL)crl;
            out.println("-----BEGIN X509 CRL-----");
            new BASE64Encoder().encodeBuffer(xcrl.getEncoded(), out);
            out.println("-----END X509 CRL-----");
        } else {
            out.println(crl.toString());
        }
    }
    private void doPrintCertReq(InputStream in, PrintStream out)
            throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuffer sb = new StringBuffer();
        boolean started = false;
        while (true) {
            String s = reader.readLine();
            if (s == null) break;
            if (!started) {
                if (s.startsWith("-----")) {
                    started = true;
                }
            } else {
                if (s.startsWith("-----")) {
                    break;
                }
                sb.append(s);
            }
        }
        PKCS10 req = new PKCS10(new BASE64Decoder().decodeBuffer(new String(sb)));
        PublicKey pkey = req.getSubjectPublicKeyInfo();
        out.printf(rb.getString("PKCS.10.Certificate.Request.Version.1.0.Subject.s.Public.Key.s.format.s.key."),
                req.getSubjectName(), pkey.getFormat(), pkey.getAlgorithm());
        for (PKCS10Attribute attr: req.getAttributes().getAttributes()) {
            ObjectIdentifier oid = attr.getAttributeId();
            if (oid.equals(PKCS9Attribute.EXTENSION_REQUEST_OID)) {
                CertificateExtensions exts = (CertificateExtensions)attr.getAttributeValue();
                if (exts != null) {
                    printExtensions(rb.getString("Extension.Request."), exts, out);
                }
            } else {
                out.println(attr.getAttributeId());
                out.println(attr.getAttributeValue());
            }
        }
        if (debug) {
            out.println(req);   
        }
    }
    private void printCertFromStream(InputStream in, PrintStream out)
        throws Exception
    {
        Collection<? extends Certificate> c = null;
        try {
            c = cf.generateCertificates(in);
        } catch (CertificateException ce) {
            throw new Exception(rb.getString("Failed.to.parse.input"), ce);
        }
        if (c.isEmpty()) {
            throw new Exception(rb.getString("Empty.input"));
        }
        Certificate[] certs = c.toArray(new Certificate[c.size()]);
        for (int i=0; i<certs.length; i++) {
            X509Certificate x509Cert = null;
            try {
                x509Cert = (X509Certificate)certs[i];
            } catch (ClassCastException cce) {
                throw new Exception(rb.getString("Not.X.509.certificate"));
            }
            if (certs.length > 1) {
                MessageFormat form = new MessageFormat
                        (rb.getString("Certificate.i.1."));
                Object[] source = {new Integer(i + 1)};
                out.println(form.format(source));
            }
            if (rfc) dumpCert(x509Cert, out);
            else printX509Cert(x509Cert, out);
            if (i < (certs.length-1)) {
                out.println();
            }
        }
    }
    private void doPrintCert(final PrintStream out) throws Exception {
        if (jarfile != null) {
            JarFile jf = new JarFile(jarfile, true);
            Enumeration<JarEntry> entries = jf.entries();
            Set<CodeSigner> ss = new HashSet<>();
            byte[] buffer = new byte[8192];
            int pos = 0;
            while (entries.hasMoreElements()) {
                JarEntry je = entries.nextElement();
                InputStream is = null;
                try {
                    is = jf.getInputStream(je);
                    while (is.read(buffer) != -1) {
                    }
                } finally {
                    if (is != null) {
                        is.close();
                    }
                }
                CodeSigner[] signers = je.getCodeSigners();
                if (signers != null) {
                    for (CodeSigner signer: signers) {
                        if (!ss.contains(signer)) {
                            ss.add(signer);
                            out.printf(rb.getString("Signer.d."), ++pos);
                            out.println();
                            out.println();
                            out.println(rb.getString("Signature."));
                            out.println();
                            for (Certificate cert: signer.getSignerCertPath().getCertificates()) {
                                X509Certificate x = (X509Certificate)cert;
                                if (rfc) {
                                    out.println(rb.getString("Certificate.owner.") + x.getSubjectDN() + "\n");
                                    dumpCert(x, out);
                                } else {
                                    printX509Cert(x, out);
                                }
                                out.println();
                            }
                            Timestamp ts = signer.getTimestamp();
                            if (ts != null) {
                                out.println(rb.getString("Timestamp."));
                                out.println();
                                for (Certificate cert: ts.getSignerCertPath().getCertificates()) {
                                    X509Certificate x = (X509Certificate)cert;
                                    if (rfc) {
                                        out.println(rb.getString("Certificate.owner.") + x.getSubjectDN() + "\n");
                                        dumpCert(x, out);
                                    } else {
                                        printX509Cert(x, out);
                                    }
                                    out.println();
                                }
                            }
                        }
                    }
                }
            }
            jf.close();
            if (ss.size() == 0) {
                out.println(rb.getString("Not.a.signed.jar.file"));
            }
        } else if (sslserver != null) {
            SSLContext sc = SSLContext.getInstance("SSL");
            final boolean[] certPrinted = new boolean[1];
            sc.init(null, new TrustManager[] {
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                        for (int i=0; i<certs.length; i++) {
                            X509Certificate cert = certs[i];
                            try {
                                if (rfc) {
                                    dumpCert(cert, out);
                                } else {
                                    out.println("Certificate #" + i);
                                    out.println("====================================");
                                    printX509Cert(cert, out);
                                    out.println();
                                }
                            } catch (Exception e) {
                                if (debug) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        if (certs.length > 0) {
                            certPrinted[0] = true;
                        }
                    }
                }
            }, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(
                    new HostnameVerifier() {
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    });
            Exception ex = null;
            try {
                new URL("https:
            } catch (Exception e) {
                ex = e;
            }
            if (!certPrinted[0]) {
                Exception e = new Exception(
                        rb.getString("No.certificate.from.the.SSL.server"));
                if (ex != null) {
                    e.initCause(ex);
                }
                throw e;
            }
        } else {
            InputStream inStream = System.in;
            if (filename != null) {
                inStream = new FileInputStream(filename);
            }
            try {
                printCertFromStream(inStream, out);
            } finally {
                if (inStream != System.in) {
                    inStream.close();
                }
            }
        }
    }
    private void doSelfCert(String alias, String dname, String sigAlgName)
        throws Exception
    {
        if (alias == null) {
            alias = keyAlias;
        }
        Pair<Key,char[]> objs = recoverKey(alias, storePass, keyPass);
        PrivateKey privKey = (PrivateKey)objs.fst;
        if (keyPass == null)
            keyPass = objs.snd;
        if (sigAlgName == null) {
            sigAlgName = getCompatibleSigAlgName(privKey.getAlgorithm());
        }
        Certificate oldCert = keyStore.getCertificate(alias);
        if (oldCert == null) {
            MessageFormat form = new MessageFormat
                (rb.getString("alias.has.no.public.key"));
            Object[] source = {alias};
            throw new Exception(form.format(source));
        }
        if (!(oldCert instanceof X509Certificate)) {
            MessageFormat form = new MessageFormat
                (rb.getString("alias.has.no.X.509.certificate"));
            Object[] source = {alias};
            throw new Exception(form.format(source));
        }
        byte[] encoded = oldCert.getEncoded();
        X509CertImpl certImpl = new X509CertImpl(encoded);
        X509CertInfo certInfo = (X509CertInfo)certImpl.get(X509CertImpl.NAME
                                                           + "." +
                                                           X509CertImpl.INFO);
        Date firstDate = getStartDate(startDate);
        Date lastDate = new Date();
        lastDate.setTime(firstDate.getTime() + validity*1000L*24L*60L*60L);
        CertificateValidity interval = new CertificateValidity(firstDate,
                                                               lastDate);
        certInfo.set(X509CertInfo.VALIDITY, interval);
        certInfo.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(
                    new java.util.Random().nextInt() & 0x7fffffff));
        X500Name owner;
        if (dname == null) {
            owner = (X500Name)certInfo.get(X509CertInfo.SUBJECT + "." +
                                           CertificateSubjectName.DN_NAME);
        } else {
            owner = new X500Name(dname);
            certInfo.set(X509CertInfo.SUBJECT + "." +
                         CertificateSubjectName.DN_NAME, owner);
        }
        certInfo.set(X509CertInfo.ISSUER + "." +
                     CertificateIssuerName.DN_NAME, owner);
        X509CertImpl newCert = new X509CertImpl(certInfo);
        newCert.sign(privKey, sigAlgName);
        AlgorithmId sigAlgid = (AlgorithmId)newCert.get(X509CertImpl.SIG_ALG);
        certInfo.set(CertificateAlgorithmId.NAME + "." +
                     CertificateAlgorithmId.ALGORITHM, sigAlgid);
        certInfo.set(X509CertInfo.VERSION,
                        new CertificateVersion(CertificateVersion.V3));
        CertificateExtensions ext = createV3Extensions(
                null,
                (CertificateExtensions)certInfo.get(X509CertInfo.EXTENSIONS),
                v3ext,
                oldCert.getPublicKey(),
                null);
        certInfo.set(X509CertInfo.EXTENSIONS, ext);
        newCert = new X509CertImpl(certInfo);
        newCert.sign(privKey, sigAlgName);
        keyStore.setKeyEntry(alias, privKey,
                             (keyPass != null) ? keyPass : storePass,
                             new Certificate[] { newCert } );
        if (verbose) {
            System.err.println(rb.getString("New.certificate.self.signed."));
            System.err.print(newCert.toString());
            System.err.println();
        }
    }
    private boolean installReply(String alias, InputStream in)
        throws Exception
    {
        if (alias == null) {
            alias = keyAlias;
        }
        Pair<Key,char[]> objs = recoverKey(alias, storePass, keyPass);
        PrivateKey privKey = (PrivateKey)objs.fst;
        if (keyPass == null) {
            keyPass = objs.snd;
        }
        Certificate userCert = keyStore.getCertificate(alias);
        if (userCert == null) {
            MessageFormat form = new MessageFormat
                (rb.getString("alias.has.no.public.key.certificate."));
            Object[] source = {alias};
            throw new Exception(form.format(source));
        }
        Collection<? extends Certificate> c = cf.generateCertificates(in);
        if (c.isEmpty()) {
            throw new Exception(rb.getString("Reply.has.no.certificates"));
        }
        Certificate[] replyCerts = c.toArray(new Certificate[c.size()]);
        Certificate[] newChain;
        if (replyCerts.length == 1) {
            newChain = establishCertChain(userCert, replyCerts[0]);
        } else {
            newChain = validateReply(alias, userCert, replyCerts);
        }
        if (newChain != null) {
            keyStore.setKeyEntry(alias, privKey,
                                 (keyPass != null) ? keyPass : storePass,
                                 newChain);
            return true;
        } else {
            return false;
        }
    }
    private boolean addTrustedCert(String alias, InputStream in)
        throws Exception
    {
        if (alias == null) {
            throw new Exception(rb.getString("Must.specify.alias"));
        }
        if (keyStore.containsAlias(alias)) {
            MessageFormat form = new MessageFormat(rb.getString
                ("Certificate.not.imported.alias.alias.already.exists"));
            Object[] source = {alias};
            throw new Exception(form.format(source));
        }
        X509Certificate cert = null;
        try {
            cert = (X509Certificate)cf.generateCertificate(in);
        } catch (ClassCastException cce) {
            throw new Exception(rb.getString("Input.not.an.X.509.certificate"));
        } catch (CertificateException ce) {
            throw new Exception(rb.getString("Input.not.an.X.509.certificate"));
        }
        boolean selfSigned = false;
        if (isSelfSigned(cert)) {
            cert.verify(cert.getPublicKey());
            selfSigned = true;
        }
        if (noprompt) {
            keyStore.setCertificateEntry(alias, cert);
            return true;
        }
        String reply = null;
        String trustalias = keyStore.getCertificateAlias(cert);
        if (trustalias != null) {
            MessageFormat form = new MessageFormat(rb.getString
                ("Certificate.already.exists.in.keystore.under.alias.trustalias."));
            Object[] source = {trustalias};
            System.err.println(form.format(source));
            reply = getYesNoReply
                (rb.getString("Do.you.still.want.to.add.it.no."));
        } else if (selfSigned) {
            if (trustcacerts && (caks != null) &&
                    ((trustalias=caks.getCertificateAlias(cert)) != null)) {
                MessageFormat form = new MessageFormat(rb.getString
                        ("Certificate.already.exists.in.system.wide.CA.keystore.under.alias.trustalias."));
                Object[] source = {trustalias};
                System.err.println(form.format(source));
                reply = getYesNoReply
                        (rb.getString("Do.you.still.want.to.add.it.to.your.own.keystore.no."));
            }
            if (trustalias == null) {
                printX509Cert(cert, System.out);
                reply = getYesNoReply
                        (rb.getString("Trust.this.certificate.no."));
            }
        }
        if (reply != null) {
            if ("YES".equals(reply)) {
                keyStore.setCertificateEntry(alias, cert);
                return true;
            } else {
                return false;
            }
        }
        try {
            Certificate[] chain = establishCertChain(null, cert);
            if (chain != null) {
                keyStore.setCertificateEntry(alias, cert);
                return true;
            }
        } catch (Exception e) {
            printX509Cert(cert, System.out);
            reply = getYesNoReply
                (rb.getString("Trust.this.certificate.no."));
            if ("YES".equals(reply)) {
                keyStore.setCertificateEntry(alias, cert);
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
    private char[] getNewPasswd(String prompt, char[] oldPasswd)
        throws Exception
    {
        char[] entered = null;
        char[] reentered = null;
        for (int count = 0; count < 3; count++) {
            MessageFormat form = new MessageFormat
                (rb.getString("New.prompt."));
            Object[] source = {prompt};
            System.err.print(form.format(source));
            entered = Password.readPassword(System.in);
            passwords.add(entered);
            if (entered == null || entered.length < 6) {
                System.err.println(rb.getString
                    ("Password.is.too.short.must.be.at.least.6.characters"));
            } else if (Arrays.equals(entered, oldPasswd)) {
                System.err.println(rb.getString("Passwords.must.differ"));
            } else {
                form = new MessageFormat
                        (rb.getString("Re.enter.new.prompt."));
                Object[] src = {prompt};
                System.err.print(form.format(src));
                reentered = Password.readPassword(System.in);
                passwords.add(reentered);
                if (!Arrays.equals(entered, reentered)) {
                    System.err.println
                        (rb.getString("They.don.t.match.Try.again"));
                } else {
                    Arrays.fill(reentered, ' ');
                    return entered;
                }
            }
            if (entered != null) {
                Arrays.fill(entered, ' ');
                entered = null;
            }
            if (reentered != null) {
                Arrays.fill(reentered, ' ');
                reentered = null;
            }
        }
        throw new Exception(rb.getString("Too.many.failures.try.later"));
    }
    private String getAlias(String prompt) throws Exception {
        if (prompt != null) {
            MessageFormat form = new MessageFormat
                (rb.getString("Enter.prompt.alias.name."));
            Object[] source = {prompt};
            System.err.print(form.format(source));
        } else {
            System.err.print(rb.getString("Enter.alias.name."));
        }
        return (new BufferedReader(new InputStreamReader(
                                        System.in))).readLine();
    }
    private String inputStringFromStdin(String prompt) throws Exception {
        System.err.print(prompt);
        return (new BufferedReader(new InputStreamReader(
                                        System.in))).readLine();
    }
    private char[] getKeyPasswd(String alias, String otherAlias,
                                char[] otherKeyPass)
        throws Exception
    {
        int count = 0;
        char[] keyPass = null;
        do {
            if (otherKeyPass != null) {
                MessageFormat form = new MessageFormat(rb.getString
                        ("Enter.key.password.for.alias."));
                Object[] source = {alias};
                System.err.println(form.format(source));
                form = new MessageFormat(rb.getString
                        (".RETURN.if.same.as.for.otherAlias."));
                Object[] src = {otherAlias};
                System.err.print(form.format(src));
            } else {
                MessageFormat form = new MessageFormat(rb.getString
                        ("Enter.key.password.for.alias."));
                Object[] source = {alias};
                System.err.print(form.format(source));
            }
            System.err.flush();
            keyPass = Password.readPassword(System.in);
            passwords.add(keyPass);
            if (keyPass == null) {
                keyPass = otherKeyPass;
            }
            count++;
        } while ((keyPass == null) && count < 3);
        if (keyPass == null) {
            throw new Exception(rb.getString("Too.many.failures.try.later"));
        }
        return keyPass;
    }
    private void printX509Cert(X509Certificate cert, PrintStream out)
        throws Exception
    {
        MessageFormat form = new MessageFormat
                (rb.getString(".PATTERN.printX509Cert"));
        Object[] source = {cert.getSubjectDN().toString(),
                        cert.getIssuerDN().toString(),
                        cert.getSerialNumber().toString(16),
                        cert.getNotBefore().toString(),
                        cert.getNotAfter().toString(),
                        getCertFingerPrint("MD5", cert),
                        getCertFingerPrint("SHA1", cert),
                        getCertFingerPrint("SHA-256", cert),
                        cert.getSigAlgName(),
                        cert.getVersion()
                        };
        out.println(form.format(source));
        if (cert instanceof X509CertImpl) {
            X509CertImpl impl = (X509CertImpl)cert;
            X509CertInfo certInfo = (X509CertInfo)impl.get(X509CertImpl.NAME
                                                           + "." +
                                                           X509CertImpl.INFO);
            CertificateExtensions exts = (CertificateExtensions)
                    certInfo.get(X509CertInfo.EXTENSIONS);
            if (exts != null) {
                printExtensions(rb.getString("Extensions."), exts, out);
            }
        }
    }
    private static void printExtensions(String title, CertificateExtensions exts, PrintStream out)
            throws Exception {
        int extnum = 0;
        Iterator<Extension> i1 = exts.getAllExtensions().iterator();
        Iterator<Extension> i2 = exts.getUnparseableExtensions().values().iterator();
        while (i1.hasNext() || i2.hasNext()) {
            Extension ext = i1.hasNext()?i1.next():i2.next();
            if (extnum == 0) {
                out.println();
                out.println(title);
                out.println();
            }
            out.print("#"+(++extnum)+": "+ ext);
            if (ext.getClass() == Extension.class) {
                byte[] v = ext.getExtensionValue();
                if (v.length == 0) {
                    out.println(rb.getString(".Empty.value."));
                } else {
                    new sun.misc.HexDumpEncoder().encodeBuffer(ext.getExtensionValue(), out);
                    out.println();
                }
            }
            out.println();
        }
    }
    private boolean isSelfSigned(X509Certificate cert) {
        return signedBy(cert, cert);
    }
    private boolean signedBy(X509Certificate end, X509Certificate ca) {
        if (!ca.getSubjectDN().equals(end.getIssuerDN())) {
            return false;
        }
        try {
            end.verify(ca.getPublicKey());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    private static Certificate getTrustedSigner(Certificate cert, KeyStore ks)
            throws Exception {
        if (ks.getCertificateAlias(cert) != null) {
            return cert;
        }
        for (Enumeration<String> aliases = ks.aliases();
                aliases.hasMoreElements(); ) {
            String name = aliases.nextElement();
            Certificate trustedCert = ks.getCertificate(name);
            if (trustedCert != null) {
                try {
                    cert.verify(trustedCert.getPublicKey());
                    return trustedCert;
                } catch (Exception e) {
                }
            }
        }
        return null;
    }
    private X500Name getX500Name() throws IOException {
        BufferedReader in;
        in = new BufferedReader(new InputStreamReader(System.in));
        String commonName = "Unknown";
        String organizationalUnit = "Unknown";
        String organization = "Unknown";
        String city = "Unknown";
        String state = "Unknown";
        String country = "Unknown";
        X500Name name;
        String userInput = null;
        int maxRetry = 20;
        do {
            if (maxRetry-- < 0) {
                throw new RuntimeException(rb.getString(
                        "Too.many.retries.program.terminated"));
            }
            commonName = inputString(in,
                    rb.getString("What.is.your.first.and.last.name."),
                    commonName);
            organizationalUnit = inputString(in,
                    rb.getString
                        ("What.is.the.name.of.your.organizational.unit."),
                    organizationalUnit);
            organization = inputString(in,
                    rb.getString("What.is.the.name.of.your.organization."),
                    organization);
            city = inputString(in,
                    rb.getString("What.is.the.name.of.your.City.or.Locality."),
                    city);
            state = inputString(in,
                    rb.getString("What.is.the.name.of.your.State.or.Province."),
                    state);
            country = inputString(in,
                    rb.getString
                        ("What.is.the.two.letter.country.code.for.this.unit."),
                    country);
            name = new X500Name(commonName, organizationalUnit, organization,
                                city, state, country);
            MessageFormat form = new MessageFormat
                (rb.getString("Is.name.correct."));
            Object[] source = {name};
            userInput = inputString
                (in, form.format(source), rb.getString("no"));
        } while (collator.compare(userInput, rb.getString("yes")) != 0 &&
                 collator.compare(userInput, rb.getString("y")) != 0);
        System.err.println();
        return name;
    }
    private String inputString(BufferedReader in, String prompt,
                               String defaultValue)
        throws IOException
    {
        System.err.println(prompt);
        MessageFormat form = new MessageFormat
                (rb.getString(".defaultValue."));
        Object[] source = {defaultValue};
        System.err.print(form.format(source));
        System.err.flush();
        String value = in.readLine();
        if (value == null || collator.compare(value, "") == 0) {
            value = defaultValue;
        }
        return value;
    }
    private void dumpCert(Certificate cert, PrintStream out)
        throws IOException, CertificateException
    {
        if (rfc) {
            BASE64Encoder encoder = new BASE64Encoder();
            out.println(X509Factory.BEGIN_CERT);
            encoder.encodeBuffer(cert.getEncoded(), out);
            out.println(X509Factory.END_CERT);
        } else {
            out.write(cert.getEncoded()); 
        }
    }
    private void byte2hex(byte b, StringBuffer buf) {
        char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
                            '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        int high = ((b & 0xf0) >> 4);
        int low = (b & 0x0f);
        buf.append(hexChars[high]);
        buf.append(hexChars[low]);
    }
    private String toHexString(byte[] block) {
        StringBuffer buf = new StringBuffer();
        int len = block.length;
        for (int i = 0; i < len; i++) {
             byte2hex(block[i], buf);
             if (i < len-1) {
                 buf.append(":");
             }
        }
        return buf.toString();
    }
    private Pair<Key,char[]> recoverKey(String alias, char[] storePass,
                                       char[] keyPass)
        throws Exception
    {
        Key key = null;
        if (keyStore.containsAlias(alias) == false) {
            MessageFormat form = new MessageFormat
                (rb.getString("Alias.alias.does.not.exist"));
            Object[] source = {alias};
            throw new Exception(form.format(source));
        }
        if (!keyStore.entryInstanceOf(alias, KeyStore.PrivateKeyEntry.class) &&
                !keyStore.entryInstanceOf(alias, KeyStore.SecretKeyEntry.class)) {
            MessageFormat form = new MessageFormat
                (rb.getString("Alias.alias.has.no.key"));
            Object[] source = {alias};
            throw new Exception(form.format(source));
        }
        if (keyPass == null) {
            try {
                key = keyStore.getKey(alias, storePass);
                keyPass = storePass;
                passwords.add(keyPass);
            } catch (UnrecoverableKeyException e) {
                if (!token) {
                    keyPass = getKeyPasswd(alias, null, null);
                    key = keyStore.getKey(alias, keyPass);
                } else {
                    throw e;
                }
            }
        } else {
            key = keyStore.getKey(alias, keyPass);
        }
        return Pair.of(key, keyPass);
    }
    private Pair<Entry,char[]> recoverEntry(KeyStore ks,
                            String alias,
                            char[] pstore,
                            char[] pkey) throws Exception {
        if (ks.containsAlias(alias) == false) {
            MessageFormat form = new MessageFormat
                (rb.getString("Alias.alias.does.not.exist"));
            Object[] source = {alias};
            throw new Exception(form.format(source));
        }
        PasswordProtection pp = null;
        Entry entry;
        try {
            entry = ks.getEntry(alias, pp);
            pkey = null;
        } catch (UnrecoverableEntryException une) {
            if(P11KEYSTORE.equalsIgnoreCase(ks.getType()) ||
                KeyStoreUtil.isWindowsKeyStore(ks.getType())) {
                throw une;
            }
            if (pkey != null) {
                pp = new PasswordProtection(pkey);
                entry = ks.getEntry(alias, pp);
            } else {
                try {
                    pp = new PasswordProtection(pstore);
                    entry = ks.getEntry(alias, pp);
                    pkey = pstore;
                } catch (UnrecoverableEntryException une2) {
                    if (P12KEYSTORE.equalsIgnoreCase(ks.getType())) {
                        throw une2;
                    } else {
                        pkey = getKeyPasswd(alias, null, null);
                        pp = new PasswordProtection(pkey);
                        entry = ks.getEntry(alias, pp);
                    }
                }
            }
        }
        return Pair.of(entry, pkey);
    }
    private String getCertFingerPrint(String mdAlg, Certificate cert)
        throws Exception
    {
        byte[] encCertInfo = cert.getEncoded();
        MessageDigest md = MessageDigest.getInstance(mdAlg);
        byte[] digest = md.digest(encCertInfo);
        return toHexString(digest);
    }
    private void printWarning() {
        System.err.println();
        System.err.println(rb.getString
            (".WARNING.WARNING.WARNING."));
        System.err.println(rb.getString
            (".The.integrity.of.the.information.stored.in.your.keystore."));
        System.err.println(rb.getString
            (".WARNING.WARNING.WARNING."));
        System.err.println();
    }
    private Certificate[] validateReply(String alias,
                                        Certificate userCert,
                                        Certificate[] replyCerts)
        throws Exception
    {
        int i;
        PublicKey userPubKey = userCert.getPublicKey();
        for (i=0; i<replyCerts.length; i++) {
            if (userPubKey.equals(replyCerts[i].getPublicKey())) {
                break;
            }
        }
        if (i == replyCerts.length) {
            MessageFormat form = new MessageFormat(rb.getString
                ("Certificate.reply.does.not.contain.public.key.for.alias."));
            Object[] source = {alias};
            throw new Exception(form.format(source));
        }
        Certificate tmpCert = replyCerts[0];
        replyCerts[0] = replyCerts[i];
        replyCerts[i] = tmpCert;
        X509Certificate thisCert = (X509Certificate)replyCerts[0];
        for (i=1; i < replyCerts.length-1; i++) {
            int j;
            for (j=i; j<replyCerts.length; j++) {
                if (signedBy(thisCert, (X509Certificate)replyCerts[j])) {
                    tmpCert = replyCerts[i];
                    replyCerts[i] = replyCerts[j];
                    replyCerts[j] = tmpCert;
                    thisCert = (X509Certificate)replyCerts[i];
                    break;
                }
            }
            if (j == replyCerts.length) {
                throw new Exception
                    (rb.getString("Incomplete.certificate.chain.in.reply"));
            }
        }
        if (noprompt) {
            return replyCerts;
        }
        Certificate topCert = replyCerts[replyCerts.length-1];
        Certificate root = getTrustedSigner(topCert, keyStore);
        if (root == null && trustcacerts && caks != null) {
            root = getTrustedSigner(topCert, caks);
        }
        if (root == null) {
            System.err.println();
            System.err.println
                    (rb.getString("Top.level.certificate.in.reply."));
            printX509Cert((X509Certificate)topCert, System.out);
            System.err.println();
            System.err.print(rb.getString(".is.not.trusted."));
            String reply = getYesNoReply
                    (rb.getString("Install.reply.anyway.no."));
            if ("NO".equals(reply)) {
                return null;
            }
        } else {
            if (root != topCert) {
                Certificate[] tmpCerts =
                    new Certificate[replyCerts.length+1];
                System.arraycopy(replyCerts, 0, tmpCerts, 0,
                                 replyCerts.length);
                tmpCerts[tmpCerts.length-1] = root;
                replyCerts = tmpCerts;
            }
        }
        return replyCerts;
    }
    private Certificate[] establishCertChain(Certificate userCert,
                                             Certificate certToVerify)
        throws Exception
    {
        if (userCert != null) {
            PublicKey origPubKey = userCert.getPublicKey();
            PublicKey replyPubKey = certToVerify.getPublicKey();
            if (!origPubKey.equals(replyPubKey)) {
                throw new Exception(rb.getString
                        ("Public.keys.in.reply.and.keystore.don.t.match"));
            }
            if (certToVerify.equals(userCert)) {
                throw new Exception(rb.getString
                        ("Certificate.reply.and.certificate.in.keystore.are.identical"));
            }
        }
        Hashtable<Principal, Vector<Certificate>> certs = null;
        if (keyStore.size() > 0) {
            certs = new Hashtable<Principal, Vector<Certificate>>(11);
            keystorecerts2Hashtable(keyStore, certs);
        }
        if (trustcacerts) {
            if (caks!=null && caks.size()>0) {
                if (certs == null) {
                    certs = new Hashtable<Principal, Vector<Certificate>>(11);
                }
                keystorecerts2Hashtable(caks, certs);
            }
        }
        Vector<Certificate> chain = new Vector<>(2);
        if (buildChain((X509Certificate)certToVerify, chain, certs)) {
            Certificate[] newChain = new Certificate[chain.size()];
            int j=0;
            for (int i=chain.size()-1; i>=0; i--) {
                newChain[j] = chain.elementAt(i);
                j++;
            }
            return newChain;
        } else {
            throw new Exception
                (rb.getString("Failed.to.establish.chain.from.reply"));
        }
    }
    private boolean buildChain(X509Certificate certToVerify,
                        Vector<Certificate> chain,
                        Hashtable<Principal, Vector<Certificate>> certs) {
        Principal issuer = certToVerify.getIssuerDN();
        if (isSelfSigned(certToVerify)) {
            chain.addElement(certToVerify);
            return true;
        }
        Vector<Certificate> vec = certs.get(issuer);
        if (vec == null) {
            return false;
        }
        for (Enumeration<Certificate> issuerCerts = vec.elements();
             issuerCerts.hasMoreElements(); ) {
            X509Certificate issuerCert
                = (X509Certificate)issuerCerts.nextElement();
            PublicKey issuerPubKey = issuerCert.getPublicKey();
            try {
                certToVerify.verify(issuerPubKey);
            } catch (Exception e) {
                continue;
            }
            if (buildChain(issuerCert, chain, certs)) {
                chain.addElement(certToVerify);
                return true;
            }
        }
        return false;
    }
    private String getYesNoReply(String prompt)
        throws IOException
    {
        String reply = null;
        int maxRetry = 20;
        do {
            if (maxRetry-- < 0) {
                throw new RuntimeException(rb.getString(
                        "Too.many.retries.program.terminated"));
            }
            System.err.print(prompt);
            System.err.flush();
            reply = (new BufferedReader(new InputStreamReader
                                        (System.in))).readLine();
            if (collator.compare(reply, "") == 0 ||
                collator.compare(reply, rb.getString("n")) == 0 ||
                collator.compare(reply, rb.getString("no")) == 0) {
                reply = "NO";
            } else if (collator.compare(reply, rb.getString("y")) == 0 ||
                       collator.compare(reply, rb.getString("yes")) == 0) {
                reply = "YES";
            } else {
                System.err.println(rb.getString("Wrong.answer.try.again"));
                reply = null;
            }
        } while (reply == null);
        return reply;
    }
    public static KeyStore getCacertsKeyStore()
        throws Exception
    {
        String sep = File.separator;
        File file = new File(System.getProperty("java.home") + sep
                             + "lib" + sep + "security" + sep
                             + "cacerts");
        if (!file.exists()) {
            return null;
        }
        FileInputStream fis = null;
        KeyStore caks = null;
        try {
            fis = new FileInputStream(file);
            caks = KeyStore.getInstance(JKS);
            caks.load(fis, null);
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
        return caks;
    }
    private void keystorecerts2Hashtable(KeyStore ks,
                Hashtable<Principal, Vector<Certificate>> hash)
        throws Exception {
        for (Enumeration<String> aliases = ks.aliases();
                                        aliases.hasMoreElements(); ) {
            String alias = aliases.nextElement();
            Certificate cert = ks.getCertificate(alias);
            if (cert != null) {
                Principal subjectDN = ((X509Certificate)cert).getSubjectDN();
                Vector<Certificate> vec = hash.get(subjectDN);
                if (vec == null) {
                    vec = new Vector<Certificate>();
                    vec.addElement(cert);
                } else {
                    if (!vec.contains(cert)) {
                        vec.addElement(cert);
                    }
                }
                hash.put(subjectDN, vec);
            }
        }
    }
    private static Date getStartDate(String s) throws IOException {
        Calendar c = new GregorianCalendar();
        if (s != null) {
            IOException ioe = new IOException(
                    rb.getString("Illegal.startdate.value"));
            int len = s.length();
            if (len == 0) {
                throw ioe;
            }
            if (s.charAt(0) == '-' || s.charAt(0) == '+') {
                int start = 0;
                while (start < len) {
                    int sign = 0;
                    switch (s.charAt(start)) {
                        case '+': sign = 1; break;
                        case '-': sign = -1; break;
                        default: throw ioe;
                    }
                    int i = start+1;
                    for (; i<len; i++) {
                        char ch = s.charAt(i);
                        if (ch < '0' || ch > '9') break;
                    }
                    if (i == start+1) throw ioe;
                    int number = Integer.parseInt(s.substring(start+1, i));
                    if (i >= len) throw ioe;
                    int unit = 0;
                    switch (s.charAt(i)) {
                        case 'y': unit = Calendar.YEAR; break;
                        case 'm': unit = Calendar.MONTH; break;
                        case 'd': unit = Calendar.DATE; break;
                        case 'H': unit = Calendar.HOUR; break;
                        case 'M': unit = Calendar.MINUTE; break;
                        case 'S': unit = Calendar.SECOND; break;
                        default: throw ioe;
                    }
                    c.add(unit, sign * number);
                    start = i + 1;
                }
            } else  {
                String date = null, time = null;
                if (len == 19) {
                    date = s.substring(0, 10);
                    time = s.substring(11);
                    if (s.charAt(10) != ' ')
                        throw ioe;
                } else if (len == 10) {
                    date = s;
                } else if (len == 8) {
                    time = s;
                } else {
                    throw ioe;
                }
                if (date != null) {
                    if (date.matches("\\d\\d\\d\\d\\/\\d\\d\\/\\d\\d")) {
                        c.set(Integer.valueOf(date.substring(0, 4)),
                                Integer.valueOf(date.substring(5, 7))-1,
                                Integer.valueOf(date.substring(8, 10)));
                    } else {
                        throw ioe;
                    }
                }
                if (time != null) {
                    if (time.matches("\\d\\d:\\d\\d:\\d\\d")) {
                        c.set(Calendar.HOUR_OF_DAY, Integer.valueOf(time.substring(0, 2)));
                        c.set(Calendar.MINUTE, Integer.valueOf(time.substring(0, 2)));
                        c.set(Calendar.SECOND, Integer.valueOf(time.substring(0, 2)));
                        c.set(Calendar.MILLISECOND, 0);
                    } else {
                        throw ioe;
                    }
                }
            }
        }
        return c.getTime();
    }
    private static int oneOf(String s, String... list) throws Exception {
        int[] match = new int[list.length];
        int nmatch = 0;
        int experiment = Integer.MAX_VALUE;
        for (int i = 0; i<list.length; i++) {
            String one = list[i];
            if (one == null) {
                experiment = i;
                continue;
            }
            if (one.toLowerCase(Locale.ENGLISH)
                    .startsWith(s.toLowerCase(Locale.ENGLISH))) {
                match[nmatch++] = i;
            } else {
                StringBuffer sb = new StringBuffer();
                boolean first = true;
                for (char c: one.toCharArray()) {
                    if (first) {
                        sb.append(c);
                        first = false;
                    } else {
                        if (!Character.isLowerCase(c)) {
                            sb.append(c);
                        }
                    }
                }
                if (sb.toString().equalsIgnoreCase(s)) {
                    match[nmatch++] = i;
                }
            }
        }
        if (nmatch == 0) {
            return -1;
        } else if (nmatch == 1) {
            return match[0];
        } else {
            if (match[1] > experiment) {
                return match[0];
            }
            StringBuffer sb = new StringBuffer();
            MessageFormat form = new MessageFormat(rb.getString
                ("command.{0}.is.ambiguous."));
            Object[] source = {s};
            sb.append(form.format(source));
            sb.append("\n    ");
            for (int i=0; i<nmatch && match[i]<experiment; i++) {
                sb.append(' ');
                sb.append(list[match[i]]);
            }
            throw new Exception(sb.toString());
        }
    }
    private GeneralName createGeneralName(String t, String v)
            throws Exception {
        GeneralNameInterface gn;
        int p = oneOf(t, "EMAIL", "URI", "DNS", "IP", "OID");
        if (p < 0) {
            throw new Exception(rb.getString(
                    "Unrecognized.GeneralName.type.") + t);
        }
        switch (p) {
            case 0: gn = new RFC822Name(v); break;
            case 1: gn = new URIName(v); break;
            case 2: gn = new DNSName(v); break;
            case 3: gn = new IPAddressName(v); break;
            default: gn = new OIDName(v); break; 
        }
        return new GeneralName(gn);
    }
    private static final String[] extSupported = {
                        "BasicConstraints",
                        "KeyUsage",
                        "ExtendedKeyUsage",
                        "SubjectAlternativeName",
                        "IssuerAlternativeName",
                        "SubjectInfoAccess",
                        "AuthorityInfoAccess",
                        null,
                        "CRLDistributionPoints",
    };
    private ObjectIdentifier findOidForExtName(String type)
            throws Exception {
        switch (oneOf(type, extSupported)) {
            case 0: return PKIXExtensions.BasicConstraints_Id;
            case 1: return PKIXExtensions.KeyUsage_Id;
            case 2: return PKIXExtensions.ExtendedKeyUsage_Id;
            case 3: return PKIXExtensions.SubjectAlternativeName_Id;
            case 4: return PKIXExtensions.IssuerAlternativeName_Id;
            case 5: return PKIXExtensions.SubjectInfoAccess_Id;
            case 6: return PKIXExtensions.AuthInfoAccess_Id;
            case 8: return PKIXExtensions.CRLDistributionPoints_Id;
            default: return new ObjectIdentifier(type);
        }
    }
    private CertificateExtensions createV3Extensions(
            CertificateExtensions reqex,
            CertificateExtensions ext,
            List <String> extstrs,
            PublicKey pkey,
            PublicKey akey) throws Exception {
        if (ext != null && reqex != null) {
            throw new Exception("One of request and original should be null.");
        }
        if (ext == null) ext = new CertificateExtensions();
        try {
            if (reqex != null) {
                for(String extstr: extstrs) {
                    if (extstr.toLowerCase(Locale.ENGLISH).startsWith("honored=")) {
                        List<String> list = Arrays.asList(
                                extstr.toLowerCase(Locale.ENGLISH).substring(8).split(","));
                        if (list.contains("all")) {
                            ext = reqex;    
                        }
                        for (String item: list) {
                            if (item.equals("all")) continue;
                            boolean add = true;
                            int action = -1;
                            String type = null;
                            if (item.startsWith("-")) {
                                add = false;
                                type = item.substring(1);
                            } else {
                                int colonpos = item.indexOf(':');
                                if (colonpos >= 0) {
                                    type = item.substring(0, colonpos);
                                    action = oneOf(item.substring(colonpos+1),
                                            "critical", "non-critical");
                                    if (action == -1) {
                                        throw new Exception(rb.getString
                                            ("Illegal.value.") + item);
                                    }
                                }
                            }
                            String n = reqex.getNameByOid(findOidForExtName(type));
                            if (add) {
                                Extension e = (Extension)reqex.get(n);
                                if (!e.isCritical() && action == 0
                                        || e.isCritical() && action == 1) {
                                    e = Extension.newExtension(
                                            e.getExtensionId(),
                                            !e.isCritical(),
                                            e.getExtensionValue());
                                    ext.set(n, e);
                                }
                            } else {
                                ext.delete(n);
                            }
                        }
                        break;
                    }
                }
            }
            for(String extstr: extstrs) {
                String name, value;
                boolean isCritical = false;
                int eqpos = extstr.indexOf('=');
                if (eqpos >= 0) {
                    name = extstr.substring(0, eqpos);
                    value = extstr.substring(eqpos+1);
                } else {
                    name = extstr;
                    value = null;
                }
                int colonpos = name.indexOf(':');
                if (colonpos >= 0) {
                    if (oneOf(name.substring(colonpos+1), "critical") == 0) {
                        isCritical = true;
                    }
                    name = name.substring(0, colonpos);
                }
                if (name.equalsIgnoreCase("honored")) {
                    continue;
                }
                int exttype = oneOf(name, extSupported);
                switch (exttype) {
                    case 0:     
                        int pathLen = -1;
                        boolean isCA = false;
                        if (value == null) {
                            isCA = true;
                        } else {
                            try {   
                                pathLen = Integer.parseInt(value);
                                isCA = true;
                            } catch (NumberFormatException ufe) {
                                for (String part: value.split(",")) {
                                    String[] nv = part.split(":");
                                    if (nv.length != 2) {
                                        throw new Exception(rb.getString
                                                ("Illegal.value.") + extstr);
                                    } else {
                                        if (nv[0].equalsIgnoreCase("ca")) {
                                            isCA = Boolean.parseBoolean(nv[1]);
                                        } else if (nv[0].equalsIgnoreCase("pathlen")) {
                                            pathLen = Integer.parseInt(nv[1]);
                                        } else {
                                            throw new Exception(rb.getString
                                                ("Illegal.value.") + extstr);
                                        }
                                    }
                                }
                            }
                        }
                        ext.set(BasicConstraintsExtension.NAME,
                                new BasicConstraintsExtension(isCritical, isCA,
                                pathLen));
                        break;
                    case 1:     
                        if(value != null) {
                            boolean[] ok = new boolean[9];
                            for (String s: value.split(",")) {
                                int p = oneOf(s,
                                       "digitalSignature",  
                                       "nonRepudiation",    
                                       "keyEncipherment",   
                                       "dataEncipherment",  
                                       "keyAgreement",      
                                       "keyCertSign",       
                                       "cRLSign",           
                                       "encipherOnly",      
                                       "decipherOnly",      
                                       "contentCommitment"  
                                       );
                                if (p < 0) {
                                    throw new Exception(rb.getString("Unknown.keyUsage.type.") + s);
                                }
                                if (p == 9) p = 1;
                                ok[p] = true;
                            }
                            KeyUsageExtension kue = new KeyUsageExtension(ok);
                            ext.set(KeyUsageExtension.NAME, Extension.newExtension(
                                    kue.getExtensionId(),
                                    isCritical,
                                    kue.getExtensionValue()));
                        } else {
                            throw new Exception(rb.getString
                                    ("Illegal.value.") + extstr);
                        }
                        break;
                    case 2:     
                        if(value != null) {
                            Vector<ObjectIdentifier> v = new Vector<>();
                            for (String s: value.split(",")) {
                                int p = oneOf(s,
                                        "anyExtendedKeyUsage",
                                        "serverAuth",       
                                        "clientAuth",       
                                        "codeSigning",      
                                        "emailProtection",  
                                        "",                 
                                        "",                 
                                        "",                 
                                        "timeStamping",     
                                        "OCSPSigning"       
                                       );
                                if (p < 0) {
                                    try {
                                        v.add(new ObjectIdentifier(s));
                                    } catch (Exception e) {
                                        throw new Exception(rb.getString(
                                                "Unknown.extendedkeyUsage.type.") + s);
                                    }
                                } else if (p == 0) {
                                    v.add(new ObjectIdentifier("2.5.29.37.0"));
                                } else {
                                    v.add(new ObjectIdentifier("1.3.6.1.5.5.7.3." + p));
                                }
                            }
                            ext.set(ExtendedKeyUsageExtension.NAME,
                                    new ExtendedKeyUsageExtension(isCritical, v));
                        } else {
                            throw new Exception(rb.getString
                                    ("Illegal.value.") + extstr);
                        }
                        break;
                    case 3:     
                    case 4:     
                        if(value != null) {
                            String[] ps = value.split(",");
                            GeneralNames gnames = new GeneralNames();
                            for(String item: ps) {
                                colonpos = item.indexOf(':');
                                if (colonpos < 0) {
                                    throw new Exception("Illegal item " + item + " in " + extstr);
                                }
                                String t = item.substring(0, colonpos);
                                String v = item.substring(colonpos+1);
                                gnames.add(createGeneralName(t, v));
                            }
                            if (exttype == 3) {
                                ext.set(SubjectAlternativeNameExtension.NAME,
                                        new SubjectAlternativeNameExtension(
                                            isCritical, gnames));
                            } else {
                                ext.set(IssuerAlternativeNameExtension.NAME,
                                        new IssuerAlternativeNameExtension(
                                            isCritical, gnames));
                            }
                        } else {
                            throw new Exception(rb.getString
                                    ("Illegal.value.") + extstr);
                        }
                        break;
                    case 5:     
                    case 6:     
                        if (isCritical) {
                            throw new Exception(rb.getString(
                                    "This.extension.cannot.be.marked.as.critical.") + extstr);
                        }
                        if(value != null) {
                            List<AccessDescription> accessDescriptions =
                                    new ArrayList<>();
                            String[] ps = value.split(",");
                            for(String item: ps) {
                                colonpos = item.indexOf(':');
                                int colonpos2 = item.indexOf(':', colonpos+1);
                                if (colonpos < 0 || colonpos2 < 0) {
                                    throw new Exception(rb.getString
                                            ("Illegal.value.") + extstr);
                                }
                                String m = item.substring(0, colonpos);
                                String t = item.substring(colonpos+1, colonpos2);
                                String v = item.substring(colonpos2+1);
                                int p = oneOf(m,
                                        "",
                                        "ocsp",         
                                        "caIssuers",    
                                        "timeStamping", 
                                        "",
                                        "caRepository"  
                                        );
                                ObjectIdentifier oid;
                                if (p < 0) {
                                    try {
                                        oid = new ObjectIdentifier(m);
                                    } catch (Exception e) {
                                        throw new Exception(rb.getString(
                                                "Unknown.AccessDescription.type.") + m);
                                    }
                                } else {
                                    oid = new ObjectIdentifier("1.3.6.1.5.5.7.48." + p);
                                }
                                accessDescriptions.add(new AccessDescription(
                                        oid, createGeneralName(t, v)));
                            }
                            if (exttype == 5) {
                                ext.set(SubjectInfoAccessExtension.NAME,
                                        new SubjectInfoAccessExtension(accessDescriptions));
                            } else {
                                ext.set(AuthorityInfoAccessExtension.NAME,
                                        new AuthorityInfoAccessExtension(accessDescriptions));
                            }
                        } else {
                            throw new Exception(rb.getString
                                    ("Illegal.value.") + extstr);
                        }
                        break;
                    case 8: 
                        if(value != null) {
                            String[] ps = value.split(",");
                            GeneralNames gnames = new GeneralNames();
                            for(String item: ps) {
                                colonpos = item.indexOf(':');
                                if (colonpos < 0) {
                                    throw new Exception("Illegal item " + item + " in " + extstr);
                                }
                                String t = item.substring(0, colonpos);
                                String v = item.substring(colonpos+1);
                                gnames.add(createGeneralName(t, v));
                            }
                            ext.set(CRLDistributionPointsExtension.NAME,
                                    new CRLDistributionPointsExtension(
                                        isCritical, Collections.singletonList(
                                        new DistributionPoint(gnames, null, null))));
                        } else {
                            throw new Exception(rb.getString
                                    ("Illegal.value.") + extstr);
                        }
                        break;
                    case -1:
                        ObjectIdentifier oid = new ObjectIdentifier(name);
                        byte[] data = null;
                        if (value != null) {
                            data = new byte[value.length() / 2 + 1];
                            int pos = 0;
                            for (char c: value.toCharArray()) {
                                int hex;
                                if (c >= '0' && c <= '9') {
                                    hex = c - '0' ;
                                } else if (c >= 'A' && c <= 'F') {
                                    hex = c - 'A' + 10;
                                } else if (c >= 'a' && c <= 'f') {
                                    hex = c - 'a' + 10;
                                } else {
                                    continue;
                                }
                                if (pos % 2 == 0) {
                                    data[pos/2] = (byte)(hex << 4);
                                } else {
                                    data[pos/2] += hex;
                                }
                                pos++;
                            }
                            if (pos % 2 != 0) {
                                throw new Exception(rb.getString(
                                        "Odd.number.of.hex.digits.found.") + extstr);
                            }
                            data = Arrays.copyOf(data, pos/2);
                        } else {
                            data = new byte[0];
                        }
                        ext.set(oid.toString(), new Extension(oid, isCritical,
                                new DerValue(DerValue.tag_OctetString, data)
                                        .toByteArray()));
                        break;
                    default:
                        throw new Exception(rb.getString(
                                "Unknown.extension.type.") + extstr);
                }
            }
            ext.set(SubjectKeyIdentifierExtension.NAME,
                    new SubjectKeyIdentifierExtension(
                        new KeyIdentifier(pkey).getIdentifier()));
            if (akey != null && !pkey.equals(akey)) {
                ext.set(AuthorityKeyIdentifierExtension.NAME,
                        new AuthorityKeyIdentifierExtension(
                        new KeyIdentifier(akey), null, null));
            }
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
        return ext;
    }
    private void usage() {
        if (command != null) {
            System.err.println("keytool " + command +
                    rb.getString(".OPTION."));
            System.err.println();
            System.err.println(rb.getString(command.description));
            System.err.println();
            System.err.println(rb.getString("Options."));
            System.err.println();
            String[] left = new String[command.options.length];
            String[] right = new String[command.options.length];
            boolean found = false;
            int lenLeft = 0;
            for (int j=0; j<left.length; j++) {
                Option opt = command.options[j];
                left[j] = opt.toString();
                if (opt.arg != null) left[j] += " " + opt.arg;
                if (left[j].length() > lenLeft) {
                    lenLeft = left[j].length();
                }
                right[j] = rb.getString(opt.description);
            }
            for (int j=0; j<left.length; j++) {
                System.err.printf(" %-" + lenLeft + "s  %s\n",
                        left[j], right[j]);
            }
            System.err.println();
            System.err.println(rb.getString(
                    "Use.keytool.help.for.all.available.commands"));
        } else {
            System.err.println(rb.getString(
                    "Key.and.Certificate.Management.Tool"));
            System.err.println();
            System.err.println(rb.getString("Commands."));
            System.err.println();
            for (Command c: Command.values()) {
                if (c == KEYCLONE) break;
                System.err.printf(" %-20s%s\n", c, rb.getString(c.description));
            }
            System.err.println();
            System.err.println(rb.getString(
                    "Use.keytool.command.name.help.for.usage.of.command.name"));
        }
    }
    private void tinyHelp() {
        usage();
        if (debug) {
            throw new RuntimeException("NO BIG ERROR, SORRY");
        } else {
            System.exit(1);
        }
    }
    private void errorNeedArgument(String flag) {
        Object[] source = {flag};
        System.err.println(new MessageFormat(
                rb.getString("Command.option.flag.needs.an.argument.")).format(source));
        tinyHelp();
    }
    private char[] getPass(String modifier, String arg) {
        char[] output = getPassWithModifier(modifier, arg);
        if (output != null) return output;
        tinyHelp();
        return null;    
    }
    public static char[] getPassWithModifier(String modifier, String arg) {
        if (modifier == null) {
            return arg.toCharArray();
        } else if (collator.compare(modifier, "env") == 0) {
            String value = System.getenv(arg);
            if (value == null) {
                System.err.println(rb.getString(
                        "Cannot.find.environment.variable.") + arg);
                return null;
            } else {
                return value.toCharArray();
            }
        } else if (collator.compare(modifier, "file") == 0) {
            try {
                URL url = null;
                try {
                    url = new URL(arg);
                } catch (java.net.MalformedURLException mue) {
                    File f = new File(arg);
                    if (f.exists()) {
                        url = f.toURI().toURL();
                    } else {
                        System.err.println(rb.getString(
                                "Cannot.find.file.") + arg);
                        return null;
                    }
                }
                BufferedReader br = new BufferedReader(new InputStreamReader(
                            url.openStream()));
                String value = br.readLine();
                br.close();
                if (value == null) {
                    return new char[0];
                } else {
                    return value.toCharArray();
                }
            } catch (IOException ioe) {
                System.err.println(ioe);
                return null;
            }
        } else {
            System.err.println(rb.getString("Unknown.password.type.") +
                    modifier);
            return null;
        }
    }
}
class Pair<A, B> {
    public final A fst;
    public final B snd;
    public Pair(A fst, B snd) {
        this.fst = fst;
        this.snd = snd;
    }
    public String toString() {
        return "Pair[" + fst + "," + snd + "]";
    }
    private static boolean equals(Object x, Object y) {
        return (x == null && y == null) || (x != null && x.equals(y));
    }
    public boolean equals(Object other) {
        return
            other instanceof Pair &&
            equals(fst, ((Pair)other).fst) &&
            equals(snd, ((Pair)other).snd);
    }
    public int hashCode() {
        if (fst == null) return (snd == null) ? 0 : snd.hashCode() + 1;
        else if (snd == null) return fst.hashCode() + 2;
        else return fst.hashCode() * 17 + snd.hashCode();
    }
    public static <A,B> Pair<A,B> of(A a, B b) {
        return new Pair<>(a,b);
    }
}
