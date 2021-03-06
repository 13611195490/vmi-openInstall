package com.tigerjoys.shark.miai.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.shark.miai.common.constant.CommonConst;
import org.shark.miai.common.enums.CouponGroupEnum;
import org.shark.miai.common.enums.PlatformEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tigerjoys.nbs.common.ActionResult;
import com.tigerjoys.nbs.common.cache.CacheRedis;
import com.tigerjoys.nbs.common.http.HttpUtils;
import com.tigerjoys.nbs.common.utils.BeanUtils;
import com.tigerjoys.nbs.common.utils.DistanceUtils;
import com.tigerjoys.nbs.common.utils.ExecutorServiceHelper;
import com.tigerjoys.nbs.common.utils.JsonHelper;
import com.tigerjoys.nbs.common.utils.MD5;
import com.tigerjoys.nbs.common.utils.Tools;
import com.tigerjoys.nbs.common.utils.sequence.IdGenerater;
import com.tigerjoys.nbs.mybatis.core.page.PageModel;
import com.tigerjoys.nbs.mybatis.core.sql.Restrictions;
import com.tigerjoys.nbs.web.context.RequestHeader;
import com.tigerjoys.nbs.web.context.RequestUtils;
import com.tigerjoys.shark.miai.Const;
import com.tigerjoys.shark.miai.RedisCacheConst;
import com.tigerjoys.shark.miai.agent.IAppUserAllowanceAgent;
import com.tigerjoys.shark.miai.agent.ICouponAgent;
import com.tigerjoys.shark.miai.agent.IFreeVideoChatExperienceAgent;
import com.tigerjoys.shark.miai.agent.IGlobalBroadcastAgent;
import com.tigerjoys.shark.miai.agent.IIOSUserSmsAgent;
import com.tigerjoys.shark.miai.agent.INeteaseAgent;
import com.tigerjoys.shark.miai.agent.IRedFlowerAgent;
import com.tigerjoys.shark.miai.agent.ISendMessageAgent;
import com.tigerjoys.shark.miai.agent.ISysConfigAgent;
import com.tigerjoys.shark.miai.agent.ITencentIMAgent;
import com.tigerjoys.shark.miai.agent.IUserAgent;
import com.tigerjoys.shark.miai.agent.IUserDiamondAgent;
import com.tigerjoys.shark.miai.agent.IUserForegroundAgent;
import com.tigerjoys.shark.miai.agent.IUserGeoAgent;
import com.tigerjoys.shark.miai.agent.IUserIncomeAgent;
import com.tigerjoys.shark.miai.agent.IUserOnlineListAgent;
import com.tigerjoys.shark.miai.agent.IUserOrdinaryOnlineListAgent;
import com.tigerjoys.shark.miai.agent.IUserWakeUpMessageAgent;
import com.tigerjoys.shark.miai.agent.dto.AreaDto;
import com.tigerjoys.shark.miai.agent.dto.UserBO;
import com.tigerjoys.shark.miai.agent.dto.UserLoginBO;
import com.tigerjoys.shark.miai.agent.dto.VchatRoomObscurationConfigDto;
import com.tigerjoys.shark.miai.agent.dto.transfer.UserCreatDto;
import com.tigerjoys.shark.miai.agent.dto.transfer.UserModifyDto;
import com.tigerjoys.shark.miai.agent.enums.AgentErrorCodeEnum;
import com.tigerjoys.shark.miai.agent.enums.AppUserAllowanceTypeEnum;
import com.tigerjoys.shark.miai.agent.enums.GlobalBroadcastTypeEnum;
import com.tigerjoys.shark.miai.agent.enums.NeteaseErrorEnum;
import com.tigerjoys.shark.miai.agent.enums.RedFlowerAccountLogTypeEnum;
import com.tigerjoys.shark.miai.agent.enums.SendSmsTypeEnum;
import com.tigerjoys.shark.miai.agent.enums.UserDiamondAccountLogTypeEnum;
import com.tigerjoys.shark.miai.agent.enums.UserFrEnum;
import com.tigerjoys.shark.miai.agent.enums.UserIncomeAccountLogTypeEnum;
import com.tigerjoys.shark.miai.agent.enums.UserLoginLogTypeEnum;
import com.tigerjoys.shark.miai.agent.service.IAppAreaService;
import com.tigerjoys.shark.miai.agent.service.IValidCodeService;
import com.tigerjoys.shark.miai.dto.service.DynamicUserDataDto;
import com.tigerjoys.shark.miai.dto.service.RegLoginDto;
import com.tigerjoys.shark.miai.dto.service.ThirdRegistDto;
import com.tigerjoys.shark.miai.enums.ErrorCodeEnum;
import com.tigerjoys.shark.miai.es.service.IEsMobileUserAppRecordService;
import com.tigerjoys.shark.miai.inter.contract.IAnchorOnlineContract;
import com.tigerjoys.shark.miai.inter.contract.IAppChannelContract;
import com.tigerjoys.shark.miai.inter.contract.IAppPackageContract;
import com.tigerjoys.shark.miai.inter.contract.IAppVersionContract;
import com.tigerjoys.shark.miai.inter.contract.ICopyUserContract;
import com.tigerjoys.shark.miai.inter.contract.IDeviceContract;
import com.tigerjoys.shark.miai.inter.contract.IPayUserRecordContract;
import com.tigerjoys.shark.miai.inter.contract.IRootUserRegLogContract;
import com.tigerjoys.shark.miai.inter.contract.IShareVipCardContract;
import com.tigerjoys.shark.miai.inter.contract.IThirdPartySpreadContract;
import com.tigerjoys.shark.miai.inter.contract.IUserContract;
import com.tigerjoys.shark.miai.inter.contract.IUserLoginLogContract;
import com.tigerjoys.shark.miai.inter.contract.IUserLogoutLogContract;
import com.tigerjoys.shark.miai.inter.contract.IUserPrivacySecurityContract;
import com.tigerjoys.shark.miai.inter.contract.IUserRegLogContract;
import com.tigerjoys.shark.miai.inter.entity.AnchorOnlineEntity;
import com.tigerjoys.shark.miai.inter.entity.AppChannelEntity;
import com.tigerjoys.shark.miai.inter.entity.AppPackageEntity;
import com.tigerjoys.shark.miai.inter.entity.AppVersionEntity;
import com.tigerjoys.shark.miai.inter.entity.CopyUserEntity;
import com.tigerjoys.shark.miai.inter.entity.DeviceEntity;
import com.tigerjoys.shark.miai.inter.entity.PayUserRecordEntity;
import com.tigerjoys.shark.miai.inter.entity.RootUserRegLogEntity;
import com.tigerjoys.shark.miai.inter.entity.ThirdPartySpreadEntity;
import com.tigerjoys.shark.miai.inter.entity.UserLoginLogEntity;
import com.tigerjoys.shark.miai.inter.entity.UserLogoutLogEntity;
import com.tigerjoys.shark.miai.inter.entity.UserPrivacySecurityEntity;
import com.tigerjoys.shark.miai.inter.entity.UserRegLogEntity;
import com.tigerjoys.shark.miai.service.IChannelCheckService;
import com.tigerjoys.shark.miai.service.IConfService;
import com.tigerjoys.shark.miai.service.IRegLoginService;
import com.tigerjoys.shark.miai.service.IUserService;
import com.tigerjoys.shark.miai.utils.DES;
import com.tigerjoys.shark.miai.utils.FileUploadResult;
import com.tigerjoys.shark.miai.utils.Helper;
import com.tigerjoys.shark.miai.utils.ServiceHelper;
import com.tigerjoys.shark.miai.utils.SignUtil;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 注册登录服务类
 * @author lipeng
 *
 */
@Service
public class RegLoginServiceImpl implements IRegLoginService {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private IUserAgent userAgent;
	
	@Autowired
	private IUserContract userContract;
	
	@Autowired
	private IUserGeoAgent userGeoAgent;
	
	@Autowired
	private ITencentIMAgent tencentIMAgent;
	
	@Autowired
	private IUserDiamondAgent userDiamondAgent;
	
	//第三方登录服务
	@Autowired
	private IValidCodeService validCodeService;
	
	@Autowired
	private IAppAreaService appAreaService;
	
	@Autowired
	private IShareVipCardContract  shareVipCardContract;
	
	@Autowired
	private INeteaseAgent neteaseAgent;
	
	@Autowired
	private IUserOnlineListAgent userOnlineListAgent;
	
	@Autowired
	private IAnchorOnlineContract anchorOnlineContract;
	
	@Autowired
	private ISendMessageAgent sendMessageAgent;
	
	@Autowired
	private IUserLoginLogContract userLoginLogContract;
	
	@Autowired
	private IUserRegLogContract userRegLogContract;
	
	@Autowired
	private IRootUserRegLogContract rootUserRegLogContract;
	
	@Autowired
	private IAppVersionContract appVersionContract;
	
	@Autowired
	private IUserOrdinaryOnlineListAgent userOrdinaryOnlineListAgent;
	
	@Autowired
	IAppUserAllowanceAgent  appUserAllowanceAgent;
	
	@Autowired
	private ICouponAgent couponAgent;
	
	@Autowired
	private IConfService confService;
	
	@Autowired
	private IUserPrivacySecurityContract userPrivacySecurityContract;
	
	@Autowired
	private ISysConfigAgent sysConfigAgent;
	
	@Autowired
	private IGlobalBroadcastAgent globalBroadcastAgent;
	
	@Autowired
	private IUserWakeUpMessageAgent userWakeUpMessageAgent;
	
	@Autowired
	private IUserIncomeAgent userIncomeAgent;
	
	@Autowired
	private IDeviceContract deviceContract;
	
	@Autowired
	private ICopyUserContract copyUserContract;
	
	@Autowired
	private IPayUserRecordContract payUserRecordContract;
	
	@Autowired
	private IAppChannelContract appChannelContract;
	
	@Autowired
	private IAppPackageContract appPackageContract;
	
	@Autowired
	private IUserService userService;//注册登录服务
	
	@Autowired
	@Qualifier(RedisCacheConst.REDIS_PUBLIC_CACHE)
	private CacheRedis cacheRedis;

