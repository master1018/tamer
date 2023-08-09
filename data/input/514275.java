public class NameSplitterTest extends TestCase {
    private NameSplitter mNameSplitter;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createNameSplitter(Locale.US);
    }
    private void createNameSplitter(Locale locale) {
        mNameSplitter = new NameSplitter("Mr, Ms, Mrs", "d', st, st., von", "Jr., M.D., MD, D.D.S.",
                "&, AND", locale);
    }
    public void testNull() {
        assertSplitName(null, null, null, null, null, null);
        assertJoinedName(null, null, null, null, null, null);
    }
    public void testEmpty() {
        assertSplitName("", null, null, null, null, null);
        assertJoinedName(null, null, null, null, null, null);
    }
    public void testSpaces() {
        assertSplitName(" ", null, null, null, null, null);
        assertJoinedName(null, null, null, null, null, null);
    }
    public void testFamilyName() {
        assertSplitName("Smith", null, "Smith", null, null, null);
        assertJoinedName("Smith", null, "Smith", null, null, null);
    }
    public void testIgnoreSuffix() {
        assertSplitName("Ms MD", "Ms", null, null, "MD", null);
        assertJoinedName("MD", "Ms", null, null, "MD", null);
    }
    public void testGivenFamilyName() {
        assertSplitName("John Smith", null, "John", null, "Smith", null);
        assertJoinedName("John Smith", null, "John", null, "Smith", null);
    }
    public void testGivenMiddleFamilyName() {
        assertSplitName("John Edward Smith", null, "John", "Edward", "Smith", null);
        assertJoinedName("John Edward Smith", null, "John", "Edward", "Smith", null);
    }
    public void testThreeNamesAndFamilyName() {
        assertSplitName("John Edward Kevin Smith", null, "John Edward", "Kevin", "Smith", null);
        assertJoinedName("John Edward Kevin Smith", null, "John Edward", "Kevin", "Smith", null);
    }
    public void testPrefixFivenFamilyName() {
        assertSplitName("Mr. John Smith", "Mr", "John", null, "Smith", null);
        assertJoinedName("John Smith", "Mr", "John", null, "Smith", null);
        assertSplitName("Mr.John Smith", "Mr", "John", null, "Smith", null);
        assertJoinedName("John Smith", "Mr", "John", null, "Smith", null);
    }
    public void testFivenFamilyNameSuffix() {
        assertSplitName("John Smith Jr", null, "John", null, "Smith", "Jr");
        assertJoinedName("John Smith, Jr.", null, "John", null, "Smith", "Jr");
    }
    public void testGivenFamilyNameSuffixWithDot() {
        assertSplitName("John Smith M.D.", null, "John", null, "Smith", "M.D.");
        assertJoinedName("John Smith, M.D.", null, "John", null, "Smith", "M.D.");
        assertSplitName("John Smith D D S", null, "John", null, "Smith", "D D S");
        assertJoinedName("John Smith, D D S", null, "John", null, "Smith", "D D S");
    }
    public void testGivenSuffixFamilyName() {
        assertSplitName("John von Smith", null, "John", null, "von Smith", null);
        assertJoinedName("John von Smith", null, "John", null, "von Smith", null);
    }
    public void testGivenSuffixFamilyNameWithDot() {
        assertSplitName("John St.Smith", null, "John", null, "St. Smith", null);
        assertJoinedName("John St. Smith", null, "John", null, "St. Smith", null);
    }
    public void testPrefixGivenMiddleFamily() {
        assertSplitName("Mr. John Kevin Smith", "Mr", "John", "Kevin", "Smith", null);
        assertJoinedName("John Kevin Smith", "Mr", "John", "Kevin", "Smith", null);
        assertSplitName("Mr.John Kevin Smith", "Mr", "John", "Kevin", "Smith", null);
        assertJoinedName("John Kevin Smith", "Mr", "John", "Kevin", "Smith", null);
    }
    public void testPrefixGivenMiddleFamilySuffix() {
        assertSplitName("Mr. John Kevin Smith Jr.", "Mr", "John", "Kevin", "Smith", "Jr.");
        assertJoinedName("John Kevin Smith, Jr.", "Mr", "John", "Kevin", "Smith", "Jr");
    }
    public void testPrefixGivenMiddlePrefixFamilySuffixWrongCapitalization() {
        assertSplitName("MR. john keVin VON SmiTh JR.", "MR", "john", "keVin", "VON SmiTh", "JR.");
        assertJoinedName("john keVin VON SmiTh, JR.", "MR", "john", "keVin", "VON SmiTh", "JR");
    }
    public void testPrefixFamilySuffix() {
        assertSplitName("von Smith Jr.", null, null, null, "von Smith", "Jr.");
        assertJoinedName("von Smith, Jr.", null, null, null, "von Smith", "Jr");
    }
    public void testFamilyNameGiven() {
        assertSplitName("Smith, John", null, "John", null, "Smith", null);
        assertSplitName("Smith  , John", null, "John", null, "Smith", null);
        assertSplitName("Smith, John Kimble", null, "John", "Kimble", "Smith", null);
        assertSplitName("Smith, John K.", null, "John", "K.", "Smith", null);
        assertSplitName("Smith, John, Jr.", null, "John", null, "Smith", "Jr.");
        assertSplitName("Smith, John Kimble, Jr.", null, "John", "Kimble", "Smith", "Jr.");
        assertSplitName("von Braun, John, Jr.", null, "John", null, "von Braun", "Jr.");
        assertSplitName("von Braun, John Kimble, Jr.", null, "John", "Kimble", "von Braun", "Jr.");
    }
    public void testTwoNamesAndFamilyNameWithAmpersand() {
        assertSplitName("John & Edward Smith", null, "John & Edward", null, "Smith", null);
        assertJoinedName("John & Edward Smith", null, "John & Edward", null, "Smith", null);
        assertSplitName("John and Edward Smith", null, "John and Edward", null, "Smith", null);
        assertSplitName("Smith, John and Edward", null, "John and Edward", null, "Smith", null);
        assertJoinedName("John and Edward Smith", null, "John and Edward", null, "Smith", null);
    }
    public void testWithMiddleInitialAndNoDot() {
        assertSplitName("John E. Smith", null, "John", "E.", "Smith", null);
        assertJoinedName("John E Smith", null, "John", "E", "Smith", null);
    }
    public void testWithLongGivenNameAndDot() {
        assertSplitName("John Ed. K. Smith", null, "John Ed.", "K.", "Smith", null);
        assertJoinedName("John Ed. K Smith", null, "John Ed.", "K", "Smith", null);
    }
    public void testGuessFullNameStyleEmpty() {
        assertFullNameStyle(FullNameStyle.UNDEFINED, null);
        assertFullNameStyle(FullNameStyle.UNDEFINED, "");
    }
    public void testGuessFullNameStyleWestern() {
        assertFullNameStyle(FullNameStyle.WESTERN, "John Doe");
        assertFullNameStyle(FullNameStyle.JAPANESE, "A\u3080\u308D\u306A\u307F\u3048");
        assertFullNameStyle(FullNameStyle.WESTERN, "\u0152uvre");
        assertFullNameStyle(FullNameStyle.WESTERN, "\uFF5C.?+Smith");
    }
    public void testGuessFullNameStyleJapanese() {
        createNameSplitter(Locale.JAPAN);
        assertFullNameStyle(FullNameStyle.JAPANESE, "\u3042\u3080\u308D\u306A\u307F\u3048");
        assertFullNameStyle(FullNameStyle.JAPANESE, "\u30A2\u30E0\u30ED \u30CA\u30DF\u30A8");
        assertFullNameStyle(FullNameStyle.JAPANESE, "\uFF71\uFF91\uFF9B \uFF85\uFF90\uFF74");
        assertFullNameStyle(FullNameStyle.JAPANESE, "\u5B89\u5BA4\u5948\u7F8E\u6075");
        assertFullNameStyle(FullNameStyle.JAPANESE, "\u5B89\u5BA4\u5948\u7F8E\u6075",
                "\u3042\u3080\u308D", null, "\u306A\u307F\u3048");
        assertFullNameStyle(FullNameStyle.JAPANESE, "A\u3080\u308D\u306A\u307F\u3048");
    }
    public void testGuessFullNameStyleChinese() {
        createNameSplitter(Locale.CHINA);
        assertFullNameStyle(FullNameStyle.CHINESE, "\u675C\u9D51");
        assertFullNameStyle(FullNameStyle.CHINESE, "\u675C\u9D51",
                "du4", null, "juan1");
        assertFullNameStyle(FullNameStyle.CHINESE, "\uFF5C--(\u675C\u9D51)");
    }
    public void testGuessPhoneticNameStyle() {
        assertPhoneticNameStyle(PhoneticNameStyle.JAPANESE, "\u3042\u3080\u308D", null, null);
        assertPhoneticNameStyle(PhoneticNameStyle.JAPANESE, null, "\u3042\u3080\u308D", null);
        assertPhoneticNameStyle(PhoneticNameStyle.JAPANESE, null, null, "\u306A\u307F\u3048");
        assertPhoneticNameStyle(PhoneticNameStyle.JAPANESE, "\u3042\u3080\u308D", null,
                "\u306A\u307F\u3048");
        assertPhoneticNameStyle(PhoneticNameStyle.JAPANESE, "\u30A2\u30E0\u30ED", null,
                "\u30CA\u30DF\u30A8");
        assertPhoneticNameStyle(PhoneticNameStyle.JAPANESE, "\u30A2\u30E0\u30ED", null,
                "\u30CA\u30DF\u30A8");
        assertPhoneticNameStyle(PhoneticNameStyle.PINYIN, "du4", null, "juan1");
    }
    public void testSplitJapaneseName() {
        createNameSplitter(Locale.JAPAN);
        assertSplitName("\u3042\u3080\u308D", null, "\u3042\u3080\u308D", null, null, null);
        assertSplitName("\u3042\u3080\u308D \u306A\u307F\u3048", null, "\u306A\u307F\u3048", null,
                "\u3042\u3080\u308D", null);
        assertSplitName("\u3042\u3080\u308D \u3068\u304A\u308B \u306A\u307F\u3048", null,
                "\u3068\u304A\u308B \u306A\u307F\u3048", null, "\u3042\u3080\u308D", null);
        assertSplitName("\u6BB5\u5C0F\u6D9B", null, "\u6BB5\u5C0F\u6D9B", null, null, null);
    }
    public void testSplitChineseName() {
        createNameSplitter(Locale.CHINA);
        assertSplitName("\u6BB5\u5C0F", null, "\u5C0F", null, "\u6BB5", null);
        assertSplitName("\u6BB5\u5C0F\u6D9B", null, "\u6D9B", "\u5C0F", "\u6BB5", null);
        assertSplitName("\u6BB5\u5C0F\u6D9B\u6D9C", null, "\u6D9C", "\u6D9B", "\u6BB5\u5C0F", null);
    }
    public void testJoinJapaneseName() {
        createNameSplitter(Locale.JAPAN);
        assertJoinedName("\u3042\u3080\u308D", FullNameStyle.JAPANESE, null, "\u3042\u3080\u308D",
                null, null, null, true);
        assertJoinedName("\u3084\u307E\u3056\u304D \u3068\u304A\u308B", FullNameStyle.JAPANESE,
                null, "\u3068\u304A\u308B", null, "\u3084\u307E\u3056\u304D", null, false);
        assertJoinedName("\u3084\u307E\u3056\u304D \u3068\u304A\u308B \u3068\u304A\u308B",
                FullNameStyle.JAPANESE, null, "\u3068\u304A\u308B", "\u3068\u304A\u308B",
                "\u3084\u307E\u3056\u304D", null, false);
    }
    public void testJoinChineseName() {
        createNameSplitter(Locale.CHINA);
        assertJoinedName("\u6BB5\u5C0F\u6D9B", FullNameStyle.CHINESE, null,
                "\u6D9B", "\u5C0F", "\u6BB5", null, true);
        assertJoinedName("\u6BB5\u5C0F\u6D9B", FullNameStyle.CHINESE, null,
                "\u6D9B", "\u5C0F", "\u6BB5", null, false);
    }
    private void assertSplitName(String fullName, String prefix, String givenNames,
            String middleName, String familyName, String suffix) {
        final Name name = new Name();
        mNameSplitter.split(name, fullName);
        assertEquals(prefix, name.getPrefix());
        assertEquals(givenNames, name.getGivenNames());
        assertEquals(middleName, name.getMiddleName());
        assertEquals(familyName, name.getFamilyName());
        assertEquals(suffix, name.getSuffix());
    }
    private void assertJoinedName(String expected, String prefix, String givenNames,
            String middleName, String familyName, String suffix) {
        assertJoinedName(expected, FullNameStyle.WESTERN, prefix, givenNames, middleName,
                familyName, suffix, true);
    }
    private void assertJoinedName(String expected, int nameStyle, String prefix, String givenNames,
            String middleName, String familyName, String suffix, boolean givenNameFirst) {
        Name name = new Name();
        name.fullNameStyle = nameStyle;
        name.prefix = prefix;
        name.givenNames = givenNames;
        name.middleName = middleName;
        name.familyName = familyName;
        name.suffix = suffix;
        String actual = mNameSplitter.join(name, givenNameFirst);
        assertEquals(expected, actual);
    }
    private void assertFullNameStyle(int expectedFullNameStyle, String fullName) {
        Name name = new Name();
        mNameSplitter.split(name, fullName);
        mNameSplitter.guessNameStyle(name);
        assertEquals(expectedFullNameStyle, name.fullNameStyle);
    }
    private void assertFullNameStyle(int expectedFullNameStyle, String fullName,
            String phoneticFamilyName, String phoneticMiddleName, String phoneticGivenName) {
        Name name = new Name();
        mNameSplitter.split(name, fullName);
        name.phoneticFamilyName = phoneticFamilyName;
        name.phoneticMiddleName = phoneticMiddleName;
        name.phoneticGivenName = phoneticGivenName;
        mNameSplitter.guessNameStyle(name);
        assertEquals(expectedFullNameStyle, name.fullNameStyle);
    }
    private void assertPhoneticNameStyle(int expectedPhoneticNameStyle, String phoneticFamilyName,
            String phoneticMiddleName, String phoneticGivenName) {
        Name name = new Name();
        name.phoneticFamilyName = phoneticFamilyName;
        name.phoneticMiddleName = phoneticMiddleName;
        name.phoneticGivenName = phoneticGivenName;
        mNameSplitter.guessNameStyle(name);
        assertEquals(expectedPhoneticNameStyle, name.phoneticNameStyle);
    }
}
