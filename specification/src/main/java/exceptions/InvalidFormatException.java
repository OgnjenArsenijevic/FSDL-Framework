package exceptions;

public class InvalidFormatException extends SuperException
{
    public InvalidFormatException() {}

    public InvalidFormatException(String message)
    {
        super(message);
    }
}
