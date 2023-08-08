class GotoStatement extends Statement {
    public Status compile(final Tokenizer tokens) {
        if (program == null) return new Status(Status.NOACTIVEPGM);
        byteCode = new ByteCode(session, this);
        if (tokens.assumeNextToken("USING")) {
            final Expression exp = new Expression(session);
            if (!tokens.assumeNextToken("(")) return new Status(Status.INVUSING);
            exp.compile(byteCode, tokens);
            if (exp.status.failed()) return exp.status;
            if (!tokens.assumeNextToken(")")) return new Status(Status.INVUSING);
            byteCode.add(ByteCode._JMPIND);
            return new Status(Status.SUCCESS);
        }
        if (tokens.testNextToken(Tokenizer.INTEGER)) {
            int lnum = Integer.parseInt(tokens.nextToken());
            try {
                addLineNumberPosition(tokens.getPosition() - 1);
            } catch (JBasicException e) {
                return e.getStatus();
            }
            byteCode.add(ByteCode._GOTO, lnum);
            return new Status();
        }
        if (tokens.testNextToken(Tokenizer.IDENTIFIER)) {
            final String label = tokens.nextToken();
            byteCode.add(ByteCode._JMP, label);
            return new Status(Status.SUCCESS);
        }
        return new Status(Status.NOSUCHLABEL, tokens.nextToken());
    }
}
