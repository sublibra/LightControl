package se.wtm.sublibra.lightControl;

/**
 * Stores executed command, result and a potential exception if something in the transfer
 * didn't go as intended.
 */
public class ServerRequest{

    public static final int LIST = 0;
    public static final int TOGGLE = 1;
    public static final int DIM = 2;

    public String mResultValue;
    public Exception mException;
    public int command;
    public String requestURL;

    public String getResultValue() {
        return mResultValue;
    }

    public void setResultValue(String mResultValue) {
        this.mResultValue = mResultValue;
    }

    public String getRequestURL() {
        return requestURL;
    }

    public void setRequestURL(String requestURL) {
        this.requestURL = requestURL;
    }

    public Exception getException() {
        return mException;
    }

    public void setException(Exception mException) {
        this.mException = mException;
    }

    public int getCommand() {
        return command;
    }

    public void setCommand(int command) {
        this.command = command;
    }

    public ServerRequest(int command, String requestURL){
        this.command = command;
        this.requestURL = requestURL;
    }

    public ServerRequest(int command, String requestURL, String resultValue) {
        this.command = command;
        this.requestURL = requestURL;
        mResultValue = resultValue;
    }

    public ServerRequest(int command, String requestURL, Exception exception) {
        this.command = command;
        this.requestURL = requestURL;
        mException = exception;
    }
}

