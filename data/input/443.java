public class TubeClusterView extends ClusterView {
    public TubeClusterView() {
        setTypeName("Tube Cluster");
    }
    protected void setupDefaultMaterial() {
        setupDefaultAppearance(Colours.pinkMaterial);
    }
    protected void setupHighlightMaterial() {
        setupHighlightAppearance(Colours.yellowMaterial);
    }
    public void draw() {
        org.viewer.graph.Cluster c = (org.viewer.graph.Cluster) getNode();
        double radius = (double) getRadius();
        setResizeTranslateTransform(new Vector3d(radius, radius, 1d), new Vector3f(c.getPosition()));
    }
    public void init() {
        setExpandedView();
        Cylinder inside = new Cylinder(0.99f, 2.4f, Cylinder.GENERATE_NORMALS_INWARD, getAppearance());
        Cylinder outside = new Cylinder(1f, 2.4f, getAppearance());
        Shape3D is = new Shape3D(inside.getShape(Cylinder.BODY).getGeometry());
        Shape3D os = new Shape3D(outside.getShape(Cylinder.BODY).getGeometry());
        is.setAppearance(getAppearance());
        os.setAppearance(getAppearance());
        Transform3D t = new Transform3D();
        t.rotX(Math.PI / 2);
        t.setTranslation(new Vector3d(0.0, 0.0, 1.2));
        TransformGroup g = new TransformGroup(t);
        g.addChild(is);
        g.addChild(os);
        getTransformGroup().addChild(g);
        makePickable(is);
        makePickable(os);
    }
    public ImageIcon getIcon() {
        return new ImageIcon(org.viewer.images.Images.class.getResource("planarcluster.png"));
    }
}
