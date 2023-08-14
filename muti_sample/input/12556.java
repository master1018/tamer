public class TestResource extends ListResourceBundle {
    public Object[][] getContents() {
        return contents;
    }
    static final Object[][] contents = {
        { "Now", "Now is the time for all..." },
        { "Time", "Howdy Doody Time!" },
        { "Good", new Integer(27) },
        { "Men", new String[] { "1", "2", "C" } },
        { "Come", "Come into my parlor..." }
    };
}
