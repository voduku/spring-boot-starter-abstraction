package io.github.voduku.model.criteria;

import java.math.BigDecimal;
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
public class DecimalCriteria extends SearchCriteria<BigDecimal> {

  BigDecimal gt;
  BigDecimal lt;
  BigDecimal gte;
  BigDecimal lte;

  public BigDecimal getGt() {
    return gt;
  }

  public DecimalCriteria setGt(BigDecimal gt) {
    this.gt = gt;
    return this;
  }

  public BigDecimal getLt() {
    return lt;
  }

  public DecimalCriteria setLt(BigDecimal lt) {
    this.lt = lt;
    return this;
  }

  public BigDecimal getGte() {
    return gte;
  }

  public DecimalCriteria setGte(BigDecimal gte) {
    this.gte = gte;
    return this;
  }

  public BigDecimal getLte() {
    return lte;
  }

  public DecimalCriteria setLte(BigDecimal lte) {
    this.lte = lte;
    return this;
  }
}
