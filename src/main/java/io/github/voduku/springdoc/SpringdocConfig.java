package io.github.voduku.springdoc;

import io.github.voduku.model.AbstractEntity;
import io.github.voduku.model.AbstractSearch;
import io.github.voduku.model.AbstractSearch.Fields;
import io.github.voduku.model.criteria.DateCriteria;
import io.github.voduku.model.criteria.DecimalCriteria;
import io.github.voduku.model.criteria.NumberCriteria;
import io.github.voduku.model.criteria.StringCriteria;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.MethodParameter;
import org.springframework.web.method.HandlerMethod;

/**
 * @author VuDo
 * @since 4/20/2021
 */
public class SpringdocConfig {

  private static final List<String> EXPLICIT_SEARCH_PARAMETERS = List.of(Fields.includes.name(), Fields.excludes.name());
  private static final Set<String> AVAILABLE_METADATA = Arrays.stream(AbstractEntity.Fields.values()).map(Enum::name).collect(Collectors.toSet());
  private static final List<String> OPERATIONS_TO_BE_FILTERED = List.of("getCustom", "getSlice", "getPage");
  private static final String DOT = ".";
  private static final String ID = "id";
  private static final String QUERY = "query";
  private static final String CUSTOM = "Custom";
  private static final List<Class<?>> unparsableIdTypes = List.of(Long.class, String.class);

  @Bean
  public OperationCustomizer customizer() {
    return (ops, handlerMethod) -> {
      if (ops.getParameters() == null) {
        ops.setParameters(new ArrayList<>());
      }
      if (Arrays.stream(handlerMethod.getMethodParameters()).anyMatch(param -> unparsableIdTypes.contains(param.getParameterType()))) {
        var schema = getSchema(handlerMethod.getMethodParameters()[0].getParameterType());
        ops.getParameters().add(0, new Parameter().name(ID).in(QUERY).required(true).schema(schema));
      }
      customizeForCriteria(ops, handlerMethod);
      ops.getParameters().stream().filter(parameter -> EXPLICIT_SEARCH_PARAMETERS.contains(parameter.getName()))
          .forEach(parameter -> setEnumForParameter(parameter, handlerMethod));
      return ops;
    };
  }

  private void setEnumForParameter(Parameter parameter, HandlerMethod handlerMethod) {
    Arrays.stream(handlerMethod.getMethodParameters())
        .filter(methodParameter -> AbstractSearch.class.isAssignableFrom(methodParameter.getParameterType()))
        .findFirst()
        .map(this::getEnums)
        .ifPresent(enums -> parameter.setDescription("Available values to be " + parameter.getName().replace("s", "d")
            + ": " + Stream.concat(enums.stream(), AVAILABLE_METADATA.stream()).distinct().collect(Collectors.joining(", "))));
  }

  @SuppressWarnings("unchecked")
  private Set<String> getEnums(MethodParameter param) {
    Class<Enum<?>> c = (Class<Enum<?>>) ((ParameterizedType) param.getParameterType().getGenericSuperclass()).getActualTypeArguments()[0];
    return Arrays.stream(c.getEnumConstants()).map(Enum::name).collect(Collectors.toCollection(LinkedHashSet::new));
  }

  private void customizeForCriteria(Operation operation, HandlerMethod handlerMethod) {
    if (OPERATIONS_TO_BE_FILTERED.stream().noneMatch(operation.getOperationId()::contains)) {
      return;
    }
    if (operation.getOperationId().contains(CUSTOM)) {
      operation.getParameters().removeIf(param -> param.getName().contains(DOT));
      return;
    }
    var fieldSchemas = Arrays.stream(handlerMethod.getMethodParameters())
        .map(MethodParameter::getParameterType)
        .filter(AbstractSearch.class::isAssignableFrom)
        .map(Class::getDeclaredFields)
        .flatMap(Arrays::stream)
        .collect(Collectors.toMap(Field::getName, this::getSchema));

    var paramNames = new HashSet<>();
    List<Parameter> toBeRemoved = new ArrayList<>();
    var operationParameters = operation.getParameters();
    operationParameters.forEach(param -> {
      String name = param.getName().split("\\.")[0];
      if (!param.getName().contains(DOT)) {
        return;
      }
      if (paramNames.contains(name)) {
        toBeRemoved.add(param);
        return;
      }
      paramNames.add(name);
      Schema<?> schema = fieldSchemas.get(name);
      param.name(name + ".eq").schema(schema).description(getDescription(schema));
    });
    operationParameters.removeAll(toBeRemoved);
  }

  @SneakyThrows
  private Schema<?> getSchema(Field field) {
    if (field.getType().equals(StringCriteria.class)) {
      return new StringSchema();
    }
    if (field.getType().equals(DecimalCriteria.class)) {
      return new NumberSchema().format("decimal");
    }
    if (field.getType().equals(NumberCriteria.class) || field.getType().equals(DateCriteria.class)) {
      return new IntegerSchema().format("int64");
    }
    throw new UnsupportedOperationException("Field type in search class be a XCriteria type. Ex: StringCriteria");
  }

  private String getDescription(Schema<?> schema) {
    if (schema instanceof StringSchema) {
      return "You can use .eq, .in, .isNull or .like with corresponding data type and sql semantic to apply filtering";
    }
    if (schema instanceof NumberSchema || schema instanceof IntegerSchema) {
      return "You can use .eq, .in, .isNull, .gt, .lt, .gte, .lte with corresponding data type and sql semantic to apply filtering";
    }
    return null;
  }

  private Schema<?> getSchema(Class<?> clazz) {
    if (clazz == null) {
      throw new IllegalArgumentException("Schema class must not be null");
    } else if (clazz == String.class) {
      return new Schema<String>();
    } else if (clazz == Long.class) {
      return new Schema<Long>();
    } else {
      return new Schema<>();
    }
  }
}
