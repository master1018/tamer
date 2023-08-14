public class TransformXPath2Filter extends TransformSpi {
   public static final String implementedTransformURI =
      Transforms.TRANSFORM_XPATH2FILTER;
   protected String engineGetURI() {
      return implementedTransformURI;
   }
   protected XMLSignatureInput enginePerformTransform(XMLSignatureInput input, Transform _transformObject)
           throws TransformationException {
          CachedXPathAPIHolder.setDoc(_transformObject.getElement().getOwnerDocument());
      try {
          List unionNodes=new ArrayList();
           List substractNodes=new ArrayList();
           List intersectNodes=new ArrayList();
         CachedXPathFuncHereAPI xPathFuncHereAPI =
            new CachedXPathFuncHereAPI(CachedXPathAPIHolder.getCachedXPathAPI());
         Element []xpathElements =XMLUtils.selectNodes(
                _transformObject.getElement().getFirstChild(),
                   XPath2FilterContainer.XPathFilter2NS,
                   XPath2FilterContainer._TAG_XPATH2);
         int noOfSteps = xpathElements.length;
         if (noOfSteps == 0) {
            Object exArgs[] = { Transforms.TRANSFORM_XPATH2FILTER, "XPath" };
            throw new TransformationException("xml.WrongContent", exArgs);
         }
         Document inputDoc = null;
         if (input.getSubNode() != null) {
            inputDoc = XMLUtils.getOwnerDocument(input.getSubNode());
         } else {
            inputDoc = XMLUtils.getOwnerDocument(input.getNodeSet());
         }
         for (int i = 0; i < noOfSteps; i++) {
            Element xpathElement =XMLUtils.selectNode(
               _transformObject.getElement().getFirstChild(),
                  XPath2FilterContainer.XPathFilter2NS,
                  XPath2FilterContainer._TAG_XPATH2,i);
            XPath2FilterContainer xpathContainer =
               XPath2FilterContainer.newInstance(xpathElement,
                                                   input.getSourceURI());
            NodeList subtreeRoots = xPathFuncHereAPI.selectNodeList(inputDoc,
                                       xpathContainer.getXPathFilterTextNode(),
                                       CachedXPathFuncHereAPI.getStrFromNode(xpathContainer.getXPathFilterTextNode()),
                                       xpathContainer.getElement());
            if (xpathContainer.isIntersect()) {
                intersectNodes.add(subtreeRoots);
             } else if (xpathContainer.isSubtract()) {
                 substractNodes.add(subtreeRoots);
             } else if (xpathContainer.isUnion()) {
                unionNodes.add(subtreeRoots);
             }
         }
         input.addNodeFilter(new XPath2NodeFilter(convertNodeListToSet(unionNodes),
                         convertNodeListToSet(substractNodes),convertNodeListToSet(intersectNodes)));
         input.setNodeSet(true);
         return input;
      } catch (TransformerException ex) {
         throw new TransformationException("empty", ex);
      } catch (DOMException ex) {
         throw new TransformationException("empty", ex);
      } catch (CanonicalizationException ex) {
         throw new TransformationException("empty", ex);
      } catch (InvalidCanonicalizerException ex) {
         throw new TransformationException("empty", ex);
      } catch (XMLSecurityException ex) {
         throw new TransformationException("empty", ex);
      } catch (SAXException ex) {
         throw new TransformationException("empty", ex);
      } catch (IOException ex) {
         throw new TransformationException("empty", ex);
      } catch (ParserConfigurationException ex) {
         throw new TransformationException("empty", ex);
      }
   }
   static Set convertNodeListToSet(List l){
           Set result=new HashSet();
           for (int j=0;j<l.size();j++) {
                   NodeList rootNodes=(NodeList) l.get(j);
               int length = rootNodes.getLength();
               for (int i = 0; i < length; i++) {
                    Node rootNode = rootNodes.item(i);
                    result.add(rootNode);
                 }
           }
           return result;
   }
}
class XPath2NodeFilter implements NodeFilter {
        boolean hasUnionNodes;
        boolean hasSubstractNodes;
        boolean hasIntersectNodes;
        XPath2NodeFilter(Set unionNodes, Set substractNodes,
                        Set intersectNodes) {
                this.unionNodes=unionNodes;
                hasUnionNodes=!unionNodes.isEmpty();
                this.substractNodes=substractNodes;
                hasSubstractNodes=!substractNodes.isEmpty();
                this.intersectNodes=intersectNodes;
                hasIntersectNodes=!intersectNodes.isEmpty();
        }
        Set unionNodes;
        Set substractNodes;
        Set intersectNodes;
   public int isNodeInclude(Node currentNode) {
           int result=1;
           if (hasSubstractNodes && rooted(currentNode, substractNodes)) {
                      result = -1;
           } else if (hasIntersectNodes && !rooted(currentNode, intersectNodes)) {
                   result = 0;
           }
      if (result==1)
          return 1;
      if (hasUnionNodes) {
          if (rooted(currentNode, unionNodes)) {
                   return 1;
          }
          result=0;
      }
      return result;
   }
   int inSubstract=-1;
   int inIntersect=-1;
   int inUnion=-1;
   public int isNodeIncludeDO(Node n, int level) {
           int result=1;
           if (hasSubstractNodes) {
                   if ((inSubstract==-1) || (level<=inSubstract)) {
                           if (inList(n,  substractNodes)) {
                                   inSubstract=level;
                           } else {
                                   inSubstract=-1;
                           }
                   }
                   if (inSubstract!=-1){
                           result=-1;
                   }
           }
           if (result!=-1){
                   if (hasIntersectNodes) {
                   if ((inIntersect==-1) || (level<=inIntersect)) {
                           if (!inList(n,  intersectNodes)) {
                                   inIntersect=-1;
                                   result=0;
                           } else {
                                   inIntersect=level;
                           }
                   }
                   }
           }
          if (level<=inUnion)
                   inUnion=-1;
      if (result==1)
          return 1;
      if (hasUnionNodes) {
          if ((inUnion==-1) && inList(n,  unionNodes)) {
                  inUnion=level;
          }
          if (inUnion!=-1)
                  return 1;
          result=0;
      }
      return result;
   }
   static boolean  rooted(Node currentNode, Set nodeList ) {
           if (nodeList.contains(currentNode)) {
                   return true;
           }
           Iterator it=nodeList.iterator();
           while (it.hasNext()) {
                        Node rootNode = (Node) it.next();
                        if (XMLUtils.isDescendantOrSelf(rootNode,currentNode)) {
                                   return true;
                        }
           }
           return false;
   }
      static boolean  inList(Node currentNode, Set nodeList ) {
              return nodeList.contains(currentNode);
      }
}
