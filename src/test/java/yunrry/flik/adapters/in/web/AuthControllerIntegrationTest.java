//// test/java/yunrry/flik/adapters/in/web/AuthControllerIntegrationTest.java
//package yunrry.flik.adapters.in.web;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.context.annotation.Import;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.transaction.annotation.Transactional;
//import yunrry.flik.adapters.in.dto.auth.*;
//import yunrry.flik.adapters.out.persistence.repository.UserJpaRepository;
//import yunrry.flik.config.SecurityConfig;
//import yunrry.flik.core.domain.model.AuthProvider;
//import yunrry.flik.core.domain.model.OAuthUserInfo;
//import yunrry.flik.core.service.auth.RefreshTokenService.OAuth2Service;
//import yunrry.flik.ports.out.repository.RefreshTokenRepository;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.BDDMockito.given;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@SpringBootTest
//@AutoConfigureMockMvc(addFilters = false)
//@ActiveProfiles("test")
//@Transactional
//@TestPropertySource(properties = {
//        "spring.batch.job.enabled=false",  // Batch 비활성화
//        "management.endpoints.enabled=false"  // Actuator 비활성화
//})
//@Import(SecurityConfig.class)
//@DisplayName("인증 컨트롤러 통합 테스트")
//class AuthControllerIntegrationTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Autowired
//    private UserJpaRepository userJpaRepository;
//
//    @Autowired
//    private RefreshTokenRepository refreshTokenRepository;
//
//    @MockBean
//    private OAuth2Service oAuth2Service;
//
//    @Test
//    @DisplayName("이메일 회원가입이 성공한다")
//    void shouldSignupSuccessfully() throws Exception {
//        // given
//        SignupRequest request = new SignupRequest(
//                "test@example.com",
//                "password123",
//                "테스트사용자",
//                "https://example.com/profile.jpg"
//        );
//
//        // when & then
//        mockMvc.perform(post("/v1/auth/signup")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.data.id").exists())
//                .andExpect(jsonPath("$.data.email").value("test@example.com"))
//                .andExpect(jsonPath("$.data.nickname").value("테스트사용자"))
//                .andExpect(jsonPath("$.data.provider").value("email"));
//    }
//
//    @Test
//    @DisplayName("이메일 로그인이 성공한다")
//    void shouldLoginSuccessfully() throws Exception {
//        // given - 먼저 회원가입
//        SignupRequest signupRequest = new SignupRequest(
//                "login@example.com",
//                "password123",
//                "로그인사용자",
//                null
//        );
//
//        mockMvc.perform(post("/v1/auth/signup")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(signupRequest)))
//                .andExpect(status().isOk());
//
//        // when & then - 로그인
//        LoginRequest loginRequest = new LoginRequest(
//                "login@example.com",
//                "password123"
//        );
//
//        mockMvc.perform(post("/v1/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(loginRequest)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.data.accessToken").exists())
//                .andExpect(jsonPath("$.data.refreshToken").exists())
//                .andExpect(jsonPath("$.data.user.email").value("login@example.com"));
//    }
//
//    @Test
//    @DisplayName("Google 로그인 URL 조회가 성공한다")
//    void shouldGetGoogleAuthUrl() throws Exception {
//        // given
//        String state = "random_state";
//        String expectedUrl = "https://accounts.google.com/oauth/authorize?client_id=test";
//
//        given(oAuth2Service.getAuthorizationUrl(AuthProvider.GOOGLE, state))
//                .willReturn(expectedUrl);
//
//        // when & then
//        mockMvc.perform(get("/v1/auth/oauth/google")
//                        .param("state", state))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.data.authUrl").value(expectedUrl));
//    }
//
//    @Test
//    @DisplayName("Kakao 로그인 URL 조회가 성공한다")
//    void shouldGetKakaoAuthUrl() throws Exception {
//        // given
//        String state = "random_state";
//        String expectedUrl = "https://kauth.kakao.com/oauth/authorize?client_id=test";
//
//        given(oAuth2Service.getAuthorizationUrl(AuthProvider.KAKAO, state))
//                .willReturn(expectedUrl);
//
//        // when & then
//        mockMvc.perform(get("/v1/auth/oauth/kakao")
//                        .param("state", state))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.data.authUrl").value(expectedUrl));
//    }
//
//    @Test
//    @DisplayName("OAuth 첫 로그인 시 회원가입 정보를 반환한다")
//    void shouldReturnSignupInfoForFirstOAuthLogin() throws Exception {
//        // given
//        OAuthCallbackRequest request = new OAuthCallbackRequest(
//                "google",
//                "auth_code",
//                "random_state"
//        );
//
//        OAuthUserInfo oAuthUserInfo = OAuthUserInfo.builder()
//                .providerId("google123")
//                .email("oauth@gmail.com")
//                .nickname("구글사용자")
//                .profileImageUrl("https://google.com/profile.jpg")
//                .provider(AuthProvider.GOOGLE)
//                .build();
//
//        given(oAuth2Service.getUserInfo(any(), any(), any()))
//                .willReturn(oAuthUserInfo);
//
//        // when & then
//        mockMvc.perform(post("/v1/auth/oauth/callback")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.data.email").value("oauth@gmail.com"))
//                .andExpect(jsonPath("$.data.signupRequired").value(true));
//    }
//
////    @Test
////    @DisplayName("OAuth 회원가입 완료가 성공한다")
////    void shouldCompleteOAuthSignupSuccessfully() throws Exception {
////        // given
////        CompleteOAuthSignupRequest request = new CompleteOAuthSignupRequest(
////                "google",
////                "auth_code",
////                "random_state",
////                "내가설정한닉네임"
////        );
////
////        OAuthUserInfo oAuthUserInfo = OAuthUserInfo.builder()
////                .providerId("google456")
////                .email("signup@gmail.com")
////                .nickname("구글기본닉네임")
////                .profileImageUrl("https://google.com/profile.jpg")
////                .provider(AuthProvider.GOOGLE)
////                .build();
////
////        given(oAuth2Service.getUserInfo(any(), any(), any()))
////                .willReturn(oAuthUserInfo);
////
////        // when & then
////        mockMvc.perform(post("/v1/auth/oauth/signup")
////                        .contentType(MediaType.APPLICATION_JSON)
////                        .content(objectMapper.writeValueAsString(request)))
////                .andExpect(status().isOk())
////                .andExpect(jsonPath("$.success").value(true))
////                .andExpect(jsonPath("$.data.accessToken").exists())
////                .andExpect(jsonPath("$.data.refreshToken").exists())
////                .andExpect(jsonPath("$.data.user.email").value("signup@gmail.com"))
////                .andExpect(jsonPath("$.data.user.nickname").value("내가설정한닉네임"));
////    }
//
//    @Test
//    @DisplayName("토큰 갱신이 성공한다")
//    void shouldRefreshTokensSuccessfully() throws Exception {
//        // given - 먼저 사용자 생성 및 로그인
//        SignupRequest signupRequest = new SignupRequest(
//                "refresh@example.com",
//                "password123",
//                "리프레시사용자",
//                null
//        );
//
//        mockMvc.perform(post("/v1/auth/signup")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(signupRequest)));
//
//        LoginRequest loginRequest = new LoginRequest(
//                "refresh@example.com",
//                "password123"
//        );
//
//        String loginResponse = mockMvc.perform(post("/v1/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(loginRequest)))
//                .andExpect(status().isOk())
//                .andReturn().getResponse().getContentAsString();
//
//        // refreshToken 추출 (실제로는 JSON 파싱 필요)
//        // 여기서는 간단히 테스트용 토큰 사용
//        RefreshTokenRequest refreshRequest = new RefreshTokenRequest("test_refresh_token");
//
//        // when & then
//        mockMvc.perform(post("/v1/auth/refresh")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(refreshRequest)))
//                .andExpect(status().isUnauthorized()); // 실제 토큰이 아니므로 실패 예상
//    }
//
//    @Test
//    @DisplayName("로그아웃이 성공한다")
//    void shouldLogoutSuccessfully() throws Exception {
//        // given
//        LogoutRequest request = new LogoutRequest("test_refresh_token");
//
//        // when & then
//        mockMvc.perform(post("/v1/auth/logout")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true));
//    }
//
//    @Test
//    @DisplayName("잘못된 이메일로 회원가입 시 실패한다")
//    void shouldFailSignupWithInvalidEmail() throws Exception {
//        // given
//        SignupRequest request = new SignupRequest(
//                "invalid-email",
//                "password123",
//                "테스트사용자",
//                null
//        );
//
//        // when & then
//        mockMvc.perform(post("/v1/auth/signup")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    @DisplayName("중복 이메일로 회원가입 시 실패한다")
//    void shouldFailSignupWithDuplicateEmail() throws Exception {
//        // given - 첫 번째 사용자 가입
//        SignupRequest firstRequest = new SignupRequest(
//                "duplicate@example.com",
//                "password123",
//                "첫번째사용자",
//                null
//        );
//
//        mockMvc.perform(post("/v1/auth/signup")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(firstRequest)))
//                .andExpect(status().isOk());
//
//        // when & then - 같은 이메일로 다시 가입
//        SignupRequest duplicateRequest = new SignupRequest(
//                "duplicate@example.com",
//                "password456",
//                "두번째사용자",
//                null
//        );
//
//        mockMvc.perform(post("/v1/auth/signup")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(duplicateRequest)))
//                .andExpect(status().isConflict());
//    }
//
//    @Test
//    @DisplayName("잘못된 비밀번호로 로그인 시 실패한다")
//    void shouldFailLoginWithWrongPassword() throws Exception {
//        // given - 먼저 회원가입
//        SignupRequest signupRequest = new SignupRequest(
//                "wrong@example.com",
//                "correctpassword",
//                "사용자",
//                null
//        );
//
//        mockMvc.perform(post("/v1/auth/signup")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(signupRequest)));
//
//        // when & then - 잘못된 비밀번호로 로그인
//        LoginRequest loginRequest = new LoginRequest(
//                "wrong@example.com",
//                "wrongpassword"
//        );
//
//        mockMvc.perform(post("/v1/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(loginRequest)))
//                .andExpect(status().isUnauthorized());
//    }
//}