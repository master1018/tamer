public class test {
        protected int getSampleSizeInBytes() {
            return getFormat().getFrameSize() / getFormat().getChannels();
        }
}
