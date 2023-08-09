class SunConnection {
    private static String JDK_REGISTRATION_URL = "https:
    private static String SANDBOX_TESTING_URL = "https:
    private static String REGISTRATION_WEB_PATH = "RegistrationWeb/register";
    private static String SVCTAG_REGISTER_TESTING = "servicetag.register.testing";
    private static String SVCTAG_REGISTRATION_URL = "servicetag.registration.url";
    private static String SVCTAG_CONNECTION_TIMEOUT = "servicetag.connection.timeout";
    private SunConnection() {
    }
    static URL getRegistrationURL(String registrationURN, Locale locale, String version) {
        String url = System.getProperty(SVCTAG_REGISTRATION_URL);
        if (url == null) {
            if (System.getProperty(SVCTAG_REGISTER_TESTING) != null) {
                url = SANDBOX_TESTING_URL;
            } else {
                url = JDK_REGISTRATION_URL;
            }
        }
        url += REGISTRATION_WEB_PATH;
        url = url.trim();
        if (url.length() == 0) {
            throw new InternalError("Empty registration url set");
        }
        String registerURL = rewriteURL(url, registrationURN, locale, version);
        try {
            return new URL(registerURL);
        } catch (MalformedURLException ex) {
            InternalError x =
                new InternalError(ex.getMessage());
            x.initCause(ex);
            throw x;
        }
    }
    private static String rewriteURL(String url, String registryURN, Locale locale, String version) {
        StringBuilder sb = new StringBuilder(url.trim());
        int len = sb.length();
        if (sb.charAt(len-1) != '/') {
            sb.append('/');
        }
        sb.append(registryURN);
        sb.append("?");
        sb.append("product=jdk");
        sb.append("&");
        sb.append("locale=").append(locale.toString());
        sb.append("&");
        sb.append("version=").append(version);
        return sb.toString();
    }
    public static void register(RegistrationData regData,
                                Locale locale,
                                String version) throws IOException {
        URL url = getRegistrationURL(regData.getRegistrationURN(),
                                     locale,
                                     version);
        boolean succeed = postRegistrationData(url, regData);
        if (succeed) {
            openBrowser(url);
        } else {
            openOfflineRegisterPage();
        }
    }
    private static void openBrowser(URL url) throws IOException {
        if (!BrowserSupport.isSupported()) {
            if (Util.isVerbose()) {
                System.out.println("Browser is not supported");
            }
            return;
        }
        try {
            BrowserSupport.browse(url.toURI());
        } catch (URISyntaxException ex) {
            InternalError x = new InternalError("Error in registering: " + ex.getMessage());
            x.initCause(ex);
            throw x;
        } catch (IllegalArgumentException ex) {
            if (Util.isVerbose()) {
                ex.printStackTrace();
            }
        } catch (UnsupportedOperationException ex) {
            if (Util.isVerbose()) {
                ex.printStackTrace();
            }
        }
    }
    private static boolean postRegistrationData(URL url,
                                                RegistrationData registration) {
        try {
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setUseCaches(false);
            con.setAllowUserInteraction(false);
            String timeout = System.getProperty(SVCTAG_CONNECTION_TIMEOUT, "10");
            con.setConnectTimeout(Util.getIntValue(timeout) * 1000);
            if (Util.isVerbose()) {
                System.out.println("Connecting to post registration data at " + url);
            }
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "text/xml;charset=\"utf-8\"");
            con.connect();
            OutputStream out = null;
            try {
                out = con.getOutputStream();
                registration.storeToXML(out);
                out.flush();
            } finally {
                if (out != null) {
                    out.close();
                }
            }
            int returnCode = con.getResponseCode();
            if (Util.isVerbose()) {
                System.out.println("POST return status = " + returnCode);
                printReturnData(con, returnCode);
            }
            return (returnCode == HttpURLConnection.HTTP_OK);
        } catch (MalformedURLException me) {
            InternalError x = new InternalError("Error in registering: " + me.getMessage());
            x.initCause(me);
            throw x;
        } catch (Exception ioe) {
            if (Util.isVerbose()) {
                ioe.printStackTrace();
            }
            return false;
        }
    }
    private static void openOfflineRegisterPage()
            throws IOException {
        if (!BrowserSupport.isSupported()) {
            if (Util.isVerbose()) {
                System.out.println("Browser is not supported");
            }
            return;
        }
        File registerPage = Installer.getRegistrationHtmlPage();
        try {
            BrowserSupport.browse(registerPage.toURI());
        } catch (FileNotFoundException ex) {
            InternalError x =
                new InternalError("Error in launching " + registerPage + ": " + ex.getMessage());
            x.initCause(ex);
            throw x;
        } catch (IllegalArgumentException ex) {
            if (Util.isVerbose()) {
                ex.printStackTrace();
            }
        } catch (UnsupportedOperationException ex) {
            if (Util.isVerbose()) {
                ex.printStackTrace();
            }
        }
    }
    private static void printReturnData(HttpURLConnection con, int returnCode)
            throws IOException {
        BufferedReader reader = null;
        try {
            if (returnCode < 400) {
                reader = new BufferedReader(
                             new InputStreamReader(con.getInputStream()));
            } else {
                reader = new BufferedReader(
                             new InputStreamReader(con.getErrorStream()));
            }
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            System.out.println("Response is : ");
            System.out.println(sb.toString());
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }
}
