class AppletViewerPanel extends AppletPanel {
    static boolean debug = false;
    URL documentURL;
    URL baseURL;
    Hashtable atts;
    private static final long serialVersionUID = 8890989370785545619L;
    AppletViewerPanel(URL documentURL, Hashtable atts) {
        this.documentURL = documentURL;
        this.atts = atts;
        String att = getParameter("codebase");
        if (att != null) {
            if (!att.endsWith("/")) {
                att += "/";
            }
            try {
                baseURL = new URL(documentURL, att);
            } catch (MalformedURLException e) {
            }
        }
        if (baseURL == null) {
            String file = documentURL.getFile();
            int i = file.lastIndexOf('/');
            if (i >= 0 && i < file.length() - 1) {
                try {
                    baseURL = new URL(documentURL, file.substring(0, i + 1));
                } catch (MalformedURLException e) {
                }
            }
        }
        if (baseURL == null)
                baseURL = documentURL;
    }
    public String getParameter(String name) {
        return (String)atts.get(name.toLowerCase());
    }
    public URL getDocumentBase() {
        return documentURL;
    }
    public URL getCodeBase() {
        return baseURL;
    }
    public int getWidth() {
        String w = getParameter("width");
        if (w != null) {
            return Integer.valueOf(w).intValue();
        }
        return 0;
    }
    public int getHeight() {
        String h = getParameter("height");
        if (h != null) {
            return Integer.valueOf(h).intValue();
        }
        return 0;
    }
    public boolean hasInitialFocus()
    {
        if (isJDK11Applet() || isJDK12Applet())
            return false;
        String initialFocus = getParameter("initial_focus");
        if (initialFocus != null)
        {
            if (initialFocus.toLowerCase().equals("false"))
                return false;
        }
        return true;
    }
    public String getCode() {
        return getParameter("code");
    }
    public String getJarFiles() {
        return getParameter("archive");
    }
    public String getSerializedObject() {
        return getParameter("object");
    }
    public AppletContext getAppletContext() {
        return (AppletContext)getParent();
    }
    static void debug(String s) {
        if(debug)
            System.err.println("AppletViewerPanel:::" + s);
    }
    static void debug(String s, Throwable t) {
        if(debug) {
            t.printStackTrace();
            debug(s);
        }
    }
}
