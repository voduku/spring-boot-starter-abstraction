# Controllers:

- Create your controller like usual and extends
  ```
  AbstractController<REQUEST, RESPONSE, S extends AbstractSearch<?>, KEY extends Serializable>
  ```
- This should be enough to provide you with 6 apis fully documented in springdoc/swagger. Examples of these apis can get quite intensive so please check it out
  yourself at [Swagger](http://locahost:8080/swagger-ui/index.html), don't forget to run your application:
    - GET / :
        - Request param(s) binded from your KEY.
        - Ex: The object below will be mapped to request params: ?id=123&?name=abc
        ```java
        public class StudentKey implement Serializable {
           private Long id;
           private String name;
        }
        ```
    - GET /custom :
        - KEY request param as above
        - SEARCH only basic search features like `includes`, `excludes`
    - GET /slice :
        - Response optimization
        - Filtering
    - Get /page
        - Response optimization
        - Filtering
    - POST /
        - Request Body
    - PUT /
        - KEY as request param(s)
        - Request body
    - DELETE /
        - KEY as request param(s)

Full example of a controller:

```java

@RestController
@RequestMapping("/student")
public class StudentController extends AbstractController<StudentRequest, StudentResponse, StudentSearch, StudentKey> {
// or just Long instead of StudentKey if your key type is Long
}
```