package jp.techacademy.toshio.mori.taskapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import io.realm.RealmChangeListener
import io.realm.Sort
import android.content.Intent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import io.realm.RealmQuery
import kotlinx.android.synthetic.main.content_input.*

const val EXTRA_TASK = "jp.techacademy.toshio.mori.taskapp.TASK" // IntentのEXTRAキーワードに使う文字列EXTRA定義

class MainActivity : AppCompatActivity(),View.OnClickListener {
    // RealmChangeListenerクラスのmRealmListenerはRealmのデータベースに追加、削除など変化があると呼ばれるリスナー。
    // onChangeメソッドをオーバーライドしてreloadListViewメソッドを呼び出す
    private lateinit var mRealm: Realm
    private val mRealmListener = object : RealmChangeListener<Realm> {
        override fun onChange(element: Realm) {
            reloadListView()
        }
    }

    private lateinit var mTaskAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // UI 部品設定
        match_button.setOnClickListener(this)

        fab.setOnClickListener { view ->
            val intent = Intent(this, InputActivity::class.java)
            startActivity(intent)
        }

        // Realmの設定
        mRealm = Realm.getDefaultInstance()         // getDefaultInstanceメソッドでオブジェクトを取得
        mRealm.addChangeListener(mRealmListener)   // mRealmLstenerをaddChangeListenerメソッドで設定

        // ListViewの設定
        mTaskAdapter = TaskAdapter(this)    // onCreateメソッドでTaskAdaperを生成

        // ListViewをタップしたときの処理
        listView1.setOnItemClickListener { parent, _, position, _ ->
            // 入力・編集する画面に遷移させる
            val task = parent.adapter.getItem(position) as Task
            val intent = Intent(this, InputActivity::class.java)
            intent.putExtra(EXTRA_TASK, task.id)
            startActivity(intent)
        }

        // ListViewを長押ししたときの処理
        listView1.setOnItemLongClickListener { parent, _, position, _ ->
            // タスクを削除する
            val task = parent.adapter.getItem(position) as Task

            // ダイアログを表示する
            val builder = AlertDialog.Builder(this)

            builder.setTitle("削除")
            builder.setMessage(task.title + "を削除しますか")

            builder.setPositiveButton("OK") { _, _ ->
                val results = mRealm.where(Task::class.java).equalTo("id", task.id).findAll()

                mRealm.beginTransaction()
                results.deleteAllFromRealm()
                mRealm.commitTransaction()

                val resultIntent =
                    Intent(applicationContext, TaskAlarmReceiver::class.java)    // ↓↓↓ 7.3で追加
                val resultPendingIntent = PendingIntent.getBroadcast(
                    this,
                    task.id,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                alarmManager.cancel(resultPendingIntent)                                        // ↑↑↑ 7.3で追加

                reloadListView()
            }

            builder.setNegativeButton("CANCEL", null)

            val dialog = builder.create()
            dialog.show()

            true
        }
        reloadListView()
    }

    //　category検索　
        override fun onClick(v: View?) {
        var match_edit_text = this
        match_button.setOnClickListener{

        val match_edit_text = findViewById<EditText>(R.id.match_edit_text)
        val str = match_edit_text?.text.toString()
            // Realmデータベースから、「すべてのcategoryを取得して並べた結果」を取得。match_edit_textとmrealmのcategoryとイコール。
        val taskRealmResults = mRealm.where(Task::class.java).equalTo("category", str).findAll().sort("category", Sort.DESCENDING)
        mTaskAdapter.mTaskList = mRealm.copyFromRealm(taskRealmResults)

        listView1.adapter = mTaskAdapter

        mTaskAdapter.notifyDataSetChanged()
            }
        }

    private fun reloadListView() {
        // Realmデータベースから、「すべてのデータを取得して新しい日時順に並べた結果」を取得
        val taskRealmResults = mRealm.where(Task::class.java).findAll().sort("date", Sort.DESCENDING)

        // 上記の結果を、TaskListとしてセットする
        mTaskAdapter.mTaskList = mRealm.copyFromRealm(taskRealmResults)

        // TaskのListView用のアダプタに渡す
        listView1.adapter = mTaskAdapter

        // 表示を更新するために、アダプターにデータが変更されたことを知らせる
        mTaskAdapter.notifyDataSetChanged()
    }

    override fun onDestroy() {      // onDestroyメソッドをオーバーライドしてRealmクラスのcloseメソッドを呼び出し
        super.onDestroy()

        mRealm.close()              // getDefaultInstanceメソッドで取得したRealmクラスのオブジェクトはcloseメソッドで終了させる
    }
}

