package exceptions;

public class TerminalErrorException extends SuperException
{
    public TerminalErrorException() {}

    public TerminalErrorException(String message)
    {
        super(message);
    }
}
