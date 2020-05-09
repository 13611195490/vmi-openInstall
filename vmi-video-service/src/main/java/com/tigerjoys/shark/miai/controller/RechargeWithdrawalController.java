package com.tigerjoys.shark.miai.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.shark.miai.common.annotation.ForbidDialAnnotation;
import org.shark.miai.common.enums.AppNameEnum;
import org.shark.miai.common.util.AESFieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.tigerjoys.nbs.common.ActionResult;
import com.tigerjoys.nbs.common.beans.Produce;
import com.tigerjoys.nbs.common.utils.JsonHelper;
import com.tigerjoys.nbs.common.utils.Tools;
import com.tigerjoys.nbs.mybatis.core.page.PageModel;
import com.tigerjoys.nbs.mybatis.core.sql.Restrictions;
import com.tigerjoys.nbs.web.BaseController;
import com.tigerjoys.nbs.web.annotations.FilterHeader;
import com.tigerjoys.nbs.web.annotations.Login;
import com.tigerjoys.nbs.web.annotations.NoSign;
import com.tigerjoys.nbs.web.annotations.UserClientService;
import com.tigerjoys.nbs.web.context.BeatContext;
import com.tigerjoys.nbs.web.context.RequestHeader;
import com.tigerjoys.nbs.web.context.RequestUtils;
import com.tigerjoys.nbs.web.context.UserDetails;
import com.tigerjoys.shark.miai.Const;
import com.tigerjoys.shark.miai.agent.ISendMessageAgent;
import com.tigerjoys.shark.miai.agent.ISysConfigAgent;
import com.tigerjoys.shark.miai.agent.IUserAgent;
import com.tigerjoys.shark.miai.agent.IUserIncomeAgent;
import com.tigerjoys.shark.miai.agent.IUserPointAgent;
import com.tigerjoys.shark.miai.agent.IUserTariffeAgent;
import com.tigerjoys.shark.miai.agent.dto.ChargeDeductionConfigDto;
import com.tigerjoys.shark.miai.agent.dto.UserBO;
import com.tigerjoys.shark.miai.agent.dto.VacuateConfigDto;
import com.tigerjoys.shark.miai.agent.dto.result.IncomeResultDto;
import com.tigerjoys.shark.miai.agent.dto.transfer.UserModifyDto;
import com.tigerjoys.shark.miai.agent.enums.AgentErrorCodeEnum;
import com.tigerjoys.shark.miai.agent.enums.PayChannelEnum;
import com.tigerjoys.shark.miai.agent.enums.RechargeCategoryPriceEnum;
import com.tigerjoys.shark.miai.agent.enums.RechargePriceIosTypeEnum;
import com.tigerjoys.shark.miai.agent.enums.SendSmsTypeEnum;
import com.tigerjoys.shark.miai.agent.enums.UserIncomeAccountLogTypeEnum;
import com.tigerjoys.shark.miai.agent.enums.UserIncomeAccountLogTypeEnum.AccountType;
import com.tigerjoys.shark.miai.agent.enums.UserPointAccountLogTypeEnum;
import com.tigerjoys.shark.miai.agent.service.IValidCodeService;
import com.tigerjoys.shark.miai.dto.service.DlgAndGoPage;
import com.tigerjoys.shark.miai.dto.service.RechargePriceDto;
import com.tigerjoys.shark.miai.enums.ErrorCodeEnum;
import com.tigerjoys.shark.miai.inter.contract.IAnchorOnlineContract;
import com.tigerjoys.shark.miai.inter.contract.ICommodityGroupRelationshipContract;
import com.tigerjoys.shark.miai.inter.contract.ICopyUserContract;
import com.tigerjoys.shark.miai.inter.contract.IFreeVideoChatExperienceLogContract;
import com.tigerjoys.shark.miai.inter.contract.IRechargeChannelContract;
import com.tigerjoys.shark.miai.inter.contract.IRechargeOrderContract;
import com.tigerjoys.shark.miai.inter.contract.IUserDiamondPriceWatchLogContract;
import com.tigerjoys.shark.miai.inter.contract.IUserIncomeAccountContract;
import com.tigerjoys.shark.miai.inter.contract.IUserMobileAskContract;
import com.tigerjoys.shark.miai.inter.contract.IUserPayActionContract;
import com.tigerjoys.shark.miai.inter.entity.AnchorOnlineEntity;
import com.tigerjoys.shark.miai.inter.entity.CommodityGroupRelationshipEntity;
import com.tigerjoys.shark.miai.inter.entity.CopyUserEntity;
import com.tigerjoys.shark.miai.inter.entity.FreeVideoChatExperienceLogEntity;
import com.tigerjoys.shark.miai.inter.entity.RechargeChannelEntity;
import com.tigerjoys.shark.miai.inter.entity.RechargePriceEntity;
import com.tigerjoys.shark.miai.inter.entity.UserDiamondPriceWatchLogEntity;
import com.tigerjoys.shark.miai.inter.entity.UserIncomeAccountEntity;
import com.tigerjoys.shark.miai.inter.entity.UserMobileAskEntity;
import com.tigerjoys.shark.miai.inter.entity.UserPayActionEntity;
import com.tigerjoys.shark.miai.service.IRechargeDialExperienceService;
import com.tigerjoys.shark.miai.service.IRechargeGuardVipService;
import com.tigerjoys.shark.miai.service.IRechargeRedFlowerService;
import com.tigerjoys.shark.miai.service.IRechargeWithdrawalService;

/**
 * @Description: 充值提现
 * @author mouzhanpeng
 * @date 2017年5月11日 下午2:52:03
 * @version
 * @since 1.8.0
 */
@Controller


//@TestEncrypt("pUOj7GGbnHNF3q72/4D9sUl6azlJzO/JJO32FZQzJZUO2rSJKcoaTCM9JlW134P+1p/+R4gnDTtLFJyQKCZxNibJvV+CM7z/fJ7mQIzBe4eTHDihfemdLm2oT1HbmADDsrCB4nkuA4SQ3X5dJUQvi259NVZute8RUW7l2lrNz6TeemQAoa4xnIpws9dnrihDAe/NL6X6VZR5C6MllHHSOoRglZM4zCzE0ba2KMoU9Hr4Et8Q3xGFKUSqOLIHGSSfR83KOXIeBP+Wv50FcYNhUMjGCzdZS4ID8eJOx2xnUhR4N0KKwR+R4w0iEtAi33L8XAxQpg6LNDbJf/fjIa0k72KPbIHZEftjy1jPLl5+GR3BUZRMMLG1HtxfAcFINRIbc5M3ERnrBFC3bhXjkgwtpMd6JotRJXx+1PdeC1MuaPU4pVZD9EvjUB+nkaldcL9dGnydVo3+SQYkQFkoa6GRxjF3cr+hGq7lpNsL0S9q+xzs9RgFFYBRaH73iJDFnKLDZK0NRy7Hx7BuovQEEpfuCA==")

