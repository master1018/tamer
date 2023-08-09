public abstract class Canonicalizer20010315 extends CanonicalizerBase {
        boolean firstCall=true;
        final SortedSet result= new TreeSet(COMPARE);
    static final String XMLNS_URI=Constants.NamespaceSpecNS;
    static final String XML_LANG_URI=Constants.XML_LANG_SPACE_SpecNS;
    static class XmlAttrStack {
        int currentLevel=0;
        int lastlevel=0;
        XmlsStackElement cur;
        static class XmlsStackElement {
                int level;
                boolean rendered=false;
                List nodes=new ArrayList();
        };
        List levels=new ArrayList();
        void push(int level) {
                currentLevel=level;
                if (currentLevel==-1)
                        return;
                cur=null;
                while (lastlevel>=currentLevel) {
                        levels.remove(levels.size()-1);
                        if (levels.size()==0) {
                                lastlevel=0;
                                return;
                        }
                        lastlevel=((XmlsStackElement)levels.get(levels.size()-1)).level;
                }
        }
        void addXmlnsAttr(Attr n) {
                if (cur==null) {
                        cur=new XmlsStackElement();
                        cur.level=currentLevel;
                        levels.add(cur);
                        lastlevel=currentLevel;
                }
                cur.nodes.add(n);
        }
        void getXmlnsAttr(Collection col) {
                int size=levels.size()-1;
                if (cur==null) {
                        cur=new XmlsStackElement();
                        cur.level=currentLevel;
                        lastlevel=currentLevel;
                        levels.add(cur);
                }
                boolean parentRendered=false;
                XmlsStackElement e=null;
                if (size==-1) {
                        parentRendered=true;
                } else {
                        e=(XmlsStackElement)levels.get(size);
                        if (e.rendered && e.level+1==currentLevel)
                                parentRendered=true;
                }
                if (parentRendered) {
                                col.addAll(cur.nodes);
                                cur.rendered=true;
                                return;
                        }
                        Map loa = new HashMap();
                for (;size>=0;size--) {
                        e=(XmlsStackElement)levels.get(size);
                        Iterator it=e.nodes.iterator();
                        while (it.hasNext()) {
                                Attr n=(Attr)it.next();
                                if (!loa.containsKey(n.getName()))
                                        loa.put(n.getName(),n);
                        }
                };
                        cur.rendered=true;
                col.addAll(loa.values());
        }
    }
    XmlAttrStack xmlattrStack=new XmlAttrStack();
   public Canonicalizer20010315(boolean includeComments) {
      super(includeComments);
   }
   Iterator handleAttributesSubtree(Element E,  NameSpaceSymbTable ns )
           throws CanonicalizationException {
          if (!E.hasAttributes() && !firstCall) {
         return null;
      }
      final SortedSet result = this.result;
      result.clear();
      NamedNodeMap attrs = E.getAttributes();
      int attrsLength = attrs.getLength();
      for (int i = 0; i < attrsLength; i++) {
         Attr N = (Attr) attrs.item(i);
         String NUri =N.getNamespaceURI();
         if (XMLNS_URI!=NUri) {
            result.add(N);
            continue;
         }
         String NName=N.getLocalName();
         String NValue=N.getValue();
         if (XML.equals(NName)
                 && XML_LANG_URI.equals(NValue)) {
                continue;
         }
         Node n=ns.addMappingAndRender(NName,NValue,N);
          if (n!=null) {
             result.add(n);
             if (C14nHelper.namespaceIsRelative(N)) {
                Object exArgs[] = { E.getTagName(), NName, N.getNodeValue() };
                throw new CanonicalizationException(
                   "c14n.Canonicalizer.RelativeNamespace", exArgs);
             }
          }
      }
      if (firstCall) {
        ns.getUnrenderedNodes(result);
        xmlattrStack.getXmlnsAttr(result);
                firstCall=false;
      }
      return result.iterator();
   }
   Iterator handleAttributes(Element E,  NameSpaceSymbTable ns ) throws CanonicalizationException {
        xmlattrStack.push(ns.getLevel());
    boolean isRealVisible=isVisibleDO(E,ns.getLevel())==1;
    NamedNodeMap attrs = null;
    int attrsLength = 0;
    if (E.hasAttributes()) {
        attrs=E.getAttributes();
       attrsLength= attrs.getLength();
    }
    SortedSet result = this.result;
    result.clear();
    for (int i = 0; i < attrsLength; i++) {
       Attr N = (Attr) attrs.item(i);
       String NUri =N.getNamespaceURI();
       if (XMLNS_URI!=NUri) {
           if (XML_LANG_URI==NUri) {
                          xmlattrStack.addXmlnsAttr(N);
           } else if (isRealVisible){
                result.add(N);
          }
          continue;
       }
       String NName=N.getLocalName();
       String NValue=N.getValue();
       if ("xml".equals(NName)
               && XML_LANG_URI.equals(NValue)) {
          continue;
       }
            if  (isVisible(N))  {
                if (!isRealVisible && ns.removeMappingIfRender(NName)) {
                        continue;
                }
                Node n=ns.addMappingAndRender(NName,NValue,N);
                        if (n!=null) {
                                        result.add(n);
                    if (C14nHelper.namespaceIsRelative(N)) {
                       Object exArgs[] = { E.getTagName(), NName, N.getNodeValue() };
                       throw new CanonicalizationException(
                          "c14n.Canonicalizer.RelativeNamespace", exArgs);
                   }
                         }
        } else {
                if (isRealVisible && NName!=XMLNS) {
                        ns.removeMapping(NName);
                } else {
                        ns.addMapping(NName,NValue,N);
                }
        }
    }
    if (isRealVisible) {
        Attr xmlns = E.getAttributeNodeNS(XMLNS_URI, XMLNS);
        Node n=null;
        if (xmlns == null) {
                n=ns.getMapping(XMLNS);
        } else if ( !isVisible(xmlns)) {
                n=ns.addMappingAndRender(XMLNS,"",nullNode);
        }
        if (n!=null) {
                        result.add(n);
        }
        xmlattrStack.getXmlnsAttr(result);
        ns.getUnrenderedNodes(result);
    }
    return result.iterator();
   }
   public byte[] engineCanonicalizeXPathNodeSet(Set xpathNodeSet, String inclusiveNamespaces)
           throws CanonicalizationException {
      throw new CanonicalizationException(
         "c14n.Canonicalizer.UnsupportedOperation");
   }
   public byte[] engineCanonicalizeSubTree(Node rootNode, String inclusiveNamespaces)
           throws CanonicalizationException {
      throw new CanonicalizationException(
         "c14n.Canonicalizer.UnsupportedOperation");
   }
   void circumventBugIfNeeded(XMLSignatureInput input) throws CanonicalizationException, ParserConfigurationException, IOException, SAXException {
           if (!input.isNeedsToBeExpanded())
                   return;
           Document doc = null;
       if (input.getSubNode() != null) {
           doc=XMLUtils.getOwnerDocument(input.getSubNode());
       } else {
           doc=XMLUtils.getOwnerDocument(input.getNodeSet());
       }
           XMLUtils.circumventBug2650(doc);
   }
   void handleParent(Element e, NameSpaceSymbTable ns) {
           if (!e.hasAttributes()) {
                        return;
           }
           xmlattrStack.push(-1);
           NamedNodeMap attrs = e.getAttributes();
           int attrsLength = attrs.getLength();
           for (int i = 0; i < attrsLength; i++) {
                   Attr N = (Attr) attrs.item(i);
                   if (Constants.NamespaceSpecNS!=N.getNamespaceURI()) {
                           if (XML_LANG_URI==N.getNamespaceURI()) {
                                   xmlattrStack.addXmlnsAttr(N);
                           }
                           continue;
                   }
                   String NName=N.getLocalName();
                   String NValue=N.getNodeValue();
                   if (XML.equals(NName)
                                   && Constants.XML_LANG_SPACE_SpecNS.equals(NValue)) {
                                continue;
                   }
                   ns.addMapping(NName,NValue,N);
           }
   }
}
