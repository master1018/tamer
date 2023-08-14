public class LayoutDevicesXsd {
    public static final String NS_LAYOUT_DEVICE_XSD =
        "http:
    public static final String NODE_LAYOUT_DEVICES = "layout-devices";              
    public static final String NODE_DEVICE = "device";                              
    public static final String NODE_DEFAULT = "default";                            
    public static final String NODE_CONFIG = "config";                              
    public static final String NODE_COUNTRY_CODE = "country-code";                  
    public static final String NODE_NETWORK_CODE = "network-code";                  
    public static final String NODE_SCREEN_SIZE = "screen-size";                    
    public static final String NODE_SCREEN_RATIO = "screen-ratio";                  
    public static final String NODE_SCREEN_ORIENTATION = "screen-orientation";      
    public static final String NODE_PIXEL_DENSITY = "pixel-density";                
    public static final String NODE_TOUCH_TYPE = "touch-type";                      
    public static final String NODE_KEYBOARD_STATE = "keyboard-state";              
    public static final String NODE_TEXT_INPUT_METHOD = "text-input-method";        
    public static final String NODE_NAV_METHOD = "nav-method";                      
    public static final String NODE_SCREEN_DIMENSION = "screen-dimension";          
    public static final String NODE_SIZE = "size";                                  
    public static final String NODE_XDPI = "xdpi";                                  
    public static final String NODE_YDPI = "ydpi";                                  
    public static final String ATTR_NAME = "name";                                  
    public static InputStream getXsdStream() {
        return LayoutDevicesXsd.class.getResourceAsStream("layout-devices.xsd");    
    }
    public static Validator getValidator(ErrorHandler handler) throws SAXException {
        InputStream xsdStream = getXsdStream();
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(new StreamSource(xsdStream));
        Validator validator = schema.newValidator();
        if (handler != null) {
            validator.setErrorHandler(handler);
        }
        return validator;
    }
}
