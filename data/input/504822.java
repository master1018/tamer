public abstract class Presenter implements IModelChangedObserver {
    protected final Context mContext;
    protected ViewInterface mView;
    protected Model mModel;
    public Presenter(Context context, ViewInterface view, Model model) {
        mContext = context;
        mView = view;
        mModel = model;
        mModel.registerModelChangedObserver(this);
    }
    public ViewInterface getView() {
        return mView;
    }
    public void setView(ViewInterface view) {
        mView = view;
    }
    public Model getModel() {
        return mModel;
    }
    public abstract void present();
}
