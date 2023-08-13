public abstract class Canonicalizer11 extends CanonicalizerBase {
    boolean firstCall = true;
    final SortedSet result = new TreeSet(COMPARE);
    static final String XMLNS_URI = Constants.NamespaceSpecNS;
    static final String XML_LANG_URI = Constants.XML_LANG_SPACE_SpecNS;
    static Logger log = Logger.getLogger(Canonicalizer11.class.getName());
    static class XmlAttrStack {
        int currentLevel = 0;
        int lastlevel = 0;
        XmlsStackElement cur;
        static class XmlsStackElement {
            int level;
            boolean rendered = false;
            List nodes = new ArrayList();
        };
        List levels = new ArrayList();
        void push(int level) {
            currentLevel = level;
            if (currentLevel == -1)
                return;
            cur = null;
            while (lastlevel >= currentLevel) {
                levels.remove(levels.size() - 1);
                if (levels.size() == 0) {
                    lastlevel = 0;
                    return;
                }
                lastlevel=((XmlsStackElement)levels.get(levels.size()-1)).level;
            }
        }
        void addXmlnsAttr(Attr n) {
            if (cur == null) {
                cur = new XmlsStackElement();
                cur.level = currentLevel;
                levels.add(cur);
                lastlevel = currentLevel;
            }
            cur.nodes.add(n);
        }
        void getXmlnsAttr(Collection col) {
            if (cur == null) {
                cur = new XmlsStackElement();
                cur.level = currentLevel;
                lastlevel = currentLevel;
                levels.add(cur);
            }
            int size = levels.size() - 2;
            boolean parentRendered = false;
            XmlsStackElement e = null;
            if (size == -1) {
                parentRendered = true;
            } else {
                e = (XmlsStackElement) levels.get(size);
                if (e.rendered && e.level+1 == currentLevel)
                    parentRendered = true;
            }
            if (parentRendered) {
                col.addAll(cur.nodes);
                cur.rendered = true;
                return;
            }
            Map loa = new HashMap();
            List baseAttrs = new ArrayList();
            boolean successiveOmitted = true;
            for (;size>=0;size--) {
                e = (XmlsStackElement) levels.get(size);
                if (e.rendered) {
                    successiveOmitted = false;
                }
                Iterator it = e.nodes.iterator();
                while (it.hasNext() && successiveOmitted) {
                    Attr n = (Attr) it.next();
                    if (n.getLocalName().equals("base")) {
                        if (!e.rendered) {
                            baseAttrs.add(n);
                        }
                    } else if (!loa.containsKey(n.getName()))
                        loa.put(n.getName(), n);
                }
            }
            if (!baseAttrs.isEmpty()) {
                Iterator it = cur.nodes.iterator();
                String base = null;
                Attr baseAttr = null;
                while (it.hasNext()) {
                    Attr n = (Attr) it.next();
                    if (n.getLocalName().equals("base")) {
                        base = n.getValue();
                        baseAttr = n;
                        break;
                    }
                }
                it = baseAttrs.iterator();
                while (it.hasNext()) {
                    Attr n = (Attr) it.next();
                    if (base == null) {
                        base = n.getValue();
                        baseAttr = n;
                    } else {
                        try {
                            base = joinURI(n.getValue(), base);
                        } catch (URISyntaxException ue) {
                            ue.printStackTrace();
                        }
                    }
                }
                if (base != null && base.length() != 0) {
                    baseAttr.setValue(base);
                    col.add(baseAttr);
                }
            }
            cur.rendered = true;
            col.addAll(loa.values());
        }
    };
    XmlAttrStack xmlattrStack = new XmlAttrStack();
    public Canonicalizer11(boolean includeComments) {
        super(includeComments);
    }
    Iterator handleAttributesSubtree(Element E, NameSpaceSymbTable ns)
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
            String NUri = N.getNamespaceURI();
            if (XMLNS_URI != NUri) {
                result.add(N);
                continue;
            }
            String NName = N.getLocalName();
            String NValue = N.getValue();
            if (XML.equals(NName)
                && XML_LANG_URI.equals(NValue)) {
                continue;
            }
            Node n = ns.addMappingAndRender(NName, NValue, N);
            if (n != null) {
                result.add(n);
                if (C14nHelper.namespaceIsRelative(N)) {
                    Object exArgs[] = {E.getTagName(), NName, N.getNodeValue()};
                    throw new CanonicalizationException(
                        "c14n.Canonicalizer.RelativeNamespace", exArgs);
                }
            }
        }
        if (firstCall) {
            ns.getUnrenderedNodes(result);
            xmlattrStack.getXmlnsAttr(result);
            firstCall = false;
        }
        return result.iterator();
    }
    Iterator handleAttributes(Element E, NameSpaceSymbTable ns)
        throws CanonicalizationException {
        xmlattrStack.push(ns.getLevel());
        boolean isRealVisible = isVisibleDO(E, ns.getLevel()) == 1;
        NamedNodeMap attrs = null;
        int attrsLength = 0;
        if (E.hasAttributes()) {
            attrs = E.getAttributes();
            attrsLength = attrs.getLength();
        }
        SortedSet result = this.result;
        result.clear();
        for (int i = 0; i < attrsLength; i++) {
            Attr N = (Attr) attrs.item(i);
            String NUri = N.getNamespaceURI();
            if (XMLNS_URI != NUri) {
                if (XML_LANG_URI == NUri) {
                    if (N.getLocalName().equals("id")) {
                        if (isRealVisible) {
                            result.add(N);
                        }
                    } else {
                        xmlattrStack.addXmlnsAttr(N);
                    }
                } else if (isRealVisible) {
                    result.add(N);
                }
                continue;
            }
            String NName = N.getLocalName();
            String NValue = N.getValue();
            if ("xml".equals(NName)
                && XML_LANG_URI.equals(NValue)) {
                continue;
            }
            if (isVisible(N))  {
                if (!isRealVisible && ns.removeMappingIfRender(NName)) {
                    continue;
                }
                Node n = ns.addMappingAndRender(NName, NValue, N);
                if (n != null) {
                    result.add(n);
                    if (C14nHelper.namespaceIsRelative(N)) {
                        Object exArgs[] =
                            { E.getTagName(), NName, N.getNodeValue() };
                        throw new CanonicalizationException(
                            "c14n.Canonicalizer.RelativeNamespace", exArgs);
                    }
                }
            } else {
                if (isRealVisible && NName != XMLNS) {
                    ns.removeMapping(NName);
                } else {
                    ns.addMapping(NName, NValue, N);
                }
            }
        }
        if (isRealVisible) {
            Attr xmlns = E.getAttributeNodeNS(XMLNS_URI, XMLNS);
            Node n = null;
            if (xmlns == null) {
                n = ns.getMapping(XMLNS);
            } else if (!isVisible(xmlns)) {
                n = ns.addMappingAndRender(XMLNS, "", nullNode);
            }
            if (n != null) {
                result.add(n);
            }
            xmlattrStack.getXmlnsAttr(result);
            ns.getUnrenderedNodes(result);
        }
        return result.iterator();
    }
    public byte[] engineCanonicalizeXPathNodeSet(Set xpathNodeSet,
        String inclusiveNamespaces) throws CanonicalizationException {
        throw new CanonicalizationException(
         "c14n.Canonicalizer.UnsupportedOperation");
    }
    public byte[] engineCanonicalizeSubTree(Node rootNode,
        String inclusiveNamespaces) throws CanonicalizationException {
        throw new CanonicalizationException(
            "c14n.Canonicalizer.UnsupportedOperation");
    }
    void circumventBugIfNeeded(XMLSignatureInput input)
        throws CanonicalizationException, ParserConfigurationException,
        IOException, SAXException {
        if (!input.isNeedsToBeExpanded())
            return;
        Document doc = null;
        if (input.getSubNode() != null) {
            doc = XMLUtils.getOwnerDocument(input.getSubNode());
        } else {
            doc = XMLUtils.getOwnerDocument(input.getNodeSet());
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
            if (Constants.NamespaceSpecNS != N.getNamespaceURI()) {
                if (XML_LANG_URI == N.getNamespaceURI()) {
                    xmlattrStack.addXmlnsAttr(N);
                }
                continue;
            }
            String NName = N.getLocalName();
            String NValue = N.getNodeValue();
            if (XML.equals(NName)
                && Constants.XML_LANG_SPACE_SpecNS.equals(NValue)) {
                continue;
            }
            ns.addMapping(NName,NValue,N);
        }
    }
    private static String joinURI(String baseURI, String relativeURI)
        throws URISyntaxException {
        String bscheme = null;
        String bauthority = null;
        String bpath = "";
        String bquery = null;
        String bfragment = null; 
        if (baseURI != null) {
            if (baseURI.endsWith("..")) {
                baseURI = baseURI + "/";
            }
            URI base = new URI(baseURI);
            bscheme = base.getScheme();
            bauthority = base.getAuthority();
            bpath = base.getPath();
            bquery = base.getQuery();
            bfragment = base.getFragment();
        }
        URI r = new URI(relativeURI);
        String rscheme = r.getScheme();
        String rauthority = r.getAuthority();
        String rpath = r.getPath();
        String rquery = r.getQuery();
        String rfragment = null;
        String tscheme, tauthority, tpath, tquery, tfragment;
        if (rscheme != null && rscheme.equals(bscheme)) {
            rscheme = null;
        }
        if (rscheme != null) {
            tscheme = rscheme;
            tauthority = rauthority;
            tpath = removeDotSegments(rpath);
            tquery = rquery;
        } else {
            if (rauthority != null) {
                tauthority = rauthority;
                tpath = removeDotSegments(rpath);
                tquery = rquery;
            } else {
                if (rpath.length() == 0) {
                    tpath = bpath;
                    if (rquery != null) {
                        tquery = rquery;
                    } else {
                        tquery = bquery;
                    }
                } else {
                    if (rpath.startsWith("/")) {
                        tpath = removeDotSegments(rpath);
                    } else {
                        if (bauthority != null && bpath.length() == 0) {
                            tpath = "/" + rpath;
                        } else {
                            int last = bpath.lastIndexOf('/');
                            if (last == -1) {
                                tpath = rpath;
                            } else {
                                tpath = bpath.substring(0, last+1) + rpath;
                            }
                        }
                        tpath = removeDotSegments(tpath);
                    }
                    tquery = rquery;
                }
                tauthority = bauthority;
            }
            tscheme = bscheme;
        }
        tfragment = rfragment;
        return new URI(tscheme, tauthority, tpath, tquery, tfragment).toString();
    }
    private static String removeDotSegments(String path) {
        log.log(java.util.logging.Level.FINE, "STEP   OUTPUT BUFFER\t\tINPUT BUFFER");
        String input = path;
        while (input.indexOf("
            input = input.replaceAll("
        }
        StringBuffer output = new StringBuffer();
        if (input.charAt(0) == '/') {
            output.append("/");
            input = input.substring(1);
        }
        printStep("1 ", output.toString(), input);
        while (input.length() != 0) {
            if (input.startsWith("./")) {
                input = input.substring(2);
                printStep("2A", output.toString(), input);
            } else if (input.startsWith("../")) {
                input = input.substring(3);
                if (!output.toString().equals("/")) {
                    output.append("../");
                }
                printStep("2A", output.toString(), input);
            } else if (input.startsWith("/./")) {
                input = input.substring(2);
                printStep("2B", output.toString(), input);
            } else if (input.equals("/.")) {
                input = input.replaceFirst("/.", "/");
                printStep("2B", output.toString(), input);
            } else if (input.startsWith("/../")) {
                input = input.substring(3);
                if (output.length() == 0) {
                    output.append("/");
                } else if (output.toString().endsWith("../")) {
                    output.append("..");
                } else if (output.toString().endsWith("..")) {
                    output.append("/..");
                } else {
                    int index = output.lastIndexOf("/");
                    if (index == -1) {
                        output = new StringBuffer();
                        if (input.charAt(0) == '/') {
                            input = input.substring(1);
                        }
                    } else {
                        output = output.delete(index, output.length());
                    }
                }
                printStep("2C", output.toString(), input);
            } else if (input.equals("/..")) {
                input = input.replaceFirst("/..", "/");
                if (output.length() == 0) {
                    output.append("/");
                } else if (output.toString().endsWith("../")) {
                    output.append("..");
                } else if (output.toString().endsWith("..")) {
                    output.append("/..");
                } else {
                    int index = output.lastIndexOf("/");
                    if (index == -1) {
                        output = new StringBuffer();
                        if (input.charAt(0) == '/') {
                            input = input.substring(1);
                        }
                    } else {
                        output = output.delete(index, output.length());
                    }
                }
                printStep("2C", output.toString(), input);
            } else if (input.equals(".")) {
                input = "";
                printStep("2D", output.toString(), input);
            } else if (input.equals("..")) {
                if (!output.toString().equals("/"))
                    output.append("..");
                input = "";
                printStep("2D", output.toString(), input);
            } else {
                int end = -1;
                int begin = input.indexOf('/');
                if (begin == 0) {
                    end = input.indexOf('/', 1);
                } else {
                    end = begin;
                    begin = 0;
                }
                String segment;
                if (end == -1) {
                    segment = input.substring(begin);
                    input = "";
                } else {
                    segment = input.substring(begin, end);
                    input = input.substring(end);
                }
                output.append(segment);
                printStep("2E", output.toString(), input);
            }
        }
        if (output.toString().endsWith("..")) {
            output.append("/");
            printStep("3 ", output.toString(), input);
        }
        return output.toString();
    }
    private static void printStep(String step, String output, String input) {
        if (log.isLoggable(java.util.logging.Level.FINE)) {
            log.log(java.util.logging.Level.FINE, " " + step + ":   " + output);
            if (output.length() == 0) {
                log.log(java.util.logging.Level.FINE, "\t\t\t\t" + input);
            } else {
                log.log(java.util.logging.Level.FINE, "\t\t\t" + input);
            }
        }
    }
}
