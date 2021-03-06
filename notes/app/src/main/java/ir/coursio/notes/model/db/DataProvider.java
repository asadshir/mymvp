package ir.coursio.notes.model.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by Taher on 28/05/2017.
 * Project: notes
 */

public class DataProvider extends ContentProvider {

    private static final int FOLDERS = 1;
    private static final int NOTES = 2;

    //Utility class to aid in matching URIs in content providers
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        //uri for all notes
        uriMatcher.addURI(DataContract.CONTENT_AUTHORITY, DataContract.PATH_FOLDERS, FOLDERS);
        //uri for notes of a specific folder
        uriMatcher.addURI(DataContract.CONTENT_AUTHORITY, DataContract.PATH_NOTES + DataContract.PATH_FOLDERS + "/#", NOTES);
    }


    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database;
        DbHelper dbHelper;
        Cursor cursor;

        //Choose the table to query and a sort order based on the code returned for the incoming URI.
        switch (uriMatcher.match(uri)) {
            // If the incoming URI was for all of folders
            case FOLDERS:
                dbHelper = new DbHelper(getContext());
                database = dbHelper.getReadableDatabase();
                cursor = database.query(DataContract.FoldersEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            // If the incoming URI was for all notes of a specific folder
            case NOTES:
                dbHelper = new DbHelper(getContext());
                database = dbHelper.getReadableDatabase();
                selection = DataContract.NoteEntry.COLUMN_FOLDER_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(DataContract.NoteEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalStateException("IllegalStateException for URI: " + uri);
        }
        assert cursor != null;
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int uriType = uriMatcher.match(uri);
        switch (uriType) {
            case FOLDERS:
                return DataContract.FoldersEntry.CONTENT_LIST_TYPE;
            case NOTES:
                return DataContract.FoldersEntry.CONTENT_LIST_TYPE;
            default:
                throw new IllegalStateException("IllegalStateException for uriType: " + uriType);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int uriType = uriMatcher.match(uri);
        SQLiteDatabase database;
        DbHelper dbHelper;
        switch (uriType) {
            case FOLDERS:
                //add new folder
                dbHelper = new DbHelper(getContext());
                database = dbHelper.getWritableDatabase();

                long insertId = database.insertWithOnConflict(DataContract.FoldersEntry.TABLE_NAME
                        , null, values, SQLiteDatabase.CONFLICT_REPLACE);

                getContext().getContentResolver().notifyChange(uri, null);
                database.close();
                return ContentUris.withAppendedId(uri, insertId);
            case NOTES:
                //insert a new note in a specific folder
                dbHelper = new DbHelper(getContext());
                database = dbHelper.getWritableDatabase();
                long insertID = database.insertWithOnConflict(DataContract.NoteEntry.TABLE_NAME
                        , null, values, SQLiteDatabase.CONFLICT_REPLACE);

                getContext().getContentResolver().notifyChange(uri, null);
                database.close();
                return ContentUris.withAppendedId(uri, insertID);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int uriType = uriMatcher.match(uri);
        DbHelper dbHelper = new DbHelper(getContext());
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        int rowsDeleted;
        switch (uriType) {
            case NOTES:
                // Delete requested note
                rowsDeleted = database.delete(DataContract.NoteEntry.TABLE_NAME, DataContract.FoldersEntry._ID + "=" + selection,
                        null);
                break;
            case FOLDERS:
                // Delete the requested folder
                rowsDeleted = database.delete(
                        DataContract.FoldersEntry.TABLE_NAME,
                        DataContract.FoldersEntry._ID + "=" + selection,
                        null);
                // Delete all of folder's notes
                database.delete(
                        DataContract.NoteEntry.TABLE_NAME,
                        DataContract.NoteEntry.COLUMN_FOLDER_ID + "=" + selection,
                        null);

                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;

    }

    /**
     * Update notes.
     */
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int uriType = uriMatcher.match(uri);
        SQLiteDatabase database;
        DbHelper dbHelper;
        switch (uriType) {
            case NOTES:
                dbHelper = new DbHelper(getContext());
                database = dbHelper.getWritableDatabase();
                int updateId = database.update(DataContract.NoteEntry.TABLE_NAME
                        , values, selection, selectionArgs);

                getContext().getContentResolver().notifyChange(uri, null);
                database.close();
                return updateId;
        }
        return 0;
    }
}
