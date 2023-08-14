public class TrackerActivity extends ListActivity {
    static final String LOG_TAG = "LocationTracker";
    private TrackerListHelper mDataHelper;
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mDataHelper = new TrackerListHelper(this);
        mDataHelper.bindListUI(R.layout.entrylist_item);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.start_service_menu: {
                startService(new Intent(TrackerActivity.this,
                        TrackerService.class));
                break;
            }
            case R.id.stop_service_menu: {
                stopService(new Intent(TrackerActivity.this,
                        TrackerService.class));
                break;
            }
            case R.id.settings_menu: {
                launchSettings();
                break;
            }
            case R.id.export_kml: {
                exportKML();
                break;
            }
            case R.id.export_csv: {
                exportCSV();
                break;
            }
            case R.id.clear_data_menu: {
                clearData();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }
    private void clearData() {
        Runnable clearAction = new Runnable() {
            public void run() {
                TrackerDataHelper helper =
                    new TrackerDataHelper(TrackerActivity.this);
                helper.deleteAll();
            }
        };
        showConfirm(R.string.delete_confirm, clearAction);
    }
    private void showConfirm(int textId, final Runnable onConfirmAction) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.confirm_title);
        dialogBuilder.setMessage(textId);
        dialogBuilder.setPositiveButton(android.R.string.ok,
                new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                onConfirmAction.run();
            }
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel,
                new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        dialogBuilder.show();
    }
    private void exportCSV() {
        String exportFileName = getUniqueFileName("csv");
        exportFile(null, exportFileName, new TrackerDataHelper(this,
                TrackerDataHelper.CSV_FORMATTER));
    }
    private void exportKML() {
        String exportFileName = getUniqueFileName(
                LocationManager.NETWORK_PROVIDER + ".kml");
        exportFile(LocationManager.NETWORK_PROVIDER, exportFileName,
                new TrackerDataHelper(this, TrackerDataHelper.KML_FORMATTER));
        exportFileName = getUniqueFileName(
                LocationManager.GPS_PROVIDER + ".kml");
        exportFile(LocationManager.GPS_PROVIDER, exportFileName,
                new TrackerDataHelper(this, TrackerDataHelper.KML_FORMATTER));
    }
    private void exportFile(String tagFilter,
                            String exportFileName,
                            TrackerDataHelper trackerData) {
        BufferedWriter exportWriter = null;
        Cursor cursor = trackerData.query(tagFilter);
        try {
            exportWriter = new BufferedWriter(new FileWriter(exportFileName));
            exportWriter.write(trackerData.getOutputHeader());
            String line = null;
            while ((line = trackerData.getNextOutput(cursor)) != null) {
                exportWriter.write(line);
            }
            exportWriter.write(trackerData.getOutputFooter());
            Toast.makeText(this, "Successfully exported data to " +
                    exportFileName, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Error exporting file: " +
                    e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            Log.e(LOG_TAG, "Error exporting file", e);
        } finally {
            closeWriter(exportWriter);
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    private void closeWriter(Writer exportWriter) {
        if (exportWriter != null) {
            try {
                exportWriter.close();
            } catch (IOException e) {
                Log.e(LOG_TAG, "error closing file", e);
            }
        }
    }
    private String getUniqueFileName(String ext) {
        File dir = new File("/sdcard/locationtracker");
        if (!dir.exists()) {
            dir.mkdir();
        }
        return "/sdcard/locationtracker/tracking-" +
            DateUtils.getCurrentTimestamp() + "." + ext;
    }
    private void launchSettings() {
        Intent settingsIntent = new Intent();
        settingsIntent.setClass(this, SettingsActivity.class);
        startActivity(settingsIntent);
    }
}
