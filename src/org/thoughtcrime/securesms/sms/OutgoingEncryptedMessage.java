package org.thoughtcrime.securesms.sms;

import org.thoughtcrime.securesms.recipients.Recipients;

public class OutgoingEncryptedMessage extends OutgoingTextMessage {

  public OutgoingEncryptedMessage(Recipients recipients, String body, String replyBody, long expiresIn) {
    super(recipients, body, replyBody, expiresIn, -1);
  }

  public OutgoingEncryptedMessage(Recipients recipients, String body, long expiresIn) {
    this(recipients, body, null, expiresIn);
  }

  private OutgoingEncryptedMessage(OutgoingEncryptedMessage base, String body, String replyBody) {
    super(base, body, replyBody);
  }

  @Override
  public boolean isSecureMessage() {
    return true;
  }

  @Override
  public OutgoingTextMessage withBody(String body, String replyBody) {
    return new OutgoingEncryptedMessage(this, body, replyBody);
  }

  @Override
  public OutgoingTextMessage withBody(String body) {
    return this.withBody(body, this.getReplyBody());
  }
}
