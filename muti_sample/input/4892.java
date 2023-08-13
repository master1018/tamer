public class RTFEditorKit extends StyledEditorKit {
    public RTFEditorKit() {
        super();
    }
    public String getContentType() {
        return "text/rtf";
    }
    public void read(InputStream in, Document doc, int pos) throws IOException, BadLocationException {
        if (doc instanceof StyledDocument) {
            RTFReader rdr = new RTFReader((StyledDocument) doc);
            rdr.readFromStream(in);
            rdr.close();
        } else {
            super.read(in, doc, pos);
        }
    }
    public void write(OutputStream out, Document doc, int pos, int len)
        throws IOException, BadLocationException {
            RTFGenerator.writeDocument(doc, out);
    }
    public void read(Reader in, Document doc, int pos)
        throws IOException, BadLocationException {
        if (doc instanceof StyledDocument) {
            RTFReader rdr = new RTFReader((StyledDocument) doc);
            rdr.readFromReader(in);
            rdr.close();
        } else {
            super.read(in, doc, pos);
        }
    }
    public void write(Writer out, Document doc, int pos, int len)
        throws IOException, BadLocationException {
        throw new IOException("RTF is an 8-bit format");
    }
}
