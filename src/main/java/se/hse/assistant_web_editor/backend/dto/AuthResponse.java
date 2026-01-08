package se.hse.assistant_web_editor.backend.dto;

public record AuthResponse(boolean success, String message, String error) {

    public static AuthResponse success(String message) {
        return new AuthResponse(true, message, null);
    }

    public static AuthResponse error(String error) {
        return new AuthResponse(false, null, error);
    }
}
