package kr.co.cc.work.controller;

import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import kr.co.cc.work.dto.WorkDTO;
import kr.co.cc.work.service.WorkService;

@Controller
public class WorkController {
	
	Logger logger = LoggerFactory.getLogger(getClass());
		
	@Autowired WorkService service;	
	String time;
	String date;
	
	// 근태 관리 workHistoryList > 
	@GetMapping(value="/workHistoryList.go")
	public ModelAndView workHistoryList(HttpSession session) {
		formattedDateTime();
		logger.info(time + date);
		return service.workHistoryList(session);
	}
	
	@GetMapping(value="/timeGo.do")
	public ModelAndView timeGo(HttpSession session, Model model) {
		formattedDateTime();
		String id = (String) session.getAttribute("loginId");
		String msg = "이미 출근을 등록하였습니다.";
		
		int row = service.timeGoBefore(id,date);
		logger.info("timeGoBefore row : " + row);		
		
		if(row==0) {
			service.timeGo(id);
			msg = "출근이 등록되었습니다.";
		}				
		model.addAttribute("msg", msg);		
		return service.workHistoryList(session);
	}
	
	@GetMapping(value="/timeEnd.do")
	public ModelAndView timeEnd(HttpSession session, Model model) {
		
		formattedDateTime();
		String id = (String) session.getAttribute("loginId");
		String msg = "출근이 등록되지 않았습니다.";
		
		int row = service.timeGoBefore(id,date);
		logger.info("timeGoBefore row : " + row);		
		
		if(row==1) {
			service.timeEnd(id,date,time);
			msg = "퇴근이 등록되었습니다.";
		}
		model.addAttribute("msg", msg);
		return service.workHistoryList(session);
	}
	
	
	@GetMapping(value="/WorkChangeRequest.go")
	public ModelAndView WorkChangeRequestGo(@RequestParam HashMap<String, String> params) {
		logger.info("WorkChangeRequest go params : {}", params);
		ModelAndView mav = new ModelAndView("workHistoryReq");
		mav.addObject("params",params);
		return mav;
	}
	
	@GetMapping(value="/WorkChangeRequest.do")
	public ModelAndView WorkChangeRequest(@RequestParam HashMap<String, String> params, Model model, HttpSession session) {
		logger.info("WorkChangeRequest do params : {}", params);
		
		ModelAndView mav = new ModelAndView();
		
		String msg = "";
		
		if(params.get("update_time_h").equals("")) {
			msg = "시간을 입력해주세요.";
			mav = WorkChangeRequestGo(params);
		} else if (params.get("update_time_m").equals("")) {
			msg = "시간을 입력해주세요.";
			mav = WorkChangeRequestGo(params);
		} else if (params.get("type").equals("")) {			
			msg = "유형을 선택해주세요.";
			mav = WorkChangeRequestGo(params);
		} else if (params.get("reason").equals("")) {
			msg = "사유를 입력해주세요.";
			mav = WorkChangeRequestGo(params);
		} else {
			int row=service.WorkChangeRequestChk(params.get("id"),params.get("type"));
			if(row>0) {
				msg = "이미 등록한 이력이 있습니다.";
				mav = WorkChangeRequestGo(params);
			} else {
				params.put("update_time", params.get("update_time_h")+":"+params.get("update_time_m")+":00");
				service.WorkChangeRequest(params);
				msg = "수정이 등록 되었습니다.";
				mav = workHistoryReqListGo(session);
			}
		}
		model.addAttribute("msg",msg);
		return mav;
	}
	
	@GetMapping(value="/workHistoryReqList.go")
	public ModelAndView workHistoryReqListGo(HttpSession session) {
		return service.workHistoryReqListGo(session);
	}
	
	@GetMapping(value="/WorkChangeAdmin.do")
	public ModelAndView WorkChangeAdmin(HttpSession session, @RequestParam int approval, 
			@RequestParam int id, @RequestParam String type, @RequestParam Time update_time) {
		
		String msg = "이미 처리된 요청입니다.";
		String flag = "WorkChangeAdmin";
		
		//이미 처리 했는지 확인
		int WorkChangeAdminChk = service.WorkChangeAdminChk(id,type);
		if(WorkChangeAdminChk != 0) {
			int row = service.WorkChangeAdmin(id,type,approval);
			logger.info("WorkChangeAdmin row : " + row);
			// 승인된 건에 대해서만 처리 (반려는 변경 X)
			if(approval==1) {
				if(row==1) {
					if(type.equals("출근")){
						row = service.workHistoryChange_go(id,update_time);	
						msg = "출근 변경 요청을 승인하였습니다.";
					} else {
						row = service.workHistoryChange_end(id,update_time);
						msg = "퇴근 변경 요청을 승인하였습니다.";
					}				
					logger.info("WorkChangeAdmin row2 : " + row);
				}			
			} else {
				msg = "요청을 반려하였습니다.";
			}
		}
		return service.workHistoryList_Ad(session,msg,flag);
	}
	
