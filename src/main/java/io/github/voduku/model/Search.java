package io.github.voduku.model;

import io.github.voduku.model.criteria.CriteriaHandler;
import java.util.Map;

/**
 * @author VuDo
 * @since 5/19/2021
 */
public interface Search {

  Map<String, CriteriaHandler<?>> getCriteria();
}
