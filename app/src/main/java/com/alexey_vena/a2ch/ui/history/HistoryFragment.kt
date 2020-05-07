package com.alexey_vena.a2ch.ui.history

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.alexey_vena.a2ch.R
import com.alexey_vena.a2ch.adapters.HistoryListAdapter
import com.alexey_vena.a2ch.databinding.HistoryFragmentBinding
import com.alexey_vena.a2ch.ui.posts.PostsActivity
import com.alexey_vena.a2ch.util.BOARD_NAME
import com.alexey_vena.a2ch.util.THREAD_NUM
import com.alexey_vena.a2ch.util.log
import kotlinx.android.synthetic.main.history_fragment.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class HistoryFragment : Fragment(), KodeinAware {
    override val kodein by kodein()
    private val factory: HistoryViewModelFactory by instance()
    private lateinit var binding: HistoryFragmentBinding
    private  var historyListAdapter: HistoryListAdapter? = null
    private  var viewModel: HistoryViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this, factory).get(HistoryViewModel::class.java)
        binding = HistoryFragmentBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
            lifecycleOwner = this@HistoryFragment.viewLifecycleOwner
        }
        initHistoryList()
        initObservers()
        return binding.root
    }

    fun loadHistory(){
        viewModel?.loadHistory()
        initHistoryList()
    }

    private fun initHistoryList() {
        viewModel?.threads?.observe(viewLifecycleOwner, Observer {
            historyListAdapter?.submitList(it)
            history_list.scheduleLayoutAnimation()
        })
        historyListAdapter = HistoryListAdapter(viewModel)
        binding.root.findViewById<RecyclerView>(R.id.history_list).adapter = historyListAdapter
    }

    private fun initObservers() {

        viewModel?.startPostsActivity?.observe(viewLifecycleOwner, Observer {
            val data = it.peekContent()
            startPostsActivity(data.board, data.thread)
        })
    }




    private fun startPostsActivity(board: String, thread: String){
        startActivity(
            Intent(requireContext(), PostsActivity::class.java)
                .putExtra(THREAD_NUM, thread)
                .putExtra(BOARD_NAME, board)
        )
    }


}
