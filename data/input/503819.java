@TestTargetClass(Audio.class)
public class MediaStore_AudioTest extends TestCase {
    private String mKeyForBeatles;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mKeyForBeatles = Audio.keyFor("beatles");
    }
    @TestTargetNew(
      level = TestLevel.COMPLETE,
      method = "keyFor",
      args = {String.class}
    )
    public void testKeyFor() {
        assertEquals(mKeyForBeatles, Audio.keyFor("[beatles]"));
        assertEquals(mKeyForBeatles, Audio.keyFor("(beatles)"));
        assertEquals(mKeyForBeatles, Audio.keyFor("beatles!"));
        assertEquals(mKeyForBeatles, Audio.keyFor("beatles?"));
        assertEquals(mKeyForBeatles, Audio.keyFor("'beatles'"));
        assertEquals(mKeyForBeatles, Audio.keyFor("beatles."));
        assertEquals(mKeyForBeatles, Audio.keyFor("beatles,"));
        assertEquals(mKeyForBeatles, Audio.keyFor("  beatles  "));
        assertEquals(mKeyForBeatles, Audio.keyFor("BEATLES"));
        assertEquals(mKeyForBeatles, Audio.keyFor("the beatles"));
        assertEquals(mKeyForBeatles, Audio.keyFor("a beatles"));
        assertEquals(mKeyForBeatles, Audio.keyFor("an beatles"));
        assertEquals(mKeyForBeatles, Audio.keyFor("beatles,the"));
        assertEquals(mKeyForBeatles, Audio.keyFor("beatles,a"));
        assertEquals(mKeyForBeatles, Audio.keyFor("beatles,an"));
        assertEquals(mKeyForBeatles, Audio.keyFor("beatles, the"));
        assertEquals(mKeyForBeatles, Audio.keyFor("beatles, a"));
        assertEquals(mKeyForBeatles, Audio.keyFor("beatles, an"));
        assertTrue(Audio.keyFor("areosmith").compareTo(mKeyForBeatles) < 0);
        assertTrue(Audio.keyFor("coldplay").compareTo(mKeyForBeatles) > 0);
        assertTrue(Audio.keyFor("¿Cómo esto funciona?").compareTo(mKeyForBeatles) < 0);
        assertTrue(Audio.keyFor("Le passé composé").compareTo(mKeyForBeatles) > 0);
    }
}
