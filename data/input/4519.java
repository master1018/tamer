public class Win32PrintServiceLookup extends PrintServiceLookup {
    private String defaultPrinter;
    private PrintService defaultPrintService;
    private String[] printers; 
    private PrintService[] printServices; 
    static {
        java.security.AccessController.doPrivileged(
                    new sun.security.action.LoadLibraryAction("awt"));
    }
    private static Win32PrintServiceLookup win32PrintLUS;
    public static Win32PrintServiceLookup getWin32PrintLUS() {
        if (win32PrintLUS == null) {
            PrintServiceLookup.lookupDefaultPrintService();
        }
        return win32PrintLUS;
    }
    public Win32PrintServiceLookup() {
        if (win32PrintLUS == null) {
            win32PrintLUS = this;
            String osName = AccessController.doPrivileged(
                new sun.security.action.GetPropertyAction("os.name"));
            if (osName != null && osName.startsWith("Windows 98")) {
                return;
            }
            PrinterChangeListener thr = new PrinterChangeListener();
            thr.setDaemon(true);
            thr.start();
        } 
    }
    public synchronized PrintService[] getPrintServices() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPrintJobAccess();
        }
        if (printServices == null) {
            refreshServices();
        }
        return printServices;
    }
    private synchronized void refreshServices() {
        printers = getAllPrinterNames();
        if (printers == null) {
            printServices = new PrintService[0];
            return;
        }
        PrintService[] newServices = new PrintService[printers.length];
        PrintService defService = getDefaultPrintService();
        for (int p = 0; p < printers.length; p++) {
            if (defService != null &&
                printers[p].equals(defService.getName())) {
                newServices[p] = defService;
            } else {
                if (printServices == null) {
                    newServices[p] = new Win32PrintService(printers[p]);
                } else {
                    int j;
                    for (j = 0; j < printServices.length; j++) {
                        if ((printServices[j]!= null) &&
                            (printers[p].equals(printServices[j].getName()))) {
                            newServices[p] = printServices[j];
                            printServices[j] = null;
                            break;
                        }
                    }
                    if (j == printServices.length) {
                        newServices[p] = new Win32PrintService(printers[p]);
                    }
                }
            }
        }
        if (printServices != null) {
            for (int j=0; j < printServices.length; j++) {
                if ((printServices[j] instanceof Win32PrintService) &&
                    (!printServices[j].equals(defaultPrintService))) {
                    ((Win32PrintService)printServices[j]).invalidateService();
                }
            }
        }
        printServices = newServices;
    }
    public synchronized PrintService getPrintServiceByName(String name) {
        if (name == null || name.equals("")) {
            return null;
        } else {
            PrintService[] printServices = getPrintServices();
            for (int i=0; i<printServices.length; i++) {
                if (printServices[i].getName().equals(name)) {
                    return printServices[i];
                }
            }
            return null;
        }
    }
    boolean matchingService(PrintService service,
                            PrintServiceAttributeSet serviceSet) {
        if (serviceSet != null) {
            Attribute [] attrs =  serviceSet.toArray();
            Attribute serviceAttr;
            for (int i=0; i<attrs.length; i++) {
                serviceAttr
                    = service.getAttribute((Class<PrintServiceAttribute>)attrs[i].getCategory());
                if (serviceAttr == null || !serviceAttr.equals(attrs[i])) {
                    return false;
                }
            }
        }
        return true;
    }
    public PrintService[] getPrintServices(DocFlavor flavor,
                                           AttributeSet attributes) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
          security.checkPrintJobAccess();
        }
        PrintRequestAttributeSet requestSet = null;
        PrintServiceAttributeSet serviceSet = null;
        if (attributes != null && !attributes.isEmpty()) {
            requestSet = new HashPrintRequestAttributeSet();
            serviceSet = new HashPrintServiceAttributeSet();
            Attribute[] attrs = attributes.toArray();
            for (int i=0; i<attrs.length; i++) {
                if (attrs[i] instanceof PrintRequestAttribute) {
                    requestSet.add(attrs[i]);
                } else if (attrs[i] instanceof PrintServiceAttribute) {
                    serviceSet.add(attrs[i]);
                }
            }
        }
        PrintService[] services = null;
        if (serviceSet != null && serviceSet.get(PrinterName.class) != null) {
            PrinterName name = (PrinterName)serviceSet.get(PrinterName.class);
            PrintService service = getPrintServiceByName(name.getValue());
            if (service == null || !matchingService(service, serviceSet)) {
                services = new PrintService[0];
            } else {
                services = new PrintService[1];
                services[0] = service;
            }
        } else {
            services = getPrintServices();
        }
        if (services.length == 0) {
            return services;
        } else {
            ArrayList matchingServices = new ArrayList();
            for (int i=0; i<services.length; i++) {
                try {
                    if (services[i].
                        getUnsupportedAttributes(flavor, requestSet) == null) {
                        matchingServices.add(services[i]);
                    }
                } catch (IllegalArgumentException e) {
                }
            }
            services = new PrintService[matchingServices.size()];
            return (PrintService[])matchingServices.toArray(services);
        }
    }
    public MultiDocPrintService[]
        getMultiDocPrintServices(DocFlavor[] flavors,
                                 AttributeSet attributes) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
          security.checkPrintJobAccess();
        }
        return new MultiDocPrintService[0];
    }
    public synchronized PrintService getDefaultPrintService() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
          security.checkPrintJobAccess();
        }
        defaultPrinter = getDefaultPrinterName();
        if (defaultPrinter == null) {
            return null;
        }
        if ((defaultPrintService != null) &&
            defaultPrintService.getName().equals(defaultPrinter)) {
            return defaultPrintService;
        }
        defaultPrintService = null;
        if (printServices != null) {
            for (int j=0; j<printServices.length; j++) {
                if (defaultPrinter.equals(printServices[j].getName())) {
                    defaultPrintService = printServices[j];
                    break;
                }
            }
        }
        if (defaultPrintService == null) {
            defaultPrintService = new Win32PrintService(defaultPrinter);
        }
        return defaultPrintService;
    }
    class PrinterChangeListener extends Thread {
        long chgObj;
        PrinterChangeListener() {
            chgObj = notifyFirstPrinterChange(null);
        }
        public void run() {
            if (chgObj != -1) {
                while (true) {
                    if (notifyPrinterChange(chgObj) != 0) {
                        try {
                            refreshServices();
                        } catch (SecurityException se) {
                            break;
                        }
                    } else {
                        notifyClosePrinterChange(chgObj);
                        break;
                    }
                }
            }
        }
    }
    private native String getDefaultPrinterName();
    private native String[] getAllPrinterNames();
    private native long notifyFirstPrinterChange(String printer);
    private native void notifyClosePrinterChange(long chgObj);
    private native int notifyPrinterChange(long chgObj);
}
