public class XPathContainer extends SignatureElementProxy implements TransformParam {
   public XPathContainer(Document doc) {
      super(doc);
   }
   public void setXPath(String xpath) {
      if (this._constructionElement.getChildNodes() != null) {
         NodeList nl = this._constructionElement.getChildNodes();
         for (int i = 0; i < nl.getLength(); i++) {
            this._constructionElement.removeChild(nl.item(i));
         }
      }
      Text xpathText = this._doc.createTextNode(xpath);
      this._constructionElement.appendChild(xpathText);
   }
   public String getXPath() {
      return this.getTextFromTextChild();
   }
   public String getBaseLocalName() {
      return Constants._TAG_XPATH;
   }
}
