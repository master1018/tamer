public class ImageFilter implements ImageConsumer, Cloneable {
    protected ImageConsumer consumer;
    public ImageFilter() {
        super();
    }
    public ImageFilter getFilterInstance(ImageConsumer ic) {
        ImageFilter filter = (ImageFilter)clone();
        filter.consumer = ic;
        return filter;
    }
    @SuppressWarnings("unchecked")
    public void setProperties(Hashtable<?, ?> props) {
        Hashtable<Object, Object> fprops;
        if (props == null) {
            fprops = new Hashtable<Object, Object>();
        } else {
            fprops = (Hashtable<Object, Object>)props.clone();
        }
        String propName = "Filters"; 
        String prop = "Null filter"; 
        Object o = fprops.get(propName);
        if (o != null) {
            if (o instanceof String) {
                prop = (String)o + "; " + prop; 
            } else {
                prop = o.toString() + "; " + prop; 
            }
        }
        fprops.put(propName, prop);
        consumer.setProperties(fprops);
    }
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
    public void resendTopDownLeftRight(ImageProducer ip) {
        ip.requestTopDownLeftRightResend(this);
    }
    public void setColorModel(ColorModel model) {
        consumer.setColorModel(model);
    }
    public void setPixels(int x, int y, int w, int h, ColorModel model, int[] pixels, int off,
            int scansize) {
        consumer.setPixels(x, y, w, h, model, pixels, off, scansize);
    }
    public void setPixels(int x, int y, int w, int h, ColorModel model, byte[] pixels, int off,
            int scansize) {
        consumer.setPixels(x, y, w, h, model, pixels, off, scansize);
    }
    public void setDimensions(int width, int height) {
        consumer.setDimensions(width, height);
    }
    public void setHints(int hints) {
        consumer.setHints(hints);
    }
    public void imageComplete(int status) {
        consumer.imageComplete(status);
    }
}
