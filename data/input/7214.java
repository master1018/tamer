public class StripProperties {
    private static void error(String msg, Exception e) {
        System.err.println("ERROR: stripproperties: " + msg);
        if ( e != null ) {
            System.err.println("EXCEPTION: " + e.toString());
            e.printStackTrace();
        }
    }
    private static List<String> parseOptions(String args[]) {
        List<String> files = new ArrayList<String>();
        for ( int i = 0; i < args.length ; i++ ) {
            if ( "-optionsfile".equals(args[i]) && i+1 < args.length ) {
                String filename = args[++i];
                FileInputStream finput = null;
                byte contents[] = null;
                try {
                    finput = new FileInputStream(filename);
                    int byteCount = finput.available();
                    if ( byteCount <= 0 ) {
                        error("The -optionsfile file is empty", null);
                        files = null;
                    } else {
                        contents = new byte[byteCount];
                        int bytesRead = finput.read(contents);
                        if ( byteCount != bytesRead ) {
                            error("Cannot read all of -optionsfile file", null);
                            files = null;
                        }
                    }
                } catch ( IOException e ) {
                    error("cannot open " + filename, e);
                    files = null;
                }
                if ( finput != null ) {
                    try {
                        finput.close();
                    } catch ( IOException e ) {
                        files = null;
                        error("cannot close " + filename, e);
                    }
                }
                if ( files != null && contents != null ) {
                    String tokens[] = (new String(contents)).split("\\s+");
                    if ( tokens.length > 0 ) {
                        List<String> ofiles = parseOptions(tokens);
                        if ( ofiles != null ) {
                            files.addAll(ofiles);
                        } else {
                            error("No files found in file", null);
                            files = null;
                        }
                    }
                }
                if ( files == null ) {
                    break;
                }
            } else {
                files.add(args[i]);
            }
        }
        return files;
    }
    private static boolean stripFiles(List<String> files) {
        boolean ok = true;
        for ( String file : files ) {
            Properties prop = new Properties();
            InputStream in = null;
            try {
                in = new BufferedInputStream(new FileInputStream(file));
                prop.load(in);
            } catch ( FileNotFoundException e ) {
                error("Cannot access file " + file, e);
                ok = false;
            } catch ( IOException e ) {
                error("IO exception processing file " + file, e);
                ok = false;
            }
            if ( in != null ) {
                try {
                    in.close();
                } catch ( IOException e ) {
                    error("IO exception closing file " + file, e);
                    ok = false;
                }
            }
            if ( !ok ) {
                break;
            }
            OutputStream out = null;
            try {
                out = new FileOutputStream(file);
                storeProperties(prop, out);
                out.flush();
            } catch ( IOException e ) {
                error("IO exception processing file " + file, e);
                ok = false;
            }
            if ( out != null ) {
                try {
                    out.close();
                } catch ( IOException e ) {
                    error("IO exception closing file " + file, e);
                    ok = false;
                }
            }
            if ( !ok ) {
                break;
            }
        }
        return ok;
    }
    public static void main(String args[]) {
        List<String> files = parseOptions(args);
        if ( files == null || !stripFiles(files) ) {
            System.exit(1);
        }
    }
    private static final String specialSaveChars = "=: \t\r\n\f#!";
    private static String saveConvert(String theString, boolean escapeSpace) {
        int len = theString.length();
        StringBuffer outBuffer = new StringBuffer(len*2);
        for(int x=0; x<len; x++) {
            char aChar = theString.charAt(x);
            switch(aChar) {
                case ' ':
                    if (x == 0 || escapeSpace) {
                        outBuffer.append('\\');
                    }
                    outBuffer.append(' ');
                    break;
                case '\\':
                    outBuffer.append('\\');
                    outBuffer.append('\\');
                    break;
                case '\t':
                    outBuffer.append('\\');
                    outBuffer.append('t');
                    break;
                case '\n':
                    outBuffer.append('\\');
                    outBuffer.append('n');
                    break;
                case '\r':
                    outBuffer.append('\\');
                    outBuffer.append('r');
                    break;
                case '\f':
                    outBuffer.append('\\');
                    outBuffer.append('f');
                    break;
                default:
                    if ((aChar < 0x0020) || (aChar == 0x007e) || (aChar > 0x00ff)) {
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
    private static void storeProperties(Properties properties, OutputStream out)
    throws IOException {
        BufferedWriter awriter;
        awriter = new BufferedWriter(new OutputStreamWriter(out, "8859_1"));
        for (Enumeration e = properties.keys(); e.hasMoreElements();) {
            String key = (String)e.nextElement();
            String val = (String)properties.get(key);
            key = saveConvert(key, true);
            val = saveConvert(val, false);
            writeln(awriter, key + "=" + val);
        }
        awriter.flush();
    }
    private static void writeln(BufferedWriter bw, String s) throws IOException {
        bw.write(s);
        bw.write("\n");
    }
    private static char toHex(int nibble) {
        return hexDigit[(nibble & 0xF)];
    }
    private static final char[] hexDigit = {
        '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
    };
}
