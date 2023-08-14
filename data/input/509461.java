public class OperationScheduler {
    public static class Options {
        public long backoffFixedMillis = 0;
        public long backoffIncrementalMillis = 5000;
        public long maxMoratoriumMillis = 24 * 3600 * 1000;
        public long minTriggerMillis = 0;
        public long periodicIntervalMillis = 0;
        @Override
        public String toString() {
            return String.format(
                    "OperationScheduler.Options[backoff=%.1f+%.1f max=%.1f min=%.1f period=%.1f]",
                    backoffFixedMillis / 1000.0, backoffIncrementalMillis / 1000.0,
                    maxMoratoriumMillis / 1000.0, minTriggerMillis / 1000.0,
                    periodicIntervalMillis / 1000.0);
        }
    }
    private static final String PREFIX = "OperationScheduler_";
    private final SharedPreferences mStorage;
    public OperationScheduler(SharedPreferences storage) {
        mStorage = storage;
    }
    public static Options parseOptions(String spec, Options options)
            throws IllegalArgumentException {
        for (String param : spec.split(" +")) {
            if (param.length() == 0) continue;
            if (param.startsWith("backoff=")) {
                int plus = param.indexOf('+', 8);
                if (plus < 0) {
                    options.backoffFixedMillis = parseSeconds(param.substring(8));
                } else {
                    if (plus > 8) {
                        options.backoffFixedMillis = parseSeconds(param.substring(8, plus));
                    }
                    options.backoffIncrementalMillis = parseSeconds(param.substring(plus + 1));
                }
            } else if (param.startsWith("max=")) {
                options.maxMoratoriumMillis = parseSeconds(param.substring(4));
            } else if (param.startsWith("min=")) {
                options.minTriggerMillis = parseSeconds(param.substring(4));
            } else if (param.startsWith("period=")) {
                options.periodicIntervalMillis = parseSeconds(param.substring(7));
            } else {
                options.periodicIntervalMillis = parseSeconds(param);
            }
        }
        return options;
    }
    private static long parseSeconds(String param) throws NumberFormatException {
        return (long) (Float.parseFloat(param) * 1000);
    }
    public long getNextTimeMillis(Options options) {
        boolean enabledState = mStorage.getBoolean(PREFIX + "enabledState", true);
        if (!enabledState) return Long.MAX_VALUE;
        boolean permanentError = mStorage.getBoolean(PREFIX + "permanentError", false);
        if (permanentError) return Long.MAX_VALUE;
        int errorCount = mStorage.getInt(PREFIX + "errorCount", 0);
        long now = currentTimeMillis();
        long lastSuccessTimeMillis = getTimeBefore(PREFIX + "lastSuccessTimeMillis", now);
        long lastErrorTimeMillis = getTimeBefore(PREFIX + "lastErrorTimeMillis", now);
        long triggerTimeMillis = mStorage.getLong(PREFIX + "triggerTimeMillis", Long.MAX_VALUE);
        long moratoriumSetMillis = getTimeBefore(PREFIX + "moratoriumSetTimeMillis", now);
        long moratoriumTimeMillis = getTimeBefore(PREFIX + "moratoriumTimeMillis",
                moratoriumSetMillis + options.maxMoratoriumMillis);
        long time = triggerTimeMillis;
        if (options.periodicIntervalMillis > 0) {
            time = Math.min(time, lastSuccessTimeMillis + options.periodicIntervalMillis);
        }
        time = Math.max(time, moratoriumTimeMillis);
        time = Math.max(time, lastSuccessTimeMillis + options.minTriggerMillis);
        if (errorCount > 0) {
            time = Math.max(time, lastErrorTimeMillis + options.backoffFixedMillis +
                    options.backoffIncrementalMillis * errorCount);
        }
        return time;
    }
    public long getLastSuccessTimeMillis() {
        return mStorage.getLong(PREFIX + "lastSuccessTimeMillis", 0);
    }
    public long getLastAttemptTimeMillis() {
        return Math.max(
                mStorage.getLong(PREFIX + "lastSuccessTimeMillis", 0),
                mStorage.getLong(PREFIX + "lastErrorTimeMillis", 0));
    }
    private long getTimeBefore(String name, long max) {
        long time = mStorage.getLong(name, 0);
        if (time > max) mStorage.edit().putLong(name, (time = max)).commit();
        return time;
    }
    public void setTriggerTimeMillis(long millis) {
        mStorage.edit().putLong(PREFIX + "triggerTimeMillis", millis).commit();
    }
    public void setMoratoriumTimeMillis(long millis) {
        mStorage.edit()
                .putLong(PREFIX + "moratoriumTimeMillis", millis)
                .putLong(PREFIX + "moratoriumSetTimeMillis", currentTimeMillis())
                .commit();
    }
    public boolean setMoratoriumTimeHttp(String retryAfter) {
        try {
            long ms = Long.valueOf(retryAfter) * 1000;
            setMoratoriumTimeMillis(ms + currentTimeMillis());
            return true;
        } catch (NumberFormatException nfe) {
            try {
                setMoratoriumTimeMillis(AndroidHttpClient.parseDate(retryAfter));
                return true;
            } catch (IllegalArgumentException iae) {
                return false;
            }
        }
    }
    public void setEnabledState(boolean enabled) {
        mStorage.edit().putBoolean(PREFIX + "enabledState", enabled).commit();
    }
    public void onSuccess() {
        resetTransientError();
        resetPermanentError();
        mStorage.edit()
                .remove(PREFIX + "errorCount")
                .remove(PREFIX + "lastErrorTimeMillis")
                .remove(PREFIX + "permanentError")
                .remove(PREFIX + "triggerTimeMillis")
                .putLong(PREFIX + "lastSuccessTimeMillis", currentTimeMillis()).commit();
    }
    public void onTransientError() {
        mStorage.edit().putLong(PREFIX + "lastErrorTimeMillis", currentTimeMillis()).commit();
        mStorage.edit().putInt(PREFIX + "errorCount",
                mStorage.getInt(PREFIX + "errorCount", 0) + 1).commit();
    }
    public void resetTransientError() {
        mStorage.edit().remove(PREFIX + "errorCount").commit();
    }
    public void onPermanentError() {
        mStorage.edit().putBoolean(PREFIX + "permanentError", true).commit();
    }
    public void resetPermanentError() {
        mStorage.edit().remove(PREFIX + "permanentError").commit();
    }
    public String toString() {
        StringBuilder out = new StringBuilder("[OperationScheduler:");
        for (String key : new TreeSet<String>(mStorage.getAll().keySet())) {  
            if (key.startsWith(PREFIX)) {
                if (key.endsWith("TimeMillis")) {
                    Time time = new Time();
                    time.set(mStorage.getLong(key, 0));
                    out.append(" ").append(key.substring(PREFIX.length(), key.length() - 10));
                    out.append("=").append(time.format("%Y-%m-%d/%H:%M:%S"));
                } else {
                    out.append(" ").append(key.substring(PREFIX.length()));
                    out.append("=").append(mStorage.getAll().get(key).toString());
                }
            }
        }
        return out.append("]").toString();
    }
    protected long currentTimeMillis() {
        return System.currentTimeMillis();
    }
}
