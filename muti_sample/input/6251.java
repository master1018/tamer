class DocLocale {
    final String localeName;
    final Locale locale;
    final Collator collator;
    private final DocEnv docenv;
    private final BreakIterator sentenceBreaker;
    private boolean useBreakIterator = false;
    static final String[] sentenceTerminators =
                    {
                        "<p>", "</p>", "<h1>", "<h2>",
                        "<h3>", "<h4>", "<h5>", "<h6>",
                        "</h1>", "</h2>", "</h3>", "</h4>", "</h5>",
                        "</h6>", "<hr>", "<pre>", "</pre>"
                    };
    DocLocale(DocEnv docenv, String localeName, boolean useBreakIterator) {
        this.docenv = docenv;
        this.localeName = localeName;
        this.useBreakIterator = useBreakIterator;
        locale = getLocale();
        if (locale == null) {
            docenv.exit();
        } else {
            Locale.setDefault(locale);
        }
        collator = Collator.getInstance(locale);
        sentenceBreaker = BreakIterator.getSentenceInstance(locale);
    }
    private Locale getLocale() {
        Locale userlocale = null;
        if (localeName.length() > 0) {
            int firstuscore = localeName.indexOf('_');
            int seconduscore = -1;
            String language = null;
            String country = null;
            String variant = null;
            if (firstuscore == 2) {
                language = localeName.substring(0, firstuscore);
                seconduscore = localeName.indexOf('_', firstuscore + 1);
                if (seconduscore > 0) {
                    if (seconduscore != firstuscore + 3 ||
                           localeName.length() <= seconduscore + 1) {
                        docenv.error(null, "main.malformed_locale_name", localeName);
                        return null;
                    }
                    country = localeName.substring(firstuscore + 1,
                                                   seconduscore);
                    variant = localeName.substring(seconduscore + 1);
                } else if (localeName.length() == firstuscore + 3) {
                    country = localeName.substring(firstuscore + 1);
                } else {
                    docenv.error(null, "main.malformed_locale_name", localeName);
                    return null;
                }
            } else if (firstuscore == -1 && localeName.length() == 2) {
                language = localeName;
            } else {
                docenv.error(null, "main.malformed_locale_name", localeName);
                return null;
            }
            userlocale = searchLocale(language, country, variant);
            if (userlocale == null) {
                docenv.error(null, "main.illegal_locale_name", localeName);
                return null;
            } else {
                return userlocale;
            }
        } else {
            return Locale.getDefault();
        }
    }
    private Locale searchLocale(String language, String country,
                                String variant) {
        Locale[] locales = Locale.getAvailableLocales();
        for (int i = 0; i < locales.length; i++) {
            if (locales[i].getLanguage().equals(language) &&
               (country == null || locales[i].getCountry().equals(country)) &&
               (variant == null || locales[i].getVariant().equals(variant))) {
                return locales[i];
            }
        }
        return null;
    }
    String localeSpecificFirstSentence(DocImpl doc, String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        int index = s.indexOf("-->");
        if(s.trim().startsWith("<!--") && index != -1) {
            return localeSpecificFirstSentence(doc, s.substring(index + 3, s.length()));
        }
        if (useBreakIterator || !locale.getLanguage().equals("en")) {
            sentenceBreaker.setText(s.replace('\n', ' '));
            int start = sentenceBreaker.first();
            int end = sentenceBreaker.next();
            return s.substring(start, end).trim();
        } else {
            return englishLanguageFirstSentence(s).trim();
        }
    }
    private String englishLanguageFirstSentence(String s) {
        if (s == null) {
            return null;
        }
        int len = s.length();
        boolean period = false;
        for (int i = 0 ; i < len ; i++) {
            switch (s.charAt(i)) {
                case '.':
                    period = true;
                    break;
                case ' ':
                case '\t':
                case '\n':
            case '\r':
            case '\f':
                    if (period) {
                        return s.substring(0, i);
                    }
                    break;
            case '<':
                    if (i > 0) {
                        if (htmlSentenceTerminatorFound(s, i)) {
                            return s.substring(0, i);
                        }
                    }
                    break;
                default:
                    period = false;
            }
        }
        return s;
    }
    private boolean htmlSentenceTerminatorFound(String str, int index) {
        for (int i = 0; i < sentenceTerminators.length; i++) {
            String terminator = sentenceTerminators[i];
            if (str.regionMatches(true, index, terminator,
                                  0, terminator.length())) {
                    return true;
            }
        }
        return false;
    }
}
