package org.thoughtcrime.securesms.sms;

import org.thoughtcrime.securesms.database.model.SmsMessageRecord;
import org.thoughtcrime.securesms.recipients.Recipient;

public class OutgoingTextMessage {

  private final Recipient recipient;
  private final String    message;
  private final String    replyBody;
  private final int       subscriptionId;
  private final long      expiresIn;

  public OutgoingTextMessage(Recipient recipient, String message, int subscriptionId) {
    this(recipient, message, 0, subscriptionId);
  }

  public OutgoingTextMessage(Recipient recipient, String message, String replyBody, long expiresIn, int subscriptionId) {
    this.recipient      = recipient;
    this.message        = message;
    this.replyBody      = replyBody;
    this.expiresIn      = expiresIn;
    this.subscriptionId = subscriptionId;
  }

  public OutgoingTextMessage(Recipient recipient, String message, long expiresIn, int subscriptionId) {
    this(recipient, message, null, expiresIn, subscriptionId);
  }

  protected OutgoingTextMessage(OutgoingTextMessage base, String body, String replyBody) {
    this.recipient      = base.getRecipient();
    this.subscriptionId = base.getSubscriptionId();
    this.expiresIn      = base.getExpiresIn();
    this.message        = body;
    this.replyBody      = replyBody;
  }

  protected OutgoingTextMessage(OutgoingTextMessage base, String body) {
    this(base, body, null);
  }

  public long getExpiresIn() {
    return expiresIn;
  }

  public int getSubscriptionId() {
    return subscriptionId;
  }

  public String getMessageBody() {
    return message;
  }

  public String getReplyBody() {
    return replyBody;
  }

  public Recipient getRecipient() {
    return recipient;
  }

  public boolean isKeyExchange() {
    return false;
  }

  public boolean isSecureMessage() {
    return false;
  }

  public boolean isEndSession() {
    return false;
  }

  public boolean isPreKeyBundle() {
    return false;
  }

  public boolean isIdentityVerified() {
    return false;
  }

  public boolean isIdentityDefault() {
    return false;
  }

  public static OutgoingTextMessage from(SmsMessageRecord record) {
    if (record.isSecure()) {
      return new OutgoingEncryptedMessage(record.getRecipient(), record.getBody().getBody(), record.getBody().getReplyBody(), record.getExpiresIn());
    } else if (record.isKeyExchange()) {
      return new OutgoingKeyExchangeMessage(record.getRecipient(), record.getBody().getBody());
    } else if (record.isEndSession()) {
      return new OutgoingEndSessionMessage(new OutgoingTextMessage(record.getRecipient(), record.getBody().getBody(), 0, -1));
    } else {
      return new OutgoingTextMessage(record.getRecipient(), record.getBody().getBody(), record.getBody().getReplyBody(), record.getExpiresIn(), record.getSubscriptionId());
    }
  }

  public OutgoingTextMessage withBody(String body, String replyBody) {
    return new OutgoingTextMessage(this, body, replyBody);
  }

  public OutgoingTextMessage withBody(String body) {
    return this.withBody(body, null);
  }
}
