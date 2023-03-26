package com.test.news.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(info = @Info(title = "Kakao Mint News Researcher with Swagger",
		description = "카카오 민트 뉴스 연구소", version = "v2.5"))

@Configuration
public class SwaggerConfig {}
// @Configuration
// public class SwaggerConfig {

//     @Bean
//     public OpenAPI openAPI(@Value("")) {
//         return new Docket(DocumentationType.SWAGGER_2)
//                 .apiInfo(apiInfo())
//                 .select()
//                 .apis(RequestHandlerSelectors.basePackage("com.test.news"))
//                 .paths(PathSelectors.any())
//                 .build();
//     }

//     private ApiInfo apiInfo() {
//         return new ApiInfoBuilder()
//                 .title("Kakao Mint News Researcher with Swagger")
//                 .description("카카오 민트 뉴스 연구소")
//                 .version("2.5.0")
//                 .build();
//     }
// }
