package com.pascloud.module.pasdev.web;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.pascloud.bean.pasdev.PasfileVo;
import com.pascloud.module.common.web.BaseController;
import com.pascloud.module.pasdev.service.PasdevService;
import com.pascloud.utils.PasCloudCode;
import com.pascloud.vo.result.ResultCommon;

@Controller
@RequestMapping("module/pasdev")
public class PasdevController extends BaseController {
	
	@Autowired
	private PasdevService m_pasdevService;
	
	@RequestMapping("/index.html")
	public ModelAndView index(HttpServletRequest request){
		ModelAndView view = new ModelAndView("pasdev/index");
		
		return view;
	}

	@RequestMapping("/pasfiles.json")
	@ResponseBody
	public List<PasfileVo> getPasFiles(HttpServletRequest request){
		List<PasfileVo> result = new ArrayList<>();
		result = m_pasdevService.getPasdevFiles();
		return result;
	}
	
	@RequestMapping("/modifyPasfiles.json")
	@ResponseBody
	public ResultCommon modifyPasFiles(HttpServletRequest request){
		ResultCommon result = new ResultCommon(PasCloudCode.SUCCESS);
		m_pasdevService.modifyPasdevFilesWidthID("dn0");
		return result;
	}
}
