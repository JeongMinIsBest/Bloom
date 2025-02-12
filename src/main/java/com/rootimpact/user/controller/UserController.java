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

@RestController
@RequestMapping(value = "/api/users", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin("*")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "회원 API", description = "회원")
public class UserController {

    private final UserService userService;

    @Operation(summary = "userId 중복 체크 API", description = "회원 가입 시 userId 중복인지 확인")
    @GetMapping("/checkDuplicated/userId/{userId}")
    public ResponseEntity<String> checkDuplicatedUserId(
            @Parameter(description = "userId", required = true, example = "abcd")
            @PathVariable("userId") String userId) {
        log.info("이메일 중복 체크 요청: {}", userId);
        try {
            boolean isDuplicated = userService.checkDuplicatedUserId(userId);

            if (!isDuplicated) {
                log.info("사용 가능한 userId입니다: {}", userId);
                return ResponseEntity.ok("사용 가능한 phone");
            } else {
                log.warn("중복된 userId 입니다: {}", userId);
                return ResponseEntity.status(HttpStatus.CONFLICT).body("중복된 userId");
            }
        } catch (Exception e) {
            log.error("userId 중복 체크 중 오류 발생: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 에러 입니다");
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
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("중복된 아이디 입니다");
            }
            userService.signUp(user);
            log.info("회원가입 성공: {}", user.getUserId());
            return ResponseEntity.accepted().body("회원 가입에 성공했습니다");
        } catch (Exception e) {
            log.error("회원가입 중 오류 발생: {}", user.getUserId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 에러 입니다");
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
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("잘못된 userId.");
            }

            // 로그인 성공 시 세션에 사용자 정보 저장
            HttpSession session = request.getSession();
            session.setAttribute("user", userId);

            log.info("로그인 성공: {}", userId);
            return ResponseEntity.ok("로그인 성공 " + userId);

        } catch (Exception e) {
            log.error("로그인 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 에러 입니다");
        }
    }

    @Operation(summary = "로그아웃 API", description = "사용자 로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false); // 세션이 없으면 null 반환
            if (session != null) {
                String userId = (String) session.getAttribute("userId"); // 세션에서 userId 가져오기
                session.invalidate(); // 세션 무효화로 사용자 정보 삭제

                if (userId != null) {
                    log.info("{} 로그아웃 성공", userId); // 로그에 userId 추가
                    return ResponseEntity.ok(userId + " 로그아웃 성공");
                } else {
                    log.info("로그아웃 성공");
                    return ResponseEntity.ok("로그아웃 성공");
                }
            } else {
                log.warn("세션이 존재하지 않습니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("세션이 존재하지 않습니다");
            }
        } catch (Exception e) {
            log.error("로그아웃 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 에러 입니다");
        }
    }
}
