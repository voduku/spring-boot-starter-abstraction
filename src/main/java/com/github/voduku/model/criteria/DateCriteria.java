package io.github.voduku.model.criteria;

import lombok.Getter;
import lombok.Setter;

/**
 * @author VuDo
 * @since 1.0.0
 */
@Getter
@Setter
public class DateCriteria extends SearchCriteria<Long> {

  Long gt;
  Long lt;
  Long gte;
  Long lte;
}
