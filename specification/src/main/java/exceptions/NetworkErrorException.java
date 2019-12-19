package exceptions;

public class NetworkErrorException extends SuperException
{
    public NetworkErrorException() {}

    public NetworkErrorException(String message)
    {
        super(message);
    }
}
