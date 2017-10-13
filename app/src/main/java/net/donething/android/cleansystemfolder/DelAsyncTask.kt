package net.donething.android.cleansystemfolder

import android.os.AsyncTask
import net.donething.android.tools.CommHelper

// Created by Donething on 2017-10-13.

class DelAsyncTask(val activity: MainActivity, val delWaitList: ArrayList<String>) : AsyncTask<String, Int, String>() {
    override fun onPreExecute() {
        activity.progressBar.max = delWaitList.size
        activity.progressBar.progress = 0
        activity.progressText.text = "正在删除无用的系统文件夹:"
        activity.progressDialog.show()
    }

    override fun doInBackground(vararg p0: String?): String {
        var folderDel = 0                         // 已删除的文件夹数
        activity.progressText.text = "删除系统文件夹"
        delWaitList.forEach {
            CommHelper.log("i", "删除系统文件夹：$it")
            try {
                CommHelper.execRootCmd("rm -r \"$it\"")
                hadDelFolderList.add(it)
                folderDel++
                publishProgress(folderDel)
            } catch (e: Exception) {
                CommHelper.log("e", "删除系统文件夹($it)出错：${e.message}", e)
            }
        }
        return ""
    }

    override fun onProgressUpdate(vararg values: Int?) {
        activity.progressText.text = "${values[0]}"
    }

    override fun onPostExecute(result: String?) {
        activity.progressDialog.dismiss()
        val folders = StringBuilder()
        hadDelFolderList.forEach {
            folders.append("$it\n")
        }
        CommHelper.makeDialog(activity, "文件夹删除完成", folders.toString(), "明白了").show()
    }

    val hadDelFolderList = arrayListOf<String>()
}