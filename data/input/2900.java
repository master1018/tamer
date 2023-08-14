public class test {
    public String getKeywords() {
        ChannelExt ext = getChannelExt();
        if (ext != null) {
            return ext.getKeywords();
        } else {
            return null;
        }
    }
}