@RequestMapping(value = "/api", produces = Produce.TEXT_ENCODE)
public class RechargeWithdrawalController extends BaseController {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private IRechargeWithdrawalService rechargeWithdrawalService;
	
	@Autowired
	private IUserIncomeAgent userIncomeAgent;
	
	@Autowired
	private ICommodityGroupRelationshipContract commodityGroupRelationshipContract;
	
	@Autowired
	private IUserPayActionContract userPayActionContract;
	
	@Autowired
	private IUserIncomeAccountContract userIncomeAccountContract;
	
	@Autowired
	private IUserDiamondPriceWatchLogContract userDiamondPriceWatchLogContract;
	
	@Autowired
	private ISendMessageAgent sendMessageAgent;
	
	//第三方登录服务
	@Autowired
	private IValidCodeService validCodeService;
	
	@Autowired
	private ISysConfigAgent sysConfigAgent;

	
	@Autowired
	private IUserAgent userAgent;
	
	@Autowired
	private IUserPointAgent userPointAgent;
	
	@Autowired
	private IUserMobileAskContract userMobileAskContract;
	
	@Autowired
	private ICopyUserContract copyUserContract;
	
	@Autowired
	private IRechargeOrderContract rechargeOrderContract;
	
	@Autowired
	private IRechargeRedFlowerService rechargeRedFlowerService;
	
	
	@Autowired
	private IAnchorOnlineContract anchorOnlineContract;
	
	@Autowired
	private IRechargeGuardVipService rechargeGaurdVipService;
	
	@Autowired
	private IRechargeDialExperienceService rechargeDialExperienceService;
	
	@Autowired
	private IFreeVideoChatExperienceLogContract freeVideoChatExperienceLogContract;
	
	@Autowired
	private IUserTariffeAgent userTariffeAgent;
	
	
	@Autowired
	private IRechargeChannelContract rechargeChannelContract;
	
	
	/**
	 * 充值金额列表
	 * 
	 * @return ActionResult
	 */
	@UserClientService("recharge")
	@Login
	@RequestMapping("/recharge/price/list")
	@ResponseBody
	public ActionResult listHomePrice(@RequestBody(required = false) String body) {
		try {
		  if(Tools.isNotNull(body)){
        JSONObject json = JsonHelper.toJsonObject(body);
        if(1 == json.getIntValue("category")){
          return rechargeWithdrawalService.getNativePriceList();
        }else if(2 == json.getIntValue("category")){
          return rechargeWithdrawalService.getEnergyPriceList();
        }
				return rechargeWithdrawalService.getNativePriceList();
      }else{
        return rechargeWithdrawalService.getNativePriceList();
      }
		} catch (Exception e) {
			logger.error("获取充值列表出错", e);
			return ActionResult.fail();
		}
	}
	
	
	/**
	 * 充值金额列表
	 * 
	 * @return ActionResult
	 */
	@UserClientService("recharge")
	@Login
	@RequestMapping("/category/price/list")
	@ResponseBody
	public ActionResult listCategoryPrice(@RequestBody(required = false) String body) throws Exception{
		RequestHeader header = RequestUtils.getCurrent().getHeader();
		String packageName = header.getPackageName(); 
		JSONObject json = JsonHelper.toJsonObject(body);
		   int category = json.getIntValue("category");
		   long anchorId = json.getLongValue("anchorId");
		   RechargeCategoryPriceEnum priceType = RechargeCategoryPriceEnum.getByCode(category);
		   Map<String, Object> outMap = new HashMap<>();
		   switch(priceType){
			   case video:
				   outMap = rechargeWithdrawalService.getPriceList(1,RechargeCategoryPriceEnum.video.getCode());
				   break;
			   case audio:
				   outMap = rechargeWithdrawalService.getPriceList(1,RechargeCategoryPriceEnum.audio.getCode());
				   break;
			   case video_ipa:
				   if(AppNameEnum.ios_com_duidui_duijiaoyou.getPackageName().equals(packageName)){
					   outMap = rechargeWithdrawalService.getPriceList(1,RechargePriceIosTypeEnum.ios_com_duidui_duijiaoyou.getCode());
				   }else  if(AppNameEnum.ios_com_xqjy_milian.getPackageName().equals(packageName)){
					   outMap = rechargeWithdrawalService.getPriceList(1,RechargePriceIosTypeEnum.ios_com_xqjy_milian.getCode());
				   }else  if(AppNameEnum.ios_com_xq_yuanyuan.getPackageName().equals(packageName)){
					   outMap = rechargeWithdrawalService.getPriceList(1,RechargePriceIosTypeEnum.ios_com_xq_yuanyuan.getCode());
				   }else  if(AppNameEnum.ios_com_jiaoyou_quliao.getPackageName().equals(packageName)){
					   outMap = rechargeWithdrawalService.getPriceList(1,RechargePriceIosTypeEnum.ios_com_jiaoyou_quliao.getCode());
				   }else{
					   outMap = rechargeWithdrawalService.getPriceList(1,RechargePriceIosTypeEnum.ios_com_tjhj_miyou.getCode()); 
				   }
				   break;
			/*
			   case audio_ipa:
				   outMap = rechargeWithdrawalService.getPriceList(1,RechargeCategoryPriceEnum.audio_ipa.getCode());
				   break;
				   */
			   case red_flower:
				   outMap = rechargeRedFlowerService.getPriceList();
				   break;
			   case guard:
				   outMap = rechargeGaurdVipService.getPriceList(anchorId,1);
				   break;
			   case VIP:
				   outMap = rechargeGaurdVipService.getVipPriceList(header.getMobile_model());
				   break;
			   case VIP_ipa:
				   if(AppNameEnum.ios_com_duidui_duijiaoyou.getPackageName().equals(packageName)){
					   outMap = rechargeGaurdVipService.getPriceList(0,3);
				   }else if(AppNameEnum.ios_com_tjhj_miyou.getPackageName().equals(packageName)){
					   outMap = rechargeGaurdVipService.getPriceList(0,6);
				   }else if(AppNameEnum.ios_com_xqjy_milian.getPackageName().equals(packageName)){
					   outMap = rechargeGaurdVipService.getPriceList(0,7);
				   }else if(AppNameEnum.ios_com_xq_yuanyuan.getPackageName().equals(packageName)){
					   outMap = rechargeGaurdVipService.getPriceList(0,8);
				   }else if(AppNameEnum. ios_com_jiaoyou_quliao.getPackageName().equals(packageName)){
					   outMap = rechargeGaurdVipService.getPriceList(0,9);
				   }
				  
				   break;
			   case weeks_card:
				   outMap = rechargeGaurdVipService.getWeeksCardPriceList();
				   break;
			   default:
				   outMap = rechargeWithdrawalService.getPriceList(1,RechargeCategoryPriceEnum.video.getCode());
				   break;
		   } 
		return ActionResult.success(outMap);
	}

	

	
	/**
	 * 进行充值列表页面检测是否需要进行弹出充值送商品的弹窗
	 * 
	 * @return ActionResult
	 * @throws Exception 
	 */
	@UserClientService("recharge")
	@RequestMapping("/recharge/showWindow")
	@ResponseBody
	public ActionResult showWindow() throws Exception {
		BeatContext bc = RequestUtils.getCurrent();
		long userId = bc.getUserid();
		PageModel pageModel = PageModel.getPageModel();
		pageModel.addQuery(Restrictions.eq("userid", userId));
		pageModel.addQuery(Restrictions.eq("show_status", 0));
		List<CommodityGroupRelationshipEntity> list = commodityGroupRelationshipContract.load(pageModel);
		if(Tools.isNotNull(list) && list.size() > 0) {
			DlgAndGoPage dlgAndGoPage = new DlgAndGoPage();
			dlgAndGoPage.setBtnName("去领取");
			dlgAndGoPage.setAndroidPageTag("WebSingleSaveFragment");
			dlgAndGoPage.setAndroidPageParam(dlgAndGoPage.getTagParam(Const.WEB_SITE+"/api/commodity/getCommodityList"));
			dlgAndGoPage.setHintInfo("您获得一个"+list.get(0).getGroup_detail().substring(2)+"哦\n赶紧去领取吧~");
			//清除对应的显示状态
			Map<String,Object> updateStatement = new HashMap<>();
			updateStatement.put("show_status", 1);
			commodityGroupRelationshipContract.updateByProperty(updateStatement, "userid", userId);
			return ActionResult.success(dlgAndGoPage);
		}
		return ActionResult.success();
	}

