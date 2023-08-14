public class UnixPrintServiceLookup extends PrintServiceLookup
    implements BackgroundServiceLookup, Runnable {
    private String defaultPrinter;
    private PrintService defaultPrintService;
    private PrintService[] printServices; 
    private Vector lookupListeners = null;
    private static String debugPrefix = "UnixPrintServiceLookup>> ";
    private static boolean pollServices = true;
    private static final int DEFAULT_MINREFRESH = 120;  
    private static int minRefreshTime = DEFAULT_MINREFRESH;
    static String osname;
    static {
        String pollStr = java.security.AccessController.doPrivileged(
            new sun.security.action.GetPropertyAction("sun.java2d.print.polling"));
        if (pollStr != null) {
            if (pollStr.equalsIgnoreCase("true")) {
                pollServices = true;
            } else if (pollStr.equalsIgnoreCase("false")) {
                pollServices = false;
            }
        }
        String refreshTimeStr = java.security.AccessController.doPrivileged(
            new sun.security.action.GetPropertyAction(
                "sun.java2d.print.minRefreshTime"));
        if (refreshTimeStr != null) {
            try {
                minRefreshTime = (new Integer(refreshTimeStr)).intValue();
            } catch (NumberFormatException e) {
            }
            if (minRefreshTime < DEFAULT_MINREFRESH) {
                minRefreshTime = DEFAULT_MINREFRESH;
            }
        }
        osname = java.security.AccessController.doPrivileged(
            new sun.security.action.GetPropertyAction("os.name"));
    }
    static boolean isSysV() {
        return osname.equals("SunOS");
    }
    static boolean isBSD() {
        return osname.equals("Linux");
    }
    static final int UNINITIALIZED = -1;
    static final int BSD_LPD = 0;
    static final int BSD_LPD_NG = 1;
    static int cmdIndex = UNINITIALIZED;
    String[] lpcFirstCom = {
        "/usr/sbin/lpc status | grep : | sed -ne '1,1 s/:
        "/usr/sbin/lpc status | grep -E '^[ 0-9a-zA-Z_-]*@' | awk -F'@' '{print $1}'"
    };
    String[] lpcAllCom = {
        "/usr/sbin/lpc status | grep : | sed -e 's/:
        "/usr/sbin/lpc -a status | grep -E '^[ 0-9a-zA-Z_-]*@' | awk -F'@' '{print $1}' | sort"
    };
    String[] lpcNameCom = {
        "| grep : | sed -ne 's/:
        "| grep -E '^[ 0-9a-zA-Z_-]*@' | awk -F'@' '{print $1}'"
    };
    static int getBSDCommandIndex() {
        String command  = "/usr/sbin/lpc status";
        String[] names = execCmd(command);
        if ((names == null) || (names.length == 0)) {
            return BSD_LPD_NG;
        }
        for (int i=0; i<names.length; i++) {
            if (names[i].indexOf('@') != -1) {
                return BSD_LPD_NG;
            }
        }
        return BSD_LPD;
    }
    public UnixPrintServiceLookup() {
        if (pollServices) {
            PrinterChangeListener thr = new PrinterChangeListener();
            thr.setDaemon(true);
            thr.start();
            IPPPrintService.debug_println(debugPrefix+"polling turned on");
        }
    }
    public synchronized PrintService[] getPrintServices() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPrintJobAccess();
        }
        if (printServices == null || !pollServices) {
            refreshServices();
        }
        if (printServices == null) {
            return new PrintService[0];
        } else {
            return printServices;
        }
    }
    public synchronized void refreshServices() {
        String[] printers = null; 
        String[] printerURIs = null; 
        getDefaultPrintService();
        if (CUPSPrinter.isCupsRunning()) {
            printerURIs = CUPSPrinter.getAllPrinters();
            if ((printerURIs != null) && (printerURIs.length > 0)) {
                printers = new String[printerURIs.length];
                for (int i=0; i<printerURIs.length; i++) {
                    int lastIndex = printerURIs[i].lastIndexOf("/");
                    printers[i] = printerURIs[i].substring(lastIndex+1);
                }
            }
        } else {
            if (isSysV()) {
                printers = getAllPrinterNamesSysV();
            } else { 
                printers = getAllPrinterNamesBSD();
            }
        }
        if (printers == null) {
            if (defaultPrintService != null) {
                printServices = new PrintService[1];
                printServices[0] = defaultPrintService;
            } else {
                printServices = null;
            }
            return;
        }
        ArrayList printerList = new ArrayList();
        int defaultIndex = -1;
        for (int p=0; p<printers.length; p++) {
            if (printers[p] == null) {
                continue;
            }
            if ((defaultPrintService != null)
                && printers[p].equals(defaultPrintService.getName())) {
                printerList.add(defaultPrintService);
                defaultIndex = printerList.size() - 1;
            } else {
                if (printServices == null) {
                    IPPPrintService.debug_println(debugPrefix+
                                                  "total# of printers = "+printers.length);
                    if (CUPSPrinter.isCupsRunning()) {
                        try {
                            printerList.add(new IPPPrintService(printers[p],
                                                                printerURIs[p],
                                                                true));
                        } catch (Exception e) {
                            IPPPrintService.debug_println(debugPrefix+
                                                          " getAllPrinters Exception "+
                                                          e);
                        }
                    } else {
                        printerList.add(new UnixPrintService(printers[p]));
                    }
                } else {
                    int j;
                    for (j=0; j<printServices.length; j++) {
                        if ((printServices[j] != null) &&
                            (printers[p].equals(printServices[j].getName()))) {
                            printerList.add(printServices[j]);
                            printServices[j] = null;
                            break;
                        }
                    }
                    if (j == printServices.length) {      
                        if (CUPSPrinter.isCupsRunning()) {
                            try {
                                printerList.add(new IPPPrintService(
                                                               printers[p],
                                                               printerURIs[p],
                                                               true));
                            } catch (Exception e) {
                                IPPPrintService.debug_println(debugPrefix+
                                                              " getAllPrinters Exception "+
                                                              e);
                            }
                        } else {
                            printerList.add(new UnixPrintService(printers[p]));
                        }
                    }
                }
            }
        }
        if (printServices != null) {
            for (int j=0; j < printServices.length; j++) {
                if ((printServices[j] instanceof UnixPrintService) &&
                    (!printServices[j].equals(defaultPrintService))) {
                    ((UnixPrintService)printServices[j]).invalidateService();
                }
            }
        }
        if (defaultIndex == -1 && defaultPrintService != null) {
            printerList.add(defaultPrintService);
            defaultIndex = printerList.size() - 1;
        }
        printServices = (PrintService[])printerList.toArray(
                                      new PrintService[] {});
        if (defaultIndex > 0) {
            PrintService saveService = printServices[0];
            printServices[0] = printServices[defaultIndex];
            printServices[defaultIndex] = saveService;
        }
    }
    private boolean matchesAttributes(PrintService service,
                                      PrintServiceAttributeSet attributes) {
        Attribute [] attrs =  attributes.toArray();
        Attribute serviceAttr;
        for (int i=0; i<attrs.length; i++) {
            serviceAttr
                = service.getAttribute((Class<PrintServiceAttribute>)attrs[i].getCategory());
            if (serviceAttr == null || !serviceAttr.equals(attrs[i])) {
                return false;
            }
        }
        return true;
    }
      private boolean checkPrinterName(String s) {
        char c;
        for (int i=0; i < s.length(); i++) {
          c = s.charAt(i);
          if (Character.isLetterOrDigit(c) ||
              c == '-' || c == '_' || c == '.' || c == '/') {
            continue;
          } else {
            return false;
          }
        }
        return true;
      }
    private PrintService getServiceByName(PrinterName nameAttr) {
        String name = nameAttr.getValue();
        PrintService printer = null;
        if (name == null || name.equals("") || !checkPrinterName(name)) {
            return null;
        }
        if (isSysV()) {
            printer = getNamedPrinterNameSysV(name);
        } else {
            printer = getNamedPrinterNameBSD(name);
        }
        return printer;
    }
    private PrintService[]
        getPrintServices(PrintServiceAttributeSet serviceSet) {
        if (serviceSet == null || serviceSet.isEmpty()) {
            return getPrintServices();
        }
        PrintService[] services;
        PrinterName name = (PrinterName)serviceSet.get(PrinterName.class);
        PrintService defService;
        if (name != null && (defService = getDefaultPrintService()) != null) {
            PrinterName defName =
                (PrinterName)defService.getAttribute(PrinterName.class);
            if (defName != null && name.equals(defName)) {
                if (matchesAttributes(defService, serviceSet)) {
                    services = new PrintService[1];
                    services[0] = defService;
                    return services;
                } else {
                    return new PrintService[0];
                }
            } else {
                PrintService service = getServiceByName(name);
                if (service != null &&
                    matchesAttributes(service, serviceSet)) {
                    services = new PrintService[1];
                    services[0] = service;
                    return services;
                } else {
                    return new PrintService[0];
                }
            }
        } else {
            Vector matchedServices = new Vector();
            services = getPrintServices();
            for (int i = 0; i< services.length; i++) {
                if (matchesAttributes(services[i], serviceSet)) {
                    matchedServices.add(services[i]);
                }
            }
            services = new PrintService[matchedServices.size()];
            for (int i = 0; i< services.length; i++) {
                services[i] = (PrintService)matchedServices.elementAt(i);
            }
            return services;
        }
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
        PrintService[] services = getPrintServices(serviceSet);
        if (services.length == 0) {
            return services;
        }
        if (CUPSPrinter.isCupsRunning()) {
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
        } else {
            PrintService service = services[0];
            if ((flavor == null ||
                 service.isDocFlavorSupported(flavor)) &&
                 service.getUnsupportedAttributes(flavor, requestSet) == null)
            {
                return services;
            } else {
                return new PrintService[0];
            }
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
        defaultPrintService = null;
        IPPPrintService.debug_println("isRunning ? "+
                                      (CUPSPrinter.isCupsRunning()));
        if (CUPSPrinter.isCupsRunning()) {
            defaultPrinter = CUPSPrinter.getDefaultPrinter();
        } else {
            if (isSysV()) {
                defaultPrinter = getDefaultPrinterNameSysV();
            } else {
                defaultPrinter = getDefaultPrinterNameBSD();
            }
        }
        if (defaultPrinter == null) {
            return null;
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
            if (CUPSPrinter.isCupsRunning()) {
                try {
                    PrintService defaultPS =
                        new IPPPrintService(defaultPrinter,
                                            new URL("http:
                                                    CUPSPrinter.getServer()+":"+
                                                    CUPSPrinter.getPort()+"/"+
                                                    defaultPrinter));
                    defaultPrintService = defaultPS;
                } catch (Exception e) {
                }
            } else {
                defaultPrintService = new UnixPrintService(defaultPrinter);
            }
        }
        return defaultPrintService;
    }
    public synchronized void
        getServicesInbackground(BackgroundLookupListener listener) {
        if (printServices != null) {
            listener.notifyServices(printServices);
        } else {
            if (lookupListeners == null) {
                lookupListeners = new Vector();
                lookupListeners.add(listener);
                Thread lookupThread = new Thread(this);
                lookupThread.start();
            } else {
                lookupListeners.add(listener);
            }
        }
    }
    private PrintService[] copyOf(PrintService[] inArr) {
        if (inArr == null || inArr.length == 0) {
            return inArr;
        } else {
            PrintService []outArr = new PrintService[inArr.length];
            System.arraycopy(inArr, 0, outArr, 0, inArr.length);
            return outArr;
        }
    }
    public void run() {
        PrintService[] services = getPrintServices();
        synchronized (this) {
            BackgroundLookupListener listener;
            for (int i=0; i<lookupListeners.size(); i++) {
                listener =
                    (BackgroundLookupListener)lookupListeners.elementAt(i);
                listener.notifyServices(copyOf(services));
            }
            lookupListeners = null;
        }
    }
    private String getDefaultPrinterNameBSD() {
        if (cmdIndex == UNINITIALIZED) {
            cmdIndex = getBSDCommandIndex();
        }
        String[] names = execCmd(lpcFirstCom[cmdIndex]);
        if (names == null || names.length == 0) {
            return null;
        }
        if ((cmdIndex==BSD_LPD_NG) &&
            (names[0].startsWith("missingprinter"))) {
            return null;
        }
        return names[0];
    }
    private PrintService getNamedPrinterNameBSD(String name) {
      if (cmdIndex == UNINITIALIZED) {
        cmdIndex = getBSDCommandIndex();
      }
      String command = "/usr/sbin/lpc status " + name + lpcNameCom[cmdIndex];
      String[] result = execCmd(command);
      if (result == null || !(result[0].equals(name))) {
          return null;
      }
      return new UnixPrintService(name);
    }
    private String[] getAllPrinterNamesBSD() {
        if (cmdIndex == UNINITIALIZED) {
            cmdIndex = getBSDCommandIndex();
        }
        String[] names = execCmd(lpcAllCom[cmdIndex]);
        if (names == null || names.length == 0) {
          return null;
        }
        return names;
    }
    private String getDefaultPrinterNameSysV() {
        String defaultPrinter = "lp";
        String command = "/usr/bin/lpstat -d";
        String [] names = execCmd(command);
        if (names == null || names.length == 0) {
            return defaultPrinter;
        } else {
            int index = names[0].indexOf(":");
            if (index == -1  || (names[0].length() <= index+1)) {
                return null;
            } else {
                String name = names[0].substring(index+1).trim();
                if (name.length() == 0) {
                    return null;
                } else {
                    return name;
                }
            }
        }
    }
    private PrintService getNamedPrinterNameSysV(String name) {
        String command = "/usr/bin/lpstat -v " + name;
        String []result = execCmd(command);
        if (result == null || result[0].indexOf("unknown printer") > 0) {
            return null;
        } else {
            return new UnixPrintService(name);
        }
    }
    private String[] getAllPrinterNamesSysV() {
        String defaultPrinter = "lp";
        String command = "/usr/bin/lpstat -v|/usr/bin/expand|/usr/bin/cut -f3 -d' ' |/usr/bin/cut -f1 -d':' | /usr/bin/sort";
        String [] names = execCmd(command);
        ArrayList printerNames = new ArrayList();
        for (int i=0; i < names.length; i++) {
            if (!names[i].equals("_default") &&
                !names[i].equals(defaultPrinter) &&
                !names[i].equals("")) {
                printerNames.add(names[i]);
            }
        }
        return (String[])printerNames.toArray(new String[printerNames.size()]);
    }
    static String[] execCmd(final String command) {
        ArrayList results = null;
        try {
            final String[] cmd = new String[3];
            if (isSysV()) {
                cmd[0] = "/usr/bin/sh";
                cmd[1] = "-c";
                cmd[2] = "env LC_ALL=C " + command;
            } else {
                cmd[0] = "/bin/sh";
                cmd[1] = "-c";
                cmd[2] = "LC_ALL=C " + command;
            }
            results = (ArrayList)AccessController.doPrivileged(
                new PrivilegedExceptionAction() {
                    public Object run() throws IOException {
                        Process proc;
                        BufferedReader bufferedReader = null;
                        File f = File.createTempFile("prn","xc");
                        cmd[2] = cmd[2]+">"+f.getAbsolutePath();
                        proc = Runtime.getRuntime().exec(cmd);
                        try {
                            boolean done = false; 
                            while (!done) {
                                try {
                                    proc.waitFor();
                                    done = true;
                                } catch (InterruptedException e) {
                                }
                            }
                            if (proc.exitValue() == 0) {
                                FileReader reader = new FileReader(f);
                                bufferedReader = new BufferedReader(reader);
                                String line;
                                ArrayList results = new ArrayList();
                                while ((line = bufferedReader.readLine())
                                       != null) {
                                    results.add(line);
                                }
                                return results;
                            }
                        } finally {
                            f.delete();
                            if (bufferedReader != null) {
                                bufferedReader.close();
                            }
                            proc.getInputStream().close();
                            proc.getErrorStream().close();
                            proc.getOutputStream().close();
                        }
                        return null;
                    }
                });
        } catch (PrivilegedActionException e) {
        }
        if (results == null) {
            return new String[0];
        } else {
            return (String[])results.toArray(new String[results.size()]);
        }
    }
    private class PrinterChangeListener extends Thread {
        public void run() {
            int refreshSecs;
            while (true) {
                try {
                    refreshServices();
                } catch (Exception se) {
                    IPPPrintService.debug_println(debugPrefix+"Exception in refresh thread.");
                    break;
                }
                if ((printServices != null) &&
                    (printServices.length > minRefreshTime)) {
                    refreshSecs = printServices.length;
                } else {
                    refreshSecs = minRefreshTime;
                }
                try {
                    sleep(refreshSecs * 1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }
}
