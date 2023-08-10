public class BSPScene extends Scene<TransformGroup> {
    private BSPClusterManager clusterManager;
    protected void setClusterManager(BSPClusterManager clusterManager) {
        this.clusterManager = clusterManager;
    }
    public BSPClusterManager getClusterManager() {
        return (clusterManager);
    }
    @Override
    protected void addShapeNode(Shape3D face) {
        super.addShapeNode(face);
    }
    @SuppressWarnings("unchecked")
    public List<ColliderNode> getColliders() {
        List<ColliderNode> colliders = new ArrayList<ColliderNode>();
        for (Shape3D face : getShapeNodes()) {
            ColliderGeometry cg = new ColliderGeometry();
            cg.setModel(face);
            BiTreeCollider collider = new BiTreeCollider();
            collider.build(cg);
            ColliderNode cn = new ColliderNode(face, ColliderNode.CT_GEOMETRY, ColliderNode.CT_GEOMETRY, false, collider);
            cn.setTwoSided(true);
            colliders.add(cn);
        }
        return (colliders);
    }
    protected BSPScene() {
        super();
    }
}
