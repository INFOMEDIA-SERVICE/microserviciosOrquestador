package infomediaservice.gatewayserver.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@EnableWebFluxSecurity
public class SpringSecurityConfig {

	@Autowired
	private JwtAuthenticationFilter authenticationFilter;

	@Bean
	public SecurityWebFilterChain configure(ServerHttpSecurity http) {
		return http.authorizeExchange()
				.pathMatchers("api/v1/**").permitAll()
				.anyExchange().authenticated()
				.and().addFilterAt(authenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
				.csrf().disable()
				.build();
	}
}
