public class RepoSource implements IDescription {
    private String mUrl;
    private final boolean mUserSource;
    private Package[] mPackages;
    private String mDescription;
    private String mFetchError;
    public RepoSource(String url, boolean userSource) {
        if (url.endsWith("/")) {  
            url += SdkRepository.URL_DEFAULT_XML_FILE;
        }
        mUrl = url;
        mUserSource = userSource;
        setDefaultDescription();
    }
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RepoSource) {
            RepoSource rs = (RepoSource) obj;
            return  rs.isUserSource() == this.isUserSource() && rs.getUrl().equals(this.getUrl());
        }
        return false;
    }
    @Override
    public int hashCode() {
        return mUrl.hashCode() ^ Boolean.valueOf(mUserSource).hashCode();
    }
    public boolean isUserSource() {
        return mUserSource;
    }
    public String getUrl() {
        return mUrl;
    }
    public Package[] getPackages() {
        return mPackages;
    }
    public void clearPackages() {
        mPackages = null;
    }
    public String getShortDescription() {
        return mUrl;
    }
    public String getLongDescription() {
        return mDescription == null ? "" : mDescription;  
    }
    public String getFetchError() {
        return mFetchError;
    }
    public void load(ITaskMonitor monitor, boolean forceHttp) {
        monitor.setProgressMax(4);
        setDefaultDescription();
        String url = mUrl;
        if (forceHttp) {
            url = url.replaceAll("https:
        }
        monitor.setDescription("Fetching %1$s", url);
        monitor.incProgress(1);
        mFetchError = null;
        Boolean[] validatorFound = new Boolean[] { Boolean.FALSE };
        String[] validationError = new String[] { null };
        Exception[] exception = new Exception[] { null };
        ByteArrayInputStream xml = fetchUrl(url, exception);
        Document validatedDoc = null;
        boolean usingAlternateXml = false;
        String validatedUri = null;
        if (xml != null) {
            monitor.setDescription("Validate XML");
            String uri = validateXml(xml, url, validationError, validatorFound);
            if (uri != null) {
                validatedDoc = getDocument(xml, monitor);
                validatedUri = uri;
            } else if (validatorFound[0].equals(Boolean.TRUE)) {
                validatedDoc = findAlternateToolsXml(xml);
                if (validatedDoc != null) {
                    validationError[0] = null;  
                    validatedUri = SdkRepository.NS_SDK_REPOSITORY;
                    usingAlternateXml = true;
                }
            } else {
                mFetchError = "No suitable XML Schema Validator could be found in your Java environment. Please update your version of Java.";
            }
        }
        if (validatedDoc == null && !url.endsWith(SdkRepository.URL_DEFAULT_XML_FILE)) {
            if (!url.endsWith("/")) {       
                url += "/";                 
            }
            url += SdkRepository.URL_DEFAULT_XML_FILE;
            xml = fetchUrl(url, exception);
            if (xml != null) {
                String uri = validateXml(xml, url, validationError, validatorFound);
                if (uri != null) {
                    validationError[0] = null;  
                    validatedDoc = getDocument(xml, monitor);
                    validatedUri = uri;
                } else if (validatorFound[0].equals(Boolean.TRUE)) {
                    validatedDoc = findAlternateToolsXml(xml);
                    if (validatedDoc != null) {
                        validationError[0] = null;  
                        validatedUri = SdkRepository.NS_SDK_REPOSITORY;
                        usingAlternateXml = true;
                    }
                } else {
                    mFetchError = "No suitable XML Schema Validator could be found in your Java environment. Please update your version of Java.";
                }
            }
            if (validatedDoc != null) {
                monitor.setResult("Repository found at %1$s", url);
                mUrl = url;
            }
        }
        if (exception[0] != null) {
            mFetchError = "Failed to fetch URL";
            String reason = null;
            if (exception[0] instanceof FileNotFoundException) {
                reason = "File not found";
                mFetchError += ": " + reason;
            } else if (exception[0] instanceof SSLKeyException) {
                reason = "HTTPS SSL error. You might want to force download through HTTP in the settings.";
                mFetchError += ": HTTPS SSL error";
            } else if (exception[0].getMessage() != null) {
                reason = exception[0].getMessage();
            } else {
                reason = String.format("Unknown (%1$s)", exception[0].getClass().getName());
            }
            monitor.setResult("Failed to fetch URL %1$s, reason: %2$s", url, reason);
        }
        if(validationError[0] != null) {
            monitor.setResult("%s", validationError[0]);  
        }
        if (validatedDoc == null) {
            return;
        }
        if (usingAlternateXml) {
            boolean isADT = false;
            try {
                Class<?> adt = Class.forName("com.android.ide.eclipse.adt.AdtPlugin");  
                isADT = (adt != null);
            } catch (ClassNotFoundException e) {
            }
            String info;
            if (isADT) {
                info = "This repository requires a more recent version of ADT. Please update the Eclipse Android plugin.";
                mDescription = "This repository requires a more recent version of ADT, the Eclipse Android plugin.\nYou must update it before you can see other new packages.";
            } else {
                info = "This repository requires a more recent version of the Tools. Please update.";
                mDescription = "This repository requires a more recent version of the Tools.\nYou must update it before you can see other new packages.";
            }
            mFetchError = mFetchError == null ? info : mFetchError + ". " + info;
        }
        monitor.incProgress(1);
        if (xml != null) {
            monitor.setDescription("Parse XML");
            monitor.incProgress(1);
            parsePackages(validatedDoc, validatedUri, monitor);
            if (mPackages == null || mPackages.length == 0) {
                mDescription += "\nNo packages found.";
            } else if (mPackages.length == 1) {
                mDescription += "\nOne package found.";
            } else {
                mDescription += String.format("\n%1$d packages found.", mPackages.length);
            }
        }
        monitor.incProgress(1);
    }
    private void setDefaultDescription() {
        if (mUserSource) {
            mDescription = String.format("Add-on Source: %1$s", mUrl);
        } else {
            mDescription = String.format("SDK Source: %1$s", mUrl);
        }
    }
    private ByteArrayInputStream fetchUrl(String urlString, Exception[] outException) {
        URL url;
        try {
            url = new URL(urlString);
            InputStream is = null;
            int inc = 65536;
            int curr = 0;
            byte[] result = new byte[inc];
            try {
                is = url.openStream();
                int n;
                while ((n = is.read(result, curr, result.length - curr)) != -1) {
                    curr += n;
                    if (curr == result.length) {
                        byte[] temp = new byte[curr + inc];
                        System.arraycopy(result, 0, temp, 0, curr);
                        result = temp;
                    }
                }
                return new ByteArrayInputStream(result, 0, curr);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                }
            }
        } catch (Exception e) {
            outException[0] = e;
        }
        return null;
    }
    private String validateXml(ByteArrayInputStream xml, String url,
            String[] outError, Boolean[] validatorFound) {
        String lastError = null;
        String extraError = null;
        for (int version = SdkRepository.NS_LATEST_VERSION; version >= 1; version--) {
            try {
                Validator validator = getValidator(version);
                if (validator == null) {
                    lastError = "XML verification failed for %1$s.\nNo suitable XML Schema Validator could be found in your Java environment. Please consider updating your version of Java.";
                    validatorFound[0] = Boolean.FALSE;
                    continue;
                }
                validatorFound[0] = Boolean.TRUE;
                xml.reset();
                validator.validate(new StreamSource(xml));
                return SdkRepository.getSchemaUri(version);
            } catch (Exception e) {
                lastError = "XML verification failed for %1$s.\nError: %2$s";
                extraError = e.getMessage();
                if (extraError == null) {
                    extraError = e.getClass().getName();
                }
            }
        }
        if (lastError != null) {
            outError[0] = String.format(lastError, url, extraError);
        }
        return null;
    }
    protected Document findAlternateToolsXml(InputStream xml) {
        if (xml == null) {
            return null;
        }
        try {
            xml.reset();
        } catch (IOException e1) {
        }
        Document oldDoc = null;
        Document newDoc = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringComments(false);
            factory.setValidating(false);
            factory.setNamespaceAware(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            oldDoc = builder.parse(xml);
            factory.setNamespaceAware(true);
            builder = factory.newDocumentBuilder();
            newDoc = builder.newDocument();
        } catch (Exception e) {
        }
        if (oldDoc == null || newDoc == null) {
            return null;
        }
        Pattern nsPattern = Pattern.compile(SdkRepository.NS_SDK_REPOSITORY_PATTERN);
        Node oldRoot = null;
        String prefix = null;
        for (Node child = oldDoc.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                prefix = null;
                String name = child.getNodeName();
                int pos = name.indexOf(':');
                if (pos > 0 && pos < name.length() - 1) {
                    prefix = name.substring(0, pos);
                    name = name.substring(pos + 1);
                }
                if (SdkRepository.NODE_SDK_REPOSITORY.equals(name)) {
                    NamedNodeMap attrs = child.getAttributes();
                    String xmlns = "xmlns";                                         
                    if (prefix != null) {
                        xmlns += ":" + prefix;                                      
                    }
                    Node attr = attrs.getNamedItem(xmlns);
                    if (attr != null) {
                        String uri = attr.getNodeValue();
                        if (uri != null && nsPattern.matcher(uri).matches()) {
                            oldRoot = child;
                            break;
                        }
                    }
                }
            }
        }
        if (oldRoot == null || prefix == null || prefix.length() == 0) {
            return null;
        }
        final String ns = SdkRepository.NS_SDK_REPOSITORY;
        Element newRoot = newDoc.createElementNS(ns, SdkRepository.NODE_SDK_REPOSITORY);
        newRoot.setPrefix(prefix);
        newDoc.appendChild(newRoot);
        int numTool = 0;
        Node tool = null;
        while ((tool = findChild(oldRoot, tool, prefix, SdkRepository.NODE_TOOL)) != null) {
            try {
                Node revision = findChild(tool, null, prefix, SdkRepository.NODE_REVISION);
                Node archives = findChild(tool, null, prefix, SdkRepository.NODE_ARCHIVES);
                if (revision == null || archives == null) {
                    continue;
                }
                int rev = 0;
                try {
                    String content = revision.getTextContent();
                    content = content.trim();
                    rev = Integer.parseInt(content);
                    if (rev < 1) {
                        continue;
                    }
                } catch (NumberFormatException ignore) {
                    continue;
                }
                Element newTool = newDoc.createElementNS(ns, SdkRepository.NODE_TOOL);
                newTool.setPrefix(prefix);
                appendChild(newTool, ns, prefix,
                        SdkRepository.NODE_REVISION, Integer.toString(rev));
                Element newArchives = appendChild(newTool, ns, prefix,
                                                  SdkRepository.NODE_ARCHIVES, null);
                int numArchives = 0;
                Node archive = null;
                while ((archive = findChild(archives,
                                            archive,
                                            prefix,
                                            SdkRepository.NODE_ARCHIVE)) != null) {
                    try {
                        Os os = (Os) XmlParserUtils.getEnumAttribute(archive,
                                SdkRepository.ATTR_OS,
                                Os.values(),
                                null );
                        Arch arch = (Arch) XmlParserUtils.getEnumAttribute(archive,
                                SdkRepository.ATTR_ARCH,
                                Arch.values(),
                                Arch.ANY);
                        if (os == null || !os.isCompatible() ||
                                arch == null || !arch.isCompatible()) {
                            continue;
                        }
                        Node node = findChild(archive, null, prefix, SdkRepository.NODE_URL);
                        String url = node == null ? null : node.getTextContent().trim();
                        if (url == null || url.length() == 0) {
                            continue;
                        }
                        node = findChild(archive, null, prefix, SdkRepository.NODE_SIZE);
                        long size = 0;
                        try {
                            size = Long.parseLong(node.getTextContent());
                        } catch (Exception e) {
                        }
                        if (size < 1) {
                            continue;
                        }
                        node = findChild(archive, null, prefix, SdkRepository.NODE_CHECKSUM);
                        if (node == null) {
                            continue;
                        }
                        NamedNodeMap attrs = node.getAttributes();
                        Node typeNode = attrs.getNamedItem(SdkRepository.ATTR_TYPE);
                        if (typeNode == null ||
                                !SdkRepository.ATTR_TYPE.equals(typeNode.getNodeName()) ||
                                !SdkRepository.SHA1_TYPE.equals(typeNode.getNodeValue())) {
                            continue;
                        }
                        String sha1 = node == null ? null : node.getTextContent().trim();
                        if (sha1 == null || sha1.length() != SdkRepository.SHA1_CHECKSUM_LEN) {
                            continue;
                        }
                        Element ar = appendChild(newArchives, ns, prefix,
                                                 SdkRepository.NODE_ARCHIVE, null);
                        ar.setAttributeNS(ns, SdkRepository.ATTR_OS, os.getXmlName());
                        ar.setAttributeNS(ns, SdkRepository.ATTR_ARCH, arch.getXmlName());
                        appendChild(ar, ns, prefix, SdkRepository.NODE_URL, url);
                        appendChild(ar, ns, prefix, SdkRepository.NODE_SIZE, Long.toString(size));
                        Element cs = appendChild(ar, ns, prefix, SdkRepository.NODE_CHECKSUM, sha1);
                        cs.setAttributeNS(ns, SdkRepository.ATTR_TYPE, SdkRepository.SHA1_TYPE);
                        numArchives++;
                    } catch (Exception ignore1) {
                    }
                } 
                if (numArchives > 0) {
                    newRoot.appendChild(newTool);
                    numTool++;
                }
            } catch (Exception ignore2) {
            }
        } 
        return numTool > 0 ? newDoc : null;
    }
    private Node findChild(Node rootNode, Node after, String prefix, String nodeName) {
        nodeName = prefix + ":" + nodeName;
        Node child = after == null ? rootNode.getFirstChild() : after.getNextSibling();
        for(; child != null; child = child.getNextSibling()) {
            if (child.getNodeType() == Node.ELEMENT_NODE && nodeName.equals(child.getNodeName())) {
                return child;
            }
        }
        return null;
    }
    private Element appendChild(Element rootNode, String namespaceUri,
            String prefix, String nodeName,
            String nodeValue) {
        Element node = rootNode.getOwnerDocument().createElementNS(namespaceUri, nodeName);
        node.setPrefix(prefix);
        if (nodeValue != null) {
            node.setTextContent(nodeValue);
        }
        rootNode.appendChild(node);
        return node;
    }
    private Validator getValidator(int version) throws SAXException {
        InputStream xsdStream = SdkRepository.getXsdStream(version);
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        if (factory == null) {
            return null;
        }
        Schema schema = factory.newSchema(new StreamSource(xsdStream));
        Validator validator = schema == null ? null : schema.newValidator();
        return validator;
    }
    protected boolean parsePackages(Document doc, String nsUri, ITaskMonitor monitor) {
        assert doc != null;
        Node root = getFirstChild(doc, nsUri, SdkRepository.NODE_SDK_REPOSITORY);
        if (root != null) {
            ArrayList<Package> packages = new ArrayList<Package>();
            HashMap<String, String> licenses = new HashMap<String, String>();
            for (Node child = root.getFirstChild();
                 child != null;
                 child = child.getNextSibling()) {
                if (child.getNodeType() == Node.ELEMENT_NODE &&
                        nsUri.equals(child.getNamespaceURI()) &&
                        child.getLocalName().equals(SdkRepository.NODE_LICENSE)) {
                    Node id = child.getAttributes().getNamedItem(SdkRepository.ATTR_ID);
                    if (id != null) {
                        licenses.put(id.getNodeValue(), child.getTextContent());
                    }
                }
            }
            for (Node child = root.getFirstChild();
                 child != null;
                 child = child.getNextSibling()) {
                if (child.getNodeType() == Node.ELEMENT_NODE &&
                        nsUri.equals(child.getNamespaceURI())) {
                    String name = child.getLocalName();
                    Package p = null;
                    try {
                        if (SdkRepository.NODE_ADD_ON.equals(name)) {
                            p = new AddonPackage(this, child, licenses);
                        } else if (SdkRepository.NODE_EXTRA.equals(name)) {
                            p = new ExtraPackage(this, child, licenses);
                        } else if (!mUserSource) {
                            if (SdkRepository.NODE_PLATFORM.equals(name)) {
                                p = new PlatformPackage(this, child, licenses);
                            } else if (SdkRepository.NODE_DOC.equals(name)) {
                                p = new DocPackage(this, child, licenses);
                            } else if (SdkRepository.NODE_TOOL.equals(name)) {
                                p = new ToolPackage(this, child, licenses);
                            } else if (SdkRepository.NODE_SAMPLE.equals(name)) {
                                p = new SamplePackage(this, child, licenses);
                            }
                        }
                        if (p != null) {
                            packages.add(p);
                            monitor.setDescription("Found %1$s", p.getShortDescription());
                        }
                    } catch (Exception e) {
                    }
                }
            }
            mPackages = packages.toArray(new Package[packages.size()]);
            Arrays.sort(mPackages, null);
            return true;
        }
        return false;
    }
    private Node getFirstChild(Node node, String nsUri, String xmlLocalName) {
        for(Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeType() == Node.ELEMENT_NODE &&
                    nsUri.equals(child.getNamespaceURI())) {
                if (xmlLocalName == null || child.getLocalName().equals(xmlLocalName)) {
                    return child;
                }
            }
        }
        return null;
    }
    private Document getDocument(ByteArrayInputStream xml, ITaskMonitor monitor) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringComments(true);
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            xml.reset();
            Document doc = builder.parse(new InputSource(xml));
            return doc;
        } catch (ParserConfigurationException e) {
            monitor.setResult("Failed to create XML document builder");
        } catch (SAXException e) {
            monitor.setResult("Failed to parse XML document");
        } catch (IOException e) {
            monitor.setResult("Failed to read XML document");
        }
        return null;
    }
}
