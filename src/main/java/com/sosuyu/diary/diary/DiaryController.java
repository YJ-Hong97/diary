package com.sosuyu.diary.diary;

import static org.hamcrest.CoreMatchers.nullValue;

import java.time.LocalDate;

import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.modelmbean.ModelMBeanNotificationBroadcaster;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import javax.websocket.server.PathParam;

import org.json.simple.JSONObject;
import org.junit.validator.PublicClassValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.sosuyu.diary.imageUpload.EmptyFileException;
import com.sosuyu.diary.imageUpload.FileUploadFailedException;
import com.sosuyu.diary.imageUpload.UploadS3Service;
import com.sosuyu.diary.security.Member;
import com.sosuyu.diary.security.MemberRepository;
import com.sosuyu.diary.util.PageMaker;
import com.sosuyu.diary.util.PageVO;

import lombok.extern.java.Log;
@Log
@RestController
public class DiaryController {
	@Autowired
	UploadS3Service uploadS3Service;
	@Autowired
	DiaryRepository diaryRepository;
	@Autowired
	SimpMessagingTemplate template;
	@Autowired
	MemberRepository memberRepository;
	@Autowired
	LetterRepository letterRepository;
	@Autowired
	ReplyRepository replyRepository;
	@GetMapping("/writeMyDiary")
	public ModelAndView write(ModelAndView mnv, HttpSession session) {
		Member member = (Member) session.getAttribute("user");
		String now = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
		diaryRepository.findByDateAndWriter(now, member).ifPresentOrElse(d->{
			mnv.addObject("diary",d);
		}, new Runnable() {
			
			@Override
			public void run() {
				mnv.addObject("diary",null);
			}
		});
		List<DiaryVO> diaryList = diaryRepository.findByWriter(member);
		mnv.addObject("diaryList",diaryList);
		mnv.setViewName("/diary/writeMyDiary");
		return mnv;
	}
	@GetMapping("/lookMyDiary/{date}")
	public ModelAndView view(ModelAndView mnv, HttpSession session,@PathVariable("date")String date,PageVO pageVO) {
		Member member = (Member)session.getAttribute("user");
		date = date.replaceAll("-", ".");
		DiaryVO diary = diaryRepository.findByDateAndWriter(date, member).get();
		List<DiaryVO> diaryList = diaryRepository.findByWriter(member);
		Pageable page = pageVO.makePageable(0,0,"replyNo");
		Page<DReply> replies = replyRepository.findAll(replyRepository.makePredicate(diary), page);
		mnv.addObject("replies",new PageMaker<>(replies));
		mnv.addObject("diary",diary);
		mnv.addObject("diaryList",diaryList);
		mnv.setViewName("/diary/writeMyDiary");
		return mnv;
		
	}
	@GetMapping("/lookMyDiary/{date}/{pageNo}")
	public ModelAndView view(ModelAndView mnv, HttpSession session,@PathVariable("date")String date,PageVO pageVO,@PathVariable("pageNo")int pageNo) {
		Member member = (Member)session.getAttribute("user");
		date = date.replaceAll("-", ".");
		DiaryVO diary = diaryRepository.findByDateAndWriter(date, member).get();
		List<DiaryVO> diaryList = diaryRepository.findByWriter(member);
		Pageable page = pageVO.makePageable(0,pageNo,"replyNo");
		Page<DReply> replies = replyRepository.findAll(replyRepository.makePredicate(diary), page);
		mnv.addObject("replies",new PageMaker<>(replies));
		mnv.addObject("diary",diary);
		mnv.addObject("diaryList",diaryList);
		mnv.setViewName("/diary/writeMyDiary");
		return mnv;
		
	}
	@PostMapping("/writeMyDiary")
	public ModelAndView write(ModelAndView mnv,HttpSession session,String title,String content,MultipartFile photo,String weather,String article) throws EmptyFileException, FileUploadFailedException {
		Member member = (Member)session.getAttribute("user");
		String url = uploadS3Service.uploadFile(photo, "diary");
		String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
		diaryRepository.findByDateAndWriter(date, member).ifPresentOrElse(d->{
			d.setArticle(article);
			d.setContent(content);
			d.setPhoto(url);
			d.setTitle(title);
			d.setWeather(weather);
			diaryRepository.save(d);
		}, new Runnable() {
			
			@Override
			public void run() {
				DiaryVO diary = DiaryVO.builder()
						.writer(member)
						.weather(weather)
						.title(title)
						.content(content)
						.photo(url)
						.date(date)
						.article(article)
						.build();
				diaryRepository.save(diary);
			}
		});
		
		mnv.setViewName("redirect:/index");
		return mnv;
		
	}
	@MessageMapping("/email/{yourEmail}")
	public void offer(JSONObject object) {
		String email = (String) object.get("from");
		String myEmail = (String) object.get("to");
		Member member = memberRepository.findById(myEmail).get();
		Member from = memberRepository.findById(email).get();
		Letter letter = Letter.builder()
				.from(from)
				.to(member)
				.type(LetterType.FRIEND)
				.content("친구하자!")
				.build();
		letterRepository.save(letter);
		member.setPush(member.getPush()+1);
		memberRepository.save(member);
		Map<String, Object> map = new HashMap<>();
		map.put("push", member.getPush());
		map.put("letter", letter);
		
		template.convertAndSend("/sub/email/"+myEmail,map);
	}
	@GetMapping("/accept/{letterId}")
	public ModelAndView accept(ModelAndView mnv, HttpSession session, @PathVariable("letterId")Long letterId) {
		Member member = (Member)session.getAttribute("user");
		Letter letter = letterRepository.findById(letterId).get();
		letter.setApprove(letterApprove.ACCEPT);
		letterRepository.save(letter);
		member.setPush(member.getPush()-1);
		memberRepository.save(member);
		
		mnv.setViewName("redirect:/index");
		return mnv;
	}
	@Transactional
	@GetMapping("/deny/{letterId}")
	public ModelAndView deny(ModelAndView mnv,@PathVariable("letterId")Long letterId) {
		Letter letter = letterRepository.findById(letterId).get();
		letter.setApprove(letterApprove.DENY);
		letterRepository.save(letter);
		mnv.setViewName("redirect:/index");
		return mnv;
	}
	@GetMapping("/lookYourDiary/{yourEmail}")
	public ModelAndView yourDiary(ModelAndView mnv, @PathVariable("yourEmail")String yourEmail,PageVO pageVO) {
		Member you = memberRepository.findById(yourEmail).get();
		List<DiaryVO> yourList = diaryRepository.findByWriterOrderByDiaryNoDesc(you);
		DiaryVO lastDiary = yourList.get(0);
		Pageable page = pageVO.makePageable(0,0,"replyNo");
		Page<DReply> replies = replyRepository.findAll(replyRepository.makePredicate(lastDiary), page);
		mnv.addObject("replies",new PageMaker<>(replies));
		mnv.addObject("diary",lastDiary);
		mnv.addObject("diaryList",yourList);
		mnv.setViewName("/diary/yourDiary");
		return mnv;
		
	}
	
