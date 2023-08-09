public class ExtendedWikiHelper extends SimpleWikiHelper {
    private static final String STYLE_SHEET = "<style>h2 {font-size:1.2em;font-weight:normal;} " +
            "a {color:#6688cc;} ol {padding-left:1.5em;} blockquote {margin-left:0em;} " +
            ".interProject, .noprint {display:none;} " +
            "li, blockquote {margin-top:0.5em;margin-bottom:0.5em;}</style>";
    private static final Pattern sValidSections =
        Pattern.compile("(verb|noun|adjective|pronoun|interjection)", Pattern.CASE_INSENSITIVE);
    private static final Pattern sSectionSplit =
        Pattern.compile("^=+(.+?)=+.+?(?=^=)", Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern sInvalidWord = Pattern.compile("[^A-Za-z0-9 ]");
    public static final String WIKI_AUTHORITY = "wiktionary";
    public static final String WIKI_LOOKUP_HOST = "lookup";
    public static final String MIME_TYPE = "text/html";
    public static final String ENCODING = "utf-8";
    private static final String WIKTIONARY_RANDOM =
        "http:
    private static final String STUB_SECTION = "\n=Stub section=";
    private static final int RANDOM_TRIES = 3;
    private static class FormatRule {
        private Pattern mPattern;
        private String mReplaceWith;
        public FormatRule(String pattern, String replaceWith, int flags) {
            mPattern = Pattern.compile(pattern, flags);
            mReplaceWith = replaceWith;
        }
        public FormatRule(String pattern, String replaceWith) {
            this(pattern, replaceWith, 0);
        }
        public String apply(String input) {
            Matcher m = mPattern.matcher(input);
            return m.replaceAll(mReplaceWith);
        }
    }
    private static final List<FormatRule> sFormatRules = new ArrayList<FormatRule>();
    static {
        sFormatRules.add(new FormatRule("^=+(.+?)=+", "</ol><h2>$1</h2><ol>",
                Pattern.MULTILINE));
        sFormatRules.add(new FormatRule("^#+\\*?:(.+?)$", "<blockquote>$1</blockquote>",
                Pattern.MULTILINE));
        sFormatRules.add(new FormatRule("^#+:?\\*(.+?)$", "<ul><li>$1</li></ul>",
                Pattern.MULTILINE));
        sFormatRules.add(new FormatRule("^#+(.+?)$", "<li>$1</li>",
                Pattern.MULTILINE));
        sFormatRules.add(new FormatRule("\\[\\[([^:\\|\\]]+)\\]\\]",
                String.format("<a href=\"%s:
        sFormatRules.add(new FormatRule("\\[\\[([^:\\|\\]]+)\\|([^\\]]+)\\]\\]",
                String.format("<a href=\"%s:
        sFormatRules.add(new FormatRule("'''(.+?)'''", "<b>$1</b>"));
        sFormatRules.add(new FormatRule("([^'])''([^'].*?[^'])''([^'])", "$1<i>$2</i>$3"));
        sFormatRules.add(new FormatRule("(\\{+.+?\\}+|\\[\\[[^:]+:[^\\\\|\\]]+\\]\\]|" +
                "\\[http.+?\\]|\\[\\[Category:.+?\\]\\])", "", Pattern.MULTILINE | Pattern.DOTALL));
        sFormatRules.add(new FormatRule("\\[\\[([^\\|\\]]+\\|)?(.+?)\\]\\]", "$2",
                Pattern.MULTILINE));
    }
    public static String getRandomWord() throws ApiException, ParseException {
        int tries = 0;
        while (tries++ < RANDOM_TRIES) {
            String content = getUrlContent(WIKTIONARY_RANDOM);
            try {
                JSONObject response = new JSONObject(content);
                JSONObject query = response.getJSONObject("query");
                JSONArray random = query.getJSONArray("random");
                JSONObject word = random.getJSONObject(0);
                String foundWord = word.getString("title");
                if (foundWord != null &&
                        !sInvalidWord.matcher(foundWord).find()) {
                    return foundWord;
                }
            } catch (JSONException e) {
                throw new ParseException("Problem parsing API response", e);
            }
        }
        return null;
    }
    public static String formatWikiText(String wikiText) {
        if (wikiText == null) {
            return null;
        }
        wikiText = wikiText.concat(STUB_SECTION);
        HashSet<String> foundSections = new HashSet<String>();
        StringBuilder builder = new StringBuilder();
        Matcher sectionMatcher = sSectionSplit.matcher(wikiText);
        while (sectionMatcher.find()) {
            String title = sectionMatcher.group(1);
            if (!foundSections.contains(title) &&
                    sValidSections.matcher(title).matches()) {
                String sectionContent = sectionMatcher.group();
                foundSections.add(title);
                builder.append(sectionContent);
            }
        }
        wikiText = builder.toString();
        for (FormatRule rule : sFormatRules) {
            wikiText = rule.apply(wikiText);
        }
        if (!TextUtils.isEmpty(wikiText)) {
            return STYLE_SHEET + wikiText;
        } else {
            return null;
        }
    }
}
