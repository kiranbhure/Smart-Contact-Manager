package com.smart.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name="CONTACT")
public class Contact {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int CId;
	
	@NotBlank(message = "Name field is required !!")
	@Size(min=2, max=20, message = "minimum 2 and maximum 20 characters allowed !!")
	private String name;
	
	private String nickName;
	
	@NotBlank(message = "Work field is required !!")
	private String work;
	
	@Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}", message = "Invalid Email !!")
	@NotBlank(message = "Email field is required !!")
	private String email;
	
	@Size(min=10, max=10, message = "Invalid Number !!")
	private String phone;
	
	private String image;
	
	@Lob
	private String description;
	
	@ManyToOne
	@JsonIgnore
	private User user;

	public Contact() {
		super();
	}

	public Contact(int cId, String name, String nickName, String work, String email, String phone, String image,
			String description) {
		super();
		CId = cId;
		this.name = name;
		this.nickName = nickName;
		this.work = work;
		this.email = email;
		this.phone = phone;
		this.image = image;
		this.description = description;
	}

	public int getCId() {
		return CId;
	}

	public void setCId(int cId) {
		CId = cId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getWork() {
		return work;
	}

	public void setWork(String work) {
		this.work = work;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	@ManyToOne
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public String toString() {
		return "Contact [CId=" + CId + ", name=" + name + ", nickName=" + nickName + ", work=" + work + ", email="
				+ email + ", phone=" + phone + ", image=" + image + ", description=" + description + ", user=" + user
				+ "]";
	}
	
	
	
}
