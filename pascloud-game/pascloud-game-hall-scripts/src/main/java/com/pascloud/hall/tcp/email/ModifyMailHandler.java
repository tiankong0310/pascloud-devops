package com.pascloud.hall.tcp.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pascloud.core.handler.HandlerEntity;
import com.pascloud.core.handler.TcpHandler;
import com.pascloud.game.model.mongo.hall.dao.MailDao;
import com.pascloud.game.model.strut.Mail;
import com.pascloud.game.model.strut.Mail.MailType;
import com.pascloud.hall.manager.MailManager;
import com.pascloud.message.Mid.MID;
import com.pascloud.message.hall.HallChatMessage.ModifyMailRequest;
import com.pascloud.message.hall.HallChatMessage.ModifyMailResponse;

/**
 * 修改邮件状态
 */
@HandlerEntity(mid = MID.ModifyMailReq_VALUE, msg = ModifyMailRequest.class)
public class ModifyMailHandler extends TcpHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(ModifyMailHandler.class);

	@Override
	public void run() {
		ModifyMailRequest req = getMsg();
		Mail mail = MailManager.getInstance().getMail(req.getMailId());
		if (mail == null) {
			LOGGER.warn("{}请求邮件{}已不存在", rid, req.getMailId());
			return;
		}
		if (mail.getType() == MailType.PRIVATE.ordinal() && mail.getReceiverId() != rid) {
			LOGGER.warn("{}请求邮件{}不属于自己", rid, req.getMailId());
			return;
		}
		if (req.getState() <= mail.getState()) {
			LOGGER.warn("{}请求状态{}非法，邮件当前状态为{}", rid, req.getState(), mail.getState());
			return;
		}
		mail = MailDao.modifyMailState(mail.getId(), req.getState());
		if (mail.getState() != req.getState()) {
			LOGGER.warn("{}更新状态{}失败", rid, req.getState());
			return;
		}
		
		//TODO 领取物品等逻辑
		
		ModifyMailResponse.Builder builder = ModifyMailResponse.newBuilder();
		builder.setResult(0);
		sendIdMsg(builder.build());
	}

}
