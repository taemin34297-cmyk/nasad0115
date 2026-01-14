package com.example.nasda.controller;

import com.example.nasda.domain.UserEntity;
import com.example.nasda.service.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.nasda.dto.post.PostViewDto;
import com.example.nasda.repository.CommentRepository;
import com.example.nasda.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import com.example.nasda.dto.UserJoinDto;
import com.example.nasda.service.UserService;


import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final LoginService loginService;
    private final UserService userService;
    private final PostService postService;
    private final CommentRepository commentRepository;

    @GetMapping("/login")
    public String loginForm() {
        return "user/login";
    }

    // ✅ 추가 1: 회원가입 화면 GET
    @GetMapping("/signup")
    public String signupForm() {
        return "user/signup";
    }

    // ✅ 추가 2: 마이페이지 화면 GET
    @GetMapping("/mypage")
    public String mypage(HttpSession session, Model model) {
        UserEntity loginUser = (UserEntity) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/user/login";

        Integer userId = loginUser.getUserId();

        // 템플릿이 요구하는 모델 값들
        model.addAttribute("user", loginUser);

        model.addAttribute("postCount", postService.countMyPosts(userId));
        model.addAttribute("commentCount", commentRepository.countByUserId(userId));

        List<PostViewDto> myPosts = postService.getMyRecentPosts(userId, 4);
        model.addAttribute("myPosts", myPosts); // ✅ 절대 null이면 안 됨

        return "user/mypage";
    }

    @PostMapping("/login")
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        HttpSession session,
                        Model model) {
        try {
            UserEntity loginUser = loginService.login(username, password);

            if (loginUser != null) {
                session.setAttribute("loginUser", loginUser);

                UsernamePasswordAuthenticationToken token =
                        new UsernamePasswordAuthenticationToken(
                                loginUser.getLoginId(),
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_USER"))
                        );

                SecurityContextHolder.getContext().setAuthentication(token);

                session.setAttribute(
                        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                        SecurityContextHolder.getContext()
                );

                return "redirect:/";
            }

            model.addAttribute("errorMessage", "아이디 또는 비밀번호가 일치하지 않습니다.");
            return "user/login";

        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "user/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request,
                         HttpServletResponse response,
                         HttpSession session) {

        session.invalidate();
        new SecurityContextLogoutHandler().logout(
                request, response, SecurityContextHolder.getContext().getAuthentication()
        );

        return "redirect:/";
    }

    @PostMapping("/signup")
    public String signup(@RequestParam String loginId,
                         @RequestParam String password,
                         @RequestParam String nickname,
                         @RequestParam String email,
                         Model model) {
        try {
            UserJoinDto dto = new UserJoinDto(loginId, password, nickname, email);
            userService.join(dto);

            return "redirect:/user/login";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "user/signup";
        }
    }
}
