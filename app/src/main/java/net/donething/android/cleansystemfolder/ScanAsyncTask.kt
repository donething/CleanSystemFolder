package net.donething.android.cleansystemfolder

import android.content.DialogInterface
import android.os.AsyncTask
import kotlinx.android.synthetic.main.activity_main.*
import net.donething.android.tools.CommHelper

// Created by Donething on 2017-10-13.

class ScanAsyncTask(val activity: MainActivity) : AsyncTask<String, Int, String>() {
    override fun onPreExecute() {
        activity.progressText.text = "正在扫描系统文件夹："
        activity.progressDialog.show()
    }

    override fun doInBackground(vararg p0: String?): String {
        var appFileList: ArrayList<String>          // 系统目录下的文件列表变量
        var folderTotal = 0                         // 扫描的总文件夹数

        CommHelper.execRootCmd("mount -o rw,remount /system")           // 挂载system分区
        val folders = activity.etFolders.text.toString().trim()
        folders.split("\n").forEach { sysFolder ->
            val sysFiles = CommHelper.execRootCmd("ls $sysFolder")      // 获取系统文件夹下的文件列表
            sysFiles.forEach { appFolder ->
                folderTotal++
                publishProgress(folderTotal)
                appFileList = CommHelper.execRootCmd("ls $sysFolder/$appFolder")    // 获取应用文件夹下文件列表
                // 如果应用文件夹下没有".apk"文件，则表示是无用的文件夹，待删除
                if (!(appFileList.toString().contains(".apk", true))) {
                    delWaitList.add("$sysFolder/$appFolder")
                }
            }
        }
        activity.progressDialog.dismiss()
        return ""
    }

    override fun onProgressUpdate(vararg values: Int?) {
        activity.progressStatus.text = "${values[0].toString()}"
    }

    override fun onPostExecute(result: String?) {
        activity.progressDialog.dismiss()
        val folders = StringBuilder()
        delWaitList.forEachIndexed { index, str ->
            folders.append("$index. $str\n")
        }
        CommHelper.makeDialog(activity, "是否删除下列系统文件夹(${delWaitList.size})", folders.toString(), "确定删除", DialogInterface.OnClickListener { _, _ ->
            DelAsyncTask(activity, delWaitList).execute()
        }, "取消").show()
    }

    private val delWaitList = arrayListOf<String>()     // 保存待删文件夹列表
}