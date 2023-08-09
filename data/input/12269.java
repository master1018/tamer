public abstract class StreamPrintServiceFactory {
    static class Services {
        private ArrayList listOfFactories = null;
    }
    private static Services getServices() {
        Services services =
            (Services)AppContext.getAppContext().get(Services.class);
        if (services == null) {
            services = new Services();
            AppContext.getAppContext().put(Services.class, services);
        }
        return services;
    }
    private static ArrayList getListOfFactories() {
        return getServices().listOfFactories;
    }
    private static ArrayList initListOfFactories() {
        ArrayList listOfFactories = new ArrayList();
        getServices().listOfFactories = listOfFactories;
        return listOfFactories;
    }
     public static StreamPrintServiceFactory[]
         lookupStreamPrintServiceFactories(DocFlavor flavor,
                                           String outputMimeType) {
         ArrayList list = getFactories(flavor, outputMimeType);
         return (StreamPrintServiceFactory[])
               (list.toArray(new StreamPrintServiceFactory[list.size()]));
     }
    public abstract String getOutputFormat();
    public abstract DocFlavor[] getSupportedDocFlavors();
    public abstract StreamPrintService getPrintService(OutputStream out);
    private static ArrayList getAllFactories() {
        synchronized (StreamPrintServiceFactory.class) {
          ArrayList listOfFactories = getListOfFactories();
            if (listOfFactories != null) {
                return listOfFactories;
            } else {
                listOfFactories = initListOfFactories();
            }
            try {
                java.security.AccessController.doPrivileged(
                     new java.security.PrivilegedExceptionAction() {
                        public Object run() {
                            Iterator<StreamPrintServiceFactory> iterator =
                                ServiceLoader.load
                                (StreamPrintServiceFactory.class).iterator();
                            ArrayList lof = getListOfFactories();
                            while (iterator.hasNext()) {
                                try {
                                    lof.add(iterator.next());
                                }  catch (ServiceConfigurationError err) {
                                    if (System.getSecurityManager() != null) {
                                        err.printStackTrace();
                                    } else {
                                        throw err;
                                    }
                                }
                            }
                            return null;
                        }
                });
            } catch (java.security.PrivilegedActionException e) {
            }
            return listOfFactories;
        }
    }
    private static boolean isMember(DocFlavor flavor, DocFlavor[] flavors) {
        for (int f=0; f<flavors.length; f++ ) {
            if (flavor.equals(flavors[f])) {
                return true;
            }
        }
        return false;
    }
    private static ArrayList getFactories(DocFlavor flavor, String outType) {
        if (flavor == null && outType == null) {
            return getAllFactories();
        }
        ArrayList list = new ArrayList();
        Iterator iterator = getAllFactories().iterator();
        while (iterator.hasNext()) {
            StreamPrintServiceFactory factory =
                (StreamPrintServiceFactory)iterator.next();
            if ((outType == null ||
                 outType.equalsIgnoreCase(factory.getOutputFormat())) &&
                (flavor == null ||
                 isMember(flavor, factory.getSupportedDocFlavors()))) {
                list.add(factory);
            }
        }
        return list;
    }
}
