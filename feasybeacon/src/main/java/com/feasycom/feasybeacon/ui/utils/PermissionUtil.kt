package com.feasycom.feasybeacon.ui.utils

import android.content.Context
import android.os.Build
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists

object PermissionUtil {

    fun hasRequiredPermissions(context: Context): Boolean {
        val locationPermissionGranted =
            XXPermissions.isGrantedPermission(
                context,
                PermissionLists.getAccessFineLocationPermission()
            )
        val bluetoothScanGranted =
            XXPermissions.isGrantedPermission(
                context, PermissionLists.getBluetoothScanPermission()
            )
        val bluetoothConnectGranted =
            XXPermissions.isGrantedPermission(
                context, PermissionLists.getBluetoothConnectPermission()
            )

        return locationPermissionGranted && bluetoothScanGranted && bluetoothConnectGranted
    }

}