	/**
	 * 包装请求数据
	 * 
	 * @return ActionResult
	 */
	@UserClientService("recharge")
	@Login
	@RequestMapping("/recharge/wrap/data")
	@ResponseBody
	public ActionResult wrapPayData(@RequestBody String body) {
		try {
			JSONObject json = JsonHelper.toJsonObject(body);
			Integer category = json.getInteger("category");
			String transactionId = json.getString("transactionId");
			String receipt = json.getString("receipt");
			Map<String,String> ipaParam = new HashMap<>();
			ipaParam.put("transactionId", transactionId);
			ipaParam.put("receipt", receipt);
			
			long userId = RequestUtils.getCurrent().getUserid();
			String checkClientId = RequestUtils.getCurrent().getHeader().getClientId();
			if(Const.USER_STOP_RECHARGE_USERID_CLIENT_IP_MAP.containsKey(String.valueOf(userId)) 
					|| Const.USER_STOP_RECHARGE_USERID_CLIENT_IP_MAP.containsKey(checkClientId) 
					|| Const.USER_STOP_RECHARGE_USERID_CLIENT_IP_MAP.containsKey(Tools.getIP(RequestUtils.getCurrent().getRequest())) 
					){
				return ActionResult.fail(101,"系统升级中，暂时无法充值");
			}
			
			
			if(Tools.isNotNull(category)){
				if(RechargeCategoryPriceEnum.video.getCode() == category 
						|| RechargeCategoryPriceEnum.audio.getCode()== category 
						|| RechargeCategoryPriceEnum.video_ipa.getCode()== category 
						|| RechargeCategoryPriceEnum.audio_ipa.getCode()== category 
						){
					return rechargeWithdrawalService.recharge(json.getLongValue("priceId"), json.getIntValue("channel"), json.getDoubleValue("money"), json.getDoubleValue("income"), ipaParam);
				}else if(RechargeCategoryPriceEnum.red_flower.getCode() == category){
					return rechargeRedFlowerService.recharge(json.getLongValue("priceId"), json.getIntValue("channel"), json.getDoubleValue("money"), json.getDoubleValue("income"), null);
				}else if(RechargeCategoryPriceEnum.guard.getCode() == category || RechargeCategoryPriceEnum.VIP.getCode() == category || RechargeCategoryPriceEnum.VIP_ipa.getCode() == category || RechargeCategoryPriceEnum.weeks_card.getCode() == category){
					return rechargeGaurdVipService.recharge(json.getLongValue("priceId"), json.getIntValue("channel"), json.getDoubleValue("money"), json.getDoubleValue("income"), ipaParam,json.getLongValue("anchorId"));
				}else if(RechargeCategoryPriceEnum.dial_experience.getCode() == category ){
					return rechargeDialExperienceService.recharge(json.getLongValue("priceId"), json.getIntValue("channel"), json.getDoubleValue("money"), json.getDoubleValue("income"), null);
				}
			}
		} catch (Exception e) {
			logger.error("包装请求数据过程出错", e);
			return ActionResult.fail();
		}
		return ActionResult.fail();
	}
	

	
	/**
	 * h5支付处理
	 * 
	 * @return ActionResult
	 */
	@UserClientService("recharge")
	@ForbidDialAnnotation
	@Login
	@RequestMapping(value = "/recharge/h5/pay", produces = Produce.TEXT_JSON)
	@ResponseBody
	public ActionResult h5Pay(@RequestBody String body) {
		try {
			JSONObject json = JsonHelper.toJsonObject(body);
			int category = json.getIntValue("category");
			if(RechargeCategoryPriceEnum.video.getCode() == category 
					|| RechargeCategoryPriceEnum.audio.getCode()== category 
					|| RechargeCategoryPriceEnum.video_ipa.getCode()== category 
					|| RechargeCategoryPriceEnum.audio_ipa.getCode()== category 
					){
				return rechargeWithdrawalService.recharge(json.getLongValue("priceId"), json.getIntValue("channel"), json.getDoubleValue("money"), json.getDoubleValue("income"), null);
			}else if(RechargeCategoryPriceEnum.red_flower.getCode() == category){
				return rechargeRedFlowerService.recharge(json.getLongValue("priceId"), json.getIntValue("channel"), json.getDoubleValue("money"), json.getDoubleValue("income"), null);
			}else if(RechargeCategoryPriceEnum.guard.getCode() == category || RechargeCategoryPriceEnum.VIP.getCode() == category || RechargeCategoryPriceEnum.weeks_card.getCode() == category ){
				return rechargeGaurdVipService.recharge(json.getLongValue("priceId"), json.getIntValue("channel"), json.getDoubleValue("money"), json.getDoubleValue("income"), null,json.getLongValue("anchorId"));
			}else if(RechargeCategoryPriceEnum.dial_experience.getCode() == category ){
				return rechargeDialExperienceService.recharge(json.getLongValue("priceId"), json.getIntValue("channel"), json.getDoubleValue("money"), json.getDoubleValue("income"), null);
			}
			return rechargeWithdrawalService.recharge(json.getLongValue("priceId"), json.getIntValue("channel"), json.getDoubleValue("money"), json.getDoubleValue("income"), null);
		} catch (Exception e) {
			logger.error("包装H5请求数据过程出错", e);
			return ActionResult.fail();
		}
	}
	
