public final class EaasyStreet {
    private static final String EVI0001T = "EVI0001T";
    private static final String EVI0003T = "EVI0003T";
    private static final String EVI0004T = "EVI0004T";
    private static final String EVI0010T = "EVI0010T";
    private static final String EVI0011T = "EVI0011T";
    private static final String EVI0014W = "EVI0014W";
    private static final String SERVICE_LOCATOR_TEXT_KEY = "string.service.locator";
    private static final String INITIALIZATION_NOTICE_KEY = "string.initialization.notice";
    private static final String TERMINATION_NOTICE_KEY = "string.termination.notice";
    private static final MessageResources localStrings = MessageResources.getMessageResources("org.eaasyst.eaa.syst.LocalStrings");
    private static final Log log = LogFactory.getLog("EaasyStreet");
    private static final Properties defaultValues = PropertiesManager.getProperties("org.eaasyst.eaa.syst.DefaultValues");
    private static final int periodicLimit = 10000;
    private static HashMap serviceLocators = new HashMap();
    private static HashMap threads = new HashMap();
    private static HashMap sessions = new HashMap();
    private static int runningCount = 0;
    public static void init(ServletConfig config) throws EaasyStreetSystemError {
        PropertiesManager propertiesManager = new PropertiesManager(config.getServletContext());
        Properties props = propertiesManager.loadProperties(defaultValues.getProperty(Constants.CFG_CONFIG_SRC), defaultValues);
        config.getServletContext().setAttribute(Constants.CTX_SYSTEM_PROPERTIES, props);
        String systemName = null;
        if (props.getProperty(Constants.CFG_SYSTEM_NAME).equals(systemName)) {
            log.warn(localStrings.getMessage(EVI0014W, new String[] { Constants.CFG_SYSTEM_NAME, systemName }));
        } else {
            systemName = props.getProperty(Constants.CFG_SYSTEM_NAME);
        }
        config.getServletContext().setAttribute(Constants.CTX_SYSTEM_NAME, systemName);
        String serviceLocatorClassName = props.getProperty(Constants.CFG_SVC_LOCATOR_CLASS);
        ServiceLocator serviceLocator = null;
        try {
            serviceLocator = (ServiceLocator) Class.forName(serviceLocatorClassName).newInstance();
        } catch (Exception e) {
            try {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                serviceLocator = (ServiceLocator) classLoader.loadClass(serviceLocatorClassName).newInstance();
            } catch (Throwable t) {
                systemError(EVI0003T, new String[] { localStrings.getMessage(SERVICE_LOCATOR_TEXT_KEY), serviceLocatorClassName, t.toString() }, t);
            }
        }
        if (serviceLocator != null) {
            synchronized (serviceLocators) {
                if (serviceLocators.containsKey(systemName)) {
                    systemError(EVI0001T, null, null);
                } else {
                    log.info(localStrings.getMessage(INITIALIZATION_NOTICE_KEY, systemName));
                    serviceLocators.put(systemName, serviceLocator);
                    register(systemName, null);
                    serviceLocator.initialize(config);
                }
            }
        }
    }
    public static void destroy(String systemName) {
        synchronized (serviceLocators) {
            serviceLocators.remove(systemName);
        }
        synchronized (threads) {
            List toBeRemoved = new ArrayList();
            Iterator i = threads.keySet().iterator();
            while (i.hasNext()) {
                String key = (String) i.next();
                ServiceRegistration registration = (ServiceRegistration) threads.get(key);
                if (systemName.equals(registration.getSystemName())) {
                    toBeRemoved.add(key);
                }
            }
            i = toBeRemoved.iterator();
            while (i.hasNext()) {
                threads.remove(i.next());
            }
        }
        synchronized (sessions) {
            List toBeRemoved = new ArrayList();
            Iterator i = sessions.keySet().iterator();
            while (i.hasNext()) {
                String key = (String) i.next();
                ServiceRegistration registration = (ServiceRegistration) sessions.get(key);
                if (systemName.equals(registration.getSystemName())) {
                    toBeRemoved.add(key);
                }
            }
            i = toBeRemoved.iterator();
            while (i.hasNext()) {
                sessions.remove(i.next());
            }
        }
        log.info(localStrings.getMessage(TERMINATION_NOTICE_KEY, systemName));
    }
    public static void register(String systemName, HttpServletRequest req) {
        ServiceRegistration registration = null;
        String threadId = Thread.currentThread().getName();
        Date dateTime = new Date();
        String sessionId = null;
        String userId = null;
        String useName = null;
        if (req != null) {
            HttpSession session = req.getSession();
            if (session != null) {
                sessionId = session.getId();
                User profile = (User) session.getAttribute(Constants.SAK_USER_PROFILE);
                if (profile != null) {
                    Person person = profile.getPerson();
                    userId = profile.getUserId();
                    useName = person.getUseName();
                }
                synchronized (sessions) {
                    if (sessions.containsKey(sessionId)) {
                        registration = (ServiceRegistration) sessions.get(sessionId);
                    }
                }
            }
        }
        if (registration == null) {
            registration = new ServiceRegistration();
            registration.setThreadId(threadId);
            registration.setDateTime(dateTime);
            registration.setSystemName(systemName);
            registration.setSessionId(sessionId);
            registration.setUserId(userId);
            registration.setUseName(useName);
            registration.setServletRequest(req);
            synchronized (threads) {
                threads.put(threadId, registration);
            }
            if (sessionId != null) {
                synchronized (sessions) {
                    sessions.put(sessionId, registration);
                }
            }
        } else {
            registration.setThreadId(threadId);
            registration.setDateTime(dateTime);
            registration.setSystemName(systemName);
            registration.setUserId(userId);
            registration.setUseName(useName);
            registration.setServletRequest(req);
            synchronized (threads) {
                threads.put(threadId, registration);
            }
        }
        runningCount = runningCount + 1;
        if (runningCount > periodicLimit) {
            runningCount = 0;
            periodicHouseCleaning();
        }
    }
    public static boolean isAuthenticated() {
        return isAuthenticated(getServletRequest());
    }
    public static boolean isAuthenticated(HttpServletRequest req) {
        ServiceLocator serviceLocator = null;
        try {
            serviceLocator = getServiceLocator();
        } catch (Exception e) {
            logWarn(e.toString(), e);
        }
        if (serviceLocator != null) {
            return serviceLocator.isAuthenticated(req);
        } else {
            return false;
        }
    }
    public static boolean isAuthorized(String type, String resource) {
        return isAuthorized(type, resource, null, getServletRequest());
    }
    public static boolean isAuthorized(String type, String resource, String function) {
        return isAuthorized(type, resource, function, getServletRequest());
    }
    public static boolean isAuthorized(String type, String resource, HttpServletRequest req) {
        return isAuthorized(type, resource, null, req);
    }
    public static boolean isAuthorized(String type, String resource, String function, HttpServletRequest req) {
        ServiceLocator serviceLocator = null;
        try {
            serviceLocator = getServiceLocator();
        } catch (Exception e) {
            logWarn(e.toString(), e);
        }
        if (serviceLocator != null) {
            return serviceLocator.isAuthorized(req, type, resource, function);
        } else {
            return false;
        }
    }
    public static String getRealPath(String path) {
        ServiceLocator serviceLocator = null;
        try {
            serviceLocator = getServiceLocator();
        } catch (Exception e) {
            logWarn(e.toString(), e);
        }
        String returnValue = null;
        if (serviceLocator != null) {
            returnValue = serviceLocator.getRealPath(path);
        }
        return returnValue;
    }
    public static Properties getProperties() {
        ServiceLocator serviceLocator = null;
        try {
            serviceLocator = getServiceLocator();
        } catch (Exception e) {
            logWarn(e.toString(), e);
        }
        Properties returnValue = null;
        if (serviceLocator != null) {
            returnValue = serviceLocator.getProperties();
        }
        return returnValue;
    }
    public static Properties getProperties(String source) {
        ServiceLocator serviceLocator = null;
        try {
            serviceLocator = getServiceLocator();
        } catch (Exception e) {
            logWarn(e.toString(), e);
        }
        Properties returnValue = null;
        if (serviceLocator != null) {
            returnValue = serviceLocator.getProperties(source);
        }
        return returnValue;
    }
    public static MessageResources getApplicationResources() {
        ServiceLocator serviceLocator = null;
        try {
            serviceLocator = getServiceLocator();
        } catch (Exception e) {
            logWarn(e.toString(), e);
        }
        MessageResources returnValue = null;
        if (serviceLocator != null) {
            returnValue = serviceLocator.getApplicationResources();
        }
        return returnValue;
    }
    public static String getNavigation() {
        return getNavigation(getServletRequest());
    }
    public static String getNavigation(HttpServletRequest req) {
        ServiceLocator serviceLocator = null;
        try {
            serviceLocator = getServiceLocator();
        } catch (Exception e) {
            logWarn(e.toString(), e);
        }
        String formattedNavigation = Constants.EMPTY_STRING;
        if (serviceLocator != null) {
            formattedNavigation = serviceLocator.getNavigation(req);
        }
        return formattedNavigation;
    }
    public static void setUserMessage(UserMessage message) {
        setUserMessage(getServletRequest(), message);
    }
    public static void setUserMessage(HttpServletRequest req, UserMessage message) {
        ServiceLocator serviceLocator = null;
        try {
            serviceLocator = getServiceLocator();
        } catch (Exception e) {
            logWarn(e.toString(), e);
        }
        if (serviceLocator != null) {
            serviceLocator.setUserMessage(req, message);
        }
    }
    public static DeliveredNotice notify(String destination, String templateId, Object sourceData) {
        DeliveredNotice deliveredNotice = null;
        ServiceLocator serviceLocator = null;
        try {
            serviceLocator = getServiceLocator();
        } catch (Exception e) {
            logWarn(e.toString(), e);
        }
        if (serviceLocator != null) {
            deliveredNotice = serviceLocator.notify(destination, templateId, sourceData);
        }
        return deliveredNotice;
    }
    public static DeliveredNotice notify(String destination, NoticeTemplate template, Object sourceData) {
        DeliveredNotice deliveredNotice = null;
        ServiceLocator serviceLocator = null;
        try {
            serviceLocator = getServiceLocator();
        } catch (Exception e) {
            logWarn(e.toString(), e);
        }
        if (serviceLocator != null) {
            deliveredNotice = serviceLocator.notify(destination, template, sourceData);
        }
        return deliveredNotice;
    }
    public static DeliveredNotice notify(String destination, String templateId, Object sourceData, String deliveryMethod) {
        DeliveredNotice deliveredNotice = null;
        ServiceLocator serviceLocator = null;
        try {
            serviceLocator = getServiceLocator();
        } catch (Exception e) {
            logWarn(e.toString(), e);
        }
        if (serviceLocator != null) {
            deliveredNotice = serviceLocator.notify(destination, templateId, sourceData, deliveryMethod);
        }
        return deliveredNotice;
    }
    public static DeliveredNotice notify(String destination, NoticeTemplate template, Object sourceData, String deliveryMethod) {
        DeliveredNotice deliveredNotice = null;
        ServiceLocator serviceLocator = null;
        try {
            serviceLocator = getServiceLocator();
        } catch (Exception e) {
            logWarn(e.toString(), e);
        }
        if (serviceLocator != null) {
            deliveredNotice = serviceLocator.notify(destination, template, sourceData, deliveryMethod);
        }
        return deliveredNotice;
    }
    public static void handleSafeEvent(Event event) {
        handleSafeEvent(getServletRequest(), event);
    }
    public static void handleSafeEvent(HttpServletRequest req, Event event) {
        try {
            handleEvent(req, event);
        } catch (Exception e) {
            Exception x = new EventHandlingException(new EventDetails(event, null, req), e.toString(), e);
            logError(x.toString(), x);
            logError(e.toString(), e);
        }
        return;
    }
    public static void handleEvent(Event event) throws EventHandlingException, EventDrivenException {
        handleEvent(getServletRequest(), event);
    }
    public static void handleEvent(HttpServletRequest req, Event event) throws EventHandlingException, EventDrivenException {
        ServiceLocator serviceLocator = null;
        try {
            serviceLocator = getServiceLocator();
        } catch (Exception e) {
            logWarn(e.toString(), e);
        }
        if (serviceLocator == null) {
            throw new EventHandlingException(new EventDetails(event, null, req), null, null);
        } else {
            serviceLocator.handleEvent(req, event);
        }
    }
    public static Class getClass(String className) {
        ServiceLocator serviceLocator = null;
        try {
            serviceLocator = getServiceLocator();
        } catch (Exception e) {
            log.warn(e.toString(), e);
        }
        if (serviceLocator != null) {
            return serviceLocator.getClass(className);
        } else {
            return null;
        }
    }
    public static Object getInstance(String className) {
        ServiceLocator serviceLocator = null;
        try {
            serviceLocator = getServiceLocator();
        } catch (Exception e) {
            log.warn(e.toString(), e);
        }
        if (serviceLocator != null) {
            return serviceLocator.getInstance(className);
        } else {
            return null;
        }
    }
    public static Object getNewInstance(String className) {
        ServiceLocator serviceLocator = null;
        try {
            serviceLocator = getServiceLocator();
        } catch (Exception e) {
            log.warn(e.toString(), e);
        }
        if (serviceLocator != null) {
            return serviceLocator.getNewInstance(className);
        } else {
            return null;
        }
    }
    public static void logTrace(Object message) {
        ServiceLocator serviceLocator = null;
        try {
            serviceLocator = getServiceLocator();
        } catch (Exception e) {
            logWarn(e.toString(), e);
        }
        if (serviceLocator != null) {
            serviceLocator.logTrace(getServletRequest(), message, null);
        } else {
            log.trace(message);
        }
    }
    public static void logTrace(Object message, Throwable t) {
        ServiceLocator serviceLocator = null;
        try {
            serviceLocator = getServiceLocator();
        } catch (Exception e) {
            logWarn(e.toString(), e);
        }
        if (serviceLocator != null) {
            serviceLocator.logTrace(getServletRequest(), message, t);
        } else {
            log.trace(message, t);
        }
    }
    public static void logDebug(Object message) {
        ServiceLocator serviceLocator = null;
        try {
            serviceLocator = getServiceLocator();
        } catch (Exception e) {
            logWarn(e.toString(), e);
        }
        if (serviceLocator != null) {
            serviceLocator.logDebug(getServletRequest(), message, null);
        } else {
            log.debug(message);
        }
    }
    public static void logDebug(Object message, Throwable t) {
        ServiceLocator serviceLocator = null;
        try {
            serviceLocator = getServiceLocator();
        } catch (Exception e) {
            logWarn(e.toString(), e);
        }
        if (serviceLocator != null) {
            serviceLocator.logDebug(getServletRequest(), message, t);
        } else {
            log.debug(message, t);
        }
    }
    public static void logInfo(Object message) {
        ServiceLocator serviceLocator = null;
        try {
            serviceLocator = getServiceLocator();
        } catch (Exception e) {
            logWarn(e.toString(), e);
        }
        if (serviceLocator != null) {
            serviceLocator.logInfo(getServletRequest(), message, null);
        } else {
            log.info(message);
        }
    }
    public static void logInfo(Object message, Throwable t) {
        ServiceLocator serviceLocator = null;
        try {
            serviceLocator = getServiceLocator();
        } catch (Exception e) {
            logWarn(e.toString(), e);
        }
        if (serviceLocator != null) {
            serviceLocator.logInfo(getServletRequest(), message, t);
        } else {
            log.info(message, t);
        }
    }
    public static void logWarn(Object message) {
        ServiceLocator serviceLocator = null;
        try {
            serviceLocator = getServiceLocator();
        } catch (Exception e) {
            logWarn(e.toString(), e);
        }
        if (serviceLocator != null) {
            serviceLocator.logWarn(getServletRequest(), message, null);
        } else {
            log.warn(message);
        }
    }
    public static void logWarn(Object message, Throwable t) {
        ServiceLocator serviceLocator = null;
        try {
            serviceLocator = getServiceLocator();
        } catch (Exception e) {
            if (e != null) {
                log.fatal(e.toString(), e);
                if (e.getClass().isAssignableFrom(EaasyStreetSystemError.class)) {
                    EaasyStreetSystemError ese = (EaasyStreetSystemError) e;
                    if (ese.getRootCause() != null) {
                        log.fatal(ese.getRootCause().toString(), ese.getRootCause());
                    }
                }
            }
        }
        if (serviceLocator != null) {
            serviceLocator.logWarn(getServletRequest(), message, t);
        } else {
            log.warn(message, t);
        }
    }
    public static void logError(Object message) {
        ServiceLocator serviceLocator = null;
        try {
            serviceLocator = getServiceLocator();
        } catch (Exception e) {
            logWarn(e.toString(), e);
        }
        if (serviceLocator != null) {
            serviceLocator.logError(getServletRequest(), message, null);
        } else {
            log.error(message);
        }
    }
    public static void logError(Object message, Throwable t) {
        ServiceLocator serviceLocator = null;
        try {
            serviceLocator = getServiceLocator();
        } catch (Exception e) {
            logWarn(e.toString(), e);
        }
        if (serviceLocator != null) {
            serviceLocator.logError(getServletRequest(), message, t);
        } else {
            log.error(message, t);
        }
    }
    public static void logFatal(Object message) {
        ServiceLocator serviceLocator = null;
        try {
            serviceLocator = getServiceLocator();
        } catch (Exception e) {
            logWarn(e.toString(), e);
        }
        if (serviceLocator != null) {
            serviceLocator.logFatal(getServletRequest(), message, null);
        } else {
            log.fatal(message);
        }
    }
    public static void logFatal(Object message, Throwable t) {
        ServiceLocator serviceLocator = null;
        try {
            serviceLocator = getServiceLocator();
        } catch (Exception e) {
            logWarn(e.toString(), e);
        }
        if (serviceLocator != null) {
            serviceLocator.logFatal(getServletRequest(), message, t);
        } else {
            log.fatal(message, t);
        }
    }
    public static String getPresentationTheme() {
        ServiceLocator serviceLocator = null;
        try {
            serviceLocator = getServiceLocator();
        } catch (Exception e) {
            logWarn(e.toString(), e);
        }
        if (serviceLocator != null) {
            return serviceLocator.getPresentationTheme(getServletRequest());
        } else {
            return null;
        }
    }
    public static String getThemeProperty(String key) {
        return getThemeProperty(getPresentationTheme(), key);
    }
    public static String getThemeProperty(String theme, String key) {
        ServiceLocator serviceLocator = null;
        try {
            serviceLocator = getServiceLocator();
        } catch (Exception e) {
            logWarn(e.toString(), e);
        }
        if (serviceLocator != null) {
            return serviceLocator.getThemeProperty(theme, key);
        } else {
            return null;
        }
    }
    public static String getProperty(String key) {
        ServiceLocator serviceLocator = null;
        try {
            serviceLocator = getServiceLocator();
        } catch (Exception e) {
            logWarn(e.toString(), e);
        }
        if (serviceLocator != null) {
            return serviceLocator.getProperty(key);
        } else {
            return null;
        }
    }
    public static Object getContextAttribute(String key) {
        ServiceLocator serviceLocator = null;
        try {
            serviceLocator = getServiceLocator();
        } catch (Exception e) {
            logWarn(e.toString(), e);
        }
        if (serviceLocator != null) {
            return serviceLocator.getContextAttribute(key);
        } else {
            return null;
        }
    }
    public static void setContextAttribute(String key, Object value) {
        ServiceLocator serviceLocator = null;
        try {
            serviceLocator = getServiceLocator();
        } catch (Exception e) {
            logWarn(e.toString(), e);
        }
        if (serviceLocator != null) {
            serviceLocator.setContextAttribute(key, value);
        }
    }
    public static void removeContextAttribute(String key) {
        ServiceLocator serviceLocator = null;
        try {
            serviceLocator = getServiceLocator();
        } catch (Exception e) {
            logWarn(e.toString(), e);
        }
        if (serviceLocator != null) {
            serviceLocator.removeContextAttribute(key);
        }
    }
    public static boolean isEaasyStreetActive() {
        boolean isActive = false;
        String thisThread = Thread.currentThread().getName();
        if (threads.containsKey(thisThread)) {
            ServiceRegistration registration = (ServiceRegistration) threads.get(thisThread);
            String systemName = registration.getSystemName();
            if (serviceLocators.containsKey(systemName)) {
                ServiceLocator serviceLocator = (ServiceLocator) serviceLocators.get(systemName);
                if (serviceLocator.getInitializationError() == null) {
                    isActive = true;
                }
            }
        }
        return isActive;
    }
    public static HttpServletRequest getServletRequest() {
        HttpServletRequest request = null;
        String thisThread = Thread.currentThread().getName();
        if (threads.containsKey(thisThread)) {
            ServiceRegistration registration = (ServiceRegistration) threads.get(thisThread);
            request = registration.getServletRequest();
        }
        return request;
    }
    private static ServiceLocator getServiceLocator() throws EaasyStreetSystemError {
        ServiceLocator serviceLocator = null;
        String thisThread = Thread.currentThread().getName();
        if (threads.containsKey(thisThread)) {
            ServiceRegistration registration = (ServiceRegistration) threads.get(thisThread);
            String systemName = registration.getSystemName();
            if (serviceLocators.containsKey(systemName)) {
                serviceLocator = (ServiceLocator) serviceLocators.get(systemName);
                if (serviceLocator.getInitializationError() != null) {
                    Throwable ie = serviceLocator.getInitializationError();
                    systemError(EVI0010T, new String[] { systemName, ie.toString() }, ie);
                }
            } else {
                systemError(EVI0004T, new String[] { systemName }, null);
            }
        } else {
            systemError(EVI0011T, null, null);
        }
        return serviceLocator;
    }
    private static void systemError(String internalEventKey, Object[] args, Throwable rootCause) throws EaasyStreetSystemError {
        log.error(localStrings.getMessage(internalEventKey, args), rootCause);
        throw new EaasyStreetSystemError(localStrings.getMessage(internalEventKey, args), rootCause);
    }
    private static void periodicHouseCleaning() {
        log.info("[In] EaasyStreet.periodicHouseCleaning()");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -1);
        Date cutOff = cal.getTime();
        log.info("Cut off date/time = " + cutOff);
        log.info("Thread size (before) = " + threads.size());
        log.info("Session size (before) = " + sessions.size());
        ArrayList keys = new ArrayList();
        synchronized (threads) {
            Iterator i = threads.keySet().iterator();
            while (i.hasNext()) {
                String key = (String) i.next();
                ServiceRegistration registration = (ServiceRegistration) threads.get(key);
                Date regDate = registration.getDateTime();
                if (regDate.before(cutOff)) {
                    keys.add(key);
                }
            }
            i = keys.iterator();
            while (i.hasNext()) {
                threads.remove(i.next());
            }
        }
        keys = new ArrayList();
        synchronized (sessions) {
            Iterator i = sessions.keySet().iterator();
            while (i.hasNext()) {
                String key = (String) i.next();
                ServiceRegistration registration = (ServiceRegistration) sessions.get(key);
                Date regDate = registration.getDateTime();
                if (regDate.before(cutOff)) {
                    keys.add(key);
                }
            }
            i = keys.iterator();
            while (i.hasNext()) {
                sessions.remove(i.next());
            }
        }
        log.info("Thread size (after) = " + threads.size());
        log.info("Session size (after) = " + sessions.size());
        log.info("[Out] EaasyStreet.periodicHouseCleaning()");
    }
}
