package com.piggymetrics.notification.service;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.piggymetrics.notification.client.AccountServiceClient;
import com.piggymetrics.notification.domain.NotificationType;
import com.piggymetrics.notification.domain.Recipient;
//import java.util.concurrent.CompletableFuture;

@Service
public class NotificationServiceImpl implements NotificationService {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private AccountServiceClient client;

	@Autowired
	private RecipientService recipientService;

	@Autowired
	private EmailService emailService;

	// 异步执行
    private ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 20, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    
	@Override
	@Scheduled(cron = "${backup.cron}")
	public void sendBackupNotifications() {

		final NotificationType type = NotificationType.BACKUP;

		List<Recipient> recipients = recipientService.findReadyToNotify(type);
		log.info("found {} recipients for backup notification", recipients.size());

		for (Recipient elem : recipients) {
            executor.submit(new BackupNotificationsCallable(elem, type));
        }
		/*recipients.forEach(recipient -> CompletableFuture.runAsync(() -> {
			try {
				String attachment = client.getAccount(recipient.getAccountName());
				emailService.send(type, recipient, attachment);
				recipientService.markNotified(type, recipient);
			} catch (Throwable t) {
				log.error("an error during backup notification for {}", recipient, t);
			}
		}));*/
	}

	@Override
	@Scheduled(cron = "${remind.cron}")
	public void sendRemindNotifications() {

		final NotificationType type = NotificationType.REMIND;

		List<Recipient> recipients = recipientService.findReadyToNotify(type);
		log.info("found {} recipients for remind notification", recipients.size());

		for (Recipient elem : recipients) {
            executor.submit(new RemindNotificationsCallable(elem, type));
        }
		
		/*recipients.forEach(recipient -> CompletableFuture.runAsync(() -> {
			try {
				emailService.send(type, recipient, null);
				recipientService.markNotified(type, recipient);
			} catch (Throwable t) {
				log.error("an error during remind notification for {}", recipient, t);
			}
		}));*/
	}
	
	class BackupNotificationsCallable implements Callable<Void> {

	    private Recipient recipient;
	    private NotificationType type;
	    
	    public BackupNotificationsCallable(Recipient recipient, NotificationType type) {
            this.recipient = recipient;
            this.type = type;
        }
        @Override
        public Void call() throws Exception {
         // TODO Auto-generated method stub
            try {
                String attachment = client.getAccount(recipient.getAccountName());
                emailService.send(type, recipient, attachment);
                recipientService.markNotified(type, recipient);
            } catch (Throwable t) {
                log.error("an error during backup notification for {}", recipient, t);
            }
            return null;
        }
	    
	}
	
	class RemindNotificationsCallable implements Callable<Void> {

        private Recipient recipient;
        private NotificationType type;
        
        public RemindNotificationsCallable(Recipient recipient, NotificationType type) {
            this.recipient = recipient;
            this.type = type;
        }
        @Override
        public Void call() throws Exception {
            // TODO Auto-generated method stub
            try {
                emailService.send(type, recipient, null);
                recipientService.markNotified(type, recipient);
            } catch (Throwable t) {
                log.error("an error during remind notification for {}", recipient, t);
            }
            return null;
        }
        
    }
}
