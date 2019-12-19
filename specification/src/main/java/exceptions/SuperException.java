package exceptions;

public abstract class SuperException extends Exception
{
    public SuperException() {}

    public SuperException(String message)
    {
        super(message);
    }
}
