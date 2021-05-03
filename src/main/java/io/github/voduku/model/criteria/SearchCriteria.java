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
public class SearchCriteria<T> {

  T eq;
  T[] in;
  Boolean isNull;

  public T getEq() {
    return this.eq;
  }

  public SearchCriteria<T> setEq(T eq) {
    this.eq = eq;
    return this;
  }

  public T[] getIn() {
    return this.in;
  }

  public SearchCriteria<T> setIn(T[] in) {
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
}

