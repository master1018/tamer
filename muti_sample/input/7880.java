public class PrintDefaultLocale {
    public static void main(String[] args) {
        System.out.printf("default locale: ID: %s, Name: %s\n",
            Locale.getDefault().toString(),
            Locale.getDefault().getDisplayName(Locale.US));
        System.out.printf("display locale: ID: %s, Name: %s\n",
            Locale.getDefault(Locale.Category.DISPLAY).toString(),
            Locale.getDefault(Locale.Category.DISPLAY).getDisplayName(Locale.US));
        System.out.printf("format locale: ID: %s, Name: %s\n",
            Locale.getDefault(Locale.Category.FORMAT).toString(),
            Locale.getDefault(Locale.Category.FORMAT).getDisplayName(Locale.US));
        System.out.printf("default charset: %s\n", Charset.defaultCharset());
    }
}
