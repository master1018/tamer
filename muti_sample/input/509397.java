public class SampleCode {
    String mSource;
    String mDest;
    String mTitle;
    public SampleCode(String source, String dest, String title) {
        mSource = source;
        mTitle = title;
        int len = dest.length();
        if (len > 1 && dest.charAt(len-1) != '/') {
            mDest = dest + '/';
        } else {
            mDest = dest;
        }
    }
    public void write(boolean offlineMode) {
        File f = new File(mSource);
        if (!f.isDirectory()) {
            System.out.println("-samplecode not a directory: " + mSource);
            return;
        }
        if (offlineMode) writeIndexOnly(f, mDest, offlineMode);
        else writeDirectory(f, mDest);
    }
    public static String convertExtension(String s, String ext) {
        return s.substring(0, s.lastIndexOf('.')) + ext;
    }
    public static String[] IMAGES = { ".png", ".jpg", ".gif" };
    public static String[] TEMPLATED = { ".java", ".xml" };
    public static boolean inList(String s, String[] list) {
        for (String t: list) {
            if (s.endsWith(t)) {
                return true;
            }
        }
        return false;
    }
    public void writeDirectory(File dir, String relative) {
        TreeSet<String> dirs = new TreeSet<String>();
        TreeSet<String> files = new TreeSet<String>();
        String subdir = relative; 
        for (File f: dir.listFiles()) {
            String name = f.getName();
            if (name.startsWith(".") || name.startsWith("_")) {
                continue;
            }
            if (f.isFile()) {
                String out = relative + name;
                if (inList(out, IMAGES)) {
                    ClearPage.copyFile(f, out);
                    writeImagePage(f, convertExtension(out, DroidDoc.htmlExtension), subdir);
                    files.add(name);
                }
                if (inList(out, TEMPLATED)) {
                    ClearPage.copyFile(f, out);
                    writePage(f, convertExtension(out, DroidDoc.htmlExtension), subdir);
                    files.add(name);
                }
            }
            else if (f.isDirectory()) {
                writeDirectory(f, relative + name + "/");
                dirs.add(name);
            }
        }
        int i;
        HDF hdf = writeIndex(dir);
        hdf.setValue("subdir", subdir);
        i=0;
        for (String d: dirs) {
            hdf.setValue("subdirs." + i + ".name", d);
            i++;
        }
        i=0;
        for (String f: files) {
            hdf.setValue("files." + i + ".name", f);
            hdf.setValue("files." + i + ".href", convertExtension(f, ".html"));
            i++;
        }
        ClearPage.write(hdf, "sampleindex.cs", relative + "/index" + DroidDoc.htmlExtension);
    }
    public void writeIndexOnly(File dir, String relative, Boolean offline) {
        HDF hdf = writeIndex(dir);
        if (!offline) relative = "/" + relative;
        ClearPage.write(hdf, "sampleindex.cs", relative + "index" +
                        DroidDoc.htmlExtension);
    }
    public HDF writeIndex(File dir) {
        HDF hdf = DroidDoc.makeHDF();
        hdf.setValue("page.title", dir.getName() + " - " + mTitle);
        hdf.setValue("projectTitle", mTitle);
        String filename = dir.getPath() + "/_index.html";
        String summary = SampleTagInfo.readFile(new SourcePositionInfo(filename,
                          -1,-1), filename, "sample code", true, false, true);
        if (summary == null) {
            summary = "";
        }
        hdf.setValue("summary", summary);
        return hdf;
    }
    public void writePage(File f, String out, String subdir) {
        String name = f.getName();
        String filename = f.getPath();
        String data = SampleTagInfo.readFile(new SourcePositionInfo(filename, -1,-1), filename,
                                                "sample code", true, true, true);
        data = DroidDoc.escape(data);
        HDF hdf = DroidDoc.makeHDF();
        hdf.setValue("page.title", name);
        hdf.setValue("subdir", subdir);
        hdf.setValue("realFile", name);
        hdf.setValue("fileContents", data);
        ClearPage.write(hdf, "sample.cs", out);
    }
    public void writeImagePage(File f, String out, String subdir) {
        String name = f.getName();
        String data = "<img src=\"" + name + "\" title=\"" + name + "\" />";
        HDF hdf = DroidDoc.makeHDF();
        hdf.setValue("page.title", name);
        hdf.setValue("subdir", subdir);
        hdf.setValue("realFile", name);
        hdf.setValue("fileContents", data);
        ClearPage.write(hdf, "sample.cs", out);
    }
}
