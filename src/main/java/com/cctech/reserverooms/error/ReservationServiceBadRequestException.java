package com.cctech.reserverooms.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ReservationServiceBadRequestException extends RuntimeException {
    public ReservationServiceBadRequestException(String s) {
        super(s);
    }
}
