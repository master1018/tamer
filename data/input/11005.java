public class DefaultFocusManager extends FocusManager {
    final FocusTraversalPolicy gluePolicy =
        new LegacyGlueFocusTraversalPolicy(this);
    private final FocusTraversalPolicy layoutPolicy =
        new LegacyLayoutFocusTraversalPolicy(this);
    private final LayoutComparator comparator =
        new LayoutComparator();
    public DefaultFocusManager() {
        setDefaultFocusTraversalPolicy(gluePolicy);
    }
    public Component getComponentAfter(Container aContainer,
                                       Component aComponent)
    {
        Container root = (aContainer.isFocusCycleRoot())
            ? aContainer
            : aContainer.getFocusCycleRootAncestor();
        if (root != null) {
            FocusTraversalPolicy policy = root.getFocusTraversalPolicy();
            if (policy != gluePolicy) {
                return policy.getComponentAfter(root, aComponent);
            }
            comparator.setComponentOrientation(root.getComponentOrientation());
            return layoutPolicy.getComponentAfter(root, aComponent);
        }
        return null;
    }
    public Component getComponentBefore(Container aContainer,
                                        Component aComponent)
    {
        Container root = (aContainer.isFocusCycleRoot())
            ? aContainer
            : aContainer.getFocusCycleRootAncestor();
        if (root != null) {
            FocusTraversalPolicy policy = root.getFocusTraversalPolicy();
            if (policy != gluePolicy) {
                return policy.getComponentBefore(root, aComponent);
            }
            comparator.setComponentOrientation(root.getComponentOrientation());
            return layoutPolicy.getComponentBefore(root, aComponent);
        }
        return null;
    }
    public Component getFirstComponent(Container aContainer) {
        Container root = (aContainer.isFocusCycleRoot())
            ? aContainer
            : aContainer.getFocusCycleRootAncestor();
        if (root != null) {
            FocusTraversalPolicy policy = root.getFocusTraversalPolicy();
            if (policy != gluePolicy) {
                return policy.getFirstComponent(root);
            }
            comparator.setComponentOrientation(root.getComponentOrientation());
            return layoutPolicy.getFirstComponent(root);
        }
        return null;
    }
    public Component getLastComponent(Container aContainer) {
        Container root = (aContainer.isFocusCycleRoot())
            ? aContainer
            : aContainer.getFocusCycleRootAncestor();
        if (root != null) {
            FocusTraversalPolicy policy = root.getFocusTraversalPolicy();
            if (policy != gluePolicy) {
                return policy.getLastComponent(root);
            }
            comparator.setComponentOrientation(root.getComponentOrientation());
            return layoutPolicy.getLastComponent(root);
        }
        return null;
    }
    public boolean compareTabOrder(Component a, Component b) {
        return (comparator.compare(a, b) < 0);
    }
}
final class LegacyLayoutFocusTraversalPolicy
    extends LayoutFocusTraversalPolicy
{
    LegacyLayoutFocusTraversalPolicy(DefaultFocusManager defaultFocusManager) {
        super(new CompareTabOrderComparator(defaultFocusManager));
    }
}
final class CompareTabOrderComparator implements Comparator<Component> {
    private final DefaultFocusManager defaultFocusManager;
    CompareTabOrderComparator(DefaultFocusManager defaultFocusManager) {
        this.defaultFocusManager = defaultFocusManager;
    }
    public int compare(Component o1, Component o2) {
        if (o1 == o2) {
            return 0;
        }
        return (defaultFocusManager.compareTabOrder(o1, o2)) ? -1 : 1;
    }
}
