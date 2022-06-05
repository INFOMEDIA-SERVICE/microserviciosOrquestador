package infomediaservice.vuplaformserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IdentityResponseData {
    public String response;
    public Integer statusCode;
}
