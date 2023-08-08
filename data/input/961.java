public class RegexUtils {
    public static List<String> getMatched(String text, String regex) {
        List<String> result = new ArrayList<String>();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            result.add(matcher.group());
        }
        return result;
    }
    public static List<List> getGroups(String text, String regex) {
        List<List> result = new ArrayList<List>();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            List<String> list = new ArrayList<String>();
            for (int i = 0; i < matcher.groupCount() + 1; i++) {
                list.add(matcher.group(i));
            }
            result.add(list);
        }
        return result;
    }
}
