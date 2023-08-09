public class XPathFilterCHGPContainer extends ElementProxy
        implements TransformParam {
   private static final String _TAG_INCLUDE_BUT_SEARCH = "IncludeButSearch";
   private static final String _TAG_EXCLUDE_BUT_SEARCH = "ExcludeButSearch";
   private static final String _TAG_EXCLUDE = "Exclude";
   public static final String _TAG_XPATHCHGP = "XPathAlternative";
   public static final String _ATT_INCLUDESLASH = "IncludeSlashPolicy";
   public static final boolean IncludeSlash = true;
   public static final boolean ExcludeSlash = false;
   private XPathFilterCHGPContainer() {
   }
   private XPathFilterCHGPContainer(Document doc, boolean includeSlashPolicy,
                                    String includeButSearch,
                                    String excludeButSearch, String exclude) {
      super(doc);
      if (includeSlashPolicy) {
         this._constructionElement
            .setAttributeNS(null, XPathFilterCHGPContainer._ATT_INCLUDESLASH, "true");
      } else {
         this._constructionElement
            .setAttributeNS(null, XPathFilterCHGPContainer._ATT_INCLUDESLASH, "false");
      }
      if ((includeButSearch != null)
              && (includeButSearch.trim().length() > 0)) {
         Element includeButSearchElem =
            ElementProxy.createElementForFamily(doc, this.getBaseNamespace(),
                                        XPathFilterCHGPContainer
                                           ._TAG_INCLUDE_BUT_SEARCH);
         includeButSearchElem
            .appendChild(this._doc
               .createTextNode(indentXPathText(includeButSearch)));
         XMLUtils.addReturnToElement(this._constructionElement);
         this._constructionElement.appendChild(includeButSearchElem);
      }
      if ((excludeButSearch != null)
              && (excludeButSearch.trim().length() > 0)) {
         Element excludeButSearchElem =
         ElementProxy.createElementForFamily(doc, this.getBaseNamespace(),
                                        XPathFilterCHGPContainer
                                           ._TAG_EXCLUDE_BUT_SEARCH);
         excludeButSearchElem
            .appendChild(this._doc
               .createTextNode(indentXPathText(excludeButSearch)));
         XMLUtils.addReturnToElement(this._constructionElement);
         this._constructionElement.appendChild(excludeButSearchElem);
      }
      if ((exclude != null) && (exclude.trim().length() > 0)) {
         Element excludeElem = ElementProxy.createElementForFamily(doc,
                                  this.getBaseNamespace(),
                                  XPathFilterCHGPContainer._TAG_EXCLUDE);
         excludeElem
            .appendChild(this._doc.createTextNode(indentXPathText(exclude)));
         XMLUtils.addReturnToElement(this._constructionElement);
         this._constructionElement.appendChild(excludeElem);
      }
      XMLUtils.addReturnToElement(this._constructionElement);
   }
   static String indentXPathText(String xp) {
      if ((xp.length() > 2) && (!Character.isWhitespace(xp.charAt(0)))) {
         return "\n" + xp + "\n";
      }
      return xp;
   }
   private XPathFilterCHGPContainer(Element element, String BaseURI)
           throws XMLSecurityException {
      super(element, BaseURI);
   }
   public static XPathFilterCHGPContainer getInstance(Document doc,
           boolean includeSlashPolicy, String includeButSearch,
           String excludeButSearch, String exclude) {
      return new XPathFilterCHGPContainer(doc, includeSlashPolicy,
                                          includeButSearch, excludeButSearch,
                                          exclude);
   }
   public static XPathFilterCHGPContainer getInstance(
           Element element, String BaseURI) throws XMLSecurityException {
      return new XPathFilterCHGPContainer(element, BaseURI);
   }
   private String getXStr(String type) {
      if (this.length(this.getBaseNamespace(), type) != 1) {
         return "";
      }
      Element xElem = XMLUtils.selectNode(this._constructionElement.getFirstChild(), this.getBaseNamespace(),
                         type,0);
      return XMLUtils.getFullTextChildrenFromElement(xElem);
   }
   public String getIncludeButSearch() {
      return this.getXStr(XPathFilterCHGPContainer._TAG_INCLUDE_BUT_SEARCH);
   }
   public String getExcludeButSearch() {
      return this.getXStr(XPathFilterCHGPContainer._TAG_EXCLUDE_BUT_SEARCH);
   }
   public String getExclude() {
      return this.getXStr(XPathFilterCHGPContainer._TAG_EXCLUDE);
   }
   public boolean getIncludeSlashPolicy() {
      return this._constructionElement
         .getAttributeNS(null, XPathFilterCHGPContainer._ATT_INCLUDESLASH)
         .equals("true");
   }
   private Node getHereContextNode(String type) {
      if (this.length(this.getBaseNamespace(), type) != 1) {
         return null;
      }
      return XMLUtils.selectNodeText(this._constructionElement.getFirstChild(), this.getBaseNamespace(),
                         type,0);
   }
   public Node getHereContextNodeIncludeButSearch() {
      return this
         .getHereContextNode(XPathFilterCHGPContainer._TAG_INCLUDE_BUT_SEARCH);
   }
   public Node getHereContextNodeExcludeButSearch() {
      return this
         .getHereContextNode(XPathFilterCHGPContainer._TAG_EXCLUDE_BUT_SEARCH);
   }
   public Node getHereContextNodeExclude() {
      return this.getHereContextNode(XPathFilterCHGPContainer._TAG_EXCLUDE);
   }
   public final String getBaseLocalName() {
      return XPathFilterCHGPContainer._TAG_XPATHCHGP;
   }
   public final String getBaseNamespace() {
      return Transforms.TRANSFORM_XPATHFILTERCHGP;
   }
}
