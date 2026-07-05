package com.sprintfries.api.exception;

import lombok.Getter;

@Getter
public class AccountLockedException extends RuntimeException {
    private final long remainingMinutes;

    public AccountLockedException(String message, long remainingMinutes) {
        super(message);
        this.remainingMinutes = remainingMinutes;
    }
}
