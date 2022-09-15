package com.sosuyu.diary.diary;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.sosuyu.diary.security.Member;

public interface LetterRepository extends CrudRepository<Letter, Long>{
	List<Letter> findByTo(Member member);
	List<Letter> findByToAndTypeAndApprove(Member member,LetterType type,letterApprove approve);
	List<Letter> findByToAndApprove(Member member,letterApprove approve);
	List<Letter> findByFromAndApprove(Member member,letterApprove approve );
	Optional<Letter> findByReply(DReply reply);
}
