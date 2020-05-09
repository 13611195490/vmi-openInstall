package org.shark.miai.common.enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.shark.miai.common.dto.KeyBean;

import com.tigerjoys.nbs.common.utils.JsonHelper;
import com.tigerjoys.nbs.common.utils.Tools;

/**
 * 首页活动区域app内部页面(android和ios)
 * @author liuman
 *
 */
public enum IndexActivityAreaEnum {
	
	//dateSomeone("dateSomeone" , "ServiceDetailFragment", "14","相约" , ""),
	webSingle("webSingle" , "WebSingleSaveFragment", "8","H5页面" , "1#H5地址,2#H5标题,3#参数"),
	//inviteFriends("inviteFriends" , "WebSingleSaveFragment", "8","邀请好友" , "1#H5地址,2#H5标题"),
	chargePage("chargePage" , "WalletFragment", "13","我的钱包" , "1#类型"),
	vipMemeberPage("vipMemeberPage" , "MyVipFragment", "15","vip会员" , ""),
	talentAuthentication ("talentAuthentication" , "WebSingleSaveFragment", "8","达人认证" , "1#H5地址,2#H5标题"),
	myCollect ("myCollect" , "DispatchCollectionFragment", "17","我的收藏" , ""),
	appRanking ("appRanking" , "LeaderBoardFragment", "23","排行榜" , ""),
	signTask ("signTask" , "DailyTaskFragment", "25","签到" , ""),
	weekCard ("weekCard" , "WeekCardFragment", "26","周卡" , ""),
	iosChargeDiamond ("iosChargeDiamond" , "diamondFragment", "27","IOS原生充值钻" , ""), //IOS IAP转用
	;
	
	private static final Map<String , List<KeyBean>> paramMap = new HashMap<>();
	
	//此处添加映射关系
	static {
		for(IndexActivityAreaEnum v : IndexActivityAreaEnum.values()) {
			List<KeyBean> list = new ArrayList<>();
			
			String[] sarray = Tools.split(v.getParam());
			if(Tools.isNotNull(sarray)) {
				for(String s : sarray) {
					String[] k = s.split("#");
					list.add(new KeyBean("strValue"+k[0], k[1]));
				}
			}
			
			paramMap.put(v.getCode(), list);
		}
	}
	
	private String code;
	private String androidPage;
	private String iosPage;
	private String desc;
	private String param;//参数名称

	private IndexActivityAreaEnum(String code,String androidPage , String iosPage ,String desc , String param) {
		this.code = code;
		this.androidPage = androidPage;
		this.iosPage = iosPage;
		this.desc = desc;
		this.param = param;
	}

	public static String getDescByCode(String code) {
		if(code == null) return null;
		for (IndexActivityAreaEnum refer : IndexActivityAreaEnum.values())
			if (code.equals(refer.getCode()))
				return refer.getDesc();
		return null;
	}
	
	public static String getAndroidPageByCode(String code) {
		if(code == null) return null;
		for (IndexActivityAreaEnum refer : IndexActivityAreaEnum.values())
			if (code.equals(refer.getCode()))
				return refer.getAndroidPage();
		return null;
	}
	
	public static String getIosPageByCode(String code) {
		if(code == null) return null;
		for (IndexActivityAreaEnum refer : IndexActivityAreaEnum.values())
			if (code.equals(refer.getCode()))
				return refer.getIosPage();
		return null;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getAndroidPage() {
		return androidPage;
	}

	public void setAndroidPage(String androidPage) {
		this.androidPage = androidPage;
	}

	public String getIosPage() {
		return iosPage;
	}

	public void setIosPage(String iosPage) {
		this.iosPage = iosPage;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}
	
	public static String getJsonParam(){
		return JsonHelper.toJson(paramMap);
	}
	
	/**
	 * 获得app内页的参数拼接列表
	 * @param key - String
	 * @return List<KeyBean>
	 */
	public static List<KeyBean> getParamKeyList(String key){
		return paramMap.get(key);
	}

}
