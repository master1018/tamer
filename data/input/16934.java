public class CompileProperties {
    public static void main(String[] args) {
        CompileProperties cp = new CompileProperties();
        boolean ok = cp.run(args);
        if ( !ok ) {
            System.exit(1);
        }
    }
    static interface Log {
        void info(String msg);
        void verbose(String msg);
        void error(String msg, Exception e);
    }
    private String propfiles[];
    private String outfiles[] ;
    private String supers[]   ;
    private int compileCount = 0;
    private boolean quiet = false;
    private Log log;
    public void setLog(Log log) {
        this.log = log;
    }
    public boolean run(String[] args) {
        if (log == null) {
            log = new Log() {
                public void error(String msg, Exception e) {
                    System.err.println("ERROR: CompileProperties: " + msg);
                    if ( e != null ) {
                        System.err.println("EXCEPTION: " + e.toString());
                        e.printStackTrace();
                    }
                }
                public void info(String msg) {
                    System.out.println(msg);
                }
                public void verbose(String msg) {
                    if (!quiet)
                        System.out.println(msg);
                }
            };
        }
        boolean ok = true;
        if (args.length == 2 && args[0].charAt(0) != '-' ) {
            ok = createFile(args[0], args[1], "java.util.ListResourceBundle");
        } else if (args.length == 3) {
            ok = createFile(args[0], args[1], args[2]);
        } else if (args.length == 0) {
            usage(log);
            ok = false;
        } else {
            ok = parseOptions(args);
            if ( ok && compileCount == 0 ) {
                log.error("options parsed but no files to compile", null);
                ok = false;
            }
            if ( !ok ) {
                usage(log);
            } else {
                for ( int i = 0; i < compileCount && ok ; i++ ) {
                    ok = createFile(propfiles[i], outfiles[i], supers[i]);
                }
            }
        }
        return ok;
    }
    private boolean parseOptions(String args[]) {
        boolean ok = true;
        if ( compileCount > 0 ) {
            String new_propfiles[] = new String[compileCount + args.length];
            String new_outfiles[]  = new String[compileCount + args.length];
            String new_supers[]    = new String[compileCount + args.length];
            System.arraycopy(propfiles, 0, new_propfiles, 0, compileCount);
            System.arraycopy(outfiles, 0, new_outfiles, 0, compileCount);
            System.arraycopy(supers, 0, new_supers, 0, compileCount);
            propfiles = new_propfiles;
            outfiles  = new_outfiles;
            supers    = new_supers;
        } else {
            propfiles = new String[args.length];
            outfiles  = new String[args.length];
            supers    = new String[args.length];
        }
        for ( int i = 0; i < args.length ; i++ ) {
            if ( "-compile".equals(args[i]) && i+3 < args.length ) {
                propfiles[compileCount] = args[++i];
                outfiles[compileCount]  = args[++i];
                supers[compileCount]    = args[++i];
                compileCount++;
            } else if ( "-optionsfile".equals(args[i]) && i+1 < args.length ) {
                String filename = args[++i];
                FileInputStream finput = null;
                byte contents[] = null;
                try {
                    finput = new FileInputStream(filename);
                    int byteCount = finput.available();
                    if ( byteCount <= 0 ) {
                        log.error("The -optionsfile file is empty", null);
                        ok = false;
                    } else {
                        contents = new byte[byteCount];
                        int bytesRead = finput.read(contents);
                        if ( byteCount != bytesRead ) {
                            log.error("Cannot read all of -optionsfile file", null);
                            ok = false;
                        }
                    }
                } catch ( IOException e ) {
                    log.error("cannot open " + filename, e);
                    ok = false;
                }
                if ( finput != null ) {
                    try {
                        finput.close();
                    } catch ( IOException e ) {
                        ok = false;
                        log.error("cannot close " + filename, e);
                    }
                }
                if ( ok = true && contents != null ) {
                    String tokens[] = (new String(contents)).split("\\s+");
                    if ( tokens.length > 0 ) {
                        ok = parseOptions(tokens);
                    }
                }
                if ( !ok ) {
                    break;
                }
            } else if ( "-quiet".equals(args[i]) ) {
                quiet = true;
            } else {
                log.error("argument error", null);
                ok = false;
            }
        }
        return ok;
    }
    private boolean createFile(String propertiesPath, String outputPath,
            String superClass) {
        boolean ok = true;
        log.verbose("parsing: " + propertiesPath);
        Properties p = new Properties();
        try {
            p.load(new FileInputStream(propertiesPath));
        } catch ( FileNotFoundException e ) {
            ok = false;
            log.error("Cannot find file " + propertiesPath, e);
        } catch ( IOException e ) {
            ok = false;
            log.error("IO error on file " + propertiesPath, e);
        }
        if ( ok ) {
            String packageName = inferPackageName(propertiesPath, outputPath);
            log.verbose("inferred package name: " + packageName);
            List<String> sortedKeys = new ArrayList<String>();
            for ( Object key : p.keySet() ) {
                sortedKeys.add((String)key);
            }
            Collections.sort(sortedKeys);
            Iterator keys = sortedKeys.iterator();
            StringBuffer data = new StringBuffer();
            while (keys.hasNext()) {
                Object key = keys.next();
                data.append("            { \"" + escape((String)key) + "\", \"" +
                        escape((String)p.get(key)) + "\" },\n");
            }
            File file = new File(outputPath);
            String name = file.getName();
            int dotIndex = name.lastIndexOf('.');
            String className;
            if (dotIndex == -1) {
                className = name;
            } else {
                className = name.substring(0, dotIndex);
            }
            String packageString = "";
            if (packageName != null && !packageName.equals("")) {
                packageString = "package " + packageName + ";\n\n";
            }
            Writer writer = null;
            try {
                writer = new BufferedWriter(
                        new OutputStreamWriter(new FileOutputStream(outputPath), "8859_1"));
                MessageFormat format = new MessageFormat(FORMAT);
                writer.write(format.format(new Object[] { packageString, className, superClass, data }));
            } catch ( IOException e ) {
                ok = false;
                log.error("IO error writing to file " + outputPath, e);
            }
            if ( writer != null ) {
                try {
                    writer.flush();
                } catch ( IOException e ) {
                    ok = false;
                    log.error("IO error flush " + outputPath, e);
                }
                try {
                    writer.close();
                } catch ( IOException e ) {
                    ok = false;
                    log.error("IO error close " + outputPath, e);
                }
            }
            log.verbose("wrote: " + outputPath);
        }
        return ok;
    }
    private static void usage(Log log) {
        log.info("usage:");
        log.info("    java CompileProperties path_to_properties_file path_to_java_output_file [super_class]");
        log.info("      -OR-");
        log.info("    java CompileProperties {-compile path_to_properties_file path_to_java_output_file super_class} -or- -optionsfile filename");
        log.info("");
        log.info("Example:");
        log.info("    java CompileProperties -compile test.properties test.java java.util.ListResourceBundle");
        log.info("    java CompileProperties -optionsfile option_file");
        log.info("option_file contains: -compile test.properties test.java java.util.ListResourceBundle");
    }
    private static String escape(String theString) {
        int len = theString.length();
        StringBuffer outBuffer = new StringBuffer(len*2);
        for(int x=0; x<len; x++) {
            char aChar = theString.charAt(x);
            switch(aChar) {
                case '\\':outBuffer.append('\\'); outBuffer.append('\\');
                break;
                case '\t':outBuffer.append('\\'); outBuffer.append('t');
                break;
                case '\n':outBuffer.append('\\'); outBuffer.append('n');
                break;
                case '\r':outBuffer.append('\\'); outBuffer.append('r');
                break;
                case '\f':outBuffer.append('\\'); outBuffer.append('f');
                break;
                default:
                    if ((aChar < 0x0020) || (aChar > 0x007e)) {
                        outBuffer.append('\\');
                        outBuffer.append('u');
                        outBuffer.append(toHex((aChar >> 12) & 0xF));
                        outBuffer.append(toHex((aChar >>  8) & 0xF));
                        outBuffer.append(toHex((aChar >>  4) & 0xF));
                        outBuffer.append(toHex( aChar        & 0xF));
                    } else {
                        if (specialSaveChars.indexOf(aChar) != -1) {
                            outBuffer.append('\\');
                        }
                        outBuffer.append(aChar);
                    }
            }
        }
        return outBuffer.toString();
    }
    private static String inferPackageName(String inputPath, String outputPath) {
        inputPath  = new File(inputPath).getPath();
        outputPath = new File(outputPath).getPath();
        String sep;
        if (File.separatorChar == '\\') {
            sep = "\\\\";
        } else {
            sep = File.separator;
        }
        String[] inputs  = inputPath.split(sep);
        String[] outputs = outputPath.split(sep);
        int inStart  = 0;
        int inEnd    = inputs.length - 2;
        int outEnd   = outputs.length - 2;
        int i = inEnd;
        int j = outEnd;
        while (i >= 0 && j >= 0) {
            if (!inputs[i].equals(outputs[j]) ||
                    (inputs[i].equals("gensrc") && inputs[j].equals("gensrc"))) {
                ++i;
                ++j;
                break;
            }
            --i;
            --j;
        }
        String result;
        if (i < 0 || j < 0 || i >= inEnd || j >= outEnd) {
            result = "";
        } else {
            if (inputs[i].equals("classes") && outputs[j].equals("classes")) {
                ++i;
            }
            inStart = i;
            StringBuffer buf = new StringBuffer();
            for (i = inStart; i <= inEnd; i++) {
                buf.append(inputs[i]);
                if (i < inEnd) {
                    buf.append('.');
                }
            }
            result = buf.toString();
        }
        return result;
    }
    private static final String FORMAT =
            "{0}" +
            "public final class {1} extends {2} '{'\n" +
            "    protected final Object[][] getContents() '{'\n" +
            "        return new Object[][] '{'\n" +
            "{3}" +
            "        };\n" +
            "    }\n" +
            "}\n";
    private static char toHex(int nibble) {
        return hexDigit[(nibble & 0xF)];
    }
    private static final char[] hexDigit = {
        '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
    };
    private static final String specialSaveChars = "\"";
}
