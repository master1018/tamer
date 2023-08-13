public class Support_Resources {
    public static final String RESOURCE_PACKAGE = "/tests/resources/";
    public static final String RESOURCE_PACKAGE_NAME = "tests.resources";
    public static InputStream getStream(String name) {
        return Support_Resources.class.getResourceAsStream(RESOURCE_PACKAGE
                + name);
    }
    public static String getURL(String name) {
        String folder = null;
        String fileName = name;
        File resources = createTempFolder();
        int index = name.lastIndexOf("/");
        if (index != -1) {
            folder = name.substring(0, index);
            name = name.substring(index + 1);
        }
        copyFile(resources, folder, name);
        URL url = null;
        String resPath = resources.toString();
        if (resPath.charAt(0) == '/' || resPath.charAt(0) == '\\') {
            resPath = resPath.substring(1);
        }
        try {
            url = new URL("file:/" + resPath + "/" + fileName);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url.toString();
    }
    public static File createTempFolder() {
        File folder = null;
        try {
            folder = File.createTempFile("hyts_resources", "", null);
            folder.delete();
            folder.mkdirs();
        } catch (IOException e) {
            e.printStackTrace();
        }
        folder.deleteOnExit();
        return folder;
    }
    public static File copyFile(File root, String folder, String file) {
        File f;
        if (folder != null) {
            f = new File(root.toString() + "/" + folder);
            if (!f.exists()) {
                f.mkdirs();
                f.deleteOnExit();
            }
        } else {
            f = root;
        }
        File dest = new File(f.toString() + "/" + file);
        InputStream in = Support_Resources.getStream(folder == null ? file
                : folder + "/" + file);
        try {
            copyLocalFileto(dest, in);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dest;
    }
    public static File createTempFile(String suffix) throws IOException {
        return File.createTempFile("hyts_", suffix, null);
    }
    public static void copyLocalFileto(File dest, InputStream in)
            throws FileNotFoundException, IOException {
        if (!dest.exists()) {
            FileOutputStream out = new FileOutputStream(dest);
            int result;
            byte[] buf = new byte[4096];
            while ((result = in.read(buf)) != -1) {
                out.write(buf, 0, result);
            }
            in.close();
            out.close();
            dest.deleteOnExit();
        }
    }
    public static File getExternalLocalFile(String url) throws IOException,
            MalformedURLException {
        File resources = createTempFolder();
        InputStream in = new URL(url).openStream();
        File temp = new File(resources.toString() + "/local.tmp");
        copyLocalFileto(temp, in);
        return temp;
    }
    public static String getResourceURL(String resource) {
        return "http:
    }
    public static InputStream getResourceStream(String name) {
        InputStream is = Support_Resources.class.getResourceAsStream(name);
        if (is == null) {
            name = RESOURCE_PACKAGE + name;
            is = Support_Resources.class.getResourceAsStream(name);
            if (is == null) {
                throw new RuntimeException("Failed to load resource: " + name);
            }
        }
        return is;
    }
    public static int writeResourceToStream(String name, OutputStream out) {
        InputStream input = getResourceStream(name);
        byte[] buffer = new byte[512];
        int total = 0;
        int count;
        try {
            count = input.read(buffer);
            while (count != -1) {
                out.write(buffer, 0, count);
                total = total + count;
                count = input.read(buffer);
            }
            return total;
        } catch (IOException e) {
            throw new RuntimeException("Failed to write to passed stream.", e);
        }
    }
    public static String getAbsoluteResourcePath(String name) {
        URL url = ClassLoader.getSystemClassLoader().getResource(name);
        if (url == null) {
            throw new RuntimeException("Failed to load resource: " + name);
        }
        try {
            return new File(url.toURI()).getAbsolutePath();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to load resource: " + name);
        }
    }
    public static File resourceToTempFile(String path) throws IOException {
        File f = File.createTempFile("out", ".xml");
        f.deleteOnExit();
        FileOutputStream out = new FileOutputStream(f);
        InputStream xml = Support_Resources.class.getResourceAsStream(path);
        int b;
        while ((b = xml.read()) != -1) {
            out.write(b);
        }
        out.flush();
        out.close();
        xml.close();
        return f;
    }
}
