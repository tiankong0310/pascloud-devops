package com.pascloud.hall.http.gm;

import java.lang.reflect.Method;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pascloud.core.handler.HandlerEntity;
import com.pascloud.core.handler.HttpHandler;
import com.pascloud.core.mina.config.MinaServerConfig;
import com.pascloud.core.redis.jedis.JedisManager;
import com.pascloud.core.utils.JsonUtil;
import com.pascloud.core.utils.MsgUtil;
import com.pascloud.game.model.constant.Config;
import com.pascloud.game.model.redis.key.HallKey;
import com.pascloud.hall.manager.RoleManager;
import com.pascloud.hall.server.HallServer;
import com.pascloud.message.ServerMessage.ServerEventRequest;

/**
 * GM处理器，所有gm在大厅处理，捕鱼游戏等用redis发布
 * <p>
 * 	http://192.168.0.17:9102/gm?cmd=addGold&roleId=1&gold=10000&auth=daa0cf5b-e72d-422c-b278-450b28a702c6
 * </p>
 */
@HandlerEntity(path="/gm")
public class GmHandler extends HttpHandler {
	private static final Logger LOGGER=LoggerFactory.getLogger(GmHandler.class);
	private static final Map<String, Method> GM_METHOD=JsonUtil.getFieldMethodMap(GmHandler.class, null);

	@Override
	public void run() {
		String auth = getString("auth");
		MinaServerConfig minaServerConfig = HallServer.getInstance().getHallHttpServer().getMinaServerConfig();
		if (!Config.SERVER_AUTH.equals(auth)||!minaServerConfig.getIp().startsWith("192")||!minaServerConfig.getIp().startsWith("127")) {
			sendMsg("权限验证失败");
			return;
		}
		String result = execute();
		
		LOGGER.info("{}使用GM结果:{}",MsgUtil.getIp(getSession()),result);
		if(getSession()!=null) {
			sendMsg(result);
		}
	}
	
	/**
	 * 执行命令
	 * @return
	 */
	public String execute() {
		String cmd = getString("cmd"); 	//命令，方法名称
		if(cmd.equalsIgnoreCase("execute")) {
			return "指令错误";
		}
		String result=String.format("GM %s 执行失败", getMessage().getQueryString());
		if(GM_METHOD.containsKey(cmd.trim())) {
			Method method = GM_METHOD.get(cmd);
			try {
				result=(String)method.invoke(this);
			} catch (Exception e) {
				LOGGER.error("GM",e);
			} 
		}
		return result;
	}
	
	
	/**
	 * 增减金币
	 * @return
	 */
	protected String addGold() {
		long roleId = getLong("roleId");
		int gold=getInt("gold");
		String key=HallKey.Role_Map_Info.getKey(roleId);
		JedisManager.getJedisCluster().hincrBy(key, "gold", gold);
		String info=String.format("角色%d增加%d金币", roleId,gold);
		RoleManager.getInstance().publishGoldChange(roleId, gold);
		return info;
	}
	
	/**
	 * 设置角色等级
	 * @return
	 */
	protected String setLevel() {
		long roleId = getLong("roleId");
		String level=getString("level");
		String key=HallKey.Role_Map_Info.getKey(roleId);
		JedisManager.getJedisCluster().hset(key, "level", level);
		String info=String.format("角色%d等级设为%d", roleId,level);
		//TODO 通知子游戏改变？
		return info;
		
	}
	
	/**
	 * 下线玩家
	 * @return
	 */
	protected String offLineRole() {
		long roleId = getLong("roleId");
		ServerEventRequest.Builder builder=ServerEventRequest.newBuilder();
		builder.setType(1);
		builder.setId(roleId);
		HallServer.getInstance().getHall2GateClient().broadcastMsg(builder.build(), roleId);
		return String.format("角色%d将被踢下线", roleId);
	}

}
