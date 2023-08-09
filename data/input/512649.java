public class FilteredImageSource implements ImageProducer {
    private final ImageProducer source;
    private final ImageFilter filter;
    private final Hashtable<ImageConsumer, ImageConsumer> consTable = new Hashtable<ImageConsumer, ImageConsumer>();
    public FilteredImageSource(ImageProducer orig, ImageFilter imgf) {
        source = orig;
        filter = imgf;
    }
    public synchronized boolean isConsumer(ImageConsumer ic) {
        if (ic != null) {
            return consTable.containsKey(ic);
        }
        return false;
    }
    public void startProduction(ImageConsumer ic) {
        addConsumer(ic);
        ImageConsumer fic = consTable.get(ic);
        source.startProduction(fic);
    }
    public void requestTopDownLeftRightResend(ImageConsumer ic) {
        if (ic != null && isConsumer(ic)) {
            ImageFilter fic = (ImageFilter)consTable.get(ic);
            fic.resendTopDownLeftRight(source);
        }
    }
    public synchronized void removeConsumer(ImageConsumer ic) {
        if (ic != null && isConsumer(ic)) {
            ImageConsumer fic = consTable.get(ic);
            source.removeConsumer(fic);
            consTable.remove(ic);
        }
    }
    public synchronized void addConsumer(ImageConsumer ic) {
        if (ic != null && !isConsumer(ic)) {
            ImageConsumer fic = filter.getFilterInstance(ic);
            source.addConsumer(fic);
            consTable.put(ic, fic);
        }
    }
}
