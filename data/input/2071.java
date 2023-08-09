public class GreenTaglet extends BaseInlineTaglet {
    public GreenTaglet() {
        name = "green";
    }
    public static void register(Map tagletMap) {
       GreenTaglet tag = new GreenTaglet();
       Taglet t = (Taglet) tagletMap.get(tag.getName());
       if (t != null) {
           tagletMap.remove(tag.getName());
       }
       tagletMap.put(tag.getName(), tag);
    }
    public TagletOutput getTagletOutput(Tag tag, TagletWriter writer) {
        ArrayList inlineTags = new ArrayList();
        inlineTags.add(new TextTag(tag.holder(), "<font color=\"green\">"));
        inlineTags.addAll(Arrays.asList(tag.inlineTags()));
        inlineTags.add(new TextTag(tag.holder(), "</font>"));
        return writer.commentTagsToOutput(tag, (Tag[]) inlineTags.toArray(new Tag[] {}));
    }
}
