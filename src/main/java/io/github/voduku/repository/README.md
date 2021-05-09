# Repositories

- You will need to create an interface extending `io.github.voduku.repository.Repository`. This interface includes your regular `JpaRepository`.
- After creating the interface, you can simply add this code to your main to make all your repos run the same way
    - `@EnableJpaRepositories(repositoryBaseClass = RepositoryImpl.class)` 
    