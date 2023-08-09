public class SlideModel extends Model implements List<MediaModel>, EventListener {
    public static final String TAG = "Mms/slideshow";
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = DEBUG ? Config.LOGD : Config.LOGV;
    private static final int DEFAULT_SLIDE_DURATION = 5000;
    private final ArrayList<MediaModel> mMedia = new ArrayList<MediaModel>();
    private MediaModel mText;
    private MediaModel mImage;
    private MediaModel mAudio;
    private MediaModel mVideo;
    private boolean mCanAddImage = true;
    private boolean mCanAddAudio = true;
    private boolean mCanAddVideo = true;
    private int mDuration;
    private boolean mVisible = true;
    private short mFill;
    private int mSlideSize;
    private SlideshowModel mParent;
    public SlideModel(SlideshowModel slideshow) {
        this(DEFAULT_SLIDE_DURATION, slideshow);
    }
    public SlideModel(int duration, SlideshowModel slideshow) {
        mDuration = duration;
        mParent = slideshow;
    }
    public SlideModel(int duration, ArrayList<MediaModel> mediaList) {
        mDuration = duration;
        int maxDur = 0;
        for (MediaModel media : mediaList) {
            internalAdd(media);
            int mediaDur = media.getDuration();
            if (mediaDur > maxDur) {
                maxDur = mediaDur;
            }
        }
        updateDuration(maxDur);
    }
    private void internalAdd(MediaModel media) throws IllegalStateException {
        if (media == null) {
            return;
        }
        if (media.isText()) {
            String contentType = media.getContentType();
            if (TextUtils.isEmpty(contentType) || ContentType.TEXT_PLAIN.equals(contentType)
                    || ContentType.TEXT_HTML.equals(contentType)) {
                internalAddOrReplace(mText, media);
                mText = media;
            } else {
                Log.w(TAG, "[SlideModel] content type " + media.getContentType() +
                        " isn't supported (as text)");
            }
        } else if (media.isImage()) {
            if (mCanAddImage) {
                internalAddOrReplace(mImage, media);
                mImage = media;
                mCanAddVideo = false;
            } else {
                throw new IllegalStateException();
            }
        } else if (media.isAudio()) {
            if (mCanAddAudio) {
                internalAddOrReplace(mAudio, media);
                mAudio = media;
                mCanAddVideo = false;
            } else {
                throw new IllegalStateException();
            }
        } else if (media.isVideo()) {
            if (mCanAddVideo) {
                internalAddOrReplace(mVideo, media);
                mVideo = media;
                mCanAddImage = false;
                mCanAddAudio = false;
            } else {
                throw new IllegalStateException();
            }
        }
    }
    private void internalAddOrReplace(MediaModel old, MediaModel media) {
        int addSize = media.getMediaResizable() ? 0 : media.getMediaSize();
        int removeSize;
        if (old == null) {
            if (null != mParent) {
                mParent.checkMessageSize(addSize);
            }
            mMedia.add(media);
            increaseSlideSize(addSize);
            increaseMessageSize(addSize);
        } else {
            removeSize = old.getMediaSize();
            if (addSize > removeSize) {
                if (null != mParent) {
                    mParent.checkMessageSize(addSize - removeSize);
                }
                increaseSlideSize(addSize - removeSize);
                increaseMessageSize(addSize - removeSize);
            } else {
                decreaseSlideSize(removeSize - addSize);
                decreaseMessageSize(removeSize - addSize);
            }
            mMedia.set(mMedia.indexOf(old), media);
            old.unregisterAllModelChangedObservers();
        }
        for (IModelChangedObserver observer : mModelChangedObservers) {
            media.registerModelChangedObserver(observer);
        }
    }
    private boolean internalRemove(Object object) {
        if (mMedia.remove(object)) {
            if (object instanceof TextModel) {
                mText = null;
            } else if (object instanceof ImageModel) {
                mImage = null;
                mCanAddVideo = true;
            } else if (object instanceof AudioModel) {
                mAudio = null;
                mCanAddVideo = true;
            } else if (object instanceof VideoModel) {
                mVideo = null;
                mCanAddImage = true;
                mCanAddAudio = true;
            }
            int decreaseSize = ((MediaModel) object).getMediaResizable() ? 0
                                        : ((MediaModel) object).getMediaSize();
            decreaseSlideSize(decreaseSize);
            decreaseMessageSize(decreaseSize);
            ((Model) object).unregisterAllModelChangedObservers();
            return true;
        }
        return false;
    }
    public int getDuration() {
        return mDuration;
    }
    public void setDuration(int duration) {
        mDuration = duration;
        notifyModelChanged(true);
    }
    public int getSlideSize() {
        return mSlideSize;
    }
    public void increaseSlideSize(int increaseSize) {
        if (increaseSize > 0) {
            mSlideSize += increaseSize;
        }
    }
    public void decreaseSlideSize(int decreaseSize) {
        if (decreaseSize > 0) {
            mSlideSize -= decreaseSize;
        }
    }
    public void setParent(SlideshowModel parent) {
        mParent = parent;
    }
    public void increaseMessageSize(int increaseSize) {
        if ((increaseSize > 0) && (null != mParent)) {
            int size = mParent.getCurrentMessageSize();
            size += increaseSize;
            mParent.setCurrentMessageSize(size);
        }
    }
    public void decreaseMessageSize(int decreaseSize) {
        if ((decreaseSize > 0) && (null != mParent)) {
            int size = mParent.getCurrentMessageSize();
            size -= decreaseSize;
            mParent.setCurrentMessageSize(size);
        }
    }
    public boolean add(MediaModel object) {
        internalAdd(object);
        notifyModelChanged(true);
        return true;
    }
    public boolean addAll(Collection<? extends MediaModel> collection) {
        throw new UnsupportedOperationException("Operation not supported.");
    }
    public void clear() {
        if (mMedia.size() > 0) {
            for (MediaModel media : mMedia) {
                media.unregisterAllModelChangedObservers();
                int decreaseSize = media.getMediaSize();
                decreaseSlideSize(decreaseSize);
                decreaseMessageSize(decreaseSize);
            }
            mMedia.clear();
            mText = null;
            mImage = null;
            mAudio = null;
            mVideo = null;
            mCanAddImage = true;
            mCanAddAudio = true;
            mCanAddVideo = true;
            notifyModelChanged(true);
        }
    }
    public boolean contains(Object object) {
        return mMedia.contains(object);
    }
    public boolean containsAll(Collection<?> collection) {
        return mMedia.containsAll(collection);
    }
    public boolean isEmpty() {
        return mMedia.isEmpty();
    }
    public Iterator<MediaModel> iterator() {
        return mMedia.iterator();
    }
    public boolean remove(Object object) {
        if ((object != null) && (object instanceof MediaModel)
                && internalRemove(object)) {
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
        return mMedia.size();
    }
    public Object[] toArray() {
        return mMedia.toArray();
    }
    public <T> T[] toArray(T[] array) {
        return mMedia.toArray(array);
    }
    public void add(int location, MediaModel object) {
        throw new UnsupportedOperationException("Operation not supported.");
    }
    public boolean addAll(int location,
            Collection<? extends MediaModel> collection) {
        throw new UnsupportedOperationException("Operation not supported.");
    }
    public MediaModel get(int location) {
        if (mMedia.size() == 0) {
            return null;
        }
        return mMedia.get(location);
    }
    public int indexOf(Object object) {
        return mMedia.indexOf(object);
    }
    public int lastIndexOf(Object object) {
        return mMedia.lastIndexOf(object);
    }
    public ListIterator<MediaModel> listIterator() {
        return mMedia.listIterator();
    }
    public ListIterator<MediaModel> listIterator(int location) {
        return mMedia.listIterator(location);
    }
    public MediaModel remove(int location) {
        MediaModel media = mMedia.get(location);
        if ((media != null) && internalRemove(media)) {
            notifyModelChanged(true);
        }
        return media;
    }
    public MediaModel set(int location, MediaModel object) {
        throw new UnsupportedOperationException("Operation not supported.");
    }
    public List<MediaModel> subList(int start, int end) {
        return mMedia.subList(start, end);
    }
    public boolean isVisible() {
        return mVisible;
    }
    public void setVisible(boolean visible) {
        mVisible = visible;
        notifyModelChanged(true);
    }
    public short getFill() {
        return mFill;
    }
    public void setFill(short fill) {
        mFill = fill;
        notifyModelChanged(true);
    }
    @Override
    protected void registerModelChangedObserverInDescendants(
            IModelChangedObserver observer) {
        for (MediaModel media : mMedia) {
            media.registerModelChangedObserver(observer);
        }
    }
    @Override
    protected void unregisterModelChangedObserverInDescendants(
            IModelChangedObserver observer) {
        for (MediaModel media : mMedia) {
            media.unregisterModelChangedObserver(observer);
        }
    }
    @Override
    protected void unregisterAllModelChangedObserversInDescendants() {
        for (MediaModel media : mMedia) {
            media.unregisterAllModelChangedObservers();
        }
    }
    public void handleEvent(Event evt) {
        if (evt.getType().equals(SmilParElementImpl.SMIL_SLIDE_START_EVENT)) {
            if (LOCAL_LOGV) {
                Log.v(TAG, "Start to play slide: " + this);
            }
            mVisible = true;
        } else if (mFill != ElementTime.FILL_FREEZE) {
            if (LOCAL_LOGV) {
                Log.v(TAG, "Stop playing slide: " + this);
            }
            mVisible = false;
        }
        notifyModelChanged(false);
    }
    public boolean hasText() {
        return mText != null;
    }
    public boolean hasImage() {
        return mImage != null;
    }
    public boolean hasAudio() {
        return mAudio != null;
    }
    public boolean hasVideo() {
        return mVideo != null;
    }
    public boolean removeText() {
        return remove(mText);
    }
    public boolean removeImage() {
        return remove(mImage);
    }
    public boolean removeAudio() {
        return remove(mAudio);
    }
    public boolean removeVideo() {
        return remove(mVideo);
    }
    public TextModel getText() {
        return (TextModel) mText;
    }
    public ImageModel getImage() {
        return (ImageModel) mImage;
    }
    public AudioModel getAudio() {
        return (AudioModel) mAudio;
    }
    public VideoModel getVideo() {
        return (VideoModel) mVideo;
    }
    public void updateDuration(int duration) {
        if (duration <= 0) {
            return;
        }
        if ((duration > mDuration)
                || (mDuration == DEFAULT_SLIDE_DURATION)) {
            mDuration = duration;
        }
    }
}