	@Autowired
	private IRedFlowerAgent redFlowerAgent;
	
	@Autowired
	private IChannelCheckService channelCheckService;
	
	@Autowired
	private IUserForegroundAgent userForegroundAgent;
	
	@Autowired
	private IEsMobileUserAppRecordService esMobileUserAppRecordService;
	
	@Autowired
	private IFreeVideoChatExperienceAgent freeVideoChatExperienceAgent;

	@Autowired
	private IUserLogoutLogContract  userLogoutLogContract;
	
	@Autowired
	private IThirdPartySpreadContract  thirdPartySpreadContract;
	
	@Autowired
	private IIOSUserSmsAgent iOSUserSmsAgent;
	
	private final Random random = new Random();
	
	@Override
	public RegLoginDto login(ThirdRegistDto dto) throws Exception {
		logger.info("注册数据"+dto.toString());
		//此处验证一下openid是否注册过。如果注册过就直接登录即可
		boolean ifReg = false;
		UserBO user = null;
		if (!dto.getIfRegLogin()) {
			user = userAgent.findByUsername(dto.getOpenid());
			//第三方登录可以用手机号登录
			if(user == null && Tools.isMobile(dto.getOpenid())) user = userAgent.findByMobile(dto.getOpenid());
		}
		RequestHeader requestHeader = RequestUtils.getCurrent().getHeader();
		if(user == null) {
			ifReg = true;
			UserCreatDto creat = new UserCreatDto();
			creat.setPhoto(dto.getPhoto());
			if (Tools.isNotNull(dto.getBirthday())) {
				creat.setBirthday(Tools.getDate(dto.getBirthday()));
			} else {
				creat.setBirthday(Tools.getDate("1990-01-01"));
			}
			creat.setFr(dto.getFr());
			creat.setOpenid(dto.getOpenid());
			if (Tools.isNotNull(dto.getNickname())) {
				creat.setNickname(dto.getNickname().length()>10?dto.getNickname().substring(0,10):dto.getNickname());
			}
			
			//是否有city变量
			AreaDto cityDto = null;
			if (dto.getCityCode()!=null) {
				AreaDto[] areas = appAreaService.getAreasByBaiduCode(dto.getCityCode());
				creat.setCountryid(areas[0]!=null?areas[0].getId():0L);
				creat.setProvinceid(areas[1]!=null?areas[1].getId():0L);
				creat.setCityid(areas[2]!=null?areas[2].getId():0L);
				cityDto = areas[2];
			}else if(dto.getCityName()!=null){
				AreaDto cityArea = appAreaService.getCityByName(dto.getCityName());
				if (Tools.isNotNull(cityArea)) {
					AreaDto[] areas = appAreaService.getAreas(cityArea.getId());
	 				creat.setCountryid(areas[0]!=null?areas[0].getId():0L);
					creat.setProvinceid(areas[1]!=null?areas[1].getId():0L);
					creat.setCityid(areas[2]!=null?areas[2].getId():0L);
					cityDto = areas[2];
				}
			}
			
			//如果没有设置城市变量，则根据IP地址自动获取城市信息
			if(cityDto == null) {
				String areaName = ServiceHelper.getCityNameByIP(Tools.getIP(RequestUtils.getCurrent().getRequest()));
				if(Tools.isNotNull(areaName)) {
					AreaDto cityArea = appAreaService.getCityByName(areaName);
					if(cityArea != null) {
						AreaDto[] areas = appAreaService.getAreas(cityArea.getId());
						creat.setCountryid(areas[0]!=null?areas[0].getId():0L);
						creat.setProvinceid(areas[1]!=null?areas[1].getId():0L);
						creat.setCityid(areas[2]!=null?areas[2].getId():0L);
						cityDto = areas[2];
					}
				}
			}
			//本地库没有获得城市信息在通过ip138 获得城市信息
			if(cityDto == null) {
				String[] areaNameArray = ServiceHelper.get138ArrayByIP(Tools.getIP(RequestUtils.getCurrent().getRequest()));
				if (Tools.isNotNull(areaNameArray)) {
					if(Tools.isNotNull(areaNameArray[2])) {
						AreaDto cityArea = appAreaService.getCityByName(areaNameArray[2]);
						if(cityArea != null) {
							AreaDto[] areas = appAreaService.getAreas(cityArea.getId());
							creat.setCountryid(areas[0]!=null?areas[0].getId():0L);
							creat.setProvinceid(areas[1]!=null?areas[1].getId():0L);
							creat.setCityid(areas[2]!=null?areas[2].getId():0L);
							cityDto = areas[2];
						}
					}else if(Tools.isNotNull(areaNameArray[1])){
						AreaDto provinceArea = appAreaService.getProvinceByName(areaNameArray[1]);
						if(provinceArea != null) {
							AreaDto[] areas = appAreaService.getAreas(provinceArea.getId());
							creat.setCountryid(areas[0]!=null?areas[0].getId():0L);
							creat.setProvinceid(areas[1]!=null?areas[1].getId():0L);
							creat.setCityid(areas[2]!=null?areas[2].getId():0L);
							cityDto = areas[2];
						}
					}
				}
			}
			creat.setSex(dto.getSex());
			if (dto.getSex()==0) {
				creat.setSex(1);
			}
			
			//设置推送方式
			creat.setPushchannel(dto.getPushchannel());
			creat.setHuaweiToken(requestHeader.getHuaweiToken());
			
			creat.setSignature(dto.getSignature());
			creat.setClientid(requestHeader.getClientId());
			creat.setPlatform(requestHeader.getOs_type());
			creat.setAppVersion(requestHeader.getVersion());
			creat.setVersionCode(requestHeader.getVersioncode());
			creat.setPackageName(requestHeader.getPackageName());//包名   
			creat.setChannel(requestHeader.getChannel());
			logger.info("注册手机类型="+requestHeader.getOs_type());
			//如果客户端传递过来的经纬度为空并且能够匹配到城市信息，则随机一个当前城市中10KM内的坐标点给用户
			if((Tools.doubleValue(dto.getLng()) == 0D || Tools.doubleValue(dto.getLat()) == 0D) && (cityDto != null && Tools.doubleValue(cityDto.getLng()) != 0D && Tools.doubleValue(cityDto.getLat()) != 0D)) {
				double[] randomZuoBiao = DistanceUtils.getRandomDistance(cityDto.getLng(), cityDto.getLat(), 10000);
				
				creat.setLng(randomZuoBiao[0]);
				creat.setLat(randomZuoBiao[1]);
			} else {
				creat.setLng(dto.getLng());
				creat.setLat(dto.getLat());
			}
			
			JSONObject json = JsonHelper.toJsonObject(dto.getInviteCode());
			creat.setInviteCode(json.getString("inviteKey"));
			
			/*creat.setSexOpinion(dto.getSexOpinion());
			creat.setSpouseOpinion(dto.getSpouseOpinion());
			creat.setMakeFriend(dto.getMakeFriend());*/
			creat.setSexOpinion(1);
			creat.setSpouseOpinion(1);
			creat.setMakeFriend(1);
			final AreaDto city = cityDto;
			//匿名
			user = userAgent.createUser(creat,userEntity->{
				//将刚刚注册的用户的注册时间加入到redis的zset中
				cacheRedis.zadd(CommonConst.registerUser, userEntity.getCreate_time().getTime(), String.valueOf(userEntity.getId()));
				
				//新用户注册送钻
				PageModel pageModel = new PageModel();
				pageModel.addQuery(Restrictions.eq("client_id", requestHeader.getClientId()));
				List<UserLoginLogEntity> loginLog = userLoginLogContract.load(pageModel);
				if(Tools.isNull(loginLog)){
					boolean send = channelCheckService.checkSendFlower();
					if(send)
						redFlowerAgent.increaseFlowerAccount(userEntity.getId(), 3, RedFlowerAccountLogTypeEnum.register_user, userEntity.getId()+"register_user");
				}
				String cityCode = requestHeader.getCityCode();
				boolean check = channelCheckService.checkChannel();
				//特殊处理北京地区的不送体验机会
				if (Tools.isNull(loginLog) && !("131".equals(cityCode))) {
					
					//女性用户不进行赠送体验机会
					if(Tools.isNotNull(dto.getSex()) && dto.getSex() == 2) {
						check = true;
					}
					if(!check) {
						int award = appUserAllowanceAgent.sendAllowance(requestHeader.getClientId(), requestHeader.getChannel(), requestHeader.getVersioncode(), userEntity.getId());
						if(award == AppUserAllowanceTypeEnum.diamond.getCode()) {
							userDiamondAgent.changeDiamondAccount(userEntity.getId(), sysConfigAgent.getIntvalue(Const.NEW_USER_AWARD), (long)0, UserDiamondAccountLogTypeEnum.reg_award.getCode(), 1, null, userEntity.getId()+"_reg_award", UserDiamondAccountLogTypeEnum.reg_award.getDesc());
						} else if(award == AppUserAllowanceTypeEnum.money.getCode()) {
							long logId = IdGenerater.generateId();
							userIncomeAgent.changeIncomeAccount(userEntity.getId(), 6 * 100, 1, UserIncomeAccountLogTypeEnum.new_user_award, String.valueOf(logId), UserIncomeAccountLogTypeEnum.new_user_award.getDesc());
						} else if(award == AppUserAllowanceTypeEnum.experienceCard.getCode()) {
							VchatRoomObscurationConfigDto configDto = sysConfigAgent.obscurationConfig();
							freeVideoChatExperienceAgent.insertRecord(userEntity.getId(), requestHeader.getChannel(),Tools.intValue(configDto.getDialHelperTime()),0);
						}
					}
				}
				if(!check) {
					//触发发送消息的业务
					ExecutorServiceHelper.executor.execute(new SendMessageThread(userEntity.getId()));
				}
				
				if (Tools.formatString(requestHeader.getChannel()).equals("Huawei_yoyo3")||Tools.formatString(requestHeader.getChannel()).equals("shenghe_wxb")) {
					CopyUserEntity copyUser= new CopyUserEntity();
					BeanUtils.copyBean(userEntity,copyUser);
					copyUserContract.insert(copyUser);
				}
				
				ThirdPartySpreadEntity thirdPartySpread = thirdPartySpreadContract.findByProperty("clientId", creat.getClientid());
				if (thirdPartySpread!=null) {
					ThirdPartySpreadEntity entity = new ThirdPartySpreadEntity();
					entity.setId(thirdPartySpread.getId());
					entity.setUpdate_time(new Date());
					entity.setUserid(userEntity.getId());
					thirdPartySpreadContract.update(entity);
				}
				
				//用户注册信息记录，2018年5月2日 18:03:31由userloginlog改为此代码，注册跟登录和退出分开，利于统计数据
				UserRegLogEntity log = new UserRegLogEntity();
				log.setCity_name(city!=null?city.getName():null);
				log.setClient_id(creat.getClientid());
				log.setCreate_time(userEntity.getCreate_time());
				log.setIp(Tools.getIP(RequestUtils.getCurrent().getRequest()));
				log.setLat(Tools.doubleValue(dto.getLat()));
				log.setLng(Tools.doubleValue(dto.getLng()));
				log.setUserid(userEntity.getId());
				log.setApptype(requestHeader.getOs_type());
				log.setAppversion(requestHeader.getVersion());
				if (thirdPartySpread!=null) {
					log.setChannel("kuaishou_cpd");
				}else{
					log.setChannel(Tools.formatString(requestHeader.getChannel()));
				}
				log.setFr(userEntity.getFr());
				log.setPackage_name(requestHeader.getPackageName());
				userRegLogContract.insert(log);
				//记录登录信息
				UserLoginLogEntity userLoginLog = new UserLoginLogEntity();
				userLoginLog.setCity_name(city!=null?city.getName():null);
				userLoginLog.setClient_id(requestHeader.getClientId());
				userLoginLog.setCreate_time(new Date());
				userLoginLog.setIp(Tools.getIP(RequestUtils.getCurrent().getRequest()));
				userLoginLog.setLat(Tools.doubleValue(dto.getLat()));
				userLoginLog.setLng(Tools.doubleValue(dto.getLng()));
				userLoginLog.setType(UserLoginLogTypeEnum.login.getCode());
				userLoginLog.setUser_id(userEntity.getId());
				userLoginLog.setApptype(requestHeader.getOs_type());
				userLoginLog.setAppversion(requestHeader.getVersion());
				userLoginLog.setChannel(Tools.formatString(requestHeader.getChannel()));
				userLoginLog.setPackage_name(requestHeader.getPackageName());
				userLoginLogContract.insert(userLoginLog);
				
				//检测是否属于对应的快手推广进行的注册用户
				/*if(Tools.isNotNull(requestHeader.getChannel()) && requestHeader.getChannel().equals("kuaishou_cpd")) {
					kuaiShouConfirmAgent.confirm(requestHeader.getClientId(), KuaiShouAdTypeEnum.user.getCode());
				}*/
			});
			
			
			if(dto.getIfRegLogin() && Tools.isNotNull(dto.getPhoto())) {
				//下载图片，存储起来
				final String photo = dto.getPhoto();
				final long userid = user.getUserid();
				ExecutorServiceHelper.executor.execute(new Runnable(){
					
					@Override
					public void run() {
						String filePath = Helper.getUploadFilePath("user")+Helper.getUploadFileName("jpg");
						
						logger.info("下载头像保存："+filePath+",下载地址："+photo+",头像保存路径:"+filePath);
						
						try {
							HttpUtils.getCallback(photo, null , null , null , entity-> {
								FileUploadResult result = Helper.uploadFile(entity.getEntity().getContent(), filePath);
								logger.info("头像下载："+(result.getCode()==0?"成功":"失败")+"--"+JsonHelper.toJson(result));
								if(result.getCode() == ErrorCodeEnum.success.getCode()) {
									//更新图片
									UserModifyDto dto = new UserModifyDto();
									dto.setUserid(userid);	
									dto.setPhoto(filePath);
									try {
										userAgent.updateUser(dto);
										//更新网易头像
										Map<String,String> params = new HashMap<>();
										params.put("accid", String.valueOf(userid));
										params.put("icon", Const.getCdn(filePath));
										JSONObject data = neteaseAgent.updateUser(params);
										if (NeteaseErrorEnum.success.getCode() != data.getIntValue("code")) {
											logger.warn("update userinfo to Netease occured error:{}",data);
										}
									} catch (Exception e) {
										logger.error(e.getMessage() , e);
									}
								}
							});
						} catch (Exception e) {
							logger.error(e.getMessage() , e);
						}
					}
				});
			}else{
				dto.setPhoto(dto.getPhoto());
				//更新网易头像
				Map<String,String> params = new HashMap<>();
				params.put("accid", String.valueOf(user.getUserid()));
				params.put("icon", Const.getCdn(dto.getPhoto()));
				JSONObject data = neteaseAgent.updateUser(params);
				if (NeteaseErrorEnum.success.getCode() != data.getIntValue("code")) {
					logger.warn("update userinfo to Netease occured error:{}",data);
				}
			}
		} else {
			Date currDate = new Date();
			String token;
			
			//手机登录的没送优惠券的送优惠券
			if (Tools.isMobile(user.getUsername())&&shareVipCardContract.queryCardStatus(user.getUsername())) {
				couponAgent.addUserCoupon(user.getUserid(), CouponGroupEnum.offline_ktv.getGroup());
				shareVipCardContract.usedCard(user.getUsername());
			}
			//查看是否需要更新信息
			UserModifyDto modify = new UserModifyDto();
			modify.setUserid(user.getUserid());
			modify.setRefresh_time(currDate);
			modify.setPlatform(requestHeader.getOs_type());
			modify.setClientid(requestHeader.getClientId());
			modify.setAppVersion(requestHeader.getVersion());
			modify.setVersionCode(requestHeader.getVersioncode());
			modify.setPackageName(requestHeader.getPackageName());
			modify.setChannel(requestHeader.getChannel());
			//如果当前时间-最后登录时间 > 一周，则重新生成token
			if(currDate.getTime() - user.getLastLoginDate().getTime() > Const.USER_TOKEN_EXPIRE_MILLIS) {
				token = MD5.encode(System.currentTimeMillis()+"#"+user.getUserid()+"#"+new Random().nextInt(10000));
				modify.setUnique_key(token);
			} else {
				token = user.getUniqueKey();
			}
			
			//设置推送方式
			modify.setPushchannel(dto.getPushchannel());
			modify.setHuaweiToken(requestHeader.getHuaweiToken());
			
			AreaDto cityDto = null;
			if (dto.getCityCode()!=null) {
				AreaDto[] areas = appAreaService.getAreasByBaiduCode(dto.getCityCode());
				modify.setCountryid(areas[0]!=null?areas[0].getId():0L);
				modify.setProvinceid(areas[1]!=null?areas[1].getId():0L);
				modify.setCityid(areas[2]!=null?areas[2].getId():0L);
				cityDto = areas[2];
			}else if(dto.getCityName()!=null){
				AreaDto cityArea = appAreaService.getCityByName(dto.getCityName());
				if (Tools.isNotNull(cityArea)) {
					AreaDto[] areas = appAreaService.getAreas(cityArea.getId());
					modify.setCountryid(areas[0]!=null?areas[0].getId():0L);
					modify.setProvinceid(areas[1]!=null?areas[1].getId():0L);
					modify.setCityid(areas[2]!=null?areas[2].getId():0L);
					cityDto = areas[2];
				}
			}
			
			//如果没有设置城市变量，则根据IP地址自动获取城市信息
			if(cityDto == null) {
				String areaName = ServiceHelper.getCityNameByIP(Tools.getIP(RequestUtils.getCurrent().getRequest()));
				if(Tools.isNotNull(areaName)) {
					AreaDto cityArea = appAreaService.getCityByName(areaName);
					if(cityArea != null) {
						AreaDto[] areas = appAreaService.getAreas(cityArea.getId());
						modify.setCountryid(areas[0]!=null?areas[0].getId():0L);
						modify.setProvinceid(areas[1]!=null?areas[1].getId():0L);
						modify.setCityid(areas[2]!=null?areas[2].getId():0L);
						cityDto = areas[2];
					}
				}
			}
			//本地库没有获得城市信息在通过ip138 获得城市信息
			if(cityDto == null) {
				String[] areaNameArray = ServiceHelper.get138ArrayByIP(Tools.getIP(RequestUtils.getCurrent().getRequest()));
				if (Tools.isNotNull(areaNameArray)) {
					if(Tools.isNotNull(areaNameArray[2])) {
						AreaDto cityArea = appAreaService.getCityByName(areaNameArray[2]);
						if(cityArea != null) {
							AreaDto[] areas = appAreaService.getAreas(cityArea.getId());
							modify.setCountryid(areas[0]!=null?areas[0].getId():0L);
							modify.setProvinceid(areas[1]!=null?areas[1].getId():0L);
							modify.setCityid(areas[2]!=null?areas[2].getId():0L);
						}
					}else if(Tools.isNotNull(areaNameArray[1])){
						AreaDto provinceArea = appAreaService.getProvinceByName(areaNameArray[1]);
						if(provinceArea != null) {
							AreaDto[] areas = appAreaService.getAreas(provinceArea.getId());
							modify.setCountryid(areas[0]!=null?areas[0].getId():0L);
							modify.setProvinceid(areas[1]!=null?areas[1].getId():0L);
							modify.setCityid(areas[2]!=null?areas[2].getId():0L);
						}
					}
				}
			}
			userAgent.updateUser(modify);
			
			String areaName = ServiceHelper.getCityNameByIP(Tools.getIP(RequestUtils.getCurrent().getRequest()));
			//用户登录信息记录
			UserLoginLogEntity log = new UserLoginLogEntity();
			log.setCity_name(areaName);
			log.setClient_id(requestHeader.getClientId());
			log.setCreate_time(new Date());
			log.setIp(Tools.getIP(RequestUtils.getCurrent().getRequest()));
			log.setLat(Tools.doubleValue(dto.getLat()));
			log.setLng(Tools.doubleValue(dto.getLng()));
			log.setType(UserLoginLogTypeEnum.login.getCode());
			log.setUser_id(user.getUserid());
			log.setApptype(requestHeader.getOs_type());
			log.setAppversion(requestHeader.getVersion());
			log.setChannel(Tools.formatString(requestHeader.getChannel()));
			log.setPackage_name(requestHeader.getPackageName());
			userLoginLogContract.insert(log);
			
			double lat = Tools.doubleValue(dto.getLat()) , lng = Tools.doubleValue(dto.getLng());
			if(lat != 0 && lng != 0) {
				//更新用户最新的经纬度信息
				userGeoAgent.modifyUserGeo(user.getUserid(), lng, lat, null, null);
			}
			
			//此处需要更新刷新一下User对象的数据,一定不能忘记了
			user.setLastLoginDate(modify.getRefresh_time());
			user.setUniqueKey(token);
			user.setPlatform(modify.getPlatform());
		}
		//查看是否是200钻以上用户 添加到全局广播
		if (!user.isWaiter() && userDiamondAgent.getDiamondDeposit(user.getUserid())>=4000) {
			globalBroadcastAgent.insert(user.getUserid(), 0, 0, GlobalBroadcastTypeEnum.online.getCode(), 0);
		};
		//将用户信息放入到在线列表中
		try {
			userOnlineListAgent.addOnlineUser(user.getUserid(), requestHeader.getClientId());
			if (!user.isWaiter()) {
				userOrdinaryOnlineListAgent.addOnlineUser(user.getUserid());
			} else {
				userForegroundAgent.removeBackgroundAnchor(user.getUserid());
			}
		} catch(Exception e) {
			logger.error(e.getMessage() , e);
		}
		
		logger.info("用户注册：" + JsonHelper.toJson(user));
		
		PageModel pageModel = new PageModel();
		pageModel.addQuery(Restrictions.eq("code", requestHeader.getVersioncode()));
		pageModel.addQuery(Restrictions.eq("platform", requestHeader.getOs_type()));
		List<AppVersionEntity> versionList = appVersionContract.load(pageModel);
		int incomeStatus = 0;
		if (Tools.isNotNull(versionList)) {
			incomeStatus = versionList.get(0).getIncome_status();
		}
		
		return getUserData(user , ifReg , dto.getPhoto() ,incomeStatus);
	}
	
