package exceptions;

public class UserException extends SuperException
{
    public UserException() {}

    public UserException(String message)
    {
        super(message);
    }
}