	/**
	 * h5微信支付查询接口
	 * 
	 * @return ActionResult
	 */
	@UserClientService("recharge")
	@Login
	@RequestMapping(value = "/recharge/h5/pay/query", produces = Produce.TEXT_JSON)
	@ResponseBody
	public ActionResult h5PayQuery(@RequestBody String body) {
		int state = 0;
		long time = 0;
		try {
			JSONObject json = JsonHelper.toJsonObject(body);
			String orderId = null;
			if(Tools.isNotNull(json)) {
				orderId = json.getString("orderId");
			}
			if(Tools.isNotNull(orderId)) {
				UserPayActionEntity entity = userPayActionContract.findById(Long.parseLong(orderId));
				if(Tools.isNotNull(entity)) {
					state = entity.getStatus();
					time = new Date().getTime() - entity.getCreate_time().getTime();
				}
			}
			if(state == 1) {
				//检测是否需要跳转到对应的页面
				return ActionResult.success();
			} else {
				time = time / 1000;
				if(time > 120) {
					//超过的指定的时间值
					return ActionResult.fail(ErrorCodeEnum.h5_pay_query_timeout);
				} else {
					//可以进行进行查询处理
					return ActionResult.fail(ErrorCodeEnum.h5_pay_query_time);
				}
			}
		} catch (Exception e) {
			return ActionResult.fail();
		}
	}
	
	@FilterHeader
	@RequestMapping(value = "/recharge/h5/pay/finsh", produces = Produce.TEXT_HTML)
	public String h5PayFinsh() {
		return "wallet/payFinish_wx";
	}
	
	
	/**
	 * IOS版h5支付页面
	 * 
	 * @return ActionResult
	 */
	@UserClientService("recharge")
	@Login
	@RequestMapping(value = "/product/p/{priceId}", produces = Produce.TEXT_HTML)
	public String payH5(@PathVariable long priceId,Model model) throws Exception{
		RechargePriceEntity entity = rechargeWithdrawalService.getRechargeCustomPrice(priceId);
		model.addAttribute("money",Tools.formatDouble2PercentToString(entity.getMoney()));
		model.addAttribute("diamond",entity.getDiamond());
		model.addAttribute("priceId",priceId);
		model.addAttribute("afs",Const.WEB_SITE+"/api/product/pController/"+priceId);
		model.addAttribute("encrypt", RequestUtils.getCurrent().getHeaderEncrypt());
		return "wallet/pay";
	}
	
	/**
	 * IOS版h5支付处理
	 * 
	 * @return ActionResult
	 */
	@UserClientService("recharge")
	@Login
	@RequestMapping(value = "/product/pController/{priceId}", produces = Produce.TEXT_HTML)
	public String zfbCharge(@PathVariable long priceId,Model model) throws Exception{
		ActionResult result = rechargeWithdrawalService.rechargeH5(priceId, PayChannelEnum.aliH5.getCode(),0, 0,"");
		model.addAttribute("content",result.getData());
		return "wallet/aliPayH5";
	}
	
	
	/**
	 * 不限系统h5支付页面
	 * 
	 * @return ActionResult
	 */
	@UserClientService("recharge")
	@ForbidDialAnnotation
	@Login
	@RequestMapping(value = "/product/all/{category}/{priceId}", produces = Produce.TEXT_HTML)
	public String payH5(@PathVariable long category,@PathVariable long priceId,Model model,HttpServletRequest request) throws Exception{
		long userId = RequestUtils.getCurrent().getUserid();
		int  osType = RequestUtils.getCurrent().getHeader().getOs_type();
		String anchorId = request.getParameter("anchorId");
		RechargePriceDto entity = rechargeWithdrawalService.getRechargePriceByPriceId(osType,category,priceId);
		model.addAttribute("money",Tools.formatDouble2PercentToString(entity.getMoney()));
		model.addAttribute("diamond",entity.getDiamond());
		long incomeBalance = userIncomeAgent.getIncomeBalance(userId, AccountType.GENERAL);
		
		float incomeRatio = 0;
		ChargeDeductionConfigDto  chargeDeductionDto = sysConfigAgent.chargeDeduction();
			if(chargeDeductionDto.getStatus()==1){
				incomeRatio = chargeDeductionDto.getRatio(); 
			}
		
		double deduction = entity.getMoney()*incomeRatio;
		if( Tools.parseDouble(incomeBalance+"") >=deduction){
			model.addAttribute("deduction", Tools.formatDouble2(deduction/100.0));
		}else{
			model.addAttribute("deduction", Tools.formatDouble2PercentToString(incomeBalance));
		}
		
		model.addAttribute("deductionRatio", incomeBalance > 0 && deduction >0 ? true:false);
		model.addAttribute("ratio",  String.valueOf(Tools.getFloat(incomeRatio*100,2))+"%");
		model.addAttribute("priceId",priceId+"");
		model.addAttribute("afs","/api/product/all/pController");
		model.addAttribute("category",category);
		model.addAttribute("buyText",entity.getBuyText());
		model.addAttribute("encrypt", RequestUtils.getCurrent().getHeaderEncrypt());
		model.addAttribute("anchorId", Tools.isNotNull(anchorId)?anchorId:"0");
		
		RechargeChannelEntity zfb = rechargeChannelContract.findById(1L);
		if(Tools.isNotNull(zfb)){
			model.addAttribute("zfbChannel",zfb.getStatus());
		}else {
			model.addAttribute("zfbChannel",1);
		}
		RechargeChannelEntity weiXin = rechargeChannelContract.findById(2L);
		if(Tools.isNotNull(weiXin)){
			model.addAttribute("weiXinChannel",weiXin.getStatus());
		}else {
			model.addAttribute("weiXinChannel",1);
		}
		
		String checkClientId = RequestUtils.getCurrent().getHeader().getClientId();
		if(Const.USER_STOP_RECHARGE_USERID_CLIENT_IP_MAP.containsKey(String.valueOf(userId)) 
				|| Const.USER_STOP_RECHARGE_USERID_CLIENT_IP_MAP.containsKey(checkClientId) 
				|| Const.USER_STOP_RECHARGE_USERID_CLIENT_IP_MAP.containsKey(Tools.getIP(RequestUtils.getCurrent().getRequest())) 
				){
			return "wallet/grade";
		}
		
		
		return "wallet/allPay";
	}
	
	
	/**
	 * 不限系统h5支付处理
	 * 
	 * @return ActionResult
	 */
	@ForbidDialAnnotation
	@UserClientService("recharge")
	@Login
	@RequestMapping(value = "/product/all/pController", produces = Produce.TEXT_HTML)
	public String zfbCharge(Model model,HttpServletRequest request) throws Exception{
		String categoryStr = request.getParameter("category");
		String priceId = request.getParameter("priceId");
		String money = request.getParameter("money");
		String income = request.getParameter("income");
		String anchorId = request.getParameter("anchorId");
		
		int category = Integer.parseInt(categoryStr); 
		logger.info("pController_param:"+"category:"+category+";priceId:"+priceId+";money:"+money+";income:"+income+""+";anchorId="+anchorId);
		ActionResult result = null;
		if(RechargeCategoryPriceEnum.video.getCode() == category 
				|| RechargeCategoryPriceEnum.audio.getCode()== category 
				|| RechargeCategoryPriceEnum.video_ipa.getCode()== category 
				|| RechargeCategoryPriceEnum.audio_ipa.getCode()== category 
				){
			result = rechargeWithdrawalService.recharge(Long.parseLong(priceId), PayChannelEnum.aliH5.getCode(), Tools.parseDouble(money), Tools.parseDouble(income), null);
		}else if(RechargeCategoryPriceEnum.red_flower.getCode() == category){
			result = rechargeRedFlowerService.recharge(Long.parseLong(priceId), PayChannelEnum.aliH5.getCode(), Tools.parseDouble(money), Tools.parseDouble(income), null);
		}else if(RechargeCategoryPriceEnum.guard.getCode() == category ){
			if(Tools.isNotNull(anchorId) && Tools.parseLong(anchorId)>0 ){
				result = rechargeGaurdVipService.recharge(Long.parseLong(priceId), PayChannelEnum.aliH5.getCode(), Tools.parseDouble(money), Tools.parseDouble(income), null,Long.parseLong(anchorId));
			}else{
				model.addAttribute("msg","主播ID不能为空");
				return "error";
			}
			
		}else if(RechargeCategoryPriceEnum.VIP.getCode() == category || RechargeCategoryPriceEnum.weeks_card.getCode() == category){
			result = rechargeGaurdVipService.recharge(Long.parseLong(priceId), PayChannelEnum.aliH5.getCode(), Tools.parseDouble(money), Tools.parseDouble(income), null,0L);
		}else if(RechargeCategoryPriceEnum.dial_experience.getCode() == category ){
			result = rechargeDialExperienceService.recharge(Long.parseLong(priceId), PayChannelEnum.aliH5.getCode(), Tools.parseDouble(money), Tools.parseDouble(income), null);
		}
		if(Tools.isNotNull(result)){
			model.addAttribute("content",result.getData());	
			if(result.getCode() !=ActionResult.success().getCode()){
				model.addAttribute("msg",result.getCodemsg());
				return "error";
			}
		}
		return "wallet/aliPayH5";
	}

	

	
	
	
	

