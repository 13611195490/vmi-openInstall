package com.tigerjoys.shark.miai.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tigerjoys.nbs.common.ActionResult;
import com.tigerjoys.nbs.common.beans.Produce;
import com.tigerjoys.nbs.common.utils.JsonHelper;
import com.tigerjoys.nbs.common.utils.encry.AESCipher;
import com.tigerjoys.nbs.web.annotations.Login;
import com.tigerjoys.nbs.web.annotations.NoSign;
import com.tigerjoys.nbs.web.annotations.UserClientService;
import com.tigerjoys.nbs.web.context.RequestUtils;
import com.tigerjoys.shark.miai.agent.IUserAgent;
import com.tigerjoys.shark.miai.agent.IUserGeoAgent;
import com.tigerjoys.shark.miai.agent.dto.UserBO;
import com.tigerjoys.shark.miai.agent.enums.PhotoCheckedLogTypeEnum;
import com.tigerjoys.shark.miai.annotations.WaiterActionOnline;
import com.tigerjoys.shark.miai.dto.service.UserDetailDto;
import com.tigerjoys.shark.miai.dto.service.UserInformDto;
import com.tigerjoys.shark.miai.enums.ErrorCodeEnum;
import com.tigerjoys.shark.miai.service.IUserService;

/**
 * 用户详情controller
 * @author lipeng
 *
 */
@Login
@Controller
@RequestMapping(value="/api/user")
public class UserController {
	
	private static final Logger logger = LoggerFactory.getLogger(ConfController.class);
	
	@Autowired
	private IUserService userService;
	
	@Autowired
	private IUserGeoAgent userGeoAgent;
	
	@Autowired
	private IUserAgent userAgent;
	
	/**
	 * 用户详情
	 * @return
	 * @throws Exception
	 */
	@UserClientService("info")
	@RequestMapping(value="/info/detail")
	@ResponseBody
	public ActionResult userinfo() throws Exception {
		long userid = RequestUtils.getCurrent().getUserid();
		return userService.userInfo(userid);
	}
	
	/**
	 * 用户详情-修改资料
	 * @param info
	 * @return
	 * @throws Exception
	 */
	@UserClientService("info")
	@RequestMapping(value="/info/change")
	@ResponseBody
	public ActionResult changeUserInfo(@RequestBody UserDetailDto info) throws Exception {
		return userService.changeUserInfo(info);
	}
	
	/**
	 * 用户详情-修改头像
	 * @param photo
	 * @return
	 * @throws Exception
	 */
	@UserClientService("info")
	@RequestMapping(value="/info/changephoto",method=RequestMethod.POST)
	@NoSign
	@ResponseBody
	public ActionResult changeUserPhoto(@RequestParam("photo") MultipartFile photo) throws Exception {
		return userService.changeUserPhoto(photo);
	}
	
	/**
	 * 用户详情-修改背景图
	 * @return
	 * @throws Exception
	 */
	@UserClientService("info")
	@RequestMapping(value="/info/changeBgPicture",method=RequestMethod.POST)
	@ResponseBody
	public ActionResult changeBgPicture() throws Exception {
		return userService.changeBgPicture();
	}
	
	/**
	 * 用户详情-保存背景图
	 * @return
	 * @throws Exception
	 */
	@UserClientService("info")
	@RequestMapping(value="/info/addBgPicture",method=RequestMethod.POST, produces=Produce.TEXT_ENCODE)
	@NoSign
	@ResponseBody
	public ActionResult addBgPicture(@RequestParam("parameters") String parameters , @RequestParam(value="bgpicture",required=false) MultipartFile bgpicture) throws Exception {
		parameters = AESCipher.aesDecryptString(parameters);
		return userService.addBgPicture(JsonHelper.toJsonObject(parameters),bgpicture);
	}
	
	/**
	 * 用户联系方式验证 - 发送手机验证码
	 * @param body
	 * @return
	 * @throws Exception
	 */
	@UserClientService("info")
	@RequestMapping(value = "/info/sendAuthCode",method=RequestMethod.POST)
	public @ResponseBody ActionResult sendAuthcode(@RequestBody String body) throws Exception {
		JSONObject json = JsonHelper.toJsonObject(body);
		return userService.authSendCode(json.getString("mobile"));
	}
	
	/**
	 * 用户联系方式验证 - 发送手机验证码
	 * @param body
	 * @return
	 * @throws Exception
	 */
	@UserClientService("info")
	@RequestMapping(value = "/info/modifyMobileSendCode",method=RequestMethod.POST)
	public @ResponseBody ActionResult modifyMobileSendCode(@RequestBody String body) throws Exception {
		JSONObject json = JsonHelper.toJsonObject(body);
		long userId = RequestUtils.getCurrent().getUserid();
		return userService.modifyMobileSendCode(userId,json.getString("mobile"));
	}
	
