package com.hdu.yuan.heartrate.object;

public class People {
	private int id;
	private String name;
	private String sex;
	private String age;
	private String identifydata;
	private int size;
	private String imagePath;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	public String getAge() {
		return age;
	}
	public void setAge(String age) {
		this.age = age;
	}
	public String getImagePath() {
		return imagePath;
	}
	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}
	public String getIdentifydata() {
		return identifydata;
	}
	public void setIdentifydata(String identifydata) {
		this.identifydata = identifydata;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}	
//	@Override
//	public String toString() {
//		return "People [id=" + id + ", name=" + name + ", sex=" + sex
//				+ ", age=" + age + ", imagePath=" + imagePath + "]";
//	}
//	
	@Override
	public String toString() {
		return "People [id=" + id + ", name=" + name + ", sex=" + sex
				+ ", age=" + age + ", identifydata=" + identifydata + ", size="
				+ size + ", imagePath=" + imagePath + "]";
	}
}
