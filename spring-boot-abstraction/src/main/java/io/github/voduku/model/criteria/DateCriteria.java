package io.github.voduku.model.criteria;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
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
public class DateCriteria extends SearchCriteria<Date> {

  Date gt;
  Date lt;
  Date gte;
  Date lte;

  public Date getGt() {
    return gt;
  }

  @JsonProperty("gt")
  public DateCriteria setGt(Long gt) {
    if (gt != null) {
      this.gt = new Date(gt);
    }
    return this;
  }

  @JsonProperty("eq")
  public DateCriteria setEq(Long eq) {
    if (eq != null) {
      this.eq = new Date(eq);
    }
    return this;
  }

  @JsonProperty("in")
  public DateCriteria setDateIn(Collection<Long> in) {
    if (in != null) {
      this.in = in.stream().map(Date::new).collect(Collectors.toUnmodifiableList());
    }
    return this;
  }

  public Date getLt() {
    return lt;
  }

  @JsonProperty("lt")
  public DateCriteria setLt(Long lt) {
    if (lt != null) {
      this.lt = new Date(lt);
    }
    return this;
  }

  public Date getGte() {
    return gte;
  }

  @JsonProperty("gte")
  public DateCriteria setGte(Long gte) {
    if (gte != null) {
      this.gte = new Date(gte);
    }
    return this;
  }

  public Date getLte() {
    return lte;
  }

  @JsonProperty("lte")
  public DateCriteria setLte(Long lte) {
    if (lte != null) {
      this.lte = new Date(lte);
    }
    return this;
  }

  @Override
  public List<Predicate> handle(CriteriaBuilder cb, Expression<Date> expression) {
    List<Predicate> predicates = handleCriteria(cb, expression);
    if (gt != null) {
      predicates.add(cb.greaterThan(expression, gt));
    }
    if (gte != null) {
      predicates.add(cb.greaterThanOrEqualTo(expression, gte));
    }
    if (lt != null) {
      predicates.add(cb.lessThan(expression, lt));
    }
    if (lte != null) {
      predicates.add(cb.lessThanOrEqualTo(expression, lte));
    }
    return predicates;
  }
}
