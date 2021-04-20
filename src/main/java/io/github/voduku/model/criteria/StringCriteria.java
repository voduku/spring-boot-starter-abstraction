package io.github.voduku.model.criteria;

import lombok.Getter;
import lombok.Setter;

/**
 * @author VuDo
 * @since 1.0.0
 */
@Getter
@Setter
public class StringCriteria extends SearchCriteria<String> {

  String like;
}
