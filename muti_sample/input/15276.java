public abstract class CertPathHelper {
    protected static CertPathHelper instance;
    protected CertPathHelper() {
    }
    protected abstract void implSetPathToNames(X509CertSelector sel,
            Set<GeneralNameInterface> names);
    protected abstract void implSetDateAndTime(X509CRLSelector sel, Date date, long skew);
    static void setPathToNames(X509CertSelector sel,
            Set<GeneralNameInterface> names) {
        instance.implSetPathToNames(sel, names);
    }
    static void setDateAndTime(X509CRLSelector sel, Date date, long skew) {
        instance.implSetDateAndTime(sel, date, skew);
    }
}
