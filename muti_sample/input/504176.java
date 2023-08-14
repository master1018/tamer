public class CreationParams {
    public final long MAXIMIZED_VERT = 1;
    public final long MAXIMIZED_HORIZ = 2;
    public final long MAXIMIZED = 3;
    public final static int DECOR_TYPE_FRAME = 1;
    public final static int DECOR_TYPE_DIALOG = 2;
    public final static int DECOR_TYPE_POPUP = 3;
    public final static int DECOR_TYPE_UNDECOR = 4;
    public final static int DECOR_TYPE_NONE = 0;
    public int x = 0;
    public int y = 0;
    public int w = 1;
    public int h = 1;
    public int decorType = DECOR_TYPE_NONE;
    public boolean child = false;
    public boolean resizable = true;
    public boolean undecorated = false;
    public boolean visible = false;
    public boolean topmost = false;
    public boolean disabled = false;
    public boolean iconified = false;
    public int maximizedState = 0;
    public boolean locationByPlatform = false;
    public long parentId = 0;
    public String name = null;
}