# Springdoc

- All you have to do is to create a bean extending `SpringdocConfig` and everything will be setup to work with abstract controller apis
- Example:
  ```java
    @Configuration(proxyBeanMethods = false)
    public class SwaggerConfig extends SpringdocConfig {
        
        @Bean
        public OpenAPI publicApi(@Value("${server.port}") Integer port) {
            return new OpenAPI()
                .info(apiInfo())
                .servers(servers(port));
        }
        
        private List<Server> servers(Integer port) {
            Server local = new Server();
            local.setUrl("http://localhost:" + port);
            return List.of(dev, local);
        }
        
        private Info apiInfo() {
            return new Info().title("Service").description("Abstract APIs").version("1.0.0")
            .contact(new Contact().name("Vu Do").email("dovu42@gmail.com"));
        }
    }
  ```