	/**
	 * 用户联系方式验证 - 验证保存联系方式
	 * @param body
	 * @return
	 * @throws Exception
	 */
	@UserClientService("info")
	@RequestMapping(value = "/info/authCodeAdd",method=RequestMethod.POST)
	public @ResponseBody ActionResult authcodeAdd(@RequestBody String body) throws Exception {
		return userService.authMobileAdd(JsonHelper.toJsonObject(body));
	}
	
	
	/**
	 * 更换手机号联系方式
	 * @param body
	 * @return
	 * @throws Exception
	 */
	@UserClientService("info")
	@RequestMapping(value = "/info/modifyMobile",method=RequestMethod.POST)
	public @ResponseBody ActionResult modifyMobile(@RequestBody String body) throws Exception {
		long userId = RequestUtils.getCurrent().getUserid();
		return userService.modifyMobile(userId,JsonHelper.toJsonObject(body));
	}
	
	/**
	 * 用户详情-视频认证
	 * @return
	 * @throws Exception
	 */
	@UserClientService("info")
	@RequestMapping(value="/info/videoAuth",method=RequestMethod.POST)
	public @ResponseBody ActionResult changeContactInfo() throws Exception {
		long userid = RequestUtils.getCurrent().getUserid();
		return userService.getVideoAuth(userid);
	}
	
	/**
	 * 用户详情-上传形象视频
	 * @return
	 * @throws Exception
	 */
	@UserClientService("info")
	@RequestMapping(value="/info/upImgVideo",method=RequestMethod.POST)
	@NoSign
	@ResponseBody
	public ActionResult upImgVideo(HttpServletRequest request) throws Exception {
		return userService.upImgVideo(request);
	}
	
	/**
	 * 用户详情-上传认证视频
	 * @return
	 * @throws Exception
	 */
	@UserClientService("info")
	@RequestMapping(value="/info/upVideoAuth",method=RequestMethod.POST)
	@NoSign
	@ResponseBody
	public ActionResult upVideoAuth(HttpServletRequest request) throws Exception {
		return userService.upVideoAuth(request);
	}
	
	/**
	 * 我的页面
	 * @return
	 * @throws Exception
	 */
	@UserClientService("info")
	@RequestMapping(value="/myPage")
	@WaiterActionOnline
	@ResponseBody
	public ActionResult myPage() throws Exception {
		return userService.getMyPage();
	}
	
	/**
	 * 我的页面活动查看记录
	 * throws Exception
	 */
	@UserClientService("info")
	@RequestMapping(value="/viewActivity")
	@ResponseBody
	public ActionResult addActivityViewLog(@RequestBody String body) throws Exception {
		JSONObject json = JsonHelper.toJsonObject(body);
		return userService.addActivityViewLog(json.getLong("indexCode"));
	}
	

	
	/**
	 * 更新用户的定位坐标
	 * @param body - 参数
	 * @return ActionResult
	 * @throws Exception
	 */
	@UserClientService("user_geo")
	@RequestMapping(value="/geo")
	@ResponseBody
	public ActionResult usergeo(@RequestBody String body) throws Exception {
		JSONObject json = JsonHelper.toJsonObject(body);
		
		String city = json.getString("city");
		Integer cityCode = json.getInteger("cityCode");
		double lng = json.getDoubleValue("longitude");
		double lat = json.getDoubleValue("latitude");
		
		if(lng == 0 || lat == 0) {
			return ActionResult.fail(ErrorCodeEnum.parameter_error);
		}
		
		//更新用户GEO数据
		userGeoAgent.modifyUserGeo(RequestUtils.getCurrent().getUserid(), lng, lat , city , cityCode);
		
		return ActionResult.success();
	}
	
	/**
	 * 举报用户
	 * @return
	 * @throws Exception
	 */
	@UserClientService("user")
	@RequestMapping(value="/inform")
	@ResponseBody
	public ActionResult addInform(@RequestBody UserInformDto userInform) throws Exception {
		return userService.addInform(userInform);
	}
	
