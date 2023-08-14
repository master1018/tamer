public class CheckProp {
    public static void main(String[] args) throws IOException {
        File fPropList = new File(System.getProperty("test.src", "."), "PropList.txt");
        int i, j;
        BufferedReader sbfr = new BufferedReader(new FileReader(fPropList));
        Matcher m = Pattern.compile("(\\p{XDigit}+)(?:\\.{2}(\\p{XDigit}+))?\\s*;\\s+(\\w+)\\s+#.*").matcher("");
        Map<String, ArrayList<Integer>> propMap =  new LinkedHashMap<>();
        String line = null;
        int lineNo = 0;
        while ((line = sbfr.readLine()) != null) {
            lineNo++;
            if (line.length() <= 1 || line.charAt(0) == '#') {
                continue;
            }
            m.reset(line);
            if (m.matches()) {
                int start = Integer.parseInt(m.group(1), 16);
                int end = (m.group(2)==null)?start
                          :Integer.parseInt(m.group(2), 16);
                String name = m.group(3);
                ArrayList<Integer> list = propMap.get(name);
                if (list == null) {
                    list = new ArrayList<Integer>();
                    propMap.put(name, list);
                }
                while (start <= end)
                    list.add(start++);
            } else {
                System.out.printf("Warning: Unrecognized line %d <%s>%n", lineNo, line);
            }
        }
        sbfr.close();
        Integer[] otherLowercase = propMap.get("Other_Lowercase").toArray(new Integer[0]);
        Integer[] otherUppercase = propMap.get("Other_Uppercase").toArray(new Integer[0]);
        Integer[] otherAlphabetic = propMap.get("Other_Alphabetic").toArray(new Integer[0]);
        Integer[] ideographic = propMap.get("Ideographic").toArray(new Integer[0]);
        int fails = 0;
        for (int cp = MIN_CODE_POINT; cp < MAX_CODE_POINT; cp++) {
            int type = getType(cp);
            if (isLowerCase(cp) !=
                (type == LOWERCASE_LETTER ||
                 Arrays.binarySearch(otherLowercase, cp) >= 0))
            {
                fails++;
                System.err.printf("Wrong isLowerCase(U+%04x)\n", cp);
            }
            if (isUpperCase(cp) !=
                (type == UPPERCASE_LETTER ||
                 Arrays.binarySearch(otherUppercase, cp) >= 0))
            {
                fails++;
                System.err.printf("Wrong isUpperCase(U+%04x)\n", cp);
            }
            if (isAlphabetic(cp) !=
                (type == UPPERCASE_LETTER || type == LOWERCASE_LETTER ||
                 type == TITLECASE_LETTER || type == MODIFIER_LETTER  ||
                 type == OTHER_LETTER     || type == OTHER_LETTER ||
                 type == LETTER_NUMBER ||
                 Arrays.binarySearch(otherAlphabetic, cp) >=0))
            {
                fails++;
                System.err.printf("Wrong isAlphabetic(U+%04x)\n", cp);
            }
            if (isIdeographic(cp) !=
                (Arrays.binarySearch(ideographic, cp) >= 0))
            {
                fails++;
                System.err.printf("Wrong isIdeographic(U+%04x)\n", cp);
            }
        }
        if (fails != 0)
            throw new RuntimeException("CheckProp failed=" + fails);
    }
}
