public class Proofread
{
    static FileWriter out = null;
    static final Pattern WHITESPACE = Pattern.compile("\\r?\\n");
    static final String INDENT = "        ";
    static final String NEWLINE = "\n" + INDENT;
    public static void initProofread(String filename)
    {
        try {
            out = new FileWriter(filename);
            out.write("javadoc proofread file: " + filename + "\n");
        }
        catch (IOException e) {
            if (out != null) {
                try {
                    out.close();
                }
                catch (IOException ex) {
                }
                out = null;
            }
            System.err.println("error opening file: " + filename);
        }
    }
    public static void finishProofread(String filename)
    {
        if (out == null) {
            return;
        }
        try {
            out.close();
        }
        catch (IOException e) {
        }
    }
    public static void write(String s)
    {
        if (out == null) {
            return ;
        }
        try {
            out.write(s);
        }
        catch (IOException e) {
        }
    }
    public static void writeIndented(String s)
    {
        s = s.trim();
        Matcher m = WHITESPACE.matcher(s);
        s = m.replaceAll(NEWLINE);
        write(INDENT);
        write(s);
        write("\n");
    }
    public static void writeFileHeader(String filename)
    {
        write("\n\n=== ");
        write(filename);
        write(" ===\n");
    }
    public static void writeTagList(TagInfo[] tags)
    {
        if (out == null) {
            return;
        }
        for (TagInfo t: tags) {
            String k = t.kind();
            if ("Text".equals(t.name())) {
                writeIndented(t.text());
            }
            else if ("@more".equals(k)) {
                writeIndented("");
            }
            else if ("@see".equals(k)) {
                SeeTagInfo see = (SeeTagInfo)t;
                String label = see.label();
                if (label == null) {
                    label = "";
                }
                writeIndented("{" + see.name() + " ... " + label + "}");
            }
            else if ("@code".equals(k)) {
                writeIndented(t.text());
            }
            else if ("@samplecode".equals(k)) {
                writeIndented(t.text());
            }
            else {
                writeIndented("{" + (t.name() != null ? t.name() : "") + "/" +
                        t.text() + "}");
            }
        }
    }
    public static void writePackages(String filename, TagInfo[] tags)
    {
        if (out == null) {
            return;
        }
        writeFileHeader(filename);
        writeTagList(tags);
    }
    public static void writePackage(String filename, TagInfo[] tags)
    {
        if (out == null) {
            return;
        }
        writeFileHeader(filename);
        writeTagList(tags);
    }
    public static void writeClass(String filename, ClassInfo cl)
    {
        if (out == null) {
            return;
        }
        writeFileHeader(filename);
        writeTagList(cl.inlineTags());
        for (FieldInfo f: cl.enumConstants()) {
            write("ENUM: " + f.name() + "\n");
            writeTagList(f.inlineTags());
        }
        for (FieldInfo f: cl.selfFields()) {
            write("FIELD: " + f.name() + "\n");
            writeTagList(f.inlineTags());
        }
        for (MethodInfo m: cl.constructors()) {
            write("CONSTRUCTOR: " + m.name() + "\n");
            writeTagList(m.inlineTags().tags());
        }
        for (MethodInfo m: cl.selfMethods()) {
            write("METHOD: " + m.name() + "\n");
            writeTagList(m.inlineTags().tags());
        }
    }
}
