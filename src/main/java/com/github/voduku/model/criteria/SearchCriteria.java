package com.github.voduku.model.criteria;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author VuDo
 * @since 1.0.0
 */
@Getter
@Setter
@Accessors
public class SearchCriteria<T> {

  T eq;
  T[] in;
  Boolean isNull;
}

