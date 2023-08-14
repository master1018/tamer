public class XmlPrimitiveSerializer implements PrimitiveSerializer {
    private String mVersionNs;
    private String mTransacNs;
    public XmlPrimitiveSerializer(String versionNs, String transacNs) {
        mVersionNs = versionNs;
        mTransacNs = transacNs;
    }
    public void serialize(Primitive primitive, OutputStream out) throws IOException {
        try {
            Writer writer = 
                new BufferedWriter(new OutputStreamWriter(out, "UTF-8"), 8192);
            PrimitiveElement elem = primitive.createMessage(mVersionNs,
                    mTransacNs);
            writeElement(writer, elem);
            writer.flush();
        } catch (UnsupportedEncodingException e) {
            ImpsLog.logError(e);
        }
    }
    private void writeElement(Writer writer, PrimitiveElement element)
            throws IOException {
        writer.write('<');
        writer.write(element.getTagName());
        Map<String, String> attrMap = element.getAttributes();
        if(attrMap != null && attrMap.size() > 0) {
            for (Map.Entry<String, String> entry : attrMap.entrySet()) {
                writer.write(' ');
                writer.write(entry.getKey());
                writer.write("=\"");
                writeEncoded(writer, entry.getValue());
                writer.write('"');
            }
        }
        if (element.getContents() != null) {
            writer.write('>');
            writeEncoded(writer, element.getContents());
            writer.write("</");
            writer.write(element.getTagName());
            writer.write('>');
        } else if (element.getChildCount() > 0) {
            writer.write('>');
            for (PrimitiveElement child : element.getChildren()) {
                writeElement(writer, child);
            }
            writer.write("</");
            writer.write(element.getTagName());
            writer.write('>');
        } else {
            writer.write("/>");
        }
    }
    private void writeEncoded(Writer writer, String str) throws IOException {
        int len = str.length();
        for (int i = 0; i < len; i++) {
            char ch = str.charAt(i);
            switch (ch) {
                case '<':
                    writer.write("&lt;");
                    break;
                case '>':
                    writer.write("&gt;");
                    break;
                case '&':
                    writer.write("&amp;");
                    break;
                case '"':
                    writer.write("&quot;");
                    break;
                case '\'':
                    writer.write("&apos;");
                    break;
                default:
                    writer.write(ch);
            }
        }
    }
}
