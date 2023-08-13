public abstract class PrintServiceLookup {
    static class Services {
        private ArrayList listOfLookupServices = null;
        private ArrayList registeredServices = null;
    }
    private static Services getServicesForContext() {
        Services services =
            (Services)AppContext.getAppContext().get(Services.class);
        if (services == null) {
            services = new Services();
            AppContext.getAppContext().put(Services.class, services);
        }
        return services;
    }
    private static ArrayList getListOfLookupServices() {
        return getServicesForContext().listOfLookupServices;
    }
    private static ArrayList initListOfLookupServices() {
        ArrayList listOfLookupServices = new ArrayList();
        getServicesForContext().listOfLookupServices = listOfLookupServices;
        return listOfLookupServices;
    }
    private static ArrayList getRegisteredServices() {
        return getServicesForContext().registeredServices;
    }
    private static ArrayList initRegisteredServices() {
        ArrayList registeredServices = new ArrayList();
        getServicesForContext().registeredServices = registeredServices;
        return registeredServices;
    }
    public static final PrintService[]
        lookupPrintServices(DocFlavor flavor,
                            AttributeSet attributes) {
        ArrayList list = getServices(flavor, attributes);
        return (PrintService[])(list.toArray(new PrintService[list.size()]));
    }
    public static final MultiDocPrintService[]
        lookupMultiDocPrintServices(DocFlavor[] flavors,
                                    AttributeSet attributes) {
        ArrayList list = getMultiDocServices(flavors, attributes);
        return (MultiDocPrintService[])
            list.toArray(new MultiDocPrintService[list.size()]);
    }
    public static final PrintService lookupDefaultPrintService() {
        Iterator psIterator = getAllLookupServices().iterator();
        while (psIterator.hasNext()) {
            try {
                PrintServiceLookup lus = (PrintServiceLookup)psIterator.next();
                PrintService service = lus.getDefaultPrintService();
                if (service != null) {
                    return service;
                }
            } catch (Exception e) {
            }
        }
        return null;
    }
    public static boolean registerServiceProvider(PrintServiceLookup sp) {
        synchronized (PrintServiceLookup.class) {
            Iterator psIterator = getAllLookupServices().iterator();
            while (psIterator.hasNext()) {
                try {
                    Object lus = psIterator.next();
                    if (lus.getClass() == sp.getClass()) {
                        return false;
                    }
                } catch (Exception e) {
                }
            }
            getListOfLookupServices().add(sp);
            return true;
        }
    }
    public static boolean registerService(PrintService service) {
        synchronized (PrintServiceLookup.class) {
            if (service instanceof StreamPrintService) {
                return false;
            }
            ArrayList registeredServices = getRegisteredServices();
            if (registeredServices == null) {
                registeredServices = initRegisteredServices();
            }
            else {
              if (registeredServices.contains(service)) {
                return false;
              }
            }
            registeredServices.add(service);
            return true;
        }
    }
    public abstract PrintService[] getPrintServices(DocFlavor flavor,
                                                    AttributeSet attributes);
    public abstract PrintService[] getPrintServices() ;
    public abstract MultiDocPrintService[]
        getMultiDocPrintServices(DocFlavor[] flavors,
                                 AttributeSet attributes);
    public abstract PrintService getDefaultPrintService();
    private static ArrayList getAllLookupServices() {
        synchronized (PrintServiceLookup.class) {
            ArrayList listOfLookupServices = getListOfLookupServices();
            if (listOfLookupServices != null) {
                return listOfLookupServices;
            } else {
                listOfLookupServices = initListOfLookupServices();
            }
            try {
                java.security.AccessController.doPrivileged(
                     new java.security.PrivilegedExceptionAction() {
                        public Object run() {
                            Iterator<PrintServiceLookup> iterator =
                                ServiceLoader.load(PrintServiceLookup.class).
                                iterator();
                            ArrayList los = getListOfLookupServices();
                            while (iterator.hasNext()) {
                                try {
                                    los.add(iterator.next());
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
            return listOfLookupServices;
        }
    }
    private static ArrayList getServices(DocFlavor flavor,
                                         AttributeSet attributes) {
        ArrayList listOfServices = new ArrayList();
        Iterator psIterator = getAllLookupServices().iterator();
        while (psIterator.hasNext()) {
            try {
                PrintServiceLookup lus = (PrintServiceLookup)psIterator.next();
                PrintService[] services=null;
                if (flavor == null && attributes == null) {
                    try {
                    services = lus.getPrintServices();
                    } catch (Throwable tr) {
                    }
                } else {
                    services = lus.getPrintServices(flavor, attributes);
                }
                if (services == null) {
                    continue;
                }
                for (int i=0; i<services.length; i++) {
                    listOfServices.add(services[i]);
                }
            } catch (Exception e) {
            }
        }
        ArrayList registeredServices = null;
        try {
          SecurityManager security = System.getSecurityManager();
          if (security != null) {
            security.checkPrintJobAccess();
          }
          registeredServices = getRegisteredServices();
        } catch (SecurityException se) {
        }
        if (registeredServices != null) {
            PrintService[] services = (PrintService[])
                registeredServices.toArray(
                           new PrintService[registeredServices.size()]);
            for (int i=0; i<services.length; i++) {
                if (!listOfServices.contains(services[i])) {
                    if (flavor == null && attributes == null) {
                        listOfServices.add(services[i]);
                    } else if (((flavor != null &&
                                 services[i].isDocFlavorSupported(flavor)) ||
                                flavor == null) &&
                               null == services[i].getUnsupportedAttributes(
                                                      flavor, attributes)) {
                        listOfServices.add(services[i]);
                    }
                }
            }
        }
        return listOfServices;
    }
    private static ArrayList getMultiDocServices(DocFlavor[] flavors,
                                                 AttributeSet attributes) {
        ArrayList listOfServices = new ArrayList();
        Iterator psIterator = getAllLookupServices().iterator();
        while (psIterator.hasNext()) {
            try {
                PrintServiceLookup lus = (PrintServiceLookup)psIterator.next();
                MultiDocPrintService[] services  =
                    lus.getMultiDocPrintServices(flavors, attributes);
                if (services == null) {
                    continue;
                }
                for (int i=0; i<services.length; i++) {
                    listOfServices.add(services[i]);
                }
            } catch (Exception e) {
            }
        }
        ArrayList registeredServices = null;
        try {
          SecurityManager security = System.getSecurityManager();
          if (security != null) {
            security.checkPrintJobAccess();
          }
          registeredServices = getRegisteredServices();
        } catch (Exception e) {
        }
        if (registeredServices != null) {
            PrintService[] services = (PrintService[])
                registeredServices.toArray(
                           new PrintService[registeredServices.size()]);
            for (int i=0; i<services.length; i++) {
                if (services[i] instanceof MultiDocPrintService &&
                    !listOfServices.contains(services[i])) {
                    if (flavors == null || flavors.length == 0) {
                        listOfServices.add(services[i]);
                    } else {
                        boolean supported = true;
                        for (int f=0; f<flavors.length; f++) {
                            if (services[i].isDocFlavorSupported(flavors[f])) {
                                if (services[i].getUnsupportedAttributes(
                                     flavors[f], attributes) != null) {
                                        supported = false;
                                        break;
                                }
                            } else {
                                supported = false;
                                break;
                            }
                        }
                        if (supported) {
                            listOfServices.add(services[i]);
                        }
                    }
                }
            }
        }
        return listOfServices;
    }
}
