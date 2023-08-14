public class PowerGaugePreference extends Preference {
    private Drawable mIcon;
    private PercentageBar mGauge;
    private double mValue;
    private BatterySipper mInfo;
    private double mPercent;
    public PowerGaugePreference(Context context, Drawable icon, BatterySipper info) {
        super(context);
        setLayoutResource(R.layout.preference_powergauge);
        mIcon = icon;
        mGauge = new PercentageBar();
        mGauge.bar = context.getResources().getDrawable(R.drawable.app_gauge);
        mInfo = info;
    }
    void setGaugeValue(double percent) {
        mValue = percent;
        mGauge.percent = mValue;
    }
    void setPercent(double percent) {
        mPercent = percent;
    }
    BatterySipper getInfo() {
        return mInfo;
    }
    void setIcon(Drawable icon) {
        mIcon = icon;
        notifyChanged();
    }
    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        ImageView appIcon = (ImageView) view.findViewById(R.id.appIcon);
        if (mIcon == null) {
            mIcon = getContext().getResources().getDrawable(android.R.drawable.sym_def_app_icon);
        }
        appIcon.setImageDrawable(mIcon);
        ImageView appGauge = (ImageView) view.findViewById(R.id.appGauge);
        appGauge.setImageDrawable(mGauge);
        TextView percentView = (TextView) view.findViewById(R.id.percent);
        percentView.setText((int) (Math.ceil(mPercent)) + "%");
    }
}
