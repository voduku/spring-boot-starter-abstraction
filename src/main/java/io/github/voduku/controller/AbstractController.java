package io.github.voduku.controller;

import io.github.voduku.model.RestResult;
import io.github.voduku.service.Service;
import io.github.voduku.model.AbstractSearch;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.io.Serializable;
import java.nio.file.AccessDeniedException;
import javax.persistence.NoResultException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Provide basic CRUD APIs for any subclasses. Check Swagger for API details
 * <br>A controller needs a {@link Service} in order to work. Make sure you create a corresponding service.
 * @see <a href="http://locahost:8080/swagger-ui/index.html">SWAGGER</a>
 * @author VuDo
 * @since 1.0.0
 */
@Slf4j
@Getter
@Setter
public class AbstractController<REQUEST, RESPONSE, S extends AbstractSearch<?>, KEY extends Serializable> {

  protected static final String CUSTOM = "/custom";

  protected static final String SLICE = "/slice";

  protected static final String PAGE = "/page";

  @Autowired
  protected Service<REQUEST, RESPONSE, KEY> service;

  @GetMapping
  @Operation(description = "Get data by ID. All parameters are required")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Get data success"),
      @ApiResponse(responseCode = "204", description = "Performing get single data request but the data with associated key doesn't exist"),
      @ApiResponse(responseCode = "400", description = "Bad request. Check errors return in property 'messages'"),
      @ApiResponse(responseCode = "401", description = "Either request needs bearer or profile doesn't have permission or profile doesn't own the data"),
      @ApiResponse(responseCode = "403", description = "Either request needs bearer or profile doesn't have permission"),
      @ApiResponse(responseCode = "404", description = "Either your path is wrong or there is no data for the given ID"),
      @ApiResponse(responseCode = "500", description = "This happens when there is something wrong with the server. Ex: Database connection failed, Micro-services communication failed, etc.")
  })
  public ResponseEntity<RestResult<RESPONSE>> get(@ParameterObject @NotNull @Valid KEY id) {
    return ResponseEntity.ok(RestResult.ok(service.get(id), "Get data success"));
  }

  @GetMapping(CUSTOM)
  @Operation(description = "Get data by ID. Response body properties are excludable to reduce response foot print")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Get data success"),
      @ApiResponse(responseCode = "204", description = "Performing get single data request but the data with associated key doesn't exist"),
      @ApiResponse(responseCode = "400", description = "Bad request. Check errors return in property 'messages'"),
      @ApiResponse(responseCode = "401", description = "Either request needs bearer or profile doesn't have permission or profile doesn't own the data"),
      @ApiResponse(responseCode = "403", description = "Either request needs bearer or profile doesn't have permission"),
      @ApiResponse(responseCode = "404", description = "Either your path is wrong or there is no data for the given ID"),
      @ApiResponse(responseCode = "500", description = "This happens when there is something wrong with the server. Ex: Database connection failed, Micro-services communication failed, etc.")
  })
  public ResponseEntity<RestResult<RESPONSE>> getCustom(@ParameterObject @NotNull @Valid KEY id, @ParameterObject @Valid S params) {
    return ResponseEntity.ok(RestResult.ok(service.get(id, params), "Get data success"));
  }

  @GetMapping(SLICE)
  @Operation(description = "Get a slice of data. Individual response body properties are excludable to reduce response foot print.<br>"
      + "A Slice doesn't knows about the total number of elements and pages available because it doesn't trigger a count query to calculate the overall number.<br>"
      + "It knows only about whether a next Slice is available, which might be sufficient when walking through a larger result set.<br>"
      + "See details at <a href='https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.special-parameters'>Spring Documentation Reference</a>.<br>"
      + "<b>NOTES:</b> You can also exclude all metadata properties by setting <b><i>excludeMetadata</i></b> to <b><i>true</i></b> or add <b><i>?excludeMetadata=1</i></b> in HTML query param(s)")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Get data success"),
      @ApiResponse(responseCode = "400", description = "Bad request. Check errors return in property 'messages'"),
      @ApiResponse(responseCode = "401", description = "Either request needs bearer or profile doesn't have permission or profile doesn't own the data"),
      @ApiResponse(responseCode = "403", description = "Either request needs bearer or profile doesn't have permission"),
      @ApiResponse(responseCode = "404", description = "Won't happen unless your path is wrong"),
      @ApiResponse(responseCode = "500", description = "This happens when there is something wrong with the server. Ex: Database connection failed, Micro-services communication failed, etc.")
  })
  public ResponseEntity<RestResult<Slice<RESPONSE>>> getSlice(@ParameterObject @Valid S params, @ParameterObject Pageable pageable) {
    return ResponseEntity.ok(RestResult.ok(service.search(params, pageable), "Get data success"));
  }

  @GetMapping(PAGE)
  @Operation(description = "Get a slice of data. Individual response body properties are excludable to reduce response foot print.<br>"
      + "A Page knows about the total number of elements and pages available.<br>"
      + "It does so by the infrastructure triggering a count query to calculate the overall number.<br>"
      + "See details at <a href='https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.special-parameters'>Spring Documentation Reference</a>.<br>"
      + "<b>NOTES:</b> You can also exclude all metadata properties by setting <b><i>excludeMetadata</i></b> to <b><i>true</i></b> or add <b><i>?excludeMetadata=1</i></b> in HTML query param(s)")

  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Get data success"),
      @ApiResponse(responseCode = "400", description = "Bad request. Check errors return in property 'messages'"),
      @ApiResponse(responseCode = "401", description = "Either request needs bearer or profile doesn't have permission or profile doesn't own the data"),
      @ApiResponse(responseCode = "403", description = "Either request needs bearer or profile doesn't have permission"),
      @ApiResponse(responseCode = "404", description = "Won't happen unless your path is wrong"),
      @ApiResponse(responseCode = "500", description = "This happens when there is something wrong with the server. Ex: Database connection failed, Micro-services communication failed, etc.")
  })
  public ResponseEntity<RestResult<Page<RESPONSE>>> getPage(@ParameterObject @Valid S params, @ParameterObject Pageable pageable) {
    return ResponseEntity.ok(RestResult.ok(service.searchPage(params, pageable), "Get data success"));
  }

  @PostMapping
  @Operation(description = "Create data")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Create data success"),
      @ApiResponse(responseCode = "400", description = "Bad request. Check errors return in property 'messages'"),
      @ApiResponse(responseCode = "401", description = "Either request needs bearer or profile doesn't have permission or profile doesn't own the data"),
      @ApiResponse(responseCode = "403", description = "Either request needs bearer or profile doesn't have permission"),
      @ApiResponse(responseCode = "404", description = "Won't happen unless your path is wrong"),
      @ApiResponse(responseCode = "500", description = "This happens when there is something wrong with the server. Ex: Database connection failed, Micro-services communication failed, etc.")
  })
  public ResponseEntity<RestResult<RESPONSE>> create(@ParameterObject @Valid KEY id, @RequestBody @Valid REQUEST request) {
    return ResponseEntity.ok(RestResult.ok(service.create(id, request), "Create data success"));
  }

  @PutMapping
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Update data success"),
      @ApiResponse(responseCode = "400", description = "Bad request. Check errors return in property 'messages'"),
      @ApiResponse(responseCode = "401", description = "Either request needs bearer or profile doesn't have permission or profile doesn't own the data"),
      @ApiResponse(responseCode = "403", description = "Either request needs bearer or profile doesn't have permission"),
      @ApiResponse(responseCode = "404", description = "Won't happen unless your path is wrong"),
      @ApiResponse(responseCode = "500", description = "This happens when there is something wrong with the server. Ex: Database connection failed, Micro-services communication failed, etc.")
  })
  public ResponseEntity<RestResult<RESPONSE>> update(@ParameterObject @NotNull @Valid KEY id, @Valid @RequestBody REQUEST request) {
    return ResponseEntity.ok(RestResult.ok(service.update(id, request), "Update data success"));
  }

  @DeleteMapping
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Delete data success"),
      @ApiResponse(responseCode = "400", description = "Bad request. Check errors return in property 'messages'"),
      @ApiResponse(responseCode = "401", description = "Either request needs bearer or profile doesn't have permission or profile doesn't own the data"),
      @ApiResponse(responseCode = "403", description = "Either request needs bearer or profile doesn't have permission"),
      @ApiResponse(responseCode = "404", description = "Either your path is wrong or there is no data for the given ID"),
      @ApiResponse(responseCode = "500", description = "This happens when there is something wrong with the server. Ex: Database connection failed, Micro-services communication failed, etc.")
  })
  public ResponseEntity<RestResult<Void>> delete(@ParameterObject @NotNull @Valid KEY id) {
    service.delete(id);
    return ResponseEntity.ok(RestResult.ok("Delete data success"));
  }

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

//  @ExceptionHandler({FeignClientException.class})
//  public ResponseEntity<RestResult<String>> handleFeignClientException(FeignClientException exception) {
//    log.error(exception.getMessage(), exception);
//    return new ResponseEntity<>(RestResult.error(exception.getData(), exception.getMessage()),
//        exception.getHttpStatus() != null ? exception.getHttpStatus() : HttpStatus.INTERNAL_SERVER_ERROR);
//  }

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

  @ExceptionHandler({Exception.class})
  public ResponseEntity<RestResult<Void>> handleException(Exception exception) {
    log.error(exception.getMessage(), exception);
    return new ResponseEntity<>(RestResult.error(exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
