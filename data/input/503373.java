public class ClassFileParser {
    private BufferedWriter bw; 
    public static void main(String[] args) throws IOException {
        ClassFileParser cfp = new ClassFileParser();
        cfp.process(args[0], args[1], args[2]);
    }
    private void process(final String srcDir, final String classesDir,
            final String absSrcFilePath) throws IOException {
        ClassPathOpener opener;
        String fileName = absSrcFilePath;
        String pckPath = fileName.substring(srcDir.length() + 1);
        String pck = pckPath.substring(0, pckPath.lastIndexOf("/"));
        String cName = pckPath.substring(pck.length() + 1);
        cName = cName.substring(0, cName.lastIndexOf("."));
        String cfName = pck+"/"+cName+".class";
        String inFile = classesDir + "/" + pck + "/" + cName + ".class";
        if (!new File(inFile).exists()) {
            throw new RuntimeException("cannot read:" + inFile);
        }
        byte[] bytes = FileUtils.readFile(inFile);
        String outFile = absSrcFilePath.substring(0, absSrcFilePath
                .lastIndexOf("/"))+ "/" + cName + ".cfh";
        Writer w;
        try {
            w = new OutputStreamWriter(new FileOutputStream(new File(outFile)));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("cannot write to file:"+outFile, e);
        }
        ClassFileParser.this.processFileBytes(w, cfName, bytes);
    }
    void processFileBytes(Writer w, String name, final byte[] allbytes) throws IOException {
        String fixedPathName = fixPath(name);
        DirectClassFile cf = new DirectClassFile(allbytes, fixedPathName, true);
        bw = new BufferedWriter(w);
        String className = fixedPathName.substring(0, fixedPathName.lastIndexOf("."));
        out("
        cf.setObserver(new ParseObserver() {
            private int cur_indent = 0;
            private int checkpos = 0;
            public void changeIndent(int indentDelta) {
                cur_indent += indentDelta;
            }
            public void startParsingMember(ByteArray bytes, int offset,
                    String name, String descriptor) {
                out("
                        + offset + ", len:" + (bytes.size() - offset)
                        + ",desc: " + descriptor);
            }
            public void endParsingMember(ByteArray bytes, int offset,
                    String name, String descriptor, Member member) {
                ByteArray ba = bytes.slice(offset, bytes.size());
                out("
                        + descriptor);
            }
            public void parsed(ByteArray bytes, int offset, int len,
                    String human) {
                human = human.replace('\n', ' ');
                out("
                        + ", h: " + human);
                if (len > 0) {
                    ByteArray ba = bytes.slice(offset, offset + len);
                    check(ba);
                    out("
                    out("   " + dumpBytes(ba));
                }
            }
            private void out(String msg) {
                ClassFileParser.this.out(msg, cur_indent);
            }
            private void check(ByteArray ba) {
                int len = ba.size();
                int offset = checkpos;
                for (int i = 0; i < len; i++) {
                    int b = ba.getByte(i);
                    byte b2 = allbytes[i + offset];
                    if (b != b2)
                        throw new RuntimeException("byte dump mismatch at pos "
                                + (i + offset));
                }
                checkpos += len;
            }
            private String dumpBytes(ByteArray ba) {
                String s = "";
                for (int i = 0; i < ba.size(); i++) {
                    int byt = ba.getUnsignedByte(i);
                    String hexVal = Integer.toHexString(byt);
                    if (hexVal.length() == 1) {
                        hexVal = "0" + hexVal;
                    }
                    s += hexVal + " ";
                }
                return s;
            }
            private String dumpReadableString(ByteArray ba) {
                String s = "";
                for (int i = 0; i < ba.size(); i++) {
                    int bb = ba.getUnsignedByte(i);
                    if (bb > 31 && bb < 127) {
                        s += (char) bb;
                    } else {
                        s += ".";
                    }
                    s += "  ";
                }
                return s;
            }
        });
        cf.setAttributeFactory(StdAttributeFactory.THE_ONE);
        cf.getMagic();
        bw.close();
    }
    private String getIndent(int indent) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent * 4; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }
    private void out(String msg, int cur_indent) {
        try {
            bw.write(getIndent(cur_indent) + msg);
            bw.newLine();
        } catch (IOException ioe) {
            throw new RuntimeException("error while writing to the writer", ioe);
        }
    }
    private static String fixPath(String path) {
        if (File.separatorChar == '\\') {
            path = path.replace('\\', '/');
        }
        int index = path.lastIndexOf("/./");
        if (index != -1) {
            return path.substring(index + 3);
        }
        if (path.startsWith("./")) {
            return path.substring(2);
        }
        return path;
    }
}
