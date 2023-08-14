public abstract class AndroidContentAssist implements IContentAssistProcessor {
    private static Pattern sFirstAttribute = Pattern.compile(
            "^ *[a-zA-Z_:]+ *= *(?:\"[^<\"]*\"|'[^<']*')");  
    private static Pattern sFirstElementWord = Pattern.compile("^[a-zA-Z0-9_:]+"); 
    private static Pattern sWhitespace = Pattern.compile("\\s+"); 
    protected final static String ROOT_ELEMENT = "";
    private ElementDescriptor mRootDescriptor;
    private final int mDescriptorId;
    private AndroidEditor mEditor;
    public AndroidContentAssist(int descriptorId) {
        mDescriptorId = descriptorId;
    }
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
        if (mEditor == null) {
            mEditor = getAndroidEditor(viewer);
            if (mEditor == null) {
                AdtPlugin.log(IStatus.ERROR, "Editor not found during completion");
                return null;
            }
        }
        UiElementNode rootUiNode = mEditor.getUiRootNode();
        Object[] choices = null; 
        String parent = "";      
        String wordPrefix = extractElementPrefix(viewer, offset);
        char needTag = 0;
        boolean isElement = false;
        boolean isAttribute = false;
        Node currentNode = getNode(viewer, offset);
        if (currentNode == null)
            return null;
        UiElementNode currentUiNode =
            rootUiNode == null ? null : rootUiNode.findXmlNode(currentNode);
        if (currentNode == null) {
            return null;
        }
        if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
            parent = currentNode.getNodeName();
            if (wordPrefix.equals(parent)) {
                isElement = true;
                choices = getChoicesForElement(parent, currentNode);
            } else {
                isAttribute = true;
                AttribInfo info = parseAttributeInfo(viewer, offset);
                if (info != null) {
                    choices = getChoicesForAttribute(parent, currentNode, currentUiNode, info);
                    if (info.correctedPrefix != null) {
                        wordPrefix = info.correctedPrefix;
                    }
                    needTag = info.needTag;
                }
            }
        } else if (currentNode.getNodeType() == Node.TEXT_NODE) {
            isElement = true;
            choices = getChoicesForTextNode(currentNode);
        }
        if (choices == null || choices.length == 0) return null;
        if (isElement) {
            int offset2 = offset - wordPrefix.length() - 1;
            int c1 = extractChar(viewer, offset2);
            if (!((c1 == '<') || (c1 == '/' && extractChar(viewer, offset2 - 1) == '<'))) {
                needTag = '<';
            }
        }
        int selectionLength = 0;
        ISelection selection = viewer.getSelectionProvider().getSelection();
        if (selection instanceof TextSelection) {
            TextSelection textSelection = (TextSelection)selection;
            selectionLength = textSelection.getLength();
        }
        return computeProposals(offset, currentNode, choices, wordPrefix, needTag,
                isAttribute, selectionLength);
    }
    private String lookupNamespacePrefix(Node node, String nsUri) {
        if (XmlnsAttributeDescriptor.XMLNS_URI.equals(nsUri)) {
            return "xmlns"; 
        }
        HashSet<String> visited = new HashSet<String>();
        String prefix = null;
        for (; prefix == null &&
                    node != null &&
                    node.getNodeType() == Node.ELEMENT_NODE;
               node = node.getParentNode()) {
            NamedNodeMap attrs = node.getAttributes();
            for (int n = attrs.getLength() - 1; n >= 0; --n) {
                Node attr = attrs.item(n);
                if ("xmlns".equals(attr.getPrefix())) {  
                    String uri = attr.getNodeValue();
                    if (SdkConstants.NS_RESOURCES.equals(uri)) {
                        return attr.getLocalName();
                    }
                    visited.add(uri);
                }
            }
        }
        prefix = SdkConstants.NS_RESOURCES.equals(nsUri) ? "android" : "ns"; 
        String base = prefix;
        for (int i = 1; visited.contains(prefix); i++) {
            prefix = base + Integer.toString(i);
        }
        return prefix;
    }
    private Object[] getChoicesForElement(String parent, Node current_node) {
        ElementDescriptor grandparent = null;
        if (current_node.getParentNode().getNodeType() == Node.ELEMENT_NODE) {
            grandparent = getDescriptor(current_node.getParentNode().getNodeName());
        } else if (current_node.getParentNode().getNodeType() == Node.DOCUMENT_NODE) {
            grandparent = getRootDescriptor();
        }
        if (grandparent != null) {
            for (ElementDescriptor e : grandparent.getChildren()) {
                if (e.getXmlName().startsWith(parent)) {
                    return grandparent.getChildren();
                }
            }
        }
        return null;
    }
    private Object[] getChoicesForAttribute(String parent,
            Node currentNode, UiElementNode currentUiNode, AttribInfo attrInfo) {
        Object[] choices = null;
        if (attrInfo.isInValue) {
            String value = attrInfo.value;
            if (value.startsWith("'") || value.startsWith("\"")) {   
                value = value.substring(1);
                attrInfo.correctedPrefix = value;
            } else {
                attrInfo.needTag = '"';
            }
            if (currentUiNode != null) {
                String attrName = attrInfo.name;
                int pos = attrName.indexOf(':');
                if (pos >= 0) {
                    attrName = attrName.substring(pos + 1);
                }
                UiAttributeNode currAttrNode = null;
                for (UiAttributeNode attrNode : currentUiNode.getUiAttributes()) {
                    if (attrNode.getDescriptor().getXmlLocalName().equals(attrName)) {
                        currAttrNode = attrNode;
                        break;
                    }
                }
                if (currAttrNode != null) {
                    choices = currAttrNode.getPossibleValues(value);
                    if (currAttrNode instanceof UiFlagAttributeNode) {
                        pos = value.indexOf('|');
                        if (pos >= 0) {
                            attrInfo.correctedPrefix = value = value.substring(pos + 1);
                            attrInfo.needTag = 0;
                        }
                    }
                }
            }
            if (choices == null) {
                String greatGrandParentName = null;
                Node grandParent = currentNode.getParentNode();
                if (grandParent != null) {
                    Node greatGrandParent = grandParent.getParentNode();
                    if (greatGrandParent != null) {
                        greatGrandParentName = greatGrandParent.getLocalName();
                    }
                }
                AndroidTargetData data = mEditor.getTargetData();
                if (data != null) {
                    choices = data.getAttributeValues(parent, attrInfo.name, greatGrandParentName);
                }
            }
        } else {
            if (currentUiNode != null) {
                choices = currentUiNode.getAttributeDescriptors();
            } else {
                ElementDescriptor parent_desc = getDescriptor(parent);
                choices = parent_desc.getAttributes();
            }
        }
        return choices;
    }
    private Object[] getChoicesForTextNode(Node currentNode) {
        Object[] choices = null;
        String parent;
        Node parent_node = currentNode.getParentNode();
        if (parent_node.getNodeType() == Node.ELEMENT_NODE) {
            parent = parent_node.getNodeName();
            ElementDescriptor desc = getDescriptor(parent);
            if (desc != null) {
                choices = desc.getChildren();
            }
        } else if (parent_node.getNodeType() == Node.DOCUMENT_NODE) {
            choices = getRootDescriptor().getChildren();
        }
        return choices;
    }
    private ICompletionProposal[] computeProposals(int offset, Node currentNode,
            Object[] choices, String wordPrefix, char need_tag,
            boolean is_attribute, int selectionLength) {
        ArrayList<CompletionProposal> proposals = new ArrayList<CompletionProposal>();
        HashMap<String, String> nsUriMap = new HashMap<String, String>();
        for (Object choice : choices) {
            String keyword = null;
            String nsPrefix = null;
            Image icon = null;
            String tooltip = null;
            if (choice instanceof ElementDescriptor) {
                keyword = ((ElementDescriptor)choice).getXmlName();
                icon    = ((ElementDescriptor)choice).getIcon();
                tooltip = DescriptorsUtils.formatTooltip(((ElementDescriptor)choice).getTooltip());
            } else if (choice instanceof TextValueDescriptor) {
                continue; 
            } else if (choice instanceof SeparatorAttributeDescriptor) {
                continue; 
            } else if (choice instanceof AttributeDescriptor) {
                keyword = ((AttributeDescriptor)choice).getXmlLocalName();
                icon    = ((AttributeDescriptor)choice).getIcon();
                if (choice instanceof TextAttributeDescriptor) {
                    tooltip = ((TextAttributeDescriptor) choice).getTooltip();
                }
                String nsUri = ((AttributeDescriptor)choice).getNamespaceUri();
                if (nsUri != null) {
                    nsPrefix = nsUriMap.get(nsUri);
                    if (nsPrefix == null) {
                        nsPrefix = lookupNamespacePrefix(currentNode, nsUri);
                        nsUriMap.put(nsUri, nsPrefix);
                    }
                }
                if (nsPrefix != null) {
                    nsPrefix += ":"; 
                }
            } else if (choice instanceof String) {
                keyword = (String) choice;
            } else {
                continue; 
            }
            String nsKeyword = nsPrefix == null ? keyword : (nsPrefix + keyword);
            if (keyword.startsWith(wordPrefix) ||
                    (nsPrefix != null && keyword.startsWith(nsPrefix)) ||
                    (nsPrefix != null && nsKeyword.startsWith(wordPrefix))) {
                if (nsPrefix != null) {
                    keyword = nsPrefix + keyword;
                }
                String end_tag = ""; 
                if (need_tag != 0) {
                    if (need_tag == '"') {
                        keyword = need_tag + keyword;
                        end_tag = String.valueOf(need_tag);
                    } else if (need_tag == '<') {
                        if (elementCanHaveChildren(choice)) {
                            end_tag = String.format("></%1$s>", keyword);  
                            keyword = need_tag + keyword;
                        } else {
                            keyword = need_tag + keyword;
                            end_tag = "/>";  
                        }
                    }
                }
                CompletionProposal proposal = new CompletionProposal(
                        keyword + end_tag,                  
                        offset - wordPrefix.length(),           
                        wordPrefix.length() + selectionLength,  
                        keyword.length(),                   
                        icon,                               
                        null,                               
                        null,                               
                        tooltip                             
                        );
                proposals.add(proposal);
            }
        }
        return proposals.toArray(new ICompletionProposal[proposals.size()]);
    }
    private boolean elementCanHaveChildren(Object descriptor) {
        if (descriptor instanceof ElementDescriptor) {
            ElementDescriptor desc = (ElementDescriptor) descriptor;
            if (desc.hasChildren()) {
                return true;
            }
            for (AttributeDescriptor attr_desc : desc.getAttributes()) {
                if (attr_desc instanceof TextValueDescriptor) {
                    return true;
                }
            }
        }
        return false;
    }
    private ElementDescriptor getDescriptor(String nodeName) {
        return getRootDescriptor().findChildrenDescriptor(nodeName, true );
    }
    public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
        return null;
    }
    public char[] getCompletionProposalAutoActivationCharacters() {
        return new char[]{ '<', ':', '=' };
    }
    public char[] getContextInformationAutoActivationCharacters() {
        return null;
    }
    public IContextInformationValidator getContextInformationValidator() {
        return null;
    }
    public String getErrorMessage() {
        return null;
    }
    protected String extractElementPrefix(ITextViewer viewer, int offset) {
        int i = offset;
        IDocument document = viewer.getDocument();
        if (i > document.getLength()) return ""; 
        try {
            for (; i > 0; --i) {
                char ch = document.getChar(i - 1);
                if (Character.isWhitespace(ch) ||
                        ch == '<' || ch == '>' || ch == '\'' || ch == '"' || ch == '=') {
                    break;
                }
            }
            return document.get(i, offset - i);
        } catch (BadLocationException e) {
            return ""; 
        }
    }
    protected char extractChar(ITextViewer viewer, int offset) {
        IDocument document = viewer.getDocument();
        if (offset > document.getLength()) return 0;
        try {
            return document.getChar(offset);
        } catch (BadLocationException e) {
            return 0;
        }
    }
    private class AttribInfo {
        public boolean isInValue = false;
        public String name = null;
        public String value = null;
        public String correctedPrefix = null;
        public char needTag = 0;
    }
    private AttribInfo parseAttributeInfo(ITextViewer viewer, int offset) {
        AttribInfo info = new AttribInfo();
        IDocument document = viewer.getDocument();
        int n = document.getLength();
        if (offset <= n) {
            try {
                n = offset;
                for (;offset > 0; --offset) {
                    char ch = document.getChar(offset - 1);
                    if (ch == '<') break;
                }
                String text = document.get(offset, n - offset);
                text = sWhitespace.matcher(text).replaceAll(" "); 
                text = sFirstElementWord.matcher(text).replaceFirst("");  
                if (!text.startsWith(" ")) { 
                    return null;
                }
                String temp;
                do {
                    temp = text;
                    text = sFirstAttribute.matcher(temp).replaceFirst("");  
                } while(!temp.equals(text));
                int pos_equal = text.indexOf('=');
                if (pos_equal == -1) {
                    info.isInValue = false;
                    info.name = text.trim();
                } else {
                    info.isInValue = true;
                    info.name = text.substring(0, pos_equal).trim();
                    info.value = text.substring(pos_equal + 1).trim();
                }
                return info;
            } catch (BadLocationException e) {
            }
        }
        return null;
    }
    protected Node getNode(ITextViewer viewer, int offset) {
        Node node = null;
        try {
            IModelManager mm = StructuredModelManager.getModelManager();
            if (mm != null) {
                IStructuredModel model = mm.getExistingModelForRead(viewer.getDocument());
                if (model != null) {
                    for(; offset >= 0 && node == null; --offset) {
                        node = (Node) model.getIndexedRegion(offset);
                    }
                }
            }
        } catch (Exception e) {
        }
        return node;
    }
    private ElementDescriptor getRootDescriptor() {
        if (mRootDescriptor == null) {
            AndroidTargetData data = mEditor.getTargetData();
            if (data != null) {
                IDescriptorProvider descriptorProvider = data.getDescriptorProvider(mDescriptorId);
                if (descriptorProvider != null) {
                    mRootDescriptor = new ElementDescriptor("",     
                            descriptorProvider.getRootElementDescriptors());
                }
            }
        }
        return mRootDescriptor;
    }
    private AndroidEditor getAndroidEditor(ITextViewer viewer) {
        IWorkbenchWindow wwin = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (wwin != null) {
            IWorkbenchPage page = wwin.getActivePage();
            if (page != null) {
                IEditorPart editor = page.getActiveEditor();
                if (editor instanceof AndroidEditor) {
                    ISourceViewer ssviewer = ((AndroidEditor) editor).getStructuredSourceViewer();
                    if (ssviewer == viewer) {
                        return (AndroidEditor) editor;
                    }
                }
            }
        }
        return null;
    }
}
