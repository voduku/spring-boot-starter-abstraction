package io.github.voduku.model.criteria;

import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

/**
 * @author VuDo
 * @since 1.0.0
 */
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Accessors(fluent = true, chain = true)
public class NumberCriteria extends SearchCriteria<Number> {

  Number gt;
  Number lt;
  Number gte;
  Number lte;

  public Number getGt() {
    return gt;
  }

  public NumberCriteria setGt(Number gt) {
    this.gt = gt;
    return this;
  }

  public Number getLt() {
    return lt;
  }

  public NumberCriteria setLt(Number lt) {
    this.lt = lt;
    return this;
  }

  public Number getGte() {
    return gte;
  }

  public NumberCriteria setGte(Number gte) {
    this.gte = gte;
    return this;
  }

  public Number getLte() {
    return lte;
  }

  public NumberCriteria setLte(Number lte) {
    this.lte = lte;
    return this;
  }

  @Override
  public List<Predicate> handle(CriteriaBuilder cb, Expression<Number> expression) {
    List<Predicate> predicates = handleCriteria(cb, expression);
    if (gt != null) {
      predicates.add(cb.gt(expression, gt));
    }
    if (gte != null) {
      predicates.add(cb.ge(expression, gte));
    }
    if (lt != null) {
      predicates.add(cb.lt(expression, lt));
    }
    if (lte != null) {
      predicates.add(cb.le(expression, lte));
    }
    return predicates;
  }
}
