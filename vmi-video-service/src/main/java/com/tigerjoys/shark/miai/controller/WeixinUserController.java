package com.tigerjoys.shark.miai.controller;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tigerjoys.nbs.common.beans.Produce;
import com.tigerjoys.nbs.common.utils.Tools;
import com.tigerjoys.nbs.web.annotations.Login;
import com.tigerjoys.nbs.web.annotations.NoSign;
import com.tigerjoys.nbs.web.annotations.TestEncrypt;
import com.tigerjoys.nbs.web.annotations.UserClientService;
import com.tigerjoys.nbs.web.context.RequestUtils;
import com.tigerjoys.shark.miai.service.IUserService;

/**
 * 私密相册/视频购买和列表等
 * @author lipeng
 *
 */
@Login
@Controller
@RequestMapping(value = "/wx/user")
//@TestEncrypt("naihnwhQOxMW3SSDjwBCalG8cAb7Z0o2/Vb65AsQC87EDBZACqpfpBvRClXVo0ejLhaGHw2TzHgXMG2eOSXxpWAJJ6KIJ+uZ8xEnRGBS9zZ7wjsdi7CFCZv/nthy18Wide7w7SsKN1if0eLL6hhU1ebsJCsA0gpuijLhgsKp21epnm4J7Wjqk7f1xqGNyZLUOmFmw8UZRvZPgD3XcoHQUS1ZoaV5wr/tNjqXGlcrzqb7Cr+b0S785LS0YfjZsrzCOLn9VgHCjvtMJqE7KHohXxAWRsdLz3g1iBA/rz9hdaPwreQq9Fcz1aObr6Qr5DMUYRRp0Qjxj1fKSlym8/qPwDvTIx+1M3J40166MBUwb9GhCu0h7/aUHtiXBBNUeP6XCZjVLjOW4uXoxFPA9xZvgYIwRy05+gomCLXSuLtdFCC+Z/uGFEGUN87cyu4sjDK8MHGjrbpsDzzqKtfYkgg2ZQ7nneqiiTw8ILt2snWUfeWUaEIRdhKZHjrx0EPoxKyc")
public class WeixinUserController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(WeixinUserController.class);
	
	@Autowired
	private IUserService userService;
	

	/**
	 * H5-个人主页
	 * @return - String
	 * @throws Exception
	 */
	@UserClientService("wx")
	@NoSign
	@RequestMapping(value = "/userHomePage", produces = Produce.TEXT_HTML)
	public String statisticsPay(HttpServletRequest request, Model model) throws Exception {
		String userid = request.getParameter("userid");
		model.addAttribute("encrypt", RequestUtils.getCurrent().getHeaderEncrypt());
		model.addAttribute("homePage", userService.getWxUserHomePage(Tools.parseLong(userid)));
		return "weixinChat/WeixinChatPageDetails";
	}

}
