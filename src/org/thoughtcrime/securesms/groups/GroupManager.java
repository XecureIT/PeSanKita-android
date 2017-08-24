package org.thoughtcrime.securesms.groups;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.protobuf.ByteString;

import org.thoughtcrime.securesms.attachments.Attachment;
import org.thoughtcrime.securesms.attachments.UriAttachment;
import org.thoughtcrime.securesms.crypto.MasterSecret;
import org.thoughtcrime.securesms.database.AttachmentDatabase;
import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.database.GroupDatabase;
import org.thoughtcrime.securesms.mms.OutgoingGroupMediaMessage;
import org.thoughtcrime.securesms.providers.SingleUseBlobProvider;
import org.thoughtcrime.securesms.recipients.Recipient;
import org.thoughtcrime.securesms.recipients.RecipientFactory;
import org.thoughtcrime.securesms.recipients.Recipients;
import org.thoughtcrime.securesms.sms.MessageSender;
import org.thoughtcrime.securesms.util.BitmapUtil;
import org.thoughtcrime.securesms.util.GroupUtil;
import org.thoughtcrime.securesms.util.TextSecurePreferences;
import org.thoughtcrime.securesms.util.Util;
import org.whispersystems.signalservice.api.util.InvalidNumberException;
import org.whispersystems.signalservice.internal.push.SignalServiceProtos.GroupContext;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ws.com.google.android.mms.ContentType;

import static org.thoughtcrime.securesms.R.id.recipients;

public class GroupManager {
  public static @NonNull GroupActionResult createGroup(@NonNull  Context        context,
                                                       @NonNull  MasterSecret   masterSecret,
                                                       @NonNull  Set<Recipient> members,
                                                       @Nullable Bitmap         avatar,
                                                       @Nullable String         name)
      throws InvalidNumberException
  {
    final byte[]        avatarBytes       = BitmapUtil.toByteArray(avatar);
    final GroupDatabase groupDatabase     = DatabaseFactory.getGroupDatabase(context);
    final byte[]        groupId           = groupDatabase.allocateGroupId();
    final Set<String>   memberE164Numbers = getE164Numbers(context, members);
    final String        ownerE164Number   = Util.canonicalizeNumber(context,
            TextSecurePreferences.getLocalNumber(context));

    memberE164Numbers.add(TextSecurePreferences.getLocalNumber(context));
    groupDatabase.create(groupId, name, new LinkedList<>(memberE164Numbers), ownerE164Number,
            Collections.<String>emptyList(), null, null);
    groupDatabase.updateAvatar(groupId, avatarBytes);
    return sendGroupUpdate(context, masterSecret, groupId, memberE164Numbers, ownerE164Number,
            Collections.<String>emptySet(), name, avatarBytes, null);
  }

  private static Set<String> getE164Numbers(Context context, Collection<Recipient> recipients)
      throws InvalidNumberException
  {
    final Set<String> results = new HashSet<>();
    if(recipients != null) {
      for (Recipient recipient : recipients) {
        results.add(Util.canonicalizeNumber(context, recipient.getNumber()));
      }
    }

    return results;
  }

  private static Set<String> getE164FromNumbers(Context context, Collection<String> numbers)
          throws InvalidNumberException
  {
    final Set<String> results = new HashSet<>();
    if(numbers != null) {
      for (String number : numbers) {
        results.add(Util.canonicalizeNumber(context, number));
      }
    }

    return results;
  }

  private static void removeLocalRecipient(Context context, Set<Recipient> recipients) {
    String localNumber = TextSecurePreferences.getLocalNumber(context);
    for (Recipient recipient : recipients) {
      if(localNumber.equals(Util.canonicalizeNumber(context, recipient.getNumber(), recipient.getNumber()))) {
        recipients.remove(recipients.remove(recipient));
        break;
      }
    }
  }

