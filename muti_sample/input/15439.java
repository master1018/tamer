public class LayoutFocusTraversalPolicy extends SortingFocusTraversalPolicy
    implements Serializable
{
    private static final SwingDefaultFocusTraversalPolicy fitnessTestPolicy =
        new SwingDefaultFocusTraversalPolicy();
    public LayoutFocusTraversalPolicy() {
        super(new LayoutComparator());
    }
    LayoutFocusTraversalPolicy(Comparator<? super Component> c) {
        super(c);
    }
    public Component getComponentAfter(Container aContainer,
                                       Component aComponent) {
        if (aContainer == null || aComponent == null) {
            throw new IllegalArgumentException("aContainer and aComponent cannot be null");
        }
        Comparator comparator = getComparator();
        if (comparator instanceof LayoutComparator) {
            ((LayoutComparator)comparator).
                setComponentOrientation(aContainer.
                                        getComponentOrientation());
        }
        return super.getComponentAfter(aContainer, aComponent);
    }
    public Component getComponentBefore(Container aContainer,
                                        Component aComponent) {
        if (aContainer == null || aComponent == null) {
            throw new IllegalArgumentException("aContainer and aComponent cannot be null");
        }
        Comparator comparator = getComparator();
        if (comparator instanceof LayoutComparator) {
            ((LayoutComparator)comparator).
                setComponentOrientation(aContainer.
                                        getComponentOrientation());
        }
        return super.getComponentBefore(aContainer, aComponent);
    }
    public Component getFirstComponent(Container aContainer) {
        if (aContainer == null) {
            throw new IllegalArgumentException("aContainer cannot be null");
        }
        Comparator comparator = getComparator();
        if (comparator instanceof LayoutComparator) {
            ((LayoutComparator)comparator).
                setComponentOrientation(aContainer.
                                        getComponentOrientation());
        }
        return super.getFirstComponent(aContainer);
    }
    public Component getLastComponent(Container aContainer) {
        if (aContainer == null) {
            throw new IllegalArgumentException("aContainer cannot be null");
        }
        Comparator comparator = getComparator();
        if (comparator instanceof LayoutComparator) {
            ((LayoutComparator)comparator).
                setComponentOrientation(aContainer.
                                        getComponentOrientation());
        }
        return super.getLastComponent(aContainer);
    }
     protected boolean accept(Component aComponent) {
        if (!super.accept(aComponent)) {
            return false;
        } else if (SunToolkit.isInstanceOf(aComponent, "javax.swing.JTable")) {
            return true;
        } else if (SunToolkit.isInstanceOf(aComponent, "javax.swing.JComboBox")) {
            JComboBox box = (JComboBox)aComponent;
            return box.getUI().isFocusTraversable(box);
        } else if (aComponent instanceof JComponent) {
            JComponent jComponent = (JComponent)aComponent;
            InputMap inputMap = jComponent.getInputMap(JComponent.WHEN_FOCUSED,
                                                       false);
            while (inputMap != null && inputMap.size() == 0) {
                inputMap = inputMap.getParent();
            }
            if (inputMap != null) {
                return true;
            }
        }
        return fitnessTestPolicy.accept(aComponent);
    }
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(getComparator());
        out.writeBoolean(getImplicitDownCycleTraversal());
    }
    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        setComparator((Comparator)in.readObject());
        setImplicitDownCycleTraversal(in.readBoolean());
    }
}
class SwingDefaultFocusTraversalPolicy
    extends java.awt.DefaultFocusTraversalPolicy
{
    public boolean accept(Component aComponent) {
        return super.accept(aComponent);
    }
}
