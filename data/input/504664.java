public class SslError {
  public static final int SSL_NOTYETVALID = 0;
    public static final int SSL_EXPIRED = 1;
    public static final int SSL_IDMISMATCH = 2;
    public static final int SSL_UNTRUSTED = 3;
    public static final int SSL_MAX_ERROR = 4;
    int mErrors;
    SslCertificate mCertificate;
    public SslError(int error, SslCertificate certificate) {
        addError(error);
        mCertificate = certificate;
    }
    public SslError(int error, X509Certificate certificate) {
        addError(error);
        mCertificate = new SslCertificate(certificate);
    }
    public SslCertificate getCertificate() {
        return mCertificate;
    }
    public boolean addError(int error) {
        boolean rval = (0 <= error && error < SslError.SSL_MAX_ERROR);
        if (rval) {
            mErrors |= (0x1 << error);
        }
        return rval;
    }
    public boolean hasError(int error) {
        boolean rval = (0 <= error && error < SslError.SSL_MAX_ERROR);
        if (rval) {
            rval = ((mErrors & (0x1 << error)) != 0);
        }
        return rval;
    }
    public int getPrimaryError() {
        if (mErrors != 0) {
            for (int error = SslError.SSL_MAX_ERROR - 1; error >= 0; --error) {
                if ((mErrors & (0x1 << error)) != 0) {
                    return error;
                }
            }
        }
        return 0;
    }
    public String toString() {
        return "primary error: " + getPrimaryError() +
            " certificate: " + getCertificate();
    }
}
