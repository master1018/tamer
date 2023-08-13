public class AddressOps {
  public static boolean lessThan(Address a1, Address a2) {
    if (a2 == null) {
      return false;
    } else if (a1 == null) {
      return true;
    } else {
      return a1.lessThan(a2);
    }
  }
  public static boolean lessThanOrEqual(Address a1, Address a2) {
    if (a2 == null) {
      return (a1 == null);
    } else if (a1 == null) {
      return true;
    } else {
      return a1.lessThanOrEqual(a2);
    }
  }
  public static boolean greaterThan(Address a1, Address a2) {
    if (a1 == null) {
      return false;
    } else if (a2 == null) {
      return true;
    } else {
      return a1.greaterThan(a2);
    }
  }
  public static boolean greaterThanOrEqual(Address a1, Address a2) {
    if (a1 == null) {
      return (a2 == null);
    } else if (a2 == null) {
      return true;
    } else {
      return a1.greaterThanOrEqual(a2);
    }
  }
  public static boolean equal(Address a1, Address a2) {
    if ((a1 == null) && (a2 == null)) {
      return true;
    }
    if ((a1 == null) || (a2 == null)) {
      return false;
    }
    return (a1.equals(a2));
  }
  public static boolean lt(Address a1, Address a2) {
    return lessThan(a1, a2);
  }
  public static boolean lte(Address a1, Address a2) {
    return lessThanOrEqual(a1, a2);
  }
  public static boolean gt(Address a1, Address a2) {
    return greaterThan(a1, a2);
  }
  public static boolean gte(Address a1, Address a2) {
    return greaterThanOrEqual(a1, a2);
  }
  public static Address max(Address a1, Address a2) {
    return (gt(a1, a2) ? a1 : a2);
  }
  public static Address min(Address a1, Address a2) {
    return (lt(a1, a2) ? a1 : a2);
  }
}
