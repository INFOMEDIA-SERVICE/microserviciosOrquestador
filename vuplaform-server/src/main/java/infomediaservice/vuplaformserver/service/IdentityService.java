package infomediaservice.vuplaformserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import infomediaservice.vuplaformserver.dto.CitizenInformationRequest;
import infomediaservice.vuplaformserver.dto.IdentityExperian;
import infomediaservice.vuplaformserver.dto.IdentityResponseData;
import infomediaservice.vuplaformserver.dto.IdentityVU;
import infomediaservice.vuplaformserver.entity.InfoCitizen;
import infomediaservice.vuplaformserver.entity.Transaction;
import infomediaservice.vuplaformserver.entity.Validation;
import infomediaservice.vuplaformserver.entity.VuCustomer;
import infomediaservice.vuplaformserver.repository.InfoCitizenRepository;
import infomediaservice.vuplaformserver.repository.TransactionRepository;
import infomediaservice.vuplaformserver.repository.ValidationRepository;
import infomediaservice.vuplaformserver.repository.VuCustomerRepository;
import infomediaservice.vuplaformserver.util.ResultCode;
import infomediaservice.vuplaformserver.util.WSDataCredito;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@Log4j2
public class IdentityService {

    @Autowired
    private WSDataCredito wsDataCredito;

    @Autowired
    private VuCustomerRepository vuCustomerRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ValidationRepository validationRepository;

    @Autowired
    private InfoCitizenRepository infoCitizenRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final Validator validator = javax.validation.Validation.buildDefaultValidatorFactory().getValidator();

    private final static String requestType = "VALIDACION_IDENTIDAD";

    private Map<Integer, Integer> tipoDocumentoConversionMap;

    @PostConstruct
    private void init(){
        //Inicializar mapa de conversion de tipo de documento de identidad
        tipoDocumentoConversionMap = new HashMap<>();
        tipoDocumentoConversionMap.put(5, 1);
        tipoDocumentoConversionMap.put(49, 4);
    }

    public IdentityResponseData consultar(String jsonRequest){

        LocalDateTime timeNow = LocalDateTime.now();

        //TODO: Cambiar cliente quemado en [1]?
        //Obtener informacion del cliente
        Integer customerId = 1;
        VuCustomer customer = vuCustomerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Unable to find Customer with id "+customerId));

        //Crear y guardar transaccion
        Transaction transaction = new Transaction(null, UUID.randomUUID().toString()
                , LocalDateTime.now(), customer, requestType);
        transactionRepository.save(transaction);

        //Validar Json del request
        CitizenInformationRequest request = null;
        try {
            //Validar formato
            try{
                request = objectMapper.readValue(jsonRequest, CitizenInformationRequest.class);
            }catch (JsonProcessingException e){
                throw handleJsonProcessingException(e);
            }

            //Validar restricciones
            Set<ConstraintViolation<CitizenInformationRequest>> errors = validator.validate(request);

            if(errors.size()>0){
                StringBuilder errorsMsg = new StringBuilder();
                for(ConstraintViolation<CitizenInformationRequest> error : errors){
                    errorsMsg.append(error.getPropertyPath()).append(" ").append(error.getMessage()).append(", ");
                }
                errorsMsg.delete(errorsMsg.length()-2, errorsMsg.length());

                throw new ValidationException(errorsMsg.toString());
            }

            //Validar tipo de documento
            if(tipoDocumentoConversionMap.get(request.data.documentType)==null){
                throw new ValidationException("Invalid document type "+request.data.documentType);
            }
        }catch (ValidationException e){
            //En caso de tener errores responder con error de validacion
            return new IdentityResponseData(processValidationException(e, request, transaction
                    , timeNow, customer, ResultCode.ERROR_VALIDACION, 400, "Incorrect parameters"), 400);
        }

        //Iniciar consulta datacretito experian
        WSDataCredito.ConsultarHC2Response hc2Response = null;
        try{
            hc2Response = wsDataCredito.consultarHC2(tipoDocumentoConversionMap
                    .get(request.data.documentType), request.number ,request.data.lastName);
            //Validar respuesta
            if(hc2Response.isSuccess()){
                IdentityExperian identityExperian;
                String identityExperianJson;
                //Validar formato
                try{
                    identityExperianJson = hc2Response.getResult();
                    identityExperian = objectMapper.readValue(identityExperianJson, IdentityExperian.class);
                }catch (JsonProcessingException e){
                    throw handleJsonProcessingException(e);
                }

                //Validar codigo de respuesta
                if(identityExperian.informes.informe.respuesta.equals("14")){
                    IdentityVU identityVU = new IdentityVU(identityExperian);
                    String identityVUJson;

                    //Crear respuesta de validacion de identidad para VU
                    try {
                        identityVUJson = objectMapper.writeValueAsString(identityVU);
                    }catch(JsonProcessingException e){
                        throw handleJsonProcessingException(e);
                    }

                    //Crear registro de validacion exitosa
                    Validation validation = new Validation(null, 200
                            , createValidationMessage(true, null), requestType, timeNow, transaction);

                    //Crear registro de InfoCitizen
                    String consultationDate = consultationDateFormatConverter(identityExperian.informes.informe.fechaConsulta);
                    IdentityExperian.Informe informe = identityExperian.informes.informe;
                    InfoCitizen infoCitizen;
                    //Es nacional o extranjero
                    if(informe.naturalNacional!=null){
                        infoCitizen = new InfoCitizen(null, informe.identificacionDigitada.toString()
                                , informe.tipoIdDigitado.toString(), true, ResultCode.IDENTIDAD_VALIDADA.name()
                                , identityExperianJson, consultationDate, informe.naturalNacional.nombres
                                , informe.naturalNacional.primerApellido, informe.naturalNacional.segundoApellido
                                , informe.naturalNacional.nombreCompleto, informe.naturalNacional.validada
                                , informe.naturalNacional.identificacion.estado, informe.naturalNacional.identificacion.fechaExpedicion
                                , informe.naturalNacional.identificacion.ciudad, informe.naturalNacional.identificacion.departamento
                                , customer, transaction, validation, timeNow, null);

                    }else if(informe.naturalExtranjera!=null){
                        infoCitizen = new InfoCitizen(null, informe.tipoIdDigitado.toString()
                                , informe.identificacionDigitada.toString(), true, ResultCode.IDENTIDAD_VALIDADA.name()
                                , identityExperianJson, consultationDate, null, null, null
                                , informe.naturalExtranjera.nombre, informe.naturalExtranjera.validada
                                , null, null, null, null
                                , customer, transaction, validation, timeNow, informe.naturalExtranjera.nacionalidad);
                    }else{
                        throw new ValidationException("The response could not be processed: Person data not found");
                    }

                    //Guardar registros de Validacion e InfoCitizen
                    validationRepository.save(validation);
                    infoCitizenRepository.save(infoCitizen);

                    //Responder con la validacion de VU
                    return new IdentityResponseData(identityVUJson, 200);
                }else{
                    //En caso de codigo de respuesta incorrecto lanzar error de validacion
                    throw new ValidationException("Person not found");
                }
            }else{
                //En caso de respuesta no exitosa lanzar error de validacion
                String errorMsg;
                if(hc2Response.getError()!=null){
                    errorMsg = hc2Response.getError();
                }else{
                    errorMsg = "Unknown error";
                }
                throw new ValidationException(errorMsg);
            }
        }catch (Exception e){
            //Imprimir en log request y response para debug
            printErrorRequestResponse(request, hc2Response);
            //En caso de tener errores responder con error de validacion
            return new IdentityResponseData(processValidationException(e, request, transaction
                    , timeNow, customer, ResultCode.ERROR_EN_SOLICITUD, 500
                    , "Internal error"), 500);
        }

    }

