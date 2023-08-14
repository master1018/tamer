public class Whitelist {
    private List<Bundle> mConditions;
    public Whitelist() {
        mConditions = new ArrayList<Bundle>();
    }
    public Whitelist(List<Bundle> conditions) {
        this.mConditions = conditions;
    }
    public void addApp(String app) {
        Bundle bundle = new Bundle();
        bundle.putString("packageName", app);
        mConditions.add(bundle);
    }
    public boolean matches(FieldContext context) {
        for (Bundle condition : mConditions) {
            if (matches(condition, context.getBundle())) {
                return true;
            }
        }
        return false;
    }
    private boolean matches(Bundle condition, Bundle target) {
        for (String key : condition.keySet()) {
          if (!condition.getString(key).equals(target.getString(key))) {
            return false;
          }
        }
        return true;
    }
}
