public class SlideshowEditor {
    private static final String TAG = "Mms:slideshow";
    public static final int MAX_SLIDE_NUM = 10;
    private final Context mContext;
    private final SlideshowModel mModel;
    public SlideshowEditor(Context context, SlideshowModel model) {
        mContext = context;
        mModel = model;
    }
    public boolean addNewSlide() {
        int position = mModel.size();
        return addNewSlide(position);
    }
    public boolean addNewSlide(int position) {
        int size = mModel.size();
        if (size < MAX_SLIDE_NUM) {
            SlideModel slide = new SlideModel(mModel);
            TextModel text = new TextModel(
                    mContext, ContentType.TEXT_PLAIN, "text_" + size + ".txt",
                    mModel.getLayout().getTextRegion());
            slide.add(text);
            mModel.add(position, slide);
            return true;
        } else {
            Log.w(TAG, "The limitation of the number of slides is reached.");
            return false;
        }
    }
    public void removeSlide(int position) {
        mModel.remove(position);
    }
    public void removeAllSlides() {
        while (mModel.size() > 0) {
            removeSlide(0);
        }
    }
    public boolean removeText(int position) {
        return mModel.get(position).removeText();
    }
    public boolean removeImage(int position) {
        return mModel.get(position).removeImage();
    }
    public boolean removeVideo(int position) {
        return mModel.get(position).removeVideo();
    }
    public boolean removeAudio(int position) {
        return mModel.get(position).removeAudio();
    }
    public void changeText(int position, String newText) {
        if (newText != null) {
            SlideModel slide = mModel.get(position);
            TextModel text = slide.getText();
            if (text == null) {
                text = new TextModel(mContext,
                        ContentType.TEXT_PLAIN, "text_" + position + ".txt",
                        mModel.getLayout().getTextRegion());
                text.setText(newText);
                slide.add(text);
            } else if (!newText.equals(text.getText())) {
                text.setText(newText);
            }
        }
    }
    public void changeImage(int position, Uri newImage) throws MmsException {
        mModel.get(position).add(new ImageModel(
                mContext, newImage, mModel.getLayout().getImageRegion()));
    }
    public void changeAudio(int position, Uri newAudio) throws MmsException {
        AudioModel audio = new AudioModel(mContext, newAudio);
        SlideModel slide = mModel.get(position);
        slide.add(audio);
        slide.updateDuration(audio.getDuration());
    }
    public void changeVideo(int position, Uri newVideo) throws MmsException {
        VideoModel video = new VideoModel(mContext, newVideo,
                mModel.getLayout().getImageRegion());
        SlideModel slide = mModel.get(position);
        slide.add(video);
        slide.updateDuration(video.getDuration());
    }
    public void moveSlideUp(int position) {
        mModel.add(position - 1, mModel.remove(position));
    }
    public void moveSlideDown(int position) {
        mModel.add(position + 1, mModel.remove(position));
    }
    public void changeDuration(int position, int dur) {
        if (dur >= 0) {
            mModel.get(position).setDuration(dur);
        }
    }
    public void changeLayout(int layout) {
        mModel.getLayout().changeTo(layout);
    }
    public RegionModel getImageRegion() {
        return mModel.getLayout().getImageRegion();
    }
    public RegionModel getTextRegion() {
        return mModel.getLayout().getTextRegion();
    }
}
