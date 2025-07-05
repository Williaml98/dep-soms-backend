//package com.dep.soms.dto.incident;
//
//import lombok.*;
//
//import java.time.LocalDateTime;
//
//@Getter
//@Setter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class AttachmentDto {
//    private Long id;
//    private String fileName;
//    private String fileType;
//    private String fileUrl;
//    private LocalDateTime uploadTime;
//}

package com.dep.soms.dto.incident;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentDto {

    private Long id;
    private String fileName;
    private String fileType;
    private String fileUrl; // This will point to /api/uploads/{filename}
    private Long fileSize;
    private String uploadedByName;
    private LocalDateTime uploadTime;
}

