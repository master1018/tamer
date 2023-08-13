public class test {
    public String getContentRule() {
        ChannelExt ext = getChannelExt();
        if (ext != null) {
            return ext.getContentRule();
        } else {
            return null;
        }
    }
}
