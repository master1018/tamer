public class NavigationFilter {
    public void setDot(FilterBypass fb, int dot, Position.Bias bias) {
        fb.setDot(dot, bias);
    }
    public void moveDot(FilterBypass fb, int dot, Position.Bias bias) {
        fb.moveDot(dot, bias);
    }
    public int getNextVisualPositionFrom(JTextComponent text, int pos,
                                         Position.Bias bias, int direction,
                                         Position.Bias[] biasRet)
                                           throws BadLocationException {
        return text.getUI().getNextVisualPositionFrom(text, pos, bias,
                                                      direction, biasRet);
    }
    public static abstract class FilterBypass {
        public abstract Caret getCaret();
        public abstract void setDot(int dot, Position.Bias bias);
        public abstract void moveDot(int dot, Position.Bias bias);
    }
}
