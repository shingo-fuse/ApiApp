package jp.techacademy.shingo.fuse.apiapp

import android.content.Context
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jp.techacademy.shingo.fuse.apiapp.databinding.FragmentApiBinding
import androidx.recyclerview.widget.LinearLayoutManager
import android.os.Handler
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.squareup.moshi.Moshi
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException

class ApiFragment : Fragment() {
    private var _binding: FragmentApiBinding? = null
    private  var keywordUrl :String = "ランチ"


    //一覧に表示するShopを保持
    private  var list = mutableListOf<Shop>()
    //現在のページ
    private  var page = 0
    //Apiでデータを読み込み中ですフラグ。追加ページの読み込みの時にこれがないと連続して読み込んでしまう。
    private  var isLoading =false


    private val binding get() = _binding!!

    private val apiAdapter by lazy { ApiAdapter() }
    private val handler = Handler(Looper.getMainLooper())

    // Fragment -> Activity にFavoriteの変更を通知する
    private var fragmentCallback: FragmentCallback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentCallback) {
            fragmentCallback = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentApiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // ここから初期化処理を行う
        // ApiAdapterのお気に入り追加、削除用のメソッドの追加を行う
        apiAdapter.apply {
            // Adapterの処理をそのままActivityに通知する
            onClickAddFavorite = {
                fragmentCallback?.onAddFavorite(it)
            }
            // Adapterの処理をそのままActivityに通知する
            onClickDeleteFavorite = {
                fragmentCallback?.onDeleteFavorite(it.id)
            }

            //Itemをクリックした時
            onClickItem = { id: String, name: String, url: String, imageUrls: String, address: String,isDeleted: Boolean->
                fragmentCallback?.onClickItem(id,name,imageUrls,url,address,isDeleted)
            }
        }

        binding.button.setOnClickListener{
            val keyWord = binding.editText.text.toString()
            keywordUrl = keyWord
            updateData()

        }

        // RecyclerViewの初期化
        binding.recyclerView.apply {
            adapter = apiAdapter
            layoutManager = LinearLayoutManager(requireContext()) // 一列ずつ表示
            //Scrollを検知するListnerを実装する。これによって、RecyclerViewは縦方向なので、byだけ考慮してする。
            addOnScrollListener(object : RecyclerView.OnScrollListener(){
                // dx はx軸方向の変化量(横) dy はy軸方向の変化量(縦) ここではRecyclerViewは縦方向なので、dyだけ考慮する
                override fun onScrolled(recyclerView: RecyclerView,dx:Int,dy:Int){
                    super.onScrolled(recyclerView, dx, dy)
                    // 縦方向の変化量(スクロール量)が0の時は動いていないので何も処理はしない
                    if(dy == 0){
                        return
                    }
                    //RecyclerViewの現在の表示アイテム数
                    val totalCount = apiAdapter.itemCount

                    //RecyclerViewの現在見えている最後のViewHolderのposition
                    val lastVisibleItem =
                        (layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                    // totalCountとlastVisibleItemから全体のアイテム数のうちどこまでが見えているかがわかる
                    // (例:totalCountが20、lastVisibleItemが15の時は、現在のスクロール位置から下に5件見えていないアイテムがある)
                    // 一番下にスクロールした時に次の20件を表示する等の実装が可能になる。
                    // ユーザビリティを考えると、一番下にスクロールしてから追加した場合、一度スクロールが止まるので、ユーザーは気付きにくい
                    // ここでは、一番下から5番目を表示した時に追加読み込みする様に実装する
                    if(!isLoading && lastVisibleItem >= totalCount - 6){//読み込み中でない、かつ、現在のスクロール位置から下に５検見えないいちがある。
                        updateData(true)
                    }
                }
            })
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            updateData()
        }
        updateData()
    }

    /**
     * お気に入りが削除されたときの処理（Activityからコールされる）
     */
    fun updateView() {
        // RecyclerViewのAdapterに対して再描画のリクエストをする
        apiAdapter.notifyItemRangeChanged(0, apiAdapter.itemCount)
    }

    private fun updateData(isAdd :Boolean = false) {
        //読み込み中なら処理を行わずに終了
        if(isLoading){
            return
        }else{
            isLoading = true
        }
        //追加モードならページを加算
        if(isAdd){
            page++
        }else{
            page =0
            list.clear()
        }
        //開始位置を計算
        val start = page * COUNT + 1


        val url = StringBuilder()
            .append(getString(R.string.base_url)) // https://webservice.recruit.co.jp/hotpepper/gourmet/v1/
            .append("?key=").append(getString(R.string.api_key)) // Apiを使うためのApiKey
            .append("&start=").append(start) // 何件目からのデータを取得するか
            .append("&count=").append(COUNT) // 1回で20件取得する
            .append("&keyword=")
            .append(keywordUrl) // お店の検索ワード。ここでは例として「ランチ」を検索
            .append("&format=json") // ここで利用しているAPIは戻りの形をxmlかjsonが選択することができる。Androidで扱う場合はxmlよりもjsonの方が扱いやすいので、jsonを選択
            .toString()
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
        val request = Request.Builder()
            .url(url)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { // Error時の処理
                e.printStackTrace()
                handler.post {
                    updateRecyclerView(listOf())
                }
                isLoading = false //読み込みフラグを断る。
            }

            override fun onResponse(call: Call, response: Response) { // 成功時の処理
                // Jsonを変換するためのAdapterを用意
                val moshi = Moshi.Builder().build()
                val jsonAdapter = moshi.adapter(ApiResponse::class.java)

                var list = listOf<Shop>()
                response.body?.string()?.also {
                    val apiResponse = jsonAdapter.fromJson(it)
                    if (apiResponse != null) {
                        list = apiResponse.results.shop
                    }
                }
                handler.post {
                    updateRecyclerView(list)
                }
                isLoading = false
            }
        })
    }

    private fun updateRecyclerView(list: List<Shop>) {
        apiAdapter.submitList(list)
        // SwipeRefreshLayoutのくるくるを消す
        binding.swipeRefreshLayout.isRefreshing = false
    }

    companion object {
        // 1回のAPIで取得する件数
        private const val COUNT = 20
    }
}















