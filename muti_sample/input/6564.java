public class CPPExpressions {
  private static Pattern castPattern;
  public static class CastExpr {
    private String type;
    private String address;
    private CastExpr(String type, String address) {
      this.type = type;
      this.address = address;
    }
    public String getType() {
      return type;
    }
    public String getAddress() {
      return address;
    }
  }
  public static class StaticFieldExpr {
    private String containingType;
    private String fieldName;
    private StaticFieldExpr(String containingType, String fieldName) {
      this.containingType = containingType;
      this.fieldName = fieldName;
    }
    public String getContainingType() {
      return containingType;
    }
    public String getFieldName() {
      return fieldName;
    }
  }
  public static CastExpr parseCast(String expr) {
    if (castPattern == null) {
      castPattern = Pattern.compile("\\s*\\(\\s*([0-9A-Za-z:_]*)\\s*\\*\\s*\\)\\s*([0-9a-zA-Z]*)\\s*");
    }
    Matcher matcher = castPattern.matcher(expr);
    if (matcher.matches()) {
      String type = matcher.group(1);
      String addr = matcher.group(2);
      return new CastExpr(type, addr);
    }
    return null;
  }
  public static StaticFieldExpr parseStaticField(String expr) {
    String sep = "::";
    int idx = expr.lastIndexOf(sep);
    if (idx < 0) {
      return null;
    }
    String containingType = expr.substring(0, idx);
    String fieldName = expr.substring(idx + sep.length(), expr.length());
    return new StaticFieldExpr(containingType, fieldName);
  }
}
