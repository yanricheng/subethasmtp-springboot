package smtp;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.Properties;


public class SmptSender {
    public static void main(String[] args) throws MessagingException, GeneralSecurityException, InterruptedException {
        Properties props = new Properties();
        String smtpSrv = "smtp.163.com";
        String user = "yanricheng2@163.com";
        String cc = "yrc@servyou.com.cn";
        String psd = "YRNRRCCEFMCOKTMV";
        // 开启debug调试
        props.setProperty("mail.debug", "true");
        // 发送服务器需要身份验证
//        props.setProperty("mail.smtp.auth", "true");
        // 设置邮件服务器主机名
        props.setProperty("mail.host", "mail.163.com");
        props.setProperty("mail.smtp.host", smtpSrv);
        props.setProperty("mail.smtp.port", "25");
        props.put("mail.smtp.connectiontimeout", 300000);
        props.put("mail.smtp.timeout", 600000);
        // 发送邮件协议名称
        props.setProperty("mail.transport.protocol", "smtp");
        //启用ssl
//        MailSSLSocketFactory sf = new MailSSLSocketFactory();
//        sf.setTrustAllHosts(true);
//        props.put("mail.smtp.ssl.enable", "true");
//        props.put("mail.smtp.ssl.socketFactory", sf);
//        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        Session session = Session.getInstance(props);
        Message msg = new MimeMessage(session);
        msg.setSubject("hello,world 你好世界！" + new Date().toLocaleString());
        StringBuilder builder = new StringBuilder();
        builder.append("url = " + "http://www.baidu.com");
        builder.append("\n这是一个测试！");
        builder.append("\n时间 " + new Date().toLocaleString());
        msg.setText(builder.toString());
        msg.setFrom(new InternetAddress(user));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(cc));
        msg.setRecipient(Message.RecipientType.CC, new InternetAddress(cc));
//        msg.setRecipient(Message.RecipientType.BCC, new InternetAddress("huizi@yanrc.net"));


        Transport transport = session.getTransport();
        final DeliveredStateFuture future = new DeliveredStateFuture();
        transport.addTransportListener(new TransportListener() {
            public void messageDelivered(TransportEvent arg0) {
                System.out.println("发送成功！");
                future.setState(DeliveredState.MESSAGE_DELIVERED);
            }


            public void messageNotDelivered(TransportEvent arg0) {
                future.setState(DeliveredState.MESSAGE_NOT_DELIVERED);
            }


            public void messagePartiallyDelivered(TransportEvent arg0) {
                future.setState(DeliveredState.MESSAGE_PARTIALLY_DELIVERED);
            }
        });
        transport.connect(smtpSrv, user, psd);
        transport.sendMessage(msg, new Address[]{new InternetAddress(user)});
        transport.close();
        future.waitForReady();
    }


    private enum DeliveredState {
        INITIAL, MESSAGE_DELIVERED, MESSAGE_NOT_DELIVERED, MESSAGE_PARTIALLY_DELIVERED,
    }


    private static class DeliveredStateFuture {
        private DeliveredState state = DeliveredState.INITIAL;


        synchronized void waitForReady() throws InterruptedException {
            if (state == DeliveredState.INITIAL) {
                wait();
            }
        }


        synchronized DeliveredState getState() {
            return state;
        }


        synchronized void setState(DeliveredState newState) {
            state = newState;
            notifyAll();
        }
    }
}