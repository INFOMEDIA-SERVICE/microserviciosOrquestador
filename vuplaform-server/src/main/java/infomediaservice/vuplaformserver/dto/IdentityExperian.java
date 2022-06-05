package infomediaservice.vuplaformserver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IdentityExperian {
    @JsonProperty("Informes")
    public Informes informes;

    public static class Edad{
        public Integer min;
        public Integer max;
    }

    public static class Identificacion{
        public String fechaExpedicion;
        public String estado;
        public Long numero;
        public String ciudad;
        public String departamento;
    }

    public static class NaturalNacional{
        public Boolean rut;
        @JsonProperty("Edad")
        public Edad edad;
        @JsonProperty("Identificacion")
        public Identificacion identificacion;
        public String primerApellido;
        public String segundoApellido;
        public Boolean validada;
        public String nombreCompleto;
        public String nombres;
       /* @JsonProperty("InfoDemografica")
        public String infoDemografica;*/
    }

    public static class NaturalExtranjera{
        public Boolean rut;
        @JsonProperty("Llave")
        public Long llave;
        public String numero;
        public Boolean validada;
        public String nombre;
        public String nacionalidad;
       /* @JsonProperty("InfoDemografica")
        public String infoDemografica;*/
    }

    public static class Informe{
        public String fechaConsulta;
        public Integer tipoIdDigitado;
        public Long identificacionDigitada;
        public String apellidoDigitado;
        public String respuesta;
        public String codSeguridad;
        @JsonProperty("NaturalNacional")
        public NaturalNacional naturalNacional;
        @JsonProperty("NaturalExtranjera")
        public NaturalExtranjera naturalExtranjera;
    }

    public static class Informes{
        @JsonProperty("Informe")
        public Informe informe;
    }
}
