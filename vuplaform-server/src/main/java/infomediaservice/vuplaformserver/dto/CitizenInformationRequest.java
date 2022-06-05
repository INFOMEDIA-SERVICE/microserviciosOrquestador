package infomediaservice.vuplaformserver.dto;

import javax.validation.Valid;
import javax.validation.constraints.*;

public class CitizenInformationRequest {
    @NotNull
    public Long number;
    @Valid
    @NotNull
    public Data data;

    public class Data{
        @NotNull
        @Min(0)
        @Max(99)
        public Integer documentType;
        @Size(min = 1 , max = 2)
        public String gender;
        @NotBlank
        public String lastName;
    }
}
