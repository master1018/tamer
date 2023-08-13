public class SdkRepository {
    public static final String URL_GOOGLE_SDK_REPO_SITE =
        "https:
    public static final String URL_DEFAULT_XML_FILE = "repository.xml";         
    private static final String NS_SDK_REPOSITORY_BASE =
        "http:
    public static final String NS_SDK_REPOSITORY_PATTERN =
        NS_SDK_REPOSITORY_BASE + "[1-9][0-9]*";        
    public static final int NS_LATEST_VERSION = 2;
    public static final String NS_SDK_REPOSITORY = getSchemaUri(NS_LATEST_VERSION);
    public static final String NODE_SDK_REPOSITORY = "sdk-repository";          
    public static final String NODE_PLATFORM = "platform";                      
    public static final String NODE_ADD_ON   = "add-on";                        
    public static final String NODE_TOOL     = "tool";                          
    public static final String NODE_DOC      = "doc";                           
    public static final String NODE_SAMPLE   = "sample";                        
    public static final String NODE_EXTRA    = "extra";                         
    public static final String NODE_LICENSE       = "license";                  
    public static final String NODE_USES_LICENSE  = "uses-license";             
    public static final String NODE_REVISION      = "revision";                 
    public static final String NODE_DESCRIPTION   = "description";              
    public static final String NODE_DESC_URL      = "desc-url";                 
    public static final String NODE_RELEASE_NOTE  = "release-note";             
    public static final String NODE_RELEASE_URL   = "release-url";              
    public static final String NODE_OBSOLETE      = "obsolete";                 
    public static final String NODE_MIN_TOOLS_REV = "min-tools-rev";            
    public static final String NODE_MIN_API_LEVEL = "min-api-level";            
    public static final String NODE_VERSION   = "version";                      
    public static final String NODE_API_LEVEL = "api-level";                    
    public static final String NODE_CODENAME = "codename";                      
    public static final String NODE_VENDOR    = "vendor";                       
    public static final String NODE_NAME      = "name";                         
    public static final String NODE_LIBS      = "libs";                         
    public static final String NODE_LIB       = "lib";                          
    public static final String NODE_PATH = "path";                              
    public static final String NODE_ARCHIVES = "archives";                      
    public static final String NODE_ARCHIVE  = "archive";                       
    public static final String NODE_SIZE     = "size";                          
    public static final String NODE_CHECKSUM = "checksum";                      
    public static final String NODE_URL      = "url";                           
    public static final String ATTR_TYPE = "type";                              
    public static final String ATTR_OS   = "os";                                
    public static final String ATTR_ARCH = "arch";                              
    public static final String ATTR_ID = "id";                                  
    public static final String ATTR_REF = "ref";                                
    public static final String SHA1_TYPE = "sha1";                              
    public static final int SHA1_CHECKSUM_LEN = 40;
    public static InputStream getXsdStream(int version) {
        String filename = String.format("sdk-repository-%d.xsd", version);      
        return SdkRepository.class.getResourceAsStream(filename);
    }
    public static String getSchemaUri(int version) {
        return String.format(NS_SDK_REPOSITORY_BASE + "%d", version);           
    }
}
