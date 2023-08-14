public abstract class WordReader
{
    private static final char COMMENT_CHARACTER = '#';
    private File       baseDir;
    private WordReader includeWordReader;
    private String     currentLine;
    private int        currentLineLength;
    private int        currentIndex;
    private String     currentWord;
    private String     currentComments;
    protected WordReader(File baseDir)
    {
        this.baseDir = baseDir;
    }
    public void setBaseDir(File baseDir)
    {
        if (includeWordReader != null)
        {
            includeWordReader.setBaseDir(baseDir);
        }
        else
        {
            this.baseDir = baseDir;
        }
    }
    public File getBaseDir()
    {
        return includeWordReader != null ?
            includeWordReader.getBaseDir() :
            baseDir;
    }
    public void includeWordReader(WordReader newIncludeWordReader)
    {
        if (includeWordReader == null)
        {
            includeWordReader = newIncludeWordReader;
        }
        else
        {
            includeWordReader.includeWordReader(newIncludeWordReader);
        }
    }
    public String nextWord() throws IOException
    {
        currentWord = null;
        if (includeWordReader != null)
        {
            currentWord = includeWordReader.nextWord();
            if (currentWord != null)
            {
                return currentWord;
            }
            includeWordReader.close();
            includeWordReader = null;
        }
        while (currentLine != null &&
               currentIndex < currentLineLength &&
               Character.isWhitespace(currentLine.charAt(currentIndex)))
        {
            currentIndex++;
        }
        while (currentLine == null || currentIndex == currentLineLength)
        {
            currentLine = nextLine();
            if (currentLine == null)
            {
                return null;
            }
            int comments_start = currentLine.indexOf(COMMENT_CHARACTER);
            if (comments_start >= 0)
            {
                currentLineLength = comments_start;
                String comment = currentLine.substring(comments_start + 1);
                currentComments = currentComments == null ?
                    comment :
                    currentComments + '\n' + comment;
            }
            else
            {
                currentLineLength = currentLine.length();
            }
            currentIndex = 0;
            while (currentIndex < currentLineLength &&
                   Character.isWhitespace(currentLine.charAt(currentIndex)))
            {
                currentIndex++;
            }
        }
        int startIndex = currentIndex;
        int endIndex;
        char startChar = currentLine.charAt(startIndex);
        if (isDelimiter(startChar))
        {
            endIndex = ++currentIndex;
        }
        else if (isQuote(startChar))
        {
            startIndex++;
            do
            {
                currentIndex++;
                if (currentIndex == currentLineLength)
                {
                    currentWord = currentLine.substring(startIndex-1, currentIndex);
                    throw new IOException("Missing closing quote for "+locationDescription());
                }
            }
            while (currentLine.charAt(currentIndex) != startChar);
            endIndex = currentIndex++;
        }
        else
        {
            while (currentIndex < currentLineLength)
            {
                char currentCharacter = currentLine.charAt(currentIndex);
                if (isDelimiter(currentCharacter) ||
                    Character.isWhitespace(currentCharacter))
                {
                    break;
                }
                currentIndex++;
            }
            endIndex = currentIndex;
        }
        currentWord = currentLine.substring(startIndex, endIndex);
        return currentWord;
    }
    public String lastComments() throws IOException
    {
        if (includeWordReader == null)
        {
            String comments = currentComments;
            currentComments = null;
            return comments;
        }
        else
        {
            return includeWordReader.lastComments();
        }
    }
    public String locationDescription()
    {
        return
            (includeWordReader == null ?
                (currentWord == null ?
                    "end of " :
                    "'" + currentWord + "' in " ) :
                (includeWordReader.locationDescription() + ",\n" +
                 "  included from ")) +
            lineLocationDescription();
    }
    protected abstract String nextLine() throws IOException;
    protected abstract String lineLocationDescription();
    public void close() throws IOException
    {
        if (includeWordReader != null)
        {
            includeWordReader.close();
            includeWordReader = null;
        }
    }
    private boolean isDelimiter(char character)
    {
        return character == '@' ||
               character == '{' ||
               character == '}' ||
               character == '(' ||
               character == ')' ||
               character == ',' ||
               character == ';' ||
               character == File.pathSeparatorChar;
    }
    private boolean isQuote(char character)
    {
        return character == '\'' ||
               character == '"';
    }
}
