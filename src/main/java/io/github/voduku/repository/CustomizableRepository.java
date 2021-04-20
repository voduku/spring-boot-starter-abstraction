package io.github.voduku.repository;

import io.github.voduku.model.AbstractSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

/**
 * Classes implementing this class should support customizable queries that only get and return what is needed based on the params {@link AbstractSearch}.
 *
 * @param <ENTITY> Entity Type
 * @param <KEY> Entity Key
 * @author VuDo
 * @since 1.0.0
 */
public interface CustomizableRepository<ENTITY, KEY> {

  /**
   * Get an entity with given {@link KEY} with optional functionalities to optimize database request and response
   * @param key entity key
   * @param params optional customizing params
   * @return an {@link ENTITY} entity
   */
  ENTITY get(KEY key, AbstractSearch<?> params);

  /**
   * // @formatter:off
   * Search a {@link Slice} of {@link ENTITY} entities filtering by subclasses of {@link AbstractSearch} with options to customize response to get only what is needed all the way to database and back.
   * <br>Correct usage of this api should improve the overall performance of the server.
   * // @formatter:on
   *
   * @param params filtering params {@link AbstractSearch}
   * @param pageable paging for the search
   * @return a {@link Slice} {@link ENTITY} which is never null other wise throw an exception if something goes wrong in the process. Ex: no entity found for the given key.
   */
  Slice<ENTITY> search(AbstractSearch<?> params, Pageable pageable);

  /**
   * // @formatter:off
   * Search a {@link Page} of {@link ENTITY} entities filtering by subclasses of {@link AbstractSearch} with options to customize response to get only what is needed all the way to database and back.
   * <br>Correct usage of this api should improve the overall performance of the server.
   * // @formatter:on
   *
   * @param params filtering params {@link AbstractSearch}
   * @param pageable paging for the search
   * @return an updated {@link ENTITY} which is never null other wise throw an exception if something goes wrong in the process. Ex: no entity found for the given key.
   */
  Page<ENTITY> searchPage(AbstractSearch<?> params, Pageable pageable);
}
