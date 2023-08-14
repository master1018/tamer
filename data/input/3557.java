            "class",    "finally",      "long",         "strictfp",     "volatile",
            "const",    "float",        "native",       "super",        "while",
            "null",     "true",         "false"
        };
        for(String kw : kws)
            s.add(kw);
        keywords = Collections.unmodifiableSet(s);
    }
    public static boolean isKeyword(CharSequence s) {
        String keywordOrLiteral = s.toString();
        return keywords.contains(keywordOrLiteral);
    }
}
