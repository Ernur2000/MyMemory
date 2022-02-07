package com.ascien.mymemory

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.ascien.mymemory.databinding.ActivityMainBinding
import com.ascien.mymemory.models.BoardSize
import com.ascien.mymemory.models.MemoryGame
import com.ascien.mymemory.utils.EXTRA_BOARD_SIZE
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    companion object{
        private const val TAG = "MainActivity"
        private const val CREATE_REQUEST_CODE = 777
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var memoryGame: MemoryGame
    private lateinit var adapter: MemoryBoardAdapter
    private var boardSize: BoardSize = BoardSize.Medium
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val intent = Intent(this,CreateActivity::class.java)
        intent.putExtra(EXTRA_BOARD_SIZE,BoardSize.Medium)
        startActivity(intent)
        setupBoard()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.mi_refresh->{
                if(memoryGame.getNumMoves() > 0 && !memoryGame.haveWonGame()){
                    showAlertDialog("Quit you current game?", null,View.OnClickListener {
                        setupBoard()
                    })
                }else {
                    setupBoard()
                }
                return true
            }
            R.id.mi_new_size->{
                showNewSizeSialog()
                return true
            }
            R.id.mi_custom->{
                showCreationDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCreationDialog() {
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size,null)
        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)
        showAlertDialog("Create your own memory board",boardSizeView,View.OnClickListener {
            // Set a new value for the board size
            val desiredBoardSize = when (radioGroupSize.checkedRadioButtonId){
                R.id.rbEasy -> BoardSize.Easy
                R.id.rbMedium -> BoardSize.Medium
                else -> BoardSize.Hard
            }
            // Navigate to a new activity
//            val getResult =
//                registerForActivityResult(
//                    ActivityResultContracts.StartActivityForResult()
//                ) {
//                    if (it.resultCode == Activity.RESULT_OK) {
//                        val value = it.data?.getStringExtra("input")
//                    }
//                }
//
//            getResult.launch(intent)
            val intent = Intent(this,CreateActivity::class.java)
            intent.putExtra(EXTRA_BOARD_SIZE,desiredBoardSize)
            startActivityForResult(intent, CREATE_REQUEST_CODE)

        })
    }

    private fun showNewSizeSialog() {
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size,null)
        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)
        when (boardSize){
            BoardSize.Easy -> radioGroupSize.check(R.id.rbEasy)
            BoardSize.Medium ->radioGroupSize.check(R.id.rbMedium)
            BoardSize.Hard -> radioGroupSize.check(R.id.rbHard)
        }
        showAlertDialog("Choose new size",boardSizeView,View.OnClickListener {
            // Set a new value for the board size
            boardSize = when (radioGroupSize.checkedRadioButtonId){
                R.id.rbEasy -> BoardSize.Easy
                R.id.rbMedium -> BoardSize.Medium
                else -> BoardSize.Hard
            }
            setupBoard()
        })
    }

    private fun showAlertDialog(title: String,view: View?,positiveClickListener:View.OnClickListener) {
        AlertDialog.Builder(this)
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(view)
            .setNegativeButton("Cancel",null)
            .setPositiveButton("OK"){_,_->
                positiveClickListener.onClick(null)
            }.show()
    }

    private fun setupBoard() {
        when(boardSize){
            BoardSize.Easy -> {
                binding.tvNumMoves.text = getString(R.string.easy_size)
                binding.tvNumPairs.text = getString(R.string.easy_pairs)
            }
            BoardSize.Medium -> {
                binding.tvNumMoves.text = getString(R.string.medium_size)
                binding.tvNumPairs.text = getString(R.string.medium_pairs)
            }
            BoardSize.Hard -> {
                binding.tvNumMoves.text = getString(R.string.hard_size)
                binding.tvNumPairs.text = getString(R.string.hard_pairs)
            }
        }
        binding.tvNumPairs.setTextColor(ContextCompat.getColor(this,R.color.color_progress_none))
        memoryGame = MemoryGame(boardSize)

        adapter = MemoryBoardAdapter(this,boardSize,memoryGame.cards, object: MemoryBoardAdapter.CardClickListener{
            override fun onCardClicked(position: Int) {
                updateGameWithFlip(position)
            }

        })
        binding.rvBoard.adapter = adapter
        binding.rvBoard.setHasFixedSize(true)
        binding.rvBoard.layoutManager = GridLayoutManager(this,boardSize.getWidth())
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateGameWithFlip(position: Int) {
        // Error checking
        if (memoryGame.haveWonGame()){
            // Alert the user of an invalid move
                Snackbar.make(binding.clRoot, "You already won!", Snackbar.LENGTH_LONG).show()
            return
        }
        if(memoryGame.isCardFaceUp(position)){
            // Alert the user of an invalid move
            Snackbar.make(binding.clRoot, "Invalid move!", Snackbar.LENGTH_SHORT).show()
            return
        }
        // Actually flip over the card
        if(memoryGame.flipCard(position)){
            Log.i(TAG,"Found a match! Num pairs found: ${memoryGame.numPairsFound}")
            val color = ArgbEvaluator().evaluate(
                memoryGame.numPairsFound.toFloat() / boardSize.getNumPairs(),
                ContextCompat.getColor(this,R.color.color_progress_none),
                ContextCompat.getColor(this,R.color.color_progress_full),
            ) as Int
            binding.tvNumPairs.setTextColor(color)
            binding.tvNumPairs.text = "Pairs: ${memoryGame.numPairsFound} / ${boardSize.getNumPairs()}"
            if (memoryGame.haveWonGame()){
                Snackbar.make(binding.clRoot,"You won! Congratulations", Snackbar.LENGTH_LONG).show()
            }
        }
        binding.tvNumMoves.text = "Moves: ${memoryGame.getNumMoves()}"
        adapter.notifyDataSetChanged()
    }
}