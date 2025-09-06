package yunrry.flik.adapters.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import yunrry.flik.adapters.in.dto.Response;
import yunrry.flik.adapters.in.dto.user.*;
import yunrry.flik.core.domain.model.User;
import yunrry.flik.ports.in.usecase.GetUserUseCase;
import yunrry.flik.ports.in.usecase.UpdateUserUseCase;


@Tag(name = "User", description = "사용자 API")
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final GetUserUseCase getUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<Response<UserProfileResponse>> getMyProfile(@AuthenticationPrincipal Long userId) {
        User user = getUserUseCase.getUser(userId);
        UserProfileResponse response = UserProfileResponse.from(user);

        return ResponseEntity.ok(Response.success(response));
    }

    @Operation(summary = "프로필 수정", description = "사용자 프로필 정보를 수정합니다.")
    @PutMapping("/me")
    public ResponseEntity<Response<UserProfileResponse>> updateProfile(
            @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal Long userId) {

        User updatedUser = updateUserUseCase.updateProfile(userId, request.nickname(), request.profileImageUrl());
        UserProfileResponse response = UserProfileResponse.from(updatedUser);

        return ResponseEntity.ok(Response.success(response));
    }
}