    //Guarda registro de de error de validacion y crea la respuesta adecuada
    private String processValidationException(Exception e, CitizenInformationRequest request, Transaction transaction
            , LocalDateTime timeNow, VuCustomer customer, ResultCode resultCode, Integer httpCode, String msg){
        log.catching(e);
        ObjectNode errorResponse = objectMapper.createObjectNode();
        errorResponse.put("code", 1000);
        errorResponse.put("message", msg);
        errorResponse.put("reason", e.getMessage());

        Validation validation = new Validation(null,httpCode,
                createValidationMessage(false, e.getMessage()), requestType, timeNow, transaction);
        validationRepository.save(validation);

        if(request!=null){
            InfoCitizen infoCitizen = new InfoCitizen(null, request.number.toString()
                    , request.data.documentType.toString(), false, resultCode.name(), errorResponse.toString()
                    , null, null, request.data.lastName, null, null
                    , null, null, null, null, null
                    , customer, transaction, validation, timeNow, null);
            infoCitizenRepository.save(infoCitizen);
        }
        return errorResponse.toString();
    }

    //Manejar error de procesamiento de json y crear error de validacion
    private ValidationException handleJsonProcessingException(JsonProcessingException e){
        log.catching(e);
        String errorMsg;
        if(e instanceof InvalidFormatException){
            InvalidFormatException invalidFormatException = (InvalidFormatException) e;
            errorMsg = "Cannot deserialize value of type "
                    +invalidFormatException.getTargetType().getSimpleName()+" from String \""
                    +invalidFormatException.getValue()+"\"";
        }else{
            errorMsg = e.getMessage();
        }
        return new ValidationException(errorMsg);
    }

    //Crear mensaje de validacion
    private String createValidationMessage(boolean success, String msg){
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("success", success);
        if(msg!=null){
            objectNode.put("info", msg);
        }
        return objectNode.toString();
    }

    //Convertir formato de fecha de consultacion en respuesta de experian
    private String consultationDateFormatConverter(String dateInput){
        return LocalDateTime.parse(dateInput, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a"));
    }

    //Imprimir request y response para debug
    private void printErrorRequestResponse(CitizenInformationRequest request, WSDataCredito.ConsultarHC2Response response){
        log.error("Retrieve Experian identity information fail");
        if(request!=null){
            try {
                log.error("JSON request was:\n"+objectMapper
                        .writerWithDefaultPrettyPrinter().writeValueAsString(request));
            }catch (Exception ignore){}
        }
        if(response!=null) {
            log.error("XML request was:\n"+response.getRequest());
            log.error("Response was:\n"+response.getResponse());
        }
    }
}
