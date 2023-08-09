public class ModifierAbstract {
  public static boolean start(RootDoc root) throws Exception {
    ClassDoc[] classarr = root.classes();
    for (int i = 0; i < classarr.length; i++) {
        if (classarr[i].isInterface()) {
            String modifier = classarr[i].modifiers();
            if (modifier.indexOf("abstract") > 0) {
                throw new Exception("Keyword `abstract' found in the " +
                                    "modifier string for class " +
                                    classarr[i].qualifiedName());
            }
        }
    }
    return true;
  }
}
