public class BoldTaglet extends BaseInlineTaglet {
    public BoldTaglet() {
        name = "bold";
    }
    public static void register(Map tagletMap) {
       BoldTaglet tag = new BoldTaglet();
       Taglet t = (Taglet) tagletMap.get(tag.getName());
       if (t != null) {
           tagletMap.remove(tag.getName());
       }
       tagletMap.put(tag.getName(), tag);
    }
    public TagletOutput getTagletOutput(Tag tag, TagletWriter writer) {
        ArrayList inlineTags = new ArrayList();
        inlineTags.add(new TextTag(tag.holder(), "<b>"));
        inlineTags.addAll(Arrays.asList(tag.inlineTags()));
        inlineTags.add(new TextTag(tag.holder(), "</b>"));
        return writer.commentTagsToOutput(tag, (Tag[]) inlineTags.toArray(new Tag[] {}));
    }
}
