package com.sosuyu.diary.diary;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.sosuyu.diary.security.Member;

public interface DiaryRepository extends CrudRepository<DiaryVO, Long>{
	Optional<DiaryVO> findByDateAndWriter(String date,Member member);
	List<DiaryVO> findByWriter(Member member);
	List<DiaryVO> findByWriterOrderByDiaryNoDesc(Member writer);
}
