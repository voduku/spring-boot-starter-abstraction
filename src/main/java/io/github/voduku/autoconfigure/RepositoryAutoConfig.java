package io.github.voduku.autoconfigure;

import io.github.voduku.repository.Repository;
import io.github.voduku.repository.RepositoryImpl;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.SneakyThrows;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.util.ClassUtils;
import org.springframework.web.context.support.GenericWebApplicationContext;

/**
 * @author VuDo
 * @since 5/5/2021
 */
@Order(Ordered.LOWEST_PRECEDENCE - 999999999)
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE - 999999999)
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({Repository.class})
public class RepositoryAutoConfig {

  static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";
  private final ClassPathScanningCandidateComponentProvider provider;
  private final GenericWebApplicationContext factory;

  public RepositoryAutoConfig(GenericWebApplicationContext factory) {
    this.provider = new ClassPathScanningCandidateComponentProvider(false);
    this.factory = factory;
    registerBeans();
  }

  @SneakyThrows
  private void registerBeans() {
    Set<BeanDefinition> components = findComponents();

    if (components.isEmpty()) {
      return;
    }

    for (BeanDefinition component : components) {
      Class<?> clazz = Class.forName(component.getBeanClassName());
      ParameterizedType interfaces = (ParameterizedType) clazz.getGenericInterfaces()[0];
      Type[] types = interfaces.getActualTypeArguments();
      factory.registerBean(clazz.getSimpleName() + "Impl", RepositoryImpl.class, () -> new RepositoryImpl<>((Class<?>) types[0], (Class<?>) types[1].getClass()));
    }
    Arrays.stream(factory.getBeanDefinitionNames()).filter(n -> !n.contains(".")).forEach(System.out::println);
  }

  private String getBasePackage() {
    String basePackage = Objects.requireNonNull(provider.getEnvironment().getProperty("sun.java.command"));
    return basePackage.substring(0, basePackage.lastIndexOf("."));
  }

  private String resolveBasePackage(String basePackage) {
    return ClassUtils.convertClassNameToResourcePath(provider.getEnvironment().resolveRequiredPlaceholders(basePackage));
  }

  @SneakyThrows
  private Set<BeanDefinition> findComponents() {
    String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
        resolveBasePackage(getBasePackage()) + '/' + DEFAULT_RESOURCE_PATTERN;
    Resource[] resources = new PathMatchingResourcePatternResolver().getResources(packageSearchPath);
    Set<BeanDefinition> components = new HashSet<>();
    for (Resource resource : resources) {
      if (!resource.isReadable()) {
        continue;
      }
      MetadataReader metadataReader = provider.getMetadataReaderFactory().getMetadataReader(resource);
      if (metadataReader.getClassMetadata().isInterface()
          && Arrays.stream(metadataReader.getClassMetadata().getInterfaceNames()).anyMatch(name -> name.equals(Repository.class.getName()))) {
        ScannedGenericBeanDefinition sbd = new ScannedGenericBeanDefinition(metadataReader);
        sbd.setSource(resource);
        components.add(sbd);
      }
    }
    return components;
  }
}
