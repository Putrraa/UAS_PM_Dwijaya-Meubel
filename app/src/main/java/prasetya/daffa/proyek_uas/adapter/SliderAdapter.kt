package prasetya.daffa.proyek_uas.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import prasetya.daffa.proyek_uas.databinding.ItemSliderBinding

class SliderAdapter(
    private val images: List<Int>
) : RecyclerView.Adapter<SliderAdapter.SliderViewHolder>() {

    inner class SliderViewHolder(val b: ItemSliderBinding) :
        RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        val binding = ItemSliderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SliderViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return images.size
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        holder.b.imgSlider.setImageResource(images[position])
    }
}