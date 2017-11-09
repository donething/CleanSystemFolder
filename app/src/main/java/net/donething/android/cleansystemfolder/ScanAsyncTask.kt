package net.donething.android.cleansystemfolder

import android.content.DialogInterface
import android.os.AsyncTask
import kotlinx.android.synthetic.main.activity_main.*
import net.donething.android.tools.CommHelper

// Created by Donething on 2017-10-13.

class ScanAsyncTask(val activity: MainActivity) : AsyncTask<String, Int, String>() {
    override fun onPreExecute() {
        updatePText("正在扫描应用文件夹：")
        activity.progressDialog.show()
    }

    override fun doInBackground(vararg p0: String?): String {
        var appFileList: ArrayList<String>          // 系统目录下的文件列表变量
        var folderTotal = 0                         // 扫描的总文件夹数

        CommHelper.execRootCmd("mount -o rw,remount /system")           // 挂载system分区
        val folders = activity.etFolders.text.toString().trim().split("\n")
        folders.forEach { sysFolder ->
            val sysFiles = CommHelper.execRootCmd("ls -d \"$sysFolder\"/*/")      // 获取系统文件夹下的文件夹列表
            sysFiles.forEach { appFolder ->
                updatePText("正在扫描：$sysFolder")
                folderTotal++
                updatePStatus(folderTotal.toString())
                appFileList = CommHelper.execRootCmd("ls \"$appFolder\"")    // 获取应用文件夹下文件列表
                // 如果应用文件夹下没有".apk"文件，则表示是无用的文件夹，待删除
                if (!(appFileList.toString().contains(".apk", true))) {
                    CommHelper.log("i", "添加待删除的系统文件夹：$appFolder")
                    delWaitList.add(appFolder)
                }
            }
        }
        return "扫描完成"
    }

    override fun onProgressUpdate(vararg values: Int?) {
        // 更新进度条；1.更新进度值；2.更新进度条下方的何种任务文本；3.更新何种任务右边的具体任务状态；4.更换任务后，重置进度
        when (values[0]) {
            pUpdateProgress -> activity.progressBar.progress = values[1] ?: activity.progressBar.progress
            pUpdateText -> activity.progressText.text = pText
            pUpdateStatus -> activity.progressStatus.text = pStatus
            pReset -> {
                activity.progressBar.progress = 0
                activity.progressBar.max = values[1] ?: activity.progressBar.max
                activity.progressStatus.text = activity.getString(R.string.progress_tv_status)
            }
        }
    }

    override fun onPostExecute(result: String?) {
        resetProgress(100)
        activity.progressDialog.dismiss()
        if (delWaitList.isEmpty()) {
            CommHelper.makeDialog(activity, "扫描完成", "恭喜，没有空的应用文件夹", "知道了，消失").show()
            return
        }
        val folders = StringBuilder()
        delWaitList.forEach {
            folders.append("$it\n")
        }
        CommHelper.makeDialog(activity, "是否删除下列空文件夹(${delWaitList.size})", folders.toString(), "确定删除", DialogInterface.OnClickListener { _, _ ->
            DelAsyncTask(activity, delWaitList).execute()
        }, "取消").show()
    }

    // 以下4项的意义已在onProgressUpdate(vararg values: Int?) 里说明
    private val updatePText = { text: String ->
        pText = text
        publishProgress(pUpdateText)
    }
    private val updatePStatus = { text: String ->
        pStatus = text
        publishProgress(pUpdateStatus)
    }
    private val updateProgress = { progress: Int ->
        publishProgress(pUpdateProgress, progress)
    }
    private val resetProgress = { max: Int ->
        publishProgress(pReset, max)
    }

    private var pText = ""      // 进度应该显示的文本
    private var pStatus = ""    // 进度状态文本
    // 更新进度条的信号指示值
    private val pUpdateProgress = 100
    private val pUpdateText = 101
    private val pUpdateStatus = 102
    private val pReset = 103

    private val delWaitList = arrayListOf<String>()     // 保存待删文件夹列表
}