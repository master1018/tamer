public class DocFile
{
    private static final Pattern LINE = Pattern.compile("(.*)[\r]?\n",
                                                        Pattern.MULTILINE);
    private static final Pattern PROP = Pattern.compile("([^=]+)=(.*)");
    public static String readFile(String filename)
    {
        try {
            File f = new File(filename);
            int length = (int)f.length();
            FileInputStream is = new FileInputStream(f);
            InputStreamReader reader = new InputStreamReader(is, "UTF-8");
            char[] buf = new char[length];
            int index = 0;
            int amt;
            while (true) {
                amt = reader.read(buf, index, length-index);
                if (amt < 1) {
                    break;
                }
                index += amt;
            }
            return new String(buf, 0, index);
        }
        catch (IOException e) {
            return null;
        }
    }
    public static void writePage(String docfile, String relative,
                                    String outfile)
    {
        HDF hdf = DroidDoc.makeHDF();
        String filedata = readFile(docfile);
        int start = -1;
        int lineno = 1;
        Matcher lines = LINE.matcher(filedata);
        String line = null;
        while (lines.find()) {
            line = lines.group(1);
            if (line.length() > 0) {
                if (line.equals("@jd:body")) {
                    start = lines.end();
                    break;
                }
                Matcher prop = PROP.matcher(line);
                if (prop.matches()) {
                    String key = prop.group(1);
                    String value = prop.group(2);
                    hdf.setValue(key, value);
                } else {
                    break;
                }
            }
            lineno++;
        }
        if (start < 0) {
            System.err.println(docfile + ":" + lineno + ": error parsing docfile");
            if (line != null) {
                System.err.println(docfile + ":" + lineno + ":" + line);
            }
            System.exit(1);
        }
        String fromTemplate = hdf.getValue("template.which", "");
        String fromPage = hdf.getValue("page.onlyfortemplate", "");
        if (!"".equals(fromPage) && !fromTemplate.equals(fromPage)) {
            return;
        }
        String commentText = filedata.substring(start);
        Comment comment = new Comment(commentText, null,
                                    new SourcePositionInfo(docfile, lineno, 1));
        TagInfo[] tags = comment.tags();
        TagInfo.makeHDF(hdf, "root.descr", tags);
        hdf.setValue("commentText", commentText);
        String fromWhichmodule = hdf.getValue("android.whichmodule", "");
        if (fromWhichmodule.equals("online-pdk")) {
            hdf.setValue("online-pdk", "true");
            ClearPage.write(hdf, "docpage.cs", outfile);
        } else {
            if (outfile.indexOf("sdk/") != -1) {
                hdf.setValue("sdk", "true");
                if ((outfile.indexOf("index.html") != -1) || (outfile.indexOf("features.html") != -1)) {
                    ClearPage.write(hdf, "sdkpage.cs", outfile);
                } else {
                    ClearPage.write(hdf, "docpage.cs", outfile);
                }
            } else if (outfile.indexOf("guide/") != -1) {
                hdf.setValue("guide", "true");
                ClearPage.write(hdf, "docpage.cs", outfile);
            } else if (outfile.indexOf("resources/") != -1) {
                hdf.setValue("resources", "true");
                ClearPage.write(hdf, "docpage.cs", outfile);
            } else {
                ClearPage.write(hdf, "nosidenavpage.cs", outfile);
            }
        }
    } 
}
