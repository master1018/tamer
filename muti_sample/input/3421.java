public class bug6857057 {
    bug6857057() {
        Element elem = new StubBranchElement(" G L Y P H V");
        GlyphView view = new GlyphView(elem);
        float pos = elem.getStartOffset();
        float len = elem.getEndOffset() - pos;
        int res = view.getBreakWeight(View.X_AXIS, pos, len);
        if (res != View.ExcellentBreakWeight) {
            throw new RuntimeException("breakWeight != ExcellentBreakWeight");
        }
    }
    public static void main(String[] args) throws Throwable {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new bug6857057();
            }
        });
        System.out.println("OK");
    }
}
