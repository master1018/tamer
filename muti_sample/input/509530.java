public class SlideshowModel extends Model
        implements List<SlideModel>, IModelChangedObserver {
    private static final String TAG = "Mms/slideshow";
    private final LayoutModel mLayout;
    private final ArrayList<SlideModel> mSlides;
    private SMILDocument mDocumentCache;
    private PduBody mPduBodyCache;
    private int mCurrentMessageSize;
    private Context mContext;
    public static final int SLIDESHOW_SLOP = 1024;
    private SlideshowModel(Context context) {
        mLayout = new LayoutModel();
        mSlides = new ArrayList<SlideModel>();
        mContext = context;
    }
    private SlideshowModel (
            LayoutModel layouts, ArrayList<SlideModel> slides,
            SMILDocument documentCache, PduBody pbCache,
            Context context) {
        mLayout = layouts;
        mSlides = slides;
        mContext = context;
        mDocumentCache = documentCache;
        mPduBodyCache = pbCache;
        for (SlideModel slide : mSlides) {
            increaseMessageSize(slide.getSlideSize());
            slide.setParent(this);
        }
    }
    public static SlideshowModel createNew(Context context) {
        return new SlideshowModel(context);
    }
    public static SlideshowModel createFromMessageUri(
            Context context, Uri uri) throws MmsException {
        return createFromPduBody(context, getPduBody(context, uri));
    }
    public static SlideshowModel createFromPduBody(Context context, PduBody pb) throws MmsException {
        SMILDocument document = SmilHelper.getDocument(pb);
        SMILLayoutElement sle = document.getLayout();
        SMILRootLayoutElement srle = sle.getRootLayout();
        int w = srle.getWidth();
        int h = srle.getHeight();
        if ((w == 0) || (h == 0)) {
            w = LayoutManager.getInstance().getLayoutParameters().getWidth();
            h = LayoutManager.getInstance().getLayoutParameters().getHeight();
            srle.setWidth(w);
            srle.setHeight(h);
        }
        RegionModel rootLayout = new RegionModel(
                null, 0, 0, w, h);
        ArrayList<RegionModel> regions = new ArrayList<RegionModel>();
        NodeList nlRegions = sle.getRegions();
        int regionsNum = nlRegions.getLength();
        for (int i = 0; i < regionsNum; i++) {
            SMILRegionElement sre = (SMILRegionElement) nlRegions.item(i);
            RegionModel r = new RegionModel(sre.getId(), sre.getFit(),
                    sre.getLeft(), sre.getTop(), sre.getWidth(), sre.getHeight(),
                    sre.getBackgroundColor());
            regions.add(r);
        }
        LayoutModel layouts = new LayoutModel(rootLayout, regions);
        SMILElement docBody = document.getBody();
        NodeList slideNodes = docBody.getChildNodes();
        int slidesNum = slideNodes.getLength();
        ArrayList<SlideModel> slides = new ArrayList<SlideModel>(slidesNum);
        for (int i = 0; i < slidesNum; i++) {
            SMILParElement par = (SMILParElement) slideNodes.item(i);
            NodeList mediaNodes = par.getChildNodes();
            int mediaNum = mediaNodes.getLength();
            ArrayList<MediaModel> mediaSet = new ArrayList<MediaModel>(mediaNum);
            for (int j = 0; j < mediaNum; j++) {
                SMILMediaElement sme = (SMILMediaElement) mediaNodes.item(j);
                try {
                    MediaModel media = MediaModelFactory.getMediaModel(
                            context, sme, layouts, pb);
                    SmilHelper.addMediaElementEventListeners(
                            (EventTarget) sme, media);
                    mediaSet.add(media);
                } catch (DrmException e) {
                    Log.e(TAG, e.getMessage(), e);
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
            SlideModel slide = new SlideModel((int) (par.getDur() * 1000), mediaSet);
            slide.setFill(par.getFill());
            SmilHelper.addParElementEventListeners((EventTarget) par, slide);
            slides.add(slide);
        }
        SlideshowModel slideshow = new SlideshowModel(layouts, slides, document, pb, context);
        slideshow.registerModelChangedObserver(slideshow);
        return slideshow;
    }
    public PduBody toPduBody() {
        if (mPduBodyCache == null) {
            mDocumentCache = SmilHelper.getDocument(this);
            mPduBodyCache = makePduBody(mDocumentCache);
        }
        return mPduBodyCache;
    }
    private PduBody makePduBody(SMILDocument document) {
        return makePduBody(null, document, false);
    }
    private PduBody makePduBody(Context context, SMILDocument document, boolean isMakingCopy) {
        PduBody pb = new PduBody();
        boolean hasForwardLock = false;
        for (SlideModel slide : mSlides) {
            for (MediaModel media : slide) {
                if (isMakingCopy) {
                    if (media.isDrmProtected() && !media.isAllowedToForward()) {
                        hasForwardLock = true;
                        continue;
                    }
                }
                PduPart part = new PduPart();
                if (media.isText()) {
                    TextModel text = (TextModel) media;
                    if (TextUtils.isEmpty(text.getText())) {
                        continue;
                    }
                    part.setCharset(text.getCharset());
                }
                part.setContentType(media.getContentType().getBytes());
                String src = media.getSrc();
                String location;
                boolean startWithContentId = src.startsWith("cid:");
                if (startWithContentId) {
                    location = src.substring("cid:".length());
                } else {
                    location = src;
                }
                part.setContentLocation(location.getBytes());
                if (startWithContentId) {
                    part.setContentId(location.getBytes());
                }
                else {
                    int index = location.lastIndexOf(".");
                    String contentId = (index == -1) ? location
                            : location.substring(0, index);
                    part.setContentId(contentId.getBytes());
                }
                if (media.isDrmProtected()) {
                    DrmWrapper wrapper = media.getDrmObject();
                    part.setDataUri(wrapper.getOriginalUri());
                    part.setData(wrapper.getOriginalData());
                } else if (media.isText()) {
                    part.setData(((TextModel) media).getText().getBytes());
                } else if (media.isImage() || media.isVideo() || media.isAudio()) {
                    part.setDataUri(media.getUri());
                } else {
                    Log.w(TAG, "Unsupport media: " + media);
                }
                pb.addPart(part);
            }
        }
        if (hasForwardLock && isMakingCopy && context != null) {
            Toast.makeText(context,
                    context.getString(R.string.cannot_forward_drm_obj),
                    Toast.LENGTH_LONG).show();
            document = SmilHelper.getDocument(pb);
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        SmilXmlSerializer.serialize(document, out);
        PduPart smilPart = new PduPart();
        smilPart.setContentId("smil".getBytes());
        smilPart.setContentLocation("smil.xml".getBytes());
        smilPart.setContentType(ContentType.APP_SMIL.getBytes());
        smilPart.setData(out.toByteArray());
        pb.addPart(0, smilPart);
        return pb;
    }
    public PduBody makeCopy(Context context) {
        return makePduBody(context, SmilHelper.getDocument(this), true);
    }
    public SMILDocument toSmilDocument() {
        if (mDocumentCache == null) {
            mDocumentCache = SmilHelper.getDocument(this);
        }
        return mDocumentCache;
    }
    public static PduBody getPduBody(Context context, Uri msg) throws MmsException {
        PduPersister p = PduPersister.getPduPersister(context);
        GenericPdu pdu = p.load(msg);
        int msgType = pdu.getMessageType();
        if ((msgType == PduHeaders.MESSAGE_TYPE_SEND_REQ)
                || (msgType == PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF)) {
            return ((MultimediaMessagePdu) pdu).getBody();
        } else {
            throw new MmsException();
        }
    }
    public void setCurrentMessageSize(int size) {
        mCurrentMessageSize = size;
    }
    public int getCurrentMessageSize() {
        return mCurrentMessageSize;
    }
    public void increaseMessageSize(int increaseSize) {
        if (increaseSize > 0) {
            mCurrentMessageSize += increaseSize;
        }
    }
    public void decreaseMessageSize(int decreaseSize) {
        if (decreaseSize > 0) {
            mCurrentMessageSize -= decreaseSize;
        }
    }
    public LayoutModel getLayout() {
        return mLayout;
    }
    public boolean add(SlideModel object) {
        int increaseSize = object.getSlideSize();
        checkMessageSize(increaseSize);
        if ((object != null) && mSlides.add(object)) {
            increaseMessageSize(increaseSize);
            object.registerModelChangedObserver(this);
            for (IModelChangedObserver observer : mModelChangedObservers) {
                object.registerModelChangedObserver(observer);
            }
            notifyModelChanged(true);
            return true;
        }
        return false;
    }
    public boolean addAll(Collection<? extends SlideModel> collection) {
        throw new UnsupportedOperationException("Operation not supported.");
    }
    public void clear() {
        if (mSlides.size() > 0) {
            for (SlideModel slide : mSlides) {
                slide.unregisterModelChangedObserver(this);
                for (IModelChangedObserver observer : mModelChangedObservers) {
                    slide.unregisterModelChangedObserver(observer);
                }
            }
            mCurrentMessageSize = 0;
            mSlides.clear();
            notifyModelChanged(true);
        }
    }
    public boolean contains(Object object) {
        return mSlides.contains(object);
    }
    public boolean containsAll(Collection<?> collection) {
        return mSlides.containsAll(collection);
    }
    public boolean isEmpty() {
        return mSlides.isEmpty();
    }
    public Iterator<SlideModel> iterator() {
        return mSlides.iterator();
    }
    public boolean remove(Object object) {
        if ((object != null) && mSlides.remove(object)) {
            SlideModel slide = (SlideModel) object;
            decreaseMessageSize(slide.getSlideSize());
            slide.unregisterAllModelChangedObservers();
            notifyModelChanged(true);
            return true;
        }
        return false;
    }
    public boolean removeAll(Collection<?> collection) {
        throw new UnsupportedOperationException("Operation not supported.");
    }
    public boolean retainAll(Collection<?> collection) {
        throw new UnsupportedOperationException("Operation not supported.");
    }
    public int size() {
        return mSlides.size();
    }
    public Object[] toArray() {
        return mSlides.toArray();
    }
    public <T> T[] toArray(T[] array) {
        return mSlides.toArray(array);
    }
    public void add(int location, SlideModel object) {
        if (object != null) {
            int increaseSize = object.getSlideSize();
            checkMessageSize(increaseSize);
            mSlides.add(location, object);
            increaseMessageSize(increaseSize);
            object.registerModelChangedObserver(this);
            for (IModelChangedObserver observer : mModelChangedObservers) {
                object.registerModelChangedObserver(observer);
            }
            notifyModelChanged(true);
        }
    }
    public boolean addAll(int location,
            Collection<? extends SlideModel> collection) {
        throw new UnsupportedOperationException("Operation not supported.");
    }
    public SlideModel get(int location) {
        return (location >= 0 && location < mSlides.size()) ? mSlides.get(location) : null;
    }
    public int indexOf(Object object) {
        return mSlides.indexOf(object);
    }
    public int lastIndexOf(Object object) {
        return mSlides.lastIndexOf(object);
    }
    public ListIterator<SlideModel> listIterator() {
        return mSlides.listIterator();
    }
    public ListIterator<SlideModel> listIterator(int location) {
        return mSlides.listIterator(location);
    }
    public SlideModel remove(int location) {
        SlideModel slide = mSlides.remove(location);
        if (slide != null) {
            decreaseMessageSize(slide.getSlideSize());
            slide.unregisterAllModelChangedObservers();
            notifyModelChanged(true);
        }
        return slide;
    }
    public SlideModel set(int location, SlideModel object) {
        SlideModel slide = mSlides.get(location);
        if (null != object) {
            int removeSize = 0;
            int addSize = object.getSlideSize();
            if (null != slide) {
                removeSize = slide.getSlideSize();
            }
            if (addSize > removeSize) {
                checkMessageSize(addSize - removeSize);
                increaseMessageSize(addSize - removeSize);
            } else {
                decreaseMessageSize(removeSize - addSize);
            }
        }
        slide =  mSlides.set(location, object);
        if (slide != null) {
            slide.unregisterAllModelChangedObservers();
        }
        if (object != null) {
            object.registerModelChangedObserver(this);
            for (IModelChangedObserver observer : mModelChangedObservers) {
                object.registerModelChangedObserver(observer);
            }
        }
        notifyModelChanged(true);
        return slide;
    }
    public List<SlideModel> subList(int start, int end) {
        return mSlides.subList(start, end);
    }
    @Override
    protected void registerModelChangedObserverInDescendants(
            IModelChangedObserver observer) {
        mLayout.registerModelChangedObserver(observer);
        for (SlideModel slide : mSlides) {
            slide.registerModelChangedObserver(observer);
        }
    }
    @Override
    protected void unregisterModelChangedObserverInDescendants(
            IModelChangedObserver observer) {
        mLayout.unregisterModelChangedObserver(observer);
        for (SlideModel slide : mSlides) {
            slide.unregisterModelChangedObserver(observer);
        }
    }
    @Override
    protected void unregisterAllModelChangedObserversInDescendants() {
        mLayout.unregisterAllModelChangedObservers();
        for (SlideModel slide : mSlides) {
            slide.unregisterAllModelChangedObservers();
        }
    }
    public void onModelChanged(Model model, boolean dataChanged) {
        if (dataChanged) {
            mDocumentCache = null;
            mPduBodyCache = null;
        }
    }
    public void sync(PduBody pb) {
        for (SlideModel slide : mSlides) {
            for (MediaModel media : slide) {
                PduPart part = pb.getPartByContentLocation(media.getSrc());
                if (part != null) {
                    media.setUri(part.getDataUri());
                }
            }
        }
    }
    public void checkMessageSize(int increaseSize) throws ContentRestrictionException {
        ContentRestriction cr = ContentRestrictionFactory.getContentRestriction();
        cr.checkMessageSize(mCurrentMessageSize, increaseSize, mContext.getContentResolver());
    }
    public boolean isSimple() {
        if (size() != 1)
            return false;
        SlideModel slide = get(0);
        if (!(slide.hasImage() ^ slide.hasVideo()))
            return false;
        if (slide.hasAudio())
            return false;
        return true;
    }
    public void prepareForSend() {
        if (size() == 1) {
            TextModel text = get(0).getText();
            if (text != null) {
                text.cloneText();
            }
        }
    }
    public void finalResize(Uri messageUri) throws MmsException, ExceedMessageSizeException {
        int resizableCnt = 0;
        int fixedSizeTotal = 0;
        for (SlideModel slide : mSlides) {
            for (MediaModel media : slide) {
                if (media.getMediaResizable()) {
                    ++resizableCnt;
                } else {
                    fixedSizeTotal += media.getMediaSize();
                }
            }
        }
        if (resizableCnt > 0) {
            int remainingSize = MmsConfig.getMaxMessageSize() - fixedSizeTotal - SLIDESHOW_SLOP;
            if (remainingSize <= 0) {
                throw new ExceedMessageSizeException("No room for pictures");
            }
            long messageId = ContentUris.parseId(messageUri);
            int bytesPerMediaItem = remainingSize / resizableCnt;
            for (SlideModel slide : mSlides) {
                for (MediaModel media : slide) {
                    if (media.getMediaResizable()) {
                        media.resizeMedia(bytesPerMediaItem, messageId);
                    }
                }
            }
            int totalSize = 0;
            for (SlideModel slide : mSlides) {
                for (MediaModel media : slide) {
                    totalSize += media.getMediaSize();
                }
            }
            if (totalSize > MmsConfig.getMaxMessageSize()) {
                throw new ExceedMessageSizeException("After compressing pictures, message too big");
            }
            setCurrentMessageSize(totalSize);
            onModelChanged(this, true);     
            PduBody pb = toPduBody();
            PduPersister.getPduPersister(mContext).updateParts(messageUri, pb);
        }
    }
}
