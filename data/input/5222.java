public class CommandLine {
    public static String[] parse(String[] args)
        throws IOException
    {
        ListBuffer<String> newArgs = new ListBuffer<String>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.length() > 1 && arg.charAt(0) == '@') {
                arg = arg.substring(1);
                if (arg.charAt(0) == '@') {
                    newArgs.append(arg);
                } else {
                    loadCmdFile(arg, newArgs);
                }
            } else {
                newArgs.append(arg);
            }
        }
        return newArgs.toList().toArray(new String[newArgs.length()]);
    }
    private static void loadCmdFile(String name, ListBuffer<String> args)
        throws IOException
    {
        Reader r = new BufferedReader(new FileReader(name));
        StreamTokenizer st = new StreamTokenizer(r);
        st.resetSyntax();
        st.wordChars(' ', 255);
        st.whitespaceChars(0, ' ');
        st.commentChar('#');
        st.quoteChar('"');
        st.quoteChar('\'');
        while (st.nextToken() != StreamTokenizer.TT_EOF) {
            args.append(st.sval);
        }
        r.close();
    }
}
