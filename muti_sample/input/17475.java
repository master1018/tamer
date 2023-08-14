public class Test4652928 extends AbstractTest<List> {
    public static void main(String[] args) {
        new Test4652928().test(true);
    }
    protected List getObject() {
        List<BeanContext> list = new ArrayList<BeanContext>();
        list.add(fill(new BeanContextSupport()));
        list.add(fill(new BeanContextServicesSupport()));
        return list;
    }
    private static BeanContext fill(BeanContext context) {
        context.add(new JLabel("label"));
        context.add(new JButton("button"));
        JButton button = new JButton();
        button.setText("another button");
        context.add(button);
        return context;
    }
}