	/**
	 * 提现收益申请页
	 * 
	 * @return ActionResult
	 */
	@UserClientService("withdrawal")
	@Login
	@RequestMapping("/income/page/data")
	@ResponseBody
	public ActionResult incomeData() {
		try {
			return rechargeWithdrawalService.getIncomePage();
		} catch (Exception e) {
			logger.error("获取收益首页出错", e);
			return ActionResult.fail();
		}
	}
	
	/**
	 * 提现收益申请页
	 * 
	 * @return ActionResult
	 */
	@UserClientService("withdrawal")
	@Login
	@NoSign
	@RequestMapping(value = "/income/h5Page",produces = Produce.TEXT_HTML)
	public String incomeH5(Model model) throws Exception {
		long userid = RequestUtils.getCurrent().getUserid();
		UserIncomeAccountEntity account = userIncomeAccountContract.findByUserIdAndType(userid, AccountType.GENERAL.getCode());
		long noWithdrawalBalance = userIncomeAgent.getNoWithdrawalBounds(userid, AccountType.GENERAL);
		if (null != account) {
			long balance =account.getBalance()>=noWithdrawalBalance?account.getBalance()-noWithdrawalBalance:0L;
			model.addAttribute("deposit", account.getDeposit() / 100.0);
			model.addAttribute("balance",  balance/ 100.0);
		}else{
			model.addAttribute("deposit", 0.0);
			model.addAttribute("balance", 0.0);
		}
		
		VacuateConfigDto vacuate = sysConfigAgent.vacuate();
		model.addAttribute("taxRatio", vacuate.getTaxRatio());
		model.addAttribute("encrypt",RequestUtils.getCurrent().getHeaderEncrypt());
		
		return "wallet/myIncome";
		
	}
	
