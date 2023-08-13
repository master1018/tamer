class ServiceProxy {
    static class ServiceConfigurationError extends Error {
        static final long serialVersionUID = 7732091036771098303L;
        ServiceConfigurationError(String msg) {
            super(msg);
        }
    }
    private static final String prefix = "META-INF/services/";
    private static void fail(Class<?> service, String msg)
            throws ServiceConfigurationError {
        throw new ServiceConfigurationError(service.getName() + ": " + msg);
    }
    private static void fail(Class<?> service, URL u, int line, String msg)
            throws ServiceConfigurationError {
        fail(service, u + ":" + line + ": " + msg);
    }
    private static boolean parse(Class<?> service, URL u) throws ServiceConfigurationError {
        InputStream in = null;
        BufferedReader r = null;
        try {
            in = u.openStream();
            r = new BufferedReader(new InputStreamReader(in, "utf-8"));
            int lc = 1;
            String ln;
            while ((ln = r.readLine()) != null) {
                int ci = ln.indexOf('#');
                if (ci >= 0) ln = ln.substring(0, ci);
                ln = ln.trim();
                int n = ln.length();
                if (n != 0) {
                    if ((ln.indexOf(' ') >= 0) || (ln.indexOf('\t') >= 0))
                        fail(service, u, lc, "Illegal configuration-file syntax");
                    int cp = ln.codePointAt(0);
                    if (!Character.isJavaIdentifierStart(cp))
                        fail(service, u, lc, "Illegal provider-class name: " + ln);
                    for (int i = Character.charCount(cp); i < n; i += Character.charCount(cp)) {
                        cp = ln.codePointAt(i);
                        if (!Character.isJavaIdentifierPart(cp) && (cp != '.'))
                            fail(service, u, lc, "Illegal provider-class name: " + ln);
                    }
                    return true;
                }
            }
        } catch (FileNotFoundException x) {
            return false;
        } catch (IOException x) {
            fail(service, ": " + x);
        } finally {
            try {
                if (r != null) r.close();
            } catch (IOException y) {
                fail(service, ": " + y);
            }
            try {
                if (in != null) in.close();
            } catch (IOException y) {
                fail(service, ": " + y);
            }
        }
        return false;
    }
    public static boolean hasService(Class<?> service, URL[] urls)
            throws ServiceConfigurationError {
        for (URL url: urls) {
            try {
                String fullName = prefix + service.getName();
                URL u = new URL(url, fullName);
                boolean found = parse(service, u);
                if (found)
                    return true;
            } catch (MalformedURLException e) {
            }
        }
        return false;
    }
}
