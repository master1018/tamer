public class PropertyNamesTest {
    public static void main(String[] args) throws Exception {
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        ObjectName pointName = new ObjectName("a:type=Point");
        PointMXBean pointmx = new PointImpl();
        mbs.registerMBean(pointmx, pointName);
        Point point = new Point(1, 2);
        PointMXBean pointproxy =
            JMX.newMXBeanProxy(mbs, pointName, PointMXBean.class);
        Point point1 = pointproxy.identity(point);
        if (point1.getX() != point.getX() || point1.getY() != point.getY())
            throw new Exception("Point doesn't match");
        System.out.println("Point test passed");
        ObjectName evolveName = new ObjectName("a:type=Evolve");
        EvolveMXBean evolvemx = new EvolveImpl();
        mbs.registerMBean(evolvemx, evolveName);
        Evolve evolve =
            new Evolve(59, "tralala", Collections.singletonList("tiddly"));
        EvolveMXBean evolveProxy =
            JMX.newMXBeanProxy(mbs, evolveName, EvolveMXBean.class);
        Evolve evolve1 = evolveProxy.identity(evolve);
        if (evolve1.getOldInt() != evolve.getOldInt()
                || !evolve1.getNewString().equals(evolve.getNewString())
                || !evolve1.getNewerList().equals(evolve.getNewerList()))
            throw new Exception("Evolve doesn't match");
        System.out.println("Evolve test passed");
        ObjectName evolvedName = new ObjectName("a:type=Evolved");
        EvolveMXBean evolvedmx = new EvolveImpl();
        mbs.registerMBean(evolvedmx, evolvedName);
        CompositeType evolvedType =
            new CompositeType("Evolved", "descr", new String[] {"oldInt"},
                              new String[] {"oldInt descr"},
                              new OpenType[] {SimpleType.INTEGER});
        CompositeData evolvedData =
            new CompositeDataSupport(evolvedType, new String[] {"oldInt"},
                                     new Object[] {5});
        CompositeData evolved1 = (CompositeData)
            mbs.invoke(evolvedName, "identity", new Object[] {evolvedData},
                       new String[] {CompositeData.class.getName()});
        if ((Integer) evolved1.get("oldInt") != 5
                || !evolved1.get("newString").equals("defaultString")
                || ((String[]) evolved1.get("newerList")).length != 0)
            throw new Exception("Evolved doesn't match: " + evolved1);
        System.out.println("Evolved test passed");
    }
    public static class Point {
        @ConstructorProperties({"x", "y"})
        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
        public int getY() {
            return y;
        }
        public int getX() {
            return x;
        }
        private final int x, y;
    }
    public static interface PointMXBean {
        Point identity(Point x);
    }
    public static class PointImpl implements PointMXBean {
        public Point identity(Point x) {
            return x;
        }
    }
    public static class Evolve {
        @ConstructorProperties({"oldInt"})
        public Evolve(int oldInt) {
            this(oldInt, "defaultString");
        }
        @ConstructorProperties({"oldInt", "newString"})
        public Evolve(int oldInt, String newString) {
            this(oldInt, newString, Collections.<String>emptyList());
        }
        @ConstructorProperties({"oldInt", "newString", "newerList"})
        public Evolve(int oldInt, String newString, List<String> newerList) {
            this.oldInt = oldInt;
            this.newString = newString;
            this.newerList = newerList;
        }
        public int getOldInt() {
            return oldInt;
        }
        public String getNewString() {
            return newString;
        }
        public List<String> getNewerList() {
            return newerList;
        }
        private final int oldInt;
        private final String newString;
        private final List<String> newerList;
    }
    public static interface EvolveMXBean {
        Evolve identity(Evolve x);
    }
    public static class EvolveImpl implements EvolveMXBean {
        public Evolve identity(Evolve x) {
            return x;
        }
    }
}
