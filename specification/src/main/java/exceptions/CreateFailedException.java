package exceptions;

public class CreateFailedException extends SuperException
{
    public CreateFailedException() {}

    public CreateFailedException(String message)
    {
        super(message);
    }
}
