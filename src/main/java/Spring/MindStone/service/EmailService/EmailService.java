package Spring.MindStone.service.EmailService;

import Spring.MindStone.apiPayload.code.status.ErrorStatus;
import Spring.MindStone.apiPayload.exception.handler.AuthHandler;
import Spring.MindStone.apiPayload.exception.handler.MemberInfoHandler;
import Spring.MindStone.domain.member.MemberInfo;
import Spring.MindStone.repository.memberRepository.MemberInfoRepository;
import Spring.MindStone.web.dto.memberDto.MemberInfoRequestDTO;
import Spring.MindStone.web.dto.memberDto.MemberInfoResponseDTO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;
    private final MemberInfoRepository MemberInfoRepository;
    private final MemberInfoRepository memberInfoRepository;

    Map<String, VerificationData> verificationMap = new ConcurrentHashMap<>();
    private final Random random = new Random();

    private static final String CHAR_SET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";

    // 임시 비밀번호 생성
    public String generateTemporaryPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < 10; i++) {
            int index = random.nextInt(CHAR_SET.length());
            password.append(CHAR_SET.charAt(index));
        }

        return password.toString();
    }

    // 임시 비밀번호 전송
    public void sendTemporaryPassword(String email, String temporaryPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[마인드스톤] 임시 비밀번호");
        message.setText("임시 비밀번호: " + temporaryPassword);

        mailSender.send(message);
    }

    // 임시 비밀번호 DB 저장
    public void updatePassword(String email, String temporaryPassword) {
        MemberInfo memberInfo = memberInfoRepository.findByEmail(email)
                .orElseThrow(() -> new MemberInfoHandler(ErrorStatus.MEMBER_NOT_FOUND));

        String encodedPassword = passwordEncoder.encode(temporaryPassword);
        memberInfo.setPassword(encodedPassword);

        memberInfoRepository.save(memberInfo);
    }

    // 임시 비밀번호 로직
    public void handleTemporaryPassword(MemberInfoRequestDTO.EmailDto request) {
        String temporaryPassword = generateTemporaryPassword();
        sendTemporaryPassword(request.getEmail(), temporaryPassword);
        updatePassword(request.getEmail(), temporaryPassword);
    }

    // 인증번호 생성
    public String generateVerificationCode() {
        return String.valueOf(1000 + random.nextInt(9000));
    }

    // 인증번호 전송
    public void sendVerificationCode(String email, String verificationCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("마인드 스톤 회원가입 인증번호 안내");
        message.setText("안녕하세요.\n마인드 스톤을 이용해 주셔서 감사합니다.\n아래의 인증번호를 입력하여 회원가입을 완료해 주세요.\n\n" +
                "인증번호: " + verificationCode + "\n\n해당 인증번호는 5분 후에 만료됩니다. 5분 내로 입력하여 주시기 바랍니다.\n" +
                "궁금한 사항이 있으시면 고객센터로 문의해 주세요.\n\n감사합니다.\n마인드 스톤 드림");

        mailSender.send(message);
    }

    // 인증번호 저장
    public void saveVerificationCode(String email, String code) {
        verificationMap.put(email, new VerificationData(code, LocalDateTime.now()));
    }

    // 인증번호 로직
    public void handleVerificationCode(MemberInfoRequestDTO.EmailDto request) {
        String verificationCode = generateVerificationCode();
        sendVerificationCode(request.getEmail(), verificationCode);
        saveVerificationCode(request.getEmail(), verificationCode);
    }

    // 인증번호 검증
    public MemberInfoResponseDTO.EmailDto verifyCode(MemberInfoRequestDTO.VerificationDto request) {
        VerificationData data = verificationMap.get(request.getEmail());

        if (data == null || data.isExpired()) {
            throw new AuthHandler(ErrorStatus.VERIFICATION_CODE_EXPIRED);
        }

        if (!data.getCode().equals(request.getCode())) {
            throw new AuthHandler(ErrorStatus.VERIFICATION_CODE_WRONG);
        }

        return MemberInfoResponseDTO.EmailDto.builder()
                .email(request.getEmail())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Getter
    private static class VerificationData {
        private final String code;
        private final LocalDateTime createdAt;

        public VerificationData(String code, LocalDateTime createdAt) {
            this.code = code;
            this.createdAt = createdAt;
        }

        public boolean isExpired() {
            return createdAt.plusMinutes(5).isBefore(LocalDateTime.now());
        }
    }

}
