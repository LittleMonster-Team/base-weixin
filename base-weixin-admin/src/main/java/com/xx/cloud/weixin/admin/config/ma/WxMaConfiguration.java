package com.xx.cloud.weixin.admin.config.ma;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.message.WxMaMessageRouter;
import cn.hutool.json.JSONUtil;
import com.fly.cloud.common.core.constant.CommonConstants;
import com.xx.cloud.weixin.admin.service.WxAppService;
import com.xx.cloud.weixin.admin.service.WxUserService;
import com.google.common.collect.Maps;
import com.xx.cloud.weixin.admin.config.open.WxOpenConfiguration;
import com.xx.cloud.weixin.common.constant.WxMaConstants;
import com.xx.cloud.weixin.common.entity.ThirdSession;
import com.xx.cloud.weixin.common.entity.WxApp;
import com.xx.cloud.weixin.common.entity.WxUser;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 小程序Configuration
 * @author JL
 *
 */
@Configuration
public class WxMaConfiguration {
	/**
	 * 全局缓存WxMaService
	 */
	private static Map<String, WxMaService> maServices = Maps.newHashMap();

	/**
	 * 全局缓存WxMaMessageRouter
	 */
	private static Map<String, WxMaMessageRouter> routers = Maps.newHashMap();

	private static RedisTemplate redisTemplate;
	private static WxAppService wxAppService;
	private static WxUserService wxUserService;
	public WxMaConfiguration(RedisTemplate redisTemplate, WxAppService wxAppService,WxUserService wxUserService){
		this.redisTemplate = redisTemplate;
		this.wxAppService = wxAppService;
		this.wxUserService = wxUserService;
	}
	/**
	 *  获取全局缓存WxMaService
	 * @param appId
	 * @return
	 */
	public static WxMaService getMaService(String appId) {
		WxMaService wxMaService = maServices.get(appId);
		if(wxMaService == null) {
			WxApp wxApp = wxAppService.getById(appId);
			if(wxApp!=null) {
				if(CommonConstants.YES.equals(wxApp.getIsComponent())){//第三方平台
					wxMaService = WxOpenConfiguration.getOpenService().getWxOpenComponentService().getWxMaServiceByAppid(appId);
					maServices.put(appId, wxMaService);
					routers.put(appId, newRouter(wxMaService));
				}else{
					WxMaInRedisConfigStorage configStorage = new WxMaInRedisConfigStorage(redisTemplate);
					configStorage.setAppid(wxApp.getId());
					configStorage.setSecret(wxApp.getSecret());
					configStorage.setToken(wxApp.getToken());
					configStorage.setAesKey(wxApp.getAesKey());
					wxMaService = new WxMaServiceImpl();
					wxMaService.setWxMaConfig(configStorage);
					maServices.put(appId, wxMaService);
					routers.put(appId, newRouter(wxMaService));
				}
			}
		}
		return wxMaService;
	}

	/**
	 * 移除WxMaService缓存
	 * @param appId
	 */
	public static void removeWxMaService(String appId){
		maServices.remove(appId);
		routers.remove(appId);
	}

	private static WxMaMessageRouter newRouter(WxMaService service) {
		final WxMaMessageRouter router = new WxMaMessageRouter(service);
		return router;
	}

	/**
	 * 通过request获取appId
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public static String getAppId(HttpServletRequest request) {
		//https://servicewechat.com/wxd7da5b5a941bcc1c/devtools/page-frame.html
		String referer = request.getHeader("Referer");
		String appId = referer.replace("https://servicewechat.com/", "");
		appId = appId.substring(0, appId.indexOf("/"));
		return appId;
	}

	/**
	 * 通过request获取WxApp
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public static WxApp getApp(HttpServletRequest request) throws Exception {
		String appId = getAppId(request);
		WxApp wxApp = wxAppService.getById(appId);
		if(wxApp==null) {
			throw new Exception("系统内无此小程序：" + appId);
		}
		return wxApp;
	}

	/**
	 * 小程序登录
	 * @param wxApp
	 * @param jsCode
	 * @return
	 * @throws WxErrorException
	 */
	public static WxUser loginMa(WxApp wxApp, String jsCode) throws WxErrorException {
		WxMaJscode2SessionResult jscode2session = WxMaConfiguration.getMaService(wxApp.getId()).jsCode2SessionInfo(jsCode);
		WxUser wxUser = wxUserService.getByOpenId(wxApp.getId(),jscode2session.getOpenid());
		if(wxUser==null) {//新增用户
			wxUser = new WxUser();
			wxUser.setAppId(wxApp.getId());
			wxUser.setAppType(wxApp.getAppType());
			wxUser.setOpenId(jscode2session.getOpenid());
			wxUser.setSessionKey(jscode2session.getSessionKey());
			wxUser.setUnionId(jscode2session.getUnionid());
			wxUserService.save(wxUser);
		}else {//更新SessionKey
			wxUser.setAppId(wxApp.getId());
			wxUser.setAppType(wxApp.getAppType());
			wxUser.setOpenId(jscode2session.getOpenid());
			wxUser.setSessionKey(jscode2session.getSessionKey());
			wxUser.setUnionId(jscode2session.getUnionid());
			wxUserService.updateById(wxUser);
		}

		String thirdSession = UUID.randomUUID().toString();
		ThirdSession thirdSessionData = new ThirdSession();
		thirdSessionData.setAppId(wxApp.getId());
		thirdSessionData.setSessionKey(wxUser.getSessionKey());
		thirdSessionData.setWxUserId(wxUser.getId());
		thirdSessionData.setOpenId(wxUser.getOpenId());
		//将3rd_session和用户信息存入redis，并设置过期时间
		String key = WxMaConstants.THIRD_SESSION_BEGIN + ":" + thirdSession;
		redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(thirdSessionData) , WxMaConstants.TIME_OUT_SESSION, TimeUnit.HOURS);
		wxUser.setSessionKey(thirdSession);
		return wxUser;
	}
}
