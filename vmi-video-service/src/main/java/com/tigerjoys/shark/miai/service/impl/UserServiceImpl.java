package com.tigerjoys.shark.miai.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.shark.miai.common.cloud.storage.ICloudStorage;
import org.shark.miai.common.enums.IncomeEnum;
import org.shark.miai.common.enums.IndexActivityAreaEnum;
import org.shark.miai.common.enums.MakeFriendEnum;
import org.shark.miai.common.enums.MarriageEnum;
import org.shark.miai.common.enums.PlatformEnum;
import org.shark.miai.common.util.AESFieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.tigerjoys.nbs.common.ActionResult;
import com.tigerjoys.nbs.common.utils.BeanUtils;
import com.tigerjoys.nbs.common.utils.JsonHelper;
import com.tigerjoys.nbs.common.utils.Tools;
import com.tigerjoys.nbs.mybatis.core.page.PageModel;
import com.tigerjoys.nbs.mybatis.core.sql.Projections;
import com.tigerjoys.nbs.mybatis.core.sql.Restrictions;
import com.tigerjoys.nbs.web.context.RequestHeader;
import com.tigerjoys.nbs.web.context.RequestUtils;
import com.tigerjoys.shark.miai.Const;
import com.tigerjoys.shark.miai.agent.IAnchorContactAgent;
import com.tigerjoys.shark.miai.agent.IAnchorDefendAgent;
import com.tigerjoys.shark.miai.agent.IAnchorDynamicPriceAgent;
import com.tigerjoys.shark.miai.agent.IAnchorImageAgent;
import com.tigerjoys.shark.miai.agent.IAnchorRecvUserAgent;
import com.tigerjoys.shark.miai.agent.IDynamicAgent;
import com.tigerjoys.shark.miai.agent.INeteaseAgent;
import com.tigerjoys.shark.miai.agent.ISendMessageAgent;
import com.tigerjoys.shark.miai.agent.ISysConfigAgent;
import com.tigerjoys.shark.miai.agent.IUserAgent;
import com.tigerjoys.shark.miai.agent.IUserBgPictureAgent;
import com.tigerjoys.shark.miai.agent.IUserCharmAgent;
import com.tigerjoys.shark.miai.agent.IUserDiamondAgent;
import com.tigerjoys.shark.miai.agent.IUserEnergyAgent;
import com.tigerjoys.shark.miai.agent.IUserExtensionAgent;
import com.tigerjoys.shark.miai.agent.IUserFriendAgent;
import com.tigerjoys.shark.miai.agent.IUserGiftAgent;
import com.tigerjoys.shark.miai.agent.IUserLookContactsLogAgent;
import com.tigerjoys.shark.miai.agent.IUserMypageActivityAgent;
import com.tigerjoys.shark.miai.agent.IUserOnlineStateAgent;
import com.tigerjoys.shark.miai.agent.IUserVideoAuthAgent;
import com.tigerjoys.shark.miai.agent.dto.AreaDto;
import com.tigerjoys.shark.miai.agent.dto.UserBO;
import com.tigerjoys.shark.miai.agent.dto.UserBgPictureBO;
import com.tigerjoys.shark.miai.agent.dto.UserExtensionBO;
import com.tigerjoys.shark.miai.agent.dto.UserFriendBO;
import com.tigerjoys.shark.miai.agent.dto.UserVideoAuthBO;
import com.tigerjoys.shark.miai.agent.dto.result.AgentResult;
import com.tigerjoys.shark.miai.agent.dto.result.DiamondResultDto;
import com.tigerjoys.shark.miai.agent.dto.transfer.UserModifyDto;
import com.tigerjoys.shark.miai.agent.enums.AgentErrorCodeEnum;
import com.tigerjoys.shark.miai.agent.enums.AnchorOnlineStateEnum;
import com.tigerjoys.shark.miai.agent.enums.AnchorStateEnum;
import com.tigerjoys.shark.miai.agent.enums.GlobalBroadcastTypeEnum;
import com.tigerjoys.shark.miai.agent.enums.NeteaseErrorEnum;
import com.tigerjoys.shark.miai.agent.enums.PhotoCheckedLogTypeEnum;
import com.tigerjoys.shark.miai.agent.enums.SendSmsTypeEnum;
import com.tigerjoys.shark.miai.agent.neteasecheck.INeteaseTextCheck;
import com.tigerjoys.shark.miai.agent.service.IAppAreaService;
import com.tigerjoys.shark.miai.dto.service.AnchorIntimateRankingsDto;
import com.tigerjoys.shark.miai.dto.service.BgPictureVO;
import com.tigerjoys.shark.miai.dto.service.BtnData;
import com.tigerjoys.shark.miai.dto.service.DlgAndGoPage;
import com.tigerjoys.shark.miai.dto.service.DlgAndGoPageNew;
import com.tigerjoys.shark.miai.dto.service.GlobalBroadcastDto;
import com.tigerjoys.shark.miai.dto.service.ImpressionDto;
import com.tigerjoys.shark.miai.dto.service.UserBaseInfo;
import com.tigerjoys.shark.miai.dto.service.UserBasicVO;
import com.tigerjoys.shark.miai.dto.service.UserCloseDto;
import com.tigerjoys.shark.miai.dto.service.UserContactsVO;
import com.tigerjoys.shark.miai.dto.service.UserDetailDto;
import com.tigerjoys.shark.miai.dto.service.UserEvaluationDto;
import com.tigerjoys.shark.miai.dto.service.UserExtensionVO;
import com.tigerjoys.shark.miai.dto.service.UserGiftListDto;
import com.tigerjoys.shark.miai.dto.service.UserGiftListStatisticsDto;
import com.tigerjoys.shark.miai.dto.service.UserGiftListVO;
import com.tigerjoys.shark.miai.dto.service.UserHomePageDto;
import com.tigerjoys.shark.miai.dto.service.UserHomePageOldVO;
import com.tigerjoys.shark.miai.dto.service.UserHomePageVO;
import com.tigerjoys.shark.miai.dto.service.UserImgVideoVO;
import com.tigerjoys.shark.miai.dto.service.UserInformDto;
import com.tigerjoys.shark.miai.dto.service.UserMypageVO;
import com.tigerjoys.shark.miai.dto.service.UserShortVideoDto;
import com.tigerjoys.shark.miai.dto.service.UserSimpleDto;
import com.tigerjoys.shark.miai.dto.service.UserVideoAuthVO;
import com.tigerjoys.shark.miai.dto.service.WeixinUserHomePageVO;
import com.tigerjoys.shark.miai.enums.DlgAndGoPageEnum;
import com.tigerjoys.shark.miai.enums.ErrorCodeEnum;
import com.tigerjoys.shark.miai.enums.StaticWebUrlEnum;
import com.tigerjoys.shark.miai.inter.contract.IAnchorAudioContract;
import com.tigerjoys.shark.miai.inter.contract.IAnchorEvaluationLogContract;
import com.tigerjoys.shark.miai.inter.contract.IAnchorEvaluationStatisticsContract;
import com.tigerjoys.shark.miai.inter.contract.IAnchorIntimateRankingsContract;
import com.tigerjoys.shark.miai.inter.contract.IAnchorIntimateRankingsFakeContract;
import com.tigerjoys.shark.miai.inter.contract.IAnchorOnlineContract;
import com.tigerjoys.shark.miai.inter.contract.IAnchorTagContract;
import com.tigerjoys.shark.miai.inter.contract.IAppChannelContract;
import com.tigerjoys.shark.miai.inter.contract.IAppLabelContract;
import com.tigerjoys.shark.miai.inter.contract.IAppPackageContract;
import com.tigerjoys.shark.miai.inter.contract.IAppVersionContract;
import com.tigerjoys.shark.miai.inter.contract.ICopyUserContract;
import com.tigerjoys.shark.miai.inter.contract.ICopyUserInformContract;
import com.tigerjoys.shark.miai.inter.contract.IGlobalBroadcastContract;
import com.tigerjoys.shark.miai.inter.contract.IShortVideoContract;
import com.tigerjoys.shark.miai.inter.contract.IUserBlacklistContract;
import com.tigerjoys.shark.miai.inter.contract.IUserChatGiftContract;
import com.tigerjoys.shark.miai.inter.contract.IUserChatGiftLogContract;
import com.tigerjoys.shark.miai.inter.contract.IUserChatGiftLogFakeContract;
import com.tigerjoys.shark.miai.inter.contract.IUserChatGiftStatisticsContract;
import com.tigerjoys.shark.miai.inter.contract.IUserChatGiftStatisticsFakeContract;
import com.tigerjoys.shark.miai.inter.contract.IUserContract;
import com.tigerjoys.shark.miai.inter.contract.IUserInformContract;
import com.tigerjoys.shark.miai.inter.contract.IUserInviteContract;
import com.tigerjoys.shark.miai.inter.contract.IUserLoginLogContract;
import com.tigerjoys.shark.miai.inter.contract.IUserPhotoResourceContract;
import com.tigerjoys.shark.miai.inter.contract.IUserPrivacySecurityContract;
import com.tigerjoys.shark.miai.inter.contract.IUserTypeFeedbackContract;
import com.tigerjoys.shark.miai.inter.entity.AnchorEvaluationLogEntity;
import com.tigerjoys.shark.miai.inter.entity.AnchorEvaluationStatisticsEntity;
import com.tigerjoys.shark.miai.inter.entity.AnchorIntimateRankingsEntity;
import com.tigerjoys.shark.miai.inter.entity.AnchorIntimateRankingsFakeEntity;
import com.tigerjoys.shark.miai.inter.entity.AnchorOnlineEntity;
import com.tigerjoys.shark.miai.inter.entity.AnchorTagEntity;
import com.tigerjoys.shark.miai.inter.entity.AppChannelEntity;
import com.tigerjoys.shark.miai.inter.entity.AppLabelEntity;
import com.tigerjoys.shark.miai.inter.entity.AppPackageEntity;
import com.tigerjoys.shark.miai.inter.entity.AppVersionEntity;
import com.tigerjoys.shark.miai.inter.entity.CopyUserEntity;
import com.tigerjoys.shark.miai.inter.entity.CopyUserInformEntity;
import com.tigerjoys.shark.miai.inter.entity.GlobalBroadcastEntity;
import com.tigerjoys.shark.miai.inter.entity.ShortVideoEntity;
import com.tigerjoys.shark.miai.inter.entity.UserBlacklistEntity;
import com.tigerjoys.shark.miai.inter.entity.UserChatGiftEntity;
import com.tigerjoys.shark.miai.inter.entity.UserChatGiftLogEntity;
import com.tigerjoys.shark.miai.inter.entity.UserChatGiftLogFakeEntity;
import com.tigerjoys.shark.miai.inter.entity.UserChatGiftStatisticsEntity;
import com.tigerjoys.shark.miai.inter.entity.UserChatGiftStatisticsFakeEntity;
import com.tigerjoys.shark.miai.inter.entity.UserEntity;
import com.tigerjoys.shark.miai.inter.entity.UserInformEntity;
import com.tigerjoys.shark.miai.inter.entity.UserLoginLogEntity;
import com.tigerjoys.shark.miai.inter.entity.UserPhotoResourceEntity;
import com.tigerjoys.shark.miai.inter.entity.UserPrivacySecurityEntity;
import com.tigerjoys.shark.miai.inter.entity.UserTypeFeedbackEntity;
import com.tigerjoys.shark.miai.service.IChannelCheckService;
import com.tigerjoys.shark.miai.service.IConfService;
import com.tigerjoys.shark.miai.service.IPhotoCheckedLogService;
import com.tigerjoys.shark.miai.service.IUserPhotoService;
import com.tigerjoys.shark.miai.service.IUserService;
import com.tigerjoys.shark.miai.service.IVChatTextYXService;
import com.tigerjoys.shark.miai.utils.FileUploadResult;
import com.tigerjoys.shark.miai.utils.Helper;
import com.tigerjoys.shark.miai.utils.ServiceHelper;

/**
 * 用户服务实现类
 * @author lipeng
 *
 */
@Service
public class UserServiceImpl implements IUserService {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final Random random = new Random();
	
	@Autowired
	private IUserAgent userAgent;
	
	@Autowired
	private IUserContract userContract;
	
	@Autowired
	private INeteaseAgent neteaseAgent;
	
	@Autowired
	private IDynamicAgent dynamicAgent;
	
	@Autowired
	private IUserInformContract userInformContract;
	
	@Autowired
	private ICopyUserInformContract copyUserInformContract;
	
	@Autowired
	private IUserVideoAuthAgent userVideoAuthAgent;
	
	@Autowired
	private IUserInviteContract userInviteContract;
	
	@Autowired
	private IUserLookContactsLogAgent userLookContactsLogAgent;
	
	@Autowired
	private IUserBlacklistContract userBlacklistContract;
	
	@Autowired
	private IUserFriendAgent userFriendAgent;
	
	@Autowired
	private IUserCharmAgent userCharmAgent;
	
	@Autowired
	private INeteaseTextCheck neteaseTextCheck;
	
	@Autowired
	@Qualifier("upYunCloudStorage")
	private ICloudStorage upYunCloudStorage;
	
	@Autowired
	private IUserMypageActivityAgent userMypageActivityAgent;
	
	@Autowired
	private IAppAreaService appAreaService;
	
	@Autowired
	private IUserExtensionAgent userExtensionAgent;
	
	@Autowired
	private ISendMessageAgent sendMessageAgent;
	
	@Autowired
	private IUserDiamondAgent userDiamondAgent;
	
	@Autowired
	private IAnchorOnlineContract anchorOnlineContract;
	
	@Autowired
	private IAnchorDefendAgent anchorDefendAgent;
	
	@Autowired
	private IUserBgPictureAgent userBgPictureAgent;
	
	@Autowired
	private IAnchorIntimateRankingsContract anchorIntimateRankingsContract;
	
	@Autowired
	private IAnchorIntimateRankingsFakeContract anchorIntimateRankingsFakeContract;
	
	@Autowired
	private IUserTypeFeedbackContract userTypeFeedbackContract;
	
	@Autowired
	private IUserPrivacySecurityContract userPrivacySecurityContract;
	
	@Autowired
	private IUserLoginLogContract userLoginLogContract;
	
	@Autowired
	private ISysConfigAgent sysConfigAgent;
	
	@Autowired
	private IChannelCheckService channelCheckService;
	
	@Autowired
	private IShortVideoContract shortVideoContract;
	
	@Autowired
	private IGlobalBroadcastContract globalBroadcastContract;
	
	@Autowired
	private IUserChatGiftStatisticsContract userChatGiftStatisticsContract;
	
	@Autowired
	private IUserChatGiftStatisticsFakeContract userChatGiftStatisticsFakeContract;
	
	@Autowired
	private IUserChatGiftLogContract userChatGiftLogContract;
	
