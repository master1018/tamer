public class Builtin {
    private Builtin() {}
    private static final String BUILTIN =
        "<api>\n" +
        " <package name=\"java.util\">\n" +
        "  <class name=\"EnumSet\"\n" +
        "   extends=\"java.util.AbstractSet\">\n" +
        "   <method name=\"of\" return=\"java.util.EnumSet\">\n" +
        "    <parameter name=\"e\" type=\"java.lang.Enum\"/>\n" +
        "   </method>\n" +
        "   <method name=\"of\" return=\"java.util.EnumSet\">\n" +
        "    <parameter name=\"e1\" type=\"java.lang.Enum\"/>\n" +
        "    <parameter name=\"e2\" type=\"java.lang.Enum\"/>\n" +
        "   </method>\n" +
        "   <method name=\"of\" return=\"java.util.EnumSet\">\n" +
        "    <parameter name=\"e1\" type=\"java.lang.Enum\"/>\n" +
        "    <parameter name=\"e2\" type=\"java.lang.Enum\"/>\n" +
        "    <parameter name=\"e3\" type=\"java.lang.Enum\"/>\n" +
        "   </method>\n" +
        "   <method name=\"of\" return=\"java.util.EnumSet\">\n" +
        "    <parameter name=\"e1\" type=\"java.lang.Enum\"/>\n" +
        "    <parameter name=\"e2\" type=\"java.lang.Enum\"/>\n" +
        "    <parameter name=\"e3\" type=\"java.lang.Enum\"/>\n" +
        "    <parameter name=\"e4\" type=\"java.lang.Enum\"/>\n" +
        "   </method>\n" +
        "   <method name=\"of\" return=\"java.util.EnumSet\">\n" +
        "    <parameter name=\"e1\" type=\"java.lang.Enum\"/>\n" +
        "    <parameter name=\"e2\" type=\"java.lang.Enum\"/>\n" +
        "    <parameter name=\"e3\" type=\"java.lang.Enum\"/>\n" +
        "    <parameter name=\"e4\" type=\"java.lang.Enum\"/>\n" +
        "    <parameter name=\"e5\" type=\"java.lang.Enum\"/>\n" +
        "   </method>\n" +
        "  </class>\n" +
        " </package>\n" +
        " <package name=\"android.os\">\n" +
        "  <class name=\"RemoteCallbackList\"\n" +
        "   extends=\"java.lang.Object\">\n" +
        "   <method name=\"register\" return=\"boolean\">\n" +
        "    <parameter name=\"callback\" type=\"android.os.IInterface\"/>\n" +
        "   </method>\n" +
        "   <method name=\"unregister\" return=\"boolean\">\n" +
        "    <parameter name=\"callback\" type=\"android.os.IInterface\"/>\n" +
        "   </method>\n" +
        "  </class>\n" +
        "  <class name=\"AsyncTask\"\n" +
        "   extends=\"java.lang.Object\">\n" +
        "   <method name=\"onPostExecute\" return=\"void\">\n" +
        "    <parameter name=\"result\" type=\"java.lang.Object\"/>\n" +
        "   </method>\n" +
        "   <method name=\"onProgressUpdate\" return=\"void\">\n" +
        "    <parameter name=\"values\" type=\"java.lang.Object[]\"/>\n" +
        "   </method>\n" +
        "   <method name=\"execute\" return=\"android.os.AsyncTask\">\n" +
        "    <parameter name=\"params\" type=\"java.lang.Object[]\"/>\n" +
        "   </method>\n" +
        "  </class>\n" +
        " </package>\n" +
        " <package name=\"android.widget\">\n" +
        "  <class name=\"AutoCompleteTextView\"\n" +
        "   extends=\"android.widget.EditText\">\n" +
        "   <method name=\"setAdapter\" return=\"void\">\n" +
        "    <parameter name=\"adapter\" type=\"android.widget.ListAdapter\"/>\n" +
        "   </method>\n" +
        "  </class>\n" +
        " </package>\n" +
        "</api>\n"
        ;
    public static StringReader getReader() {
        return new StringReader(BUILTIN);
    }
}
