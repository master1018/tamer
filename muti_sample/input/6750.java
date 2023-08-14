public class BytecodeNode extends AbstractNode {
    private Set<InputNode> nodes;
    public BytecodeNode(InputBytecode bytecode, InputGraph graph, String bciValue) {
        super(Children.LEAF);
        this.setDisplayName(bytecode.getBci() + " " + bytecode.getName());
        bciValue = bytecode.getBci() + " " + bciValue;
        bciValue = bciValue.trim();
        Properties.PropertySelector<InputNode> selector = new Properties.PropertySelector<InputNode>(graph.getNodes());
        StringPropertyMatcher matcher = new StringPropertyMatcher("bci", bciValue);
        List<InputNode> nodeList = selector.selectMultiple(matcher);
        if (nodeList.size() > 0) {
            nodes = new HashSet<InputNode>();
            for (InputNode n : nodeList) {
                nodes.add(n);
            }
            this.setDisplayName(this.getDisplayName() + " (" + nodes.size() + " nodes)");
        }
    }
    @Override
    public Image getIcon(int i) {
        if (nodes != null) {
            return Utilities.loadImage("com/sun/hotspot/igv/bytecodes/images/link.gif");
        } else {
            return Utilities.loadImage("com/sun/hotspot/igv/bytecodes/images/bytecode.gif");
        }
    }
    @Override
    public Image getOpenedIcon(int i) {
        return getIcon(i);
    }
    @Override
    public Action[] getActions(boolean b) {
        return new Action[]{(Action) SelectBytecodesAction.findObject(SelectBytecodesAction.class, true)};
    }
    @Override
    public Action getPreferredAction() {
        return (Action) SelectBytecodesAction.findObject(SelectBytecodesAction.class, true);
    }
    @Override
    public <T extends Node.Cookie> T getCookie(Class<T> aClass) {
        if (aClass == SelectBytecodesCookie.class && nodes != null) {
            return (T) (new SelectBytecodesCookie(nodes));
        }
        return super.getCookie(aClass);
    }
}
