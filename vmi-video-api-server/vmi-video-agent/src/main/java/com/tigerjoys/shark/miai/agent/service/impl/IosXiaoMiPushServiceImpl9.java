package com.tigerjoys.shark.miai.agent.service.impl;

import org.springframework.stereotype.Service;

import com.tigerjoys.shark.miai.agent.constant.PushConstant;
import com.xiaomi.xmpush.server.Sender;
import com.xiaomi.xmpush.server.Subscription;

/**
 * ios端小米推送服务类
 * 
 * @author liuman
 *
 */
@Service("iosXiaomiPush9")
public class IosXiaoMiPushServiceImpl9 extends IosXiaoMiPushServiceImpl {

	private static final String iosAccountSecret9 = PushConstant.ios_xiaomi_accountSecret9;
	
	@Override
	protected Sender initSender() {
		return new Sender(iosAccountSecret9);
	}
	
	@Override
	protected Subscription initSubscription() {
		return new Subscription(iosAccountSecret9);
	}
	
	@Override
	protected String implName() {
		return "iosXiaomiPush9";
	}
	
	
}
