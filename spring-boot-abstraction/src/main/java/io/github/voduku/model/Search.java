package io.github.voduku.model;

import io.github.voduku.model.criteria.CriteriaHandler;
import java.util.Map;
import java.util.Set;

/**
 * @author VuDo
 * @since 5/19/2021
 */
public interface Search {

  Set<String> getExcludes();
  Set<String> getIncludes();
  Map<String, CriteriaHandler<?>> getCriteria();
}
