package infomediaservice.vuplaformserver.controller;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController

public class InformationController {

    //Para generar log
    private final Logger logger = LoggerFactory.getLogger(InformationController.class);

    //resilience4j tolerancia a fallos por si falla completar accion

    @Autowired
    private CircuitBreaker.CircuitBreakerFuture cbFactory;
}
