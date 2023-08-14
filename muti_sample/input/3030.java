public class XMLSignatureInputDebugger {
        private Set _xpathNodeSet;
        private Set _inclusiveNamespaces;
        private Document _doc = null;
        private Writer _writer = null;
        static final String HTMLPrefix = "<!DOCTYPE HTML PUBLIC \"-
                        + "<html>\n"
                        + "<head>\n"
                        + "<title>Caninical XML node set</title>\n"
                        + "<style type=\"text/css\">\n"
                        + "<!-- \n"
                        + ".INCLUDED { \n"
                        + "   color: #000000; \n"
                        + "   background-color: \n"
                        + "   #FFFFFF; \n"
                        + "   font-weight: bold; } \n"
                        + ".EXCLUDED { \n"
                        + "   color: #666666; \n"
                        + "   background-color: \n"
                        + "   #999999; } \n"
                        + ".INCLUDEDINCLUSIVENAMESPACE { \n"
                        + "   color: #0000FF; \n"
                        + "   background-color: #FFFFFF; \n"
                        + "   font-weight: bold; \n"
                        + "   font-style: italic; } \n"
                        + ".EXCLUDEDINCLUSIVENAMESPACE { \n"
                        + "   color: #0000FF; \n"
                        + "   background-color: #999999; \n"
                        + "   font-style: italic; } \n"
                        + "--> \n"
                        + "</style> \n"
                        + "</head>\n"
                        + "<body bgcolor=\"#999999\">\n"
                        + "<h1>Explanation of the output</h1>\n"
                        + "<p>The following text contains the nodeset of the given Reference before it is canonicalized. There exist four different styles to indicate how a given node is treated.</p>\n"
                        + "<ul>\n"
                        + "<li class=\"INCLUDED\">A node which is in the node set is labeled using the INCLUDED style.</li>\n"
                        + "<li class=\"EXCLUDED\">A node which is <em>NOT</em> in the node set is labeled EXCLUDED style.</li>\n"
                        + "<li class=\"INCLUDEDINCLUSIVENAMESPACE\">A namespace which is in the node set AND in the InclusiveNamespaces PrefixList is labeled using the INCLUDEDINCLUSIVENAMESPACE style.</li>\n"
                        + "<li class=\"EXCLUDEDINCLUSIVENAMESPACE\">A namespace which is in NOT the node set AND in the InclusiveNamespaces PrefixList is labeled using the INCLUDEDINCLUSIVENAMESPACE style.</li>\n"
                        + "</ul>\n" + "<h1>Output</h1>\n" + "<pre>\n";
        static final String HTMLSuffix = "</pre></body></html>";
        static final String HTMLExcludePrefix = "<span class=\"EXCLUDED\">";
        static final String HTMLExcludeSuffix = "</span>";
        static final String HTMLIncludePrefix = "<span class=\"INCLUDED\">";
        static final String HTMLIncludeSuffix = "</span>";
        static final String HTMLIncludedInclusiveNamespacePrefix = "<span class=\"INCLUDEDINCLUSIVENAMESPACE\">";
        static final String HTMLIncludedInclusiveNamespaceSuffix = "</span>";
        static final String HTMLExcludedInclusiveNamespacePrefix = "<span class=\"EXCLUDEDINCLUSIVENAMESPACE\">";
        static final String HTMLExcludedInclusiveNamespaceSuffix = "</span>";
        private static final int NODE_BEFORE_DOCUMENT_ELEMENT = -1;
        private static final int NODE_NOT_BEFORE_OR_AFTER_DOCUMENT_ELEMENT = 0;
        private static final int NODE_AFTER_DOCUMENT_ELEMENT = 1;
        static final AttrCompare ATTR_COMPARE = new AttrCompare();
        private XMLSignatureInputDebugger() {
        }
        public XMLSignatureInputDebugger(
                        XMLSignatureInput xmlSignatureInput) {
                if (!xmlSignatureInput.isNodeSet()) {
                        this._xpathNodeSet = null;
                } else {
                        this._xpathNodeSet = xmlSignatureInput._inputNodeSet;
                }
        }
        public XMLSignatureInputDebugger(
                        XMLSignatureInput xmlSignatureInput, Set inclusiveNamespace) {
                this(xmlSignatureInput);
                this._inclusiveNamespaces = inclusiveNamespace;
        }
        public String getHTMLRepresentation() throws XMLSignatureException {
                if ((this._xpathNodeSet == null) || (this._xpathNodeSet.size() == 0)) {
                        return HTMLPrefix + "<blink>no node set, sorry</blink>"
                                        + HTMLSuffix;
                }
                {
                        Node n = (Node) this._xpathNodeSet.iterator().next();
                        this._doc = XMLUtils.getOwnerDocument(n);
                }
                try {
                        this._writer = new StringWriter();
                        this.canonicalizeXPathNodeSet(this._doc);
                        this._writer.close();
                        return this._writer.toString();
                } catch (IOException ex) {
                        throw new XMLSignatureException("empty", ex);
                } finally {
                        this._xpathNodeSet = null;
                        this._doc = null;
                        this._writer = null;
                }
        }
        private void canonicalizeXPathNodeSet(Node currentNode)
                        throws XMLSignatureException, IOException {
                int currentNodeType = currentNode.getNodeType();
                switch (currentNodeType) {
                case Node.DOCUMENT_TYPE_NODE:
                default:
                        break;
                case Node.ENTITY_NODE:
                case Node.NOTATION_NODE:
                case Node.DOCUMENT_FRAGMENT_NODE:
                case Node.ATTRIBUTE_NODE:
                        throw new XMLSignatureException("empty");
                case Node.DOCUMENT_NODE:
                        this._writer.write(HTMLPrefix);
                        for (Node currentChild = currentNode.getFirstChild(); currentChild != null; currentChild = currentChild
                                        .getNextSibling()) {
                                this.canonicalizeXPathNodeSet(currentChild);
                        }
                        this._writer.write(HTMLSuffix);
                        break;
                case Node.COMMENT_NODE:
                        if (this._xpathNodeSet.contains(currentNode)) {
                                this._writer.write(HTMLIncludePrefix);
                        } else {
                                this._writer.write(HTMLExcludePrefix);
                        }
                        int position = getPositionRelativeToDocumentElement(currentNode);
                        if (position == NODE_AFTER_DOCUMENT_ELEMENT) {
                                this._writer.write("\n");
                        }
                        this.outputCommentToWriter((Comment) currentNode);
                        if (position == NODE_BEFORE_DOCUMENT_ELEMENT) {
                                this._writer.write("\n");
                        }
                        if (this._xpathNodeSet.contains(currentNode)) {
                                this._writer.write(HTMLIncludeSuffix);
                        } else {
                                this._writer.write(HTMLExcludeSuffix);
                        }
                        break;
                case Node.PROCESSING_INSTRUCTION_NODE:
                        if (this._xpathNodeSet.contains(currentNode)) {
                                this._writer.write(HTMLIncludePrefix);
                        } else {
                                this._writer.write(HTMLExcludePrefix);
                        }
                        position = getPositionRelativeToDocumentElement(currentNode);
                        if (position == NODE_AFTER_DOCUMENT_ELEMENT) {
                                this._writer.write("\n");
                        }
                        this.outputPItoWriter((ProcessingInstruction) currentNode);
                        if (position == NODE_BEFORE_DOCUMENT_ELEMENT) {
                                this._writer.write("\n");
                        }
                        if (this._xpathNodeSet.contains(currentNode)) {
                                this._writer.write(HTMLIncludeSuffix);
                        } else {
                                this._writer.write(HTMLExcludeSuffix);
                        }
                        break;
                case Node.TEXT_NODE:
                case Node.CDATA_SECTION_NODE:
                        if (this._xpathNodeSet.contains(currentNode)) {
                                this._writer.write(HTMLIncludePrefix);
                        } else {
                                this._writer.write(HTMLExcludePrefix);
                        }
                        outputTextToWriter(currentNode.getNodeValue());
                        for (Node nextSibling = currentNode.getNextSibling(); (nextSibling != null)
                                        && ((nextSibling.getNodeType() == Node.TEXT_NODE) || (nextSibling
                                                        .getNodeType() == Node.CDATA_SECTION_NODE)); nextSibling = nextSibling
                                        .getNextSibling()) {
                                this.outputTextToWriter(nextSibling.getNodeValue());
                        }
                        if (this._xpathNodeSet.contains(currentNode)) {
                                this._writer.write(HTMLIncludeSuffix);
                        } else {
                                this._writer.write(HTMLExcludeSuffix);
                        }
                        break;
                case Node.ELEMENT_NODE:
                        Element currentElement = (Element) currentNode;
                        if (this._xpathNodeSet.contains(currentNode)) {
                                this._writer.write(HTMLIncludePrefix);
                        } else {
                                this._writer.write(HTMLExcludePrefix);
                        }
                        this._writer.write("&lt;");
                        this._writer.write(currentElement.getTagName());
                        if (this._xpathNodeSet.contains(currentNode)) {
                                this._writer.write(HTMLIncludeSuffix);
                        } else {
                                this._writer.write(HTMLExcludeSuffix);
                        }
                        NamedNodeMap attrs = currentElement.getAttributes();
                        int attrsLength = attrs.getLength();
                        Object attrs2[] = new Object[attrsLength];
                        for (int i = 0; i < attrsLength; i++) {
                                attrs2[i] = attrs.item(i);
                        }
                        Arrays.sort(attrs2, ATTR_COMPARE);
                        Object attrs3[] = attrs2;
                        for (int i = 0; i < attrsLength; i++) {
                                Attr a = (Attr) attrs3[i];
                                boolean included = this._xpathNodeSet.contains(a);
                                boolean inclusive = this._inclusiveNamespaces.contains(a
                                                .getName());
                                if (included) {
                                        if (inclusive) {
                                                this._writer
                                                                .write(HTMLIncludedInclusiveNamespacePrefix);
                                        } else {
                                                this._writer.write(HTMLIncludePrefix);
                                        }
                                } else {
                                        if (inclusive) {
                                                this._writer
                                                                .write(HTMLExcludedInclusiveNamespacePrefix);
                                        } else {
                                                this._writer.write(HTMLExcludePrefix);
                                        }
                                }
                                this.outputAttrToWriter(a.getNodeName(), a.getNodeValue());
                                if (included) {
                                        if (inclusive) {
                                                this._writer
                                                                .write(HTMLIncludedInclusiveNamespaceSuffix);
                                        } else {
                                                this._writer.write(HTMLIncludeSuffix);
                                        }
                                } else {
                                        if (inclusive) {
                                                this._writer
                                                                .write(HTMLExcludedInclusiveNamespaceSuffix);
                                        } else {
                                                this._writer.write(HTMLExcludeSuffix);
                                        }
                                }
                        }
                        if (this._xpathNodeSet.contains(currentNode)) {
                                this._writer.write(HTMLIncludePrefix);
                        } else {
                                this._writer.write(HTMLExcludePrefix);
                        }
                        this._writer.write("&gt;");
                        if (this._xpathNodeSet.contains(currentNode)) {
                                this._writer.write(HTMLIncludeSuffix);
                        } else {
                                this._writer.write(HTMLExcludeSuffix);
                        }
                        for (Node currentChild = currentNode.getFirstChild(); currentChild != null; currentChild = currentChild
                                        .getNextSibling()) {
                                this.canonicalizeXPathNodeSet(currentChild);
                        }
                        if (this._xpathNodeSet.contains(currentNode)) {
                                this._writer.write(HTMLIncludePrefix);
                        } else {
                                this._writer.write(HTMLExcludePrefix);
                        }
                        this._writer.write("&lt;/");
                        this._writer.write(currentElement.getTagName());
                        this._writer.write("&gt;");
                        if (this._xpathNodeSet.contains(currentNode)) {
                                this._writer.write(HTMLIncludeSuffix);
                        } else {
                                this._writer.write(HTMLExcludeSuffix);
                        }
                        break;
                }
        }
        private int getPositionRelativeToDocumentElement(Node currentNode) {
                if (currentNode == null) {
                        return NODE_NOT_BEFORE_OR_AFTER_DOCUMENT_ELEMENT;
                }
                Document doc = currentNode.getOwnerDocument();
                if (currentNode.getParentNode() != doc) {
                        return NODE_NOT_BEFORE_OR_AFTER_DOCUMENT_ELEMENT;
                }
                Element documentElement = doc.getDocumentElement();
                if (documentElement == null) {
                        return NODE_NOT_BEFORE_OR_AFTER_DOCUMENT_ELEMENT;
                }
                if (documentElement == currentNode) {
                        return NODE_NOT_BEFORE_OR_AFTER_DOCUMENT_ELEMENT;
                }
                for (Node x = currentNode; x != null; x = x.getNextSibling()) {
                        if (x == documentElement) {
                                return NODE_BEFORE_DOCUMENT_ELEMENT;
                        }
                }
                return NODE_AFTER_DOCUMENT_ELEMENT;
        }
        private void outputAttrToWriter(String name, String value)
                        throws IOException {
                this._writer.write(" ");
                this._writer.write(name);
                this._writer.write("=\"");
                int length = value.length();
                for (int i = 0; i < length; i++) {
                        char c = value.charAt(i);
                        switch (c) {
                        case '&':
                                this._writer.write("&amp;amp;");
                                break;
                        case '<':
                                this._writer.write("&amp;lt;");
                                break;
                        case '"':
                                this._writer.write("&amp;quot;");
                                break;
                        case 0x09: 
                                this._writer.write("&amp;#x9;");
                                break;
                        case 0x0A: 
                                this._writer.write("&amp;#xA;");
                                break;
                        case 0x0D: 
                                this._writer.write("&amp;#xD;");
                                break;
                        default:
                                this._writer.write(c);
                                break;
                        }
                }
                this._writer.write("\"");
        }
        private void outputPItoWriter(ProcessingInstruction currentPI)
                        throws IOException {
                if (currentPI == null) {
                        return;
                }
                this._writer.write("&lt;?");
                String target = currentPI.getTarget();
                int length = target.length();
                for (int i = 0; i < length; i++) {
                        char c = target.charAt(i);
                        switch (c) {
                        case 0x0D:
                                this._writer.write("&amp;#xD;");
                                break;
                        case ' ':
                                this._writer.write("&middot;");
                                break;
                        case '\n':
                                this._writer.write("&para;\n");
                                break;
                        default:
                                this._writer.write(c);
                                break;
                        }
                }
                String data = currentPI.getData();
                length = data.length();
                if (length > 0) {
                    this._writer.write(" ");
                    for (int i = 0; i < length; i++) {
                        char c = data.charAt(i);
                        switch (c) {
                            case 0x0D:
                                this._writer.write("&amp;#xD;");
                                break;
                            default:
                                this._writer.write(c);
                                break;
                        }
                    }
                }
                this._writer.write("?&gt;");
        }
        private void outputCommentToWriter(Comment currentComment)
                        throws IOException {
                if (currentComment == null) {
                        return;
                }
                this._writer.write("&lt;!--");
                String data = currentComment.getData();
                int length = data.length();
                for (int i = 0; i < length; i++) {
                        char c = data.charAt(i);
                        switch (c) {
                        case 0x0D:
                                this._writer.write("&amp;#xD;");
                                break;
                        case ' ':
                                this._writer.write("&middot;");
                                break;
                        case '\n':
                                this._writer.write("&para;\n");
                                break;
                        default:
                                this._writer.write(c);
                                break;
                        }
                }
                this._writer.write("--&gt;");
        }
        private void outputTextToWriter(String text) throws IOException {
                if (text == null) {
                        return;
                }
                int length = text.length();
                for (int i = 0; i < length; i++) {
                        char c = text.charAt(i);
                        switch (c) {
                        case '&':
                                this._writer.write("&amp;amp;");
                                break;
                        case '<':
                                this._writer.write("&amp;lt;");
                                break;
                        case '>':
                                this._writer.write("&amp;gt;");
                                break;
                        case 0xD:
                                this._writer.write("&amp;#xD;");
                                break;
                        case ' ':
                                this._writer.write("&middot;");
                                break;
                        case '\n':
                                this._writer.write("&para;\n");
                                break;
                        default:
                                this._writer.write(c);
                                break;
                        }
                }
        }
}
