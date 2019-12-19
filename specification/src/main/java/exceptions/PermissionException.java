package exceptions;

public class PermissionException extends SuperException
{
    public PermissionException() {}

    public PermissionException(String message)
    {
        super(message);
    }
}
