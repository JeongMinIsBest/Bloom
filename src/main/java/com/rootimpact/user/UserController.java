//package com.rootimpact.user;
//
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.media.Content;
//import io.swagger.v3.oas.annotations.media.Schema;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//@RestController
//@RequestMapping("/api/users")
//@CrossOrigin("*")
//@RequiredArgsConstructor
//@Slf4j
//@Tag(name = "회원 API", description = "회원")
//public class UserController {
//
//    @Operation(summary = "회원가입 API", description = "회원가입 페이지를 통해 입력한 정보로 회원가입")
//    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<?> signUp (
//            @io.swagger.v3.oas.annotations.parameters.RequestBody(
//                    description = "이름, 전화번호, 아이디, 패스워드 ",
//                    required = true,
//                    content = @Content(
//                            mediaType = "application/json",
//                            schema = @Schema(
//                                    example = "{\"name\": \"김블룸\", \"phone\": \"010-1234-5678\", " +
//                                            "\"userId\": \"bloom\", \"password\": \"1234\"}"
//                            )
//                    )
//            )
//            @RequestPart("user") UserDTO user,
//            @RequestPart("profileImage") MultipartFile profileImage) {
//        log.info("회원가입 요청: {}", user.getEmail());
//        try {
//            if (userService.checkDuplicatedEmail(user.getEmail())) {
//                log.warn("이미 존재하는 이메일로 회원가입 시도: {}", user.getEmail());
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("중복된 email 입니다");
//            }
//
//            userService.signUp(user, profileImage);
//            log.info("회원가입 성공: {}", user.getEmail());
//            return ResponseEntity.accepted().body("회원 가입에 성공했습니다");
//        } catch (Exception e) {
//            log.error("회원가입 중 오류 발생: {}", user.getEmail(), e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 에러 입니다");
//        }
//    }
//
//}
