package infomediaservice.vuplaformserver.controller;

import infomediaservice.vuplaformserver.dto.IdentityResponseData;
import infomediaservice.vuplaformserver.service.IdentityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class IdentityController {

    @Autowired
    private IdentityService identityService;

    @PostMapping(value = "information/citizenInformation"
            , produces="application/json; charset=UTF-8", consumes="application/json; charset=UTF-8")
    public ResponseEntity<String> citizenInformation(@RequestBody String jsonRequest){
        IdentityResponseData responseData = identityService.consultar(jsonRequest);
        return new ResponseEntity<>(responseData.response
                , HttpStatus.resolve(responseData.statusCode));
    }

    @GetMapping
    public String root(){
        return "Validacion Identidad Experian";
    }
}
