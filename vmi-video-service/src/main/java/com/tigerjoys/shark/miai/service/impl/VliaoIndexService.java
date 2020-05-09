package com.tigerjoys.shark.miai.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.shark.miai.common.constant.CommonConst;
import org.shark.miai.common.enums.IndexActivityAreaEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.tigerjoys.nbs.common.ActionResult;
import com.tigerjoys.nbs.common.cache.CacheRedis;
import com.tigerjoys.nbs.common.utils.ExecutorServiceHelper;
import com.tigerjoys.nbs.common.utils.JsonHelper;
import com.tigerjoys.nbs.common.utils.Tools;
import com.tigerjoys.nbs.common.utils.sequence.IdGenerater;
import com.tigerjoys.nbs.mybatis.core.page.PageModel;
import com.tigerjoys.nbs.mybatis.core.page.SortBean;
import com.tigerjoys.nbs.mybatis.core.sql.Projections;
import com.tigerjoys.nbs.mybatis.core.sql.Restrictions;
import com.tigerjoys.nbs.web.context.RequestHeader;
import com.tigerjoys.nbs.web.context.RequestUtils;
import com.tigerjoys.shark.miai.Const;
import com.tigerjoys.shark.miai.agent.IIOSUserSmsAgent;
import com.tigerjoys.shark.miai.agent.INeteaseAgent;
import com.tigerjoys.shark.miai.agent.IRedFlowerAgent;
import com.tigerjoys.shark.miai.agent.ISendMessageAgent;
import com.tigerjoys.shark.miai.agent.ISysConfigAgent;
import com.tigerjoys.shark.miai.agent.ITextAutioMsgAgent;
import com.tigerjoys.shark.miai.agent.IUserAgent;
import com.tigerjoys.shark.miai.agent.IUserDiamondAgent;
import com.tigerjoys.shark.miai.agent.IUserFriendAgent;
import com.tigerjoys.shark.miai.agent.IUserOrdinaryOnlineListAgent;
import com.tigerjoys.shark.miai.agent.IUserPointAgent;
import com.tigerjoys.shark.miai.agent.constant.AgentRedisCacheConst;
import com.tigerjoys.shark.miai.agent.dto.AreaDto;
import com.tigerjoys.shark.miai.agent.dto.SceneMessageDto;
import com.tigerjoys.shark.miai.agent.dto.UserBO;
import com.tigerjoys.shark.miai.agent.dto.transfer.UserModifyDto;
import com.tigerjoys.shark.miai.agent.enums.AgentErrorCodeEnum;
import com.tigerjoys.shark.miai.agent.enums.AnchorStateEnum;
import com.tigerjoys.shark.miai.agent.enums.NeteaseErrorEnum;
import com.tigerjoys.shark.miai.agent.enums.ScenceMessageTypeEnum;
import com.tigerjoys.shark.miai.agent.enums.SendSmsTypeEnum;
import com.tigerjoys.shark.miai.agent.enums.TextAudioMsgEnum;
import com.tigerjoys.shark.miai.agent.enums.UserPointAccountLogTypeEnum;
import com.tigerjoys.shark.miai.agent.service.IAppAreaService;
import com.tigerjoys.shark.miai.agent.service.IValidCodeService;
import com.tigerjoys.shark.miai.dto.index.GotoDataItem;
import com.tigerjoys.shark.miai.dto.service.AhchorShortVideoDto;
import com.tigerjoys.shark.miai.dto.service.AnchorCreateDto;
import com.tigerjoys.shark.miai.dto.service.AnchorListVO;
import com.tigerjoys.shark.miai.dto.service.AnchorVideoWatchDto;
import com.tigerjoys.shark.miai.dto.service.BannerBData;
import com.tigerjoys.shark.miai.dto.service.HotAnchorDto;
import com.tigerjoys.shark.miai.dto.service.RecommendTalentListVO;
import com.tigerjoys.shark.miai.dto.service.UserBaseInfo;
import com.tigerjoys.shark.miai.enums.ErrorCodeEnum;
import com.tigerjoys.shark.miai.inter.contract.IAnchorHotContract;
import com.tigerjoys.shark.miai.inter.contract.IAnchorHotUserContract;
import com.tigerjoys.shark.miai.inter.contract.IAnchorOnlineContract;
import com.tigerjoys.shark.miai.inter.contract.IAppDailScenceContract;
import com.tigerjoys.shark.miai.inter.contract.IAppShamDailContract;
import com.tigerjoys.shark.miai.inter.contract.IAppStartMsgUserContract;
import com.tigerjoys.shark.miai.inter.contract.IAppStartSendMsgContract;
import com.tigerjoys.shark.miai.inter.contract.IBannerContract;
import com.tigerjoys.shark.miai.inter.contract.IFreeVideoChatExperienceContract;
import com.tigerjoys.shark.miai.inter.contract.IRechargeOrderContract;
import com.tigerjoys.shark.miai.inter.contract.IShortVideoContract;
import com.tigerjoys.shark.miai.inter.contract.ISysConfigContract;
import com.tigerjoys.shark.miai.inter.contract.IUserBlacklistContract;
import com.tigerjoys.shark.miai.inter.contract.IUserFirstRechargeLogContract;
import com.tigerjoys.shark.miai.inter.contract.IUserPayActionContract;
import com.tigerjoys.shark.miai.inter.contract.IUserPaySendDiamondContract;
import com.tigerjoys.shark.miai.inter.contract.IUserTextChatHistoryContract;
import com.tigerjoys.shark.miai.inter.contract.IUserTextChatInfoLogContract;
import com.tigerjoys.shark.miai.inter.contract.IVchatRoomContract;
import com.tigerjoys.shark.miai.inter.entity.AnchorHotEntity;
import com.tigerjoys.shark.miai.inter.entity.AnchorHotUserEntity;
import com.tigerjoys.shark.miai.inter.entity.AnchorOnlineEntity;
import com.tigerjoys.shark.miai.inter.entity.AppDailScenceEntity;
import com.tigerjoys.shark.miai.inter.entity.AppShamDailEntity;
import com.tigerjoys.shark.miai.inter.entity.AppStartMsgUserEntity;
import com.tigerjoys.shark.miai.inter.entity.AppStartSendMsgEntity;
import com.tigerjoys.shark.miai.inter.entity.BannerEntity;
import com.tigerjoys.shark.miai.inter.entity.ShortVideoEntity;
import com.tigerjoys.shark.miai.inter.entity.SysConfigEntity;
import com.tigerjoys.shark.miai.inter.entity.UserAudioChatInfoLogEntity;
import com.tigerjoys.shark.miai.inter.entity.UserBlacklistEntity;
import com.tigerjoys.shark.miai.inter.entity.UserTextChatInfoLogEntity;
import com.tigerjoys.shark.miai.service.IChannelCheckService;
import com.tigerjoys.shark.miai.service.IConfService;
import com.tigerjoys.shark.miai.service.IMsgSceneService;
import com.tigerjoys.shark.miai.service.IVliaoIndexService;
import com.tigerjoys.shark.miai.utils.ServiceHelper;

import redis.clients.jedis.Tuple;

/**
 * 处理测试版首页数据
 * @author shiming
 *
 */
@Service
public class VliaoIndexService implements IVliaoIndexService {
	
	private static final Logger logger = LoggerFactory.getLogger(VliaoIndexService.class);
	
	//private LoadingCache<Integer, List<UserBaseInfo>> cache;
	@Autowired
	private IAppAreaService appAreaService;
	
	@Autowired
	private IAnchorOnlineContract anchorOnlineContract;
	
	@Autowired
	private ISysConfigAgent sysConfigAgent;
	
	@Autowired
	private IBannerContract bannerContract;
	
	@Autowired
	private IUserAgent userAgent;
	
	@Autowired
	private IUserBlacklistContract userBlacklistContract;
	
	@Autowired
	private IUserDiamondAgent userDiamondAgent;
	
	@Autowired
	private IShortVideoContract shortVideoContract;
	
	@Autowired
	@Qualifier(AgentRedisCacheConst.REDIS_USER_ONLINE_LIST_CACHE)
	private CacheRedis anchorOnlineCacheRedis;
	
	@Autowired
	private ISendMessageAgent sendMessageAgent;
	
	@Autowired
	private IConfService confService;
	
	@Autowired
	private IAppStartMsgUserContract appStartMsgUserContract;
	
	@Autowired
	private IAppStartSendMsgContract appStartSendMsgContract;
	
	@Autowired
	private IAnchorHotContract anchorHotContract;
	
	@Autowired
	private IAnchorHotUserContract anchorHotUserContract;
	
	//第三方登录服务
	@Autowired
	private IValidCodeService validCodeService;

	@Autowired
	private IRedFlowerAgent redFlowerAgent;
	
	@Autowired
	private IIOSUserSmsAgent iOSUserSmsAgent;
	
	@Autowired
	private ISysConfigContract sysConfigContract;
	
	/**
	 * 不允许连续符号的出现
	 */
	private final Pattern symbolPattern = Pattern.compile("[_-]{2,}", Pattern.CASE_INSENSITIVE);
	
	//假视频来电对应的视频
	private LoadingCache<Integer, List<ShortVideoEntity>> anchorVideos;
	
	//ios审核需要展示的数据
	private LoadingCache<Integer, List<AnchorOnlineEntity>> applyAnchor;
	
	//ios审核需要展示的数据
	//private LoadingCache<Integer, List<AnchorOnlineEntity>> iosAnchor;
	
	//存储对应的发送消息的用户id
	private LoadingCache<Integer, List<Long>> userids;
	
	//存储消息内容
	List<String> msg = new ArrayList<>();

	//ios审核需要展示的数据
	private LoadingCache<Integer, List<AnchorOnlineEntity>> ios2Anchor;

	@Autowired
	private INeteaseAgent neteaseAgent;
	
	@Autowired
	private IChannelCheckService channelCheckService;
	
	@Autowired
	private IUserFriendAgent userFriendAgent;
	
	@Autowired
	private IUserPointAgent userPointAgent;
	
	@Autowired
	private IRechargeOrderContract rechargeOrderContract;
	
	@Autowired
	private IUserPaySendDiamondContract userPaySendDiamondContract;
	
	@Autowired
	private IUserPayActionContract userPayActionContract;
	
	@Autowired
	private IMsgSceneService msgSceneService;
	
	@Autowired
	private IAppShamDailContract appShamDailContract;
	
	@Autowired
	private IVchatRoomContract vchatRoomContract;
	
	@Autowired
	private IFreeVideoChatExperienceContract freeVideoChatExperienceContract;
	
	@Autowired
	private IUserFirstRechargeLogContract userFirstRechargeLogContract;
	
	@Autowired
	private IUserTextChatHistoryContract userTextChatHistoryContract;
	
	@Autowired
	private IUserOrdinaryOnlineListAgent userOrdinaryOnlineListAgent;
	
	@Autowired
	private IAppDailScenceContract appDailScenceContract;
	
	List<List<SceneMessageDto>> data = null;
	
	@Autowired
	private ITextAutioMsgAgent textAutioMsgAgent;
	
