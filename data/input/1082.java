class WindowsPreferences extends AbstractPreferences{
    private static PlatformLogger logger;
    private static final byte[] WINDOWS_ROOT_PATH
                               = stringToByteArray("Software\\JavaSoft\\Prefs");
    private static final int HKEY_CURRENT_USER = 0x80000001;
    private static final int HKEY_LOCAL_MACHINE = 0x80000002;
    private static final int USER_ROOT_NATIVE_HANDLE = HKEY_CURRENT_USER;
    private static final int SYSTEM_ROOT_NATIVE_HANDLE = HKEY_LOCAL_MACHINE;
    private static final int MAX_WINDOWS_PATH_LENGTH = 256;
    static final Preferences userRoot =
         new WindowsPreferences(USER_ROOT_NATIVE_HANDLE, WINDOWS_ROOT_PATH);
    static final Preferences systemRoot =
        new WindowsPreferences(SYSTEM_ROOT_NATIVE_HANDLE, WINDOWS_ROOT_PATH);
    private static final int ERROR_SUCCESS = 0;
    private static final int ERROR_FILE_NOT_FOUND = 2;
    private static final int ERROR_ACCESS_DENIED = 5;
    private static final int NATIVE_HANDLE = 0;
    private static final int ERROR_CODE = 1;
    private static final int SUBKEYS_NUMBER = 0;
    private static final int VALUES_NUMBER = 2;
    private static final int MAX_KEY_LENGTH = 3;
    private static final int MAX_VALUE_NAME_LENGTH = 4;
    private static final int DISPOSITION = 2;
    private static final int REG_CREATED_NEW_KEY = 1;
    private static final int REG_OPENED_EXISTING_KEY = 2;
    private static final int NULL_NATIVE_HANDLE = 0;
    private static final int DELETE = 0x10000;
    private static final int KEY_QUERY_VALUE = 1;
    private static final int KEY_SET_VALUE = 2;
    private static final int KEY_CREATE_SUB_KEY = 4;
    private static final int KEY_ENUMERATE_SUB_KEYS = 8;
    private static final int KEY_READ = 0x20019;
    private static final int KEY_WRITE = 0x20006;
    private static final int KEY_ALL_ACCESS = 0xf003f;
    private static int INIT_SLEEP_TIME = 50;
    private static int MAX_ATTEMPTS = 5;
    private boolean isBackingStoreAvailable = true;
    private static native int[] WindowsRegOpenKey(int hKey, byte[] subKey,
                                                         int securityMask);
    private static int[] WindowsRegOpenKey1(int hKey, byte[] subKey,
                                                      int securityMask) {
        int[] result = WindowsRegOpenKey(hKey, subKey, securityMask);
        if (result[ERROR_CODE] == ERROR_SUCCESS) {
            return result;
        } else if (result[ERROR_CODE] == ERROR_FILE_NOT_FOUND) {
            logger().warning("Trying to recreate Windows registry node " +
            byteArrayToString(subKey) + " at root 0x" +
            Integer.toHexString(hKey) + ".");
            int handle = WindowsRegCreateKeyEx(hKey, subKey)[NATIVE_HANDLE];
            WindowsRegCloseKey(handle);
            return WindowsRegOpenKey(hKey, subKey, securityMask);
        } else if (result[ERROR_CODE] != ERROR_ACCESS_DENIED) {
            long sleepTime = INIT_SLEEP_TIME;
            for (int i = 0; i < MAX_ATTEMPTS; i++) {
            try {
                Thread.sleep(sleepTime);
            } catch(InterruptedException e) {
                return result;
            }
            sleepTime *= 2;
            result = WindowsRegOpenKey(hKey, subKey, securityMask);
            if (result[ERROR_CODE] == ERROR_SUCCESS) {
                return result;
            }
            }
        }
        return result;
    }
    private static native int WindowsRegCloseKey(int hKey);
    private static native int[] WindowsRegCreateKeyEx(int hKey, byte[] subKey);
    private static int[] WindowsRegCreateKeyEx1(int hKey, byte[] subKey) {
        int[] result = WindowsRegCreateKeyEx(hKey, subKey);
        if (result[ERROR_CODE] == ERROR_SUCCESS) {
                return result;
            } else {
                long sleepTime = INIT_SLEEP_TIME;
                for (int i = 0; i < MAX_ATTEMPTS; i++) {
                try {
                    Thread.sleep(sleepTime);
                } catch(InterruptedException e) {
                    return result;
                }
                sleepTime *= 2;
                result = WindowsRegCreateKeyEx(hKey, subKey);
                if (result[ERROR_CODE] == ERROR_SUCCESS) {
                return result;
                }
            }
        }
        return result;
    }
    private static native int WindowsRegDeleteKey(int hKey, byte[] subKey);
    private static native int WindowsRegFlushKey(int hKey);
    private static int WindowsRegFlushKey1(int hKey) {
        int result = WindowsRegFlushKey(hKey);
        if (result == ERROR_SUCCESS) {
                return result;
            } else {
                long sleepTime = INIT_SLEEP_TIME;
                for (int i = 0; i < MAX_ATTEMPTS; i++) {
                try {
                    Thread.sleep(sleepTime);
                } catch(InterruptedException e) {
                    return result;
                }
                sleepTime *= 2;
                result = WindowsRegFlushKey(hKey);
                if (result == ERROR_SUCCESS) {
                return result;
                }
            }
        }
        return result;
    }
    private static native byte[] WindowsRegQueryValueEx(int hKey,
                                                              byte[] valueName);
    private static native int WindowsRegSetValueEx(int hKey, byte[] valueName,
                                                         byte[] value);
    private static int WindowsRegSetValueEx1(int hKey, byte[] valueName,
                                                         byte[] value) {
        int result = WindowsRegSetValueEx(hKey, valueName, value);
        if (result == ERROR_SUCCESS) {
                return result;
            } else {
                long sleepTime = INIT_SLEEP_TIME;
                for (int i = 0; i < MAX_ATTEMPTS; i++) {
                try {
                    Thread.sleep(sleepTime);
                } catch(InterruptedException e) {
                    return result;
                }
                sleepTime *= 2;
                result = WindowsRegSetValueEx(hKey, valueName, value);
                if (result == ERROR_SUCCESS) {
                return result;
                }
            }
        }
        return result;
    }
    private static native int WindowsRegDeleteValue(int hKey, byte[] valueName);
    private static native int[] WindowsRegQueryInfoKey(int hKey);
    private static int[] WindowsRegQueryInfoKey1(int hKey) {
        int[] result = WindowsRegQueryInfoKey(hKey);
        if (result[ERROR_CODE] == ERROR_SUCCESS) {
                return result;
            } else {
                long sleepTime = INIT_SLEEP_TIME;
                for (int i = 0; i < MAX_ATTEMPTS; i++) {
                try {
                    Thread.sleep(sleepTime);
                } catch(InterruptedException e) {
                    return result;
                }
                sleepTime *= 2;
                result = WindowsRegQueryInfoKey(hKey);
                if (result[ERROR_CODE] == ERROR_SUCCESS) {
                return result;
                }
            }
        }
        return result;
    }
    private static native byte[] WindowsRegEnumKeyEx(int hKey, int subKeyIndex,
                                      int maxKeyLength);
    private static byte[] WindowsRegEnumKeyEx1(int hKey, int subKeyIndex,
                                      int maxKeyLength) {
        byte[] result = WindowsRegEnumKeyEx(hKey, subKeyIndex, maxKeyLength);
        if (result != null) {
                return result;
            } else {
                long sleepTime = INIT_SLEEP_TIME;
                for (int i = 0; i < MAX_ATTEMPTS; i++) {
                try {
                    Thread.sleep(sleepTime);
                } catch(InterruptedException e) {
                    return result;
                }
                sleepTime *= 2;
                result = WindowsRegEnumKeyEx(hKey, subKeyIndex, maxKeyLength);
                if (result != null) {
                return result;
                }
            }
        }
        return result;
    }
    private static native byte[] WindowsRegEnumValue(int hKey, int valueIndex,
                                      int maxValueNameLength);
    private static byte[] WindowsRegEnumValue1(int hKey, int valueIndex,
                                      int maxValueNameLength) {
        byte[] result = WindowsRegEnumValue(hKey, valueIndex,
                                                            maxValueNameLength);
        if (result != null) {
                return result;
            } else {
                long sleepTime = INIT_SLEEP_TIME;
                for (int i = 0; i < MAX_ATTEMPTS; i++) {
                try {
                    Thread.sleep(sleepTime);
                } catch(InterruptedException e) {
                    return result;
                }
                sleepTime *= 2;
                result = WindowsRegEnumValue(hKey, valueIndex,
                                                            maxValueNameLength);
                if (result != null) {
                return result;
                }
            }
        }
        return result;
    }
    private WindowsPreferences(WindowsPreferences parent, String name) {
        super(parent, name);
        int parentNativeHandle = parent.openKey(KEY_CREATE_SUB_KEY, KEY_READ);
        if (parentNativeHandle == NULL_NATIVE_HANDLE) {
            isBackingStoreAvailable = false;
            return;
        }
        int[] result =
               WindowsRegCreateKeyEx1(parentNativeHandle, toWindowsName(name));
        if (result[ERROR_CODE] != ERROR_SUCCESS) {
            logger().warning("Could not create windows registry "
            + "node " + byteArrayToString(windowsAbsolutePath()) +
            " at root 0x" + Integer.toHexString(rootNativeHandle()) +
            ". Windows RegCreateKeyEx(...) returned error code " +
            result[ERROR_CODE] + ".");
            isBackingStoreAvailable = false;
            return;
        }
        newNode = (result[DISPOSITION] == REG_CREATED_NEW_KEY);
        closeKey(parentNativeHandle);
        closeKey(result[NATIVE_HANDLE]);
    }
    private  WindowsPreferences(int rootNativeHandle, byte[] rootDirectory) {
        super(null,"");
        int[] result =
                WindowsRegCreateKeyEx1(rootNativeHandle, rootDirectory);
        if (result[ERROR_CODE] != ERROR_SUCCESS) {
            logger().warning("Could not open/create prefs root node " +
            byteArrayToString(windowsAbsolutePath()) + " at root 0x" +
            Integer.toHexString(rootNativeHandle()) +
            ". Windows RegCreateKeyEx(...) returned error code " +
            result[ERROR_CODE] + ".");
            isBackingStoreAvailable = false;
            return;
        }
        newNode = (result[DISPOSITION] == REG_CREATED_NEW_KEY);
        closeKey(result[NATIVE_HANDLE]);
    }
    private byte[] windowsAbsolutePath() {
        ByteArrayOutputStream bstream = new ByteArrayOutputStream();
        bstream.write(WINDOWS_ROOT_PATH, 0, WINDOWS_ROOT_PATH.length-1);
        StringTokenizer tokenizer = new StringTokenizer(absolutePath(),"/");
        while (tokenizer.hasMoreTokens()) {
            bstream.write((byte)'\\');
            String nextName = tokenizer.nextToken();
            byte[] windowsNextName = toWindowsName(nextName);
            bstream.write(windowsNextName, 0, windowsNextName.length-1);
        }
        bstream.write(0);
        return bstream.toByteArray();
    }
    private int openKey(int securityMask) {
        return openKey(securityMask, securityMask);
    }
    private int openKey(int mask1, int mask2) {
        return openKey(windowsAbsolutePath(), mask1,  mask2);
    }
    private int openKey(byte[] windowsAbsolutePath, int mask1, int mask2) {
        if (windowsAbsolutePath.length <= MAX_WINDOWS_PATH_LENGTH + 1) {
             int[] result = WindowsRegOpenKey1(rootNativeHandle(),
                                               windowsAbsolutePath, mask1);
             if (result[ERROR_CODE] == ERROR_ACCESS_DENIED && mask2 != mask1)
                 result = WindowsRegOpenKey1(rootNativeHandle(),
                                             windowsAbsolutePath, mask2);
             if (result[ERROR_CODE] != ERROR_SUCCESS) {
                logger().warning("Could not open windows "
                + "registry node " + byteArrayToString(windowsAbsolutePath()) +
                " at root 0x" + Integer.toHexString(rootNativeHandle()) +
                ". Windows RegOpenKey(...) returned error code " +
                result[ERROR_CODE] + ".");
                result[NATIVE_HANDLE] = NULL_NATIVE_HANDLE;
                if (result[ERROR_CODE] == ERROR_ACCESS_DENIED) {
                    throw new SecurityException("Could not open windows "
                + "registry node " + byteArrayToString(windowsAbsolutePath()) +
                " at root 0x" + Integer.toHexString(rootNativeHandle()) +
                ": Access denied");
                }
             }
             return result[NATIVE_HANDLE];
        } else {
            return openKey(rootNativeHandle(), windowsAbsolutePath, mask1, mask2);
        }
    }
    private int openKey(int nativeHandle, byte[] windowsRelativePath,
                        int mask1, int mask2) {
        if (windowsRelativePath.length <= MAX_WINDOWS_PATH_LENGTH + 1 ) {
             int[] result = WindowsRegOpenKey1(nativeHandle,
                                               windowsRelativePath, mask1);
             if (result[ERROR_CODE] == ERROR_ACCESS_DENIED && mask2 != mask1)
                 result = WindowsRegOpenKey1(nativeHandle,
                                             windowsRelativePath, mask2);
             if (result[ERROR_CODE] != ERROR_SUCCESS) {
                logger().warning("Could not open windows "
                + "registry node " + byteArrayToString(windowsAbsolutePath()) +
                " at root 0x" + Integer.toHexString(nativeHandle) +
                ". Windows RegOpenKey(...) returned error code " +
                result[ERROR_CODE] + ".");
                result[NATIVE_HANDLE] = NULL_NATIVE_HANDLE;
             }
             return result[NATIVE_HANDLE];
        } else {
            int separatorPosition = -1;
            for (int i = MAX_WINDOWS_PATH_LENGTH; i > 0; i--) {
                if (windowsRelativePath[i] == ((byte)'\\')) {
                    separatorPosition = i;
                    break;
                }
            }
            byte[] nextRelativeRoot = new byte[separatorPosition+1];
            System.arraycopy(windowsRelativePath, 0, nextRelativeRoot,0,
                                                      separatorPosition);
            nextRelativeRoot[separatorPosition] = 0;
            byte[] nextRelativePath = new byte[windowsRelativePath.length -
                                      separatorPosition - 1];
            System.arraycopy(windowsRelativePath, separatorPosition+1,
                             nextRelativePath, 0, nextRelativePath.length);
            int nextNativeHandle = openKey(nativeHandle, nextRelativeRoot,
                                           mask1, mask2);
            if (nextNativeHandle == NULL_NATIVE_HANDLE) {
                return NULL_NATIVE_HANDLE;
            }
            int result = openKey(nextNativeHandle, nextRelativePath,
                                 mask1,mask2);
            closeKey(nextNativeHandle);
            return result;
        }
    }
    private void closeKey(int nativeHandle) {
        int result = WindowsRegCloseKey(nativeHandle);
        if (result != ERROR_SUCCESS) {
            logger().warning("Could not close windows "
            + "registry node " + byteArrayToString(windowsAbsolutePath()) +
            " at root 0x" + Integer.toHexString(rootNativeHandle()) +
            ". Windows RegCloseKey(...) returned error code " + result + ".");
        }
    }
    protected void putSpi(String javaName, String value) {
    int nativeHandle = openKey(KEY_SET_VALUE);
    if (nativeHandle == NULL_NATIVE_HANDLE) {
        isBackingStoreAvailable = false;
        return;
    }
    int result =  WindowsRegSetValueEx1(nativeHandle,
                          toWindowsName(javaName), toWindowsValueString(value));
    if (result != ERROR_SUCCESS) {
        logger().warning("Could not assign value to key " +
        byteArrayToString(toWindowsName(javaName))+ " at Windows registry node "
       + byteArrayToString(windowsAbsolutePath()) + " at root 0x"
       + Integer.toHexString(rootNativeHandle()) +
       ". Windows RegSetValueEx(...) returned error code " + result + ".");
        isBackingStoreAvailable = false;
        }
    closeKey(nativeHandle);
    }
    protected String getSpi(String javaName) {
    int nativeHandle = openKey(KEY_QUERY_VALUE);
    if (nativeHandle == NULL_NATIVE_HANDLE) {
        return null;
    }
    Object resultObject =  WindowsRegQueryValueEx(nativeHandle,
                                                  toWindowsName(javaName));
    if (resultObject == null) {
        closeKey(nativeHandle);
        return null;
    }
    closeKey(nativeHandle);
    return toJavaValueString((byte[]) resultObject);
    }
    protected void removeSpi(String key) {
        int nativeHandle = openKey(KEY_SET_VALUE);
        if (nativeHandle == NULL_NATIVE_HANDLE) {
        return;
        }
        int result =
            WindowsRegDeleteValue(nativeHandle, toWindowsName(key));
        if (result != ERROR_SUCCESS && result != ERROR_FILE_NOT_FOUND) {
            logger().warning("Could not delete windows registry "
            + "value " + byteArrayToString(windowsAbsolutePath())+ "\\" +
            toWindowsName(key) + " at root 0x" +
            Integer.toHexString(rootNativeHandle()) +
            ". Windows RegDeleteValue(...) returned error code " +
            result + ".");
            isBackingStoreAvailable = false;
        }
        closeKey(nativeHandle);
    }
    protected String[] keysSpi() throws BackingStoreException{
        int nativeHandle = openKey(KEY_QUERY_VALUE);
        if (nativeHandle == NULL_NATIVE_HANDLE) {
            throw new BackingStoreException("Could not open windows"
            + "registry node " + byteArrayToString(windowsAbsolutePath()) +
            " at root 0x" + Integer.toHexString(rootNativeHandle()) + ".");
        }
        int[] result =  WindowsRegQueryInfoKey1(nativeHandle);
        if (result[ERROR_CODE] != ERROR_SUCCESS) {
            String info = "Could not query windows"
            + "registry node " + byteArrayToString(windowsAbsolutePath()) +
            " at root 0x" + Integer.toHexString(rootNativeHandle()) +
            ". Windows RegQueryInfoKeyEx(...) returned error code " +
            result[ERROR_CODE] + ".";
            logger().warning(info);
            throw new BackingStoreException(info);
        }
        int maxValueNameLength = result[MAX_VALUE_NAME_LENGTH];
        int valuesNumber = result[VALUES_NUMBER];
        if (valuesNumber == 0) {
            closeKey(nativeHandle);
            return new String[0];
       }
       String[] valueNames = new String[valuesNumber];
       for (int i = 0; i < valuesNumber; i++) {
            byte[] windowsName = WindowsRegEnumValue1(nativeHandle, i,
                                                        maxValueNameLength+1);
            if (windowsName == null) {
                String info =
                "Could not enumerate value #" + i + "  of windows node " +
                byteArrayToString(windowsAbsolutePath()) + " at root 0x" +
                Integer.toHexString(rootNativeHandle()) + ".";
                logger().warning(info);
                throw new BackingStoreException(info);
            }
            valueNames[i] = toJavaName(windowsName);
        }
        closeKey(nativeHandle);
        return valueNames;
    }
    protected String[] childrenNamesSpi() throws BackingStoreException {
        int nativeHandle = openKey(KEY_ENUMERATE_SUB_KEYS| KEY_QUERY_VALUE);
        if (nativeHandle == NULL_NATIVE_HANDLE) {
            throw new BackingStoreException("Could not open windows"
            + "registry node " + byteArrayToString(windowsAbsolutePath()) +
            " at root 0x" + Integer.toHexString(rootNativeHandle()) + ".");
        }
        int[] result =  WindowsRegQueryInfoKey1(nativeHandle);
        if (result[ERROR_CODE] != ERROR_SUCCESS) {
            String info = "Could not query windows"
            + "registry node " + byteArrayToString(windowsAbsolutePath()) +
            " at root 0x" + Integer.toHexString(rootNativeHandle()) +
            ". Windows RegQueryInfoKeyEx(...) returned error code " +
            result[ERROR_CODE] + ".";
            logger().warning(info);
            throw new BackingStoreException(info);
        }
        int maxKeyLength = result[MAX_KEY_LENGTH];
        int subKeysNumber = result[SUBKEYS_NUMBER];
        if (subKeysNumber == 0) {
            closeKey(nativeHandle);
            return new String[0];
        }
        String[] subkeys = new String[subKeysNumber];
        String[] children = new String[subKeysNumber];
        for (int i = 0; i < subKeysNumber; i++) {
            byte[] windowsName = WindowsRegEnumKeyEx1(nativeHandle, i,
                                                                maxKeyLength+1);
            if (windowsName == null) {
                String info =
                "Could not enumerate key #" + i + "  of windows node " +
                byteArrayToString(windowsAbsolutePath()) + " at root 0x" +
                Integer.toHexString(rootNativeHandle()) + ". ";
                logger().warning(info);
                throw new BackingStoreException(info);
            }
            String javaName = toJavaName(windowsName);
            children[i] = javaName;
        }
        closeKey(nativeHandle);
        return children;
    }
    public void flush() throws BackingStoreException{
        if (isRemoved()) {
            parent.flush();
            return;
        }
        if (!isBackingStoreAvailable) {
            throw new BackingStoreException(
                                       "flush(): Backing store not available.");
        }
        int nativeHandle = openKey(KEY_READ);
        if (nativeHandle == NULL_NATIVE_HANDLE) {
            throw new BackingStoreException("Could not open windows"
            + "registry node " + byteArrayToString(windowsAbsolutePath()) +
            " at root 0x" + Integer.toHexString(rootNativeHandle()) + ".");
        }
        int result = WindowsRegFlushKey1(nativeHandle);
        if (result != ERROR_SUCCESS) {
            String info = "Could not flush windows "
            + "registry node " + byteArrayToString(windowsAbsolutePath())
            + " at root 0x" + Integer.toHexString(rootNativeHandle()) +
            ". Windows RegFlushKey(...) returned error code " + result + ".";
            logger().warning(info);
            throw new BackingStoreException(info);
        }
        closeKey(nativeHandle);
    }
    public void sync() throws BackingStoreException{
        if (isRemoved())
            throw new IllegalStateException("Node has been removed");
        flush();
    }
    protected AbstractPreferences childSpi(String name) {
            return new WindowsPreferences(this, name);
    }
    public void removeNodeSpi() throws BackingStoreException {
        int parentNativeHandle =
                         ((WindowsPreferences)parent()).openKey(DELETE);
        if (parentNativeHandle == NULL_NATIVE_HANDLE) {
            throw new BackingStoreException("Could not open parent windows"
            + "registry node of " + byteArrayToString(windowsAbsolutePath()) +
            " at root 0x" + Integer.toHexString(rootNativeHandle()) + ".");
        }
        int result =
                WindowsRegDeleteKey(parentNativeHandle, toWindowsName(name()));
        if (result != ERROR_SUCCESS) {
            String info = "Could not delete windows "
            + "registry node " + byteArrayToString(windowsAbsolutePath()) +
            " at root 0x" + Integer.toHexString(rootNativeHandle()) +
            ". Windows RegDeleteKeyEx(...) returned error code " +
            result + ".";
            logger().warning(info);
            throw new BackingStoreException(info);
        }
        closeKey(parentNativeHandle);
    }
    private static String toJavaName(byte[] windowsNameArray) {
        String windowsName = byteArrayToString(windowsNameArray);
        if ((windowsName.length()>1) &&
                                   (windowsName.substring(0,2).equals("/!"))) {
            return toJavaAlt64Name(windowsName);
        }
        StringBuffer javaName = new StringBuffer();
        char ch;
        for (int i = 0; i < windowsName.length(); i++){
            if ((ch = windowsName.charAt(i)) == '/') {
                char next = ' ';
                if ((windowsName.length() > i + 1) &&
                   ((next = windowsName.charAt(i+1)) >= 'A') && (next <= 'Z')) {
                ch = next;
                i++;
                } else  if ((windowsName.length() > i + 1) && (next == '/')) {
                ch = '\\';
                i++;
                }
            } else if (ch == '\\') {
                ch = '/';
            }
            javaName.append(ch);
        }
        return javaName.toString();
    }
    private static String toJavaAlt64Name(String windowsName) {
        byte[] byteBuffer =
                          Base64.altBase64ToByteArray(windowsName.substring(2));
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < byteBuffer.length; i++) {
            int firstbyte = (byteBuffer[i++] & 0xff);
            int secondbyte =  (byteBuffer[i] & 0xff);
            result.append((char)((firstbyte << 8) + secondbyte));
        }
        return result.toString();
    }
    private static byte[] toWindowsName(String javaName) {
        StringBuffer windowsName = new StringBuffer();
        for (int i = 0; i < javaName.length(); i++) {
            char ch =javaName.charAt(i);
            if ((ch < 0x0020)||(ch > 0x007f)) {
                return toWindowsAlt64Name(javaName);
            }
            if (ch == '\\') {
                windowsName.append("
            } else if (ch == '/') {
                windowsName.append('\\');
            } else if ((ch >= 'A') && (ch <='Z')) {
                windowsName.append("/" + ch);
            } else {
                windowsName.append(ch);
            }
        }
        return stringToByteArray(windowsName.toString());
    }
    private static byte[] toWindowsAlt64Name(String javaName) {
        byte[] javaNameArray = new byte[2*javaName.length()];
        int counter = 0;
        for (int i = 0; i < javaName.length();i++) {
                int ch = javaName.charAt(i);
                javaNameArray[counter++] = (byte)(ch >>> 8);
                javaNameArray[counter++] = (byte)ch;
        }
        return stringToByteArray(
                           "/!" + Base64.byteArrayToAltBase64(javaNameArray));
    }
     private static String toJavaValueString(byte[] windowsNameArray) {
        String windowsName = byteArrayToString(windowsNameArray);
        StringBuffer javaName = new StringBuffer();
        char ch;
        for (int i = 0; i < windowsName.length(); i++){
            if ((ch = windowsName.charAt(i)) == '/') {
                char next = ' ';
                if (windowsName.length() > i + 1 &&
                                    (next = windowsName.charAt(i + 1)) == 'u') {
                    if (windowsName.length() < i + 6){
                        break;
                    } else {
                        ch = (char)Integer.parseInt
                                      (windowsName.substring(i + 2, i + 6), 16);
                        i += 5;
                    }
                } else
                if ((windowsName.length() > i + 1) &&
                          ((windowsName.charAt(i+1)) >= 'A') && (next <= 'Z')) {
                ch = next;
                i++;
                } else  if ((windowsName.length() > i + 1) &&
                                               (next == '/')) {
                ch = '\\';
                i++;
                }
            } else if (ch == '\\') {
                ch = '/';
            }
            javaName.append(ch);
        }
        return javaName.toString();
    }
    private static byte[] toWindowsValueString(String javaName) {
        StringBuffer windowsName = new StringBuffer();
        for (int i = 0; i < javaName.length(); i++) {
            char ch =javaName.charAt(i);
            if ((ch < 0x0020)||(ch > 0x007f)){
                windowsName.append("/u");
                String hex = Integer.toHexString(javaName.charAt(i));
                StringBuffer hex4 = new StringBuffer(hex);
                hex4.reverse();
                int len = 4 - hex4.length();
                for (int j = 0; j < len; j++){
                    hex4.append('0');
                }
                for (int j = 0; j < 4; j++){
                    windowsName.append(hex4.charAt(3 - j));
                }
            } else if (ch == '\\') {
                windowsName.append("
            } else if (ch == '/') {
                windowsName.append('\\');
            } else if ((ch >= 'A') && (ch <='Z')) {
                windowsName.append("/" + ch);
            } else {
                windowsName.append(ch);
            }
        }
        return stringToByteArray(windowsName.toString());
    }
    private int rootNativeHandle() {
        return (isUserNode()? USER_ROOT_NATIVE_HANDLE :
                              SYSTEM_ROOT_NATIVE_HANDLE);
    }
    private static byte[] stringToByteArray(String str) {
        byte[] result = new byte[str.length()+1];
        for (int i = 0; i < str.length(); i++) {
            result[i] = (byte) str.charAt(i);
        }
        result[str.length()] = 0;
        return result;
    }
    private static String byteArrayToString(byte[] array) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < array.length - 1; i++) {
            result.append((char)array[i]);
        }
        return result.toString();
    }
    protected void flushSpi() throws BackingStoreException {
    }
    protected void syncSpi() throws BackingStoreException {
    }
    private static synchronized PlatformLogger logger() {
        if (logger == null) {
            logger = PlatformLogger.getLogger("java.util.prefs");
        }
        return logger;
    }
}
