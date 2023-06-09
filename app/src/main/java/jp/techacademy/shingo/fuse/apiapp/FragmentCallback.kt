package jp.techacademy.shingo.fuse.apiapp



interface FragmentCallback {
    //Itemを押した時の処理
    fun onClickItem(id:String, name:String , url:String , imageUrls:String ,address: String,deleted:Boolean)

    // お気に入り追加時の処理
    fun onAddFavorite(shop: Shop)

    // お気に入り削除時の処理
    fun  onDeleteFavorite (id: String)

}