package com.gcdemo.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * 项目名称：  LogPrintByWebsocket   
 * 类名称：  DataShowController   
 * 描述：  页面展示控制层
 * @author  gaocheng   
 * 创建时间：  2017年11月28日 下午2:30:40 
 * 修改人：gaocheng    修改日期： 2017年11月28日
 * 修改备注：
 *
 */
@Controller
public class DataShowController {

	@RequestMapping(value = "/toServiceLog.do", method = RequestMethod.GET)
	public ModelAndView toServiceLog(HttpServletRequest request, HttpServletResponse response) {

		String userType = request.getParameter("userType");
		String serviceIP = request.getParameter("serviceIP");
		String projectPath = request.getParameter("projectPath");
		// 最好通过配置文件获取
		String wsurllog = "ws://172.0.0.1:8080/LogPrintByWebsocket/websocketconnection.wst";

		// 禁止非法访问
		if (StringUtils.isNoneBlank(serviceIP, projectPath)) {
			Map<String, String> m = new HashMap<String, String>(4);
			// ws地址放置到前台
			m.put("wsurllog", wsurllog);
			m.put("userType", userType);
			m.put("serviceIP", serviceIP);
			m.put("projectPath", projectPath);

			return new ModelAndView("service_log", "info", m);
		} else {
			return new ModelAndView("error");
		}

	}
}
