package com.sosuyu.diary.diary;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.ManyToAny;
import org.springframework.data.annotation.CreatedDate;

import com.sosuyu.diary.security.Member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaryVO {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long diaryNo;
	private String date;
	private String title;
	private String weather;
	private String photo;
	private String content;
	@ManyToOne
	private Member writer;
	private String article;
	
}
