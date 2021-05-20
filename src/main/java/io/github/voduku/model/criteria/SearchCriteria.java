package io.github.voduku.model.criteria;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.springframework.util.CollectionUtils;

/**
 * @author VuDo
 * @since 1.0.0
 */
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Accessors(fluent = true, chain = true)
public class SearchCriteria<T> implements CriteriaHandler<T> {

  T eq;
  Collection<? extends T> in;
  Boolean isNull;

  public T getEq() {
    return this.eq;
  }

  public SearchCriteria<T> setEq(T eq) {
    this.eq = eq;
    return this;
  }

  public Collection<? extends T> getIn() {
    return this.in;
  }

  public SearchCriteria<T> setIn(Collection<? extends T> in) {
    this.in = in;
    return this;
  }

  public Boolean getIsNull() {
    return this.isNull;
  }

  public SearchCriteria<T> setIsNull(Boolean isNull) {
    this.isNull = isNull;
    return this;
  }

  public List<Predicate> handle(CriteriaBuilder cb, Expression<T> expression) {
    return handleCriteria(cb, expression);
  }

  protected List<Predicate> handleCriteria(CriteriaBuilder cb, Expression<T> expression) {
    List<Predicate> predicates = new ArrayList<>();
    if (eq != null) {
      predicates.add(handleEqual(cb, expression));
    }
    if (!CollectionUtils.isEmpty(in)) {
      predicates.add(handleIn(expression));
    }
    if (Objects.equals(isNull, Boolean.TRUE)) {
      predicates.add(expression.isNull());
    }
    if (Objects.equals(isNull, Boolean.FALSE)) {
      predicates.add(expression.isNotNull());
    }
    return predicates;
  }

  protected Predicate handleEqual(CriteriaBuilder cb, Expression<T> expression) {
    return cb.equal(expression, eq);
  }

  protected Predicate handleIn(Expression<T> expression) {
    return expression.in(in);
  }
}

