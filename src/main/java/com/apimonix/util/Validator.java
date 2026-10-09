package com.apimonix.util;

import com.apimonix.exception.ValidationException;

import java.net.URI;
import java.net.URISyntaxException;

public class Validator {
    public static void validateEndpointName(String name){
        if(name==null || name.isBlank()){
            throw new ValidationException("Endpoint name cannot be empty");
        }

        String trimmed = name.trim();
        if(trimmed.length()<3){
            throw new ValidationException(
                    "Endpoint name must be at least 3 characters"
            );
        }

        if (trimmed.length() > 100) {
            throw new ValidationException(
                    "Endpoint name cannot exceed 100 characters");
        }

        if (!trimmed.matches("^[a-zA-Z0-9 _\\-\\.]+$")) {
            throw new ValidationException(
                    "Endpoint name can only contain letters, numbers, spaces, hyphens, underscores and dots");
        }
    }

    public static void validateUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new ValidationException("URL cannot be empty");
        }

        if (url.length() > 2048) {
            throw new ValidationException(
                    "URL cannot exceed 2048 characters");
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new ValidationException(
                    "URL must start with http:// or https://");
        }

        try {
            URI uri = new URI(url);
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw new ValidationException(
                        "URL must contain a valid host");
            }
        } catch (URISyntaxException e) {
            throw new ValidationException(
                    "Invalid URL format: " + e.getMessage());
        }
    }

    public static void validateUuid(String id, String fieldName) {
        if (id == null || id.isBlank()) {
            throw new ValidationException(fieldName + " cannot be empty");
        }

        try {
            java.util.UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new ValidationException(
                    fieldName + " is not a valid ID format");
        }
    }
}
