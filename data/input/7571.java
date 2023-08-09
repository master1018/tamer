public abstract class InternalFrameFocusTraversalPolicy
    extends FocusTraversalPolicy
{
    public Component getInitialComponent(JInternalFrame frame) {
        return getDefaultComponent(frame);
    }
}
