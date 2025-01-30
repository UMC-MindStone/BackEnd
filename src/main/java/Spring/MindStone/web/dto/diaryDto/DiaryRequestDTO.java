package Spring.MindStone.web.dto.diaryDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;


public class DiaryRequestDTO {

    @Getter
    @Setter
    public static class DiaryCreationRequestDTO {
        @NotBlank
        private String bodyPart;
        @NotNull
        private LocalDate date;
    }

    @Getter
    @Setter
    public static class DiaryGetRequestDTO {
        private Long id;          // Optional: 사용자 ID
        @NotNull
        private LocalDate date;
    }
}

