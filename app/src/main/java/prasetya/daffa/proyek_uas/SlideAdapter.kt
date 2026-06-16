package prasetya.daffa.proyek_uas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class SlideAdapter(private val images: List<Int>) :
    RecyclerView.Adapter<SlideAdapter.SlideViewHolder>() {

    private val MULTIPLIER = 500

    inner class SlideViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgSlide: ImageView = view.findViewById(R.id.imgSlide)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlideViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_slide, parent, false)
        return SlideViewHolder(view)
    }

    override fun onBindViewHolder(holder: SlideViewHolder, position: Int) {
        holder.imgSlide.setImageResource(images[position % images.size])
    }

    override fun getItemCount(): Int = if (images.isEmpty()) 0 else images.size * MULTIPLIER

    fun getRealCount() = images.size

    fun getStartPosition() = images.size * (MULTIPLIER / 2)
}