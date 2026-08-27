package com.jarvis.assistant.tools

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.BatteryManager
import android.telephony.SmsManager

class PhoneActions(private val context: Context) {

    fun execute(toolName: String, args: Map<String, String>): String {
        return try {
            when (toolName) {
                "send_sms" -> {
                    val phone = args["phone"] ?: return "Phone number missing"
                    val message = args["message"] ?: return "Message text missing"
                    val smsManager = context.getSystemService(SmsManager::class.java)
                    smsManager.sendTextMessage(phone, null, message, null, null)
                    "SMS sent successfully to $phone"
                }

                "make_call" -> {
                    val phone = args["phone"] ?: return "Phone number missing"
                    val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phone")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    "Calling $phone"
                }

                "toggle_flashlight" -> {
                    val status = args["status"]?.lowercase() == "on"
                    val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                    val cameraId = cameraManager.cameraIdList[0]
                    cameraManager.setTorchMode(cameraId, status)
                    "Flashlight turned ${if (status) "ON" else "OFF"}"
                }

                "open_app" -> {
                    val appName = args["app_name"]?.lowercase() ?: return "App name missing"
                    val launchIntent = when {
                        appName.contains("whatsapp") -> context.packageManager.getLaunchIntentForPackage("com.whatsapp")
                        appName.contains("youtube") -> context.packageManager.getLaunchIntentForPackage("com.google.android.youtube")
                        appName.contains("chrome") -> context.packageManager.getLaunchIntentForPackage("com.android.chrome")
                        appName.contains("camera") -> Intent("android.media.action.IMAGE_CAPTURE").apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                        else -> null
                    }
                    if (launchIntent != null) {
                        context.startActivity(launchIntent)
                        "Opening $appName"
                    } else {
                        "App $appName not found on device"
                    }
                }

                "check_battery" -> {
                    val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                    val batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                    "Current battery level is $batLevel percent"
                }

                else -> "Unknown command"
            }
        } catch (e: Exception) {
            "Action failed: ${e.localizedMessage}"
        }
    }
}
