public abstract class TimeZoneNameProvider extends LocaleServiceProvider {
    protected TimeZoneNameProvider() {
    }
    public abstract String getDisplayName(String ID, boolean daylight, int style, Locale locale);
}
