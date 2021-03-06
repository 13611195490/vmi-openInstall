package com.tigerjoys.shark.miai.service;


import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
import com.tigerjoys.nbs.common.ActionResult;
import com.tigerjoys.shark.miai.agent.dto.UserBO;
import com.tigerjoys.shark.miai.dto.service.RegLoginDto;
import com.tigerjoys.shark.miai.dto.service.ThirdRegistDto;

/**
 * 注册登录接口
 * @author lipeng
 *
 */
@Service
public interface IRegLoginService {
	
	/**
	 * 发送手机验证码并且发送短信
	 * @param mobile 手机号码
	 * @return
	 * @throws Exception
	 */
	public ActionResult regSendCode(String mobile)throws Exception;
	
	/**
	 * 登录操作
	 * @param dto
	 * @return
	 * @throws Exception
	 */
	public RegLoginDto login(ThirdRegistDto dto) throws Exception;
	
	/**
	 * 判断用户手机价格是否大于1000
	 * @param clientId
	 * @return
	 * @throws Exception
	 */
	public boolean ifMobileModelMoreThanOneThousand(String clientId) throws Exception;

	/**
	 * 自动登录
	 * @param uniqueKey - 用户自动登录唯一key
	 * throws Exception
	 */
	public RegLoginDto autoLogin(String uniqueKey,Integer pushchannel)throws Exception;

	/**
	 * 注销登录
	 * @return
	 * @throws Exception
	 */
	public ActionResult loginout(HttpServletRequest request)throws Exception;

	/**
	 * 手机号登录
	 * @param jsonObject
	 * @param request
	 * @return
	 * @throws Exception 
	 */
	public ActionResult mobileLogin(JSONObject json) throws Exception;
	
	/**
	 * 2.2.0版本后首次登录
	 * @param dto
	 * @return
	 */
	public RegLoginDto reg(ThirdRegistDto dto) throws Exception;

	/**
	 * 2.2.0版本后登录注册
	 * @param jsonObject
	 * @return
	 * @throws Exception
	 */
	public ActionResult regLogin(JSONObject json, MultipartFile pictures) throws Exception;

	/**
	 * 登录前获得登录方式
	 */
	public ActionResult getLoginWay() throws Exception;


	/**
	 * 记录root用户登录log
	 * @param dto
	 * @return
	 * @throws Exception
	 */
	public void loginVipUser(ThirdRegistDto dto) throws Exception;
	
	/**
	 * 注销登录
	 * @return
	 * @throws Exception
	 */
	public ActionResult userLogout(HttpServletRequest request)throws Exception;

	/**
	 * 手机号一键登录
	 * @param jsonObject
	 * @param request
	 * @return
	 * @throws Exception 
	 */
	public ActionResult oneKeyLogin(JSONObject json) throws Exception;

	/**
	 * 微信H5登录注册页面
	 * @param openid
	 * @return
	 * @throws Exception
	 */
	public UserBO WeixinH5Login(String openid) throws Exception;
}