	@Override
	public void loginVipUser(ThirdRegistDto dto) throws Exception {
		logger.info("注册数据"+dto.toString());
		//此处验证一下openid是否注册过。如果注册过就直接登录即可
		UserBO user = null;
		if (dto.getTime()<=0) {
			if (!dto.getIfRegLogin()) {
				user = userAgent.findByUsername(dto.getOpenid());
				//第三方登录可以用手机号登录
				if(user == null && Tools.isMobile(dto.getOpenid())) user = userAgent.findByMobile(dto.getOpenid());
			}
		}
		RequestHeader requestHeader = RequestUtils.getCurrent().getHeader();
		RootUserRegLogEntity log = new RootUserRegLogEntity();
		log.setUserid(user!=null?user.getUserid():dto.getTime());
		log.setCity_name(dto.getCityName()!=null?dto.getCityName():null);
		log.setClient_id(requestHeader.getClientId());
		log.setCreate_time(new Date());
		log.setIp(Tools.getIP(RequestUtils.getCurrent().getRequest()));
		log.setLat(Tools.doubleValue(dto.getLat()));
		log.setLng(Tools.doubleValue(dto.getLng()));
		log.setApptype(requestHeader.getOs_type());
		log.setAppversion(requestHeader.getVersion());
		log.setChannel(Tools.formatString(requestHeader.getChannel()));
		log.setFr(dto.getFr());
		log.setPackage_name(requestHeader.getPackageName());
		rootUserRegLogContract.insert(log);
		//return ActionResult.fail(ErrorCodeEnum.root_user_reg_login.getCode(),ErrorCodeEnum.root_user_reg_login.getDesc());
	}
	
	
	/**
	 * 判断用户手机是否大于1000 大于为true 小于为false
	 * @param clientId
	 * @return
	 * @throws Exception 
	 */
	public boolean ifMobileModelMoreThanOneThousand(String clientId) throws Exception {
		boolean b = true;
		/*DeviceEntity device = deviceContract.findByProperty("clientid", clientId);
		if (device!=null){
			RechargeCustomMobileEntity mobile = rechargeCustomMobileContract.findByProperty("name", device.getMobile_model());
			if(mobile!=null){
				RechargeCustomCategoryEntity category = rechargeCustomCategoryContract.findById(mobile.getCategory_id());
				if (category!=null && category.getName().equals("n≤1000")) {
					b = false;
				}
			}
		} */
		return b;
	}

