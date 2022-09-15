package com.sosuyu.diary.diary;

import java.sql.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.swing.text.AbstractDocument.Content;

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
public class Letter {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long letterId;
	@ManyToOne
	private Member from;
	@ManyToOne
	private Member to;
	private letterApprove approve;
	private LetterType type;
	@CreationTimestamp
	private Date date;
	private String content;
	@ManyToOne
	private DReply reply;
}
