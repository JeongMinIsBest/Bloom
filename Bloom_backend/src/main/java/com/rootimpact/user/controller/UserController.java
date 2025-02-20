package com.rootimpact.user.controller;

import com.rootimpact.user.dto.UserDto;
import com.rootimpact.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value = "/api/users", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin("*")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "회원 API", description = "회원")
public class UserController {

    private final UserService userService;

    @Operation(summary = "아이디 중복체크 API", description = "회원가입 과정에서 userId 중복체크")
    @GetMapping("/checkDuplicated/userId/{userId}")
    public ResponseEntity<Map<String, Object>> checkDuplicatedUserId(@PathVariable("userId") String userId) {
        log.info("이메일 중복 체크 요청: {}", userId);
        try {
            boolean isDuplicated = userService.checkDuplicatedUserId(userId);
            Map<String, Object> response = Map.of(
                    "status", isDuplicated ? 400 : 200,
                    "message", isDuplicated ? "중복된 userId" : "사용 가능한 userId",
                    "isDuplicated", isDuplicated
            );
            return ResponseEntity.status(isDuplicated ? HttpStatus.CONFLICT : HttpStatus.OK).body(response);
        } catch (Exception e) {
            log.error("userId 중복 체크 중 오류 발생: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", 500, "message", "서버 에러 입니다"));
        }
    }

    @Operation(summary = "회원가입 API", description = "회원가입 페이지를 통해 입력한 정보로 회원가입")
    @PostMapping
    public ResponseEntity<?> signUp (
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "아이디, 비밀번호 ",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"userId\": \"abcd\", \"password\": \"1234\"}")
                    )
            )
            @RequestBody UserDto user)
    {
        try {
            if (userService.checkDuplicatedUserId(user.getUserId())) {
                log.warn("이미 존재하는 userId로 회원가입 시도: {}", user.getUserId());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        Map.of("status", 400, "message", "중복된 아이디 입니다")
                );
            }
            userService.signUp(user);
            log.info("회원가입 성공: {}", user.getUserId());
            return ResponseEntity.accepted().body(
                    Map.of("status", 202, "message", "회원 가입에 성공했습니다")
            );
        } catch (Exception e) {
            log.error("회원가입 중 오류 발생: {}", user.getUserId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", 500, "message", "서버 에러 입니다"));
        }
    }

    @Operation(summary = "로그인 API", description = "휴대폰 번호로 로그인")
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "아이디, 비밀번호 ",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"userId\": \"abcd\", \"password\": \"1234\"}")
                    )
            )
            @RequestBody UserDto userDto,  // UserDto를 받아서 처리
            HttpServletRequest request  // 세션을 이용하기 위해 HttpServletRequest를 받음
    ) {
        try {
            String userId = userDto.getUserId();
            String password = userDto.getPassword();
            boolean isValidUser = userService.checkLogin(userId, password);

            if (!isValidUser) {
                log.warn("잘못된 로그인 시도: {}", userId);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        Map.of("status", 401, "message", "잘못된 userId.")
                );
            }

            HttpSession session = request.getSession();
            session.setAttribute("user", userId);
            log.info("로그인 성공: {}", userId);

            return ResponseEntity.ok(Map.of(
                    "status", 200,
                    "message", "로그인 성공",
                    "userId", userId
            ));
        } catch (Exception e) {
            log.error("로그인 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", 500, "message", "서버 에러 입니다"));
        }
    }

    @Operation(summary = "로그아웃 API", description = "사용자 로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                String userId = (String) session.getAttribute("user");
                session.invalidate();

                log.info("{} 로그아웃 성공", userId);
                return ResponseEntity.ok(Map.of(
                        "status", 200,
                        "message", "로그아웃 성공",
                        "userId", userId
                ));
            } else {
                log.warn("세션이 존재하지 않습니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        Map.of("status", 400, "message", "세션이 존재하지 않습니다")
                );
            }
        } catch (Exception e) {
            log.error("로그아웃 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", 500, "message", "서버 에러 입니다"));
        }
    }
}
