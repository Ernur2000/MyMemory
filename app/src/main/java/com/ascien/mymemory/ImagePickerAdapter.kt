package com.ascien.mymemory

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ascien.mymemory.databinding.CardImageBinding
import com.ascien.mymemory.models.BoardSize
import kotlin.math.min

class ImagePickerAdapter(
    private val context: Context,
    private val imageUris: List<Uri>,
    private val boardSize: BoardSize
    ) : RecyclerView.Adapter<ImagePickerAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CardImageBinding.inflate(LayoutInflater.from(context),parent,false)
        val cardWidth = parent.width / boardSize.getWidth()
        val cardHeight = parent.height / boardSize.getHeight()
        val cardSideLength = min(cardWidth,cardHeight)
        val layoutParams = binding.ivCustomImage.layoutParams
        layoutParams.width = cardSideLength
        layoutParams.height = cardSideLength
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(position < imageUris.size){
            holder.bind(imageUris[position])
        }else{
            holder.bind()
        }
    }

    override fun getItemCount() = boardSize.getNumPairs()


    inner class ViewHolder(binding: CardImageBinding) : RecyclerView.ViewHolder(binding.root){
        private val ivCustomImage = binding.ivCustomImage
        fun bind(uri: Uri){
            ivCustomImage.setImageURI(uri)
            ivCustomImage.setOnClickListener(null)
        }
        fun bind(){
            ivCustomImage.setOnClickListener {
                // launch intent for user to select photos
                //Log.i("asdasd","Clicked on $position")
            }
        }
    }
}
