package ber.com.microservice.composite.product.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Component
public class CustomConfig {

	@Value("${api.common.version}")
	String apiVersion;
	
	@Value("${api.common.title}")
	String apiTitle;
	
	@Value("${api.common.description}")
	String apiDescription;
	
	@Value("${api.common.termsOfService}")
	String apiTermsOfService;
	
	@Value("${api.common.license}")
	String apiLicense;
	
	@Value("${api.common.licenseUrl}")
	String apiLicenseUrl;
	
	@Value("${api.common.externalDocDesc}")
	String apiExternalDocDesc;
	
	@Value("${api.common.externalDocUrl}")
	String apiExternalDocUrl;

	@Value("${api.common.contact.name}")	
	String apiContactName;
	
	@Value("${api.common.contact.url}")
	String apiContactUrl;
	
	@Value("${api.common.contact.email}")
	String apiContactEmail;
	
	
	
	@Bean
	public OpenAPI getOpenApiDocumentation() {
		
		return new OpenAPI()
				.info(new Info()
						.title(apiTitle)
						.description(apiDescription)
						.version(apiVersion)
						.contact(new Contact()
								.name(apiContactName)
								.email(apiContactEmail)
								.url(apiContactUrl)
						).termsOfService(apiTermsOfService)
						.license(new License()
								.name(apiLicense)
								.url(apiLicenseUrl)
						)).externalDocs(new ExternalDocumentation()
								.description(apiExternalDocDesc)
								.url(apiExternalDocUrl));
		
	}
	
	
}
