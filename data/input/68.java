public abstract class AbstractProtostuffSerializer implements ObjectSerializer<MediaContent> {
    public MediaContent create() {
        MediaContent mediaContent = new MediaContent(new Media("http:
        return mediaContent;
    }
}
