public class FdnList extends ADNList {
    private static final int MENU_ADD = 1;
    private static final int MENU_EDIT = 2;
    private static final int MENU_DELETE = 3;
    private static final String INTENT_EXTRA_NAME = "name";
    private static final String INTENT_EXTRA_NUMBER = "number";
    @Override
    protected Uri resolveIntent() {
        Intent intent = getIntent();
        intent.setData(Uri.parse("content:
        return intent.getData();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        Resources r = getResources();
        menu.add(0, MENU_ADD, 0, r.getString(R.string.menu_add))
                .setIcon(android.R.drawable.ic_menu_add);
        menu.add(0, MENU_EDIT, 0, r.getString(R.string.menu_edit))
                .setIcon(android.R.drawable.ic_menu_edit);
        menu.add(0, MENU_DELETE, 0, r.getString(R.string.menu_delete))
                .setIcon(android.R.drawable.ic_menu_delete);
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        boolean hasSelection = (getSelectedItemPosition() >= 0);
        menu.findItem(MENU_ADD).setVisible(true);
        menu.findItem(MENU_EDIT).setVisible(hasSelection);
        menu.findItem(MENU_DELETE).setVisible(hasSelection);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ADD:
                addContact();
                return true;
            case MENU_EDIT:
                editSelected();
                return true;
            case MENU_DELETE:
                deleteSelected();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        editSelected(position);
    }
    private void addContact() {
        Intent intent = new Intent();
        intent.setClass(this, EditFdnContactScreen.class);
        startActivity(intent);
    }
    private void editSelected() {
        editSelected(getSelectedItemPosition());
    }
    private void editSelected(int position) {
        if (mCursor.moveToPosition(position)) {
            String name = mCursor.getString(NAME_COLUMN);
            String number = mCursor.getString(NUMBER_COLUMN);
            Intent intent = new Intent();
            intent.setClass(this, EditFdnContactScreen.class);
            intent.putExtra(INTENT_EXTRA_NAME, name);
            intent.putExtra(INTENT_EXTRA_NUMBER, number);
            startActivity(intent);
        }
    }
    private void deleteSelected() {
        if (mCursor.moveToPosition(getSelectedItemPosition())) {
            String name = mCursor.getString(NAME_COLUMN);
            String number = mCursor.getString(NUMBER_COLUMN);
            Intent intent = new Intent();
            intent.setClass(this, DeleteFdnContactScreen.class);
            intent.putExtra(INTENT_EXTRA_NAME, name);
            intent.putExtra(INTENT_EXTRA_NUMBER, number);
            startActivity(intent);
        }
    }
}
