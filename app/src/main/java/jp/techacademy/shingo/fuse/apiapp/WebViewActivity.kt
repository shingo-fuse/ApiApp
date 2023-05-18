package jp.techacademy.shingo.fuse.apiapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import jp.techacademy.shingo.fuse.apiapp.databinding.ActivityWebViewBinding

class WebViewActivity : AppCompatActivity(),FragmentCallback {
    private  lateinit var binding:ActivityWebViewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.webView.loadUrl(intent.getStringExtra(KEY_URL).toString())

        binding.favoriteImage.apply {
            val shop = FavoriteShop()

            var isFavorite = FavoriteShop.findBy(shop.id) != null

            // 白抜きの星を設定
            setImageResource(if (isFavorite) R.drawable.ic_star else R.drawable.ic_star_border)

            setOnClickListener {
                if (isFavorite) {
                    deleteFavorite(shop.id)
                } else {
                    onAddFavorite()
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

        fun start(
            activity: Activity,
            id: String,
            name: String,
            imageUrls: String,
            url: String,
            address: String
        ) {
            activity.startActivity(
                Intent(activity, WebViewActivity::class.java).apply {
                    putExtra(KEY_ID, id)
                    putExtra(KEY_NAME, name)
                    putExtra(KEY_IMAGE_URLS, imageUrls)
                    putExtra(KEY_URL, url)
                    putExtra(KEY_ADDRESS, address)
                }
            )
        }
    }

    override fun onClickItem(
        id: String,
        name: String,
        url: String,
        imageUrls: String,
        address: String
    ) {

    }
    /**
     * Favoriteに追加するときのメソッド(Fragment -> Activity へ通知する)
     */
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




    /**
     * Favoriteから削除するときのメソッド(Fragment -> Activity へ通知する)
     */
    override fun onDeleteFavorite(id: String) {
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
