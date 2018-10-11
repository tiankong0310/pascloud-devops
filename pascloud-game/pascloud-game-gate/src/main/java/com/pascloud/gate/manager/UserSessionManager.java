package com.pascloud.gate.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.protobuf.Message;
import com.pascloud.core.script.ScriptManager;
import com.pascloud.game.model.constant.Reason;
import com.pascloud.gate.script.IUserScript;
import com.pascloud.gate.struct.UserSession;

/**
 * 用户连接会话管理类
 * 
 */
public class UserSessionManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(UserSessionManager.class);
	private static volatile UserSessionManager userSessionManager;

	/** 用户session key：sessionID */
	private Map<Long, UserSession> allSessions = new ConcurrentHashMap<>();

	/** 用户session key：userID */
	private Map<Long, UserSession> userSessions = new ConcurrentHashMap<>();

	/** 用户session key：roleID */
	private Map<Long, UserSession> roleSessions = new ConcurrentHashMap<>();

	private UserSessionManager() {
		super();
	}

	public static UserSessionManager getInstance() {
		if (userSessionManager == null) {
			synchronized (UserSessionManager.class) {
				if (userSessionManager == null) {
					userSessionManager = new UserSessionManager();
				}
			}
		}
		return userSessionManager;
	}

	/**
	 * 用户连接服务器
	 * 
	 * @param userSession
	 */
	public void onUserConnected(UserSession userSession) {
		if (userSession.getClientSession() != null) {
			allSessions.put(userSession.getClientSession().getId(), userSession);
		}
	}

	/**
	 * 登录大厅
	 * 
	 * @param session
	 */
	public void loginHallSuccess(UserSession userSession, long userId, long roleId) {
		userSession.setUserId(userId);
		userSession.setRoleId(roleId);
		// session.setAttribute(GateTcpUserServerHandler.USER_SESSION,
		// userSession);
		userSessions.put(userId, userSession);
		roleSessions.put(roleId, userSession);
	}

	public UserSession getUserSessionByUserId(long userId) {
		return userSessions.get(userId);
	}

	public UserSession getUserSessionbyRoleId(long roleId) {
		return roleSessions.get(roleId);
	}

	/**
	 * 用户session
	 * 
	 * @param sessionId
	 * @return
	 */
	public UserSession getUserSessionBySessionId(long sessionId) {
		return allSessions.get(sessionId);
	}

	public void quit(UserSession userSession, Reason reason) {
		allSessions.remove(userSession.getClientSession().getId());
		userSessions.remove(userSession.getUserId());
		roleSessions.remove(userSession.getRoleId());
	}
	
	/**
	 * 广播消息给前端客户端
	 * @param msg
	 */
	public void  broadcast(Message msg) {
		this.allSessions.values().forEach(session ->session.sendToClient(msg));
	}
	
	/**
	 * 所有在线人数
	 * @return
	 */
	public int getOlineCount() {
		return allSessions.size();
	}
	
	/**
	 * 服务器关闭
	 */
	public void onShutdown() {
		//踢出玩家
		for(UserSession userSession:allSessions.values()) {
			ScriptManager.getInstance().getBaseScriptEntry().executeScripts(IUserScript.class, script->script.quit(userSession.getClientSession(), Reason.ServerClose));
		}
	}
	
}
