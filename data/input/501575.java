public class LevelTestResource extends ListResourceBundle {
    public Object[][] getContents() {
        return contents;
    }
    @SuppressWarnings("nls")
    static final Object[][] contents = { { "Level_error", "Name" },
            { "Localized", "Localized message" }, };
}