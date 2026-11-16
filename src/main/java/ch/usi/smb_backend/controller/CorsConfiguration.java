package ch.usi.smb_backend.controller;

import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Lets the frontend, served from another domain, call the API from the browser. Only the origins
 * in {@code app.cors.allowed-origins} may (comma-separated; patterns like
 * {@code https://*.example.org} work); set it in production with the environment variable
 * {@code APP_CORS_ALLOWED_ORIGINS}. The API only reads, so only reading methods are allowed, and
 * no cookies or credentials are ever sent.
 */
@Configuration(proxyBeanMethods = false)
class CorsConfiguration implements WebMvcConfigurer {

	/** How long a browser may reuse a preflight answer before asking again. */
	private static final Duration PREFLIGHT_CACHE = Duration.ofHours(1);

	private final List<String> allowedOrigins;

	CorsConfiguration(@Value("${app.cors.allowed-origins}") List<String> allowedOrigins) {
		this.allowedOrigins = allowedOrigins.stream().map(String::strip).filter(origin -> !origin.isEmpty()).toList();
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping(ApiPaths.BASE + "/**")
				.allowedOriginPatterns(allowedOrigins.toArray(String[]::new))
				.allowedMethods("GET", "HEAD", "OPTIONS")
				.allowCredentials(false)
				.maxAge(PREFLIGHT_CACHE.toSeconds());
	}
}
