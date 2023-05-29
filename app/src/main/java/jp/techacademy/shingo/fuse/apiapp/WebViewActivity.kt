package jp.techacademy.shingo.fuse.apiapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Contacts.SettingsColumns.KEY
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import jp.techacademy.shingo.fuse.apiapp.databinding.ActivityWebViewBinding
import jp.techacademy.shingo.fuse.apiapp.Shop as Shop


class WebViewActivity : AppCompatActivity(),FragmentCallback {
    private lateinit var binding: ActivityWebViewBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.webView.loadUrl(intent.getStringExtra(KEY_URL).toString())

        val shopId = intent.getStringExtra(KEY_ID).toString()
        val shopName = intent.getStringExtra(KEY_NAME).toString()
        val shopImageUrl = intent.getStringExtra(KEY_IMAGE_URLS).toString()
        val shopUrl = intent.getStringExtra(KEY_URL).toString()
        val shopAddress = intent.getStringExtra(KEY_ADDRESS).toString()
        val shopIsDelete = intent.getBooleanExtra(KEY_ISDELETED,false)


        val shop = Shop(
            shopId,
            shopAddress,
            CouponUrls(pc = shopUrl, sp = shopUrl),
            shopImageUrl,
            shopName
        )
        val favoriteShop = FavoriteShop().apply{
            id = shopId
            name = shopName
            imageUrl = shopImageUrl
            address = shopAddress
            url = shopUrl
            isDeleted = shopIsDelete
        }

        binding.favoriteImage.apply {

            var isFavorite = FavoriteShop.findBy(shop.id) != null

            // 白抜きの星を設定
            setImageResource(if (isFavorite) R.drawable.ic_star else R.drawable.ic_star_border)

            setOnClickListener {
                if (isFavorite) {
                    onDeleteFavorite(favoriteShop.id)
                } else {
                    onAddFavorite(shop)
                }
                // お気に入り状態を更新
                isFavorite = !isFavorite
                setImageResource(if (isFavorite) R.drawable.ic_star else R.drawable.ic_star_border)


            }
        }
    }


    companion object {
        private const val KEY_ID = "Key_id"
        private const val KEY_NAME = "Key_name"
        private const val KEY_IMAGE_URLS = "Key_imageUrls"
        private const val KEY_URL = "Key_url"
        private const val KEY_ADDRESS = "Key_address"
        private  const val KEY_ISDELETED = "key_deleted"


        fun start(
            activity: Activity,
            id: String,
            name: String,
            imageUrls: String,
            url: String,
            address: String,
            isDeleted:Boolean

            ) {
            activity.startActivity(
                Intent(activity, WebViewActivity::class.java).apply {
                    putExtra(KEY_ID, id)
                    putExtra(KEY_NAME, name)
                    putExtra(KEY_IMAGE_URLS, imageUrls)
                    putExtra(KEY_URL, url)
                    putExtra(KEY_ADDRESS, address)
                    putExtra(KEY_ISDELETED, isDeleted)

                }
            )
        }
    }


    override fun onClickItem(
        id: String,
        name: String,
        url: String,
        imageUrls: String,
        address: String,
        isDeleted: Boolean

        ) {}

    override fun onAddFavorite(shop: Shop) {
        FavoriteShop.insert(FavoriteShop().apply {
            id = shop.id
            name = shop.name
            imageUrl = shop.logoImage
            address = shop.address
            url = shop.couponUrls.sp.ifEmpty {
                shop.couponUrls.pc
            }
        })
    }


    override fun onDeleteFavorite(id:String) {
        showConfirmDeleteFavoriteDialog(id)
    }


    private fun showConfirmDeleteFavoriteDialog(id: String) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_favorite_dialog_title)
            .setMessage(R.string.delete_favorite_dialog_message)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                deleteFavorite(id)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> }
            .create()
            .show()
    }

    private fun deleteFavorite(id: String) {
        FavoriteShop.delete(id)

    }


}
