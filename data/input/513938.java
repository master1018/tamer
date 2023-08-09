public final class CollationAttribute
{ 
  public static final int VALUE_DEFAULT = -1;
  public static final int VALUE_PRIMARY = 0;
  public static final int VALUE_SECONDARY = 1;
  public static final int VALUE_TERTIARY = 2;
  public static final int VALUE_DEFAULT_STRENGTH = VALUE_TERTIARY;
  public static final int VALUE_QUATERNARY = 3;
  public static final int VALUE_IDENTICAL = 15;
  public static final int VALUE_OFF = 16;
  public static final int VALUE_ON = 17;
  public static final int VALUE_SHIFTED = 20;
  public static final int VALUE_NON_IGNORABLE = 21;
  public static final int VALUE_LOWER_FIRST = 24;
  public static final int VALUE_UPPER_FIRST = 25;
  public static final int VALUE_ON_WITHOUT_HANGUL = 28;
  public static final int VALUE_ATTRIBUTE_VALUE_COUNT = 29;
  public static final int FRENCH_COLLATION = 0;
  public static final int ALTERNATE_HANDLING = 1;
  public static final int CASE_FIRST = 2;
  public static final int CASE_LEVEL = 3;
  public static final int NORMALIZATION_MODE = 4; 
  public static final int STRENGTH = 5;
  public static final int ATTRIBUTE_COUNT = 6;
  static boolean checkStrength(int strength)
  {
    if (strength < VALUE_PRIMARY || 
        (strength > VALUE_QUATERNARY && strength != VALUE_IDENTICAL))
      return false;
    return true;
  }
  static boolean checkType(int type)
  {
    if (type < FRENCH_COLLATION || type > STRENGTH)
      return false;
    return true;
  }
  static boolean checkNormalization(int type)
  {
    if (type != VALUE_ON && type != VALUE_OFF 
        && type != VALUE_ON_WITHOUT_HANGUL) {
        return false;
    }
    return true;
  }
  static boolean checkAttribute(int type, int value)
  {
    if (value == VALUE_DEFAULT) {
      return true;
    }
    switch (type)
    {
      case FRENCH_COLLATION :
                          if (value >= VALUE_OFF && value <= VALUE_ON)
                            return true;
                          break;
      case ALTERNATE_HANDLING :
                          if (value >= VALUE_SHIFTED && 
                              value <= VALUE_NON_IGNORABLE)
                            return true;
                          break;
      case CASE_FIRST :
                          if (value >= VALUE_LOWER_FIRST && 
                              value <= VALUE_UPPER_FIRST)
                            return true;
                          break;
      case CASE_LEVEL :
                          return (value == VALUE_ON || 
                                  value <= VALUE_OFF);
      case NORMALIZATION_MODE : 
                          return (value == VALUE_OFF || value == VALUE_ON ||
                                  value == VALUE_ON_WITHOUT_HANGUL);
      case STRENGTH :
                          checkStrength(value);
    }
    return false;
  }
}
