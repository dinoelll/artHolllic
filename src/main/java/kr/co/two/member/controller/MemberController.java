package kr.co.two.member.controller;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.co.two.member.dto.MemberDTO;
import kr.co.two.member.service.MemberService;

@Controller
public class MemberController {

	@Autowired MemberService service;
	
	Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired PasswordEncoder encoder;

	@RequestMapping(value="/employeeWrite")
	public String employeeWrite() {
		
		logger.info("employeeWrite Controller");
		
		return "employeeWrite";
	}
	
	@RequestMapping(value="/employeeList.go")
	public String employeeList() {
		
		logger.info("employeeList Controller");
		
		return "employeeList";
	}
	
	@RequestMapping(value="/employeeList.ajax")
	@ResponseBody
	public HashMap<String, Object> employeeListCall(@RequestParam HashMap<String,Object> params) {
		
		logger.info("employeeList Call Controller");
		return service.employeeList(params);
	}
	
	@RequestMapping(value="/join.go")
	public String employeeJoinGo() {
		
		logger.info("employeeJoin Go Controller");
		
		return "employeeWrite";
	}
	
	@PostMapping(value="/join.do")
	public ModelAndView employeeJoinDo(@RequestParam HashMap<String, String> params, @RequestParam MultipartFile profile) {
	    
		logger.info("employeeJoin Do Controller");
	    logger.info("params: " + params);
	    logger.info("profile :"+ profile);
	    
	    String emailPrefix = params.get("emailPrefix");
	    String email3 = params.get("email3");
	    
	    String email = emailPrefix + "@" + email3;
	    logger.info("email :"+email);
	    
	    String year = params.get("year");
	    String month = params.get("month");
	    String day = params.get("day");

	    String birthdayString = year + "-" + month + "-" + day;

	    // Convert birthday value to a Date object
	    try {
	        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	        Date utilDate = dateFormat.parse(birthdayString);
	        java.sql.Date birthdayDate = new java.sql.Date(utilDate.getTime());
	        params.put("birthday", birthdayDate.toString());
	    } catch (ParseException e) {
	        // Handle birthday conversion error
	        logger.error("Failed to parse birthday: " + birthdayString, e);
	    }

	    return service.join(params,profile,email);
	}
	
	@RequestMapping(value="/admin.ajax")
	@ResponseBody
	public void admin(@RequestParam String memberId, @RequestParam Boolean adminValue) {
		
		logger.info("employeeJoin Controller");
		logger.info("memberId :"+memberId+"/"+"adminValue :"+adminValue);
		
		service.admin(memberId,adminValue);
	}

	@RequestMapping(value="/changePw.ajax")
	@ResponseBody
	public String changePw(@RequestParam String pw) {
		
		logger.info("changePw Controller");
		logger.info("pw :"+pw);
		
		int success = service.changePw(pw);
		logger.info("success :"+success);
		
		// JSON 응답 생성
	    ObjectMapper mapper = new ObjectMapper();
	    Map<String, Integer> response = new HashMap<>();
	    response.put("success", success);
	    String jsonResponse = "";
	    try {
	        jsonResponse = mapper.writeValueAsString(response);
	    } catch (JsonProcessingException e) {
	        e.printStackTrace();
	    }

	    return jsonResponse;
	}
	
	@PostMapping(value="/login.do")
	public String Login(@RequestParam String id, @RequestParam String pw, HttpSession session, Model model) {
		
		logger.info("Login Controller");
		logger.info("id :"+id+"/"+"pw :"+pw);
		
		String page = "login";
		
		if(service.login(id, pw)) {
			page = "main";
			/* session.setAttribute("loginId", id); */
		}else {
			model.addAttribute("msg","Id 또는 Password를 확인 해 주세요");
		}
		return page;
	}
	
	@RequestMapping(value="/updateMember.go")
	public String updateMember(@RequestParam String member_id, Model model) {
	    logger.info("updateMember Controller");
	    logger.info("member_id :"+member_id);
	    
	    MemberDTO memberDto = service.memberDetail(member_id);
	    
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	    String birthday = dateFormat.format(memberDto.getBirthday());
	    String[] birthdayArr = birthday.split("-");
	    String year = birthdayArr[0];
	    String month = birthdayArr[1];
	    String day = birthdayArr[2];
	    
	    Map<String, Object> memberMap = new HashMap<>();
	    memberMap.put("year", year);
	    memberMap.put("month", month);
	    memberMap.put("day", day);
	    
	    String email = memberDto.getEmail(); // 저장된 이메일 가져오기
	    String[] emailArr = email.split("@"); // @를 기준으로 나누기
	    String emailPrefix = emailArr[0]; // 이메일 앞부분
	    String email3 = emailArr[1]; // 이메일 뒷부분

	    model.addAttribute("emailPrefix", emailPrefix);
	    model.addAttribute("email3", email3);
	    
	    model.addAttribute("memberMap", memberMap);
	    model.addAttribute("member",memberDto);
	    
	    return "employeeUpdate";
	}
	
	@PostMapping(value="/update.do")
	public ModelAndView employeeUpdate(@RequestParam HashMap<String, String> params, @RequestParam MultipartFile profile) {
		
		logger.info("employeeUpdate Controller");
		logger.info("params: " + params);
	    logger.info("profile :"+ profile);
	    
	    String emailPrefix = params.get("emailPrefix");
	    String email3 = params.get("email3");
	    
	    String email = emailPrefix + "@" + email3;
	    logger.info("email :"+email);
	    
	    String year = params.get("year");
	    String month = params.get("month");
	    String day = params.get("day");

	    String birthdayString = year + "-" + month + "-" + day;

	    // Convert birthday value to a Date object
	    try {
	        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	        Date utilDate = dateFormat.parse(birthdayString);
	        java.sql.Date birthdayDate = new java.sql.Date(utilDate.getTime());
	        params.put("birthday", birthdayDate.toString());
	    } catch (ParseException e) {
	        // Handle birthday conversion error
	        logger.error("Failed to parse birthday: " + birthdayString, e);
	    }

	    return service.update(params,profile,email);
	}
	
	@RequestMapping(value="/companyAddress.go")
	public String companyAddressList() {
		
		logger.info("companyAddress Controller");
		
		return "companyAddress";
	}
	
	@RequestMapping(value="/companyList.ajax")
	@ResponseBody
	public HashMap<String, Object> companyListCall(@RequestParam HashMap<String,Object> params) {
		
		logger.info("companyList Call Controller");
		return service.companyList(params);
	}
	
	@RequestMapping(value="/companyWrite.ajax")
	@ResponseBody
	public String companyWrite(@RequestParam HashMap<String,Object> params) {
		
		logger.info("companyWrite Call Controller");
		logger.info("params :"+params);
		
		return service.companyWrite(params);
	}
	
	@RequestMapping(value="/companyDetail.ajax")
	@ResponseBody
	public HashMap<String, Object> companyDetail(@RequestParam HashMap<String,Object> params) {
		
		logger.info("companyUpdate Call Controller");
		logger.info("params :"+params);
		String cooper_id = (String) params.get("cooper_id");
		
		HashMap<String, Object> result = new HashMap<String, Object>();
	    result.put("company", service.companyDetail(cooper_id));

	    return result;
	}
	
	@PostMapping(value="/companyUpdate.ajax")
	public String companyUpdate(@RequestParam HashMap<String, String> params) {
		
		logger.info("companyUpdate Do Controller");
		logger.info("params :"+params);
		
		return service.companyupdate(params);
		
	}

}
