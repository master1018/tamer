public class LogTag {
    public static void logBookmarkAdded(String url, String where) {
        EventLog.writeEvent(EventLogTags.BROWSER_BOOKMARK_ADDED, url + "|"
            + where);
    }
    public static void logPageFinishedLoading(String url, long duration) {
        EventLog.writeEvent(EventLogTags.BROWSER_PAGE_LOADED, url + "|"
            + duration);
    }
    public static void logTimeOnPage(String url, long duration) {
        EventLog.writeEvent(EventLogTags.BROWSER_TIMEONPAGE, url + "|"
            + duration);
    }
}