	/**
	 * 
	 * 我的钱包
	 * @return ActionResult
	 */
	@UserClientService("withdrawal")
	@ForbidDialAnnotation 
	@Login
	@RequestMapping(value = "/income/zuanList",produces = Produce.TEXT_HTML)
	public String zuanList(HttpServletRequest request,Model model) throws Exception {
		String pricePage = request.getParameter("pricePage");
		String userName = RequestUtils.getCurrent().getUser().getUsername();
		Long userId = RequestUtils.getCurrent().getUserid();
		String packageName = RequestUtils.getCurrent().getHeader().getPackageName();
		String channel = RequestUtils.getCurrent().getHeader().getChannel();
		int os_type= RequestUtils.getCurrent().getHeader().getOs_type();
		/*
		if(packageName.equals(AppNameEnum.ios_com_yo_miliaoliao.getPackageName())){
			
		}else if(confService.testIOS() || Const.IOS_TEST_MOBILE_ACCOUNT_MAP.containsKey(userName)){
			if(packageName.equals(AppNameEnum.ios_com_paopao_paopao.getPackageName())
				|| packageName.equals(AppNameEnum.ios_com_yjlt_yjlt.getPackageName())
				|| packageName.equals(AppNameEnum.ios_com_yujian2019_yujian2019.getPackageName())
					){
				return "share/localShare_ios_paopao";
			}else if (packageName.equals(AppNameEnum.ios_com_qiguo_qiguo.getPackageName())){
				model.addAttribute("bgName", "qiguoBG.png");
				return "share/localShare_ios_qiguo";
			}
			return "share/localShare_ios_show";
		}
		*/
		
	
		
		
		UserBO user = userAgent.findById(userId);
		
		/*
		if (vChatTextYXService.checkShowVIPFragment(user) && user.getVip() == 0 && os_type == 1){
			
			Map<String, Object> outMap = rechargeGuardVipService.getVipPriceList(RequestUtils.getCurrent().getHeader().getMobile_model());
			model.addAllAttributes(outMap);
			logger.info("chargeVipList:"+JsonHelper.toJson(outMap));
			//TODO:前端给页面
			return "integral/goH5Vip";
		}
		*/
			/*
			PageModel pageModel = PageModel.getLimitModel(0, 30);
			pageModel.addQuery(Restrictions.eq("state", 1));
			pageModel.addQuery(Restrictions.eq("flag", 0));
			List<AnchorOnlineEntity> anchors = anchorOnlineContract.load(pageModel);
			List<WebSubscribeIndexDto> dtoList = new ArrayList<WebSubscribeIndexDto>();
			List<AnchorOnlineEntity> anchorList = new ArrayList<AnchorOnlineEntity>();
			if(Tools.isNotNull(anchors) ){
				Collections.shuffle(anchors);
				anchorList.addAll(anchors);
				
				pageModel.clearAll();
				pageModel = PageModel.getLimitModel(0, 30);
				pageModel.addQuery(Restrictions.eq("state", 1));
				pageModel.addQuery(Restrictions.gt("flag", 0));
				anchors = anchorOnlineContract.load(pageModel);
				if(Tools.isNotNull(anchors)){
					Collections.shuffle(anchors);
					anchorList.addAll(anchors);
				}
				
			}
			if (anchorList.size()>=4){
				anchorList = anchorList.subList(0, 4);
				for(AnchorOnlineEntity anchor:anchorList){
					WebSubscribeIndexDto dto = new WebSubscribeIndexDto();
					dto.setUserid(anchor.getUserid()+"");
					dto.setNickname(anchor.getNickname());
					dto.setPhoto(ServiceHelper.getUserSmallPhoto(anchor.getPhoto()));
					dtoList.add(dto);
				}
			}
			model.addAttribute("anchors", dtoList);
			
			return "integral/goVip";
			}
		*/
		boolean mobleFlag = false;
		boolean moblePop = false;
		if(Tools.isNotNull(user)){
			if(!Tools.isPhone(user.getUsername()) && Tools.isNull(user.getMobile())){
				mobleFlag = true;
				UserMobileAskEntity ask = userMobileAskContract.findByProperty("userid", userId);
				if(Tools.isNull(ask)){
					moblePop = true;
					ask = new UserMobileAskEntity();
					ask.setUserid(userId);
					ask.setCreate_time(new Date());
					userMobileAskContract.insert(ask);
				}
			}
		}
		Map<String,Object> diamondMap = rechargeWithdrawalService.getPriceList(0,0);
		Map<String,Object> flowerMap = rechargeRedFlowerService.getPriceList();
		model.addAttribute("diamondPrice",diamondMap);
		model.addAttribute("flowerPrice",flowerMap);
		model.addAttribute("mobleFlag",mobleFlag);
		model.addAttribute("moblePop",moblePop);
		
	
	
		model.addAttribute("quickChargeFalg",0);
		PageModel pageModel = PageModel.getPageModel();
		pageModel.addQuery(Restrictions.eq("user_id", userId));
		pageModel.addQuery(Restrictions.eq("status",1));
		if(rechargeOrderContract.count(pageModel) == 0 ){
			UserDiamondPriceWatchLogEntity watchLog = userDiamondPriceWatchLogContract.findByProperty("userid",userId);
			FreeVideoChatExperienceLogEntity freeVideoChatExperienceLogEntity =  freeVideoChatExperienceLogContract.findByProperty("userid", userId);
			if(Tools.isNull(watchLog) &&  Tools.isNull(freeVideoChatExperienceLogEntity)){
				model.addAttribute("quickChargeFalg",1);
				watchLog = new UserDiamondPriceWatchLogEntity();
				watchLog.setUserid(userId);
				watchLog.setCreate_time(new Date());
				userDiamondPriceWatchLogContract.insert(watchLog);
			}
			
		}else{
			model.addAttribute("quickChargeFalg",0);
		}
		//本渠道也不显示快充
		if("User_Share_i".equalsIgnoreCase(channel) || "User_Share_a".equalsIgnoreCase(channel) ){
			model.addAttribute("quickChargeFalg",0);
		}
		/*
		if(AppNameEnum.andriod_com_tjhj_miliao.getPackageName().equalsIgnoreCase(packageName)){
			model.addAttribute("quickChargeFalg",0);
		}
		*/
		if(Tools.isNotNull(user)){
			long day = (Tools.getDayTime()-Tools.getDayTime(user.getCreateTime()))/(24 * 60 * 60 * 1000);
			if(day>=30){
				model.addAttribute("quickChargeFalg",0);
			}
		}
		if(!AppNameEnum.andriod_com_tjhj_miliao.getPackageName().equalsIgnoreCase(packageName)){
			String cityCode = RequestUtils.getCurrent().getHeader().getCityCode();
			UserBO userBo = userAgent.findById(userId);
			if(!Const.IS_TEST){
				if("332".equals(cityCode) || userBo.getProvinceid()==3958L || "131".equals(cityCode) || userBo.getProvinceid()==3593L || "Huawei_yoyo3".equalsIgnoreCase(packageName)){
					model.addAttribute("quickChargeFalg",0);
				}
			}
			
		}
		
		//全部
		model.addAttribute("quickChargeFalg",0);
		
		
		/*
		Map<String, Object> dialExperience = rechargeDialExperienceService.getPriceList();
		if(Tools.isNotNull(dialExperience)){
			model.addAllAttributes(dialExperience);
			model.addAttribute("quickChargeFalg",false);
			logger.info("rechargeDialExperienceService:"+JsonHelper.toJson(dialExperience));
		}
		*/
		
		if(rechargeWithdrawalService.checkUserAccountLimit(userId)){
			model.addAttribute("quickChargeFalg",0);
		}
		
		model.addAttribute("incomeRecordDesc","我的收益");
		model.addAttribute("incomeRecordUrl","/api/income/h5Page");
		
		AnchorOnlineEntity anchor =  anchorOnlineContract.getAnchorOnlineEntity(userId);
		if(Tools.isNotNull(anchor)){
			if(anchor.getShow_income()==0){
				model.addAttribute("incomeRecordDesc","我的流水");
				model.addAttribute("incomeRecordUrl","/api/income/cash/newList");
			}
		}
		
		
		if("1".equals(pricePage)){
			model.addAttribute("pricePage","1");
		}else{
			model.addAttribute("pricePage","0");
		}
		
		/*
		if(userFirstRechargeLogContract.checkFirstRecharge(userId, FirstPayTypeEnum.diamond.getCode())){
			model.addAttribute("firstChargeFalg","1");
		}else{
			model.addAttribute("firstChargeFalg","0");
		}
		*/
		model.addAttribute("firstChargeFalg","0");
		
		if (AppNameEnum.andriod_com_tjhj_dvzs.getPackageName().equals(packageName)){
			model.addAttribute("weekCardFlag","0");
		}else{
			if (RequestUtils.getCurrent().getHeader().getVersioncode()>=54) {
				model.addAttribute("weekCardFlag","1");
			}else{
				model.addAttribute("weekCardFlag","0");
			}
			
		}
		
		return "wallet/zuanList";
		
	}
	
	
	/**
	 * 
	 * 体验购买弹窗
	 * @return ActionResult
	 */
	@UserClientService("withdrawal")
	@Login
	@RequestMapping(value = "/buy/dialExperience",produces = Produce.TEXT_HTML)
	public String dialExperience(Model model) throws Exception {
		Map<String,Object> priceMap = rechargeDialExperienceService.getPriceList();
		model.addAllAttributes(priceMap);
		return "/activity/goddess/experience";
	}
	
