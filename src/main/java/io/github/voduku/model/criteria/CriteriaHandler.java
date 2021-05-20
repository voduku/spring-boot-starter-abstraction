package io.github.voduku.model.criteria;

import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

/**
 * Handler for search criteria
 *
 * @author VuDo
 * @since 5/19/2021
 */
public interface CriteriaHandler<T> {

  /**
   * You can create your own criteria implementing this handler.
   *
   * @param cb         CriteriaBuilder
   * @param expression this will be an expression of a field (column). This is the result of root.get(column)
   * @return a predicate or null (to be ignored)
   */
  List<Predicate> handle(CriteriaBuilder cb, Expression<T> expression);
}