	/**
	 * 获得腾讯登录状态
	 * @param user
	 * @param ifReg
	 * @param photo
	 * @return
	 */
	public int getTencentLoginCode(UserBO user, boolean ifReg, String photo){
		Map<String, String> contentType = new HashMap<>();
		contentType.put("Identifier", "TcId"+user.getUserid());
		contentType.put("Nick", user.getNickname());
		if (ifReg) {
			contentType.put("FaceUrl", photo);
		}else{
			contentType.put("FaceUrl", Const.getCdn(user.getPhoto()));
		}
		int code = 0;
		try {
		JSONObject json = tencentIMAgent.createUser(JsonHelper.toJson(contentType));
		code = json.getIntValue("ErrorCode");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return code;
	}
	
	/**
	 * 登录成功后返回的数据
	 * 
	 * @param user - UserBO
	 * @param isReg - 是否是注册
	 * @param photo 
	 * @param incomeStatus 
	 * @param tokenPair - token信息
	 * @return Map<String,Object>
	 * @throws Exception
	 */
	private RegLoginDto getUserData(UserBO user, boolean ifReg, String photo, int incomeStatus) throws Exception {
		RegLoginDto rld = new RegLoginDto();
		rld.setUserid(user.getUserid());
		rld.setIdcard(user.getIdcard()+"");
		if (ifReg) {
			rld.setPhoto(photo);
			rld.setIfReg(ifReg);
		}else{
			rld.setPhoto(ServiceHelper.getUserPhoto(user.getPhoto()));
			rld.setIfReg(ifReg);
		}
		rld.setNeedToProfile(0);
		rld.setOpenid(user.getUsername());
		rld.setIfVip(user.vipValue()>0);
		//vip状态   0不是；1是；2已过期
		if (user.getVipExpire()!=null) {
			if (user.vipValue()>=0) {
				rld.setVipStatus(1);
			}else{
				rld.setVipStatus(2);
			}
		}else{
			rld.setVipStatus(0);
		}
		if (user.getFr()==4&&(RequestUtils.getCurrent().getHeader().getPackageName().equals("com.zdkj.lttool")||RequestUtils.getCurrent().getHeader().getPackageName().equals("com.tjhj.miyou"))) {
			rld.setNickname("未登录");
		}else{
			rld.setNickname(user.getNickname());
		}
		//rld.setNickname(user.getNickname());
		rld.setUsername(user.getUsername());
		if (Tools.isNull(user.getMobile())) {
			PayUserRecordEntity payUserRecord = payUserRecordContract.findByProperty("userid", user.getUserid());
			if (payUserRecord != null || RequestUtils.getCurrent().getHeader().getPackageName().equals("com.tjhj.miliao")) {
				rld.setMobile("1");
			}else{
				rld.setMobile("");
			}
		}else{
			rld.setMobile(user.getMobile());
		}
		rld.setSex(user.getSex());
		rld.setImId(String.valueOf(user.getUserid()));
		rld.setImPass(user.getImToken());
		rld.setBirthday(Tools.getDate(user.getBirthday()));
		if (user.getCityid() != 0 ) {
			AreaDto area = appAreaService.getById(user.getCityid());
			if(area != null) {
				rld.setCityName(area.getName());
			}
		}
		rld.setSignature(user.getSignature());
		rld.setToken(user.getUniqueKey());
		
		//if(Const.IOS_TEST_MOBILE_ACCOUNT_MAP.containsKey(user.getUsername())) {
		if(confService.testIOS() || iOSUserSmsAgent.getUserSmsList().containsKey(user.getUsername())) {
			rld.setPrivacy(1);	
		}else{
			UserPrivacySecurityEntity ups = userPrivacySecurityContract.findByProperty("userid", user.getUserid());
			if (ups!=null) {
				rld.setPrivacy(ups.getTalent_status());
			}else{
				rld.setPrivacy(0);	
			}
		}

		rld.setIncomeStatus(incomeStatus);
		AnchorOnlineEntity aoEntity =  anchorOnlineContract.findByProperty("userid", user.getUserid());
		rld.setUserType(aoEntity==null?0:1);
		rld.setAnchorTypeVideo(aoEntity==null?0:aoEntity.getVideo_type());
		rld.setAnchorTypeAudio(aoEntity==null?0:aoEntity.getAudio_type());
		DynamicUserDataDto dynamicData = new DynamicUserDataDto();
		dynamicData.setIsVip(user.getVip()>0?1:0);
		channelCheckService.checkChannelReport();		
		dynamicData.setShowWeb(channelCheckService.checkChannelReport()?0:1);
		rld.setDynamicData(dynamicData);
		return rld;
	}
	
	@Override
	public RegLoginDto autoLogin(String uniqueKey,Integer pushchannel) throws Exception {
		UserBO user = userAgent.findByUniqueKey(uniqueKey);
		if (Tools.isNotNull(user)) {
			//将用户信息放入到在线列表中
			try {
				userOnlineListAgent.addOnlineUser(user.getUserid(), RequestUtils.getCurrent().getHeader().getClientId());
			} catch(Exception e) {
				logger.error(e.getMessage() , e);
			}
			Date currDate = new Date();
			String token;
			RequestHeader requestHeader = RequestUtils.getCurrent().getHeader();
			//查看是否是200钻以上用户 添加到全局广播
			if (!user.isWaiter() && userDiamondAgent.getDiamondDeposit(user.getUserid())>=4000) {
				globalBroadcastAgent.insert(user.getUserid(), 0, 0, GlobalBroadcastTypeEnum.online.getCode(), 0);
			};
			//查看是否需要更新信息
			UserModifyDto modify = new UserModifyDto();
			try {
				List<String> appList = esMobileUserAppRecordService.queryLastLoginAppList(user.getUserid());
				if (Tools.isNotNull(appList)) {
					boolean q = false;
					boolean l = false;
					if (Tools.isNotNull(user.getLabels())) {
						if (!user.getLabels().contains("Q")) {
							if (appList.contains("cn.xuexi.android")) {
								q = true;
							}
						}
						if (!user.getLabels().contains("L")) {
							apppakageList.retainAll(appList);
							if (apppakageList.size()>=3) {
								l = true;
							}
						}
						if (q||l) {
							modify.setLabels(user.getLabels()+(q?"Q":"")+(l?"L":""));
						}
					}else{
						if (appList.contains("cn.xuexi.android")) {
							q = true;
						}
						apppakageList.retainAll(appList);
						if (apppakageList.size()>=3) {
							l = true;
						}
						if (q||l) {
							modify.setLabels((q?"Q":"")+(l?"L":""));
						}
					}
				}
			} catch (Exception e2) {
				logger.error(e2.getMessage() , e2);
			}
			modify.setUserid(user.getUserid());
			modify.setRefresh_time(currDate);
			modify.setClientid(requestHeader.getClientId());
			modify.setAppVersion(requestHeader.getVersion());
			modify.setVersionCode(requestHeader.getVersioncode());
			modify.setPlatform(requestHeader.getOs_type());
			modify.setPackageName(requestHeader.getPackageName());
			modify.setChannel(requestHeader.getChannel());
			//如果当前时间-最后登录时间 > 一周，则重新生成token
			if(currDate.getTime() - user.getLastLoginDate().getTime() > Const.USER_TOKEN_EXPIRE_MILLIS) {
				token = MD5.encode(System.currentTimeMillis()+"#"+user.getUserid()+"#"+new Random().nextInt(10000));
				modify.setUnique_key(token);
			} else {
				token = user.getUniqueKey();
			}
			//设置推送渠道
			modify.setPushchannel(pushchannel);
			modify.setHuaweiToken(requestHeader.getHuaweiToken());
			
			AreaDto cityDto = null;
			if (RequestUtils.getCurrent().getHeader().getCityCode()!=null) {
				AreaDto[] areas = appAreaService.getAreasByBaiduCode(Tools.parseInt(RequestUtils.getCurrent().getHeader().getCityCode()));
				modify.setCountryid(areas[0]!=null?areas[0].getId():0L);
				modify.setProvinceid(areas[1]!=null?areas[1].getId():0L);
				modify.setCityid(areas[2]!=null?areas[2].getId():0L);
				cityDto = areas[2];
			}else if(RequestUtils.getCurrent().getHeader().getCityName()!=null){
				AreaDto cityArea = appAreaService.getCityByName(RequestUtils.getCurrent().getHeader().getCityName());
				if (Tools.isNotNull(cityArea)) {
					AreaDto[] areas = appAreaService.getAreas(cityArea.getId());
					modify.setCountryid(areas[0]!=null?areas[0].getId():0L);
					modify.setProvinceid(areas[1]!=null?areas[1].getId():0L);
					modify.setCityid(areas[2]!=null?areas[2].getId():0L);
					cityDto = areas[2];
				}
			}
			
			
			
			if(cityDto == null) {
				String areaName = ServiceHelper.getCityNameByIP(Tools.getIP(RequestUtils.getCurrent().getRequest()));
				if(Tools.isNotNull(areaName)) {
					AreaDto cityArea = appAreaService.getCityByName(areaName);
					if(cityArea != null) {
						AreaDto[] areas = appAreaService.getAreas(cityArea.getId());
						modify.setCountryid(areas[0]!=null?areas[0].getId():0L);
						modify.setProvinceid(areas[1]!=null?areas[1].getId():0L);
						modify.setCityid(areas[2]!=null?areas[2].getId():0L);
						cityDto = areas[2];
					}
				}
			}
			//本地库没有获得城市信息在通过ip138 获得城市信息
			if(cityDto == null) {
				String[] areaNameArray = ServiceHelper.get138ArrayByIP(Tools.getIP(RequestUtils.getCurrent().getRequest()));
				if (Tools.isNotNull(areaNameArray)) {
					if(Tools.isNotNull(areaNameArray[2])) {
						AreaDto cityArea = appAreaService.getCityByName(areaNameArray[2]);
						if(cityArea != null) {
							AreaDto[] areas = appAreaService.getAreas(cityArea.getId());
							modify.setCountryid(areas[0]!=null?areas[0].getId():0L);
							modify.setProvinceid(areas[1]!=null?areas[1].getId():0L);
							modify.setCityid(areas[2]!=null?areas[2].getId():0L);
						}
					}else if(Tools.isNotNull(areaNameArray[1])){
						AreaDto provinceArea = appAreaService.getProvinceByName(areaNameArray[1]);
						if(provinceArea != null) {
							AreaDto[] areas = appAreaService.getAreas(provinceArea.getId());
							modify.setCountryid(areas[0]!=null?areas[0].getId():0L);
							modify.setProvinceid(areas[1]!=null?areas[1].getId():0L);
							modify.setCityid(areas[2]!=null?areas[2].getId():0L);
						}
					}
				}
			}
			userAgent.updateUser(modify);
			
			//用户登录信息记录
			UserLoginLogEntity log = new UserLoginLogEntity();
			if (cityDto!=null) {
				log.setCity_name(cityDto.getName());
				log.setLat(cityDto.getLat());
				log.setLng(cityDto.getLng());
			}else{
				log.setLat(0.0);
				log.setLng(0.0);
			}
			log.setClient_id(requestHeader.getClientId());
			log.setCreate_time(new Date());
			log.setIp(Tools.getIP(RequestUtils.getCurrent().getRequest()));
			log.setType(UserLoginLogTypeEnum.login.getCode());
			log.setUser_id(user.getUserid());
			log.setApptype(requestHeader.getOs_type());
			log.setAppversion(requestHeader.getVersion());
			log.setChannel(Tools.formatString(requestHeader.getChannel()));
			log.setPackage_name(requestHeader.getPackageName());
			userLoginLogContract.insert(log);
			
			//此处需要更新刷新一下User对象的数据,一定不能忘记了
			user.setLastLoginDate(modify.getRefresh_time());
			user.setUniqueKey(token);
			user.setPlatform(modify.getPlatform());
			
			PageModel pageModel = new PageModel();
			pageModel.addQuery(Restrictions.eq("code", requestHeader.getVersioncode()));
			pageModel.addQuery(Restrictions.eq("platform", requestHeader.getOs_type()));
			List<AppVersionEntity> versionList = appVersionContract.load(pageModel);
			int incomeStatus = 0;
			if (Tools.isNotNull(versionList)) {
				incomeStatus = versionList.get(0).getIncome_status();
			}
			
			if (!user.isWaiter()) {
				userOrdinaryOnlineListAgent.addOnlineUser(user.getUserid());
			}
			
			//用户在自动登录过程中触发的通知栏消息
			userWakeUpMessageAgent.wakeUpMessage(user.getUserid());
			return getUserData(user , false , "", incomeStatus);
		}else{
			return null;
		}
	}
	
	@Override
	public ActionResult loginout(HttpServletRequest request) throws Exception {
		RequestHeader header = RequestUtils.getCurrent().getHeader();
		
		long userid = RequestUtils.getCurrent().getUserid();
		UserLoginBO loginbo = new UserLoginBO();
		loginbo.setClient_id(header.getClientId());
		loginbo.setLng(0.0);
		loginbo.setLat(0.0);
		loginbo.setIp(Tools.getIP(request));
		loginbo.setCity_name(header.getCityName());
		loginbo.setApptype(header.getOs_type());
		loginbo.setAppversion(header.getVersion());
		loginbo.setChannel(header.getChannel());
		loginbo.setPackage_name(header.getPackageName());
		userAgent.loginOut(userid, loginbo);
		UserBO user = userAgent.findById(userid);
		if(Tools.isNotNull(user) && user.isWaiter()) {
			//进制主播线下处理
			AnchorOnlineEntity anchor = anchorOnlineContract.findByProperty("userid", userid);
			if(Tools.isNotNull(anchor)) {
				anchor.setOnline(0);
				anchor.setUpdate_time(new Date());
				anchorOnlineContract.update(anchor);
				//清除对应的在线缓存  暂时不进行清除处理
			}
		}
		return ActionResult.success();
	}
	
	@Override
	public ActionResult regSendCode(String mobile) throws Exception {
		return sendMessageAgent.sendMobileValidCode(mobile, SendSmsTypeEnum.regist);
	}


	@Override
	public ActionResult mobileLogin(JSONObject json) throws Exception {
		String mobile  = json.getString("mobile");
		String validCode  = json.getString("validCode");
		String inviteCode  = json.getString("inviteCode");
		String lng = json.getString("lng");
		String lat = json.getString("lat");
		String cityName = json.getString("cityName");
		Integer cityCode = json.getInteger("cityCode");
		Integer vip = json.getIntValue("vip");
		Long time = json.getLongValue("time");
		Integer pushchannel = json.getInteger("pushchannel");
		
		if (mobile.equals("15873952675") && RequestUtils.getCurrent().getHeader().getOs_type()==2 && !RequestUtils.getCurrent().getHeader().getChannel().equals("App Store")) {
			return ActionResult.fail(9999,"账号不存在，无法登陆！");
		}
		if (mobile.equals("18811315514") && RequestUtils.getCurrent().getHeader().getOs_type()==2 && !RequestUtils.getCurrent().getHeader().getChannel().equals("ios_miyou")) {
			return ActionResult.fail(9999,"账号不存在，无法登陆！");
		}
		if (mobile.equals("13688939043") && RequestUtils.getCurrent().getHeader().getOs_type()==2 && !RequestUtils.getCurrent().getHeader().getChannel().equals("ios_duidui")) {
			return ActionResult.fail(9999,"账号不存在，无法登陆！");
		}
		
		//兼容IOS审核 by chengang 2017年11月8日 10:32:48
		String accountValidCode = iOSUserSmsAgent.getUserSmsList().get(mobile);
		if(accountValidCode != null) {
			if(!accountValidCode.equals(validCode)) {
				return ActionResult.fail(AgentErrorCodeEnum.valid_code_error);
			}
		} else if(Const.robotPhone.contains(mobile)) {
			if(!Const.ROBOT_PHONE_CODE.equals(validCode)) {
				return ActionResult.fail(AgentErrorCodeEnum.valid_code_error);
			}
		} else {
			if (Tools.isNull(validCodeService.getValidCode(mobile))) {
				return ActionResult.fail(AgentErrorCodeEnum.valid_code_pass);
			}
			if (!sendMessageAgent.checkCode(mobile, validCode)) {
				return ActionResult.fail(AgentErrorCodeEnum.valid_code_error);
			}
		}
		
		ThirdRegistDto dto = new ThirdRegistDto();
		dto.setOpenid(mobile);
		dto.setLng(Tools.parseDouble(lng));
		dto.setLat(Tools.parseDouble(lat));
		dto.setInviteCode(inviteCode);
		dto.setCityName(cityName);
		dto.setCityCode(cityCode);
		dto.setFr(UserFrEnum.mobile.getCode());
		dto.setSex(1);
		dto.setVip(vip);
		dto.setTime(time);
		UserBO user = userAgent.findByUsername(mobile);
		//第三方登录可以用手机号登录
		if(user == null && Tools.isMobile(mobile)) user = userAgent.findByMobile(mobile);
		//注销账号处理方法
		if (user!=null&&user.getStatus()==-1) {
			UserLogoutLogEntity logoutLog =  userLogoutLogContract.findById(user.getUserid());
			if (logoutLog!=null) {
				long day = (System.currentTimeMillis()-Tools.getDayTime(logoutLog.getCreate_time()))/Tools.DAY_MILLIS;
				if (day<=180) {
					return ActionResult.fail(ErrorCodeEnum.user_already_logout.getCode() , ErrorCodeEnum.user_already_logout.getDesc());
				}else{
					Map<String,Object> updateStatement = new HashMap<>();
					updateStatement.put("mobile", null);
					userContract.updateById(updateStatement, user.getUserid());
					UserModifyDto modify = new UserModifyDto();
					modify.setUserid(user.getUserid());
					modify.setUserName(user.getUsername()+"-"+random.nextInt(9999));
					userAgent.updateUser(modify);
					user = null;
				}
			}
		}
		if (user!=null && user.isWaiter()) {
			if (RequestUtils.getCurrent().getHeader().getOs_type()==1 &&!RequestUtils.getCurrent().getHeader().getPackageName().equals("com.tjhj.dvzs")) {
				return ActionResult.fail(ErrorCodeEnum.only_user_can_login.getCode() , ErrorCodeEnum.only_user_can_login.getDesc());
			}
			if (RequestUtils.getCurrent().getHeader().getOs_type()==2 && !RequestUtils.getCurrent().getHeader().getPackageName().equals("com.zdkj.lttool")) {
				return ActionResult.fail(ErrorCodeEnum.only_user_can_login.getCode() , ErrorCodeEnum.only_user_can_login.getDesc());
			}
		}
		if (RequestUtils.getCurrent().getHeader().getPackageName().equals("com.tjhj.dvzs") || RequestUtils.getCurrent().getHeader().getPackageName().equals("com.zdkj.lttool")) {
			if (user==null||!user.isWaiter()) {
				return ActionResult.fail(ErrorCodeEnum.only_V_can_login.getCode() , ErrorCodeEnum.only_V_can_login.getDesc());
			}
		}
		if (user == null) {
			dto.setIfRegLogin(true);
		}else{
			dto.setIfRegLogin(false);
		}
		if (dto.getVip()==1) {
			loginVipUser(dto);
		}
		dto.setPushchannel(pushchannel);
		return ActionResult.success(login(dto));
	}

	@Override
	public RegLoginDto reg(ThirdRegistDto dto) throws Exception {
		RegLoginDto rld = new RegLoginDto();
		rld.setOpenid(dto.getOpenid());
		rld.setNeedToProfile(1);
		rld.setFr(dto.getFr());
		rld.setLat(dto.getLat());
		rld.setLng(dto.getLng());
		return rld;
	}

	@Override
	public ActionResult regLogin(JSONObject json , MultipartFile pictures) throws Exception {
		String nickname = json.getString("nickname");
		String openid = json.getString("openid");
		String birthday = json.getString("birthday");
		Integer fr = json.getInteger("fr");
		Integer sex = json.getInteger("sex");
		Double lat = json.getDouble("lat");
		Double lng = json.getDouble("lng");
		
		Integer sexOpinion = json.getInteger("sexOpinion");
		Integer spouseOpinion = json.getInteger("spouseOpinion");
		Integer makeFriend = json.getInteger("makeFriend");
		
		String photoPath = null;
		
		if (!userService.nickNameValid(nickname)) {
			return ActionResult.fail(ErrorCodeEnum.user_nickname_no_rule.getCode(),ErrorCodeEnum.user_nickname_no_rule.getDesc());
		}
		if (userAgent.findByNickname(nickname)!=null) {
			return ActionResult.fail(ErrorCodeEnum.user_nickname_exist.getCode(),ErrorCodeEnum.user_nickname_exist.getDesc());
		}
		if (pictures == null) {
			return ActionResult.fail(ErrorCodeEnum.photo_empty_error.getCode(), "请上传头像");
		}
		if (Tools.getAge(Tools.getDate(birthday))<18) {//年龄不能小于18岁
			return ActionResult.fail(ErrorCodeEnum.age_less_than_18.getCode(), ErrorCodeEnum.age_less_than_18.getDesc());
		}
		
		
		FileUploadResult result = Helper.uploadPicture(pictures, "user");
		if (result.getCode() == 0) {
			photoPath = result.getFilePath();
		}
		
		ThirdRegistDto dto = new ThirdRegistDto();
		dto.setOpenid(openid);
		dto.setPhoto(photoPath);
		dto.setSex(sex);
		dto.setBirthday(birthday);
		dto.setNickname(nickname);
		dto.setLng(lng);
		dto.setLat(lat);
		dto.setFr(fr);
		dto.setIfRegLogin(true);
		
		dto.setSexOpinion(sexOpinion);
		dto.setSpouseOpinion(spouseOpinion);
		dto.setMakeFriend(makeFriend);
		return ActionResult.success(login(dto));
	}

	@Override
	public ActionResult getLoginWay() throws Exception {
		// 根据渠道获取渠道ID
		AppChannelEntity channel = appChannelContract.findByProperty("name", RequestUtils.getCurrent().getHeader().getChannel());
		// 根据渠道获取渠道ID
		AppPackageEntity appPackage = appPackageContract.findByProperty("name", RequestUtils.getCurrent().getHeader().getPackageName());
		
		AppChannelEntity allChannel = appChannelContract.findByProperty("simple_name", Const.ALL_CAHNNEL);
		if (channel == null) {
			return ActionResult.fail(ErrorCodeEnum.no_find_channel.getCode(), ErrorCodeEnum.no_find_channel.getDesc());
		}
		
		if (appPackage == null) {
			return ActionResult.fail(ErrorCodeEnum.no_find_package.getCode(), ErrorCodeEnum.no_find_package.getDesc());
		}
		Map<String, Integer> data = new HashMap<>();
		if (RequestUtils.getCurrent().getHeader().getPackageName().equals("com.tjhj.miliao")) {
			data.put("touristShow", 0);
			data.put("thirdShow", 1);
			return ActionResult.success(data);
		}
		if (RequestUtils.getCurrent().getHeader().getPackageName().equals("com.tjhj.dvzs")) {
			data.put("touristShow", 0);
			data.put("thirdShow", 1);
			return ActionResult.success(data);
		}
		PageModel pageModel = PageModel.getPageModel(1, 1);
		pageModel.addQuery(Restrictions.eq("channel_id", channel.getId()));
		pageModel.addQuery(Restrictions.eq("package_id", appPackage.getId()));
		pageModel.addQuery(Restrictions.eq("code", RequestUtils.getCurrent().getHeader().getVersioncode()));
		pageModel.addQuery(Restrictions.eq("platform", RequestUtils.getCurrent().getHeader().getOs_type()));
		//pageModel.addQuery(Restrictions.eq("status", 1));
		List<AppVersionEntity> list = appVersionContract.load(pageModel);
		int touristShow = 0;
		int thirdShow = 0;
		if (Tools.isNotNull(list)) {
			AppVersionEntity version = list.get(0);
			touristShow = version.getTourist_show();
			thirdShow = version.getThird_show();
		}else{
			pageModel.clearAll();
			pageModel.addQuery(Restrictions.eq("channel_id", allChannel.getId()));
			pageModel.addQuery(Restrictions.eq("package_id", appPackage.getId()));
			pageModel.addQuery(Restrictions.eq("code", RequestUtils.getCurrent().getHeader().getVersioncode()));
			pageModel.addQuery(Restrictions.eq("platform", RequestUtils.getCurrent().getHeader().getOs_type()));
			//pageModel.addQuery(Restrictions.eq("status", 1));
			List<AppVersionEntity> alllist = appVersionContract.load(pageModel);
			if (Tools.isNotNull(alllist)) {
				AppVersionEntity version = alllist.get(0);
				touristShow = version.getTourist_show();
				thirdShow = version.getThird_show();
			}
		}
		
		DeviceEntity device = deviceContract.findByProperty("clientid", RequestUtils.getCurrent().getHeader().getClientId());
		if (device!=null){
			if (device.getCreate_time().getTime()>Tools.getDateTime("2019-08-27 16:30:00").getTime()) {
				String areaName = ServiceHelper.getProvinceNameByIP(Tools.getIP(RequestUtils.getCurrent().getRequest()));
				if(Tools.isNotNull(areaName)) {
					if (areaName.equals("天津")) {
						thirdShow = 0;
					}
				}else{
					String[] areaNameArray = ServiceHelper.get138ArrayByIP(Tools.getIP(RequestUtils.getCurrent().getRequest()));
					if (Tools.isNotNull(areaNameArray)){
						if(Tools.isNotNull(areaNameArray[1])){
							if (areaNameArray[1].equals("天津")) {
								thirdShow = 0;
							}
						}
					}
				}
			}
		}
		data.put("touristShow", touristShow);
		data.put("thirdShow", thirdShow);
		return ActionResult.success(data);
	}
	
	@Override
	public ActionResult userLogout(HttpServletRequest request) throws Exception {
		RequestHeader header = RequestUtils.getCurrent().getHeader();
		long userid = RequestUtils.getCurrent().getUserid();
		UserLoginBO loginbo = new UserLoginBO();
		loginbo.setClient_id(header.getClientId());
		loginbo.setLng(0.0);
		loginbo.setLat(0.0);
		loginbo.setIp(Tools.getIP(request));
		loginbo.setCity_name(header.getCityName());
		loginbo.setApptype(header.getOs_type());
		loginbo.setAppversion(header.getVersion());
		loginbo.setChannel(header.getChannel());
		loginbo.setPackage_name(header.getPackageName());
		userAgent.loginOut(userid, loginbo);
		UserBO user = userAgent.findById(userid);
		if (user!=null) {
			UserLogoutLogEntity  log = new UserLogoutLogEntity();
			log.setUserid(userid);
			log.setNickname(user.getNickname());
			log.setStatus(0);
			log.setCreate_time(new Date());
			userLogoutLogContract.insert(log);
			UserModifyDto modify = new UserModifyDto();
			modify.setUserid(user.getUserid());
			modify.setStatus(-1);
			userAgent.updateUser(modify);
			//进制主播线下处理
			AnchorOnlineEntity anchor = anchorOnlineContract.findByProperty("userid", userid);
			if(Tools.isNotNull(anchor)) {
				anchor.setOnline(0);
				anchor.setUpdate_time(new Date());
				anchorOnlineContract.update(anchor);
				//清除对应的在线缓存  暂时不进行清除处理
			}
		}
		return ActionResult.success("注销提交成功");
	}
	
	
	@Override
	public ActionResult oneKeyLogin(JSONObject json) throws Exception {
		String appkey = json.getString("appkey");
		String appSecret = json.getString("appSecret");
		String token  = json.getString("token");
		String opToken  = json.getString("opToken");
		String operator  = json.getString("operator");
		String mobile  = getMobile(appkey,appSecret,token,opToken,operator);
		if (!Tools.isMobile(mobile)) {
			return ActionResult.fail(AgentErrorCodeEnum.db_error);
		}
		String inviteCode  = json.getString("inviteCode");
		String lng = json.getString("lng");
		String lat = json.getString("lat");
		String cityName = json.getString("cityName");
		Integer cityCode = json.getInteger("cityCode");
		Integer vip = json.getIntValue("vip");
		Long time = json.getLongValue("time");
		Integer pushchannel = json.getInteger("pushchannel");
		
		if (mobile.equals("15873952675") && RequestUtils.getCurrent().getHeader().getOs_type()==2 && !RequestUtils.getCurrent().getHeader().getChannel().equals("App Store")) {
			return ActionResult.fail(9999,"账号不存在，无法登陆！");
		}
		if (mobile.equals("18811315514") && RequestUtils.getCurrent().getHeader().getOs_type()==2 && !RequestUtils.getCurrent().getHeader().getChannel().equals("ios_miyou")) {
			return ActionResult.fail(9999,"账号不存在，无法登陆！");
		}
		if (mobile.equals("13688939043") && RequestUtils.getCurrent().getHeader().getOs_type()==2 && !RequestUtils.getCurrent().getHeader().getChannel().equals("ios_duidui")) {
			return ActionResult.fail(9999,"账号不存在，无法登陆！");
		}
		
		ThirdRegistDto dto = new ThirdRegistDto();
		dto.setOpenid(mobile);
		dto.setLng(Tools.parseDouble(lng));
		dto.setLat(Tools.parseDouble(lat));
		dto.setInviteCode(inviteCode);
		dto.setCityName(cityName);
		dto.setCityCode(cityCode);
		dto.setFr(UserFrEnum.mobile.getCode());
		dto.setSex(1);
		dto.setVip(vip);
		dto.setTime(time);
		UserBO user = userAgent.findByUsername(mobile);
		//第三方登录可以用手机号登录
		if(user == null && Tools.isMobile(mobile)) user = userAgent.findByMobile(mobile);
		//注销账号处理方法
		if (user!=null&&user.getStatus()==-1) {
			UserLogoutLogEntity logoutLog =  userLogoutLogContract.findById(user.getUserid());
			if (logoutLog!=null) {
				long day = (System.currentTimeMillis()-Tools.getDayTime(logoutLog.getCreate_time()))/Tools.DAY_MILLIS;
				if (day<=180) {
					return ActionResult.fail(ErrorCodeEnum.user_already_logout.getCode() , ErrorCodeEnum.user_already_logout.getDesc());
				}else{
					Map<String,Object> updateStatement = new HashMap<>();
					updateStatement.put("mobile", null);
					userContract.updateById(updateStatement, user.getUserid());
					UserModifyDto modify = new UserModifyDto();
					modify.setUserid(user.getUserid());
					modify.setUserName(user.getUsername()+"-"+random.nextInt(9999));
					userAgent.updateUser(modify);
					user = null;
				}
			}
		}
		if (user!=null && user.isWaiter()) {
			if (RequestUtils.getCurrent().getHeader().getOs_type()==1 &&!RequestUtils.getCurrent().getHeader().getPackageName().equals("com.tjhj.dvzs")) {
				return ActionResult.fail(ErrorCodeEnum.only_user_can_login.getCode() , ErrorCodeEnum.only_user_can_login.getDesc());
			}
			if (RequestUtils.getCurrent().getHeader().getOs_type()==2 && !RequestUtils.getCurrent().getHeader().getPackageName().equals("com.zdkj.lttool")) {
				return ActionResult.fail(ErrorCodeEnum.only_user_can_login.getCode() , ErrorCodeEnum.only_user_can_login.getDesc());
			}
		}
		if (RequestUtils.getCurrent().getHeader().getPackageName().equals("com.tjhj.dvzs") || RequestUtils.getCurrent().getHeader().getPackageName().equals("com.zdkj.lttool")) {
			if (user==null||!user.isWaiter()) {
				return ActionResult.fail(ErrorCodeEnum.only_V_can_login.getCode() , ErrorCodeEnum.only_V_can_login.getDesc());
			}
		}
		if (user == null) {
			dto.setIfRegLogin(true);
		}else{
			dto.setIfRegLogin(false);
		}
		if (dto.getVip()==1) {
			loginVipUser(dto);
		}
		dto.setPushchannel(pushchannel);
		return ActionResult.success(login(dto));
	}
	
	
	private String getMobile(String appkey, String appSecret, String token, String opToken, String operator) throws Exception {
		String authHost = "http://identify.verify.mob.com/";
        String url = authHost + "auth/auth/sdkClientFreeLogin";
        HashMap<String, Object> request = new HashMap<>();
        request.put("appkey", appkey);
        request.put("token", token);
        request.put("opToken", opToken);
        request.put("operator", operator);
        request.put("timestamp", System.currentTimeMillis());
        request.put("sign", SignUtil.getSign(request, appSecret));
        String response = postRequestNoSecurity(url, null, request);

        JSONObject jsonObject = JSONObject.parseObject(response);
        if (200 == jsonObject.getInteger("status")) {
            String res = jsonObject.getString("res");
            byte[] decode = DES.decode(Base64Utils.decode(res.getBytes()), appSecret.getBytes());
            JSONObject json = JSONObject.parseObject(new String(decode));
            return json.getString("phone");
        }
		return null;
	}

	 public static String postRequestNoSecurity(String url, Map<String, String> headers, Object data) throws Exception {
	        String securityReq = JSON.toJSONString(data);
	        OkHttpClient okHttpClient = new OkHttpClient.Builder().readTimeout(30, TimeUnit.SECONDS).build();
	        RequestBody body = RequestBody.create(MediaType.parse("application/json"), securityReq);
	        Request.Builder builder = new Request.Builder();
	        if (Tools.isNotNull(headers)) {
	            for (Map.Entry<String, String> entry : headers.entrySet()) {
	                builder.addHeader(entry.getKey(), entry.getValue());
	            }
	        }
	        final Request request = builder.addHeader("Content-Length", String.valueOf(securityReq.length()))
	                .url(url)
	                .post(body)
	                .build();
	        Call call = okHttpClient.newCall(request);
	        Response response = call.execute();

	        String securityRes = response.body().string();
	        return securityRes;
	    }

	public static List<String> apppakageList = new ArrayList<String>(); 
	static{
		apppakageList.add("me.yidui");
		apppakageList.add("com.meiliyue");
		apppakageList.add("cn.mimilive.sysm");
		apppakageList.add("com.immomo.momo");
		apppakageList.add("com.yuedan");
		apppakageList.add("com.leetek.melover");
		apppakageList.add("com.maskpark");
		apppakageList.add("com.yyk.whenchat");
		apppakageList.add("com.yunlian.yudao");
		apppakageList.add("cn.v6.xiuchang");
		apppakageList.add("com.yr.huajian");
		apppakageList.add("com.VideoCall.GuangShu");
		apppakageList.add("com.welove520.welove");
		apppakageList.add("com.yyk.knowchat");
		apppakageList.add("com.YSMY.rcx");
		apppakageList.add("com.meiliao.mlfsb");
		apppakageList.add("com.mm.peiliao");
		apppakageList.add("com.axiaodiao.huacha");
		apppakageList.add("com.coco3g.hudiegu");
		apppakageList.add("com.quyue.android");
		apppakageList.add("com.ninexiu.sixninexiu");
		apppakageList.add("com.mobilevoice.findyou");
		apppakageList.add("com.mosheng");
		apppakageList.add("com.jieyuanppp.tcamyxy");
		apppakageList.add("com.memezhibo.android");
		apppakageList.add("com.justdo.yeliao");
		apppakageList.add("com.tongchengsupeiyp.yayha");
		apppakageList.add("com.mm.youliao");
		apppakageList.add("com.xunai.sleep");
		apppakageList.add("com.youyuan.yhb");
		apppakageList.add("com.yr.huaxiu");
		apppakageList.add("com.vliao.vchat");
		apppakageList.add("com.imodan");
		apppakageList.add("com.jieyuanppp.baitcpaoyueai");
		apppakageList.add("com.zhenyue.im");
		apppakageList.add("com.xiaouliao.uul");
	}
	@Override
	public UserBO WeixinH5Login(String openid) throws Exception {
		logger.info("微信H5注册openid="+openid);
		UserBO user = userAgent.findByUsername(openid);
		if(user == null) {
			UserCreatDto creat = new UserCreatDto();
			creat.setBirthday(Tools.getDate("1990-01-01"));
			creat.setFr(UserFrEnum.weixinH5.getCode());
			creat.setOpenid(openid);
			//是否有city变量
			AreaDto cityDto = null;
			//如果没有设置城市变量，则根据IP地址自动获取城市信息
			if(cityDto == null) {
				String areaName = ServiceHelper.getCityNameByIP(Tools.getIP(RequestUtils.getCurrent().getRequest()));
				if(Tools.isNotNull(areaName)) {
					AreaDto cityArea = appAreaService.getCityByName(areaName);
					if(cityArea != null) {
						AreaDto[] areas = appAreaService.getAreas(cityArea.getId());
						creat.setCountryid(areas[0]!=null?areas[0].getId():0L);
						creat.setProvinceid(areas[1]!=null?areas[1].getId():0L);
						creat.setCityid(areas[2]!=null?areas[2].getId():0L);
						cityDto = areas[2];
					}
				}
			}
			//本地库没有获得城市信息在通过ip138 获得城市信息
			if(cityDto == null) {
				String[] areaNameArray = ServiceHelper.get138ArrayByIP(Tools.getIP(RequestUtils.getCurrent().getRequest()));
				if (Tools.isNotNull(areaNameArray)) {
					if(Tools.isNotNull(areaNameArray[2])) {
						AreaDto cityArea = appAreaService.getCityByName(areaNameArray[2]);
						if(cityArea != null) {
							AreaDto[] areas = appAreaService.getAreas(cityArea.getId());
							creat.setCountryid(areas[0]!=null?areas[0].getId():0L);
							creat.setProvinceid(areas[1]!=null?areas[1].getId():0L);
							creat.setCityid(areas[2]!=null?areas[2].getId():0L);
							cityDto = areas[2];
						}
					}else if(Tools.isNotNull(areaNameArray[1])){
						AreaDto provinceArea = appAreaService.getProvinceByName(areaNameArray[1]);
						if(provinceArea != null) {
							AreaDto[] areas = appAreaService.getAreas(provinceArea.getId());
							creat.setCountryid(areas[0]!=null?areas[0].getId():0L);
							creat.setProvinceid(areas[1]!=null?areas[1].getId():0L);
							creat.setCityid(areas[2]!=null?areas[2].getId():0L);
							cityDto = areas[2];
						}
					}
				}
			}
			creat.setSex(1);
			creat.setClientid(openid);
			creat.setPlatform(PlatformEnum.H5.type);
			creat.setAppVersion("1.0.0");
			creat.setVersionCode(1);
			creat.setPackageName("com.tigerjoys.weixin.h5");//包名   
			creat.setChannel("H5_weixin");
			logger.info("注册手机类型="+PlatformEnum.H5.type);
			creat.setLng(0.0);
			creat.setLat(0.0);
			creat.setSexOpinion(1);
			creat.setSpouseOpinion(1);
			creat.setMakeFriend(1);
			final AreaDto city = cityDto;
			//匿名
			user = userAgent.createUser(creat,userEntity->{
				//将刚刚注册的用户的注册时间加入到redis的zset中
				cacheRedis.zadd(CommonConst.registerUser, userEntity.getCreate_time().getTime(), String.valueOf(userEntity.getId()));
				
				//新用户注册送钻
				PageModel pageModel = new PageModel();
				pageModel.addQuery(Restrictions.eq("client_id", openid));
				List<UserLoginLogEntity> loginLog = userLoginLogContract.load(pageModel);
				if(Tools.isNull(loginLog)){
					boolean send = channelCheckService.checkSendFlower();
					if(send)
						redFlowerAgent.increaseFlowerAccount(userEntity.getId(), 3, RedFlowerAccountLogTypeEnum.register_user, userEntity.getId()+"register_user");
				}
				String cityCode = city.getCitycode();
				//特殊处理北京地区的不送体验机会
				if (Tools.isNull(loginLog) && !("131".equals(cityCode))) {
					int award = appUserAllowanceAgent.sendAllowance(openid, "H5_weixin", 1, userEntity.getId());
					if(award == AppUserAllowanceTypeEnum.diamond.getCode()) {
						userDiamondAgent.changeDiamondAccount(userEntity.getId(), sysConfigAgent.getIntvalue(Const.NEW_USER_AWARD), (long)0, UserDiamondAccountLogTypeEnum.reg_award.getCode(), 1, null, userEntity.getId()+"_reg_award", UserDiamondAccountLogTypeEnum.reg_award.getDesc());
					} else if(award == AppUserAllowanceTypeEnum.money.getCode()) {
						long logId = IdGenerater.generateId();
						userIncomeAgent.changeIncomeAccount(userEntity.getId(), 6 * 100, 1, UserIncomeAccountLogTypeEnum.new_user_award, String.valueOf(logId), UserIncomeAccountLogTypeEnum.new_user_award.getDesc());
					} else if(award == AppUserAllowanceTypeEnum.experienceCard.getCode()) {
						VchatRoomObscurationConfigDto configDto = sysConfigAgent.obscurationConfig();
						freeVideoChatExperienceAgent.insertRecord(userEntity.getId(), "H5_weixin",Tools.intValue(configDto.getDialHelperTime()),0);
					}
				}
				
				//用户注册信息记录，2018年5月2日 18:03:31由userloginlog改为此代码，注册跟登录和退出分开，利于统计数据
				UserRegLogEntity log = new UserRegLogEntity();
				log.setCity_name(city!=null?city.getName():null);
				log.setClient_id(creat.getClientid());
				log.setCreate_time(userEntity.getCreate_time());
				log.setIp(Tools.getIP(RequestUtils.getCurrent().getRequest()));
				log.setLat(0.0);
				log.setLng(0.0);
				log.setUserid(userEntity.getId());
				log.setApptype(PlatformEnum.H5.type);
				log.setAppversion("1.0.0");
				log.setChannel("H5_weixin");
				log.setFr(userEntity.getFr());
				log.setPackage_name("com.tigerjoys.weixin.h5");
				userRegLogContract.insert(log);
				//记录登录信息
				UserLoginLogEntity userLoginLog = new UserLoginLogEntity();
				userLoginLog.setCity_name(city!=null?city.getName():null);
				userLoginLog.setClient_id(creat.getClientid());
				userLoginLog.setCreate_time(new Date());
				userLoginLog.setIp(Tools.getIP(RequestUtils.getCurrent().getRequest()));
				userLoginLog.setLat(0.0);
				userLoginLog.setLng(0.0);
				userLoginLog.setType(UserLoginLogTypeEnum.register.getCode());
				userLoginLog.setUser_id(userEntity.getId());
				userLoginLog.setApptype(PlatformEnum.H5.type);
				userLoginLog.setAppversion("1.0.0");
				userLoginLog.setChannel("H5_weixin");
				userLoginLog.setPackage_name("com.tigerjoys.weixin.h5");
				userLoginLogContract.insert(userLoginLog);
			});
		} else {
			Date currDate = new Date();
			String token = null;

			//查看是否需要更新信息
			UserModifyDto modify = new UserModifyDto();
			modify.setUserid(user.getUserid());
			modify.setRefresh_time(currDate);
			//如果当前时间-最后登录时间 > 一周，则重新生成token
			if(currDate.getTime() - user.getLastLoginDate().getTime() > Const.USER_TOKEN_EXPIRE_MILLIS) {
				token = MD5.encode(System.currentTimeMillis()+"#"+user.getUserid()+"#"+new Random().nextInt(10000));
				modify.setUnique_key(token);
			} else {
				token = user.getUniqueKey();
			}
			userAgent.updateUser(modify);

			//此处需要更新刷新一下User对象的数据,一定不能忘记了
			user.setUniqueKey(token);
			user.setLastLoginDate(modify.getRefresh_time());
			
			UserLoginLogEntity log = new UserLoginLogEntity();
			log.setCity_name("");
			log.setClient_id(user.getClientid());
			log.setCreate_time(currDate);
			log.setIp(Tools.getIP(RequestUtils.getCurrent().getRequest()));
			log.setLat(0.0);
			log.setLng(0.0);
			log.setType(UserLoginLogTypeEnum.login.getCode());
			log.setUser_id(user.getUserid());
			log.setApptype(user.getPlatform());
			log.setAppversion("1.0.0");
			log.setChannel("H5_weixin");
			log.setPackage_name("com.tigerjoys.weixin.h5");
			userLoginLogContract.insert(log);
		}
		logger.info("用户注册：" + JsonHelper.toJson(user));
		return user;
	}
	
	/**
	 * 触发发送消息的业务
	 */
	private class SendMessageThread implements Runnable {
		
		private long userid;
		
		public SendMessageThread(long userid) {
			this.userid = userid;
		}
		
		@Override
		public void run() {
			try {
				Thread.sleep(2500);
				String msg = "恭喜您：系统已把您推荐给同城在线女神，如您收到招呼信息和视频来电，并非机器人，全部100%真实认证女用户，在聊天过程中请注意文明礼貌！\n 祝你度过美好时光！";
				if(Const.IS_TEST) {
					neteaseAgent.pushOneMessage(131879189602173184L, userid, msg, true);
				} else {
					neteaseAgent.pushOneMessage(65418719477628672L, userid, msg, true);
				}
			} catch (Exception e) {
				
			}
		}
	}
	
}
