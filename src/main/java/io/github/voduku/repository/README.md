# Repositories

- You will need to create an interface extending `io.github.voduku.repository.Repository`. This interface includes your regular `JpaRepository`
  and `CustomizableRepository`
- After the interface, create an impl class to get access to response optimization feature and/or entity filtering. You have 2 options:
    - Extends `AbstractRepository<ENTITY, KEY>`: a string based query creation approach which doesn't support filtering.
    - Extends `AbstractCriteriaRepository<ENTITY, KEY>`: a criteria api based approach which support both response optimization and filtering.
- When extending any of the 2 abstract classes, you have options to call super(), super(Class<ENTITY>), super(Class<ENTITY>, List<String> entityIdFields).
  Performance improve in left to right order since the more information you give the less I have to reflect upon. Of course the more code you have to right.
- Note: You don't really need a constructor calling super() if you decide to go this path.
- You will also be able to override anything inside. Make sure you read the code to ensure you know what you are doing.
- Example:
  ```java
  public interface StudentRepository extends Repository<Student, StudentKey> {

  }

  public class StudentRepositoryImpl extends AbstractCriteriaRepository<Student, StudentKey> {
  
    // Optional
    public StudentRepositoryImpl(){
      super(Student.class, Arrays.stream(Student.Fields.values).map(Enum::name).collect(Collectors.toList());
    }
  }
  ```