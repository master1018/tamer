public class ManyFiles {
    static int count;
    static List files = new ArrayList();
    static List streams = new ArrayList();
    static int NUM_FILES = 2050;
    public static void main(String args[]) throws Exception {
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Linux")||osName.startsWith("SunOS"))
            return;
        for (int n = 0; n < NUM_FILES; n++) {
            File f = new File("file" + count++);
            files.add(f);
            streams.add(new FileOutputStream(f));
        }
        Iterator i = streams.iterator();
        while(i.hasNext()) {
            FileOutputStream fos = (FileOutputStream)i.next();
            fos.close();
        }
        i = files.iterator();
        while(i.hasNext()) {
            File f = (File)i.next();
            f.delete();
        }
    }
}
