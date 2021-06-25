package io.github.voduku.autoconfigure;

import io.github.voduku.model.CommonMapper;
import io.github.voduku.model.CommonMapperImpl;
import io.github.voduku.model.MapperConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author VuDo
 * @since 4/21/2021
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(MapperConfig.class)
public class MapperAutoConfig {

  @Bean
  @ConditionalOnMissingBean
  public CommonMapper commonMapper() {
    return new CommonMapperImpl();
  }
}
