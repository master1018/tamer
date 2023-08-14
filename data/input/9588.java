public class ApacheOctetStreamData extends OctetStreamData
    implements ApacheData {
    private XMLSignatureInput xi;
    public ApacheOctetStreamData(XMLSignatureInput xi)
        throws CanonicalizationException, IOException {
        super(xi.getOctetStream(), xi.getSourceURI(), xi.getMIMEType());
        this.xi = xi;
    }
    public XMLSignatureInput getXMLSignatureInput() {
        return xi;
    }
}
