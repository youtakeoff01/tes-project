package com.edcs.tds.storm.util;

import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edcs.tds.storm.model.EmailEntity;
import com.sun.mail.util.MailSSLSocketFactory;

public class SendEmailTask implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(SendEmailTask.class);

	private EmailEntity emailEntity;

	public SendEmailTask(EmailEntity emailEntity) {
		this.emailEntity = emailEntity;
	}

	public SendEmailTask() {
	}

	public void sendEmail() throws Exception {
		Properties props = new Properties();
		props.setProperty("mail.transsport.protocol", "smtp");
		if (emailEntity.isBooeanSsl()) {
			MailSSLSocketFactory sf = null;
			try {
				sf = new MailSSLSocketFactory();
			} catch (GeneralSecurityException e) {
				e.printStackTrace();
			}
			sf.setTrustAllHosts(true);
			props.put("mail.smtp.ssl.enable", "true");
			props.put("mail.smtp.ssl.socketFactory", sf);

		}
		props.setProperty("mail.smtp.host", emailEntity.getSendServer());
		props.setProperty("mail.smtp.auth", "true");

		Session session = Session.getInstance(props);
		session.setDebug(true);
		MimeMessage message = createEmail(session, emailEntity.getSendAccount(), emailEntity.getReceiveAccounts(),
				emailEntity.getContent());

		Transport transport = session.getTransport();
		transport.connect(emailEntity.getSendAccount(), emailEntity.getEmailPassword());
		transport.sendMessage(message, message.getAllRecipients());
	}

	private MimeMessage createEmail(Session session, String sendMail, List<String> receiveAccounts, String content)
			throws Exception {
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(sendMail, "测试数据预警", "UTF-8"));// 邮件标题
		InternetAddress[] addresses = new InternetAddress[receiveAccounts.size()];
		for (int i = 0; i < receiveAccounts.size(); i++) {
			addresses[i] = new InternetAddress(receiveAccounts.get(i));
		}
		message.setRecipients(MimeMessage.RecipientType.TO, addresses);
		message.setSubject("测试数据预警信息", "UTF-8");// 邮件主题
		message.setContent(content, "text/html;charset=UTF-8");
		message.setSentDate(new Date());
		message.saveChanges();
		return message;
	}

	@Override
	public void run() {
		try {
			sendEmail();
		} catch (Exception e) {
			logger.error("发送邮件出现异常，异常信息为｛｝", e);
		}
	}

}
