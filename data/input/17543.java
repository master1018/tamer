public class XMLSignatureInput implements Cloneable {
    static java.util.logging.Logger log =
        java.util.logging.Logger.getLogger
            (XMLSignatureInput.class.getName());
    InputStream _inputOctetStreamProxy = null;
    Set _inputNodeSet = null;
    Node _subNode=null;
    Node excludeNode=null;
    boolean excludeComments=false;
    boolean isNodeSet=false;
    byte []bytes=null;
    private String _MIMEType = null;
    private String _SourceURI = null;
    List nodeFilters=new ArrayList();
    boolean needsToBeExpanded=false;
    OutputStream outputStream=null;
    public boolean isNeedsToBeExpanded() {
        return needsToBeExpanded;
    }
    public void setNeedsToBeExpanded(boolean needsToBeExpanded) {
        this.needsToBeExpanded = needsToBeExpanded;
    }
    public XMLSignatureInput(byte[] inputOctets) {
        this.bytes=inputOctets;
    }
    public XMLSignatureInput(InputStream inputOctetStream)  {
        this._inputOctetStreamProxy=inputOctetStream;
    }
    public XMLSignatureInput(String inputStr) {
        this(inputStr.getBytes());
    }
    public XMLSignatureInput(String inputStr, String encoding)
           throws UnsupportedEncodingException {
        this(inputStr.getBytes(encoding));
    }
    public XMLSignatureInput(Node rootNode)
    {
        this._subNode = rootNode;
    }
    public XMLSignatureInput(Set inputNodeSet) {
        this._inputNodeSet = inputNodeSet;
    }
    public Set getNodeSet() throws CanonicalizationException,
        ParserConfigurationException, IOException, SAXException {
        return getNodeSet(false);
    }
    public Set getNodeSet(boolean circumvent)
           throws ParserConfigurationException, IOException, SAXException,
                  CanonicalizationException {
        if (this._inputNodeSet!=null) {
            return this._inputNodeSet;
        }
        if ((this._inputOctetStreamProxy==null)&& (this._subNode!=null) ) {
            if (circumvent) {
                XMLUtils.circumventBug2650(XMLUtils.getOwnerDocument(_subNode));
            }
            this._inputNodeSet = new HashSet();
            XMLUtils.getSet(_subNode,this._inputNodeSet, excludeNode, this.excludeComments);
            return this._inputNodeSet;
        } else if (this.isOctetStream()) {
            convertToNodes();
            HashSet result=new HashSet();
            XMLUtils.getSet(_subNode, result,null,false);
            return result;
        }
        throw new RuntimeException(
            "getNodeSet() called but no input data present");
    }
    public InputStream getOctetStream() throws IOException  {
        return getResetableInputStream();
    }
    public InputStream getOctetStreamReal () {
        return this._inputOctetStreamProxy;
    }
    public byte[] getBytes() throws IOException, CanonicalizationException {
        if (bytes!=null) {
            return bytes;
        }
        InputStream is = getResetableInputStream();
        if (is!=null) {
            if (bytes==null) {
                is.reset();
                bytes=JavaUtils.getBytesFromStream(is);
            }
            return bytes;
        }
        Canonicalizer20010315OmitComments c14nizer =
                new Canonicalizer20010315OmitComments();
        bytes=c14nizer.engineCanonicalize(this);
        return bytes;
    }
    public boolean isNodeSet() {
        return (( (this._inputOctetStreamProxy == null)
              && (this._inputNodeSet != null) ) || isNodeSet);
    }
    public boolean isElement() {
        return ((this._inputOctetStreamProxy==null)&& (this._subNode!=null)
                && (this._inputNodeSet==null) && !isNodeSet);
    }
    public boolean isOctetStream() {
        return ( ((this._inputOctetStreamProxy != null) || bytes!=null)
              && ((this._inputNodeSet == null) && _subNode ==null));
    }
    public boolean isOutputStreamSet() {
        return outputStream != null;
    }
    public boolean isByteArray() {
        return ( (bytes!=null)
              && ((this._inputNodeSet == null) && _subNode ==null));
    }
    public boolean isInitialized() {
        return (this.isOctetStream() || this.isNodeSet());
    }
    public String getMIMEType() {
        return this._MIMEType;
    }
    public void setMIMEType(String MIMEType) {
        this._MIMEType = MIMEType;
    }
    public String getSourceURI() {
        return this._SourceURI;
    }
    public void setSourceURI(String SourceURI) {
        this._SourceURI = SourceURI;
    }
    public String toString() {
        if (this.isNodeSet()) {
            return "XMLSignatureInput/NodeSet/" + this._inputNodeSet.size()
                   + " nodes/" + this.getSourceURI();
        }
        if (this.isElement()) {
            return "XMLSignatureInput/Element/" + this._subNode
                + " exclude "+ this.excludeNode + " comments:" +
                this.excludeComments +"/" + this.getSourceURI();
        }
        try {
            return "XMLSignatureInput/OctetStream/" + this.getBytes().length
                   + " octets/" + this.getSourceURI();
        } catch (IOException iex) {
            return "XMLSignatureInput/OctetStream
        } catch (CanonicalizationException cex) {
            return "XMLSignatureInput/OctetStream
        }
    }
    public String getHTMLRepresentation() throws XMLSignatureException {
        XMLSignatureInputDebugger db = new XMLSignatureInputDebugger(this);
        return db.getHTMLRepresentation();
    }
    public String getHTMLRepresentation(Set inclusiveNamespaces)
           throws XMLSignatureException {
        XMLSignatureInputDebugger db = new XMLSignatureInputDebugger( this,
                                        inclusiveNamespaces);
        return db.getHTMLRepresentation();
    }
    public Node getExcludeNode() {
        return excludeNode;
    }
    public void setExcludeNode(Node excludeNode) {
        this.excludeNode = excludeNode;
    }
    public Node getSubNode() {
        return _subNode;
    }
    public boolean isExcludeComments() {
        return excludeComments;
    }
    public void setExcludeComments(boolean excludeComments) {
        this.excludeComments = excludeComments;
    }
    public void updateOutputStream(OutputStream diOs)
    throws CanonicalizationException, IOException {
        updateOutputStream(diOs, false);
    }
    public void updateOutputStream(OutputStream diOs, boolean c14n11)
    throws CanonicalizationException, IOException {
        if (diOs==outputStream) {
            return;
        }
        if (bytes!=null) {
            diOs.write(bytes);
            return;
        } else if (_inputOctetStreamProxy==null) {
            CanonicalizerBase c14nizer = null;
            if (c14n11) {
                c14nizer = new Canonicalizer11_OmitComments();
            } else {
                c14nizer = new Canonicalizer20010315OmitComments();
            }
            c14nizer.setWriter(diOs);
            c14nizer.engineCanonicalize(this);
            return;
        } else {
            InputStream is = getResetableInputStream();
            if (bytes!=null) {
                diOs.write(bytes,0,bytes.length);
                return;
            }
            is.reset();
            int num;
            byte[] bytesT = new byte[1024];
            while ((num=is.read(bytesT))>0) {
                diOs.write(bytesT,0,num);
            }
        }
    }
    public void setOutputStream(OutputStream os) {
        outputStream=os;
    }
    protected InputStream getResetableInputStream() throws IOException{
        if ((_inputOctetStreamProxy instanceof ByteArrayInputStream) ) {
            if (!_inputOctetStreamProxy.markSupported()) {
                throw new RuntimeException("Accepted as Markable but not truly been"+_inputOctetStreamProxy);
            }
            return _inputOctetStreamProxy;
        }
        if (bytes!=null) {
            _inputOctetStreamProxy=new ByteArrayInputStream(bytes);
            return _inputOctetStreamProxy;
        }
        if (_inputOctetStreamProxy ==null)
            return null;
        if (_inputOctetStreamProxy.markSupported()) {
            log.log(java.util.logging.Level.INFO, "Mark Suported but not used as reset");
        }
        bytes=JavaUtils.getBytesFromStream(_inputOctetStreamProxy);
        _inputOctetStreamProxy.close();
        _inputOctetStreamProxy=new ByteArrayInputStream(bytes);
        return _inputOctetStreamProxy;
    }
    public void addNodeFilter(NodeFilter filter) {
        if (isOctetStream()) {
            try {
                convertToNodes();
            } catch (Exception e) {
                throw new XMLSecurityRuntimeException("signature.XMLSignatureInput.nodesetReference",e);
            }
        }
        nodeFilters.add(filter);
    }
    public List getNodeFilters() {
        return nodeFilters;
    }
    public void setNodeSet(boolean b) {
        isNodeSet=b;
    }
    void convertToNodes() throws CanonicalizationException,
        ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
        dfactory.setValidating(false);
        dfactory.setNamespaceAware(true);
        DocumentBuilder db = dfactory.newDocumentBuilder();
        try {
            db.setErrorHandler(new com.sun.org.apache.xml.internal.security.utils
               .IgnoreAllErrorHandler());
            Document doc = db.parse(this.getOctetStream());
            this._subNode=doc.getDocumentElement();
        } catch (SAXException ex) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write("<container>".getBytes());
            baos.write(this.getBytes());
            baos.write("</container>".getBytes());
            byte result[] = baos.toByteArray();
            Document document = db.parse(new ByteArrayInputStream(result));
            this._subNode=document.getDocumentElement().getFirstChild().getFirstChild();
        }
        this._inputOctetStreamProxy=null;
        this.bytes=null;
    }
}
