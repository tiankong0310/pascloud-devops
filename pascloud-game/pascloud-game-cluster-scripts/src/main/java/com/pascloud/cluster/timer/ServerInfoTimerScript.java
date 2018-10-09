package com.pascloud.cluster.timer;

import java.time.LocalTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pascloud.cluster.manager.ServerManager;
import com.pascloud.core.email.MailManager;
import com.pascloud.core.script.ITimerEventScript;

public class ServerInfoTimerScript implements ITimerEventScript {
	private static final Logger LOGGER=LoggerFactory.getLogger(ServerInfoTimerScript.class);

	@Override
	public void secondHandler(LocalTime localTime) {
//		LOGGER.debug("serverInfo");
	}

	@Override
	public void minuteHandler(LocalTime localTime) {
		ITimerEventScript.super.minuteHandler(localTime);
	}

	@Override
	public void hourHandler(LocalTime localTime) {
		ServerManager.getInstance().getServers().values().forEach(map -> map.forEach((serverId, serverInfo) -> {
			// 发送服务器内存使用量不足
			double freePro = serverInfo.getFreeMemory() * 1.0 / serverInfo.getTotalMemory();
			if (freePro < 0.1d) {
				StringBuffer sb = new StringBuffer(serverInfo.getName());
				sb.append("<br>IP:").append(serverInfo.getIp()).append(" 空闲内存:").append(serverInfo.getFreeMemory())
						.append(" 总内存:").append(serverInfo.getTotalMemory());
				MailManager.getInstance().sendTextMailAsync("服务器内存不足", sb.toString(), "359135103@qq.com");
			}
		}));
	}

}