	@GetMapping("/lookYourDiary/{yourEmail}/{date}")
	public ModelAndView yourDiaryDate(ModelAndView mnv,@PathVariable("yourEmail")String yourEmail,@PathVariable("date")String date,PageVO pageVO) {
		Member you = memberRepository.findById(yourEmail).get();
		date = date.replaceAll("-",".");
		DiaryVO diary = diaryRepository.findByDateAndWriter(date, you).get();
		List<DiaryVO> diaryList = diaryRepository.findByWriter(you);
		Pageable page = pageVO.makePageable(0,0,"replyNo");
		Page<DReply> replies = replyRepository.findAll(replyRepository.makePredicate(diary), page);
		mnv.addObject("replies",new PageMaker<>(replies));
		mnv.addObject("diary",diary);
		mnv.addObject("diaryList",diaryList);
		mnv.setViewName("/diary/yourDiary");
		return mnv;
		
	}
	@GetMapping("/lookYourDiary/{yourEmail}/{date}/{pageNo}")
	public ModelAndView yourDiaryDatePage(ModelAndView mnv,@PathVariable("yourEmail")String yourEmail,@PathVariable("date")String date,PageVO pageVO,@PathVariable("pageNo")String pageNo) {
		Member you = memberRepository.findById(yourEmail).get();
		date = date.replaceAll("-",".");
		DiaryVO diary = diaryRepository.findByDateAndWriter(date, you).get();
		List<DiaryVO> diaryList = diaryRepository.findByWriter(you);
		Pageable page = pageVO.makePageable(0,Integer.parseInt(pageNo),"replyNo");
		Page<DReply> replies = replyRepository.findAll(replyRepository.makePredicate(diary), page);
		
		mnv.addObject("replies",new PageMaker<>(replies));
		mnv.addObject("diary",diary);
		mnv.addObject("diaryList",diaryList);
		mnv.setViewName("/diary/yourDiary");
		return mnv;
		
	}
	@MessageMapping("/deny")
	public void denyUpdate(JSONObject object) {
		Long letterId =Long.parseLong(object.get("letterId").toString());
		String email = letterRepository.findById(letterId).get().getFrom().getEmail();
		template.convertAndSend("/sub/deny/"+email,object);
	}
	@Transactional
	@GetMapping("/confirmDeny/{letterId}")
	public ModelAndView confirmDeny(ModelAndView mnv,@PathVariable("letterId")String letterId) {
		letterRepository.deleteById(Long.parseLong(letterId));
		mnv.setViewName("redirect:/index");
		return mnv;
	}
	@PostMapping("/writeReply")
	public ModelAndView writeReply(ModelAndView mnv,String content,String diaryNo, HttpSession session) {
		Member member = (Member) session.getAttribute("user");
		DiaryVO diary = diaryRepository.findById(Long.parseLong(diaryNo)).get();
		String yourEmail = diary.getWriter().getEmail();
		String date = diary.getDate().split("%")[0];
		replyRepository.findByDiaryAndWriter(diary, member).ifPresentOrElse(r->{
			r.setContent(content);
			replyRepository.save(r);
		}, new Runnable() {
			
			@Override
			public void run() {
				
				DReply reply = DReply.builder()
						.content(content)
						.diary(diary)
						.writer(member)
						.build();
				replyRepository.save(reply);
			}
		});
		DReply reply = replyRepository.findByDiaryAndWriter(diary, member).get();
		Letter letter = Letter.builder()
				.from(member)
				.to(diary.getWriter())
				.type(LetterType.REPLY)
				.content("댓글왔숑")
				.reply(reply)
				.build();
		letterRepository.save(letter);
		
		mnv.setViewName("redirect:/lookYourDiary/"+yourEmail+"/"+date);
		return mnv;
	}
	@Transactional
	@GetMapping("/lookReply/{letterId}")
	public ModelAndView lookReply(ModelAndView mnv,HttpSession session,@PathVariable("letterId")String letterId) {
		Member member = (Member) session.getAttribute("user");
		Letter letter = letterRepository.findById(Long.parseLong(letterId)).get();
		DiaryVO diary = letter.getReply().getDiary();
		member.setPush(member.getPush()-1);
		memberRepository.save(member);
		letterRepository.delete(letter);
		mnv.setViewName("redirect:/lookMyDiary/"+diary.getDate());
		return mnv;
		
	}
	@GetMapping("/lazyConfirm/{letterId}")
	public ModelAndView lazyConfirm(ModelAndView mnv,@PathVariable("letterId")String letterId) {
		Letter letter = letterRepository.findById(Long.parseLong(letterId)).get();
		letterRepository.delete(letter);
		mnv.setViewName("redirect:/index");
		return mnv;
	}
}
