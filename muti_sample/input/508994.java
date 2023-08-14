public final class AlbumEntry extends Entry {
    public static final EntrySchema SCHEMA = new EntrySchema(AlbumEntry.class);
    @Column(Columns.SYNC_ACCOUNT)
    public String syncAccount;
    @Column(Columns.PHOTOS_ETAG)
    public String photosEtag = null;
    @Column(Columns.PHOTOS_DIRTY)
    public boolean photosDirty;
    @Column(Columns.EDIT_URI)
    public String editUri;
    @Column(Columns.USER)
    public String user;
    @Column(value = Columns.TITLE)
    public String title;
    @Column(value = Columns.SUMMARY)
    public String summary;
    @Column(Columns.DATE_PUBLISHED)
    public long datePublished;
    @Column(Columns.DATE_UPDATED)
    public long dateUpdated;
    @Column(Columns.DATE_EDITED)
    public long dateEdited;
    @Column(Columns.NUM_PHOTOS)
    public int numPhotos;
    @Column(Columns.BYTES_USED)
    public long bytesUsed;
    @Column(Columns.LOCATION_STRING)
    public String locationString;
    @Column(Columns.THUMBNAIL_URL)
    public String thumbnailUrl;
    @Column(Columns.HTML_PAGE_URL)
    public String htmlPageUrl;
    public static final class Columns extends PicasaApi.Columns {
        public static final String PHOTOS_ETAG = "photos_etag";
        public static final String USER = "user";
        public static final String BYTES_USED = "bytes_used";
        public static final String NUM_PHOTOS = "num_photos";
        public static final String LOCATION_STRING = "location_string";
        public static final String PHOTOS_DIRTY = "photos_dirty";
    }
    @Override
    public void clear() {
        super.clear();
        syncAccount = null;
        photosDirty = false;
        editUri = null;
        user = null;
        title = null;
        summary = null;
        datePublished = 0;
        dateUpdated = 0;
        dateEdited = 0;
        numPhotos = 0;
        bytesUsed = 0;
        locationString = null;
        thumbnailUrl = null;
        htmlPageUrl = null;
    }
    @Override
    public void setPropertyFromXml(String uri, String localName, Attributes attrs, String content) {
        char localNameChar = localName.charAt(0);
        if (uri.equals(GDataParser.GPHOTO_NAMESPACE)) {
            switch (localNameChar) {
            case 'i':
                if (localName.equals("id")) {
                    id = Long.parseLong(content);
                }
                break;
            case 'u':
                if (localName.equals("user")) {
                    user = content;
                }
                break;
            case 'n':
                if (localName.equals("numphotos")) {
                    numPhotos = Integer.parseInt(content);
                }
                break;
            case 'b':
                if (localName.equals("bytesUsed")) {
                    bytesUsed = Long.parseLong(content);
                }
                break;
            }
        } else if (uri.equals(GDataParser.ATOM_NAMESPACE)) {
            switch (localNameChar) {
            case 't':
                if (localName.equals("title")) {
                    title = content;
                }
                break;
            case 's':
                if (localName.equals("summary")) {
                    summary = content;
                }
                break;
            case 'p':
                if (localName.equals("published")) {
                    datePublished = GDataParser.parseAtomTimestamp(content);
                }
                break;
            case 'u':
                if (localName.equals("updated")) {
                    dateUpdated = GDataParser.parseAtomTimestamp(content);
                }
                break;
            case 'l':
                if (localName.equals("link")) {
                    String rel = attrs.getValue("", "rel");
                    String href = attrs.getValue("", "href");
                    if (rel.equals("alternate") && attrs.getValue("", "type").equals("text/html")) {
                        htmlPageUrl = href;
                    } else if (rel.equals("edit")) {
                        editUri = href;
                    }
                }
                break;
            }
        } else if (uri.equals(GDataParser.APP_NAMESPACE)) {
            if (localName.equals("edited")) {
                dateEdited = GDataParser.parseAtomTimestamp(content);
            }
        } else if (uri.equals(GDataParser.MEDIA_RSS_NAMESPACE)) {
            if (localName == "thumbnail") {
                thumbnailUrl = attrs.getValue("", "url");
            }
        }
    }
}
