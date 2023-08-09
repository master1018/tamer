public class ZoneInfoFile {
    public static final byte[]  JAVAZI_LABEL = {
        (byte)'j', (byte)'a', (byte)'v', (byte)'a', (byte)'z', (byte)'i', (byte)'\0'
    };
    private static final int    JAVAZI_LABEL_LENGTH = JAVAZI_LABEL.length;
    public static final byte    JAVAZI_VERSION = 0x01;
    public static final byte    TAG_RawOffset = 1;
    public static final byte    TAG_LastDSTSaving = 2;
    public static final byte    TAG_CRC32 = 3;
    public static final byte    TAG_Transition = 4;
    public static final byte    TAG_Offset = 5;
    public static final byte    TAG_SimpleTimeZone = 6;
    public static final byte    TAG_GMTOffsetWillChange = 7;
    public static final String  JAVAZM_FILE_NAME = "ZoneInfoMappings";
    public static final byte[]  JAVAZM_LABEL = {
        (byte)'j', (byte)'a', (byte)'v', (byte)'a', (byte)'z', (byte)'m', (byte)'\0'
    };
    private static final int    JAVAZM_LABEL_LENGTH = JAVAZM_LABEL.length;
    public static final byte    JAVAZM_VERSION = 0x01;
    public static final byte    TAG_ZoneIDs = 64;
    public static final byte    TAG_RawOffsets = 65;
    public static final byte    TAG_RawOffsetIndices = 66;
    public static final byte    TAG_ZoneAliases = 67;
    public static final byte    TAG_TZDataVersion = 68;
    public static final byte    TAG_ExcludedZones = 69;
    private static Map<String, ZoneInfo> zoneInfoObjects = null;
    private static final String ziDir = AccessController.doPrivileged(
        new PrivilegedAction<String>() {
            public String run() {
                String zi = System.getProperty("java.home") +
                    File.separator + "lib" + File.separator + "zi";
                try {
                    zi = FileSystems.getDefault().getPath(zi).toRealPath().toString();
                } catch(Exception e) {
                }
                return zi;
            }
        });
    public static String getFileName(String ID) {
        if (File.separatorChar == '/') {
            return ID;
        }
        return ID.replace('/', File.separatorChar);
    }
    public static ZoneInfo getCustomTimeZone(String originalId, int gmtOffset) {
        String id = toCustomID(gmtOffset);
        ZoneInfo zi = getFromCache(id);
        if (zi == null) {
            zi = new ZoneInfo(id, gmtOffset);
            zi = addToCache(id, zi);
            if (!id.equals(originalId)) {
                zi = addToCache(originalId, zi);
            }
        }
        return (ZoneInfo) zi.clone();
    }
    public static String toCustomID(int gmtOffset) {
        char sign;
        int offset = gmtOffset / 60000;
        if (offset >= 0) {
            sign = '+';
        } else {
            sign = '-';
            offset = -offset;
        }
        int hh = offset / 60;
        int mm = offset % 60;
        char[] buf = new char[] { 'G', 'M', 'T', sign, '0', '0', ':', '0', '0' };
        if (hh >= 10) {
            buf[4] += hh / 10;
        }
        buf[5] += hh % 10;
        if (mm != 0) {
            buf[7] += mm / 10;
            buf[8] += mm % 10;
        }
        return new String(buf);
    }
    public static ZoneInfo getZoneInfo(String id) {
        ZoneInfo zi = getFromCache(id);
        if (zi == null) {
            zi = createZoneInfo(id);
            if (zi == null) {
                return null;
            }
            zi = addToCache(id, zi);
        }
        return (ZoneInfo) zi.clone();
    }
    synchronized static ZoneInfo getFromCache(String id) {
        if (zoneInfoObjects == null) {
            return null;
        }
        return zoneInfoObjects.get(id);
    }
    synchronized static ZoneInfo addToCache(String id, ZoneInfo zi) {
        if (zoneInfoObjects == null) {
            zoneInfoObjects = new HashMap<String, ZoneInfo>();
        } else {
            ZoneInfo zone = zoneInfoObjects.get(id);
            if (zone != null) {
                return zone;
            }
        }
        zoneInfoObjects.put(id, zi);
        return zi;
    }
    private static ZoneInfo createZoneInfo(String id) {
        byte[] buf = readZoneInfoFile(getFileName(id));
        if (buf == null) {
            return null;
        }
        int index = 0;
        int filesize = buf.length;
        int rawOffset = 0;
        int dstSavings = 0;
        int checksum = 0;
        boolean willGMTOffsetChange = false;
        long[] transitions = null;
        int[] offsets = null;
        int[] simpleTimeZoneParams = null;
        try {
            for (index = 0; index < JAVAZI_LABEL.length; index++) {
                if (buf[index] != JAVAZI_LABEL[index]) {
                    System.err.println("ZoneInfo: wrong magic number: " + id);
                    return null;
                }
            }
            if (buf[index++] > JAVAZI_VERSION) {
                System.err.println("ZoneInfo: incompatible version ("
                                   + buf[index - 1] + "): " + id);
                return null;
            }
            while (index < filesize) {
                byte tag = buf[index++];
                int  len = ((buf[index++] & 0xFF) << 8) + (buf[index++] & 0xFF);
                if (filesize < index+len) {
                    break;
                }
                switch (tag) {
                case TAG_CRC32:
                    {
                        int val = buf[index++] & 0xff;
                        val = (val << 8) + (buf[index++] & 0xff);
                        val = (val << 8) + (buf[index++] & 0xff);
                        val = (val << 8) + (buf[index++] & 0xff);
                        checksum = val;
                    }
                    break;
                case TAG_LastDSTSaving:
                    {
                        short val = (short)(buf[index++] & 0xff);
                        val = (short)((val << 8) + (buf[index++] & 0xff));
                        dstSavings = val * 1000;
                    }
                    break;
                case TAG_RawOffset:
                    {
                        int val = buf[index++] & 0xff;
                        val = (val << 8) + (buf[index++] & 0xff);
                        val = (val << 8) + (buf[index++] & 0xff);
                        val = (val << 8) + (buf[index++] & 0xff);
                        rawOffset = val;
                    }
                    break;
                case TAG_Transition:
                    {
                        int n = len / 8;
                        transitions = new long[n];
                        for (int i = 0; i < n; i ++) {
                            long val = buf[index++] & 0xff;
                            val = (val << 8) + (buf[index++] & 0xff);
                            val = (val << 8) + (buf[index++] & 0xff);
                            val = (val << 8) + (buf[index++] & 0xff);
                            val = (val << 8) + (buf[index++] & 0xff);
                            val = (val << 8) + (buf[index++] & 0xff);
                            val = (val << 8) + (buf[index++] & 0xff);
                            val = (val << 8) + (buf[index++] & 0xff);
                            transitions[i] = val;
                        }
                    }
                    break;
                case TAG_Offset:
                    {
                        int n = len / 4;
                        offsets = new int[n];
                        for (int i = 0; i < n; i ++) {
                            int val = buf[index++] & 0xff;
                            val = (val << 8) + (buf[index++] & 0xff);
                            val = (val << 8) + (buf[index++] & 0xff);
                            val = (val << 8) + (buf[index++] & 0xff);
                            offsets[i] = val;
                        }
                    }
                    break;
                case TAG_SimpleTimeZone:
                    {
                        if (len != 32 && len != 40) {
                            System.err.println("ZoneInfo: wrong SimpleTimeZone parameter size");
                            return null;
                        }
                        int n = len / 4;
                        simpleTimeZoneParams = new int[n];
                        for (int i = 0; i < n; i++) {
                            int val = buf[index++] & 0xff;
                            val = (val << 8) + (buf[index++] & 0xff);
                            val = (val << 8) + (buf[index++] & 0xff);
                            val = (val << 8) + (buf[index++] & 0xff);
                            simpleTimeZoneParams[i] = val;
                        }
                    }
                    break;
                case TAG_GMTOffsetWillChange:
                    {
                        if (len != 1) {
                            System.err.println("ZoneInfo: wrong byte length for TAG_GMTOffsetWillChange");
                        }
                        willGMTOffsetChange = buf[index++] == 1;
                    }
                    break;
                default:
                    System.err.println("ZoneInfo: unknown tag < " + tag + ">. ignored.");
                    index += len;
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("ZoneInfo: corrupted zoneinfo file: " + id);
            return null;
        }
        if (index != filesize) {
            System.err.println("ZoneInfo: wrong file size: " + id);
            return null;
        }
        return new ZoneInfo(id, rawOffset, dstSavings, checksum,
                            transitions, offsets, simpleTimeZoneParams,
                            willGMTOffsetChange);
    }
    private volatile static SoftReference<List<String>> zoneIDs = null;
    static List<String> getZoneIDs() {
        List<String> ids = null;
        SoftReference<List<String>> cache = zoneIDs;
        if (cache != null) {
            ids = cache.get();
            if (ids != null) {
                return ids;
            }
        }
        byte[] buf = null;
        buf = getZoneInfoMappings();
        int index = JAVAZM_LABEL_LENGTH + 1;
        int filesize = buf.length;
        try {
        loop:
            while (index < filesize) {
                byte tag = buf[index++];
                int     len = ((buf[index++] & 0xFF) << 8) + (buf[index++] & 0xFF);
                switch (tag) {
                case TAG_ZoneIDs:
                    {
                        int n = (buf[index++] << 8) + (buf[index++] & 0xFF);
                        ids = new ArrayList<String>(n);
                        for (int i = 0; i < n; i++) {
                            byte m = buf[index++];
                            ids.add(new String(buf, index, m, "UTF-8"));
                            index += m;
                        }
                    }
                    break loop;
                default:
                    index += len;
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("ZoneInfo: corrupted " + JAVAZM_FILE_NAME);
        }
        zoneIDs = new SoftReference<List<String>>(ids);
        return ids;
    }
    static Map<String, String> getZoneAliases() {
        byte[] buf = getZoneInfoMappings();
        int index = JAVAZM_LABEL_LENGTH + 1;
        int filesize = buf.length;
        Map<String, String> aliases = null;
        try {
        loop:
            while (index < filesize) {
                byte tag = buf[index++];
                int     len = ((buf[index++] & 0xFF) << 8) + (buf[index++] & 0xFF);
                switch (tag) {
                case TAG_ZoneAliases:
                    {
                        int n = (buf[index++] << 8) + (buf[index++] & 0xFF);
                        aliases = new HashMap<String, String>(n);
                        for (int i = 0; i < n; i++) {
                            byte m = buf[index++];
                            String name = new String(buf, index, m, "UTF-8");
                            index += m;
                            m = buf[index++];
                            String realName = new String(buf, index, m, "UTF-8");
                            index += m;
                            aliases.put(name, realName);
                        }
                    }
                    break loop;
                default:
                    index += len;
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("ZoneInfo: corrupted " + JAVAZM_FILE_NAME);
            return null;
        }
        return aliases;
    }
    private volatile static SoftReference<List<String>> excludedIDs = null;
    private volatile static boolean hasNoExcludeList = false;
    static List<String> getExcludedZones() {
        if (hasNoExcludeList) {
            return null;
        }
        List<String> excludeList = null;
        SoftReference<List<String>> cache = excludedIDs;
        if (cache != null) {
            excludeList = cache.get();
            if (excludeList != null) {
                return excludeList;
            }
        }
        byte[] buf = getZoneInfoMappings();
        int index = JAVAZM_LABEL_LENGTH + 1;
        int filesize = buf.length;
        try {
          loop:
            while (index < filesize) {
                byte tag = buf[index++];
                int     len = ((buf[index++] & 0xFF) << 8) + (buf[index++] & 0xFF);
                switch (tag) {
                case TAG_ExcludedZones:
                    {
                        int n = (buf[index++] << 8) + (buf[index++] & 0xFF);
                        excludeList = new ArrayList<String>();
                        for (int i = 0; i < n; i++) {
                            byte m = buf[index++];
                            String name = new String(buf, index, m, "UTF-8");
                            index += m;
                            excludeList.add(name);
                        }
                    }
                    break loop;
                default:
                    index += len;
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("ZoneInfo: corrupted " + JAVAZM_FILE_NAME);
            return null;
        }
        if (excludeList != null) {
            excludedIDs = new SoftReference<List<String>>(excludeList);
        } else {
            hasNoExcludeList = true;
        }
        return excludeList;
    }
    private volatile static SoftReference<byte[]> rawOffsetIndices = null;
    static byte[] getRawOffsetIndices() {
        byte[] indices = null;
        SoftReference<byte[]> cache = rawOffsetIndices;
        if (cache != null) {
            indices = cache.get();
            if (indices != null) {
                return indices;
            }
        }
        byte[] buf = getZoneInfoMappings();
        int index = JAVAZM_LABEL_LENGTH + 1;
        int filesize = buf.length;
        try {
        loop:
            while (index < filesize) {
                byte tag = buf[index++];
                int     len = ((buf[index++] & 0xFF) << 8) + (buf[index++] & 0xFF);
                switch (tag) {
                case TAG_RawOffsetIndices:
                    {
                        indices = new byte[len];
                        for (int i = 0; i < len; i++) {
                            indices[i] = buf[index++];
                        }
                    }
                    break loop;
                default:
                    index += len;
                    break;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("ZoneInfo: corrupted " + JAVAZM_FILE_NAME);
        }
        rawOffsetIndices = new SoftReference<byte[]>(indices);
        return indices;
    }
    private volatile static SoftReference<int[]> rawOffsets = null;
    static int[] getRawOffsets() {
        int[] offsets = null;
        SoftReference<int[]> cache = rawOffsets;
        if (cache != null) {
            offsets = cache.get();
            if (offsets != null) {
                return offsets;
            }
        }
        byte[] buf = getZoneInfoMappings();
        int index = JAVAZM_LABEL_LENGTH + 1;
        int filesize = buf.length;
        try {
        loop:
            while (index < filesize) {
                byte tag = buf[index++];
                int     len = ((buf[index++] & 0xFF) << 8) + (buf[index++] & 0xFF);
                switch (tag) {
                case TAG_RawOffsets:
                    {
                        int n = len/4;
                        offsets = new int[n];
                        for (int i = 0; i < n; i++) {
                            int val = buf[index++] & 0xff;
                            val = (val << 8) + (buf[index++] & 0xff);
                            val = (val << 8) + (buf[index++] & 0xff);
                            val = (val << 8) + (buf[index++] & 0xff);
                            offsets[i] = val;
                        }
                    }
                    break loop;
                default:
                    index += len;
                    break;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("ZoneInfo: corrupted " + JAVAZM_FILE_NAME);
        }
        rawOffsets = new SoftReference<int[]>(offsets);
        return offsets;
    }
    private volatile static SoftReference<byte[]> zoneInfoMappings = null;
    private static byte[] getZoneInfoMappings() {
        byte[] data;
        SoftReference<byte[]> cache = zoneInfoMappings;
        if (cache != null) {
            data = cache.get();
            if (data != null) {
                return data;
            }
        }
        data = readZoneInfoFile(JAVAZM_FILE_NAME);
        if (data == null) {
            return null;
        }
        int index;
        for (index = 0; index < JAVAZM_LABEL.length; index++) {
            if (data[index] != JAVAZM_LABEL[index]) {
                System.err.println("ZoneInfo: wrong magic number: " + JAVAZM_FILE_NAME);
                return null;
            }
        }
        if (data[index++] > JAVAZM_VERSION) {
            System.err.println("ZoneInfo: incompatible version ("
                               + data[index - 1] + "): " + JAVAZM_FILE_NAME);
            return null;
        }
        zoneInfoMappings = new SoftReference<byte[]>(data);
        return data;
    }
    private static byte[] readZoneInfoFile(final String fileName) {
        byte[] buffer = null;
        try {
            buffer = (byte[]) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws IOException {
                    File file = new File(ziDir, fileName);
                    if (!file.exists() || !file.isFile()) {
                        return null;
                    }
                    file = file.getCanonicalFile();
                    String path = file.getCanonicalPath();
                    byte[] buf = null;
                    if (path != null && path.startsWith(ziDir)) {
                        int filesize = (int)file.length();
                        if (filesize > 0) {
                            FileInputStream fis = new FileInputStream(file);
                            buf = new byte[filesize];
                            try {
                                if (fis.read(buf) != filesize) {
                                    throw new IOException("read error on " + fileName);
                                }
                            } finally {
                                fis.close();
                            }
                        }
                    }
                    return buf;
                }
            });
        } catch (PrivilegedActionException e) {
            Exception ex = e.getException();
            if (!(ex instanceof FileNotFoundException) || JAVAZM_FILE_NAME.equals(fileName)) {
                System.err.println("ZoneInfo: " + ex.getMessage());
            }
        }
        return buffer;
    }
}
