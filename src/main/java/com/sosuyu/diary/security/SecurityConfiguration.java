package com.sosuyu.diary.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	@Autowired
	MemberService memberService;
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/css/**", "/js/**", "/images/**", "/lib/**");
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(memberService).passwordEncoder(passwordEncoder());
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
		http.authorizeRequests() // HttpServletRequest에 따라 접근(access)을 제한

				.antMatchers("/signUp/**", "/auth/**").permitAll() // 로그인없이 허용
				.anyRequest().authenticated().and() // anyRequest() 나머지요청은 authenticated() : 인증된 사용자만 접근가능(반드시 로그인을
													// 해야한다.),
				// anonymous():인증되지않은 사용자가 접근가능
				.formLogin() // form 기반으로 인증을 하도록 한다. 로그인 정보는 기본적으로 HttpSession을 이용
				.loginPage("/auth/login") // auth/login로그인 페이지 링크 .... post의 이름이 같다면 loginProcessingUrl생략
				// .successHandler(customSuccessHandler)
				// .loginProcessingUrl("/auth/login")// 이름이 다르게 추가하면 controller에 인증구현 , 생략하거나
				// 이름이 get과 같으면 자동인증처리 ...지금은
				// username이 전달안됨 // 스프링시큐리티가 해당주소로 오는 요청을 가로채서 대신한다.
				// 지금 사용자생성 postMapping에 가지않는 이유?
				// .usernameParameter("username")
				// .passwordParameter("password")
				//.defaultSuccessUrl("redirect:/index") // 로그인 성공 후 리다이렉트 주소
				.permitAll(); // 접근전부허용

		http.logout() // 로그아웃에 관한 설정을 의미
				.logoutRequestMatcher(new AntPathRequestMatcher("/logout")).logoutSuccessUrl("/auth/login") // 로그아웃 성공시
																											// // 주소
				.invalidateHttpSession(true); // 세션 지우기
// csrf(크로스사이트 위조요청에 대한 설정) 토큰 비활성화 (test시에는 disable권장)
		http.exceptionHandling().accessDeniedPage("/accessDenied"); // 403 예외처리 핸들링 권한이 없는 대상이 접속을시도했을 때

	}
}
