public class VRforCAD {
    public static final int MODE_CAD = 0;
    public static final int MODE_VISUALIZATION = 1;
    public static final int MODE_ROBOTICS = 2;
    public static final int MODE_MEDICINE = 3;
    public static final int MODE_FREEFORM = 4;
    public static final int GEOMETRY_TRIANGLES = 0;
    public static final int GEOMETRY_QUADS = 1;
    public static final int GEOMETRY_TRIANGLES_BY_REF = 2;
    public static final int GEOMETRY_QUADS_BY_REF = 3;
    private StartingLogo sl = null;
    private ModelInterface mi;
    private ControllerInterface ci;
    public VRforCAD() {
        showStartingLogo();
        initialize();
    }
    private void initialize() {
        mi = new VRforCADModel();
        ci = new VRforCADController(this, mi);
    }
    private void showStartingLogo() {
        sl = new StartingLogo(null);
        sl.setLocationRelativeTo(null);
        sl.setVisible(true);
    }
    public void disposeStartingLogo() {
        sl.dispose();
    }
}
