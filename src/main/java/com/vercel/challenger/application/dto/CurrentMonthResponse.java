package com.vercel.challenger.application.dto;

import java.time.LocalDateTime;

public record CurrentMonthResponse(
        int year,
        int month
) {
    public static CurrentMonthResponse from(LocalDateTime localDateTime) {
        return new CurrentMonthResponse(
                localDateTime.getYear(),
                localDateTime.getMonthValue()
        );
    }
}
