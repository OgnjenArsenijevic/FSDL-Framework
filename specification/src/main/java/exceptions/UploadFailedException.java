package exceptions;

public class UploadFailedException extends SuperException
{
    public UploadFailedException() {}

    public UploadFailedException(String message)
    {
        super(message);
    }
}
