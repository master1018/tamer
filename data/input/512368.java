public abstract class AbstractMessageParser {
  public static interface Resources {
    public Set<String> getSchemes();
    public TrieNode getDomainSuffixes();
    public TrieNode getSmileys();
    public TrieNode getAcronyms();
  }
  protected abstract Resources getResources();
  public static final String musicNote = "\u266B ";
  private String text;
  private int nextChar;
  private int nextClass;
  private ArrayList<Part> parts;
  private ArrayList<Token> tokens;
  private HashMap<Character,Format> formatStart;
  private boolean parseSmilies;
  private boolean parseAcronyms;
  private boolean parseFormatting;
  private boolean parseUrls;
  private boolean parseMeText;
  private boolean parseMusic;
  public AbstractMessageParser(String text) {
    this(text, true, true, true, true, true, true);
  }
  public AbstractMessageParser(String text, boolean parseSmilies,
      boolean parseAcronyms, boolean parseFormatting, boolean parseUrls,
      boolean parseMusic, boolean parseMeText) {
    this.text = text;
    this.nextChar = 0;
    this.nextClass = 10;
    this.parts = new ArrayList<Part>();
    this.tokens = new ArrayList<Token>();
    this.formatStart = new HashMap<Character,Format>();
    this.parseSmilies = parseSmilies;
    this.parseAcronyms = parseAcronyms;
    this.parseFormatting = parseFormatting;
    this.parseUrls = parseUrls;
    this.parseMusic = parseMusic;
    this.parseMeText = parseMeText;
  }
  public final String getRawText() { return text; }
  public final int getPartCount() { return parts.size(); }
  public final Part getPart(int index) { return parts.get(index); }
  public final List<Part> getParts() { return parts; }
  public void parse() {
    if (parseMusicTrack()) {
      buildParts(null);
      return;
    }
    String meText = null;
    if (parseMeText && text.startsWith("/me") && (text.length() > 3) &&
        Character.isWhitespace(text.charAt(3))) {
      meText = text.substring(0, 4);
      text = text.substring(4);
    }
    boolean wasSmiley = false;
    while (nextChar < text.length()) {
      if (!isWordBreak(nextChar)) {
        if (!wasSmiley || !isSmileyBreak(nextChar)) {
          throw new AssertionError("last chunk did not end at word break");
        }
      }
      if (parseSmiley()) {
        wasSmiley = true;
      } else {
        wasSmiley = false;
        if (!parseAcronym() && !parseURL() && !parseFormatting()) {
          parseText();
        }
      }
    }
    for (int i = 0; i < tokens.size(); ++i) {
      if (tokens.get(i).isMedia()) {
        if ((i > 0) && (tokens.get(i - 1) instanceof Html)) {
          ((Html)tokens.get(i - 1)).trimLeadingWhitespace();
        }
        if ((i + 1 < tokens.size()) && (tokens.get(i + 1) instanceof Html)) {
          ((Html)tokens.get(i + 1)).trimTrailingWhitespace();
        }
      }
    }
    for (int i = 0; i < tokens.size(); ++i) {
      if (tokens.get(i).isHtml() &&
          (tokens.get(i).toHtml(true).length() == 0)) {
        tokens.remove(i);
        --i;  
      }
    }
    buildParts(meText);
  }
  public static Token tokenForUrl(String url, String text) {
    if(url == null) {
      return null;
    }
    Video video = Video.matchURL(url, text);
    if (video != null) {
      return video;
    }
    YouTubeVideo ytVideo = YouTubeVideo.matchURL(url, text);
    if (ytVideo != null) {
      return ytVideo;
    }
    Photo photo = Photo.matchURL(url, text);
    if (photo != null) {
      return photo;
    }
    FlickrPhoto flickrPhoto = FlickrPhoto.matchURL(url, text);
    if (flickrPhoto != null) {
      return flickrPhoto;
    }
    return new Link(url, text);
  }
  private void buildParts(String meText) {
    for (int i = 0; i < tokens.size(); ++i) {
      Token token = tokens.get(i);
      if (token.isMedia() || (parts.size() == 0) || lastPart().isMedia()) {
        parts.add(new Part());
      }
      lastPart().add(token);
    }
    if (parts.size() > 0) {
      parts.get(0).setMeText(meText);
    }
  }
  private Part lastPart() { return parts.get(parts.size() - 1); }
  private boolean parseMusicTrack() {
    if (parseMusic && text.startsWith(musicNote)) {
      addToken(new MusicTrack(text.substring(musicNote.length())));
      nextChar = text.length();
      return true;
    }
    return false;
  }
  private void parseText() {
    StringBuilder buf = new StringBuilder();
    int start = nextChar;
    do {
      char ch = text.charAt(nextChar++);
      switch (ch) {
        case '<':  buf.append("&lt;"); break;
        case '>':  buf.append("&gt;"); break;
        case '&':  buf.append("&amp;"); break;
        case '"':  buf.append("&quot;"); break;
        case '\'':  buf.append("&apos;"); break;
        case '\n':  buf.append("<br>"); break;
        default:  buf.append(ch); break;
      }
    } while (!isWordBreak(nextChar));
    addToken(new Html(text.substring(start, nextChar), buf.toString()));
  }
  private boolean parseSmiley() {
    if(!parseSmilies) {
      return false;
    }
    TrieNode match = longestMatch(getResources().getSmileys(), this, nextChar,
                                  true);
    if (match == null) {
      return false;
    } else {
      int previousCharClass = getCharClass(nextChar - 1);
      int nextCharClass = getCharClass(nextChar + match.getText().length());
      if ((previousCharClass == 2 || previousCharClass == 3)
          && (nextCharClass == 2 || nextCharClass == 3)) {
        return false;
      }
      addToken(new Smiley(match.getText()));
      nextChar += match.getText().length();
      return true;
    }
  }
  private boolean parseAcronym() {
    if(!parseAcronyms) {
      return false;
    }
    TrieNode match = longestMatch(getResources().getAcronyms(), this, nextChar);
    if (match == null) {
      return false;
    } else {
      addToken(new Acronym(match.getText(), match.getValue()));
      nextChar += match.getText().length();
      return true;
    }
  }
  private boolean isDomainChar(char c) {
    return c == '-' || Character.isLetter(c) || Character.isDigit(c);
  }
  private boolean isValidDomain(String domain) {
    if (matches(getResources().getDomainSuffixes(), reverse(domain))) {
      return true;
    }
    return false;
  }
  private boolean parseURL() {
    if (!parseUrls || !isURLBreak(nextChar)) {
      return false;
    }
    int start = nextChar;
    int index = start;
    while ((index < text.length()) && isDomainChar(text.charAt(index))) {
      index += 1;
    }
    String url = "";
    boolean done = false;
    if (index == text.length()) {
      return false;
    } else if (text.charAt(index) == ':') {
      String scheme = text.substring(nextChar, index);
      if (!getResources().getSchemes().contains(scheme)) {
        return false;
      }
    } else if (text.charAt(index) == '.') {
      while (index < text.length()) {
        char ch = text.charAt(index);
        if ((ch != '.') && !isDomainChar(ch)) {
          break;
        } else {
          index += 1;
        }
      }
      String domain = text.substring(nextChar, index);
      if (!isValidDomain(domain)) {
        return false;
      }
      if ((index + 1 < text.length()) && (text.charAt(index) == ':')) {
        char ch = text.charAt(index + 1);
        if (Character.isDigit(ch)) {
          index += 1;
          while ((index < text.length()) &&
                 Character.isDigit(text.charAt(index))) {
            index += 1;
          }
        }
      }
      if (index == text.length()) {
        done = true;
      } else {
        char ch = text.charAt(index);
        if (ch == '?') {
          if (index + 1 == text.length()) {
            done = true;
          } else {
            char ch2 = text.charAt(index + 1);
            if (Character.isWhitespace(ch2) || isPunctuation(ch2)) {
              done = true;
            }
          }
        } else if (isPunctuation(ch)) {
          done = true;
        } else if (Character.isWhitespace(ch)) {
          done = true;
        } else if ((ch == '/') || (ch == '#')) {
        } else {
          return false;
        }
      }
      url = "http:
    } else {
      return false;
    }
    if (!done) {
      while ((index < text.length()) &&
             !Character.isWhitespace(text.charAt(index))) {
        index += 1;
      }
    }
    String urlText = text.substring(start, index);
    url += urlText;
    addURLToken(url, urlText);
    nextChar = index;
    return true;
  }
  private void addURLToken(String url, String text) {
     addToken(tokenForUrl(url, text));
  }
  private boolean parseFormatting() {
    if(!parseFormatting) {
      return false;
    }
    int endChar = nextChar;
    while ((endChar < text.length()) && isFormatChar(text.charAt(endChar))) {
      endChar += 1;
    }
    if ((endChar == nextChar) || !isWordBreak(endChar)) {
      return false;
    }
    LinkedHashMap<Character, Boolean> seenCharacters =
        new LinkedHashMap<Character, Boolean>();
    for (int index = nextChar; index < endChar; ++index) {
      char ch = text.charAt(index);
      Character key = Character.valueOf(ch);
      if (seenCharacters.containsKey(key)) {
        addToken(new Format(ch, false));
      } else {
        Format start = formatStart.get(key);
        if (start != null) {
          start.setMatched(true);
          formatStart.remove(key);
          seenCharacters.put(key, Boolean.TRUE);
        } else {
          start = new Format(ch, true);
          formatStart.put(key, start);
          addToken(start);
          seenCharacters.put(key, Boolean.FALSE);
        }
      }
    }
    for (Character key : seenCharacters.keySet()) {
      if (seenCharacters.get(key) == Boolean.TRUE) {
        Format end = new Format(key.charValue(), false);
        end.setMatched(true);
        addToken(end);
      }
    }
    nextChar = endChar;
    return true;
  }
  private boolean isWordBreak(int index) {
    return getCharClass(index - 1) != getCharClass(index);
  }
  private boolean isSmileyBreak(int index) {
    if (index > 0 && index < text.length()) {
      if (isSmileyBreak(text.charAt(index - 1), text.charAt(index))) {
        return true;
      }
    }
    return false;
  }
  private boolean isURLBreak(int index) {
    switch (getCharClass(index - 1)) {
      case 2:
      case 3:
      case 4:
        return false;
      case 0:
      case 1:
      default:
        return true;
    }
  }
  private int getCharClass(int index) {
    if ((index < 0) || (text.length() <= index)) {
      return 0;
    }
    char ch = text.charAt(index);
    if (Character.isWhitespace(ch)) {
      return 1;
    } else if (Character.isLetter(ch)) {
      return 2;
    } else if (Character.isDigit(ch)) {
      return 3;
    } else if (isPunctuation(ch)) {
      return ++nextClass;
    } else {
      return 4;
    }
  }
  private static boolean isSmileyBreak(char c1, char c2) {
    switch (c1) {
      case '$': case '&': case '*': case '+': case '-':
      case '/': case '<': case '=': case '>': case '@':
      case '[': case '\\': case ']': case '^': case '|':
      case '}': case '~':
        switch (c2) {
          case '#': case '$': case '%': case '*': case '/':
          case '<': case '=': case '>': case '@': case '[':
          case '\\': case '^': case '~':
            return true;
        }
    }
    return false;
  }
  private static boolean isPunctuation(char ch) {
    switch (ch) {
      case '.': case ',': case '"': case ':': case ';':
      case '?': case '!': case '(': case ')':
        return true;
      default:
        return false;
    }
  }
  private static boolean isFormatChar(char ch) {
    switch (ch) {
      case '*': case '_': case '^':
        return true;
      default:
        return false;
    }
  }
  public static abstract class Token {
    public enum Type {
      HTML ("html"),
      FORMAT ("format"),  
      LINK ("l"),
      SMILEY ("e"),
      ACRONYM ("a"),
      MUSIC ("m"),
      GOOGLE_VIDEO ("v"),
      YOUTUBE_VIDEO ("yt"),
      PHOTO ("p"),
      FLICKR ("f");
      private String stringRep;
      Type(String stringRep) {
        this.stringRep = stringRep;
      }
      public String toString() {
        return this.stringRep;
      }
    }
    protected Type type;
    protected String text;
    protected Token(Type type, String text) {
      this.type = type;
      this.text = text;
    }
    public Type getType() { return type; }
    public List<String> getInfo() {
      List<String> info = new ArrayList<String>();
      info.add(getType().toString());
      return info;
    }
    public String getRawText() { return text; }
    public boolean isMedia() { return false; }
    public abstract boolean isHtml();
    public boolean isArray() { return !isHtml(); }
    public String toHtml(boolean caps) { throw new AssertionError("not html"); }
    public boolean controlCaps() { return false; }
    public boolean setCaps() { return false; }
  }
  public static class Html extends Token {
    private String html;
    public Html(String text, String html) {
      super(Type.HTML, text);
      this.html = html;
    }
    public boolean isHtml() { return true; }
    public String toHtml(boolean caps) {
      return caps ? html.toUpperCase() : html;
    }
    public List<String> getInfo() {
      throw new UnsupportedOperationException();
    }
    public void trimLeadingWhitespace() {
      text = trimLeadingWhitespace(text);
      html = trimLeadingWhitespace(html);
    }
    public void trimTrailingWhitespace() {
      text = trimTrailingWhitespace(text);
      html = trimTrailingWhitespace(html);
    }
    private static String trimLeadingWhitespace(String text) {
      int index = 0;
      while ((index < text.length()) &&
             Character.isWhitespace(text.charAt(index))) {
        ++index;
      }
      return text.substring(index);
    }
    public static String trimTrailingWhitespace(String text) {
      int index = text.length();
      while ((index > 0) && Character.isWhitespace(text.charAt(index - 1))) {
        --index;
      }
      return text.substring(0, index);
    }
  }
  public static class MusicTrack extends Token {
    private String track;
    public MusicTrack(String track) {
      super(Type.MUSIC, track);
      this.track = track;
    }
    public String getTrack() { return track; }
    public boolean isHtml() { return false; }
    public List<String> getInfo() {
      List<String> info = super.getInfo();
      info.add(getTrack());
      return info;
    }
  }
  public static class Link extends Token {
    private String url;
    public Link(String url, String text) {
      super(Type.LINK, text);
      this.url = url;
    }
    public String getURL() { return url; }
    public boolean isHtml() { return false; }
    public List<String> getInfo() {
      List<String> info = super.getInfo();
      info.add(getURL());
      info.add(getRawText());
      return info;
    }
  }
  public static class Video extends Token {
    private static final Pattern URL_PATTERN = Pattern.compile(
        "(?i)http:
        + ".*?\\bdocid=(-?\\d+).*");
    private String docid;
    public Video(String docid, String text) {
      super(Type.GOOGLE_VIDEO, text);
      this.docid = docid;
    }
    public String getDocID() { return docid; }
    public boolean isHtml() { return false; }
    public boolean isMedia() { return true; }
    public static Video matchURL(String url, String text) {
      Matcher m = URL_PATTERN.matcher(url);
      if (m.matches()) {
        return new Video(m.group(1), text);
      } else {
        return null;
      }
    }
    public List<String> getInfo() {
      List<String> info = super.getInfo();
      info.add(getRssUrl(docid));
      info.add(getURL(docid));
      return info;
    }
    public static String getRssUrl(String docid) {
      return "http:
             + "?type=docid&output=rss&sourceid=gtalk&docid=" + docid;
    }
    public static String getURL(String docid) {
      return getURL(docid, null);
    }
    public static String getURL(String docid, String extraParams) {
      if (extraParams == null) {
        extraParams = "";
      } else if (extraParams.length() > 0) {
        extraParams += "&";
      }
      return "http:
             + "docid=" + docid;
    }
  }
  public static class YouTubeVideo extends Token {
    private static final Pattern URL_PATTERN = Pattern.compile(
        "(?i)http:
        + ".*\\bv=([-_a-zA-Z0-9=]+).*");
    private String docid;
    public YouTubeVideo(String docid, String text) {
      super(Type.YOUTUBE_VIDEO, text);
      this.docid = docid;
    }
    public String getDocID() { return docid; }
    public boolean isHtml() { return false; }
    public boolean isMedia() { return true; }
    public static YouTubeVideo matchURL(String url, String text) {
      Matcher m = URL_PATTERN.matcher(url);
      if (m.matches()) {
        return new YouTubeVideo(m.group(1), text);
      } else {
        return null;
      }
    }
    public List<String> getInfo() {
      List<String> info = super.getInfo();
      info.add(getRssUrl(docid));
      info.add(getURL(docid));
      return info;
    }
    public static String getRssUrl(String docid) {
      return "http:
    }
    public static String getURL(String docid) {
      return getURL(docid, null);
    }
    public static String getURL(String docid, String extraParams) {
      if (extraParams == null) {
        extraParams = "";
      } else if (extraParams.length() > 0) {
        extraParams += "&";
      }
      return "http:
    }
    public static String getPrefixedURL(boolean http, String prefix,
                                        String docid, String extraParams) {
      String protocol = "";
      if (http) {
        protocol = "http:
      }
      if (prefix == null) {
        prefix = "";
      }
      if (extraParams == null) {
        extraParams = "";
      } else if (extraParams.length() > 0) {
        extraParams += "&";
      }
      return protocol + prefix + "youtube.com/watch?" + extraParams + "v=" +
              docid;
    }
  }
  public static class Photo extends Token {
    private static final Pattern URL_PATTERN = Pattern.compile(
        "http:
    private String user;
    private String album;
    private String photo;  
    public Photo(String user, String album, String photo, String text) {
      super(Type.PHOTO, text);
      this.user = user;
      this.album = album;
      this.photo = photo;
    }
    public String getUser() { return user; }
    public String getAlbum() { return album; }
    public String getPhoto() { return photo; }
    public boolean isHtml() { return false; }
    public boolean isMedia() { return true; }
    public static Photo matchURL(String url, String text) {
      Matcher m = URL_PATTERN.matcher(url);
      if (m.matches()) {
        return new Photo(m.group(1), m.group(2), m.group(3), text);
      } else {
        return null;
      }
    }
    public List<String> getInfo() {
      List<String> info = super.getInfo();
      info.add(getRssUrl(getUser()));
      info.add(getAlbumURL(getUser(), getAlbum()));
      if (getPhoto() != null) {
        info.add(getPhotoURL(getUser(), getAlbum(), getPhoto()));
      } else {
        info.add((String)null);
      }
      return info;
    }
    public static String getRssUrl(String user) {
      return "http:
        "?category=album&alt=rss";
    }
    public static String getAlbumURL(String user, String album) {
      return "http:
    }
    public static String getPhotoURL(String user, String album, String photo) {
      return "http:
             + photo;
    }
  }
  public static class FlickrPhoto extends Token {
    private static final Pattern URL_PATTERN = Pattern.compile(
        "http:
    private static final Pattern GROUPING_PATTERN = Pattern.compile(
        "http:
        "([^/?#&]+)/?");
    private static final String SETS = "sets";
    private static final String TAGS = "tags";
    private String user;
    private String photo;      
    private String grouping;   
    private String groupingId; 
    public FlickrPhoto(String user, String photo, String grouping,
                       String groupingId, String text) {
      super(Type.FLICKR, text);
      if (!TAGS.equals(user)) {
        this.user = user;
        this.photo = (!"show".equals(photo) ? photo : null);
        this.grouping = grouping;
        this.groupingId = groupingId;
      } else {
        this.user = null;
        this.photo = null;
        this.grouping = TAGS;
        this.groupingId = photo;
      }
    }
    public String getUser() { return user; }
    public String getPhoto() { return photo; }
    public String getGrouping() { return grouping; }
    public String getGroupingId() { return groupingId; }
    public boolean isHtml() { return false; }
    public boolean isMedia() { return true; }
    public static FlickrPhoto matchURL(String url, String text) {
      Matcher m = GROUPING_PATTERN.matcher(url);
      if (m.matches()) {
        return new FlickrPhoto(m.group(1), null, m.group(2), m.group(3), text);
      }
      m = URL_PATTERN.matcher(url);
      if (m.matches()) {
        return new FlickrPhoto(m.group(1), m.group(2), null, null, text);
      } else {
        return null;
      }
    }
    public List<String> getInfo() {
      List<String> info = super.getInfo();
      info.add(getUrl());
      info.add(getUser() != null ? getUser() : "");
      info.add(getPhoto() != null ? getPhoto() : "");
      info.add(getGrouping() != null ? getGrouping() : "");
      info.add(getGroupingId() != null ? getGroupingId() : "");
      return info;
    }
    public String getUrl() {
      if (SETS.equals(grouping)) {
        return getUserSetsURL(user, groupingId);
      } else if (TAGS.equals(grouping)) {
        if (user != null) {
          return getUserTagsURL(user, groupingId);
        } else {
          return getTagsURL(groupingId);
        }
      } else if (photo != null) {
        return getPhotoURL(user, photo);
      } else {
        return getUserURL(user);
      }
    }
    public static String getRssUrl(String user) {
      return null;
    }
    public static String getTagsURL(String tag) {
      return "http:
    }
    public static String getUserURL(String user) {
      return "http:
    }
    public static String getPhotoURL(String user, String photo) {
      return "http:
    }
    public static String getUserTagsURL(String user, String tagId) {
      return "http:
    }
    public static String getUserSetsURL(String user, String setId) {
      return "http:
    }
  }
  public static class Smiley extends Token {
    public Smiley(String text) {
      super(Type.SMILEY, text);
    }
    public boolean isHtml() { return false; }
    public List<String> getInfo() {
      List<String> info = super.getInfo();
      info.add(getRawText());
      return info;
    }
  }
  public static class Acronym extends Token {
    private String value;
    public Acronym(String text, String value) {
      super(Type.ACRONYM, text);
      this.value = value;
    }
    public String getValue() { return value; }
    public boolean isHtml() { return false; }
    public List<String> getInfo() {
      List<String> info = super.getInfo();
      info.add(getRawText());
      info.add(getValue());
      return info;
    }
  }
  public static class Format extends Token {
    private char ch;
    private boolean start;
    private boolean matched;
    public Format(char ch, boolean start) {
      super(Type.FORMAT, String.valueOf(ch));
      this.ch = ch;
      this.start = start;
    }
    public void setMatched(boolean matched) { this.matched = matched; }
    public boolean isHtml() { return true; }
    public String toHtml(boolean caps) {
      if (matched) {
        return start ? getFormatStart(ch) : getFormatEnd(ch);
      } else {
        return (ch == '"') ? "&quot;" : String.valueOf(ch);
      }
    }
    public List<String> getInfo() {
      throw new UnsupportedOperationException();
    }
    public boolean controlCaps() { return (ch == '^'); }
    public boolean setCaps() { return start; }
    private String getFormatStart(char ch) {
      switch (ch) {
        case '*': return "<b>";
        case '_': return "<i>";
        case '^': return "<b><font color=\"#005FFF\">"; 
        case '"': return "<font color=\"#999999\">\u201c";
        default: throw new AssertionError("unknown format '" + ch + "'");
      }
    }
    private String getFormatEnd(char ch) {
      switch (ch) {
        case '*': return "</b>";
        case '_': return "</i>";
        case '^': return "</font></b>"; 
        case '"': return "\u201d</font>";
        default: throw new AssertionError("unknown format '" + ch + "'");
      }
    }
  }
  private void addToken(Token token) {
    tokens.add(token);
  }
  public String toHtml() {
    StringBuilder html = new StringBuilder();
    for (Part part : parts) {
      boolean caps = false;
      html.append("<p>");
      for (Token token : part.getTokens()) {
        if (token.isHtml()) {
          html.append(token.toHtml(caps));
        } else {
          switch (token.getType()) {
          case LINK:
            html.append("<a href=\"");
            html.append(((Link)token).getURL());
            html.append("\">");
            html.append(token.getRawText());
            html.append("</a>");
            break;
          case SMILEY:
            html.append(token.getRawText());
            break;
          case ACRONYM:
            html.append(token.getRawText());
            break;
          case MUSIC:
            html.append(((MusicTrack)token).getTrack());
            break;
          case GOOGLE_VIDEO:
            html.append("<a href=\"");
            html.append(((Video)token).getURL(((Video)token).getDocID()));
            html.append("\">");
            html.append(token.getRawText());
            html.append("</a>");
            break;
          case YOUTUBE_VIDEO:
            html.append("<a href=\"");
            html.append(((YouTubeVideo)token).getURL(
                ((YouTubeVideo)token).getDocID()));
            html.append("\">");
            html.append(token.getRawText());
            html.append("</a>");
            break;
          case PHOTO: {
            html.append("<a href=\"");
            html.append(Photo.getAlbumURL(
                ((Photo)token).getUser(), ((Photo)token).getAlbum()));
            html.append("\">");
            html.append(token.getRawText());
            html.append("</a>");
            break;
          }
          case FLICKR:
            Photo p = (Photo) token;
            html.append("<a href=\"");
            html.append(((FlickrPhoto)token).getUrl());
            html.append("\">");
            html.append(token.getRawText());
            html.append("</a>");
            break;
          default:
            throw new AssertionError("unknown token type: " + token.getType());
          }
        }
        if (token.controlCaps()) {
          caps = token.setCaps();
        }
      }
      html.append("</p>\n");
    }
    return html.toString();
  }
  protected static String reverse(String str) {
    StringBuilder buf = new StringBuilder();
    for (int i = str.length() - 1; i >= 0; --i) {
      buf.append(str.charAt(i));
    }
    return buf.toString();
  }
  public static class TrieNode {
    private final HashMap<Character,TrieNode> children =
        new HashMap<Character,TrieNode>();
    private String text;
    private String value;
    public TrieNode() { this(""); }
    public TrieNode(String text) {
      this.text = text;
    }
    public final boolean exists() { return value != null; }
    public final String getText() { return text; }
    public final String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public TrieNode getChild(char ch) {
      return children.get(Character.valueOf(ch));
    }
    public TrieNode getOrCreateChild(char ch) {
      Character key = Character.valueOf(ch);
      TrieNode node = children.get(key);
      if (node == null) {
        node = new TrieNode(text + String.valueOf(ch));
        children.put(key, node);
      }
      return node;
    }
    public static  void addToTrie(TrieNode root, String str, String value) {
      int index = 0;
      while (index < str.length()) {
        root = root.getOrCreateChild(str.charAt(index++));
      }
      root.setValue(value);
    }
  }
  private static boolean matches(TrieNode root, String str) {
    int index = 0;
    while (index < str.length()) {
      root = root.getChild(str.charAt(index++));
      if (root == null) {
        break;
      } else if (root.exists()) {
        return true;
      }
    }
    return false;
  }
  private static TrieNode longestMatch(
      TrieNode root, AbstractMessageParser p, int start) {
    return longestMatch(root, p, start, false);
  }
  private static TrieNode longestMatch(
      TrieNode root, AbstractMessageParser p, int start, boolean smiley) {
    int index = start;
    TrieNode bestMatch = null;
    while (index < p.getRawText().length()) {
      root = root.getChild(p.getRawText().charAt(index++));
      if (root == null) {
        break;
      } else if (root.exists()) {
        if (p.isWordBreak(index)) {
          bestMatch = root;
        } else if (smiley && p.isSmileyBreak(index)) {
          bestMatch = root;
        }
      }
    }
    return bestMatch;
  }
  public static class Part {
    private String meText;
    private ArrayList<Token> tokens;
    public Part() {
      this.tokens = new ArrayList<Token>();
    }
    public String getType(boolean isSend) {
      return (isSend ? "s" : "r") + getPartType();
    }
    private String getPartType() {
      if (isMedia()) {
        return "d";
      } else if (meText != null) {
        return "m";
      } else {
        return "";
      }
    }
    public boolean isMedia() {
      return (tokens.size() == 1) && tokens.get(0).isMedia();
    }
    public Token getMediaToken() {
      if(isMedia()) {
        return tokens.get(0);
      }
      return null;
    }
    public void add(Token token) {
      if (isMedia()) {
        throw new AssertionError("media ");
      }
       tokens.add(token);
    }
    public void setMeText(String meText) {
      this.meText = meText;
    }
    public String getRawText() {
      StringBuilder buf = new StringBuilder();
      if (meText != null) {
        buf.append(meText);
      }
      for (int i = 0; i < tokens.size(); ++i) {
        buf.append(tokens.get(i).getRawText());
      }
      return buf.toString();
    }
    public ArrayList<Token> getTokens() { return tokens; }
  }
}
