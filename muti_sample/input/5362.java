public class Extern {
    private Map<String,Item> packageToItemMap;
    private final Configuration configuration;
    private boolean linkoffline = false;
    private class Item {
        final String packageName;
        final String path;
        final boolean relative;
        Item(String packageName, String path, boolean relative) {
            this.packageName = packageName;
            this.path = path;
            this.relative = relative;
            if (packageToItemMap == null) {
                packageToItemMap = new HashMap<String,Item>();
            }
            if (!packageToItemMap.containsKey(packageName)) { 
                packageToItemMap.put(packageName, this);        
            }
        }
        public String toString() {
            return packageName + (relative? " -> " : " => ") + path;
        }
    }
    public Extern(Configuration configuration) {
        this.configuration = configuration;
    }
    public boolean isExternal(ProgramElementDoc doc) {
        if (packageToItemMap == null) {
            return false;
        }
        return packageToItemMap.get(doc.containingPackage().name()) != null;
    }
    public String getExternalLink(String pkgName,
                                  String relativepath, String link) {
        Item fnd = findPackageItem(pkgName);
        if (fnd != null) {
            String externlink = fnd.path + link;
            if (fnd.relative) {  
                return relativepath + externlink;
            } else {
                return externlink;
            }
        }
        return null;
    }
    public boolean url(String url, String pkglisturl,
                              DocErrorReporter reporter, boolean linkoffline) {
        this.linkoffline = linkoffline;
        String errMsg = composeExternPackageList(url, pkglisturl);
        if (errMsg != null) {
            reporter.printWarning(errMsg);
            return false;
        } else {
            return true;
        }
    }
    private Item findPackageItem(String pkgName) {
        if (packageToItemMap == null) {
            return null;
        }
        return packageToItemMap.get(pkgName);
    }
    private String composeExternPackageList(String urlOrDirPath, String pkgListUrlOrDirPath) {
        urlOrDirPath = adjustEndFileSeparator(urlOrDirPath);
        pkgListUrlOrDirPath = adjustEndFileSeparator(pkgListUrlOrDirPath);
        return isUrl(pkgListUrlOrDirPath) ?
            fetchURLComposeExternPackageList(urlOrDirPath, pkgListUrlOrDirPath) :
            readFileComposeExternPackageList(urlOrDirPath, pkgListUrlOrDirPath);
    }
    private String adjustEndFileSeparator(String url) {
        String filesep = "/";
        if (!url.endsWith(filesep)) {
            url += filesep;
        }
        return url;
    }
    private String fetchURLComposeExternPackageList(String urlpath,
                                                   String pkglisturlpath) {
        String link = pkglisturlpath + "package-list";
        try {
            readPackageList((new URL(link)).openStream(), urlpath, false);
        } catch (MalformedURLException exc) {
            return configuration.getText("doclet.MalformedURL", link);
        } catch (IOException exc) {
            return configuration.getText("doclet.URL_error", link);
        }
        return null;
    }
    private String readFileComposeExternPackageList(String path,
                                                   String pkgListPath) {
        String link = pkgListPath + "package-list";
        if (! ((new File(pkgListPath)).isAbsolute() || linkoffline)){
            link = configuration.destDirName + link;
        }
        try {
            File file = new File(link);
            if (file.exists() && file.canRead()) {
                readPackageList(new FileInputStream(file), path,
                    ! ((new File(path)).isAbsolute() || isUrl(path)));
            } else {
                return configuration.getText("doclet.File_error", link);
            }
        } catch (FileNotFoundException exc) {
            return configuration.getText("doclet.File_error", link);
        } catch (IOException exc) {
            return configuration.getText("doclet.File_error", link);
        }
        return null;
    }
    private void readPackageList(InputStream input, String path,
                                boolean relative)
                         throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(input));
        StringBuffer strbuf = new StringBuffer();
        try {
            int c;
            while ((c = in.read()) >= 0) {
                char ch = (char)c;
                if (ch == '\n' || ch == '\r') {
                    if (strbuf.length() > 0) {
                        String packname = strbuf.toString();
                        String packpath = path +
                                      packname.replace('.', '/') + '/';
                        new Item(packname, packpath, relative);
                        strbuf.setLength(0);
                    }
                } else {
                    strbuf.append(ch);
                }
            }
        } finally {
            input.close();
        }
    }
    public boolean isUrl (String urlCandidate) {
        try {
            new URL(urlCandidate);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }
}
