package com.example.a2ch.ui.boards

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.a2ch.adapters.BoardListAdapter
import com.example.a2ch.databinding.BoardsFragmentBinding
import com.example.a2ch.ui.threads.ThreadsActivity
import com.example.a2ch.util.BOARD_NAME
import com.example.a2ch.util.toast
import kotlinx.android.synthetic.main.boards_fragment.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance


class BoardsFragment : Fragment(), KodeinAware {
    override val kodein by kodein()
    private val factory by instance<BoardsViewModelFactory>()

    private lateinit var viewModel: BoardsViewModel
    private lateinit var binding: BoardsFragmentBinding
    lateinit var boardListAdapter: BoardListAdapter


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


    private fun initObservers() {
        viewModel.startCategory.observe(viewLifecycleOwner, Observer {
            val name = it.peekContent()
            startActivity(
                Intent(requireContext(), ThreadsActivity::class.java).putExtra(
                    BOARD_NAME, name
                )
            )
        })
        viewModel.error.observe(viewLifecycleOwner, Observer {
            if(it!=null){
                requireContext().toast(it.peekContent().msg)
            }

        })
        viewModel.boards.observe(viewLifecycleOwner, Observer {
            boardListAdapter.updateList(it)
        })
    }



    private fun initList() {
        boardListAdapter = BoardListAdapter(viewModel)
        board_list.adapter = boardListAdapter
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        initList()
    }


    fun filter(text: String){
        boardListAdapter.filter.filter(text)
    }

}
