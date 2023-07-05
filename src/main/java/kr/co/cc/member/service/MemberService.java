package kr.co.cc.member.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import kr.co.cc.member.dao.MemberDAO;
import kr.co.cc.member.dto.MemberDTO;

@Service
@MapperScan(value={"kr.co.cc.member.dao"})
public class MemberService {
	
	@Autowired MemberDAO memberdao;
	
	@Autowired PasswordEncoder encoder;
	
	Logger logger = LoggerFactory.getLogger(getClass());
	
	@Value("${spring.servlet.multipart.location}") private String attachmentRoot;
	
	
	
	public ModelAndView join(HashMap<String, String> params, MultipartFile file, MemberDTO dto) {
		logger.info("file: " + file);
		logger.info(dto.getUser_id()+"/"+dto.getPassword());
		String enc_pass = encoder.encode(dto.getPassword());
		dto.setPassword(enc_pass);
		int success = memberdao.join(dto);
		logger.info("join success : "+success);
		
		String msg = "회원등록에 실패하였습니다.";
		String page = "JoinForm";
		
		if (success > 0) {
			msg = "회원등록에 성공하였습니다.";
			page = "Login";
			String userId = dto.getId();	
			logger.info("userId: " + userId);
	        if (file != null && !file.isEmpty()) {
	            // 입력받은 파일 이름
	            String oriFileName = file.getOriginalFilename();
	            // 확장자를 추출하기 위한 과정
	            String ext = oriFileName.substring(oriFileName.lastIndexOf("."));
	            // 새로운 파일 이름은?
	            UUID uuid = UUID.randomUUID();
	            String newFileName = uuid.toString() + ext;
	            logger.info("파일 업로드 : "+oriFileName+"=>"+newFileName+"으로 변경될 예정");
	            String classification = "프로필사진";
	            try {
	                byte[] bytes = file.getBytes();

	                Path path = Paths.get(attachmentRoot + "/" + newFileName);
	                Files.write(path, bytes);
	                memberdao.userfileWrite(oriFileName, newFileName, classification, userId);
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }	        
	    }
		
		ModelAndView mav = new ModelAndView();
		mav.setViewName(page);
		mav.addObject("msg", msg);  
		return mav;
	}

	
	public Map<String, Object> login(String user_id, String password,String id) {
		
		Map<String, Object> enc_pw = memberdao.login(user_id);
	
		if (enc_pw != null && !enc_pw.isEmpty()) {
	        boolean matches = encoder.matches(password, enc_pw.get("password").toString());
	    }
		
	    return enc_pw;
	}
	
	
    public HashMap<String, Object> idChk(String user_id) {
         
         HashMap<String, Object> map = new HashMap<String, Object>();
         logger.info("service user_id");
         int idChk = memberdao.idChk(user_id);
         map.put("idChk", idChk);
         return map;
     }
    
    
    public HashMap<String, Object> sendMail(HashMap<String, String> params) {
		HashMap<String, Object> resMap = new HashMap<String, Object>();

		MemberDTO userInfo = memberdao.getUserInfo(params);

		if (userInfo != null) {
			Properties props = new Properties();

			props.put("mail.smtp.host", "smtp.gmail.com");
			props.put("mail.smtp.port", "587");
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

			Session session = Session.getInstance(props, new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication("archeagemd1@gmail.com", "xgltqqhqiitmzzyd");  // 보내는 사람 메일, 구글메일 인증키																							
				}
			});

			String receiver = userInfo.getEmail();
			String title = "[Creator Company] 아이디 찾기"; // 메일 제목
			String content = "<b> 아이디 : " + userInfo.getUser_id() + "</b>"; // 메일 내용
			Message message = new MimeMessage(session);
			try {
				message.setFrom(new InternetAddress("archeagemd1@gmail.com", "관리자", "utf-8")); // 보내는 사람 메일, 구글메일 인증키
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(receiver));
				message.setSubject(title);
				message.setContent(content, "text/html; charset=utf-8");

				Transport.send(message);
				resMap.put("code", "COMPLETE");
			} catch (Exception e) {
				e.printStackTrace();
				resMap.put("code", "ERROR");
			}
		} else {
			resMap.put("code", "NO_DATA");
		}

		return resMap;
	}
	
    
    public HashMap<String, Object> sendPWMail(HashMap<String, String> params) {
		HashMap<String, Object> resMap = new HashMap<String, Object>();

		MemberDTO userInfoPW = memberdao.getUserInfoPW(params);
		logger.info(userInfoPW.getName());
		
		if (userInfoPW != null) {
			String temporaryPassword = generateTemporaryPassword(); // 임시 비밀번호 생성
			temporaryPassword = temporaryPassword.substring(0, 6);
			String encodedPassword = encodePassword(temporaryPassword); // 비밀번호 암호화
			userInfoPW.setPassword(encodedPassword);
			
			// DB에 임시 비밀번호 업데이트
	        boolean updateResult = memberdao.updateTemporaryPassword(userInfoPW);
	        
	        if(updateResult) {
	        
				Properties props = new Properties();
		
				props.put("mail.smtp.host", "smtp.gmail.com");
				props.put("mail.smtp.port", "587");
				props.put("mail.smtp.auth", "true");
				props.put("mail.smtp.starttls.enable", "true");
				props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

				Session session = Session.getInstance(props, new Authenticator() {
					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication("archeagemd1@gmail.com", "xgltqqhqiitmzzyd"); // 보내는 사람 메일, sendmail구글쪽																							
					}
				});

				String receiver = userInfoPW.getEmail();
				String title = "[Creator Company] 임시 비밀번호 발급"; // 메일 제목
				String content = "<b>임시 비밀번호: " + temporaryPassword + "</b>"; // 메일 내용
				logger.info("암호화 전 tp : " + temporaryPassword);
				logger.info("암호화 후 ep : " + encodedPassword);
				Message message = new MimeMessage(session);
					try {
						message.setFrom(new InternetAddress("archeagemd1@gmail.com", "관리자", "utf-8")); // 보내는 사람 메일, 보내는 사람 이름
						message.addRecipient(Message.RecipientType.TO, new InternetAddress(receiver));
						message.setSubject(title);
						message.setContent(content, "text/html; charset=utf-8");
		
						Transport.send(message);
						resMap.put("code", "TRANSFER_COMPLETE");
					} catch (Exception e) {
						e.printStackTrace();
						resMap.put("code", "ERROR");
					}
	        }
	    } else {
			resMap.put("code", "DB_UPDATE_ERROR");
		}

		return resMap;
	}
    
    // sendPWMail에서 암호화된 비밀번호 관련 작업 수행
    private String encodePassword(String password) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(password);
    }

    
    // UUID를 사용하여 임시 비밀번호 생성
	private String generateTemporaryPassword() {
        return UUID.randomUUID().toString();
    }
    
	// 로그인시 세션에 id값 넣기위한 설정
	public String loginid(String user_id) {
		return memberdao.loginid(user_id);
	}


	public MemberDTO userInfo(Object attribute) {
		return memberdao.userInfo(attribute);
	}

	public String userInfoUpdate(HashMap<String, String> params, MultipartFile file) {
		String userId = params.get("userId");
  		int row = memberdao.userInfoUpdate(params);
  		String page = row>0 ? "redirect:/userinfo.go?userId="+userId : "redirect:/userinfo.go";
  		logger.info("update => "+page);
//  		 if(!file.getOriginalFilename().equals("")) {
//  			String type="fileChange";
//  			 photoSave(file,params,type); 
//  		 }
  		 
  		return page;
	}

	public ModelAndView departmentlist(HashMap<String, String> params) {
		
		logger.info("departmentlist 출력");
		ModelAndView mav = new ModelAndView("departmentlist");
		ArrayList<MemberDTO> departmentlist = memberdao.departmentlist(params);
		logger.info("departmentlist cnt" + departmentlist.size());
		mav.addObject("departmentlist", departmentlist);
		return mav;
	}

}