	@Autowired
	private IUserTextChatInfoLogContract userTextChatInfoLogContract;

	
	@PostConstruct
	public void init() {
		
		//初始化对应的消息场景
		List<SceneMessageDto> scene = null;
		data = new ArrayList<List<SceneMessageDto>>();
		
		//场景1
		scene = new ArrayList<>();
		SceneMessageDto dto = new SceneMessageDto();
		dto.setContent("现在疫情很严重呀");
		dto.setType(ScenceMessageTypeEnum.text.getCode());
		scene.add(dto);
		
		data.add(scene);	
		
		//场景2
		scene = new ArrayList<>();
		dto = new SceneMessageDto();
		dto.setContent("这软件怎么用呀，刚来这里还不会玩，是来视频的吗？");
		dto.setType(ScenceMessageTypeEnum.text.getCode());
		scene.add(dto);
		data.add(scene);	
		
		//场景3
		scene = new ArrayList<>();
		dto = new SceneMessageDto();
		dto.setContent("你好，有时间认识下吗？");
		dto.setType(ScenceMessageTypeEnum.text.getCode());
		scene.add(dto);
		data.add(scene);	
		
		//场景4
		scene = new ArrayList<>();
		dto = new SceneMessageDto();
		dto.setContent("能陪我视频一会吗？");
		dto.setType(ScenceMessageTypeEnum.text.getCode());
		scene.add(dto);
		data.add(scene);	
		
		//场景5
		scene = new ArrayList<>();
		dto = new SceneMessageDto();
		dto.setContent("你好，你玩这个多久了呀？");
		dto.setType(ScenceMessageTypeEnum.text.getCode());
		scene.add(dto);
		data.add(scene);
		
		//场景6
		scene = new ArrayList<>();
		dto = new SceneMessageDto();
		dto.setContent("现在疫情是不是快结束了");
		dto.setType(ScenceMessageTypeEnum.text.getCode());
		scene.add(dto);
		data.add(scene);	

		//场景7
		scene = new ArrayList<>();
		dto = new SceneMessageDto();
		dto.setContent("有空陪我聊聊天吗？");
		dto.setType(ScenceMessageTypeEnum.text.getCode());
		scene.add(dto);
		data.add(scene);
		
		//场景8
		scene = new ArrayList<>();
		dto = new SceneMessageDto();
		dto.setContent("你好");
		dto.setType(ScenceMessageTypeEnum.text.getCode());
		scene.add(dto);
		data.add(scene);

		//场景9
		scene = new ArrayList<>();
		dto = new SceneMessageDto();
		dto.setContent("帅哥，在吗？");
		dto.setType(ScenceMessageTypeEnum.text.getCode());
		scene.add(dto);
		data.add(scene);
		
		//场景10
		scene = new ArrayList<>();
		dto = new SceneMessageDto();
		dto.setContent("你好，和你聊聊，实在没事做");
		dto.setType(ScenceMessageTypeEnum.text.getCode());
		scene.add(dto);
		data.add(scene);
		
		//场景11
		scene = new ArrayList<>();
		dto = new SceneMessageDto();
		dto.setContent("小哥哥，在吗？");
		dto.setType(ScenceMessageTypeEnum.text.getCode());
		scene.add(dto);
		data.add(scene);
		
		//场景12
		scene = new ArrayList<>();
		dto = new SceneMessageDto();
		dto.setContent("找我有好看的哦~");
		dto.setType(ScenceMessageTypeEnum.text.getCode());
		scene.add(dto);
		data.add(scene);
		
		//场景13
		scene = new ArrayList<>();
		dto = new SceneMessageDto();
		dto.setContent("你现在一个人吗？");
		dto.setType(ScenceMessageTypeEnum.text.getCode());
		scene.add(dto);
		data.add(scene);
		
		//场景14
		scene = new ArrayList<>();
		dto = new SceneMessageDto();
		dto.setContent("你好呀");
		dto.setType(ScenceMessageTypeEnum.text.getCode());
		scene.add(dto);
		data.add(scene);
		
		//场景15
		scene = new ArrayList<>();
		dto = new SceneMessageDto();
		dto.setContent("在吗？");
		dto.setType(ScenceMessageTypeEnum.text.getCode());
		scene.add(dto);
		data.add(scene);
		
		msg.add("可以和你视频聊天吗，小哥哥，我对你很有眼缘的呀");
		msg.add("小哥哥，喜欢我这样的萌萌的性感的么？");
		msg.add("你好，可以认识一下吗？");
		msg.add("哥哥，我想和你一对一聊天哦");
		msg.add("帅锅，在干嘛那？");
		msg.add("哥哥你好啊，我们认识下啊");
		msg.add("有没有时间聊一聊？");
		msg.add("嗨，在干什么呢？");
		msg.add("好无聊呀~");
		msg.add("你好，哪里人呀");
		msg.add("在吗？能和你聊一会天吗？");
		msg.add("想聊聊吗？一个人不寂寞吗？");
		msg.add("看你挺好的，咱们聊聊吧~");
		msg.add("你想要的我都有");
		msg.add("平时在家里都做些什么呢?");
		msg.add("你好，可以认识一下你吗？");
		msg.add("今晚一个人吗？要不视频聊聊天啊");
		msg.add("好寂寞呀，可以陪我聊聊吗？");
		msg.add("今晚就我一个人哦~聊点刺激的~");
		msg.add("想不想聊些刺激的话题呢？");
		msg.add("来视频呀");
		msg.add("我想你陪我聊会儿天可以吗");
		msg.add("就你了，今天陪我聊会吧，聊什么都行。");
		msg.add("最近挺烦的，能够讲个笑话给我听吗？");
		msg.add("在吗？你在哪儿？看在不在同一个城市？");
		msg.add("要是无聊的话可以找我聊天的。");
		msg.add("好无聊呀，能陪我聊聊天吗？");
		msg.add("想看看我的身材吗？咱们视频聊天吧");
		msg.add("你是哪个城市的，咱们认识一下。");
		msg.add("每天都这样，无聊死了。能和我聊一聊吗？");
		msg.add("聊你想聊的，看你想看的。");
		msg.add("哥哥，我想和你一对一聊天哦");
		msg.add("哥哥你好啊，我们认识下啊");
		msg.add("嗨，在干什么呢？");
		msg.add("你为什么不找我聊天啊，小哥哥，我对你很有眼缘的呀");
		msg.add("好无聊呀，能陪我聊聊天吗？");
		msg.add("好无聊呀,想找个人聊聊天");
		msg.add("好无聊呀~");
		msg.add("今晚就我一个人哦~聊点刺激的~");
		msg.add("今晚一个人吗？要不视频聊聊天啊");
		msg.add("就你了，今天陪我聊会吧，聊什么都行。");
		msg.add("陪我聊会儿天吧");
		msg.add("看你挺好的，咱们聊聊吧~");
		msg.add("在吗？能和你聊一会天吗？");
		msg.add("亲爱的来个视频吧");
		msg.add("最近挺烦的，能够讲个笑话给我听吗？");
		msg.add("小哥哥可以和你视频聊聊天吗？");
		msg.add("好无聊啊，可以开视频陪我聊聊天吗？");
		msg.add("缘分让我们相遇，可以看看小哥哥吗？");
		msg.add("小哥哥是哪里人啊，聊聊天可以吗？");
		msg.add("想跟帅哥视频聊聊天。");
		
		/*
		cache = CacheBuilder.newBuilder().maximumSize(2500).expireAfterWrite(2, TimeUnit.HOURS)
				.build(new CacheLoader<Integer, List<UserBaseInfo>> (){

					@Override
					public List<UserBaseInfo> load(Integer num) throws Exception {
						PageModel pageModel = new PageModel();
						//获取不是机器人的数据
						pageModel.addQuery(Restrictions.eq("flag", 0));
						//获取处于正常状态的主播
						pageModel.addQuery(Restrictions.eq("state", 1));
						List<AnchorOnlineEntity> anchors = anchorOnlineContract.load(pageModel);
						List<UserBaseInfo> list = null;
						if(Tools.isNotNull(anchors)) {
							Collections.shuffle(anchors);
							list = new ArrayList<>();
							for (AnchorOnlineEntity anchor : anchors) {
								UserBaseInfo user  = new UserBaseInfo();
								user.setUserId(anchor.getUserid());
								user.setNickName(anchor.getNickname());
								user.setPhoto(ServiceHelper.getUserSmallPhoto(anchor.getPhoto()));
								user.setAnchorIntr(anchor.getIntro());
								list.add(user);
							}
						}
						return Collections.unmodifiableList(list);
					}
				});
		*/
		
		anchorVideos = CacheBuilder.newBuilder().maximumSize(500).expireAfterWrite(3, TimeUnit.HOURS)
				.build(new CacheLoader<Integer, List<ShortVideoEntity>> (){

					@Override
					public List<ShortVideoEntity> load(Integer num) throws Exception {
						PageModel pageModel = PageModel.getLimitModel(0, 100);
						pageModel.addQuery(Restrictions.eq("status", 1));
						pageModel.desc("create_time");
						List<ShortVideoEntity> videos = shortVideoContract.load(pageModel);
						if(Tools.isNotNull(videos)) {
							Collections.shuffle(videos);
						}
						return Collections.unmodifiableList(videos);
					}
				});
		
		applyAnchor = CacheBuilder.newBuilder().maximumSize(500).expireAfterWrite(3, TimeUnit.HOURS)
				.build(new CacheLoader<Integer, List<AnchorOnlineEntity>> (){

					@Override
					public List<AnchorOnlineEntity> load(Integer num) throws Exception {
						PageModel pageModel = PageModel.getPageModel();
						pageModel.addQuery(Restrictions.eq("state", -9));
						pageModel.addQuery(Restrictions.eq("flag", -1));
						pageModel.addSort("online", SortBean.ORDER_DESC);
						pageModel.addSort("audit_time", SortBean.ORDER_DESC);
						List<AnchorOnlineEntity> anchors = anchorOnlineContract.load(pageModel);
						return Collections.unmodifiableList(anchors);
					}
				});
		
		/*
		iosAnchor = CacheBuilder.newBuilder().maximumSize(500).expireAfterWrite(3, TimeUnit.HOURS)
				.build(new CacheLoader<Integer, List<AnchorOnlineEntity>> (){

					@Override
					public List<AnchorOnlineEntity> load(Integer num) throws Exception {
						//使用对应的真主播发送消息
						PageModel pageModel = PageModel.getPageModel();
						pageModel.addQuery(Restrictions.eq("state", 1));
						pageModel.addQuery(Restrictions.eq("flag", 0));
						pageModel.addSort("online", SortBean.ORDER_DESC);
						pageModel.addSort("audit_time", SortBean.ORDER_DESC);
						List<AnchorOnlineEntity> anchors = anchorOnlineContract.load(pageModel);
						return Collections.unmodifiableList(anchors);
					}
				});
		*/
		
		userids = CacheBuilder.newBuilder().maximumSize(500).expireAfterWrite(3, TimeUnit.HOURS)
				.build(new CacheLoader<Integer, List<Long>> (){

					@Override
					public List<Long> load(Integer num) throws Exception {
						PageModel pageModel = PageModel.getPageModel();
						pageModel.addQuery(Restrictions.eq("state", 1));
						pageModel.addQuery(Restrictions.eq("flag", 0));
						pageModel.addQuery(Restrictions.ne("userid", 133916870360236288L));
						pageModel.addSort("online", SortBean.ORDER_DESC);
						pageModel.addSort("audit_time", SortBean.ORDER_DESC);
						List<AnchorOnlineEntity> anchors = anchorOnlineContract.load(pageModel);
						List<Long> ids = null;
						if(Tools.isNotNull(anchors)) {
							ids = new ArrayList<>();
							for (AnchorOnlineEntity anchor : anchors) {
								ids.add(anchor.getUserid());
							}
						}
						return Collections.unmodifiableList(ids);
					}
				});
		ios2Anchor = CacheBuilder.newBuilder().maximumSize(500).expireAfterWrite(3, TimeUnit.HOURS)
				.build(new CacheLoader<Integer, List<AnchorOnlineEntity>> (){

					@Override
					public List<AnchorOnlineEntity> load(Integer num) throws Exception {
						PageModel pageModel = PageModel.getPageModel();
						pageModel.addQuery(Restrictions.eq("state", -9));
						pageModel.addQuery(Restrictions.eq("flag", 1));
						List<AnchorOnlineEntity> anchors = anchorOnlineContract.load(pageModel);
						if(Tools.isNotNull(anchors)) {
							Collections.shuffle(anchors);
						}
						return Collections.unmodifiableList(anchors);
					}
				});
	}
	
	@Override
	public ActionResult searchAnchor(String body) throws Exception {
		RequestHeader header =RequestUtils.getCurrent().getHeader();
		//用于标识是否是特殊的密聊包
		int app = 0;
		if(Tools.isNotNull(header)) {
			String packageName = header.getPackageName();
			if("com.tjhj.miliao".equals(packageName)) {
				app = 1;
			}
		}
		boolean random = true;
		boolean flag = false;
		JSONObject obj = JsonHelper.toJsonObject(body);
		String word = null;
		if(Tools.isNotNull(obj)) {
			word = obj.getString("word");
			if(Tools.isNotNull(word)) {
				random = false;
			}
		}
		//确定当前是否处于审核状态
		flag = channelCheckService.checkChannel();
		List<UserBaseInfo> list = null;
		List<AnchorOnlineEntity> anchors = null;
		//进一步确认是否是进行随机返回主播
		if(random) {
			PageModel pageModel = PageModel.getPageModel();
			if(flag) {
				pageModel.addQuery(Restrictions.eq("state", -9));
				pageModel.addQuery(Restrictions.sql("flag=3 order by rand() limit 5"));
			} else {
				if(app == 1) {
					pageModel.addQuery(Restrictions.in("app", 0,1));
				} else {
					pageModel.addQuery(Restrictions.in("app", 0,2));
				}
				pageModel.addQuery(Restrictions.eq("state", 1));
				pageModel.addQuery(Restrictions.eq("online", 3));
				pageModel.addQuery(Restrictions.sql("flag=0 order by rand() limit 5"));
			}
			anchors = anchorOnlineContract.load(pageModel);
		} else {
			//首先对给定的关键字进行去除空格操作处理
			PageModel pageModel = PageModel.getPageModel();
			pageModel.addLimitField(0, 20);
			if(flag) {
				pageModel.addQuery(Restrictions.eq("state", -9));
				pageModel.addQuery(Restrictions.eq("flag", 3));
			} else {
				if(app == 1) {
					pageModel.addQuery(Restrictions.in("app", 0,1));
				} else {
					pageModel.addQuery(Restrictions.in("app", 0,2));
				}
				pageModel.addQuery(Restrictions.eq("state", 1));
			}
			pageModel.addQuery(Restrictions.like("nickname", word));
			pageModel.addQuery(Restrictions.notin("userid", iOSUserSmsAgent.getUserIdList().keySet()));
			pageModel.desc("online");
			anchors = anchorOnlineContract.load(pageModel);
		}
		if(Tools.isNotNull(anchors) && anchors.size() > 0) {
			list = new ArrayList<>(anchors.size());
			for (AnchorOnlineEntity anchor : anchors) {
				UserBaseInfo user  = new UserBaseInfo();
				user.setUserId(anchor.getUserid());
				user.setNickName(anchor.getNickname());
				user.setPhoto(ServiceHelper.getUserSmallPhoto(anchor.getPhoto()));
				user.setAnchorIntr(anchor.getIntro());
				list.add(user);
			}
		}
		//拼装返回数据
		Map<String, Object> data = new HashMap<>();
		if(random) {
			data.put("anchors", list);
		} else {
			data.put("word", word);
			data.put("anchors", list);
		}
		return ActionResult.success(data);
	}

