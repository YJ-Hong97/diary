package com.sosuyu.diary.security;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sosuyu.diary.diary.LetterRepository;
import com.sosuyu.diary.diary.letterApprove;


@Service
public class MemberService implements UserDetailsService{
	@Autowired
	HttpSession httpSession;
	@Autowired
	MemberRepository memberRepository;
	PasswordEncoder passwordEncoder= new BCryptPasswordEncoder();
	@Autowired
	LetterRepository letterRepository;
	
	@Transactional
	public void join(Member member, HttpServletResponse response) {
		String password = passwordEncoder.encode(member.getPassword());
		member.setPassword(password);
		memberRepository.save(member);
		Cookie cookie1 = new Cookie("email",member.getEmail());
		Cookie cookie2 = new Cookie("profile", member.getProfile());
		
		response.addCookie(cookie1);
		response.addCookie(cookie2);
		
		
		
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		UserDetails member = memberRepository.findById(email).filter(u -> u != null).map(u -> new SecurityUser(u)).get();
		httpSession.setAttribute("user", memberRepository.findById(email).get());
		
		Member user = memberRepository.findById(email).get();
		List<Member> bookList = new ArrayList<>();
		letterRepository.findByToAndApprove(user, letterApprove.ACCEPT).forEach(l->{
			bookList.add(l.getFrom());
		});
		letterRepository.findByFromAndApprove(user, letterApprove.ACCEPT).forEach(l->{
			if(!bookList.contains(l.getTo())) {
				bookList.add(l.getTo());
			}
		});
		httpSession.setAttribute("bookList", bookList);
		return member;
	}
	
	
	
}
