package io.github.voduku.service;

import io.github.voduku.model.AbstractMapper;
import io.github.voduku.model.AbstractSearch;
import io.github.voduku.model.RestResult;
import io.github.voduku.repository.Repository;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Function;
import javax.persistence.NoResultException;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author VuDo
 * @since 1.0.0
 */
@Setter
@Getter
@Transactional
public abstract class AbstractService<REQUEST, RESPONSE, ENTITY, KEY> implements Service<REQUEST, RESPONSE, KEY> {

  @Autowired
  protected Repository<ENTITY, KEY> repo;
  @Autowired(required = false)
  protected ResourceBundle resourceBundle;
  @Autowired
  protected AbstractMapper<REQUEST, RESPONSE, ENTITY, KEY> mapper;
  protected Function<REQUEST, REQUEST> beforeCreate = request -> request;
  protected Function<RESPONSE, RESPONSE> afterCreate = response -> response;
  protected Function<REQUEST, REQUEST> beforeUpdate = request -> request;
  protected Function<RESPONSE, RESPONSE> afterUpdate = response -> response;
  protected Function<RESPONSE, RESPONSE> afterFindOne = response -> response;
  protected Function<RESPONSE, RESPONSE> afterSearchSlice = response -> response;
  protected Function<RESPONSE, RESPONSE> afterSearchPage = response -> response;
  protected Function<AbstractSearch<?>, AbstractSearch<?>> searchTransformer = params -> params;

  public boolean exists(KEY key) {
    return Optional.of(key)
        .map(getRepo()::existsById)
        .get();
  }

  @SneakyThrows
  public RESPONSE create(KEY key, REQUEST request) {
    return Optional.of(request)
        .map(getBeforeCreate())
        .map(rq -> getMapper().toEntity(key, rq))
        .map(getRepo()::save)
        .map(getMapper()::toResponse)
        .map(getAfterCreate())
        .orElseThrow(() -> new Exception(resourceBundle.getString("err.default")));
  }

  @SneakyThrows
  public RESPONSE update(KEY key, REQUEST request) {
    return Optional.of(request)
        .map(getBeforeUpdate())
        .flatMap(ignored -> getRepo().findById(key))
        .map(entity -> getMapper().updateEntity(request, entity))
        .map(getRepo()::save)
        .map(getMapper()::toResponse)
        .map(getAfterUpdate())
        .orElseThrow(() -> new Exception(resourceBundle.getString("err.default")));
  }

  public RESPONSE get(KEY key) {
    return getRepo().findById(key)
        .map(getMapper()::toResponse)
        .map(getAfterFindOne())
        .orElseThrow(NoResultException::new);
  }

  public RESPONSE get(KEY key, AbstractSearch<?> parameters) {
    return Optional.of(parameters)
        .map(params -> getRepo().get(key, params))
        .map(getMapper()::toResponse)
        .map(getAfterFindOne())
        .orElseThrow(NoResultException::new);
  }

  public void delete(KEY key) {
    getRepo().findById(key)
        .ifPresentOrElse(
            entity -> getRepo().delete(entity),
            () -> {
              throw new NoResultException();
            }
        );
  }

  @SneakyThrows
  public Slice<RESPONSE> search(AbstractSearch<?> parameters, Pageable pageable) {
    return Optional.of(parameters)
        .map(getSearchTransformer())
        .map(params -> getRepo().search(parameters, pageable))
        .map(slice -> slice.map(getMapper()::toResponse))
        .map(slice -> slice.map(getAfterSearchSlice()))
        .orElseThrow(() -> new Exception(resourceBundle.getString("err.default")));
  }

  @SneakyThrows
  public Page<RESPONSE> searchPage(AbstractSearch<?> parameters, Pageable pageable) {
    return Optional.of(parameters)
        .map(getSearchTransformer())
        .map(params -> getRepo().searchPage(parameters, pageable))
        .map(slice -> slice.map(getMapper()::toResponse))
        .map(slice -> slice.map(getAfterSearchPage()))
        .orElseThrow(() -> new Exception(resourceBundle.getString("err.default")));
  }

  @SneakyThrows
  protected <T, U> U extractData(Function<T, ResponseEntity<RestResult<U>>> function, T arg) {
    try {
      ResponseEntity<RestResult<U>> response = function.apply(arg);
      return response != null && response.getBody() != null && response.getBody().getData() != null ? response.getBody().getData() : null;
    } catch (Exception e) {
      throw new Exception(resourceBundle.getString("err.default"));
    }
  }

  protected <T> T extractData(ResponseEntity<RestResult<T>> response) {
    return response != null && response.getBody() != null && response.getBody().getData() != null ? response.getBody().getData() : null;
  }

  protected <T> T extractData(RestResult<T> response) {
    return response != null && response.getData() != null ? response.getData() : null;
  }
}
