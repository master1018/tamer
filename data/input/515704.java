final public class LSSerializerImpl implements DOMConfiguration, LSSerializer {
    private static final String DEFAULT_END_OF_LINE;
    static {
        String lineSeparator = (String) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                try {
                    return System.getProperty("line.separator");
                }
                catch (SecurityException ex) {}
                return null;
            }
        });
        DEFAULT_END_OF_LINE = lineSeparator != null && 
            (lineSeparator.equals("\r\n") || lineSeparator.equals("\r")) ? lineSeparator : "\n";
    }
    private Serializer fXMLSerializer = null;
    protected int fFeatures = 0;
    private  DOM3Serializer fDOMSerializer = null;
    private LSSerializerFilter fSerializerFilter = null;  
    private Node fVisitedNode = null;
    private String fEndOfLine = DEFAULT_END_OF_LINE;
    private DOMErrorHandler fDOMErrorHandler = null;
    private Properties fDOMConfigProperties = null;
    private String fEncoding; 
    private final static int CANONICAL = 0x1 << 0;
    private final static int CDATA = 0x1 << 1;
    private final static int CHARNORMALIZE = 0x1 << 2;
    private final static int COMMENTS = 0x1 << 3;
    private final static int DTNORMALIZE = 0x1 << 4;    
    private final static int ELEM_CONTENT_WHITESPACE = 0x1 << 5;
    private final static int ENTITIES = 0x1 << 6;
    private final static int INFOSET = 0x1 << 7;
    private final static int NAMESPACES = 0x1 << 8;
    private final static int NAMESPACEDECLS = 0x1 << 9;
    private final static int NORMALIZECHARS = 0x1 << 10;
    private final static int SPLITCDATA = 0x1 << 11;   
    private final static int VALIDATE = 0x1 << 12;   
    private final static int SCHEMAVALIDATE = 0x1 << 13;
    private final static int WELLFORMED = 0x1 << 14;   
    private final static int DISCARDDEFAULT = 0x1 << 15;       
    private final static int PRETTY_PRINT = 0x1 << 16;
    private final static int IGNORE_CHAR_DENORMALIZE = 0x1 << 17;
    private final static int XMLDECL = 0x1 << 18;    
    private String fRecognizedParameters [] = {
            DOMConstants.DOM_CANONICAL_FORM,
            DOMConstants.DOM_CDATA_SECTIONS,
            DOMConstants.DOM_CHECK_CHAR_NORMALIZATION,
            DOMConstants.DOM_COMMENTS,
            DOMConstants.DOM_DATATYPE_NORMALIZATION,
            DOMConstants.DOM_ELEMENT_CONTENT_WHITESPACE,
            DOMConstants.DOM_ENTITIES,
            DOMConstants.DOM_INFOSET,
            DOMConstants.DOM_NAMESPACES,
            DOMConstants.DOM_NAMESPACE_DECLARATIONS,
            DOMConstants.DOM_SPLIT_CDATA,
            DOMConstants.DOM_VALIDATE,
            DOMConstants.DOM_VALIDATE_IF_SCHEMA,
            DOMConstants.DOM_WELLFORMED,
            DOMConstants.DOM_DISCARD_DEFAULT_CONTENT,
            DOMConstants.DOM_FORMAT_PRETTY_PRINT,
            DOMConstants.DOM_IGNORE_UNKNOWN_CHARACTER_DENORMALIZATIONS,
            DOMConstants.DOM_XMLDECL,
            DOMConstants.DOM_ERROR_HANDLER
    };
    public LSSerializerImpl () {
        fFeatures |= CDATA;
        fFeatures |= COMMENTS;
        fFeatures |= ELEM_CONTENT_WHITESPACE;
        fFeatures |= ENTITIES;
        fFeatures |= NAMESPACES;
        fFeatures |= NAMESPACEDECLS;
        fFeatures |= SPLITCDATA;
        fFeatures |= WELLFORMED;
        fFeatures |= DISCARDDEFAULT;
        fFeatures |= XMLDECL;
        fDOMConfigProperties = new Properties();
        initializeSerializerProps();
        Properties  configProps = OutputPropertiesFactory.getDefaultMethodProperties("xml");
        fXMLSerializer = SerializerFactory.getSerializer(configProps);
        fXMLSerializer.setOutputFormat(fDOMConfigProperties);
    }
    public void initializeSerializerProps () {
        fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS
                + DOMConstants.DOM_CANONICAL_FORM, DOMConstants.DOM3_DEFAULT_FALSE);
        fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS
                + DOMConstants.DOM_CDATA_SECTIONS, DOMConstants.DOM3_DEFAULT_TRUE);
        fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS
                + DOMConstants.DOM_CHECK_CHAR_NORMALIZATION,
                DOMConstants.DOM3_DEFAULT_FALSE);
        fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS
                + DOMConstants.DOM_COMMENTS, DOMConstants.DOM3_DEFAULT_TRUE);
        fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS
                + DOMConstants.DOM_DATATYPE_NORMALIZATION,
                DOMConstants.DOM3_DEFAULT_FALSE);
        fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS
                + DOMConstants.DOM_ELEMENT_CONTENT_WHITESPACE,
                DOMConstants.DOM3_DEFAULT_TRUE);
        fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS
                + DOMConstants.DOM_ENTITIES, DOMConstants.DOM3_DEFAULT_TRUE);
        fDOMConfigProperties.setProperty(DOMConstants.S_XERCES_PROPERTIES_NS
                + DOMConstants.DOM_ENTITIES, DOMConstants.DOM3_DEFAULT_TRUE);
        if ((fFeatures & INFOSET) != 0) {
            fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS
                    + DOMConstants.DOM_NAMESPACES, DOMConstants.DOM3_DEFAULT_TRUE);
            fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS
                    + DOMConstants.DOM_NAMESPACE_DECLARATIONS,
                    DOMConstants.DOM3_DEFAULT_TRUE);
            fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS
                    + DOMConstants.DOM_COMMENTS, DOMConstants.DOM3_DEFAULT_TRUE);
            fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS
                    + DOMConstants.DOM_ELEMENT_CONTENT_WHITESPACE,
                    DOMConstants.DOM3_DEFAULT_TRUE);
            fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS
                    + DOMConstants.DOM_WELLFORMED, DOMConstants.DOM3_DEFAULT_TRUE);
            fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS
                    + DOMConstants.DOM_ENTITIES, DOMConstants.DOM3_DEFAULT_FALSE);
            fDOMConfigProperties.setProperty(DOMConstants.S_XERCES_PROPERTIES_NS
                    + DOMConstants.DOM_ENTITIES, DOMConstants.DOM3_DEFAULT_FALSE);
            fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS
                    + DOMConstants.DOM_CDATA_SECTIONS,
                    DOMConstants.DOM3_DEFAULT_FALSE);
            fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS
                    + DOMConstants.DOM_VALIDATE_IF_SCHEMA,
                    DOMConstants.DOM3_DEFAULT_FALSE);
            fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS
                    + DOMConstants.DOM_DATATYPE_NORMALIZATION,
                    DOMConstants.DOM3_DEFAULT_FALSE);
        }
        fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS
                + DOMConstants.DOM_NAMESPACES, DOMConstants.DOM3_DEFAULT_TRUE);
        fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS
                + DOMConstants.DOM_NAMESPACE_DECLARATIONS,
                DOMConstants.DOM3_DEFAULT_TRUE);
        fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS
                + DOMConstants.DOM_SPLIT_CDATA, DOMConstants.DOM3_DEFAULT_TRUE);
        fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS
                + DOMConstants.DOM_VALIDATE, DOMConstants.DOM3_DEFAULT_FALSE);
        fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS
                + DOMConstants.DOM_VALIDATE_IF_SCHEMA,
                DOMConstants.DOM3_DEFAULT_FALSE);
        fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS
                + DOMConstants.DOM_WELLFORMED, DOMConstants.DOM3_DEFAULT_TRUE);
        fDOMConfigProperties.setProperty(
                DOMConstants.S_XSL_OUTPUT_INDENT,
                DOMConstants.DOM3_DEFAULT_TRUE);
        fDOMConfigProperties.setProperty(
                OutputPropertiesFactory.S_KEY_INDENT_AMOUNT, Integer.toString(3));
        fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS
                + DOMConstants.DOM_DISCARD_DEFAULT_CONTENT,
                DOMConstants.DOM3_DEFAULT_TRUE);
        fDOMConfigProperties.setProperty(DOMConstants.S_XSL_OUTPUT_OMIT_XML_DECL, "no");
    }    
    public boolean canSetParameter(String name, Object value) {
        if (value instanceof Boolean){
            if ( name.equalsIgnoreCase(DOMConstants.DOM_CDATA_SECTIONS)
                    || name.equalsIgnoreCase(DOMConstants.DOM_COMMENTS)
                    || name.equalsIgnoreCase(DOMConstants.DOM_ENTITIES)
                    || name.equalsIgnoreCase(DOMConstants.DOM_INFOSET)
                    || name.equalsIgnoreCase(DOMConstants.DOM_ELEMENT_CONTENT_WHITESPACE)
                    || name.equalsIgnoreCase(DOMConstants.DOM_NAMESPACES)    
                    || name.equalsIgnoreCase(DOMConstants.DOM_NAMESPACE_DECLARATIONS)
                    || name.equalsIgnoreCase(DOMConstants.DOM_SPLIT_CDATA)
                    || name.equalsIgnoreCase(DOMConstants.DOM_WELLFORMED)    
                    || name.equalsIgnoreCase(DOMConstants.DOM_DISCARD_DEFAULT_CONTENT)
                    || name.equalsIgnoreCase(DOMConstants.DOM_FORMAT_PRETTY_PRINT)                
                    || name.equalsIgnoreCase(DOMConstants.DOM_XMLDECL)){
                return true;
            }
            else if (name.equalsIgnoreCase(DOMConstants.DOM_CANONICAL_FORM)
                    || name.equalsIgnoreCase(DOMConstants.DOM_CHECK_CHAR_NORMALIZATION)
                    || name.equalsIgnoreCase(DOMConstants.DOM_DATATYPE_NORMALIZATION)
                    || name.equalsIgnoreCase(DOMConstants.DOM_VALIDATE_IF_SCHEMA)
                    || name.equalsIgnoreCase(DOMConstants.DOM_VALIDATE)
                    ) {
                return !((Boolean)value).booleanValue();
            }
            else if (name.equalsIgnoreCase(DOMConstants.DOM_IGNORE_UNKNOWN_CHARACTER_DENORMALIZATIONS)) {
                return ((Boolean)value).booleanValue();
            }
        }
        else if (name.equalsIgnoreCase(DOMConstants.DOM_ERROR_HANDLER) &&
                value == null || value instanceof DOMErrorHandler){
            return true;
        }
        return false;
    }
    public Object getParameter(String name) throws DOMException {
        if (name.equalsIgnoreCase(DOMConstants.DOM_COMMENTS)) {
            return ((fFeatures & COMMENTS) != 0) ? Boolean.TRUE : Boolean.FALSE;
        } else if (name.equalsIgnoreCase(DOMConstants.DOM_CDATA_SECTIONS)) {
            return ((fFeatures & CDATA) != 0) ? Boolean.TRUE : Boolean.FALSE;
        } else if (name.equalsIgnoreCase(DOMConstants.DOM_ENTITIES)) {
            return ((fFeatures & ENTITIES) != 0) ? Boolean.TRUE : Boolean.FALSE;
        } else if (name.equalsIgnoreCase(DOMConstants.DOM_NAMESPACES)) {
            return ((fFeatures & NAMESPACES) != 0) ? Boolean.TRUE : Boolean.FALSE;
        } else if (name.equalsIgnoreCase(DOMConstants.DOM_NAMESPACE_DECLARATIONS)) {
            return ((fFeatures & NAMESPACEDECLS) != 0) ? Boolean.TRUE : Boolean.FALSE;
        } else if (name.equalsIgnoreCase(DOMConstants.DOM_SPLIT_CDATA)) {
            return ((fFeatures & SPLITCDATA) != 0) ? Boolean.TRUE : Boolean.FALSE;
        } else if (name.equalsIgnoreCase(DOMConstants.DOM_WELLFORMED)) {
            return ((fFeatures & WELLFORMED) != 0) ? Boolean.TRUE : Boolean.FALSE;
        }  else if (name.equalsIgnoreCase(DOMConstants.DOM_DISCARD_DEFAULT_CONTENT)) {
            return ((fFeatures & DISCARDDEFAULT) != 0) ? Boolean.TRUE : Boolean.FALSE;
        } else if (name.equalsIgnoreCase(DOMConstants.DOM_FORMAT_PRETTY_PRINT)) {
            return ((fFeatures & PRETTY_PRINT) != 0) ? Boolean.TRUE : Boolean.FALSE;
        } else if (name.equalsIgnoreCase(DOMConstants.DOM_XMLDECL)) {
            return ((fFeatures & XMLDECL) != 0) ? Boolean.TRUE : Boolean.FALSE;
        } else if (name.equalsIgnoreCase(DOMConstants.DOM_ELEMENT_CONTENT_WHITESPACE)) {
            return ((fFeatures & ELEM_CONTENT_WHITESPACE) != 0) ? Boolean.TRUE : Boolean.FALSE;
        } else if (name.equalsIgnoreCase(DOMConstants.DOM_FORMAT_PRETTY_PRINT)) {
            return ((fFeatures & PRETTY_PRINT) != 0) ? Boolean.TRUE : Boolean.FALSE;
        } else if (name.equalsIgnoreCase(DOMConstants.DOM_IGNORE_UNKNOWN_CHARACTER_DENORMALIZATIONS)) {
            return Boolean.TRUE;
        } else if (name.equalsIgnoreCase(DOMConstants.DOM_CANONICAL_FORM)
                || name.equalsIgnoreCase(DOMConstants.DOM_CHECK_CHAR_NORMALIZATION)
                || name.equalsIgnoreCase(DOMConstants.DOM_DATATYPE_NORMALIZATION) 
                || name.equalsIgnoreCase(DOMConstants.DOM_VALIDATE)
                || name.equalsIgnoreCase(DOMConstants.DOM_VALIDATE_IF_SCHEMA)) {
            return Boolean.FALSE;
        } else if (name.equalsIgnoreCase(DOMConstants.DOM_INFOSET)){
            if ((fFeatures & ENTITIES) == 0 &&
                    (fFeatures & CDATA) == 0 &&
                    (fFeatures & ELEM_CONTENT_WHITESPACE) != 0 &&
                    (fFeatures & NAMESPACES) != 0 &&
                    (fFeatures & NAMESPACEDECLS) != 0 &&
                    (fFeatures & WELLFORMED) != 0 &&
                    (fFeatures & COMMENTS) != 0) {
                return Boolean.TRUE;
            }                 
            return Boolean.FALSE;
        } else if (name.equalsIgnoreCase(DOMConstants.DOM_ERROR_HANDLER)) {
            return fDOMErrorHandler;
        } else if (
                name.equalsIgnoreCase(DOMConstants.DOM_SCHEMA_LOCATION)
                || name.equalsIgnoreCase(DOMConstants.DOM_SCHEMA_TYPE)) {
            return null;
        } else {
            String msg = Utils.messages.createMessage(
                    MsgKey.ER_FEATURE_NOT_FOUND,
                    new Object[] { name });
            throw new DOMException(DOMException.NOT_FOUND_ERR, msg);
        }
    }
    public DOMStringList getParameterNames() {
        return new DOMStringListImpl(fRecognizedParameters);
    }
    public void setParameter(String name, Object value) throws DOMException {
        if (value instanceof Boolean) {
            boolean state = ((Boolean) value).booleanValue();
            if (name.equalsIgnoreCase(DOMConstants.DOM_COMMENTS)) {
                fFeatures = state ? fFeatures | COMMENTS : fFeatures
                        & ~COMMENTS;
                if (state) {
                    fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS 
                            + DOMConstants.DOM_COMMENTS, DOMConstants.DOM3_EXPLICIT_TRUE);
                } else {
                    fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS 
                            + DOMConstants.DOM_COMMENTS, DOMConstants.DOM3_EXPLICIT_FALSE);
                }                
            } else if (name.equalsIgnoreCase(DOMConstants.DOM_CDATA_SECTIONS)) {
                fFeatures =  state ? fFeatures | CDATA : fFeatures
                        & ~CDATA;
                if (state) {
                    fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS 
                            + DOMConstants.DOM_CDATA_SECTIONS, DOMConstants.DOM3_EXPLICIT_TRUE);
                } else {
                    fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS 
                            + DOMConstants.DOM_CDATA_SECTIONS, DOMConstants.DOM3_EXPLICIT_FALSE);
                }
            } else if (name.equalsIgnoreCase(DOMConstants.DOM_ENTITIES)) {
                fFeatures = state ? fFeatures | ENTITIES : fFeatures
                        & ~ENTITIES;
                if (state) {
                    fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS 
                            + DOMConstants.DOM_ENTITIES, DOMConstants.DOM3_EXPLICIT_TRUE);
                    fDOMConfigProperties.setProperty(
                            DOMConstants.S_XERCES_PROPERTIES_NS
                            + DOMConstants.DOM_ENTITIES, DOMConstants.DOM3_EXPLICIT_TRUE);
                } else {
                    fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS 
                            + DOMConstants.DOM_ENTITIES, DOMConstants.DOM3_EXPLICIT_FALSE);
                    fDOMConfigProperties.setProperty(
                            DOMConstants.S_XERCES_PROPERTIES_NS
                            + DOMConstants.DOM_ENTITIES, DOMConstants.DOM3_EXPLICIT_FALSE);
                }
            } else if (name.equalsIgnoreCase(DOMConstants.DOM_NAMESPACES)) {
                fFeatures = state ? fFeatures | NAMESPACES : fFeatures
                        & ~NAMESPACES;
                if (state) {
                    fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS 
                            + DOMConstants.DOM_NAMESPACES, DOMConstants.DOM3_EXPLICIT_TRUE);
                } else {
                    fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS 
                            + DOMConstants.DOM_NAMESPACES, DOMConstants.DOM3_EXPLICIT_FALSE); 
                }       
            } else if (name
                    .equalsIgnoreCase(DOMConstants.DOM_NAMESPACE_DECLARATIONS)) {
                fFeatures = state ? fFeatures | NAMESPACEDECLS
                        : fFeatures & ~NAMESPACEDECLS;
                if (state) {
                    fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS 
                            + DOMConstants.DOM_NAMESPACE_DECLARATIONS, DOMConstants.DOM3_EXPLICIT_TRUE);
                } else {
                    fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS 
                            + DOMConstants.DOM_NAMESPACE_DECLARATIONS, DOMConstants.DOM3_EXPLICIT_FALSE); 
                } 
            } else if (name.equalsIgnoreCase(DOMConstants.DOM_SPLIT_CDATA)) {
                fFeatures = state ? fFeatures | SPLITCDATA : fFeatures
                        & ~SPLITCDATA;
                if (state) {
                    fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS 
                            + DOMConstants.DOM_SPLIT_CDATA, DOMConstants.DOM3_EXPLICIT_TRUE);
                } else {
                    fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS 
                            + DOMConstants.DOM_SPLIT_CDATA, DOMConstants.DOM3_EXPLICIT_FALSE); 
                }  
            } else if (name.equalsIgnoreCase(DOMConstants.DOM_WELLFORMED)) {
                fFeatures = state ? fFeatures | WELLFORMED : fFeatures
                        & ~WELLFORMED;
                if (state) {
                    fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS 
                            + DOMConstants.DOM_WELLFORMED, DOMConstants.DOM3_EXPLICIT_TRUE);
                } else {
                    fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS 
                            + DOMConstants.DOM_WELLFORMED, DOMConstants.DOM3_EXPLICIT_FALSE); 
                }                  
            } else if (name
                    .equalsIgnoreCase(DOMConstants.DOM_DISCARD_DEFAULT_CONTENT)) {
                fFeatures = state ? fFeatures | DISCARDDEFAULT
                        : fFeatures & ~DISCARDDEFAULT;
                if (state) {
                    fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS 
                            + DOMConstants.DOM_DISCARD_DEFAULT_CONTENT, DOMConstants.DOM3_EXPLICIT_TRUE);
                } else {
                    fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS 
                            + DOMConstants.DOM_DISCARD_DEFAULT_CONTENT, DOMConstants.DOM3_EXPLICIT_FALSE); 
                }                    
            } else if (name.equalsIgnoreCase(DOMConstants.DOM_FORMAT_PRETTY_PRINT)) {
                fFeatures = state ? fFeatures | PRETTY_PRINT : fFeatures
                        & ~PRETTY_PRINT;
                if (state) {
                    fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS 
                            + DOMConstants.DOM_FORMAT_PRETTY_PRINT, DOMConstants.DOM3_EXPLICIT_TRUE);
                }
                else {
                    fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS 
                            + DOMConstants.DOM_FORMAT_PRETTY_PRINT, DOMConstants.DOM3_EXPLICIT_FALSE);
                }
            } else if (name.equalsIgnoreCase(DOMConstants.DOM_XMLDECL)) {
                fFeatures = state ? fFeatures | XMLDECL : fFeatures
                        & ~XMLDECL;
                if (state) {
                    fDOMConfigProperties.setProperty(DOMConstants.S_XSL_OUTPUT_OMIT_XML_DECL, "no");
                } else {
                    fDOMConfigProperties.setProperty(DOMConstants.S_XSL_OUTPUT_OMIT_XML_DECL, "yes"); 
                }       
            } else if (name.equalsIgnoreCase(DOMConstants.DOM_ELEMENT_CONTENT_WHITESPACE)) {
                fFeatures = state ? fFeatures | ELEM_CONTENT_WHITESPACE : fFeatures
                        & ~ELEM_CONTENT_WHITESPACE;
                if (state) {
                    fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS 
                            + DOMConstants.DOM_ELEMENT_CONTENT_WHITESPACE, DOMConstants.DOM3_EXPLICIT_TRUE);
                } else {
                    fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS 
                            + DOMConstants.DOM_ELEMENT_CONTENT_WHITESPACE, DOMConstants.DOM3_EXPLICIT_FALSE);
                }            
            } else if (name.equalsIgnoreCase(DOMConstants.DOM_IGNORE_UNKNOWN_CHARACTER_DENORMALIZATIONS)) {
                if (!state) {
                    String msg = Utils.messages.createMessage(
                            MsgKey.ER_FEATURE_NOT_SUPPORTED,
                            new Object[] { name });
                    throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
                } else {
                    fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS 
                            + DOMConstants.DOM_IGNORE_UNKNOWN_CHARACTER_DENORMALIZATIONS, DOMConstants.DOM3_EXPLICIT_TRUE);
                }
            } else if (name.equalsIgnoreCase(DOMConstants.DOM_CANONICAL_FORM)
                    || name.equalsIgnoreCase(DOMConstants.DOM_VALIDATE_IF_SCHEMA)
                    || name.equalsIgnoreCase(DOMConstants.DOM_VALIDATE)
                    || name.equalsIgnoreCase(DOMConstants.DOM_CHECK_CHAR_NORMALIZATION)
                    || name.equalsIgnoreCase(DOMConstants.DOM_DATATYPE_NORMALIZATION)
                    ) {
                if (state) {
                    String msg = Utils.messages.createMessage(
                            MsgKey.ER_FEATURE_NOT_SUPPORTED,
                            new Object[] { name });
                    throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
                } else {
                    if (name.equalsIgnoreCase(DOMConstants.DOM_CANONICAL_FORM)) {
                        fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS 
                                + DOMConstants.DOM_CANONICAL_FORM, DOMConstants.DOM3_EXPLICIT_FALSE);
                    } else if (name.equalsIgnoreCase(DOMConstants.DOM_VALIDATE_IF_SCHEMA)) {
                        fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS 
                                + DOMConstants.DOM_VALIDATE_IF_SCHEMA, DOMConstants.DOM3_EXPLICIT_FALSE);
                    } else if (name.equalsIgnoreCase(DOMConstants.DOM_VALIDATE)) {
                        fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS 
                                + DOMConstants.DOM_VALIDATE, DOMConstants.DOM3_EXPLICIT_FALSE);
                    } else if (name.equalsIgnoreCase(DOMConstants.DOM_VALIDATE_IF_SCHEMA)) {
                        fDOMConfigProperties.setProperty(DOMConstants.DOM_CHECK_CHAR_NORMALIZATION 
                                + DOMConstants.DOM_CHECK_CHAR_NORMALIZATION, DOMConstants.DOM3_EXPLICIT_FALSE);
                    } else if (name.equalsIgnoreCase(DOMConstants.DOM_DATATYPE_NORMALIZATION)) {
                        fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS 
                                + DOMConstants.DOM_DATATYPE_NORMALIZATION, DOMConstants.DOM3_EXPLICIT_FALSE);
                    } 
                }
            } else if (name.equalsIgnoreCase(DOMConstants.DOM_INFOSET)) {
                if (state) {
                    fFeatures &= ~ENTITIES;
                    fFeatures &= ~CDATA;
                    fFeatures &= ~SCHEMAVALIDATE;
                    fFeatures &= ~DTNORMALIZE;
                    fFeatures |= NAMESPACES;
                    fFeatures |= NAMESPACEDECLS;
                    fFeatures |= WELLFORMED;
                    fFeatures |= ELEM_CONTENT_WHITESPACE;
                    fFeatures |= COMMENTS;
                    fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS 
                            + DOMConstants.DOM_NAMESPACES, DOMConstants.DOM3_EXPLICIT_TRUE); 
                    fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS 
                            + DOMConstants.DOM_NAMESPACE_DECLARATIONS, DOMConstants.DOM3_EXPLICIT_TRUE);
                    fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS 
                            + DOMConstants.DOM_COMMENTS, DOMConstants.DOM3_EXPLICIT_TRUE);
                    fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS 
                            + DOMConstants.DOM_ELEMENT_CONTENT_WHITESPACE, DOMConstants.DOM3_EXPLICIT_TRUE);
                    fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS 
                            + DOMConstants.DOM_WELLFORMED, DOMConstants.DOM3_EXPLICIT_TRUE);
                    fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS 
                            + DOMConstants.DOM_ENTITIES, DOMConstants.DOM3_EXPLICIT_FALSE);
                    fDOMConfigProperties.setProperty(DOMConstants.S_XERCES_PROPERTIES_NS
                            + DOMConstants.DOM_ENTITIES, DOMConstants.DOM3_EXPLICIT_FALSE);
                    fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS 
                            + DOMConstants.DOM_CDATA_SECTIONS, DOMConstants.DOM3_EXPLICIT_FALSE);
                    fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS 
                            + DOMConstants.DOM_VALIDATE_IF_SCHEMA, DOMConstants.DOM3_EXPLICIT_FALSE);            
                    fDOMConfigProperties.setProperty(DOMConstants.S_DOM3_PROPERTIES_NS 
                            + DOMConstants.DOM_DATATYPE_NORMALIZATION, DOMConstants.DOM3_EXPLICIT_FALSE);
                }
            } else {
                if (name.equalsIgnoreCase(DOMConstants.DOM_ERROR_HANDLER) ||
                    name.equalsIgnoreCase(DOMConstants.DOM_SCHEMA_LOCATION) ||
                    name.equalsIgnoreCase(DOMConstants.DOM_SCHEMA_TYPE)) {
                    String msg = Utils.messages.createMessage(
                            MsgKey.ER_TYPE_MISMATCH_ERR,
                            new Object[] { name });
                    throw new DOMException(DOMException.TYPE_MISMATCH_ERR, msg);
                }
                String msg = Utils.messages.createMessage(
                        MsgKey.ER_FEATURE_NOT_FOUND,
                        new Object[] { name });
                throw new DOMException(DOMException.NOT_FOUND_ERR, msg);
            }
        } 
        else if (name.equalsIgnoreCase(DOMConstants.DOM_ERROR_HANDLER)) {
            if (value == null || value instanceof DOMErrorHandler) {
                fDOMErrorHandler = (DOMErrorHandler)value;
            } else {
                String msg = Utils.messages.createMessage(
                        MsgKey.ER_TYPE_MISMATCH_ERR,
                        new Object[] { name });
                throw new DOMException(DOMException.TYPE_MISMATCH_ERR, msg);
            }
        } else if (
                name.equalsIgnoreCase(DOMConstants.DOM_SCHEMA_LOCATION)
                || name.equalsIgnoreCase(DOMConstants.DOM_SCHEMA_TYPE)) {
            if (value != null) {
                if (!(value instanceof String)) {
                    String msg = Utils.messages.createMessage(
                            MsgKey.ER_TYPE_MISMATCH_ERR,
                            new Object[] { name });
                    throw new DOMException(DOMException.TYPE_MISMATCH_ERR, msg);
                }
                String msg = Utils.messages.createMessage(
                        MsgKey.ER_FEATURE_NOT_SUPPORTED,
                        new Object[] { name });
                throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
            }
        } else {
            if (name.equalsIgnoreCase(DOMConstants.DOM_COMMENTS) ||
                    name.equalsIgnoreCase(DOMConstants.DOM_CDATA_SECTIONS) ||
                    name.equalsIgnoreCase(DOMConstants.DOM_ENTITIES) ||
                    name.equalsIgnoreCase(DOMConstants.DOM_NAMESPACES) ||
                    name.equalsIgnoreCase(DOMConstants.DOM_NAMESPACE_DECLARATIONS) ||
                    name.equalsIgnoreCase(DOMConstants.DOM_SPLIT_CDATA) ||
                    name.equalsIgnoreCase(DOMConstants.DOM_WELLFORMED) ||
                    name.equalsIgnoreCase(DOMConstants.DOM_DISCARD_DEFAULT_CONTENT) ||
                    name.equalsIgnoreCase(DOMConstants.DOM_FORMAT_PRETTY_PRINT) ||
                    name.equalsIgnoreCase(DOMConstants.DOM_XMLDECL) ||
                    name.equalsIgnoreCase(DOMConstants.DOM_ELEMENT_CONTENT_WHITESPACE) ||
                    name.equalsIgnoreCase(DOMConstants.DOM_IGNORE_UNKNOWN_CHARACTER_DENORMALIZATIONS) ||
                    name.equalsIgnoreCase(DOMConstants.DOM_CANONICAL_FORM) ||
                    name.equalsIgnoreCase(DOMConstants.DOM_VALIDATE_IF_SCHEMA) ||
                    name.equalsIgnoreCase(DOMConstants.DOM_VALIDATE) ||
                    name.equalsIgnoreCase(DOMConstants.DOM_CHECK_CHAR_NORMALIZATION) ||
                    name.equalsIgnoreCase(DOMConstants.DOM_DATATYPE_NORMALIZATION) ||
                    name.equalsIgnoreCase(DOMConstants.DOM_INFOSET)) {
                String msg = Utils.messages.createMessage(
                        MsgKey.ER_TYPE_MISMATCH_ERR,
                        new Object[] { name });
                throw new DOMException(DOMException.TYPE_MISMATCH_ERR, msg);
            }
            String msg = Utils.messages.createMessage(
                    MsgKey.ER_FEATURE_NOT_FOUND,
                    new Object[] { name });
            throw new DOMException(DOMException.NOT_FOUND_ERR, msg);
        }
    }
    public DOMConfiguration getDomConfig() {
        return (DOMConfiguration)this;
    }
    public LSSerializerFilter getFilter() {
        return fSerializerFilter;
    }
    public String getNewLine() {
        return fEndOfLine;
    }
    public void setFilter(LSSerializerFilter filter) {
        fSerializerFilter = filter;
    }
    public void setNewLine(String newLine) {
        fEndOfLine = (newLine != null) ? newLine : DEFAULT_END_OF_LINE;
    }
    public boolean write(Node nodeArg, LSOutput destination) throws LSException {
        if (destination == null) {
            String msg = Utils.messages
            .createMessage(
                    MsgKey.ER_NO_OUTPUT_SPECIFIED,
                    null);
            if (fDOMErrorHandler != null) {
                fDOMErrorHandler.handleError(new DOMErrorImpl(
                        DOMError.SEVERITY_FATAL_ERROR, msg,
                        MsgKey.ER_NO_OUTPUT_SPECIFIED));
            }
            throw new LSException(LSException.SERIALIZE_ERR, msg);
        } 
        if (nodeArg == null ) {
            return false;
        }
        Serializer serializer = fXMLSerializer;
        serializer.reset();
        if ( nodeArg != fVisitedNode) {
            String xmlVersion = getXMLVersion(nodeArg);
            fEncoding = destination.getEncoding();
            if (fEncoding == null ) {
            	fEncoding = getInputEncoding(nodeArg);
            	fEncoding = fEncoding != null ? fEncoding : getXMLEncoding(nodeArg) == null? "UTF-8": getXMLEncoding(nodeArg);
            }
            if (!Encodings.isRecognizedEncoding(fEncoding)) {
                String msg = Utils.messages
                .createMessage(
                        MsgKey.ER_UNSUPPORTED_ENCODING,
                        null);
                if (fDOMErrorHandler != null) {
                    fDOMErrorHandler.handleError(new DOMErrorImpl(
                            DOMError.SEVERITY_FATAL_ERROR, msg,
                            MsgKey.ER_UNSUPPORTED_ENCODING));
                }
                throw new LSException(LSException.SERIALIZE_ERR, msg);            	
            }
            serializer.getOutputFormat().setProperty("version", xmlVersion);
            fDOMConfigProperties.setProperty(DOMConstants.S_XERCES_PROPERTIES_NS + DOMConstants.S_XML_VERSION, xmlVersion);
            fDOMConfigProperties.setProperty(DOMConstants.S_XSL_OUTPUT_ENCODING, fEncoding);
            if ( (nodeArg.getNodeType() != Node.DOCUMENT_NODE
                    || nodeArg.getNodeType() != Node.ELEMENT_NODE
                    || nodeArg.getNodeType() != Node.ENTITY_NODE)
                    && ((fFeatures & XMLDECL) != 0)) {
                fDOMConfigProperties.setProperty(
                        DOMConstants.S_XSL_OUTPUT_OMIT_XML_DECL,
                        DOMConstants.DOM3_DEFAULT_FALSE);
            }
            fVisitedNode = nodeArg;
        } 
        fXMLSerializer.setOutputFormat(fDOMConfigProperties);
        try {
            Writer writer = destination.getCharacterStream();
            if (writer == null ) {
                OutputStream outputStream = destination.getByteStream();
                if ( outputStream == null) {
                    String uri = destination.getSystemId();
                    if (uri == null) {
                        String msg = Utils.messages
                        .createMessage(
                                MsgKey.ER_NO_OUTPUT_SPECIFIED,
                                null);
                        if (fDOMErrorHandler != null) {
                            fDOMErrorHandler.handleError(new DOMErrorImpl(
                                    DOMError.SEVERITY_FATAL_ERROR, msg,
                                    MsgKey.ER_NO_OUTPUT_SPECIFIED));
                        }
                        throw new LSException(LSException.SERIALIZE_ERR, msg);
                    } else {
                        String absoluteURI = SystemIDResolver.getAbsoluteURI(uri);
                        URL url = new URL(absoluteURI);
                        OutputStream urlOutStream = null;
                        String protocol = url.getProtocol();
                        String host = url.getHost();
                        if (protocol.equalsIgnoreCase("file") 
                                && (host == null || host.length() == 0 || host.equals("localhost"))) {
                            urlOutStream = new FileOutputStream(getPathWithoutEscapes(url.getPath()));
                        } else {
                            URLConnection urlCon = url.openConnection();
                            urlCon.setDoInput(false);
                            urlCon.setDoOutput(true);
                            urlCon.setUseCaches(false); 
                            urlCon.setAllowUserInteraction(false);
                            if (urlCon instanceof HttpURLConnection) {
                                HttpURLConnection httpCon = (HttpURLConnection) urlCon;
                                httpCon.setRequestMethod("PUT");
                            }
                            urlOutStream = urlCon.getOutputStream();
                        }
                        serializer.setOutputStream(urlOutStream);
                    }
                } else {
                    serializer.setOutputStream(outputStream);     
                }
            } else {
                serializer.setWriter(writer);
            }
            if (fDOMSerializer == null) {
               fDOMSerializer = (DOM3Serializer)serializer.asDOM3Serializer();
            } 
            if (fDOMErrorHandler != null) {
                fDOMSerializer.setErrorHandler(fDOMErrorHandler);
            }
            if (fSerializerFilter != null) {
                fDOMSerializer.setNodeFilter(fSerializerFilter);
            }
            fDOMSerializer.setNewLine(fEndOfLine.toCharArray());
            fDOMSerializer.serializeDOM3(nodeArg);
        } catch( UnsupportedEncodingException ue) {
            String msg = Utils.messages
            .createMessage(
                    MsgKey.ER_UNSUPPORTED_ENCODING,
                    null);
            if (fDOMErrorHandler != null) {
                fDOMErrorHandler.handleError(new DOMErrorImpl(
                        DOMError.SEVERITY_FATAL_ERROR, msg,
                        MsgKey.ER_UNSUPPORTED_ENCODING, ue));
            }
            throw (LSException) createLSException(LSException.SERIALIZE_ERR, ue).fillInStackTrace();
        } catch (LSException lse) {
            throw lse;
        } catch (RuntimeException e) {
            throw (LSException) createLSException(LSException.SERIALIZE_ERR, e).fillInStackTrace();
        }  catch (Exception e) {
            if (fDOMErrorHandler != null) {
                fDOMErrorHandler.handleError(new DOMErrorImpl(
                        DOMError.SEVERITY_FATAL_ERROR, e.getMessage(),
                        null, e));
            }
            throw (LSException) createLSException(LSException.SERIALIZE_ERR, e).fillInStackTrace();
        }        
        return true;
    }
    public String writeToString(Node nodeArg) throws DOMException, LSException {
        if (nodeArg == null) {
            return null;
        }
        Serializer serializer = fXMLSerializer;
        serializer.reset();
        if (nodeArg != fVisitedNode){
            String xmlVersion = getXMLVersion(nodeArg);
            serializer.getOutputFormat().setProperty("version", xmlVersion);
            fDOMConfigProperties.setProperty(DOMConstants.S_XERCES_PROPERTIES_NS + DOMConstants.S_XML_VERSION, xmlVersion);
            fDOMConfigProperties.setProperty(DOMConstants.S_XSL_OUTPUT_ENCODING, "UTF-16");
            if  ((nodeArg.getNodeType() != Node.DOCUMENT_NODE
                    || nodeArg.getNodeType() != Node.ELEMENT_NODE
                    || nodeArg.getNodeType() != Node.ENTITY_NODE)
                    && ((fFeatures & XMLDECL) != 0)) {
                fDOMConfigProperties.setProperty(
                        DOMConstants.S_XSL_OUTPUT_OMIT_XML_DECL,
                        DOMConstants.DOM3_DEFAULT_FALSE);
            }            
            fVisitedNode = nodeArg;       
        } 
        fXMLSerializer.setOutputFormat(fDOMConfigProperties);
        StringWriter output = new StringWriter();
        try {
            serializer.setWriter(output);
            if (fDOMSerializer == null) {
                fDOMSerializer = (DOM3Serializer)serializer.asDOM3Serializer();
            } 
            if (fDOMErrorHandler != null) {
                fDOMSerializer.setErrorHandler(fDOMErrorHandler);
            }
            if (fSerializerFilter != null) {
                fDOMSerializer.setNodeFilter(fSerializerFilter);
            }
            fDOMSerializer.setNewLine(fEndOfLine.toCharArray());
            fDOMSerializer.serializeDOM3(nodeArg);
        } catch (LSException lse) {
            throw lse;
        } catch (RuntimeException e) {
            throw (LSException) createLSException(LSException.SERIALIZE_ERR, e).fillInStackTrace();
        }  catch (Exception e) {
            if (fDOMErrorHandler != null) {
                fDOMErrorHandler.handleError(new DOMErrorImpl(
                        DOMError.SEVERITY_FATAL_ERROR, e.getMessage(),
                        null, e));
            }
            throw (LSException) createLSException(LSException.SERIALIZE_ERR, e).fillInStackTrace();
        }        
        return output.toString();
    }
    public boolean writeToURI(Node nodeArg, String uri) throws LSException {
        if (nodeArg == null ) {
            return false;
        }
        Serializer serializer = fXMLSerializer;
        serializer.reset();
        if (nodeArg != fVisitedNode) {
            String xmlVersion = getXMLVersion(nodeArg);
            fEncoding = getInputEncoding(nodeArg);
            if (fEncoding == null ) {
            	fEncoding = fEncoding != null ? fEncoding : getXMLEncoding(nodeArg) == null? "UTF-8": getXMLEncoding(nodeArg);
            }
            serializer.getOutputFormat().setProperty("version", xmlVersion);
            fDOMConfigProperties.setProperty(DOMConstants.S_XERCES_PROPERTIES_NS + DOMConstants.S_XML_VERSION, xmlVersion);
            fDOMConfigProperties.setProperty(DOMConstants.S_XSL_OUTPUT_ENCODING, fEncoding);
            if ( (nodeArg.getNodeType() != Node.DOCUMENT_NODE
                    || nodeArg.getNodeType() != Node.ELEMENT_NODE
                    || nodeArg.getNodeType() != Node.ENTITY_NODE)
                    && ((fFeatures & XMLDECL) != 0))  {
                fDOMConfigProperties.setProperty(
                        DOMConstants.S_XSL_OUTPUT_OMIT_XML_DECL,
                        DOMConstants.DOM3_DEFAULT_FALSE);
            }
            fVisitedNode = nodeArg;
        } 
        fXMLSerializer.setOutputFormat(fDOMConfigProperties);
        try {
            if (uri == null) {
                String msg = Utils.messages.createMessage(
                        MsgKey.ER_NO_OUTPUT_SPECIFIED, null);
                if (fDOMErrorHandler != null) {
                    fDOMErrorHandler.handleError(new DOMErrorImpl(
                            DOMError.SEVERITY_FATAL_ERROR, msg,
                            MsgKey.ER_NO_OUTPUT_SPECIFIED));
                }
                throw new LSException(LSException.SERIALIZE_ERR, msg);
            } else {
                String absoluteURI = SystemIDResolver.getAbsoluteURI(uri);
                URL url = new URL(absoluteURI);
                OutputStream urlOutStream = null;
                String protocol = url.getProtocol();
                String host = url.getHost();
                if (protocol.equalsIgnoreCase("file")
                        && (host == null || host.length() == 0 || host
                                .equals("localhost"))) {
                    urlOutStream = new FileOutputStream(getPathWithoutEscapes(url.getPath()));
                } else {
                    URLConnection urlCon = url.openConnection();
                    urlCon.setDoInput(false);
                    urlCon.setDoOutput(true);
                    urlCon.setUseCaches(false);
                    urlCon.setAllowUserInteraction(false);
                    if (urlCon instanceof HttpURLConnection) {
                        HttpURLConnection httpCon = (HttpURLConnection) urlCon;
                        httpCon.setRequestMethod("PUT");
                    }
                    urlOutStream = urlCon.getOutputStream();
                }
                serializer.setOutputStream(urlOutStream);
            }
            if (fDOMSerializer == null) {
                fDOMSerializer = (DOM3Serializer)serializer.asDOM3Serializer();
            } 
            if (fDOMErrorHandler != null) {
                fDOMSerializer.setErrorHandler(fDOMErrorHandler);
            }
            if (fSerializerFilter != null) {
                fDOMSerializer.setNodeFilter(fSerializerFilter);
            }
            fDOMSerializer.setNewLine(fEndOfLine.toCharArray());
            fDOMSerializer.serializeDOM3(nodeArg);
        } catch (LSException lse) {
            throw lse;
        } catch (RuntimeException e) {
            throw (LSException) createLSException(LSException.SERIALIZE_ERR, e).fillInStackTrace();
        }  catch (Exception e) {
            if (fDOMErrorHandler != null) {
                fDOMErrorHandler.handleError(new DOMErrorImpl(
                        DOMError.SEVERITY_FATAL_ERROR, e.getMessage(),
                        null, e));
            }
            throw (LSException) createLSException(LSException.SERIALIZE_ERR, e).fillInStackTrace();
        }        
        return true;
    }
    protected String getXMLVersion(Node nodeArg) {
        Document doc = null;
        if (nodeArg != null) {
            if (nodeArg.getNodeType() == Node.DOCUMENT_NODE) {
                doc = (Document)nodeArg;
            } else { 
                doc = nodeArg.getOwnerDocument();
            }
            if (doc != null && doc.getImplementation().hasFeature("Core","3.0")) {
                return doc.getXmlVersion();
            }
        } 
        return "1.0";
    }
    protected String getXMLEncoding(Node nodeArg) {
        Document doc = null;
        if (nodeArg != null) {
            if (nodeArg.getNodeType() == Node.DOCUMENT_NODE) {
                doc = (Document)nodeArg;
            } else { 
                doc = nodeArg.getOwnerDocument();
            }
            if (doc != null && doc.getImplementation().hasFeature("Core","3.0")) {
                return doc.getXmlEncoding();
            }
        } 
        return "UTF-8";
    }
    protected String getInputEncoding(Node nodeArg)  {
        Document doc = null;
        if (nodeArg != null) {
            if (nodeArg.getNodeType() == Node.DOCUMENT_NODE) {
                doc = (Document)nodeArg;
            } else { 
                doc = nodeArg.getOwnerDocument();
            }
            if (doc != null && doc.getImplementation().hasFeature("Core","3.0")) {
                return doc.getInputEncoding();
            }
        } 
        return null;
    }
    public DOMErrorHandler getErrorHandler() {
        return fDOMErrorHandler;
    }
    private static String getPathWithoutEscapes(String origPath) {
        if (origPath != null && origPath.length() != 0 && origPath.indexOf('%') != -1) {
            StringTokenizer tokenizer = new StringTokenizer(origPath, "%");
            StringBuffer result = new StringBuffer(origPath.length());
            int size = tokenizer.countTokens();
            result.append(tokenizer.nextToken());
            for(int i = 1; i < size; ++i) {
                String token = tokenizer.nextToken();
                if (token.length() >= 2 && isHexDigit(token.charAt(0)) && 
                        isHexDigit(token.charAt(1))) {
                    result.append((char)Integer.valueOf(token.substring(0, 2), 16).intValue());
                    token = token.substring(2);
                }
                result.append(token);
            }
            return result.toString();
        }
        return origPath;
    }
    private static boolean isHexDigit(char c) {
        return (c >= '0' && c <= '9' || 
                c >= 'a' && c <= 'f' || 
                c >= 'A' && c <= 'F');
    }
    private static LSException createLSException(short code, Throwable cause) {
        LSException lse = new LSException(code, cause != null ? cause.getMessage() : null);
        if (cause != null && ThrowableMethods.fgThrowableMethodsAvailable) {
            try {
                ThrowableMethods.fgThrowableInitCauseMethod.invoke(lse, new Object [] {cause});
            }
            catch (Exception e) {}
        }
        return lse;
    }
    static class ThrowableMethods {
        private static java.lang.reflect.Method fgThrowableInitCauseMethod = null;
        private static boolean fgThrowableMethodsAvailable = false;
        private ThrowableMethods() {}
        static {
            try {
                fgThrowableInitCauseMethod = Throwable.class.getMethod("initCause", new Class [] {Throwable.class});
                fgThrowableMethodsAvailable = true;
            }
            catch (Exception exc) {
                fgThrowableInitCauseMethod = null;
                fgThrowableMethodsAvailable = false;
            }
        }
    }
}