	/**
	 * 拉黑/解除用户
	 * @param body
	 * @return
	 * @throws Exception
	 */
	@UserClientService("user")
	@RequestMapping(value="/doBlacklist")
	@ResponseBody
	public ActionResult addBlacklist(@RequestBody String body) throws Exception {
		return userService.doBlacklist(JsonHelper.toJsonObject(body));
	}
	
	
	/**
	 * 主播对用户 拉黑 (没有解除)
	 * @param body
	 * @return
	 * @throws Exception
	 */
	@UserClientService("user")
	@RequestMapping(value="/anchorBlacklist")
	@ResponseBody
	public ActionResult anchorBlacklist(@RequestBody String body) throws Exception {
		long userId = RequestUtils.getCurrent().getUserid();
		JSONObject json =JsonHelper.toJsonObject(body);
		Long otherId = json.getLong("otherId");
		JSONArray infoArray = json.getJSONArray("info");
		List<String> list = new ArrayList<String>();
		infoArray.forEach(v->{
			list.add(String.valueOf(v));
		});
		return userService.anchorBlacklist(userId,otherId,list);
	}
	
	/**
	 * ta的主页
	 * @param body
	 * @return
	 * @throws Exception
	 */
	@UserClientService("user")
	@RequestMapping(value="/userHomePage")
	@ResponseBody
	public ActionResult userHomePage(@RequestBody String body) throws Exception {
		return userService.getUserHomePage(JsonHelper.toJsonObject(body));
	}
	
	/**
	 * ta的主页头部信息
	 * @param body
	 * @return
	 * @throws Exception
	 */
	@UserClientService("user")
	@RequestMapping(value="/homePageHead")
	@ResponseBody
	public ActionResult homePageHead(@RequestBody String body) throws Exception {
		return userService.getUserHomePageHead(JsonHelper.toJsonObject(body));
	}
	
