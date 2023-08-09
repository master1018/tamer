@TestTargetClass(AlphabetIndexer.class)
public class AlphabetIndexerTest extends AndroidTestCase {
    private static final String[] COUNTRIES_LIST = new String[]
        {"Argentina", "Australia", "China", "France", "Germany", "Italy", "Japan", "United States"};
    private static final String[] NAMES_LIST = new String[]
        {"Andy", "Bergkamp", "David", "Jacky", "Kevin", "Messi", "Michael", "Steven"};
    private static final String ALPHABET = " ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int SORTED_COLUMN_INDEX = 0;
    private static final int INDEX_OF_ARGENTINA = 0;
    private static final int INDEX_OF_CHINA = 2;
    private static final int INDEX_OF_UNITED_STATES = 7;
    private static final int INDEX_OF_BERGKAMP = 1;
    private static final int INDEX_OF_MESSI = 5;
    private static final int INDEX_OF_STEVEN = 7;
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "AlphabetIndexer",
            args = {android.database.Cursor.class, int.class, java.lang.CharSequence.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getPositionForSection",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getSectionForPosition",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getSections",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setCursor",
            args = {android.database.Cursor.class}
        )
    })
    public void testAlphabetIndexer() {
        Cursor c1 = createCursor("Country", COUNTRIES_LIST);
        AlphabetIndexer indexer = new AlphabetIndexer(c1, SORTED_COLUMN_INDEX, ALPHABET);
        Object[] sections = indexer.getSections();
        assertTrue(sections instanceof String[]);
        assertEquals(ALPHABET.length(), sections.length);
        assertEquals(ALPHABET.charAt(0), ((String[]) sections)[0].charAt(0));
        assertEquals(ALPHABET.charAt(1), ((String[]) sections)[1].charAt(0));
        assertEquals(ALPHABET.charAt(ALPHABET.length() - 2),
                ((String[]) sections)[ALPHABET.length() - 2].charAt(0));
        assertEquals(ALPHABET.charAt(ALPHABET.length() - 1),
                ((String[]) sections)[ALPHABET.length() - 1].charAt(0));
        int index = ALPHABET.indexOf('A');
        assertEquals(INDEX_OF_ARGENTINA, indexer.getPositionForSection(index));
        index = ALPHABET.indexOf('C');
        assertEquals(INDEX_OF_CHINA, indexer.getPositionForSection(index));
        index = ALPHABET.indexOf('T');
        assertEquals(INDEX_OF_UNITED_STATES, indexer.getPositionForSection(index));
        index = ALPHABET.indexOf('X');
        assertEquals(COUNTRIES_LIST.length, indexer.getPositionForSection(index));
        assertEquals(ALPHABET.indexOf('A'), indexer.getSectionForPosition(0));
        assertEquals(ALPHABET.indexOf('C'), indexer.getSectionForPosition(2));
        assertEquals(ALPHABET.indexOf('G'), indexer.getSectionForPosition(4));
        assertEquals(ALPHABET.indexOf('J'), indexer.getSectionForPosition(6));
        Cursor c2 = createCursor("Name", NAMES_LIST);
        indexer.setCursor(c2);
        index = ALPHABET.indexOf('B');
        assertEquals(INDEX_OF_BERGKAMP, indexer.getPositionForSection(index));
        index = ALPHABET.indexOf('M');
        assertEquals(INDEX_OF_MESSI, indexer.getPositionForSection(index));
        index = ALPHABET.indexOf('P');
        assertEquals(INDEX_OF_STEVEN, indexer.getPositionForSection(index));
        index = ALPHABET.indexOf('T');
        assertEquals(NAMES_LIST.length, indexer.getPositionForSection(index));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "compare",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testCompare() {
        Cursor cursor = createCursor("Country", COUNTRIES_LIST);
        MyAlphabetIndexer indexer = new MyAlphabetIndexer(cursor, SORTED_COLUMN_INDEX, ALPHABET);
        assertEquals(0, indexer.compare("Golfresort", "G"));
        assertTrue(indexer.compare("Golfresort", "F") > 0);
        assertTrue(indexer.compare("Golfresort", "H") < 0);
    }
    @SuppressWarnings("unchecked")
    private Cursor createCursor(String listName, String[] listData) {
        String[] columns = { listName };
        ArrayList<ArrayList> list = new ArrayList<ArrayList>();
        for (String cell : listData) {
            ArrayList<String> row = new ArrayList<String>();
            row.add(cell);
            list.add(row);
        }
        return new ArrayListCursor(columns, list);
    }
    private static class MyAlphabetIndexer extends AlphabetIndexer {
        public MyAlphabetIndexer(Cursor cursor, int sortedColumnIndex, CharSequence alphabet) {
            super(cursor, sortedColumnIndex, alphabet);
        }
        protected int compare(String word, String letter) {
            return super.compare(word, letter);
        }
    }
}
