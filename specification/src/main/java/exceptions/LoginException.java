package exceptions;

public class LoginException extends SuperException
{
    public LoginException() {}

    public LoginException(String message)
    {
        super(message);
    }
}
