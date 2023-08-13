public final class VersionInfo
{
    public static VersionInfo getInstance(String version)
    {
        int length  = version.length();
        int array[] = {0, 0, 0, 0};
        int count   = 0;
        int index   = 0;
        while (count < 4 && index < length) {
            char c = version.charAt(index);
            if (c == '.') {
                count ++;
            }
            else {
                c -= '0';
                if (c < 0 || c > 9) {
                    throw new IllegalArgumentException(INVALID_VERSION_NUMBER_);
                }
                array[count] *= 10;
                array[count] += c;
            }
            index ++;
        }
        if (index != length) {
            throw new IllegalArgumentException(
                                               "Invalid version number: String '" + version + "' exceeds version format");
        }
        for (int i = 0; i < 4; i ++) {
            if (array[i] < 0 || array[i] > 255) {
                throw new IllegalArgumentException(INVALID_VERSION_NUMBER_);
            }
        }
        return getInstance(array[0], array[1], array[2], array[3]);
    }
    public static VersionInfo getInstance(int major, int minor, int milli,
                                          int micro)
    {
        if (major < 0 || major > 255 || minor < 0 || minor > 255 ||
            milli < 0 || milli > 255 || micro < 0 || micro > 255) {
            throw new IllegalArgumentException(INVALID_VERSION_NUMBER_);
        }
        int     version = getInt(major, minor, milli, micro);
        Integer key     = Integer.valueOf(version);
        Object  result  = MAP_.get(key);
        if (result == null) {
            result = new VersionInfo(version);
            MAP_.put(key, result);
        }
        return (VersionInfo)result;
    }
    public int compareTo(VersionInfo other)
    {
        return m_version_ - other.m_version_;
    }
    private int m_version_;
    private static final HashMap MAP_ = new HashMap();
    private static final String INVALID_VERSION_NUMBER_ =
        "Invalid version number: Version number may be negative or greater than 255";
    private VersionInfo(int compactversion)
    {
        m_version_ = compactversion;
    }
    private static int getInt(int major, int minor, int milli, int micro)
    {
        return (major << 24) | (minor << 16) | (milli << 8) | micro;
    }
}
