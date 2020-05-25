package com.dvach_2ch.a2ch.ui.favourite

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.dvach_2ch.a2ch.adapters.FavouritesAdapter
import com.dvach_2ch.a2ch.databinding.FavouritesFragmentBinding
import com.dvach_2ch.a2ch.ui.posts.PostsActivity
import com.dvach_2ch.a2ch.util.BOARD_NAME
import com.dvach_2ch.a2ch.util.THREAD_NUM
import kotlinx.android.synthetic.main.favourites_fragment.view.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class FavouritesFragment : Fragment(), KodeinAware {
    override val kodein by kodein()
    private val factory: FavouritesViewModelFactory by instance()
    private lateinit var binding: FavouritesFragmentBinding
    private var viewModel: FavouritesViewModel? = null
    private lateinit var favouritesAdapter: FavouritesAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this, factory).get(FavouritesViewModel::class.java)
        binding = FavouritesFragmentBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewmodel = viewModel
        }


        viewModel?.startPostsActivity?.observe(viewLifecycleOwner, Observer {
            val data = it.peekContent()
            startPostsActivity(data.board, data.thread)
        })
        return binding.root
    }


    override fun onResume() {
        super.onResume()
        viewModel?.loadFavourites()
        initFavouritesList()
    }


    private fun startPostsActivity(board: String, thread: String) {
        startActivity(
            Intent(requireContext(), PostsActivity::class.java)
                .putExtra(THREAD_NUM, thread)
                .putExtra(BOARD_NAME, board)
        )
    }

    private fun initFavouritesList() {
        viewModel?.threads?.observe(viewLifecycleOwner, Observer {

            favouritesAdapter.submitList(it)
            binding.root.favourites_list.scheduleLayoutAnimation()
        })
        favouritesAdapter = FavouritesAdapter(viewModel)
        binding.root.favourites_list.adapter = favouritesAdapter


    }
}
