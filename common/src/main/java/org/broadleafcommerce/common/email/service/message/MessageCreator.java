/*
 * #%L
 * BroadleafCommerce Common Libraries
 * %%
 * Copyright (C) 2009 - 2013 Broadleaf Commerce
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.broadleafcommerce.common.email.service.message;

import org.broadleafcommerce.common.email.domain.EmailTarget;
import org.broadleafcommerce.common.email.service.info.EmailInfo;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

public abstract class MessageCreator {

    private JavaMailSender mailSender;

    public MessageCreator(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendMessage(final Map<String, Object> props) throws MailException {
        MimeMessagePreparator preparator = buildMimeMessagePreparator(props);
        this.mailSender.send(preparator);
    }

    public abstract String buildMessageBody(EmailInfo info, Map<String, Object> props);

    public MimeMessagePreparator buildMimeMessagePreparator(final Map<String, Object> props) {
        MimeMessagePreparator preparator = new MimeMessagePreparator() {

            @Override
            public void prepare(MimeMessage mimeMessage) throws Exception {
                EmailTarget emailUser = (EmailTarget) props.get(EmailPropertyType.USER.getType());
                EmailInfo info = (EmailInfo) props.get(EmailPropertyType.INFO.getType());
                MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, info.getEncoding());
                message.setTo(emailUser.getEmailAddress());
                message.setFrom(info.getFromAddress());
                message.setSubject(info.getSubject());
                if (emailUser.getBCCAddresses() != null && emailUser.getBCCAddresses().length > 0) {
                    message.setBcc(emailUser.getBCCAddresses());
                }
                if (emailUser.getCCAddresses() != null && emailUser.getCCAddresses().length > 0) {
                    message.setCc(emailUser.getCCAddresses());
                }
                String messageBody = info.getMessageBody();
                if (messageBody == null) {
                    messageBody = buildMessageBody(info, props);
                }
                String plainMessageBody = htmlToPlain(messageBody);
                message.setText(plainMessageBody, messageBody);
                for (Attachment attachment : info.getAttachments()) {
                    ByteArrayDataSource dataSource = new ByteArrayDataSource(attachment.getData(), attachment.getMimeType());
                    message.addAttachment(attachment.getFilename(), dataSource);
                }
            }
        };
        return preparator;

    }

    public JavaMailSender getMailSender() {
        return mailSender;
    }

    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    private static String htmlToPlain(String html) throws IOException {
    	final StringBuilder sb = new StringBuilder();
    	HTMLEditorKit.ParserCallback parserCallback = new HTMLEditorKit.ParserCallback() {
    	    String link;

    	    @Override
    	    public void handleText(final char[] data, final int pos) {
    	        String s = new String(data);
    	        if (link != null && link.equals(s)) {
    	        	link = null;
    	        }
    	        sb.append(s.trim());
    	    }

    	    @Override
    	    public void handleStartTag(final HTML.Tag t, final MutableAttributeSet a, final int pos) {
    	        if (t == HTML.Tag.BR) {
    	        	sb.append("\n");
    	        }
    	    	if (t == HTML.Tag.A) {
    	        	link = a.getAttribute(HTML.Attribute.HREF).toString();
    	        }
    	        if (t == HTML.Tag.IMG) {
    	        	sb.append(a.getAttribute(HTML.Attribute.ALT));
    	        }
    	    }
    	    
    	    @Override
    	    public void handleEndTag(HTML.Tag t, int pos) {
    	        if (t == HTML.Tag.A && link != null) {
    	        	sb.append(" <" + link + "> ");
    	        	link = null;
    	        }
    	        if (t == HTML.Tag.DIV || t == HTML.Tag.P 
    	        		|| t == HTML.Tag.TR || t == HTML.Tag.TD || t == HTML.Tag.TH) {
    	            sb.append("\n");
    	        }
            }

    	    @Override
    	    public void handleSimpleTag(final HTML.Tag t, final MutableAttributeSet a, final int pos) {
    	        handleStartTag(t, a, pos);
    	    }
    	};
    	new ParserDelegator().parse(new StringReader(html), parserCallback, true);
    	return sb.toString();
    }
    
}
