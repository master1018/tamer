public class DemoDataSet
{
    private final static String LOG_TAG = "DemoDataSet";
    private ContentResolver mContentResolver;
    public final void add(Context context)
    {
        mContentResolver = context.getContentResolver();
        mContentResolver.delete(Contacts.People.CONTENT_URI, null, null);
        addDefaultData();
        addDefaultImages();
    }
    private final void addDefaultImages()
    {
        File rootDirectory = Environment.getRootDirectory();
        String [] files
            = new File(rootDirectory, "images").list();
        int count = files.length;
        if (count == 0) {
            Slog.i(LOG_TAG, "addDefaultImages: no images found!");
            return;
        }
        for (int i = 0; i < count; i++)
        {
            String name = files[i];
            String path = rootDirectory + "/" + name;
            try {
                Images.Media.insertImage(mContentResolver, path, name, null);
            } catch (FileNotFoundException e) {
                Slog.e(LOG_TAG, "Failed to import image " + path, e);
            }
        }
    }
    private final void addDefaultData()
    {
        Slog.i(LOG_TAG, "Adding default data...");
        Intent intent = new Intent(
                Intent.ACTION_CALL, Uri.fromParts("voicemail", "", null));
        addShortcut("1", intent);
    }
    private final Uri addImage(String name, Uri file)
    {
        ContentValues imagev = new ContentValues();
        imagev.put("name", name);
        Uri url = null;
        AssetManager ass = AssetManager.getSystem();
        InputStream in = null;
        OutputStream out = null;
        try
        {
            in = ass.open(file.toString());
            url = mContentResolver.insert(Images.Media.INTERNAL_CONTENT_URI, imagev);
            out = mContentResolver.openOutputStream(url);
            final int size = 8 * 1024;
            byte[] buf = new byte[size];
            int count = 0;
            do
            {
                count = in.read(buf, 0, size);
                if (count > 0) {
                    out.write(buf, 0, count);
                }
            } while (count > 0);
        }
        catch (Exception e)
        {
            Slog.e(LOG_TAG, "Failed to insert image '" + file + "'", e);
            url = null;
        }
        return url;
    }
    private final Uri addShortcut(String shortcut, Intent intent)
    {
        if (Config.LOGV) Slog.v(LOG_TAG, "addShortcut: shortcut=" + shortcut + ", intent=" + intent);
        return Settings.Bookmarks.add(mContentResolver, intent, null, null,
                                      shortcut != null ? shortcut.charAt(0) : 0, 0);
    }
}
