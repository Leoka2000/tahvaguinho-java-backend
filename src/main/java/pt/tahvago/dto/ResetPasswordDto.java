package pt.tahvago.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordDto {
    private String code;
    private String newPassword;
}