	@Autowired
	private IUserEnergyAgent userEnergyAgent;
	
	@Autowired
	private IUserOnlineStateAgent userOnlineStateAgent;
	
	@Autowired
	private IUserPhotoResourceContract userPhotoResourceContract;
	
	@Autowired
	private IAnchorEvaluationStatisticsContract anchorEvaluationStatisticsContract;
	
	@Autowired
	private IAppLabelContract appLabelContract;
	
	@Autowired
	private IAppPackageContract appPackageContract;
	
	@Autowired
	private IAppChannelContract appChannelContract;
	
	@Autowired
	private IAppVersionContract appVersionContract;
	
	@Autowired
	private IAnchorEvaluationLogContract anchorEvaluationLogContract;
	
	@Autowired
	private IUserChatGiftContract userChatGiftContract;
	
	@Autowired
	private IUserChatGiftLogFakeContract userChatGiftLogFakeContract;
	
	@Autowired
	private IConfService confService;
	
	@Autowired
	private IAnchorImageAgent anchorImageAgent;
	
	@Autowired
	private IUserGiftAgent userGiftAgent;
	
	@Autowired
	private IAnchorAudioContract anchorAudioContract;
	
	@Autowired
	private IAnchorRecvUserAgent anchorRecvUserAgent;
	
	@Autowired
	private IAnchorDynamicPriceAgent  anchorDynamicPriceAgent ;
	
	@Autowired
	private ICopyUserContract copyUserContract;
	
	@Autowired
	private IAnchorTagContract anchorTagContract;
	
	@Autowired
	private IPhotoCheckedLogService photoCheckedLogService;
	
	@Autowired
	private IVChatTextYXService vChatTextYXService;
	
	@Autowired
	private IAnchorContactAgent anchorContactAgent;
	
	@Autowired
	private IUserPhotoService userPhotoService;
	
	/**
	 * 用户拓展信息分隔符
	 */
	private static final String SEPARATOR = "#";
	
	/**
	 * 不允许连续符号的出现
	 */
	private final Pattern symbolPattern = Pattern.compile("[_-]{2,}", Pattern.CASE_INSENSITIVE);
	
	@Override
	public ActionResult userInfo(long userid) throws Exception {
		if(userid <= 0) {
			return ActionResult.fail(ErrorCodeEnum.parameter_error.getCode(), ErrorCodeEnum.parameter_error.getDesc());
		}
		
		UserDetailDto user = getUserDetailDto(userid);
		if(user == null) {
			return ActionResult.fail(ErrorCodeEnum.user_not_found.getCode() , ErrorCodeEnum.user_not_found.getDesc());
		}
		
		return ActionResult.success(user);
	}

	public UserDetailDto getUserDetailDto(long userid) throws Exception {
		UserBO user = userAgent.findById(userid);
		UserExtensionBO ue = userExtensionAgent.findByUserId(userid);
		if(user == null) {
			return null;
		}
		return transferUserDetailDto(user , ue);
	}
	
	@Override
	public ActionResult changeUserPhoto(MultipartFile photo) throws Exception {
		if (photo == null) {
			return ActionResult.fail(ErrorCodeEnum.photo_empty_error.getCode(), "请上传头像");
		}
		FileUploadResult result = Helper.uploadPicture(photo, "user");
		if (result.getCode() == 0) {
			String photoPath = result.getFilePath();
			UserModifyDto user = new UserModifyDto();
			user.setUserid(RequestUtils.getCurrent().getUserid());
			user.setVideo_auth(photoPath);//启用废弃字段 作为待审核头像地址
			user.setRefresh_time(new Date());
			userAgent.updateUser(user);
			CopyUserEntity copyUser = copyUserContract.findById(RequestUtils.getCurrent().getUserid());
			if (copyUser!=null) {
				CopyUserEntity copyUserTemp = new CopyUserEntity();
				copyUserTemp.setId(RequestUtils.getCurrent().getUserid());
				copyUserTemp.setVideo_auth(photoPath);//启用废弃字段 作为待审核头像地址
				user.setRefresh_time(new Date());
				copyUserContract.update(copyUserTemp);
			}
			
			
			/*if (RequestUtils.getCurrent().getHeader().getOs_type()==1) { //0未知,1安卓,2IOS
			}else{
				UserModifyDto user = new UserModifyDto();
				user.setUserid(RequestUtils.getCurrent().getUserid());
				user.setPhoto(photoPath);
				userAgent.updateUser(user);
				UserBO userBO = userAgent.findById(RequestUtils.getCurrent().getUserid());
				AnchorOnlineEntity anchor = anchorOnlineContract.findByProperty("userid", userBO.getUserid());
				if (userBO != null && anchor != null) {
					anchorOnlineContract.updateAnchorInfo(RequestUtils.getCurrent().getUserid(), "", photoPath);
				}
				//更新网易头像
				try {
					Map<String,String> params = new HashMap<>();
					params.put("accid", String.valueOf(user.getUserid()));
					params.put("icon", ServiceHelper.getUserPhoto(user.getPhoto()));
					JSONObject data = neteaseAgent.updateUser(params);
					if (NeteaseErrorEnum.success.getCode() != data.getIntValue("code")) {
						logger.warn("update icon to Netease occured error:{}",data);
					}
				} catch (Exception e) {
					logger.warn("update netease icon fail!", e);
				}
			}*/
			return ActionResult.success(ServiceHelper.getUserPhoto(photoPath));
		} else {
			return ActionResult.fail(result.getCode(), result.getMsg());
		}
	}
	
	@Override
	public ActionResult addBgPicture(JSONObject json, MultipartFile bgPicture) throws Exception {
		Integer bgId = json.getInteger("bgId");
		if (bgId != 0) {
			UserBgPictureBO bo = userBgPictureAgent.findById(bgId);
			UserModifyDto user = new UserModifyDto();
			user.setUserid(RequestUtils.getCurrent().getUserid());
			user.setBgPicture(bo.getUrl());
			userAgent.updateUser(user);
			return ActionResult.success(ServiceHelper.getCdnPhoto(bo.getUrl()));
		}else{
			if (bgPicture == null) {
				return ActionResult.fail(ErrorCodeEnum.bg_picture_empty_error.getCode(), "请上传图片");
			}
			
			FileUploadResult result = Helper.uploadPicture(bgPicture, "bgImg");
			if (result.getCode() == 0) {
				String photoPath = result.getFilePath();
				UserModifyDto user = new UserModifyDto();
				user.setUserid(RequestUtils.getCurrent().getUserid());
				user.setBgPicture(photoPath);
				userAgent.updateUser(user);
				return ActionResult.success(ServiceHelper.getCdnPhoto(photoPath));
			} else {
				return ActionResult.fail(result.getCode(), result.getMsg());
			}
		}
			
	}
	
	@Override
	public ActionResult changeUserInfo(UserDetailDto info) throws Exception {
		if (RequestUtils.getCurrent().getUserid() <= 0) {
			return ActionResult.fail(ErrorCodeEnum.parameter_error.getCode() ,ErrorCodeEnum.parameter_error.getDesc());
		}
		UserBO bo = userAgent.findById(RequestUtils.getCurrent().getUserid());
		if (bo == null) {
			return ActionResult.fail(ErrorCodeEnum.db_error.getCode() ,ErrorCodeEnum.db_error.getDesc());
		}
		CopyUserEntity copyUser = copyUserContract.findById(RequestUtils.getCurrent().getUserid());
		UserModifyDto user = new UserModifyDto();
		String nickname = info.getNickname();
		String anchorSignature = info.getAnchorSignature();
		String anchorIntr = info.getAnchorIntr();
		// 此处需要判断昵称是否符合规则
		AnchorOnlineEntity anchor = anchorOnlineContract.findByProperty("userid", bo.getUserid());
		if (!nickname.equals(bo.getNickname())) {
			nickname = nickname.trim();
			// 此处判断昵称是否符合规则
			if (!nickNameValid(nickname)) {
				return ActionResult.fail(ErrorCodeEnum.user_nickname_no_rule.getCode(),ErrorCodeEnum.user_nickname_no_rule.getDesc());
			}
			if (userAgent.findByNickname(nickname)!=null){
				return ActionResult.fail(ErrorCodeEnum.user_nickname_exist.getCode(),ErrorCodeEnum.user_nickname_exist.getDesc());
			}
			JSONObject jObject =JsonHelper.toJsonObject(neteaseTextCheck.check(user.getUserid(), nickname)) ;
			int code = jObject.getIntValue("code");
			if (code==200) {
				JSONObject resultObject = jObject.getJSONObject("result");
	            int action = resultObject.getIntValue("action");
	            if (action!=0) {
	            	return ActionResult.fail(ErrorCodeEnum.user_nickname_check_refuse.getCode(),ErrorCodeEnum.user_nickname_check_refuse.getDesc());
				}
			}
			user.setNickname(nickname);
		}
		if (Tools.isNotNull(anchorSignature)&&!anchorSignature.equals(bo.getSignature())) {
			user.setSignature(anchorSignature);
			if (anchor!=null) {
				anchor.setSignature(anchorSignature);
			}
		}
		if (Tools.isNotNull(anchorIntr)&&!anchorIntr.equals(bo.getIntroduce())) {
			user.setIntroduce(anchorIntr);
			if (anchor!=null) {
				anchor.setIntro(anchorIntr);
			}
		}
		
		if (anchor!=null) {
			//anchorOnlineContract.updateAnchorInfo(RequestUtils.getCurrent().getUserid(), nickname, "");
			anchor.setNickname(nickname);
			anchor.setId(anchor.getId());
			anchorOnlineContract.update(anchor);
		}
		if (Tools.isNotNull(user)) {
			user.setUserid(RequestUtils.getCurrent().getUserid());
			userAgent.updateUser(user);
			if (copyUser!=null) {
				CopyUserEntity copyUserTemp = new CopyUserEntity();
				copyUserTemp.setId(RequestUtils.getCurrent().getUserid());
				copyUserTemp.setNickname(nickname);
				copyUserContract.update(copyUserTemp);
			}
		}
		//更新网易昵称
		if(Tools.isNotNull(user.getNickname())){
			try {
				Map<String,String> params = new HashMap<>();
				params.put("accid", String.valueOf(user.getUserid()));
				params.put("name", user.getNickname().trim());
				JSONObject data = neteaseAgent.updateUser(params);
				if (NeteaseErrorEnum.success.getCode() != data.getIntValue("code")) {
					logger.warn("update nickName to Netease occured error:{}",data);
				}
			} catch (Exception e) {
				logger.warn("update netease nickName fail!", e);
			}
		}
		
		return ActionResult.success(transferUpdateUserDetailDto(user));
	}
	
	private UserDetailDto transferUpdateUserDetailDto(UserModifyDto user) {
		if(user == null) {
			return null;
		}
		UserDetailDto dto = new UserDetailDto();
		dto.setUserid(user.getUserid());
		dto.setNickname(user.getNickname());
		return dto;
	}

