package com.forpets.view.reserve;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.forpets.biz.carediary.CareDiaryService;
import com.forpets.biz.carediary.CareDiaryVO;
import com.forpets.biz.partner.PartnerService;
import com.forpets.biz.partner.PartnerVO;
import com.forpets.biz.pet.PetService;
import com.forpets.biz.pet.PetVO;
import com.forpets.biz.reserve.ReServeVO;
import com.forpets.biz.reserve.ReserveService;
import com.forpets.biz.reserve.impl.ReserveDAO;
import com.forpets.biz.review.ReviewService;
import com.forpets.biz.service.Service;
import com.forpets.biz.service.ServiceVO;
import com.forpets.biz.user.UserVO;

@Controller
@SessionAttributes("Reserve")
public class ReserveController {
	@Autowired
	private ReserveService reserveService;
	@Autowired
	private ReviewService reviewService;
	@Autowired
	private CareDiaryService careDiaryService;
	
	@Autowired
	private PetService petService;
	@Autowired
	private PartnerService partnerService;
	@Autowired
	private Service serviceService;
	
	/**
	 * 예약내역으로 이동하는 메서드
	 */
	
	@RequestMapping(value = "/myInfo/check-reservation")
	public String viewReserveList (ReServeVO vo, ReserveDAO reserveDAO, Model model,HttpSession session) {
		System.out.println("--->Enter in Reserve-check page....");
		
		UserVO SessionVO = (UserVO) session.getAttribute("member");
		vo.setUser_id(SessionVO.getUser_id());
		
		HashMap<String, Integer> state = new HashMap<String, Integer>();
		
		state.put("reserveCnt", reserveService.selectCount(vo));
		state.put("BeforeCnt", reserveService.selectBeforeCount(vo));
		state.put("IngCnt", reserveService.selectIngCount(vo));
		state.put("CpleteCnt", reserveService.selectCompleteCount(vo));
		
		try {
			model.addAttribute("reserveList", reserveService.getReserveList(vo));
			model.addAttribute("state", state);
			
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("--->Reserve Controller Error");
		}
		return "myInfo/my_reserve";
	}	
	
	@RequestMapping(value="/myInfo/myreserveDetail")
	public String myreserveDetail(ReServeVO rvo, PetVO pvo, PartnerVO partvo, ServiceVO svo, Model model) {
		System.out.println("myreserveDetail...");
		System.out.println("reserve_num : " + rvo.getReserve_num());
		List<ReServeVO> rvoList = (List<ReServeVO>) reserveService.reserveDetailLIst(rvo);
		PetVO newpvo = petService.getPetDetail(rvoList.get(0).getPet_id());
		PartnerVO newpartvo = partnerService.getPartner(partvo, rvoList.get(0).getPart_id());
		List<ServiceVO> svoList = new ArrayList<ServiceVO>();
		int total_price = 0;
		for(int i=0;i<rvoList.size();i++) {
			ServiceVO a = serviceService.getServ(svo, rvoList.get(i).getS_num());
			total_price += a.getS_price();
			svoList.add(a);
		}
		
		model.addAttribute("reserve", rvoList.get(0));
		model.addAttribute("pet_info", newpvo);
		model.addAttribute("part_info", newpartvo);
		model.addAttribute("servList", svoList);
		model.addAttribute("total_price", total_price);
		return "myInfo/my_reserve_detail";
	}
	
	//예약내역데이터를 가져와서 
	@RequestMapping(value = "/myInfo/getCptReserve")
	public String viewReviewReserveList (ReServeVO vo, ReserveDAO reserveDAO, Model model,HttpSession session) {
		
			UserVO voU = (UserVO) session.getAttribute("member");
			vo.setUser_id(voU.getUser_id());
			

			model.addAttribute("reserveList", reserveService.getCPTReserveList(vo));
		
		return "myInfo/myReview";
		
	}
	
	
	// - 최지혁
	@RequestMapping(value="/Service/choice")
	public String choice(UserVO vo, HttpSession session) {
		System.out.println("---> choice 실행");
		System.out.println("---> choice 완료, choice 이동");
		return "Service/choice";
		
	}
	
	// - 최지혁
	@RequestMapping(value="/Service/normal")
	public String normal(UserVO vo, HttpSession session) {
		System.out.println("---> normal 실행");
		if(session.getAttribute("member") == null) {
			System.out.println("---> normal 완료, login 이동");
			return "member/loginMain";
		}
		else {
			System.out.println("---> normal 완료, normal 이동");
			return "Service/normal";
		}
		
	}
	