	/*
	@Override
	public ActionResult searchAnchor(String body) throws Exception {
		boolean random = true;
		JSONObject obj = JsonHelper.toJsonObject(body);
		String word = null;
		if(Tools.isNotNull(obj)) {
			word = obj.getString("word");
			if(Tools.isNotNull(word)) {
				random = false;
			}
		}
		RequestHeader header =RequestUtils.getCurrent().getHeader();
		String userName = RequestUtils.getCurrent().getUser().getUsername();
		//处理新版本中iso的数据  版本34 
		if(Tools.isNotNull(header) && header.getVersioncode() >= 34) {
			boolean flag = false;
			//首先检测是否是提审账号 
			if (Const.IOS_TEST_MOBILE_ACCOUNT_MAP.containsKey(userName)) {
				flag = true;
			}
			//然后检测当前后台配置的开关是否是真实
			if(!flag && confService.testIOS()) {
				flag = true;
			}
			
			if(header.getVersioncode() == 35) {
				flag = false;
			}
			
			if(flag) {
				List<UserBaseInfo> list = null;
				List<AnchorOnlineEntity> anchors = iosAnchor.get(1);
				if(Tools.isNotNull(anchors) && anchors.size() > 0) {
					list = new ArrayList<>();
					int index = getRandomNumber(0, anchors.size()-4);
					for(int i=0; i<3; i++) {
						AnchorOnlineEntity anchor = anchors.get(index++);
						if(Tools.isNotNull(anchor)) {
							UserBaseInfo user  = new UserBaseInfo();
							user.setUserId(anchor.getUserid());
							user.setNickName(anchor.getNickname());
							user.setPhoto(ServiceHelper.getUserSmallPhoto(anchor.getPhoto()));
							user.setAnchorIntr(anchor.getIntro());
							list.add(user);
						}
					}
				}
				Map<String, Object> data = new HashMap<>();
				data.put("word", word);
				data.put("anchors", list);
				return ActionResult.success(data);
			}
		}
		
		//单独处理提升账号
		if (Const.IOS_TEST_MOBILE_ACCOUNT_MAP.containsKey(userName) ||(Tools.isNotNull(header) && confService.testIOS())) {
			List<UserBaseInfo> list = null;
			List<AnchorOnlineEntity> anchors = applyAnchor.get(1);
			if(Tools.isNotNull(anchors) && anchors.size() > 0) {
				list = new ArrayList<>();
				int index = getRandomNumber(0, anchors.size()-4);
				for(int i=0; i<3; i++) {
					AnchorOnlineEntity anchor = anchors.get(index++);
					if(Tools.isNotNull(anchor)) {
						UserBaseInfo user  = new UserBaseInfo();
						user.setUserId(anchor.getUserid());
						user.setNickName(anchor.getNickname());
						user.setPhoto(ServiceHelper.getUserSmallPhoto(anchor.getPhoto()));
						user.setAnchorIntr(anchor.getIntro());
						list.add(user);
					}
				}
			}
			Map<String, Object> data = new HashMap<>();
			data.put("word", word);
			data.put("anchors", list);
			return ActionResult.success(data);
		}

		List<UserBaseInfo> list = null;
		//进一步确认是否是进行随机返回主播
		if(random) {
			PageModel pageModel = PageModel.getPageModel();
			pageModel.addQuery(Restrictions.eq("state", 1));
			pageModel.addQuery(Restrictions.eq("online", 3));
			pageModel.addQuery(Restrictions.sql("flag=0 order by rand() limit 5"));
			List<AnchorOnlineEntity> anchors = anchorOnlineContract.load(pageModel);
			if(Tools.isNotNull(anchors) && anchors.size() > 0) {
				list = new ArrayList<>(anchors.size());
				for (AnchorOnlineEntity anchor : anchors) {
					UserBaseInfo user  = new UserBaseInfo();
					user.setUserId(anchor.getUserid());
					user.setNickName(anchor.getNickname());
					user.setPhoto(ServiceHelper.getUserSmallPhoto(anchor.getPhoto()));
					user.setAnchorIntr(anchor.getIntro());
					list.add(user);
				}
			}
		} else {
			//首先对给定的关键字进行去除空格操作处理
			PageModel pageModel = PageModel.getPageModel();
			pageModel.addLimitField(0, 20);
			pageModel.addQuery(Restrictions.eq("state", 1));
			pageModel.addQuery(Restrictions.like("nickname", word));
			pageModel.desc("online");
			List<AnchorOnlineEntity> anchors = anchorOnlineContract.load(pageModel);
			if(Tools.isNotNull(anchors) && anchors.size() > 0) {
				list = new ArrayList<>(anchors.size());
				for (AnchorOnlineEntity anchor : anchors) {
					UserBaseInfo user  = new UserBaseInfo();
					user.setUserId(anchor.getUserid());
					user.setNickName(anchor.getNickname());
					user.setPhoto(ServiceHelper.getUserSmallPhoto(anchor.getPhoto()));
					user.setAnchorIntr(anchor.getIntro());
					list.add(user);
				}
			}
		}
		//拼装返回数据
		Map<String, Object> data = new HashMap<>();
		if(random) {
			data.put("anchors", list);
		} else {
			data.put("word", word);
			data.put("anchors", list);
		}
		return ActionResult.success(data);
	}
	*/
	
	@Override
	public ActionResult getAnchors(String body) throws Exception {
		RequestHeader header =RequestUtils.getCurrent().getHeader();
		String userName = RequestUtils.getCurrent().getUser().getUsername();
		//处理新版本中iso的数据  版本34 
		if(Tools.isNotNull(header) && header.getVersioncode() >= 34) {
			boolean flag = false;
			//首先检测是否是提审账号 
			if (iOSUserSmsAgent.getUserSmsList().containsKey(userName)) {
				flag = true;
			}
			//然后检测当前后台配置的开关是否是真实
			if(!flag && confService.testIOS()) {
				flag = true;
			}
			if(flag) {
				//根据版本返回对应的提审数据返回提审使用的假数据
				List<AnchorListVO> datas = new ArrayList<>();
				int version = header.getVersioncode();
				if(version == 61 || version == 62) {
					PageModel pageModel = PageModel.getPageModel();
					pageModel.addQuery(Restrictions.eq("state", -9));
					pageModel.addQuery(Restrictions.eq("flag", 1));
					if(version == 62) {
						pageModel.addQuery(Restrictions.lt("id", 235));
					} else {
						//添加对应的拉黑操作处理
						long userid = RequestUtils.getCurrent().getUser().getUserid();
						PageModel blackPageModel = PageModel.getPageModel();
						blackPageModel.addQuery(Restrictions.eq("userid", userid));
						List<UserBlacklistEntity> blacks = userBlacklistContract.load(blackPageModel);
						if(Tools.isNotNull(blacks) && blacks.size() > 0) {
							List<Long> userids = new ArrayList<>();
							for (UserBlacklistEntity black : blacks) {
								userids.add(black.getBlackid());
							}
							//添加拉黑过滤条件
							pageModel.addQuery(Restrictions.notin("userid", userids));
						}
						pageModel.addQuery(Restrictions.ge("id", 235));
					}
					List<AnchorOnlineEntity> anchors = anchorOnlineContract.load(pageModel);
					if(Tools.isNotNull(anchors) && anchors.size() > 0) {
						Collections.shuffle(anchors);
						int index = 60;
						if(version == 62)
							index = 20;
						for(int i=0; i < index && i < anchors.size(); i++) {
							AnchorOnlineEntity anchor = anchors.get(i);
							if(Tools.isNotNull(anchor)) {
								AnchorListVO vo = new AnchorListVO();
								//暂时不处理城市信息
								vo.setNickName(anchor.getNickname());
								vo.setOnlineStatue(anchor.getOnline());
								vo.setPhoto(ServiceHelper.getUserPhoto(anchor.getPhoto()));
								vo.setPrice(anchor.getPrice());
								if(anchor.getPrice() > 0) {
									vo.setPriceDes(anchor.getPrice() + "钻/分钟");
								} else {
									vo.setPriceDes("");
								}
								vo.setSignaName(anchor.getSignature());
								vo.setStar(anchor.getStar());
								vo.setUserid(anchor.getUserid());
								datas.add(vo);
							}
						}
					}
				} else {
					List<AnchorOnlineEntity> anchors = ios2Anchor.get(1);
					if(Tools.isNotNull(anchors) && anchors.size() > 0) {
						int index = getRandomNumber(0, anchors.size()-22);
						for(int i=0; i<20; i++) {
							AnchorOnlineEntity anchor = anchors.get(index++);
							if(Tools.isNotNull(anchor)) {
								AnchorListVO vo = new AnchorListVO();
								//暂时不处理城市信息
								vo.setNickName(anchor.getNickname());
								vo.setOnlineStatue(anchor.getOnline());
								vo.setPhoto(ServiceHelper.getUserPhoto(anchor.getPhoto()));
								vo.setPrice(anchor.getPrice());
								if(anchor.getPrice() > 0) {
									vo.setPriceDes(anchor.getPrice() + "钻/分钟");
								} else {
									vo.setPriceDes("");
								}
								vo.setSignaName(anchor.getSignature());
								vo.setStar(anchor.getStar());
								vo.setUserid(anchor.getUserid());
								datas.add(vo);
							}
						}
						//进行打乱操作处理
						Collections.shuffle(datas);
					}
				}
				boolean next = false;
				return ActionResult.success(datas,null,next);
			}
		}
		
		//特殊处理ios审核的假数据
		// 审核通过后暂时注释掉审核期间的假数据   后期可能会需要
		if(Tools.isNotNull(header) && confService.testIOS()) {
			PageModel pageModel = PageModel.getPageModel();
			pageModel.addQuery(Restrictions.eq("state", -9));
			pageModel.addQuery(Restrictions.eq("flag", -1));
			List<AnchorOnlineEntity> anchors = anchorOnlineContract.load(pageModel);
			List<AnchorListVO> datas = null;
			if(Tools.isNotNull(anchors) && anchors.size() > 0) {
				datas = new ArrayList<>();
				for (AnchorOnlineEntity anchor : anchors) {
					UserBO user = userAgent.findById(anchor.getUserid());
					if(Tools.isNotNull(user)) {
						AnchorListVO vo = new AnchorListVO();
						vo.setAge(getAge(user.getBirthday()));
						//暂时不处理城市信息
						vo.setCityName(null);
						vo.setGender(user.getSex());
						vo.setNickName(user.getNickname());
						vo.setOnlineStatue(anchor.getOnline());
						vo.setPhoto(ServiceHelper.getUserPhoto(user.getPhoto()));
						vo.setPrice(anchor.getPrice());
						if(anchor.getPrice() > 0) {
							vo.setPriceDes(anchor.getPrice() + "钻/分钟");
						} else {
							vo.setPriceDes("");
						}
						vo.setSignaName(user.getSignature());
						vo.setStar(anchor.getStar());
						vo.setUserid(anchor.getUserid());
						datas.add(vo);
					}
				}
			}
			boolean next = false;
			return ActionResult.success(datas,null,next);
		}
		
		
		//特殊处理android审核假数据
		/*
		RequestHeader header =RequestUtils.getCurrent().getHeader();
		if(Tools.isNotNull(header) && Tools.isNotNull(header.getChannel()) && header.getChannel().equals("Vivo_AP_DM_YO")) {
			PageModel pageModel = PageModel.getPageModel();
			pageModel.addQuery(Restrictions.eq("state", -9));
			pageModel.addQuery(Restrictions.eq("flag", 1));
			List<AnchorOnlineEntity> anchors = anchorOnlineContract.load(pageModel);
			List<AnchorListVO> datas = null;
			if(Tools.isNotNull(anchors) && anchors.size() > 0) {
				datas = new ArrayList<>();
				for (AnchorOnlineEntity anchor : anchors) {
					UserBO user = userAgent.findById(anchor.getUserid());
					if(Tools.isNotNull(user)) {
						AnchorListVO vo = new AnchorListVO();
						vo.setAge(getAge(user.getBirthday()));
						//暂时不处理城市信息
						vo.setCityName(null);
						vo.setGender(user.getSex());
						vo.setNickName(user.getNickname());
						vo.setOnlineStatue(anchor.getOnline());
						vo.setPhoto(ServiceHelper.getUserPhoto(user.getPhoto()));
						vo.setPrice(anchor.getPrice());
						vo.setSignaName(user.getSignature());
						vo.setStar(anchor.getStar());
						vo.setUserid(anchor.getUserid());
						datas.add(vo);
					}
				}
			}
			boolean next = false;
			return ActionResult.success(datas,null,next);
		}
		*/
		
		//特殊处理ios账号审核假数据
		if (iOSUserSmsAgent.getUserSmsList().containsKey(userName)) {
			List<AnchorOnlineEntity> anchors = applyAnchor.get(1);
			List<AnchorListVO> datas = null;
			if(Tools.isNotNull(anchors)) {
				datas = new ArrayList<>();
				for (AnchorOnlineEntity anchor : anchors) {
					AnchorListVO vo = new AnchorListVO();
					vo.setNickName(anchor.getNickname());
					vo.setOnlineStatue(anchor.getOnline());
					vo.setPhoto(ServiceHelper.getUserPhoto(anchor.getPhoto()));
					vo.setPrice(anchor.getPrice());
					if(anchor.getPrice() > 0) {
						vo.setPriceDes(anchor.getPrice() + "钻/分钟");
					} else {
						vo.setPriceDes("");
					}
					vo.setSignaName(anchor.getSignature());
					vo.setStar(anchor.getStar());
					vo.setUserid(anchor.getUserid());
					datas.add(vo);
				}
			}
			return ActionResult.success(datas, null, false);
		}
		
		//特殊处理android审核假数据
		boolean flag = false;
		//针对对应渠道
		if(Tools.isNotNull(header) && Tools.isNotNull(header.getChannel()) && header.getChannel().equals("Huawei_AP_DM_YO")) {
			flag = true;
		}
		
		//针对所有的android渠道
		if(Tools.isNotNull(header) && header.getOs_type() != 2) {
			flag = true;
		}
		
		if(flag) {
			PageModel pageModel = PageModel.getPageModel();
			pageModel.addQuery(Restrictions.eq("state", -9));
			pageModel.addQuery(Restrictions.eq("flag", 1));
			List<AnchorOnlineEntity> anchors = anchorOnlineContract.load(pageModel);
			List<AnchorListVO> datas = null;
			if(Tools.isNotNull(anchors) && anchors.size() > 0) {
				datas = new ArrayList<>();
				for (AnchorOnlineEntity anchor : anchors) {
					UserBO user = userAgent.findById(anchor.getUserid());
					if(Tools.isNotNull(user)) {
						AnchorListVO vo = new AnchorListVO();
						vo.setAge(getAge(user.getBirthday()));
						//暂时不处理城市信息
						vo.setCityName(null);
						vo.setGender(user.getSex());
						vo.setNickName(user.getNickname());
						vo.setOnlineStatue(anchor.getOnline());
						vo.setPhoto(ServiceHelper.getUserPhoto(user.getPhoto()));
						//vo.setPrice(anchor.getPrice());
						//设置固定价格
						vo.setPrice(5);
						vo.setSignaName(user.getSignature());
						vo.setStar(anchor.getStar());
						vo.setUserid(anchor.getUserid());
						datas.add(vo);
					}
				}
			}
			boolean next = false;
			return ActionResult.success(datas,null,next);
		}
		
//		String key = "anchor_online_state_1537262400469";
		long userid = RequestUtils.getCurrent().getUserid();
		JSONObject obj = JsonHelper.toJsonObject(body);
		String query = null;
		int index = 1;
		if(Tools.isNotNull(obj)) {
			JSONObject stamp = obj.getJSONObject("stamp");
			if(Tools.isNotNull(stamp)) {
				index = stamp.getIntValue("index");
				query = stamp.getString("query_time");
			}
		}
		if(Tools.isNull(query)) {
			query = anchorOnlineCacheRedis.get("anchor_online_new_list");
		}
		//直接没有对应的查询信息 直接不返回数据就可以了
		if(Tools.isNull(query))
			return ActionResult.success(null, null, false);
		//配置对应的查询key值
		String key = "anchor_online_state_"+ query;
		System.err.println(key);
		//设置进行查询数据的条件
		Set<Tuple> returnSet = anchorOnlineCacheRedis.zrangeByScoreWithScores(key, index, index+20, 0, 21);
		List<AnchorListVO> datas = null;
		boolean next = false;
		if(Tools.isNotNull(returnSet)) {
			System.err.println(returnSet.size());
			if(returnSet.size() > 20)
				next = true;
			datas = new ArrayList<>();
			for (Tuple tuple : returnSet) {
				if(Tools.isNotNull(tuple)) {
					String data = tuple.getElement();
					index = (int) tuple.getScore();
					AnchorOnlineEntity anchor = JsonHelper.toObject(data, AnchorOnlineEntity.class);
					if(Tools.isNotNull(anchor)) {
						//进行主播过滤自己的显示处理
						//if(anchor.getUserid() != userid) {
							//直接在主播表中获取对应的数据
							AnchorListVO vo = new AnchorListVO();
							vo.setNickName(anchor.getNickname());
							vo.setOnlineStatue(anchor.getOnline());
							vo.setPhoto(ServiceHelper.getUserPhoto(anchor.getPhoto()));
							vo.setPrice(anchor.getPrice());
							if(anchor.getPrice() > 0) {
								vo.setPriceDes(anchor.getPrice() + "钻/分钟");
							} else {
								vo.setPriceDes("");
							}
							vo.setSignaName(anchor.getSignature());
							vo.setStar(anchor.getStar());
							vo.setUserid(anchor.getUserid());
							datas.add(vo);
						//}
					}
				}
			}
			if(datas.size() > 21) {
				datas = datas.subList(0, 20);
			} else {
				//不足二十条就 需要设置一下新的查询位置
				index = index + 1;
			}
		}
		//拼接进行下次出来的stamp
		Map<String, String> stamp = new HashMap<>();
		stamp.put("query_time", query);
		stamp.put("index", index + "");
		return ActionResult.success(datas, stamp, next);
	}
	

	
	public int getRandomNumber(int min, int max) {
		Random random = new Random();  
		int randomNumber =  random.nextInt(max)%(max-min+1) + min; 
		return randomNumber;
	}
	
