package com.dvach_2ch.a2ch.ui.boards

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.dvach_2ch.a2ch.R
import com.dvach_2ch.a2ch.adapters.BoardListAdapter
import com.dvach_2ch.a2ch.databinding.BoardsFragmentBinding
import com.dvach_2ch.a2ch.ui.threads.ThreadsActivity
import com.dvach_2ch.a2ch.util.BOARD_NAME
import com.dvach_2ch.a2ch.util.log
import com.dvach_2ch.a2ch.util.toast
import com.dvach_2ch.a2ch.views.RecyclerFastScroll
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance


class BoardsFragment : Fragment(), KodeinAware {
    override val kodein by kodein()
    private val factory by instance<BoardsViewModelFactory>()

    private var viewModel: BoardsViewModel? = null
    private lateinit var binding: BoardsFragmentBinding
    private var boardListAdapter: BoardListAdapter? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this, factory).get(BoardsViewModel::class.java)
        binding = BoardsFragmentBinding.inflate(
            inflater, container, false
        )

        initObservers()

        return binding.apply {
            viewmodel = viewModel
        }.root
    }

    fun loadBoards() {
        viewModel?.loadBoards()
    }

    private fun initObservers() {
        viewModel?.startCategory?.observe(viewLifecycleOwner, Observer {
            val name = it.peekContent()
            startActivity(
                Intent(requireContext(), ThreadsActivity::class.java).putExtra(
                    BOARD_NAME, name
                )
            )
        })
        viewModel?.error?.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                requireContext().toast(it.peekContent().msg)
            }

        })
        viewModel?.boards?.observe(viewLifecycleOwner, Observer {
            boardListAdapter?.updateList(it)
        })
    }


    private fun initList() {
        boardListAdapter = BoardListAdapter(viewModel)
        binding.boardList.adapter = boardListAdapter

        val color = TypedValue()
        requireContext().theme.resolveAttribute(R.attr.colorAccent, color, true)
        RecyclerFastScroll(binding.boardList, color.data, color.data)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        initList()
    }


    fun filter(text: String) {
        boardListAdapter?.filter?.filter(text)
    }

}
