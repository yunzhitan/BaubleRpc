package top.yunzhitan.transport;

public class ConnectFailedException extends RuntimeException{

    private static final long serialVersionUID = -2138246764343543L;

    public ConnectFailedException() {
        super();
    }

    public ConnectFailedException(String message) {
        super(message);
    }

    public ConnectFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConnectFailedException(Throwable cause) {
        super(cause);
    }
}
