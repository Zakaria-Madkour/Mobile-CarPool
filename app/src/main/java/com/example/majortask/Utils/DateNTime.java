package com.example.majortask.Utils;

import static android.service.controls.ControlsProviderService.TAG;

import android.util.Log;
import android.widget.Toast;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;

public class DateNTime {
    public static LocalDateTime parseDateTime(String date, String time) {
        String dateTimeString = date + "T" + time;
        // Define multiple date-time format patterns
        String[] patterns = {"dd-MM-yyyy'T'HH:mm", "dd-MM-yyyy'T'H:mm", "dd-MM-yyyy'T'HH:m", "dd-MM-yyyy'T'H:m",
                "d-MM-yyyy'T'HH:mm", "d-MM-yyyy'T'H:mm", "d-MM-yyyy'T'HH:m", "d-MM-yyyy'T'H:m",
                "dd-M-yyyy'T'HH:mm", "dd-M-yyyy'T'H:mm", "dd-M-yyyy'T'HH:m", "dd-M-yyyy'T'H:m",
                "d-M-yyyy'T'HH:mm", "d-M-yyyy'T'H:mm", "d-M-yyyy'T'HH:m", "d-M-yyyy'T'H:m",};

        // Create a DateTimeFormatterBuilder and add the patterns
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        for (String pattern : patterns) {
            builder.appendOptional(DateTimeFormatter.ofPattern(pattern).withResolverStyle(ResolverStyle.STRICT));
        }
        // Create the DateTimeFormatter with the combined patterns
        DateTimeFormatter formatter = builder.toFormatter(Locale.ENGLISH);

        // Parse the string into a LocalDateTime object using the formatter
        try {
            LocalDateTime parsedDateTime = LocalDateTime.parse(dateTimeString, formatter);
            return parsedDateTime;

        } catch (DateTimeParseException e) {
            Log.e(TAG, "Error: Invalid date-time format or value" + e.toString());
        }
        return null;
    }

    public static boolean isPast(String day, String time) {
        LocalDateTime timeGiven = DateNTime.parseDateTime(day, time);
        LocalDateTime currentDateTime = LocalDateTime.now();
        return timeGiven.isBefore(currentDateTime);
    }

    public static double timeToRide(String day, String time) {
        LocalDateTime timeGiven = DateNTime.parseDateTime(day, time);
        LocalDateTime currentDateTime = LocalDateTime.now();
        // Calculate the difference using Duration.between
        Duration duration = Duration.between(currentDateTime, timeGiven);
        // Get the difference in hours and minutes
        double hoursDifference = duration.toHours();
        long minutesDifference = duration.toMinutes() % 60;
        hoursDifference += minutesDifference / 60.0;
        return hoursDifference;
    }
}
