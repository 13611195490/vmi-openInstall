package com.tigerjoys.shark.miai.service.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.tigerjoys.nbs.common.utils.Tools;
import com.tigerjoys.shark.miai.agent.enums.AnchorAppointTypeEnum;
import com.tigerjoys.shark.miai.inter.contract.IAnchorAppointContract;
import com.tigerjoys.shark.miai.inter.entity.AnchorAppointEntity;
import com.tigerjoys.shark.miai.utils.BaseTestConfig;

/**
 * 约会数据生成用例
 */
public class AnchorAppointTest extends BaseTestConfig {

	@Autowired
	private IAnchorAppointContract anchorAppointContract;
	
	@Test
	public void createAnchorAppointData() throws Exception {
		int index = 0;
		List<Long> userids = new ArrayList<>();
		/*
		userids.add(65418691671490304L);
		userids.add(65418693063999232L);
		userids.add(65418693535858432L);
		userids.add(65418693967871744L);
		userids.add(65418697029713664L);
		userids.add(65418697558195968L);
		userids.add(65418698199924480L);
		userids.add(65418701460995840L);
		userids.add(65418701876231936L);
		userids.add(65418702280982272L);
		userids.add(65418702671052544L);
		userids.add(65418703048539904L);
		userids.add(65418703377792768L);
		userids.add(65418703755280128L);
		userids.add(65418704120184576L);
		userids.add(65418704462020352L);
		userids.add(65418704805953280L);
		userids.add(65418712749965056L);
		userids.add(65418713056149248L);
		userids.add(65418713444122368L);
		userids.add(65418717902667520L);
		userids.add(65418718194171648L);
		userids.add(65418718835900160L);
		userids.add(65418719110627072L);
		userids.add(65418719477628672L);
		userids.add(65418719867698944L);
		userids.add(97870961675599616L);
		userids.add(97870975424528128L);
		userids.add(97870988282167040L);
		userids.add(97871001259343616L);
		userids.add(97871014255394560L);
		userids.add(97871027146587904L);
		userids.add(97871040023101184L);
		userids.add(97871052937363200L);
		userids.add(97871065855819520L);
		userids.add(97871078755401472L);
		userids.add(97871091711606528L);
		userids.add(97871104554565376L);
		userids.add(97871117433175808L);
		userids.add(97871130328563456L);
		userids.add(97871143213465344L);
		userids.add(66138512797270272L);
		userids.add(89140177645142272L);
		userids.add(85346323880018176L);
		userids.add(89136902629818624L);
		userids.add(85157397089288448L);
		userids.add(95490958562492672L);
		userids.add(89294704962765056L);
		userids.add(95661110660759808L);
		userids.add(95663363241083136L);
		userids.add(95667131265384704L);
		userids.add(89137489406656768L);
		userids.add(95662085328929024L);
		userids.add(67183208860549376L);
		userids.add(90380078321565952L);
		userids.add(32441318881952000L);
		userids.add(36775801156337920L);
		userids.add(95852748465045760L);
		userids.add(95853218430517504L);
		userids.add(96750263429300480L);
		userids.add(98036383414812928L);
		userids.add(98034860381241600L);
		userids.add(85545876916863232L);
		userids.add(98932769570226432L);
		userids.add(98933041510023424L);
		userids.add(98041328406954240L);
		userids.add(33697911852302592L);
		userids.add(99666199784259840L);
		userids.add(99675794160288000L);
		userids.add(92584844218925312L);
		userids.add(85545876916863232L);
		userids.add(98932769570226432L);
		userids.add(98933041510023424L);
		userids.add(98041328406954240L);
		userids.add(33697911852302592L);
		userids.add(99666199784259840L);
		userids.add(99675794160288000L);
		userids.add(92584844218925312L);
		*/
		userids.add(132484736267387136L);
		userids.add(132502929635606784L);
		userids.add(132506737633722624L);
		userids.add(134282491784659200L);
		userids.add(137224557351207168L);
		userids.add(139300336738304256L);
		userids.add(139735307573592320L);
		userids.add(140643866800881920L);
		userids.add(141779275492688128L);
		userids.add(143697638542344448L);
		userids.add(143855966188798208L);
		userids.add(146440642122285312L);
		userids.add(149152702933303552L);
		userids.add(149680189509533952L);
		userids.add(144173658772930816L);
		userids.add(150312021009695232L);
		userids.add(151698164458520832L);
		userids.add(142276339156713728L);
		userids.add(152464049752310272L);
		userids.add(153165189141823744L);
		userids.add(154392008136392960L);
		userids.add(154742452471792128L);
		userids.add(155821949130441216L);
		userids.add(155974754432516352L);
		userids.add(156413764051206400L);
		userids.add(156718303220400640L);
		userids.add(157229750681666048L);
		userids.add(157894367143592448L);
		userids.add(158238042711196160L);
		userids.add(160755361148633344L);
		userids.add(160921835901354496L);
		userids.add(161342084782031360L);
		userids.add(162558127615574272L);
		userids.add(163185760887963904L);
		userids.add(162823974846267904L);
		userids.add(164160844482019584L);
		userids.add(164164235851530496L);
		userids.add(139722947884548352L);
		userids.add(166113026519400960L);
		userids.add(166533516276859136L);
		userids.add(167569303418241280L);
		userids.add(168785326404337920L);
		userids.add(169462293290091008L);
		userids.add(169468546829582592L);
		userids.add(165160950115140096L);
		userids.add(171270174628970752L);
		userids.add(171821639953089024L);
		userids.add(171973513186443776L);
		userids.add(172162560240452096L);
		userids.add(136992036243964160L);
		userids.add(164215172412539136L);
		userids.add(173005408844185856L);
		userids.add(150024574795317504L);
		userids.add(150028220176072960L);
		userids.add(150028437881422080L);
		userids.add(150028852995883264L);
		userids.add(150029075723911424L);
		userids.add(150029601786102016L);
		userids.add(150029854773936384L);
		userids.add(150030168933597440L);
		userids.add(150030812692152576L);
		userids.add(150031225357140224L);
		userids.add(150031527628046592L);
		userids.add(150031973136531712L);
		userids.add(150032306824872192L);
		userids.add(150032561993744640L);
		userids.add(150032829567271168L);
		userids.add(150033214887493888L);
		userids.add(150033704545222912L);
		userids.add(150034342645661952L);
		userids.add(150034633661153536L);
		userids.add(150035547167981824L);
		userids.add(150035816628945152L);
		userids.add(150036373680750848L);
		userids.add(150037273644171520L);
		userids.add(150037648153575680L);
		userids.add(150038579551207680L);
		userids.add(150039359609962752L);
		userids.add(150040318547394816L);
		userids.add(152396055468638464L);
		userids.add(152396187928953088L);
		userids.add(152396289477247232L);
		userids.add(152396411405664512L);
		userids.add(152396545948451072L);
		userids.add(152396732223783168L);
		userids.add(152396868534468864L);
		userids.add(152397159516406016L);
		userids.add(152397698977300736L);
		userids.add(152398007915053312L);
		
		List<AnchorAppointEntity> list = new ArrayList<AnchorAppointEntity>();
		List<String> contents = new ArrayList<>();
		contents.add("相爱是什么感觉？就像你为我做的每一道菜给我带来的温馨和甜蜜，有兴趣认识一下吗？一起吃个饭");
		contents.add("炸鸡啤酒来一套，不知道他家做的怎么样。还是挺期待的，约个时间一起去吃吃呗");
		contents.add("为什么这个软件上面有那么多好吃的，搞得我都想吃一遍");
		contents.add("去吃完这次在减肥，怎么样，就这么愉快的决定了");
		contents.add("无辣不欢无麻不乐，寻一男士，只限男士应约");
		contents.add("有兴趣一起的吗？顺便交个朋友，一个人有点寂寞");
		contents.add("哈哈，很喜欢吃他家的，有男士和我一起吗");
		contents.add("有没有一起吃个饭看电影撸串的同伴呢？约个呗");
		contents.add("唯有美食不能够辜负，不在乎身形，喜欢就好，找吃货");
		contents.add("好饿，去吃个饭吧，约个人，快点，约好就去");
		contents.add("找个吃货，然后我们一起吃遍所有的好吃的，怎么样");
		contents.add("很喜欢吃火锅，谁带我去好好吃呀，看见了就是缘分，不要犹豫了");
		contents.add("知道吗？我在静静的坐着等待，等你的相约");
		contents.add("早就想去吃了，居然这里也有推荐，择日不如撞日，有人一起去吃吗？约个吧");
		contents.add("心情不好，有人陪我吃饭吗？");
		contents.add("真心找个男票，有没有想法一致的，一起脱单");
		contents.add("吃鱼啦，听说吃鱼有很多好处呢，想有个人陪我");
		contents.add("有约会的吗？一起吃个饭可好");
		contents.add("哪个哥哥带我去吃好吃的呀，自己一个人好孤单呀");
		contents.add("就是想找个人一起吃个饭，恰巧你看见的话就是缘分，一起去呗");
		contents.add("先吃饭再聊天，一起畅谈人生，喜欢逗比模式的聊天");
		contents.add("听说心情不好的时候，就要吃甜点，有人请我吗");
		contents.add("带我去吃饭的都是好男人");
		contents.add("找个环境不错的餐厅吃饭，一起聊聊天，有小哥哥吗？");
		contents.add("重口味，有喜欢的吗？");
		contents.add("喜欢交朋友，一起去吃个饭吧");
		for (String content : contents) {
			AnchorAppointEntity t = new AnchorAppointEntity();
			t.setUserid(userids.get(index));
			t.setContent(content);
			t.setType(AnchorAppointTypeEnum.food.getCode());
			
			t.setEnd_time(Tools.getdate(new Date(), getRandomNumber(3, 9)));
			t.setCreate_time(new Date());
			list.add(t);
			index++;
		}
		
		
		contents = new ArrayList<>();
		contents.add("想看电影想吃爆米花，想找个男票一起去，有报名的吗?");
		contents.add("一起去看电影吧，看什么不重要，重要的是看电影的人");
		contents.add("最近有什么好看的电影吗？");
		contents.add("今晚有谁和我一起去看电影呀？");
		contents.add("小哥哥们，今天有时间哦，吃饭看电影，速报名哦~");
		contents.add("有谁要约我的吗？");
		contents.add("今天心情不是很好，有没有陪我的？");
		contents.add("好无聊，最近有没有好看的电影");
		contents.add("有喜欢看浪漫电影的吗");
		contents.add("赶紧约我，只通过帅的男票");
		for (String content : contents) {
			AnchorAppointEntity t = new AnchorAppointEntity();
			t.setUserid(userids.get(index));
			t.setContent(content);
			t.setType(AnchorAppointTypeEnum.movie.getCode());
			
			t.setEnd_time(Tools.getdate(new Date(), getRandomNumber(3, 9)));
			t.setCreate_time(new Date());
			list.add(t);
			index++;
		}
		
		contents = new ArrayList<>();
		contents.add("心情不好，一起喝点小酒，有人作陪吗？");
		contents.add("当你读这条信息，你已欠我一个约会，什么时候见呀");
		contents.add("想去酒吧了，嘿嘿，找个时间去艳遇、艳遇、艳遇、重要的事情说3遍");
		contents.add("最近心情不好，找个人聊会天，谁有空呀");
		contents.add("真心交朋友，有谁陪我去泡吧吗");
		contents.add("好无聊，有约的吗？");
		contents.add("文艺青年一枚，找共同爱好的人");
		contents.add("摇晃的红酒杯，，，寻找刺激");
		contents.add("今晚的夜谁来陪");
		contents.add("少一点套路，多一点真诚，不喜欢做作的人");
		contents.add("莫名烦躁，找个地方喝酒");
		contents.add("一个人在家好无聊，谁带我去泡吧呀");
		contents.add("浪起来，有约的吗");
		contents.add("晚上交个朋友，认识下，有一起嗨的吗");
		contents.add("酒吧附件约起来，有一起的吗");
		for (String content : contents) {
			AnchorAppointEntity t = new AnchorAppointEntity();
			t.setUserid(userids.get(index));
			t.setContent(content);
			t.setType(AnchorAppointTypeEnum.bar.getCode());
			
			t.setEnd_time(Tools.getdate(new Date(), getRandomNumber(3, 9)));
			t.setCreate_time(new Date());
			list.add(t);
			index++;
		}
		
		contents = new ArrayList<>();
		contents.add("有人一起去逛街吗?");
		contents.add("想买个口红，有谁陪我去吗");
		contents.add("突然想到游乐园里面玩了，有小哥哥一起去的吗？");
		contents.add("听说万达里面有好玩的，一起玩的速约");
		contents.add("有没有小哥哥陪我呀");
		contents.add("好无聊呀");
		contents.add("有谁给我买件衣服吗？答应你的所有要求哦~");
		for (String content : contents) {
			AnchorAppointEntity t = new AnchorAppointEntity();
			t.setUserid(userids.get(index));
			t.setContent(content);
			t.setType(AnchorAppointTypeEnum.street.getCode());
			
			t.setEnd_time(Tools.getdate(new Date(), getRandomNumber(3, 9)));
			t.setCreate_time(new Date());
			list.add(t);
			index++;
		}
		
		contents = new ArrayList<>();
		contents.add("烧脑游戏，周末很是无聊，在家多无聊呀");
		contents.add("阳光灿烂，这么好的天气不约岂不是太可惜了");
		contents.add("周末就出去浪");
		contents.add("有打游戏的小哥哥吗，教我一下呗");
		contents.add("有玩密室逃脱的吗，想去玩");
		contents.add("小哥哥在哪儿呢？周末约起来");
		for (String content : contents) {
			AnchorAppointEntity t = new AnchorAppointEntity();
			t.setUserid(userids.get(index));
			t.setContent(content);
			t.setType(AnchorAppointTypeEnum.paly.getCode());
			
			t.setEnd_time(Tools.getdate(new Date(), getRandomNumber(3, 9)));
			t.setCreate_time(new Date());
			list.add(t);
			index++;
		}
		
		contents = new ArrayList<>();
		contents.add("可惜不是你，陪我到最后，谁陪我唱到最后");
		contents.add("唱的不是很好听，但就是突然想唱歌了，你陪我去吗");
		contents.add("谁陪我去ktv把五月天的所有歌都唱一遍，快来约我吧");
		contents.add("纯唱歌，不要问我为什么，就是想唱歌了");
		contents.add("有哪位小哥哥想听我唱歌呀");
		contents.add("无聊死了，约我一起去唱歌吧");
		for (String content : contents) {
			AnchorAppointEntity t = new AnchorAppointEntity();
			t.setUserid(userids.get(index));
			t.setContent(content);
			t.setType(AnchorAppointTypeEnum.sing.getCode());
			
			t.setEnd_time(Tools.getdate(new Date(), getRandomNumber(3, 9)));
			t.setCreate_time(new Date());
			list.add(t);
			index++;
		}
		
		contents = new ArrayList<>();
		contents.add("嘿嘿，一起去运动运动呗，好久没有运动了");
		contents.add("来一场激烈运动，有报名的吗？");
		contents.add("有打羽毛球的吗？");
		contents.add("有一同去健身的吗？想保持好身材");
		for (String content : contents) {
			AnchorAppointEntity t = new AnchorAppointEntity();
			t.setUserid(userids.get(index));
			t.setContent(content);
			t.setType(AnchorAppointTypeEnum.sport.getCode());
			
			t.setEnd_time(Tools.getdate(new Date(), getRandomNumber(3, 9)));
			t.setCreate_time(new Date());
			list.add(t);
			index++;
		}
		
		anchorAppointContract.insertAll(list);
	}
	
	public int getRandomNumber(int min, int max) {
		Random random = new Random();  
		int randomNumber =  random.nextInt(max)%(max-min+1) + min; 
		return randomNumber;
	}
}