	/**
	 * 验证昵称是否符合规范
	 * 
	 * @param nickname
	 *            - 昵称
	 * @return True or False
	 */
	@Override
	public boolean nickNameValid(String nickname) {
		if (nickname == null) {
			return false;
		}
		if (nickname.length() < 1 || nickname.length() > 10) {
			return false;
		}

		// 仅支持汉字，数字，大小写字母和_-并且不允许符号开头
		if (!Tools.matches("[0-9A-Za-z\\u4e00-\\u9fa5][0-9A-Za-z\\u4e00-\\u9fa5_-]+", nickname)) {
			return false;
		}
		// 纯数字是不允许的
		if (Tools.matches("[0-9]+", nickname)) {
			return false;
		}
		// 不允许符号连续
		Matcher m = symbolPattern.matcher(nickname);
		if (m.find()) {
			return false;
		}

		if (nickname.startsWith("yoyo")) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public ActionResult changeBgPicture() throws Exception {
		List<BgPictureVO> list = new ArrayList<BgPictureVO>();
		List<UserBgPictureBO> bgList = userBgPictureAgent.getBgPicList();
		if(Tools.isNull(bgList)) return null;
		for (UserBgPictureBO bgPic : bgList) {
			BgPictureVO vo = new BgPictureVO();
			vo.setId(bgPic.getId());
			vo.setUrl(ServiceHelper.getCdnPhoto(bgPic.getUrl()));
			list.add(vo);
		}
		return ActionResult.success(list);
	}
	
	@Override
	public ActionResult getMyPage() throws Exception {
		long userid = RequestUtils.getCurrent().getUserid();
		if (userid <= 0) {
			return ActionResult.fail(ErrorCodeEnum.parameter_error.getCode(),ErrorCodeEnum.parameter_error.getDesc());
		}
		UserBO user = userAgent.findById(userid);
		UserMypageVO vo = new UserMypageVO();
		UserBaseInfo info = new UserBaseInfo();
		vo.setUserid(user.getUserid());
		//if (user.getFr()==4&&(RequestUtils.getCurrent().getHeader().getPackageName().equals("com.zdkj.lttool")||RequestUtils.getCurrent().getHeader().getPackageName().equals("com.tjhj.miyou"))) {
		if (user.getFr()==4&&RequestUtils.getCurrent().getHeader().getOs_type()==2) {
			vo.setNickname("未登录");
		}else{
			vo.setNickname(user.getNickname());
		}
		if (Tools.isNotNull(user.getVideoAuth())) {
			vo.setPhoto(ServiceHelper.getUserSmallPhoto(user.getVideoAuth()));
			vo.setVerifyText("审核中");
		}else{
			vo.setPhoto(ServiceHelper.getUserSmallPhoto(user.getPhoto()));
		}
		
		if (Tools.isNull(user.getBgPicture())) {
			String bgPictureUrl = userBgPictureAgent.getBgPic();
			vo.setBgPicture(ServiceHelper.getCdnPhoto(bgPictureUrl));
			UserModifyDto userModify = new UserModifyDto();
			userModify.setUserid(user.getUserid());
			userModify.setBgPicture(bgPictureUrl);
			userAgent.updateUser(userModify);
		}else{
			vo.setBgPicture(ServiceHelper.getCdnPhoto(user.getBgPicture()));
		}
		
		AnchorOnlineEntity aoEntity =  anchorOnlineContract.findByProperty("userid", userid);
		
		vo.setIsV(aoEntity==null?0:aoEntity.getState()==AnchorStateEnum.pass.getCode()||aoEntity.getState()==AnchorStateEnum.undercarriage.getCode()?1:2);
		if (aoEntity!=null && (aoEntity.getState()==AnchorStateEnum.pass.getCode()||aoEntity.getState()==AnchorStateEnum.undercarriage.getCode())) {
			info.setAnchorStar(aoEntity.getStar());
			if (!RequestUtils.getCurrent().getHeader().getChannel().equals("App Store")) {
				if (aoEntity.getAnchor_video_price()>0 && aoEntity.getVideo_type()==1) {
					info.setAnchorPrice(aoEntity.getAnchor_video_price()+"钻/分钟");
				}
				if (aoEntity.getAnchor_audio_price()>0 && aoEntity.getAudio_type()==1) {
					info.setAnchorAudioPrice(aoEntity.getAnchor_audio_price()+"钻/分钟");
				}
			}
			info.setAnchorStar(aoEntity.getStar());
			info.setAnchorTypeVideo(aoEntity.getVideo_type());
			info.setAnchorTypeAudio(aoEntity.getAudio_type());
			PageModel pageModel = new PageModel();
			pageModel.addQuery(Restrictions.eq("userid", userid));
			pageModel.addQuery(Restrictions.ge("status", 0));
			long audioCount = anchorAudioContract.count(pageModel);
			vo.setAudioStatus(audioCount>0?1:0);
		}else{
			info.setNormalLevel("LV."+user.getDegreeid());
		}
		vo.setVip(user.vipValue()<=0?0:1);
		
		vo.setUserInfo(info);
		int isInvite = 0;
		if (Tools.isNotNull(userInviteContract.findByProperty("userid", RequestUtils.getCurrent().getUserid()))){
			isInvite = 1;
		} else {
			Date currdate = new Date();
			Calendar ca = Calendar.getInstance();
			ca.setTime(user.getCreateTime());
			ca.add(Calendar.DATE, 3);
			Date date = ca.getTime();
			isInvite = currdate.before(date)?0:1;
			if (RequestUtils.getCurrent().getHeader().getPackageName().equals("com.tjhj.miliao")) {
				isInvite = 0;
			}
		}
		vo.setIsInvite(isInvite);
		
		AppChannelEntity channel = appChannelContract.findByProperty("name", RequestUtils.getCurrent().getHeader().getChannel());
		
		if (Tools.getDayTime(user.getCreateTime())<Tools.getDayTime(Tools.getDate("2019-12-05"))||user.getVip()==1||(channel==null?0:channel.getVip_status())==1) {
			vo.setWalletShow(1);
		/*}else if(RequestUtils.getCurrent().getHeader().getPackageName().equals("com.tjhj.miliao")
				||RequestUtils.getCurrent().getHeader().getChannel().equals("kuaishou_cpd")){
			vo.setWalletShow(1);*/
		}else{
			vo.setWalletShow(sysConfigAgent.getVIPPopStatus()==0?1:0);
		}
		
		return ActionResult.success(vo);
	}

	@Override
	public ActionResult addActivityViewLog(long indexCode) throws Exception {
		long userid = RequestUtils.getCurrent().getUserid();
		long id = indexCode-Const.MYPAGE_ACTIVITY_INDEXCODE_INIT;
		if (userid > 0 && id > 0) {
			userMypageActivityAgent.addActivityViewLog(id, userid);
		}
		return ActionResult.success();
	}
	
	
	@Override
	public UserBasicVO getUserBasicVO(long userid) throws Exception {
		UserBO user = userAgent.findById(userid);
		if(user == null) {
			return null;
		}
		
		AreaDto cityArea = appAreaService.getCity(user.getCityid());
		
		UserBasicVO vo = new UserBasicVO();
		vo.setAge(Tools.getAge(user.getBirthday()));
		vo.setBirthday(Tools.getDate(user.getBirthday()));
		vo.setCityid(cityArea != null ? cityArea.getBaiduCode() : 0);
		vo.setCityName(cityArea != null ? cityArea.getName() : "");
		vo.setGender(user.getSex());
		vo.setHead(ServiceHelper.getUserSmallPhoto(user.getPhoto()));
		vo.setBigHead(ServiceHelper.getUserBigPhoto(user.getPhoto()));
		vo.setUserid(userid);
		vo.setNickname(user.getNickname());
		vo.setTalentVip(user.talentVipValue());
		vo.setVideoAuth(ServiceHelper.getCdnVideo(user.getVideoAuth()));
		vo.setVip(user.vipValue());
		vo.setSignature(user.getSignature());
		vo.setTalentLevelId(user.getWaiterLevelId());
		vo.setTalentLevelName("");
		vo.setTalentLevelImgUrl("");
		return vo;
	}
	
	@Override
	public UserExtensionVO getUserExtensionVO(long userid) throws Exception {
		UserExtensionBO u = userExtensionAgent.findByUserId(userid);
		if(u == null) {
			return null;
		}
		
		return BeanUtils.copyBean(u, UserExtensionVO.class);
	}

	@Override
	public UserSimpleDto getUserSimpleDto(UserEntity user) throws Exception {
		if(user == null) {
			return null;
		}
		
		UserSimpleDto dto = new UserSimpleDto();
		dto.setNickname(user.getNickname());
		dto.setPhoto(ServiceHelper.getUserSmallPhoto(user.getPhoto()));
		dto.setUserid(user.getId());
		
		return dto;
	}
	
	
	
	/**
	 * 将用户详情的DAO实体转换
	 * @param user - UserBO
	 * @param ue 
	 * @param userInfo - UserInfoBO
	 * @param cashAccount - UserCashAccountEntity
	 * @return UserDto
	 * @throws Exception 
	 */
	private UserDetailDto transferUserDetailDto(UserBO user, UserExtensionBO ue) throws Exception {
		if(user == null) {
			return null;
		}
		UserDetailDto dto = new UserDetailDto();
		dto.setUserid(user.getUserid());
		dto.setNickname(user.getNickname());
		if (Tools.isNotNull(user.getVideoAuth())) {
			dto.setPhoto(ServiceHelper.getUserSmallPhoto(user.getVideoAuth()));
			dto.setVerifyText("审核中");
		}else{
			dto.setPhoto(ServiceHelper.getUserSmallPhoto(user.getPhoto()));
		}
		dto.setBirthday(Tools.getDate(user.getBirthday()));
		dto.setAnchorSignature(user.getSignature());
		dto.setAnchorIntr(user.getIntroduce());
		
		return dto;
	}
	
	
	
	@Override
	public boolean checkBasicInfoComplete(UserBO user) {
		//昵称肯定是存在的
		if(user.getSex() > 0 && user.getBirthday() != null && user.getCityid() > 0 && Tools.isNotNull(user.getSignature())) {
			return true;
		}
		return false;
	}

	@Override
	public boolean checkMaterialComplete(UserExtensionBO userExt) {
		if(userExt != null) {
			if(Tools.isNotNull(userExt.getZodiac()) && userExt.getMarriage() > 0 && Tools.isNotNull(userExt.getJob()) && userExt.getIncome() > 0 &&
					userExt.getStature() > 0 && userExt.getWeight() > 0 && userExt.getSexOpinion() > 0 && userExt.getSpouseOpinion() > 0 &&
					userExt.getMakeFriend() > 0 && Tools.isNotNull(userExt.getTraitPoint())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean checkContactComplete(UserBO user) {
		if(Tools.isNotNull(user.getMobile())) {
			return true;
		}
		return false;
	}
	
	@Override
	public ActionResult authSendCode(String mobile) throws Exception {
		UserBO user = userAgent.findByMobile(mobile);
		if (user!=null ) {
			return ActionResult.fail(ErrorCodeEnum.auth_mobile_exist);
		}
		return sendMessageAgent.sendMobileValidCode(mobile, SendSmsTypeEnum.auth_mobile);
	}
	
	@Override
	public ActionResult modifyMobileSendCode(Long userId, String mobile ) throws Exception {
		UserBO mobileUser = userAgent.findByMobile(mobile);	
		UserBO userNameUser = userAgent.findByUsername(mobile);
		//UserBO user = userAgent.findById(userId);
			if (mobileUser!=null ) {
				if (!userId.equals(mobileUser.getUserid())){
					return ActionResult.fail(ErrorCodeEnum.auth_mobile_exist);
				}
				if (Tools.isNotNull(mobileUser.getMobile())) {
					if (mobile.equals(AESFieldUtils.decrypt(mobileUser.getMobile()))) {
						return ActionResult.fail(ErrorCodeEnum.modify_mobile_same);
					}
				}
			}
			if (userNameUser!=null ) {
				if (!userId.equals(userNameUser.getUserid())){
					return ActionResult.fail(ErrorCodeEnum.auth_mobile_exist);
				}
				if (userNameUser.getUsername().equals(mobile)){
					return ActionResult.fail(ErrorCodeEnum.modify_mobile_same);
				}
			}
		return sendMessageAgent.sendMobileValidCode(mobile, SendSmsTypeEnum.auth_mobile);
	}

	@Override
	public ActionResult authMobileAdd(JSONObject json) throws Exception {
		String mobile  = json.getString("mobile");
		String validCode  = json.getString("validCode");
		String qq = json.getString("qq");
		String weixin = json.getString("weixin");
		Integer contactStatus = json.getInteger("contactStatus");
		if (!sendMessageAgent.checkCode(mobile, validCode)) {
			return ActionResult.fail(AgentErrorCodeEnum.valid_code_error);
		}
		UserBO userBo = userAgent.findByMobile(mobile);
		if (userBo!=null) {
			return ActionResult.fail(ErrorCodeEnum.auth_mobile_exist);
		}
		UserModifyDto user = new UserModifyDto();
		user.setUserid(RequestUtils.getCurrent().getUserid());
		user.setMobile(mobile);
		user.setQq(qq);
		user.setWeixin(weixin);
		user.setContact_status(contactStatus);
		userAgent.updateUser(user);
		return ActionResult.success();
	}
	
	@Override
	public ActionResult modifyMobile(Long userId,JSONObject json) throws Exception {
		String mobile  = json.getString("mobile");
		String validCode  = json.getString("validCode");
		if (!sendMessageAgent.checkCode(mobile, validCode)) {
			return ActionResult.fail(AgentErrorCodeEnum.valid_code_error);
		}
		
		UserBO mobileUser = userAgent.findByMobile(mobile);	
		UserBO userNameUser = userAgent.findByUsername(mobile);
		UserBO userBo = userAgent.findById(userId);
		
		if (mobileUser!=null) {
			if (!userId.equals(mobileUser.getUserid())){
				return ActionResult.fail(ErrorCodeEnum.auth_mobile_exist);
			}
		}
		if (userNameUser!=null) {
			if (!userId.equals(userNameUser.getUserid())){
				return ActionResult.fail(ErrorCodeEnum.auth_mobile_exist);
			}
		}
		
		UserModifyDto user = new UserModifyDto();
		user.setUserid(userId);
		if (Tools.isMobile(userBo.getUsername())) {
			user.setUserName(mobile);
		}
		user.setMobile(mobile);
		userAgent.updateUser(user);
		return ActionResult.success("更换手机号成功");
	}
	

	@Override
	public ActionResult addInform(UserInformDto userInform) throws Exception {
		if (Tools.isNull(userInform)) {
			return ActionResult.fail(ErrorCodeEnum.db_error.getCode(),ErrorCodeEnum.db_error.getDesc());
		}
		RequestHeader requestHeader = RequestUtils.getCurrent().getHeader();
		try {
			UserInformEntity ui = new UserInformEntity();
			ui.setUserid(RequestUtils.getCurrent().getUserid());
			ui.setCreate_time(new Date());
			ui.setInformid(userInform.getInformid());
			ui.setInform_type(userInform.getInformType());
			ui.setInform_info(userInform.getInformInfo());
			ui.setState(0);
			userInformContract.insert(ui);
			
			if(Tools.isNotNull(requestHeader)) {
				if( "shenghe_wxb".equals(requestHeader.getChannel()) || "Huawei_yoyo3".equals(requestHeader.getChannel())) {
					CopyUserInformEntity ui1 = new CopyUserInformEntity();
					ui1.setUserid(RequestUtils.getCurrent().getUserid());
					ui1.setCreate_time(new Date());
					ui1.setInformid(userInform.getInformid());
					ui1.setInform_type(userInform.getInformType());
					ui1.setInform_info(userInform.getInformInfo());
					ui1.setState(0);
					copyUserInformContract.insert(ui1);
				}
			}
			
			return ActionResult.success("举报成功！");
		} catch (Exception e) {
			e.printStackTrace();
			return ActionResult.fail(ErrorCodeEnum.db_error.getCode(),ErrorCodeEnum.db_error.getDesc());
		}
	}

	@Override
	public ActionResult doBlacklist(JSONObject json) throws Exception {
		Long userid = json.getLong("userid");
		if (Tools.isNull(userid)) {
			return ActionResult.fail(ErrorCodeEnum.db_error.getCode(),ErrorCodeEnum.db_error.getDesc());
		} 
		PageModel pageModel = PageModel.getLimitModel(0, 1);
		pageModel.addQuery(Restrictions.eq("userid", RequestUtils.getCurrent().getUserid()));
		pageModel.addQuery(Restrictions.eq("blackid", userid));
		List<UserBlacklistEntity> ubllist = userBlacklistContract.load(pageModel);
		try {
			if (Tools.isNotNull(ubllist)) {//黑名单已有做解除操作
				UserBlacklistEntity ubentity = ubllist.get(0);
				userBlacklistContract.delete(ubentity);
				neteaseAgent.modifyBadRelation(RequestUtils.getCurrent().getUserid(), userid, false);
			}else{
				UserBlacklistEntity ublist = new UserBlacklistEntity();
				ublist.setUserid(RequestUtils.getCurrent().getUserid());
				ublist.setCreate_time(new Date());
				ublist.setBlackid(userid);
				userBlacklistContract.insert(ublist);
				neteaseAgent.modifyBadRelation(RequestUtils.getCurrent().getUserid(), userid, true);
			}
			return ActionResult.success();
		} catch (Exception e) {
			e.printStackTrace();
			return ActionResult.fail(ErrorCodeEnum.db_error.getCode(),ErrorCodeEnum.db_error.getDesc());
		}
	}

	
	@Override
	public ActionResult anchorBlacklist(long userid,Long otherId,List<String> complaintList) throws Exception {
	
		if (Tools.isNull(otherId)) {
			return ActionResult.fail(ErrorCodeEnum.db_error.getCode(),ErrorCodeEnum.db_error.getDesc());
		} 
		PageModel pageModel = PageModel.getLimitModel(0, 1);
		pageModel.addQuery(Restrictions.eq("userid", userid));
		pageModel.addQuery(Restrictions.eq("blackid", otherId));
		List<UserBlacklistEntity> ubllist = userBlacklistContract.load(pageModel);
		try{
			if (Tools.isNotNull(ubllist)) {//黑名单已有做解除操作
				UserBlacklistEntity ubentity = ubllist.get(0);
				ubentity.setEvaluation_text(JsonHelper.toJson(complaintList));
				userBlacklistContract.update(ubentity);
				//userBlacklistContract.delete(ubentity);
				neteaseAgent.modifyBadRelation(RequestUtils.getCurrent().getUserid(), otherId, true);
			}else{
				UserBlacklistEntity ublist = new UserBlacklistEntity();
				ublist.setUserid(RequestUtils.getCurrent().getUserid());
				ublist.setEvaluation_text(JsonHelper.toJson(complaintList));
				ublist.setCreate_time(new Date());
				ublist.setBlackid(otherId);
				userBlacklistContract.insert(ublist);
				neteaseAgent.modifyBadRelation(RequestUtils.getCurrent().getUserid(), otherId, true);
			}
			return ActionResult.success();
		} catch (Exception e) {
			logger.info("拉黑失败",e);
			return ActionResult.fail(ErrorCodeEnum.db_error.getCode(),ErrorCodeEnum.db_error.getDesc());
		}
	}
	
	
	@Override
	public ActionResult getUserHomePage(JSONObject json) throws Exception {
		long userid = json.getLong("userid");
		if (userid<=0) {
			return ActionResult.fail(ErrorCodeEnum.db_error.getCode(),ErrorCodeEnum.db_error.getDesc());
		} 
		UserHomePageOldVO user = getUserHomePageOldVO(userid);
		if(user == null) {
			return ActionResult.fail(ErrorCodeEnum.user_not_found.getCode() , ErrorCodeEnum.user_not_found.getDesc());
		}
		
		return ActionResult.success(user);
	}
	
	private UserHomePageOldVO getUserHomePageOldVO(long userid) throws Exception {
		UserBO user = userAgent.findById(userid);
		UserBO userSelf = userAgent.findById(RequestUtils.getCurrent().getUserid());
		UserExtensionBO ue = userExtensionAgent.findByUserId(userid);
		UserPrivacySecurityEntity ups = userPrivacySecurityContract.findByProperty("userid", RequestUtils.getCurrent().getUserid());
		if(user == null && userSelf == null) {
			return null;
		}
		return transferUserHomePageOldVO(user , ue , userSelf,ups);
	}

	private UserHomePageOldVO transferUserHomePageOldVO(UserBO user, UserExtensionBO ue, UserBO userSelf,UserPrivacySecurityEntity ups) throws Exception {
		if(user == null) {
			return null;
		}
		
		UserHomePageOldVO vo =new UserHomePageOldVO();
		UserHomePageDto dto = new UserHomePageDto();
		dto.setUserid(user.getUserid());
		dto.setIdcard(user.getIdcard());
		dto.setNickname(user.getNickname());
		dto.setPhoto(ServiceHelper.getUserSmallPhoto(user.getPhoto()));
		dto.setPhotoBig(ServiceHelper.getCdnPhoto(user.getPhoto()));
		dto.setSex(user.getSex());
		dto.setBirthday(Tools.getDate(user.getBirthday()));
		dto.setAge(Tools.getAge(user.getBirthday())+"");
		if (user.getCityid()!=0) {
			AreaDto cityArea = appAreaService.getCity(user.getCityid());
			if (cityArea!=null) {
				int cityId = Tools.parseInt(RequestUtils.getCurrent().getHeader().getCityCode());
				AreaDto areaDto = appAreaService.getCityByBaiduCode(Tools.parseInt(RequestUtils.getCurrent().getHeader().getCityCode()));
				String cityName = "";
				if (areaDto != null) {
					cityName = cityId != 0 ? areaDto.getName() : "北京";
				} else {
					cityName = "北京";
				}
				
				dto.setCityName(cityName);
			}
		}
		dto.setSignature(user.getSignature());
		//魅力值
		dto.setCharm(userCharmAgent.getCharmByUserid(user.getUserid())+1);//魅力值
		userCharmAgent.addUserCharm(user.getUserid());//刷新魅力值
		dto.setIsAttention(bAttention(user.getUserid()));
		//判断联系方式是否验证 联系方式是否公开
		if (Tools.isNotNull(user.getContactText())) {
			dto.setIsContactInfo(true);//联系方式已验证
			if (user.getContactStatus()==1) { //联系方式状态：1显示,2不显示',
				dto.setIsContactStatus(true);
				//dto.setMobile(user.getMobile());
				//dto.setQq(user.getQq());
				//dto.setWeixin(user.getWeixin());
			}else{
				dto.setIsContactStatus(false);
			}
		}else{//如果手机号为空 联系状态是否显示无意义
			dto.setIsContactInfo(false);
		}
		//信用保障
		dto.setIsCreditAssu(user.talentVipValue()!=0);
		dto.setIsVip(user.vipValue()!=0);
		dto.setAttentionCount((int) userFriendAgent.findUserFriendCount(user.getUserid()));
		dto.setFansCount((int) userFriendAgent.findFriendUserCount(user.getUserid()));
		if (Tools.isNull(user.getBgPicture())) {
			String bgPictureUrl = userBgPictureAgent.getBgPic();
			dto.setBgImage(ServiceHelper.getCdnPhoto(bgPictureUrl));
			UserModifyDto userModify = new UserModifyDto();
			userModify.setUserid(user.getUserid());
			userModify.setBgPicture(bgPictureUrl);
			userAgent.updateUser(userModify);
		}else{
			dto.setBgImage(ServiceHelper.getCdnPhoto(user.getBgPicture()));
		}
		dto.setBgImage(ServiceHelper.getCdnPhoto(user.getBgPicture()!=null?user.getBgPicture():userBgPictureAgent.getBgPic()));
		//dto.setVideo(ServiceHelper.getCdnVideo(user.getVideoAuth()));
		int canSeeVideo = 0;
		if (userSelf.getVideoAuth()!=null && user.getVideoAuth()!=null) {
			canSeeVideo =1;
		}
		dto.setCanSeeVideo(canSeeVideo);
		dto.setTalentLevelId(user.getWaiterLevelId());
		if (user.getWaiterLevelId()>0 && Tools.isNotNull(ups)) {
			dto.setPrivacy(ups.getTalent_status());
		}else{
			dto.setPrivacy(0);
		}
		//判断视频是否认证
		if (userVideoAuthAgent.ifAuthVideo(user.getUserid())) {
			UserVideoAuthBO uvBO = userVideoAuthAgent.findByUserId(user.getUserid());
			dto.setIsVideoAuth(true);
			dto.setIsAuth(true);
			dto.setVideoAuthUrl(ServiceHelper.getCdnPhoto(uvBO.getVideoAuth()));
		}else{
			dto.setIsVideoAuth(false);
			dto.setIsAuth(false);
		}
		dto.setIsInBlackList(userBlacklistContract.getUserBlackList(RequestUtils.getCurrent().getUserid(), user.getUserid()) != null);
		dto.setHasVideo(Tools.isNotNull(user.getVideoAuth())?1:0);
		List<String> extensionList = new ArrayList<String>();
		if (Tools.isNotNull(ue)) {
			dto.setMakeFriend(MakeFriendEnum.getDescByCode(ue.getMakeFriend()));
			
			if (Tools.isNotNull(ue.getZodiac())) {
				extensionList.add("星座"+SEPARATOR+ue.getZodiac());
			}
			if (ue.getMarriage()>0) {
				extensionList.add("感情状况"+SEPARATOR+MarriageEnum.getDescByCode(ue.getMarriage()));
			}
			if (Tools.isNotNull(ue.getJob())) {
				extensionList.add("职业"+SEPARATOR+ue.getJob());
			}
			if (ue.getIncome()>0) {
				extensionList.add("收入"+SEPARATOR+IncomeEnum.getDescByCode(ue.getIncome()));
			} 
			if (ue.getStature()>0) {
				extensionList.add("身高"+SEPARATOR+ue.getStature()+"cm");
			}
			if (ue.getWeight()>0) {
				extensionList.add("体重"+SEPARATOR+ue.getWeight()+"kg");
			}
			vo.setTraitPoint(ue.getTraitPoint());
		}
		
		
		Map<String, List<String>> map = dynamicAgent.getTopPhoto(user.getUserid());
		List<String> bigPaths = map.get("bigPaths");
		List<String> smallPaths = map.get("smallPaths");
		
		if (Tools.isNotNull(userSelf.getVideoAuth())) {
			vo.setIsAuthOfSelf(true);
		}else{
			vo.setIsAuthOfSelf(false);
		}
		vo.setIsVipOfSelf(userSelf.vipValue()!=0);
		
		vo.setUserInfo(dto);
		vo.setExtensionList(extensionList);
		//vo.setAppointList(getAppointList(user.getUserid()));
		vo.setDynamicList(Helper.replacePath(dynamicAgent.getTopDynamic(user.getUserid()),ServiceHelper.COMMON_150_TAG));
		vo.setPictureList(Helper.replacePath(smallPaths ,ServiceHelper.COMMON_150_TAG));//
		vo.setPictureOriginalList(Helper.replacePath(bigPaths,null));
		vo.setDynamicUrl(Const.getTaDynamic(user.getUserid()));
		vo.setShowVideoChat(user.getAllowVideoChat());
		long balance = 0;
		
		//主播像普通人 发起视频
		if (userSelf.isWaiter() && !user.isWaiter()) {
			//获得普通用户钻石余额
			balance = userDiamondAgent.getDiamondBalance(user.getUserid());
			//拿到自己的价格
			AnchorOnlineEntity anchor = anchorOnlineContract.findByProperty("userid", userSelf.getUserid());
			//vo.setAudioBalanceEnough(1);
			if (Tools.isNotNull(anchor)) {
				vo.setBalanceEnough(anchor.getPrice() * 3 <= balance?1:-1);
			}else{
				vo.setBalanceEnough(-1);
			}
			int onlineStatue = userOnlineStateAgent.userOnlineState(user.getUserid());
			vo.setOnlineStatue(onlineStatue==3?3:4);
		}else{
			//普通用户打给主播/主播打主播 扣发起人钱
			//获得自己钻石数
			balance = userDiamondAgent.getDiamondBalance(userSelf.getUserid());
			//获得对方价格
			AnchorOnlineEntity anchor = anchorOnlineContract.findByProperty("userid", user.getUserid());
			if (Tools.isNotNull(anchor)) {
				vo.setBalanceEnough(anchor.getPrice() * 3 <= balance?1:0);
			}else{
				vo.setBalanceEnough(0);
			}
			//vo.setAudioBalanceEnough(Const.USER_AUDIO_CHAT_DIAMONDS_COST * 3 <= balance?1:0);
			vo.setOnlineStatue(userOnlineStateAgent.userOnlineState(user.getUserid()));
		}
		//提审账号
		//String userName = RequestUtils.getCurrent().getUser().getUsername();
		//if (Const.IOS_TEST_MOBILE_ACCOUNT_MAP.containsKey(userName)) {
		if (confService.testIOS()) {
			vo.setBalanceEnough(1);
		}
		return vo;
	}
	

	/**
	 * 判断是否关注
	 * @param friendid
	 * @return
	 * @throws Exception
	 */
	private boolean bAttention(long friendid) throws Exception{
		UserFriendBO uf = userFriendAgent.findUserFriend(RequestUtils.getCurrent().getUserid(), friendid);
		if (Tools.isNotNull(uf)) {
			return true;
		}
		return false;
		
	}
	
	
	@Override
	public ActionResult getUserHomePageHead(JSONObject json) throws Exception {
		long userid = json.getLong("userid");
		if (userid<=0) {
			return ActionResult.fail(ErrorCodeEnum.db_error.getCode(),ErrorCodeEnum.db_error.getDesc());
		} 
		UserHomePageVO user = getUserHomePageVO(userid);
		if(user == null) {
			return ActionResult.fail(ErrorCodeEnum.user_not_found.getCode() , ErrorCodeEnum.user_not_found.getDesc());
		}
		
		return ActionResult.success(user);
	}
	
	private UserHomePageVO getUserHomePageVO(long userid) throws Exception {
		AnchorOnlineEntity anchor = anchorOnlineContract.findByProperty("userid", userid);
		UserBO user = userAgent.findById(userid);
		UserBO userSelf = userAgent.findById(RequestUtils.getCurrent().getUserid());
		if(user == null && userSelf == null && anchor == null) {
			return null;
		}
		return transferUserHomePageVO(user, userSelf , anchor);
	}

	private UserHomePageVO transferUserHomePageVO(UserBO user, UserBO userSelf,AnchorOnlineEntity anchor) throws Exception {
		if(user == null) {
			return null;
		}
		UserHomePageVO vo =new UserHomePageVO();
		UserBaseInfo userInfo = new UserBaseInfo();
		userInfo.setUserId(user.getUserid());
		userInfo.setNickName(anchor.getNickname());
		userInfo.setAnchorStar(anchor.getStar());
		//处理华为渠道审核期间显示固定价格的处理
		/*
		if(Tools.isNotNull(header) && Tools.isNotNull(header.getChannel()) && header.getChannel().equals("Huawei_AP_DM_YO")) {
			userInfo.setAnchorPrice(5+"钻/分钟");
		}
		*/
		RequestHeader header =RequestUtils.getCurrent().getHeader();
		//String userName = RequestUtils.getCurrent().getUser().getUsername();
		//&& Const.IOS_TEST_MOBILE_ACCOUNT_MAP.containsKey(userName)
		if (header.getVersioncode() >= 59 ) {
			userInfo.setAnchorStatus(4);
		}else{
			userInfo.setAnchorStatus(userOnlineStateAgent.userOnlineState(user.getUserid()));
		}
		userInfo.setAnchorFans(((int) userFriendAgent.findFriendUserCount(user.getUserid())*9));
		//userInfo.setAnchorIntr(anchor.getIntro());
		userInfo.setAnchorIntr("");
		userInfo.setAnchorTypeVideo(anchor.getVideo_type());
		userInfo.setAnchorTypeAudio(anchor.getAudio_type());
		userInfo.setAnchorSignature(anchor.getSignature());
		if (anchor.getTag()>0) {
			AnchorTagEntity anchorTag = anchorTagContract.findById(anchor.getTag());
			if (Tools.isNotNull(anchorTag) && Tools.isNotNull(anchorTag.getPath())) {
				userInfo.setAnchorTag(ServiceHelper.getCdnPhoto(anchorTag.getPath()));
			}
		}
		if (RequestUtils.getCurrent().getHeader().getOs_type()==2) {
			if (getIosStatus()==1) {
				if (anchor.getPrice()>0 && anchor.getVideo_type()==1) {
					userInfo.setAnchorPrice(anchorDynamicPriceAgent.getAnchorDynamicPrice(userSelf.getUserid(), anchor.getPrice())+"钻/分钟");
				}
				if (anchor.getAudio_price()>0 && anchor.getAudio_type()==1) {
					userInfo.setAnchorAudioPrice(anchorDynamicPriceAgent.getAnchorDynamicPrice(userSelf.getUserid(), anchor.getAudio_price())+"钻/分钟");
				}
			}
		}else{
			if (anchor.getPrice()>0 && anchor.getVideo_type()==1) {
				userInfo.setAnchorPrice(anchorDynamicPriceAgent.getAnchorDynamicPrice(userSelf.getUserid(), anchor.getPrice())+"钻/分钟");
			}
			if (anchor.getAudio_price()>0 && anchor.getAudio_type()==1) {
				userInfo.setAnchorAudioPrice(anchorDynamicPriceAgent.getAnchorDynamicPrice(userSelf.getUserid(), anchor.getAudio_price())+"钻/分钟");
			}
		}
		userInfo.setPhoto(ServiceHelper.getCdnPhoto(anchor.getPhoto()));
		vo.setAgeDistance(anchor.getAge()+"岁 | "+anchor.getDistance()+"km");
		vo.setUserInfo(userInfo);
		vo.setAudioText(anchor.getAudio_time()+"s");
		vo.setAudioPath(ServiceHelper.getCdnVideo(anchor.getAudio_path()));
		
		List<String> pictureList = new ArrayList<String>();
		if (RequestUtils.getCurrent().getHeader().getVersioncode()>59&&RequestUtils.getCurrent().getHeader().getOs_type()==1) {
			pictureList.add(ServiceHelper.getCdnPhoto(anchor.getPhoto()));
		}else{
			//获取主播图片
			PageModel pageModel = PageModel.getLimitModel(0, 8);
			pageModel.addQuery(Restrictions.eq("userid", user.getUserid()));
			pageModel.addQuery(Restrictions.eq("state", 1));
			pageModel.addQuery(Restrictions.eq("obscure", 0));
			pageModel.desc("create_time");
			List<UserPhotoResourceEntity> userPhotoList = userPhotoResourceContract.load(pageModel);
			if (Tools.isNotNull(userPhotoList)) {
				for (UserPhotoResourceEntity userPhoto : userPhotoList) {
					pictureList.add(ServiceHelper.getCdnPhoto(userPhoto.getPath()));
				}
			}else{
				pictureList.add(ServiceHelper.getCdnPhoto(anchor.getPhoto()));
			}
		}
		
		vo.setPictureList(pictureList);
		
		//获取用户亲密榜 取前3个
		List<String> friendPhotoList = new ArrayList<String>();
		PageModel pageModel = PageModel.getLimitModel(0, 3);
		pageModel.addLimitField(0, 3);
		pageModel.addQuery(Restrictions.eq("anchor_userid", user.getUserid()));
		pageModel.desc("total_money");
		List<AnchorIntimateRankingsEntity> airList = anchorIntimateRankingsContract.load(pageModel);
		if (Tools.isNotNull(airList)) {
			for (AnchorIntimateRankingsEntity airEntity : airList) {
				UserBO userBO = userAgent.findById(airEntity.getUserid());
				if (userBO != null) {
					friendPhotoList.add(ServiceHelper.getCdnPhoto(userBO.getPhoto()));
				}
			}
		}
		vo.setFriendPhotoList(friendPhotoList);
		vo.setIsAttension(bAttention(userSelf.getUserid(), user.getUserid()));
		pageModel.clearAll();
		pageModel.addLimitField(0, 5);
		pageModel.addQuery(Restrictions.eq("other_id", user.getUserid()));
		pageModel.desc("create_time");
		List<UserChatGiftLogEntity> giftList = userChatGiftLogContract.load(pageModel);
		List<String> giftIconList = new ArrayList<String>();
		if (Tools.isNotNull(giftList)) {
			for (UserChatGiftLogEntity giftLog : giftList) {
				UserChatGiftEntity gift = userChatGiftContract.findById(giftLog.getGift_id());
				if (gift!=null) {
					giftIconList.add(ServiceHelper.getCdnPhoto(gift.getIcon()));
				}
			}
		}
		vo.setGiftIconList(giftIconList);
		/*int reportShow = 0;
		if (channelCheckService.checkChannelReport()) {
			reportShow = 1;
		}else{
			reportShow = ifVchat(user.getUserid(),userSelf.getUserid())?1:0;
		}
		vo.setReportShow(reportShow);*/
		vo.setReportShow(1);
		
		vo.setGuardData(anchorDefendAgent.anchorTop4Defend(user.getUserid()));
		vo.setGuardStatus(anchorDefendAgent.userIsAnchorDefend(userSelf.getUserid(), user.getUserid())?1:0);
		return vo;
	}
	
	private int getIosStatus() throws Exception {
		AppPackageEntity appPackage = appPackageContract.findByProperty("name", RequestUtils.getCurrent().getHeader().getPackageName());
		AppChannelEntity channel = appChannelContract.findByProperty("name", RequestUtils.getCurrent().getHeader().getChannel());
		if (appPackage!=null&&channel!=null) {
			PageModel pageModel = PageModel.getPageModel(0, 1);
			pageModel.addQuery(Restrictions.eq("platform", 2));
			pageModel.addQuery(Restrictions.eq("code", RequestUtils.getCurrent().getHeader().getVersioncode()));
			pageModel.addQuery(Restrictions.eq("channel_id", channel.getId()));
			pageModel.addQuery(Restrictions.eq("package_id", appPackage.getId()));
			pageModel.addQuery(Restrictions.eq("status", 1));
			List<AppVersionEntity> list = appVersionContract.load(pageModel);
			if (Tools.isNotNull(list)) {
				return list.get(0).getIos_status();
			}
		}
		return 0;
	}

	/**
	 * 判断是否聊过天
	 * @param anchorid
	 * @param userid
	 * @return
	 * @throws Exception
	 */
	/*
	private boolean ifVchat(long anchorid, long userid) throws Exception {
		PageModel pageModel = new PageModel();
		pageModel.addQuery(Restrictions.eq("userid", userid));
		pageModel.addQuery(Restrictions.eq("anchorid", anchorid));
		pageModel.addQuery(Restrictions.ge("vchat_duration", 1));
		pageModel.addQuery(Restrictions.in("free_vchart_falg", 0,2));
		if (vchatRoomContract.count(pageModel)>0) {
			return true;
		}
		return false;
	}
	*/
	/**
	 * 判断是否关注
	 * @param friendid
	 * @return
	 * @throws Exception
	 */
	private int bAttention(long selfid,long friendid) throws Exception{
		UserFriendBO uf = userFriendAgent.findUserFriend(selfid, friendid);
		if (Tools.isNotNull(uf)) {
			return 1;
		}
		return 0;
	}

	@Override
	public ActionResult getUserHomePagedesc(long userid, long stamp) throws Exception {
		if (userid<=0) {
			return ActionResult.fail(ErrorCodeEnum.db_error.getCode(),ErrorCodeEnum.db_error.getDesc());
		} 
		AnchorOnlineEntity anchor = anchorOnlineContract.findByProperty("userid", userid);
		UserBO user = userAgent.findById(userid);
		UserBO userSelf = userAgent.findById(RequestUtils.getCurrent().getUserid());
		if(user == null && userSelf == null && anchor == null) {
			return null;
		}
		if(user == null) {
			return ActionResult.fail(ErrorCodeEnum.user_not_found.getCode() , ErrorCodeEnum.user_not_found.getDesc());
		}
		
		UserHomePageVO vo =new UserHomePageVO();
		UserBaseInfo userInfo = new UserBaseInfo();
		List<ImpressionDto> impression = new ArrayList<ImpressionDto>();
		PageModel pageModel = new PageModel();
		if (stamp==0) {
			pageModel.clearAll();
			pageModel.addLimitField(0, 3);
			pageModel.addQuery(Restrictions.eq("userid", userid));
			pageModel.desc("total_num");
			List<AnchorEvaluationStatisticsEntity> aesList = anchorEvaluationStatisticsContract.load(pageModel);
			if (Tools.isNotNull(aesList)) {
				for (AnchorEvaluationStatisticsEntity aesEntity : aesList) {
					AppLabelEntity label = appLabelContract.findById(aesEntity.getLabel_id());
					if (label != null) {
						ImpressionDto dto = new ImpressionDto();
						dto.setDesc(label.getName());
						dto.setColor(label.getColor());
						impression.add(dto);
					}
				}
			}
			vo.setUserImpression(impression);
			userInfo.setAnchorSignature(anchor.getSignature());
			vo.setUserInfo(userInfo);
			if (Tools.isNotNull(anchor.getLabel())) {
				vo.setSelfReportedDesc(JsonHelper.toJsonArray(anchor.getLabel()));
			}
			List<String> personalData = new ArrayList<String>();
			if (Tools.isNotNull(anchor)) {
				String s = null;
				if (userOnlineStateAgent.userOnlineState(user.getUserid())==AnchorOnlineStateEnum.online.getCode()) {
					s=AnchorOnlineStateEnum.online.getDesc();
				}else if(userOnlineStateAgent.userOnlineState(user.getUserid())==AnchorOnlineStateEnum.talking.getCode()){
					s=AnchorOnlineStateEnum.talking.getDesc();
				}else{
					pageModel.clearAll();
					pageModel.addLimitField(0, 1);
					pageModel.desc("create_time");
					pageModel.addQuery(Restrictions.eq("user_id", user.getUserid()));
					List<UserLoginLogEntity> list = userLoginLogContract.load(pageModel);
					if (Tools.isNotNull(list)) {
						long min = (System.currentTimeMillis() - list.get(0).getCreate_time().getTime())/Tools.MINUTE_MILLIS;
						if (min<0) {
							min=5;
						}
						int hour= 0;
						if (min<60) {
							s= min+"分钟前";
						}else{
							hour = (int) (min/60);
							if (hour<24) {
								s=hour+"小时前";
							}else{
								s="1天前";
							}
						}
					}else{
						s="1天前";
					}
				}
				personalData.add("最后登录"+SEPARATOR+s);
				RequestHeader header =RequestUtils.getCurrent().getHeader();
				//String userName = RequestUtils.getCurrent().getUser().getUsername();
				if (header.getOs_type()!=2) {
					//if (!Const.IOS_TEST_MOBILE_ACCOUNT_MAP.containsKey(userName)) {
						if (anchor.getRecv_num() == 0) {
							personalData.add("接听率"+SEPARATOR+"0%");
						}else{
							personalData.add("接听率"+SEPARATOR+(int)((float)anchor.getRecv_num()/anchor.getDial_num()*100)+"%");
						}
					//}
				}
				personalData.add("身高"+SEPARATOR+anchor.getStature());
				personalData.add("体重"+SEPARATOR+anchor.getWeight());
				personalData.add("星座"+SEPARATOR+Tools.getConstellation(user.getBirthday()));
				//AreaDto cityArea = appAreaService.getCityByBaiduCode(Tools.parseInt(anchor.getCityid()+""));
				//personalData.add("城市"+SEPARATOR+(cityArea != null ? cityArea.getName() : ""));
			}
			vo.setPersonalData(personalData);
			vo.setEvaluateCount(anchor.getEvaluation_num());
			vo.setNoEvaluateCount(anchor.getRecv_num()-anchor.getEvaluation_num());
		}
		int pagesize = 10;
		pageModel.clearAll();
		pageModel.setPageSize(pagesize + 1);
		pageModel.addQuery(Restrictions.eq("anchor_userid", userid));
		pageModel.desc("create_time");
		if (stamp != 0) {
			pageModel.addQuery(Restrictions.lt("id", stamp)); // 小于stamp为id
		}
		List<AnchorEvaluationLogEntity> list = anchorEvaluationLogContract.load(pageModel);
		List<UserEvaluationDto> userEvaluationList = new ArrayList<UserEvaluationDto>();
		if (Tools.isNotNull(list)) {
			int i = 0;
			for (AnchorEvaluationLogEntity aelEntity : list) {
				if (Tools.isNotNull(aelEntity)) {
					UserBO userBO = userAgent.findById(aelEntity.getUserid());
					if (userBO!=null) {
						UserEvaluationDto ueDto = new UserEvaluationDto();
						ueDto.setPhoto(ServiceHelper.getCdnPhoto(userBO.getPhoto()));
						ueDto.setNickName(userBO.getNickname());
						ueDto.setEvaluationText(aelEntity.getEvaluation_text()!=null?aelEntity.getEvaluation_text():"");
						if (Tools.isNotNull(aelEntity.getEvaluation_labels())) {
							ueDto.setUserImpression(JsonHelper.toJsonArray(aelEntity.getEvaluation_labels()));
						}
						userEvaluationList.add(ueDto);
						stamp = aelEntity.getId();
						if (++i >= pagesize) {
							break;
						}
					}
				}
			}
		}
		vo.setUserEvaluationList(userEvaluationList);
		List<UserShortVideoDto> userShortVideoList = new ArrayList<UserShortVideoDto>();
		pageModel.clearAll();
		pageModel.addQuery(Restrictions.eq("userid", userid));
		pageModel.addQuery(Restrictions.eq("obscure", 0));//是否私密
		pageModel.addQuery(Restrictions.eq("status", 1));
		pageModel.desc("create_time");
		List<ShortVideoEntity> videoList = shortVideoContract.load(pageModel);
		if (Tools.isNotNull(videoList)) {
			for (ShortVideoEntity entity : videoList) {
				UserShortVideoDto dto = new UserShortVideoDto();
				dto.setVideoPhoto(ServiceHelper.getCdnPhoto(entity.getVideo_photo()));
				dto.setVideoPath(ServiceHelper.getCdnVideo(entity.getVideo_path()));
				userShortVideoList.add(dto);
			}
			vo.setShortVideoList(userShortVideoList);
		}
		vo.setIndividuationImgList(anchorImageAgent.getAnchorImage(userid));
		return ActionResult.success(vo, stamp, (list != null && list.size() > pagesize) ? true : false);
	}
	
	@Override
	public ActionResult getUserImpression(JSONObject json) throws Exception {
		long userid = json.getLong("userid");
		List<ImpressionDto> impression = new ArrayList<ImpressionDto>();
		PageModel pageModel = new PageModel();
		pageModel.addQuery(Restrictions.eq("userid", userid));
		pageModel.desc("total_num");
		List<AnchorEvaluationStatisticsEntity> aesList = anchorEvaluationStatisticsContract.load(pageModel);
		if (Tools.isNotNull(aesList)) {
			for (AnchorEvaluationStatisticsEntity aesEntity : aesList) {
				AppLabelEntity label = appLabelContract.findById(aesEntity.getLabel_id());
				if (label != null) {
					ImpressionDto dto = new ImpressionDto();
					dto.setDesc(label.getName()+" "+aesEntity.getTotal_num());
					dto.setColor(label.getColor());
					impression.add(dto);
				}
			}
		}
		return  ActionResult.success(impression);
	}
	
	@Override
	public ActionResult homePageCloseList(long userid) throws Exception {
		PageModel pageModel = new PageModel();
		pageModel.addLimitField(0, 8);
		pageModel.addQuery(Restrictions.eq("anchor_userid", userid));
		pageModel.desc("total_money");
		List<AnchorIntimateRankingsFakeEntity> fakeList = anchorIntimateRankingsFakeContract.load(pageModel);
		pageModel.clearAll();
		pageModel.addLimitField(0, 2);
		pageModel.addQuery(Restrictions.eq("anchor_userid", userid));
		pageModel.desc("total_money");
		List<AnchorIntimateRankingsEntity> list = anchorIntimateRankingsContract.load(pageModel);
		if (Tools.isNull(fakeList)) {
			if (Tools.isNotNull(list)) {
				fakeList = userGiftAgent.creatIntimateRankingsFakeDate(userid,list.get(0).getTotal_money()*19);
			}else{
				fakeList = userGiftAgent.creatIntimateRankingsFakeDate(userid,0);
			}
		}
		List<AnchorIntimateRankingsDto> rankingsDtoList = new ArrayList<AnchorIntimateRankingsDto>();
		if (Tools.isNotNull(fakeList)) {
			for (AnchorIntimateRankingsFakeEntity fakeEntity : fakeList) {
				AnchorIntimateRankingsDto dto = new AnchorIntimateRankingsDto();
				BeanUtils.copyBean(fakeEntity, dto);
				dto.setType(2);
				rankingsDtoList.add(dto);
			}
		}
		if (Tools.isNotNull(list)) {
			for (AnchorIntimateRankingsEntity entity : list) {
				AnchorIntimateRankingsDto dto = new AnchorIntimateRankingsDto();
				BeanUtils.copyBean(entity, dto);
				dto.setTotal_money(entity.getTotal_money()*19);
				dto.setType(1);
				rankingsDtoList.add(dto);
			}
		}
		List<UserCloseDto> closeList = new ArrayList<UserCloseDto>();
		if (Tools.isNotNull(rankingsDtoList)) {
			//rankingsDtoList.sort(new SortByTotalMoney());
			//Collections.sort(rankingsDtoList, new SortByTotalMoney());
			rankingsDtoList.sort((h1, h2) -> h2.getTotal_money().compareTo(h1.getTotal_money()));
			for (AnchorIntimateRankingsDto airDto : rankingsDtoList) {
				UserBO user = userAgent.findById(airDto.getUserid());
				if (user != null) {
					UserCloseDto dto = new UserCloseDto();
					UserBaseInfo userInfo = new UserBaseInfo();
					userInfo.setUserId(airDto.getUserid());
					userInfo.setPhoto(ServiceHelper.getCdnPhoto(user.getPhoto()));
					userInfo.setNickName(user.getNickname());
					dto.setUserInfo(userInfo);
					dto.setCloseValue(airDto.getTotal_money());
					closeList.add(dto);
				}
			}
		}
		return ActionResult.success(closeList);
	}
	
	@Override
	public ActionResult closeList(long userid, long stamp) throws Exception {
		PageModel pageModel = new PageModel();
		int pagesize = 10;
		if(stamp <= 0) stamp = 1;
		pageModel.addPageField((int) stamp, pagesize + 1);
		pageModel.addQuery(Restrictions.eq("anchor_userid", userid));
		pageModel.desc("total_money");
		List<UserCloseDto> closeList = new ArrayList<UserCloseDto>();
		List<AnchorIntimateRankingsEntity> airList = anchorIntimateRankingsContract.load(pageModel);
		int i = 0;
		if (Tools.isNotNull(airList)) {
			for (AnchorIntimateRankingsEntity airEntity : airList) {
				UserBO user = userAgent.findById(airEntity.getUserid());
				if (user != null) {
					UserCloseDto dto = new UserCloseDto();
					UserBaseInfo userInfo = new UserBaseInfo();
					userInfo.setUserId(airEntity.getUserid());
					userInfo.setPhoto(ServiceHelper.getCdnPhoto(user.getPhoto()));
					userInfo.setNickName(user.getNickname());
					userInfo.setNormalLevel("LV."+user.getDegreeid());
					long balance = userDiamondAgent.getDiamondBalance(airEntity.getUserid());
					userInfo.setBalance(balance>0?"有钻":"");
					dto.setUserInfo(userInfo);
					dto.setCloseValue(airEntity.getTotal_money()*99);
					dto.setStateStr(userOnlineStateAgent.userOnlineState(airEntity.getUserid())==AnchorOnlineStateEnum.online.getCode()?AnchorOnlineStateEnum.online.getDesc():"");
					closeList.add(dto);
					if (++i >= pagesize){
						break;
					}
				}
			}
		}
		return ActionResult.success(closeList, stamp+1, (airList != null && airList.size() > pagesize) ? true : false);
	}

	/*@Override
	public ActionResult getGiftList(long anchorid) throws Exception {
		UserGiftListVO giftVO = new UserGiftListVO();
		PageModel pageModel = PageModel.getPageModel();
		pageModel.addProjection(Projections.sum("gift_count").as("total"));
		pageModel.addQuery(Restrictions.eq("anchorid", anchorid));
		List<Map<String, Object>> maps = userChatGiftStatisticsContract.loadGroupBy(pageModel);
		if (Tools.isNotNull(maps)) {
			Map<String, Object> map = maps.get(0);
			Long giftTotalCount = Tools.parseLong(map.get("total"));
			if (giftTotalCount>0) {
				giftVO.setGiftTotalCountStr("礼物总数："+giftTotalCount);
				pageModel.clearAll();
				pageModel.addLimitField(0, 10);
				pageModel.desc("gift_contribution");
				pageModel.addQuery(Restrictions.eq("anchorid", anchorid));
				List<UserChatGiftStatisticsEntity> list = userChatGiftStatisticsContract.load(pageModel);
				List<UserGiftListDto> giftDtoList = new ArrayList<UserGiftListDto>();
				if (Tools.isNotNull(list)) {
					for (UserChatGiftStatisticsEntity entity : list) {
						UserGiftListDto dto = new UserGiftListDto();
						UserBO user = userAgent.findById(entity.getUserid());
						UserBaseInfo info = new UserBaseInfo();
						info.setPhoto(ServiceHelper.getCdnPhoto(user.getPhoto()));
						info.setNickName(user.getNickname().length()>5?user.getNickname().substring(0,4)+"...":user.getNickname());
						info.setUserId(user.getUserid());
						dto.setUserInfo(info);
						dto.setGiftContribution("礼物贡献："+entity.getGift_contribution()+"钻");
						dto.setGiftCount("礼物数量："+entity.getGift_count());
						pageModel.clearAll();
						pageModel.addLimitField(0, 3);
						pageModel.addQuery(Restrictions.eq("user_id", user.getUserid()));
						pageModel.addQuery(Restrictions.eq("other_id", anchorid));
						pageModel.desc("create_time");
						List<UserChatGiftLogEntity> giftList = userChatGiftLogContract.load(pageModel);
						List<String> imgList = new ArrayList<String>();
						if (Tools.isNotNull(giftList)) {
							for (UserChatGiftLogEntity giftLog : giftList) {
								UserChatGiftEntity giftEntity = userChatGiftContract.findById(giftLog.getGift_id());
								if (giftEntity!=null) {
									imgList.add(ServiceHelper.getCdnPhoto(giftEntity.getIcon()));
								}
							}
						}
						dto.setImgList(imgList);
						giftDtoList.add(dto);
					}
					giftVO.setGiftList(giftDtoList);
				}
			}
		}
		return ActionResult.success(giftVO);
	}*/
	
	@Override
	public ActionResult getGiftList(long anchorid) throws Exception {
		UserGiftListVO giftVO = new UserGiftListVO();
		//先拿假数据
		PageModel pageModel = new PageModel();
		pageModel.desc("gift_contribution");
		pageModel.addLimitField(0, 8);
		pageModel.addQuery(Restrictions.eq("anchorid", anchorid));
		List<UserChatGiftStatisticsFakeEntity> fakeList = userChatGiftStatisticsFakeContract.load(pageModel);
		
		pageModel.clearAll();
		pageModel.addLimitField(0, 2);
		pageModel.desc("gift_contribution");
		pageModel.addQuery(Restrictions.eq("anchorid", anchorid));
		List<UserChatGiftStatisticsEntity> list = userChatGiftStatisticsContract.load(pageModel);
		if (Tools.isNull(fakeList)) {
			if (Tools.isNotNull(list)) {
				fakeList = userGiftAgent.creatGiftFakeDate(anchorid,list.get(0).getGift_contribution()*19,list.get(0).getGift_count());
			}else{
				fakeList = userGiftAgent.creatGiftFakeDate(anchorid,0,0);
			}
		}
		List<UserGiftListStatisticsDto> statisticsDtoList = new ArrayList<UserGiftListStatisticsDto>();
		if (Tools.isNotNull(fakeList)) {
			for (UserChatGiftStatisticsFakeEntity statisticsFakeEntity : fakeList) {
				UserGiftListStatisticsDto dto = new UserGiftListStatisticsDto();
				BeanUtils.copyBean(statisticsFakeEntity, dto);
				dto.setType(2);
				statisticsDtoList.add(dto);
			}
		}
		if (Tools.isNotNull(list)) {
			for (UserChatGiftStatisticsEntity statisticsEntity : list) {
				UserGiftListStatisticsDto dto = new UserGiftListStatisticsDto();
				BeanUtils.copyBean(statisticsEntity, dto);
				dto.setGift_contribution(statisticsEntity.getGift_contribution()*19);
				dto.setType(1);
				statisticsDtoList.add(dto);
			}
		}
		long giftTotalCount = 0;
		pageModel.clearAll();
		pageModel.addProjection(Projections.sum("gift_count").as("total"));
		pageModel.addQuery(Restrictions.eq("anchorid", anchorid));
		List<Map<String, Object>> maps = userChatGiftStatisticsContract.loadGroupBy(pageModel);
		if (Tools.isNotNull(maps)) {
			Map<String, Object> map = maps.get(0);
			if (Tools.isNotNull(map)) {
				giftTotalCount = Tools.parseLong(map.get("total"));
			}
		}
		List<UserGiftListDto> giftDtoList = new ArrayList<UserGiftListDto>();
		if (Tools.isNotNull(statisticsDtoList)) {
			//statisticsDtoList.sort(new SortByGiftContribution());
			//Collections.sort(statisticsDtoList, new SortByGiftContribution());
			statisticsDtoList.sort((h1, h2) -> h2.getGift_contribution().compareTo(h1.getGift_contribution()));
			for (UserGiftListStatisticsDto statisticsDto : statisticsDtoList) {
				UserGiftListDto dto = new UserGiftListDto();
				UserBO user = userAgent.findById(statisticsDto.getUserid());
				UserBaseInfo info = new UserBaseInfo();
				info.setPhoto(ServiceHelper.getCdnPhoto(user.getPhoto()));
				info.setNickName(user.getNickname().length()>5?user.getNickname().substring(0,4)+"...":user.getNickname());
				info.setUserId(user.getUserid());
				dto.setUserInfo(info);
				List<String> imgList = new ArrayList<String>();
				if (statisticsDto.getType()==1) {
					dto.setGiftContribution("贡献值："+statisticsDto.getGift_contribution());
					dto.setGiftCount("");
					pageModel.clearAll();
					pageModel.addLimitField(0, 3);
					pageModel.addQuery(Restrictions.eq("user_id", user.getUserid()));
					pageModel.addQuery(Restrictions.eq("other_id", anchorid));
					pageModel.desc("create_time");
					List<UserChatGiftLogEntity> giftList = userChatGiftLogContract.load(pageModel);
					if (Tools.isNotNull(giftList)) {
						for (UserChatGiftLogEntity giftLog : giftList) {
							UserChatGiftEntity giftEntity = userChatGiftContract.findById(giftLog.getGift_id());
							if (giftEntity!=null) {
								imgList.add(ServiceHelper.getCdnPhoto(giftEntity.getIcon()));
							}
						}
					}
					dto.setImgList(imgList);
				}else{
					dto.setGiftContribution("礼物贡献："+statisticsDto.getGift_contribution());
					dto.setGiftCount("");
					pageModel.clearAll();
					pageModel.addLimitField(0, 3);
					pageModel.addQuery(Restrictions.eq("user_id", user.getUserid()));
					pageModel.addQuery(Restrictions.eq("other_id", anchorid));
					pageModel.desc("create_time");
					List<UserChatGiftLogFakeEntity> giftList = userChatGiftLogFakeContract.load(pageModel);
					if (Tools.isNotNull(giftList)) {
						for (UserChatGiftLogFakeEntity giftLog : giftList) {
							UserChatGiftEntity giftEntity = userChatGiftContract.findById(giftLog.getGift_id());
							if (giftEntity!=null) {
								imgList.add(ServiceHelper.getCdnPhoto(giftEntity.getIcon()));
							}
						}
					}
					dto.setImgList(imgList);
					giftTotalCount+=statisticsDto.getGift_count();
				}
				giftDtoList.add(dto);
			}
			giftVO.setGiftList(giftDtoList);
			giftVO.setGiftTotalCountStr("礼物总数："+giftTotalCount);
		}
		return ActionResult.success(giftVO);
	}
	
	/**
	 * 
	 * @author lipeng
	 * 排序ArrayList
	 */
	@SuppressWarnings("rawtypes")
	class SortByGiftContribution implements Comparator {
        public int compare(Object o1, Object o2) {
        	UserGiftListStatisticsDto s1 = (UserGiftListStatisticsDto) o1;
        	UserGiftListStatisticsDto s2 = (UserGiftListStatisticsDto) o2;
	        if (s1.getGift_contribution() > s2.getGift_contribution()) return -1;
	        return 1;
	    }
    }
	
	
	
	
	/**
	 * 
	 * @author lipeng
	 * 排序ArrayList
	 */
	@SuppressWarnings("rawtypes")
	class SortByTotalMoney implements Comparator {
		public int compare(Object o1, Object o2) {
			AnchorIntimateRankingsDto s1 = (AnchorIntimateRankingsDto) o1;
			AnchorIntimateRankingsDto s2 = (AnchorIntimateRankingsDto) o2;
			if (s1.getTotal_money() > s2.getTotal_money()) return -1;
			return 1;
		}
	}
	
	@Override
	public ActionResult getGlobalBroadcastList(int time) throws Exception {
		if (channelCheckService.checkChannel()) {
			return null;
		}
		long userid = RequestUtils.getCurrent().getUserid();
		if (userid <= 0) {
			return ActionResult.fail(ErrorCodeEnum.parameter_error.getCode(),ErrorCodeEnum.parameter_error.getDesc());
		}
		UserBO user = userAgent.findById(userid);
		PageModel pageModel = PageModel.getLimitModel(0, 15);
		if (user!=null&&user.isWaiter()) {
			List<Long> useridList = anchorRecvUserAgent.userInvisibilityAnchor(userid);
			if (Tools.isNotNull(useridList)) {
				pageModel.addQuery(Restrictions.notin("userid", useridList));
			}
		}
		//3699 深圳 3713 东莞 4149 天津 3697 广州 4146 北京
		if (user.getCityid()==4149||user.getCityid()==4146) {
			pageModel.addQuery(Restrictions.ne("type", GlobalBroadcastTypeEnum.recharge.getCode()));
		}
		pageModel.desc("create_time");
		List<GlobalBroadcastEntity> list = globalBroadcastContract.load(pageModel);
		List<GlobalBroadcastDto> dtoList = new ArrayList<GlobalBroadcastDto>();
		
		boolean isThreeStar = false;
		if (user!=null) {
			if (user.isWaiter()) {
				AnchorOnlineEntity anchor = anchorOnlineContract.findById(userid);
				if (anchor!=null) {
					if (anchor.getStar()==3) {
						isThreeStar = true;
					}
				}
			}
		}
		if (Tools.isNotNull(list)) {
			for (GlobalBroadcastEntity entity : list) {
				if (isThreeStar && entity.getType() == GlobalBroadcastTypeEnum.recharge.getCode()) {
					if (getNumInContent(entity.getContent())>=100) {
						continue;
					}
				}
				GlobalBroadcastDto dto = new GlobalBroadcastDto();
				dto.setId(entity.getId());
				dto.setPhoto(ServiceHelper.getCdnPhoto(entity.getPhoto()));
				dto.setNickName(entity.getNickName().length()>5?entity.getNickName().substring(0,4)+"...":entity.getNickName());
				if (entity.getType() == GlobalBroadcastTypeEnum.recharge.getCode()) {
					if (getNumInContent(entity.getContent())<58) {
						dto.setContent(entity.getContent().replace(getNumInContent(entity.getContent()).toString(), "58"));
					}else{
						dto.setContent(entity.getContent());
					}
				}else{
					dto.setContent(entity.getContent().replaceAll("土豪", ""));
				}
				dto.setType(entity.getType());
				dto.setImg(ServiceHelper.getCdnPhoto(entity.getImg()));
				dtoList.add(dto);
			}
		}
		return ActionResult.success(dtoList);
	}
	
	/**
	 * 生成全局广播假数据
	 * @param userid 
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unused")
	private List<GlobalBroadcastEntity> getFakeGlobalBroadcastData(long userid) throws Exception {
		PageModel pageModel = new PageModel();
		pageModel.addQuery(Restrictions.eq("status", 1));
		pageModel.addQuery(Restrictions.sql("id != "+userid+" order by rand() limit 15"));
		List<UserEntity> userList = userContract.load(pageModel);
		List<GlobalBroadcastEntity> list = new ArrayList<GlobalBroadcastEntity>();
		if (Tools.isNotNull(userList)) {
			for (UserEntity user : userList) {
				GlobalBroadcastEntity entity = new GlobalBroadcastEntity();
				entity.setId(user.getId());
				entity.setNickName("-"+user.getNickname());
				entity.setPhoto(user.getPhoto());
				entity.setContent(getFakeGlobalBroadcastContent());
				list.add(entity);
			}
		}
		return list;
	}

	/**
	 * 随机生成假数据广播内容
	 * 50元  50%
	   100元 20%
	   200元 15%
	   500元 10%
	   1000元 5%
	 * @return
	 */
	private String getFakeGlobalBroadcastContent() {
		int select = random.nextInt(100);
		int money = 100;
		if (50>=select && select>30) {
			money = 200;
		}else if(30 >= select && select > 10) {
			money = 500;
		}else if(10 >= select && select > 3) {
			money = 1000;
		}else if(3 >= select && select > 1) {
			money = 2000;
		}else if(1 == select) {
			money = 5000;
		}
		return "成功充值"+money+"元";
	}

	/**
	 * 获得充值金额
	 * @return
	 */
	private static Integer getNumInContent(String content) {
		String regEx="[^0-9]";  
		Pattern p = Pattern.compile(regEx);  
		Matcher m = p.matcher(content);  
		return Tools.parseInt(m.replaceAll("").trim());
	}
	
	@Override
	public ActionResult getVideoAuth(long userid) throws Exception {
		if(userid <= 0) {
			return ActionResult.fail(ErrorCodeEnum.parameter_error.getCode(), ErrorCodeEnum.parameter_error.getDesc());
		}
		UserVideoAuthVO uvaVo = new UserVideoAuthVO();
		UserVideoAuthBO uvBO = userVideoAuthAgent.findByUserId(userid);
		
		if (Tools.isNotNull(uvBO)) {
			uvaVo.setVideoAuth(ServiceHelper.getCdnPhoto(uvBO.getVideoAuth()));
			uvaVo.setVideoAuthState(uvBO.getStatus());
			uvaVo.setVideoPicture(ServiceHelper.getCdnPhoto(uvBO.getVideoAuthPic()));
		}else{
			uvaVo.setVideoAuthState(2);
		}
		return ActionResult.success(uvaVo);
	}

	@Override
	public ActionResult upImgVideo(HttpServletRequest request) throws Exception {
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		MultipartFile imgVideo = (MultipartFile) multipartRequest.getFile("imgVideo");
		
		//先保存在本地的临时文件夹中
		FileUploadResult uploadResult = Helper.uploadTempFile(imgVideo, "mov,mp4", "user/imgVideo");
		if(uploadResult.getCode() == ErrorCodeEnum.success.getCode()) {
			String videoFilePath = Const.TEMP_FILE_UPLOAD_DIR + uploadResult.getFilePath();
			File videoFile = new File(videoFilePath);
			File uploadPicture = null;
			try {
				if(videoFile.exists()) {
					//上传到又拍云
					logger.info("上传又拍云的文件路径======================" + videoFilePath);
					boolean isUpload = upYunCloudStorage.writeFile(uploadResult.getFilePath(), videoFile, true);
					if(!isUpload){
						//处理上传文件到又拍云出现了错误
						return ActionResult.fail(ErrorCodeEnum.video_upload_error);
					}
					//上传完成后进行视频的截图调用处理
					//配置一个进行上传图片的路径和名称
					String base = Helper.getUploadFilePath("user/imgVideo/snap");
					String savePath = Helper.getUploadFilePath("user/imgVideo/snap") + Helper.getUploadFileName("jpg");
					logger.info("上传又拍云的视频截图路径======================" + savePath);	
					boolean isSnapshot = upYunCloudStorage.mediaSnapshot(uploadResult.getFilePath(), savePath, "00:00:00");
					if(!isSnapshot){
						//处理上传截图失败的错误
						return ActionResult.fail(ErrorCodeEnum.video_upload_error);
					}
					//在自己的服务器上创建对应的截图文件
					//首先在本地创建对应
					File uploaddir= new File(Const.TEMP_FILE_UPLOAD_DIR + base);
					if (!uploaddir.exists()) {
						uploaddir.mkdirs();
					}
					uploadPicture = new File(Const.TEMP_FILE_UPLOAD_DIR + savePath);
					boolean isUploadPicture =  upYunCloudStorage.readFile(savePath, uploadPicture);
					if(!isUploadPicture){
						//处理上传截图失败的错误
						return ActionResult.fail(ErrorCodeEnum.video_upload_error);
					}
					//将视频截图上传到minio服务器
					FileUploadResult fileResult = Helper.uploadFile(new FileInputStream(uploadPicture), savePath);
					if(fileResult.getCode() != ErrorCodeEnum.success.getCode()) {
						return ActionResult.fail(ErrorCodeEnum.video_upload_error.getCode(), "上传视频截图失败");
					}
					//最后将视频上传到minio服务器上
					Helper.uploadFile(new FileInputStream(videoFile), uploadResult.getFilePath());
					
					UserBO userBO = userAgent.findById(RequestUtils.getCurrent().getUserid());
					if (Tools.isNotNull(userBO)) {
						UserModifyDto user = new UserModifyDto();
						user.setUserid(RequestUtils.getCurrent().getUserid());
						user.setVideo_auth(uploadResult.getFilePath());
						user.setVideo_auth_pic(savePath);
						userAgent.updateUser(user);
					}
					UserImgVideoVO uvaVo = new UserImgVideoVO();
					uvaVo.setVideoPath(ServiceHelper.getCdnVideo(uploadResult.getFilePath()));
					uvaVo.setVideoPicture(ServiceHelper.getCdnPhoto(savePath));
					return ActionResult.success(uvaVo);
				} else {
					logger.warn("视频文件不存在,path:" + videoFile.getAbsolutePath());
				}
			} catch (Exception e) {
				logger.error(e.getMessage() , e);
			} finally {
				//删除视频文件
				videoFile.delete();
				//删除截图文件
				if(uploadPicture != null && uploadPicture.exists()) {
					uploadPicture.delete();
				}
			}
		}
		
		return ActionResult.fail(uploadResult.getCode(),uploadResult.getMsg());
	}
	@Override
	public ActionResult upVideoAuth(HttpServletRequest request) throws Exception {
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		MultipartFile videoAuth = (MultipartFile) multipartRequest.getFile("videoAuth");
		
		//先保存在本地的临时文件夹中
		FileUploadResult uploadResult = Helper.uploadTempFile(videoAuth, "mov,mp4", "user/videoAuth");
		if(uploadResult.getCode() == ErrorCodeEnum.success.getCode()) {
			String videoFilePath = Const.TEMP_FILE_UPLOAD_DIR + uploadResult.getFilePath();
			File videoFile = new File(videoFilePath);
			File uploadPicture = null;
			try {
				if(videoFile.exists()) {
					//上传到又拍云
					logger.info("上传又拍云的文件路径======================" + videoFilePath);
					boolean isUpload = upYunCloudStorage.writeFile(uploadResult.getFilePath(), videoFile, true);
					if(!isUpload){
						//处理上传文件到又拍云出现了错误
						return ActionResult.fail(ErrorCodeEnum.video_upload_error);
					}
					//上传完成后进行视频的截图调用处理
					//配置一个进行上传图片的路径和名称
					String base = Helper.getUploadFilePath("user/videoAuth/snap");
					String savePath = Helper.getUploadFilePath("user/videoAuth/snap") + Helper.getUploadFileName("jpg");
					logger.info("上传又拍云的视频截图路径======================" + savePath);	
					boolean isSnapshot = upYunCloudStorage.mediaSnapshot(uploadResult.getFilePath(), savePath, "00:00:00");
					if(!isSnapshot){
						//处理上传截图失败的错误
						return ActionResult.fail(ErrorCodeEnum.video_upload_error);
					}
					//在自己的服务器上创建对应的截图文件
					//首先在本地创建对应
					File uploaddir= new File(Const.TEMP_FILE_UPLOAD_DIR + base);
					if (!uploaddir.exists()) {
						uploaddir.mkdirs();
					}
					uploadPicture = new File(Const.TEMP_FILE_UPLOAD_DIR + savePath);
					boolean isUploadPicture =  upYunCloudStorage.readFile(savePath, uploadPicture);
					if(!isUploadPicture){
						//处理上传截图失败的错误
						return ActionResult.fail(ErrorCodeEnum.video_upload_error);
					}
					//将视频截图上传到minio服务器
					FileUploadResult fileResult = Helper.uploadFile(new FileInputStream(uploadPicture), savePath);
					if(fileResult.getCode() != ErrorCodeEnum.success.getCode()) {
						return ActionResult.fail(ErrorCodeEnum.video_upload_error.getCode(), "上传视频截图失败");
					}
					//最后将视频上传到minio服务器上
					Helper.uploadFile(new FileInputStream(videoFile), uploadResult.getFilePath());
					
					UserVideoAuthBO uvEntity = userVideoAuthAgent.findByUserId(RequestUtils.getCurrent().getUserid());
					if (Tools.isNotNull(uvEntity)) {
						uvEntity.setCreateTime(new Date());
						uvEntity.setStatus(0);
						uvEntity.setVideoAuth(uploadResult.getFilePath());
						uvEntity.setVideoAuthPic(savePath);
						userVideoAuthAgent.update(uvEntity);
					}else{
						UserVideoAuthBO uv = new UserVideoAuthBO();
						uv.setCreateTime(new Date());
						uv.setUserid(RequestUtils.getCurrent().getUserid());
						uv.setStatus(0);
						uv.setVideoAuth(uploadResult.getFilePath());
						uv.setVideoAuthPic(savePath);
						userVideoAuthAgent.insert(uv);
					}
					UserVideoAuthVO uvaVo = new UserVideoAuthVO();
					uvaVo.setVideoAuth(ServiceHelper.getCdnPhoto(uploadResult.getFilePath()));
					uvaVo.setVideoPicture(ServiceHelper.getCdnPhoto(savePath));
					uvaVo.setVideoAuthState(0);
					return ActionResult.success(uvaVo);
				} else {
					logger.warn("视频文件不存在,path:" + videoFile.getAbsolutePath());
				}
			} catch (Exception e) {
				logger.error(e.getMessage() , e);
			} finally {
				//删除视频文件
				videoFile.delete();
				//删除截图文件
				if(uploadPicture != null && uploadPicture.exists()) {
					uploadPicture.delete();
				}
			}
		}
		return ActionResult.fail(uploadResult.getCode(),uploadResult.getMsg());
	}

	@Override
	public ActionResult lookContacts(JSONObject json) throws Exception {
		long otherid = json.getLong("userid");
		if (otherid<=0) {
			return ActionResult.fail(ErrorCodeEnum.db_error.getCode(),ErrorCodeEnum.db_error.getDesc());
		} 
		if (userLookContactsLogAgent.ifLooked(RequestUtils.getCurrent().getUserid(), otherid)) {
			return ActionResult.success(getUserContactsVO(otherid));
		}
		if (userLookContactsLogAgent.getCount(RequestUtils.getCurrent().getUserid())>=Const.LOOK_CONTACTS_LIMIT) {
			return ActionResult.fail(ErrorCodeEnum.look_user_contacts_limit_full.getCode(),ErrorCodeEnum.look_user_contacts_limit_full.getDesc());
		}
		userLookContactsLogAgent.creat(RequestUtils.getCurrent().getUserid(), otherid);
		return ActionResult.success(getUserContactsVO(otherid));
	}

	private Object getUserContactsVO(long otherid) throws Exception {
		UserBO user = userAgent.findById(otherid);
		if(user == null) {
			return ActionResult.fail(ErrorCodeEnum.db_error.getCode(),ErrorCodeEnum.db_error.getDesc());
		}
		UserContactsVO vo = new UserContactsVO();
		vo.setMobile(user.getMobile());
		vo.setQq(user.getQq());
		vo.setWeixin(user.getWeixin());
		return vo;
	}

	@Override
	public ActionResult addUserTypeFeedback(JSONObject json) throws Exception {
		String info = json.getString("info").trim();
		if (info == null) return ActionResult.fail(ErrorCodeEnum.user_type_feedback_null.getCode(),ErrorCodeEnum.user_type_feedback_null.getDesc());
		if(RequestUtils.getCurrent().getUserid() <= 0) {
			return ActionResult.fail(ErrorCodeEnum.parameter_error.getCode(), ErrorCodeEnum.parameter_error.getDesc());
		}
		try {
			UserTypeFeedbackEntity type = new UserTypeFeedbackEntity();
			type.setCreate_time(new Date());
			type.setUserid(RequestUtils.getCurrent().getUserid());
			type.setInfo(info);
			userTypeFeedbackContract.insert(type);
		} catch (Exception e) {
			logger.error(e.getMessage() , e);
		}
		return ActionResult.success("提交成功！");
	}

	@Override
	public ActionResult checkUserRight(long otherId, int type) throws Exception {
		UserBO user = (UserBO)RequestUtils.getCurrent().getUser();
		UserBO other = userAgent.findById(otherId);
		Map<String, Object> data = new HashMap<>();
		DiamondResultDto<Long> resultDto = null;
		if(user.vipValue() > 0 || AgentErrorCodeEnum.success.getCode() == ((resultDto = userDiamondAgent.checkUserRight(user.getUserid(), otherId, Const.USER_CHECK_RIGHT_DIAMOND, type)).getCode())){
			if(1 == type){
				data.put("contact", other.getContactText());
			}else{
				data.put("video", Const.getCdn(other.getVideoAuth()));
			}
			return ActionResult.success(data);
		}else{
			//21010
			data.put("warrText", "付费查看（" + Const.USER_CHECK_RIGHT_DIAMOND +"钻）\n成为会员，无限次查看");
			return ActionResult.fail(resultDto.getCode(),resultDto.getMsg(), data);
		}
	}

	@Override
	public ActionResult checkUserRightWithEnergy(long otherId, int type) throws Exception {
		UserBO user = (UserBO)RequestUtils.getCurrent().getUser();
		UserBO other = userAgent.findById(otherId);
		Map<String, Object> data = new HashMap<>();
		AgentResult result = null;
		if(user.vipValue() > 0 || AgentErrorCodeEnum.success.getCode() == ((result = userEnergyAgent.checkUserRight(user.getUserid(), otherId, Const.USER_CHECK_RIGHT_ENERGY, type)).getCode())){
			if(1 == type){
				data.put("contact", other.getContactText());
			}else{
				data.put("video", Const.getCdn(other.getVideoAuth()));
			}
			return ActionResult.success(data);
		}else{
			//21010
			data.put("warrText", "付费查看（" + Const.USER_CHECK_RIGHT_ENERGY +"能量）\n成为会员，无限次查看");
			return ActionResult.fail(result.getCode(),result.getCodemsg(), data);
		}
	}
	
	@Override
	public ActionResult lookPrivacyPhoto(long photoId, PhotoCheckedLogTypeEnum logType, PlatformEnum osType) throws Exception {
		int os = RequestUtils.getCurrent().getHeader().getOs_type();
		if(osType != null) {
			os = osType.type;
		}
		
		UserBO user = (UserBO)RequestUtils.getCurrent().getUser();
		if(logType == PhotoCheckedLogTypeEnum.PHOTO || logType == PhotoCheckedLogTypeEnum.VIDEO) {
			int diamond = 0;
			long anchorId;
			String picture = null;
			if(logType == PhotoCheckedLogTypeEnum.PHOTO) {
				UserPhotoResourceEntity photoResource = userPhotoResourceContract.findById(photoId);
				if(photoResource == null) {
					return ActionResult.fail(ErrorCodeEnum.db_not_found.getCode(), "未找到指定的相册");
				}
				if(Tools.intValue(photoResource.getObscure()) == 0) {
					return ActionResult.fail(ErrorCodeEnum.db_not_found.getCode(), "该相册非私密相册，不需要付费");
				}
				
				anchorId = photoResource.getUserid();
				picture = ServiceHelper.getCdnPhoto(photoResource.getPath());
				diamond = Const.USER_PHOTO_PRIVACY_DIAMOND;
			} else {
				ShortVideoEntity video = shortVideoContract.findById(photoId);
				if(video == null) {
					return ActionResult.fail(ErrorCodeEnum.db_not_found.getCode(), "未找到指定的视频");
				}
				if(Tools.intValue(video.getObscure()) == 0) {
					return ActionResult.fail(ErrorCodeEnum.db_not_found.getCode(), "该视频非私密视频，不需要付费");
				}
				anchorId = video.getUserid();
				picture = ServiceHelper.getCdnPhoto(video.getVideo_photo());
				diamond = Const.USER_VIDEO_PRIVACY_DIAMOND;
			}
			

			AnchorOnlineEntity anchor = anchorOnlineContract.findByProperty("userid", anchorId);
			if(anchor == null) {
				return ActionResult.fail(ErrorCodeEnum.db_not_found.getCode(), "主播信息未找到");
			}
			
			//首先判断是否有购买过
			if(photoCheckedLogService.getUserPhotoCheckedLog(user.getUserid(), photoId, logType) != null) {
				Map<String, Object> dataMap = Maps.newHashMap();
				dataMap.put("picture", picture);
				dataMap.put("buy", 1);
				return ActionResult.success(dataMap);
			}
			
			//获取提示信息配置
			JSONObject hitJSON = sysConfigAgent.getJSONValue(Const.LOOK_PRIVACY_PHOTO_HIT_INFO);
			if(hitJSON == null) {
				hitJSON = JsonHelper.EMPTY_JSON;
			}
			
			if(os != PlatformEnum.H5.type) {
				//判断是否弹出VIP框
				if (vChatTextYXService.checkShowVIPFragment(user)) {
					return CheckFailVIPReturnDesc(hitJSON);
				}
			}
			
			//进行购买支付
			if(userDiamondAgent.getDiamondBalance(user.getUserid()) >= diamond) {
				DiamondResultDto<Long> result = photoCheckedLogService.pay(user.getUserid(), anchor, photoId, diamond, logType);//.getCode() == 
				if(result.getCode() == AgentErrorCodeEnum.success.getCode()) {
					Map<String, Object> dataMap = Maps.newHashMap();
					dataMap.put("picture", picture);
					dataMap.put("buy", 1);
					return ActionResult.success(dataMap);
				} else if(result.getCode() == AgentErrorCodeEnum.repeate_record.getCode()) {
					return ActionResult.fail(ErrorCodeEnum.state_error.getCode(), "数据异常，请联系客服处理");
				} else if(result.getCode() != AgentErrorCodeEnum.not_enough.getCode()) {
					return ActionResult.fail();
				}
			}
			
			if(os != PlatformEnum.H5.type) {
				boolean ios = false;
				if(os == PlatformEnum.ios.type)
					ios = true;
				
				DlgAndGoPageNew dlgAndGoPage = new DlgAndGoPageNew();
				if(ios) {
					BtnData chargebtn = new BtnData("充值");
					chargebtn.setAndroidPageTag(DlgAndGoPageEnum.iosChargeDiamond.getAndroidPage());
					dlgAndGoPage.setBtnDataList(Arrays.asList(new BtnData("取消"), chargebtn));
				} else {
					//费用不足 充值弹窗
					BtnData chargebtn = new BtnData("充值");
					chargebtn.setAndroidPageTag(DlgAndGoPageEnum.webSingle.getAndroidPage());
					chargebtn.setAndroidPageParam(DlgAndGoPage.getTagParam(Const.WEB_SITE+"/api/income/zuanList","我的钱包"));
					dlgAndGoPage.setBtnDataList(Arrays.asList(chargebtn));
				}
				dlgAndGoPage.setHintInfo(Tools.formatString(hitJSON.getString("not_diamond"), "哎呀，余额不足\n 赶紧去看看"));
				
				Map<String, Object> dataMap = new HashMap<>();
				dataMap.put("dlgAndGoPage", dlgAndGoPage);
				return ActionResult.success(dataMap);
			} else {
				return ActionResult.fail(ErrorCodeEnum.balance_no_enough.getCode(), "充值后即可畅看哦~");
			}
		} else {
			return ActionResult.fail();
		}
	}
	
	public ActionResult CheckFailVIPReturnDesc(JSONObject hitJSON) {
		Map<String, Object> data = new HashMap<>();
		
		DlgAndGoPageNew dlgAndGoPage = new DlgAndGoPageNew();
		//非vip用户
		String hintInfo = Tools.formatString(hitJSON.getString("not_vip"), "开通特色服务享受畅看特权\n额外赠送100元话费");
		int os_type = RequestUtils.getCurrent().getHeader().getOs_type();
		BtnData chargebtn = new BtnData("去开通");
		if (os_type == PlatformEnum.ios.type) {
			chargebtn.setAndroidPageTag(IndexActivityAreaEnum.vipMemeberPage.getAndroidPage());
			chargebtn.setAndroidPageParam(DlgAndGoPage.getTagParam(0));
			dlgAndGoPage.setBtnDataList(Arrays.asList(new BtnData("取消"),chargebtn));
		} else {
			chargebtn.setAndroidPageTag(IndexActivityAreaEnum.webSingle.getAndroidPage());
			chargebtn.setAndroidPageParam(DlgAndGoPage.getTagParam(Const.WEB_SITE+StaticWebUrlEnum.USER_VIP_SERVICE_H5.getPath(),"VIP特色服务"));
			dlgAndGoPage.setBtnDataList(Arrays.asList(chargebtn));
		}
		dlgAndGoPage.setHintInfo(hintInfo);
		
		data.put("dlgAndGoPage", dlgAndGoPage);
		data.put("state", 0);

		data.put("iosNeedRecharge", 0);
		data.put("iosMessage", hintInfo);

		return ActionResult.success(data);
	}
	
	public static void main(String[] args) {
	}

	
	@Override
	public WeixinUserHomePageVO getWxUserHomePage(long userid) throws Exception {
		//访问用户
		long visitUserId = RequestUtils.getCurrent().getUserid();
		
		AnchorOnlineEntity anchor = anchorOnlineContract.findByProperty("userid", userid);
		UserBO user = userAgent.findById(userid);
		if(user == null) {
			return null;
		}
		WeixinUserHomePageVO vo =new WeixinUserHomePageVO();
		vo.setUserid(user.getUserid());
		vo.setNickName(anchor.getNickname());
		vo.setPhoto(ServiceHelper.getCdnPhoto(anchor.getPhoto()));
		vo.setAnchorStar(anchor.getStar());
		vo.setAnchorFans(((int) userFriendAgent.findFriendUserCount(user.getUserid())*9));
		vo.setAnchorSignature(anchor.getSignature());
		if (anchor.getTag()>0) {
			AnchorTagEntity anchorTag = anchorTagContract.findById(anchor.getTag());
			if (Tools.isNotNull(anchorTag) && Tools.isNotNull(anchorTag.getPath())) {
				vo.setAnchorTag(ServiceHelper.getCdnPhoto(anchorTag.getPath()));
			}
		}
		vo.setAgeDistance(anchor.getAge()+"岁 | "+anchor.getDistance()+"km");
		Map<String,String> mapInfo = new HashMap<>();
		
		
		if (Tools.isNotNull(anchor)) {
			String s = null;
			if (userOnlineStateAgent.userOnlineState(user.getUserid())==AnchorOnlineStateEnum.online.getCode()) {
				s=AnchorOnlineStateEnum.online.getDesc();
			}else if(userOnlineStateAgent.userOnlineState(user.getUserid())==AnchorOnlineStateEnum.talking.getCode()){
				s=AnchorOnlineStateEnum.talking.getDesc();
			}else{
				PageModel pageModel = PageModel.getLimitModel(0, 1);
				pageModel.desc("create_time");
				pageModel.addQuery(Restrictions.eq("user_id", user.getUserid()));
				List<UserLoginLogEntity> list = userLoginLogContract.load(pageModel);
				if (Tools.isNotNull(list)) {
					long min = (System.currentTimeMillis() - list.get(0).getCreate_time().getTime())/Tools.MINUTE_MILLIS;
					if (min<0) {
						min=5;
					}
					int hour= 0;
					if (min<60) {
						s= min+"分钟前";
					}else{
						hour = (int) (min/60);
						if (hour<24) {
							s=hour+"小时前";
						}else{
							s="1天前";
						}
					}
				}else{
					s="1天前";
				}
			}
			mapInfo.put("最后登录", s);
			RequestHeader header =RequestUtils.getCurrent().getHeader();
			//String userName = RequestUtils.getCurrent().getUser().getUsername();
			if (header.getOs_type()!=2) {
				//if (!Const.IOS_TEST_MOBILE_ACCOUNT_MAP.containsKey(userName)) {
					if (anchor.getRecv_num() == 0) {
						mapInfo.put("接听率", "0%");
					}else{
						mapInfo.put("接听率",(int)((float)anchor.getRecv_num()/anchor.getDial_num()*100)+"%");
					}
				//}
			}
			mapInfo.put("身高",anchor.getStature());
			mapInfo.put("体重",anchor.getWeight());
			mapInfo.put("星座",Tools.getConstellation(user.getBirthday()));
		}
		vo.setUserInfo(mapInfo);
		PageModel pageModel = PageModel.getLimitModel(0, 10);
		pageModel.addQuery(Restrictions.eq("anchor_userid", userid));
		pageModel.desc("create_time");
		List<AnchorEvaluationLogEntity> list = anchorEvaluationLogContract.load(pageModel);
		List<UserEvaluationDto> userEvaluationList = new ArrayList<UserEvaluationDto>();
		if (Tools.isNotNull(list)) {
			for (AnchorEvaluationLogEntity aelEntity : list) {
				if (Tools.isNotNull(aelEntity)) {
					UserBO userBO = userAgent.findById(aelEntity.getUserid());
					if (userBO!=null) {
						UserEvaluationDto ueDto = new UserEvaluationDto();
						if (userBO.getPhoto()!=null) {
							ueDto.setPhoto(ServiceHelper.getCdnPhoto(userBO.getPhoto()));
						}else{
							ueDto.setPhoto(Const.getDefaultUserFace());
						}
						ueDto.setNickName(userBO.getNickname());
						ueDto.setEvaluationText(aelEntity.getEvaluation_text()!=null?aelEntity.getEvaluation_text():"");
						if (Tools.isNotNull(aelEntity.getEvaluation_labels())) {
							ueDto.setUserImpression(JsonHelper.toJsonArray(aelEntity.getEvaluation_labels()));
						}
						userEvaluationList.add(ueDto);
					}
				}
			}
		}
		vo.setUserEvaluationList(userEvaluationList);
		vo.setShowContact(anchorContactAgent.showAnchorContact(RequestUtils.getCurrent().getUserid(), userid));
		
		vo.setAlbumList(userPhotoService.getUserPhotoList(visitUserId, anchor.getUserid(), 20, null));
		vo.setVideoList(userPhotoService.getUserVideoList(visitUserId, anchor.getUserid(), 20, null));
		
		return vo;
	}

}