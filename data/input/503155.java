public final class PatternFilenameFilter implements FilenameFilter {
  private final Pattern pattern;
  public PatternFilenameFilter(String patternStr) {
    this(Pattern.compile(patternStr));
  }
  public PatternFilenameFilter(Pattern pattern) {
    this.pattern = Preconditions.checkNotNull(pattern);
  }
   public boolean accept(File dir, String fileName) {
    return pattern.matcher(fileName).matches();
  }
}
