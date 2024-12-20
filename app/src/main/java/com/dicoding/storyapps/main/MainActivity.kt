package com.dicoding.storyapps.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.storyapps.R
import com.dicoding.storyapps.databinding.ActivityMainBinding
import com.dicoding.storyapps.login.LoginActivity
import com.dicoding.storyapps.maps.StoryMapsActivity
import com.dicoding.storyapps.story.AddNewStoryActivity
import com.dicoding.storyapps.story.StoryAdapter
import com.dicoding.storyapps.uti.ViewModelFactory
import com.dicoding.storyapps.welcome.WelcomeActivity

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mainViewModel: MainViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }

    private val storyAdapter = StoryAdapter()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupView()
        setupAction()
        setupObserver()

    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.title = "Momentos!"

        val layoutManager = LinearLayoutManager(this)
        binding.apply {
            rvStoryList.layoutManager = layoutManager
            rvStoryList.setHasFixedSize(true)
            rvStoryList.adapter = storyAdapter
        }
    }

    private fun setupAction() {

        binding.swipeRefresh.setOnRefreshListener {
                finish()
            overridePendingTransition(0, 0)
            startActivity(intent)
            overridePendingTransition(0, 0)
            binding.swipeRefresh.isRefreshing = false
        }

        binding.floatBtnAddStory.setOnClickListener {
            startActivity(
                Intent(
                    this@MainActivity,
                    AddNewStoryActivity::class.java
                )
            )
        }
    }

    private fun setupObserver() {

        mainViewModel.getUserToken().observe(
            this@MainActivity
        ) { session ->
            if (session.token == "") {
                val intent = Intent(
                    this@MainActivity,
                    WelcomeActivity::class.java
                )
                startActivity(intent)
                finish()
            } else {
                getStoryList(session.token)
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.option_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.action_logout -> {
                mainViewModel.userLogout()

                val intent = Intent(
                    this@MainActivity,
                    LoginActivity::class.java
                )
                startActivity(intent)
                finish()
            }

           R.id.action_maps -> {
                val intent = Intent(
                    this@MainActivity,
                    StoryMapsActivity::class.java
                )
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getStoryList(token: String) {
        showLoading(true)
        mainViewModel.getStories(token).observe(
            this@MainActivity
        ) {
            showLoading(false)
            storyAdapter.submitData(lifecycle, it)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

}