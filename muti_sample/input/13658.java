public abstract class CanonicalizerSpi {
   public byte[] engineCanonicalize(byte[] inputBytes)
           throws javax.xml.parsers.ParserConfigurationException,
                  java.io.IOException, org.xml.sax.SAXException,
                  CanonicalizationException {
      java.io.ByteArrayInputStream bais = new ByteArrayInputStream(inputBytes);
      InputSource in = new InputSource(bais);
      DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
      dfactory.setNamespaceAware(true);
      DocumentBuilder db = dfactory.newDocumentBuilder();
      Document document = db.parse(in);
      byte result[] = this.engineCanonicalizeSubTree(document);
      return result;
   }
   public byte[] engineCanonicalizeXPathNodeSet(NodeList xpathNodeSet)
           throws CanonicalizationException {
      return this
         .engineCanonicalizeXPathNodeSet(XMLUtils
            .convertNodelistToSet(xpathNodeSet));
   }
   public byte[] engineCanonicalizeXPathNodeSet(NodeList xpathNodeSet, String inclusiveNamespaces)
           throws CanonicalizationException {
      return this
         .engineCanonicalizeXPathNodeSet(XMLUtils
            .convertNodelistToSet(xpathNodeSet), inclusiveNamespaces);
   }
   public abstract String engineGetURI();
   public abstract boolean engineGetIncludeComments();
   public abstract byte[] engineCanonicalizeXPathNodeSet(Set xpathNodeSet)
      throws CanonicalizationException;
   public abstract byte[] engineCanonicalizeXPathNodeSet(Set xpathNodeSet, String inclusiveNamespaces)
      throws CanonicalizationException;
   public abstract byte[] engineCanonicalizeSubTree(Node rootNode)
      throws CanonicalizationException;
   public abstract byte[] engineCanonicalizeSubTree(Node rootNode, String inclusiveNamespaces)
      throws CanonicalizationException;
   public abstract void setWriter(OutputStream os);
   protected boolean reset=false;
}
