public class GetClassLoadingTime {
    private static HotspotClassLoadingMBean mbean =
        (HotspotClassLoadingMBean)ManagementFactoryHelper.getHotspotClassLoadingMBean();
    private static final long MIN_TIME_FOR_PASS = 1;
    private static final long MAX_TIME_FOR_PASS = Long.MAX_VALUE;
    private static boolean trace = false;
    public static void main(String args[]) throws Exception {
        if (args.length > 0 && args[0].equals("trace")) {
            trace = true;
        }
        long time = mbean.getClassLoadingTime();
        if (trace) {
            System.out.println("Class loading time (ms): " + time);
        }
        if (time < MIN_TIME_FOR_PASS || time > MAX_TIME_FOR_PASS) {
            throw new RuntimeException("Class loading time " +
                                       "illegal value: " + time + " ms " +
                                       "(MIN = " + MIN_TIME_FOR_PASS + "; " +
                                       "MAX = " + MAX_TIME_FOR_PASS + ")");
        }
        for (int i = 0; i < 1000; i++) {
            Class.forName("ClassToLoad0", true, new KlassLoader());
        }
        long time2 = mbean.getClassLoadingTime();
        long count = mbean.getLoadedClassCount();
        if (trace) {
            System.out.println("(new count is " + count + ")");
            System.out.println("Class loading time2 (ms): " + time2);
        }
        if (time2 <= time) {
            throw new RuntimeException("Class loading time " +
                                       "did not increase when class loaded" +
                                       "(time = " + time + "; " +
                                       "time2 = " + time2 + ")");
        }
        System.out.println("Test passed.");
    }
}
class KlassLoader extends ClassLoader {
  static String klassDir="";
  static int index=0;
  public KlassLoader() {
      super(null);
  }
  protected synchronized Class findClass(String name)
                        throws ClassNotFoundException {
        String cname = klassDir
            + (klassDir == "" ? "" : "/")
            +name.replace('.', '/')
            +".class";
        FileInputStream in;
        try {
                in=new FileInputStream(cname);
                if (in == null) {
                        throw new ClassNotFoundException("getResourceAsStream("
                                +cname+")");
                }
        } catch(java.io.FileNotFoundException e ) {
                throw new ClassNotFoundException("getResourceAsStream("
                        +cname+") : "
                        +e);
        }
        int len;
        byte data[];
        try {
                len = in.available();
                data = new byte[len];
                for (int total = 0; total < data.length; ) {
                        total += in.read(data, total, data.length - total);
                }
        } catch (IOException e) {
                throw new ClassNotFoundException(cname, e);
        } finally {
                try {
                        in.close();
                } catch (IOException e) {
                }
        }
        return defineClass(name, data, 0, data.length);
  }
}
