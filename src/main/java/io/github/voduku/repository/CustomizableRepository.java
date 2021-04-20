package io.github.voduku.repository;

import io.github.voduku.model.AbstractSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

/**
 * @author VuDo
 * @since 1.0.0
 */
public interface CustomizableRepository<ENTITY, KEY> {

  ENTITY get(KEY key, AbstractSearch<?> params);

  Slice<ENTITY> search(AbstractSearch<?> params, Pageable pageable);

  Page<ENTITY> searchPage(AbstractSearch<?> params, Pageable pageable);
}
