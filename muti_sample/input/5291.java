public class LoadCounts {
    private static ClassLoadingMXBean mbean
        = ManagementFactory.getClassLoadingMXBean();
    public static void main(String argv[]) throws Exception {
        int classesNowPrev = mbean.getLoadedClassCount();
        long classesTotalPrev = mbean.getTotalLoadedClassCount();
        System.out.println("Loading 4 classes with the system class loader");
        new SimpleOne();
        new SimpleTwo();
        new Chain();
        int classesNow = mbean.getLoadedClassCount();
        long classesTotal = mbean.getTotalLoadedClassCount();
        if (classesNow > classesTotal)
            throw new RuntimeException("getLoadedClassCount() > "
                                     + "getTotalLoadedClassCount()");
        if (classesNowPrev + 4 != classesNow)
            throw new RuntimeException("Number of loaded classes is "
                                     + (classesNowPrev + 4) + ", but "
                                     + "MBean.getLoadedClassCount() returned "
                                     + classesNow);
        if (classesTotalPrev + 4 != classesTotal)
            throw new RuntimeException("Total number of loaded classes is "
                                     + (classesTotalPrev + 4) + ", but "
                                     + "MBean.getTotalLoadedClassCount() "
                                     + "returned " + classesTotal);
        System.out.println("Creating new class loader instances");
        LeftHand leftHand = new LeftHand();
        RightHand rightHand = new RightHand();
        LoaderForTwoInstances ins1 = new LoaderForTwoInstances();
        LoaderForTwoInstances ins2 = new LoaderForTwoInstances();
        System.out.println("Loading 2 class instances; each by " +
                           "2 initiating class loaders.");
        classesNowPrev = mbean.getLoadedClassCount();
        classesTotalPrev = mbean.getTotalLoadedClassCount();
        try {
            Class.forName("Body", true, leftHand);
            Class.forName("Body", true, rightHand);
            Class.forName("TheSameClass", true, ins1);
            Class.forName("TheSameClass", true, ins2);
        } catch (ClassNotFoundException e) {
            System.out.println("Unexpected excetion " + e);
            e.printStackTrace(System.out);
            throw new RuntimeException();
        }
        classesNow = mbean.getLoadedClassCount();
        classesTotal = mbean.getTotalLoadedClassCount();
        if (classesNowPrev + 2 != classesNow)
            throw new RuntimeException("Expected Number of loaded classes is "
                                     + (classesNowPrev + 4) + ", but "
                                     + "MBean.getLoadedClassCount() returned "
                                     + classesNow);
        if (classesTotalPrev + 2 != classesTotal)
            throw new RuntimeException("Total number of loaded classes is "
                                     + (classesTotalPrev + 4) + ", but "
                                     + "MBean.getTotalLoadedClassCount() "
                                     + "returned " + classesTotal);
        System.out.println("Test passed.");
    }
}
class SimpleOne {}
class SimpleTwo {}
class Chain {
    Slave slave = new Slave();
}
class Slave {}
class LeftHand extends ClassLoader {
    public LeftHand() {
        super(LeftHand.class.getClassLoader());
    }
}
class RightHand extends ClassLoader {
    public RightHand() {
        super(RightHand.class.getClassLoader());
    }
}
class Body {}
class LoaderForTwoInstances extends ClassLoader {
    public LoaderForTwoInstances() {
        super(LoaderForTwoInstances.class.getClassLoader());
    }
}
class TheSameClass {}
