public class PduPart {
    public static final int P_Q                  = 0x80;
    public static final int P_CHARSET            = 0x81;
    public static final int P_LEVEL              = 0x82;
    public static final int P_TYPE               = 0x83;
    public static final int P_DEP_NAME           = 0x85;
    public static final int P_DEP_FILENAME       = 0x86;
    public static final int P_DIFFERENCES        = 0x87;
    public static final int P_PADDING            = 0x88;
    public static final int P_CT_MR_TYPE         = 0x89;
    public static final int P_DEP_START          = 0x8A;
    public static final int P_DEP_START_INFO     = 0x8B;
    public static final int P_DEP_COMMENT        = 0x8C;
    public static final int P_DEP_DOMAIN         = 0x8D;
    public static final int P_MAX_AGE            = 0x8E;
    public static final int P_DEP_PATH           = 0x8F;
    public static final int P_SECURE             = 0x90;
    public static final int P_SEC                = 0x91;
    public static final int P_MAC                = 0x92;
    public static final int P_CREATION_DATE      = 0x93;
    public static final int P_MODIFICATION_DATE  = 0x94;
    public static final int P_READ_DATE          = 0x95;
    public static final int P_SIZE               = 0x96;
    public static final int P_NAME               = 0x97;
    public static final int P_FILENAME           = 0x98;
    public static final int P_START              = 0x99;
    public static final int P_START_INFO         = 0x9A;
    public static final int P_COMMENT            = 0x9B;
    public static final int P_DOMAIN             = 0x9C;
    public static final int P_PATH               = 0x9D;
     public static final int P_CONTENT_TYPE       = 0x91;
     public static final int P_CONTENT_LOCATION   = 0x8E;
     public static final int P_CONTENT_ID         = 0xC0;
     public static final int P_DEP_CONTENT_DISPOSITION = 0xAE;
     public static final int P_CONTENT_DISPOSITION = 0xC5;
     public static final int P_CONTENT_TRANSFER_ENCODING = 0xC8;
     public static final String CONTENT_TRANSFER_ENCODING =
             "Content-Transfer-Encoding";
     public static final String P_BINARY = "binary";
     public static final String P_7BIT = "7bit";
     public static final String P_8BIT = "8bit";
     public static final String P_BASE64 = "base64";
     public static final String P_QUOTED_PRINTABLE = "quoted-printable";
     static final byte[] DISPOSITION_FROM_DATA = "from-data".getBytes();
     static final byte[] DISPOSITION_ATTACHMENT = "attachment".getBytes();
     static final byte[] DISPOSITION_INLINE = "inline".getBytes();
     public static final int P_DISPOSITION_FROM_DATA  = 0x80;
     public static final int P_DISPOSITION_ATTACHMENT = 0x81;
     public static final int P_DISPOSITION_INLINE     = 0x82;
     private Map<Integer, Object> mPartHeader = null;
     private Uri mUri = null;
     private byte[] mPartData = null;
     private static final String TAG = "PduPart";
     public PduPart() {
         mPartHeader = new HashMap<Integer, Object>();
     }
     public void setData(byte[] data) {
         if(data == null) {
            return;
        }
         mPartData = new byte[data.length];
         System.arraycopy(data, 0, mPartData, 0, data.length);
     }
     public byte[] getData() {
         if(mPartData == null) {
            return null;
         }
         byte[] byteArray = new byte[mPartData.length];
         System.arraycopy(mPartData, 0, byteArray, 0, mPartData.length);
         return byteArray;
     }
     public void setDataUri(Uri uri) {
         mUri = uri;
     }
     public Uri getDataUri() {
         return mUri;
     }
     public void setContentId(byte[] contentId) {
         if((contentId == null) || (contentId.length == 0)) {
             throw new IllegalArgumentException(
                     "Content-Id may not be null or empty.");
         }
         if ((contentId.length > 1)
                 && ((char) contentId[0] == '<')
                 && ((char) contentId[contentId.length - 1] == '>')) {
             mPartHeader.put(P_CONTENT_ID, contentId);
             return;
         }
         byte[] buffer = new byte[contentId.length + 2];
         buffer[0] = (byte) (0xff & '<');
         buffer[buffer.length - 1] = (byte) (0xff & '>');
         System.arraycopy(contentId, 0, buffer, 1, contentId.length);
         mPartHeader.put(P_CONTENT_ID, buffer);
     }
     public byte[] getContentId() {
         return (byte[]) mPartHeader.get(P_CONTENT_ID);
     }
     public void setCharset(int charset) {
         mPartHeader.put(P_CHARSET, charset);
     }
     public int getCharset() {
         Integer charset = (Integer) mPartHeader.get(P_CHARSET);
         if(charset == null) {
             return 0;
         } else {
             return charset.intValue();
         }
     }
     public void setContentLocation(byte[] contentLocation) {
         if(contentLocation == null) {
             throw new NullPointerException("null content-location");
         }
         mPartHeader.put(P_CONTENT_LOCATION, contentLocation);
     }
     public byte[] getContentLocation() {
         return (byte[]) mPartHeader.get(P_CONTENT_LOCATION);
     }
     public void setContentDisposition(byte[] contentDisposition) {
         if(contentDisposition == null) {
             throw new NullPointerException("null content-disposition");
         }
         mPartHeader.put(P_CONTENT_DISPOSITION, contentDisposition);
     }
     public byte[] getContentDisposition() {
         return (byte[]) mPartHeader.get(P_CONTENT_DISPOSITION);
     }
     public void setContentType(byte[] contentType) {
         if(contentType == null) {
             throw new NullPointerException("null content-type");
         }
         mPartHeader.put(P_CONTENT_TYPE, contentType);
     }
     public byte[] getContentType() {
         return (byte[]) mPartHeader.get(P_CONTENT_TYPE);
     }
     public void setContentTransferEncoding(byte[] contentTransferEncoding) {
         if(contentTransferEncoding == null) {
             throw new NullPointerException("null content-transfer-encoding");
         }
         mPartHeader.put(P_CONTENT_TRANSFER_ENCODING, contentTransferEncoding);
     }
     public byte[] getContentTransferEncoding() {
         return (byte[]) mPartHeader.get(P_CONTENT_TRANSFER_ENCODING);
     }
     public void setName(byte[] name) {
         if(null == name) {
             throw new NullPointerException("null content-id");
         }
         mPartHeader.put(P_NAME, name);
     }
     public byte[] getName() {
         return (byte[]) mPartHeader.get(P_NAME);
     }
     public void setFilename(byte[] fileName) {
         if(null == fileName) {
             throw new NullPointerException("null content-id");
         }
         mPartHeader.put(P_FILENAME, fileName);
     }
     public byte[] getFilename() {
         return (byte[]) mPartHeader.get(P_FILENAME);
     }
    public String generateLocation() {
        byte[] location = (byte[]) mPartHeader.get(P_NAME);
        if(null == location) {
            location = (byte[]) mPartHeader.get(P_FILENAME);
            if (null == location) {
                location = (byte[]) mPartHeader.get(P_CONTENT_LOCATION);
            }
        }
        if (null == location) {
            byte[] contentId = (byte[]) mPartHeader.get(P_CONTENT_ID);
            return "cid:" + new String(contentId);
        } else {
            return new String(location);
        }
    }
}
