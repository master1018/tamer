public class MmsThumbnailPresenter extends Presenter {
    public MmsThumbnailPresenter(Context context, ViewInterface view, Model model) {
        super(context, view, model);
    }
    @Override
    public void present() {
        SlideModel slide = ((SlideshowModel) mModel).get(0);
        if (slide != null) {
            presentFirstSlide((SlideViewInterface) mView, slide);
        }
    }
    private void presentFirstSlide(SlideViewInterface view, SlideModel slide) {
        view.reset();
        if (slide.hasImage()) {
            presentImageThumbnail(view, slide.getImage());
        } else if (slide.hasVideo()) {
            presentVideoThumbnail(view, slide.getVideo());
        } else if (slide.hasAudio()) {
            presentAudioThumbnail(view, slide.getAudio());
        }
    }
    private void presentVideoThumbnail(SlideViewInterface view, VideoModel video) {
        if (video.isDrmProtected()) {
            showDrmIcon(view, video.getSrc());
        } else {
            view.setVideo(video.getSrc(), video.getUri());
        }
    }
    private void presentImageThumbnail(SlideViewInterface view, ImageModel image) {
        if (image.isDrmProtected()) {
            showDrmIcon(view, image.getSrc());
        } else {
            view.setImage(image.getSrc(), image.getBitmap());
        }
    }
    protected void presentAudioThumbnail(SlideViewInterface view, AudioModel audio) {
        if (audio.isDrmProtected()) {
            showDrmIcon(view, audio.getSrc());
        } else {
            view.setAudio(audio.getUri(), audio.getSrc(), audio.getExtras());
        }
    }
    private void showDrmIcon(SlideViewInterface view, String name) {
        Bitmap bitmap = BitmapFactory.decodeResource(
                mContext.getResources(), R.drawable.ic_mms_drm_protected);
        view.setImage(name, bitmap);
    }
    public void onModelChanged(Model model, boolean dataChanged) {
    }
}
