package util.web;

import java.util.ArrayList;

public class WebList {
    ArrayList<String> list;
	
    String heading;
	String description;
	
	public WebList(ArrayList<String> list, String heading, String description) {
		this.list = list;
		this.heading = heading;
		this.description = description;
	}
	
	public ArrayList<String> getList() {
		return list;
	}
	public void setList(ArrayList<String> list) {
		this.list = list;
	}
	public String getHeading() {
		return heading;
	}
	public void setHeading(String heading) {
		this.heading = heading;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public String toString() {
		return  list+"#@"+heading + "#@" + description;
	}
}
