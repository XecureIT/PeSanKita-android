package org.thoughtcrime.securesms.sms;

public class IncomingEncryptedMessage extends IncomingTextMessage {

  public IncomingEncryptedMessage(IncomingTextMessage base, String newBody, String replyBody) {
    super(base, newBody, replyBody);
  }

  public IncomingEncryptedMessage(IncomingTextMessage base, String newBody) {
    this(base, newBody, null);
  }

  @Override
  public IncomingTextMessage withMessageBody(String body, String replyBody) {
    return new IncomingEncryptedMessage(this, body, replyBody);
  }

  @Override
  public IncomingTextMessage withMessageBody(String body) {
    return this.withMessageBody(body, this.getReplyBody());
  }

  @Override
  public boolean isSecureMessage() {
    return true;
  }
}
