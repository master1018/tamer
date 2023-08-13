final class emmarun
{
    public static void main (final String [] args)
        throws EMMARuntimeException
    {
        final Command command = Command.create ("run", emmarun.class.getName (), args);
        command.run ();
    }
} 
