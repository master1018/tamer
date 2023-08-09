public class URLClassLoader extends SecureClassLoader {
    ArrayList<URL> originalUrls;
    List<URL> searchList;
    ArrayList<URLHandler> handlerList;
    Map<URL, URLHandler> handlerMap = new HashMap<URL, URLHandler>();
    private URLStreamHandlerFactory factory;
    private AccessControlContext currentContext;
    static class SubURLClassLoader extends URLClassLoader {
        private boolean checkingPackageAccess = false;
        SubURLClassLoader(URL[] urls) {
            super(urls, ClassLoader.getSystemClassLoader());
        }
        SubURLClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }
        @Override
        protected synchronized Class<?> loadClass(String className,
                                                  boolean resolveClass) throws ClassNotFoundException {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null && !checkingPackageAccess) {
                int index = className.lastIndexOf('.');
                if (index > 0) { 
                    try {
                        checkingPackageAccess = true;
                        sm.checkPackageAccess(className.substring(0, index));
                    } finally {
                        checkingPackageAccess = false;
                    }
                }
            }
            return super.loadClass(className, resolveClass);
        }
    }
    static class IndexFile {
        private HashMap<String, ArrayList<URL>> map;
        static IndexFile readIndexFile(JarFile jf, JarEntry indexEntry, URL url) {
            BufferedReader in = null;
            InputStream is = null;
            try {
                String parentURLString = getParentURL(url).toExternalForm();
                String prefix = "jar:" 
                        + parentURLString + "/"; 
                is = jf.getInputStream(indexEntry);
                in = new BufferedReader(new InputStreamReader(is, "UTF8"));
                HashMap<String, ArrayList<URL>> pre_map = new HashMap<String, ArrayList<URL>>();
                if (in.readLine() == null) return null;
                if (in.readLine() == null) return null;
                TOP_CYCLE:
                while (true) {
                    String line = in.readLine();
                    if (line == null) {
                        break;
                    }
                    URL jar = new URL(prefix + line + "!/"); 
                    while (true) {
                        line = in.readLine();
                        if (line == null) {
                            break TOP_CYCLE;
                        }
                        if ("".equals(line)) {
                            break;
                        }
                        ArrayList<URL> list;
                        if (pre_map.containsKey(line)) {
                            list = pre_map.get(line);
                        } else {
                            list = new ArrayList<URL>();
                            pre_map.put(line, list);
                        }
                        list.add(jar);
                    }
                }
                if (!pre_map.isEmpty()) {
                    return new IndexFile(pre_map);
                }
            } catch (MalformedURLException e) {
            } catch (IOException e) {
            }
            finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                    }
                }
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                }
            }
            return null;
        }
        private static URL getParentURL(URL url) throws IOException {
            URL fileURL = ((JarURLConnection) url.openConnection()).getJarFileURL();
            String file = fileURL.getFile();
            String parentFile = new File(file).getParent();
            parentFile = parentFile.replace(File.separatorChar, '/');
            if (parentFile.charAt(0) != '/') {
                parentFile = "/" + parentFile; 
            }
            URL parentURL = new URL(fileURL.getProtocol(), fileURL
                    .getHost(), fileURL.getPort(), parentFile);
            return parentURL;
        }
        public IndexFile(HashMap<String, ArrayList<URL>> map) {
            this.map = map;
        }
        ArrayList<URL> get(String name) {
            return map.get(name);
        }
    }
    class URLHandler {
        URL url;
        URL codeSourceUrl;
        public URLHandler(URL url) {
            this.url = url;
            this.codeSourceUrl = url;
        }
        void findResources(String name, ArrayList<URL> resources) {
            URL res = findResource(name);
            if (res != null && !resources.contains(res)) {
                resources.add(res);
            }
        }
        Class<?> findClass(String packageName, String name, String origName) {
            URL resURL = targetURL(url, name);
            if (resURL != null) {
                try {
                    InputStream is = resURL.openStream();
                    return createClass(is, packageName, origName);
                } catch (IOException e) {
                }
            }
            return null;
        }
        Class<?> createClass(InputStream is, String packageName, String origName) {
            if (is == null) {
                return null;
            }
            byte[] clBuf = null;
            try {
                clBuf = getBytes(is);
            } catch (IOException e) {
                return null;
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
            if (packageName != null) {
                String packageDotName = packageName.replace('/', '.');
                Package packageObj = getPackage(packageDotName);
                if (packageObj == null) {
                    definePackage(packageDotName, null, null,
                            null, null, null, null, null);
                } else {
                    if (packageObj.isSealed()) {
                        throw new SecurityException(Msg
                                .getString("K004c")); 
                    }
                }
            }
            return defineClass(origName, clBuf, 0, clBuf.length, new CodeSource(codeSourceUrl, (Certificate[]) null));
        }
        URL findResource(String name) {
            URL resURL = targetURL(url, name);
            if (resURL != null) {
                try {
                    URLConnection uc = resURL.openConnection();
                    uc.getInputStream().close();
                    if (!resURL.getProtocol().equals("http")) { 
                        return resURL;
                    }
                    int code;
                    if ((code = ((HttpURLConnection) uc).getResponseCode()) >= 200
                            && code < 300) {
                        return resURL;
                    }
                } catch (SecurityException e) {
                    return null;
                } catch (IOException e) {
                    return null;
                }
            }
            return null;
        }
        URL targetURL(URL base, String name) {
            try {
                String file = base.getFile() + URIEncoderDecoder.quoteIllegal(name,
                        "/@" + URI.someLegal);
                return new URL(base.getProtocol(), base.getHost(), base.getPort(),
                        file, null);
            } catch (UnsupportedEncodingException e) {
                return null;
            } catch (MalformedURLException e) {
                return null;
            }
        }
    }
    class URLJarHandler extends URLHandler {
        final JarFile jf;
        final String prefixName;
        final IndexFile index;
        final Map<URL, URLHandler> subHandlers = new HashMap<URL, URLHandler>();
        public URLJarHandler(URL url, URL jarURL, JarFile jf, String prefixName) {
            super(url);
            this.jf = jf;
            this.prefixName = prefixName;
            this.codeSourceUrl = jarURL;
            final JarEntry je = jf.getJarEntry("META-INF/INDEX.LIST"); 
            this.index = (je == null ? null : IndexFile.readIndexFile(jf, je, url));
        }
        public URLJarHandler(URL url, URL jarURL, JarFile jf, String prefixName, IndexFile index) {
            super(url);
            this.jf = jf;
            this.prefixName = prefixName;
            this.index = index;
            this.codeSourceUrl = jarURL;
        }
        IndexFile getIndex() {
            return index;
        }
        @Override
        void findResources(String name, ArrayList<URL> resources) {
            URL res = findResourceInOwn(name);
            if (res != null && !resources.contains(res)) {
                resources.add(res);
            }
            if (index != null) {
                int pos = name.lastIndexOf("/"); 
                String indexedName = (pos > 0) ? name.substring(0, pos) : name;
                ArrayList<URL> urls = index.get(indexedName);
                if (urls != null) {
                    urls.remove(url);
                    for (URL url : urls) {
                        URLHandler h = getSubHandler(url);
                        if (h != null) {
                            h.findResources(name, resources);
                        }
                    }
                }
            }
        }
        @Override
        Class<?> findClass(String packageName, String name, String origName) {
            String entryName = prefixName + name;
            JarEntry entry = jf.getJarEntry(entryName);
            if (entry != null) {
                try {
                    Manifest manifest = jf.getManifest();
                    return createClass(entry, manifest, packageName, origName);
                } catch (IOException e) {
                }
            }
            if (index != null) {
                ArrayList<URL> urls;
                if (packageName == null) {
                    urls = index.get(name);
                } else {
                    urls = index.get(packageName);
                }
                if (urls != null) {
                    urls.remove(url);
                    for (URL url : urls) {
                        URLHandler h = getSubHandler(url);
                        if (h != null) {
                            Class<?> res = h.findClass(packageName, name, origName);
                            if (res != null) {
                                return res;
                            }
                        }
                    }
                }
            }
            return null;
        }
        private Class<?> createClass(JarEntry entry, Manifest manifest, String packageName, String origName) {
            InputStream is = null;
            byte[] clBuf = null;
            try {
                is = jf.getInputStream(entry);
                clBuf = getBytes(is);
            } catch (IOException e) {
                return null;
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                }
            }
            if (packageName != null) {
                String packageDotName = packageName.replace('/', '.');
                Package packageObj = getPackage(packageDotName);
                if (packageObj == null) {
                    if (manifest != null) {
                        definePackage(packageDotName, manifest,
                                codeSourceUrl);
                    } else {
                        definePackage(packageDotName, null, null,
                                null, null, null, null, null);
                    }
                } else {
                    boolean exception = packageObj.isSealed();
                    if (manifest != null) {
                        if (isSealed(manifest, packageName + "/")) {
                            exception = !packageObj
                                    .isSealed(codeSourceUrl);
                        }
                    }
                    if (exception) {
                        throw new SecurityException(Msg
                                .getString("K0352", packageName)); 
                    }
                }
            }
            CodeSource codeS = new CodeSource(codeSourceUrl, entry.getCertificates());
            return defineClass(origName, clBuf, 0, clBuf.length, codeS);
        }
        URL findResourceInOwn(String name) {
            String entryName = prefixName + name;
            if (jf.getEntry(entryName) != null) {
                return targetURL(url, name);
            }
            return null;
        }
        @Override
        URL findResource(String name) {
            URL res = findResourceInOwn(name);
            if (res != null) {
                return res;
            }
            if (index != null) {
                int pos = name.lastIndexOf("/"); 
                String indexedName = (pos > 0) ? name.substring(0, pos) : name;
                ArrayList<URL> urls = index.get(indexedName);
                if (urls != null) {
                    urls.remove(url);
                    for (URL url : urls) {
                        URLHandler h = getSubHandler(url);
                        if (h != null) {
                            res = h.findResource(name);
                            if (res != null) {
                                return res;
                            }
                        }
                    }
                }
            }
            return null;
        }
        private synchronized URLHandler getSubHandler(URL url) {
            URLHandler sub = subHandlers.get(url);
            if (sub != null) {
                return sub;
            }
            String protocol = url.getProtocol();
            if (protocol.equals("jar")) { 
                sub = createURLJarHandler(url);
            } else if (protocol.equals("file")) { 
                sub = createURLSubJarHandler(url);
            } else {
                sub = createURLHandler(url);
            }
            if (sub != null) {
                subHandlers.put(url, sub);
            }
            return sub;
        }
        private URLHandler createURLSubJarHandler(URL url) {
            String prefixName;
            String file = url.getFile();
            if (url.getFile().endsWith("!/")) { 
                prefixName = "";
            } else {
                int sepIdx = file.lastIndexOf("!/"); 
                if (sepIdx == -1) {
                    return null;
                }
                sepIdx += 2;
                prefixName = file.substring(sepIdx);
            }
            try {
                URL jarURL = ((JarURLConnection) url
                        .openConnection()).getJarFileURL();
                JarURLConnection juc = (JarURLConnection) new URL(
                        "jar", "", 
                        jarURL.toExternalForm() + "!/").openConnection(); 
                JarFile jf = juc.getJarFile();
                URLJarHandler jarH = new URLJarHandler(url, jarURL, jf, prefixName, null);
                return jarH;
            } catch (IOException e) {
            }
            return null;
        }
    }
    class URLFileHandler extends URLHandler {
        private String prefix;
        public URLFileHandler(URL url) {
            super(url);
            String baseFile = url.getFile();
            String host = url.getHost();
            int hostLength = 0;
            if (host != null) {
                hostLength = host.length();
            }
            StringBuilder buf = new StringBuilder(2 + hostLength
                    + baseFile.length());
            if (hostLength > 0) {
                buf.append("
            }
            buf.append(baseFile);
            prefix = buf.toString();
        }
        @Override
        Class<?> findClass(String packageName, String name, String origName) {
            String filename = prefix + name;
            try {
                filename = URLDecoder.decode(filename, "UTF-8"); 
            } catch (IllegalArgumentException e) {
                return null;
            } catch (UnsupportedEncodingException e) {
                return null;
            }
            File file = new File(filename);
            if (file.exists()) {
                try {
                    InputStream is = new FileInputStream(file);
                    return createClass(is, packageName, origName);
                } catch (FileNotFoundException e) {
                }
            }
            return null;
        }
        @Override
        URL findResource(String name) {
            int idx = 0;
            String filename;
            while (idx < name.length() &&
                   ((name.charAt(idx) == '/') || (name.charAt(idx) == '\\'))) {
                idx++;
            }
            if (idx > 0) {
                name = name.substring(idx);
            }
            try {
                filename = URLDecoder.decode(prefix, "UTF-8") + name; 
                if (new File(filename).exists()) {
                    return targetURL(url, name);
                }
                return null;
            } catch (IllegalArgumentException e) {
                return null;
            } catch (UnsupportedEncodingException e) {
                throw new AssertionError(e);
            }
        }
    }
    public URLClassLoader(URL[] urls) {
        this(urls, ClassLoader.getSystemClassLoader(), null);
    }
    public URLClassLoader(URL[] urls, ClassLoader parent) {
        this(urls, parent, null);
    }
    protected void addURL(URL url) {
        try {
            originalUrls.add(url);
            searchList.add(createSearchURL(url));
        } catch (MalformedURLException e) {
        }
    }
    @Override
    public Enumeration<URL> findResources(final String name) throws IOException {
        if (name == null) {
            return null;
        }
        ArrayList<URL> result = AccessController.doPrivileged(
                new PrivilegedAction<ArrayList<URL>>() {
                    public ArrayList<URL> run() {
                        ArrayList<URL> results = new ArrayList<URL>();
                        findResourcesImpl(name, results);
                        return results;
                    }
                }, currentContext);
        SecurityManager sm;
        int length = result.size();
        if (length > 0 && (sm = System.getSecurityManager()) != null) {
            ArrayList<URL> reduced = new ArrayList<URL>(length);
            for (int i = 0; i < length; i++) {
                URL url = result.get(i);
                try {
                    sm.checkPermission(url.openConnection().getPermission());
                    reduced.add(url);
                } catch (IOException e) {
                } catch (SecurityException e) {
                }
            }
            result = reduced;
        }
        return Collections.enumeration(result);
    }
    void findResourcesImpl(String name, ArrayList<URL> result) {
        int n = 0;
        while (true) {
            URLHandler handler = getHandler(n++);
            if (handler == null) {
                break;
            }
            handler.findResources(name, result);
        }
    }
    private static byte[] getBytes(InputStream is)
            throws IOException {
        byte[] buf = new byte[4096];
        ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);
        int count;
        while ((count = is.read(buf)) > 0) {
            bos.write(buf, 0, count);
        }
        return bos.toByteArray();
    }
    @Override
    protected PermissionCollection getPermissions(final CodeSource codesource) {
        PermissionCollection pc = super.getPermissions(codesource);
        URL u = codesource.getLocation();
        if (u.getProtocol().equals("jar")) { 
            try {
                u = ((JarURLConnection) u.openConnection()).getJarFileURL();
            } catch (IOException e) {
            }
        }
        if (u.getProtocol().equals("file")) { 
            String path = u.getFile();
            String host = u.getHost();
            if (host != null && host.length() > 0) {
                path = "
            }
            if (File.separatorChar != '/') {
                path = path.replace('/', File.separatorChar);
            }
            if (isDirectory(u)) {
                pc.add(new FilePermission(path + "-", "read")); 
            } else {
                pc.add(new FilePermission(path, "read")); 
            }
        } else {
            String host = u.getHost();
            if (host.length() == 0) {
                host = "localhost"; 
            }
            pc.add(new SocketPermission(host, "connect, accept")); 
        }
        return pc;
    }
    public URL[] getURLs() {
        return originalUrls.toArray(new URL[originalUrls.size()]);
    }
    private static boolean isDirectory(URL url) {
        String file = url.getFile();
        return (file.length() > 0 && file.charAt(file.length() - 1) == '/');
    }
    public static URLClassLoader newInstance(final URL[] urls) {
        URLClassLoader sub = AccessController
                .doPrivileged(new PrivilegedAction<URLClassLoader>() {
                    public URLClassLoader run() {
                        return new SubURLClassLoader(urls);
                    }
                });
        sub.currentContext = AccessController.getContext();
        return sub;
    }
    public static URLClassLoader newInstance(final URL[] urls,
                                             final ClassLoader parentCl) {
        URLClassLoader sub = AccessController
                .doPrivileged(new PrivilegedAction<URLClassLoader>() {
                    public URLClassLoader run() {
                        return new SubURLClassLoader(urls, parentCl);
                    }
                });
        sub.currentContext = AccessController.getContext();
        return sub;
    }
    public URLClassLoader(URL[] searchUrls, ClassLoader parent,
                          URLStreamHandlerFactory factory) {
        super(parent);
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkCreateClassLoader();
        }
        this.factory = factory;
        currentContext = AccessController.getContext();
        int nbUrls = searchUrls.length;
        originalUrls = new ArrayList<URL>(nbUrls);
        handlerList = new ArrayList<URLHandler>(nbUrls);
        searchList = Collections.synchronizedList(new ArrayList<URL>(nbUrls));
        for (int i = 0; i < nbUrls; i++) {
            originalUrls.add(searchUrls[i]);
            try {
                searchList.add(createSearchURL(searchUrls[i]));
            } catch (MalformedURLException e) {
            }
        }
    }
    @Override
    protected Class<?> findClass(final String clsName)
            throws ClassNotFoundException {
        Class<?> cls = AccessController.doPrivileged(
                new PrivilegedAction<Class<?>>() {
                    public Class<?> run() {
                        return findClassImpl(clsName);
                    }
                }, currentContext);
        if (cls != null) {
            return cls;
        }
        throw new ClassNotFoundException(clsName);
    }
    private URL createSearchURL(URL url) throws MalformedURLException {
        if (url == null) {
            return url;
        }
        String protocol = url.getProtocol();
        if (isDirectory(url) || protocol.equals("jar")) { 
            return url;
        }
        if (factory == null) {
            return new URL("jar", "", 
                    -1, url.toString() + "!/"); 
        }
        return new URL("jar", "", 
                -1, url.toString() + "!/", 
                factory.createURLStreamHandler("jar"));
    }
    @Override
    public URL findResource(final String name) {
        if (name == null) {
            return null;
        }
        URL result = AccessController.doPrivileged(new PrivilegedAction<URL>() {
            public URL run() {
                return findResourceImpl(name);
            }
        }, currentContext);
        SecurityManager sm;
        if (result != null && (sm = System.getSecurityManager()) != null) {
            try {
                sm.checkPermission(result.openConnection().getPermission());
            } catch (IOException e) {
                return null;
            } catch (SecurityException e) {
                return null;
            }
        }
        return result;
    }
    URL findResourceImpl(String resName) {
        int n = 0;
        while (true) {
            URLHandler handler = getHandler(n++);
            if (handler == null) {
                break;
            }
            URL res = handler.findResource(resName);
            if (res != null) {
                return res;
            }
        }
        return null;
    }
    URLHandler getHandler(int num) {
        if (num < handlerList.size()) {
            return handlerList.get(num);
        }
        makeNewHandler();
        if (num < handlerList.size()) {
            return handlerList.get(num);
        }
        return null;
    }
    private synchronized void makeNewHandler() {
        while (!searchList.isEmpty()) {
            URL nextCandidate = searchList.remove(0);
            if (nextCandidate == null) {  
                throw new NullPointerException(Msg.getString("KA024")); 
            }
            if (!handlerMap.containsKey(nextCandidate)) {
                URLHandler result;
                String protocol = nextCandidate.getProtocol();
                if (protocol.equals("jar")) { 
                    result = createURLJarHandler(nextCandidate);
                } else if (protocol.equals("file")) { 
                    result = createURLFileHandler(nextCandidate);
                } else {
                    result = createURLHandler(nextCandidate);
                }
                if (result != null) {
                    handlerMap.put(nextCandidate, result);
                    handlerList.add(result);
                    return;
                }
            }
        }
    }
    private URLHandler createURLHandler(URL url) {
        return new URLHandler(url);
    }
    private URLHandler createURLFileHandler(URL url) {
        return new URLFileHandler(url);
    }
    private URLHandler createURLJarHandler(URL url) {
        String prefixName;
        String file = url.getFile();
        if (url.getFile().endsWith("!/")) { 
            prefixName = "";
        } else {
            int sepIdx = file.lastIndexOf("!/"); 
            if (sepIdx == -1) {
                return null;
            }
            sepIdx += 2;
            prefixName = file.substring(sepIdx);
        }
        try {
            URL jarURL = ((JarURLConnection) url
                    .openConnection()).getJarFileURL();
            JarURLConnection juc = (JarURLConnection) new URL(
                    "jar", "", 
                    jarURL.toExternalForm() + "!/").openConnection(); 
            JarFile jf = juc.getJarFile();
            URLJarHandler jarH = new URLJarHandler(url, jarURL, jf, prefixName);
            if (jarH.getIndex() == null) {
                try {
                    Manifest manifest = jf.getManifest();
                    if (manifest != null) {
                        String classpath = manifest.getMainAttributes().getValue(
                                Attributes.Name.CLASS_PATH);
                        if (classpath != null) {
                            searchList.addAll(0, getInternalURLs(url, classpath));
                        }
                    }
                } catch (IOException e) {
                }
            }
            return jarH;
        } catch (IOException e) {
        }
        return null;
    }
    protected Package definePackage(String packageName, Manifest manifest,
                                    URL url) throws IllegalArgumentException {
        Attributes mainAttributes = manifest.getMainAttributes();
        String dirName = packageName.replace('.', '/') + "/"; 
        Attributes packageAttributes = manifest.getAttributes(dirName);
        boolean noEntry = false;
        if (packageAttributes == null) {
            noEntry = true;
            packageAttributes = mainAttributes;
        }
        String specificationTitle = packageAttributes
                .getValue(Attributes.Name.SPECIFICATION_TITLE);
        if (specificationTitle == null && !noEntry) {
            specificationTitle = mainAttributes
                    .getValue(Attributes.Name.SPECIFICATION_TITLE);
        }
        String specificationVersion = packageAttributes
                .getValue(Attributes.Name.SPECIFICATION_VERSION);
        if (specificationVersion == null && !noEntry) {
            specificationVersion = mainAttributes
                    .getValue(Attributes.Name.SPECIFICATION_VERSION);
        }
        String specificationVendor = packageAttributes
                .getValue(Attributes.Name.SPECIFICATION_VENDOR);
        if (specificationVendor == null && !noEntry) {
            specificationVendor = mainAttributes
                    .getValue(Attributes.Name.SPECIFICATION_VENDOR);
        }
        String implementationTitle = packageAttributes
                .getValue(Attributes.Name.IMPLEMENTATION_TITLE);
        if (implementationTitle == null && !noEntry) {
            implementationTitle = mainAttributes
                    .getValue(Attributes.Name.IMPLEMENTATION_TITLE);
        }
        String implementationVersion = packageAttributes
                .getValue(Attributes.Name.IMPLEMENTATION_VERSION);
        if (implementationVersion == null && !noEntry) {
            implementationVersion = mainAttributes
                    .getValue(Attributes.Name.IMPLEMENTATION_VERSION);
        }
        String implementationVendor = packageAttributes
                .getValue(Attributes.Name.IMPLEMENTATION_VENDOR);
        if (implementationVendor == null && !noEntry) {
            implementationVendor = mainAttributes
                    .getValue(Attributes.Name.IMPLEMENTATION_VENDOR);
        }
        return definePackage(packageName, specificationTitle,
                specificationVersion, specificationVendor, implementationTitle,
                implementationVersion, implementationVendor, isSealed(manifest,
                dirName) ? url : null);
    }
    private boolean isSealed(Manifest manifest, String dirName) {
        Attributes mainAttributes = manifest.getMainAttributes();
        String value = mainAttributes.getValue(Attributes.Name.SEALED);
        boolean sealed = value != null && value.toLowerCase().equals("true"); 
        Attributes attributes = manifest.getAttributes(dirName);
        if (attributes != null) {
            value = attributes.getValue(Attributes.Name.SEALED);
            if (value != null) {
                sealed = value.toLowerCase().equals("true"); 
            }
        }
        return sealed;
    }
    private ArrayList<URL> getInternalURLs(URL root, String classpath) {
        StringTokenizer tokenizer = new StringTokenizer(classpath);
        ArrayList<URL> addedURLs = new ArrayList<URL>();
        String file = root.getFile();
        int jarIndex = file.lastIndexOf("!/") - 1; 
        int index = file.lastIndexOf("/", jarIndex) + 1; 
        if (index == 0) {
            index = file.lastIndexOf(
                    System.getProperty("file.separator"), jarIndex) + 1; 
        }
        file = file.substring(0, index);
        while (tokenizer.hasMoreElements()) {
            String element = tokenizer.nextToken();
            if (!element.equals("")) { 
                try {
                    URL url = new URL(new URL(file), element);
                    addedURLs.add(createSearchURL(url));
                } catch (MalformedURLException e) {
                }
            }
        }
        return addedURLs;
    }
    Class<?> findClassImpl(String className) {
        String partialName = className.replace('.', '/');
        final String classFileName = new StringBuilder(partialName).append(".class").toString(); 
        String packageName = null;
        int position = partialName.lastIndexOf('/');
        if ((position = partialName.lastIndexOf('/')) != -1) {
            packageName = partialName.substring(0, position);
        }
        int n = 0;
        while (true) {
            URLHandler handler = getHandler(n++);
            if (handler == null) {
                break;
            }
            Class<?> res = handler.findClass(packageName, classFileName, className);
            if (res != null) {
                return res;
            }
        }
        return null;
    }
}
