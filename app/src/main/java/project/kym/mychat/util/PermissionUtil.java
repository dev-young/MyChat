package project.kym.mychat.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import project.kym.mychat.R;

public class PermissionUtil {
    public static void requestStoragePermissions(final Context context, PermissionListener permissionlistener) {
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
        boolean isDenied = false;
        for(String permission : permissions){
            int permssionCheck = ContextCompat.checkSelfPermission(context, permission);
            if(permssionCheck == PackageManager.PERMISSION_DENIED){
                isDenied = true;
                break;
            }
        }

        if(isDenied){
            TedPermission.with(context)
                    .setPermissionListener(permissionlistener)
                    .setRationaleMessage(context.getString(R.string.permission_rationale_storage))
                    .setDeniedMessage(context.getString(R.string.permission_denied_explanation))
                    .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                    .check();
        } else
            permissionlistener.onPermissionGranted();
    }
}
