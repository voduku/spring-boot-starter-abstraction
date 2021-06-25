package io.github.voduku.controller;

import feign.FeignException;
import io.github.voduku.model.RestResult;
import java.util.Objects;
import javax.persistence.NoResultException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * @author VuDo
 * @since 5/28/2021
 */
@Slf4j
public abstract class BaseController {

  @ExceptionHandler({IllegalArgumentException.class})
  public ResponseEntity<RestResult<Void>> handleInvalidRequestException(Exception exception) {
    log.error(exception.getMessage(), exception);
    return ResponseEntity.badRequest().body(RestResult.error(exception.getLocalizedMessage()));
  }

  @ExceptionHandler({MethodArgumentNotValidException.class})
  public ResponseEntity<RestResult<Void>> handleInvalidRequestException(MethodArgumentNotValidException exception) {
    log.error(exception.getMessage(), exception);
    return ResponseEntity.badRequest().body(RestResult.error(exception.getFieldErrors()));
  }

  @ExceptionHandler({AccessDeniedException.class})
  public ResponseEntity<RestResult<Void>> handleAccessDeniedException(AccessDeniedException exception) {
    log.error(exception.getMessage(), exception);
    return new ResponseEntity<>(RestResult.error(exception.getMessage()), HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler({NoResultException.class, EmptyResultDataAccessException.class})
  public ResponseEntity<RestResult<Void>> handleNoResultException(Exception exception) {
    String errorMessage = exception instanceof EmptyResultDataAccessException ? exception.getCause().getMessage() : exception.getMessage();
    if (!StringUtils.hasLength(errorMessage)) {
      errorMessage = "Can't find data with given ID";
    }
    log.error(errorMessage, exception);
    return new ResponseEntity<>(RestResult.error(errorMessage), HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler({FeignException.class})
  public ResponseEntity<RestResult<Void>> handleFeignException(FeignException exception) {
    log.error(exception.getMessage(), exception);
    return new ResponseEntity<>(RestResult.error(exception.getMessage()), Objects.requireNonNull(HttpStatus.resolve(exception.status())));
  }

  @ExceptionHandler({Exception.class})
  public ResponseEntity<RestResult<Void>> handleException(Exception exception) {
    log.error(exception.getMessage(), exception);
    return new ResponseEntity<>(RestResult.error(exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
