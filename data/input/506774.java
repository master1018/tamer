class ActivityResult extends ResultInfo {
    final HistoryRecord mFrom;
    public ActivityResult(HistoryRecord from, String resultWho,
            int requestCode, int resultCode, Intent data) {
        super(resultWho, requestCode, resultCode, data);
        mFrom = from;
    }
}
