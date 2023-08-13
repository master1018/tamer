public class XPath2FilterContainer04 extends ElementProxy
        implements TransformParam {
   private static final String _ATT_FILTER = "Filter";
   private static final String _ATT_FILTER_VALUE_INTERSECT = "intersect";
   private static final String _ATT_FILTER_VALUE_SUBTRACT = "subtract";
   private static final String _ATT_FILTER_VALUE_UNION = "union";
   public static final String _TAG_XPATH2 = "XPath";
   public static final String XPathFilter2NS =
      "http:
   private XPath2FilterContainer04() {
   }
   private XPath2FilterContainer04(Document doc, String xpath2filter,
                                 String filterType) {
      super(doc);
      this._constructionElement.setAttributeNS(null, XPath2FilterContainer04._ATT_FILTER,
                                             filterType);
      if ((xpath2filter.length() > 2)
              && (!Character.isWhitespace(xpath2filter.charAt(0)))) {
         XMLUtils.addReturnToElement(this._constructionElement);
         this._constructionElement.appendChild(doc.createTextNode(xpath2filter));
         XMLUtils.addReturnToElement(this._constructionElement);
      } else {
         this._constructionElement
            .appendChild(doc.createTextNode(xpath2filter));
      }
   }
   private XPath2FilterContainer04(Element element, String BaseURI)
           throws XMLSecurityException {
      super(element, BaseURI);
      String filterStr =
         this._constructionElement
            .getAttributeNS(null, XPath2FilterContainer04._ATT_FILTER);
      if (!filterStr
              .equals(XPath2FilterContainer04
              ._ATT_FILTER_VALUE_INTERSECT) &&!filterStr
                 .equals(XPath2FilterContainer04
                 ._ATT_FILTER_VALUE_SUBTRACT) &&!filterStr
                    .equals(XPath2FilterContainer04._ATT_FILTER_VALUE_UNION)) {
         Object exArgs[] = { XPath2FilterContainer04._ATT_FILTER, filterStr,
                             XPath2FilterContainer04._ATT_FILTER_VALUE_INTERSECT
                             + ", "
                             + XPath2FilterContainer04._ATT_FILTER_VALUE_SUBTRACT
                             + " or "
                             + XPath2FilterContainer04._ATT_FILTER_VALUE_UNION };
         throw new XMLSecurityException("attributeValueIllegal", exArgs);
      }
   }
   public static XPath2FilterContainer04 newInstanceIntersect(Document doc,
           String xpath2filter) {
      return new XPath2FilterContainer04(doc, xpath2filter,
                                       XPath2FilterContainer04
                                          ._ATT_FILTER_VALUE_INTERSECT);
   }
   public static XPath2FilterContainer04 newInstanceSubtract(Document doc,
           String xpath2filter) {
      return new XPath2FilterContainer04(doc, xpath2filter,
                                       XPath2FilterContainer04
                                          ._ATT_FILTER_VALUE_SUBTRACT);
   }
   public static XPath2FilterContainer04 newInstanceUnion(Document doc,
           String xpath2filter) {
      return new XPath2FilterContainer04(doc, xpath2filter,
                                       XPath2FilterContainer04
                                          ._ATT_FILTER_VALUE_UNION);
   }
   public static XPath2FilterContainer04 newInstance(
           Element element, String BaseURI) throws XMLSecurityException {
      return new XPath2FilterContainer04(element, BaseURI);
   }
   public boolean isIntersect() {
      return this._constructionElement
         .getAttributeNS(null, XPath2FilterContainer04._ATT_FILTER)
         .equals(XPath2FilterContainer04._ATT_FILTER_VALUE_INTERSECT);
   }
   public boolean isSubtract() {
      return this._constructionElement
         .getAttributeNS(null, XPath2FilterContainer04._ATT_FILTER)
         .equals(XPath2FilterContainer04._ATT_FILTER_VALUE_SUBTRACT);
   }
   public boolean isUnion() {
      return this._constructionElement
         .getAttributeNS(null, XPath2FilterContainer04._ATT_FILTER)
         .equals(XPath2FilterContainer04._ATT_FILTER_VALUE_UNION);
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
      return XPath2FilterContainer04._TAG_XPATH2;
   }
   public final String getBaseNamespace() {
      return XPath2FilterContainer04.XPathFilter2NS;
   }
}
