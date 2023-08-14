public class CertException extends SecurityException {
    private static final long serialVersionUID = 6930793039696446142L;
    public static final int verf_INVALID_SIG = 1;
    public static final int verf_INVALID_REVOKED = 2;
    public static final int verf_INVALID_NOTBEFORE = 3;
    public static final int verf_INVALID_EXPIRED = 4;
    public static final int verf_CA_UNTRUSTED = 5;
    public static final int verf_CHAIN_LENGTH = 6;
    public static final int verf_PARSE_ERROR = 7;
    public static final int err_CONSTRUCTION = 8;
    public static final int err_INVALID_PUBLIC_KEY = 9;
    public static final int err_INVALID_VERSION = 10;
    public static final int err_INVALID_FORMAT = 11;
    public static final int err_ENCODING = 12;
    private int         verfCode;
    private String      moreData;
    public CertException(int code, String moredata)
    {
        verfCode = code;
        moreData = moredata;
    }
    public CertException(int code)
    {
        verfCode = code;
    }
    public int getVerfCode() { return verfCode; }
    public String getMoreData() { return moreData; }
    public String getVerfDescription()
    {
        switch (verfCode) {
        case verf_INVALID_SIG:
            return "The signature in the certificate is not valid.";
        case verf_INVALID_REVOKED:
            return "The certificate has been revoked.";
        case verf_INVALID_NOTBEFORE:
            return "The certificate is not yet valid.";
        case verf_INVALID_EXPIRED:
            return "The certificate has expired.";
        case verf_CA_UNTRUSTED:
            return "The Authority which issued the certificate is not trusted.";
        case verf_CHAIN_LENGTH:
            return "The certificate path to a trusted authority is too long.";
        case verf_PARSE_ERROR:
            return "The certificate could not be parsed.";
        case err_CONSTRUCTION:
            return "There was an error when constructing the certificate.";
        case err_INVALID_PUBLIC_KEY:
            return "The public key was not in the correct format.";
        case err_INVALID_VERSION:
            return "The certificate has an invalid version number.";
        case err_INVALID_FORMAT:
            return "The certificate has an invalid format.";
        case err_ENCODING:
            return "Problem encountered while encoding the data.";
        default:
            return "Unknown code:  " + verfCode;
        }
    }
    public String toString()
    {
        return "[Certificate Exception: " + getMessage() + "]";
    }
    public String getMessage()
    {
        return getVerfDescription()
                + ( (moreData != null)
                    ? ( "\n  (" + moreData + ")" ) : "" );
    }
}
