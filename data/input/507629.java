class ViewHelper {
    private View mView;
    void setView(View view) {
        mView = view;
    }
    void showError(int msgId) {
        TextView v = (TextView) mView.findViewById(R.id.error);
        v.setText(msgId);
        if (v != null) v.setVisibility(View.VISIBLE);
    }
    String getText(int viewId) {
        return ((TextView) mView.findViewById(viewId)).getText().toString();
    }
    void setText(int viewId, String text) {
        if (text == null) return;
        TextView v = (TextView) mView.findViewById(viewId);
        if (v != null) v.setText(text);
    }
    void setText(int viewId, int textId) {
        TextView v = (TextView) mView.findViewById(viewId);
        if (v != null) v.setText(textId);
    }
}
