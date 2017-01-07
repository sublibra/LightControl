package se.wtm.sublibra.lightControl;

/**
 * Wrapper class that serves as a union of a result value and an exception. When the download
 * task has completed, either the result value or exception can be a non-null value.
 * This allows you to pass exceptions to the UI thread that were thrown during doInBackground().
 */
public class NetworkResult {

    public String mResultValue;
    public Exception mException;

    public String getResultValue() {
        return mResultValue;
    }

    public void setResultValue(String mResultValue) {
        this.mResultValue = mResultValue;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Exception getException() {
        return mException;
    }

    public void setException(Exception mException) {
        this.mException = mException;
    }

    public String command;
    public NetworkResult(String command, String resultValue) {
        this.command = command;
        mResultValue = resultValue;
    }
    public NetworkResult(String command, Exception exception) {
        this.command = command;
        mException = exception;
    }
}

