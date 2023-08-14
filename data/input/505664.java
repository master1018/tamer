public class MenuDialogHelper implements DialogInterface.OnKeyListener, DialogInterface.OnClickListener {
    private MenuBuilder mMenu;
    private ListAdapter mAdapter;
    private AlertDialog mDialog;
    public MenuDialogHelper(MenuBuilder menu) {
        mMenu = menu;
    }
    public void show(IBinder windowToken) {
        final MenuBuilder menu = mMenu;
        mAdapter = menu.getMenuAdapter(MenuBuilder.TYPE_DIALOG);
        final AlertDialog.Builder builder = new AlertDialog.Builder(menu.getContext())
                .setAdapter(mAdapter, this); 
        final View headerView = menu.getHeaderView();
        if (headerView != null) {
            builder.setCustomTitle(headerView);
        } else {
            builder.setIcon(menu.getHeaderIcon()).setTitle(menu.getHeaderTitle());
        }
        builder.setOnKeyListener(this);
        builder.setRecycleOnMeasureEnabled(false);
        mDialog = builder.create();
        WindowManager.LayoutParams lp = mDialog.getWindow().getAttributes();
        lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
        if (windowToken != null) {
            lp.token = windowToken;
        }
        lp.flags |= WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
        mDialog.show();
    }
    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_BACK) {
            if (event.getAction() == KeyEvent.ACTION_DOWN
                    && event.getRepeatCount() == 0) {
                Window win = mDialog.getWindow();
                if (win != null) {
                    View decor = win.getDecorView();
                    if (decor != null) {
                        KeyEvent.DispatcherState ds = decor.getKeyDispatcherState();
                        if (ds != null) {
                            ds.startTracking(event, this);
                            return true;
                        }
                    }
                }
            } else if (event.getAction() == KeyEvent.ACTION_UP && !event.isCanceled()) {
                Window win = mDialog.getWindow();
                if (win != null) {
                    View decor = win.getDecorView();
                    if (decor != null) {
                        KeyEvent.DispatcherState ds = decor.getKeyDispatcherState();
                        if (ds != null && ds.isTracking(event)) {
                            mMenu.close(true);
                            dialog.dismiss();
                            return true;
                        }
                    }
                }
            }
        }
        return mMenu.performShortcut(keyCode, event, 0);
    }
    public void dismiss() {
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }
    public void onClick(DialogInterface dialog, int which) {
        mMenu.performItemAction((MenuItemImpl) mAdapter.getItem(which), 0);
    }
}
