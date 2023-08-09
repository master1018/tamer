public final class AttrsXmlParser {
    private Document mDocument;
    private String mOsAttrsXmlPath;
    private HashMap<String, AttributeInfo> mAttributeMap;
    private HashMap<String, DeclareStyleableInfo> mStyleMap;
    private Map<String, Map<String, Integer>> mEnumFlagValues;
    public AttrsXmlParser(String osAttrsXmlPath) {
        this(osAttrsXmlPath, null );
    }
    public AttrsXmlParser(String osAttrsXmlPath, AttrsXmlParser inheritableAttributes) {
        mOsAttrsXmlPath = osAttrsXmlPath;
        mStyleMap = new HashMap<String, DeclareStyleableInfo>();
        if (inheritableAttributes == null) {
            mAttributeMap = new HashMap<String, AttributeInfo>();
            mEnumFlagValues = new HashMap<String, Map<String,Integer>>();
        } else {
            mAttributeMap = new HashMap<String, AttributeInfo>(inheritableAttributes.mAttributeMap);
            mEnumFlagValues = new HashMap<String, Map<String,Integer>>(
                                                             inheritableAttributes.mEnumFlagValues);
        }
    }
    public String getOsAttrsXmlPath() {
        return mOsAttrsXmlPath;
    }
    public AttrsXmlParser preload() {
        Document doc = getDocument();
        if (doc == null) {
            AdtPlugin.log(IStatus.WARNING, "Failed to find %1$s", 
                    mOsAttrsXmlPath);
            return this;
        }
        Node res = doc.getFirstChild();
        while (res != null &&
                res.getNodeType() != Node.ELEMENT_NODE &&
                !res.getNodeName().equals("resources")) { 
            res = res.getNextSibling();
        }
        if (res == null) {
            AdtPlugin.log(IStatus.WARNING, "Failed to find a <resources> node in %1$s", 
                    mOsAttrsXmlPath);
            return this;
        }
        parseResources(res);
        return this;
    }
    public void loadViewAttributes(ViewClassInfo info) {
        if (getDocument() != null) {
            String xmlName = info.getShortClassName();
            DeclareStyleableInfo style = mStyleMap.get(xmlName);
            if (style != null) {
                info.setAttributes(style.getAttributes());
                info.setJavaDoc(style.getJavaDoc());
            }
        }
    }
    public void loadLayoutParamsAttributes(LayoutParamsInfo info) {
        if (getDocument() != null) {
            String xmlName = String.format("%1$s_%2$s", 
                    info.getViewLayoutClass().getShortClassName(),
                    info.getShortClassName());
            xmlName = xmlName.replaceFirst("Params$", ""); 
            DeclareStyleableInfo style = mStyleMap.get(xmlName);
            if (style != null) {
                info.setAttributes(style.getAttributes());
            }
        }
    }
    public Map<String, DeclareStyleableInfo> getDeclareStyleableList() {
        return Collections.unmodifiableMap(mStyleMap);
    }
    public Map<String, Map<String, Integer>> getEnumFlagValues() {
        return mEnumFlagValues;
    }
    private Document getDocument() {
        if (mDocument == null) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringComments(false);
            try {
                DocumentBuilder builder = factory.newDocumentBuilder();
                mDocument = builder.parse(new File(mOsAttrsXmlPath));
            } catch (ParserConfigurationException e) {
                AdtPlugin.log(e, "Failed to create XML document builder for %1$s", 
                        mOsAttrsXmlPath);
            } catch (SAXException e) {
                AdtPlugin.log(e, "Failed to parse XML document %1$s", 
                        mOsAttrsXmlPath);
            } catch (IOException e) {
                AdtPlugin.log(e, "Failed to read XML document %1$s", 
                        mOsAttrsXmlPath);
            }
        }
        return mDocument;
    }
    private void parseResources(Node res) {
        Node lastComment = null;
        for (Node node = res.getFirstChild(); node != null; node = node.getNextSibling()) {
            switch (node.getNodeType()) {
            case Node.COMMENT_NODE:
                lastComment = node;
                break;
            case Node.ELEMENT_NODE:
                if (node.getNodeName().equals("declare-styleable")) {          
                    Node nameNode = node.getAttributes().getNamedItem("name"); 
                    if (nameNode != null) {
                        String name = nameNode.getNodeValue();
                        Node parentNode = node.getAttributes().getNamedItem("parent"); 
                        String parents = parentNode == null ? null : parentNode.getNodeValue();
                        if (name != null && !mStyleMap.containsKey(name)) {
                            DeclareStyleableInfo style = parseDeclaredStyleable(name, node);
                            if (parents != null) {
                                style.setParents(parents.split("[ ,|]"));  
                            }
                            mStyleMap.put(name, style);
                            if (lastComment != null) {
                                style.setJavaDoc(parseJavadoc(lastComment.getNodeValue()));
                            }
                        }
                    }
                } else if (node.getNodeName().equals("attr")) {                
                    parseAttr(node, lastComment);
                }
                lastComment = null;
                break;
            }
        }
    }
    private AttributeInfo parseAttr(Node attrNode, Node lastComment) {
        AttributeInfo info = null;
        Node nameNode = attrNode.getAttributes().getNamedItem("name"); 
        if (nameNode != null) {
            String name = nameNode.getNodeValue();
            if (name != null) {
                info = mAttributeMap.get(name);
                if (info == null || info.getFormats().length == 0) {
                    info = parseAttributeTypes(attrNode, name);
                    if (info != null) {
                        mAttributeMap.put(name, info);
                    }
                } else if (lastComment != null) {
                    info = new AttributeInfo(info);
                }
                if (info != null) {
                    if (lastComment != null) {
                        info.setJavaDoc(parseJavadoc(lastComment.getNodeValue()));
                        info.setDeprecatedDoc(parseDeprecatedDoc(lastComment.getNodeValue()));
                    }
                }
            }
        }
        return info;
    }
    private DeclareStyleableInfo parseDeclaredStyleable(String styleName,
            Node declareStyleableNode) {
        ArrayList<AttributeInfo> attrs = new ArrayList<AttributeInfo>();
        Node lastComment = null;
        for (Node node = declareStyleableNode.getFirstChild();
             node != null;
             node = node.getNextSibling()) {
            switch (node.getNodeType()) {
            case Node.COMMENT_NODE:
                lastComment = node;
                break;
            case Node.ELEMENT_NODE:
                if (node.getNodeName().equals("attr")) {                       
                    AttributeInfo info = parseAttr(node, lastComment);
                    if (info != null) {
                        attrs.add(info);
                    }
                }
                lastComment = null;
                break;
            }
        }
        return new DeclareStyleableInfo(styleName, attrs.toArray(new AttributeInfo[attrs.size()]));
    }
    private AttributeInfo parseAttributeTypes(Node attrNode, String name) {
        TreeSet<AttributeInfo.Format> formats = new TreeSet<AttributeInfo.Format>();
        String[] enumValues = null;
        String[] flagValues = null;
        Node attrFormat = attrNode.getAttributes().getNamedItem("format"); 
        if (attrFormat != null) {
            for (String f : attrFormat.getNodeValue().split("\\|")) { 
                try {
                    Format format = AttributeInfo.Format.valueOf(f.toUpperCase());
                    if (format != null &&
                            format != AttributeInfo.Format.ENUM &&
                            format != AttributeInfo.Format.FLAG) {
                        formats.add(format);
                    }
                } catch (IllegalArgumentException e) {
                    AdtPlugin.log(e, "Unknown format name '%s' in <attr name=\"%s\">, file '%s'.", 
                            f, name, getOsAttrsXmlPath());
                }
            }
        }
        enumValues = parseEnumFlagValues(attrNode, "enum", name); 
        if (enumValues != null) {
            formats.add(AttributeInfo.Format.ENUM);
        }
        flagValues = parseEnumFlagValues(attrNode, "flag", name); 
        if (flagValues != null) {
            formats.add(AttributeInfo.Format.FLAG);
        }
        AttributeInfo info = new AttributeInfo(name,
                formats.toArray(new AttributeInfo.Format[formats.size()]));
        info.setEnumValues(enumValues);
        info.setFlagValues(flagValues);
        return info;
    }
    private String[] parseEnumFlagValues(Node attrNode, String filter, String attrName) {
        ArrayList<String> names = null;
        for (Node child = attrNode.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals(filter)) {
                Node nameNode = child.getAttributes().getNamedItem("name");  
                if (nameNode == null) {
                    AdtPlugin.log(IStatus.WARNING,
                            "Missing name attribute in <attr name=\"%s\"><%s></attr>", 
                            attrName, filter);
                } else {
                    if (names == null) {
                        names = new ArrayList<String>();
                    }
                    String name = nameNode.getNodeValue();
                    names.add(name);
                    Node valueNode = child.getAttributes().getNamedItem("value");  
                    if (valueNode == null) {
                        AdtPlugin.log(IStatus.WARNING,
                                "Missing value attribute in <attr name=\"%s\"><%s name=\"%s\"></attr>", 
                                attrName, filter, name);
                    } else {
                        String value = valueNode.getNodeValue();
                        try {
                            int i = value.startsWith("0x") ?
                                    Integer.parseInt(value.substring(2), 16 ) :
                                    Integer.parseInt(value);
                            Map<String, Integer> map = mEnumFlagValues.get(attrName);
                            if (map == null) {
                                map = new HashMap<String, Integer>();
                                mEnumFlagValues.put(attrName, map);
                            }
                            map.put(name, Integer.valueOf(i));
                        } catch(NumberFormatException e) {
                            AdtPlugin.log(e,
                                    "Value in <attr name=\"%s\"><%s name=\"%s\" value=\"%s\"></attr> is not a valid decimal or hexadecimal", 
                                    attrName, filter, name, value);
                        }
                    }
                }
            }
        }
        return names == null ? null : names.toArray(new String[names.size()]);
    }
    private String parseJavadoc(String comment) {
        if (comment == null) {
            return null;
        }
        comment = comment.replaceAll("\\s+", " "); 
        comment = comment.replaceAll("(?:\\{@deprecated[^}]*\\}|@deprecated[^@}]*)", "");
        comment = comment.replaceFirst("^\\s*(.*?(?:$|(?<![a-zA-Z]\\.[a-zA-Z])\\.(?=\\s))).*", "$1"); 
        return comment;
    }
    private String parseDeprecatedDoc(String comment) {
        if (comment == null) {
            return null;
        }
        comment = comment.replaceAll("\\s+", " "); 
        int pos = comment.indexOf("{@deprecated");
        if (pos >= 0) {
            comment = comment.substring(pos + 12 );
            comment = comment.replaceFirst("^([^}]*).*", "$1");
        } else if ((pos = comment.indexOf("@deprecated")) >= 0) {
            comment = comment.substring(pos + 11 );
            comment = comment.replaceFirst("^(.*?)(?:@.*|$)", "$1");
        } else {
            return null;
        }
        return comment.trim();
    }
}
