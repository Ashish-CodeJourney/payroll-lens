package com.acme.payrolllens.api;

import java.util.Map;

public record ApiError(String error, String message, Map<String, String> fieldErrors) {
}
