public class MockShortcutRefresher implements ShortcutRefresher {
    public void cancelPendingTasks() {
    }
    public void markShortcutRefreshed(Source source, String shortcutId) {
    }
    public void refresh(Suggestion shortcut, Listener listener) {
    }
    public void reset() {
    }
    public boolean shouldRefresh(Source source, String shortcutId) {
        return shortcutId != null;
    }
}
