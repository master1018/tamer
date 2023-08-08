public class CSVParser {
    public static ArrayList parseCSVFile(String fileName) throws IOException, FileNotFoundException {
        String lineRead = null;
        StringBuffer buffer = new StringBuffer("");
        FileInputStream fis = null;
        try {
            FileReader reader = new FileReader(fileName);
            BufferedReader buffReader = new BufferedReader(reader);
            while ((lineRead = buffReader.readLine()) != null) {
                buffer.append(lineRead).append("\n");
            }
        } finally {
            try {
                if (fis != null) fis.close();
            } catch (Exception ex) {
            }
        }
        return parseCSV(buffer.toString());
    }
    public static ArrayList parseCSV(String csvString) {
        String[] listOfRows = null;
        String[] rowValues = null;
        String[] valueNames = null;
        int numOfRows = 0;
        int numOfCols = 0;
        int count = 0;
        int innerCount = 0;
        int columnCount = 0;
        HashMap rowValueMap = null;
        ArrayList listOfMaps = null;
        listOfRows = splitString(csvString, "\n");
        valueNames = getArrayOfRows(listOfRows[0]);
        numOfCols = valueNames.length;
        numOfRows = listOfRows.length;
        listOfMaps = new ArrayList();
        for (count = 1; count < numOfRows; count++) {
            rowValues = getArrayOfRows(listOfRows[count]);
            columnCount = rowValues.length;
            rowValueMap = new HashMap();
            for (innerCount = 0; innerCount < columnCount; innerCount++) {
                rowValueMap.put(valueNames[innerCount], rowValues[innerCount]);
            }
            listOfMaps.add(rowValueMap);
        }
        return listOfMaps;
    }
    private static String[] getArrayOfRows(String inputString) {
        final String CSV_DELIMITER = "~";
        int inputLen = 0;
        int numOfCols = 0;
        int count = 0;
        boolean isQuoteCountEven = true;
        char parsedChar = '\0';
        String parseableString = null;
        StringBuffer outBuffer = null;
        String csvValues[] = null;
        inputLen = inputString.length();
        outBuffer = new StringBuffer("");
        for (count = 0; count < inputLen; count++) {
            parsedChar = inputString.charAt(count);
            if ((parsedChar == ',') && isQuoteCountEven) {
                outBuffer.append(CSV_DELIMITER);
            } else if (parsedChar == '\"') {
                isQuoteCountEven = !isQuoteCountEven;
                outBuffer.append(parsedChar);
            } else {
                outBuffer.append(parsedChar);
            }
        }
        parseableString = outBuffer.toString();
        if (parseableString.charAt(0) == '~') {
            parseableString = " " + parseableString;
        }
        if (parseableString.charAt(parseableString.length() - 1) == '~') {
            parseableString = parseableString + " ";
        }
        parseableString = replaceTag(parseableString, "~~", "~ ~");
        parseableString = replaceTag(parseableString, "~~", "~ ~");
        csvValues = splitString(parseableString, CSV_DELIMITER);
        numOfCols = csvValues.length;
        for (count = 0; count < numOfCols; count++) {
            csvValues[count] = removeCSVFormatting(csvValues[count]);
        }
        return csvValues;
    }
    private static String removeCSVFormatting(String untrimmedString) {
        String strTrimmed = null;
        strTrimmed = replaceTag(untrimmedString, "\"\"", "\"");
        strTrimmed = strTrimmed.trim();
        if ((strTrimmed.length() != 0) && (strTrimmed.charAt(0) == '\"') && (strTrimmed.charAt(strTrimmed.length() - 1) == '\"')) {
            strTrimmed = strTrimmed.substring(1, strTrimmed.length() - 1);
        }
        return strTrimmed;
    }
    private static String[] splitString(String str, String delim) {
        ArrayList list = new ArrayList();
        StringTokenizer tokenizer = new StringTokenizer(str, delim);
        while (tokenizer.hasMoreTokens()) {
            list.add(tokenizer.nextToken());
        }
        String[] ret = new String[list.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = (String) list.get(i);
        }
        return ret;
    }
    private static String replaceTag(String container, String tag, String replacement) {
        final String BLANK = "";
        StringBuffer buffer = new StringBuffer("");
        int start = 0;
        int end = 0;
        if (container == null || container.trim().length() == 0) {
            return container;
        }
        if (replacement == null) {
            replacement = BLANK;
        }
        end = container.indexOf(tag);
        while (end != -1) {
            buffer.append(container.substring(start, end));
            buffer.append(replacement);
            start = end + tag.length();
            end = container.indexOf(tag, start);
        }
        buffer.append(container.substring(start));
        return buffer.toString();
    }
}
