package io.github.voduku.model.criteria;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/**
 * @author VuDo
 * @since 1.0.0
 */
@Getter
@Setter
public class DecimalCriteria extends SearchCriteria<BigDecimal> {

  BigDecimal gt;
  BigDecimal lt;
  BigDecimal gte;
  BigDecimal lte;
}
