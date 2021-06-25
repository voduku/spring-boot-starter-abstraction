package io.github.voduku.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.FieldError;

/**
 * Wrapper for a rest response
 *
 * @author VuDo
 * @since 1.0.0
 */
@Data
@Builder(toBuilder = true)
public class RestResult<T> {

  public static final String STATUS_SUCCESS = "success";
  public static final String STATUS_ERROR = "error";

  String status;

  Collection<String> messages;

  String message;

  T data;

  public static RestResult<Void> ok() {
    return new RestResultBuilder<Void>().status(STATUS_SUCCESS).build();
  }

  public static <T> RestResult<T> ok(T data) {
    return new RestResultBuilder<T>().status(STATUS_SUCCESS).data(data).build();
  }

  public static <T> RestResult<T> ok(String message) {
    return new RestResultBuilder<T>().status(STATUS_SUCCESS).message(message).build();
  }

  public static <T> RestResult<T> ok(T data, String message) {
    return new RestResultBuilder<T>().status(STATUS_SUCCESS).data(data).message(message).build();
  }

  public static RestResult<Void> error() {
    return new RestResultBuilder<Void>().status(RestResult.STATUS_ERROR).build();
  }

  public static <T> RestResult<T> error(T data, String message) {
    return new RestResultBuilder<T>().status(RestResult.STATUS_ERROR).data(data).message(message).build();
  }

  public static RestResult<Void> error(String message) {
    return new RestResultBuilder<Void>().status(RestResult.STATUS_ERROR).message(message).build();
  }

  public static RestResult<Void> error(Collection<FieldError> errors) {
    MultiValueMap<String, String> errorMessages = new LinkedMultiValueMap<>();
    errors.forEach(error -> errorMessages.add(error.getField(), error.getDefaultMessage()));
    List<String> messages = new ArrayList<>();
    errorMessages.forEach((field, msg) -> messages.add(field + " " + String.join(" or ", msg)));
    return new RestResultBuilder<Void>().status(RestResult.STATUS_ERROR)
        .message(String.join("\r\n", messages))
        .messages(messages)
        .build();
  }
}
