package io.github.voduku.autoconfigure;

import io.github.voduku.repository.Repository;
import io.github.voduku.repository.RepositoryImpl;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * @author VuDo
 * @since 5/5/2021
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({Repository.class})
@EnableJpaRepositories(repositoryBaseClass = RepositoryImpl.class)
public class RepositoryAutoConfig {

  public RepositoryAutoConfig() {
    System.out.println("Running RepositoryAutoConfig");
  }
}
