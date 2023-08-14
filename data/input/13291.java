public class BuildStep {
    private static final Debug debug = Debug.getInstance("certpath");
    private Vertex          vertex;
    private X509Certificate cert;
    private Throwable       throwable;
    private int             result;
    public static final int POSSIBLE = 1;
    public static final int BACK = 2;
    public static final int FOLLOW = 3;
    public static final int FAIL = 4;
    public static final int SUCCEED = 5;
    public BuildStep(Vertex vtx, int res) {
        vertex = vtx;
        if (vertex != null) {
            cert = (X509Certificate)vertex.getCertificate();
            throwable = vertex.getThrowable();
        }
        result = res;
    }
    public Vertex getVertex() {
        return vertex;
    }
    public X509Certificate getCertificate() {
        return cert;
    }
    public String getIssuerName() {
        return (cert == null ? null : cert.getIssuerX500Principal().toString());
    }
    public String getIssuerName(String defaultName) {
        return (cert == null ? defaultName
                             : cert.getIssuerX500Principal().toString());
    }
    public String getSubjectName() {
        return (cert == null ? null : cert.getSubjectX500Principal().toString());
    }
    public String getSubjectName(String defaultName) {
        return (cert == null ? defaultName
                             : cert.getSubjectX500Principal().toString());
    }
    public Throwable getThrowable() {
        return throwable;
    }
    public int getResult() {
        return result;
    }
    public String resultToString(int res) {
        String resultString = "";
        switch (res) {
            case BuildStep.POSSIBLE:
                resultString = "Certificate to be tried.\n";
                break;
            case BuildStep.BACK:
                resultString = "Certificate backed out since path does not "
                    + "satisfy build requirements.\n";
                break;
            case BuildStep.FOLLOW:
                resultString = "Certificate satisfies conditions.\n";
                break;
            case BuildStep.FAIL:
                resultString = "Certificate backed out since path does not "
                    + "satisfy conditions.\n";
                break;
            case BuildStep.SUCCEED:
                resultString = "Certificate satisfies conditions.\n";
                break;
            default:
                resultString = "Internal error: Invalid step result value.\n";
        }
        return resultString;
    }
    public String toString() {
        String out = "Internal Error\n";
        switch (result) {
        case BACK:
        case FAIL:
            out = resultToString(result);
            out = out + vertex.throwableToString();
            break;
        case FOLLOW:
        case SUCCEED:
        case POSSIBLE:
            out = resultToString(result);
            break;
        default:
            out = "Internal Error: Invalid step result\n";
        }
        return out;
    }
    public String verboseToString() {
        String out = resultToString(getResult());
        switch (result) {
        case BACK:
        case FAIL:
            out = out + vertex.throwableToString();
            break;
        case FOLLOW:
        case SUCCEED:
            out = out + vertex.moreToString();
            break;
        case POSSIBLE:
            break;
        default:
            break;
        }
        out = out + "Certificate contains:\n" + vertex.certToString();
        return out;
    }
    public String fullToString() {
        String out = resultToString(getResult());
        out = out + vertex.toString();
        return out;
    }
}
