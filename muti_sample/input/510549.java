public abstract class CharMatcher implements Predicate<Character> {
  private static final String BREAKING_WHITESPACE_CHARS =
      "\t\n\013\f\r \u0085\u1680\u2028\u2029\u205f\u3000";
  private static final String NON_BREAKING_WHITESPACE_CHARS =
      "\u00a0\u180e\u202f";
  public static final CharMatcher WHITESPACE =
      anyOf(BREAKING_WHITESPACE_CHARS + NON_BREAKING_WHITESPACE_CHARS)
          .or(inRange('\u2000', '\u200a'));
  public static final CharMatcher BREAKING_WHITESPACE =
      anyOf(BREAKING_WHITESPACE_CHARS)
          .or(inRange('\u2000', '\u2006'))
          .or(inRange('\u2008', '\u200a'));
  public static final CharMatcher ASCII = inRange('\0', '\u007f');
  public static final CharMatcher DIGIT;
  static {
    CharMatcher digit = inRange('0', '9');
    String zeroes =
        "\u0660\u06f0\u07c0\u0966\u09e6\u0a66\u0ae6\u0b66\u0be6\u0c66"
            + "\u0ce6\u0d66\u0e50\u0ed0\u0f20\u1040\u1090\u17e0\u1810\u1946"
            + "\u19d0\u1b50\u1bb0\u1c40\u1c50\ua620\ua8d0\ua900\uaa50\uff10";
    for (char base : zeroes.toCharArray()) {
      digit = digit.or(inRange(base, (char) (base + 9)));
    }
    DIGIT = digit;
  }
  public static final CharMatcher JAVA_WHITESPACE
      = inRange('\u0009', (char) 13)  
      .or(inRange('\u001c', '\u0020'))
      .or(is('\u1680'))
      .or(is('\u180e'))
      .or(inRange('\u2000', '\u2006'))
      .or(inRange('\u2008', '\u200b'))
      .or(inRange('\u2028', '\u2029'))
      .or(is('\u205f'))
      .or(is('\u3000'));
  public static final CharMatcher JAVA_DIGIT = new CharMatcher() {
    @Override public boolean matches(char c) {
      return Character.isDigit(c);
    }
  };
  public static final CharMatcher JAVA_LETTER = new CharMatcher() {
    @Override public boolean matches(char c) {
      return Character.isLetter(c);
    }
  };
  public static final CharMatcher JAVA_LETTER_OR_DIGIT = new CharMatcher() {
    @Override public boolean matches(char c) {
      return Character.isLetterOrDigit(c);
    }
  };
  public static final CharMatcher JAVA_UPPER_CASE = new CharMatcher() {
    @Override public boolean matches(char c) {
      return Character.isUpperCase(c);
    }
  };
  public static final CharMatcher JAVA_LOWER_CASE = new CharMatcher() {
    @Override public boolean matches(char c) {
      return Character.isLowerCase(c);
    }
  };
  public static final CharMatcher JAVA_ISO_CONTROL = inRange('\u0000', '\u001f')
      .or(inRange('\u007f', '\u009f'));
  public static final CharMatcher INVISIBLE = inRange('\u0000', '\u0020')
      .or(inRange('\u007f', '\u00a0'))
      .or(is('\u00ad'))
      .or(inRange('\u0600', '\u0603'))
      .or(anyOf("\u06dd\u070f\u1680\u17b4\u17b5\u180e"))
      .or(inRange('\u2000', '\u200f'))
      .or(inRange('\u2028', '\u202f'))
      .or(inRange('\u205f', '\u2064'))
      .or(inRange('\u206a', '\u206f'))
      .or(is('\u3000'))
      .or(inRange('\ud800', '\uf8ff'))
      .or(anyOf("\ufeff\ufff9\ufffa\ufffb"));
  public static final CharMatcher SINGLE_WIDTH = inRange('\u0000', '\u04f9')
      .or(is('\u05be'))
      .or(inRange('\u05d0', '\u05ea'))
      .or(is('\u05f3'))
      .or(is('\u05f4'))
      .or(inRange('\u0600', '\u06ff'))
      .or(inRange('\u0750', '\u077f'))
      .or(inRange('\u0e00', '\u0e7f'))
      .or(inRange('\u1e00', '\u20af'))
      .or(inRange('\u2100', '\u213a'))
      .or(inRange('\ufb50', '\ufdff'))
      .or(inRange('\ufe70', '\ufeff'))
      .or(inRange('\uff61', '\uffdc'));
  public static final CharMatcher ANY = new CharMatcher() {
    @Override public boolean matches(char c) {
      return true;
    }
    @Override public int indexIn(CharSequence sequence) {
      return (sequence.length() == 0) ? -1 : 0;
    }
    @Override public int indexIn(CharSequence sequence, int start) {
      int length = sequence.length();
      Preconditions.checkPositionIndex(start, length);
      return (start == length) ? -1 : start;
    }
    @Override public int lastIndexIn(CharSequence sequence) {
      return sequence.length() - 1;
    }
    @Override public boolean matchesAllOf(CharSequence sequence) {
      checkNotNull(sequence);
      return true;
    }
    @Override public boolean matchesNoneOf(CharSequence sequence) {
      return sequence.length() == 0;
    }
    @Override public String removeFrom(CharSequence sequence) {
      checkNotNull(sequence);
      return "";
    }
    @Override public String replaceFrom(
        CharSequence sequence, char replacement) {
      char[] array = new char[sequence.length()];
      Arrays.fill(array, replacement);
      return new String(array);
    }
    @Override public String replaceFrom(
        CharSequence sequence, CharSequence replacement) {
      StringBuilder retval = new StringBuilder(sequence.length() * replacement.length());
      for (int i = 0; i < sequence.length(); i++) {
        retval.append(replacement);
      }
      return retval.toString();
    }
    @Override public String collapseFrom(CharSequence sequence, char replacement) {
      return (sequence.length() == 0) ? "" : String.valueOf(replacement);
    }
    @Override public String trimFrom(CharSequence sequence) {
      checkNotNull(sequence);
      return "";
    }
    @Override public int countIn(CharSequence sequence) {
      return sequence.length();
    }
    @Override public CharMatcher and(CharMatcher other) {
      return checkNotNull(other);
    }
    @Override public CharMatcher or(CharMatcher other) {
      checkNotNull(other);
      return this;
    }
    @Override public CharMatcher negate() {
      return NONE;
    }
    @Override public CharMatcher precomputed() {
      return this;
    }
  };
  public static final CharMatcher NONE = new CharMatcher() {
    @Override public boolean matches(char c) {
      return false;
    }
    @Override public int indexIn(CharSequence sequence) {
      checkNotNull(sequence);
      return -1;
    }
    @Override public int indexIn(CharSequence sequence, int start) {
      int length = sequence.length();
      Preconditions.checkPositionIndex(start, length);
      return -1;
    }
    @Override public int lastIndexIn(CharSequence sequence) {
      checkNotNull(sequence);
      return -1;
    }
    @Override public boolean matchesAllOf(CharSequence sequence) {
      return sequence.length() == 0;
    }
    @Override public boolean matchesNoneOf(CharSequence sequence) {
      checkNotNull(sequence);
      return true;
    }
    @Override public String removeFrom(CharSequence sequence) {
      return sequence.toString();
    }
    @Override public String replaceFrom(
        CharSequence sequence, char replacement) {
      return sequence.toString();
    }
    @Override public String replaceFrom(
        CharSequence sequence, CharSequence replacement) {
      checkNotNull(replacement);
      return sequence.toString();
    }
    @Override public String collapseFrom(
        CharSequence sequence, char replacement) {
      return sequence.toString();
    }
    @Override public String trimFrom(CharSequence sequence) {
      return sequence.toString();
    }
    @Override public int countIn(CharSequence sequence) {
      checkNotNull(sequence);
      return 0;
    }
    @Override public CharMatcher and(CharMatcher other) {
      checkNotNull(other);
      return this;
    }
    @Override public CharMatcher or(CharMatcher other) {
      return checkNotNull(other);
    }
    @Override public CharMatcher negate() {
      return ANY;
    }
    @Override protected void setBits(LookupTable table) {
    }
    @Override public CharMatcher precomputed() {
      return this;
    }
  };
  public static CharMatcher is(final char match) {
    return new CharMatcher() {
      @Override public boolean matches(char c) {
        return c == match;
      }
      @Override public String replaceFrom(
          CharSequence sequence, char replacement) {
        return sequence.toString().replace(match, replacement);
      }
      @Override public CharMatcher and(CharMatcher other) {
        return other.matches(match) ? this : NONE;
      }
      @Override public CharMatcher or(CharMatcher other) {
        return other.matches(match) ? other : super.or(other);
      }
      @Override public CharMatcher negate() {
        return isNot(match);
      }
      @Override protected void setBits(LookupTable table) {
        table.set(match);
      }
      @Override public CharMatcher precomputed() {
        return this;
      }
    };
  }
  public static CharMatcher isNot(final char match) {
    return new CharMatcher() {
      @Override public boolean matches(char c) {
        return c != match;
      }
      @Override public CharMatcher and(CharMatcher other) {
        return other.matches(match) ? super.and(other) : other;
      }
      @Override public CharMatcher or(CharMatcher other) {
        return other.matches(match) ? ANY : this;
      }
      @Override public CharMatcher negate() {
        return is(match);
      }
    };
  }
  public static CharMatcher anyOf(final CharSequence sequence) {
    switch (sequence.length()) {
      case 0:
        return NONE;
      case 1:
        return is(sequence.charAt(0));
      case 2:
        final char match1 = sequence.charAt(0);
        final char match2 = sequence.charAt(1);
        return new CharMatcher() {
          @Override public boolean matches(char c) {
            return c == match1 || c == match2;
          }
          @Override protected void setBits(LookupTable table) {
            table.set(match1);
            table.set(match2);
          }
          @Override public CharMatcher precomputed() {
            return this;
          }
        };
    }
    final char[] chars = sequence.toString().toCharArray();
    Arrays.sort(chars); 
    return new CharMatcher() {
      @Override public boolean matches(char c) {
        return Arrays.binarySearch(chars, c) >= 0;
      }
      @Override protected void setBits(LookupTable table) {
        for (char c : chars) {
          table.set(c);
        }
      }
    };
  }
  public static CharMatcher noneOf(CharSequence sequence) {
    return anyOf(sequence).negate();
  }
  public static CharMatcher inRange(
      final char startInclusive, final char endInclusive) {
    checkArgument(endInclusive >= startInclusive);
    return new CharMatcher() {
      @Override public boolean matches(char c) {
        return startInclusive <= c && c <= endInclusive;
      }
      @Override protected void setBits(LookupTable table) {
        char c = startInclusive;
        while (true) {
          table.set(c);
          if (c++ == endInclusive) {
            break;
          }
        }
      }
      @Override public CharMatcher precomputed() {
        return this;
      }
    };
  }
  public static CharMatcher forPredicate(
      final Predicate<? super Character> predicate) {
    checkNotNull(predicate);
    if (predicate instanceof CharMatcher) {
      return (CharMatcher) predicate;
    }
    return new CharMatcher() {
      @Override public boolean matches(char c) {
        return predicate.apply(c);
      }
      @Override public boolean apply(Character character) {
        return predicate.apply(checkNotNull(character));
      }
    };
  }
  public abstract boolean matches(char c);
  public CharMatcher negate() {
    final CharMatcher original = this;
    return new CharMatcher() {
      @Override public boolean matches(char c) {
        return !original.matches(c);
      }
      @Override public boolean matchesAllOf(CharSequence sequence) {
        return original.matchesNoneOf(sequence);
      }
      @Override public boolean matchesNoneOf(CharSequence sequence) {
        return original.matchesAllOf(sequence);
      }
      @Override public int countIn(CharSequence sequence) {
        return sequence.length() - original.countIn(sequence);
      }
      @Override public CharMatcher negate() {
        return original;
      }
    };
  }
  public CharMatcher and(CharMatcher other) {
    return new And(Arrays.asList(this, checkNotNull(other)));
  }
  private static class And extends CharMatcher {
    List<CharMatcher> components;
    And(List<CharMatcher> components) {
      this.components = components; 
    }
    @Override public boolean matches(char c) {
      for (CharMatcher matcher : components) {
        if (!matcher.matches(c)) {
          return false;
        }
      }
      return true;
    }
    @Override public CharMatcher and(CharMatcher other) {
      List<CharMatcher> newComponents = new ArrayList<CharMatcher>(components);
      newComponents.add(checkNotNull(other));
      return new And(newComponents);
    }
  }
  public CharMatcher or(CharMatcher other) {
    return new Or(Arrays.asList(this, checkNotNull(other)));
  }
  private static class Or extends CharMatcher {
    List<CharMatcher> components;
    Or(List<CharMatcher> components) {
      this.components = components; 
    }
    @Override public boolean matches(char c) {
      for (CharMatcher matcher : components) {
        if (matcher.matches(c)) {
          return true;
        }
      }
      return false;
    }
    @Override public CharMatcher or(CharMatcher other) {
      List<CharMatcher> newComponents = new ArrayList<CharMatcher>(components);
      newComponents.add(checkNotNull(other));
      return new Or(newComponents);
    }
    @Override protected void setBits(LookupTable table) {
      for (CharMatcher matcher : components) {
        matcher.setBits(table);
      }
    }
  }
  public CharMatcher precomputed() {
    return Platform.precomputeCharMatcher(this);
  }
  CharMatcher precomputedInternal() {
    final LookupTable table = new LookupTable();
    setBits(table);
    return new CharMatcher() {
      @Override public boolean matches(char c) {
        return table.get(c);
      }
      @Override public CharMatcher precomputed() {
        return this;
      }
    };
  }
  protected void setBits(LookupTable table) {
    char c = Character.MIN_VALUE;
    while (true) {
      if (matches(c)) {
        table.set(c);
      }
      if (c++ == Character.MAX_VALUE) {
        break;
      }
    }
  }
  protected static class LookupTable {
    int[] data = new int[2048];
    void set(char index) {
      data[index >> 5] |= (1 << index);
    }
    boolean get(char index) {
      return (data[index >> 5] & (1 << index)) != 0;
    }
  }
  public boolean matchesAllOf(CharSequence sequence) {
    for (int i = sequence.length() - 1; i >= 0; i--) {
      if (!matches(sequence.charAt(i))) {
        return false;
      }
    }
    return true;
  }
  public boolean matchesNoneOf(CharSequence sequence) {
    return indexIn(sequence) == -1;
  }
  public int indexIn(CharSequence sequence) {
    int length = sequence.length();
    for (int i = 0; i < length; i++) {
      if (matches(sequence.charAt(i))) {
        return i;
      }
    }
    return -1;
  }
  public int indexIn(CharSequence sequence, int start) {
    int length = sequence.length();
    Preconditions.checkPositionIndex(start, length);
    for (int i = start; i < length; i++) {
      if (matches(sequence.charAt(i))) {
        return i;
      }
    }
    return -1;
  }
  public int lastIndexIn(CharSequence sequence) {
    for (int i = sequence.length() - 1; i >= 0; i--) {
      if (matches(sequence.charAt(i))) {
        return i;
      }
    }
    return -1;
  }
  public int countIn(CharSequence sequence) {
    int count = 0;
    for (int i = 0; i < sequence.length(); i++) {
      if (matches(sequence.charAt(i))) {
        count++;
      }
    }
    return count;
  }
  public String removeFrom(CharSequence sequence) {
    String string = sequence.toString();
    int pos = indexIn(string);
    if (pos == -1) {
      return string;
    }
    char[] chars = string.toCharArray();
    int spread = 1;
    OUT:
    while (true) {
      pos++;
      while (true) {
        if (pos == chars.length) {
          break OUT;
        }
        if (matches(chars[pos])) {
          break;
        }
        chars[pos - spread] = chars[pos];
        pos++;
      }
      spread++;
    }
    return new String(chars, 0, pos - spread);
  }
  public String retainFrom(CharSequence sequence) {
    return negate().removeFrom(sequence);
  }
  public String replaceFrom(CharSequence sequence, char replacement) {
    String string = sequence.toString();
    int pos = indexIn(string);
    if (pos == -1) {
      return string;
    }
    char[] chars = string.toCharArray();
    chars[pos] = replacement;
    for (int i = pos + 1; i < chars.length; i++) {
      if (matches(chars[i])) {
        chars[i] = replacement;
      }
    }
    return new String(chars);
  }
  public String replaceFrom(CharSequence sequence, CharSequence replacement) {
    int replacementLen = replacement.length();
    if (replacementLen == 0) {
      return removeFrom(sequence);
    }
    if (replacementLen == 1) {
      return replaceFrom(sequence, replacement.charAt(0));
    }
    String string = sequence.toString();
    int pos = indexIn(string);
    if (pos == -1) {
      return string;
    }
    int len = string.length();
    StringBuilder buf = new StringBuilder((int) (len * 1.5) + 16);
    int oldpos = 0;
    do {
      buf.append(string, oldpos, pos);
      buf.append(replacement);
      oldpos = pos + 1;
      pos = indexIn(string, oldpos);
    } while (pos != -1);
    buf.append(string, oldpos, len);
    return buf.toString();
  }
  public String trimFrom(CharSequence sequence) {
    int len = sequence.length();
    int first;
    int last;
    for (first = 0; first < len; first++) {
      if (!matches(sequence.charAt(first))) {
        break;
      }
    }
    for (last = len - 1; last > first; last--) {
      if (!matches(sequence.charAt(last))) {
        break;
      }
    }
    return sequence.subSequence(first, last + 1).toString();
  }
  public String trimLeadingFrom(CharSequence sequence) {
    int len = sequence.length();
    int first;
    for (first = 0; first < len; first++) {
      if (!matches(sequence.charAt(first))) {
        break;
      }
    }
    return sequence.subSequence(first, len).toString();
  }
  public String trimTrailingFrom(CharSequence sequence) {
    int len = sequence.length();
    int last;
    for (last = len - 1; last >= 0; last--) {
      if (!matches(sequence.charAt(last))) {
        break;
      }
    }
    return sequence.subSequence(0, last + 1).toString();
  }
  public String collapseFrom(CharSequence sequence, char replacement) {
    int first = indexIn(sequence);
    if (first == -1) {
      return sequence.toString();
    }
    StringBuilder builder = new StringBuilder(sequence.length())
        .append(sequence.subSequence(0, first))
        .append(replacement);
    boolean in = true;
    for (int i = first + 1; i < sequence.length(); i++) {
      char c = sequence.charAt(i);
      if (apply(c)) {
        if (!in) {
          builder.append(replacement);
          in = true;
        }
      } else {
        builder.append(c);
        in = false;
      }
    }
    return builder.toString();
  }
  public String trimAndCollapseFrom(CharSequence sequence, char replacement) {
    int first = negate().indexIn(sequence);
    if (first == -1) {
      return ""; 
    }
    StringBuilder builder = new StringBuilder(sequence.length());
    boolean inMatchingGroup = false;
    for (int i = first; i < sequence.length(); i++) {
      char c = sequence.charAt(i);
      if (apply(c)) {
        inMatchingGroup = true;
      } else {
        if (inMatchingGroup) {
          builder.append(replacement);
          inMatchingGroup = false;
        }
        builder.append(c);
      }
    }
    return builder.toString();
  }
   public boolean apply(Character character) {
    return matches(character);
  }
}