	//WorkChangeAdmin 
	
	// 근태 관리 관리자
	@GetMapping(value="/workHistoryList_Ad.go")
	public ModelAndView workHistoryList_Ad(HttpSession session) {		
		return service.workHistoryList_Ad(session,"","");
	}
	
	// 일별 , 주별 사원 현황 / 관리자
	@GetMapping(value="/workDailyList.go")
	public ModelAndView workDailyList() {
		return service.workDailyList();
	}
	
	@GetMapping(value="/dailyListFind.ajax")
	@ResponseBody
	public HashMap<String, Object> dailyListFind(@RequestParam Date dailyListDate) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	    String formattedDate = sdf.format(dailyListDate);
	    
	    
		logger.info("formattedDate : " + formattedDate);
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		ArrayList<WorkDTO> dto = service.dailyListFind(formattedDate);
		if(dto!=null) {
			map.put("dto", dto);
			logger.info("dailyListFind dto : " + dto);
		}
		return map;
	}
	
	
	@GetMapping(value="/weekListFind.do")
	public ModelAndView weekListFind(@RequestParam Date date) {		
		logger.info("weekListFind date : " + date);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String week = sdf.format(date);		
		return service.weekListFind(week, "");
	}
	
	@GetMapping(value="workWorn.do")
	public ModelAndView workWorn(@RequestParam HashMap<String, Object> params,@RequestParam Date week,@RequestParam String member_id) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String weekRe = sdf.format(week);		
		
		String msg = "";
		
		int row = service.workWornChk(member_id,weekRe);
		if(row>0) {
			msg = "이미 경고 처리한 내역이 있습니다.";			
		} else {
			service.workWorn(params);
			msg = "경고 처리가 완료되었습니다.";
		}		
		return service.weekListFind(weekRe,msg);
	}
	
	@GetMapping(value="/wornListFind.ajax")
	@ResponseBody
	public HashMap<String, Object> wornListFind(@RequestParam String wornFind1, @RequestParam String wornFind2) {
	    
	    
		logger.info("wornFind : " + wornFind1 + wornFind2);
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		ArrayList<WorkDTO> dto;
		
		if(wornFind1.equals("name")) {
			dto = service.wornListFindName(wornFind1,wornFind2);			
		} else {
			dto = service.wornListFindId(wornFind1,wornFind2);	
		}
		
		if(dto!=null) {
			map.put("dto", dto);
			logger.info("wornListFind dto : " + dto);
		}
		return map;
	}
	
	@GetMapping(value="/workWeekList.go")
	public String workWeekList() {
		return "workWeekList";
	}
	
	@GetMapping(value="/workWornList.go")
	public ModelAndView workWornList() {
		return service.workWornList();
	}
	
	@GetMapping(value="/wornDel.ajax")
	@ResponseBody
	public HashMap<String, Object> wornDel(@RequestParam String member_id, @RequestParam String week,
			@RequestParam String wornFind1, @RequestParam String wornFind2){
		logger.info("wornDel");
		service.wornDel(member_id,week);
		HashMap<String, Object> map = new HashMap<String, Object>();		
		ArrayList<WorkDTO> dto;		
		if(wornFind1.equals("name")) {
			dto = service.wornListFindName(wornFind1,wornFind2);			
		} else {
			dto = service.wornListFindId(wornFind1,wornFind2);	
		}		
		if(dto!=null) {
			map.put("dto", dto);
			logger.info("wornListFind dto : " + dto);
		}
		
		ArrayList<WorkDTO> dto2 = service.wornAllList2();
		
		if(dto2!=null) {
			map.put("dto2", dto2);
			logger.info("wornListFind dto2 : " + dto2);
		}
		
		return map;		
	}
	
	
	
	
	// 연차 관리 workHolidayList > 
	@GetMapping(value="/workHolidayList.go")
	public ModelAndView workHolidayList(HttpSession session) {
		String id = (String) session.getAttribute("loginId");		
		return service.workHolidayList(id);
	}
	
	
	@GetMapping(value="/workHolidayList_Ad.go")
	public String workHolidayList_Ad() {
		return "workHolidayList_Ad";
	}
	
	@GetMapping(value="/annualRegistration.go")
	public String annualRegistrationGo() {
		return "annualRegistration";
	}
	
	@GetMapping(value="/annualRegistration.do")
	public String annualRegistration() {
		return "annualRegistration";
	}
	
	
	
	

	
	
	public void formattedDateTime() {
		long currentTimeMillis = System.currentTimeMillis();

	    Calendar calendar = Calendar.getInstance();
	    calendar.setTimeInMillis(currentTimeMillis);

	    SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
	    String formattedTime = format.format(calendar.getTime());	    
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = dateFormat.format(calendar.getTime());

        logger.info(formattedDate);
        logger.info(formattedTime);
        
        this.time=formattedTime;
        this.date=formattedDate;
	}
	

}
