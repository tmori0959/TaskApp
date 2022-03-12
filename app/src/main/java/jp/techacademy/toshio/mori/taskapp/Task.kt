package jp.techacademy.toshio.mori.taskapp

import java.io.Serializable
import java.util.Date
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Task : RealmObject(), Serializable {     // Serializableインターフェイス実装で生成オブジェクトをシリアライズ可能。
                                                    // open修飾子でRealmが内部的にTaskを継承したクラスを作成し利用。
    var title: String = ""      // タイトル
    var contents: String = ""   // 内容
    var date: Date =Date()      // 日時

    // id をプライマリーキーとして設定
    @PrimaryKey
    var id: Int = 0

    var category: String = ""    // 追加


}