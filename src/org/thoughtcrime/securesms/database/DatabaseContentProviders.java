package org.thoughtcrime.securesms.database;

/**
 * Created by rendra on 05/11/18.
 */

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Starting in API 26, a {@link ContentProvider} needs to be defined for each authority you wish to
 * observe changes on. These classes essentially do nothing except exist so Android doesn't complain.
 */
public class DatabaseContentProviders {

    public static class ConversationList extends NoopContentProvider {
        public static final Uri CONTENT_URI = Uri.parse("content://id.kita.pesan.secure.database.conversationlist");
    }

    public static class Conversation extends NoopContentProvider {
        private static final String CONTENT_URI_STRING = "content://id.kita.pesan.secure.database.conversation/";

        public static Uri getUriForThread(long threadId) {
            return Uri.parse(CONTENT_URI_STRING + threadId);
        }
    }

    public static class Recipient extends NoopContentProvider {
        public static final Uri RECIPIENT_PREFERENCES_URI = Uri.parse("content://xecurechat/recipients/");
    }

    private static abstract class NoopContentProvider extends ContentProvider {

        @Override
        public boolean onCreate() {
            return false;
        }

        @Nullable
        @Override
        public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
            return null;
        }

        @Nullable
        @Override
        public String getType(@NonNull Uri uri) {
            return null;
        }

        @Nullable
        @Override
        public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
            return null;
        }

        @Override
        public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
            return 0;
        }

        @Override
        public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
            return 0;
        }
    }
}
