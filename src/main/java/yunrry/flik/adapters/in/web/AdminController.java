package yunrry.flik.adapters.in.web;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import yunrry.flik.adapters.in.dto.Response;
import yunrry.flik.core.domain.exception.UnauthorizedException;
import yunrry.flik.core.service.JwtTokenProvider;

// adapters/in/web/AdminController.java
@RestController
@RequestMapping("/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final JwtTokenProvider jwtTokenProvider;

    @Value("${admin.master-key}")
    private String adminMasterKey;

    @PostMapping("/token")
    public ResponseEntity<Response<AdminTokenResponse>> createAdminToken(@RequestBody AdminTokenRequest request) {
        // 관리자 인증 로직 (예: 마스터 키 확인)
        if (!adminMasterKey.equals(request.masterKey())) {  // 환경변수 값과 비교
            throw new UnauthorizedException("Invalid master key");
        }

        String adminToken = jwtTokenProvider.createAdminToken(1L); // 관리자 ID
        AdminTokenResponse response = new AdminTokenResponse(adminToken);

        return ResponseEntity.ok(Response.success(response));
    }

    public record AdminTokenRequest(String masterKey) {}
    public record AdminTokenResponse(String adminToken) {}
}