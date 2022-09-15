package com.sosuyu.diary.diary;

import java.sql.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

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
public class DReply {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long replyNo;
	private String content;
	@CreationTimestamp
	private Date date;
	@ManyToOne
	private Member writer;
	@ManyToOne
	private DiaryVO diary;
}
