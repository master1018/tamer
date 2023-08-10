class Stemmer {
    protected static Log log = LogFactory.getLog(Stemmer.class);
    private SnowballProgram stemmer;
    private Method stemMethod;
    public Stemmer(String lang) throws SecurityException, NoSuchMethodException {
        String language = new Locale(lang).getDisplayLanguage(Locale.ENGLISH);
        try {
            Class stemClass = Class.forName("net.sf.snowball.ext." + language + "Stemmer");
            stemmer = (SnowballProgram) stemClass.newInstance();
            stemMethod = stemmer.getClass().getMethod("stem", new Class[0]);
        } catch (Exception e) {
            log.info("Error instantiating stemmer for language:" + language);
            log.info("Trying with English Stemmer.");
            stemmer = new net.sf.snowball.ext.EnglishStemmer();
            stemMethod = stemmer.getClass().getMethod("stem", new Class[0]);
        }
    }
    public String stem(String term) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        if (term == null) return null;
        stemmer.setCurrent(term.toLowerCase());
        stemMethod.invoke(stemmer, new Object[0]);
        return stemmer.getCurrent();
    }
}
