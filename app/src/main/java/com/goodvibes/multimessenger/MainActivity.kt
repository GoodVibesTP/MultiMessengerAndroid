package com.goodvibes.multimessenger

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.view.View
import android.widget.*
import android.widget.AbsListView.OnScrollListener
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.example.testdb3.db.MyDBManager
import com.goodvibes.multimessenger.databinding.ActivityMainBinding
import com.goodvibes.multimessenger.datastructure.Chat
import com.goodvibes.multimessenger.datastructure.Event
import com.goodvibes.multimessenger.datastructure.Folder
import com.goodvibes.multimessenger.datastructure.idAllFolder
import com.goodvibes.multimessenger.db.MyDBUseCase
import com.goodvibes.multimessenger.dialog.SelectFolder
import com.goodvibes.multimessenger.network.tgmessenger.Telegram
import com.goodvibes.multimessenger.network.vkmessenger.VK
import com.goodvibes.multimessenger.usecase.MainActivityUC
import com.goodvibes.multimessenger.util.ListFoldersAdapter
import com.google.android.material.navigation.NavigationView
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.Comparator


class MainActivity : AppCompatActivity() {
    lateinit var activityMainBinding : ActivityMainBinding;
    lateinit var toggle : ActionBarDrawerToggle
    lateinit var toolbar: Toolbar
    lateinit var spinner: Spinner
    lateinit var folders: ArrayList<String>
    val myDbManager = MyDBManager(this)
    lateinit var useCase: MainActivityUC
    val vk = VK
    val tg = Telegram
    var counter = 0
    lateinit var listChatsAdapter: ListChatsAdapter


    val dbUseCase = MyDBUseCase(myDbManager)


    private var numberLastChat: Int = 0
    private var isLoadingChatVK: Boolean = false
    private var numberChatOnPage: Int = 10
    private var currentFolder: Folder = Folder(idAllFolder, "AllChats")

    var mActionMode: ActionMode? = null
    lateinit var callback: ListChatsActionModeCallback

    var allChats = Collections.synchronizedList(mutableListOf<Chat>())

    private var swipeContainer: SwipeRefreshLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        vk.init(this)
        tg.init(this)
        useCase = MainActivityUC(this, vk, tg, dbUseCase)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater);
        setContentView(activityMainBinding.root)

       if (!useCase.isLogin()) {
           val intent = Intent(this, AuthorizationActivity::class.java)
           startActivity(intent)
       }

        myDbManager.openDb()
        dbUseCase.addPrimaryFolders()

        initSwipeRefresh()
        initMenu()
        initChatsAllAdapter()

        useCase.getCurrentUserVK { user ->
            var viewName = findViewById<TextView>(R.id.drawer_header_username)
            viewName.text = user.firstName + " " + user.lastName

            var userAva = findViewById<ImageView>(R.id.drawer_header_ava)
            Picasso.get().load(user.imgUri).into(userAva)
        }

        val names = arrayListOf("Sasha", "Masha", "Lena", "Alla", "Lelya")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, names)
        spinner = findViewById(R.id.sp_option)
        spinner.adapter = spinnerAdapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                folders = myDbManager.getFolders()
                spinnerAdapter.clear()
                spinnerAdapter.addAll(folders)
                spinnerAdapter.notifyDataSetChanged()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }


