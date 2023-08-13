public class GalParser extends Parser {
    private EasSyncService mService;
    GalResult mGalResult = new GalResult();
    public GalParser(InputStream in, EasSyncService service) throws IOException {
        super(in);
        mService = service;
    }
    public GalResult getGalResult() {
        return mGalResult;
    }
    @Override
    public boolean parse() throws IOException {
        if (nextTag(START_DOCUMENT) != Tags.SEARCH_SEARCH) {
            throw new IOException();
        }
        while (nextTag(START_DOCUMENT) != END_DOCUMENT) {
            if (tag == Tags.SEARCH_RESPONSE) {
                parseResponse(mGalResult);
            } else {
                skipTag();
            }
         }
         return mGalResult.total > 0;
     }
     public void parseProperties(GalResult galResult) throws IOException {
         String displayName = null;
         String email = null;
         while (nextTag(Tags.SEARCH_STORE) != END) {
             if (tag == Tags.GAL_DISPLAY_NAME) {
                 displayName = getValue();
             } else if (tag == Tags.GAL_EMAIL_ADDRESS) {
                 email = getValue();
             } else {
                 skipTag();
             }
         }
         if (displayName != null && email != null) {
             galResult.addGalData(0, displayName, email);
         }
     }
     public void parseResult(GalResult galResult) throws IOException {
         while (nextTag(Tags.SEARCH_STORE) != END) {
             if (tag == Tags.SEARCH_PROPERTIES) {
                 parseProperties(galResult);
             } else {
                 skipTag();
             }
         }
     }
     public void parseResponse(GalResult galResult) throws IOException {
         while (nextTag(Tags.SEARCH_RESPONSE) != END) {
             if (tag == Tags.SEARCH_STORE) {
                 parseStore(galResult);
             } else {
                 skipTag();
             }
         }
     }
     public void parseStore(GalResult galResult) throws IOException {
         while (nextTag(Tags.SEARCH_STORE) != END) {
             if (tag == Tags.SEARCH_RESULT) {
                 parseResult(galResult);
             } else if (tag == Tags.SEARCH_RANGE) {
                 String range = getValue();
                 if (EasSyncService.DEBUG_GAL_SERVICE) {
                     mService.userLog("GAL result range: " + range);
                 }
             } else if (tag == Tags.SEARCH_TOTAL) {
                 galResult.total = getValueInt();
             } else {
                 skipTag();
             }
         }
     }
}
