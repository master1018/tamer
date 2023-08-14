public abstract class DecodingImageSource implements ImageProducer {
    List<ImageConsumer> consumers = new ArrayList<ImageConsumer>(5);
    List<ImageDecoder> decoders = new ArrayList<ImageDecoder>(5);
    boolean loading;
    ImageDecoder decoder;
    protected abstract boolean checkConnection();
    protected abstract InputStream getInputStream();
    public synchronized void addConsumer(ImageConsumer ic) {
        if (!checkConnection()) { 
            ic.imageComplete(ImageConsumer.IMAGEERROR);
            return;
        }
        ImageConsumer cons = findConsumer(consumers, ic);
        if (cons == null) { 
            ImageDecoder d = null;
            for (Iterator<ImageDecoder> i = decoders.iterator(); i.hasNext();) {
                d = i.next();
                cons = findConsumer(d.consumers, ic);
                if (cons != null) {
                    break;
                }
            }
        }
        if (cons == null) { 
            consumers.add(ic);
        }
    }
    private void abortConsumer(ImageConsumer ic) {
        ic.imageComplete(ImageConsumer.IMAGEERROR);
        consumers.remove(ic);
    }
    private void abortAllConsumers(List<ImageConsumer> consumersList) {
        for (ImageConsumer imageConsumer : consumersList) {
            abortConsumer(imageConsumer);
        }
    }
    public synchronized void removeConsumer(ImageConsumer ic) {
        ImageDecoder d = null;
        for (Iterator<ImageDecoder> i = decoders.iterator(); i.hasNext();) {
            d = i.next();
            removeConsumer(d.consumers, ic);
            if (d.consumers.size() <= 0) {
                d.terminate();
            }
        }
        removeConsumer(consumers, ic);
    }
    private static void removeConsumer(List<ImageConsumer> consumersList, ImageConsumer ic) {
        ImageConsumer cons = null;
        for (Iterator<ImageConsumer> i = consumersList.iterator(); i.hasNext();) {
            cons = i.next();
            if (cons.equals(ic)) {
                i.remove();
            }
        }
    }
    public void requestTopDownLeftRightResend(ImageConsumer consumer) {
    }
    public synchronized void startProduction(ImageConsumer ic) {
        if (ic != null) {
            addConsumer(ic);
        }
        if (!loading && consumers.size() > 0) {
            ImageLoader.addImageSource(this);
            loading = true;
        }
    }
    public synchronized boolean isConsumer(ImageConsumer ic) {
        ImageDecoder d = null;
        for (Iterator<ImageDecoder> i = decoders.iterator(); i.hasNext();) {
            d = i.next();
            if (findConsumer(d.consumers, ic) != null) {
                return true;
            }
        }
        return findConsumer(consumers, ic) != null;
    }
    private static ImageConsumer findConsumer(List<ImageConsumer> consumersList, ImageConsumer ic) {
        ImageConsumer res = null;
        for (Iterator<ImageConsumer> i = consumersList.iterator(); i.hasNext();) {
            res = i.next();
            if (res.equals(ic)) {
                return res;
            }
        }
        return null;
    }
    synchronized void lockDecoder(ImageDecoder d) {
        if (d == decoder) {
            decoder = null;
            startProduction(null);
        }
    }
    private ImageDecoder createDecoder() {
        InputStream is = getInputStream();
        ImageDecoder decoder;
        if (is == null) {
            decoder = null;
        } else {
            decoder = ImageDecoder.createDecoder(this, is);
        }
        if (decoder != null) {
            synchronized (this) {
                decoders.add(decoder);
                this.decoder = decoder;
                loading = false;
                consumers = new ArrayList<ImageConsumer>(5); 
            }
            return decoder;
        }
        List<ImageConsumer> cs;
        synchronized (this) {
            cs = consumers;
            consumers = new ArrayList<ImageConsumer>(5);
            loading = false;
        }
        abortAllConsumers(cs);
        return null;
    }
    private synchronized void removeDecoder(ImageDecoder dr) {
        lockDecoder(dr);
        decoders.remove(dr);
    }
    public void load() {
        synchronized (this) {
            if (consumers.size() == 0) {
                loading = false;
                return;
            }
        }
        ImageDecoder d = createDecoder();
        if (d != null) {
            try {
                decoder.decodeImage();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                removeDecoder(d);
                abortAllConsumers(d.consumers);
            }
        }
    }
}
