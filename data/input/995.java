public class SALearnExternalToolPlugin extends AbstractExternalToolsPlugin {
    protected static URL websiteURL;
    static {
        try {
            websiteURL = new URL("http:
        } catch (MalformedURLException mue) {
        }
    }
    File defaultLinux = new File("/usr/bin/sa-learn");
    File defaultLocalLinux = new File("/usr/local/bin/sa-learn");
    public SALearnExternalToolPlugin() {
        super();
    }
    public String getDescription() {
        return "<html><body><p>sa-learn - train SpamAssassin's Bayesian classifier</p></body></html>";
    }
    public URL getWebsite() {
        return websiteURL;
    }
    public File locate() {
        if (OSInfo.isLinux() || OSInfo.isSolaris()) {
            if (defaultLinux.exists()) {
                return defaultLinux;
            } else if (defaultLocalLinux.exists()) {
                return defaultLocalLinux;
            }
        }
        return null;
    }
}
