package com.tigerjoys.shark.miai.agent.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.tigerjoys.shark.miai.agent.service.INewPushService;

/**
 * 向AndroidB2端推送消息服务类
 * @author liuman  
 *
 */
@Service
public class AndroidAPushMessageService extends AbstractPushMessageService{
	
	@Autowired
	@Qualifier("androidAXiaomiPush")
	private INewPushService androidXiaomiPush;
	
	@Override
	protected INewPushService getNewPushService() {
		return androidXiaomiPush;
	}
	
}
