package net.donething.android.tools

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.util.Log
import java.io.DataInputStream
import java.io.DataOutputStream


// Created by Donething on 2017-10-13.

class CommHelper {
    companion object {
        /**
         * 执行root命令，并返回执行结果
         */
        fun execRootCmd(cmd: String): ArrayList<String> {
            val result = arrayListOf<String>()
            var dos: DataOutputStream? = null
            var dis: DataInputStream? = null
            try {
                val p = Runtime.getRuntime().exec("su")     // 经过Root处理的android系统即有su命令
                dos = DataOutputStream(p.outputStream)
                dis = DataInputStream(p.inputStream)

                dos.writeBytes(cmd + "\n")
                dos.flush()
                dos.writeBytes("exit\n")
                dos.flush()
                var line = dis.readLine()
                while (line != null) {
                    result.add(line)
                    line = dis.readLine()
                }
                p.waitFor()
            } catch (e: Exception) {
                log("e", "执行命令($cmd)出错：${e.message}", e)
            } finally {
                dis?.close()
                dos?.close()
            }
            return result
        }

        /**
         * 执行root命令，不返回执行结果
         */
        fun execRootCmdSilent(cmd: String): Int {
            var result = -1
            var dos: DataOutputStream? = null
            try {
                val p = Runtime.getRuntime().exec("su");
                dos = DataOutputStream(p.outputStream)
                dos.writeBytes(cmd + "\n")
                dos.flush()
                dos.writeBytes("exit\n")
                dos.flush()
                p.waitFor()
                result = p.exitValue()
            } catch (e: Exception) {
                log("e", "执行命令($cmd)出错：${e.message}", e)
            } finally {
                dos?.close()
            }
            return result
        }

        /**
         * 生成对话框
         */
        fun makeDialog(activity: Activity, title: String, msg: String, positiveText: String? = null, positive: DialogInterface.OnClickListener? = null,
                       negativeText: String? = null, negative: DialogInterface.OnClickListener? = null, cancelAble: Boolean = true): AlertDialog {
            return AlertDialog.Builder(activity)
                    .setTitle(title)
                    .setMessage(msg)
                    .setPositiveButton(positiveText, positive)
                    .setNegativeButton(negativeText, negative)
                    .setCancelable(cancelAble)
                    .create()
        }

        /**
         * 记录日志
         * @param type 分4个等级：i,w,e,v
         * @param text 说明信息
         * @param ex 异常（默认为null）
         */
        fun log(type: String, text: String, ex: Exception? = null, toFile: Boolean = true) {
            when (type.toLowerCase()) {
                "i" -> Log.i(DEBUG_TAG, text)
                "w" -> Log.w(DEBUG_TAG, text)
                "e" -> Log.e(DEBUG_TAG, text, ex)
                else -> Log.v(DEBUG_TAG, text)
            }
        }

        private val DEBUG_TAG = "ClearFolder"
        val WAIT_SCAN_FOLDERS = "/system/app\n/system/priv-app\n"

        val SCAN_FOLDERS = "scan_folders"
    }
}