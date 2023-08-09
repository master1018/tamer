public class XPath2FilterContainer extends ElementProxy
        implements TransformParam {
   private static final String _ATT_FILTER = "Filter";
   private static final String _ATT_FILTER_VALUE_INTERSECT = "intersect";
   private static final String _ATT_FILTER_VALUE_SUBTRACT = "subtract";
   private static final String _ATT_FILTER_VALUE_UNION = "union";
   public static final String INTERSECT =
      XPath2FilterContainer._ATT_FILTER_VALUE_INTERSECT;
   public static final String SUBTRACT =
      XPath2FilterContainer._ATT_FILTER_VALUE_SUBTRACT;
   public static final String UNION =
      XPath2FilterContainer._ATT_FILTER_VALUE_UNION;
   public static final String _TAG_XPATH2 = "XPath";
   public static final String XPathFilter2NS =
      "http:
   private XPath2FilterContainer() {
   }
   private XPath2FilterContainer(Document doc, String xpath2filter,
                                 String filterType) {
      super(doc);
      this._constructionElement
         .setAttributeNS(null, XPath2FilterContainer._ATT_FILTER, filterType);
      this._constructionElement.appendChild(doc.createTextNode(xpath2filter));
   }
   private XPath2FilterContainer(Element element, String BaseURI)
           throws XMLSecurityException {
      super(element, BaseURI);
      String filterStr = this._constructionElement.getAttributeNS(null,
                            XPath2FilterContainer._ATT_FILTER);
      if (!filterStr
              .equals(XPath2FilterContainer
              ._ATT_FILTER_VALUE_INTERSECT) &&!filterStr
                 .equals(XPath2FilterContainer
                 ._ATT_FILTER_VALUE_SUBTRACT) &&!filterStr
                    .equals(XPath2FilterContainer._ATT_FILTER_VALUE_UNION)) {
         Object exArgs[] = { XPath2FilterContainer._ATT_FILTER, filterStr,
                             XPath2FilterContainer._ATT_FILTER_VALUE_INTERSECT
                             + ", "
                             + XPath2FilterContainer._ATT_FILTER_VALUE_SUBTRACT
                             + " or "
                             + XPath2FilterContainer._ATT_FILTER_VALUE_UNION };
         throw new XMLSecurityException("attributeValueIllegal", exArgs);
      }
   }
   public static XPath2FilterContainer newInstanceIntersect(Document doc,
           String xpath2filter) {
      return new XPath2FilterContainer(doc, xpath2filter,
                                       XPath2FilterContainer
                                          ._ATT_FILTER_VALUE_INTERSECT);
   }
   public static XPath2FilterContainer newInstanceSubtract(Document doc,
           String xpath2filter) {
      return new XPath2FilterContainer(doc, xpath2filter,
                                       XPath2FilterContainer
                                          ._ATT_FILTER_VALUE_SUBTRACT);
   }
   public static XPath2FilterContainer newInstanceUnion(Document doc,
           String xpath2filter) {
      return new XPath2FilterContainer(doc, xpath2filter,
                                       XPath2FilterContainer
                                          ._ATT_FILTER_VALUE_UNION);
   }
   public static NodeList newInstances(Document doc, String[][] params) {
      HelperNodeList nl = new HelperNodeList();
      XMLUtils.addReturnToElement(doc, nl);
      for (int i = 0; i < params.length; i++) {
         String type = params[i][0];
         String xpath = params[i][1];
         if (!(type.equals(XPath2FilterContainer
                 ._ATT_FILTER_VALUE_INTERSECT) || type
                    .equals(XPath2FilterContainer
                    ._ATT_FILTER_VALUE_SUBTRACT) || type
                       .equals(XPath2FilterContainer
                          ._ATT_FILTER_VALUE_UNION))) {
            throw new IllegalArgumentException("The type(" + i + ")=\"" + type
                                               + "\" is illegal");
         }
         XPath2FilterContainer c = new XPath2FilterContainer(doc, xpath, type);
         nl.appendChild(c.getElement());
         XMLUtils.addReturnToElement(doc, nl);
      }
      return nl;
   }
   public static XPath2FilterContainer newInstance(
           Element element, String BaseURI) throws XMLSecurityException {
      return new XPath2FilterContainer(element, BaseURI);
   }
   public boolean isIntersect() {
      return this._constructionElement
         .getAttributeNS(null, XPath2FilterContainer._ATT_FILTER)
         .equals(XPath2FilterContainer._ATT_FILTER_VALUE_INTERSECT);
   }
   public boolean isSubtract() {
      return this._constructionElement
         .getAttributeNS(null, XPath2FilterContainer._ATT_FILTER)
         .equals(XPath2FilterContainer._ATT_FILTER_VALUE_SUBTRACT);
   }
   public boolean isUnion() {
      return this._constructionElement
         .getAttributeNS(null, XPath2FilterContainer._ATT_FILTER)
         .equals(XPath2FilterContainer._ATT_FILTER_VALUE_UNION);
   }
   public String getXPathFilterStr() {
      return this.getTextFromTextChild();
   }
   public Node getXPathFilterTextNode() {
      NodeList children = this._constructionElement.getChildNodes();
      int length = children.getLength();
      for (int i = 0; i < length; i++) {
         if (children.item(i).getNodeType() == Node.TEXT_NODE) {
            return children.item(i);
         }
      }
      return null;
   }
   public final String getBaseLocalName() {
      return XPath2FilterContainer._TAG_XPATH2;
   }
   public final String getBaseNamespace() {
      return XPath2FilterContainer.XPathFilter2NS;
   }
}
