package Spring.MindStone.apiPayload.code.status;

import Spring.MindStone.apiPayload.code.BaseErrorCode;
import Spring.MindStone.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),

    // 멤버 관련 에러
    MEMEBER_NOT_FOUND(HttpStatus.BAD_REQUEST, "MEMBER4001", "존재하지 않는 사용자입니다."),

    // 감정 관련 에러
    EMOTION_NOT_FOUND(HttpStatus.BAD_REQUEST, "EMOTION4001", "존재하지 않는 감정 ID 입니다."),

    // 습관 관련 에러
    HABIT_NOT_FOUND(HttpStatus.BAD_REQUEST, "HABIT4001", "존재하지 않는 습관 ID 입니다."),

    // 돌 관련 에러
    STONE_NOT_FOUND(HttpStatus.BAD_REQUEST, "STONE4001", "존재하지 않는 돌 ID 입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build();
    }

}
