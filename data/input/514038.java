public final class PhotoEntry extends Entry {
    public static final EntrySchema SCHEMA = new EntrySchema(PhotoEntry.class);
    @Column("sync_account")
    public String syncAccount;
    @Column("edit_uri")
    public String editUri;
    @Column(value = "album_id", indexed = true)
    public long albumId;
    @Column(value = "display_index", indexed = true)
    public int displayIndex;
    @Column("title")
    public String title;
    @Column("summary")
    public String summary;
    @Column("date_published")
    public long datePublished;
    @Column("date_updated")
    public long dateUpdated;
    @Column("date_edited")
    public long dateEdited;
    @Column("date_taken")
    public long dateTaken;
    @Column("comment_count")
    public int commentCount;
    @Column("width")
    public int width;
    @Column("height")
    public int height;
    @Column("rotation")
    public int rotation;
    @Column("size")
    public int size;
    @Column("latitude")
    public double latitude;
    @Column("longitude")
    public double longitude;
    @Column("thumbnail_url")
    public String thumbnailUrl;
    @Column("screennail_url")
    public String screennailUrl;
    @Column("content_url")
    public String contentUrl;
    @Column("content_type")
    public String contentType;
    @Column("html_page_url")
    public String htmlPageUrl;
    @Override
    public void clear() {
        super.clear();
        syncAccount = null;
        editUri = null;
        albumId = 0;
        displayIndex = 0;
        title = null;
        summary = null;
        datePublished = 0;
        dateUpdated = 0;
        dateEdited = 0;
        dateTaken = 0;
        commentCount = 0;
        width = 0;
        height = 0;
        rotation = 0;
        size = 0;
        latitude = 0;
        longitude = 0;
        thumbnailUrl = null;
        screennailUrl = null;
        contentUrl = null;
        contentType = null;
        htmlPageUrl = null;
    }
    @Override
    public void setPropertyFromXml(String uri, String localName, Attributes attrs, String content) {
        try {
            char localNameChar = localName.charAt(0);
            if (uri.equals(GDataParser.GPHOTO_NAMESPACE)) {
                switch (localNameChar) {
                case 'i':
                    if (localName.equals("id")) {
                        id = Long.parseLong(content);
                    }
                    break;
                case 'a':
                    if (localName.equals("albumid")) {
                        albumId = Long.parseLong(content);
                    }
                    break;
                case 't':
                    if (localName.equals("timestamp")) {
                        dateTaken = Long.parseLong(content);
                    }
                    break;
                case 'c':
                    if (localName.equals("commentCount")) {
                        commentCount = Integer.parseInt(content);
                    }
                    break;
                case 'w':
                    if (localName.equals("width")) {
                        width = Integer.parseInt(content);
                    }
                    break;
                case 'h':
                    if (localName.equals("height")) {
                        height = Integer.parseInt(content);
                    }
                    break;
                case 'r':
                    if (localName.equals("rotation")) {
                        rotation = Integer.parseInt(content);
                    }
                    break;
                case 's':
                    if (localName.equals("size")) {
                        size = Integer.parseInt(content);
                    }
                    break;
                case 'l':
                    if (localName.equals("latitude")) {
                        latitude = Double.parseDouble(content);
                    } else if (localName.equals("longitude")) {
                        longitude = Double.parseDouble(content);
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
                if (localName.equals("thumbnail")) {
                    int width = Integer.parseInt(attrs.getValue("", "width"));
                    int height = Integer.parseInt(attrs.getValue("", "height"));
                    int dimension = Math.max(width, height);
                    String url = attrs.getValue("", "url");
                    if (dimension <= 300) {
                        thumbnailUrl = url;
                    } else {
                        screennailUrl = url;
                    }
                } else if (localName.equals("content")) {
                    String type = attrs.getValue("", "type");
                    if (contentUrl == null || type.startsWith("video/")) {
                        contentUrl = attrs.getValue("", "url");
                        contentType = type;
                    }
                }
            } else if (uri.equals(GDataParser.GML_NAMESPACE)) {
                if (localName.equals("pos")) {
                    int spaceIndex = content.indexOf(' ');
                    if (spaceIndex != -1) {
                        latitude = Double.parseDouble(content.substring(0, spaceIndex));
                        longitude = Double.parseDouble(content.substring(spaceIndex + 1));
                    }
                }
            }
        } catch (Exception e) {
            return;
        }
    }
}