	/**
	 * 根据出生日期获得年龄
	 * @param birthDay
	 * @return
	 */
	private int getAge(Date birthDay) {
		int age = 0;
		if (Tools.isNotNull(birthDay)) {
			age = Tools.getAge(birthDay);
		} 
		return age;
	}
	
	/**
	 * 获得Banner
	 * @return
	 * @throws Exception 
	 */
	public ActionResult getBanners() throws Exception {
		int versioncode = 0;
		RequestHeader requestHeader = RequestUtils.getCurrent().getHeader();
		//特殊处理密聊包的显示结果
		if(Tools.isNotNull(requestHeader) && "com.tjhj.miliao".equals(requestHeader.getPackageName())) {
			//设置对应的banner
			List<GotoDataItem> activities = new ArrayList<>();
			BannerEntity banner = bannerContract.findById(19L);
		
			activities.add(buildBanner(banner));
			
			BannerBData bannerData = new BannerBData();
			bannerData.setBanners(activities);
			bannerData.setTime(getTime());
			bannerData.setTabList(null);
			return ActionResult.success(bannerData);
		}
		
		//特殊处理ios的一个包
		if(Tools.isNotNull(requestHeader) && "com.xq.yuanyuan".equals(requestHeader.getPackageName())) {
			//设置对应的banner
			List<GotoDataItem> activities = new ArrayList<>();
			BannerEntity banner = null;
			if(Const.IS_TEST) {
				banner = bannerContract.findById(22L);
			} else {
				banner = bannerContract.findById(42L);
			}
			
			activities.add(buildBanner(banner));
			BannerBData bannerData = new BannerBData();
			bannerData.setBanners(activities);
			bannerData.setTime(getTime());
			bannerData.setTabList(null);
			return ActionResult.success(bannerData);
		}
		
		if(Tools.isNotNull(requestHeader)) {
			versioncode = requestHeader.getVersioncode();
		}
		List<GotoDataItem> activities = new ArrayList<>();
		boolean check = channelCheckService.checkChannel();
		List<BannerEntity> banners = null;
		if(check) {
			PageModel pageModel = PageModel.getPageModel();
			if(Const.IS_TEST) {
				pageModel.addQuery(Restrictions.eq("id", 22));
			} else {
				pageModel.addQuery(Restrictions.eq("id", 42));
			}
			banners = bannerContract.load(pageModel);
		} else {
			UserBO bo = userAgent.findById(requestHeader.getUserid());
			if(Tools.isNotNull(bo)) {
				if(bo.isWaiter()) {
					banners = bannerContract.getBannerByGroupCode(CommonConst.INDEX_ACTIVITY_AREA, 0, 10, 2);
				} else {
					banners = bannerContract.getBannerByGroupCode(CommonConst.INDEX_ACTIVITY_AREA, 0, 10, 1);
				}
			} else {
				banners = bannerContract.getBannerByGroupCode(CommonConst.INDEX_ACTIVITY_AREA, 0, 10);
			}
		}
		//单独处理提升账号
		String userName = RequestUtils.getCurrent().getUser().getUsername();
		boolean falg = false;
		if (iOSUserSmsAgent.getUserSmsList().containsKey(userName)) {
			falg =true;
		}
		String packageName = RequestUtils.getCurrent().getHeader().getPackageName();
		if(confService.testIOS() || falg){
			banners = getIOSBanner(packageName+";");
		}
		if(Tools.isNotNull(banners)) {
			for(BannerEntity banner : banners) {
				//特殊处理37版本的排行榜显示
				long id = banner.getId();
				long ids = 0;
				if(Const.IS_TEST) {
					ids = 46;
				} else {
					ids = 50;
				}
						
				if(id == ids) {
					if(versioncode >= 37)
						activities.add(buildBanner(banner));
				} else {
					activities.add(buildBanner(banner));
				}
			}
		}
		List<GotoDataItem> tabList = new ArrayList<>();
		GotoDataItem pop = null;
		if(!check) {
			if(!("com.tjhj.dvzs".equals(requestHeader.getPackageName()))) {
				List<BannerEntity> banners2 = bannerContract.getBannerByGroupCode(CommonConst.INDEX_ACTIVITY_KEY_AREA, 0, 10);
				if(Tools.isNotNull(banners2)) {
					for(BannerEntity banner : banners2) {
						tabList.add(buildBanner(banner));
					}
				}
			}
			
			//处理获取对应的悬浮窗的操作处理
			List<BannerEntity> pops = bannerContract.getBannerByGroupCode(CommonConst.INDEX_ACTIVITY_POP, 0, 1);
			if(Tools.isNotNull(pops) && pops.size() > 0) {
				pop = buildBanner(pops.get(0));
			}
		}
		
		BannerBData bannerData = new BannerBData();
		bannerData.setBanners(activities);
		bannerData.setTime(getTime());
		bannerData.setTabList(tabList);
		bannerData.setSuspension(pop);
		return ActionResult.success(bannerData);
	}
	
	/**
	 * 提审获取Banner列表
	 * @param code - String
	 * @param start - 开始位置
	 * @param pagesize - 获取数量
	 * @return List<BannerEntity>
	 * @throws Exception
	 */
	public List<BannerEntity> getIOSBanner(String memo) throws Exception{
		PageModel pageModel = PageModel.getLimitModel(0, 10);
		pageModel.addQuery(Restrictions.like("memo", "%"+memo+"%"));
		pageModel.asc("priority");
		pageModel.desc("id");
		return bannerContract.load(pageModel);
	}
	
	/**
	 * 获得首页配置的轮播图播放时间
	 * @return
	 * @throws Exception
	 */
	private int getTime() throws Exception {
		int time = 2;
		return sysConfigAgent.getIntValue(CommonConst.BANNER_INTERVAL, time);
	}
	
	/**
	 * 根据手机类型构建轮播数据
	 * @param banner
	 * @param platform
	 * @return
	 */
	private GotoDataItem buildBanner(BannerEntity banner) {
		GotoDataItem item = new GotoDataItem();
		String parameters = banner.getParameters();
		item.setAndroidPage(IndexActivityAreaEnum.getAndroidPageByCode(parameters));
		item.setIosPage(IndexActivityAreaEnum.getIosPageByCode(parameters));
		item.setParame(banner.getParame());
		item.setClickEvent("banner_click_"+banner.getId().longValue());
		item.setImageurl(Const.getCdn(banner.getImageurl()));
		return item;
	}

	@Override
	public ActionResult applyAnchor(AnchorCreateDto createDto) throws Exception {
		String nickname = createDto.getNickName();
		nickname = nickname.trim();
		// 此处判断昵称是否符合规则
		if (!nickNameValid(nickname)) {
			return ActionResult.fail(ErrorCodeEnum.user_nickname_no_rule.getCode(),ErrorCodeEnum.user_nickname_no_rule.getDesc());
		}
		
		/*
		if (!introValid(createDto.getSignature())) {
			return ActionResult.fail(ErrorCodeEnum.user_signature_no_rule.getCode(),ErrorCodeEnum.user_signature_no_rule.getDesc());
		}
		*/
		
		/*
		if (!introValid(createDto.getIntro())) {
			return ActionResult.fail(ErrorCodeEnum.user_intro_no_rule.getCode(),ErrorCodeEnum.user_intro_no_rule.getDesc());
		}
		*/
		
		if(Tools.isNull(createDto.getMobile())) {
			return ActionResult.fail(ErrorCodeEnum.share_vip_moblie_error);
		}
		
		UserBO user = (UserBO)RequestUtils.getCurrent().getUser();
		long userid = user.getUserid();
		//进行验证操作处理
		/*
		int code = checkMobileEnable(createDto.getMobile(), userid);
		if(code == 2) {
			return ActionResult.fail();
		} else if(code == 1) {
			return ActionResult.fail(ErrorCodeEnum.auth_mobile_exist);
		}
		*/
		AnchorOnlineEntity anchor = anchorOnlineContract.findByProperty("userid", userid);
		if(Tools.isNull(anchor)) {
			anchor = new AnchorOnlineEntity();
			//设置开始的默认参数值
			anchor.setCreate_time(new Date());
			anchor.setUserid(userid);
			//初始价格默认值
			anchor.setPrice(5*20);
			anchor.setAnchor_video_price(5*20);
			anchor.setAnchor_audio_price(3*20);
			anchor.setAudio_price(3*20);
			
			//设置原始价格
			anchor.setOriginal_video_price(5*20);
			anchor.setOriginal_audio_price(3*20);
			anchor.setOriginal_anchor_video_price(5*20);
			anchor.setOriginal_anchor_audio_price(3*20);
			
			anchor.setStar(3);
			//设置默认的在线状态为 离线
			anchor.setOnline(0);
			anchor.setFlag(0);
		}
		anchor.setPhoto(user.getPhoto());
		anchor.setNickname(nickname);
		anchor.setMobile(createDto.getMobile());
		anchor.setStature(createDto.getStature().replace("cm", "")+" cm");
		anchor.setWeight(createDto.getWeight().replace("kg", "")+" kg");
		anchor.setZodiac(createDto.getZodiac());
		//设置城市id
		anchor.setCityid(Tools.parseLong(createDto.getCityId()));
		anchor.setSignature(createDto.getSignature());
		anchor.setIntro(createDto.getIntro());
		anchor.setState(AnchorStateEnum.apply.getCode());
		anchor.setUpdate_time(new Date());
		if(Tools.isNotNull(anchor.getId()) && anchor.getId() > 0) {
			anchorOnlineContract.update(anchor);
		} else {
			anchorOnlineContract.insert(anchor);
		}
		//触发更改用户昵称操作
		UserModifyDto dto = new UserModifyDto();
		dto.setUserid(userid);
		dto.setNickname(createDto.getNickName());
		dto.setSignature(createDto.getSignature());
		dto.setIntroduce(createDto.getIntro());
		userAgent.updateUser(dto);
		//处理修改网易昵称操作处理
		try {
			Map<String,String> params = new HashMap<>();
			params.put("accid", String.valueOf(userid));
			params.put("name", createDto.getNickName());
			JSONObject data = neteaseAgent.updateUser(params);
			if (NeteaseErrorEnum.success.getCode() != data.getIntValue("code")) {
				logger.warn("update nickName to Netease occured error:{}",data);
			}
		} catch (Exception e) {
			logger.warn("update netease nickName fail!", e);
		}
		return ActionResult.success();
	}
	
