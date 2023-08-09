public class CommandParserTest extends CtsTestBase {
    public void testParseSimpleCommand()
                throws UnknownCommandException, CommandNotFoundException {
        final String action = "start";
        final String option = "--plan";
        final String value = "test_plan";
        CommandParser cp;
        cp = CommandParser.parse(action + " " + option + " " + value);
        assertEquals(1, cp.getOptionSize());
        assertEquals(action, cp.getAction());
        assertTrue(cp.containsKey(CTSCommand.OPTION_PLAN));
        assertEquals(value, cp.getValue(CTSCommand.OPTION_PLAN));
    }
    public void testParseMultiOptionsCommand()
                throws UnknownCommandException, CommandNotFoundException {
        final String action = "start";
        final String option1 = "--plan";
        final String value1 = "test_plan";
        final String option2 = "-d";
        final String value2 = "0";
        final String unexistOption = "unexist";
        CommandParser cp;
        cp = CommandParser.parse(action + " " + option1 + " " + value1
                + " " + option2 + " " + value2);
        assertEquals(2, cp.getOptionSize());
        assertEquals(action, cp.getAction());
        assertTrue(cp.containsKey(CTSCommand.OPTION_PLAN));
        assertEquals(value1, cp.getValue(CTSCommand.OPTION_PLAN));
        assertTrue(cp.containsKey(CTSCommand.OPTION_DEVICE));
        assertEquals(value2, cp.getValue(CTSCommand.OPTION_DEVICE));
        assertFalse(cp.containsKey(unexistOption));
    }
    public void testParseSameOptionCommand() throws CommandNotFoundException{
        final String action = "ls";
        final String option1 = "-d";
        final String value1 = "test_plan";
        final String option2 = "-d";
        final String value2 = "0";
        try {
            CommandParser.parse(action + " " + option1 + " "
                    + value1 + " " + option2 + " " + value2);
            fail("no exception");
        } catch (UnknownCommandException e) {
        }
    }
    public void testParseNoValueForOptionCommand()
                throws UnknownCommandException, CommandNotFoundException {
        final String action = "ls";
        final String option1 = "-d";
        CommandParser cp;
        cp = CommandParser.parse(action + " " + option1);
        assertEquals(1, cp.getOptionSize());
        assertEquals(action, cp.getAction());
        assertTrue(cp.containsKey(CTSCommand.OPTION_DEVICE));
        assertEquals("", cp.getValue(CTSCommand.OPTION_DEVICE));
    }
    public void testParseIllOptionCommand()
                throws UnknownCommandException, CommandNotFoundException {
        final String action = "ls";
        final String actionValue = "devices";
        CommandParser cp = CommandParser.parse(action + " " + actionValue);
        assertEquals(action, cp.getAction());
        ArrayList<String> actionValues = cp.getActionValues();
        assertEquals(1, actionValues.size());
        assertTrue(actionValues.contains(actionValue));
    }
    public void testParseMultiIllOptionCommand() throws CommandNotFoundException {
        final String action = "ls";
        final String option1 = "-devices";
        final String value1 = "v1";
        final String option2 = "op2";
        final String value2 = "v2";
        try {
            CommandParser.parse(action + " " + option1 + " " + value1 + " "
                    + option2 + " " + value2);
            fail("no exception");
        } catch (UnknownCommandException e) {
        }
    }
    public void testGetOptions()
                throws UnknownCommandException, CommandNotFoundException {
        final String action = "ls";
        final String option1 = "-d";
        final String value1 = "v1";
        final String option2 = "--plan";
        final String value2 = "v2";
        CommandParser cp;
        cp = CommandParser.parse(action + " " + option1 + " " + value1
                + " " + option2 + " " + value2);
        assertEquals(2, cp.getOptionSize());
        Set<String> set = cp.getOptionKeys();
        assertEquals(2, set.size());
        assertTrue(set.contains(CTSCommand.OPTION_DEVICE));
        assertTrue(set.contains(CTSCommand.OPTION_PLAN));
    }
    public void testParseEmptyCommand() throws UnknownCommandException {
        try {
            CommandParser.parse("");
            CommandParser.parse("             ");
            fail("should throw out exception");
        } catch (CommandNotFoundException e) {
        }
    }
    public void testParseSingleCommand()
                throws UnknownCommandException, CommandNotFoundException {
        final String action = "exit";
        CommandParser cp;
        cp = CommandParser.parse(action);
        assertEquals(action, cp.getAction());
        assertEquals(0, cp.getOptionSize());
        assertEquals(0, cp.getOptionKeys().size());
    }
    public void testParseNumberOption()
                throws UnknownCommandException, CommandNotFoundException {
        final String action = "h";
        final String actionValue = "1234";
        CommandParser cp;
        cp = CommandParser.parse(action + " " + actionValue);
        assertEquals(action, cp.getAction());
        ArrayList<String> actionValues = cp.getActionValues();
        assertEquals(1, actionValues.size());
        assertTrue(actionValues.contains(actionValue));
        assertEquals(0, cp.getOptionSize());
        Set<String> set = cp.getOptionKeys();
        assertEquals(0, set.size());
    }
    public void testParseValueNegative()
                throws UnknownCommandException, CommandNotFoundException {
        final String action = "ls";
        final String resultOption = "-r";
        final String resultValue = "-13";
        CommandParser cp;
        cp = CommandParser.parse(action + " " + resultOption + " "
                + resultValue);
        assertEquals(action, cp.getAction());
        assertEquals(1, cp.getOptionSize());
        assertTrue(cp.containsKey(CTSCommand.OPTION_RESULT));
        assertEquals(resultValue, cp.getValue(CTSCommand.OPTION_RESULT));
    }
    public void testParseCapitalLetter()
                throws UnknownCommandException, CommandNotFoundException {
        final String action = "LS";
        final String resultOption = "-R";
        final String resultValue = "-13";
        CommandParser cp;
        cp = CommandParser.parse(action + " " + resultOption + " "
                + resultValue);
        assertEquals(action.toLowerCase(), cp.getAction());
        assertEquals(1, cp.getOptionSize());
        assertTrue(cp.containsKey(CTSCommand.OPTION_RESULT));
        assertEquals(resultValue, cp.getValue(CTSCommand.OPTION_RESULT));
    }
    public void testParseActionValue()
                throws UnknownCommandException, CommandNotFoundException {
        final String action = "h";
        final String actionValue1 = "192";
        final String actionValue2 = "e";
        CommandParser cp = CommandParser.parse(action + " " + actionValue1 + " "
                + actionValue2);
        assertEquals(action, cp.getAction());
        assertEquals(0, cp.getOptionKeys().size());
        ArrayList<String> actionValues = cp.getActionValues();
        assertEquals(2, actionValues.size());
        assertTrue(actionValues.contains(actionValue1));
        assertTrue(actionValues.contains(actionValue2));
    }
    public void testParseListResultCmd() throws UnknownCommandException,
            CommandNotFoundException {
        final String action = "ls";
        final String resultOpt = "-r";
        final String resultValue = "pass";
        final String sessionOpt = "-s";
        final String sessionOptComplete = "--session";
        final String sessionId = "1";
        String cmdStr;
        CommandParser cp;
        cmdStr = action + " " + resultOpt + " " + sessionOpt + " " + sessionId;
        cp = CommandParser.parse(cmdStr);
        assertEquals(action, cp.getAction());
        assertEquals(2, cp.getOptionSize());
        assertEquals("", cp.getValue(CTSCommand.OPTION_RESULT));
        assertEquals(sessionId, cp.getValue(CTSCommand.OPTION_SESSION));
        cmdStr = action + " " + resultOpt + " " + sessionOptComplete + " "
                + sessionId;
        cp = CommandParser.parse(cmdStr);
        assertEquals(action, cp.getAction());
        assertEquals(2, cp.getOptionSize());
        assertEquals("", cp.getValue(CTSCommand.OPTION_RESULT));
        assertEquals(sessionId, cp.getValue(CTSCommand.OPTION_SESSION));
        cmdStr = action + " " + resultOpt + " " + resultValue + " " + sessionOptComplete + " "
                + sessionId;
        cp = CommandParser.parse(cmdStr);
        assertEquals(action, cp.getAction());
        assertEquals(2, cp.getOptionSize());
        assertEquals(resultValue, cp.getValue(CTSCommand.OPTION_RESULT));
        assertEquals(sessionId, cp.getValue(CTSCommand.OPTION_SESSION));
    }
}
