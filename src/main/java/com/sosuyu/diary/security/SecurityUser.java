package com.sosuyu.diary.security;

import java.util.ArrayList;




import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SecurityUser extends User{
	private static final long serialVersionUID = 1L;
	private static final String ROLE_PREFIX="ROLE_";
    private Member user;   
    
    
	public SecurityUser(String name, String password, Collection<? extends GrantedAuthority> authorities) {
		super(name, password, authorities);
	}
	public SecurityUser(Member user) {	
		super(user.getName(), user.getPassword(), makeRole(user)  );
		this.user = user;
		System.out.println("SecurityUser member:" + user);
	}
	private static List<GrantedAuthority> makeRole(Member user) {
		List<GrantedAuthority> roleList = new ArrayList<>();
		//매니저 아이디와 사용자 아이디 중복 불가. 아이디만 검사하면 매니저인지 아닌지 판별가능
		roleList.add(new SimpleGrantedAuthority(ROLE_PREFIX + "user"));
		
		return roleList;
	}
	 
	//User class에서 username필드가 있지만 google 인증시 사용되는 필드는 name
	//이를 맞추기위해 함수추가함 
	public String getName() {
		// TODO Auto-generated method stub
		return super.getUsername();
	}
	
	
	
	
	
}
