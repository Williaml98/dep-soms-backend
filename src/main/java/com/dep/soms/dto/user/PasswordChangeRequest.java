//package com.dep.soms.dto.user;
//
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.Size;
//import lombok.Data;
//import lombok.Getter;
//import lombok.Setter;
//
//@Getter
//@Setter
//public class PasswordChangeRequest {
//    @NotBlank
//    private String currentPassword;
//
//    @NotBlank
//    @Size(min = 6, max = 40)
//    private String newPassword;
//
//    @NotBlank
//    private String confirmPassword;
//}

package com.dep.soms.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordChangeRequest {
    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 6, max = 40, message = "New password must be between 6 and 40 characters")
    private String newPassword;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    @Override
    public String toString() {
        return "PasswordChangeRequest{" +
                "currentPassword=" + (currentPassword != null ? "[PROVIDED]" : "[NULL]") +
                ", newPassword=" + (newPassword != null ? "[PROVIDED]" : "[NULL]") +
                ", confirmPassword=" + (confirmPassword != null ? "[PROVIDED]" : "[NULL]") +
                '}';
    }
}
