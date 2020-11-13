package com.chatApp.sp.utils;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.chatApp.sp.model.DBGroup;
import com.chatApp.sp.model.DBUser;
import com.chatApp.sp.repository.GroupMessageRepository;
import com.chatApp.sp.repository.GroupRepository;
import com.chatApp.sp.repository.UserRepository;


@Component
public class GroupUtils {
	
	@Autowired
	GroupMessageRepository groupMessRepo;
	
	@Autowired
	GroupRepository groupRepo;
	
	@Autowired
	UserRepository userRepo;
	
	@Autowired
	CookieUtils cookieUtils;
	
	private Map<String, String> getUserGroups(DBUser user){
		Map<String, String> userGroups = user.getGroup();
		if(userGroups == null)
			return new HashMap<String, String>();
		return userGroups;
	}
	
	private void addUserGroup(String email, String groupId, String groupName) {
		DBUser user = userRepo.findByEmail(email);
		Map<String, String> userGroups = getUserGroups(user);
		userGroups.put(groupId, groupName);
		user.setGroup(userGroups);
		userRepo.save(user);
	}
	
	private void deleteUserGroup(String email, String groupId) {
		DBUser user = userRepo.findByEmail(email);
		Map<String, String> groups = getUserGroups(user);
		groups.remove(groupId);
		user.setGroup(groups);
		userRepo.save(user);
	}
	
	public boolean createGroup(String groupName, String manager) {
				
		DBGroup group = new DBGroup( manager, groupName);	
		Map<String, String> members = new HashMap<String, String>();
		members.put(manager, userRepo.findByEmail(manager).getUserName());
		group.setMembers(members);
		addUserGroup(manager, group.getGroupId(), group.getGroupName());
		groupRepo.insert(group);
		return true;
	}
	
	public DBGroup getGroupInfo(String groupId) {
		return groupRepo.findByGroupId(groupId);
	}
	
	public String deleteGroup(String groupId,String email, HttpServletRequest req) throws Exception {
		
		//String email = cookieUtils.getEmail(req);
		
		DBGroup group = groupRepo.findByGroupId(groupId);
		Map<String, String> members = group.getMembers();

			if(group.getManager().equals(email)) {
				groupRepo.delete(group);
				
				for (Map.Entry<String, String> m : members.entrySet()) {
					deleteUserGroup(m.getKey(), groupId);
				}
				return "SUCCEED";
			}else throw new Exception("something wrong!");
	}
	
	public String leaveGroup(String groupId,String email,  HttpServletRequest req) throws Exception {
		
		//String email = cookieUtils.getEmail(req);
		
		DBGroup group = groupRepo.findByGroupId(groupId);
		
		Map<String, String> groupMembers = group.getMembers();
		
		if(groupMembers.containsKey(email)) {
			deleteUserGroup(email, groupId);
			groupMembers.remove(email);
			group.setMembers(groupMembers);
			groupRepo.save(group);
			return "SUCCEED";
		}else throw new Exception("You are not a members of that group");
	}
	
	
	public String deleteMember(String groupId,String email, String member, HttpServletRequest req) throws Exception {
		
		String manager = email;//cookieUtils.getEmail(req);
		
		DBGroup group = groupRepo.findByGroupId(groupId);
		
		Map<String, String> groupMembers = group.getMembers();
		
		if (group.getManager().equals(manager)) {
			groupMembers.remove(member);
			deleteUserGroup(member, groupId);
			group.setMembers(groupMembers);
			groupRepo.save(group);
			return "SUCCEED";
		}else throw new Exception("Permission denied");
	}
	
	public Map<String, String> getMembers(String groupId){
		
		DBGroup group = groupRepo.findByGroupId(groupId);
		
		return group.getMembers();
	}
	
	public String addGroupMember(String newMember,String emai, String groupId, HttpServletRequest req) throws Exception {
		DBGroup group = groupRepo.findByGroupId(groupId);
		
		DBUser user = userRepo.findByEmail(newMember);
		
		String email = emai;//cookieUtils.getEmail(req);
		
		Map<String, String> members = group.getMembers();
		
		if(email != null && members.containsKey(email)) {
			if(user != null && !group.getMembers().containsKey(newMember)) {
				
				addUserGroup(newMember, group.getGroupId(), group.getGroupName());
				
				members.put(newMember, user.getUserName());
				group.setMembers(members);
				groupRepo.save(group);
				
				return "SUCCESS";
			}
			return "User does not exist or already a group member";
		}else throw new Exception("Something wrong!");
	}
	
}
