public class SearchableItemPreference extends CheckBoxPreference {
    private Drawable mIcon;
    SearchableItemPreference(Context context) {
        super(context);
        setLayoutResource(R.layout.searchable_item_preference);
    }
    public void setIcon(Drawable icon) {
        mIcon = icon;
    }
     @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        icon.setImageDrawable(mIcon);
    }
}
