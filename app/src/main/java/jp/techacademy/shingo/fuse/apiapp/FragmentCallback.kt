package jp.techacademy.shingo.fuse.apiapp

import android.icu.text.Transliterator.Position


interface FragmentCallback {
    //Itemを押した時の処理
    fun onClickItem(id:String, name:String , url:String , imageUrls:String ,address: String)

    // お気に入り追加時の処理
    fun onAddFavorite(shop: Shop)

    // お気に入り削除時の処理
    fun onDeleteFavorite(id: String)
}