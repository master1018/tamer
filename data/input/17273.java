public class bug6735293 {
    private static volatile JFormattedTextField jtf;
    private static volatile NavigationFilter nf;
    private static volatile JFrame jFrame;
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                jtf = new JFormattedTextField();
                nf = new NavigationFilter();
                jtf.setText("A text message");
                jFrame = new JFrame();
                jFrame.getContentPane().add(jtf);
                jFrame.pack();
                jFrame.setVisible(true);
            }
        });
        Thread.sleep(1000);
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                Position.Bias[] biasRet = {Position.Bias.Forward};
                for (int direction : new int[]{
                        SwingConstants.EAST,
                        SwingConstants.WEST,
                        SwingConstants.SOUTH,
                        SwingConstants.NORTH,
                }) {
                    for (int position : new int[]{-100, Integer.MIN_VALUE}) {
                        for (Position.Bias bias : new Position.Bias[]{Position.Bias.Backward, Position.Bias.Forward}) {
                            try {
                                nf.getNextVisualPositionFrom(jtf, position, bias, direction, biasRet);
                                throw new RuntimeException("BadLocationException was not thrown: position = " +
                                        position + ", bias = " + bias + ", direction = " + direction);
                            } catch (BadLocationException e) {
                            }
                        }
                    }
                }
                jFrame.dispose();
            }
        });
    }
}
