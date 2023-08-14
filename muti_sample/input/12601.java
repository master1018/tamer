class Driver {
        private static final ResourceBundle RESOURCE =
                ResourceBundle.getBundle("com.sun.java.util.jar.pack.DriverResource");
    public static void main(String[] ava) throws IOException {
        List<String> av = new ArrayList<>(Arrays.asList(ava));
        boolean doPack   = true;
        boolean doUnpack = false;
        boolean doRepack = false;
        boolean doZip = true;
        String logFile = null;
        String verboseProp = Utils.DEBUG_VERBOSE;
        {
            String arg0 = av.isEmpty() ? "" : av.get(0);
            switch (arg0) {
                case "--pack":
                av.remove(0);
                    break;
                case "--unpack":
                av.remove(0);
                doPack = false;
                doUnpack = true;
                    break;
            }
        }
        Map<String,String> engProps = new HashMap<>();
        engProps.put(verboseProp, System.getProperty(verboseProp));
        String optionMap;
        String[] propTable;
        if (doPack) {
            optionMap = PACK200_OPTION_MAP;
            propTable = PACK200_PROPERTY_TO_OPTION;
        } else {
            optionMap = UNPACK200_OPTION_MAP;
            propTable = UNPACK200_PROPERTY_TO_OPTION;
        }
        Map<String,String> avProps = new HashMap<>();
        try {
            for (;;) {
                String state = parseCommandOptions(av, optionMap, avProps);
            eachOpt:
                for (Iterator<String> opti = avProps.keySet().iterator();
                     opti.hasNext(); ) {
                    String opt = opti.next();
                    String prop = null;
                    for (int i = 0; i < propTable.length; i += 2) {
                        if (opt.equals(propTable[1+i])) {
                            prop = propTable[0+i];
                            break;
                        }
                    }
                    if (prop != null) {
                        String val = avProps.get(opt);
                        opti.remove();  
                        if (!prop.endsWith(".")) {
                            if (!(opt.equals("--verbose")
                                  || opt.endsWith("="))) {
                                boolean flag = (val != null);
                                if (opt.startsWith("--no-"))
                                    flag = !flag;
                                val = flag? "true": "false";
                            }
                            engProps.put(prop, val);
                        } else if (prop.contains(".attribute.")) {
                            for (String val1 : val.split("\0")) {
                                String[] val2 = val1.split("=", 2);
                                engProps.put(prop+val2[0], val2[1]);
                            }
                        } else {
                            int idx = 1;
                            for (String val1 : val.split("\0")) {
                                String prop1;
                                do {
                                    prop1 = prop+"cli."+(idx++);
                                } while (engProps.containsKey(prop1));
                                engProps.put(prop1, val1);
                            }
                        }
                    }
                }
                if ("--config-file=".equals(state)) {
                    String propFile = av.remove(0);
                    Properties fileProps = new Properties();
                    try (InputStream propIn = new FileInputStream(propFile)) {
                        fileProps.load(propIn);
                    }
                    if (engProps.get(verboseProp) != null)
                        fileProps.list(System.out);
                    for (Map.Entry<Object,Object> me : fileProps.entrySet()) {
                        engProps.put((String) me.getKey(), (String) me.getValue());
                    }
                } else if ("--version".equals(state)) {
                        System.out.println(MessageFormat.format(RESOURCE.getString(DriverResource.VERSION), Driver.class.getName(), "1.31, 07/05/05"));
                    return;
                } else if ("--help".equals(state)) {
                    printUsage(doPack, true, System.out);
                    System.exit(1);
                    return;
                } else {
                    break;
                }
            }
        } catch (IllegalArgumentException ee) {
                System.err.println(MessageFormat.format(RESOURCE.getString(DriverResource.BAD_ARGUMENT), ee));
            printUsage(doPack, false, System.err);
            System.exit(2);
            return;
        }
        for (String opt : avProps.keySet()) {
            String val = avProps.get(opt);
            switch (opt) {
                case "--repack":
                    doRepack = true;
                    break;
                case "--no-gzip":
                    doZip = (val == null);
                    break;
                case "--log-file=":
                    logFile = val;
                    break;
                default:
                    throw new InternalError(MessageFormat.format(
                            RESOURCE.getString(DriverResource.BAD_OPTION),
                            opt, avProps.get(opt)));
            }
        }
        if (logFile != null && !logFile.equals("")) {
            if (logFile.equals("-")) {
                System.setErr(System.out);
            } else {
                OutputStream log = new FileOutputStream(logFile);
                System.setErr(new PrintStream(log));
            }
        }
        boolean verbose = (engProps.get(verboseProp) != null);
        String packfile = "";
        if (!av.isEmpty())
            packfile = av.remove(0);
        String jarfile = "";
        if (!av.isEmpty())
            jarfile = av.remove(0);
        String newfile = "";  
        String bakfile = "";  
        String tmpfile = "";  
        if (doRepack) {
            if (packfile.toLowerCase().endsWith(".pack") ||
                packfile.toLowerCase().endsWith(".pac") ||
                packfile.toLowerCase().endsWith(".gz")) {
                System.err.println(MessageFormat.format(
                        RESOURCE.getString(DriverResource.BAD_REPACK_OUTPUT),
                        packfile));
                printUsage(doPack, false, System.err);
                System.exit(2);
            }
            newfile = packfile;
            if (jarfile.equals("")) {
                jarfile = newfile;
            }
            tmpfile = createTempFile(newfile, ".pack").getPath();
            packfile = tmpfile;
            doZip = false;  
        }
        if (!av.isEmpty()
            || !(jarfile.toLowerCase().endsWith(".jar")
                 || jarfile.toLowerCase().endsWith(".zip")
                 || (jarfile.equals("-") && !doPack))) {
            printUsage(doPack, false, System.err);
            System.exit(2);
            return;
        }
        if (doRepack)
            doPack = doUnpack = true;
        else if (doPack)
            doUnpack = false;
        Pack200.Packer jpack = Pack200.newPacker();
        Pack200.Unpacker junpack = Pack200.newUnpacker();
        jpack.properties().putAll(engProps);
        junpack.properties().putAll(engProps);
        if (doRepack && newfile.equals(jarfile)) {
            String zipc = getZipComment(jarfile);
            if (verbose && zipc.length() > 0)
                System.out.println(MessageFormat.format(RESOURCE.getString(DriverResource.DETECTED_ZIP_COMMENT), zipc));
            if (zipc.indexOf(Utils.PACK_ZIP_ARCHIVE_MARKER_COMMENT) >= 0) {
                    System.out.println(MessageFormat.format(RESOURCE.getString(DriverResource.SKIP_FOR_REPACKED), jarfile));
                        doPack = false;
                        doUnpack = false;
                        doRepack = false;
            }
        }
        try {
            if (doPack) {
                JarFile in = new JarFile(new File(jarfile));
                OutputStream out;
                if (packfile.equals("-")) {
                    out = System.out;
                    System.setOut(System.err);
                } else if (doZip) {
                    if (!packfile.endsWith(".gz")) {
                    System.err.println(MessageFormat.format(RESOURCE.getString(DriverResource.WRITE_PACK_FILE), packfile));
                        printUsage(doPack, false, System.err);
                        System.exit(2);
                    }
                    out = new FileOutputStream(packfile);
                    out = new BufferedOutputStream(out);
                    out = new GZIPOutputStream(out);
                } else {
                    if (!packfile.toLowerCase().endsWith(".pack") &&
                            !packfile.toLowerCase().endsWith(".pac")) {
                        System.err.println(MessageFormat.format(RESOURCE.getString(DriverResource.WIRTE_PACKGZ_FILE),packfile));
                        printUsage(doPack, false, System.err);
                        System.exit(2);
                    }
                    out = new FileOutputStream(packfile);
                    out = new BufferedOutputStream(out);
                }
                jpack.pack(in, out);
                out.close();
            }
            if (doRepack && newfile.equals(jarfile)) {
                File bakf = createTempFile(jarfile, ".bak");
                bakf.delete();
                boolean okBackup = new File(jarfile).renameTo(bakf);
                if (!okBackup) {
                        throw new Error(MessageFormat.format(RESOURCE.getString(DriverResource.SKIP_FOR_MOVE_FAILED),bakfile));
                } else {
                    bakfile = bakf.getPath();
                }
            }
            if (doUnpack) {
                InputStream in;
                if (packfile.equals("-"))
                    in = System.in;
                else
                    in = new FileInputStream(new File(packfile));
                BufferedInputStream inBuf = new BufferedInputStream(in);
                in = inBuf;
                if (Utils.isGZIPMagic(Utils.readMagic(inBuf))) {
                    in = new GZIPInputStream(in);
                }
                String outfile = newfile.equals("")? jarfile: newfile;
                OutputStream fileOut;
                if (outfile.equals("-"))
                    fileOut = System.out;
                else
                    fileOut = new FileOutputStream(outfile);
                fileOut = new BufferedOutputStream(fileOut);
                try (JarOutputStream out = new JarOutputStream(fileOut)) {
                    junpack.unpack(in, out);
                }
            }
            if (!bakfile.equals("")) {
                        new File(bakfile).delete();
                        bakfile = "";
            }
        } finally {
            if (!bakfile.equals("")) {
                File jarFile = new File(jarfile);
                jarFile.delete(); 
                new File(bakfile).renameTo(jarFile);
            }
            if (!tmpfile.equals(""))
                new File(tmpfile).delete();
        }
    }
    static private
    File createTempFile(String basefile, String suffix) throws IOException {
        File base = new File(basefile);
        String prefix = base.getName();
        if (prefix.length() < 3)  prefix += "tmp";
        File where = base.getParentFile();
        if ( base.getParentFile() == null && suffix.equals(".bak"))
            where = new File(".").getAbsoluteFile();
        File f = File.createTempFile(prefix, suffix, where);
        return f;
    }
    static private
    void printUsage(boolean doPack, boolean full, PrintStream out) {
        String prog = doPack ? "pack200" : "unpack200";
        String[] packUsage = (String[])RESOURCE.getObject(DriverResource.PACK_HELP);
        String[] unpackUsage = (String[])RESOURCE.getObject(DriverResource.UNPACK_HELP);
        String[] usage = doPack? packUsage: unpackUsage;
        for (int i = 0; i < usage.length; i++) {
            out.println(usage[i]);
            if (!full) {
            out.println(MessageFormat.format(RESOURCE.getString(DriverResource.MORE_INFO), prog));
                break;
            }
        }
    }
    static private
        String getZipComment(String jarfile) throws IOException {
        byte[] tail = new byte[1000];
        long filelen = new File(jarfile).length();
        if (filelen <= 0)  return "";
        long skiplen = Math.max(0, filelen - tail.length);
        try (InputStream in = new FileInputStream(new File(jarfile))) {
            in.skip(skiplen);
            in.read(tail);
            for (int i = tail.length-4; i >= 0; i--) {
                if (tail[i+0] == 'P' && tail[i+1] == 'K' &&
                    tail[i+2] ==  5  && tail[i+3] ==  6) {
                    i += 4+4+4+4+4+2;
                    if (i < tail.length)
                        return new String(tail, i, tail.length-i, "UTF8");
                    return "";
                }
            }
            return "";
        }
    }
    private static final String PACK200_OPTION_MAP =
        (""
         +"--repack                 $ \n  -r +>- @--repack              $ \n"
         +"--no-gzip                $ \n  -g +>- @--no-gzip             $ \n"
         +"--strip-debug            $ \n  -G +>- @--strip-debug         $ \n"
         +"--no-keep-file-order     $ \n  -O +>- @--no-keep-file-order  $ \n"
         +"--segment-limit=      *> = \n  -S +>  @--segment-limit=      = \n"
         +"--effort=             *> = \n  -E +>  @--effort=             = \n"
         +"--deflate-hint=       *> = \n  -H +>  @--deflate-hint=       = \n"
         +"--modification-time=  *> = \n  -m +>  @--modification-time=  = \n"
         +"--pass-file=        *> &\0 \n  -P +>  @--pass-file=        &\0 \n"
         +"--unknown-attribute=  *> = \n  -U +>  @--unknown-attribute=  = \n"
         +"--class-attribute=  *> &\0 \n  -C +>  @--class-attribute=  &\0 \n"
         +"--field-attribute=  *> &\0 \n  -F +>  @--field-attribute=  &\0 \n"
         +"--method-attribute= *> &\0 \n  -M +>  @--method-attribute= &\0 \n"
         +"--code-attribute=   *> &\0 \n  -D +>  @--code-attribute=   &\0 \n"
         +"--config-file=      *>   . \n  -f +>  @--config-file=        . \n"
         +"--no-strip-debug  !--strip-debug         \n"
         +"--gzip            !--no-gzip             \n"
         +"--keep-file-order !--no-keep-file-order  \n"
         +"--verbose                $ \n  -v +>- @--verbose             $ \n"
         +"--quiet        !--verbose  \n  -q +>- !--verbose               \n"
         +"--log-file=           *> = \n  -l +>  @--log-file=           = \n"
         +"--version                . \n  -V +>  @--version             . \n"
         +"--help               . \n  -? +> @--help . \n  -h +> @--help . \n"
         +"--           . \n"  
         +"-   +?    >- . \n"  
         );
    private static final String UNPACK200_OPTION_MAP =
        (""
         +"--deflate-hint=       *> = \n  -H +>  @--deflate-hint=       = \n"
         +"--verbose                $ \n  -v +>- @--verbose             $ \n"
         +"--quiet        !--verbose  \n  -q +>- !--verbose               \n"
         +"--remove-pack-file       $ \n  -r +>- @--remove-pack-file    $ \n"
         +"--log-file=           *> = \n  -l +>  @--log-file=           = \n"
         +"--config-file=        *> . \n  -f +>  @--config-file=        . \n"
         +"--           . \n"  
         +"-   +?    >- . \n"  
         +"--version                . \n  -V +>  @--version             . \n"
         +"--help               . \n  -? +> @--help . \n  -h +> @--help . \n"
         );
    private static final String[] PACK200_PROPERTY_TO_OPTION = {
        Pack200.Packer.SEGMENT_LIMIT, "--segment-limit=",
        Pack200.Packer.KEEP_FILE_ORDER, "--no-keep-file-order",
        Pack200.Packer.EFFORT, "--effort=",
        Pack200.Packer.DEFLATE_HINT, "--deflate-hint=",
        Pack200.Packer.MODIFICATION_TIME, "--modification-time=",
        Pack200.Packer.PASS_FILE_PFX, "--pass-file=",
        Pack200.Packer.UNKNOWN_ATTRIBUTE, "--unknown-attribute=",
        Pack200.Packer.CLASS_ATTRIBUTE_PFX, "--class-attribute=",
        Pack200.Packer.FIELD_ATTRIBUTE_PFX, "--field-attribute=",
        Pack200.Packer.METHOD_ATTRIBUTE_PFX, "--method-attribute=",
        Pack200.Packer.CODE_ATTRIBUTE_PFX, "--code-attribute=",
        Utils.DEBUG_VERBOSE, "--verbose",
        Utils.COM_PREFIX+"strip.debug", "--strip-debug",
    };
    private static final String[] UNPACK200_PROPERTY_TO_OPTION = {
        Pack200.Unpacker.DEFLATE_HINT, "--deflate-hint=",
        Utils.DEBUG_VERBOSE, "--verbose",
        Utils.UNPACK_REMOVE_PACKFILE, "--remove-pack-file",
    };
    private static
    String parseCommandOptions(List<String> args,
                               String options,
                               Map<String,String> properties) {
        String resultString = null;
        TreeMap<String,String[]> optmap = new TreeMap<>();
    loadOptmap:
        for (String optline : options.split("\n")) {
            String[] words = optline.split("\\p{Space}+");
            if (words.length == 0)    continue loadOptmap;
            String opt = words[0];
            words[0] = "";  
            if (opt.length() == 0 && words.length >= 1) {
                opt = words[1];  
                words[1] = "";
            }
            if (opt.length() == 0)    continue loadOptmap;
            String[] prevWords = optmap.put(opt, words);
            if (prevWords != null)
            throw new RuntimeException(MessageFormat.format(RESOURCE.getString(DriverResource.DUPLICATE_OPTION), optline.trim()));
        }
        ListIterator<String> argp = args.listIterator();
        ListIterator<String> pbp = new ArrayList<String>().listIterator();
    doArgs:
        for (;;) {
            String arg;
            if (pbp.hasPrevious()) {
                arg = pbp.previous();
                pbp.remove();
            } else if (argp.hasNext()) {
                arg = argp.next();
            } else {
                break doArgs;
            }
        tryOpt:
            for (int optlen = arg.length(); ; optlen--) {
                String opt;
            findOpt:
                for (;;) {
                    opt = arg.substring(0, optlen);
                    if (optmap.containsKey(opt))  break findOpt;
                    if (optlen == 0)              break tryOpt;
                    SortedMap<String,String[]> pfxmap = optmap.headMap(opt);
                    int len = pfxmap.isEmpty() ? 0 : pfxmap.lastKey().length();
                    optlen = Math.min(len, optlen - 1);
                    opt = arg.substring(0, optlen);
                }
                opt = opt.intern();
                assert(arg.startsWith(opt));
                assert(opt.length() == optlen);
                String val = arg.substring(optlen);  
                boolean didAction = false;
                boolean isError = false;
                int pbpMark = pbp.nextIndex();  
                String[] specs = optmap.get(opt);
            eachSpec:
                for (String spec : specs) {
                    if (spec.length() == 0)     continue eachSpec;
                    if (spec.startsWith("#"))   break eachSpec;
                    int sidx = 0;
                    char specop = spec.charAt(sidx++);
                    boolean ok;
                    switch (specop) {
                    case '+':
                        ok = (val.length() != 0);
                        specop = spec.charAt(sidx++);
                        break;
                    case '*':
                        ok = true;
                        specop = spec.charAt(sidx++);
                        break;
                    default:
                        ok = (val.length() == 0);
                        break;
                    }
                    if (!ok)  continue eachSpec;
                    String specarg = spec.substring(sidx);
                    switch (specop) {
                    case '.':  
                        resultString = (specarg.length() != 0)? specarg.intern(): opt;
                        break doArgs;
                    case '?':  
                        resultString = (specarg.length() != 0)? specarg.intern(): arg;
                        isError = true;
                        break eachSpec;
                    case '@':  
                        opt = specarg.intern();
                        break;
                    case '>':  
                        pbp.add(specarg + val);  
                        val = "";
                        break;
                    case '!':  
                        String negopt = (specarg.length() != 0)? specarg.intern(): opt;
                        properties.remove(negopt);
                        properties.put(negopt, null);  
                        didAction = true;
                        break;
                    case '$':  
                        String boolval;
                        if (specarg.length() != 0) {
                            boolval = specarg;
                        } else {
                            String old = properties.get(opt);
                            if (old == null || old.length() == 0) {
                                boolval = "1";
                            } else {
                                boolval = ""+(1+Integer.parseInt(old));
                            }
                        }
                        properties.put(opt, boolval);
                        didAction = true;
                        break;
                    case '=':  
                    case '&':  
                        boolean append = (specop == '&');
                        String strval;
                        if (pbp.hasPrevious()) {
                            strval = pbp.previous();
                            pbp.remove();
                        } else if (argp.hasNext()) {
                            strval = argp.next();
                        } else {
                            resultString = arg + " ?";
                            isError = true;
                            break eachSpec;
                        }
                        if (append) {
                            String old = properties.get(opt);
                            if (old != null) {
                                String delim = specarg;
                                if (delim.length() == 0)  delim = " ";
                                strval = old + specarg + strval;
                            }
                        }
                        properties.put(opt, strval);
                        didAction = true;
                        break;
                    default:
                        throw new RuntimeException(MessageFormat.format(RESOURCE.getString(DriverResource.BAD_SPEC),opt, spec));
                    }
                }
                if (didAction && !isError) {
                    continue doArgs;
                }
                while (pbp.nextIndex() > pbpMark) {
                    pbp.previous();
                    pbp.remove();
                }
                if (isError) {
                    throw new IllegalArgumentException(resultString);
                }
                if (optlen == 0) {
                    break tryOpt;
                }
            }
            pbp.add(arg);
            break doArgs;
        }
        args.subList(0, argp.nextIndex()).clear();
        while (pbp.hasPrevious()) {
            args.add(0, pbp.previous());
        }
        return resultString;
    }
}