>>>>>>> 27e166b (Add updating folders from db. testing for primary folders)
        callback = ListChatsActionModeCallback()
    }

    override fun onResume() {
        super.onResume()
        allChats.clear()
        numberLastChat = 0
        getStartChats()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initMenu(){
        val drawerLayout : DrawerLayout = findViewById(R.id.drawer_layout)
        val navView : NavigationView = findViewById(R.id.nav_view)

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.nav_home -> {
                    Toast.makeText(applicationContext, "Clicked home", Toast.LENGTH_LONG).show()
                }
                R.id.nav_settings -> {
                    val intent = Intent(this, AuthorizationActivity::class.java)
                    startActivity(intent)
                }
            }
            true
        }
    }

    private fun initSwipeRefresh() {
        swipeContainer = findViewById(R.id.swipeContainer);
        swipeContainer?.setOnRefreshListener(OnRefreshListener {
            allChats.clear()
            numberLastChat = 0
            getStartChats()
        })
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initChatsAllAdapter() {
    listChatsAdapter = ListChatsAdapter(this@MainActivity, allChats, this@MainActivity);
    activityMainBinding.listChats.setAdapter(listChatsAdapter);
    useCase.startUpdateListener { event ->
        when(event) {
            is Event.NewMessage -> {
                useCase.getChatByID(event.message.messenger, event.message.chatId){
                    chat: Chat ->
                    for (i in allChats.indices) {
                        if (chat.chatId == allChats[i].chatId) {
                            allChats.removeAt(i)
                            break
                        }
                    }
                    chat.lastMessage = event.message
                    allChats.add(0, chat)
                    listChatsAdapter.notifyDataSetChanged()
                }
                Log.d("VK_LOG", "new incoming message: ${event.message}")
            }
        }
    }
    }

    private fun getStartChats() {
        swipeContainer?.setRefreshing(true);
        useCase.getAllChats(numberChatOnPage, 0) { chats ->
            GlobalScope.launch(Dispatchers.Main) {
                numberLastChat = numberChatOnPage
                allChats.addAll(chats)
                allChats.sortWith(ComparatorChats().reversed())
                listChatsAdapter.notifyDataSetChanged()

                activityMainBinding.listChats.addOnScrollListener(OnScrollListenerChats())

                for (item in chats) {
                    dbUseCase.addChatsToPrimaryFolderIfNotExist(item)
                }
                swipeContainer?.setRefreshing(false);
            }
        }
    }

    private fun deleteChat(chat: Chat) {
        useCase.deleteChat(chat)
    }

    private fun moveChatToFolder(chat: Chat) : Unit {
        useCase.moveChatToFolder(chat)
    }

    private fun addFolderAndMoveChat(chat: Chat) {
        useCase.addFolder(chat)
    }

    inner class ListChatsActionModeCallback : ActionMode.Callback {
        var mClickedViewPosition: Int? = null

        fun setClickedView(view: Int?) {
            mClickedViewPosition = view
        }

        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            mode?.menuInflater?.inflate(R.menu.select_chat_menu, menu)
            mode?.setTitle("choose your option")

            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            when (item?.itemId) {
                R.id.select_chat_menu_delete -> {
                    val chat = this@MainActivity.listChatsAdapter.chats[this.mClickedViewPosition!!]
                    deleteChat(chat!!)
                    mode?.finish()
                    return true
                }
                R.id.select_chat_mov_to_folder -> {
                    val allFolders = useCase.getAllFolders()

                    val foldersAdapter = ListFoldersAdapter(this@MainActivity, allFolders)
                    val chat = this@MainActivity.listChatsAdapter.chats[this.mClickedViewPosition!!]
                    val dialog = SelectFolder(allFolders, foldersAdapter, chat!!, ::moveChatToFolder, ::addFolderAndMoveChat)
                    val manager = supportFragmentManager
                    dialog.show(manager, "Select folder")
                    return true
                }

            }
            return false;
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            mActionMode = null;
        }
    }

    inner class OnScrollListenerChats : RecyclerView.OnScrollListener()  {
       override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
           super.onScrollStateChanged(recyclerView, newState)
       }

        @SuppressLint("NotifyDataSetChanged")
        @RequiresApi(Build.VERSION_CODES.N)
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
           super.onScrolled(recyclerView, dx, dy)
           val layoutManager = recyclerView.layoutManager as LinearLayoutManager
           val visibleItemCount = layoutManager.childCount
           val totalItemCount = layoutManager.itemCount
           val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

            folders = myDbManager.getFolders()
            val spinnerAdapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, folders)

            spinner.adapter = spinnerAdapter

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    folders = myDbManager.getFolders()
                    spinnerAdapter.clear()
                    spinnerAdapter.addAll(folders)
                    spinnerAdapter.notifyDataSetChanged()


                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }
            }

           if (!isLoadingChatVK &&  (firstVisibleItemPosition + visibleItemCount >= totalItemCount)) {
               isLoadingChatVK = true
               useCase.getAllChats(numberChatOnPage, numberLastChat) {chats ->
                   numberLastChat += numberChatOnPage
                   isLoadingChatVK = false
                   allChats.addAll(chats)
                   allChats.sortWith(ComparatorChats().reversed())
                   listChatsAdapter.notifyDataSetChanged()
                   for (item in chats) {
                       dbUseCase.addChatsToPrimaryFolderIfNotExist(item)
                   }
               }
           }


       }
   }

   class ComparatorChats: Comparator<Chat> {
       override fun compare(p0: Chat?, p1: Chat?): Int {
           if (p0 == null || p1 == null) return 0
           else return p0.lastMessage!!.date.compareTo(p1.lastMessage!!.date)
       }
   }

    override fun onDestroy() {
        super.onDestroy()
        myDbManager.closeDB()
    }
}
