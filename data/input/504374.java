public abstract class FileList extends ListActivity
{
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode)
		{
			case KeyEvent.KEYCODE_DPAD_LEFT:
				if (mPath.length() > mBaseLength) {
					File f = new File(mPath);
					mFocusFile = f.getName();
					mFocusIndex = 0;
					f = f.getParentFile();
					mPath = f.getPath();
					updateList();
					return true;
				}
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				{
					Map map = (Map) getListView().getItemAtPosition(getListView().getSelectedItemPosition());
					String path = (String)map.get("path");
					if ((new File(path)).isDirectory()) {
						mPath = path;
				        mFocusFile = null;
						updateList();
					} else {
						processFile(path, false);
					}
                    return true;
				}
			default:
				break;
		}
		return super.onKeyDown(keyCode, event);
	}
	public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setupPath();
        updateList();
    }
    protected List getData()
    {
        List myData = new ArrayList<HashMap>();
        File f = new File(mPath);
        if (!f.exists()) {
        	addItem(myData, "!LayoutTests path missing!", "");
        	return myData;
        }
        String[] files = f.list();
        Arrays.sort(files);
        for (int i = 0; i < files.length; i++) {
        	StringBuilder sb = new StringBuilder(mPath);
        	sb.append(File.separatorChar);
        	sb.append(files[i]);
        	String path = sb.toString();
        	File c = new File(path);
        	if (fileFilter(c)) {
	        	if (c.isDirectory()) {
	        		addItem(myData, "<"+files[i]+">", path);
	        		if (mFocusFile != null && mFocusFile.equals(files[i]))
	        			mFocusIndex = myData.size()-1;
	        	}
	        	else
	        	    addItem(myData, files[i], path);
        	}
        }
        return myData;
    }
    protected void addItem(List<Map> data, String name, String path)
    {
        HashMap temp = new HashMap();
        temp.put("title", name);
        temp.put("path", path);
        data.add(temp);
    }
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
        Map map = (Map) l.getItemAtPosition(position);
        final String path = (String)map.get("path");
        if ((new File(path)).isDirectory()) {
            final CharSequence[] items = {"Open", "Run"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select an Action");
            builder.setSingleChoiceItems(items, -1,
                    new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case OPEN_DIRECTORY:
                            dialog.dismiss();
                            mPath = path;
                            mFocusFile = null;
                            updateList();
                            break;
                        case RUN_TESTS:
                            dialog.dismiss();
                            processDirectory(path, false);
                            break;
                    }
                }
            });
            builder.create().show();
        } else {
            processFile(path, false);
        }
    }
    abstract void processDirectory(String path, boolean selection);
    abstract void processFile(String filename, boolean selection);
    abstract boolean fileFilter(File f);
    protected void updateList() {
        setListAdapter(new SimpleAdapter(this,
                getData(),
                android.R.layout.simple_list_item_1,
                new String[] {"title"},
                new int[] {android.R.id.text1}));
        String title = mPath; 
        setTitle(title);
        getListView().setSelection(mFocusIndex);
    }
    protected void setupPath()
    {
    	mPath = "/sdcard/android/layout_tests";
    	mBaseLength = mPath.length();
    }
    protected String mPath;
    protected int mBaseLength;
    protected String mFocusFile;
    protected int mFocusIndex;
    private final static int OPEN_DIRECTORY = 0;
    private final static int RUN_TESTS = 1;
}
