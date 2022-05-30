package infomediaservice.gatewayserver.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
public class EjemploGlobalFilters implements GlobalFilter {

    private final Logger logger = LoggerFactory.getLogger(EjemploGlobalFilters.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        logger.info("Ejecutando filtro pre");
        exchange.getRequest().mutate().headers(h -> h.add("token", "123456"));

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            logger.info("Ejecutando filtro post");
            //pasamos token de la cabecera del heders de la peticion a la respuestas
            Optional.ofNullable(exchange.getRequest().getHeaders().getFirst("token")).ifPresent(valor -> {
                exchange.getResponse().getHeaders().add("token", valor);
            });
            exchange.getResponse().getCookies().add("color", ResponseCookie.from("color", "rojo").build());
            //exchange.getResponse().getHeaders().setContentType(org.springframework.http.MediaType.valueOf(MediaType.TEXT_PLAIN));
        }));
    }

}
