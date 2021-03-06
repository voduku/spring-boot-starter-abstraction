# Models:

- Entity: You know just like any entities that you normally create but there are 2 options to add metadata (Timestamp createdAt, Timestamp modifiedAt, String
  createdBy, String modifiedBy)
    - Extending `AbstractEntity` which will hide the metadata from the entity and repository (You won't be able to do response customization)
    - Implementing `BaseEntity` doing this force you to add, well, 4 fields for the metadata. Quite a lot of work imo :D
    - Note: if you do decide to follow my metadata pattern, there is a listener which do `@PrePersist` and `@PreUpdate` for you. All you need to do is
      add `@EntityListeners(Metadata.class)`
- Entity Key: As you know, there are single column PK and composite PK, I highly suggest you use `@IdClass` if you decide to use composite PK to help me easier
  support you with response optimization
    - Single column PK: If you use anything like auto-inc int or bigint in your database, make sure you use theirs wrapper classes which you probably should
    - Composite PK: You know follow the doc for `@IdClass`
    - Make sure they are **serializable**
- Request: You should create your request as usual. With `javax.validation` is even better.
- Response: Since your entity may use the metadata that I provide, you can extend `AbstractResponse` to support it as well.
    - Just add every non-relational field(s) that your entity have first since they can be excluded anyway
    - Make sure you use `@JsonInclude(Include.NON_NULL)` so your response looks clean with response optimization.
    - I will not support relational fields since things could go bad in your server really quick
- Search: If you don't want this feature, sorry, but you are forced to use it anyway. Now the fun part begin. Via `AbstractSearch`, you will be able to provide
  the ability to do entity filtering and response optimization.
    - **First**: Make sure your entity have Lombok `@FieldNameConstants(asEnum = true)` so I know (jk, the code know), you know, your frontend dev knows what
      can be used to filter and to be included or excluded from the response. This will create an inner enum `Fields`. For example: `Student.Fields`
    - Second: extends `AbstractSearch<?>` with the entity `Fields` which become
      ```java
      public StudentSearch extends AbstractSearch<Student.Fields>
      ```
    - Search options:
        - distinct: distinct filtering
        - includes: field(s) (column(s)) in your query/response
        - excludes: field(s) (column(s)) in your query/response
        - excludeMetadata: true/false which is the quick way to exclude metadata
    - Your search classes' field(s) should **only use** these type to support filtering: // FYI, you won't get away.
        - NumberCriteria: anything that is not decimal
        - DecimalCriteria: BigDecimal only for now.
        - StringCriteria: String and Enum. Yes you read it right, enum. I will be able to pick it up in your entity but make sure **you don't use ordinal**.
        - BooleanCriteria: Welp, I forgot about this, it will be added soon.
        - Filtering options:
            - ALL: .eq, .in, .isNull
            - Number/Date(which should be long millis): .gt, .gte, .lt, .lte
            - String: .like - Yes you can use `%`. Ex: %abc%
    - Imagine you have a response like this:
    ```java
    public StudentResponse extends AbstractResponse {
        private Long id;
        private String name;
        private Integer age;
        private String address;
    }
    ```
    - And you have a search like this:
    ```java
    public StudentSearch extends AbstractSearch<Student.Fields> {
        private NumberCriteria id;
        private StringCriteria name;
        private NumberCriteria age;
        private StringCriteria address;
    }
    ```
    - Your FE dev can decide what data he needs for example: id and name only. He will add this to the query param: `?includes=id&?includes=name`. The response
      he gets will be like below and trust me the JPA won't touch anything that you don't ask for except those `@OneToOne`. I can't control it. This way, the
      memory that normally require to be allocated just to store data you won't use won't be there, and you don't even have to create any projection classes
    ```json
    // This much data all the way from database to client
    {
       "id": 123,
       "name": "abc"
    }
    ```
    - Your FE dev can also filter a Slice/Page of the entity with filtering options above. Ex:
      ```
      ?name.like=%25abc%25&?age.gte=10
      ```