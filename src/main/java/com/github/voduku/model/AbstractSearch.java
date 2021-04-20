package com.github.voduku.model;

import static java.util.function.Predicate.not;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.springframework.core.GenericTypeResolver;
import org.springframework.util.CollectionUtils;

/**
 * // @formatter:off
 * All search classes should extends this class. This class's fields will be mapped into {@link org.springframework.web.bind.annotation.RequestParam} via data binding.
 * {@link #excludables} contains all the fields that are excludable when querying.
 * {@link #includes} contains all the fields to be included.
 * {@link #excludes} contains all the fields to be excluded.
 * {@link #excludeMetadata} if true, all fields like {@link AbstractEntity} will be ignored.
 * // @formatter:on
 *
 * @author VuDo
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@FieldNameConstants(asEnum = true)
public abstract class AbstractSearch<T extends Enum<T>> {

  @JsonIgnore
  @Setter(AccessLevel.NONE)
  @Parameter(hidden = true)
  protected Enum<T>[] excludables;
  @JsonIgnore
  protected List<String> includes;
  @JsonIgnore
  protected List<String> excludes;
  @JsonIgnore
  protected boolean excludeMetadata = false;

  @JsonIgnore
  @Setter(AccessLevel.NONE)
  @Parameter(hidden = true)
  private boolean configured = false;

  /**
   * Although this is slower than directly calling values(), performance is acceptable.
   */
  @SuppressWarnings("unchecked")
  public AbstractSearch() {
    this.excludables = ((Class<T>) Objects.requireNonNull(GenericTypeResolver.resolveTypeArguments(getClass(), AbstractSearch.class))[0]).getEnumConstants();
    this.includes = Arrays.stream(excludables).map(Enum::name).collect(Collectors.toList());
  }

  /**
   * Create default constructor super this constructor for optimal output performance
   */
  public AbstractSearch(Enum<T>[] excludables) {
    this.excludables = excludables;
    this.includes = Arrays.stream(excludables).map(Enum::name).collect(Collectors.toList());
  }

  public void setIncludes(List<String> includes) {
    if (configured || CollectionUtils.isEmpty(includes)) {
      return;
    }
    this.includes = includes;
    List<String> excludes = new ArrayList<>();
    for (Enum<T> field : excludables) {
      if (!includes.contains(field.name())) {
        excludes.add(field.name());
      }
    }
    if (excludeMetadata) {
      List<String> metadata = Arrays.stream(AbstractEntity.Fields.values()).map(Enum::name).collect(Collectors.toList());
      this.includes = includes.stream().filter(not(metadata::contains)).collect(Collectors.toList());
    }
    this.excludes = excludes;
  }

  public void setExcludes(List<String> excludes) {
    if (configured || CollectionUtils.isEmpty(excludes)) {
      return;
    }
    this.excludes = excludes;
    List<String> includes = new ArrayList<>();
    for (Enum<T> field : excludables) {
      if (!excludes.contains(field.name())) {
        includes.add(field.name());
      }
    }
    if (excludeMetadata) {
      for (AbstractEntity.Fields field : AbstractEntity.Fields.values()) {
        if (!excludes.contains(field.name())) {
          includes.add(field.name());
        }
      }
    }
    this.includes = includes;
  }
}
