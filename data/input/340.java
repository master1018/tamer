public class UserConfiguration {
    private List<Pattern> patternTestClasses = new ArrayList<Pattern>();
    public void setPatternTestClasses(String patternTestClasses) {
        StringTokenizer patternTokenizer = new StringTokenizer(patternTestClasses, ";");
        List<Pattern> result = new ArrayList<Pattern>();
        while (patternTokenizer.hasMoreElements()) {
            String s = patternTokenizer.nextToken();
            result.add(Pattern.compile(s, Pattern.CASE_INSENSITIVE));
        }
        this.patternTestClasses = result;
    }
    public String getPatternTestClasses() {
        StringBuffer result = new StringBuffer();
        for (Pattern p : patternTestClasses) {
            p.toString();
        }
        return result.toString().substring(0, (result.toString().length() - 1));
    }
    public List<Pattern> getPatterListTestClasses() {
        return Collections.unmodifiableList(patternTestClasses);
    }
}
