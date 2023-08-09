public class Test6632810 {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                BasicScrollPaneUI ui = new BasicScrollPaneUI();
                ui.installUI(new JScrollPane());
                try {
                    ui.getBaseline(null, 1, 1);
                    throw new RuntimeException("getBaseline(null, 1, 1) does not throw NPE");
                } catch (NullPointerException e) {
                }
                int[][] illegelParams = new int[][]{
                        {-1, 1,},
                        {1, -1,},
                        {-1, -1,},
                };
                for (int[] illegelParam : illegelParams) {
                    try {
                        int width = illegelParam[0];
                        int height = illegelParam[1];
                        ui.getBaseline(new JScrollPane(), width, height);
                        throw new RuntimeException("getBaseline(new JScrollPane(), " + width + ", " + height +
                                ") does not throw IAE");
                    } catch (IllegalArgumentException e) {
                    }
                }
            }
        });
    }
}
