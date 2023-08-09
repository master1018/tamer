public class UriMatcherTest extends TestCase
{
    static final int ROOT = 0;
    static final int PEOPLE = 1;
    static final int PEOPLE_ID = 2;
    static final int PEOPLE_PHONES = 3;
    static final int PEOPLE_PHONES_ID = 4;
    static final int PEOPLE_ADDRESSES = 5;
    static final int PEOPLE_ADDRESSES_ID = 6;
    static final int PEOPLE_CONTACTMETH = 7;
    static final int PEOPLE_CONTACTMETH_ID = 8;
    static final int CALLS = 9;
    static final int CALLS_ID = 10;
    static final int CALLERID = 11;
    static final int CALLERID_TEXT = 12;
    static final int FILTERRECENT = 13;
    @SmallTest
    public void testContentUris() {
        check("content:
        check("content:
        check("content:
        check("content:
        check("content:
        check("content:
        check("content:
        check("content:
        check("content:
        check("content:
        check("content:
        check("content:
        check("content:
        check("content:
        check("content:
        check("content:
        check("content:
        check("content:
        check("content:
        check("content:
    }
    private static final UriMatcher mURLMatcher = new UriMatcher(ROOT);
    static
    {
        mURLMatcher.addURI("people", null, PEOPLE);
        mURLMatcher.addURI("people", "#", PEOPLE_ID);
        mURLMatcher.addURI("people", "#/phones", PEOPLE_PHONES);
        mURLMatcher.addURI("people", "#/phones/blah", PEOPLE_PHONES_ID);
        mURLMatcher.addURI("people", "#/phones/#", PEOPLE_PHONES_ID);
        mURLMatcher.addURI("people", "#/addresses", PEOPLE_ADDRESSES);
        mURLMatcher.addURI("people", "#/addresses/#", PEOPLE_ADDRESSES_ID);
        mURLMatcher.addURI("people", "#/contact-methods", PEOPLE_CONTACTMETH);
        mURLMatcher.addURI("people", "#/contact-methods/#", PEOPLE_CONTACTMETH_ID);
        mURLMatcher.addURI("calls", null, CALLS);
        mURLMatcher.addURI("calls", "#", CALLS_ID);
        mURLMatcher.addURI("caller-id", null, CALLERID);
        mURLMatcher.addURI("caller-id", "*", CALLERID_TEXT);
        mURLMatcher.addURI("filter-recent", null, FILTERRECENT);
    }
    void check(String uri, int expected)
    {
        int result = mURLMatcher.match(Uri.parse(uri));
        if (result != expected) {
            String msg = "failed on " + uri;
            msg += " expected " + expected + " got " + result;
            throw new RuntimeException(msg);
        }
    }
}
