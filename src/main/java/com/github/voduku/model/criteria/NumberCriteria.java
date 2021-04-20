package com.github.voduku.model.criteria;

import lombok.Getter;
import lombok.Setter;

/**
 * @author VuDo
 * @since 1.0.0
 */
@Getter
@Setter
public class NumberCriteria extends SearchCriteria<Number> {

  Number gt;
  Number lt;
  Number gte;
  Number lte;
}
