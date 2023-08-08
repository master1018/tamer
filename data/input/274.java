public class FeatureRegexFilterFactoryTest {
    public static final String DIC_ENCODING = System.getProperty("net.moraleboost.mecab.encoding");
    TokenStream tokenizer = null;
    @Before
    public void setUp() {
        Map<String, String> args = new HashMap<String, String>();
        args.put("charset", DIC_ENCODING);
        args.put("arg", "");
        StandardMeCabTokenizerFactory factory = new StandardMeCabTokenizerFactory();
        factory.init(args);
        StringReader reader = new StringReader("本日は晴天なり。");
        tokenizer = factory.create(reader);
    }
    @Test
    public void testCreate() throws Exception {
        Map<String, String> args = new HashMap<String, String>();
        args.put("charset", "Shift_JIS");
        args.put("source", "test" + java.io.File.separator + "regexfilter.txt");
        FeatureRegexFilterFactory factory = new FeatureRegexFilterFactory();
        factory.init(args);
        TokenStream stream = factory.create(tokenizer);
        CharTermAttribute termAttr = stream.getAttribute(CharTermAttribute.class);
        while (stream.incrementToken()) {
            String termText = new String(termAttr.buffer(), 0, termAttr.length());
            if (termText.equals("は") || termText.equals("なり")) {
                fail("Filter not working.");
            }
        }
    }
}
