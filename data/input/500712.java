  public class RFC2965CommentUrlAttributeHandler implements CookieAttributeHandler {
      public RFC2965CommentUrlAttributeHandler() {
          super();
      }
      public void parse(final SetCookie cookie, final String commenturl)
              throws MalformedCookieException {
          if (cookie instanceof SetCookie2) {
              SetCookie2 cookie2 = (SetCookie2) cookie;
              cookie2.setCommentURL(commenturl);
          }
      }
      public void validate(final Cookie cookie, final CookieOrigin origin)
              throws MalformedCookieException {
      }
      public boolean match(final Cookie cookie, final CookieOrigin origin) {
          return true;
      }
  }