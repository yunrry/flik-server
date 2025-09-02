package yunrry.flik.adapters.in.dto;

public record Response<T>(boolean success, T data, String message) {

    public static <T> Response<T> success(T data) {
        return new Response<>(true, data, null);
    }

    public static <T> Response<T> error(String message) {
        return new Response<>(false, null, message);
    }
}