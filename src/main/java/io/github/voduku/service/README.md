# Service

- Create your service like usual and extends
  ```
  AbstractService<REQUEST, RESPONSE, S extends AbstractSearch<?>, KEY extends Serializable>
  ```
- If you use the interface pattern, make sure you extends this interface as well:
  ```
  Service<REQUEST, RESPONSE, S extends AbstractSearch<?>, KEY extends Serializable>
  ```
- With `AbstractService`, you are provided 6 apis just like controllers (get, getCustom, getSlice, getPage, create, update, delete). Their flow is coded
  utilizing Optional and Function, so you can easily modify the logic to your needs. Just make sure you read the code to make it easier. See this create
  example:
  ```java
  public RESPONSE create(KEY key, REQUEST request) {
    return Optional.of(request)
        .map(getBeforeCreate())
        .map(rq -> getMapper().toEntity(key, rq))
        .map(getRepo()::save)
        .map(getMapper()::toResponse)
        .map(getAfterCreate())
        .orElseThrow(getCreateException());
  }
  ```
  You can override or use setter for any of the Function in this flow. For example:
  ```java
  @Override
  public Function<StudentRequest, StudentRequest> getBeforeCreate(){
    return request -> {
      // perform request validation
      return request;
    }
  }
  
  // Or
  
  private final Function<StudentRequest, StudentRequest> beforeCreate = request -> request;
  
  public Constructor(){
    super();
    super.beforeCreate = this.beforeCreate;
  }
  ```
- For error message, you can override or use setter like above, for example with `getCreateException()`. There is support for both `java.util.ResourceBundle`
  and `spring.context.MessageSource`. By default, it will see if there is any `MessageSource` bean then get the corresponding message from the code provided
  otherwise use `java.util.ResourceBundle`.
- Example:
  ```java
  public interface StudentService extends Service<StudentRequest, StudentResponse, StudentKey> {

  }
  
  public class StudentServiceImpl extends AbstractService<StudentRequest, StudentResponse, Student, StudentKey> implements StudentService {
  
  }
  ```