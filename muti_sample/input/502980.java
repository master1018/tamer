public class TestEventHandler implements EventHandler {
    private int majorVersion = -1;
    private int minorVersion = -1;
    private int responseCode = -1;
    private String reasonPhrase;
    private Map<String, String> headerMap;
    public static final Object syncObj = new Object();
    private boolean useLowLevel = false;
    private boolean delayResponse = false;
    public final static int TEST_REQUEST_SENT = 0;
    public final static int TEST_STATUS = 1;
    public final static int TEST_HEADERS = 2;
    public final static int TEST_LOCATION_CHANGED = 3;
    public final static int TEST_DATA = 4;
    public final static int TEST_ENDDATA = 5;
    public final static int TEST_ERROR = 6;
    public final static int TEST_SSL_CERTIFICATE_ERROR = 7;
    public final static int TEST_NUM_EXPECTS = 8;
    private int expectMajor = -1;
    private int expectMinor = -1;
    private int expectCode = -1;
    private boolean[] expects = new boolean[TEST_NUM_EXPECTS];
    private boolean[] notExpecting = new boolean[TEST_NUM_EXPECTS];
    private boolean[] eventsReceived = new boolean[TEST_NUM_EXPECTS];
    private String expectLocation;
    private int expectPermanent = -1;
    private byte[] expectData;
    private int expectDataLength = -1;
    private int expectErrorId = -1;
    private int expectSslErrors = -1;
    private SslCertificate expectCertificate;
    public class TestHeader {
        public TestHeader(String n, String v) {
            name = n;
            value = v;
        }
        public String name;
        public String value;
    }
    private ArrayList<TestHeader> expectHeaders = new ArrayList<TestHeader>();
    private StringBuffer expectDetails = new StringBuffer();
    private RequestHandle mRequestHandle;
    private LowLevelNetRunner netRunner;
    public TestEventHandler() {
        for (int i = 0; i < TEST_NUM_EXPECTS; i++) {
            expects[i] = false;
            notExpecting[i] = false;
            eventsReceived[i] = false;
        }
    }
    public void requestSent() {
      Log.v(LOGTAG, "TestEventHandler:requestSent()");
      expects[TEST_REQUEST_SENT] = false;
      eventsReceived[TEST_REQUEST_SENT] = true;
      if (notExpecting[TEST_REQUEST_SENT]) {
          expectDetails.append("Request sent event received but not expected");
          expectDetails.append("\r\n");
      }
      if (useLowLevel) {
        if (delayResponse) {
          synchronized (syncObj) {
            syncObj.notifyAll();
          }
        } else {
        }
      }
    }
    public void status(int major_version, int minor_version,
        int code, String reason_phrase) {
      if (Config.LOGV) {
        Log.v(LOGTAG, "TestEventHandler:status() major: " + major_version +
            " minor: " + minor_version +
            " code: " + code +
            " reason: " + reason_phrase);
      }
      eventsReceived[TEST_STATUS] = true;
      if (notExpecting[TEST_STATUS]) {
        expectDetails.append("Status event received but not expected");
        expectDetails.append("\r\n");
      }
      majorVersion = major_version;
      minorVersion = minor_version;
      responseCode = code;
      reasonPhrase = reason_phrase;
      if (expectMajor != -1) {
        if (expectMajor == major_version) {
          expectMajor = -1;
        } else {
          expectDetails.append("Major version expected:"+expectMajor+
              " got:"+major_version);
          expectDetails.append("\r\n");
        }
      }
      if (expectMinor != -1) {
        if (expectMinor == minor_version) {
          expectMinor = -1;
        } else {
          expectDetails.append("Minor version expected:"+expectMinor+
              " got:"+minor_version);
          expectDetails.append("\r\n");
        }
      }
      if (expectCode != -1) {
        if (expectCode == code) {
          expectCode = -1;
        } else {
          expectDetails.append("Status code expected:"+expectCode+
              " got:"+code);
          expectDetails.append("\r\n");
        }
      }
      if ((expectMajor == -1) && (expectMinor == -1) && (expectCode == -1)) {
        expects[TEST_STATUS] = false;
      } else {
        System.out.println("MAJOR = "+expectMajor+" MINOR = "+expectMinor+
            " CODE = "+expectCode);
      }
    }
    public void headers(Headers headers) {
        if (Config.LOGV) {
            Log.v(LOGTAG, "TestEventHandler:headers()");
        }
        expects[TEST_HEADERS] = false;
        if (notExpecting[TEST_HEADERS]) {
            expectDetails.append("Header event received but not expected");
            expectDetails.append("\r\n");
        }
        if (expectHeaders.isEmpty()) {
            return;
        }      
        for (int i = expectHeaders.size() - 1; i >= 0; i--) {
            TestHeader h =  expectHeaders.get(i);
            System.out.println("Expected header name: " + h.name);
            String s = null;
            switch (h.name.hashCode()) {
            case -1132779846:
                s = Long.toString(headers.getContentLength());
                break;
            case 785670158:
                s = headers.getContentType();
                break;
            case 2095084583:
                s = headers.getContentEncoding();
                break;
            case 1901043637:
                s = headers.getLocation();
                break;
            case -243037365:
                s = headers.getWwwAuthenticate();
                break;
            case -301767724:
                s = headers.getProxyAuthenticate();
                break;
            case -1267267485:
                s = headers.getContentDisposition();
                break;
            case 1397189435:
                s = headers.getAcceptRanges();
                break;
            case -1309235404:
                s = headers.getExpires();
                break;
            case -208775662:
                s = headers.getCacheControl();
                break;
            case 150043680:
                s = headers.getLastModified();
                break;
            case 3123477:
                s = headers.getEtag();
                break;
            case -775651618:
                int ct = headers.getConnectionType();
                if (ct == Headers.CONN_CLOSE) {
                    s = HTTP.CONN_CLOSE;
                } else if (ct == Headers.CONN_KEEP_ALIVE) {
                    s = HTTP.CONN_KEEP_ALIVE;
                }
                break;
            default:
                s = null;
            }
            if (evaluateHeader(h, s)) {
                expectHeaders.remove(i);
            }
        }
    }
    public boolean evaluateHeader(TestHeader h, String value) {
        if (value == null) {
            expects[TEST_HEADERS] = true;
            System.out.print(" Missing!  ");
            expectDetails.append(" missing header " + h.name);
            return false;
        }
        if (h.value == null) {
            System.out.println("Expect value = null");
            return true;
        }
        System.out.println("Expect value = " +
                (h.value.toLowerCase()) + " got " +
                value.toLowerCase());
        if (!h.value.equalsIgnoreCase(value)) {
            expectDetails.append("expect header value " + h.value +
                    " got " + value);
            expects[TEST_HEADERS] = true;
            return false;
        }
        return true;
    }
    public void locationChanged(String newLocation, boolean permanent) {
      if (Config.LOGV) {
        Log.v(LOGTAG, "TestEventHandler: locationChanged() " +
            newLocation + " permanent " + permanent);
      }
      eventsReceived[TEST_LOCATION_CHANGED] = true;
      if (notExpecting[TEST_LOCATION_CHANGED]) {
        expectDetails.append("Location changed event received but "+
            "not expected");
        expectDetails.append("\r\n");
      }
      if (expectLocation != null) {
        if (expectLocation.equals(newLocation)) {
          expectLocation = null;
        } else {
          expectDetails.append("Location expected:"+expectLocation+
              " got:"+newLocation);
          expectDetails.append("\r\n");
        }
      }
      if (expectPermanent != -1) {
        if (((expectPermanent == 0) && !permanent) ||
            ((expectPermanent == 1) && permanent)){
          expectPermanent = -1;
        } else {
          expectDetails.append("Location permanent expected:"+
              expectPermanent+" got"+permanent);
          expectDetails.append("\r\n");
        }
      }
      if ((expectLocation == null) && (expectPermanent == -1))
        expects[TEST_LOCATION_CHANGED] = false;
    }
    public void data(byte[] data, int len) {
      boolean mismatch = false;
      if (Config.LOGV) {
        Log.v(LOGTAG, "TestEventHandler: data() " + len + " bytes");
      }
      eventsReceived[TEST_DATA] = true;
      if (notExpecting[TEST_DATA]) {
        expectDetails.append("Data event received but not expected");
        expectDetails.append("\r\n");
      }
      Log.v(LOGTAG, new String(data, 0, len));
      if (expectDataLength != -1) {
        if (expectDataLength == len) {
          expectDataLength = -1;
        } else {
          expectDetails.append("expect data length mismatch expected:"+
              expectDataLength+" got:"+len);
          expectDetails.append("\r\n");
        }
        if ((expectDataLength == -1) && expectData != null) {
          for (int i = 0; i < len; i++) {
            if (expectData[i] != data[i]) {
              mismatch = true;
              expectDetails.append("Expect data mismatch at byte "+
                  i+" expected:"+expectData[i]+" got:"+data[i]);
              expectDetails.append("\r\n");
              break;
            }
          }
        }
      }
      if ((expectDataLength == -1) || !mismatch)
        expects[TEST_DATA] = false;
    }
    public void endData() {
      if (Config.LOGV) {
        Log.v(LOGTAG, "TestEventHandler: endData() called");
      }
      eventsReceived[TEST_ENDDATA] = true;
      if (notExpecting[TEST_ENDDATA]) {
        expectDetails.append("End data event received but not expected");
        expectDetails.append("\r\n");
      }
      expects[TEST_ENDDATA] = false;
      if (useLowLevel) {
        if (delayResponse) {
          synchronized (syncObj) {
            syncObj.notifyAll();
          }
        } else {
          if (netRunner != null) {
            System.out.println("TestEventHandler: endData() stopping "+
                netRunner);
            netRunner.decrementRunCount();
          }
        }
      }
    }
    public void certificate(SslCertificate certificate) {}
    public void error(int id, String description) {
      if (Config.LOGV) {
        Log.v(LOGTAG, "TestEventHandler: error() called Id:" + id +
            " description " + description);
      }
      eventsReceived[TEST_ERROR] = true;
      if (notExpecting[TEST_ERROR]) {
        expectDetails.append("Error event received but not expected");
        expectDetails.append("\r\n");
      }
      if (expectErrorId != -1) {
        if (expectErrorId == id) {
          expectErrorId = -1;
        } else {
          expectDetails.append("Error Id expected:"+expectErrorId+
              " got:"+id);
          expectDetails.append("\r\n");
        }
      }
      if (expectErrorId == -1)
        expects[TEST_ERROR] = false;
      if (useLowLevel) {
        if (delayResponse) {
          synchronized (syncObj) {
            syncObj.notifyAll();
          }
        } else {
          if (netRunner != null) {
            System.out.println("TestEventHandler: endData() stopping "+
                netRunner);
            netRunner.decrementRunCount();
          }
        }
      }
    }
    public boolean handleSslErrorRequest(SslError error) {
      int primaryError = error.getPrimaryError();
      if (Config.LOGV) {
        Log.v(LOGTAG, "TestEventHandler: handleSslErrorRequest(): "+
              " primary error:" + primaryError +
              " certificate: " + error.getCertificate());
      }
      eventsReceived[TEST_SSL_CERTIFICATE_ERROR] = true;
      if (notExpecting[TEST_SSL_CERTIFICATE_ERROR]) {
        expectDetails.append("SSL Certificate error event received "+
            "but not expected");
        expectDetails.append("\r\n");
      }
      if (expectSslErrors != -1) {
        if (expectSslErrors == primaryError) {
            expectSslErrors = -1;
        } else {
            expectDetails.append("SslCertificateError id expected:"+
                expectSslErrors+" got: " + primaryError);
            expectDetails.append("\r\n");
        }
      }
      if (expectSslErrors == -1) 
        expects[TEST_SSL_CERTIFICATE_ERROR] = false;
      return false;
    }
    public void setNetRunner(LowLevelNetRunner runner) {
      setNetRunner(runner, false);
    }
    public void setNetRunner(LowLevelNetRunner runner,
        boolean delayedResponse) {
      netRunner = runner;
      useLowLevel = true;
      delayResponse = delayedResponse;
      if (!delayResponse)
        netRunner.incrementRunCount();
    }
    public void waitForRequestResponse() {
      if (!delayResponse || !useLowLevel) {
        Log.d(LOGTAG, " Cant do this without delayReponse set ");
        return;
      }
      synchronized (syncObj) {
        try {
          syncObj.wait();
        } catch (InterruptedException e) {
        }
      }
    }
    public void waitForRequestSent() {
      if (!delayResponse || !useLowLevel) {
        Log.d(LOGTAG, " Cant do this without delayReponse set ");
        return;
      }
      synchronized (syncObj) {
        try {
          syncObj.wait();
        } catch (InterruptedException e) {
        }
      }
    }
    public void expectRequestSent() {
        expects[TEST_REQUEST_SENT] = true;
    }
    public void expectNoRequestSent() {
        notExpecting[TEST_REQUEST_SENT] = true;
    }
    public void expectStatus() {
        expects[TEST_STATUS] = true;
    }
    public void expectNoStatus() {
        notExpecting[TEST_STATUS] = true;
    }
    public void expectStatus(int major, int minor, int code) {
        expects[TEST_STATUS] = true;
        expectMajor = major;
        expectMinor = minor;
        expectCode = code;
    }
    public void expectStatus(int code) {
        expects[TEST_STATUS] = true;
        expectCode = code;
    }
    public void expectHeaders() {
        expects[TEST_HEADERS] = true;
    }
    public void expectNoHeaders() {
        notExpecting[TEST_HEADERS] = true;
    }
    public void expectHeaderAdd(String name) {
        expects[TEST_HEADERS] = true;
        TestHeader h = new TestHeader(name.toLowerCase(), null);
        expectHeaders.add(h);
    }
    public void expectHeaderAdd(String name, String value) {
        expects[TEST_HEADERS] = true;
        TestHeader h = new TestHeader(name.toLowerCase(), value);
        expectHeaders.add(h);
    }
    public void expectLocationChanged() {
        expects[TEST_LOCATION_CHANGED] = true;
    }
    public void expectNoLocationChanged() {
            notExpecting[TEST_LOCATION_CHANGED] = true;
    }
    public void expectLocationChanged(String newLocation) {
        expects[TEST_LOCATION_CHANGED] = true;
        expectLocation = newLocation;
    }
    public void expectLocationChanged(String newLocation, boolean permanent) {
        expects[TEST_LOCATION_CHANGED] = true;
        expectLocation = newLocation;
        expectPermanent = permanent ? 1 : 0;
    }
    public void expectData() {
        expects[TEST_DATA] = true;
    }
    public void expectNoData() {
        notExpecting[TEST_DATA] = true;
    }
    public void expectData(int len) {
        expects[TEST_DATA] = true;
        expectDataLength = len;
    }
    public void expectData(byte[] data, int len) {
        expects[TEST_DATA] = true;
        expectData = new byte[len];
        expectDataLength = len;
        for (int i = 0; i < len; i++) {
            expectData[i] = data[i];
        }
    }
    public void expectEndData() {
        expects[TEST_ENDDATA] = true;
    }
    public void expectNoEndData() {
            notExpecting[TEST_ENDDATA] = true;
    }
    public void expectError() {
        expects[TEST_ERROR] = true;
    }
    public void expectNoError() {
        notExpecting[TEST_ERROR] = true;
    }
    public void expectError(int errorId) {
        expects[TEST_ERROR] = true;
        expectErrorId = errorId;
    }
    public void expectSSLCertificateError() {
        expects[TEST_SSL_CERTIFICATE_ERROR] = true;
    }
    public void expectNoSSLCertificateError() {
            notExpecting[TEST_SSL_CERTIFICATE_ERROR] = true;
    }
    public void expectSSLCertificateError(int errors) {
        expects[TEST_SSL_CERTIFICATE_ERROR] = true;
        expectSslErrors = errors;
    }
    public void expectSSLCertificateError(SslCertificate certificate) {
        expects[TEST_SSL_CERTIFICATE_ERROR] = true;
        expectCertificate = certificate;
    }
    public boolean expectPassed() {
        for (int i = 0; i < TEST_NUM_EXPECTS; i++) {
            if (expects[i] == true) {
                return false;
            }
        }
        for (int i = 0; i < TEST_NUM_EXPECTS; i++) {
            if (eventsReceived[i] && notExpecting[i]) {
                return false;
            }
        }
        return true;
    }
    public String getFailureMessage() {
        return expectDetails.toString();
    }
    public void resetExpects() {
        expectMajor = -1;
        expectMinor = -1;
        expectCode = -1;
        expectLocation = null;
        expectPermanent = -1;
        expectErrorId = -1;
        expectSslErrors = -1;
        expectCertificate = null;
        expectDetails.setLength(0);
        expectHeaders.clear();
        for (int i = 0; i < TEST_NUM_EXPECTS; i++) {
            expects[i] = false;
            notExpecting[i] = false;
            eventsReceived[i] = false;
        }
        for (int i = 0; i < expectDataLength; i++) {
            expectData[i] = 0;
        }
        expectDataLength = -1;
    }
    public void attachRequestHandle(RequestHandle requestHandle) {
        if (Config.LOGV) {
            Log.v(LOGTAG, "TestEventHandler.attachRequestHandle(): " +
                    "requestHandle: " +  requestHandle);
        }
        mRequestHandle = requestHandle;
    }
    public void detachRequestHandle() {
        if (Config.LOGV) {
            Log.v(LOGTAG, "TestEventHandler.detachRequestHandle(): " +
                    "requestHandle: " + mRequestHandle);
        }
        mRequestHandle = null;
    }
    protected final static String LOGTAG = "http";
}
