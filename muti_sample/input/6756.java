public class HttpCaller extends GSSCaller {
    final private HttpCallerInfo hci;
    public HttpCaller(HttpCallerInfo hci) {
        this.hci = hci;
    }
    public HttpCallerInfo info() {
        return hci;
    }
}
