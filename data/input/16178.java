public class TransformXPath extends TransformSpi {
   public static final String implementedTransformURI =
      Transforms.TRANSFORM_XPATH;
   protected String engineGetURI() {
      return implementedTransformURI;
   }
   protected XMLSignatureInput enginePerformTransform(XMLSignatureInput input, Transform _transformObject)
           throws TransformationException {
      try {
                  CachedXPathAPIHolder.setDoc(_transformObject.getElement().getOwnerDocument());
         Element xpathElement =XMLUtils.selectDsNode(
            _transformObject.getElement().getFirstChild(),
               Constants._TAG_XPATH,0);
         if (xpathElement == null) {
            Object exArgs[] = { "ds:XPath", "Transform" };
            throw new TransformationException("xml.WrongContent", exArgs);
         }
         Node xpathnode = xpathElement.getChildNodes().item(0);
         String str=CachedXPathFuncHereAPI.getStrFromNode(xpathnode);
         input.setNeedsToBeExpanded(needsCircunvent(str));
         if (xpathnode == null) {
            throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
                                   "Text must be in ds:Xpath");
         }
         input.addNodeFilter(new XPathNodeFilter( xpathElement, xpathnode, str));
         input.setNodeSet(true);
         return input;
      } catch (DOMException ex) {
         throw new TransformationException("empty", ex);
      }
   }
    private boolean needsCircunvent(String str) {
        return (str.indexOf("namespace") != -1) || (str.indexOf("name()") != -1);
    }
    static class XPathNodeFilter implements NodeFilter {
        PrefixResolverDefault prefixResolver;
        CachedXPathFuncHereAPI xPathFuncHereAPI =
            new CachedXPathFuncHereAPI(CachedXPathAPIHolder.getCachedXPathAPI());
        Node xpathnode;
        String str;
        XPathNodeFilter(Element xpathElement,
                        Node xpathnode, String str) {
            this.xpathnode=xpathnode;
            this.str=str;
            prefixResolver =new PrefixResolverDefault(xpathElement);
        }
        public int isNodeInclude(Node currentNode) {
            XObject includeInResult;
            try {
                includeInResult = xPathFuncHereAPI.eval(currentNode,
                                xpathnode, str,prefixResolver);
                if (includeInResult.bool())
                        return 1;
                return 0;
            } catch (TransformerException e) {
                Object[] eArgs = {currentNode};
                throw new XMLSecurityRuntimeException
                    ("signature.Transform.node", eArgs, e);
            } catch (Exception e) {
                Object[] eArgs = {currentNode, new Short(currentNode.getNodeType())};
                throw new XMLSecurityRuntimeException
                    ("signature.Transform.nodeAndType",eArgs, e);
            }
        }
        public int isNodeIncludeDO(Node n, int level) {
                return isNodeInclude(n);
        }
    }
}
