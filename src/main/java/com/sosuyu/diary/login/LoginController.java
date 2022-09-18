package com.sosuyu.diary.login;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.catalina.authenticator.SpnegoAuthenticator.AcceptAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.sosuyu.diary.diary.DiaryRepository;
import com.sosuyu.diary.diary.DiaryVO;
import com.sosuyu.diary.diary.Letter;
import com.sosuyu.diary.diary.LetterRepository;
import com.sosuyu.diary.diary.LetterType;
import com.sosuyu.diary.diary.letterApprove;
import com.sosuyu.diary.imageUpload.EmptyFileException;
import com.sosuyu.diary.imageUpload.FileUploadFailedException;
import com.sosuyu.diary.imageUpload.UploadS3Service;
import com.sosuyu.diary.security.Member;
import com.sosuyu.diary.security.MemberRepository;
import com.sosuyu.diary.security.MemberService;

import lombok.extern.java.Log;
@Log
@RestController
public class LoginController {
	@Autowired
	UploadS3Service uploadService;
	@Autowired
	MemberService memberService;
	@Autowired
	MemberRepository memberRepository;
	@Autowired
	LetterRepository letterRepository;
	@Autowired
	DiaryRepository diaryRepository;
	@GetMapping("/auth/login")
	public ModelAndView login(ModelAndView mnv, HttpServletRequest request, HttpServletResponse response) {
		Cookie[] cookies = request.getCookies();
		String email = null;
		String profile = null;
		
		for(Cookie cookie:cookies) {
			if(cookie.getName().equals("email")) {
				email = cookie.getValue();
			}else if(cookie.getName().equals("profile")) {
				profile=cookie.getValue();
			}
		}
		mnv.addObject("email",email);
		mnv.addObject("profile",profile);
		mnv.setViewName("auth/login");
		return mnv;
	}
	
	@GetMapping("/auth/signUp")
	public ModelAndView signUp(ModelAndView mnv) {
		mnv.setViewName("auth/signUp");
		return mnv;
	}
	@PostMapping("/auth/signUp")
	public ModelAndView signUp(ModelAndView mnv, String name, Date birth, String password,String email, MultipartFile profile,HttpServletResponse response) throws EmptyFileException, FileUploadFailedException {
		Member member = Member.builder()
				.name(name)
				.birth(birth)
				.password(password)
				.email(email)
				.build();
		String url = uploadService.uploadFile(profile,"profile");

		member.setProfile(url);
		memberService.join(member,response);
		
		mnv.setViewName("redirect:/auth/login");
		return mnv;
		
	}
	@GetMapping("/index")
	public ModelAndView main(ModelAndView mnv, HttpSession session,HttpServletResponse response ) {
		Member member = (Member)session.getAttribute("user");
		List<Letter> letters = letterRepository.findByToAndTypeAndApprove(member,LetterType.FRIEND, null);
		List<Letter> denys = letterRepository.findByFromAndApprove(member, letterApprove.DENY);
		List<Letter> replies = letterRepository.findByToAndTypeAndApprove(member, LetterType.REPLY, null);
		member.setPush(letters.size()+denys.size()+replies.size());
		log.info("----------------------");
		log.info(letters.toString());
		memberRepository.save(member);
		List<Member> bookList = new ArrayList<>();
		letterRepository.findByToAndApprove(member, letterApprove.ACCEPT).forEach(l->{
			bookList.add(l.getFrom());
		});
		letterRepository.findByFromAndApprove(member, letterApprove.ACCEPT).forEach(l->{
			if(!bookList.contains(l.getTo())) {
				bookList.add(l.getTo());
			}
		});
		log.info(bookList.toString());
		session.setAttribute("bookList", bookList);
		Cookie cookie1 = new Cookie("email",member.getEmail());
		Cookie cookie2 = new Cookie("profile", member.getProfile());
		
		response.addCookie(cookie1);
		response.addCookie(cookie2);
		mnv.addObject("denys",denys);
		mnv.addObject("bookList",bookList);
		mnv.addObject("letters",letters);
		mnv.addObject("replies",replies);
		mnv.setViewName("index");
		return mnv;
	}
}