	/**
	 * 跳传绑定手机号页面
	 * 
	 * @return ActionResult
	 */
	@UserClientService("withdrawal")
	@Login
	@NoSign
	@RequestMapping(value = "/userSafe/gotoMobilePage",produces = Produce.TEXT_HTML)
	public String gotoMobilePage(Model model) throws Exception {
		model.addAttribute("encrypt",RequestUtils.getCurrent().getHeaderEncrypt());
		long userid = RequestUtils.getCurrent().getUserid();
		if (userTariffeAgent.getTariffeBalance(userid) >= Const.GET_PHONE_MONEY) {
			model.addAttribute("picFlag", 1);
		} else {
			model.addAttribute("picFlag", 0);
		}
		return "wallet/bundMobile";
		
	}
	
	
	/**
	 * 获取验证码
	 * 
	 * @return ActionResult
	 * @throws Exception 
	 */
	@UserClientService("withdrawal")
	@Login
	@NoSign
	@RequestMapping(value = "/userSafe/bindingMobile", produces = Produce.TEXT_JSON)
	@ResponseBody
	public ActionResult bindingMobile(@RequestBody String body ) throws Exception {
		JSONObject json = JsonHelper.toJsonObject(body);
		String mobile  = json.getString("mobile");
		
		if(Tools.isNotNull(mobile)){
			String regex = "^((13[0-9])|(14[5,7])|(15[0-3,5-9])|(17[0,3,5-8])|(18[0-9])|166|198|199|(147))\\d{8}$";
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(mobile);
	        boolean isMatch = m.matches();
	        if(!isMatch){
	        	return ActionResult.fail(ErrorCodeEnum.share_vip_moblie_error);
	        }
		}
		ActionResult result = ActionResult.success();
		if(Tools.isNotNull(mobile)){
			result = sendMessageAgent.sendMobileValidCode(mobile, SendSmsTypeEnum.auth_mobile);
		}
		return result;
	}
	
	/**
	 * 获取验证码
	 * 
	 * @return ActionResult
	 * @throws Exception 
	 */
	@UserClientService("withdrawal")
	@Login
	@NoSign
	@RequestMapping(value = "/userSafe/updateMobile", produces = Produce.TEXT_JSON)
	@ResponseBody
	public ActionResult updateMobile(@RequestBody String body ) throws Exception {
		JSONObject json = JsonHelper.toJsonObject(body);
		String mobile  = json.getString("mobile");
		String recode  = json.getString("recode");
		long userid = RequestUtils.getCurrent().getUserid();
		if(Tools.isNotNull(mobile)){
			String regex = "^((13[0-9])|(14[5,7])|(15[0-3,5-9])|(17[0,3,5-8])|(18[0-9])|166|198|199|(147))\\d{8}$";
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(mobile);
	        boolean isMatch = m.matches();
	        if(!isMatch){
	        	return ActionResult.fail(ErrorCodeEnum.share_vip_moblie_error);
	        }
		}
		if(Tools.isNull(recode)){
			return ActionResult.fail(ErrorCodeEnum.sms_recode_Error);
		}
		if(Tools.isNotNull(mobile) && Tools.isNotNull(recode)){
			if(!sendMessageAgent.checkCode(mobile, recode)){
				return ActionResult.fail(AgentErrorCodeEnum.valid_code_error);
			}
			if (Tools.isNull(validCodeService.getValidCode(mobile))) {
				return ActionResult.fail(AgentErrorCodeEnum.valid_code_pass);
			}
			UserBO newUser =  userAgent.findByMobile(mobile);
			if(Tools.isNotNull(newUser)){
				return ActionResult.fail(ErrorCodeEnum.auth_mobile_exist);
			}
			newUser = userAgent.findByUsername(mobile);
			if(Tools.isNotNull(newUser)){
				return ActionResult.fail(ErrorCodeEnum.auth_mobile_exist);
			}
			
			UserModifyDto modifyDto = new UserModifyDto();
			modifyDto.setUserid(RequestUtils.getCurrent().getUserid());
			modifyDto.setMobile(mobile);
			userAgent.updateUser(modifyDto);
			RequestHeader requestHeader = RequestUtils.getCurrent().getHeader();
			if( "shenghe_wxb".equals(requestHeader.getChannel()) || "Huawei_yoyo3".equals(requestHeader.getChannel())) {
				CopyUserEntity copyUser = copyUserContract.findById(RequestUtils.getCurrent().getUserid());
				if (copyUser!=null) {
					CopyUserEntity temp = new CopyUserEntity();
					temp.setId(RequestUtils.getCurrent().getUserid());
					temp.setMobile(AESFieldUtils.encrypt(mobile));
					copyUserContract.update(temp);
				}
			}
			//手机号绑定成功送积分
			userPointAgent.changePointAccount(RequestUtils.getCurrent().getUserid(),UserPointAccountLogTypeEnum.bind_mobile.getCode(), 1, mobile, UserPointAccountLogTypeEnum.bind_mobile.getDesc());
			
			HashMap<String, Object> getPhoneMap = new HashMap<>();
			if (userTariffeAgent.getTariffeBalance(userid) >= Const.GET_PHONE_MONEY) {
				getPhoneMap.put("phoneMoneyFlag", 1);
				getPhoneMap.put("phoneUrl", Const.WEB_SITE+"/api/web/getTelephoneCharge");
			} else {
				getPhoneMap.put("phoneMoneyFlag", 0);
			}
			return ActionResult.success(getPhoneMap);
		}
		return ActionResult.fail();
	}
	
	
	/**
	 * 奖励金
	 * 
	 * @return ActionResult
	 */
	@UserClientService("bonus")
	@Login
	@RequestMapping("/bonus/page/data")
	@ResponseBody
	public ActionResult bonusData() {
		try {
			return rechargeWithdrawalService.getBonusPage();
		} catch (Exception e) {
			logger.error("获取收益首页出错", e);
			return ActionResult.fail();
		}
	}

