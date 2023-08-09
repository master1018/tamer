public class bug6532833 {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                JSpinner[] spinners = new JSpinner[2];
                for (int i = 0; i < spinners.length; i++) {
                    JSpinner spinner = new JSpinner();
                    spinner.setValue(2010);
                    Component arrowUp = spinner.getComponent(0);
                    Component arrowDown = spinner.getComponent(1);
                    LayoutManager layout = spinner.getLayout();
                    layout.removeLayoutComponent(arrowUp);
                    layout.removeLayoutComponent(arrowDown);
                    if (i == 1) {
                        spinner.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
                    }
                    spinners[i] = spinner;
                }
                JFrame frame = new JFrame();
                for (JSpinner spinner : spinners) {
                    frame.getContentPane().add(spinner);
                }
                frame.pack();
                for (JSpinner spinner : spinners) {
                    Insets insets = spinner.getInsets();
                    if (spinner.getWidth() != insets.left + insets.right + spinner.getEditor().getWidth()) {
                        throw new RuntimeException("Spinner editor width is invalid");
                    }
                }
                frame.dispose();
            }
        });
    }
}
