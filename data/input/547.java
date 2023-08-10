public final class ConnectionSettings extends LimeProps {
    private ConnectionSettings() {
    }
    public static final int C_NO_PROXY = 0;
    public static final int C_SOCKS4_PROXY = 4;
    public static final int C_SOCKS5_PROXY = 5;
    public static final int C_HTTP_PROXY = 1;
    public static final BooleanSetting EVER_ACCEPTED_INCOMING = FACTORY.createBooleanSetting("EVER_ACCEPTED_INCOMING", true);
    public static final BooleanSetting LAST_FWT_STATE = FACTORY.createExpirableBooleanSetting("LAST_FWT_STATE", false);
    public static final BooleanSetting CONNECT_ON_STARTUP = FACTORY.createBooleanSetting("CONNECT_ON_STARTUP", true);
    public static final IntSetting NUM_CONNECTIONS = FACTORY.createIntSetting("NUM_CONNECTIONS", 32);
    public static final ByteSetting SOFT_MAX = FACTORY.createByteSetting("SOFT_MAX", (byte) 3);
    public static final BooleanSetting LOCAL_IS_PRIVATE = FACTORY.createBooleanSetting("LOCAL_IS_PRIVATE", true);
    public static final BooleanSetting USE_GWEBCACHE = FACTORY.createBooleanSetting("USE_GWEBCACHE", true);
    public static final LongSetting LAST_GWEBCACHE_FETCH_TIME = FACTORY.createLongSetting("LAST_GWEBCACHE_FETCH_TIME", 0);
    public static final BooleanSetting WATCHDOG_ACTIVE = FACTORY.createBooleanSetting("WATCHDOG_ACTIVE", true);
    public static final StringSetting MULTICAST_ADDRESS = FACTORY.createStringSetting("MULTICAST_ADDRESS", "234.21.81.1");
    public static final IntSetting MULTICAST_PORT = FACTORY.createIntSetting("MULTICAST_PORT", 7799);
    public static final BooleanSetting ALLOW_MULTICAST_LOOPBACK = FACTORY.createBooleanSetting("ALLOW_MULTICAST_LOOPBACK", false);
    public static final BooleanSetting PREFERENCING_ACTIVE = FACTORY.createBooleanSetting("PREFERENCING_ACTIVE", false);
    public static final BooleanSetting ALLOW_WHILE_DISCONNECTED = FACTORY.createBooleanSetting("ALLOW_WHILE_DISCONNECTED", false);
    public static final BooleanSetting REMOVE_ENABLED = FACTORY.createBooleanSetting("REMOVE_ENABLED", true);
    public static BooleanSetting SEND_QRP = FACTORY.createBooleanSetting("SEND_QRP", true);
    public static final BooleanSetting ACCEPT_DEFLATE = FACTORY.createBooleanSetting("ACCEPT_GNUTELLA_DEFLATE", true);
    public static final BooleanSetting ENCODE_DEFLATE = FACTORY.createBooleanSetting("ENCODE_GNUTELLA_DEFLATE", true);
    public static final ByteSetting TTL = FACTORY.createByteSetting("TTL", (byte) 4);
    public static final IntSetting CONNECTION_SPEED = FACTORY.createIntSetting("CONNECTION_SPEED", 56);
    public static final IntSetting PORT = FACTORY.createIntSetting("PORT", 7791);
    public static final BooleanSetting FORCE_IP_ADDRESS = FACTORY.createBooleanSetting("FORCE_IP_ADDRESS", false);
    public static final StringSetting FORCED_IP_ADDRESS_STRING = (StringSetting) FACTORY.createStringSetting("FORCED_IP_ADDRESS_STRING", "0.0.0.0").setPrivate(true);
    public static final IntSetting FORCED_PORT = FACTORY.createIntSetting("FORCED_PORT", 7791);
    public static final BooleanSetting DISABLE_UPNP = FACTORY.createBooleanSetting("DISABLE_UPNP", false);
    public static final BooleanSetting UPNP_IN_USE = FACTORY.createBooleanSetting("UPNP_IN_USE", false);
    public static final String CONNECT_STRING_FIRST_WORD = "LION";
    public static final StringSetting CONNECT_STRING = FACTORY.createStringSetting("CONNECT_STRING", "LION CONNECT/0.4");
    public static final StringSetting CONNECT_OK_STRING = FACTORY.createStringSetting("CONNECT_OK_STRING", "LION OK");
    public static final BooleanSetting USE_NIO = FACTORY.createBooleanSetting("USE_NIO", true);
    public static final StringSetting PROXY_HOST = FACTORY.createStringSetting("PROXY_HOST", "");
    public static final IntSetting PROXY_PORT = FACTORY.createIntSetting("PROXY_PORT", 0);
    public static final BooleanSetting USE_PROXY_FOR_PRIVATE = FACTORY.createBooleanSetting("USE_PROXY_FOR_PRIVATE", false);
    public static final IntSetting CONNECTION_METHOD = FACTORY.createIntSetting("CONNECTION_TYPE", C_NO_PROXY);
    public static final BooleanSetting PROXY_AUTHENTICATE = FACTORY.createBooleanSetting("PROXY_AUTHENTICATE", false);
    public static final StringSetting PROXY_USERNAME = FACTORY.createStringSetting("PROXY_USERNAME", "");
    public static final StringSetting PROXY_PASS = FACTORY.createStringSetting("PROXY_PASS", "");
    public static final BooleanSetting USE_LOCALE_PREF = FACTORY.createBooleanSetting("USE_LOCALE_PREF", true);
    public static final IntSetting NUM_LOCALE_PREF = FACTORY.createIntSetting("NUMBER_LOCALE_PREF", 2);
    public static final IntSetting LIME_ATTEMPTS = FACTORY.createIntSetting("LIME_ATTEMPTS", 50);
    public static final LongSetting SOLICITED_GRACE_PERIOD = FACTORY.createLongSetting("SOLICITED_GRACE_PERIOD", 85000l);
    public static final IntSetting NUM_RETURN_PONGS = FACTORY.createSettableIntSetting("NUM_RETURN_PONGS", 10, "pings", 25, 5);
    public static final BooleanSetting DO_NOT_BOOTSTRAP = FACTORY.createBooleanSetting("DO_NOT_BOOTSTRAP", false);
    public static final BooleanSetting DO_NOT_MULTICAST_BOOTSTRAP = FACTORY.createBooleanSetting("DO_NOT_MULTICAST_BOOTSTRAP", false);
    public static final BooleanSetting UNSET_FIREWALLED_FROM_CONNECTBACK = FACTORY.createSettableBooleanSetting("UNSET_FIREWALLED_FROM_CONNECTBACK", false, "connectbackfirewall");
    public static final LongSetting FLUSH_DELAY_TIME = FACTORY.createSettableLongSetting("FLUSH_DELAY_TIME", 0, "flushdelay", 300, 0);
    public static final StringArraySetting EVIL_HOSTS = FACTORY.createSettableStringArraySetting("EVIL_HOSTS", new String[0], "evil_hosts");
    public static final int getMaxConnections() {
        int speed = CONNECTION_SPEED.getValue();
        if (speed <= SpeedConstants.MODEM_SPEED_INT) {
            return 3;
        } else if (speed <= SpeedConstants.CABLE_SPEED_INT) {
            return 6;
        } else if (speed <= SpeedConstants.T1_SPEED_INT) {
            return 10;
        } else {
            return 12;
        }
    }
}
