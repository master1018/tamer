public class UnderlineTaglet extends BaseInlineTaglet {
    public UnderlineTaglet() {
        name = "underline";
    }
    public static void register(Map tagletMap) {
       UnderlineTaglet tag = new UnderlineTaglet();
       Taglet t = (Taglet) tagletMap.get(tag.getName());
       if (t != null) {
           tagletMap.remove(tag.getName());
       }
       tagletMap.put(tag.getName(), tag);
    }
    public TagletOutput getTagletOutput(Tag tag, TagletWriter writer) {
        ArrayList inlineTags = new ArrayList();
        inlineTags.add(new TextTag(tag.holder(), "<u>"));
        inlineTags.addAll(Arrays.asList(tag.inlineTags()));
        inlineTags.add(new TextTag(tag.holder(), "</u>"));
        return writer.commentTagsToOutput(tag, (Tag[]) inlineTags.toArray(new Tag[] {}));
    }
}
