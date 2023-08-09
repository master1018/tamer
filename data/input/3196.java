public class PlatformInfo {
  public static String getOS() throws UnsupportedPlatformException {
    String os = System.getProperty("os.name");
    if (os.equals("SunOS")) {
      return "solaris";
    } else if (os.equals("Linux")) {
      return "linux";
    } else if (os.startsWith("Windows")) {
      return "win32";
    } else {
      throw new UnsupportedPlatformException("Operating system " + os + " not yet supported");
    }
  }
  public static String getCPU() throws UnsupportedPlatformException {
    String cpu = System.getProperty("os.arch");
    if (cpu.equals("i386")) {
      return "x86";
    } else if (cpu.equals("sparc") || cpu.equals("x86") || cpu.equals("ia64")) {
      return cpu;
    } else if (cpu.equals("sparcv9")) {
      return "sparc";
    } else if (cpu.equals("x86_64") || cpu.equals("amd64")) {
      return "amd64";
    } else {
      throw new UnsupportedPlatformException("CPU type " + cpu + " not yet supported");
    }
  }
  public static void main(String[] args) {
    System.out.println(getOS());
  }
}
