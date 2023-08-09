public class ToDoTaglet implements Taglet {
    private static final String NAME = "todo";
    private static final String HEADER = "To Do:";
    public String getName() {
        return NAME;
    }
    public boolean inField() {
        return true;
    }
    public boolean inConstructor() {
        return true;
    }
    public boolean inMethod() {
        return true;
    }
    public boolean inOverview() {
        return true;
    }
    public boolean inPackage() {
        return true;
    }
    public boolean inType() {
        return true;
    }
    public boolean isInlineTag() {
        return false;
    }
    public static void register(Map tagletMap) {
       ToDoTaglet tag = new ToDoTaglet();
       Taglet t = (Taglet) tagletMap.get(tag.getName());
       if (t != null) {
           tagletMap.remove(tag.getName());
       }
       tagletMap.put(tag.getName(), tag);
    }
    public String toString(Tag tag) {
        return "<DT><B>" + HEADER + "</B><DD>"
               + "<table cellpadding=2 cellspacing=0><tr><td bgcolor=\"yellow\">"
               + tag.text()
               + "</td></tr></table></DD>\n";
    }
    public String toString(Tag[] tags) {
        if (tags.length == 0) {
            return null;
        }
        String result = "\n<DT><B>" + HEADER + "</B><DD>";
        result += "<table cellpadding=2 cellspacing=0><tr><td bgcolor=\"yellow\">";
        for (int i = 0; i < tags.length; i++) {
            if (i > 0) {
                result += ", ";
            }
            result += tags[i].text();
        }
        return result + "</td></tr></table></DD>\n";
    }
}
