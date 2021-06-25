package io.github.voduku.model.criteria;

import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

/**
 * @author VuDo
 * @since 6/20/2021
 */
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@Accessors(fluent = true, chain = true)
public class BooleanCriteria extends SearchCriteria<Boolean> {

}
