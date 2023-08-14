public class LoggingEventHandler implements EventHandler {
    public void requestSent() {
        HttpLog.v("LoggingEventHandler:requestSent()");
    }
    public void status(int major_version,
                       int minor_version,
                       int code, 
                       String reason_phrase) {
        if (HttpLog.LOGV) {
            HttpLog.v("LoggingEventHandler:status() major: " + major_version +
                  " minor: " + minor_version +
                  " code: " + code +
                  " reason: " + reason_phrase);
        }
    }
    public void headers(Headers headers) {
        if (HttpLog.LOGV) {
            HttpLog.v("LoggingEventHandler:headers()");
            HttpLog.v(headers.toString());
        }
    }
    public void locationChanged(String newLocation, boolean permanent) {
        if (HttpLog.LOGV) {
            HttpLog.v("LoggingEventHandler: locationChanged() " + newLocation +
                      " permanent " + permanent);
        }
    }
    public void data(byte[] data, int len) {
        if (HttpLog.LOGV) {
            HttpLog.v("LoggingEventHandler: data() " + len + " bytes");
        }
    }
    public void endData() {
        if (HttpLog.LOGV) {
            HttpLog.v("LoggingEventHandler: endData() called");
        }
    }
    public void certificate(SslCertificate certificate) {
         if (HttpLog.LOGV) {
             HttpLog.v("LoggingEventHandler: certificate(): " + certificate);
         }
    }
    public void error(int id, String description) {
        if (HttpLog.LOGV) {
            HttpLog.v("LoggingEventHandler: error() called Id:" + id +
                      " description " + description);
        }
    }
    public boolean handleSslErrorRequest(SslError error) {
        if (HttpLog.LOGV) {
            HttpLog.v("LoggingEventHandler: handleSslErrorRequest():" + error);
        }
        return false;
    }
}
