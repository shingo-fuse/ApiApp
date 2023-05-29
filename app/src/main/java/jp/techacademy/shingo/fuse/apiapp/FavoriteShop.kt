package jp.techacademy.shingo.fuse.apiapp

import android.util.Log
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey



open class FavoriteShop(id: String, imageUrl: String, name: String, url: String,address:String, isDeleted:Boolean) : RealmObject{
    @PrimaryKey
    var id: String = ""
    var imageUrl: String = ""
    var name: String = ""
    var url: String = ""
    var address: String = ""
    var isDeleted: Boolean = false

    // 初期化処理
    init {
        this.id = id
        this.imageUrl = imageUrl
        this.name = name
        this.url = url
        this.address = address
        this.isDeleted = isDeleted
    }

    // realm内部呼び出し用にコンストラクタを用意
    constructor() : this("", "", "", "", "", false)

    companion object {
        /**
         * お気に入りのShopを全件取得
         */
        fun findAll(): List<FavoriteShop> {
            // Realmデータベースとの接続を開く
            val config = RealmConfiguration.create(schema = setOf(FavoriteShop::class))
            val realm = Realm.open(config)

            // Realmデータベースからお気に入り情報を取得
            // mapでディープコピーしてresultに代入する
            val result = realm.query<FavoriteShop>("isDeleted==false").find().map {
                FavoriteShop(
                    it.id, it.imageUrl, it.name, it.url, it.address, it.isDeleted
                )
            }

            // Realmデータベースとの接続を閉じる
            realm.close()

            return result
        }

        /**
         * お気に入りされているShopをidで検索して返す
         * お気に入りに登録されていなければnullで返す
         */


        fun findBy(id: String): FavoriteShop? {
            // Realmデータベースとの接続を開く
            val config = RealmConfiguration.create(schema = setOf(FavoriteShop::class))
            val realm = Realm.open(config)

            val result = realm.query<FavoriteShop>("id=='$id' and isDeleted == false " ).first().find()

            // Realmデータベースとの接続を閉じる
            realm.close()

            return result
        }

        fun findById(id: String): FavoriteShop? {
            // Realmデータベースとの接続を開く
            val config = RealmConfiguration.create(schema = setOf(FavoriteShop::class))
            val realm = Realm.open(config)

            val result = realm.query<FavoriteShop>("id=='$id' and isDeleted == false " ).first().find()

            // Realmデータベースとの接続を閉じる
            realm.close()

            return result
        }

        /**
         * お気に入り追加
         */
        fun insert(favoriteShop: FavoriteShop) {
            // Realmデータベースとの接続を開く
            val config = RealmConfiguration.create(schema = setOf(FavoriteShop::class))
            val realm = Realm.open(config)

            realm.writeBlocking {
                    copyToRealm(favoriteShop)
                }
            realm.close()
        }

        fun updateInsert() {
            val config = RealmConfiguration.create(schema = setOf(FavoriteShop::class))
            val realm = Realm.open(config)

            realm.writeBlocking {
                val favoriteShops = realm.query<FavoriteShop>().first().find()
                findLatest(favoriteShops!!).apply {
                    this!!.isDeleted =false
                }
                }
            realm.close()
        }





        /**
         * idでお気に入りから削除する
         */
        fun delete(id: String) {
            // Realmデータベースとの接続を開く
            val config = RealmConfiguration.create(schema = setOf(FavoriteShop::class))
            val realm = Realm.open(config)

            // 削除処理
            realm.writeBlocking {
                val favoriteShops = realm.query<FavoriteShop>("id == '$id'").first().find()
                findLatest(favoriteShops!!).apply {
                    this!!.isDeleted = true
                }
            }

            // Realmデータベースとの接続を閉じる
            realm.close()
        }
    }
}
