package org.activiti.engine.impl.bpmn.behavior;

import java.util.ArrayList;
import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;


import org.activiti.engine.impl.bpmn.behavior.MailActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.util.UnicodePropsUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.liferay.mail.kernel.model.MailMessage;
import com.liferay.mail.kernel.service.MailServiceUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;

/** This is implementation of Mail Behavior implemented sending email via Liferay Service
 * 
 * As result, Liferay's settings (as well as implementation at all) used for sending email
 * 
 * @author akakunin
 *
 */
public class LiferayMailActivityBehavior extends MailActivityBehavior  {
	private static Log _log = LogFactoryUtil.getLog(LiferayMailActivityBehavior.class);

	@Override
	public void execute(ActivityExecution execution) {
		_log.debug("Execute Email Task");
		
	    String toStr = getStringFromField(to, execution);
	    String fromStr = getStringFromField(from, execution);
	    String ccStr = getStringFromField(cc, execution);
	    String bccStr = getStringFromField(bcc, execution);
	    String subjectStr = getStringFromField(subject, execution);
	    String textStr = getStringFromField(text, execution);
	    String htmlStr = getStringFromField(html, execution);
	    
	    InternetAddress[] internetAddressesFrom = getEmailAddresses(fromStr);
	    InternetAddress internetAddressFrom = internetAddressesFrom != null && internetAddressesFrom.length > 0 ? internetAddressesFrom[0] : null;
	    
	    InternetAddress[] internetAddressesTo = getEmailAddresses(toStr);
	    InternetAddress[] internetAddressesCc = getEmailAddresses(ccStr);
	    InternetAddress[] internetAddressesBcc = getEmailAddresses(bccStr);
	    
	    String body = StringUtils.isNotBlank(htmlStr) ? htmlStr : textStr;
	    boolean isHtml = StringUtils.isNotBlank(htmlStr);
	    
	    sendEmail(internetAddressFrom, internetAddressesTo, internetAddressesCc, internetAddressesBcc, subjectStr, body, isHtml);
	    
	    leave(execution);
	}
	
	
	protected InternetAddress[] getEmailAddresses(String str) {
		if (StringUtils.isBlank(str)) {
			return null;
		}
		
		String[] emails = str.split(",");
		List<InternetAddress> emailAddresses = new ArrayList<InternetAddress>();
		
		for(String email : emails) {
			try {
				emailAddresses.add(new InternetAddress(email));
			} catch (AddressException e) {
				_log.error("Failed to get email address: " + e);
			}
		}
		
		InternetAddress[] addresses = new InternetAddress[emailAddresses.size()];
		return emailAddresses.toArray(addresses);
	}
	
	/** Send email with using Liferay functionality
	 * 
	 * @param internetAddressesTo
	 * @param internetAddressesCc
	 * @param internetAddressesBcc
	 * @param internetAddressFrom
	 * @param subject
	 * @param body
	 * @param isHtml
	 */
	protected void sendEmail(InternetAddress internetAddressFrom,
								InternetAddress[] internetAddressesTo, 
								 InternetAddress[] internetAddressesCc,
								 InternetAddress[] internetAddressesBcc,
								 String subject, String body, boolean isHtml) {

		if (internetAddressFrom == null) {
			String fromAddr = PropsUtil.get(PropsKeys.ADMIN_EMAIL_FROM_ADDRESS);
			String fromName = UnicodePropsUtil.get(PropsKeys.ADMIN_EMAIL_FROM_NAME);

			try {
				internetAddressFrom = new InternetAddress(fromAddr, fromName);
			} catch (Exception e) {
				_log.error(String
						.format("Error occured, while trying to create internet address using [%s]: %s",
								fromAddr, e.getMessage()));
				return;
			}
		}

		// always send mail one-by-one
		for (InternetAddress ia : internetAddressesTo) {
			MailMessage mailMessage = new MailMessage();
			
			mailMessage.setFrom(internetAddressFrom);
			mailMessage.setBody(body);
			mailMessage.setSubject(subject);
			mailMessage.setHTMLFormat(isHtml);

			InternetAddress[] iAddresses = new InternetAddress[1];
			iAddresses[0] = ia;
			mailMessage.setTo(iAddresses);
			
			// set CC & BCC
			if (internetAddressesCc != null) {
				mailMessage.setCC(internetAddressesCc);
			}
			if (internetAddressesBcc != null) {
				mailMessage.setBCC(internetAddressesBcc);
			}
			
			_log.info("Sending message with: subject=["+ mailMessage.getSubject() + "], " +
					"body=[" + mailMessage.getBody() + "], htmlFormat=[" + mailMessage.getHTMLFormat() + "], " +
					"fromAddress=[" + mailMessage.getFrom().getAddress() + "], " +
					"fromName=[" + mailMessage.getFrom().getPersonal() + "]");
			MailServiceUtil.sendEmail(mailMessage);
		}

		_log.info("Notification e-mail to addresses "
				+ ArrayUtils.toString(internetAddressesTo)
				+ " has been sent successfully");
	}

}
