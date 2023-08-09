public class BreakIteratorInfo extends ListResourceBundle {
    protected final Object[][] getContents() {
        return new Object[][] {
            {"BreakIteratorClasses",
                new String[] {
                    "RuleBasedBreakIterator",  
                    "RuleBasedBreakIterator",  
                    "RuleBasedBreakIterator",  
                    "RuleBasedBreakIterator"   
                }
            },
            {"CharacterData", "CharacterBreakIteratorData"},
            {"WordData",      "WordBreakIteratorData"},
            {"LineData",      "LineBreakIteratorData"},
            {"SentenceData",  "SentenceBreakIteratorData"},
        };
    }
}
