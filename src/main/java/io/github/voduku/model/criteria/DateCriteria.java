package io.github.voduku.model.criteria;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

/**
 * @author VuDo
 * @since 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Accessors(fluent = true, chain = true)
public class DateCriteria extends SearchCriteria<Long> {

  Long gt;
  Long lt;
  Long gte;
  Long lte;

  public Long getGt() {
    return gt;
  }

  public DateCriteria setGt(Long gt) {
    this.gt = gt;
    return this;
  }

  public Long getLt() {
    return lt;
  }

  public DateCriteria setLt(Long lt) {
    this.lt = lt;
    return this;
  }

  public Long getGte() {
    return gte;
  }

  public DateCriteria setGte(Long gte) {
    this.gte = gte;
    return this;
  }

  public Long getLte() {
    return lte;
  }

  public DateCriteria setLte(Long lte) {
    this.lte = lte;
    return this;
  }
}
