public abstract class InputMethodManager {
    private static final String threadName = "AWT-InputMethodManager";
    private static final Object LOCK = new Object();
    private static InputMethodManager inputMethodManager;
    public static final InputMethodManager getInstance() {
        if (inputMethodManager != null) {
            return inputMethodManager;
        }
        synchronized(LOCK) {
            if (inputMethodManager == null) {
                ExecutableInputMethodManager imm = new ExecutableInputMethodManager();
                if (imm.hasMultipleInputMethods()) {
                    imm.initialize();
                    Thread immThread = new Thread(imm, threadName);
                    immThread.setDaemon(true);
                    immThread.setPriority(Thread.NORM_PRIORITY + 1);
                    immThread.start();
                }
                inputMethodManager = imm;
            }
        }
        return inputMethodManager;
    }
    public abstract String getTriggerMenuString();
    public abstract void notifyChangeRequest(Component comp);
    public abstract void notifyChangeRequestByHotKey(Component comp);
    abstract void setInputContext(InputContext inputContext);
    abstract InputMethodLocator findInputMethod(Locale forLocale);
    abstract Locale getDefaultKeyboardLocale();
    abstract boolean hasMultipleInputMethods();
}
class ExecutableInputMethodManager extends InputMethodManager
                                   implements Runnable
{
    private InputContext currentInputContext;
    private String triggerMenuString;
    private InputMethodPopupMenu selectionMenu;
    private static String selectInputMethodMenuTitle;
    private InputMethodLocator hostAdapterLocator;
    private int javaInputMethodCount;         
    private Vector<InputMethodLocator> javaInputMethodLocatorList;
    private Component requestComponent;
    private InputContext requestInputContext;
    private static final String preferredIMNode = "/sun/awt/im/preferredInputMethod";
    private static final String descriptorKey = "descriptor";
    private Hashtable preferredLocatorCache = new Hashtable();
    private Preferences userRoot;
    ExecutableInputMethodManager() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        try {
            if (toolkit instanceof InputMethodSupport) {
                InputMethodDescriptor hostAdapterDescriptor =
                    ((InputMethodSupport)toolkit)
                    .getInputMethodAdapterDescriptor();
                if (hostAdapterDescriptor != null) {
                    hostAdapterLocator = new InputMethodLocator(hostAdapterDescriptor, null, null);
                }
            }
        } catch (AWTException e) {
        }
        javaInputMethodLocatorList = new Vector<InputMethodLocator>();
        initializeInputMethodLocatorList();
    }
    synchronized void initialize() {
        selectInputMethodMenuTitle = Toolkit.getProperty("AWT.InputMethodSelectionMenu", "Select Input Method");
        triggerMenuString = selectInputMethodMenuTitle;
    }
    public void run() {
        while (!hasMultipleInputMethods()) {
            try {
                synchronized (this) {
                    wait();
                }
            } catch (InterruptedException e) {
            }
        }
        while (true) {
            waitForChangeRequest();
            initializeInputMethodLocatorList();
            try {
                if (requestComponent != null) {
                    showInputMethodMenuOnRequesterEDT(requestComponent);
                } else {
                    EventQueue.invokeAndWait(new Runnable() {
                        public void run() {
                            showInputMethodMenu();
                        }
                    });
                }
            } catch (InterruptedException ie) {
            } catch (InvocationTargetException ite) {
            }
        }
    }
    private void showInputMethodMenuOnRequesterEDT(Component requester)
        throws InterruptedException, InvocationTargetException {
        if (requester == null){
            return;
        }
        class AWTInvocationLock {}
        Object lock = new AWTInvocationLock();
        InvocationEvent event =
                new InvocationEvent(requester,
                                    new Runnable() {
                                        public void run() {
                                            showInputMethodMenu();
                                        }
                                    },
                                    lock,
                                    true);
        AppContext requesterAppContext = SunToolkit.targetToAppContext(requester);
        synchronized (lock) {
            SunToolkit.postEvent(requesterAppContext, event);
            while (!event.isDispatched()) {
                lock.wait();
            }
        }
        Throwable eventThrowable = event.getThrowable();
        if (eventThrowable != null) {
            throw new InvocationTargetException(eventThrowable);
        }
    }
    void setInputContext(InputContext inputContext) {
        if (currentInputContext != null && inputContext != null) {
        }
        currentInputContext = inputContext;
    }
    public synchronized void notifyChangeRequest(Component comp) {
        if (!(comp instanceof Frame || comp instanceof Dialog))
            return;
        if (requestComponent != null)
            return;
        requestComponent = comp;
        notify();
    }
    public synchronized void notifyChangeRequestByHotKey(Component comp) {
        while (!(comp instanceof Frame || comp instanceof Dialog)) {
            if (comp == null) {
                return;
            }
            comp = comp.getParent();
        }
        notifyChangeRequest(comp);
    }
    public String getTriggerMenuString() {
        return triggerMenuString;
    }
    boolean hasMultipleInputMethods() {
        return ((hostAdapterLocator != null) && (javaInputMethodCount > 0)
                || (javaInputMethodCount > 1));
    }
    private synchronized void waitForChangeRequest() {
        try {
            while (requestComponent == null) {
                wait();
            }
        } catch (InterruptedException e) {
        }
    }
    private void initializeInputMethodLocatorList() {
        synchronized (javaInputMethodLocatorList) {
            javaInputMethodLocatorList.clear();
            try {
                AccessController.doPrivileged(new PrivilegedExceptionAction() {
                    public Object run() {
                        for (InputMethodDescriptor descriptor :
                            ServiceLoader.loadInstalled(InputMethodDescriptor.class)) {
                            ClassLoader cl = descriptor.getClass().getClassLoader();
                            javaInputMethodLocatorList.add(new InputMethodLocator(descriptor, cl, null));
                        }
                        return null;
                    }
                });
            }  catch (PrivilegedActionException e) {
                e.printStackTrace();
            }
            javaInputMethodCount = javaInputMethodLocatorList.size();
        }
        if (hasMultipleInputMethods()) {
            if (userRoot == null) {
                userRoot = getUserRoot();
            }
        } else {
            triggerMenuString = null;
        }
    }
    private void showInputMethodMenu() {
        if (!hasMultipleInputMethods()) {
            requestComponent = null;
            return;
        }
        selectionMenu = InputMethodPopupMenu.getInstance(requestComponent, selectInputMethodMenuTitle);
        selectionMenu.removeAll();
        String currentSelection = getCurrentSelection();
        if (hostAdapterLocator != null) {
            selectionMenu.addOneInputMethodToMenu(hostAdapterLocator, currentSelection);
            selectionMenu.addSeparator();
        }
        for (int i = 0; i < javaInputMethodLocatorList.size(); i++) {
            InputMethodLocator locator = javaInputMethodLocatorList.get(i);
            selectionMenu.addOneInputMethodToMenu(locator, currentSelection);
        }
        synchronized (this) {
            selectionMenu.addToComponent(requestComponent);
            requestInputContext = currentInputContext;
            selectionMenu.show(requestComponent, 60, 80); 
            requestComponent = null;
        }
    }
    private String getCurrentSelection() {
        InputContext inputContext = currentInputContext;
        if (inputContext != null) {
            InputMethodLocator locator = inputContext.getInputMethodLocator();
            if (locator != null) {
                return locator.getActionCommandString();
            }
        }
        return null;
    }
    synchronized void changeInputMethod(String choice) {
        InputMethodLocator locator = null;
        String inputMethodName = choice;
        String localeString = null;
        int index = choice.indexOf('\n');
        if (index != -1) {
            localeString = choice.substring(index + 1);
            inputMethodName = choice.substring(0, index);
        }
        if (hostAdapterLocator.getActionCommandString().equals(inputMethodName)) {
            locator = hostAdapterLocator;
        } else {
            for (int i = 0; i < javaInputMethodLocatorList.size(); i++) {
                InputMethodLocator candidate = javaInputMethodLocatorList.get(i);
                String name = candidate.getActionCommandString();
                if (name.equals(inputMethodName)) {
                    locator = candidate;
                    break;
                }
            }
        }
        if (locator != null && localeString != null) {
            String language = "", country = "", variant = "";
            int postIndex = localeString.indexOf('_');
            if (postIndex == -1) {
                language = localeString;
            } else {
                language = localeString.substring(0, postIndex);
                int preIndex = postIndex + 1;
                postIndex = localeString.indexOf('_', preIndex);
                if (postIndex == -1) {
                    country = localeString.substring(preIndex);
                } else {
                    country = localeString.substring(preIndex, postIndex);
                    variant = localeString.substring(postIndex + 1);
                }
            }
            Locale locale = new Locale(language, country, variant);
            locator = locator.deriveLocator(locale);
        }
        if (locator == null)
            return;
        if (requestInputContext != null) {
            requestInputContext.changeInputMethod(locator);
            requestInputContext = null;
            putPreferredInputMethod(locator);
        }
    }
    InputMethodLocator findInputMethod(Locale locale) {
        InputMethodLocator locator = getPreferredInputMethod(locale);
        if (locator != null) {
            return locator;
        }
        if (hostAdapterLocator != null && hostAdapterLocator.isLocaleAvailable(locale)) {
            return hostAdapterLocator.deriveLocator(locale);
        }
        initializeInputMethodLocatorList();
        for (int i = 0; i < javaInputMethodLocatorList.size(); i++) {
            InputMethodLocator candidate = javaInputMethodLocatorList.get(i);
            if (candidate.isLocaleAvailable(locale)) {
                return candidate.deriveLocator(locale);
            }
        }
        return null;
    }
    Locale getDefaultKeyboardLocale() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        if (toolkit instanceof InputMethodSupport) {
            return ((InputMethodSupport)toolkit).getDefaultKeyboardLocale();
        } else {
            return Locale.getDefault();
        }
    }
    private synchronized InputMethodLocator getPreferredInputMethod(Locale locale) {
        InputMethodLocator preferredLocator = null;
        if (!hasMultipleInputMethods()) {
            return null;
        }
        preferredLocator = (InputMethodLocator)preferredLocatorCache.get(locale.toString().intern());
        if (preferredLocator != null) {
            return preferredLocator;
        }
        String nodePath = findPreferredInputMethodNode(locale);
        String descriptorName = readPreferredInputMethod(nodePath);
        Locale advertised;
        if (descriptorName != null) {
            if (hostAdapterLocator != null &&
                hostAdapterLocator.getDescriptor().getClass().getName().equals(descriptorName)) {
                advertised = getAdvertisedLocale(hostAdapterLocator, locale);
                if (advertised != null) {
                    preferredLocator = hostAdapterLocator.deriveLocator(advertised);
                    preferredLocatorCache.put(locale.toString().intern(), preferredLocator);
                }
                return preferredLocator;
            }
            for (int i = 0; i < javaInputMethodLocatorList.size(); i++) {
                InputMethodLocator locator = javaInputMethodLocatorList.get(i);
                InputMethodDescriptor descriptor = locator.getDescriptor();
                if (descriptor.getClass().getName().equals(descriptorName)) {
                    advertised = getAdvertisedLocale(locator, locale);
                    if (advertised != null) {
                        preferredLocator = locator.deriveLocator(advertised);
                        preferredLocatorCache.put(locale.toString().intern(), preferredLocator);
                    }
                    return preferredLocator;
                }
            }
            writePreferredInputMethod(nodePath, null);
        }
        return null;
    }
    private String findPreferredInputMethodNode(Locale locale) {
        if (userRoot == null) {
            return null;
        }
        String nodePath = preferredIMNode + "/" + createLocalePath(locale);
        while (!nodePath.equals(preferredIMNode)) {
            try {
                if (userRoot.nodeExists(nodePath)) {
                    if (readPreferredInputMethod(nodePath) != null) {
                        return nodePath;
                    }
                }
            } catch (BackingStoreException bse) {
            }
            nodePath = nodePath.substring(0, nodePath.lastIndexOf('/'));
        }
        return null;
    }
    private String readPreferredInputMethod(String nodePath) {
        if ((userRoot == null) || (nodePath == null)) {
            return null;
        }
        return userRoot.node(nodePath).get(descriptorKey, null);
    }
    private synchronized void putPreferredInputMethod(InputMethodLocator locator) {
        InputMethodDescriptor descriptor = locator.getDescriptor();
        Locale preferredLocale = locator.getLocale();
        if (preferredLocale == null) {
            try {
                Locale[] availableLocales = descriptor.getAvailableLocales();
                if (availableLocales.length == 1) {
                    preferredLocale = availableLocales[0];
                } else {
                    return;
                }
            } catch (AWTException ae) {
                return;
            }
        }
        if (preferredLocale.equals(Locale.JAPAN)) {
            preferredLocale = Locale.JAPANESE;
        }
        if (preferredLocale.equals(Locale.KOREA)) {
            preferredLocale = Locale.KOREAN;
        }
        if (preferredLocale.equals(new Locale("th", "TH"))) {
            preferredLocale = new Locale("th");
        }
        String path = preferredIMNode + "/" + createLocalePath(preferredLocale);
        writePreferredInputMethod(path, descriptor.getClass().getName());
        preferredLocatorCache.put(preferredLocale.toString().intern(),
            locator.deriveLocator(preferredLocale));
        return;
    }
    private String createLocalePath(Locale locale) {
        String language = locale.getLanguage();
        String country = locale.getCountry();
        String variant = locale.getVariant();
        String localePath = null;
        if (!variant.equals("")) {
            localePath = "_" + language + "/_" + country + "/_" + variant;
        } else if (!country.equals("")) {
            localePath = "_" + language + "/_" + country;
        } else {
            localePath = "_" + language;
        }
        return localePath;
    }
    private void writePreferredInputMethod(String path, String descriptorName) {
        if (userRoot != null) {
            Preferences node = userRoot.node(path);
            if (descriptorName != null) {
                node.put(descriptorKey, descriptorName);
            } else {
                node.remove(descriptorKey);
            }
        }
    }
    private Preferences getUserRoot() {
        return (Preferences)AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return Preferences.userRoot();
            }
        });
    }
    private Locale getAdvertisedLocale(InputMethodLocator locator, Locale locale) {
        Locale advertised = null;
        if (locator.isLocaleAvailable(locale)) {
            advertised = locale;
        } else if (locale.getLanguage().equals("ja")) {
            if (locator.isLocaleAvailable(Locale.JAPAN)) {
                advertised = Locale.JAPAN;
            } else if (locator.isLocaleAvailable(Locale.JAPANESE)) {
                advertised = Locale.JAPANESE;
            }
        } else if (locale.getLanguage().equals("ko")) {
            if (locator.isLocaleAvailable(Locale.KOREA)) {
                advertised = Locale.KOREA;
            } else if (locator.isLocaleAvailable(Locale.KOREAN)) {
                advertised = Locale.KOREAN;
            }
        } else if (locale.getLanguage().equals("th")) {
            if (locator.isLocaleAvailable(new Locale("th", "TH"))) {
                advertised = new Locale("th", "TH");
            } else if (locator.isLocaleAvailable(new Locale("th"))) {
                advertised = new Locale("th");
            }
        }
        return advertised;
    }
}