	/**
	 * 审核对应的手机号是否可以使用
	 * 0 可以使用  1已经绑定  2处于绑定中
	 * @param mobile
	 * @param userid
	 * @return
	 * @throws Exception 
	 */
	private int checkMobileEnable(String mobile, long userid) throws Exception {
		PageModel pageModel = PageModel.getPageModel();
		pageModel.addQuery(Restrictions.eq("mobile", mobile));
		pageModel.desc("state");
		List<AnchorOnlineEntity> anchors = anchorOnlineContract.load(pageModel);
		if(Tools.isNotNull(anchors) && anchors.size() > 0) {
			if(anchors.size() == 1) {
				//只填写过一次,检测本账号是否是自己填写的
				AnchorOnlineEntity anchor = anchors.get(0);
				if(anchor.getUserid() == userid) {
					return 0;
				} else if(anchor.getState() == AnchorStateEnum.apply.getCode()) {
					return 2;
				} else {
					return 1;
				}
			} else {
				//填写过多次
				return 1;
			}
		}
		return 0;
	}

	@Override
	public ActionResult authSendCode(String mobile) throws Exception {
		//进行验证操作处理
		/*
		if(Tools.isNull(mobile)){
			String regex = "^((13[0-9])|(14[5,7])|(15[0-3,5-9])|(17[0,3,5-8])|(18[0-9])|166|198|199|(147))\\d{8}$";
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(mobile);
	        boolean isMatch = m.matches();
	        if(!isMatch){
	        	return ActionResult.fail(ErrorCodeEnum.share_vip_moblie_error);
	        }
		}
		*/
		//获取当前用户
		RequestHeader header = RequestUtils.getCurrent().getHeader();
		UserBO user = userAgent.findById(header.getUserid());
		UserBO newUser =  userAgent.findByMobile(mobile);
		//检测别人是否已经注册了本手机号
		if(Tools.isNotNull(newUser) && newUser.getUserid() != user.getUserid()){
			return ActionResult.fail(ErrorCodeEnum.auth_mobile_exist);
		}
		
		newUser = userAgent.findByUsername(mobile);
		if(Tools.isNotNull(newUser) && newUser.getUserid() != user.getUserid()){
			return ActionResult.fail(ErrorCodeEnum.auth_mobile_exist);
		}
		
		if(Tools.isNotNull(user.getMobile())) {
			if(!(user.getMobile().equals(mobile)))
				return ActionResult.fail(ErrorCodeEnum.auth_mobile_new);
		}
		
		return sendMessageAgent.sendMobileValidCode(mobile, SendSmsTypeEnum.auth_mobile);
	}

	@Override
	public ActionResult authMobileAdd(JSONObject json) throws Exception {
		String mobile  = json.getString("mobile");
		String validCode  = json.getString("validCode");
		RequestHeader header = RequestUtils.getCurrent().getHeader();
		UserBO user = userAgent.findById(header.getUserid());
		/*
		if(Tools.isNull(mobile)){
			String regex = "^((13[0-9])|(14[5,7])|(15[0-3,5-9])|(17[0,3,5-8])|(18[0-9])|166|198|199|(147))\\d{8}$";
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(mobile);
	        boolean isMatch = m.matches();
	        if(!isMatch){
	        	return ActionResult.fail(ErrorCodeEnum.share_vip_moblie_error);
	        }
		}
		*/
		if(Tools.isNull(validCode)){
			return ActionResult.fail(ErrorCodeEnum.sms_recode_Error);
		}
		if(Tools.isNotNull(mobile) && Tools.isNotNull(validCode)){
			if(!sendMessageAgent.checkCode(mobile, validCode)){
				return ActionResult.fail(AgentErrorCodeEnum.valid_code_error);
			}
			if (Tools.isNull(validCodeService.getValidCode(mobile))) {
				return ActionResult.fail(AgentErrorCodeEnum.valid_code_pass);
			}
			
			UserBO newUser =  userAgent.findByMobile(mobile);
			if(Tools.isNotNull(newUser) && newUser.getUserid() != user.getUserid()){
				return ActionResult.fail(ErrorCodeEnum.auth_mobile_exist);
			}
			
			newUser = userAgent.findByUsername(mobile);
			if(Tools.isNotNull(newUser) && newUser.getUserid() != user.getUserid()){
				return ActionResult.fail(ErrorCodeEnum.auth_mobile_exist);
			}
			
			if(Tools.isNotNull(user.getMobile())) {
				if(!(user.getMobile().equals(mobile)))
					return ActionResult.fail(ErrorCodeEnum.auth_mobile_new);
			}
			
			UserModifyDto modifyDto = new UserModifyDto();
			modifyDto.setUserid(RequestUtils.getCurrent().getUserid());
			modifyDto.setMobile(mobile);
			userAgent.updateUser(modifyDto);
			//手机号绑定成功送积分
			userPointAgent.changePointAccount(RequestUtils.getCurrent().getUserid(),UserPointAccountLogTypeEnum.bind_mobile.getCode(), 1, mobile, UserPointAccountLogTypeEnum.bind_mobile.getDesc());
			return ActionResult.success();
		}
		return ActionResult.fail();
	}

	/**
	 * 验证昵称是否符合规范
	 */
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
	
	/**
	 * 验证签名是否符合规范
	 */
	public boolean introValid(String intro) {
		if (intro == null) {
			return false;
		}
		if (intro.length() < 1 || intro.length() > 15) {
			return false;
		}
		// 仅支持汉字，数字，大小写字母和_-并且不允许符号开头
		if (!Tools.matches("[0-9A-Za-z\\u4e00-\\u9fa5][0-9A-Za-z\\u4e00-\\u9fa5_-]+", intro)) {
			return false;
		}
		// 纯数字是不允许的
		if (Tools.matches("[0-9]+", intro)) {
			return false;
		}
		return true;
	}

	@Override
	public ActionResult start() throws Exception {
		RequestHeader header = RequestUtils.getCurrent().getHeader();
		//处理提审
		boolean flag = false;
		//针对特殊的渠道进行发送消息
		//if(Tools.isNotNull(header) && Tools.isNotNull(header.getChannel()) && header.getChannel().equals("Huawei_AP_DM_YO")) {
		//	flag = true;
		//}
		//针对所有的渠道显示假数据的处理
//		flag = true;
		
		//处理如果是测试服务器 就进行显示数据处理
//		if(Const.IS_TEST)
//			flag = false;
		//进行特定渠道假视频屏蔽
//		if(Tools.isNotNull(header) && Tools.isNotNull(header.getChannel()) && (header.getChannel().equals("User_Share_i")|| header.getChannel().equals("Zhubo_i"))) {
//			flag = true;
//		}
		long userId = RequestUtils.getCurrent().getUserid();
		UserBO userBO = userAgent.findById(userId);
		
		/*
		//首先处理检测是否处于充值超过500的用户
		if(userId > 0) {
			PageModel pay = PageModel.getPageModel();
			pay.addQuery(Restrictions.eq("userid", userId));
			long send = userPaySendDiamondContract.count(pay);
			if(send == 0) {
				//检测本用户是否满足充值大于500
				boolean limit = checkChargeLimit(userId);
				if(limit) {
					//增加一个限制  剩余额度为6钻
					long diamond = userDiamondAgent.getDiamondBalance(userId);
					if(diamond <= 120) {
						//进行记录送钻记录 同时发送消息处理
						UserPaySendDiamondEntity t = new UserPaySendDiamondEntity();
						t.setUserid(userId);
						t.setCreate_time(new Date());
						userPaySendDiamondContract.insert(t);
						//进行送钻石处理
						userDiamondAgent.changeDiamondAccount(userId, (long)160, (long)0, UserDiamondAccountLogTypeEnum.pay_limit_award.getCode(), 1, null, IdGenerater.generateId()+"", UserDiamondAccountLogTypeEnum.pay_limit_award.getDesc());
						//进行发送消息处理
						if(Const.IS_TEST) {
							neteaseAgent.pushOneMessage(131879189602173184L, userId, "鉴于您是尊贵用户，特送您160钻体验金，您可以直接用于拨打视频，经常来玩哦，每天都有新妹子", true);
						} else {
							neteaseAgent.pushOneMessage(65418719477628672L, userId, "鉴于您是尊贵用户，特送您160钻体验金，您可以直接用于拨打视频，经常来玩哦，每天都有新妹子", true);
						}
					}
				} else {
					
					boolean first = userFirstRechargeLogContract.checkFirstRecharge(userId, FirstPayTypeEnum.all.getCode());
					if(first) {
						//非主播
						if(Tools.isNotNull(userBO) && !userBO.isWaiter()) {
							//启动检测本用户是否体验过  同时是否超过七天了  超过七天 赠送体验机会
							PageModel pageModel = PageModel.getPageModel();
							pageModel.addPageField(0, 1);
							pageModel.addQuery(Restrictions.eq("userid", userId));
							pageModel.addQuery(Restrictions.eq("free_vchart_falg", 1));
							pageModel.addQuery(Restrictions.gt("vchat_duration", 0));
							pageModel.desc("id");
							List<VchatRoomEntity> rooms = vchatRoomContract.load(pageModel);
							if(Tools.isNotNull(rooms) && rooms.size() > 0) {
								VchatRoomEntity room = rooms.get(0);
								if(Tools.isNotNull(room)) {
									//检测是否是超过七天
									long current = System.currentTimeMillis();
									long create = room.getCreate_time().getTime() + Tools.DAY_MILLIS * 7;
									logger.error("体验机会超过七天了 current"+ current +"  create"+create);
									if(current > create) {
										//检测是否有没有消费过的体验机会
										pageModel.clearAll();
										pageModel.addQuery(Restrictions.eq("userid", userId));
										long count = freeVideoChatExperienceContract.count(pageModel);
										if(count == 0) {
											//再次赠送一次体验机会
											VchatRoomObscurationConfigDto configDto = sysConfigAgent.obscurationConfig();
											freeVideoChatExperienceContract.insertRecord(userId, header.getChannel(), Tools.intValue(configDto.getDialHelperTime()));
											if(Const.IS_TEST) {
												neteaseAgent.pushOneMessage(131879189602173184L, userId, "欢迎你回来，送你一次体验机会，赶紧去和喜欢的人聊天吧", true);
											} else {
												neteaseAgent.pushOneMessage(65418719477628672L, userId, "欢迎你回来，送你一次体验机会，赶紧去和喜欢的人聊天吧", true);
											}
										}
									}
								}
							}
						}
					}
				
				}
			}
		}
		*/
		//进行特定渠道假视频屏蔽
		flag = channelCheckService.checkChannel();
		
		//特殊处理北京地区的不进行发送假数据
		/*
		if(!flag) {
			String city = header.getCityCode();
			if(Tools.isNotNull(city) && city.equals("131"))
				flag = true;
		}
		*/
		
		//单独处理特殊渠道
		if(Tools.isNotNull(header) && "Huawei_moyue".equals(header.getChannel())) {
			flag = true;
		}
		
		//进一步取消审核账号发送消息
		if(!flag) {
			if(Tools.isNotNull(header)) {
				if(header.getUserid() == 146407573757690112L)
					flag = true;
				if(header.getUserid() == 146614470655934720L)
					flag = true;
			}
		}
		
		if(!flag) {
			long flower = redFlowerAgent.getRedFlowerDeposit(userId);
			long diamond = userDiamondAgent.getDiamondDeposit(userId);
			//检测是否已经进行充值了
			if(flower > 5 || diamond > 200) {
				//进行充值了 
				flag = true;
			} else {
				PageModel pageModel = PageModel.getPageModel();
				pageModel.addQuery(Restrictions.eq("userid", userId));
				long num = appDailScenceContract.count(pageModel);
				if(num < 15) {
					//检测是否已经使用完所有的场景
					//flag = true;
					
					/**
					 * 暂时使用代码写死的方式来取消 15个场景消息  开启原始的50个场景
					 */
					//flag = false;
					//检测后台是否配置了屏蔽机制
					try {
						SysConfigEntity config = sysConfigContract.findByProperty("name", com.tigerjoys.shark.miai.agent.constant.Const.APP_ROBOT_COFIG);
						JSONObject ctrl = JsonHelper.toJsonObject(config.getValue());
						if(Tools.isNotNull(ctrl)) {
							int scence = ctrl.getIntValue("scence");
							if (scence == 1) {
								flag = true;
							}
						}
					} catch (Exception e) {
						
					}
				}
			}
		}
		
		if(!flag) {
			flag = channelCheckService.checkSendMessage();
		}

		if(flag) {
			Map<String, Object> result = new HashMap<>();
			//设置有钻石   表示不弹窗
			result.put("control", 0);
			//设置30秒启动
			result.put("time", 30);
			result.put("talant", null);
			return ActionResult.success(result);
		}

		//首先获取对应的假视频的主播信息
		int control = 0;
		RecommendTalentListVO talant = null;
		/*
		if(userId > 0) {
			//用户不是对应的主播账号
			if(Tools.isNotNull(userBO) && !userBO.isWaiter()) {
				//获取用户是否有余额
				long diamond = userDiamondAgent.getDiamondBalance(userId);
				//获取随机的值 即是否发送
				//int rand = getRandomNumber(0,2);
				
				//改成每次都进行发送
				int rand = 1;
				//检测余额和随机值满足
				if(diamond == 0 && rand == 1) {
					int version = header.getVersioncode();
					//此接口只对老用户进行假来电  新版本使用新版本的假来电
					if(version < 51) {
						//获取给该用户发送的数量值
						String date = Tools.getDate();
						String value = anchorOnlineCacheRedis.hget("send_video_"+ date, userId+"");
						logger.error("当前获取到的数量值"+value);
						
						if(Tools.isNull(value) || Integer.parseInt(value) < 1000) {
							//随机一条视频,查看对应的主播
							List<ShortVideoEntity> videos = anchorVideos.get(1);
							if(Tools.isNotNull(videos)) {
								int index = getRandomNumber(0,videos.size()-1);
								ShortVideoEntity video = videos.get(index);
								if(Tools.isNotNull(video)) {
									AnchorOnlineEntity anchor = anchorOnlineContract.findByProperty("userid", video.getUserid());
									if(Tools.isNotNull(anchor)) {
										control = 1;
										talant = new RecommendTalentListVO();
										talant.setUserid(anchor.getUserid());
										talant.setNickName(anchor.getNickname());
										talant.setPhoto(ServiceHelper.getUserSmallPhoto(anchor.getPhoto()));
										if(anchor.getUserid().longValue() > 105646462616011008L) {
											talant.setVideoUrl(ServiceHelper.getCdnVideo(video.getVideo_path()));
										} else {
											talant.setVideoUrl(Const.getCdn(video.getVideo_path()));
										}
										long num = anchorOnlineCacheRedis.hincrBy("send_video_"+ date, userId+"", 1);
										anchorOnlineCacheRedis.expire("send_video_"+ date, AgentRedisCacheConst.DEFAULT_CACHE_EXPIRE_DAY);
										logger.error("已经发送的消息数量"+num);
										
										//记录发送的假来电
										AppShamDailEntity t = new AppShamDailEntity();
										t.setUserid(userId);
										t.setVuserid(anchor.getUserid());
										t.setCreate_time(new Date());
										appShamDailContract.insert(t);
									}
								}
							}
						}
					}
				}
			}
		}
		*/
		Map<String, Object> result = new HashMap<>();
		//设置30秒启动
		//result.put("time", getRandomNumber(10, 30));
		result.put("time", 10);
		result.put("talant", talant);
		result.put("control", control);
		
		//如果不是用户就不需要发送消息了
		if(userBO.isWaiter()) {
			return ActionResult.success(result);
		}
		
		//触发发送消息处理
		int indexs = -1;
		AppStartMsgUserEntity userMsg = appStartMsgUserContract.lockByUserId(userId);
		if(Tools.isNotNull(userMsg)) {
			//首先检查 对应的时间   如果时间不是今天的说明是首次启动
			long dawn = Tools.getDayTime();
			long current = Tools.getDayTime(userMsg.getTime());
			int count = userMsg.getCount();
			int sendFlag = 0;
			if(dawn != current) {
				//这里说明是进行首次进行发送消息
				userMsg.setCount(1);
				sendFlag = 1;
			} else {
				//这里说明已经今天已近完成了第一次启动  需要处理是否进行第二次发送消息的判断处理
				if(count < 5) {
					int rand = getRandomNumber(3, 5);
					if(rand == 4) {
						//随机到第四个值   这个地方说明需要进行发送  今天的第二次消息
						userMsg.setCount(userMsg.getCount()+1);
						sendFlag = 1;
					}
				}
			}
			//检测是否需要发送消息状态值
			if(sendFlag == 1) {
				indexs = userMsg.getIndexs();
				//获取下一个需要发送的消息
				indexs = indexs + 1;
				//检测对应的消息索引是否越界了
				if(indexs >= msg.size())
					indexs = 0;
				userMsg.setTime(new Date());
				userMsg.setIndexs(indexs);
				appStartMsgUserContract.update(userMsg);
			}
		} else {
			//这个地方说明还没有对用户创建对应的消息信息表    同时此处也是完成第一次启动操作处理
			indexs = 0;
			userMsg = new AppStartMsgUserEntity();
			userMsg.setCount(1);
			userMsg.setTime(new Date());
			userMsg.setUserid(userId);
			//设置已经发送的消息的索引位置
			userMsg.setIndexs(indexs);
			appStartMsgUserContract.insert(userMsg);
		}
		
		//检测是否有需要发送的消息
		if(indexs >= 0) {
			long time = new Date().getTime();
			String mm = null;
			if(Tools.isNotNull(msg) && msg.size() > 0) {
				mm = msg.get(indexs);
			}
			if(Tools.isNull(mm))
				mm = "很高兴认识你哦!!!";
			PageModel pageModel = PageModel.getPageModel();
			pageModel.addQuery(Restrictions.eq("state", 1));
			pageModel.addQuery(Restrictions.eq("online", 3));
			//特殊处理夏军的账号
			pageModel.addQuery(Restrictions.ne("userid", 130114190751891712L));
			pageModel.addQuery(Restrictions.sql("flag=0 order by rand() limit 5"));
			List<AnchorOnlineEntity> list = anchorOnlineContract.load(pageModel);
			if(Tools.isNotNull(list) && list.size() > 0) {
				for(int i = 0; i<list.size(); i++) {
					AnchorOnlineEntity anchor = list.get(i);
					if(Tools.isNotNull(anchor)) {
						//检测是否有对应的聊天关系
						pageModel.clearAll();
						pageModel.addQuery(Restrictions.eq("user_id", anchor.getUserid()));
						pageModel.addQuery(Restrictions.eq("other_id", userId));
						long count = userTextChatHistoryContract.count(pageModel);
						if(count == 0) {
							AppStartSendMsgEntity entity = new AppStartSendMsgEntity();
							entity.setFromId(anchor.getUserid());
							entity.setToId(userId);
							entity.setMsg(mm);
							entity.setSend_time(time + 15000);
							appStartSendMsgContract.insert(entity);
							logger.error("设置发送消息 from:"+anchor.getUserid() +" userid:"+userId);
							break;
						}
					}
				}
			}
		}
		msgSceneService.defineMsgScene(userId);
		return ActionResult.success(result);
	}
	
