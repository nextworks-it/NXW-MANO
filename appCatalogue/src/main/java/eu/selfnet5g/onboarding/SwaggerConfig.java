package eu.selfnet5g.onboarding;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

   @Bean
   public Docket api(){
        return new Docket(DocumentationType.SWAGGER_2)
            .select()
            	.apis(RequestHandlerSelectors.any())
            	.paths(PathSelectors.regex("/app-catalogue/.*"))
            	.build()
            .pathMapping("/")
            .apiInfo(apiInfo());
   }

   @Bean
   public UiConfiguration uiConfig() {
     return UiConfiguration.DEFAULT;
   }
   
   private ApiInfo apiInfo() {
       ApiInfo apiInfo = new ApiInfo(
           "SELFNET Applications Catalogue",
           "The API of the NFV and SDN Applications Catalogue Service.",
           "1.0",
           "",
           new Contact("Giacomo Bernini", "https://selfnet-5g.eu/", "g.bernini@nextworks.it"),
           "Apache License Version 2.0",
           "http://www.apache.org/licenses/LICENSE-2.0"
       );
       return apiInfo;
   }
  
}
