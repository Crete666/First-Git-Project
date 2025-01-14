package com.forpets.view.pet;


import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Function;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.forpets.biz.partner.PartnerVO;
import com.forpets.biz.pet.PetService;
import com.forpets.biz.pet.PetVO;
import com.forpets.biz.pet.WorkVO;
import com.forpets.biz.pet.impl.PetDAO;
import com.forpets.biz.pet.impl.WorkDAO;
import com.forpets.biz.user.UserService;
import com.forpets.biz.user.UserVO;

@Controller
@SessionAttributes("UserPet")
public class PetController{
	@Autowired
	private PetService petService;
	@Autowired
	private UserService userServiece;
	
	//pet정보를 수정한다.
	@RequestMapping(value = "/myInfo/my-petUpd", method = RequestMethod.POST)
	public ResponseEntity<String> updatePet(PetVO vo, PetDAO petDAO, HttpServletRequest request) {
		System.out.println("==>pet udpate start");
		
		HttpSession session = request.getSession(false);
		
		if(session != null && session.getAttribute("userPet") != null) {
			petService.updatePet(vo);
			UserVO sessionVO = (UserVO) session.getAttribute("member");
			session.setAttribute("userPet", petService.getPetInfo(sessionVO.getUser_id()));
			return new ResponseEntity<String>("success", HttpStatus.OK);
		}
		return new ResponseEntity<String>("false", HttpStatus.BAD_REQUEST);
	}

	//pet정보를 등록한다.
	@RequestMapping(value = "/myInfo/my-petReg", method = RequestMethod.POST)
	public  ResponseEntity<String> insertPet(PetVO vo, PetDAO petDAO) {
		System.out.println("==>pet insert start");
		System.out.println(vo.toString());
		
		petService.insertPet(vo);
		return new ResponseEntity<String>("success", HttpStatus.OK);
	}
	
	//pet등록jsp를 View.
	@RequestMapping(value="/myInfo/my-petView")
	public String myPetView(@RequestParam(value="id", required=false)Integer pet_id, Model model, PetVO pvo) {
		
		
		if(pet_id != null) {
		System.out.println("id : " + pet_id);
		
		if(pet_id > 0) {
			model.addAttribute("userPet", petService.getPetDetail(pet_id));
			return "myInfo/mypet";
		}
		}
		return "myInfo/mypet2";
	}
	
	
	//pet_work 등록jsp를 View.
	@RequestMapping(value="/myInfo/create-roadMap")
	public String myPetWorkView() {
		System.out.println("==>myPetWorkView() start");
		return "myInfo/my_pet_work";
	}
	
	
	
	
	
	//main화면에 들어간다.
	@RequestMapping(value="/myInfo/main")
	public String viewMain() {
		return "myInfo/main";
	}
	
	//pet등록정보를 가져온다.
	@RequestMapping(value="/myInfo/getPetInfo")
	public String getPetInfo(HttpSession session,Model model) {

		UserVO SessionVO = (UserVO) session.getAttribute("member");
		
		String user_id = SessionVO.getUser_id();
		
		/*유저관련 통계자료를 저장합니다.*/
		HashMap<String, Object> data = new HashMap<String, Object>();
		//유저가입기간을 구합니다.
		data.put("userJoin", userServiece.cntUserJoinPeriod(user_id));
		//유저가 자주신청한 펫트너정보를 구합니다.
		data.put("multiPartInfo", getData(user_id, userServiece::multipleTimesPart, new PartnerVO()));
		//유저가 자주신청한 펫트너 에게 신청한 횟수를 구합니다.
		data.put("cntMultiTime", getData(user_id, userServiece::cntMultiPleTime, 0));
		//유저가 자주신청한 케어서비스를 구합니다.
		data.put("multiServe", getData(user_id, userServiece::getMultiPleServ, "없음"));
		//유저가 소모임참여한 횟수를 구합니다.
		data.put("communityPrt", getData(user_id, userServiece::cntCommunityPrt, 0));
		//유저가 서비스를 신청한 횟수를 구합니다.
		data.put("totalServe", getData(user_id, userServiece::cntTotalServe, 0));
		/*유저관련 통계자료 저장 코드 끝*/
		
		session.setAttribute("userPet", petService.getPetInfo(user_id));
		
		
		System.out.println(petService.getPetInfo(user_id).toString());
		
		
		model.addAttribute("data",data);

		return "forward:/myInfo/selectWork";
	}
	
	
	//통계데이터를 가져올때 예외가생길때를 대비한 메서드입니다.
	public static<T>T getData(String user_id, Function<String, T>func, T defaultValue){
		try {
			return func.apply(user_id);
		}catch(EmptyResultDataAccessException e) {
			return defaultValue;
		}
	}
	
	