	// - 최지혁
	@RequestMapping(value="/Service/work")
	public String work(UserVO vo, HttpSession session) {
		System.out.println("---> work 실행");
		if(session.getAttribute("member") == null) {
			System.out.println("---> work 완료, login 이동");
			return "member/loginMain";
		}
		else {
			System.out.println("---> work 완료, work 이동");
			return "Service/work";
		}
		
	}
	
	// - 최지혁
	@RequestMapping(value="/Service/pickup")
	public String pickup(UserVO vo, HttpSession session) {
		System.out.println("---> pickup 실행");
		if(session.getAttribute("member") == null) {
			System.out.println("---> pickup 완료, login 이동");
			return "member/loginMain";
		}
		else {
			System.out.println("---> pickup 완료, pickup 이동");
			return "Service/pickup";
		}
		
	}
	
	// - 최지혁
	@RequestMapping(value="/Service/reserve")
	public String reserve(ReServeVO vo, ReserveDAO reserveDAO, HttpSession session, HttpServletRequest request) {
		System.out.println("---> reserve 실행");
		session.setAttribute("reserve", reserveService.makeReserve(vo, request));
		String[] pa_List = request.getParameterValues("pick_add");
		session.setAttribute("pa_List", pa_List);
		System.out.println("---> reservo 완료");
		return "Service/reserve";
	}
	
	// - 최지혁
	@RequestMapping(value="/Service/reserveInsert")
	public String insertReserve(ReServeVO vo, PetVO pvo, Model model, HttpSession session) {
		System.out.println("---> reserveInsert 실행");
		pvo = (PetVO) session.getAttribute("pet_info");
		vo = (ReServeVO) session.getAttribute("reserve");
		boolean check = false;
		if(session.getAttribute("pa_List") != null) check = true;
		String[] pa_List = (String[]) session.getAttribute("pa_List");
		ArrayList<ServiceVO> svoList = (ArrayList) session.getAttribute("servList");
		int count = svoList.toArray().length;
		for(int i=0;i<count;i++) {
			ServiceVO svo = svoList.get(i);
			System.out.println(i+ "번째 s_num : " + svo.getS_num());
			vo.setS_num(svo.getS_num());
			if(check) {
				int j = i;
				System.out.println(j + "번째 pick_add : " + pa_List[j]);
				vo.setPick_add(pa_List[j]);
			}
			reserveService.insertReserve(vo, pvo);
		}
		System.out.println("---> reserveInsert 완료");
		return "Service/complete";
	}
	
	
	@RequestMapping(value="/partner/getReserve")
	public String getReserveListPart(ReServeVO vo, ReserveDAO dao, Model model,HttpSession session){
		PartnerVO sessionvo = (PartnerVO) session.getAttribute("partners");
		vo.setPart_id(sessionvo.getPart_id());
		model.addAttribute("reserveList", reserveService.getReserveListPart(vo));
		return "/partner/getReserve";
	}
	
	// 파트너 페이지 예약일정 관리
	@RequestMapping(value="/partner/detail")
	public String getReserve(ReServeVO vo, Model model) {
		model.addAttribute("reserve",reserveService.getReserveDetail(vo));
		return "partner/getReserveDetail";
	}
	
	@RequestMapping(value="partner/careDiaryList")
	public String getReserveListCare(ReServeVO vo, Model model, HttpSession session) {
		PartnerVO sessionvo = (PartnerVO) session.getAttribute("partners");
		vo.setPart_id(sessionvo.getPart_id());
		model.addAttribute("reserveListCare", reserveService.getReserveListCare(vo));
		return "/partner/careDiaryList";
	}
	
	@RequestMapping(value="/CareBefore")
	public String getReserveBefore(CareDiaryVO vo, Model model, HttpSession session) {
		System.out.println("reserve_num : " + vo.getReserve_num());
		careDiaryService.updateReserveStatus(vo, 2);
		return "redirect:partner/getReserve";
	}
	
	@RequestMapping(value="/CareIng")
	public String getReserveIng(CareDiaryVO vo, ReServeVO rvo, HttpSession session) {
		careDiaryService.updateReserveStatus(vo, 3);
		return "redirect:partner/getReserve";
	}
	
	@RequestMapping(value="/CareAfter")
	public String getReserveAfter(CareDiaryVO vo, ReServeVO rvo, HttpSession session) {
		careDiaryService.updateReserveStatus(vo, 4);
		return "redirect:partner/getReserve";
	}
}