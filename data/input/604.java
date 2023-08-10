public class Mp4TagCreator extends AbstractTagCreator {
    protected int getFixedTagLength(Tag tag) throws UnsupportedEncodingException {
        throw new RuntimeException("Not implemented");
    }
    protected Tag getCompatibleTag(Tag tag) {
        throw new RuntimeException("Not implemented");
    }
    protected void create(Tag tag, ByteBuffer buf, List fields, int tagSize, int padding) throws UnsupportedEncodingException {
        throw new RuntimeException("Not implemented");
    }
}