	/**
	 * scvc说明
	 * @return
	 * @throws Exception
	 */
	@NoSign
	@RequestMapping(value = "/bonus/scvc/help", produces = Produce.TEXT_HTML)
	public String scvcIntroduction() throws Exception {
		return "money/scvc";
	}
	
	/**
	 * 提现申请
	 * 
	 * @return ActionResult
	 */
	@UserClientService("withdrawal")
	@Login
	@RequestMapping("/withdrawal/cash/apply")
	@ResponseBody
	public ActionResult cashWithdrawalApply(@RequestBody String body) {
		try {
			JSONObject json = JsonHelper.toJsonObject(body);
			UserDetails user = RequestUtils.getCurrent().getUser();
			int via = json.getIntValue("via");
			String name = json.getString("name");
			String account = json.getString("account");
			if (2 == via) {
				if (Tools.isNull(name) || Tools.isNull(account)) {
					return ActionResult.fail(ErrorCodeEnum.parameter_isnull);
				}
			}
			float money = json.getFloatValue("money");
			
			if (Const.WITHDRAWAL_LIMIT > money) {// 提现限额
				return ActionResult.fail(ErrorCodeEnum.user_withdrawal_limit);
			}
			
			IncomeResultDto<Map<String, Long>> result = userIncomeAgent.withdrawalMoney((int)(money * 100), user.getUserid(), user.getNickname(), via, UserIncomeAccountLogTypeEnum.cash_withdrawal,
					name, account);
			if (0 == result.getCode()) {
				Map<String, Object> data = new HashMap<>();
				Map<String, Long> x = result.getData();
				data.put("balance", x.get("balance") / 100.0);
				data.put("detail", "提现明细\n收益提现：" + money + "元\n" + (x.get("bonus") > 0 ? "奖励金："+ x.get("bonus") / 100.0 + "元" : ""));
				return ActionResult.success(data);
			} else {
				return ActionResult.fail(AgentErrorCodeEnum.getByCode(result.getCode()));
			}
		} catch (Exception e) {
			logger.error("申请提现出错", e);
			return ActionResult.fail();
		}
	}
	
	
	/**
	 * 提现申请
	 * 
	 * @return ActionResult
	 */
	@UserClientService("withdrawal")
	@Login
	@RequestMapping(value = "/withdrawal/cash/applyH5", produces = Produce.TEXT_JSON)
	@ResponseBody
	public ActionResult cashWithdrawalApplyH5(@RequestBody String body) {
		return cashWithdrawalApply(body);
	}
	

	/**
	 * 收益明细
	 * 
	 * @return ActionResult
	 */
	@UserClientService("withdrawal")
	@Login
	@NoSign
	@RequestMapping(value = "/income/cash/list", produces = Produce.TEXT_HTML)
	public String cashIncomeLog(Model model) {
		model.addAttribute("header", RequestUtils.getCurrent().getHeaderEncrypt());
		return "wallet/incomeDetail";
	}
	
	/**
	 * 收益明细
	 * 
	 * @return ActionResult
	 */
	@UserClientService("withdrawal")
	@Login
	@NoSign
	@RequestMapping(value = "/income/cash/newList", produces = Produce.TEXT_HTML)
	public String cashIncomeNewLog(Model model) throws Exception {
		model.addAttribute("header", RequestUtils.getCurrent().getHeaderEncrypt());
		model.addAttribute("incomeRecordDesc","收益明细");
		long userId = RequestUtils.getCurrent().getUserid();
		AnchorOnlineEntity anchor =  anchorOnlineContract.getAnchorOnlineEntity(userId);
		if(Tools.isNotNull(anchor)){
			if(anchor.getShow_income()==0){
				model.addAttribute("incomeRecordDesc","流水明细");
			}
		}
		
		return "wallet/incomeDetail_new";
		//return "wallet/incomeDetailed";
	}

	/**
	 * 收益明细数据
	 * 
	 * @return ActionResult
	 */
	@UserClientService("withdrawal")
	@Login
	@NoSign
	@RequestMapping(value = "/income/cash/list/ajax", produces = Produce.TEXT_JSON)
	@ResponseBody
	public ActionResult cashIncomeLogData(@RequestBody(required=false) String body) {
		 JSONObject json = JsonHelper.toJsonObject(body);
	      Integer stamp =  json.getInteger("stamp");
		try {
			return rechargeWithdrawalService.incomeCashLog(stamp);
		} catch (Exception e) {
			logger.error("获取收益明细列表出错", e);
			return ActionResult.fail();
		}
	}

	/**
	 * 提现记录
	 * 
	 * @return ActionResult
	 */
	@UserClientService("withdrawal")
	@Login
	@NoSign
	@RequestMapping(value = "/withdrawal/cash/list", produces = Produce.TEXT_HTML)
	public String cashWithdrawalLog(Model model) {
		model.addAttribute("header", RequestUtils.getCurrent().getHeaderEncrypt());
		return "wallet/drawMoneyRecord";
	}
	
	/**
	 * 提现记录
	 * 
	 * @return ActionResult
	 */
	@UserClientService("withdrawal")
	@Login
	@NoSign
	@RequestMapping(value = "/withdrawal/cash/newlist", produces = Produce.TEXT_HTML)
	public String cashWithdrawalNewLog(Model model) {
		model.addAttribute("header", RequestUtils.getCurrent().getHeaderEncrypt());
		return "wallet/drawMoneyRecord_new";
	}

	/**
	 * 提现记录数据
	 * 
	 * @return ActionResult
	 */
	@UserClientService("withdrawal")
	@Login
	@NoSign
	@RequestMapping(value = "/withdrawal/cash/list/ajax", produces = Produce.TEXT_JSON)
	@ResponseBody
	public ActionResult cashWithdrawalLogData() {
		try {
			return rechargeWithdrawalService.withdrawalCashLog();
		} catch (Exception e) {
			logger.error("获取提现记录列表出错", e);
			return ActionResult.fail();
		}
	}
}
