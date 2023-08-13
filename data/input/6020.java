public abstract class TextSource {
  public abstract char[] getChars();
  public abstract int getStart();
  public abstract int getLength();
  public abstract int getContextStart();
  public abstract int getContextLength();
  public abstract int getLayoutFlags();
  public abstract int getBidiLevel();
  public abstract Font getFont();
  public abstract FontRenderContext getFRC();
  public abstract CoreMetrics getCoreMetrics();
  public abstract TextSource getSubSource(int start, int length, int dir);
  public static final boolean WITHOUT_CONTEXT = false;
  public static final boolean WITH_CONTEXT = true;
  public abstract String toString(boolean withContext);
}
