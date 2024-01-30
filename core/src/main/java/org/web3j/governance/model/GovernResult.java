package org.web3j.governance.model;

public class GovernResult<T> {
    private T data;
    private String errorMessage;
    private Boolean success;

    public GovernResult(T data, String errorMessage, Boolean success) {
        this.data = data;
        this.errorMessage = errorMessage;
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public static <T> GovernResult<T> success(T data) {
        return new GovernResult<>(data, null, true);
    }

    public static <T> GovernResult<T> failure(String errorMessage) {
        return new GovernResult<>(null, errorMessage, false);
    }
}
