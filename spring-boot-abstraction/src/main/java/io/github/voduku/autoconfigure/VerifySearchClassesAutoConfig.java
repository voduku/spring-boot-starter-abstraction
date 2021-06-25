package io.github.voduku.autoconfigure;

import io.github.voduku.model.AbstractSearch;
import io.github.voduku.model.criteria.SearchCriteria;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import lombok.SneakyThrows;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AssignableTypeFilter;

/**
 * @author VuDo
 * @since 4/21/2021
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(AbstractSearch.class)
public class VerifySearchClassesAutoConfig {

  private final ClassPathScanningCandidateComponentProvider provider;

  public VerifySearchClassesAutoConfig() {
    this.provider = new ClassPathScanningCandidateComponentProvider(false);
    verify();
  }

  @SneakyThrows
  public void verify() {
    provider.addIncludeFilter(new AssignableTypeFilter(AbstractSearch.class));

// scan in org.example.package

    Set<BeanDefinition> components = provider.findCandidateComponents(getBasePackage());

    if (components.isEmpty()) {
      return;
    }

    Map<String, List<String>> errorClasses = new HashMap<>();

    for (BeanDefinition component : components) {
      Class<?> clazz = Class.forName(component.getBeanClassName());

      List<String> fields = new ArrayList<>();

      for (Field declaredField : clazz.getDeclaredFields()) {
        if (!SearchCriteria.class.isAssignableFrom(declaredField.getType())) {
          fields.add(declaredField.getName());
        }
      }

      if (!fields.isEmpty()) {
        errorClasses.put(component.getBeanClassName(), fields);
      }
    }
    if (!errorClasses.isEmpty()) {
      StringBuilder error = new StringBuilder("\n\n\nPlease fix field(s) type to SearchCriteria sub-classes such as StringCriteria in:\n");
      for (Entry<String, List<String>> entry : errorClasses.entrySet()) {
        error.append("--- Class ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
      }
      throw new UnsupportedOperationException(error.toString());
    }
  }

  private String getBasePackage() {
    String basePackage = Objects.requireNonNull(provider.getEnvironment().getProperty("sun.java.command"));
    return basePackage.substring(0, basePackage.lastIndexOf("."));
  }
}
