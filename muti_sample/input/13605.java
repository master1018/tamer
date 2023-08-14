public class LocaleDataTest
{
    public static void main(String[] args) throws Exception {
        BufferedReader in = null;
        PrintWriter out = null;
        boolean writeNewFile = false;
        boolean doThrow = true;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-w")) {
                writeNewFile = true;
                doThrow = false;
            }
            else if (args[i].equals("-nothrow"))
                doThrow = false;
            else if (args[i].equals("-s") && in == null)
                in = new BufferedReader(new EscapeReader(new InputStreamReader(System.in,
                                "ISO8859_1")));
            else if (!args[i].startsWith("-") && in == null)
                in = new BufferedReader(new EscapeReader(new InputStreamReader(new
                                FileInputStream(args[i]), "ISO8859_1")));
        }
        if (in == null) {
            File localeData = new File(System.getProperty("test.src", "."), "LocaleData");
            in = new BufferedReader(new EscapeReader(new InputStreamReader(new
                            FileInputStream(localeData), "ISO8859_1")));
        }
        out = new PrintWriter(new EscapeWriter(new OutputStreamWriter(System.out,
                        "ISO8859_1")), true);
        int errorCount = doTest(in, out, writeNewFile);
        if (errorCount != 0) {
            if (!writeNewFile)
                out.println("Test failed.  " + errorCount + " errors.");
            if (doThrow)
                throw new Exception("Test failed.  " + errorCount + " errors.");
        }
        else if (!writeNewFile)
            out.println("Test passed.");
        in.close();
        out.close();
    }
    static int doTest(BufferedReader in, PrintWriter out, boolean writeNewFile)
                    throws Exception {
        int errorCount = 0;
        String key = null;
        String expectedValue = null;
        String line = in.readLine();
        while (line != null) {
            if (line.startsWith("#") || line.length() == 0) {
                if (writeNewFile)
                    out.println(line);
            }
            else {
                int index  = line.indexOf("=");
                if (index == -1) {
                    key = line;
                    expectedValue = "";
                }
                else {
                    key = line.substring(0, index);
                    if (index + 1 == line.length())
                        expectedValue = "";
                    else
                        expectedValue = line.substring(index + 1);
                }
                if (!processLine(key, expectedValue, out, writeNewFile))
                    ++errorCount;
            }
            line = in.readLine();
        }
        return errorCount;
    }
    static boolean processLine(String key, String expectedValue, PrintWriter out,
                    boolean writeNewFile) throws Exception {
        String rbName, localeName, resTag, qualifier;
        String language = "", country = "", variant = "";
        int index, oldIndex;
        index = key.indexOf("/");
        if (index == -1 || index + 1 == key.length())
            throw new Exception("Malformed input file: no slashes in \"" + key + "\"");
        rbName = key.substring(0, index);
        oldIndex = index + 1;
        index = key.indexOf("/", oldIndex);
        if (index == -1 || index + 1 == key.length())
            throw new Exception("Malformed input file: \"" + key + "\" is missing locale name");
        localeName = key.substring(oldIndex, index);
        boolean use_tag = localeName.indexOf("-") != -1;
        if (use_tag == false && localeName.length() > 0) {
            language = localeName.substring(0, 2);
            if (localeName.length() > 3) {
                country = localeName.substring(3, 5);
                if (localeName.length() > 5)
                    variant = localeName.substring(6);
            }
        }
        oldIndex = index + 1;
        index = key.indexOf("/", oldIndex);
        if (index == -1)
            index = key.length();
        resTag = key.substring(oldIndex, index);
        if(resTag.endsWith("\\")) {
            resTag = resTag.substring(0, resTag.length() - 1);
            oldIndex = index;
            index = key.indexOf("/", oldIndex + 1);
            if (index == -1) index = key.length();
            resTag += key.substring(oldIndex, index);
        }
        if (index < key.length() - 1)
            qualifier = key.substring(index + 1);
        else
            qualifier = "";
        String retrievedValue = null;
        Object resource = null;
        try {
            String fullName = null;
            if (rbName.equals("CalendarData")
                    || rbName.equals("CurrencyNames")
                    || rbName.equals("LocaleNames")
                    || rbName.equals("TimeZoneNames")) {
                fullName = "sun.util.resources." + rbName;
            } else {
                fullName = "sun.text.resources." + rbName;
            }
            Locale locale;
            if (use_tag) {
                locale = Locale.forLanguageTag(localeName);
            } else {
                locale = new Locale(language, country, variant);
            }
            ResourceBundle bundle = ResourceBundle.getBundle(fullName,
                           locale,
                           ResourceBundle.Control.getNoFallbackControl(Control.FORMAT_DEFAULT));
            resource = bundle.getObject(resTag);
        }
        catch (MissingResourceException e) {
        }
        if (resource != null) {
            if (resource instanceof String) {
                retrievedValue = (String)resource;
            }
            else if (resource instanceof String[]) {
                int element = Integer.valueOf(qualifier).intValue();
                String[] stringList = (String[])resource;
                if (element >= 0 || element < stringList.length)
                    retrievedValue = stringList[element];
            }
            else if (resource instanceof String[][]) {
                String[][] stringArray = (String[][])resource;
                int slash = qualifier.indexOf("/");
                if (slash == -1) {
                    for (int i = 0; i < stringArray.length; i++) {
                        if (stringArray[i][0].equals(qualifier))
                            retrievedValue = stringArray[i][1];
                    }
                }
                else {
                    int row = Integer.valueOf(qualifier.substring(0, slash)).intValue();
                    int column = Integer.valueOf(qualifier.substring(slash + 1)).intValue();
                    if (row >= 0 || row < stringArray.length || column >= 0 || column <
                                    stringArray[row].length)
                        retrievedValue = stringArray[row][column];
                }
            }
        }
        if (retrievedValue == null || !retrievedValue.equals(expectedValue)) {
            if (retrievedValue == null)
                retrievedValue = "<MISSING!>";
            if (writeNewFile)
                out.println(key + "=" + retrievedValue);
            else {
                out.println("Mismatch in " + key + ":");
                out.println("  file = \"" + expectedValue + "\"");
                out.println("   jvm = \"" + retrievedValue + "\"");
            }
            return false;
        }
        else {
            if (writeNewFile)
                out.println(key + "=" + expectedValue);
        }
        return true;
    }
}
class EscapeReader extends FilterReader {
    public EscapeReader(Reader in) {
        super(in);
    }
    public int read() throws IOException {
        if (buffer != null) {
            String b = buffer.toString();
            int result = b.charAt(0);
            if (b.length() > 1)
                buffer = new StringBuffer(b.substring(1));
            else
                buffer = null;
            return result;
        }
        else {
            int result = super.read();
            if (result != '\\')
                return result;
            else {
                buffer = new StringBuffer();
                result = super.read();
                buffer.append((char)result);
                if (result == 'u') {
                    for (int i = 0; i < 4; i++) {
                        result = super.read();
                        if (result == -1)
                            break;
                        buffer.append((char)result);
                    }
                    String number = buffer.toString().substring(1);
                    result = Integer.parseInt(number, 16);
                    buffer = null;
                    return result;
                }
                return '\\';
            }
        }
    }
    public int read(char[] cbuf, int start, int len) throws IOException {
        int p = start;
        int end = start + len;
        int c = 0;
        while (c != -1 && p < end) {
            c = read();
            if (c != -1)
                cbuf[p++] = (char)c;
        }
        if (c == -1 && p == start)
            return -1;
        else
            return p - start;
    }
    private StringBuffer buffer = null;
}
class EscapeWriter extends FilterWriter {
    public EscapeWriter(Writer out) {
        super(out);
    }
    public void write(int c) throws IOException {
        if ((c >= ' ' && c <= '\u007e') || c == '\r' || c == '\n')
            super.write(c);
        else {
            super.write('\\');
            super.write('u');
            String number = Integer.toHexString(c);
            if (number.length() < 4)
                number = zeros.substring(0, 4 - number.length()) + number;
            super.write(number.charAt(0));
            super.write(number.charAt(1));
            super.write(number.charAt(2));
            super.write(number.charAt(3));
        }
    }
    public void write(char[] cbuf, int off, int len) throws IOException {
        int end = off + len;
        while (off < end)
            write(cbuf[off++]);
    }
    public void write(String str, int off, int len) throws IOException {
        int end = off + len;
        while (off < end)
            write(str.charAt(off++));
    }
    private static String zeros = "0000";
}
