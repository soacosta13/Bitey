package com.example.bitey.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bitey.data.model.MealSummary
import com.example.bitey.databinding.ItemMealBinding
import com.squareup.picasso.Picasso
import com.example.bitey.R

/*meal summaries without database*/
class MealAdapter(
    private val onFavoriteClick: (MealSummary) -> Unit,
    private val onMealClick: (MealSummary) -> Unit
) : ListAdapter<MealSummary, MealAdapter.MealViewHolder>(DiffCallback)
{
    inner class MealViewHolder(
        private val binding: ItemMealBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.btnFavorite.setOnClickListener {
                val position = bindingAdapterPosition /*gets current pos of the element*/

                if (position != RecyclerView.NO_POSITION){  /*checks if the element still has a valid pos*/
                    onFavoriteClick(getItem(position))
                }
            }

            binding.root.setOnClickListener {
                val position = bindingAdapterPosition

                if (position != RecyclerView.NO_POSITION) {
                    onMealClick(getItem(position))
                }
            }

        }

        fun bind(meal: MealSummary) {
            binding.tvMealName.text = meal.name

            binding.btnFavorite.setImageResource(
                if (meal.isFavorite) {
                    R.drawable.ic_star_filled
                } else {
                    R.drawable.ic_star_outline
                }
            )

            binding.btnFavorite.contentDescription =
                binding.root.context.getString(
                    if (meal.isFavorite) {
                        R.string.cd_remove_favorite
                    } else {
                        R.string.cd_add_favorite
                    }
                )

            if (meal.thumbnail.isBlank()) {
                Picasso.get().cancelRequest(binding.ivMeal)
                binding.ivMeal.setImageResource(android.R.drawable.ic_menu_gallery)
            } else {
                Picasso.get()
                    .load(meal.thumbnail)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(binding.ivMeal)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val binding = ItemMealBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MealViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private object DiffCallback : DiffUtil.ItemCallback<MealSummary>() {
        override fun areItemsTheSame(oldItem: MealSummary, newItem: MealSummary): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: MealSummary, newItem: MealSummary): Boolean =
            oldItem == newItem
    }
}
