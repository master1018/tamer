public class MonkeyRecorder {
  private static String mXmlFilename;
  private static FileWriter mXmlFile;
  private static XMLWriter mXmlWriter;
  private static String mDirname;
  private static List<String> mScreenShotNames = new ArrayList<String>();
  private static final String ROOT_DIR = "out";
  private static final SimpleDateFormat SIMPLE_DATE_TIME_FORMAT =
      new SimpleDateFormat("yyyyMMdd-HH:mm:ss");
  public MonkeyRecorder(String scriptName, String version) throws IOException {
    File scriptFile = new File(scriptName);
    scriptName = scriptFile.getName();  
    mDirname = ROOT_DIR + "/" + stripType(scriptName) + "-" + now();
    new File(mDirname).mkdirs();
    mXmlFilename = stampFilename(stripType(scriptName) + ".xml");
    initXmlFile(scriptName, version);
  }
  private static String now() {
    return SIMPLE_DATE_TIME_FORMAT.format(Calendar.getInstance().getTime());     
  }
  private static void initXmlFile(String scriptName, String version) throws IOException {
    String[] names = new String[] { "script_name", "monkeyRunnerVersion" };
    String[] values = new String[] { scriptName, version };
    mXmlFile = new FileWriter(mDirname + "/" + mXmlFilename);
    mXmlWriter = new XMLWriter(mXmlFile);
    mXmlWriter.begin();
    mXmlWriter.comment("Monkey Script Results");
    mXmlWriter.start("script_run", names, values, names.length);
  }
  public static void addComment(String comment) throws IOException {
    mXmlWriter.comment(comment);
  }
  public static void startCommand() throws IOException {
    mXmlWriter.start("command", "dateTime", now());
  }
  public static void addInput(String cmd)  throws IOException {
    String name = "cmd";
    String value = cmd;
    mXmlWriter.tag("input", name, value);
  }
  public static void addResult(String result, String filename) throws IOException {
    int num_args = 2;
    String[] names = new String[3];
    String[] values = new String[3];
    names[0] = "result";
    values[0] = result;
    names[1] = "dateTime";
    values[1] = now();
    if (filename.length() != 0) {
      names[2] = "screenshot";
      values[2] = stampFilename(filename); 
      addScreenShot(filename);
      num_args = 3;
    }
    mXmlWriter.tag("response", names, values, num_args); 
  }
  public static void addAttribute(String name, String value) throws IOException {
    mXmlWriter.addAttribute(name, value);
  }
  public static void addDeviceVar(String name, String value) throws IOException {
    String[] names = {"name", "value"};
    String[] values = {name, value};
    mXmlWriter.tag("device_var", names, values, names.length);
  }
  private static void addScreenShot(String filename) {
    File file = new File(filename);
    String screenShotName = stampFilename(filename);
    file.renameTo(new File(mDirname, screenShotName));
    mScreenShotNames.add(screenShotName);
  }
  public static void endCommand() throws IOException {
    mXmlWriter.end();
  }
  private static String stampFilename(String filename) {
    int typeIndex = filename.lastIndexOf('.');
    if (typeIndex == -1) {
      return filename + "-" + now();
    }  
    return filename.substring(0, typeIndex) + "-" + now() + filename.substring(typeIndex);
  }
   private static String stripType(String filename) {
    int typeIndex = filename.lastIndexOf('.');
    if (typeIndex == -1)
      return filename;
    return filename.substring(0, typeIndex);
  }
  public static void close() throws IOException {
    byte[] buf = new byte[1024];
    String zipFileName = mXmlFilename + ".zip";
    endCommand();
    mXmlFile.close();
    FileOutputStream zipFile = new FileOutputStream(ROOT_DIR + "/" + zipFileName);
    ZipOutputStream out = new ZipOutputStream(zipFile);
    addFileToZip(out, mDirname + "/" + mXmlFilename, buf);
    for (String filename : mScreenShotNames) {
      addFileToZip(out, mDirname + "/" + filename, buf);
    }
    out.close();
  }
  private static void addFileToZip(ZipOutputStream zip, String filepath, byte[] buf) throws IOException {
    FileInputStream in = new FileInputStream(filepath);
    zip.putNextEntry(new ZipEntry(filepath));
    int len;
    while ((len = in.read(buf)) > 0) {
      zip.write(buf, 0, len);
    }
    zip.closeEntry();
    in.close();
  }
}
