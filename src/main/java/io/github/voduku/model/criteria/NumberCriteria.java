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
}
