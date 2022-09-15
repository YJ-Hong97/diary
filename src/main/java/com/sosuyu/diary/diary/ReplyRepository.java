package com.sosuyu.diary.diary;

import java.util.List;
import java.util.Optional;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.sosuyu.diary.security.Member;


public interface ReplyRepository extends CrudRepository<DReply, Long>,QuerydslPredicateExecutor<DReply>{
	List<DReply> findByDiary(DiaryVO diaryVO);
	Optional<DReply> findByDiaryAndWriter(DiaryVO diary,Member writer);
	public default Predicate makePredicate(DiaryVO diaryVO) {
		BooleanBuilder builder = new BooleanBuilder();
		QDReply reply =QDReply.dReply;
		
		builder.and(reply.diary.eq(diaryVO));
		builder.and(reply.replyNo.gt(0l));
		
		return builder;
	}
}
