@org.openide.util.lookup.ServiceProvider(service = NewControlAction.class)
public class NewRigidBodyAction extends AbstractNewControlAction {
    public NewRigidBodyAction() {
        name = "Static RigidBody";
    }
    @Override
    protected Control doCreateControl(Spatial spatial) {
        RigidBodyControl control = spatial.getControl(RigidBodyControl.class);
        if (control != null) {
            spatial.removeControl(control);
        }
        Node parent = spatial.getParent();
        spatial.removeFromParent();
        control = new RigidBodyControl(0);
        if (parent != null) {
            parent.attachChild(spatial);
        }
        return control;
    }
}
