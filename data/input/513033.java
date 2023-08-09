public class MyRssReader extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);
        mRssList = (ListView) findViewById(R.id.rssListView);
        mRssList.setAdapter(
                new ArrayAdapter<String>(
                        this, 
                        R.layout.list_element, 
                        new String[] { "Scientific American", "BBC", "The Onion", "Engadget" }));
}
    @Override
    protected void onSaveInstanceState(Bundle outState){
        int index = mRssList.getSelectedItemIndex();
        if(index > -1){
            outState.putInteger("lastIndexItem", index);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        menu.add(0, 0, "Start RSS Service", null);
        menu.add(0, 1, "Stop RSS Service", null);
        menu.add(0, 2, "Add New Feed", null);
        menu.add(0, 3, "Delete Feed", null);
        menu.add(0, 4, "Update All Feeds", null);
        return true;
    }  
    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(Menu.Item item){
        switch (item.getId()) {
            case 0:
                showAlert(null, "You clicked 'start'!", "ok", null, false, null);
                break;
            case 1:
                showAlert(null, "You clicked stop!", "ok", null, false, null);
                break;
            case 2:
                showAlert(null, "You clicked 'Add'!", "ok", null, false, null);
                break;
            case 3:
                showAlert(null, "You clicked 'Delete'!", "ok", null, false, null);
                break;
            case 4:
                showAlert(null, "You clicked 'Update'!", "ok", null, false, null);
                break;
            default:
                showAlert(null, "I have no idea what you clicked!", "ok", null, false, null);
                break;
        }
        return true;
    }
    ListView mRssList;
}
