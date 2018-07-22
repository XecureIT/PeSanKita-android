package org.thoughtcrime.securesms.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.recipients.Recipient;
import org.whispersystems.libsignal.util.guava.Optional;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class SelectedRecipientsAdapter extends BaseAdapter {
  @NonNull  private Context                    context;
  @Nullable private OnRecipientDeletedListener onRecipientDeletedListener;
  @NonNull  private LinkedList<RecipientWrapper>     recipients;
  @Nullable private Optional<String>           ownerNumber;
  @Nullable private Optional<Set<String>>      adminNumbers;

  public SelectedRecipientsAdapter(@NonNull Context context) {
    this(context, Collections.<Recipient>emptyList(), null, Collections.<String>emptySet());
  }

  public SelectedRecipientsAdapter(@NonNull Context context,
                                   @NonNull Collection<Recipient> existingRecipients,
                                   @Nullable String existingOwner,
                                   @Nullable Set<String> existingAdmins)
  {
    this.context             = context;
    this.ownerNumber         = Optional.fromNullable(Util.canonicalizeNumber(context, existingOwner, existingOwner));
    this.adminNumbers        = Optional.fromNullable(Util.canonicalizeNumber(context, existingAdmins));
    this.recipients          = wrapExistingMembers(existingRecipients);
  }

  public void add(@NonNull Recipient recipient, boolean isPush) {
    if (!find(recipient).isPresent()) {
      RecipientWrapper wrapper = new RecipientWrapper(recipient, true, isPush, false, false);
      this.recipients.add(wrapper);
      notifyDataSetChanged();
    }
  }

  public void addAdmin(@NonNull String number) {
    if(updateAdminRecipients(number, true) != null) {
      notifyDataSetChanged();
    }
  }

  public Optional<RecipientWrapper> find(@NonNull Recipient recipient) {
    RecipientWrapper found = null;
    for (RecipientWrapper wrapper : recipients) {
      if (wrapper.getRecipient().equals(recipient)) {
        found = wrapper;
        break;
      }
    }
    return Optional.fromNullable(found);
  }

  public Optional<RecipientWrapper> find(@NonNull String number) {
    RecipientWrapper found = null;
    for (RecipientWrapper wrapper : recipients) {
      if (wrapper.getRecipient().getAddress().serialize().equals(Util.canonicalizeNumber(context, number, number))) {
        found = wrapper;
        break;
      }
    }
    return Optional.fromNullable(found);
  }

  public void remove(@NonNull Recipient recipient) {
    Optional<RecipientWrapper> match = find(recipient);
    if (match.isPresent()) {
      recipients.remove(match.get());
      if(adminNumbers.isPresent()) {
        String number = match.get().getRecipient().getAddress().serialize();
        adminNumbers.get().remove(Util.canonicalizeNumber(context, number, number));
      }
      notifyDataSetChanged();
    }
  }

  public void removeAdmin(@NonNull String number) {
    if(updateAdminRecipients(number, false) != null) {
      notifyDataSetChanged();
    }
  }

  public Set<Recipient> getRecipients() {
    final Set<Recipient> recipientSet = new HashSet<>(recipients.size());
    for (RecipientWrapper wrapper : recipients) {
      recipientSet.add(wrapper.getRecipient());
    }
    return recipientSet;
  }

  public Optional<Set<String>> getAdminNumbers() {
    return adminNumbers;
  }

  @Override
  public int getCount() {
    return recipients.size();
  }

  public boolean hasNonPushMembers() {
    for (RecipientWrapper wrapper : recipients) {
      if (!wrapper.isPush()) return true;
    }
    return false;
  }

  @Override
  public Object getItem(int position) {
    return recipients.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(final int position, View v, final ViewGroup parent) {
    if (v == null) {
      v = LayoutInflater.from(context).inflate(R.layout.selected_recipient_list_item, parent, false);
    }

    final RecipientWrapper rw         = (RecipientWrapper)getItem(position);
    final Recipient        p          = rw.getRecipient();
//    final boolean          modifiable = rw.isModifiable();
    final boolean          owner      = rw.isOwner();
    final boolean          admin      = rw.isAdmin();

    TextView    name   = (TextView)    v.findViewById(R.id.name);
    TextView    phone  = (TextView)    v.findViewById(R.id.phone);
    ImageView   badge  = (ImageView)   v.findViewById(R.id.badge);
//    ImageButton delete = (ImageButton) v.findViewById(R.id.delete);

    phone.setText(p.getAddress().serialize());
    if(Util.isOwnNumber(context, p.getAddress())) {
      name.setText(R.string.GroupMembersDialog_me);
    } else {
      name.setText(p.getName());
    }

    if(owner) {
      badge.setImageResource(R.drawable.ic_badge_owner);
    } else if(admin) {
      badge.setImageResource(R.drawable.ic_badge_admin);
    } else {
      badge.setImageResource(android.R.color.transparent);
    }

//    delete.setVisibility(modifiable ? View.VISIBLE : View.GONE);
//    delete.setOnClickListener(new View.OnClickListener() {
//      @Override
//      public void onClick(View view) {
//        if (onRecipientDeletedListener != null) {
//          onRecipientDeletedListener.onRecipientDeleted(recipients.get(position).getRecipient());
//        }
//      }
//    });

    return v;
  }

  private LinkedList<RecipientWrapper> wrapExistingMembers(Collection<Recipient> recipients) {
    final LinkedList<RecipientWrapper> wrapperList = new LinkedList<>();
    RecipientWrapper wrapper;
    for (Recipient recipient : recipients) {
      wrapper = new RecipientWrapper(recipient, false, true,
              isOwnerNumber(recipient.getAddress().serialize()), isAdminNumber(recipient.getAddress().serialize()));
      if(Util.isOwnNumber(context, recipient.getAddress())) {
        wrapperList.push(wrapper);
      } else {
        wrapperList.add(wrapper);
      }
    }
    return wrapperList;
  }

  private RecipientWrapper updateAdminRecipients(@NonNull String number, boolean admin) {
    RecipientWrapper found = null;
    for (RecipientWrapper wrapper : recipients) {
      if (wrapper.getRecipient().getAddress().serialize().equals(number)) {
        found = new RecipientWrapper(
                wrapper.getRecipient(),
                wrapper.isModifiable(),
                wrapper.isPush(),
                wrapper.isOwner(),
                admin);
        if(adminNumbers.isPresent()) {
          if(admin) {
            adminNumbers.get().add(Util.canonicalizeNumber(context, number, number));
          } else {
            adminNumbers.get().remove(Util.canonicalizeNumber(context, number, number));
          }
        }
        recipients.set(recipients.indexOf(wrapper), found);
        return found;
      }
    }
    return found;
  }

  public boolean isOwnerNumber(String number) {
    if(ownerNumber.isPresent()) {
      return ownerNumber.get().equals(Util.canonicalizeNumber(context, number, number));
    }
    return false;
  }

  public boolean isAdminNumber(String number) {
    if (adminNumbers.isPresent()) {
      for (String adminNumber: adminNumbers.get()) {
        if(adminNumber.equals(Util.canonicalizeNumber(context, number, number))) {
          return true;
        }
      }
    }
    return false;
  }

  public void setOnRecipientDeletedListener(@Nullable OnRecipientDeletedListener listener) {
    onRecipientDeletedListener = listener;
  }

  public interface OnRecipientDeletedListener {
    void onRecipientDeleted(Recipient recipient);
  }

  public static class RecipientWrapper {
    private final Recipient recipient;
    private final boolean   modifiable;
    private final boolean   push;
    private final boolean   owner;
    private final boolean   admin;

    public RecipientWrapper(final @NonNull Recipient recipient,
                            final boolean modifiable,
                            final boolean push,
                            final boolean owner,
                            final boolean admin)
    {
      this.recipient  = recipient;
      this.modifiable = modifiable;
      this.push       = push;
      this.owner      = owner;
      this.admin      = admin;
    }

    public @NonNull Recipient getRecipient() {
      return recipient;
    }

    public boolean isModifiable() {
      return modifiable;
    }

    public boolean isPush() {
      return push;
    }

    public boolean isOwner() {
      return owner;
    }

    public boolean isAdmin() {
      return admin;
    }

    public String getRecipientNameOrNumber() {
      return recipient.getName() == null || recipient.getName().isEmpty() ?
              recipient.getAddress().serialize() : recipient.getName();
    }
  }
}