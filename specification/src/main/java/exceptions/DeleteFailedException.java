package exceptions;

public class DeleteFailedException extends SuperException
{
    public DeleteFailedException() {}

    public DeleteFailedException(String message)
    {
        super(message);
    }
}
