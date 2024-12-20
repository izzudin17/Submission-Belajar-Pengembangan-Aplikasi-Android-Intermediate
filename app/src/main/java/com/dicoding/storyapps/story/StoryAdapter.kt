package com.dicoding.storyapps.story

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.storyapps.databinding.StoryListItemBinding
import com.dicoding.storyapps.local.StoryModel
import com.dicoding.storyapps.story.StoryDetailActivity.Companion.EXTRAS_STORY

class StoryAdapter : PagingDataAdapter<StoryModel, StoryAdapter.ListViewHolder>(DIFF_CALLBACK) {

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<StoryModel> =
            object : DiffUtil.ItemCallback<StoryModel>() {

                override fun areItemsTheSame(oldItem: StoryModel, newItem: StoryModel): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(oldItem: StoryModel, newItem: StoryModel): Boolean {
                    return oldItem == newItem
                }

            }
    }

    class ListViewHolder(private val binding: StoryListItemBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(story: StoryModel) {
            Glide.with(itemView.context)
                .load(story.photoUrl)
                .into(binding.imageItemStory)
            binding.apply {
                tvItemName.text = story.name
                tvItemDate.text = story.createdAt
                tvItemDescription.text = story.description
            }

            itemView.setOnClickListener {
                val intent = Intent(
                    itemView.context,
                    StoryDetailActivity::class.java
                )
                intent.putExtra(EXTRAS_STORY, story.id)
                itemView.context.startActivity(
                    intent,
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        itemView.context as Activity,
                        Pair(binding.imageItemStory, "photo"),
                        Pair(binding.tvItemName, "name"),
                        Pair(binding.tvItemDescription, "description")
                    ).toBundle()
                )
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val storyBinding = StoryListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(storyBinding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item)
        }
    }

}
