package cn.pickup.launcher;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Minimal FileProvider that grants Uri-for-File access to downloaded
 * APKs so we can hand them to the system installer (ACTION_VIEW +
 * application/vnd.android.package-archive) on Android 7.0+ where
 * plain file:// URIs are rejected with FileUriExposedException.
 *
 * Authored here instead of pulling in androidx.core, since the
 * project's manual build pipeline has no Maven/Gradle dependency
 * resolution.
 */
public final class ApkFileProvider extends ContentProvider {

    private static final String AUTHORITY_SUFFIX = ".fileprovider";

    @Override
    public boolean onCreate() {
        return true;
    }

    public static Uri uriFor(android.content.Context ctx, File file) {
        String authority = ctx.getPackageName() + AUTHORITY_SUFFIX;
        return Uri.parse("content://" + authority + "/update/" + file.getName());
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        File file = fileFor(uri);
        if (file == null || !file.exists()) {
            throw new FileNotFoundException("Not found: " + uri);
        }
        int flags = ParcelFileDescriptor.parseMode(mode);
        return ParcelFileDescriptor.open(file, flags);
    }

    @Override
    public AssetFileDescriptor openAssetFile(Uri uri, String mode) throws FileNotFoundException {
        return new AssetFileDescriptor(openFile(uri, mode), 0, AssetFileDescriptor.UNKNOWN_LENGTH);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        File file = fileFor(uri);
        if (file == null) return null;
        String[] cols = projection != null ? projection : new String[]{
                OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE};
        MatrixCursor cursor = new MatrixCursor(cols);
        Object[] row = new Object[cols.length];
        for (int i = 0; i < cols.length; i++) {
            String c = cols[i];
            if (OpenableColumns.DISPLAY_NAME.equals(c)) row[i] = file.getName();
            else if (OpenableColumns.SIZE.equals(c)) row[i] = file.length();
            else row[i] = null;
        }
        cursor.addRow(row);
        return cursor;
    }

    @Override public String getType(Uri uri) {
        return "application/vnd.android.package-archive";
    }

    @Override public Uri insert(Uri uri, ContentValues values) { return null; }
    @Override public int delete(Uri uri, String selection, String[] selectionArgs) { return 0; }
    @Override public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) { return 0; }

    private File fileFor(Uri uri) {
        String name = uri.getLastPathSegment();
        if (name == null) return null;
        File updateDir = new File(getContext().getCacheDir(), "update");
        File file = new File(updateDir, name);
        // path-traversal guard: must be directly under updateDir
        if (!file.getParentFile().equals(updateDir)) return null;
        return file;
    }
}