	private boolean checkChargeLimit(long userid) throws Exception{
		PageModel pageModel = PageModel.getLimitModel(0, 50);
		pageModel.addProjection(Projections.sum("money").as("totalMoney"));
		pageModel.addQuery(Restrictions.eq("status",1));
		pageModel.addQuery(Restrictions.eq("user_id",userid));
		long limit = 50000L;
		boolean chargeLimitFalg = false;
		List<Map<String, Object>> maps = rechargeOrderContract.loadGroupBy(pageModel);
		if (Tools.isNotNull(maps)) {
			for(Map<String, Object> map:maps){
				if (Tools.isNotNull(map)) {
					long totalMoney = Tools.parseLong(map.get("totalMoney"));
					if(totalMoney>=limit){
						chargeLimitFalg = true;
					}
				}
			}
		}
		return chargeLimitFalg;
	}


	@Override
	public ActionResult getTabAnchors(String body) throws Exception {
		JSONObject obj = JsonHelper.toJsonObject(body);
		Map<String, Object> map = null;
		if(Tools.isNotNull(obj)) {
			JSONObject stamp = obj.getJSONObject("stamp");
			String tag = obj.getString("tag");
			//根据对应的tag来返回对应的数据
			if(Tools.isNotNull(tag)) {
				if(tag.equals("0")) {
					//获取关注的主播的数据
					map = getAttentionAnchors(stamp);
				} else {
					//获取对应分类的主播数据  新人 推荐 三星 四星 五星
					map = getTabAnchors(tag,stamp);
				}
			}
		}
		//分析获取到的对应数据 并进行设置相应的返回数据
		List<UserBaseInfo> anchors = null;
		String stampNew = null;
		boolean next = false;
		if(Tools.isNotNull(map)) {
			anchors = (List<UserBaseInfo>) map.get("anchors");
			if(Tools.isNotNull(anchors) && anchors.size() > 0) {
				stampNew = (String) map.get("stamp");
				if(anchors.size() > 20) {
					anchors = anchors.subList(0, 20);
					next = true;
				}
			}
		}
		return ActionResult.success(anchors, stampNew, next);
	}
	
	private Map<String, Object> getTabAnchors(String tag, JSONObject stamp) {
		int index = 1;
		String query = null;
		String stampNew = null;
		if(Tools.isNotNull(stamp)) {
			index = stamp.getIntValue("index");
			query = stamp.getString("query_time");
		}
		//根据对应的tag来查询对应的时间
		if(Tools.isNull(query)) {
			query = anchorOnlineCacheRedis.get("anchor_online_new_list_"+tag);
		}
		//直接没有对应的查询信息 直接不返回数据就可以了
		if(Tools.isNull(query))
			return null;
		//配置对应的查询key值
		String key = "anchor_online_state_" + tag + "_" + query;
		System.err.println(key);
		Set<Tuple> returnSet = anchorOnlineCacheRedis.zrangeByScoreWithScores(key, index, index+20, 0, 21);
		List<UserBaseInfo> datas = null;
		if(Tools.isNotNull(returnSet)) {
			datas = new ArrayList<>();
			for (Tuple tuple : returnSet) {
				if(Tools.isNotNull(tuple)) {
					String data = tuple.getElement();
					index = (int) tuple.getScore();
					AnchorOnlineEntity anchor = JsonHelper.toObject(data, AnchorOnlineEntity.class);
					if(Tools.isNotNull(anchor)) {
						UserBaseInfo vo = new UserBaseInfo();
						vo.setUserId(anchor.getUserid());
						vo.setNickName(anchor.getNickname());
						vo.setAnchorStatus(anchor.getOnline());
						vo.setPhoto(ServiceHelper.getUserPhoto(anchor.getPhoto()));
						if(anchor.getPrice() > 0) {
							vo.setAnchorPrice(anchor.getPrice()+"钻/分钟");
						} else {
							vo.setAnchorPrice("");
						}
						vo.setAnchorSignature(anchor.getSignature());
						vo.setAnchorStar(anchor.getStar());
						datas.add(vo);
					}
				}
			}
			//配置对应的戳对象
			Map<String, String> stampN = new HashMap<>();
			stampN.put("query_time", query);
			stampN.put("index", index + "");
			stampNew = JsonHelper.toJson(stampN);
		}
		Map<String, Object> data = new HashMap<>();
		data.put("anchors", datas);
		data.put("stamp", stampNew);
		return data;
	}
	
	private Map<String, Object> getAttentionAnchors(JSONObject stamp) throws Exception {
		String stampNew = null;
		List<UserBaseInfo> datas = null;
		String query = null;
		//分析对应的关注人所对应的主播状态
		long userid = RequestUtils.getCurrent().getUser().getUserid();
		if(Tools.isNotNull(stamp)) {
			query = stamp.getString("query_time");
		}
		//根据对应的tag来查询对应的时间
		if(Tools.isNull(query)) {
			query = Tools.getDateTime();
		}
		List<AnchorOnlineEntity> anchors = anchorOnlineContract.attentionAnchors(userid+"", query);
		if(Tools.isNotNull(anchors) && anchors.size() > 0) {
			
		}
		Map<String, Object> data = new HashMap<>();
		data.put("anchors", datas);
		data.put("stamp", stampNew);
		return data;
	}

	@Override
	public ActionResult getIosAnchors(Integer tag, String stamp) throws Exception {
		StringBuffer stampBuffer =new StringBuffer(Tools.isNull(stamp)?"":stamp);
		AhchorShortVideoDto outDto = new AhchorShortVideoDto();
		outDto.setType(tag);
		boolean next = false;
		List<AnchorOnlineEntity> anchorList = null;
		if(tag == 0) {
			//处理关注数据
			anchorList = getAttentionAnchors(stampBuffer);
		} else if(tag == 1) {
			//ios推荐  此处需要进行单独控制   推荐已经单独拉出去了
		} else if(tag <= 5) {
			anchorList = getTabAnchors(tag+"", stampBuffer);
		}
		
		List<AnchorListVO> datas = null;
		if(Tools.isNotNull(anchorList) && anchorList.size() > 0) {
			datas = new ArrayList<>();
			int i = 0;
			for (AnchorOnlineEntity anchor : anchorList) {
				AnchorListVO vo = new AnchorListVO();
				vo.setNickName(anchor.getNickname());
				vo.setOnlineStatue(anchor.getOnline());
				vo.setPhoto(ServiceHelper.getUserPhoto(anchor.getPhoto()));
				vo.setPrice(anchor.getPrice());
				vo.setPriceDes(anchor.getPrice() + "钻/分钟");
				vo.setSignaName(anchor.getSignature());
				vo.setStar(anchor.getStar());
				vo.setUserid(anchor.getUserid());
				datas.add(vo);
				if(++i>=20) {
					next = true;
					break;
				}
			}
		}
		
		Map<String, Object> data = new HashMap<>();
		data.put("anchors", datas);
		data.put("tag", tag);
		return ActionResult.success(data, stampBuffer.toString(), next);
	}
	
	private List<AnchorOnlineEntity> getTabAnchors(String tag, StringBuffer stamp) throws Exception {
		//显示正常的数据
		int index = 1;
		String query = null;
		if(Tools.isNotNull(stamp) && stamp.length()>0) {
			final JSONObject json = JsonHelper.toJsonObject(stamp.toString());
			index = json.getIntValue("index");
			query = json.getString("query_time");
		}
		//根据对应的tag来查询对应的时间
		if(Tools.isNull(query)) {
			query = anchorOnlineCacheRedis.get("anchor_online_new_list_"+tag);
		}
		//直接没有对应的查询信息 直接不返回数据就可以了
		if(Tools.isNull(query))
			return null;
		//配置对应的查询key值
		String key = "anchor_online_state_" + tag + "_" + query;
		System.err.println(key);
		Set<Tuple> returnSet = anchorOnlineCacheRedis.zrangeByScoreWithScores(key, index, index+20, 0, 21);
		List<AnchorOnlineEntity> datas = null;
		if(Tools.isNotNull(returnSet)) {
			datas = new ArrayList<>();
			for (Tuple tuple : returnSet) {
				if(Tools.isNotNull(tuple)) {
					String data = tuple.getElement();
					index = (int) tuple.getScore();
					AnchorOnlineEntity anchor = JsonHelper.toObject(data, AnchorOnlineEntity.class);
					if(Tools.isNotNull(anchor)) {
						datas.add(anchor);
					}
				}
			}
			//配置对应的戳对象
			Map<String, String> stampN = new HashMap<>();
			stampN.put("query_time", query);
			stampN.put("index", index + "");
			stamp.delete(0, stamp.length()).append(JsonHelper.toJson(stampN));
		}
		return datas;
	}
	
