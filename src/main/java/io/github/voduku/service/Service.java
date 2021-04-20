package io.github.voduku.service;

import io.github.voduku.model.AbstractSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

/**
 * @author VuDo
 * @since 1.0.0
 */
public interface Service<REQUEST, RESPONSE, KEY> {

  boolean exists(KEY key);

  RESPONSE create(KEY key, REQUEST request);

  RESPONSE update(KEY key, REQUEST response);

  RESPONSE get(KEY key);

  RESPONSE get(KEY key, AbstractSearch<?> parameters);

  void delete(KEY key);

  Slice<RESPONSE> search(AbstractSearch<?> parameters, Pageable pageable);

  Page<RESPONSE> searchPage(AbstractSearch<?> parameters, Pageable pageable);
}
