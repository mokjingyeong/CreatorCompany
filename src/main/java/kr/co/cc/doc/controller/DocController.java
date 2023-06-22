package kr.co.cc.doc.controller;

import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import kr.co.cc.doc.dto.ApprovalDTO;
import kr.co.cc.doc.dto.DocFormDTO;
import kr.co.cc.doc.dto.MemberDTO;
import kr.co.cc.doc.service.DocService;

@Controller
public class DocController {

	Logger logger = LoggerFactory.getLogger(this.getClass());
	@Value("${spring.servlet.multipart.location}") private String attachmentRoot;
	
	// 의존성 생성자 주입
	private final DocService service;
	
	public DocController(DocService service) {
		this.service = service;
	}
	
	// 기안문 작성 폼으로 이동
	@RequestMapping(value="/docWriteForm.do")
	public ModelAndView docWriteForm() {
		
		ModelAndView mav = new ModelAndView("docWriteForm");
		
		// 기안문 양식 불러오기
		ArrayList<DocFormDTO> docFormList = service.docFormCall();
		mav.addObject("docFormList", docFormList);
		
		// 결재 종류 불러오기
		ArrayList<ApprovalDTO> approvalKindList = service.approvalKindCall();
		// 기안은 제외(기안자가 나 자신이니까)
		approvalKindList.remove(0);
		mav.addObject("approvalKindList", approvalKindList);
		
		// 결재자 선택하기 위해 직원 리스트 불러오기
		ArrayList<MemberDTO> memberList = service.memberListCall();
		mav.addObject("memberList", memberList);
		
		return mav;
	}
	
	@RequestMapping(value="/docWrite.do")
	public ModelAndView docWrite(MultipartFile[] attachment, 
			@RequestParam String[] approvalVariable, @RequestParam String[] approvalPerson, 
			@RequestParam HashMap<String, String> params) {
		
		HashMap<String, String> map = new HashMap<String, String>();
		ArrayList<HashMap<String, String>> approvalList = new ArrayList<HashMap<String,String>>();
		for(int i=0;i<approvalVariable.length;i++) {
			map.put(approvalVariable[i], approvalPerson[i]);
			approvalList.add(map);
		}
		service.docWrite(params, approvalList, attachment);
		
		return null;
	}
	
	
}
