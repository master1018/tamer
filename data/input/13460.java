public class InterruptibleZip {
    private static String rtJar() {
        String bcp = System.getProperty("sun.boot.class.path");
        for (String pathElement : bcp.split(File.pathSeparator)) {
            if (pathElement.endsWith(File.separator + "rt.jar") &&
                new File(pathElement).exists()) {
                System.out.println("rtJar="+pathElement);
                return pathElement;
            }
            if (pathElement.endsWith(File.separator + "classes") &&
                new File(pathElement).isDirectory()) {
                System.out.println("rt.jar not available");
                return null;
            }
        }
        throw new Error("Can't find rt.jar or classes directory");
    }
    public static void main(String[] args) throws Exception {
        String rtJar = rtJar();
        if (rtJar == null) return;
        Thread.currentThread().interrupt();
        ZipFile zf = new ZipFile(rtJar);
        ZipEntry ze = zf.getEntry("java/lang/Object.class");
        InputStream is = zf.getInputStream(ze);
        byte[] buf = new byte[512];
        int n = is.read(buf);
        boolean interrupted = Thread.interrupted();
        System.out.printf("interrupted=%s n=%d name=%s%n",
                          interrupted, n, ze.getName());
        if (! interrupted) {
            throw new Error("Wrong interrupt status");
        }
        if (n != buf.length) {
            throw new Error("Read error");
        }
    }
}
