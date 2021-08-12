package com.example.dnzfind.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dnzfind.data.Dnzr
import com.example.dnzfind.databinding.DnzrItemBinding

class DnzrAdapter:
    ListAdapter<Dnzr, DnzrAdapter.DnzrViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<Dnzr>() {
        override fun areItemsTheSame(oldItem: Dnzr, newItem: Dnzr): Boolean {
            return oldItem.userName == newItem.userName
        }

        override fun areContentsTheSame(oldItem: Dnzr, newItem: Dnzr): Boolean {
            return oldItem.styles == newItem.styles
        }
    }

    class DnzrViewHolder(private var binding: DnzrItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(dnzr: Dnzr) {
            binding.dnzrName.text = dnzr.userName
            binding.dnzrDances.text = dnzr.styles.toString()
            //TODO: Add onclick listener for chat button
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DnzrViewHolder {
        val view = DnzrItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DnzrViewHolder(view)
    }

    override fun onBindViewHolder(holder: DnzrViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


}