  public static GroupActionResult updateGroup(@NonNull  Context        context,
                                              @NonNull  MasterSecret   masterSecret,
                                              @NonNull  byte[]         groupId,
                                              @NonNull  Set<Recipient> members,
                                              @Nullable Set<String>    admins,
                                              @Nullable Bitmap         avatar,
                                              @Nullable String         name)
      throws InvalidNumberException
  {
    final GroupDatabase groupDatabase     = DatabaseFactory.getGroupDatabase(context);
    final GroupDatabase.GroupRecord groupRecord = groupDatabase.getGroup(groupId);
    final Set<String>   memberE164Numbers = getE164Numbers(context, members);
    final String        ownerNumber       = groupRecord.getOwner();
    final String        ownerE164Number   = Util.canonicalizeNumber(context, ownerNumber, ownerNumber);
    final Set<String>   adminE164Numbers  = getE164FromNumbers(context, admins);
    final byte[]        avatarBytes       = BitmapUtil.toByteArray(avatar);

    removeLocalRecipient(context, members);
    List<Recipient> missingMembers = groupDatabase.getGroupMembers(groupId, false).getRecipientsList();
    missingMembers.removeAll(members);

    Recipients destRecipients = null;

    if(missingMembers.size() > 0) {
      missingMembers.addAll(members);
      destRecipients = RecipientFactory.getRecipientsFor(context, missingMembers, false);
    }

    memberE164Numbers.add(TextSecurePreferences.getLocalNumber(context));
    groupDatabase.updateMembers(groupId, new LinkedList<>(memberE164Numbers));
    groupDatabase.updateAdmins(groupId, new LinkedList<>(adminE164Numbers));
    groupDatabase.updateTitle(groupId, name);
    groupDatabase.updateAvatar(groupId, avatarBytes);

    return sendGroupUpdate(context, masterSecret, groupId, memberE164Numbers, ownerE164Number,
            adminE164Numbers, name, avatarBytes, destRecipients);
  }

  private static GroupActionResult sendGroupUpdate(@NonNull  Context      context,
                                                   @NonNull  MasterSecret masterSecret,
                                                   @NonNull  byte[]       groupId,
                                                   @NonNull  Set<String>  e164numbers,
                                                   @NonNull  String       ownerE164number,
                                                   @NonNull  Set<String>  adminE164numbers,
                                                   @Nullable String       groupName,
                                                   @Nullable byte[]       avatar,
                                                   @Nullable Recipients   destRecipients)
  {
    Attachment avatarAttachment = null;
    String     groupRecipientId = GroupUtil.getEncodedId(groupId);
    Recipients groupRecipient   = RecipientFactory.getRecipientsFromString(context, groupRecipientId, false);

    GroupContext.Builder groupContextBuilder = GroupContext.newBuilder()
                                                           .setId(ByteString.copyFrom(groupId))
                                                           .setType(GroupContext.Type.UPDATE)
                                                           .addAllMembers(e164numbers)
                                                           .addAllAdmins(adminE164numbers);
    if (groupName != null) groupContextBuilder.setName(groupName);

    if(ownerE164number != null) {
      groupContextBuilder.setOwner(ownerE164number);
    }

    GroupContext groupContext = groupContextBuilder.build();

    if (avatar != null) {
      Uri avatarUri = SingleUseBlobProvider.getInstance().createUri(avatar);
      avatarAttachment = new UriAttachment(avatarUri, ContentType.IMAGE_PNG, AttachmentDatabase.TRANSFER_PROGRESS_DONE, avatar.length);
    }

    OutgoingGroupMediaMessage outgoingMessage = new OutgoingGroupMediaMessage(groupRecipient, groupContext, avatarAttachment, System.currentTimeMillis(), 0);
    long                      threadId        = MessageSender.send(context, masterSecret, outgoingMessage, -1, false, destRecipients);

    return new GroupActionResult(groupRecipient, threadId);
  }

  public static class GroupActionResult {
    private Recipients groupRecipient;
    private long       threadId;

    public GroupActionResult(Recipients groupRecipient, long threadId) {
      this.groupRecipient = groupRecipient;
      this.threadId       = threadId;
    }

    public Recipients getGroupRecipient() {
      return groupRecipient;
    }

    public long getThreadId() {
      return threadId;
    }
  }
}
