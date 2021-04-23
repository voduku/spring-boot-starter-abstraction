package io.github.voduku.service;

import io.github.voduku.model.AbstractMapper;
import io.github.voduku.model.AbstractSearch;
import io.github.voduku.repository.Repository;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.persistence.NoResultException;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.annotation.Transactional;

/**
 * // @formatter:off
 * Basic CRUD functionalities with options to customizable search to reduce memory foot print.
 * AbstractService will require a corresponding {@link Repository} and an {@link AbstractMapper} in order to work so make sure you have the beans avialable in context.
 * <br>You should be able to override logics of CRUDs by using setters for any {@link Function}. Ex: {@link #setBeforeCreate(Function)}, {@link #setAfterCreate(Function)}, etc.
 * <br>You should also be to override logics by overriding methods such as {@link #getBeforeCreate()}, {@link #getAfterCreate()}, etc.
 * // @formatter:on
 *
 * @apiNote Service utilize mapstruct to provide quick and performant mapping for objects
 * @param <REQUEST>  Request Type. Request should only contains non-ID fields. Use {@link KEY} to map key fields
 * @param <RESPONSE> Response Type
 * @link ENTITY   Entity Type
 * @param <KEY>      Entity Key Type
 * @author VuDo
 * @since 1.0.0
 */
@Setter
@Getter
@Transactional
public abstract class AbstractService<REQUEST, RESPONSE, ENTITY, KEY> implements Service<REQUEST, RESPONSE, KEY> {

  @Autowired(required = false)
  protected ResourceBundle resourceBundle;
  @Autowired(required = false)
  protected MessageSource messageSource;
  @Autowired
  protected Repository<ENTITY, KEY> repo;
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

  protected Supplier<Exception> createException = () -> new Exception(getMessage("err.default"));
  protected Supplier<Exception> updateException = () -> new Exception(getMessage("err.default"));
  protected Supplier<Exception> deleteException = () -> new Exception(getMessage("err.default"));
  protected Supplier<Exception> findException = NoResultException::new;
  protected Supplier<Exception> searchException = () -> new Exception(getMessage("err.default"));
  protected Supplier<Exception> searchPageException = () -> new Exception(getMessage("err.default"));

  /**
   * Check if there exists an entity with the given key
   *
   * @param key entity key
   * @return boolean
   */
  public boolean exists(KEY key) {
    return Optional.of(key)
        .map(getRepo()::existsById)
        .get();
  }

  /**
   * Create an {@link ENTITY} entity
   *
   * @param key     key of the entity
   * @param request request
   * @return {@link RESPONSE} which is never null other wise throw an exception if something goes wrong in the process.
   */
  @SneakyThrows
  public RESPONSE create(KEY key, REQUEST request) {
    return Optional.of(request)
        .map(getBeforeCreate())
        .map(rq -> getMapper().toEntity(key, rq))
        .map(getRepo()::save)
        .map(getMapper()::toResponse)
        .map(getAfterCreate())
        .orElseThrow(getCreateException());
  }

  /**
   * Update an {@link ENTITY} entity
   *
   * @param key     key of the entity
   * @param request request
   * @return an updated {@link RESPONSE} which is never null other wise throw an exception if something goes wrong in the process. Ex: no entity found for the
   * given key.
   */
  @SneakyThrows
  public RESPONSE update(KEY key, REQUEST request) {
    return Optional.of(request)
        .map(getBeforeUpdate())
        .flatMap(ignored -> getRepo().findById(key))
        .map(entity -> getMapper().updateEntity(request, entity))
        .map(getRepo()::save)
        .map(getMapper()::toResponse)
        .map(getAfterUpdate())
        .orElseThrow(getUpdateException());
  }

  /**
   * Delete an {@link ENTITY} entity with the given key if found. Otherwise, throw exception.
   *
   * @param key key of the entity
   */
  public void delete(KEY key) {
    getRepo().findById(key)
        .ifPresentOrElse(
            getRepo()::delete,
            () -> {
              throw new NoResultException();
            }
        );
  }

  /**
   * Get an {@link ENTITY} entity by the given {@link KEY}. Otherwise, throw exception if nothing is found.
   *
   * @param key key of the entity
   * @return an updated {@link RESPONSE} which is never null other wise throw an exception if something goes wrong in the process. Ex: no entity found for the
   * given key.
   */
  @SneakyThrows
  public RESPONSE get(KEY key) {
    return getRepo().findById(key)
        .map(getMapper()::toResponse)
        .map(getAfterFindOne())
        .orElseThrow(getFindException());
  }

  /**
   * // @formatter:off
   * Get an {@link ENTITY} entity by the given {@link KEY} with options to customize response to get only what is needed all the way to database and back.
   * <br>Correct usage of this api should improve the overall performance of the server.
   * // @formatter:on
   *
   * @param key key of the entity
   * @return an updated {@link RESPONSE} which is never null other wise throw an exception if something goes wrong in the process. Ex: no entity found for the given key.
   */
  @SneakyThrows
  public RESPONSE get(KEY key, AbstractSearch<?> parameters) {
    return Optional.of(parameters)
        .map(params -> getRepo().get(key, params))
        .map(getMapper()::toResponse)
        .map(getAfterFindOne())
        .orElseThrow(getFindException());
  }

  /**
   * // @formatter:off
   * Search a {@link Slice} of {@link RESPONSE} entities filtering by subclasses of {@link AbstractSearch} with options to customize response to get only what is needed all the way to database and back.
   * <br>Correct usage of this api should improve the overall performance of the server.
   * // @formatter:on
   *
   * @param parameters filtering params {@link AbstractSearch}
   * @param pageable paging for the search
   * @return a {@link Slice} {@link RESPONSE} which is never null other wise throw an exception if something goes wrong in the process. Ex: no entity found for the given key.
   */
  @SneakyThrows
  public Slice<RESPONSE> search(AbstractSearch<?> parameters, Pageable pageable) {
    return Optional.of(parameters)
        .map(getSearchTransformer())
        .map(params -> getRepo().search(parameters, pageable))
        .map(slice -> slice.map(getMapper()::toResponse))
        .map(slice -> slice.map(getAfterSearchSlice()))
        .orElseThrow(getSearchException());
  }

  /**
   * // @formatter:off
   * Search a {@link Page} of {@link RESPONSE} entities filtering by subclasses of {@link AbstractSearch} with options to customize response to get only what is needed all the way to database and back.
   * <br>Correct usage of this api should improve the overall performance of the server.
   * // @formatter:on
   *
   * @param parameters filtering params {@link AbstractSearch}
   * @param pageable paging for the search
   * @return a {@link Page} of {@link RESPONSE} which is never null other wise throw an exception if something goes wrong in the process. Ex: no entity found for the given key.
   */
  @SneakyThrows
  public Page<RESPONSE> searchPage(AbstractSearch<?> parameters, Pageable pageable) {
    return Optional.of(parameters)
        .map(getSearchTransformer())
        .map(params -> getRepo().searchPage(parameters, pageable))
        .map(slice -> slice.map(getMapper()::toResponse))
        .map(slice -> slice.map(getAfterSearchPage()))
        .orElseThrow(getSearchPageException());
  }

  protected String getMessage(String messageCode) {
    return messageSource != null ? messageSource.getMessage(messageCode, new Object[0], Locale.getDefault()) :
        resourceBundle!=null ? resourceBundle.getString(messageCode) : "There is no resource available to get message";
  }
}
