package com.pascloud.gate.server.handler;

import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;
import com.pascloud.core.mina.config.BaseServerConfig;
import com.pascloud.core.mina.config.MinaServerConfig;
import com.pascloud.core.mina.handler.DefaultProtocolHandler;
import com.pascloud.core.script.ScriptManager;
import com.pascloud.core.server.AbsService;
import com.pascloud.core.utils.MsgUtil;
import com.pascloud.game.model.constant.Config;
import com.pascloud.game.model.constant.Reason;
import com.pascloud.gate.script.IUserScript;
import com.pascloud.gate.struct.UserSession;
import com.pascloud.message.hall.HallLoginMessage.LoginRequest;

/**
 * websocket 消息处理器
 * 消息Id(4)+消息内容 
 * @see ClientProtocolHandler
 */
public class GateWebSocketUserServerHandler extends DefaultProtocolHandler {
	private static final Logger LOGGER=LoggerFactory.getLogger(GateWebSocketUserServerHandler.class);
	protected AbsService<MinaServerConfig> service;
	
	public GateWebSocketUserServerHandler() {
		super(4);
	}

	/**
	 * 消息转发到大厅服或游戏服务器
	 * 
	 * @param bytes
	 *            前4字节分别为消息ID
	 */
	@Override
	protected void forward(IoSession session, int msgID, byte[] bytes) {
		// 转发到大厅服
		if (msgID < Config.HALL_MAX_MID) {
			forwardToHall(session, msgID, bytes);
			return;
		}
		// 转发到游戏服
		Object attribute = session.getAttribute(Config.USER_SESSION);
		if (attribute != null) {
			UserSession userSession = (UserSession) attribute;
			if (userSession.getRoleId() > 0) {
				if (userSession.sendToGame(bytes)) {
					return;
				} else {
					LOGGER.warn("角色[{}]没有连接游戏服务器,消息{}发送失败", userSession.getRoleId(), msgID);
					return;
				}
			}

		}
		LOGGER.warn("{}消息[{}]未找到玩家:{}", MsgUtil.getIp(session), msgID,bytes.toString());
		try {
			LoginRequest loginRequest = LoginRequest.newBuilder().mergeFrom(bytes, 0, bytes.length).build();
			
			LOGGER.info(loginRequest.toString());
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 消息转发到大厅服务器
	 * 
	 * @param session
	 * @param msgID
	 * @param bytes
	 */
	private void forwardToHall(IoSession session, int msgID, byte[] bytes) {
		Object attribute = session.getAttribute(Config.USER_SESSION);
		if (attribute != null) {
			UserSession userSession = (UserSession) attribute;
			if (userSession.getRoleId() > 0) {
				if (!userSession.sendToHall( bytes)) {
					LOGGER.warn("角色[{}]没有连接大厅服务器", userSession.getRoleId());
					return;
				}else {
					return;
				}
			}
		}
		LOGGER.warn("[{}]消息未找到对应的处理方式", msgID);
	}
	
	@Override
	public void sessionOpened(IoSession session) {
		super.sessionOpened(session);
		UserSession userSession = new UserSession(session);
		session.setAttribute(Config.USER_SESSION, userSession);
	}

	@Override
	public void sessionClosed(IoSession session) {
		super.sessionClosed(session);
		LOGGER.debug("{}关闭连接",MsgUtil.getIp(session));
		ScriptManager.getInstance().getBaseScriptEntry().executeScripts(IUserScript.class,
				script -> script.quit(session, Reason.SessionClosed));
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus idleStatus) {
		super.sessionIdle(session, idleStatus);
		ScriptManager.getInstance().getBaseScriptEntry().executeScripts(IUserScript.class,
				script -> script.quit(session, Reason.SessionIdle));
	}

	@Override
	public AbsService<? extends BaseServerConfig> getService() {
		return service;
	}
	
	public void setService(AbsService<MinaServerConfig> service) {
		this.service = service;
	}

}