	private List<AnchorOnlineEntity> getAttentionAnchors(StringBuffer stamp) throws Exception {
		String query = null;
		//分析对应的关注人所对应的主播状态
		long userid = RequestUtils.getCurrent().getUser().getUserid();
		//long userid = 89140177645142272L;
		if(userid <= 0) {
			return null;
		}
		if(Tools.isNotNull(stamp) && stamp.length()>0) {
			final JSONObject json = JsonHelper.toJsonObject(stamp.toString());
			query = json.getString("query_time");
		}
		//根据对应的tag来查询对应的时间
		if(Tools.isNull(query)) {
			query = Tools.getDateTime();
		}
		List<AnchorOnlineEntity> anchors = anchorOnlineContract.attentionAnchors(userid+"", query);
		if(Tools.isNotNull(anchors) && anchors.size() > 20) {
			//配置对应的戳对象
			Map<String, String> stampN = new HashMap<>();
			stampN.put("query_time", Tools.getDateTime(anchors.get(anchors.size()-1).getUpdate_time()));
			stamp.delete(0, stamp.length()).append(JsonHelper.toJson(stampN));
		}
		return anchors;
	}

	@Override
	public ActionResult getIosHotAnchors() throws Exception {
		//获取所有的类别
		PageModel pageModel = PageModel.getPageModel();
		pageModel.addQuery(Restrictions.eq("state", 1));
		pageModel.desc("priority");
		List<AnchorHotEntity> hots = anchorHotContract.load(pageModel);
		List<HotAnchorDto> datas = null;
		if(Tools.isNotNull(hots) && hots.size() > 0) {
			datas = new ArrayList<>();
			//拼接对应类别下的主播数据
			for (AnchorHotEntity hot : hots) {
				pageModel.clearAll();
				pageModel.addQuery(Restrictions.eq("tab", hot.getId()));
				List<AnchorHotUserEntity> anchors = anchorHotUserContract.load(pageModel);
				if(Tools.isNotNull(anchors) && anchors.size() > 0) {
					List<AnchorListVO> vos = new ArrayList<>();
					for (AnchorHotUserEntity anchor : anchors) {
						AnchorOnlineEntity data = anchorOnlineContract.findByProperty("userid", anchor.getUserid());
						if(Tools.isNotNull(data)) {
							AnchorListVO vo = new AnchorListVO();
							vo.setNickName(data.getNickname());
							vo.setOnlineStatue(data.getOnline());
							vo.setPhoto(ServiceHelper.getUserPhoto(data.getPhoto()));
							vo.setPrice(data.getPrice());
							vo.setPriceDes(data.getPrice() + "钻/分钟");
							vo.setSignaName(data.getSignature());
							vo.setStar(data.getStar());
							vo.setUserid(anchor.getUserid());
							vos.add(vo);
						}
					}
					if(Tools.isNotNull(vos) && vos.size() > 0) {
						HotAnchorDto dto = new HotAnchorDto();
						dto.setName(hot.getName());
						dto.setColor(hot.getColor());
						dto.setAnchors(vos);
						datas.add(dto);
					}
				}
			}
		}
		return ActionResult.success(datas);
	}

	@Override
	public ActionResult getIOSAnchors() throws Exception {
		long userid = RequestUtils.getCurrent().getUser().getUserid();
		List<AnchorVideoWatchDto> videoWatchList = null;
		boolean check = channelCheckService.checkChannel();
		if(check) {
			PageModel pageModel = new PageModel();
			//处理状态值
			pageModel.addQuery(Restrictions.eq("state", -9));
			//处理标记
			//pageModel.addQuery(Restrictions.eq("flag", 3));
			pageModel.addQuery(Restrictions.sql("flag=3 order by rand() limit 20"));
			List<AnchorOnlineEntity> anchors = anchorOnlineContract.load(pageModel);
			if(Tools.isNotNull(anchors)) {
				Collections.shuffle(anchors);
				videoWatchList = new ArrayList<>();
				//特定处理三个特殊账号
				if(anchors.size() > 15) {
					AnchorOnlineEntity entity = anchorOnlineContract.findByProperty("userid", 65418719110627072L);
					if(Tools.isNotNull(entity)) {
						anchors.add(getRandomNumber(0, anchors.size()-15), entity);
					}
					entity = anchorOnlineContract.findByProperty("userid", 65418718835900160L);
					if(Tools.isNotNull(entity)) {
						anchors.add(getRandomNumber(0, anchors.size()-11), entity);
					}
					entity = anchorOnlineContract.findByProperty("userid", 65418718194171648L);
					if(Tools.isNotNull(entity)) {
						anchors.add(getRandomNumber(0, anchors.size()-8), entity);
					}
				}
				
				for (AnchorOnlineEntity anchor : anchors) {
					if(Tools.isNotNull(anchor)) {
						AnchorVideoWatchDto dto = AnchorVideoWatchDto.preDto(anchor,null,0, false);
						dto.setPraiseFlag(userFriendAgent.isAttention(userid,anchor.getUserid()));
						UserBaseInfo info = dto.getUserInfo();
						if(Tools.isNotNull(info)) {
							info.setAnchorWeight(anchor.getWeight());
							info.setAnchorStature(anchor.getStature());
							info.setAnchorZodiac(anchor.getZodiac());
							if(anchor.getCityid() > 0) {
								//处理城市信息
								AreaDto cityArea = appAreaService.getCityByBaiduCode(Tools.parseInt(anchor.getCityid()+""));
								if(Tools.isNotNull(cityArea))
									info.setCityName(cityArea.getName());
							}
						}
						videoWatchList.add(dto);
					}
				}
			}
			return ActionResult.success(videoWatchList);
		}
		PageModel pageModel = new PageModel();
		pageModel.addLimitField(0, 50);
		//设置获取正常状态的主播
		pageModel.addQuery(Restrictions.eq("state", 1));
		pageModel.addSort("online", SortBean.ORDER_DESC);
		pageModel.addSort("priority", SortBean.ORDER_DESC);
		pageModel.addSort("audit_time", SortBean.ORDER_DESC);
		List<AnchorOnlineEntity> anchors = anchorOnlineContract.load(pageModel);
		
		if(Tools.isNotNull(anchors)) {
			videoWatchList = new ArrayList<>();
			List<AnchorOnlineEntity> anchors3 = new ArrayList<>();
			List<AnchorOnlineEntity> anchors2 = new ArrayList<>();
			List<AnchorOnlineEntity> anchors1 = new ArrayList<>();
			List<AnchorOnlineEntity> anchors0 = new ArrayList<>();
			for (AnchorOnlineEntity anchor : anchors) {
				if(anchor.getOnline() == 3) {
					anchors3.add(anchor);
				} else if(anchor.getOnline() == 2){
					anchors2.add(anchor);
				} else if(anchor.getOnline() == 1){
					anchors1.add(anchor);
				} else if(anchor.getOnline() == 0){
					anchors0.add(anchor);
				}
			}
			//进行数据打乱操作处理
			if(Tools.isNotNull(anchors3) && anchors3.size() > 0)
				Collections.shuffle(anchors3);
			if(Tools.isNotNull(anchors2) && anchors2.size() > 0)
				Collections.shuffle(anchors2);
			if(Tools.isNotNull(anchors1) && anchors1.size() > 0)
				Collections.shuffle(anchors1);
			if(Tools.isNotNull(anchors0) && anchors0.size() > 0)
				Collections.shuffle(anchors0);
			anchors = null;
			anchors = new ArrayList<>();
			anchors.addAll(anchors3);
			anchors.addAll(anchors2);
			anchors.addAll(anchors1);
			anchors.addAll(anchors0);
			RequestHeader header = RequestUtils.getCurrent().getHeader();
			boolean falg = false;
			if(Tools.isNotNull(header) && "ios_quliao".equals(header.getChannel()) && header.getVersioncode() >= 57) {
				falg = true;
			}
			
			for (AnchorOnlineEntity anchor : anchors) {
				if(Tools.isNotNull(anchor)) {
					AnchorVideoWatchDto dto = AnchorVideoWatchDto.preDto(anchor,null,0, false);
					dto.setPraiseFlag(userFriendAgent.isAttention(userid,anchor.getUserid()));
					UserBaseInfo info = dto.getUserInfo();
					if(Tools.isNotNull(info)) {
						if(falg)
							info.setAnchorStatus(4);
						info.setAnchorWeight(anchor.getWeight());
						info.setAnchorStature(anchor.getStature());
						info.setAnchorZodiac(anchor.getZodiac());
						if(anchor.getCityid() > 0) {
							//处理城市信息
							AreaDto cityArea = appAreaService.getCityByBaiduCode(Tools.parseInt(anchor.getCityid()+""));
							if(Tools.isNotNull(cityArea))
								info.setCityName(cityArea.getName());
						}
					}
					videoWatchList.add(dto);
				}
			}
		}
		
		return ActionResult.success(videoWatchList);
	}

	@Override
	public ActionResult temptStart(String body) throws Exception {
		JSONObject obj = JsonHelper.toJsonObject(body);
		int count = obj.getIntValue("count");
		long userId = RequestUtils.getCurrent().getUserid();
		RequestHeader header = RequestUtils.getCurrent().getHeader();
		// 0  不显示      1  显示     2  切断心跳
		int control = 0;
		RecommendTalentListVO talant = null;
		boolean flag = false;
		//进行特定渠道假视频屏蔽
		flag = channelCheckService.checkChannel();
		
		if(!flag) {
			long flower = redFlowerAgent.getRedFlowerDeposit(userId);
			long diamond = userDiamondAgent.getDiamondDeposit(userId);
			//检测是否已经进行充值了
			if(flower > 5 || diamond > 200) {
				//进行充值了 
				flag = true;
			} else {
				PageModel pageModel = PageModel.getPageModel();
				pageModel.addQuery(Restrictions.eq("userid", userId));
				long num = appDailScenceContract.count(pageModel);
				if(num < 15) {
					//检测是否已经使用完所有的场景
					//flag = true;
					/**
					 * 暂时使用代码写死的方式来取消 15个场景消息
					 */
					//flag = false;
					
					//检测后台是否配置了屏蔽机制
					try {
						SysConfigEntity config = sysConfigContract.findByProperty("name", com.tigerjoys.shark.miai.agent.constant.Const.APP_ROBOT_COFIG);
						JSONObject ctrl = JsonHelper.toJsonObject(config.getValue());
						if(Tools.isNotNull(ctrl)) {
							int scence = ctrl.getIntValue("scence");
							if (scence == 1) {
								flag = true;
							}
						}
					} catch (Exception e) {
						
					}
				}
			}
		}
		
		if(!flag) {
			flag = channelCheckService.checkShowDail();
		}
		
		//单独处理特殊渠道
		if(Tools.isNotNull(header) && "Huawei_moyue".equals(header.getChannel())) {
			flag = true;
		}
		
		if(flag) {
			control = 2;
		} else {
			if(userId > 0) {
				UserBO bo = userAgent.findById(userId);
				if(Tools.isNotNull(bo)) {
					//处理更新用户华为推送的业务逻辑
					try {
						if(bo.getPushchannel().intValue() == 1 && Tools.isNotNull(header) && Tools.isNotNull(header.getHuaweiToken())) {
							boolean update = false;
							if(Tools.isNotNull(bo.getHuaweiToken())) {
								if(!bo.getHuaweiToken().equals(header.getHuaweiToken())) {
									update = true;
								}
							} else {
								update = true;
							}
							if(update)
								userAgent.updateUserHuaweiToken(userId, header.getHuaweiToken());
						}
					} catch (Exception e) {
						logger.error("检测逻辑出现异常");
					}
					
					if(!bo.isWaiter()) {
						//检测是否进行充值操作处理
						PageModel pageModel = PageModel.getPageModel();
						pageModel.addQuery(Restrictions.eq("user_id", userId));
						pageModel.addQuery(Restrictions.eq("status", 1));
						pageModel.addQuery(Restrictions.eq("type", 1));
						long num = userPayActionContract.count(pageModel);
						if(num == 0) {
							//检测是否超过了3次  超过3次 也不经发送通知操作处理了
							if(count >= 3) {
								//设置切断标记
								control = 2;
							} else {
								//检测当前是否处于充值页面中  充值页面就不弹出了
								boolean pay = userOrdinaryOnlineListAgent.getAnchorForbidDialFlag(userId);
								if(!pay) {
									//随机获取一个虚拟主播  然后查找对应的视频
									pageModel.clearAll();
									pageModel.addQuery(Restrictions.eq("state", 1));
									pageModel.addQuery(Restrictions.eq("sex", 2));
									pageModel.addQuery(Restrictions.sql("flag=4 order by rand() limit 1"));
									List<AnchorOnlineEntity> list = anchorOnlineContract.load(pageModel);
									if(Tools.isNotNull(list) && list.size() > 0) {
										control = 1;
										AnchorOnlineEntity anchor = list.get(0);
										talant = new RecommendTalentListVO();
										talant.setUserid(anchor.getUserid());
										talant.setNickName(anchor.getNickname());
										talant.setPhoto(ServiceHelper.getUserSmallPhoto(anchor.getPhoto()));
										//获取随机的对应的视频地址
										pageModel.clearAll();
										pageModel.addQuery(Restrictions.eq("userid", anchor.getUserid()));
										pageModel.addQuery(Restrictions.sql("status>=-9 order by rand() limit 1"));
										List<ShortVideoEntity> videos = shortVideoContract.load(pageModel);
										if(Tools.isNotNull(videos) && videos.size() > 0) {
											ShortVideoEntity video = videos.get(0);
											if(Tools.isNotNull(video)) {
												talant.setVideoUrl(ServiceHelper.getCdnVideo(video.getVideo_path()));
											}
										}
										//记录发送的假来电
										AppShamDailEntity t = new AppShamDailEntity();
										t.setUserid(userId);
										t.setVuserid(anchor.getUserid());
										t.setCreate_time(new Date());
										appShamDailContract.insert(t);
									}
								} else {
									//设置切断标记
									control = 2;
								}
							}
						} else {
							//已经进行了充值操作 就不进行发送数据了   设置切断标记
							control = 2;
						}
					} else {
						//主播端不需要弹出
						control = 2;
					}
				}
			}
		}
		Map<String, Object> result = new HashMap<>();
		result.put("control", control);
		result.put("talant", talant);
		return ActionResult.success(result);
	}
	
