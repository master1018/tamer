public final class Entity {
    final private ContentValues mValues;
    final private ArrayList<NamedContentValues> mSubValues;
    public Entity(ContentValues values) {
        mValues = values;
        mSubValues = new ArrayList<NamedContentValues>();
    }
    public ContentValues getEntityValues() {
        return mValues;
    }
    public ArrayList<NamedContentValues> getSubValues() {
        return mSubValues;
    }
    public void addSubValue(Uri uri, ContentValues values) {
        mSubValues.add(new Entity.NamedContentValues(uri, values));
    }
    public static class NamedContentValues {
        public final Uri uri;
        public final ContentValues values;
        public NamedContentValues(Uri uri, ContentValues values) {
            this.uri = uri;
            this.values = values;
        }
    }
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Entity: ").append(getEntityValues());
        for (Entity.NamedContentValues namedValue : getSubValues()) {
            sb.append("\n  ").append(namedValue.uri);
            sb.append("\n  -> ").append(namedValue.values);
        }
        return sb.toString();
    }
}
