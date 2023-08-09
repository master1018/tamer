public class XPathExpressionImpl  implements javax.xml.xpath.XPathExpression{
    private XPathFunctionResolver functionResolver;
    private XPathVariableResolver variableResolver;
    private JAXPPrefixResolver prefixResolver;
    private org.apache.xpath.XPath xpath;
    private boolean featureSecureProcessing = false;
    protected XPathExpressionImpl() { };
    protected XPathExpressionImpl(org.apache.xpath.XPath xpath, 
            JAXPPrefixResolver prefixResolver, 
            XPathFunctionResolver functionResolver,
            XPathVariableResolver variableResolver ) { 
        this.xpath = xpath;
        this.prefixResolver = prefixResolver;
        this.functionResolver = functionResolver;
        this.variableResolver = variableResolver;
        this.featureSecureProcessing = false;
    };
    protected XPathExpressionImpl(org.apache.xpath.XPath xpath,
            JAXPPrefixResolver prefixResolver,
            XPathFunctionResolver functionResolver,
            XPathVariableResolver variableResolver,
            boolean featureSecureProcessing ) { 
        this.xpath = xpath;
        this.prefixResolver = prefixResolver;
        this.functionResolver = functionResolver;
        this.variableResolver = variableResolver;
        this.featureSecureProcessing = featureSecureProcessing;
    };
    public void setXPath (org.apache.xpath.XPath xpath ) {
        this.xpath = xpath;
    }  
    public Object eval(Object item, QName returnType)
            throws javax.xml.transform.TransformerException {
        XObject resultObject = eval ( item );
        return getResultAsType( resultObject, returnType );
    }
    private XObject eval ( Object contextItem )
            throws javax.xml.transform.TransformerException {
        org.apache.xpath.XPathContext xpathSupport = null;
        if ( functionResolver != null ) {
            JAXPExtensionsProvider jep = new JAXPExtensionsProvider(
                    functionResolver, featureSecureProcessing );
            xpathSupport = new org.apache.xpath.XPathContext(jep, false);
        } else {
            xpathSupport = new org.apache.xpath.XPathContext(false);
        }
        xpathSupport.setVarStack(new JAXPVariableStack(variableResolver));
        XObject xobj = null;
        Node contextNode = (Node)contextItem;
        if ( contextNode == null ) {
              contextNode = getDummyDocument();
        } 
        xobj = xpath.execute(xpathSupport, contextNode, prefixResolver );
        return xobj;
    }
    public Object evaluate(Object item, QName returnType)
        throws XPathExpressionException {
        if ( returnType == null ) {
            String fmsg = XSLMessages.createXPATHMessage( 
                    XPATHErrorResources.ER_ARG_CANNOT_BE_NULL,
                    new Object[] {"returnType"} );
            throw new NullPointerException( fmsg );
        }
        if ( !isSupported ( returnType ) ) {
            String fmsg = XSLMessages.createXPATHMessage( 
                    XPATHErrorResources.ER_UNSUPPORTED_RETURN_TYPE,
                    new Object[] { returnType.toString() } );
            throw new IllegalArgumentException ( fmsg );
        }
        try { 
            return eval( item, returnType);
        } catch ( java.lang.NullPointerException npe ) {
            throw new XPathExpressionException ( npe );
        } catch ( javax.xml.transform.TransformerException te ) {
            Throwable nestedException = te.getException();
            if ( nestedException instanceof javax.xml.xpath.XPathFunctionException ) {
                throw (javax.xml.xpath.XPathFunctionException)nestedException;
            } else {
                throw new XPathExpressionException( te);
            }
        }
    }
    public String evaluate(Object item) 
        throws XPathExpressionException {
        return (String)this.evaluate( item, XPathConstants.STRING );
    }
    static DocumentBuilderFactory dbf = null;
    static DocumentBuilder db = null;
    static Document d = null;
    public Object evaluate(InputSource source, QName returnType)
        throws XPathExpressionException {
        if ( ( source == null ) || ( returnType == null ) ) {
            String fmsg = XSLMessages.createXPATHMessage( 
                    XPATHErrorResources.ER_SOURCE_RETURN_TYPE_CANNOT_BE_NULL,
                    null );
            throw new NullPointerException ( fmsg );
        }
        if ( !isSupported ( returnType ) ) {
            String fmsg = XSLMessages.createXPATHMessage( 
                    XPATHErrorResources.ER_UNSUPPORTED_RETURN_TYPE,
                    new Object[] { returnType.toString() } );
            throw new IllegalArgumentException ( fmsg );
        }
        try {
            if ( dbf == null ) {
                dbf = DocumentBuilderFactory.newInstance();
                dbf.setNamespaceAware( true );
                dbf.setValidating( false );
            }
            db = dbf.newDocumentBuilder();
            Document document = db.parse( source );
            return eval(  document, returnType );
        } catch ( Exception e ) {
            throw new XPathExpressionException ( e );
        }
    }
    public String evaluate(InputSource source)
        throws XPathExpressionException {
        return (String)this.evaluate( source, XPathConstants.STRING );
    }
    private boolean isSupported( QName returnType ) {
        if ( ( returnType.equals( XPathConstants.STRING ) ) ||
             ( returnType.equals( XPathConstants.NUMBER ) ) ||
             ( returnType.equals( XPathConstants.BOOLEAN ) ) ||
             ( returnType.equals( XPathConstants.NODE ) ) ||
             ( returnType.equals( XPathConstants.NODESET ) )  ) {
            return true;
        }
        return false;
     }
     private Object getResultAsType( XObject resultObject, QName returnType )
        throws javax.xml.transform.TransformerException {
        if ( returnType.equals( XPathConstants.STRING ) ) {
            return resultObject.str();
        }
        if ( returnType.equals( XPathConstants.NUMBER ) ) {
            return new Double ( resultObject.num());
        }
        if ( returnType.equals( XPathConstants.BOOLEAN ) ) {
            return new Boolean( resultObject.bool());
        }
        if ( returnType.equals( XPathConstants.NODESET ) ) {
            return resultObject.nodelist();
        }
        if ( returnType.equals( XPathConstants.NODE ) ) {
            NodeIterator ni = resultObject.nodeset();
            return ni.nextNode();
        }
        String fmsg = XSLMessages.createXPATHMessage( 
                XPATHErrorResources.ER_UNSUPPORTED_RETURN_TYPE,
                new Object[] { returnType.toString()});
        throw new IllegalArgumentException ( fmsg );
    }
    private static Document getDummyDocument( ) {
        try {
            if ( dbf == null ) {
                dbf = DocumentBuilderFactory.newInstance();
                dbf.setNamespaceAware( true );
                dbf.setValidating( false );
            }
            db = dbf.newDocumentBuilder();
            DOMImplementation dim = db.getDOMImplementation();
            d = dim.createDocument("http:
                "dummyroot", null);
            return d;
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return null;
    }
}