	@Override
	public ActionResult chatStart(String body) throws Exception {
		JSONObject obj = JsonHelper.toJsonObject(body);
		int count = obj.getIntValue("count");
		long userId = RequestUtils.getCurrent().getUserid();
		UserBO bo = userAgent.findById(userId);
		if(Tools.isNull(bo) || bo.isWaiter()) {
			//主播或者非法用户 停止对应的定时器
			Map<String, Object> result = new HashMap<>();
			result.put("control", 1);
			return ActionResult.success(result);
		}
		boolean flag = false;
		flag = channelCheckService.checkChannel();
		
		//检测后台是否配置了屏蔽机制
		try {
			SysConfigEntity config = sysConfigContract.findByProperty("name", com.tigerjoys.shark.miai.agent.constant.Const.APP_ROBOT_COFIG);
			JSONObject ctrl = JsonHelper.toJsonObject(config.getValue());
			if(Tools.isNotNull(ctrl)) {
				int scence = ctrl.getIntValue("scence");
				if (scence == 0) {
					flag = true;
				}
			}
		} catch (Exception e) {
			
		}
		/**
		 * 暂时使用代码写死的方式来取消 15个场景消息
		 */
		//flag = true;
		
		//单独处理特殊渠道
		RequestHeader header = RequestUtils.getCurrent().getHeader();
		if(Tools.isNotNull(header) && "Huawei_moyue".equals(header.getChannel())) {
			flag = true;
		}
		
		if(flag) {
			//提审状态直接停止对应的定时器
			Map<String, Object> result = new HashMap<>();
			result.put("control", 1);
			return ActionResult.success(result);
		}
		
		//处理特殊账号通过
		/*
		if(userId != 169984609793147392L) {
			Map<String, Object> result = new HashMap<>();
			result.put("control", 1);
			return ActionResult.success(result);
		}
		*/
		
		long flower = redFlowerAgent.getRedFlowerDeposit(userId);
		long diamond = userDiamondAgent.getDiamondDeposit(userId);
		//检测是否已经进行充值了
		if(flower > 5 || diamond > 200) {
			//进行充值了 就停止对应的定时器了
			Map<String, Object> result = new HashMap<>();
			result.put("control", 1);
			return ActionResult.success(result);
		}
		PageModel pageModel = PageModel.getPageModel();
		pageModel.addQuery(Restrictions.eq("userid", userId));
		long num = appDailScenceContract.count(pageModel);
		if(num >= 15) {
			//检测是否已经使用完所有的场景
			Map<String, Object> result = new HashMap<>();
			result.put("control", 1);
			return ActionResult.success(result);
		}
		String key = AgentRedisCacheConst.REDIS_USER_SCENCE_FLAG_PREFIX + userId;
		int control = 0;
		//进行次数方面的统计
		if(count % 2 == 1) {
			if(count == 1) {
				//首次启动确认场景
				if(num > 0) {
					anchorOnlineCacheRedis.set(key, 6 + "");
				} else {
					anchorOnlineCacheRedis.set(key, 4 + "");
				}
			}
		} else {
			Map<String, Object> result = new HashMap<>();
			result.put("control", 0);
			return ActionResult.success(result);
		}
		//设置默认次数值
		int n = 4;
		String c = anchorOnlineCacheRedis.get(key);
		if(Tools.isNotNull(c)) {
			n = Integer.parseInt(c);
		}
		if(count > n) {
			//本次对应的启动次数已经到了
			Map<String, Object> result = new HashMap<>();
			result.put("control", 1);
			return ActionResult.success(result);
		}
		
		pageModel.clearAll();
		pageModel.addQuery(Restrictions.eq("userid", userId));
		List<AppDailScenceEntity> list = appDailScenceContract.load(pageModel);
		//确定已经使用过的主播和场景
		List<Long> anchorids = new ArrayList<>();
		//添加特殊的屏蔽主播
		anchorids.add(153165189141823744L);
		
		Map<Integer, Integer> scences = new HashMap<>();
		if(Tools.isNotNull(list)) {
			for (AppDailScenceEntity item : list) {
				anchorids.add(item.getAnchorid());
				scences.put(item.getScence(), item.getScence());
			}
		}
		
		//处理随机场景问题
		List<Integer> sc = new ArrayList<>();
		for(int i = 0; i < 15; i++) {
			if(!Tools.isNotNull(scences.get(i))) {
				sc.add(i);
			}
		}
		
		if(Tools.isNotNull(sc) && sc.size() > 0) {
			Collections.shuffle(sc);
		} else {
			//检测是否已经使用完所有的场景
			Map<String, Object> result = new HashMap<>();
			result.put("control", 1);
			return ActionResult.success(result);
		}
		
		//获取随机到的场景
		int scence = sc.get(0);
		//进行随机主播处理
		long anchorid = 0;
		//首先随机一个在线的主播
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY)-48);
		for(int i = 3; i >= 0; i--) {
			pageModel.clearAll();
			if(Tools.isNotNull(anchorids))
				pageModel.addQuery(Restrictions.notin("userid", anchorids));
			pageModel.addQuery(Restrictions.eq("state", 1));
			pageModel.addQuery(Restrictions.eq("online", i));
			pageModel.addQuery(Restrictions.gt("update_time", calendar.getTime()));
			pageModel.addQuery(Restrictions.sql("flag=0 order by rand() limit 1"));
			List<AnchorOnlineEntity> anchors = anchorOnlineContract.load(pageModel);
			if(Tools.isNotNull(anchors) && anchors.size() > 0) {
				anchorid = anchors.get(0).getUserid();
				break;
			}
		}
		
		if(anchorid == 0) {
			//检测主播资源出现了问题
			Map<String, Object> result = new HashMap<>();
			result.put("control", 1);
			return ActionResult.success(result);
		}

		//修改逻辑 首先启动插入数据进行占位的处理
		try {
			AppDailScenceEntity t = new AppDailScenceEntity();
			t.setUserid(userId);
			t.setAnchorid(anchorid);
			t.setCreate_time(new Date());
			t.setScence(scence);
			t.setScence_index(0);
			t.setScence_type(ScenceMessageTypeEnum.text.getCode());
			t.setState(0);
			t.setSend_time(new Date());
			appDailScenceContract.insert(t);
			
			//在占位操作成功之后进行发送消息操作处理
			//此处场景和主播资源都已经获取到了 触发对应场景的第一条消息
			List<SceneMessageDto> messages = data.get(scence);
			if(Tools.isNotNull(messages)) {
				SceneMessageDto message = messages.get(0);
				//根据消息类型进行发送对应的消息
				if(message.getType() == ScenceMessageTypeEnum.text.getCode()) {
					UserTextChatInfoLogEntity log = new UserTextChatInfoLogEntity();
					log.setId(IdGenerater.generateId());
					log.setUser_id(anchorid);
					log.setOther_id(userId);
					log.setFlower(0L);
					log.setChat_text(message.getContent());
					log.setCheck_taskid("");
					log.setCheck_status(TextAudioMsgEnum.success.getCode());
					log.setCheck_type("");
					log.setCheck_type_code(0);
					
					log.setCreate_time(new Date());
					userTextChatInfoLogContract.insert(log);
					neteaseAgent.pushOneMessage(anchorid, userId, message.getContent(), true);
				} else if(message.getType() == ScenceMessageTypeEnum.picture.getCode()) {
					
				} else if(message.getType() == ScenceMessageTypeEnum.video.getCode()) {
					
				} else if(message.getType() == ScenceMessageTypeEnum.audio.getCode()) {
					long audioId = IdGenerater.generateId();
					UserAudioChatInfoLogEntity audio = new UserAudioChatInfoLogEntity();
					audio.setId(audioId);
					audio.setUser_id(anchorid);
					audio.setOther_id(userId);
					audio.setAudio_url(message.getAudioUrl());
					audio.setAudio_time(message.getTime());
					audio.setCheck_taskid(audioId+"");
					audio.setFlower(0L);
					audio.setCheck_status(TextAudioMsgEnum.success.getCode());
					audio.setCreate_time(new Date());
					textAutioMsgAgent.addAutioMsg(audio);
					neteaseAgent.sendAudioMsg(anchorid, userId, audioId, message.getAudioUrl(), message.getTime(), true);
				} else if(message.getType() == ScenceMessageTypeEnum.textPostion.getCode()) {
					
				}
			}
		} catch (Exception e) {
			logger.error("有可能出现了重复场景的问题 userid"+userId +" otherid:"+anchorid +" index:" + scence);
		}
		
		logger.error("处理的用户:"+userId+"  请求的次数:"+count+" 下发的控制:"+control);
		Map<String, Object> result = new HashMap<>();
		result.put("control", control);
		return ActionResult.success(result);
	}
	
	@Override
	public ActionResult videoFail(String body) throws Exception {
		//触发后期的假消息场景
		JSONObject obj = JsonHelper.toJsonObject(body);
		int index = obj.getIntValue("index");
		long userId = RequestUtils.getCurrent().getUserid();
		logger.error("处理的用户:"+userId+"  请求的参数:"+index);
		PageModel pageModel = PageModel.getPageModel();
		pageModel.addQuery(Restrictions.eq("userid", userId));
		pageModel.addQuery(Restrictions.eq("scence", index));
		pageModel.addQuery(Restrictions.eq("scence_type", ScenceMessageTypeEnum.video.getCode()));
		List<AppDailScenceEntity> scences = appDailScenceContract.load(pageModel);
		if(Tools.isNotNull(scences)) {
			for (AppDailScenceEntity scence : scences) {
				scence.setSend(1);
				appDailScenceContract.update(scence);
				//处理发送消息
				neteaseAgent.pushOneMessage(scence.getAnchorid(), scence.getUserid(), "通话已拒绝", true);
			}
		}
		return ActionResult.success();
	}

	@Override
	public ActionResult anchorNewSlideList(long userid, int tag, int type, String stamp) throws Exception {
		if(tag == 0){
			return aSlideNewAnchorHomeList(type, stamp);
		}
		return ActionResult.success();
	}
	
	private ActionResult aSlideNewAnchorHomeList(Integer type, String stamp) throws Exception {
		AhchorShortVideoDto outDto = new AhchorShortVideoDto();
		outDto.setType(type);
		boolean next = false;
		StringBuffer stampBuffer = new StringBuffer(Tools.isNull(stamp)?"":stamp);
		List<AnchorVideoWatchDto> videoWatchList = new ArrayList<>();
		List<AnchorOnlineEntity> anchorList = null;
		//开发初期的第一个type值
		if(type == 0) {
			//处理不同类别数据
			anchorList = getNewTabAnchors(type,stampBuffer);
		}
		if(Tools.isNotNull(anchorList) && anchorList.size() > 0) {
			//循环处理进行封装数据
			for(int i = 0; i < anchorList.size() && i < 100; i++) {
				AnchorOnlineEntity re = anchorList.get(i);
				if(Tools.isNotNull(re)) {
					AnchorVideoWatchDto dto = AnchorVideoWatchDto.preDto(re,null,0, false);
					if(Tools.isNotNull(dto)) {
						if(re.getCharm().intValue() > 0) {
							UserBaseInfo info = dto.getUserInfo();
							if(Tools.isNotNull(info)) {
								//info.setBackText1("魅力值："+re.getCharm()*19);
							}
						}
						videoWatchList.add(dto);
					}
				}
			}
		}
		outDto.setAnchorList(videoWatchList);
		return ActionResult.success(outDto, null, next);
	}
	
	private List<AnchorOnlineEntity> getNewTabAnchors(int type, StringBuffer stamp) throws Exception {
		//显示正常的数据
		int index = 1;
		String query = null;
		if(Tools.isNotNull(stamp) && stamp.length() > 0) {
			JSONObject json = JsonHelper.toJsonObject(stamp.toString());
			index = json.getIntValue("index");
			query = json.getString("query_time");
		}
		//根据对应的tag来查询对应的时间
		if(Tools.isNull(query)) {
			query = anchorOnlineCacheRedis.get("new_anchor_online_list_"+type);
		}
		//直接没有对应的查询信息 直接不返回数据就可以了
		if(Tools.isNull(query))
			return null;
		//配置对应的查询key值
		String key = "new_anchor_online_state_" + type + "_" + query;
		//一次获取200个主播信息
		Set<Tuple> returnSet = anchorOnlineCacheRedis.zrangeByScoreWithScores(key, index, index+200, 0, 201);
		List<AnchorOnlineEntity> datas = null;
		int i = 0;
		if(Tools.isNotNull(returnSet)) {
			datas = new ArrayList<>();
			for (Tuple tuple : returnSet) {
				if(Tools.isNotNull(tuple)) {
					String data = tuple.getElement();
					index = (int) tuple.getScore();
					AnchorOnlineEntity anchor = JsonHelper.toObject(data, AnchorOnlineEntity.class);
					if(Tools.isNotNull(anchor)) {
						//处理假主播问题
						if(anchor.getFlag().intValue() == 4) {
							//此种类型下只显示在聊
							if(anchor.getOnline().intValue() == 2) {
								i++;
								datas.add(anchor);
							}
						} else if(anchor.getFlag().intValue() == 6) {
							//此种类型下只显示在聊
							if(anchor.getOnline().intValue() == 2) {
								i++;
								datas.add(anchor);
							}
						} else {
							i++;
							datas.add(anchor);
						}
					}
				}
				//检测是否满足了拼接的100条数据
				if(i >= 100)
					break;
			}
			//配置对应的戳对象
			Map<String, String> stampN = new HashMap<>();
			stampN.put("query_time", query);
			stampN.put("index", index + "");
			stamp.delete(0, stamp.length()).append(JsonHelper.toJson(stampN));
		}
		return datas;
	}

}