	@RequestMapping(value = "/myInfo/my-petImgUpload", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<PetVO> uploadPetImageActionPOST(MultipartFile uploadFile, HttpServletRequest request) throws IllegalStateException, IOException {
		System.out.println("uploadAjaxActionPOST..........");
		
		File checkfile = new File(uploadFile.getOriginalFilename());
		String type = null;
		
		type = Files.probeContentType(checkfile.toPath());
		System.out.println("MIME TYPE : " + type  );
		
		if(!type.startsWith("image")) {
			PetVO check = null;
			return new ResponseEntity<PetVO>(check, HttpStatus.BAD_REQUEST);
		}
		
		
	    String applicationPath = request.getServletContext().getRealPath("/");
	    String[] personalPath = applicationPath.split(File.separator+".metadata");
	    String osName = System.getProperty("os.name");
		String pet_img_path = null;
		 if(osName.contains("Windows")) {
			pet_img_path = personalPath[0] + "ForPets" + File.separator + "src" + File.separator + "main" + File.separator + "webapp" + File.separator + "resources" + File.separator + "assets" + File.separator + "upload";
		 } else if (osName.contains("Mac")) {
			pet_img_path = personalPath[0]+ File.separator + "ForPets" + File.separator + "src" + File.separator + "main" + File.separator + "webapp" + File.separator + "resources" + File.separator + "assets" + File.separator + "upload";
		 }
	    
		
		System.out.println(request.getServletContext().getRealPath("/"));
		System.out.println(applicationPath.split(File.separator+".metadata"));
		System.out.println("personalPath[0] : " + personalPath[0]);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		
		String str = sdf.format(date);
		
		String datePath = str.replace("-", File.separator);
		/* 폴더 생성 */
		File uploadPath = new File(pet_img_path, datePath);
		
		if(uploadPath.exists() == false) {
			uploadPath.mkdirs();
		}
		
		PetVO vo = new PetVO();
		
		/* 파일 이름 */
		String uploadFileName = uploadFile.getOriginalFilename();
		/* uuid적용 파일 이름 */
		String uuid = UUID.randomUUID().toString();
		
		uploadFileName = uuid+"_"+uploadFileName;
		
		/* 파일위치, 파일 이름을 합칙 file 객체 */
		File saveFile = new File(uploadPath, uploadFileName);
		vo.setImg(saveFile.toString());
		
		/* 파일저장 */
		uploadFile.transferTo(saveFile);
		
		ResponseEntity<PetVO> result = new ResponseEntity<PetVO>(vo, HttpStatus.OK);
		
		return result;
		
	}
	
	@RequestMapping("/myInfo/display")
	public ResponseEntity<byte[]>getImage(String fileName, HttpServletRequest request){
		System.out.println("getImage()....." + fileName);
		
	    String applicationPath = request.getServletContext().getRealPath("/");
	    String[] personalPath = applicationPath.split(File.separator+".metadata");
	    String osName = System.getProperty("os.name");
		String pet_img_path = null;
		 if(osName.contains("Windows")) {
			pet_img_path = personalPath[0] + "ForPets" + File.separator + "src" + File.separator + "main" + File.separator + "webapp" + File.separator + "resources" + File.separator + "assets" + File.separator + "upload";
		 } else if (osName.contains("Mac")) {
			 String a = "/";
			 String b = "\\";
			 fileName = fileName.replace(b, a);
			pet_img_path = personalPath[0]+ File.separator + "ForPets" + File.separator + "src" + File.separator + "main" + File.separator + "webapp" + File.separator + "resources" + File.separator + "assets" + File.separator + "upload" + File.separator;
		 }
		File file = new File(pet_img_path + fileName);
		
		ResponseEntity<byte[]> result = null;
		
		try {
			HttpHeaders header = new HttpHeaders();
			header.add("Content-type", Files.probeContentType(file.toPath()));
			result = new ResponseEntity<byte[]>(FileCopyUtils.copyToByteArray(file), header, HttpStatus.OK);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	@RequestMapping(value = "/myInfo/delete", method = RequestMethod.POST)
	public ResponseEntity<String> DeleteImage(String fileName,  HttpServletRequest request) {
		System.out.println("deleteImage()...."+fileName);
		
	    String applicationPath = request.getServletContext().getRealPath("/");
	    String[] personalPath = applicationPath.split(File.separator+".metadata");
		String osName = System.getProperty("os.name");
		String pet_img_path = null;
		 if(osName.contains("Windows")) {
			pet_img_path = personalPath[0] + "ForPets" + File.separator + "src" + File.separator + "main" + File.separator + "webapp" + File.separator + "resources" + File.separator + "assets" + File.separator + "upload";
		 } else if (osName.contains("Mac")) {
			 String a = "/";
			 String b = "\\";
			 fileName = fileName.replace(b, a);
			pet_img_path = personalPath[0]+ File.separator + "ForPets" + File.separator + "src" + File.separator + "main" + File.separator + "webapp" + File.separator + "resources" + File.separator + "assets" + File.separator + "upload" + File.separator;
		 }
		
		File file = null;
		try {
			file = new File(pet_img_path + URLDecoder.decode(fileName, "UTF-8"));
			file.delete();
		}catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<String>("fail", HttpStatus.NOT_IMPLEMENTED);
		}
		return new ResponseEntity<String>("success", HttpStatus.OK);
	}
	
	// - 최지혁
	@RequestMapping(value="getPet")
	@ResponseBody
	public void getPet(PetVO vo, PetDAO petDAO, HttpSession session, HttpServletRequest request) {
		System.out.println("---> getPet 실행");
		session.setAttribute("pet_info", petService.getPet(vo, request.getParameter("pet_id")));
		System.out.println("---> getPet 완료");
	}
	
	// - 최지혁
	@RequestMapping(value="getPetList")
	public String getPetList(PetVO pvo, PetDAO petDAO, HttpSession session, Model model) {
		System.out.println("---> getPetList 실행");
		UserVO uvo = (UserVO) session.getAttribute("member");
		System.out.println("user_id : " + uvo.getUser_id());
		model.addAttribute("getPetList", petService.getPetList(pvo, uvo.getUser_id()));	// Model 정보 저장
		System.out.println("---> getPetList 완료");
		return "./Service/getPetList";
	}
	
	// - 최지혁
	@RequestMapping(value="findPetWork")
	public String choicePetInfo(PetVO vo, PetDAO petDAO, HttpSession session,WorkVO voW, WorkDAO workDAO,Model model) {
		System.out.println("===>pet get start");
		UserVO voP = (UserVO) session.getAttribute("member");
		session.setAttribute("userPet", petService.getPetInfo(voP.getUser_id()));
		return "forward:/Service/showPetWork";
		
	}
	
}