	/**
	 * ta的主页资料接口
	 * @param body
	 * @return
	 * @throws Exception
	 */
	@UserClientService("user")
	@RequestMapping(value="/homePagedesc")
	@ResponseBody
	public ActionResult homePagedesc(@RequestBody String body) throws Exception {
		JSONObject json = JsonHelper.toJsonObject(body);
		long userid = json.getLong("userid");
		long stamp = json.getLongValue("stamp");
		try {
			return userService.getUserHomePagedesc(userid, stamp);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return ActionResult.fail();
	}
	
	/**
	 * ta的主页用户印象资料接口
	 * @param body
	 * @return
	 * @throws Exception
	 */
	@UserClientService("user")
	@RequestMapping(value="/userImpression")
	@ResponseBody
	public ActionResult userImpression(@RequestBody String body) throws Exception {
		return userService.getUserImpression(JsonHelper.toJsonObject(body));
	}
	
	/**
	 * 主播个人主页亲密榜列表
	 */
	@UserClientService("closeList")
	@RequestMapping(value="/homePage/closeList",method=RequestMethod.POST)
	public @ResponseBody ActionResult homePageCloseList(@RequestBody String body) throws Exception {
		JSONObject json = JsonHelper.toJsonObject(body);
		long userid = json.getLong("userid");
		try {
			return userService.homePageCloseList(userid);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return ActionResult.fail();
	}
	
	/**
	 * 主播我的页面亲密榜列表
	 */
	@UserClientService("closeList")
	@RequestMapping(value="/closeList",method=RequestMethod.POST)
	public @ResponseBody ActionResult closeList(@RequestBody String body) throws Exception {
		JSONObject json = JsonHelper.toJsonObject(body);
		long userid = json.getLong("userid");
		long stamp = json.getLongValue("stamp");
		try {
			return userService.closeList(userid, stamp);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return ActionResult.fail();
	}
	
	/**
	 * 礼物贡献榜
	 */
	@UserClientService("gift")
	@RequestMapping(value="/giftList",method=RequestMethod.POST)
	public @ResponseBody ActionResult giftList(@RequestBody String body) throws Exception {
		JSONObject json = JsonHelper.toJsonObject(body);
		long anchorid = json.getLong("userid");
		if(anchorid <= 0) {
			return ActionResult.fail(ErrorCodeEnum.db_error);
		}
		try {
			return userService.getGiftList(anchorid);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return ActionResult.fail();
	}
	
	/**
	 * 全局广播列表
	 */
	@UserClientService("globalBroadcast")
	@RequestMapping(value="/globalBroadcastList",method=RequestMethod.POST)
	public @ResponseBody ActionResult getGlobalBroadcastList(@RequestBody(required=false) String body) throws Exception {
		
		
		UserBO user = userAgent.findByUsername("15873952675");
		if (RequestUtils.getCurrent().getHeader().getUserid()==user.getUserid()) {
			return null;
		}
		UserBO userTemp = userAgent.findById(RequestUtils.getCurrent().getHeader().getUserid());
		if (userTemp!=null) {
			if (RequestUtils.getCurrent().getHeader().getPackageName().equals("com.zdkj.lttool")&&userTemp.getFr()==4) {
				return null;
			}
		}
		try {
			JSONObject json = JsonHelper.toJsonObject(body);
			int time = json.getIntValue("time");
			return userService.getGlobalBroadcastList(time);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return ActionResult.fail();
	}
	
	/**
	 * 记录全局广播
	 * @return
	 * @throws Exception
	 */
	@UserClientService("globalBroadcast")
	@RequestMapping(value="/recordGlobalBroadcast",method=RequestMethod.POST)
	public @ResponseBody ActionResult recordGlobalBroadcast() throws Exception {
		/*PageModel pageModel = new PageModel();
		pageModel.asc("create_time");
		pageModel.addQuery(Restrictions.ge("create_time",Tools.getDateTime(Tools.getDayTime())));
		long total = globalBroadcastContract.count(pageModel);
		int record = globalBroadcastAgent.getRecordCount(RequestUtils.getCurrent().getUserid());
		if (record >= total) {
			return ActionResult.success();
		}
		AgentResult result = globalBroadcastAgent.recordGlobalBroadcast(RequestUtils.getCurrent().getUserid());
		return ActionResult.success(result);*/
		return ActionResult.success();
	}
	
	/**
	 * 查看联系方式
	 * @param body
	 * @return
	 * @throws Exception
	 */
	@UserClientService("user")
	@RequestMapping(value="/lookContacts")
	@ResponseBody
	public ActionResult lookContacts(@RequestBody String body) throws Exception {
		return userService.lookContacts(JsonHelper.toJsonObject(body));
	}
	
	/**
	 * 获得用户类型反馈
	 * @param body
	 * @return
	 * @throws Exception
	 */
	@UserClientService("user")
	@RequestMapping(value="/addUserTypeFeedback")
	@ResponseBody
	public ActionResult addUserTypeFeedback(@RequestBody String body) throws Exception {
		return userService.addUserTypeFeedback(JsonHelper.toJsonObject(body));
	}

	/**
	 * 查看视频
	 * @param body
	 * @return
	 * @throws Exception
	 */
	@Login
	@UserClientService("user")
	@RequestMapping(value="/look/video")
	@ResponseBody
	public ActionResult userLookVideo(@RequestBody String body) throws Exception {
		try {
			return userService.checkUserRight(JsonHelper.toJsonObject(body).getLongValue("userid"), 2);
		} catch (Exception e) {
			return ActionResult.fail();
		}
	}

	/**
	 * 查看联系方式
	 * @param body
	 * @return
	 * @throws Exception
	 */
	@Login
	@UserClientService("user")
	@RequestMapping(value="/look/contact")
	@ResponseBody
	public ActionResult userLookContact(@RequestBody String body) throws Exception {
		try {
			return userService.checkUserRight(JsonHelper.toJsonObject(body).getLongValue("userid"), 1);
		} catch (Exception e) {
			return ActionResult.fail();
		}
	}

	/**
	 * 付费查看信息
	 * @param body
	 * @return
	 * @throws Exception
	 */
	@Login
	@UserClientService("user")
	@RequestMapping(value="/look/info")
	@ResponseBody
	public ActionResult userLookInfo(@RequestBody String body) throws Exception {
		try {
			JSONObject object = JsonHelper.toJsonObject(body);
			long otherId = object.getLongValue("otherId");
			int type = object.getIntValue("type");
			return userService.checkUserRightWithEnergy(otherId, type);
		} catch (Exception e) {
			return ActionResult.fail();
		}
	}

	/**
	 * 付费查看主播私密作品
	 * @param body
	 * @return ActionResult
	 * @throws Exception
	 */
	@Login
	@UserClientService("user")
	@RequestMapping(value="/look/privacy/photo")
	@ResponseBody
	public ActionResult lookPrivacyPhoto(@RequestBody String body) throws Exception {
		try {
			JSONObject json = JsonHelper.toJsonObject(body);
			
			long photoId = json.getLongValue("photoId");
			int type = json.getIntValue("type");//作品类型
			PhotoCheckedLogTypeEnum logType = PhotoCheckedLogTypeEnum.getByCode(type);
			if(photoId <= 0 || logType == null) {
				return ActionResult.fail(ErrorCodeEnum.parameter_error);
			}
			
			return userService.lookPrivacyPhoto(photoId, logType, null);
		} catch (Exception e) {
			return ActionResult.fail();
		}
	}
	
}
