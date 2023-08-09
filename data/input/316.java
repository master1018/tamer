public class DocRootTaglet extends BaseInlineTaglet {
    public DocRootTaglet() {
        name = "docRoot";
    }
    public TagletOutput getTagletOutput(Tag tag, TagletWriter writer) {
        return writer.getDocRootOutput();
    }
}
