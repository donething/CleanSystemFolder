package net.donething.android.cleansystemfolder

import android.app.Activity
import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import net.donething.android.tools.CommHelper

class MainActivity : Activity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefS = PreferenceManager.getDefaultSharedPreferences(this)
        editor = prefS.edit()

        val folders = prefS.getString(CommHelper.SCAN_FOLDERS, "")
        etFolders.setText(if (folders.isEmpty()) CommHelper.WAIT_SCAN_FOLDERS else folders)
        etFolders.setSelection(etFolders.text.length)
        bnStartDel.setOnClickListener(this)

        loadProgressDialog()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.bnStartDel -> {
                ScanAsyncTask(this).execute()
                editor.putString(CommHelper.SCAN_FOLDERS, etFolders.text.toString().trim()).commit()
            }
        }
    }

    private fun loadProgressDialog() {
        val view = layoutInflater.inflate(R.layout.progress_dialog, null)
        val builder = AlertDialog.Builder(this).setView(view)
        progressBar = view.findViewById(R.id.progressBar)
        progressText = view.findViewById(R.id.progress_tv_text)
        progressStatus = view.findViewById(R.id.progress_tv_status)
        progressDialog = builder
                .setTitle(getString(R.string.progress_dialog_title))
                .setCancelable(false)
                .create()
    }

    private lateinit var prefS: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    lateinit var progressDialog: AlertDialog
    lateinit var progressBar: ProgressBar
    lateinit var progressText: TextView
    lateinit var progressStatus: TextView
}
