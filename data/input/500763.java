public abstract class SimpleContentHandler extends  AbstractContentHandler {
    public abstract void headers(Header header);
    public abstract void bodyDecoded(BodyDescriptor bd, InputStream is) throws IOException;
    private Header currHeader;
    public final void startHeader() {
        currHeader = new Header();
    }
    public final void field(String fieldData) {
        currHeader.addField(Field.parse(fieldData));
    }
    public final void endHeader() {
        Header tmp = currHeader;
        currHeader = null;
        headers(tmp);
    }
    public final void body(BodyDescriptor bd, InputStream is) throws IOException {
        if (bd.isBase64Encoded()) {
            bodyDecoded(bd, new Base64InputStream(is));
        }
        else if (bd.isQuotedPrintableEncoded()) {
            bodyDecoded(bd, new QuotedPrintableInputStream(is));
        }
        else {
            bodyDecoded(bd, is);
        }
    }
}
